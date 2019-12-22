// source:
// https://github.com/Picovoice/web-voice-processor/blob/master/src/downsampling_worker.js

onmessage = function (e) {
    switch (e.data.command) {
        case "init":
            init(e.data.inputSampleRate);
            break;
        case "process":
            process(e.data.inputFrame);
            break;
        case "reset":
            reset();
            break;
    }
};

let inputSampleRate;
let inputBuffer = [];

function init(x) {
    inputSampleRate = x;
}

function process(inputFrame) {
    for (let i = 0; i < inputFrame.length; i++) {
        inputBuffer.push((inputFrame[i]) * 32767);
    }
    
    const PV_SAMPLE_RATE = 16000;
    const PV_FRAME_LENGTH = 512;
    
    while ((inputBuffer.length * PV_SAMPLE_RATE / inputSampleRate) > PV_FRAME_LENGTH) {
        let outputFrame = new Int16Array(PV_FRAME_LENGTH);
        let sum = 0;
        let num = 0;
        let outputIndex = 0;
        let inputIndex = 0;
        
        while (outputIndex < PV_FRAME_LENGTH) {
            sum = 0;
            num = 0;
            while (inputIndex < Math.min(inputBuffer.length, (outputIndex + 1) * inputSampleRate / PV_SAMPLE_RATE)) {
                sum += inputBuffer[inputIndex];
                num++;
                inputIndex++;
            }
            outputFrame[outputIndex] = sum / num;
            outputIndex++;
        }
        
        postMessage(outputFrame);
        
        inputBuffer = inputBuffer.slice(inputIndex);
    }
}

function reset() {
    inputBuffer = [];
}