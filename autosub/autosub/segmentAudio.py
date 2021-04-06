#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import numpy as np
from pydub import AudioSegment
import scipy.io.wavfile as wavfile
import featureExtraction as FE
import trainAudio as TA


def read_audio_file(input_file):
    """This function returns a numpy array that stores the audio samples of a
    specified WAV file
    
    Args:
        input_file : audio from input video file
    """

    sampling_rate = -1
    signal = np.array([])
    try:
        audiofile = AudioSegment.from_file(input_file)
        data = np.array([])
        if audiofile.sample_width == 2:
            data = np.fromstring(audiofile._data, np.int16)
        elif audiofile.sample_width == 4:
            data = np.fromstring(audiofile._data, np.int32)

        if data.size > 0:
            sampling_rate = audiofile.frame_rate
            temp_signal = []
            for chn in list(range(audiofile.channels)):
                temp_signal.append(data[chn::audiofile.channels])
            signal = np.array(temp_signal).T
    except:
        print("Error: file not found or other I/O error. (DECODING FAILED)")

    if signal.ndim == 2 and signal.shape[1] == 1:
        signal = signal.flatten()

    return sampling_rate, signal

def smooth_moving_avg(signal, window=11):
    window = int(window)
    if signal.ndim != 1:
        raise ValueError("")
    if signal.size < window:
        raise ValueError("Input vector needs to be bigger than window size.")
    if window < 3:
        return signal
    s = np.r_[2 * signal[0] - signal[window - 1::-1],
              signal, 2 * signal[-1] - signal[-1:-window:-1]]
    w = np.ones(window, 'd')
    y = np.convolve(w/w.sum(), s, mode='same')
    
    return y[window:-window + 1]

def stereo_to_mono(signal):
    """This function converts the input signal to MONO (if it is STEREO)
    
    Args:
        signal: audio file stored in a Numpy array
    """

    if signal.ndim == 2:
        if signal.shape[1] == 1:
            signal = signal.flatten()
        else:
            if signal.shape[1] == 2:
                signal = (signal[:, 1] / 2) + (signal[:, 0] / 2)
                
    return signal

def silence_removal(signal, sampling_rate, st_win, st_step, smooth_window=0.5,
                    weight=0.5):
    """Event Detection (silence removal)
    
    Args:
        signal : the input audio signal
        sampling_rate : sampling freq
        st_win, st_step : window size and step in seconds
        smoothWindow : (optinal) smooth window (in seconds)
        weight : (optinal) weight factor (0 < weight < 1) the higher, the more strict
        plot : (optinal) True if results are to be plotted
        
    Returns:
        seg_limits : list of segment limits in seconds (e.g [[0.1, 0.9], 
                [1.4, 3.0]] means that the resulting segments 
                are (0.1 - 0.9) seconds and (1.4, 3.0) seconds
    """

    if weight >= 1:
        weight = 0.99
    if weight <= 0:
        weight = 0.01

    # Step 1: feature extraction
    signal = stereo_to_mono(signal)
    st_feats, _ = FE.feature_extraction(signal, sampling_rate,
                                         st_win * sampling_rate,
                                         st_step * sampling_rate)

    # Step 2: train binary svm classifier of low vs high energy frames
    # keep only the energy short-term sequence (2nd feature)
    st_energy = st_feats[1, :]
    en = np.sort(st_energy)
    # number of 10% of the total short-term windows
    st_windows_fraction = int(len(en) / 10)

    # compute "lower" 10% energy threshold
    low_threshold = np.mean(en[0:st_windows_fraction]) + 1e-15

    # compute "higher" 10% energy threshold
    high_threshold = np.mean(en[-st_windows_fraction:-1]) + 1e-15

    # get all features that correspond to low energy
    low_energy = st_feats[:, np.where(st_energy <= low_threshold)[0]]

    # get all features that correspond to high energy
    high_energy = st_feats[:, np.where(st_energy >= high_threshold)[0]]

    # form the binary classification task and ...
    features = [low_energy.T, high_energy.T]
    # normalize and train the respective svm probabilistic model

    # (ONSET vs SILENCE)
    features_norm, mean, std = TA.normalize_features(features)
    svm = TA.train_svm(features_norm, 1.0)

    # Step 3: compute onset probability based on the trained svm
    prob_on_set = []
    for index in range(st_feats.shape[1]):
        # for each frame
        cur_fv = (st_feats[:, index] - mean) / std
        # get svm probability (that it belongs to the ONSET class)
        prob_on_set.append(svm.predict_proba(cur_fv.reshape(1, -1))[0][1])
    prob_on_set = np.array(prob_on_set)

    # smooth probability:
    prob_on_set = smooth_moving_avg(prob_on_set, smooth_window / st_step)

    # Step 4A: detect onset frame indices:
    prog_on_set_sort = np.sort(prob_on_set)

    # find probability Threshold as a weighted average
    # of top 10% and lower 10% of the values
    nt = int(prog_on_set_sort.shape[0] / 10)
    threshold = (np.mean((1 - weight) * prog_on_set_sort[0:nt]) +
         weight * np.mean(prog_on_set_sort[-nt::]))

    max_indices = np.where(prob_on_set > threshold)[0]
    # get the indices of the frames that satisfy the thresholding
    index = 0
    seg_limits = []
    time_clusters = []

    # Step 4B: group frame indices to onset segments
    while index < len(max_indices):
        # for each of the detected onset indices
        cur_cluster = [max_indices[index]]
        if index == len(max_indices)-1:
            break
        while max_indices[index+1] - cur_cluster[-1] <= 2:
            cur_cluster.append(max_indices[index+1])
            index += 1
            if index == len(max_indices)-1:
                break
        index += 1
        time_clusters.append(cur_cluster)
        seg_limits.append([cur_cluster[0] * st_step,
                           cur_cluster[-1] * st_step])

    # Step 5: Post process: remove very small segments:
    min_duration = 0.2
    seg_limits_2 = []
    for s_lim in seg_limits:
        if s_lim[1] - s_lim[0] > min_duration:
            seg_limits_2.append(s_lim)
    seg_limits = seg_limits_2

    return seg_limits

def silenceRemoval(input_file, smoothing_window = 1.0, weight = 0.2):
    """Remove silence segments from an audio file and split on those segments

    Args:
        input_file : audio from input video file
        smoothing : Smoothing window size in seconds. Defaults to 1.0.
        weight : Weight factor in (0,1). Defaults to 0.5.
    """
    
    if not os.path.isfile(input_file):
        raise Exception("Input audio file not found!")

    [fs, x] = read_audio_file(input_file)
    segmentLimits = silence_removal(x, fs, 0.05, 0.05, smoothing_window, weight)
    
    for i, s in enumerate(segmentLimits):
        strOut = "{0:s}_{1:.3f}-{2:.3f}.wav".format(input_file[0:-4], s[0], s[1])
        wavfile.write(strOut, fs, x[int(fs * s[0]):int(fs * s[1])])

#if __name__ == "__main__":
#    silenceRemoval("video.wav")