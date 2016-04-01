import java.util.ArrayList;
import processing.core.*;


public class GestureExample extends PApplet
{
	ArrayList<PVector> drawnPoints;
	GestureEngine gestureEngine;
	String textBoxEntry = "";
	String bestGuess = "No gesture templates stored";
	float inferredAngle = 0;
	
	public static void main(String args[])
	{
		PApplet.main(new String[] { "GestureExample" });
	}
	
	// PROCESSING APPLET //
	public void settings()
	{
		size(300,300);
	}
	
	public void setup()
	{
		background(255);
		gestureEngine = new GestureEngine();
		gestureEngine.loadGestureTemplatesFrom("./gestures/");
	}
	
	public void draw()
	{
		background(255);
		drawPoints(drawnPoints);
		drawUI();
	}
	
	// EVENT HANDLING //
	public void keyTyped() 
	{
		println("typed " + key + " " + keyCode);
		
		if((int)key != 8)
		{
			textBoxEntry += key;
		}
		
		if((int)key == 8 && textBoxEntry.length()>0)
		{
			textBoxEntry = textBoxEntry.substring(0, textBoxEntry.length()-1);
		}
		
		println(textBoxEntry);
	}
	
	public void mousePressed()
	{
		drawnPoints = new ArrayList<PVector>();
		if(mouseY < height-20)
		{	
			drawnPoints = new ArrayList<PVector>();
			drawnPoints.add(new PVector(mouseX,mouseY));
		}
		
		if(mouseY > height-20 && mouseX > width/2)
		{	// If mouse clicks the "ADD GESTURE" button
			if(drawnPoints != null)
			{
				if(textBoxEntry.isEmpty())
				{
					gestureEngine.trainGesture(drawnPoints, "NO_NAME");
				}
				else
				{
					gestureEngine.trainGesture(drawnPoints, textBoxEntry);
				}
			}	
		}
	}
	
	public void mouseReleased()
	{
		if(mouseY < height-20)
		{
			if(drawnPoints.size() > 1)
			{
				GestureEngine.GestureResponse result = gestureEngine.recogniseGesture(drawnPoints);
				if(result != null)
				{
					bestGuess = result.bestGuess + " (" + (int)(result.score) + "%)";
					inferredAngle = result.inferredAngle;
					//println("Inferred Angle is " + degrees(inferredAngle));
				}
			}
		}
	}
	
	public void mouseDragged()
	{
		if(mouseY < height-20)
		{
			if(drawnPoints.size() == 0)
			{
				drawnPoints.add(new PVector(mouseX, mouseY));
			}
			else 
			{
				PVector lastPoint = drawnPoints.get(drawnPoints.size()-1);
				float dstFrmLstPt = dist(lastPoint.x, lastPoint.y, mouseX, mouseY);
				
				if(dstFrmLstPt > 2)
				{
					drawnPoints.add(new PVector(mouseX, mouseY));
				}
			}
		}
	}
	
	// RENDERING & DISPLAY //
	public void drawPoints(ArrayList<PVector> points)
	{
		noFill();
		
		if(points != null) 
		{
			if(points.size() == 1)
			{
				strokeWeight(1);
				point(points.get(0).x, points.get(0).y);
			}
			else if(points.size() > 1)
			{
				for(int p=0; p<points.size()-1; p++)
				{
					stroke(0);
					strokeWeight(1);
					point(points.get(p).x, points.get(p).y);
					
					stroke(0,200);
					strokeWeight(5);
					line(points.get(p).x, points.get(p).y, points.get(p+1).x, points.get(p+1).y);
				}	
			}
		}
	}

	public void drawUI()
	{
		stroke(0);
		strokeWeight(1);
		fill(255);
		rect(0,height-20,width/2,19);
		
		if(mouseY > height-20 && mouseX > width/2)
		{	
			fill(0);
			rect(width/2,height-20,width/2-1,19);
			fill(255);
			textSize(12);
			text("ADD GESTURE", width/2+5, height-6);
		}
		else 
		{
			fill(140);
			rect(width/2,height-20,width/2-1,19);
			fill(0);
			textSize(12);
			text("ADD GESTURE", width/2+5, height-6);
		}
		
		fill(0);
		textSize(12);
		text(textBoxEntry, 5, height-6);
		
		textSize(18);
		text(bestGuess, 6, 18);
		
		noFill();
		strokeWeight(1);
		stroke(0);
		ellipse(35, 60, 60, 60);
		
		pushMatrix();
		translate(35, 60);
		rotate(inferredAngle);
		line(0,0,-25,0);
		popMatrix();
	}
	
}
