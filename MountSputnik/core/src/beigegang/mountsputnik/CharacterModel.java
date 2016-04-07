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
//		createJoints(w);
	//TODO make better motor torques and speeds
		// HEAD
	    makePart(HEAD, NONE, HEAD_X, 0, HEAD_Y, 0, 0, 0, -10,10, true, 0, 0, 0, 0, w);

		// CHEST
		makePart(CHEST, HEAD, 0, 0, CHEST_HEAD_OFFSET, HEAD_OFFSET, 0, 0,-20,20, false, 0, 0, 0, 0, w);
		
		//HIPS
		makePart(HIPS, CHEST, 0, 0, HIP_CHEST_OFFSET, CHEST_HIP_OFFSET, 0, 0,-45, 45,  true, 0, 0, 0, 0, w);
		
		// ARMS
		makePart(ARM_LEFT, CHEST, -ARM_X_CHEST_OFFSET, -CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0,0, -90,90, true, 0, 0, 0, 0, w);
		makePart(ARM_RIGHT, CHEST, ARM_X_CHEST_OFFSET, CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0, 0,-90,90, true, 0, 0, 0, 0, w);
		
		// FOREARMS
		makePart(FOREARM_LEFT, ARM_LEFT, -FOREARM_X_ARM_OFFSET, -ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, 0, 0,-45,90, true, 0, 0, 0, 0, w);
		makePart(FOREARM_RIGHT, ARM_RIGHT, FOREARM_X_ARM_OFFSET, ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, 0, 0, -45,90, true, 0, 0, 0, 0, w);
		
		//HANDS
		makePart(HAND_LEFT, FOREARM_LEFT, -HAND_X_OFFSET, -FOREARM_X_HAND_OFFSET, 
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, 0,0,-20,60, true, 30, 0, HAND_PUSH, HAND_PULL, w);
		makePart(HAND_RIGHT, FOREARM_RIGHT, HAND_X_OFFSET, FOREARM_X_HAND_OFFSET, 
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, 0, 0, -20,60, true, 30, 0, HAND_PUSH, HAND_PULL, w);
		
		// THIGHS
		makePart(THIGH_LEFT, HIPS, -THIGH_X_HIP_OFFSET, -HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, 0,0, -45,90, true, 0, 0, 0, 0, w);
		makePart(THIGH_RIGHT, HIPS, THIGH_X_HIP_OFFSET, HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, 0, 0, -90,45, true, 0, 0, 0, 0, w);
		
		// SHINS
		makePart(SHIN_LEFT,  THIGH_LEFT, -SHIN_X_THIGH_OFFSET, -THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, 0, 0, -150,20, true, 0, 0, 0, 0, w);
		makePart(SHIN_RIGHT, THIGH_RIGHT, SHIN_X_THIGH_OFFSET, THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, 0, 0, -20,150,  true, 0, 0, 0, 0, w);

		//TODO add Non-zero rotation limits for feet if that's how feet work.
		//FEET
		makePart(FOOT_LEFT, SHIN_LEFT, -FOOT_X_OFFSET, -SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, 0,  0, 0,0,  true, 0, 0,FOOT_PUSH, FOOT_PULL, w);
		makePart(FOOT_RIGHT, SHIN_RIGHT, FOOT_X_OFFSET, SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, 0, 0, 0,0, true, 0, 0, FOOT_PUSH, FOOT_PULL, w);

	}
    


	private PartModel makePart(int part, int connect, float partX, float connectX,
			float partY, float connectY, float angle, float referenceAngle, float rotationLimitLower,
			float rotationLimitUpper, boolean enableMotor, float maxTorque, float motorSpeed, float push, float pull, World w) {
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
		if(part==HAND_LEFT || part == HAND_RIGHT || part == FOOT_LEFT || part == FOOT_RIGHT){
			partModel.geometry.setUserData("extremity");
			ExtremityModel e = (ExtremityModel)partModel; 
			if (part == HAND_LEFT){
//				System.out.println("left hand set");
				e.setGripTexture(partTextures[15].getTexture());
			}
			else if (part == HAND_RIGHT){
//				System.out.println("right hand set");
				e.setGripTexture(partTextures[16].getTexture());
			}
			else {
				e.setGripTexture(texture.getTexture());
			}
		}
		
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
//should be false -
			jointDef.collideConnected = false;

			jointDef.enableLimit = true;
			//TODO why does this line make screen bounce around?
//			jointDef.referenceAngle = referenceAngle * DEG_TO_RAD;
			jointDef.lowerAngle = rotationLimitLower * DEG_TO_RAD;
			jointDef.upperAngle = rotationLimitUpper * DEG_TO_RAD;
			jointDef.enableMotor = enableMotor;
			jointDef.maxMotorTorque = maxTorque;
			jointDef.motorSpeed = motorSpeed * DEG_TO_RAD;
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
		return arm ?  (1-(MAX_ARM_DIST + y)/MAX_ARM_DIST):  (1-(MAX_LEG_DIST + y)/MAX_LEG_DIST);
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
	
	/**
	 * @author Daniel
	 * dE/dt = A (1-B*sin(angle/2))(Base energy gain)(Environmental Gain Modifier) - 
	 * - C (Exertion+1)(Environmental Loss Modifier)(3-feet)(3-hands) - D 
	 * 
	 * A, C and D are playtested constants
	 * B allows for rotation to not effect energy gain
	 * Base energy gain is a value in the character
	 * 
	 * @param gainModifier Environmental Gain Modifier
	 * @param lossModifier Environmental Loss Modifier
	 * @param rotationGain Whether or not rotation affects gain (would be false if in space or places with low gravity)
	 * @param force Current force being exerted by character
	 */
	public void updateEnergy(float gainModifier, float lossModifier, Vector2 force, boolean rotationGain){
		int b = rotationGain ? 1 : 0;
		float angle = parts.get(CHEST).getAngle();
		float exertion = Math.abs(force.y/600); //TODO: value needs adjusting based on new physics
		
		int feet = parts.get(FOOT_LEFT).getBody().getType() == BodyDef.BodyType.StaticBody ? 1 : 0;
		feet += parts.get(FOOT_RIGHT).getBody().getType() == BodyDef.BodyType.StaticBody ? 1 : 0;
		int hands = parts.get(HAND_LEFT).getBody().getType() == BodyDef.BodyType.StaticBody ? 1 : 0;
		hands += parts.get(HAND_RIGHT).getBody().getType() == BodyDef.BodyType.StaticBody ? 1 : 0;
		
		float gain = (float) (ENERGY_GAIN_MULTIPLIER * (1-b*Math.sin(angle/2.0)) * BASE_ENERGY_GAIN * gainModifier);
		float loss = ENERGY_LOSS_MULTIPLIER * (exertion + 1) * lossModifier * (3 - feet) * (3 - hands);
		loss = feet == 0 && hands == 0 ? 0 : loss;
		float dEdt = gain - loss - ENERGY_LOSS;
		
		float newEnergy = getEnergy() < 0 ? 0 : getEnergy() > 100 ? 100 : getEnergy() + dEdt;
		setEnergy(newEnergy);
	}
}
