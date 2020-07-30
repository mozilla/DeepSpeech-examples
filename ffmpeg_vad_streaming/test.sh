#!/bin/bash

set -xe

THIS=$(dirname "$0")

pushd ${THIS}
  source ../tests.sh

  npm install $(get_npm_package_url)
  npm install

  node ./index.js --audio $HOME/DeepSpeech/audio/2830-3980-0043.wav \
                  --scorer $HOME/DeepSpeech/models/deepspeech-0.8.0-models.scorer \
                  --model $HOME/DeepSpeech/models/deepspeech-0.8.0-models.pbmm

  node ./index.js --audio $HOME/DeepSpeech/audio/4507-16021-0012.wav \
                  --scorer $HOME/DeepSpeech/models/deepspeech-0.8.0-models.scorer \
                  --model $HOME/DeepSpeech/models/deepspeech-0.8.0-models.pbmm

  node ./index.js --audio $HOME/DeepSpeech/audio/8455-210777-0068.wav \
                  --scorer $HOME/DeepSpeech/models/deepspeech-0.8.0-models.scorer \
                  --model $HOME/DeepSpeech/models/deepspeech-0.8.0-models.pbmm
popd
