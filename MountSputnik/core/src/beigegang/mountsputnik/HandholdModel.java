package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public class HandholdModel extends GameObject{
	
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
	/** says if this handhold should be drawn glowing */
	private boolean glowing = false;
	/** says if the handhold is being gripped */ 
	private boolean isGripped = false;
	/** The texture the handhold will have when glowing */ 
	protected Texture glowTexture; 
	/** The texture the handhold will have when not glowing */ 
	protected Texture dullTexture; 
	/** the texture the handhold will have when gripped */ 
	protected Texture gripTexture;

	
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
	 * @param b the body of this handhold
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
	 * @param crumble the crumble factor of this handhold
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
	
	/**
	 * Constructs HandholdModel
	 *  
	 * @param t Normal texture for handhold
	 * @param glowTexture The glow texture for handhold
	 * @param gripTexture The grip texture for handhold
	 * @param x The x position of the model
	 * @param y The y position of the model
	 * @param drawSizeScale the scaling between object size and drawn size
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 */
	public HandholdModel(Texture t, Texture glowTexture, Texture gripTexture,
						 float x, float y, float drawSizeScale, Vector2 drawPositionScale){
		super(t, drawSizeScale, drawPositionScale);
		setX(x);
		setY(y);

		isCrumbling = false;
		snapPoints = new Array<Vector2>();
		snapPoints.add(getPosition());
		this.glowTexture = glowTexture;
		dullTexture = t; 
		this.gripTexture = gripTexture;
	}


	@Override
	protected void createFixtures() {
		if (body == null) {
			return;
		}

		// Create the fixture
		fixtureDef.shape = shape;
		geometry = body.createFixture(fixtureDef);
		shape.dispose();
		markDirty(false);
	}

	@Override
	protected void releaseFixtures() {

	}

	public void unglow() {
		if (!isGripped) {
			glowing = false;
			setTexture(dullTexture);
		}
	}
	public void glow() {
		if (!isGripped) {
			glowing = true;
			setTexture(glowTexture);
		}
	}
	public void ungrip(){
		isGripped = false;
		setTexture(dullTexture); 
	}
	public void grip(){
		isGripped = true;
		setTexture(gripTexture);
	}
}
