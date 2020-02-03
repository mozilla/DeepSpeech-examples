
Microphone VAD Streaming
========================

Stream from microphone to DeepSpeech, using VAD (voice activity detection). A fairly simple example demonstrating the DeepSpeech streaming API in Python. Also useful for quick, real-time testing of models and decoding parameters.

Installation
------------

.. code-block:: bash

   pip install -r requirements.txt

Uses portaudio for microphone access, so on Linux, you may need to install its header files to compile the ``pyaudio`` package:

.. code-block:: bash

   sudo apt install portaudio19-dev

Installation on MacOS may fail due to portaudio, use brew to install it:

.. code-block:: bash

   brew install portaudio

Usage
-----

.. code-block::

   usage: mic_vad_streaming.py [-h] [-v VAD_AGGRESSIVENESS] [--nospinner]
                               [-w SAVEWAV] -m MODEL [-s SCORER]
                               [-nf N_FEATURES] [-nc N_CONTEXT]

   Stream from microphone to DeepSpeech using VAD

   optional arguments:
     -h, --help            show this help message and exit
     -v VAD_AGGRESSIVENESS, --vad_aggressiveness VAD_AGGRESSIVENESS
                           Set aggressiveness of VAD: an integer between 0 and 3,
                           0 being the least aggressive about filtering out non-
                           speech, 3 the most aggressive. Default: 3
     --nospinner           Disable spinner
     -w SAVEWAV, --savewav SAVEWAV
                           Save .wav files of utterences to given directory
     -m MODEL, --model MODEL
                           Path to the model (protocol buffer binary file, or
                           entire directory containing all standard-named files
                           for model)
     -s SCORER, --scorer SCORER
                           Path to the external scorer file. Default: kenlm.scorer
     -nf N_FEATURES, --n_features N_FEATURES
                           Number of MFCC features to use. Default: 26
     -nc N_CONTEXT, --n_context N_CONTEXT
                           Size of the context window used for producing
                           timesteps in the input vector. Default: 9
