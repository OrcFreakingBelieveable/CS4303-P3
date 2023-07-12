<p align=center>Do you find university or life in general to be a constant struggle simply to keep your head above water?</p>

<p align=center>Do you consider escapism a form of cowardice?</p>

<p align=center><b>Then <i>Don’t Drown</i> could be the game for you!</b></p>

# CS4303-P3 Don't Drown
<i>An abstract life simulator in the form of a vertically scrolling platformer.</i>

## Objective and Controls 
The objective of the game is to reach the top platform of every level, collecting as many tokens along the way as you can whilst keeping ahead of the rising wave. If you spend time close to the wave then your stress level increases: your horizontal steering becomes more erratic as changes in direction are more extreme and it takes longer to come to rest when you stop steering. If you manage to put distance between yourself and the wave, then your stress will drop back down again. Your level of stress is reflected most obviously in the stress meter at the top of the screen, but it also affects the player character’s colour, the tempo of the music, and the quality of the game’s rendering. The music (which plays to the tune of the Alphabet Song/Twinkle Twinkle Little Star) can be toggled from the Settings menu.  

The controls are simple, primarily using the arrow keys: 
-	Press UP to jump if you are on a platform.
-	LEFT and RIGHT steer you horizontally, both whilst mid-air and when on platforms. 
-	DOWN can be used to drop through the platform you are currently on, which can come in handy if you are committed to getting all of a level’s tokens. 
-	SPACEBAR will briefly pause the wave once per level, but when it starts moving again it will be faster until it has made up for the pause. 
-	The game can be paused when mid-level by pressing P or Esc. 

You pass through the underside of platforms, but will bounce off of the right-hand side of the page, as well as off of the margin on the left-hand side of the page. 
Steering is stronger when on a platform than when mid-air, so jumps may require a runup to land successfully. If you stop steering before reaching the edge of a platform, or start steering the other way, then you will bounce at the edge rather than fall off the platform. 
 
## Difficulty and Debuffs 
Levels come in four difficulties (easy, medium, hard and very hard) and are grouped by debuff, of which there are seven:  

1.	None: no debuff; this is the basic game. The easy no debuff level acts as the tutorial level. 
1.	Overworked: every platform has a token on it, so if you want the perfect score then you cannot skip a single platform on your way to the top. 
1.	Panic Prone: every few seconds your stress will go up, even if you are far ahead of the wave. 
1.	Stress Motivated: your steering will be sluggish when you are not stressed enough, to the point that some jumps may be impossible to make: this forces you to carefully balance how far ahead of the wave you get. 
1.	Can’t Unwind: your stress will never reduce, no matter how far ahead of the wave you are. 
1.	Tunnel Vision: your vision is limited to a narrow horizontal slice of the screen, but you do not become stressed unless the wave is within that slice. Additionally, when your stress starts decreasing is relative to your current stress level and how long you have kept the wave out of sight, rather than just how far ahead of it you are. 
1.	Lacking Self-awareness: the stress bar is hidden, and the music and rendering are not affected by your stress level, but your steering still is, making it hard to predict how aggressively you are about to steer yourself.  

When the game is launched, a level is randomly generated for every debuff and difficulty pair, and these levels are consistent until the game is closed. From the Level Selector screen you can see your highest score for each level, as well as the amount of time you had spare for that run of the level (i.e. how much longer the wave would have taken to reach the top platform). There is also an arcade mode which generates an infinite number of random levels, with randomised debuff and difficulty pairs, noted beneath the stress bar. 
At higher difficulties the levels are taller and have more platforms per unit height. Easy levels have a slower wave speed and a page-spanning first platform to catch you if you fall at the first hurdle. 
The speed of the game is tied to the FPS, so in the Settings menu there are options to cap the framerate and slow down the game, which makes it easier. 

## Compilation Instructions 
Don’t Drown was made using the Processing library for Java, as well as the Minim library [1]. Required .jar files, as well as the licenses for Minim and the font used in the game, are included in the lib/ folder of the submission. 
Commands to be run from the DontDrown/ folder: 

    $ javac -cp lib/minim/*:lib/core.jar:src/ src/*.java
    $ java -cp lib/minim/*:lib/core.jar:src/ DontDrown

The class diagrams for my codebase (excluding my defined Enums) are included as a separate PDF. They were generated from the .class files, and as such may vary slightly from the .java files. 

## Further Details 
See `Don't Drown/CS4303 Don't Drown Report.pdf` for more information about the design and implementation of the game. 

## References 
1. D. Di Fede, “https://code.compartmental.net/minim/,” 3 August 2019. [Online]. Available: https://code.compartmental.net/minim/. [Accessed 10 May 2022].
