package com.algorithmicaudio.javareaperfilewriter;

/**
 * Reaper.java
 * 
 * This class allows the user to create algorithmic music 
 * using audio files by rendering them into the Reaper
 * file format.
 * 
 * Reaper doesn't give out their file information, so this
 * is all reverse-engineered.
 * 
 * This code has no connection to Cockos or Reaper. It is
 * totally unofficial and not guaranteed in any way. Use
 * at your own risk.
 * 
 * If you find/fix any bugs, or if you add new functionality,
 * please open a pull request on GitHub!
 */

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.lang.IllegalArgumentException;

public class ReaperFileWriter
{
	public int TrackCount = 0;
	public double TempoBpm = 140.0;
	ArrayList2d<String> Events = null;
	
	/**
	 * Default constructor starts with a single track.
	 */
	public ReaperFileWriter()
	{
		this(1); // default to 1 track
	}
	
	/**
	 * The full constructor.
	 * 
	 * @param NewTrackCount	The number of tracks in the output file.
	 */
	public ReaperFileWriter(int NewTrackCount)
	{
		// instantiate our array of events
		TrackCount = NewTrackCount;
		Events = new ArrayList2d<String>();
		Events.ensureCapacity(TrackCount);
	}
	
	/**
	 * Create a new audio event and add it to the proper list.
	 * 
	 * @param TrackNumber The index of the track to which to add the audio event. Tracks begin at 1.
	 * @param SourceFile The audio file to add to the track.
	 * @param StartTimeInSeconds When the audio file should begin.
	 * @param DurationInSeconds How long the audio file should last. This class will NOT extrapolate duration from the audio file.
	 * @param Rate The playback rate for the file.
	 */
	public void AddAudioEvent(int TrackNumber, String SourceFile, double StartTimeInSeconds, double DurationInSeconds, double Rate)
	{
		this.AddAudioEvent(TrackNumber, SourceFile, StartTimeInSeconds, DurationInSeconds, Rate, 0.0, 1.0);
	}
	
	/**
	 * Create a new audio event and add it to the proper list.
	 * 
	 * @param TrackNumber The index of the track to which to add the audio event. Tracks begin at 1.
	 * @param SourceFile The audio file to add to the track.
	 * @param StartTimeInSeconds When the audio file should begin.
	 * @param DurationInSeconds How long the audio file should last. This class will NOT extrapolate duration from the audio file.
	 * @param Rate The playback rate for the file.
	 * @param Pan The position of the audio file in the stereo field.
	 * @param Volume The volume applied to the audio file.
	 */
	public void AddAudioEvent(int TrackNumber, String SourceFile, double StartTimeInSeconds, double DurationInSeconds, double Rate, double Pan, double Volume)
	{
		if(TrackNumber < 1 || TrackNumber > this.TrackCount) throw new IllegalArgumentException("Invalid track number.");
		
		String NewEvent = FormatAudioEvent(SourceFile, StartTimeInSeconds, DurationInSeconds, Volume, Pan, Rate);
		Events.Add(NewEvent, TrackNumber - 1);
	}
	// return a properly formatted audio event in RPP format 
	private String FormatAudioEvent(String SourceFile, double StartTimeInSeconds, double DurationInSeconds, double Volume, double Pan, double Rate)
	{
		// generate the IDs for the event
		String EventId1 = java.util.UUID.randomUUID().toString();
		String EventId2 = java.util.UUID.randomUUID().toString();
		
		// insert the few variables that we want to change
		String NewEvent = "    <ITEM\r\n      POSITION " + StartTimeInSeconds + "\r\n      SNAPOFFS 0.00000000000000\r\n      LENGTH " + DurationInSeconds + "\r\n      LOOP 0\r\n      ALLTAKES 0\r\n      SEL 0\r\n   ";
		NewEvent += "FADEIN 1 0.010000 0.000000 1 0 0.000000\r\n      FADEOUT 1 0.010000 0.000000 1 0 0.000000\r\n      MUTE 0\r\n      IGUID {" + EventId1 + "}\r\n   ";
		if( Rate >= 0.0 )
		{
			NewEvent += "IID 1\r\n      NAME \"\"\r\n      VOLPAN " + String.valueOf(Volume) + " " + String.valueOf(Pan) + " 1.000000 -1.000000\r\n      SOFFS 0.00000000000000\r\n      PLAYRATE " + Rate + " 0 0.00000000000000 -1\r\n      ";
			NewEvent += "CHANMODE 0\r\n      GUID {" + EventId2 + "}\r\n      ";
			NewEvent += "<SOURCE WAVE\r\n        FILE \"" + SourceFile + "\"\r\n      >\r\n    >\r\n";
		}
		else
		{
			// if the file is reversed, then the markup is slightly different
			NewEvent += "IID 1\r\n      NAME \"\"\r\n      VOLPAN " + String.valueOf(Volume) + " " + String.valueOf(Pan) + " 1.000000 -1.000000\r\n      SOFFS 0.00000000000000\r\n      PLAYRATE " + (Rate * -1.0) + " 0 0.00000000000000 -1\r\n      ";
			NewEvent += "CHANMODE 0\r\n      GUID {" + EventId2 + "}\r\n   ";
			NewEvent += "<SOURCE SECTION\r\n        LENGTH " + DurationInSeconds + "\r\n        MODE 3\r\n        STARTPOS 0.00000000000000\r\n        OVERLAP 0.01000000000000\r\n        ";
			NewEvent += "<SOURCE WAVE\r\n          FILE \"" + SourceFile + "\"\r\n        >\r\n      >\r\n    >\r\n";
		}
		
		return NewEvent;
	}
	
