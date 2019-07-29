# Television-Network-Emulator
Inspired by kurosan. 

This program essentially emulates a television network. It will play videos according to what the time is. i.e 1:30 will play the folder of videos that you designated at 1:30, and once it's 2:00 it will switch folders.

Randomize button works,
Can drag and drop files in the file list to change order
full screen implemented
Seek bar implemented
play/pause
Utilizes a timer that runs every 30 minutes.

## How to use

In order to use this TV Network Emulator, you will need a Java version that is compatible with Java 1.8. 
The jar file can be found below.
https://github.com/frankhu1234/Television-Network-Emulator/blob/master/Kur0se/TVEmulator(v1.04).jar

Once you've ran the Jar file, there will be two accordion expand/collapse items, Settings is for loading each timeblock with videos and the Video accordion is for viewing. 

You will need to load each timeblock with a directory that contains the video you are interested in playing, and once you've properly loaded every timeblock, it will auto play per the timeblocks. 

You can sort the play order by dragging and dropping in the Files Found accordion drop down. 

You can load the folder path in the current time block accordion drop down. It has a randomize button that will randomize all the videos in the playlist and the start button will begin playing the timeblock.

The timeblocks are broken up into different times per 24 hour format. 

I.e. Monday | 0:0 is the very beginning of the day at 12AM. 
It will then continue accordingly, it will also play based on the timezone your computer is in. 

**The title of the Java Application will actually show you the current timeblock. See picture below**
https://i.imgur.com/7IpzCd5.png

In order to start playing, you will need to load the current timeblock that you are in, i.e. if it's 7:30pm, you will need to have a properly loaded timeblock at 19:30 to play. 



## Support

If there are any issues in regards to this sample, please file a GitHub issue. This sample is not supported by Azure Support or Microsoft nor is this associated with Microsoft.


## Ideas 


1. Save Video Time in timeblock and video place, to play the video at time X:XX on video Y in timeblock Z. That way it properly handles saving times for videos next week. 

2. Implementing different times is possible, easily through a timer that simply checks every 1 minute what time block it should be in. Could easily be implented with the nexttimeblock method. 



## Issues 

1. Saving time blocks still not working, requires restructuring data structures, or creating a method chain that saves the respective file lists and a loading method that loads all variables, would need to save in a structured way... 
