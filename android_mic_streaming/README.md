# Android Microphone Streaming

Android demo application that streams audio from the microphone to deepspeech and transcribes it.

## Usage

- Download the pre-trained English model and extract it:
```
curl -LO https://github.com/mozilla/DeepSpeech/releases/download/v0.6.0/deepspeech-0.6.0-models.tar.gz
tar xvf deepspeech-0.6.0-models.tar.gz
```
- Connect an android device and move a folder (e.g. named `deepspeech`) containing the model files `output_graph.tflite`, `lm.binary`, `trie` to a location of your choice on the device (for example `/sdcard/`).
- Clone the repo.
- Open the `android_mic_streaming` directory in Android Studio.
- Run the app and your device.
- Specify the path to the models directory (e.g. `/sdcard/deepspeech`).
- Start recording and the app will transcribe the spoken text.
