# DeepSpeech Electron example

This is an example of DeepSpeech running in an Electron app with a ReactJS front-end and processing .wav files.

## Install

Install NPM modules:

```
npm install
npm run rebuild
```

Download and extract audio files to `/public` directory

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/audio-0.8.0.tar.gz
tar xfvz audio-0.8.0.tar.gz -C ./public/
```

(Optional) Download or softlink DeepSpeech 0.8.0 model files to the root of the project:

```
mkdir models
cd models
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models.pbmm
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models.scorer
cd ..
```

If the files do not exist, they will be downloaded.

## Run

Run development version (Mac/Linux):

```
npm run dev
```

Run development version (Windows):

```
export BROWSER=none
npm run dev-win
```

## Package

Build distributable package (Mac/Linux):

```
npm run dist
```

Build distributable package (Windows installer):

```
export BROWSER=none
npm run dist-win
```

Test the (dmg/appimage/exe) package file that has been generated in `/dist`.

## Uninstall

The model files download to the following directories and must be deleted manually

- MacOSX: `~/Library/Application\ Support/deepspeech-electron`
- Linux:  `~/.config/deepspeech-electron`
- Windows: `~/AppData/Roaming/deepspeech-electron`
