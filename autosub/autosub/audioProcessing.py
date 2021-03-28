#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess
import numpy as np


def extract_audio(input_file, audio_file_name):
    """Extract audio from input video file and save to audio/ in root dir

    Args:
        input_file: input video file
        audio_file_name: save audio WAV file with same filename as video file
    """
    
    command = "ffmpeg -hide_banner -loglevel warning -i {} -b:a 192k -ac 1 -ar 16000 -vn {}".format(input_file, audio_file_name)
    try:
        ret = subprocess.call(command, shell=True)
        print("Extracted audio to audio/{}".format(audio_file_name.split("/")[-1]))
    except Exception as e:
        print("Error: ", str(e))
        exit(1)


def convert_samplerate(audio_path, desired_sample_rate):
    """Convert extracted audio to the format expected by DeepSpeech
    ***WONT be called as extract_audio() converts the audio to 16kHz while saving***
    
    Args:
        audio_path: audio file path
        desired_sample_rate: DeepSpeech expects 16kHz 

    Returns:
        numpy buffer: audio signal stored in numpy array
    """
    
    sox_cmd = "sox {} --type raw --bits 16 --channels 1 --rate {} --encoding signed-integer --endian little --compression 0.0 --no-dither - ".format(
        quote(audio_path), desired_sample_rate)
    try:
        output = subprocess.check_output(
            shlex.split(sox_cmd), stderr=subprocess.PIPE)
    except subprocess.CalledProcessError as e:
        raise RuntimeError("SoX returned non-zero status: {}".format(e.stderr))
    except OSError as e:
        raise OSError(e.errno, "SoX not found, use {}hz files or install it: {}".format(
            desired_sample_rate, e.strerror))

    return np.frombuffer(output, np.int16)