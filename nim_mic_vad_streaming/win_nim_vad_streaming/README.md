# MICROPHONE VAD STREAMING
Minimalistic example to demonstrate the DeepSpeech streaming  API in NIM.Raw audio is streamed from microphone to the DeepSpeech based on VAD (voice Activity Detection).

## Prerequisites:
0) Please read ``PREREQUISITES`` in [README](../README.md)  for getting the required ``libdeepspeech.so`` shared library.
1) This example depends on the ``libportaudio.dll``(precompiled portaudio library).Make sure you have this library  in PATH.If you don't have one or are unable to build one ,you can get one from [here](https://gitlab.com/eagledot/nim-portaudio/lib).

2) Download the pre-trained DeepSpeech english model (1089MB):

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.7.0/deepspeech-0.7.0-models.pbmm
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.7.0/deepspeech-0.7.0-models.scorer
```


## Installation

1. Install Nim bindings for deespeech version-0.7.0 . 
```nim
nimble install https://gitlab.com/eagledot/nim-deepspeech@0.7.0
```

2. Install Nim bindings for portudio which is  needed for microphone access.

```nim
nimble install https://gitlab.com/eagledot/nim-portaudio
```

3. Install Webrtcvad library for Voice Activity Detection(VAD engine).
```nim
nimble install webrtcvad
```  
OR

```nim
nimble install https://gitlab.com/eagledot/nim-webrtcvad
```

4. Install Wav library for reading and writing .wav files.

_Note: This lib is optional and is needed only for saving recorded audio as ``.wav`` files._
```nim
nimble install https://gitlab.com/eagledot/nim-wav
```


## BUILD:
*  Build the executable/binary as such:
```nim
nim c -f -d:release vad_stream.nim
```

## Usage:
* Using ``--saveWav`` flag is optional ,it will save the recorded audio as `.wav` files. 
``` nim 
./vad_stream.exe --model:<path/to/pretrained/model.pbmm>  --scorer:<path/to/.scorer>  --saveWav
```





