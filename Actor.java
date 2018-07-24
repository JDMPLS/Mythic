package com.gdx.mythic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Actor {
	private String actorID;
	private Animation<TextureRegion> animation;
	private float x;
	private float y;
	private int range; // movement range

	public Actor(Animation<TextureRegion> animation_, float x_, float y_, String actorID_) {
		animation = animation_;
		x = x_;
		y = y_;
		actorID = actorID_;
		range = 10;
	}
	
	public String getID() {
		return actorID;
	}

	public float getX() {
		return x;
	}

	public void setX(float xval) {
		x = xval;
	}

	public float getY() {
		return y;
	}
	
	public void setY(float yval) {
		y = yval;
	}

	public TextureRegion getTexture(float stateTime) {
		return animation.getKeyFrame(stateTime, true);
	}

	public int getRange() {
		return range;
	}
}
