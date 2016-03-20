package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class ExtremityModel extends PartModel{

	/** How hard this extremity can push
	 *  Range from 0.0 - 1.0*/
	protected float pushFactor;
	/** How hard this extremity can pull
	 *  Range from 0.0 - 1.0*/
	protected float pullFactor;
	/** Whether or not this extremity is gripping*/
	protected boolean isGripping;
	/** Texture for when extremity is gripping */ 
	protected Texture grip; 
	/** Texture for when extremity is not gripping */ 
	protected Texture notGrip; 
	
	@Override
	public ObjectType getType() {
		return ObjectType.EXTREMITY;
	}
	
	/**
	 * Returns the push factor of this object.
	 *
	 * @return the push factor of this object.
	 */
	public float getPush(){
		return pushFactor;
	}
	
	/**
	 * Sets the push factor of this object.
	 *
	 * @param push the push factor of this extremity
	 */
	public void setPush(float push){
		pushFactor = push;
	}
	
	/**
	 * Returns the pull factor of this object.
	 *
	 * @return the pull factor of this object.
	 */
	public float getPull(){
		return pullFactor;
	}
	
	/**
	 * Sets the pull factor of this object.
	 *
	 * @param pull the pull factor of this extremity
	 */
	public void setPull(float pull){
		pullFactor = pull;
	}
	
	/**
	 * Returns the whether or not this extremity is gripping
	 *
	 * @return Returns the whether or not this extremity is gripping
	 */
	public boolean isGripping(){
		return isGripping;
	}
	
	/** Sets the value of isGripping to true*/
	public void grip(){
		isGripping = true;
		setTexture(grip); 
		//TODO: change animation for gripping?
	}
	
	/** Sets the value of isGripping to false*/
	public void ungrip(){
		isGripping = false;
		setTexture(notGrip); 
		//TODO: change animation for releasing?
	}
	
	/** sets the grip texture for this extremity */ 
	public void setGripTexture(Texture t){
		grip = t; 
	}
	
	/** Contructs an ExtremityModel
	 * 
	 * @param push the push factor of this extremity
	 * @param pull the pull factor of this extremity
	 * @param t	the texture of this extremity
	 */
	public ExtremityModel(float push, float pull, float x, float y, Texture t){
		super(x, y, t);
		pushFactor= push;
		pullFactor = pull;
		isGripping = false;
		notGrip = t; 
	}
	
}
