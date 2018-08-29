package com.gdx.mythic;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Widget {
	
	// coordinates for the widget itself
	private float x;
	private float y;
	private float xShift;
	private float yShift;
	// coordinates for the source (user)
	private float sourceX;
	private float sourceY;
	// coordinates for the target
	private float targetX;
	private float targetY;
	// current rotation and origin relative to lower-left corner of image
	private float rotation;
	private float rotationX;
	private float rotationY;
	// rotation for the current movement phase
	private float phaseRotation; 
	// duration of the current phase; a second variable is used for display calculations
	private float duration;
	private float originalDuration;
	// controls the creation of the widget
	private String widgetData;
	// image data. need to think of a better way to do this for static images, but it works for now
	private Animation<TextureRegion> animation;
	// other/self-explanatory
	private float stateTime;

	public Widget(String widgetData_, float sourceX_, float sourceY_, float targetX_, float targetY_, Animation<TextureRegion> animation_) {
			widgetData = widgetData_;
			sourceX = sourceX_;
			sourceY = sourceY_;
			targetX = targetX_;
			targetY = targetY_;
			animation = animation_;
			stateTime = 0;
			
			// check the type of positioning
			if(widgetData.charAt(0) == 't') {
				x = targetX;
				y = targetY;
			}
			else if(widgetData.charAt(0) == 's') {
				x = sourceX;
				y = sourceY;
			}
			else {
				// throw exception here
			}
			widgetData = widgetData.substring(1);
			
			// grab the x offset
			x += consumeValue('x');
			y += consumeValue('y');
			rotation = consumeValue('r');
			rotationX = consumeValue('x');
			rotationY = consumeValue('y');
			consumeChar(',');
			refreshWidget();
	}

	public void draw(SpriteBatch batch) {
		batch.draw(animation.getKeyFrame(stateTime), x, y, rotationX, rotationY, 1, 1, 1, 1, rotation);
	}
	
	public void update(float dt) {
		stateTime += dt;
		x += dt * xShift / originalDuration;
		y += dt * yShift / originalDuration;
		rotation += phaseRotation * dt / originalDuration;
		duration -= dt;
		if(duration <= 0) {
			refreshWidget();
		}
		
	}
	
	public void refreshWidget() {
		if(widgetData == "end") {
			return;
		}
		duration = consumeValue('l');
		originalDuration = duration;
		xShift = consumeValue('x');
		yShift = consumeValue('y');
		phaseRotation = consumeValue('r');
		consumeChar(',');
	}
	
	// helper function to get a number from a String. this is returned as a String instead of another type
	// so that a) the calling function can do what it wants with it, and b) the number of characters is easy to find
	// might move this to a utility class later
	public String getNumber(String s) {
		String result = "";
		int index = 0;
		// handle the case for decimals
		if(s.charAt(0) == '.') {
			result += ".";
			index++;
		}
		while(Character.isDigit(s.charAt(index)))  {
			result += s.substring(index, index+1);
			index++;
		}
		return result;
	} 

/* this grabs a value in the form of [ch][#/t/s], and consumes that part of the widgetData string in the process.
this expects that either a number or t/s follow ch, and it also expects that if t/s follow then ch is either x, y or r. 
so it can calculate the xShift, yShift or rotation to the target's or the source's coordinates.
note that rotation using t/s is calculated from target to source or source to target. this means I don't yet have a way
to create something anywhere on the map and have it point directly at the target/source... yet */

	public float consumeValue(char ch) {
		boolean negative = false;
		float rVal = 0.0f;

		if(widgetData.charAt(0) == ch) {
			widgetData = widgetData.substring(1);
			if(widgetData.charAt(0) == 't') {
				widgetData = widgetData.substring(1);
				if(ch == 'x') {
					rVal = targetX - x;
				}
				else if(ch == 'y') {
					rVal = targetY - y;
				}
				else if(ch == 'r') {
					rVal = getAngle(sourceX, sourceY, targetX, targetY);
				}
				else {
					// throw exception
				}
			}
			else if(widgetData.charAt(0) == 's') {
				widgetData = widgetData.substring(1);
				if(ch == 'x') {
					rVal = sourceX - x;
				}
				else if(ch == 'y') {
					rVal = sourceY - y;
				}
				else if(ch == 'r') {
					rVal = getAngle(targetX, targetY, sourceX, sourceY);
				}
				else {
					// throw exception
				}
			}
			else {
				if(widgetData.charAt(0) == '-') {
					negative = true;
					widgetData = widgetData.substring(1);
				}
				String temp = getNumber(widgetData);
				if(temp.length() == 0) {
					// throw exception
				}
				if(negative) {
					rVal = -Float.valueOf(temp);
				}
				else {
					rVal = Float.valueOf(temp);
				}
				widgetData = widgetData.substring(temp.length());
			}
		}
		else {
			// throw exception
		}
		return rVal;
	}
	
	public void consumeChar(char ch) {
		if(widgetData.charAt(0) == ch) {
			widgetData = widgetData.substring(1);
		}
	}

	public boolean isFinished() {
		return widgetData == "end" || widgetData.length() < 3;
	}
	/* finds the angle between two points, using a line pointing to the right as 0 degrees 
	for example, the angle between (0,0) and (-1,0) would be 180 
	note that libgdx has a much easier way of doing this, but I was already halfway done when I
	found that out, so I just finished my own method instead */

	public float getAngle(Vector2 coord1, Vector2 coord2) {
		// v1 is a vector pointing right from the origin 
		// v2 is a vector directly between the two points, pointing from coord1 to coord2
		Vector2 v1 = new Vector2(1,0);
		Vector2 v2 = new Vector2(coord2.x - coord1.x, coord2.y - coord1.y);
		
		// this could be done elsewhere, but the code for computing the magnitude is just a bit unwieldy
		float v1Mag = (float) Math.sqrt(Math.pow(v1.x,2) + Math.pow(v1.y,2));
		float v2Mag = (float) Math.sqrt(Math.pow(v2.x,2) + Math.pow(v2.y,2));
	 
		float cos = ((v1.x * v2.x) + (v1.y * v2.y)) / (v1Mag * v2Mag);

		return (float) Math.toDegrees(Math.acos(cos));
	}


	// this is like the above method, but uses floats instead of vectors for the initial points
	public float getAngle(float x1, float y1, float x2, float y2) {
		float rval = 0;
		// v1 is a vector pointing right from the origin 
		// v2 is a vector directly between the two points, pointing from coord1 to coord2
		Vector2 v1 = new Vector2(1,0);
		Vector2 v2 = new Vector2(x2 - x1, y2 - y1);
		
		// this could be done elsewhere, but the code for computing the magnitude is just a bit unwieldy
		float v1Mag = (float) Math.sqrt(Math.pow(v1.x,2) + Math.pow(v1.y,2));
		float v2Mag = (float) Math.sqrt(Math.pow(v2.x,2) + Math.pow(v2.y,2));
	 
		float cos = ((v1.x * v2.x) + (v1.y * v2.y)) / (v1Mag * v2Mag);
		if(y2 - y1 > 0) {
			rval = (float) Math.toDegrees(Math.acos(cos));
		}
		else {
			rval = -(float) Math.toDegrees(Math.acos(cos));
		}
		return rval;
	}
}
