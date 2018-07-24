package com.gdx.mythic;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Cell {
	protected Actor actor;
	protected Thing thing;
	protected boolean passable;
	
	public abstract TextureRegion getTexture(float st);
	public abstract Cell clone();
	
	public Cell(Actor a, Thing t, boolean b) {
		actor = a;
		thing = t;
		passable = b;
	}

	
	public Actor getActor() {
		return actor;
	}
	
	public void setActor(Actor a) {
		actor = a;
	}
	
	public void removeActor() {
		actor = null;
	}
	
	public Thing getThing() {
		return thing;
	}
	
	public void setThing(Thing t) {
		thing = t;
	}
	
	public boolean isPassable() {
		if(!(actor== null)) {
			if(actor.getID() == "player") {
				return true;
			}
			return false;
		}
		return passable;
	}
	
	public void setPassable(boolean b) {
		passable = b;
	}
	
}
