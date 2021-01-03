# Web Microphone Websocket

This is an example of a ReactJS web application streaming microphone audio from the browser
to a NodeJS server and transmitting the DeepSpeech results back to the browser.

#### Download the pre-trained model (1.8GB):

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/output_graph.pbmm
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/pruned_lm.scorer
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