package com.surface.render.widgets;

import com.surface.value.impl.BooleanValue;

public abstract class Widget extends BooleanValue {

	protected float x, y, width, height;

	public Widget(float x, float y, String name) {
		super(name, false);
		this.x = x;
		this.y = y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public abstract void render(int mouseX, int mouseY, float renderPartialTicks);
}
