package com.gdx.mythic;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StaticCell extends Cell {
	private TextureRegion texture;

	public StaticCell(Actor a, Thing th, boolean b, TextureRegion te) {
		super(a, th, b);
		texture = te;
	}

	@Override
	public TextureRegion getTexture(float st) {
		return texture;
	}

	public void setTexture(TextureRegion t) {
		texture = t;
	}
	
	public Cell clone() {
		return new StaticCell(actor, thing, passable, texture);
	}
}
