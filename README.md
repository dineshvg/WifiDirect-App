# WifiDirect-App
A further modification of the Ultrasense app, where it can be controlled by another andorid device through Wifi direct. 

### Function

A wifi direct app where one app works as the receiver(server) and the other app works as the sender(client).
The server is embedded with a recorder app, the client using the controls sends integer values that are used to control the server. 
The server creates a (20 kHz) wave file makes a text file that functions as the markers for the wave file and also stops the wave file.
The controls for these functions are with the client.

The functionality is done using [P2P connections over Wifi](https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html)

### Application Screenshots
<img src="https://github.com/dineshvg/WifiDirect-App/blob/master/screenshot/Screenshot.png" width="440">
