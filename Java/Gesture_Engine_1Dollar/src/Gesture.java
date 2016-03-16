import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

public class Gesture
{
	ArrayList<PVector> points;
	PVector centroid;
	float indicativeAngle; // In radians
	float referenceSquareLength;
	float[] scaleFactor;
	int gestureResolution;
	String gestureName; 
	
	Gesture(GestureEngine ge, ArrayList<PVector> points)
	{
		referenceSquareLength = ge.referenceSquareLength;
		gestureResolution = ge.gestureResolution;
		processPoints(points);
	}
	
	public double distDouble(double x1, double y1, double x2, double y2)
	{	// Finds the distance of a line in double
		return Math.sqrt( ((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) );
	}
	
	public double calcLength(ArrayList<PVector> points)
	{	// Returns the length of the line 
		double distance = 0;
		for(int p=0; p<points.size()-1; p++)
		{
			PVector curpoint = points.get(p);
			PVector nxtpoint = points.get(p+1);
			distance += distDouble(curpoint.x, curpoint.y, nxtpoint.x, nxtpoint.y);
		}
		return distance;
	}
	
	public double lerpDouble(double valueA, double valueB, double amount)
	{
		return (amount*(valueB-valueA))+valueA;
	}
	
	public PVector calcCentroid(ArrayList<PVector> points)
	{
		float sumX = 0 ;
		float sumY = 0;		
		for(int p=0; p<points.size(); p++)
		{
			sumX += points.get(p).x;
			sumY += points.get(p).y;
		}
		return new PVector(sumX/points.size(),sumY/points.size());
	}
	
	public ArrayList<PVector> resample(ArrayList<PVector> originalPoints)
	{	// Re-samples input points to the a set number of points
		ArrayList<PVector> resampledPoints = new ArrayList<PVector>();
		
		// 1. Find the total length the line
		double originalLength = calcLength(originalPoints);
		// 2. Divide by the number of points needed to find the length per division
		double divisionLength = originalLength/(gestureResolution-1);
		
		PApplet.println("Total Distance: " + originalLength + " | Division Length: " + divisionLength);
		
		// 3. Traverse along the line, adding a new point when the division length is reached.
	 	double curTraversedDist = 0; // Distance traversed since last division
		int nextPointIndex = 1; // The next forward index to check from
		PVector lastCheckedPoint = originalPoints.get(0);
		
		for(int r=0; r<gestureResolution; r++)
		{
			if(r==0)
			{
				resampledPoints.add(originalPoints.get(0));
				PApplet.println("Added point " + r);
			} 
			else
			{
				while(true)
				{
					if(nextPointIndex == originalPoints.size())
					{	// If there are no more points to traverse, continue along in the direction of the last available line segment
						double neededDistance = divisionLength-curTraversedDist; // Find the amount of distance needed to reach the next point
						PVector lastDivision  = new PVector(originalPoints.get(originalPoints.size()-1).x - lastCheckedPoint.x, originalPoints.get(originalPoints.size()-1).y - lastCheckedPoint.y ); // Create a new vector with the points of the last segment 
						lastDivision.setMag((float)neededDistance); // Set that last segment vector to the needed distance
						
						resampledPoints.add(new PVector(lastCheckedPoint.x + lastDivision.x,lastCheckedPoint.y + lastDivision.y));
						PApplet.println("Added point " + r);
						lastCheckedPoint = resampledPoints.get(resampledPoints.size()-1);
						curTraversedDist = 0;
						break;
					} 
					else 
					{
						curTraversedDist += distDouble(lastCheckedPoint.x, lastCheckedPoint.y, originalPoints.get(nextPointIndex).x, originalPoints.get(nextPointIndex).y);
						
						if(curTraversedDist>=divisionLength)
						{
							double lerpAmount = divisionLength/curTraversedDist;
							double newPointX  = lerpDouble(lastCheckedPoint.x,originalPoints.get(nextPointIndex).x,lerpAmount);
							double newPointY  = lerpDouble(lastCheckedPoint.y,originalPoints.get(nextPointIndex).y,lerpAmount);		
									
							resampledPoints.add(new PVector((float)newPointX,(float)newPointY));
							PApplet.println("Added point " + r);
							curTraversedDist = 0;
							lastCheckedPoint = resampledPoints.get(resampledPoints.size()-1);
							break;
						}
						else if(curTraversedDist<divisionLength)
						{
							lastCheckedPoint = originalPoints.get(nextPointIndex);
							nextPointIndex++;
						}
					}
				}
			}
		}
		return resampledPoints;
	}
	
	public float findIndicativeAngle(ArrayList<PVector> points, PVector centroid)
	{
		PVector indicativeAngleVector = new PVector(centroid.x-points.get(0).x, centroid.y-points.get(0).y);
		return indicativeAngleVector.heading();
	}
	
	public ArrayList<PVector> zeroIndicativeAngle(ArrayList<PVector> points, PVector centroid, float indicativeAngle)
	{
		// Rotates points about centroid by the opposite of its indicative angle
		ArrayList<PVector> rotatedPoints = new ArrayList<PVector>();
		
		for(int p=0; p<points.size(); p++)
		{
			PVector rotatedPoint = new PVector(points.get(p).x-centroid.x, points.get(p).y-centroid.y);
			rotatedPoint.rotate(-indicativeAngle);
			rotatedPoints.add(new PVector(rotatedPoint.x + centroid.x, rotatedPoint.y + centroid.y));
		}
		return rotatedPoints;
	}
	
	public float[] findScaleFactor(ArrayList<PVector> points, float refSquareLength)
	{
		float minX = points.get(0).x;
		float maxX = points.get(0).x;
		float minY = points.get(0).y;
		float maxY = points.get(0).y;
		
		for(int p=0; p<points.size(); p++)
		{
			if(points.get(p).x < minX)
			{
				minX = points.get(p).x;
			} 
			
			if(points.get(p).x > maxX)
			{
				maxX = points.get(p).x;
			}
			
			if(points.get(p).y < minY)
			{
				minY = points.get(p).y;
			} 
			
			if(points.get(p).y > maxY)
			{
				maxY = points.get(p).y;
			}
		}
		
		float width  = maxX-minX;
		float height = maxY-minY;

		float scale[] = {refSquareLength/width, refSquareLength/height};
		return scale;
	}

	public ArrayList<PVector> scalePoints(ArrayList<PVector> points, float scaleX, float scaleY, PVector centroid)
	{
		ArrayList<PVector> scaledPoints = new ArrayList<PVector>();		
		for(int p=0; p<points.size(); p++)
		{
			scaledPoints.add(new PVector((points.get(p).x-centroid.x)*scaleX, (points.get(p).y-centroid.y)*scaleY));
		}
		return scaledPoints;
	}
	
	public void processPoints(ArrayList<PVector> inputPoints)
	{
		points = resample(inputPoints);
		centroid = calcCentroid(points);
		indicativeAngle = findIndicativeAngle(points,centroid);
		points = zeroIndicativeAngle(points,centroid,indicativeAngle);
		scaleFactor = findScaleFactor(inputPoints, referenceSquareLength);
		points = scalePoints(points, scaleFactor[0], scaleFactor[1], centroid);	
	}
	
}

