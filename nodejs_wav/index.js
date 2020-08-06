const STT = require('@mozilla-voice/stt');
const Fs = require('fs');
const Sox = require('sox-stream');
const MemoryStream = require('memory-stream');
const Duplex = require('stream').Duplex;
const Wav = require('node-wav');

let modelPath = './models/deepspeech-0.8.0-models.pbmm';

let model = new STT.Model(modelPath);

let desiredSampleRate = model.sampleRate();

let scorerPath = './models/deepspeech-0.8.0-models.scorer';

model.enableExternalScorer(scorerPath);

let audioFile = process.argv[2] || './audio/2830-3980-0043.wav';

if (!Fs.existsSync(audioFile)) {
	console.log('file missing:', audioFile);
	process.exit();
}

const buffer = Fs.readFileSync(audioFile);
const result = Wav.decode(buffer);

if (result.sampleRate < desiredSampleRate) {
	console.error('Warning: original sample rate (' + result.sampleRate + ') is lower than ' + desiredSampleRate + 'Hz. Up-sampling might produce erratic speech recognition.');
}

function bufferToStream(buffer) {
	let stream = new Duplex();
	stream.push(buffer);
	stream.push(null);
	return stream;
}

let audioStream = new MemoryStream();
bufferToStream(buffer).
pipe(Sox({
	global: {
		'no-dither': true,
	},
	output: {
		bits: 16,
		rate: desiredSampleRate,
		channels: 1,
		encoding: 'signed-integer',
		endian: 'little',
		compression: 0.0,
		type: 'raw'
	}
})).
pipe(audioStream);

audioStream.on('finish', () => {
	let audioBuffer = audioStream.toBuffer();
	
	const audioLength = (audioBuffer.length / 2) * (1 / desiredSampleRate);
	console.log('audio length', audioLength);
	
	let result = model.stt(audioBuffer);
	
	console.log('result:', result);
});
