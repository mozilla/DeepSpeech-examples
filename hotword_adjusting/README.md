# hotword_adjusting
This script provides an example of hot-word boosting usage. It also allows adjusting your boost values to see how they change the final transcription.

# How to use?
Run using `python 3.9`, while having a `deepspeech` installed.

This works from version 0.9.0 since it was the version that added this feature.
Example of usage:
```
hotword_adjusting.py --model model.pbmm --scorer scorer.scorer --audio audio/filename.wav --min -100.0 --max 100.0 --steps 3 --hot_words hot,cold
```
This tests combinations of hot-words: 'hot' and 'cold' on audiofile 'filename.wav'
Using prios/boost values from range [-100;100] by doing 3 steps: [-100, 0, 100]

# Example output

For an input:
```
hotword_adjusting.py --model model.pbmm --scorer scorer.scorer --audio audio/seuss.wav --min -10.0 --max 10.0 --steps 3 --hot_words bad,glad
```

```
['bad', 'glad'] = (-10.0, -10.0) :: [why are they sad and lad and that i do not know go ask your dad]
['bad', 'glad'] = (-10.0, 0.0) :: [why are they sad and glad and that i do not know go ask your dad]
['bad', 'glad'] = (-10.0, 10.0) :: [why are they sad and glad and that i do not know go ask your dad]
['bad', 'glad'] = (0.0, -10.0) :: [why are they sad and lad and that i do not know go ask your dad]
['bad', 'glad'] = (0.0, 0.0) :: [why are they sad and glad and that i do not know go ask your dad]
['bad', 'glad'] = (0.0, 10.0) :: [why are they sad and glad and that i do not know go ask your dad]
['bad', 'glad'] = (10.0, -10.0) :: [why are they bad and bad and bad i do not know go ask your dad]
['bad', 'glad'] = (10.0, 0.0) :: [why are they bad and bad and bad i do not know go ask your dad]
['bad', 'glad'] = (10.0, 10.0) :: [why are they bad and glad and bad i do not know go ask your dad]
```

