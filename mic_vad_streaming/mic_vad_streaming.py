import os
import os.path
import wave
import pyaudio
import logging

import numpy as np

from halo import Halo
from queue import Queue
from scipy import signal
from webrtcvad import Vad
from deepspeech import Model
from collections import deque
from datetime import datetime


logging.basicConfig(level=20)


class Audio(object):
    """Streams raw audio from microphone. Data is received in a separate thread, and stored in a buffer, to be read from."""

    FORMAT = pyaudio.paInt16
    # Network/VAD rate-space
    RATE_PROCESS = 16000
    CHANNELS = 1
    BLOCKS_PER_SECOND = 50

    def __init__(self, callback=None, device=None, input_rate=RATE_PROCESS, file=None):
        def proxy_callback(in_data, frame_count, time_info, status):
            #pylint: disable=unused-argument
            if self.chunk is not None:
                in_data = self.wf.readframes(self.chunk)
            callback(in_data)
            return None, pyaudio.paContinue
        if callback is None: 
            callback = lambda in_data: self.buffer_queue.put(in_data)
        self.buffer_queue = Queue()
        self.device = device
        self.input_rate = input_rate
        self.sample_rate = self.RATE_PROCESS
        self.block_size = int(self.RATE_PROCESS / float(self.BLOCKS_PER_SECOND))
        self.block_size_input = int(self.input_rate / float(self.BLOCKS_PER_SECOND))
        self.pa = pyaudio.PyAudio()

        kwargs = {
            'format': self.FORMAT,
            'channels': self.CHANNELS,
            'rate': self.input_rate,
            'input': True,
            'frames_per_buffer': self.block_size_input,
            'stream_callback': proxy_callback,
        }

        self.chunk = None
        # if not default device
        if self.device:
            kwargs['input_device_index'] = self.device
        elif file is not None:
            self.chunk = 320
            self.wf = wave.open(file, 'rb')

        self.stream = self.pa.open(**kwargs)
        self.stream.start_stream()

    def resample(self, data):
        """
        Microphone may not support our native processing sampling rate, so
        resample from input_rate to RATE_PROCESS here for webrtcvad and
        deepspeech

        Args:
            data (binary): Input audio stream
        """
        data16 = np.fromstring(string=data, dtype=np.int16)
        resample_size = int(len(data16) / self.input_rate * self.RATE_PROCESS)
        resample = signal.resample(data16, resample_size)
        resample16 = np.array(resample, dtype=np.int16)
        return resample16.tostring()

    def read_resampled(self):
        """Return a block of audio data resampled to 16000hz, blocking if necessary."""
        return self.resample(data=self.buffer_queue.get())

    def read(self):
        """Return a block of audio data, blocking if necessary."""
        return self.buffer_queue.get()

    def destroy(self):
        self.stream.stop_stream()
        self.stream.close()
        self.pa.terminate()

    frame_duration_ms = property(lambda self: 1000 * self.block_size // self.sample_rate)

    def write_wav(self, filename, data):
        logging.info(f'write wav {filename}')
        wf = wave.open(filename, 'wb')
        wf.setnchannels(self.CHANNELS)
        # wf.setsampwidth(self.pa.get_sample_size(FORMAT))
        assert self.FORMAT == pyaudio.paInt16
        wf.setsampwidth(2)
        wf.setframerate(self.sample_rate)
        wf.writeframes(data)
        wf.close()


class VADAudio(Audio):
    """Filter & segment audio with voice activity detection."""

    def __init__(self, aggressiveness=3, device=None, input_rate=None, file=None):
        super().__init__(device=device, input_rate=input_rate, file=file)
        self.vad = Vad(aggressiveness)

    def frame_generator(self):
        """Generator that yields all audio frames from microphone."""
        if self.input_rate == self.RATE_PROCESS:
            while True:
                yield self.read()
        else:
            while True:
                yield self.read_resampled()

    def vad_collector(self, padding_ms=300, ratio=0.75, frames=None):
        """Generator that yields series of consecutive audio frames comprising each utterance, separated by yielding a single None.
            Determines voice activity by ratio of frames in padding_ms. Uses a buffer to include padding_ms prior to being triggered.
            Example: (frame, ..., frame, None, frame, ..., frame, None, ...)
                      |---utterance---|        |---utterance---|
        """
        if frames is None: 
            frames = self.frame_generator()
        num_padding_frames = padding_ms // self.frame_duration_ms
        ring_buffer = deque(maxlen=num_padding_frames)
        triggered = False

        for frame in frames:
            if len(frame) < 640:
                return

            is_speech = self.vad.is_speech(frame, self.sample_rate)

            if not triggered:
                ring_buffer.append((frame, is_speech))
                num_voiced = len([f for f, speech in ring_buffer if speech])
                if num_voiced > ratio * ring_buffer.maxlen:
                    triggered = True
                    for f, s in ring_buffer:
                        yield f
                    ring_buffer.clear()
            else:
                yield frame
                ring_buffer.append((frame, is_speech))
                num_unvoiced = len([f for f, speech in ring_buffer if not speech])
                if num_unvoiced > ratio * ring_buffer.maxlen:
                    triggered = False
                    yield None
                    ring_buffer.clear()

def main(arguments):
    # Load DeepSpeech model
    if os.path.isdir(arguments.model):
        model_dir = arguments.model
        arguments.model = os.path.join(model_dir, 'output_graph.pb')
        arguments.scorer = os.path.join(model_dir, arguments.scorer)

    print('Initializing model...')
    logging.info(f'ARGS.model: {arguments.model}')
    model = Model(arguments.model)
    if arguments.scorer:
        logging.info(f'ARGS.scorer: {arguments.scorer}')
        model.enableExternalScorer(arguments.scorer)

    # Start audio with VAD
    vad_audio = VADAudio(aggressiveness=arguments.vad_aggressiveness, device=arguments.device, input_rate=arguments.rate, file=arguments.file)
    print('Listening (ctrl-C to exit)...')
    frames = vad_audio.vad_collector()

    # Stream from microphone to DeepSpeech using VAD
    spinner = None
    if not arguments.nospinner:
        spinner = Halo(spinner='line')
    stream_context = model.createStream()
    wav_data = bytearray()
    for frame in frames:
        if frame is not None:
            if spinner: 
                spinner.start()
            logging.debug('streaming frame')
            stream_context.feedAudioContent(np.frombuffer(frame, np.int16))
            if arguments.savewav: 
                wav_data.extend(frame)
        else:
            if spinner: 
                spinner.stop()
            logging.debug('end utterance')
            if arguments.savewav:
                vad_audio.write_wav(os.path.join(arguments.savewav, datetime.now().strftime('savewav_%Y-%m-%d_%H-%M-%S_%f.wav')), wav_data)
                wav_data = bytearray()
            text = stream_context.finishStream()
            print(f'Recognized: {text}')
            stream_context = model.createStream()

if __name__ == '__main__':
    DEFAULT_SAMPLE_RATE = 16000

    from argparse import ArgumentParser
    
    parser = ArgumentParser(description='Stream from microphone to DeepSpeech using VAD')

    parser.add_argument('-v', '--vad_aggressiveness', type=int, default=3,
                        help='Set aggressiveness of VAD: an integer between 0 and 3, 0 being the least aggressive about filtering out non-speech, 3 the most aggressive. Default: 3')
    parser.add_argument('--nospinner', action='store_true',
                        help='Disable spinner')
    parser.add_argument('-w', '--savewav',
                        help='Save .wav files of utterences to given directory')
    parser.add_argument('-f', '--file',
                        help='Read from .wav file instead of microphone')
    parser.add_argument('-m', '--model', required=True,
                        help='Path to the model (protocol buffer binary file, or entire directory containing all standard-named files for model)')
    parser.add_argument('-s', '--scorer',
                        help='Path to the external scorer file.')
    parser.add_argument('-d', '--device', type=int, default=None,
                        help='Device input index (Int) as listed by pyaudio.PyAudio.get_device_info_by_index(). If not provided, falls back to PyAudio.get_default_device().')
    parser.add_argument('-r', '--rate', type=int, default=DEFAULT_SAMPLE_RATE,
                        help=f'Input device sample rate. Default: {DEFAULT_SAMPLE_RATE}. Your device may require 44100.')

    ARGS = parser.parse_args()
    if ARGS.savewav: 
        os.makedirs(ARGS.savewav, exist_ok=True)
    try:
        main(ARGS)
    except KeyboardInterrupt:
        pass
