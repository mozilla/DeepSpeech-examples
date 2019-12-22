const http = require('http');
const socketIO = require('socket.io');
const DeepSpeech = require('deepspeech');
const VAD = require('node-vad');

let DEEPSPEECH_MODEL = __dirname + '/deepspeech-0.6.0-models'; // path to deepspeech english model directory

let SILENCE_THRESHOLD = 100; // how many milliseconds of inactivity before processing the audio

const SERVER_PORT = 4000; // websocket server port

const VAD_MODE = VAD.Mode.NORMAL;
// const VAD_MODE = VAD.Mode.LOW_BITRATE;
// const VAD_MODE = VAD.Mode.AGGRESSIVE;
// const VAD_MODE = VAD.Mode.VERY_AGGRESSIVE;
const vad = new VAD(VAD_MODE);

function createModel(modelDir, options) {
	let modelPath = modelDir + '/output_graph.pbmm';
	let lmPath = modelDir + '/lm.binary';
	let triePath = modelDir + '/trie';
	let model = new DeepSpeech.Model(modelPath, options.BEAM_WIDTH);
	model.enableDecoderWithLM(lmPath, triePath, options.LM_ALPHA, options.LM_BETA);
	return model;
}

let englishModel = createModel(DEEPSPEECH_MODEL, {
	BEAM_WIDTH: 1024,
	LM_ALPHA: 0.75,
	LM_BETA: 1.85
});

let modelStream;
let recordedChunks = 0;
let silenceStart = null;
let recordedAudioLength = 0;

let endTimeout = null;
function processMicrophone(data, callback) {
	vad.processAudio(data, 16000).then((res) => {
		switch (res) {
			case VAD.Event.ERROR:
				console.log("VAD ERROR");
				break;
			case VAD.Event.NOISE:
				console.log("VAD NOISE");
				break;
			case VAD.Event.SILENCE:
				processSilence(data, callback);
				break;
			case VAD.Event.VOICE:
				processVoice(data);
				break;
		}
	});
	
	// timeout after 1s of inactivity
	clearTimeout(endTimeout);
	endTimeout = setTimeout(function() {
		timeoutMicrophone(callback);
	},1000);
}

function timeoutMicrophone(callback) {
	process.stdout.write('[timeout]');
	
	let results = intermediateDecode();
	if (results) {
		if (callback) {
			callback(results);
		}
	}
	recordedChunks = 0;
	silenceStart = null;
}

function processSilence(data, callback) {
	if (recordedChunks > 0) { // recording is on
		process.stdout.write('-'); // silence detected while recording
		
		feedAudioContent(data);
		
		if (silenceStart === null) {
			silenceStart = new Date().getTime();
		}
		else {
			let now = new Date().getTime();
			if (now - silenceStart > SILENCE_THRESHOLD) {
				silenceStart = null;
				console.log('[end]');
				let results = intermediateDecode();
				if (results) {
					if (callback) {
						callback(results);
					}
				}
			}
		}
	}
	else {
		process.stdout.write('.'); // silence detected while not recording
	}
}

function processVoice(data) {
	silenceStart = null;
	if (recordedChunks === 0) {
		console.log('');
		process.stdout.write('[start]'); // recording started
	}
	else {
		process.stdout.write('='); // still recording
	}
	recordedChunks++;
	feedAudioContent(data);
}

function createStream() {
	modelStream = englishModel.createStream();
	recordedChunks = 0;
	recordedAudioLength = 0;
}

function finishStream() {
	let start = new Date();
	let text = englishModel.finishStream(modelStream);
	if (text) {
		if (text === 'i' || text === 'a') {
			// bug in DeepSpeech 0.6 causes silence to be inferred as "i" or "a"
			return;
		}
		console.log('');
		console.log('Recognized Text:', text);
		let recogTime = new Date().getTime() - start.getTime();
		return {
			text,
			recogTime,
			audioLength: Math.round(recordedAudioLength)
		};
	}
}

function intermediateDecode() {
	let results = finishStream();
	createStream();
	return results;
}

function feedAudioContent(chunk) {
	recordedAudioLength += (chunk.length / 2) * ( 1 / 16000) * 1000;
	englishModel.feedAudioContent(modelStream, chunk.slice(0, chunk.length / 2));
}

const app = http.createServer(function (req, res) {
	res.writeHead(200);
	res.write('web-microphone-websocket');
	res.end();
});

const io = socketIO(app, {});

io.on('connection', function(socket) {
	console.log('client connected');
	
	socket.once('disconnect', () => {
		console.log('client disconnected');
	});
	
	socket.on('client-ready', function(data) {
		console.log('client-ready', data);
	});
	
	createStream();
	
	socket.on('microphone-data', function(data) {
		processMicrophone(data, (results) => {
			socket.emit('recognize', results);
		});
	});
	
	socket.emit('server-ready');
});

app.listen(SERVER_PORT, 'localhost', () => {
	console.log('Socket server listening on:', SERVER_PORT);
});

