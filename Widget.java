package com.gdx.mythic;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Widget {
	public abstract void update(float deltaTime);
	
	// coordinates for the widget itself
	private float x;
	private float y;
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
			x += consumeValue(widgetData, 'x');
			y += consumeValue(widgetData, 'y');
			rotation = consumeValue(widgetData, 'r');
			rotationX = consumeValue(widgetData, 'x');
			rotationY = consumeValue(widgetData, 'y');
	}

	public void draw(SpriteBatch batch) {
		// draw stuff here
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
			index++;if(s.charAt(0) == '.') {
				result += ".";
			}
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
	public float consumeValue(String s, char ch) {
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
	
	public Widget nextWidget() {
		// returns a new Widget based on the remaining text in widgetData
		// returns a null object if the text is "end"
		if(widgetData.length() == 3) {
			if(widgetData == "end") {
				return null;
			}
			else {
				// throw exception
			}
		}
		else {
			switch(widgetData.charAt(0)) {
			case 'w':
				//return new WaitWidget();
			default: 
				// throw exception?
			}
		}
		return null;
	}

	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}
}
