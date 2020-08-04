const request = require('request');
const fs = require('fs');

// generic http download
function download(url, dest, callback) {
	var file = fs.createWriteStream(dest);
	console.log('Downloading:', url);
	const sendReq = request.get(url);
	sendReq.on('response', (response) => {
		if (response.statusCode === 200) {
			console.log('PLEASE WAIT...');
			sendReq.pipe(file);
		}
	});
	file.on('finish', () => {
		file.close();
		console.log('Saved:', dest);
		callback();
	});
}

module.exports = download;