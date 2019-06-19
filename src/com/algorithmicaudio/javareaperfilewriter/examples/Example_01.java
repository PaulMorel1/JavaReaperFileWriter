package com.algorithmicaudio.javareaperfilewriter.examples;

import com.algorithmicaudio.javareaperfilewriter.*;

public class Example_01
{
	// this variable allows us to write to a Reaper project
	ReaperFileWriter ReaperOutput = null;
	
	public static void main(String[] args)
	{
		Example_01 ex = new Example_01();
	}
	
	public Example_01()
	{
		// set up our object for saving to a reaper project
		ReaperOutput = new ReaperFileWriter();
		
		// add the event to the reaper project
		ReaperOutput.AddAudioEvent(1, "beep.wav", 0.0, 10.0, 1.0);
		
		// save the reaper file
        ReaperOutput.Save("Example_01.RPP");
        
        System.out.println("Done!");
	}
}