	// then save the text dump of the sounds used
	public void Save(String TargetFile)
	{
		try
		{
			FileWriter outFile = new FileWriter(TargetFile);
			PrintWriter out = new PrintWriter(outFile);
			
			// first, write the basic project info to the file
			out.println("<REAPER_PROJECT 0.1 \"4.31/x64\" 1355375848");
			out.println("  RIPPLE 0");
			out.println("  GROUPOVERRIDE 0 0 0");
			out.println("  AUTOXFADE 1");
			out.println("  ENVATTACH 1");
			out.println("  MIXERUIFLAGS 11 48");
			out.println("  PEAKGAIN 1.00000000000000");
			out.println("  FEEDBACK 0");
			out.println("  PANLAW 1.00000000000000");
			out.println("  PROJOFFS 0.00000000000000 0");
			out.println("  MAXPROJLEN 0 600.00000000000000");
			out.println("  GRID 3199 8 1.00000000000000 8 1.00000000000000 0");
			out.println("  TIMEMODE 1 0 -1");
			out.println("  PANMODE 3");
			out.println("  CURSOR 0.00000000000000");
			out.println("  ZOOM 35.85960414000000 0 0");
			out.println("  VZOOMEX 6");
			out.println("  USE_REC_CFG 0");
			out.println("  RECMODE 1");
			out.println("  SMPTESYNC 0 23.976024 100 40 1000 300 0 0.000000 1");
			out.println("  LOOP 1");
			out.println("  LOOPGRAN 0 4.00000000000000");
			out.println("  RECORD_PATH \"\" \"\"");
			out.println("  <RECORD_CFG");
			out.println("    ZXZhdxgB");
			out.println("  >");
			out.println("  <APPLYFX_CFG");
			out.println("  >");
			out.println("  RENDER_FILE \"\"");
			out.println("  RENDER_PATTERN \"\"");
			out.println("  RENDER_FMT 0 2 0");
			out.println("  RENDER_1X 0");
			out.println("  RENDER_RANGE 1 0.00000000000000 0.00000000000000");
			out.println("  RENDER_RESAMPLE 3 0 1");
			out.println("  RENDER_ADDTOPROJ 0");
			out.println("  RENDER_STEMS 0");
			out.println("  RENDER_DITHER 0");
			out.println("  TIMELOCKMODE 1");
			out.println("  TEMPOENVLOCKMODE 1");
			out.println("  ITEMMIX 0");
			out.println("  DEFPITCHMODE 393216");
			out.println("  TAKELANE 1");
			out.println("  SAMPLERATE 44100 0 0");
			out.println("  <RENDER_CFG");
			out.println("  >");
			out.println("  LOCK 1");
			out.println("  <METRONOME 6 2.000000");
			out.println("    VOL 0.250000 0.125000");
			out.println("    FREQ 800 1600 1");
			out.println("    BEATLEN 4");
			out.println("    SAMPLES \"\" \"\"");
			out.println("  >");
			out.println("  GLOBAL_AUTO -1");
			out.println("  TEMPO " + String.valueOf(TempoBpm) + " 4 4");
			out.println("  PLAYRATE 1.00000000000000 0 0.25000 4.00000");
			out.println("  SELECTION 0.00000000000000 0.00000000000000");
			out.println("  SELECTION2 0.00000000000000 0.00000000000000");
			out.println("  MASTERAUTOMODE 0");
			out.println("  MASTERTRACKHEIGHT 0");
			out.println("  MASTERPEAKCOL 16576");
			out.println("  MASTERMUTESOLO 0");
			out.println("  MASTERTRACKVIEW 0 0.666700 0.500000 0.500000 -1 -1 -1");
			out.println("  MASTERHWOUT 0 0 1.00000000000000 0.00000000000000 0 0 0 -1.00000000000000");
			out.println("  MASTER_NCH 2 2");
			out.println("  MASTER_VOLUME 1.00000000000000 0.00000000000000 -1.00000000000000 -1.00000000000000 1.00000000000000");
			out.println("  MASTER_PANMODE -1");
			out.println("  MASTER_FX 1");
			out.println("  MASTER_SEL 0");
			out.println("  <MASTERPLAYSPEEDENV");
			out.println("    ACT 0");
			out.println("    VIS 0 1 1.000000");
			out.println("    LANEHEIGHT 0 0");
			out.println("    ARM 0");
			out.println("    DEFSHAPE 0 -1 -1");
			out.println("  >");
			out.println("  <TEMPOENVEX");
			out.println("    ACT 0");
			out.println("    VIS 1 0 1.000000");
			out.println("    LANEHEIGHT 0 0");
			out.println("    ARM 0");
			out.println("    DEFSHAPE 1 -1 -1");
			out.println("  >");
			out.println("  <PROJBAY");
			out.println("  >");
			
			for( int i = 0; i < Events.getNumRows(); i++ )
			{
				// first, set up the data for this track
				String TrackId = java.util.UUID.randomUUID().toString();
				out.println("  <TRACK '{" + TrackId + "}'");
				out.println("    NAME \"\"");
				out.println("    PEAKCOL 16576");
				out.println("    BEAT -1");
				out.println("    AUTOMODE 0");
				out.println("    VOLPAN 1.00000000000000 0.00000000000000 -1.00000000000000 -1.00000000000000 1.00000000000000");
				out.println("    MUTESOLO 0 0 0");
				out.println("    IPHASE 0");
				out.println("    ISBUS 0 0");
				out.println("    BUSCOMP 0 0");
				out.println("    SHOWINMIX 1 0.666700 0.500000 1 0.500000 -1 -1 -1");
				out.println("    FREEMODE 0");
				out.println("    SEL 1");
				out.println("    REC 0 0 0 0 0 0 0");
				out.println("    VU 2");
				out.println("    TRACKHEIGHT 0 0");
				out.println("    INQ 0 0 0 0.5000000000 100 0 0 100");
				out.println("    NCHAN 2");
				out.println("    FX 1");
				out.println("    TRACKID {" + TrackId + "}");
				out.println("    PERF 0");
				out.println("    MIDIOUT -1");
				out.println("    MAINSEND 1 0");
				// loop through each event in this track
				for( int j = 0; j < Events.getNumCols(i); j++ )
				{
					// then append each event
					out.println(Events.get(i, j));
				}
				// end the current track
				out.println("  >");
			}
			// close out the project
			out.println(">");
			out.close();
		}
		catch(Exception ex)
		{
			System.out.println("Exception while trying to save Reaper file: " + ex.getMessage());
			System.out.println(ex.getStackTrace());
		}
	}
}

