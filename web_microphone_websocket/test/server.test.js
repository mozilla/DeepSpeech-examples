const chai = require('chai');
const chaiHttp = require('chai-http');
const should = require('should-http');
chai.use(chaiHttp);
const expect = chai.expect;
const fs = require('fs');
const io = require('socket.io-client');

const url = 'http://localhost:4000';

let audioFile1 = process.env.HOME + '/DeepSpeech/audio/2830-3980-0043.wav';
let audioFile2 = process.env.HOME + '/DeepSpeech/audio/8455-210777-0068.wav';
let audioFile3 = process.env.HOME + '/DeepSpeech/audio/4507-16021-0012.wav';

let socket;

before(function(done) {
	console.log('before');
	socket = io.connect(url, {});
	done();
});

describe('GET /', function() {
	it('should return web-microphone-websocket', function(done) {
		chai.request(url)
		.get('/')
		.end(function(err, res){
			res.should.have.status(200);
			expect(res.text).to.be.equal('web-microphone-websocket');
			done();
		});
	});
});

describe('Websocket Audio', function() {
	
	it('audioFile1: experience proof this', function(done) {
		socket.once('recognize', (results) => {
			expect(results.text).to.be.equal('experience proof this');
			done();
		});
		
		fs.createReadStream(audioFile1, {highWaterMark: 4096})
		.on('data', function (chunk) {
			socket.emit('microphone-data', chunk);
		})
		.on('end', function () {
			socket.emit('microphone-end');
		});
	});
	
	it('audioFile2: your power is sufficient i said', function(done) {
		socket.once('recognize', (results) => {
			expect(results.text).to.be.equal('your power is sufficient i said');
			done();
		});

		fs.createReadStream(audioFile2, {highWaterMark: 4096})
		.on('data', function (chunk) {
			socket.emit('microphone-data', chunk);
		})
		.on('end', function () {
			socket.emit('microphone-end');
		});
	});

	it('audioFile3: why should one halt on the way', function(done) {
		socket.once('recognize', (results) => {
			expect(results.text).to.be.equal('why should one halt on the way');
			done();
		});

		fs.createReadStream(audioFile3, {highWaterMark: 4096})
		.on('data', function (chunk) {
			socket.emit('microphone-data', chunk);
		})
		.on('end', function () {
			socket.emit('microphone-end');
		});

	});
});
