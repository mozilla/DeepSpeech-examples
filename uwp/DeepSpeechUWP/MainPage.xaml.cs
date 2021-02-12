using DeepSpeechClient.Interfaces;
using DeepSpeechClient.Models;
using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Threading.Tasks;
using Windows.Devices.Enumeration;
using Windows.Foundation;
using Windows.Media;
using Windows.Media.Audio;
using Windows.Media.Capture;
using Windows.Media.MediaProperties;
using Windows.Media.Render;
using Windows.Storage;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;

namespace DeepSpeechUWP
{
    [ComImport]
    [Guid("5B0D3235-4DBA-4D44-865E-8F1D0E4FD04D")]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    unsafe interface IMemoryBufferByteAccess
    {
        void GetBuffer(out byte* buffer, out uint capacity);
    }

    public sealed partial class MainPage : Page
    {
        private bool isFeeding = false;

        private StorageFile audioFile;
        private DeviceInformation selectedInputDevice;
        private DeviceInformationCollection inputDevices;
        private IDeepSpeech client;
        private DeepSpeechStream stream;
        private MediaEncodingProfile encoding;
        private AudioGraph graph;

        private ConcurrentQueue<short[]> bufferQueue = new ConcurrentQueue<short[]>();

        public MainPage()
        {
            this.InitializeComponent();
            InitDeepSpeech();
            ListAudioInputDevices();
            InitAudioGraph();
        }

        private async void InitAudioGraph()
        {
            try
            {
                await CreateAudioGraph();
            }
            catch (Exception exception)
            {
                error.Text = exception.Message;
            }
        }

        private void InitDeepSpeech()
        {
            string projectFolder = Directory.GetCurrentDirectory();
            string modelsFolder = Path.Combine(projectFolder, "models");
            string acousticModelPath = Path.Combine(modelsFolder, "deepspeech-0.8.0-models.pbmm");
            string scorerPath = Path.Combine(modelsFolder, "deepspeech-0.8.0-models.scorer");

            client = new DeepSpeechClient.DeepSpeech(acousticModelPath);
            client.EnableExternalScorer(scorerPath);
        }

        private async void ListAudioInputDevices()
        {
            inputDeviceList.Items.Clear();
            inputDevices = await DeviceInformation.FindAllAsync(DeviceClass.AudioCapture);

            foreach (var device in inputDevices)
            {
                inputDeviceList.Items.Add(device.Name);
            }

            inputDeviceList.SelectedIndex = 0;
            selectedInputDevice = inputDevices[0];
        }

        private async Task ResetAudioGraph()
        {
            if (graph != null)
            {
                graph.Stop();
                graph.Dispose();
            }

            await CreateAudioGraph();
        }

        private async Task CreateAudioGraph()
        {
            encoding = MediaEncodingProfile.CreateWav(AudioEncodingQuality.Low);
            encoding.Audio = AudioEncodingProperties.CreatePcm(16000, 1, 16);

            AudioGraphSettings settings = new AudioGraphSettings(AudioRenderCategory.Speech);
            settings.EncodingProperties = encoding.Audio;
            CreateAudioGraphResult result = await AudioGraph.CreateAsync(settings);

            if (result.Status != AudioGraphCreationStatus.Success)
            {
                throw new Exception($"AudioGraph creation failed because {result.Status.ToString()}");
            }

            graph = result.Graph;

            graph.UnrecoverableErrorOccurred += async (AudioGraph sender, AudioGraphUnrecoverableErrorOccurredEventArgs args) =>
            {
                await Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () => { 
                    error.Text = "Audio graph: UnrecoverableErrorOccurred"; 
                });
            };
        }

        private async Task<AudioFileInputNode> AttachFileInputNode(StorageFile file, TypedEventHandler<AudioFileInputNode, object> OnFileCompleted)
        {
            var fileInputNodeResult = await graph.CreateFileInputNodeAsync(file);
            if (fileInputNodeResult.Status != AudioFileNodeCreationStatus.Success)
            {
                error.Text = String.Format("FileInputNode creation failed because {0}", fileInputNodeResult.Status.ToString());
            }
            var fileInputNode = fileInputNodeResult.FileInputNode;
            fileInputNode.FileCompleted += OnFileCompleted;

            return fileInputNode;
        }

        private async Task<AudioDeviceOutputNode> AttachDeviceOutputNode(IAudioInputNode inputNode)
        {
            var deviceOutputNodeResult = await graph.CreateDeviceOutputNodeAsync();
            if (deviceOutputNodeResult.Status != AudioDeviceNodeCreationStatus.Success)
            {
                error.Text = String.Format("DeviceOutputNode creation failed because {0}", deviceOutputNodeResult.Status.ToString());
            }
            var deviceOutputNode = deviceOutputNodeResult.DeviceOutputNode;

            inputNode.AddOutgoingConnection(deviceOutputNode);

            return deviceOutputNode;
        }

