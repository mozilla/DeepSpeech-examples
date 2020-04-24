#!/bin/bash

set -xe

THIS=$(dirname "$0")

pushd ${THIS}
  source ../tests.sh

  npm install $(get_npm_package_url)
  npm install

  ln -s $HOME/DeepSpeech/models deepspeech-0.7.0-models

  yarn run test:client
  yarn run test:server

popd
