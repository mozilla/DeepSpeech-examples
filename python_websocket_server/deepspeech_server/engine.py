"""Module containing speech-to-text transcription functionality"""

import wave
from io import BytesIO

import ffmpeg
import numpy as np
from deepspeech import Model


def normalize_audio(audio):
    """Normalize the audio into the format required by DeepSpeech"""
    out, err = (
        ffmpeg.input("pipe:0")
        .output(
            "pipe:1",
            f="WAV",
            acodec="pcm_s16le",
            ac=1,
            ar="16k",
            loglevel="error",
            hide_banner=None,
        )
        .run(input=audio, capture_stdout=True, capture_stderr=True)
    )
    if err:
        raise Exception(err)
    return out


class SpeechToTextEngine:
    """Class to perform speech-to-text transcription and related functionality"""
    def __init__(self, model_path, scorer_path):
        self.model = Model(model_path=model_path)
        self.model.enableExternalScorer(scorer_path=scorer_path)

    def run(self, audio):
        """Perform speech-to-text transcription"""
        audio = normalize_audio(audio)
        audio = BytesIO(audio)
        with wave.Wave_read(audio) as wav:
            audio = np.frombuffer(wav.readframes(wav.getnframes()), np.int16)
        result = self.model.stt(audio_buffer=audio)
        return result
