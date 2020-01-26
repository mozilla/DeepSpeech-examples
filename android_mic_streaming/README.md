# Android Microphone Streaming

Android demo application that streams audio from the microphone to deepspeech and transcribes it.

## Prerequisites

#### Download model

Download the pre-trained English model and extract it:
```
curl -LO https://github.com/mozilla/DeepSpeech/releases/download/v0.6.1/deepspeech-0.6.1-models.tar.gz
tar xvf deepspeech-0.6.1-models.tar.gz
```

#### Android device

Connect an android device and move a folder (e.g. named `deepspeech`) containing the model files `output_graph.tflite`, `lm.binary`, `trie` to a location of your choice on the device (for example `/sdcard/`).

#### USB Debugging

Make sure to enable USB-Debugging in the developer settings of your connected android device. If haven't already, you can activate your developer settings by following [this guide from android](https://developer.android.com/studio/debug/dev-options#enable).

## Installation

To install the example app on your connected android device you can either use the command line or Android Studio.

### Command Line

```
cd android_mic_streaming
./gradlew installDebug
``` 

### Android Studio

Open the `android_mic_streaming` directory in Android Studio.  
Run the app and your connected android device.

## Usage

In the app, specify the path to the models directory (e.g. `/sdcard/deepspeech`).  
Start recording and the app will transcribe the spoken text.

## Fine-tuning the Recognition

Based on your use case or the language you are using you might change the values of `BEAM_WIDTH`, `LM_ALPHA` and `LM_BETA` to improve the speech recogintion. 

You can also alter the `NUM_BUFFER_ELEMENTS` to change the size of the audio data buffer that is fed into the model. 