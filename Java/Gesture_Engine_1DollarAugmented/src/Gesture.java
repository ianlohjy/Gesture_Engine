import java.io.File;
import java.util.ArrayList;
import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class Gesture
{
	String gestureName; 
	PVector centroid;
	float indicativeAngle; // In radians
	ArrayList<PVector> points;
	float[] scaleFactor;
	float referenceSquareLength;
	int gestureResolution;
	
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
		if(inputPoints != null)
		{
			points = resample(inputPoints);
			centroid = calcCentroid(points);
			indicativeAngle = findIndicativeAngle(points,centroid);
			points = zeroIndicativeAngle(points,centroid,indicativeAngle);
			scaleFactor = findScaleFactor(inputPoints, referenceSquareLength);
			points = scalePoints(points, scaleFactor[0], scaleFactor[1], centroid);	
			
			PApplet.println(this.gestureName);
			PApplet.println(this.centroid);
			PApplet.println(this.indicativeAngle);
			PApplet.println(this.points);
			PApplet.println(this.scaleFactor);
			PApplet.println(this.referenceSquareLength);
			PApplet.println(this.gestureResolution);
		}
		else 
		{
			PApplet.println("Could not process gesture. No points provided");
		}
	}
	
	public void loadFromJson(String file)
	{	// Loads and overwrites gesture information with date from a json file
		JSONObject loadedJson = new JSONObject();
		loadedJson = PApplet.loadJSONObject(new File(file));
		
		gestureName = loadedJson.getString("gesture_name");
		indicativeAngle = loadedJson.getFloat("indicative_angle");
		gestureResolution = loadedJson.getInt("gesture_resolution");
		referenceSquareLength = loadedJson.getInt("reference_square_length");
		
		JSONArray loadedCentroid = loadedJson.getJSONArray("centroid");
		this.centroid = new PVector(loadedCentroid.getFloat(0),loadedCentroid.getFloat(1));
		
		JSONArray loadedScaleFactor = loadedJson.getJSONArray("scale_factor");
		this.scaleFactor = new float[2];
		this.scaleFactor[0] = loadedScaleFactor.getFloat(0);
		this.scaleFactor[1] = loadedScaleFactor.getFloat(1);
		
		this.points = new ArrayList<PVector>();
		JSONArray loadedPoints = loadedJson.getJSONArray("points");
		for(int p=0; p<loadedPoints.size(); p++)
		{
			JSONArray loadedPoint = loadedPoints.getJSONArray(p);
			this.points.add(new PVector(loadedPoint.getFloat(0),loadedPoint.getFloat(1)));
		}
		
		PApplet.println(this.gestureName);
		PApplet.println(this.centroid);
		PApplet.println(this.indicativeAngle);
		PApplet.println(this.points);
		PApplet.println(this.scaleFactor);
		PApplet.println(this.referenceSquareLength);
		PApplet.println(this.gestureResolution);
	}
	
	public void saveAsJson(String folderPath)
	{	// Saves data out as a JSON file (with a .gst extension)
		JSONObject gesture = new JSONObject();
		
		gesture.setString("gesture_name", gestureName);
		gesture.setFloat("reference_square_length", referenceSquareLength);
		gesture.setInt("gesture_resolution", gestureResolution);
		
		JSONArray scaleFactor = new JSONArray();
		scaleFactor.append(this.scaleFactor[0]);
		scaleFactor.append(this.scaleFactor[1]);
		gesture.setJSONArray("scale_factor", scaleFactor);
		
		gesture.setFloat("indicative_angle", indicativeAngle);
		
		JSONArray centroid = new JSONArray();
		centroid.append(this.centroid.x);
		centroid.append(this.centroid.y);
		gesture.setJSONArray("centroid", centroid);
		
		JSONArray points = new JSONArray();
		for (int p=0; p<this.points.size();p++) 
		{
			JSONArray point = new JSONArray();
			point.append(this.points.get(p).x);
			point.append(this.points.get(p).y);
			points.append(point);
		}
		gesture.setJSONArray("points", points);	
		
		// Check to see if there are other gestures with the same name
		// If so, append a number to it
		
		String saveFileName = gestureName;
		int saveFilePostfix = 0;
		File dir = new File(folderPath);
		File [] files = dir.listFiles();
		
		for(File file : files)
		{
			PApplet.println(file.toString());
			String gestureFileName = file.toString();
			
			//sgestureFileName = gestureFileName.replaceAll("\\\\", "a");
			String[] gestureFileNameSplit = gestureFileName.split("[\\\\/]");
			gestureFileName = gestureFileNameSplit[gestureFileNameSplit.length-1];
			gestureFileName = gestureFileName.split("[.]")[0];
			int indexToPostFix = gestureFileName.lastIndexOf("_");
			
			if(indexToPostFix != -1)
			{
				String checkPostFix = gestureFileName.substring(indexToPostFix+1, gestureFileName.length());
				if(checkPostFix.matches("\\d+$"))
				{
					PApplet.println("Postfix number detected: " + checkPostFix);
					gestureFileName = gestureFileName.substring(0,indexToPostFix);
					
					PApplet.println(saveFileName);
					PApplet.println(gestureFileName);
					
					if(gestureFileName.equals(saveFileName))
					{	// If the name is the same and has a postfix number, check the number to see if its higher than the current one 
						// If so, change the postfix number to be 1 more than the highest one found so far
						if(Integer.parseInt(checkPostFix) >= saveFilePostfix)
						{
							saveFilePostfix = Integer.parseInt(checkPostFix)+1;
							PApplet.println("New save file postfix: " + saveFilePostfix);
						}
					}
				}
			}
		}
		
		gesture.save(new File(folderPath + saveFileName + "_" + saveFilePostfix + ".gst"), null);
		PApplet.println("Gesture saved");	
	}
}

