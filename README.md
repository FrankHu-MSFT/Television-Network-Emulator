# Television-Network-Emulator
Inspired by kurosan. 

Confident enough to release v1. 

This program essentially emulates a television network. It will play videos according to what the time is. i.e 1:30 will play the folder of videos that you designated at 1:30, and once it's 2:00 it will switch folders.

Randomize button works,
Can drag and drop files in the file list to change order
full screen implemented
Seek bar implemented
play/pause

Utilizes a timer that runs every 30 minutes.




## Ideas :
## ===========

Save Video Time in timeblock and video place, to play the video at time X:XX on video Y in timeblock Z. 
That way it properly handles saving times for videos next week. 

Implementing different times is possible, easily through a timer that simply checks every 1 minute what time block it should be in. Could easily be implented with the nexttimeblock method. 



## Issues :
## ============

Saving time blocks still not working, requires restructuring data structures, or creating a method chain that saves the respective file lists and a loading method that loads all variables, would need to save in a structured way... 
