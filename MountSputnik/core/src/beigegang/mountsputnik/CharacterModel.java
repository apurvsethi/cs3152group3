package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import static beigegang.mountsputnik.Constants.*;

public class CharacterModel extends GameObject{

	/** An array of all of the body's parts*/
	protected Array<PartModel> parts;
	/** An array of all of the body's joints*/
	protected Array<Joint> joints;
	/** Cache vector for organizing body parts */
	private Vector2 partCache = new Vector2();
	/** Texture assets for the body parts */
	private TextureRegion[] partTextures;
	/** Character energy */
	private float energy;
	
	@Override
	public ObjectType getType() {
		return ObjectType.CHARACTER;
	}

	@Override
	protected void createFixtures() {

	}

	@Override
	protected void releaseFixtures() {

	}

	/**
	 * Gets the energy of this character
	 * 
	 * @return character's energy
	 */
	public float getEnergy(){
		return energy;
	}
	
	/**
	 * Sets the energy of this character
	 * 
	 * @param energy New energy value
	 */
	public void setEnergy(float energy){
		this.energy = energy;
	}
	
	/**
     * Sets the drawing scale for this physics object
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param x the x-axis scale for this physics object
     * @param y the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y, World w) {
    	super.setDrawScale(x,y);
    	
    	if (partTextures != null && parts.size == 0) {
    		init(w);
    	}
    }
	
	/**
     * Returns the array of textures for the individual body parts.
     *
     * Modifying this array will have no affect on the physics objects.
     *
     * @return the array of textures for the individual body parts.
     */
    public TextureRegion[] getPartTextures() {
    	return partTextures;
    }
    
