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


<img src="https://user-images.githubusercontent.com/14927769/137028083-435d9386-2789-4084-816b-b1db1d2707b4.JPG" width="400" />

<img src="https://user-images.githubusercontent.com/14927769/137028098-3b47d7f7-ecad-464f-ad77-98f7c7e1005a.JPG" width="400" />

<img src="https://user-images.githubusercontent.com/14927769/137028106-06e356f0-8b3e-4072-bcda-959e83d63261.JPG" width="400" />
