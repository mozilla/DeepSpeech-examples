#!/bin/bash

set -xe

THIS=$(dirname "$0")

pushd ${THIS}
  source ../tests.sh

  npm install $(get_npm_package_url)
  npm install

  DEEPSPEECH_MODEL=$HOME/DeepSpeech/models node ./start.js $HOME/DeepSpeech/audio/2830-3980-0043.wav

popd
