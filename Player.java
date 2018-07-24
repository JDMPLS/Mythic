package com.gdx.mythic;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player extends Actor {

	public Player(Animation<TextureRegion> animation_, float x_, float y_, String actorID_) {
		super(animation_, x_, y_, actorID_);
		// TODO Auto-generated constructor stub
	}

	// note that we use floats for one reason and one reason only: batch.draw() LOVES floats.
	
}


