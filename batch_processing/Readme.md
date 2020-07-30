
# Setup for windows

The setup of the windows versions was quite a challenge, getting the versions right.

see `setup.ps` for the loading of the paths.

```
Windows 10 Pro
Version: 2004
Os Build : 19041.329
```

Here are the versions I have installed via chocolaty

```
Chocolatey v0.10.15
7zip v19.0 
7zip.install v19.0
anaconda3 v2020.02
audacity v2.4.1
az.powershell v4.2.0
azshell v0.2.2
azure-cli v2.7.0
azurepowershell v6.9.0
bazel v3.2.0
blender v2.83.0
chocolatey v0.10.15
chocolatey-core.extension v1.3.5.1
chocolatey-dotnetfx.extension v1.0.1
chocolatey-fastanswers.extension v0.0.2
chocolatey-visualstudio.extension v1.8.1
chocolatey-windowsupdate.extension v1.0.4
docker-desktop v2.3.0.3 
DotNet4.5.1 v4.5.1.20140606 
DotNet4.5.2 v4.5.2.20140902 
dotnetfx v4.8.0.20190930 
ffmpeg v4.2.3 
git v2.26.2
git.install
google-chrome-x64 v47.0.2526.81 
GoogleChrome v83.0.4103.97 
grep v2.1032 
KB2919355 v1.0.20160915 
KB2919442 v1.0.20160915 
KB2999226 v1.0.20181019 
KB3033929 v1.0.5 
KB3035131 v1.0.3 
KB3118401 v1.0.4 
microsoft-windows-terminal v1.0.1401.0 
msys2 v20200602.0.0 
NTop.Portable v0.3.4 
powershell-core v7.0.1
procexp v16.32 
python v3.8.3 
python3 v3.8.3 
sox.portable v14.4.1 
vcredist140 v14.26.28720.3 
vcredist2008 v9.0.30729.6163 
vcredist2015 v14.0.24215.20170201 
vcredist2017 v14.16.27033 
visualstudio-installer v2.0.1 
VisualStudio2013ExpressWeb v12.0.21005.20150920 
visualstudio2019community v16.6.1.0 
vscode v1.45.1
vscode.install v1.45.1
Wget v1.20.3.20190531 
windows-sdk-10-version-2004-windbg v10.0.19041.0 
wsl v1.0.1 
Xming v6.9.0.31 

```

The versions of software installed are :

* CUDA v10.0
from https://developer.nvidia.com/cuda-10.0-download-archive?target_os=Windows&target_arch=x86_64&target_version=10&target_type=exelocal
get the file https://developer.download.nvidia.com/compute/cuda/10.0/secure/Prod/local_installers/cuda_10.0.130_411.31_win10.exe

* cudnn-10.0-windows10-x64-v7.5.1.10
from https://developer.nvidia.com/rdp/cudnn-archive
`Download cuDNN v7.5.1 (April 22, 2019), for CUDA 10.0`
via https://developer.nvidia.com/rdp/cudnn-archive#a-collapse751-10
get the file https://developer.download.nvidia.com/compute/machine-learning/cudnn/secure/7.6.5.32/Production/10.0_20191031/cudnn-10.0-windows10-x64-v7.6.5.32.zip

* TensorRT-5.1.5.0.Windows10.x86_64.cuda-10.0.cudnn7.5
from https://developer.nvidia.com/nvidia-tensorrt-5x-download
https://developer.nvidia.com/nvidia-tensorrt-5x-download#trt51ga
via `Windows10 and CUDA 10.0 zip package`
get the file https://developer.nvidia.com/compute/machine-learning/tensorrt/5.1/ga/zips/TensorRT-5.1.5.0.Windows10.x86_64.cuda-10.0.cudnn7.5.zip


I am using these exact versions:
`pip3 install -r requirements.txt`

