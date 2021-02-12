import deepspeech
from deepspeech import Model, version
import numpy as np
import wave
import itertools
import argparse

# Example of a valid execution:
# hotwords_adjusting.py --model model.pbmm --scorer.scorer --audio audio/filename.wav --min -100.0 --max 100.0 --steps 3 --hot_words hot,cold
# This tests combinations of hot-words: 'hot' and 'cold' on audiofile 'filename.wav'
# using prios from range [-100;100] by doing 3 steps: [-100, 0, 100]

#Prints out a Cartesian product of hotwords and their prios
def test_file(filename, hotwords, min_prio, max_prio, prio_steps):

    fin = wave.open(filename, 'rb')
    audio = np.frombuffer(fin.readframes(fin.getnframes()), np.int16)
    fin.close()

    prio_lists = np.linspace(min_prio, max_prio,prio_steps).tolist()

    prio_product = itertools.product(prio_lists, repeat=len(hotwords))
    for x in itertools.product(prio_lists, repeat=len(hotwords)):
        DeepSpeech.clearHotWords()
        for y in enumerate(hotwords):
            DeepSpeech.addHotWord(hotwords[y[0]], x[y[0]])
           
        
        print(f"{hotwords} = {x} :: [{DeepSpeech.stt(audio)}]")


def main():
    if(args.min>=args.max):
        print("Error: min_prio can't be bigger than max_prio.")
    else:
        test_file(args.audio, args.hot_words.split(','), args.min, args.max, args.steps)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='DeepSpeech hot-word adjusting.')
    parser.add_argument('--model', required=True,
                    help='Path to the model (protocol buffer binary file)')
    parser.add_argument('--scorer', required=True,
                    help='Path to the external scorer file')
    parser.add_argument('--audio', type=str, required=True,
                    help='Path to the audio file to run (WAV format)')
    parser.add_argument('--min', type=float, default=-10.0,
                    help='Minimum boost value.')
    parser.add_argument('--max', type=float, default=10.0,
                    help='Maximum boost value.')
    parser.add_argument('--steps', type=int, default=6,
                    help='Number of tests per each hot-word.')
    parser.add_argument('--hot_words', type=str, required=True,
                    help='Hot-words separated by comma.')

    args = parser.parse_args()

    DeepSpeech = Model(args.model)
    DeepSpeech.enableExternalScorer(args.scorer)

    main()