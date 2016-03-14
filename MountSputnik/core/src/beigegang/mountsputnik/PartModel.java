package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;

public class PartModel extends GameObject{

	/** Enumeration of all the different types of character parts*/
	private enum PartType {
		HEAD,
		CHEST,
		ABDOMEN,
		HIPS,
		ARM_LEFT,
		ARM_RIGHT,
		FOREARM_LEFT,
		FOREARM_RIGHT,
		HAND_LEFT,
		HAND_RIGHT,
		THIGH_LEFT,
		THIGH_RIGHT,
		SHIN_LEFT,
		SHIN_RIGHT,
		FOOT_LEFT,
		FOOT_RIGHT,
	}
	
	/** The current part's type*/
	private PartType partType;
	/** A root body for this box 2d. */
    protected Body body;
	/** Density of this part, has default value of 1.0*/
	protected static float density = 1.0f;

	@Override
	public ObjectType getType() {
		return ObjectType.PART;
	}
	
	/**
	 * Returns the part type of this object.
	 *
	 * @return the part type of this object.
	 */
	public PartType getPartType(){
		return partType;
	}
	
	/**
	 * Sets the part type of this object.
	 *
	 * @param type the part type of this part
	 */
	public void setPartType(PartType type){
		partType = type;
	} 
	
	
	/**
	 * Returns the body of this object.
	 *
	 * @return the body of this object.
	 */
	public Body getBody(){
		return body;
	}
	
	/**
	 * Sets the body of this object.
	 *
	 * @param body the body of this part
	 */
	public void setBody(Body b){
		body = b;
	}
	
	/**
	 * Returns the density of this object.
	 *
	 * @return the density of this object.
	 */
	public float getDensity(){
		return density;
	}
	
	/**
	 * Sets the density of this object.
	 *
	 * @param density the density of this part
	 */
	public void setDensity(float d){
		density = d;
	}
		
	/** Contructs a PartModel
	 * 
	 * @param b	the body of this part
	 * */
	public PartModel(Body b){
		body = b;
		setX(b.getPosition().x);
		setY(b.getPosition().y);
	}
	
	/** Contructs a PartModel
	 * 
	 * @param b	the body of this part
	 * @param t	the texture of this part
	 * */
	public PartModel(Body b, Texture t){
		super(t);
		body = b;
		setX(b.getPosition().x);
		setY(b.getPosition().y);
	}
	
}
