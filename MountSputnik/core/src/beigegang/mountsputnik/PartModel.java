package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

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
	/** Density of this part, has default value of 1.0*/
	protected float density = 1.0f;

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
	 * @param b the body of this part
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
	 * @param d the density of this part
	 */
	public void setDensity(float d){
		density = d;
	}
	
	/** Contructs a PartModel
	 *
	 * @param t	the texture of this part
	 * */
	public PartModel(float x, float y, Texture t){
		super(t, t.getWidth(), t.getHeight(), 1, 1);
		shape.setAsBox(drawScale.x*t.getWidth() / 2, drawScale.y *t.getHeight() / 2);
		setX(x);
		setY(y);
	}

	/**
	 * Create new fixtures for this body, defining the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected void createFixtures() {
		if (body == null) {
			return;
		}

		// Create the fixture
		fixtureDef.shape = shape;
		fixtureDef.density = 0.5f;
		geometry = body.createFixture(fixtureDef);

		markDirty(false);
	}

	/**
	 * Release the fixtures for this body, reseting the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected void releaseFixtures() {
		if (geometry != null) {
			body.destroyFixture(geometry);
			geometry = null;
		}
	}
}
