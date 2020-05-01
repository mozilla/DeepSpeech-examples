
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
                               [-w SAVEWAV] [-f FILE] -m MODEL [-s SCORER]
                               [-d DEVICE] [-r RATE]
   
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
     -f FILE, --file FILE  Read from .wav file instead of microphone
     -m MODEL, --model MODEL
                           Path to the model (protocol buffer binary file, or
                           entire directory containing all standard-named files
                           for model)
     -s SCORER, --scorer SCORER
                           Path to the external scorer file. Default:
                           kenlm.scorer
     -d DEVICE, --device DEVICE
                           Device input index (Int) as listed by
                           pyaudio.PyAudio.get_device_info_by_index(). If not
                           provided, falls back to PyAudio.get_default_device().
     -r RATE, --rate RATE  Input device sample rate. Default: 16000. Your device
