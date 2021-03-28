#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import datetime

def write_to_file(file_handle, inferred_text, line_count, limits):
    """Write the inferred text to SRT file
    Follows a specific format for SRT files

    Args:
        file_handle : SRT file handle
        inferred_text : text to be written
        line_count : subtitle line count 
        limits : starting and ending times for text
    """
    
    d = str(datetime.timedelta(seconds=float(limits[0])))
    try:
        from_dur = "0" + str(d.split(".")[0]) + "," + str(d.split(".")[-1][:2])
    except:
        from_dur = "0" + str(d) + "," + "00"
        
    d = str(datetime.timedelta(seconds=float(limits[1])))
    try:
        to_dur = "0" + str(d.split(".")[0]) + "," + str(d.split(".")[-1][:2])
    except:
        to_dur = "0" + str(d) + "," + "00"
        
    file_handle.write(str(line_count) + "\n")
    file_handle.write(from_dur + " --> " + to_dur + "\n")
    file_handle.write(inferred_text + "\n\n")