Here is the output :
```
PS C:\Users\jmike\Documents\GitHub\DeepSpeech-examples\batch_processing> . .\test.ps1
2020-06-14 11:05:01.015450: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library cudart64_100.dll
Loading model from file C:\Users\jmike\Documents\GitHub\DeepSpeech\deepspeech-0.7.3-models.pbmm
TensorFlow: v1.15.0-24-gceb46aae58
DeepSpeech: v0.7.3-0-g88584941
2020-06-14 11:05:01.237478: I tensorflow/core/platform/cpu_feature_guard.cc:142] Your CPU supports instructions that this TensorFlow binary was not compiled to use: AVX2
2020-06-14 11:05:01.244057: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library nvcuda.dll
2020-06-14 11:05:01.466608: I tensorflow/core/common_runtime/gpu/gpu_device.cc:1618] Found device 0 with properties:
name: GeForce MX250 major: 6 minor: 1 memoryClockRate(GHz): 1.582
pciBusID: 0000:01:00.0
2020-06-14 11:05:01.466806: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library cudart64_100.dll
2020-06-14 11:05:01.473468: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library cublas64_100.dll
2020-06-14 11:05:01.476879: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library cufft64_100.dll
2020-06-14 11:05:01.478672: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library curand64_100.dll
2020-06-14 11:05:01.482925: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library cusolver64_100.dll
2020-06-14 11:05:01.485963: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library cusparse64_100.dll
2020-06-14 11:05:01.498053: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library cudnn64_7.dll
2020-06-14 11:05:01.498710: I tensorflow/core/common_runtime/gpu/gpu_device.cc:1746] Adding visible gpu devices: 0
2020-06-14 11:05:02.066853: I tensorflow/core/common_runtime/gpu/gpu_device.cc:1159] Device interconnect StreamExecutor with strength 1 edge matrix:
2020-06-14 11:05:02.067030: I tensorflow/core/common_runtime/gpu/gpu_device.cc:1165]      0
2020-06-14 11:05:02.068133: I tensorflow/core/common_runtime/gpu/gpu_device.cc:1178] 0:   N
2020-06-14 11:05:02.073298: I tensorflow/core/common_runtime/gpu/gpu_device.cc:1304] Created TensorFlow device (/job:localhost/replica:0/task:0/device:GPU:0 with 1410 MB memory) -> physical GPU (device: 0, name: GeForce MX250, pci bus id: 0000:01:00.0, compute capability: 6.1)
Loaded model in 0.941s.
Loading scorer from files C:\Users\jmike\Documents\GitHub\DeepSpeech\deepspeech-0.7.3-models.scorer
Loaded scorer in 0.0143s.
Warning: original sample rate (44100) is different than 16000hz. Resampling might produce erratic speech recognition.
Running inference.
2020-06-14 11:05:02.382781: I tensorflow/stream_executor/platform/default/dso_loader.cc:44] Successfully opened dynamic library cublas64_100.dll
```
Running via the GPU takes half the time of using the CPU and has good results.

# Driver command line

`./driver.py --model c:/Users/jmike/Documents/GitHub/DeepSpeech/deepspeech-0.7.3-models.pbmm  --scorer c:/Users/jmike/Documents/GitHub/DeepSpeech/deepspeech-0.7.3-models.scorer --dirname c:/Users/jmike/Downloads/podcast/`

# Example

It will then run the individual commands like :

`deepspeech --model C:\Users\jmike\Documents\GitHub\DeepSpeech\deepspeech-0.7.3-models.pbmm --scorer C:\Users\jmike\Documents\GitHub\DeepSpeech\deepspeech-0.7.3-models.scorer --audio 'C:\Users\jmike\Downloads\podcast\45374977-48000-2-24d9a365625bb.mp3.wav' --json`


Websites referenced:

https://chocolatey.org/packages/cuda
https://deepspeech.readthedocs.io/en/v0.7.3/?badge=latest
https://developer.nvidia.com/cuda-10.0-download-archive?target_os=Windows&target_arch=x86_64&target_version=10
https://discourse.mozilla.org/t/query-regarding-speed-of-training-and-issues-with-convergence/41874
https://discourse.mozilla.org/t/right-cuda-version-for-using-deepspeech-gpu/41927/12
https://docs.nvidia.com/deeplearning/sdk/cudnn-install/index.html#download-windows
https://github.com/MichalMazurek/python-poetry/blob/d3f6df6a6c2587d7a6034719716de257917c4b0f/dockerfiles.py
https://github.com/amitt001/delegator.py
https://github.com/tensorflow/tensorflow/issues/25807
https://github.com/tensorflow/tensorflow/issues/28223
https://github.com/tensorflow/tensorflow/issues/5968
https://hacks.mozilla.org/2019/12/deepspeech-0-6-mozillas-speech-to-text-engine/
https://palletsprojects.com/p/click/
https://www.howtoforge.com/tutorial/ffmpeg-audio-conversion/
https://www.joe0.com/2019/10/19/how-resolve-tensorflow-2-0-error-could-not-load-dynamic-library-cudart64_100-dll-dlerror-cudart64_100-dll-not-found/
https://www.programcreek.com/python/example/88033/click.Path