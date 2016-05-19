package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;

public class HandholdModel extends GameObject{
	
	/** How fast the handhold moves (in m/s)*/
	protected float velocity;
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
	/** says if the handhold is being gripped */
	private boolean isGlowing = false;
	/** says if the handhold is snapped to */
	private boolean isSnapped = false;
	/** The texture the handhold will have when not glowing */
	protected Texture texture;
	protected Texture glowTexture;
	/** 0 is normal, 1 is crumbly, 2 is slippery */
	protected int type;

	private Vector2 startPoint, endPoint;
	
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
	 * sets velocity and changes body type if necessary
	 * @param v
	 */
	public void setVelocity(float v){
		velocity = v;
		if(v>0)
			body.setType(BodyDef.BodyType.KinematicBody);
		else
			body.setType(BodyDef.BodyType.StaticBody);
	}
	
	/**
	 * @return velocity of handhold
	 */
	public float getVelocity(){
		return velocity;
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
	 * Returns whether this handhold is gripped
	 *
	 * @return whether this handhold is gripped
	 */
	public boolean getGripped() {
		return isGlowing;
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
	 * @param t Normal texture for handhold
	 * @param x The x position of the model
	 * @param y The y position of the model
	 * @param dimensions the dimensions of the handhold in box2d coordinates
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 */
	public HandholdModel(Texture t, Texture gt, int ht, float x, float y, Vector2 dimensions, Vector2 drawPositionScale){
		this(t, gt, ht, x, y, dimensions.x, dimensions.y, drawPositionScale);
	}

	/**
	 * Constructs HandholdModel
	 * @param t Normal texture for handhold
	 * @param gt glow texture
	 * @param ht type of handhold
	 * @param x The x position of the model
	 * @param y The y position of the model
	 * @param xDimension the x-dimensions of the handhold in box2d coordinates
	 * @param yDimension the y-dimensions of the handhold in box2d coordinates
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 */
	public HandholdModel(Texture t, Texture gt, int ht, float x, float y, float xDimension,
						 float yDimension, Vector2 drawPositionScale){
		super(t, xDimension, yDimension, drawPositionScale);
		setX(x);
		setY(y);

		glowTexture = gt;
		type = ht;
		isCrumbling = false;
		velocity = 0;
		snapPoints = new Array<Vector2>();
		snapPoints.add(getPosition());
		texture = t;
	}


	@Override
	protected void createFixtures() {
		if (body == null) {
			return;
		}

		// Create the fixture
		fixtureDef.shape = shape;
		geometry = body.createFixture(fixtureDef);
		//shape.dispose(); 
		markDirty(false);
	}

	@Override
	protected void releaseFixtures() {

	}

	public void unglow() {
		isGlowing = false;
	}
	public void glow() {
		isGlowing = true;
	}
	public void desnap() {
		isSnapped = false;
	}
	public void snap() {
		isSnapped = true;
	}

	@Override
	public void draw(GameCanvas canvas) {

		//TODO: remove this if check once all level blocks only contain handholds within the playable area
		if (getX() * drawPositionScale.x > canvas.getWidth() / 5 && getX() * drawPositionScale.x < canvas.getWidth() * 4 / 5) {
			if (isGlowing && !isSnapped){
				canvas.draw(glowTexture, Color.YELLOW, origin.x, origin.y, getX() * drawPositionScale.x, getY() * drawPositionScale.y,
						getAngle(), drawSizeScale.x, drawSizeScale.y);
			}else if (isSnapped){
				if (type == 0)
					canvas.draw(glowTexture, Color.LIME, origin.x, origin.y, getX() * drawPositionScale.x, getY() * drawPositionScale.y,
							getAngle(), drawSizeScale.x, drawSizeScale.y);
				else if (type == 1)
					canvas.draw(glowTexture, Color.ORANGE, origin.x, origin.y, getX() * drawPositionScale.x, getY() * drawPositionScale.y,
							getAngle(), drawSizeScale.x, drawSizeScale.y);
				else {
					canvas.draw(glowTexture, Color.SKY, origin.x, origin.y, getX() * drawPositionScale.x, getY() * drawPositionScale.y,
							getAngle(), drawSizeScale.x, drawSizeScale.y);
				}
			}
				canvas.draw(animator, Color.WHITE, origin.x, origin.y,
						getX() * drawPositionScale.x, getY() * drawPositionScale.y,
						getAngle(), drawSizeScale.x, drawSizeScale.y);
			
		}
	}

	public void setStartPoint(float x, float y) {
		startPoint = new Vector2(x, y);
		
	}

	public void setEndPoint(float x, float y) {
		endPoint = new Vector2(x, y);
		
	}

	public Vector2 getEndPoint() {
		return endPoint;
	}
	
	public Vector2 getStartPoint() {
		return startPoint;
	}

	public float getSlip() {
		return slipFactor;
	}
	
	public void updateSnapPoints(){
		snapPoints.removeIndex(0);
		snapPoints.add(getPosition());
	}
}
