#!/usr/bin/env node

const VAD = require("node-vad");
const STT = require('@mozilla-voice/stt');
const argparse = require('argparse');
const util = require('util');
const { spawn } = require('child_process');

let VersionAction = function VersionAction(options) {
	options = options || {};
	options.nargs = 0;
	argparse.Action.call(this, options);
};

util.inherits(VersionAction, argparse.Action);

VersionAction.prototype.call = function(parser) {
	STT.printVersions();
	process.exit(0);
};

let parser = new argparse.ArgumentParser({addHelp: true, description: 'Running DeepSpeech inference.'});
parser.addArgument(['--model'], {required: true, help: 'Path to the model (protocol buffer binary file)'});
parser.addArgument(['--scorer'], {help: 'Path to the scorer file', nargs: '?'});
parser.addArgument(['--audio'], {required: true, help: 'Path to the audio source to run (ffmpeg supported formats)'});
parser.addArgument(['--version'], {action: VersionAction, help: 'Print version and exits'});
let args = parser.parseArgs();

function totalTime(hrtimeValue) {
	return (hrtimeValue[0] + hrtimeValue[1] / 1000000000).toPrecision(4);
}

console.error('Loading model from file %s', args['model']);
const model_load_start = process.hrtime();
let model = new STT.Model(args['model']);
const model_load_end = process.hrtime(model_load_start);
console.error('Loaded model in %ds.', totalTime(model_load_end));

if (args['scorer']) {
	console.error('Loading scorer from file %s', args['scorer']);
	const scorer_load_start = process.hrtime();
	model.enableExternalScorer(args['scorer']);
	const scorer_load_end = process.hrtime(scorer_load_start);
	console.error('Loaded scorer in %ds.', totalTime(scorer_load_end));
}

// Defines different thresholds for voice detection
// NORMAL: Suitable for high bitrate, low-noise data. May classify noise as voice, too.
// LOW_BITRATE: Detection mode optimised for low-bitrate audio.
// AGGRESSIVE: Detection mode best suited for somewhat noisy, lower quality audio.
// VERY_AGGRESSIVE: Detection mode with lowest miss-rate. Works well for most inputs.
const VAD_MODE = VAD.Mode.NORMAL;
// const VAD_MODE = VAD.Mode.LOW_BITRATE;
// const VAD_MODE = VAD.Mode.AGGRESSIVE;
// const VAD_MODE = VAD.Mode.VERY_AGGRESSIVE;

// Time in milliseconds for debouncing speech active state
const DEBOUNCE_TIME = 20;

// Create voice activity stream
const VAD_STREAM = VAD.createStream({
	mode: VAD_MODE,
	audioFrequency: model.sampleRate(),
	debounceTime: DEBOUNCE_TIME
});

// Spawn ffmpeg process
const ffmpeg = spawn('ffmpeg', [
	'-hide_banner',
	'-nostats',
	'-loglevel', 'fatal',
	'-i', args['audio'],
	'-vn',
	'-acodec', 'pcm_s16le',
	'-ac', 1,
	'-ar', model.sampleRate(),
	'-f', 's16le',
	'pipe:'
]);

let audioLength = 0;
let sctx = model.createStream();

function finishStream() {
	const model_load_start = process.hrtime();
	console.error('Running inference.');
	console.log('Transcription: ', sctx.finishStream());
	const model_load_end = process.hrtime(model_load_start);
	console.error('Inference took %ds for %ds audio file.', totalTime(model_load_end), audioLength.toPrecision(4));
	audioLength = 0;
}

function intermediateDecode() {
	finishStream();
	sctx = model.createStream();
}

function feedAudioContent(chunk) {
	audioLength += (chunk.length / 2) * ( 1 / model.sampleRate());
	sctx.feedAudioContent(chunk);
}

function processVad(data) {
	if (data.speech.start||data.speech.state) feedAudioContent(data.audioData)
	else if (data.speech.end) { feedAudioContent(data.audioData); intermediateDecode() }
}

ffmpeg.stdout.pipe(VAD_STREAM).on('data', processVad);
