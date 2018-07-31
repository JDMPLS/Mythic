package com.gdx.mythic;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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
		duration -= dt;
		if(duration <= 0) {
			refreshWidget();
		}
		x += dt * xShift / originalDuration;
		y += dt * yShift / originalDuration;
		rotation += phaseRotation * dt / originalDuration;
		
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
	
	// checks for the presence of ch at the beginning of the string
	// if not present, throws exception. then checks for a number
	// after that character. if not present, throws an exception. 
	// if present, returns a float. has the side effect of setting the string to the substring
	// beginning at the end of the number (eg x1.0y1.0 would be set to y1.0)
	public float consumeValue(char ch) {
		boolean negative = false;
		float rVal = 0.0f;
		
		if(widgetData.charAt(0) == ch) {
			widgetData = widgetData.substring(1);
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
		return widgetData == "end";
	}

}
