# NodeJS Microphone VAD Streaming

This is a NodeJS example of recording from the microphone and streaming to
DeepSpeech with voice activity detection.

### Prerequisites:

1) The example utilized the [mic](https://github.com/ashishbajaj99/mic) NPM module which requires
either [sox](http://sox.sourceforge.net/) (Windows/Mac) or [arecord](http://alsa-project.org/) (Linux).

2) Download the pre-trained DeepSpeech english model (1089MB):

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.7.0/deepspeech-0.7.0-models.pbmm
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.7.0/deepspeech-0.7.0-models.scorer
```

#### Dependency

Is needed the library **libasound2-dev**

```
$ sudo apt-get install libasound2-dev
```

#### Install:

```
npm install
```

#### Run NodeJS server:

```
node start.js
```

#### Specify alternate DeepSpeech model path:

Use the `DEEPSPEECH_MODEL` environment variable to change models.

```
DEEPSPEECH_MODEL=~/dev/jaxcore/deepspeech-0.7.0-models/ node start.js
```