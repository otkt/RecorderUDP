# What does it do ? 
This android app captures auido from android devices microphone and sends these (stereo 44100hz 16-bit pcm raw auido data) to specified address as UDP packets.

# To capture and render these UDP packets:
A python script used with python-sounddevice module in EXAMPLES folder. 
https://github.com/spatialaudio/python-sounddevice/

# To use this real-time audio as input to other programs in Windows. 
VB-CABLE Virtual Audio Device can be used https://vb-audio.com/Cable/ with the python script. 
Use CABLE Input in python script as output channel and CABLE Output can be used as microphone device in other programs.
