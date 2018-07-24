package com.gdx.mythic;

import com.badlogic.gdx.graphics.Texture;

public class Player extends Actor {

	public Player(Texture texture_, float x_, float y_, String actorID_) {
		super(texture_, x_, y_, actorID_);
		// TODO Auto-generated constructor stub
	}

	// note that we use floats for one reason and one reason only: batch.draw() LOVES floats.
	
}


