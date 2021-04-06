import os
from setuptools import setup

DIR = os.path.dirname(os.path.abspath(__file__))
INSTALL_PACKAGES = open(os.path.join(DIR, 'requirements.txt')).read().splitlines()

with open("README.md", "r") as fh:
    README = fh.read()

setup(
    name="AutoSub",
    packages="autosub",
    version="0.0.1",
    author="Abhiroop Talasila",
    author_email="abhiroop.talasila@gmail.com",
    description="CLI application to generate subtitle file (.srt) for any video file using using STT",
    long_description=README,
    install_requires=INSTALL_PACKAGES,
    long_description_content_type="text/markdown",
    url="https://github.com/abhirooptalasila/AutoSub",
    keywords=['speech-to-text','deepspeech','machine-learning'],
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.5',
)
