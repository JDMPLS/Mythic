package com.gdx.mythic;

import com.badlogic.gdx.graphics.Texture;

public abstract class Actor {
	private String actorID;
	private Texture texture;
	private float x;
	private float y;
	private int range; // movement range

	public Actor(Texture texture_, float x_, float y_, String actorID_) {
		texture = texture_;
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

	public Texture getTexture() {
		return texture;
	}

	public int getRange() {
		return range;
	}
}
