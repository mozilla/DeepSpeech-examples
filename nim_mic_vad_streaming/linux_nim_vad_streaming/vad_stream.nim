import os ,deques,math,strutils,parseopt,tables,strformat
import alsa,webrtcvad,wav
import deepspeech

var 
    args = initTable[string, string]()
    saveWav = false
for kind,key,value in getopt():
    if key.toLower() == "savewav":
        saveWav = true
    else:
        args.add(key,value)

doAssert "model" in args  #to run without external scorer.       

#All on the Stack no GC..can be used from another thread except deviceName ..pass it as argument.
const
    rate = 16000'u32
    sampleRate = rate
    kernelBuffer = 8192'u32 #KernelBuffer size for storing micData..must not be overrun.
    nChannels = 1'u32
    format = SND_PCM_FORMAT_S16_LE
    mode = NON_BLOCKING_MODE
    frameDuration = 20  #in milliseconds.
    windowSize = 12
let
    capture_handle: snd_pcm_ref = nil
    hw_params: snd_pcm_hw_params_ref = nil
    device_name = "plughw:0,0"  #PCM hardware alsa Device.
    size = (int((frameDuration*int(rate))/1000))
    modelPtr: ModelState = nil  #deepSpeech model  
    deepStreamPtr: StreamingState = nil  #deepSpeech model stream
    modelPath = args["model"]
    
var
    text:cstring
    err: cint
    count = 0
    dir:cint  
    framesLen: clong
    vad:vadObj  #VAD Object declaration
    codeV: cint  #to hold the error codes for VAD.
    codeD: cint #to hold the error codes for deepSpeech
    #to get the data from the channel.
    frame : seq[int16]
    buff = initDeque[tuple[data: seq[int16],flag:int32]](nextPowerOfTwo(windowSize))
    triggered = false
    fwav: wavObj
    scorerPath:string
if "scorer" in args:
    scorerPath = args["scorer"]

#define a channel to hold the audio data.
var chan: Channel[seq[int16]]


#params->   deviceName:name of device to be opened  ,size:  number of frames to be read in one cycle...NOTE:  FRAMES,NOT BYTES.
proc record(deviceName:string){.thread.} =
    var recordBuff = newSeq[int16](size)  #userSpace buffer to record mic data.
    var framesLen: clong
    err = snd_pcm_open_nim(unsafeAddr(capture_handle),deviceName,SND_PCM_STREAM_CAPTURE,mode)
    doAssert err == 0'i32
    #
    err = snd_pcm_hw_params_malloc_nim(unsafeAddr(hw_params))
    doAssert err == 0'i32
    err = snd_pcm_hw_params_any_nim(capture_handle,hw_params)
    doAssert err == 0'i32
    #set InterLeaved access
    err = snd_pcm_hw_params_set_access_nim(capture_handle,hw_params,SND_PCM_ACCESS_RW_INTERLEAVED)
    doAssert err == 0'i32
    #set format
    err = snd_pcm_hw_params_set_format_nim(capture_handle,hw_params,format)
    doAssert err == 0'i32
    #Set rate
    err = snd_pcm_hw_params_set_rate_nim(capture_handle,hw_params,rate,dir)
    doAssert err == 0'i32
    #   set  nCHannels
    err = snd_pcm_hw_params_set_channels_nim(capture_handle,hw_params,nChannels)
    doAssert err == 0'i32
    err = snd_pcm_hw_params_set_buffer_size_nim(capture_handle,hw_params,kernelBuffer)

    #apply hw_params
    err = snd_pcm_hw_params_nim(capture_handle,hw_params) 
    doAssert err == 0'i32

    echo("hw_params successfully applied..")
    snd_pcm_hw_params_free_nim(hw_params)

    while true:
        framesLen = snd_pcm_readi_nim(capture_handle,addr(recordBuff[0]),culong(size)) #reading 512 samples ..singlechannel,each 2 bytes..hence 1024 bytes.
        assert framesLen == clong(size)
        
            
        chan.send(recordBuff)


#########################################################################################
proc sum[T](temp: Deque[T]): int = 
    for i in 0..<len(temp):
        result = result + temp[i].flag

############################
codeV = initVad(vad)
if codeV== 0'i32:
    echo("vad Initialized")
codeV = setMode(vad,3'i32)
assert codeV == 0'i32
###################################################################333
codeD = createModel(modelPath,unsafeaddr(modelPtr))
if codeD == 0'i32:
    echo("Model Created Successfully")
let beamWidth = getBeamWidth(modelPtr)
echo("Default Beam Width is : ",int(beamWidth))
#enable External Scorer.
if "scorer" in args:
    codeD = enableExternalScorer(modelPtr, scorerPath)
    if codeD == 0'i32:
        echo("External Scorer Enabled.")
else:
    echo("No scorer Used")
###################






chan.open()

var thread: Thread[string]
createThread[string](thread,record,device_name)
echo("Thread Created")
#receive the data from the channel..blocking call.
while true:
    frame = chan.recv()
    codeV = vad.isSpeech(frame,int(rate))
    #echo(audioData[0],"  ",codeV)

    if triggered == false:
    #now check if there is enough voiceActivity based on last `windowSize` samples
        if buff.len < windowSize:
            buff.addLast((frame,codeV))
        else:
            buff.popFirst()
            buff.addLast((frame,codeV))
        #also check the percentage of voiced samples:
        if float(sum(buff))  > float(0.5*float(windowSize)):
            triggered = true

            #START THE DEEP SPEECH STREAM...here.
            codeD = createStream(modelPtr,unsafeAddr(deepStreamPtr))
            echo("TRIGGERED !!!!!!!!!!")
            if saveWav:
                fwav = wavWrite(fmt"chunk-{count:03}.wav",uint32(sampleRate),uint16(nChannels))

            for i in 0..<len(buff):
                if saveWav:
                    fwav.writeChunk(buff[i].data)
                feedAudioContent(deepStreamPtr,cast[ptr cshort](addr(buff[i].data[0])),cuint(len(buff[i].data)))
            buff.clear()

    else:
        if buff.len < windowSize:
            buff.addLast((frame,codeV))
        else:
            buff.popFirst()
            buff.addLast((frame,codeV))
        feedAudioContent(deepStreamPtr,cast[ptr cshort](addr(frame[0])),cuint(len(frame)))    
        if saveWav:
            fwav.writeChunk(frame)
        #check the percentage of unvoiced samples
        if float(buff.len - sum(buff)) > 0.85*float(windowSize):
            #echo("Done")
            triggered = false
            buff.clear()
            text = finishStream(deepStreamPtr)
            if len(text)>0:
                echo("Transcript: ",text)
                freeString(text)
            if saveWav:
                fwav.close()
                echo("Written")
                count = count + 1





#joinThread(thread)
#echo("Thread finished..")