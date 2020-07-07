const electron = require('electron');
const app = electron.app;
const path = require('path');
const fs = require('fs');
const createWindow = require('./create-window');
const {getModel} = require('./recognize-wav');

let appDataPath;

if (fs.existsSync(path.resolve(__dirname, '../models/deepspeech-0.7.4-models.pbmm'))) {
	// if the deepspeech model was found at the root, use that directory
	appDataPath = path.resolve(__dirname, '../models');
}
else {
	// otherwise use the electron "appData" path
	appDataPath = path.resolve(electron.app.getPath('appData'), 'deepspeech-electron');
}

app.on('ready', function () {
	getModel(appDataPath, function (model) {
		console.log('model loaded')
		createWindow(model);
	});
});
