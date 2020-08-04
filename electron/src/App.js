import React, {Component} from 'react';

class App extends Component {
	constructor(props) {
		super(props);
		this.state = {
			loading: true,
			error: null,
			files: [],
			results: {}
		}
	}
	
	componentDidMount() {
		// when the component mounts, get the list of .wav files
		window.ipcRenderer.invoke('load-files')
		.then(files => {
			console.log('files', files);
			this.setState({
				loading: false,
				files
			}, () => {
				files.forEach(file => {
					// request that each file be processed by deepspeech
					console.log('recognize', file);
					window.ipcRenderer.invoke('recognize-wav', file).then(result => {
						// add the recognition results to this.state.results
						console.log('result', result);
						const results = {...this.state.results};
						results[file] = result;
						this.setState({results});
					});
				})
			});
		}).catch(e => {
			this.setState({
				loading: false,
				error: e
			});
		});
	}
	
	render() {
		if (this.state.loading) return 'Loading...';
		if (this.state.error) return 'Error: ' + this.state.error;
		return (<div className="App">
			<ul>
				{
					this.state.files.map((file, index) => {
						return (<li key={index}>
							{file} = {this.state.results[file] || '...'}
						</li>)
					})
				}
			</ul>
		</div>);
	}
}

export default App;
