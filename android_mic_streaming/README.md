# Android Microphone Streaming

Android demo application that streams audio from the microphone to deepspeech and transcribes it.

## Prerequisites

#### Download model

Download the pre-trained English model and extract it:
```
curl -LO https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models.tflite
curl -LO https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models.scorer
```

Move the model files `deepspeech-0.8.0-models.pbmm`, `deepspeech-0.8.0-models.scorer`, to the demo application's data directory on your android device.
Mind that the data directory will only be present after installing and launching the app once.

```
adb push deepspeech-0.8.0-models.tflite deepspeech-0.8.0-models.scorer /storage/emulated/0/Android/data/org.deepspeechdemo/files/
```

You can also copy the files from your file browser to the device.

#### Android device with USB Debugging

Connect an android device and make sure to enable USB-Debugging in the developer settings of the device. If haven't already, you can activate your developer settings by following [this guide from android](https://developer.android.com/studio/debug/dev-options#enable).

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

Start recording by pressing the button and the app will transcribe the spoken text.

## Fine-tuning the Recognition

Based on your use case or the language you are using you might change the values of `BEAM_WIDTH`, `LM_ALPHA` and `LM_BETA` to improve the speech recogintion. 

You can also alter the `NUM_BUFFER_ELEMENTS` to change the size of the audio data buffer that is fed into the model. 