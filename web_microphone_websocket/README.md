# Web Microphone Websocket

This is an example of a ReactJS web application streaming microphone audio from the browser
to a NodeJS server and transmitting the DeepSpeech results back to the browser.

#### Download the pre-trained model (1.8GB):

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.6.0/deepspeech-0.6.0-models.tar.gz
tar xvfz deepspeech-0.6.0-models.tar.gz
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