/**
 * 2d ArrayList abstraction taken from http://www.javaprogrammingforums.com/java-programming-tutorials/696-multi-dimension-arraylist-example.html
 *
 * @param <Type> The type of values held by this 2d ArrayList.
 */
class ArrayList2d<Type>
{
	ArrayList<ArrayList<Type>>	array;
 
	public ArrayList2d()
	{
		array = new ArrayList<ArrayList<Type>>();
	}
 
	/**
	 * ensures a minimum capacity of num rows. Note that this does not guarantee
	 * that there are that many rows.
	 * 
	 * @param num
	 */
	public void ensureCapacity(int num)
	{
		array.ensureCapacity(num);
	}
	/**
	 * Ensures that the given row has at least the given capacity. Note that
	 * this method will also ensure that getNumRows() >= row
	 * 
	 * @param row
	 * @param num
	 */
	public void ensureCapacity(int row, int num)
	{
		ensureCapacity(row);
		while (row < getNumRows())
		{
			array.add(new ArrayList<Type>());
		}
		array.get(row).ensureCapacity(num);
	}
 
	/**
	 * Adds an item at the end of the specified row. This will guarantee that at least row rows exist.
	 */
	public void Add(Type data, int row)
	{
		ensureCapacity(row);
		while(row >= getNumRows())
		{
			array.add(new ArrayList<Type>());
		}
		array.get(row).add(data);
	}
 
	public Type get(int row, int col)
	{
		return array.get(row).get(col);
	}
 
	public void set(int row, int col, Type data)
	{
		array.get(row).set(col,data);
	}
 
	public void remove(int row, int col)
	{
		array.get(row).remove(col);
	}
 
	public boolean contains(Type data)
	{
		for (int i = 0; i < array.size(); i++)
		{
			if (array.get(i).contains(data))
			{
				return true;
			}
		}
		return false;
	}
 
	public int getNumRows()
	{
		return array.size();
	}
 
	public int getNumCols(int row)
	{
		return array.get(row).size();
	}
}