    /**Initializes the character, with all of their body parts
     * 
     * @param w			The world*/
	protected void init(World w) {
		
		
		// HEAD
	    makePart(HEAD, NONE, HEAD_X, 0, HEAD_Y, 0, 0,0,2f*(float)(Math.PI), 0, 0, w);

		// CHEST
		makePart(CHEST, HEAD, 0, 0, CHEST_HEAD_OFFSET, HEAD_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);	
		
		//HIPS
		makePart(HIPS, CHEST, 0, 0, HIP_CHEST_OFFSET, CHEST_HIP_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		
		// ARMS
		makePart(ARM_LEFT, CHEST, -ARM_X_CHEST_OFFSET, -CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		makePart(ARM_RIGHT, CHEST, ARM_X_CHEST_OFFSET, CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		
		// FOREARMS
		makePart(FOREARM_LEFT, ARM_LEFT, -FOREARM_X_ARM_OFFSET, -ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		makePart(FOREARM_RIGHT, ARM_RIGHT, FOREARM_X_ARM_OFFSET, ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		
		//HANDS
		makePart(HAND_LEFT, FOREARM_LEFT, -HAND_X_OFFSET, -FOREARM_X_HAND_OFFSET, 
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, 0,0,2f*(float)(Math.PI), HAND_PUSH, HAND_PULL, w);
		makePart(HAND_RIGHT, FOREARM_RIGHT, HAND_X_OFFSET, FOREARM_X_HAND_OFFSET, 
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, 0,0,2f*(float)(Math.PI), HAND_PUSH, HAND_PULL, w);
		
		// THIGHS
		makePart(THIGH_LEFT, HIPS, -THIGH_X_HIP_OFFSET, -HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		makePart(THIGH_RIGHT, HIPS, THIGH_X_HIP_OFFSET, HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		
		// SHINS
		makePart(SHIN_LEFT,  THIGH_LEFT, -SHIN_X_THIGH_OFFSET, -THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		makePart(SHIN_RIGHT, THIGH_RIGHT, SHIN_X_THIGH_OFFSET, THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, 0,0,2f*(float)(Math.PI), 0, 0, w);
		
		//FEET
		makePart(FOOT_LEFT, SHIN_LEFT, -FOOT_X_OFFSET, -SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, 0,0,2f*(float)(Math.PI), FOOT_PUSH, FOOT_PULL, w);
		makePart(FOOT_RIGHT, SHIN_RIGHT, FOOT_X_OFFSET, SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, 0,0,2f*(float)(Math.PI), FOOT_PUSH, FOOT_PULL, w);
	}
    
	/**
	 * Helper method to make a single body part
	 * 
	 * @param part The part to make
	 * @param connect The part to connect to
	 * @param partX The x-offset of the part RELATIVE to the connecting part's offset
	 * @param partY	The y-offset of the part RELATIVE to the connecting part's offset
	 * @param connectX The x-offset of the connecting part RELATIVE to the part's offset
	 * @param connectY	The y-offset of the connecting part RELATIVE to the part's offset
	 * @param angle The angle between this part and its connecting part
	 * @param rotationLimitLower The minimum angle at which the joints can rotate
	 * @param rotationLimitUpper The maximum angle at which the joints can rotate
	 * @param w	The world this part is created in
	 * 
	 * @return the newly created part
	 */
	private PartModel makePart(int part, int connect, float partX, float connectX,
			float partY, float connectY, float angle, float rotationLimitLower,
			float rotationLimitUpper, float push, float pull, World w) {
		TextureRegion texture = partTextures[part];
		
		partCache.set(partX,partY);
		if (connect != NONE) {
			partCache.add(parts.get(connect).getPosition());
			partCache.add(connectX,connectY);
		}
		
		PartModel partModel = (push == 0.0f? 
				new PartModel(partCache.x, partCache.y, texture.getTexture())
				: new ExtremityModel(push, pull, partCache.x, partCache.y, texture.getTexture()));
		
		partModel.setDrawScale(drawScale);
		partModel.setAngle(angle);
		partModel.setBodyType(BodyDef.BodyType.DynamicBody);
		partModel.activatePhysics(w);
		
		//TODO: set density in individual parts
		//partModel.setDensity(DENSITY);
		
		parts.add(partModel);
		
		//create joint if two parts present
		if (connect != NONE){
			jointDef.bodyA = parts.get(connect).getBody();
			partCache.set(connectX, connectY);
			jointDef.localAnchorA.set(partCache);

			jointDef.bodyB = parts.get(part).getBody();
			partCache.set(-partX, -partY);
			jointDef.localAnchorB.set(partCache);

			jointDef.collideConnected = true;
			//jointDef.lowerAngle = rotationLimitLower;
			//jointDef.upperAngle = rotationLimitUpper;
			
			Joint joint = w.createJoint(jointDef);
			joints.add(joint);
		}
		
		return partModel;
	}
	
	/** Constructs a CharacterModel
	 * 
	 * @param w	the world*/
	public CharacterModel(World w){
		init(w);
		setX(HEAD_X);
		setY(HEAD_Y);
		energy = 100;
	}
	
	/** Constructs a CharacterModel
	 * 
	 * @param textures the texture map of the character
	 * @param w	the world*/
	public CharacterModel(TextureRegion[] textures, World w){
		parts = new Array<PartModel>();
		joints = new Array<Joint>();
		partTextures = textures;
		init(w);
		setX(HEAD_X);
		setY(HEAD_Y);
		energy = 100f;
	}

	/** Calculate percentage of pullY factor */
	public float calcPullPercentageY(float y, boolean arm){

		return arm ?  (1-(MAX_ARM_DIST - y)/MAX_ARM_DIST):  (1-(MAX_LEG_DIST - y)/MAX_LEG_DIST);
	}

	/** Calculate percentage of pushY factor */
	public float calcPushPercentageY(float y, boolean arm){
		return arm ?  (1-(MAX_ARM_DIST - y)/MAX_ARM_DIST):  (1-(MAX_LEG_DIST - y)/MAX_LEG_DIST);
	}

	/** Calculate percentage of pullX factor */
	public float calcPullPercentageX(float x, boolean arm){

		return arm ?  (1-x/MAX_ARM_DIST):  (1-x/MAX_LEG_DIST);
	}

	/** Calculate percentage of pushX factor */
	public float calcPushPercentageX(float x, boolean arm){
		return arm ?  (1-x/MAX_ARM_DIST):  (1-x/MAX_LEG_DIST);
	}
	/** Activate physics for each of character model's parts */
	@Override
	public boolean activatePhysics(World world) {
		for (PartModel part : parts) {
			part.activatePhysics(world);
		}
		return true;
	}

	/** Deactivate physics for each of character model's parts */
	@Override
	public void deactivatePhysics(World world) {
		for (PartModel part : parts) {
			part.deactivatePhysics(world);
		}
	}
	
	/** Draw method for character model */
	@Override
	public void draw(GameCanvas canvas) {
		for (PartModel part : parts) {
			part.draw(canvas);
		}
	}

	/** Draw debug method for character model */
	@Override
	public void drawDebug(GameCanvas canvas) {
		for (PartModel part : parts) {
			part.drawDebug(canvas);
		}
	}
}
