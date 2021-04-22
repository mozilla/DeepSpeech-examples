# -*- coding: utf-8 -*-
"""
Created on Thu Apr 22 14:23:29 2021

@author: lenovo
"""
import os

def createFolder(directory):
    try:
        if not os.path.exists(directory):
            os.makedirs(directory)
    except OSError:
        print ('Error: Creating directory. ' +  directory)
        

# Example
createFolder('Converted_16K_SR_audio')
# Creates a folder in the current directory called data