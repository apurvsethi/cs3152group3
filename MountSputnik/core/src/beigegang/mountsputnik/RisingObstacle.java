package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RisingObstacle {
	private TextureRegion texture;
	private float speed;
	private float height;
	
	public RisingObstacle(TextureRegion t, float speed){
		texture = t;
		this.speed = speed;
		height = 0;
	}
	
	public RisingObstacle(TextureRegion t, float speed, float h){
		texture = t;
		this.speed = speed;
		height = h;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public TextureRegion getTexture() {
		return texture;
	}
	
	public void setTexture(TextureRegion t) {
		texture = t;
	}
	
	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}
	
}
