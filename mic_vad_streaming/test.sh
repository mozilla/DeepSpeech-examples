#!/bin/bash

set -xe

THIS=$(dirname "$0")

pushd ${THIS}
  source ../tests.sh

  pip install --user $(get_python_wheel_url "$1")
  pip install --user -r <(grep -v deepspeech requirements.txt)

  pulseaudio &

  python mic_vad_streaming.py \
	  --model $HOME/DeepSpeech/models/deepspeech-0.9.3-models.pbmm \
	  --scorer $HOME/DeepSpeech/models/deepspeech-0.9.3-models.scorer \
	  --file $HOME/DeepSpeech/audio/2830-3980-0043.wav
popd
