# What does it do ? 
This android app captures auido from android devices microphone and sends these (stereo 44100hz 16-bit pcm raw auido data) to specified address as UDP packets.


<img src="https://user-images.githubusercontent.com/14927769/137024200-72c5b5a6-c983-4a2e-ae22-34902f36d092.jpg" width="200" />
<img src="https://user-images.githubusercontent.com/14927769/137024220-7a591109-7677-4d98-9408-eada1d926c29.JPG" width="600" />


# To capture and render these UDP packets:
A python script used with python-sounddevice module in EXAMPLES folder. https://github.com/ozayt/RecorderUDP/blob/master/EXAMPLES/recorderUDPswp100.py 

python-sounddevice module:
https://github.com/spatialaudio/python-sounddevice/

# To use this real-time audio as input to other programs in Windows. 
VB-CABLE Virtual Audio Device can be used https://vb-audio.com/Cable/ with the python script. 
Use CABLE Input in python script as output channel and CABLE Output can be used as microphone device in other programs.
