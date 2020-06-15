import os ,deques,math,strutils,parseopt,tables
import strformat
import webrtcvad,portaudio,deepspeech,wav


proc sum[T](temp: Deque[T]): int = 
    for i in 0..<len(temp):
        result = result + temp[i].flag
var 
    args = initTable[string, string]()
    saveWav = false
for kind,key,value in getopt():
    if key.toLower() == "savewav":
        saveWav = true
    else:
        args.add(key,value)
        


#doAssert args.len == 2,"Incorrect commandLine params ,Please check again"
doAssert "model" in args  #to run without external scorer.
const
    frameDuration = 20 #in milliseconds.
    sampleRate = 16000
    nChannels = 1
    windowSize = 12
var
    left:int
    curr:int
    tt: int
    buff = initDeque[tuple[data: seq[int16],flag:int32]](nextPowerOfTwo(windowSize))
    len: int
    audioData = newSeq[int16](int((frameDuration*sampleRate)/1000))
    frame: seq[int16]
    vad: vadObj
    codeV: cint
    code: cint
    codeD: cint
    triggered = false
    fwav: wavObj
    count = 0
    text: cstring
    scorerPath: string
let
    #data sharing is being done through FILES(on disk)..This is not the fastest way,being done because was not able to  make portaudio work with --threads:on flag.
    #sometime kernel may not write data to the disk...while loop tries to take care of that..worked every time by now.
    f1 = open("FIFO_rgb",fmWrite)
    f2 = open("FIFO_rgb",fmREAD)
    stream: pointer = nil #portaudio Stream pointer holder.
    modelPtr: ModelState = nil  #deepSpeech model  
    deepStreamPtr: StreamingState = nil  #deepSpeech model stream
    modelPath = args["model"]
if "scorer" in args:
    scorerPath = args["scorer"]

    
#CallBack function scheduled by OS...executed when requested raw audio data is available.
proc simpleCB(inpBuff: pointer,outBuff: pointer,framesPerBuffer: culong,timeInfo: ptr PaStreamCallbackTimeInfo ,statusFlags: PaStreamCallbackFlags,userData: pointer): PaStreamCallbackResult {.cdecl.}= 
    #writing to a file on disk.
    discard f1.writeBuffer(inpBuff,int(framesPerBuffer)*sizeof(int16))
    f1.flushFile()
    return paContinue

when isMainModule:
    codeV = initVad(vad)
    if codeV== 0'i32:
        echo("vad Initialized")
    codeV = setMode(vad,3'i32)
    assert codeV == 0'i32

    #DeepSpeech model initialization.
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
        echo("No external scorer used.")
    ###################

    #initialize the stream
    code = initPortAudio()
    echo(code," in Initializing the stream")

    #Create the defaultStream portaudio
    #TODO:  Making sure that given sampleRate is supported by chosen inputDevice.
    code  = defaultStream(unsafeAddr(stream),1,0,paInt16,sampleRate,culong(len(audioData)),simpleCB ,nil)
    echo(code," in opening the default stream")

    #start the portaudio stream.
    code = startStream(stream)
    echo(code," in starting the stream with default parameters")

    while true:
        #################################################################################################3
        left = len(audioData)*sizeof(int16)
        tt = 0 
        while true:
            curr = f2.readBuffer(cast[pointer](cast[int](addr(audioData[0]))+ tt),left)
            tt = tt + curr
            left = left - curr
            if (left > 0):
                continue
            else:
                frame = audioData 
                break
            
        codeV = vad.isSpeech(frame,sampleRate)
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
            
