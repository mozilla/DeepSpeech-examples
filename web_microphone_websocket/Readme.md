# Web Microphone Websocket

This is an example of a ReactJS web application streaming microphone audio from the browser
to a NodeJS server and transmitting the Mozilla Voice STT results back to the browser.

#### Download the pre-trained model (1.8GB):

```
wget https://github.com/mozilla/STT/releases/download/v0.8.0/deepspeech-0.8.0-models.pbmm
wget https://github.com/mozilla/STT/releases/download/v0.8.0/deepspeech-0.8.0-models.scorer
```

#### Install:

```
yarn install
```

#### Run ReactJS Client:

```
yarn start
```

#### Run NodeJS Server (in a separate terminal window):

```
node server.js
```
