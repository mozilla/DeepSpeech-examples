# NodeJS Microphone VAD Streaming

This is a NodeJS example of recording from the microphone and streaming to
DeepSpeech with voice activity detection.

### Prerequisites:

This has been tested with Node LTS v14.14.

#### Windows/OSX

1. Sox - Binary available for [download here](https://sourceforge.net/projects/sox/files/sox/)
1. [mic](https://github.com/ashishbajaj99/mic) NPM module
1. The pre-trained DeepSpeech english model and scorer(1089MB)

```
brew install sox # OSX only
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models.pbmm
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models.scorer
```

#### Linux

1. [arecord](http://alsa-project.org/)
1. The library **libasound2-dev**
1. [mic](https://github.com/ashishbajaj99/mic) NPM module - which requires [sox](http://sox.sourceforge.net/)
1. The pre-trained DeepSpeech english model and scorer(1089MB)

```
sudo apt-get install libasound2-dev alsa-utils
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models.pbmm
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models.scorer
```

### Install:

Change directory to the same directory as this Readme then:

```
npm install
```

### Run NodeJS server:

```
node start.js
```

### Specify alternate DeepSpeech model path:

Use the `DEEPSPEECH_MODEL` environment variable to change models.

```
DEEPSPEECH_MODEL=~/dev/jaxcore/deepspeech-0.8.0-models/ node start.js
```

### Troubleshooting

#### OSX Mic Input
If no mic input is detected while running node from a terminal, this is most like due to a lack of security permissions.
If you do not see a OSX UI prompt asking for permission to allow iTerm or Terminal for permission to access the Microphone, this popup may have been dismissed in the past. As it only occurs once, the following command resets the prompts for the microphone:

```bash
tccutil reset Microphone
```

#### Additional Debug information

Addtional Debug information can be show with the following command:

```bash
NODE_DEBUG=cluster,net,http,fs,tls,module,timers node start.js
```
