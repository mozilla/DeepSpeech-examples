#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import csv
import sys
import glob
import signal
import ntpath
import numpy as np
import sklearn.svm

shortTermWindow = 0.050
shortTermStep = 0.050
eps = 0.00000001


def train_svm(features, c_param, kernel='linear'):
    """Train a multi-class probabilitistic SVM classifier.
    Note:     This function is simply a wrapper to the sklearn functionality 
              for SVM training
              See function trainSVM_feature() to use a wrapper on both the 
              feature extraction and the SVM training
              (and parameter tuning) processes.
    Args:
        features : a list ([numOfClasses x 1]) whose elements 
                containt np matrices of features  each matrix 
                features[i] of class i is 
                [n_samples x numOfDimensions]
        c_param : SVM parameter C (cost of constraints violation)
        
    Returns:
        svm : the trained SVM variable

    NOTE:
        This function trains a linear-kernel SVM for a given C value.
        For a different kernel, other types of parameters should be provided.
    """

    feature_matrix, labels = features_to_matrix(features)
    svm = sklearn.svm.SVC(C=c_param, kernel=kernel, probability=True,
                          gamma='auto')
    svm.fit(feature_matrix, labels)

    return svm

def normalize_features(features):
    """This function normalizes a feature set to 0-mean and 1-std
    Used in most classifier trainning cases

    Args:
        features : list of feature matrices (each one of them is a np matrix)
        
    Returns:
        features_norm : list of NORMALIZED feature matrices
        mean : mean vector
        std : std vector
    """
    
    temp_feats = np.array([])

    for count, f in enumerate(features):
        if f.shape[0] > 0:
            if count == 0:
                temp_feats = f
            else:
                temp_feats = np.vstack((temp_feats, f))
            count += 1

    mean = np.mean(temp_feats, axis=0) + 1e-14
    std = np.std(temp_feats, axis=0) + 1e-14

    features_norm = []
    for f in features:
        ft = f.copy()
        for n_samples in range(f.shape[0]):
            ft[n_samples, :] = (ft[n_samples, :] - mean) / std
        features_norm.append(ft)
    return features_norm, mean, std


def features_to_matrix(features):
    """This function takes a list of feature matrices as argument and returns
    a single concatenated feature matrix and the respective class labels.

    Args:
        features : a list of feature matrices

    Returns:
        feature_matrix : a concatenated matrix of features
        labels : a vector of class indices
    """

    labels = np.array([])
    feature_matrix = np.array([])
    for i, f in enumerate(features):
        if i == 0:
            feature_matrix = f
            labels = i * np.ones((len(f), 1))
        else:
            feature_matrix = np.vstack((feature_matrix, f))
            labels = np.append(labels, i * np.ones((len(f), 1)))
            
    return feature_matrix, labels
