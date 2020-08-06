const STT = require('@mozilla-voice/stt');
const fs = require('fs');
const path = require('path');
const wav = require('wav');
const download = require('./download');

// return the model or download it if it is not found
function getModel(appDataPath, callback) {
	let modelPath = path.resolve(appDataPath, 'deepspeech-0.8.0-models.pbmm');
	let scorerPath = path.resolve(appDataPath, 'deepspeech-0.8.0-models.scorer');
	if (fs.existsSync(modelPath) && fs.existsSync(scorerPath)) {
		callback(createModel(modelPath, scorerPath));
	}
	else {
		// if the model files do not exist, download and save them to AppData path
		console.log('\nDOWNLOADING MODEL TO: '+appDataPath+'\n');
		const downloadPath = 'https://github.com/mozilla/DeepSpeech/releases/download/v0.8.0/deepspeech-0.8.0-models';
		download(downloadPath+'.pbmm', modelPath, function() {
			download(downloadPath+'.scorer', scorerPath, function() {
				callback(createModel(modelPath, scorerPath));
			});
		});
	}
}

// create the model
function createModel(modelPath, scorerPath) {
	const model = new STT.Model(modelPath);
	model.enableExternalScorer(scorerPath);
	return model;
}

// create a stream to process a .wav file
function recognizeWav(path, model) {
	return new Promise(function(resolve, reject) {
		try {
			let modelStream = model.createStream();
			const bufferSize = 512;
			const file = fs.createReadStream(path, {highWaterMark: bufferSize});
			const reader = new wav.Reader();
			reader.on('format', function (format) {
				if (format.sampleRate !== model.sampleRate()) {
					reject(new Error('invalid sample rate: '+format.sampleRate));
				}
				reader.on('end', function () {
					const results = modelStream.finishStream();
					resolve(results);
				});
				reader.on('data', function (data) {
					modelStream.feedAudioContent(data);
				});
			});
			file.pipe(reader);
		}
		catch(e) {
			reject(e);
		}
	});
}

module.exports = {
	getModel,
	recognizeWav
};
