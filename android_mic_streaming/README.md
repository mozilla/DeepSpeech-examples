# Android Microphone Streaming

Android demo application that streams audio from the microphone to deepspeech and transcribes it.

## Prerequisites

#### Download model

[Open the Deepspeech 0.9.3 release page](https://github.com/mozilla/DeepSpeech/releases/tag/v0.9.3)
using your device's web browser.

Download the pre-trained English model files:

- `deepspeech-0.9.3-models.tflite`
- `deepspeech-0.9.3-models.scorer`

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

Launch the app from the home screen.
It will start recording and transcribing the spoken text automatically.

Deepspeech can also be used for daily typing.
Enable it in Settings > System > Language & input > Virtual keyboard > Manage keyboards,
and switch to it by long-pressing the spacebar.
