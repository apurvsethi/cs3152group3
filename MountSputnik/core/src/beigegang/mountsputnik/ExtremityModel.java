package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class ExtremityModel extends PartModel{

	/** How hard this extremity can push
	 *  Range from 0.0 - 1.0*/
	private float pushFactor;
	/** How hard this extremity can pull
	 *  Range from 0.0 - 1.0*/
	private float pullFactor;
	/** Whether or not this extremity is gripping*/
	private boolean gripped;
	/** Texture for when extremity is gripping */ 
	private Texture grip;
	/** Texture for when extremity is not gripping */ 
	private Texture notGrip;
	
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
		return gripped;
	}
	
	/** Sets the value of isGripping to true*/
	public void grip(){
		gripped = true;
		this.setTexture(grip);
		this.body.setType(BodyDef.BodyType.StaticBody);
	}
	
	/** Sets the value of isGripping to false*/
	public void ungrip(){
		gripped = false;
		setTexture(notGrip); 
		this.body.setType(BodyDef.BodyType.DynamicBody);
	}
	
	/** Contructs an ExtremityModel
	 * 
	 * @param x the x position of this extremity
	 * @param y the y position of this extremity
	 * @param ungripTexture	the texture of this extremity
	 * @param gripTexture the gripped texture of this extremity
	 * @param drawSizeScale the scaling between object size and drawn size
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 */
	public ExtremityModel(float x, float y, Texture ungripTexture, Texture gripTexture, float drawSizeScale, Vector2 drawPositionScale){
		super(x, y, ungripTexture, drawSizeScale, drawPositionScale);
		gripped = false;
		notGrip = ungripTexture;
		grip = gripTexture;
	}
}
