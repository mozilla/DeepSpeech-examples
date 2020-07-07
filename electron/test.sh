#!/bin/bash

set -xe

THIS=$(dirname "$0")

pushd ${THIS}
  source ../tests.sh

  npm install $(get_npm_package_url)
  npm install
  npm run rebuild

  ln -s $HOME/DeepSpeech/models models
  ln -s ~/DeepSpeech/audio ./public/

  npm run dev-test
popd
