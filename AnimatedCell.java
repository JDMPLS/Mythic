package com.gdx.mythic;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimatedCell extends Cell {
	private Animation<TextureRegion> animation;

	public AnimatedCell(Actor ac, Thing t, boolean b, Animation<TextureRegion> an) {
		super(ac, t, b);
		animation = an;
	}

	

	@Override
	public TextureRegion getTexture(float st) {
		return animation.getKeyFrame(st, true);
	}
	
	public void setTexture(Animation<TextureRegion> a) {
		animation = a;
	}



	@Override
	public Cell clone() {
		return new AnimatedCell(actor, thing, passable, animation);
	}
	
}
