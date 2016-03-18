// Implementation of the $1 Gesture Recognition Method
// Uses the PVector class from Processing for point and line storage
import java.io.File;
import java.util.ArrayList;
import processing.core.*;


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
	
	public float[] recogniseGesture(Gesture candidate)
	{
		if(gestureTemplates.size() == 0) 
		{
			PApplet.println("No gesture templates stored");
			return null;
		}
		
		float avgDistance[]    = new float[gestureTemplates.size()];
		float templateScores[] = new float[gestureTemplates.size()];
		
		// Find the average distance of between corresponding points for candidate gesture and gesture template
		for(int gt=0; gt<gestureTemplates.size(); gt++)
		{
			for(int p=0; p<gestureTemplates.get(gt).points.size(); p++)
			{
				Gesture template = gestureTemplates.get(gt);
				avgDistance[gt] += PApplet.dist(template.points.get(p).x, template.points.get(p).y, candidate.points.get(p).x, candidate.points.get(p).y);
			}
			avgDistance[gt]    = avgDistance[gt]/candidate.gestureResolution;
			templateScores[gt] = 1-( avgDistance[gt]/(0.5f*PApplet.dist(0f,0f,referenceSquareLength,referenceSquareLength)) );
		}
		
		// Find highest score
		int indexWithHighestScore = 0;
		float highestScoreSoFar   = 0;
		
		for(int s=0; s<templateScores.length; s++)
		{
			if(templateScores[s] > highestScoreSoFar)
			{
				indexWithHighestScore = s;
				highestScoreSoFar = templateScores[s];
			}
		}
		
		PApplet.println("Best guess: " + gestureTemplates.get(indexWithHighestScore).gestureName + " (" +(highestScoreSoFar*100) + "%)");
		//PApplet.println("Template scores are :");
		//PApplet.println(templateScores);
		float[] result = {indexWithHighestScore, highestScoreSoFar};
		return result;
	}
	
	public void addGesture(Gesture gesture, String gestureName)
	{
		gesture.gestureName = gestureName;
		gestureTemplates.add(gesture);
		PApplet.println("Gesture added with name '" + gestureName + "'");
		gesture.saveAsJson("./gestures/");
	}
	
	public void loadGestures(String folderPath)
	{
		File dir = new File(folderPath);
		File [] files = dir.listFiles();
		
		for(File file : files)
		{
			String[] pathToLoad = file.toString().split("[\\\\/]");
			String fileToLoad   = pathToLoad[pathToLoad.length-1];
			
			// Check to see if the file is a .gst file
			if(fileToLoad.split("[.]")[1].equals("gst"))
			{
				Gesture loadedGesture = new Gesture(this,null);
				loadedGesture.loadFromJson(file.toString());
				gestureTemplates.add(loadedGesture);
				PApplet.println("Gesture " + fileToLoad + " loaded");
			}
		}
	}
}
