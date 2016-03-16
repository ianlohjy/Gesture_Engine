// Implementation of the $1 Gesture Recognition Method
// Uses the PVector class from Processing for point and line storage
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
		gestureResolution = 32;
		gestureTemplates = new ArrayList<Gesture>();
	}
	
	public String recogniseGesture(Gesture candidate)
	{
		if(gestureTemplates.size() == 0) 
		{
			PApplet.println("No gesture templates stored");
			return "No gesture templates stored";
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
		
		PApplet.println("Gesture " + gestureTemplates.get(indexWithHighestScore).gestureName + " has the highest score (" + highestScoreSoFar + ")");
		PApplet.println("Template scores are :");
		PApplet.println(templateScores);
		return gestureTemplates.get(indexWithHighestScore).gestureName + " (" + (int)(highestScoreSoFar*100) + "%)";
	}
	
	public void addGesture(Gesture gesture, String gestureName)
	{
		gesture.gestureName = gestureName;
		gestureTemplates.add(gesture);
		PApplet.println("Gesture added with name '" + gestureName + "'");
	}
}
