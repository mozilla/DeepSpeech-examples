const electron = require('electron');
const Path = require('path');
const app = electron.app;
const ipcMain = electron.ipcMain;
const BrowserWindow = electron.BrowserWindow;
const isDev = require('electron-is-dev');
if (isDev) process.env.NODE_ENV = 'dev';
const {recognizeWav} = require('./recognize-wav');
const fs = require('fs');
const path = require('path');

let mainWindow;

function createWindow(model) {
	mainWindow = new BrowserWindow({
		width: 480,
		height: 480,
		webPreferences: {
			nodeIntegration: true,
			nodeIntegrationInWorker: false,
			preload: __dirname + '/preload.js'
		}
	});
	
	mainWindow.loadURL(isDev ? 'http://localhost:3000' : `file://${Path.join(__dirname, '../build/index.html')}`);
	
	if (isDev) {
		// open Chrome Development Console
		// mainWindow.webContents.openDevTools();
	}
	
	mainWindow.on('closed', () => mainWindow = null);
	
	app.on('window-all-closed', () => {
		app.quit()
	});
	
	// message from front-end App.js, request that this file be processed by DeepSpeech
	ipcMain.handle('recognize-wav', async function (event, file) {
		let filePath = path.resolve(__dirname, 'audio', file);
		return recognizeWav(filePath, model);
	});
	
	// message from front-end App.js, retrieve list of .wav files in /public/audio
	ipcMain.handle('load-files', function (event) {
		return new Promise(function (resolve, reject) {
			try {
				let audioPath = path.resolve(__dirname, 'audio');
				fs.readdir(audioPath, function (err, files) {
					files = files.filter(function (file) {
						return file.endsWith('.wav');
					});
					resolve(files);
				});
			} catch (e) {
				reject(e.toString())
			}
		});
	});
	
	return mainWindow;
}

module.exports = createWindow;