// Implementation of the $1 Gesture Recognition Method
// Uses the PVector class from Processing for point and line storage
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

import processing.core.*;
import processing.data.JSONObject;

public class GestureEngine 
{
	ArrayList<Gesture> gestureTemplates;
	float referenceSquareLength;
	int gestureResolution;
	
	public GestureEngine()
	{
		referenceSquareLength = 100;
		gestureResolution = 64;
		gestureTemplates = new ArrayList<Gesture>();
	}
	
	public GestureResponse recogniseGesture(ArrayList<PVector> points)
	{
		Gesture candidate = new Gesture(this,points);
		return recogniseGesture(candidate);
	}
	
	public GestureResponse recogniseGesture(Gesture candidate)
	{
		if(gestureTemplates.size() == 0) 
		{
			PApplet.println("No gesture templates stored");
			return null;
		}
		
		// Setup a TreeMap for easier sorting
		TreeMap<Float, Gesture> templateScoresMap = new TreeMap<Float, Gesture>(Collections.reverseOrder());
		
		// Find the average distance of between corresponding points for candidate gesture and gesture template
		for(int gt=0; gt<gestureTemplates.size(); gt++)
		{
			float avgDistance = 0;
			Gesture template = gestureTemplates.get(gt);
			for(int p=0; p<gestureTemplates.get(gt).points.size(); p++)
			{
				avgDistance += PApplet.dist(template.points.get(p).x, template.points.get(p).y, candidate.points.get(p).x, candidate.points.get(p).y);
			}
			avgDistance = avgDistance/candidate.gestureResolution;
			// Calculate the correlation score between candidate and template
			templateScoresMap.put( (1-(avgDistance/(0.5f*PApplet.dist(0f,0f,referenceSquareLength,referenceSquareLength))))*100f, template);
		}
		
		// Move sorted TreeMap data into 2 different arrays for score and gesture. This is for easier handling within the GestureResponse class
		int curIteratorIndex            = 0;
		float[] scoresRanked            = new float[templateScoresMap.size()];
		Gesture[] gesturesRanked        = new Gesture[templateScoresMap.size()];
		Iterator<Float> treeMapIterator = templateScoresMap.keySet().iterator(); 
		
		while(treeMapIterator.hasNext())
		{ 
			scoresRanked[curIteratorIndex]   = treeMapIterator.next();
			gesturesRanked[curIteratorIndex] = templateScoresMap.get(scoresRanked[curIteratorIndex]);
			curIteratorIndex++;
		} 
		
		return new GestureResponse(gesturesRanked, scoresRanked);
	}
	
	public void trainGesture(ArrayList<PVector> points, String gestureName)
	{
		Gesture templateGesture = new Gesture(this,points);
		trainGesture(templateGesture, gestureName);
	}
	
	public void trainGesture(Gesture gesture, String gestureName)
	{
		gesture.gestureName = gestureName;
		gestureTemplates.add(gesture);
		PApplet.println("Gesture added with name '" + gestureName + "'");
		gesture.saveAsJson("./gestures/");
	}
	
	public boolean loadGestureTemplate(JSONObject json, boolean verbose)
	{
		try
		{
			Gesture loadedGesture = new Gesture(this,null);
			loadedGesture.loadFromJson(json, verbose);
			gestureTemplates.add(loadedGesture);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean loadGestureTemplate(String filePath, boolean verbose)
	{
		try
		{
			String[] pathToLoad = filePath.split("[\\\\/]");
			String fileToLoad   = pathToLoad[pathToLoad.length-1];
			
			if(fileToLoad.split("[.]")[1].equals("gst"))
			{
				Gesture loadedGesture = new Gesture(this,null);
				loadedGesture.loadFromJson(filePath, verbose);
				gestureTemplates.add(loadedGesture);
				
				if(verbose)
				{
					System.out.println("Gesture " + fileToLoad + " loaded successfully");
				}
			}
			else
			{
				return false;
			}
			
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean loadGestureTemplatesFromFolder(String folderPath, boolean verbose)
	{
		File dir = new File(folderPath);
		File [] files = dir.listFiles();
		
		try
		{
			for(File file : files)
			{
				String[] pathToLoad = file.toString().split("[\\\\/]");
				String fileToLoad   = pathToLoad[pathToLoad.length-1];
				
				// Check to see if the file is a .gst file
				if(fileToLoad.split("[.]")[1].equals("gst"))
				{
					Gesture loadedGesture = new Gesture(this,null);
					loadedGesture.loadFromJson(file.toString(), verbose);
					gestureTemplates.add(loadedGesture);
					if(verbose)
					{
						System.out.println("Gesture " + fileToLoad + " loaded successfully");
					}
				}
			}
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	class GestureResponse
	{
		String bestGuess; // This is the gesture name with the highest score
		Gesture bestGesture; // This is the gesture with the highest score
		float bestScore; // Recognition score of the best guess
		
		float[] scoresRanked;
		Gesture[] gesturesRanked;
		
		// These are empty values, but could be useful for those working with GestureResponse
		PVector startPoint;
		PVector endPoint;
		
		GestureResponse(Gesture[] gesturesRanked, float[] scoresRanked)
		{
			this.scoresRanked = scoresRanked;
			this.gesturesRanked = gesturesRanked;
			this.bestGesture = gesturesRanked[0];
			this.bestGuess = gesturesRanked[0].gestureName;
			this.bestScore = scoresRanked[0];
		}
		
		void printTopGuesses(int amount)
		{
			if(amount > gesturesRanked.length)
			{
				amount = gesturesRanked.length;
			}
			
			for(int g=0; g<amount; g++)
			{
				System.out.println("Guess " + g + " - " + gesturesRanked[g].gestureName + " (" + scoresRanked[g] + "%)");
			}
			
		}
	}
	
}
