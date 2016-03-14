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
	
	/** Array of all objects in the level*/
	protected Array<GameObject> levelObjects;
	/** Current image for this object. May change over time. */
	protected FilmStrip animator;
	/** Reference to texture origin */
	protected Vector2 origin;
	/** Radius of the object (used for collisions) */
	protected float radius;
	/** Maximum width of the climbing wall*/
	protected int wallWidth;
	/** How quickly energy is lost
	 *  Range from 0.0 - 1.0*/
	protected float energyLossFactor;
	/** How slippery handholds are
	 *  Range from 0.0 - 1.0*/
	protected float slipFactor;
	/** How strong gravity is*/
	protected float gravity;
	
	/**
	 * Returns the texture of this level
	 *
	 * @return Returns the texture of this level
	 */
	public Texture getTexture() {
		return animator == null ? null : animator.getTexture();
	}
	
	
	/**
	 * Sets the texture of this level.
	 *
	 * @param texture the texture of this level
	 */
	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture,1,1,1);
		radius = animator.getRegionHeight() / 2.0f;
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
	}
	
	/**
	 * Returns the max wall width of this level
	 *
	 * @return Returns the max wall width of this level
	 */
	public int getWallWidth(){
		return wallWidth;
	}
	
	/**
	 * Sets the max wall width of this level.
	 *
	 * @param width the max wall width of this level
	 */
	public void setWallWidth(int width){
		wallWidth = width;
	}
	
	/**
	 * Returns the energy loss factor of this level
	 *
	 * @return Returns the energy loss factor of this level
	 */
	public float getEnergyLoss(){
		return energyLossFactor;
	}
		
	/**
	 * Sets the energy loss factor of this level.
	 *
	 * @param energyLoss the energy loss factor of this level
	 */
	public void setEnergyLoss(float energyLoss){
		energyLossFactor = energyLoss;
	}
	
	/**
	 * Returns the slip factor of this level
	 *
	 * @return Returns the slip factor of this level
	 */
	public float getSlip(){
		return slipFactor;
	}
	
	/**
	 * Sets the slip factor of this level.
	 *
	 * @param slip the slip factor of this level
	 */
	public void setSlip(float slip){
		slipFactor = slip;
	}
	
	public float getGravity(){
		return gravity;
	}
	
	/**
	 * Sets the gravity of this level.
	 *
	 * @param g the gravity of this level
	 */
	public void setGravity(float g){
		gravity = g;
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
	
	/** Creates a trivial LevelModel*/
	public LevelModel(){
		
	}
	
}
