#!/bin/bash

set -xe

THIS=$(dirname "$0")
OS=$(uname)

pushd ${THIS}
  source ../tests.sh

  if [ "${OS}" = "Linux" ]; then
    export DISPLAY=':99.0'
    Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 &
    xvfb_process=$!
  fi

  npm install $(get_npm_package_url)
  npm install
  npm run rebuild

  if [ -f "${THIS}/node_modules/electron/dist/chrome-sandbox" ]; then
    export ELECTRON_DISABLE_SANDBOX=1
  fi;

  ln -s $HOME/DeepSpeech/models models
  ln -s ~/DeepSpeech/audio ./public/

  export CI=true

  npm run dev-test

  if [ "${OS}" = "Linux" ]; then
    sleep 1
    kill -9 ${xvfb_process} || true
  fi
popd
