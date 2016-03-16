import java.util.ArrayList;

import processing.core.*;

public class GestureRecognition extends PApplet
{
	ArrayList<PVector> drawnPoints;
	Gesture lastGesture;
	GestureEngine gestureEngine;
	String textBoxEntry = "";
	String recognisedGesture = "No gesture templates stored";
	
	public static void main(String args[])
	{
		PApplet.main(new String[] { "GestureRecognition" });
	}
	
	public void settings()
	{
		size(300,300);
	}
	
	public void setup()
	{
		background(255);
		gestureEngine = new GestureEngine();
	}
	
	public void draw()
	{
		background(255);
		
		noStroke();
		fill(0,50);
		ellipse(mouseX, mouseY, 10, 10);
		
		drawPoints(drawnPoints);
		/*
		if(lastGesture != null)
		{	
			pushMatrix();
			translate(lastGesture.centroid.x, lastGesture.centroid.y);
			drawPoints(lastGesture.points);
			popMatrix();
			fill(255,0,0);
			noStroke();
			ellipse(lastGesture.centroid.x, lastGesture.centroid.y, 5, 5);
			stroke(255,0,0);
			strokeWeight(2);
			line(lastGesture.centroid.x, lastGesture.centroid.y, lastGesture.points.get(0).x + lastGesture.centroid.x, lastGesture.points.get(0).y + lastGesture.centroid.y);
		}
		*/
		
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
		text(recognisedGesture, 6, 18);
	}
	
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
			lastGesture = null;
			drawnPoints = new ArrayList<PVector>();
		}
		
		if(mouseY > height-20 && mouseX > width/2)
		{	// If mouse clicks the "ADD GESTURE" button
			if(lastGesture != null)
			{
				if(textBoxEntry.isEmpty())
				{
					gestureEngine.addGesture(lastGesture, "NO_NAME");
				}
				else
				{
					gestureEngine.addGesture(lastGesture, textBoxEntry);
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
				lastGesture = new Gesture(gestureEngine, drawnPoints);
				recognisedGesture = gestureEngine.recogniseGesture(lastGesture);
			}
			//drawnPoints = new ArrayList<PVector>();
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

}
