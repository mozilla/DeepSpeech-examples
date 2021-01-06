# NodeJS voice recognition example using Mozilla DeepSpeech

Download the pre-trained model (1.8GB):

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.9.3/deepspeech-0.9.3-models.pbmm
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.9.3/deepspeech-0.9.3-models.scorer
```

Edit references to models path if necessary:

```
let modelPath = './models/deepspeech-0.9.3-models.pbmm';
let scorerPath = './models/deepspeech-0.9.3-models.scorer';
```

Install Sox (for .wav file loading):

```
brew install sox
```

Download test audio files:

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.9.3/audio-0.9.3.tar.gz
tar xfvz audio-0.9.3.tar.gz
```

Install NPM dependencies:

```
npm install
```

Run:

```
node index.js
```

Result should be something like:

```
audio length 1.975
result: experience proves this

```

Try other wav files with an argument:

```
node index.js audio/2830-3980-0043.wav
node index.js audio/8455-210777-0068.wav
node index.js audio/4507-16021-0012.wav
```

