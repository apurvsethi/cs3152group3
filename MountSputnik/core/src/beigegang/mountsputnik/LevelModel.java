package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import beigegang.util.FilmStrip;

public class LevelModel {

	/** Enum to define different level types
	 *  At this point, only level for technical exists*/
	private enum LevelType {
		TECHNICAL,
	}
	
	protected Array<GameObject> levelObjects;
	
	/** Current image for this object. May change over time. */
	protected FilmStrip animator;
	/** Reference to texture origin */
	protected Vector2 origin;
	/** Radius of the object (used for collisions) */
	protected float radius;
	/** Maximum width of the climbing wall*/
	protected int wallWidth;
	/** How quickly energy is lost*/
	protected float energyLossFactor;
	/** How slippery handholds are*/
	protected float slipFactor;
	/** How strong gravity is*/
	protected float gravity;
	
	
	
	// ACCESSORS
	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture,1,1,1);
		radius = animator.getRegionHeight() / 2.0f;
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
	}
	
	public Texture getTexture() {
		return animator == null ? null : animator.getTexture();
	}
	
	public void setWallWidth(int width){
		wallWidth = width;
	}
	
	public int getWallWidth(){
		return wallWidth;
	}
	
	public void setEnergyLoss(float energyLoss){
		energyLossFactor = energyLoss;
	}
	
	public float getEnergyLoss(){
		return energyLossFactor;
	}
	
	public void setSlip(float slip){
		slipFactor = slip;
	}
	
	public float getSlip(){
		return slipFactor;
	}
	
	public void setGravity(float g){
		gravity = g;
	}
	
	public float getGravity(){
		return gravity;
	}
		
	/** Draws all of the objects in this level*/
	private void drawObjects(GameCanvas canvas){
		for (GameObject o : levelObjects){
			o.draw(canvas);
		}
	}

	/**
	 * Draws this object to the canvas
	 *
	 * There is only one drawing pass in this application, so you can draw the objects 
	 * in any order.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas) {
		canvas.draw(animator, Color.WHITE, origin.x, origin.y, 
					origin.x, origin.y, 0.0f, 1.0f, 1.f);
		drawObjects(canvas);
	}
	
	
	
	
}