        unsafe private void ProcessFrameOutput(AudioFrame frame)
        {
            using (AudioBuffer buffer = frame.LockBuffer(AudioBufferAccessMode.Read))
            using (IMemoryBufferReference reference = buffer.CreateReference())
            {
                ((IMemoryBufferByteAccess)reference).GetBuffer(out byte* dataInBytes, out uint capacityInBytes);

                if (capacityInBytes > 0)
                {
                    float* dataInFloats = (float*)dataInBytes;

                    short[] shorts = new short[capacityInBytes / 4];

                    for (int i = 0; i < capacityInBytes / 4; i++)
                    {
                        shorts[i] = (short)((65535 * dataInFloats[i] - 1) / 2);
                    }

                    bufferQueue.Enqueue(shorts);
                }
            }
        }

        private AudioFrameOutputNode AttachSpeechRecognitionMode(IAudioInputNode inputNode)
        {
            var speechRecognitionNode = graph.CreateFrameOutputNode(encoding.Audio);
            graph.QuantumStarted += (AudioGraph sender, object args) =>
            {
                AudioFrame frame = speechRecognitionNode.GetFrame();
                ProcessFrameOutput(frame);
            };

            inputNode.AddOutgoingConnection(speechRecognitionNode);
            return speechRecognitionNode;
        }

        private void startSpeechRecognition()
        {
            isFeeding = true;
            stream = client.CreateStream();
            var feedCount = 0;
            var decodeRate = 100; // decode every 100 feeds

            Task.Run(async () =>
            {
                while (isFeeding || !bufferQueue.IsEmpty)
                    if (!bufferQueue.IsEmpty && bufferQueue.TryDequeue(out short[] buffer))
                    {
                        client.FeedAudioContent(stream, buffer, Convert.ToUInt32(buffer.Length));

                        if (++feedCount % decodeRate == 0)
                        {
                            var transcription = client.IntermediateDecode(stream);
                            await Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
                            {
                                result.Text = transcription;
                            });
                        }
                    }
            });
        }

        private async Task stopSpeechRecognition()
        {
            isFeeding = false;
            while (!bufferQueue.IsEmpty) await Task.Delay(90);
            var transcription = client.FinishStream(stream);

            await Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                result.Text = transcription;
            });
        }

        private async Task<AudioDeviceInputNode> AttachDeviceInputNode()
        {
            var deviceInputNodeResult = await graph.CreateDeviceInputNodeAsync(MediaCategory.Speech, encoding.Audio, selectedInputDevice);

            if (deviceInputNodeResult.Status != AudioDeviceNodeCreationStatus.Success)
            {
                error.Text = String.Format("DeviceInputNode creation failed because {0}", deviceInputNodeResult.Status.ToString());
            }

            var deviceInputNode = deviceInputNodeResult.DeviceInputNode;
            return deviceInputNode;
        }

        private async void selectFileButton_Click(object sender, RoutedEventArgs e)
        {
            var picker = new Windows.Storage.Pickers.FileOpenPicker
            {
                ViewMode = Windows.Storage.Pickers.PickerViewMode.Thumbnail,
                SuggestedStartLocation = Windows.Storage.Pickers.PickerLocationId.Downloads
            };
            picker.FileTypeFilter.Add(".wav");

            audioFile = await picker.PickSingleFileAsync();
            selectedFile.Text = audioFile.Path;

            playAudioFileButton.IsEnabled = true;
            transcribeAudioFileButton.IsEnabled = true;
        }

        private async void playAudioFileButton_Click(object sender, RoutedEventArgs e)
        {
            await ResetAudioGraph();

            var fileInputNode = await AttachFileInputNode(audioFile, async (AudioFileInputNode senderNode, object args) =>
            {
                graph.Stop();
                senderNode.Reset();
                await Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
                {
                    playAudioFileButton.IsEnabled = true;
                    transcribeAudioFileButton.IsEnabled = true;
                });
            });


            await AttachDeviceOutputNode(fileInputNode);

            graph.Start();
            playAudioFileButton.IsEnabled = false;
            transcribeAudioFileButton.IsEnabled = false;
        }

        private async void transcribeAudioFileButton_Click(object sender, RoutedEventArgs e)
        {
            await ResetAudioGraph();

            var fileInputNode = await AttachFileInputNode(audioFile, async (AudioFileInputNode senderNode, object args) =>
            {
                graph.Stop();
                senderNode.Reset();
                await stopSpeechRecognition();
                await Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
                {
                    playAudioFileButton.IsEnabled = true;
                    transcribeAudioFileButton.IsEnabled = true;
                });
            });

            var speechRecognitionNode = AttachSpeechRecognitionMode(fileInputNode);

            startSpeechRecognition();
            graph.Start();

            playAudioFileButton.IsEnabled = false;
            transcribeAudioFileButton.IsEnabled = false;
        }

        private async void recordButton_Click(object sender, RoutedEventArgs e)
        {
            await ResetAudioGraph();
            var deviceInputNode = await AttachDeviceInputNode();
            AttachSpeechRecognitionMode(deviceInputNode);

            stopRecordButton.IsEnabled = true;
            recordButton.IsEnabled = false;
            
            startSpeechRecognition();
            graph.Start();
        }
        private async void stopRecordButton_Click(object sender, RoutedEventArgs e)
        {
            stopRecordButton.IsEnabled = false;
            recordButton.IsEnabled = true;

            graph.Stop();
            await stopSpeechRecognition();
        }

        private void inputDeviceList_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            selectedInputDevice = inputDevices[inputDeviceList.SelectedIndex];
        }
    }
}
