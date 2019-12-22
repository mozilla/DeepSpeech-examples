import React, {Component} from 'react';
import io from 'socket.io-client';

// convert microphone float32 audio data to a pcm16 buffer to stream over websocket
const pcmBuffer = function (data) {
	let audio = new DataView(new ArrayBuffer(data.length * 2));
	for (let i = 0; i < data.length; i++) {
		let multiplier = data[i] < 0 ? 0x8000 : 0x7fff;
		let value = (data[i] * multiplier) | 0;
		audio.setInt16(i * 2, value, true);
	}
	return Buffer.from(audio.buffer);
};

class App extends Component {
	
	constructor(props) {
		super(props);
		this.state = {
			recording: false,
			recordingStart: 0,
			recordingTime: 0,
			recognitionOutput: []
		};
	}
	
	componentDidMount() {
		let recognitionCount = 0;
		
		this.socket = io.connect('http://localhost:4000', {});
		this.socket.once('connect', () => {
			console.log('socket connected');
			
			this.socket.on('server-ready', () => {
				console.log('server-ready');
			});
			
			this.socket.on('recognize', (results) => {
				console.log('recognized:', results);
				const {recognitionOutput} = this.state;
				results.id = recognitionCount++;
				recognitionOutput.unshift(results);
				this.setState({recognitionOutput});
			});
			
			this.socket.emit('client-ready');
		});
	}
	
	render() {
		return (<div className="App">
			<div>
				<button disabled={this.state.recording} onClick={this.startRecording}>
					Start Recording
				</button>
				
				<button disabled={!this.state.recording} onClick={this.stopRecording}>
					Stop Recording
				</button>
				
				{this.renderTime()}
			</div>
			{this.renderRecognitionOutput()}
		</div>);
	}
	
	renderTime() {
		return (<span>
			{(Math.round(this.state.recordingTime / 100) / 10).toFixed(1)}s
		</span>);
	}
	
	renderRecognitionOutput() {
		return (<ul>
			{this.state.recognitionOutput.map((r) => {
				return (<li key={r.id}>{r.text}</li>);
			})}
		</ul>)
	}
	
	createAudioProcessor(audioContext) {
		var processor = audioContext.createScriptProcessor(512);
		processor.onaudioprocess = (event) => {
			var data = event.inputBuffer.getChannelData(0);
			var buffer = pcmBuffer(data);
			
			// send microphone audio data to the server
			this.socket.emit('microphone-data', buffer);
		};
		
		processor.shutdown = () => {
			processor.disconnect();
			this.onaudioprocess = null;
		};
		
		processor.connect(audioContext.destination);
		
		return processor;
	}
	
	startRecording = e => {
		if (!this.state.recording) {
			this.recordingInterval = setInterval(() => {
				let recordingTime = new Date().getTime() - this.state.recordingStart;
				this.setState({recordingTime});
			}, 100);
			
			this.setState({
				recording: true,
				recordingStart: new Date().getTime(),
				recordingTime: 0
			}, () => {
				this.startMicrophone();
			});
		}
	};
	
	
	startMicrophone() {
		this.audioContext = new AudioContext({
			sampleRate: 16000
		});
		
		const success = (stream) => {
			console.log('started recording');
			this.mediaStreamSource = this.audioContext.createMediaStreamSource(stream);
			this.processor = this.createAudioProcessor(this.audioContext);
			this.mediaStreamSource.connect(this.processor);
		};
		
		const fail = (e) => {
			console.error('recording failure', e);
		};
		
		navigator.getUserMedia(
			{
				video: false,
				audio: true
			}, success, fail);
	}
	
	stopRecording = e => {
		if (this.state.recording) {
			clearInterval(this.recordingInterval);
			this.setState({
				recording: false
			}, () => {
				this.stopMicrophone();
			});
		}
	};
	
	stopMicrophone() {
		if (this.mediaStreamSource && this.processor) {
			this.mediaStreamSource.disconnect(this.processor);
		}
		if (this.processor) {
			this.processor.shutdown();
		}
		if (this.audioContext) {
			this.audioContext.close();
		}
	}
}

export default App;
