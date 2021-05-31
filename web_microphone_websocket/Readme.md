# Web Microphone Websocket

This is an example of a ReactJS web application streaming microphone audio from the browser
to a NodeJS server and transmitting the DeepSpeech results back to the browser.

## Download the pre-trained model (1.8GB):

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.9.3/deepspeech-0.9.3-models.pbmm
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.9.3/deepspeech-0.9.3-models.scorer
```

## Run example

## Option 1: Run with docker

Edit `docker-compose.yml`, adjust model path with your local model.

```yaml
    volumes: 
    - /path/to/deepspeech-0.9.3-models.pbmm:/deepspeech/deepspeech-0.9.3-models.pbmm:ro
    - /path/to/deepspeech-0.9.3-models.scorer:/deepspeech/deepspeech-0.9.3-models.scorer:ro
```

Run

```
docker-compose up -d
```

When container is ready, visit http://localhost:3000 and have fun.

## Option 2: Run locally

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