package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public class HandholdModel extends GameObject{
	
	/** A root body for this box 2d. */
    protected Body body;
    /** How crumbly handhold is
     * Range from 0.0 - 1.0*/
    protected float crumbleFactor;
    /** How slippery handhold is
     *  Range from 0.0 - 1.0*/
    protected float slipFactor;
    /** true if this handhold is crumbling
     * a handhold can no longer be selected if crumbling*/
    protected boolean isCrumbling;
    /** array of all snap points for this handhold*/
    protected Array<Vector2> snapPoints;
	
	@Override
	public ObjectType getType() {
		return ObjectType.HANDHOLD;
	}
	
	/**
	 * Returns the body of this handhold.
	 *
	 * @return the body of this handhold.
	 */
	public Body getBody(){
		return body;
	}
	
	/**
	 * Sets the body of this handhold.
	 *
	 * @param body the body of this handhold
	 */
	public void setBody(Body b){
		body = b;
	}
	
	/**
	 * Sets the slip factor of this handhold.
	 *
	 * @param slip the slip factor of this handhold
	 */
	public void setSlip(float slip){
		slipFactor = slip;
	}
	
	/**
	 * Returns the crumble factor of this handhold
	 *
	 * @return Returns the crumble factor of this handhold
	 */
	public float getCrumble(){
		return crumbleFactor;
	}
	
	/**
	 * Sets the crumble factor of this handhold.
	 *
	 * @param slip the crumble factor of this handhold
	 */
	public void setCrumble(float crumble){
		crumbleFactor = crumble;
	}
	
	/**
	 * Returns if this handhold is crumbling
	 *
	 * @return Returns if this handhold is crumbling
	 */
	public boolean isCrumbling(){
		return isCrumbling;
	}
	
	/** Sets the handhold to crumbling*/
	public void setCrumbling(){
		isCrumbling = true;
	}
	
	/**
	 * Adds a snap point to the handhold
	 * 
	 * @param snapPoint snap point for the handhold
	 */
	public void addSnapPoint(Vector2 snapPoint){
		snapPoints.add(snapPoint);
	}
	
	/**trivial handhold model constructor*/
	public HandholdModel(){
		isCrumbling = false;
	}
}