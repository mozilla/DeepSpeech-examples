## NOTE:

This directory contains two sub-directories one for ``WINDOWS`` OS and one for ``LINUX`` OS.
Read corresponding READMEs for each OS .

Only difference for both OS  is the  library used for gathering audio data from microphone .On WINDOWS ``portaudio`` is used while on LINUX  `ALSA-lib C` is used which itself provides an interface to ALSA Kernel module.

Interface to both the libs is provided through NIM code.

## PREREQUISITIES :
* ```libdeepspeech.so```

Go to the [releases](https://github.com/mozilla/DeepSpeech/releases/tag/v0.7.0) page and download the native client package based on your OS and CPU architecture.

Extract the ``libdeepspeech.so`` and put into the subdirectory depending on OS of native Client used.

#### On WINDOWS:
* Download the ```native.client.amd64.win.tar.xz ``` package .   [  same is true for ``xx.xx.amd64.cuda.win.xx``  if CUDA installed or ``xx.xx.amd64.tflite.win.xx``]
* Extract and place the ```libdeepspeech.so``` in ```win_nim_vad_streaming``` subdirectory
* Now see ``README.md`` in  ```win_nim_vad_streaming``` subdirectory.

#### On LINUX:
* Download the ```native_client.amd64.linux.cpu ``` package .[  same is true for ``xx.xx.amd64.cuda.linux.xx``  is CUDA installed or ``xx.xx.amd64.tflite.linux.xx``]
* Extract and place the ```libdeepspeech.so``` in ```linux_nim_vad_streaming``` subdirectory
* Now see ``README.md`` in  ```linux_nim_vad_streaming``` subdirectory.

_Note: One can put ``libdeepspeech.so`` in the system's PATH rather than copying it to one of subdirectories for easy usage._




## NOTE: 
Used NIM code only depends on the  shared library(``libdeepspeech.so``) used.
Given one has downloaded the native client package and extracted the ``libdeepspeech.so`` shared library and copied it  to one of the subdirectories or in system's PATH ,Code can be modified to add more  functionalities   in pure NIM and modified code would compile on any platform as long as that platform is supported by NIM. 


