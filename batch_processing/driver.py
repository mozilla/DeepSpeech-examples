import glob
import json
import os
from os.path import expanduser

import click

import delegator

# first loop over the files
# convert them to wave

# record things in 16000hz in the future or you gret this
# Warning: original sample rate (44100) is different than 16000h.z Resampling might produce erratic speech recognition.


@click.command()
@click.option("--dirname", type=click.Path(exists=True, resolve_path=True))
@click.option("--ext", default=".mp3")
@click.option(
    "--model",
    default="deepspeech-0.7.3-models.pbmm",
    type=click.Path(exists=True, resolve_path=True),
)
@click.option(
    "--scorer",
    default="deepspeech-0.7.3-models.scorer",
    type=click.Path(exists=True, resolve_path=True),
)

# manage my library of podcasts
def main(dirname, ext, model, scorer):
    print("main")
    model = expanduser(model)
    scorer = expanduser(scorer)
    pattern = dirname + "/" + "*" + ext
    audiorate = "16000"

    print(pattern)
    for filename in glob.glob(pattern):
        print(filename)

        wavefile = filename + ".wav"

        convert_command = " ".join(
            [
                "ffmpeg",
                "-i",
                "'{}'".format(filename),
                "-ar",
                audiorate,
                "'{}'".format(wavefile),
            ]
        )
        if not os.path.isfile(wavefile):
            print(convert_command)
            r = delegator.run(convert_command)
            print(r.out)
        else:
            print("skipping wave conversion that exists")

        command = " ".join(
            [
                "deepspeech",
                "--model",
                model,
                "--scorer",
                scorer,
                "--audio",
                "'{}'".format(wavefile),
                #            "--extended",
                "--json",
            ]
        )
        print(command)
        r = delegator.run(command)
        with open(filename + ".json", "w") as fo:
            print(r.out)
            fo.write(r.out)


if __name__ == "__main__":
    main()
