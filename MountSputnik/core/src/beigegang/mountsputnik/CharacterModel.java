package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;

import static beigegang.mountsputnik.Constants.*;

public class CharacterModel {

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
	private RevoluteJointDef jointDef = new RevoluteJointDef();

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

	/** Initializes the character, with all of their body parts and joints
	 *
	 * @param world    The world
	 * @param initialPositionX where character's chest should be to start (x value)
	 * @param initialPositionY where character's chest should be to start (y value)
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 */
	private void init(World world, float initialPositionX, float initialPositionY,
					  Vector2 drawPositionScale) {
		makeParts(initialPositionX, initialPositionY, drawPositionScale);

		for (PartModel part : parts) {
			part.activatePhysics(world);
		}

		makeJoints(world);
	}

	private void makeParts(float initialPositionX, float initialPositionY, Vector2 drawPositionScale) {
		// CHEST
		makePart(CHEST, NONE, initialPositionX, 0, initialPositionY, 0, drawPositionScale);

		// HEAD
		makePart(HEAD, CHEST, 0, 0, HEAD_OFFSET, CHEST_HEAD_OFFSET, drawPositionScale);

		//HIPS
		makePart(HIPS, CHEST, 0, 0, HIP_CHEST_OFFSET, CHEST_HIP_OFFSET, drawPositionScale);

		// ARMS
		makePart(ARM_LEFT, CHEST, ARM_X_CHEST_OFFSET, CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, CHEST_Y_ARM_OFFSET, drawPositionScale);
		makePart(ARM_RIGHT, CHEST, -ARM_X_CHEST_OFFSET, -CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, CHEST_Y_ARM_OFFSET, drawPositionScale).setAngle(180 * DEG_TO_RAD);

		// FOREARMS
		makePart(FOREARM_LEFT, ARM_LEFT, FOREARM_X_ARM_OFFSET, ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, drawPositionScale);
		makePart(FOREARM_RIGHT, ARM_RIGHT, -FOREARM_X_ARM_OFFSET, -ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, drawPositionScale).setAngle(180 * DEG_TO_RAD);

		//HANDS
		makeExtremity(HAND_LEFT, FOREARM_LEFT, HAND_X_OFFSET, FOREARM_X_HAND_OFFSET,
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, drawPositionScale);
		makeExtremity(HAND_RIGHT, FOREARM_RIGHT, -HAND_X_OFFSET, -FOREARM_X_HAND_OFFSET,
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, drawPositionScale).setAngle(180 * DEG_TO_RAD);

		// THIGHS
		makePart(THIGH_LEFT, HIPS, THIGH_X_HIP_OFFSET, HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, drawPositionScale);
		makePart(THIGH_RIGHT, HIPS, -THIGH_X_HIP_OFFSET, -HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, drawPositionScale);

		// SHINS
		makePart(SHIN_LEFT,  THIGH_LEFT, SHIN_X_THIGH_OFFSET, THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, drawPositionScale);
		makePart(SHIN_RIGHT, THIGH_RIGHT, -SHIN_X_THIGH_OFFSET, -THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, drawPositionScale);

		//FEET
		makeExtremity(FOOT_LEFT, SHIN_LEFT, FOOT_X_OFFSET, SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, drawPositionScale);
		makeExtremity(FOOT_RIGHT, SHIN_RIGHT, -FOOT_X_OFFSET, -SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, drawPositionScale);

	}

	/**
	 * Helper method to make a single body part
	 *
	 * @param part The part to make
	 * @param connect The part to connect to
	 * @param partX The x-offset of the part RELATIVE to the connecting part's offset
	 * @param connectX The x-offset of the connecting part RELATIVE to the part's offset
	 * @param partY    The y-offset of the part RELATIVE to the connecting part's offset
	 * @param connectY The y-offset of the connecting part RELATIVE to the part's offset
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 *
	 * @return the part that was made
	 */
	private PartModel makePart(int part, int connect, float partX, float connectX,
							   float partY, float connectY, Vector2 drawPositionScale) {
		TextureRegion texture = partTextures[part];

		if (connect != NONE) {
			partCache.set(-partX, -partY);
			partCache.add(parts.get(connect).getPosition());
			partCache.add(connectX, connectY);
		}
		else partCache.set(partX, partY);

		PartModel partModel = new PartModel(partCache.x, partCache.y, texture.getTexture(),
				CHARACTER_DRAW_SIZE_SCALE, drawPositionScale);
		partModel.setBodyType(BodyDef.BodyType.DynamicBody);
		parts.add(partModel);
		return partModel;
	}

	/**
	 * Helper method to make a single extremity part
	 *
	 * @param part The part to make
	 * @param connect The part to connect to
	 * @param partX The x-offset of the part RELATIVE to the connecting part's offset
	 * @param connectX The x-offset of the connecting part RELATIVE to the part's offset
	 * @param partY    The y-offset of the part RELATIVE to the connecting part's offset
	 * @param connectY The y-offset of the connecting part RELATIVE to the part's offset
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 *
	 * @return the part that was made
	 */
	private PartModel makeExtremity(int part, int connect, float partX, float connectX,
							   float partY, float connectY, Vector2 drawPositionScale) {
		TextureRegion texture = partTextures[part];

		if (connect != NONE) {
			partCache.set(-partX, -partY);
			partCache.add(parts.get(connect).getPosition());
			partCache.add(connectX, connectY);
		}
		else partCache.set(partX, partY);

		// TODO: Do this better, most likely with film strip
		TextureRegion grippedTexture = part + 8 > partTextures.length ? texture : partTextures[part + 8];

		ExtremityModel extremityModel = new ExtremityModel(partCache.x, partCache.y,
				texture.getTexture(), grippedTexture.getTexture(), CHARACTER_DRAW_SIZE_SCALE,
				drawPositionScale);
		extremityModel.setBodyType(BodyDef.BodyType.DynamicBody);
		parts.add(extremityModel);
		return extremityModel;
	}

	private void makeJoints(World world) {
		makeJoint(CHEST, HEAD, 0, CHEST_HEAD_OFFSET, 0);
		setJointAngleLimits(-10, 10);
		addJoint(world);

		makeJoint(HIPS, CHEST, 0, HIP_CHEST_OFFSET, 0);
		setJointAngleLimits(-45, 45);
		addJoint(world);

		makeJoint(ARM_LEFT, CHEST, ARM_X_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0);
		setJointAngleLimits(-90, 90);
		setJointMotor(0, 100);

		addJoint(world);
		makeJoint(ARM_RIGHT, CHEST, -ARM_X_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 180);
		setJointAngleLimits(-90, 90);
		setJointMotor(0, 100);

		addJoint(world);

		makeJoint(FOREARM_LEFT, ARM_LEFT, FOREARM_X_ARM_OFFSET, FOREARM_Y_ARM_OFFSET, 0);
		setJointAngleLimits(-45, 90);
		setJointMotor(0, 100);
		addJoint(world);
		makeJoint(FOREARM_RIGHT, ARM_RIGHT, -FOREARM_X_ARM_OFFSET, FOREARM_Y_ARM_OFFSET, 0);
		setJointAngleLimits(-45, 90);
		setJointMotor(0, 100);
		addJoint(world);

		makeJoint(HAND_LEFT, FOREARM_LEFT, HAND_X_OFFSET, HAND_Y_OFFSET, 0);
		setJointAngleLimits(-20, 60);
		setJointMotor(0, 100);
		addJoint(world);
		makeJoint(HAND_RIGHT, FOREARM_RIGHT, -HAND_X_OFFSET, HAND_Y_OFFSET, 0);
		setJointAngleLimits(-20, 60);
		setJointMotor(0, 100);
		addJoint(world);

		makeJoint(THIGH_LEFT, HIPS, THIGH_X_HIP_OFFSET, THIGH_Y_HIP_OFFSET, 0);
		setJointAngleLimits(-45, 90);

		addJoint(world);
		makeJoint(THIGH_RIGHT, HIPS, THIGH_X_HIP_OFFSET, THIGH_Y_HIP_OFFSET, 0);
		setJointAngleLimits(-90, 45);
		addJoint(world);

		makeJoint(SHIN_LEFT,  THIGH_LEFT, SHIN_X_THIGH_OFFSET, SHIN_Y_THIGH_OFFSET, 0);
		setJointAngleLimits(-150, 0);
//		setJointMotor(10, 30);
		addJoint(world);
		makeJoint(SHIN_RIGHT, THIGH_RIGHT, -SHIN_X_THIGH_OFFSET, SHIN_Y_THIGH_OFFSET, 0);
		setJointAngleLimits(0, 150);
//		setJointMotor(10, 30);
		addJoint(world);

		makeJoint(FOOT_LEFT, SHIN_LEFT, FOOT_X_OFFSET, FOOT_Y_OFFSET, 0);
		setJointAngleLimits(-90, 90);
//		setJointMotor(10, 30);
		addJoint(world);
		makeJoint(FOOT_RIGHT, SHIN_RIGHT, -FOOT_X_OFFSET, FOOT_Y_OFFSET, 0);
		setJointAngleLimits(-90, 90);
//		setJointMotor(10, 30);
		addJoint(world);
	}

	/**
	 * Helper method to make a single body joint
	 *  @param part The part to make
	 * @param connect The part to connect to
	 * @param partX The x-offset of the part RELATIVE to the connecting part's offset
	 * @param partY    The y-offset of the part RELATIVE to the connecting part's offset
	 * @param referenceAngle The reference angle for the joint to use
	 */
	private void makeJoint(int part, int connect, float partX, float partY,
						   float referenceAngle) {
		partCache.set(parts.get(part).getX(), parts.get(part).getY());
		partCache.add(partX, partY);
		jointDef.initialize(parts.get(part).getBody(), parts.get(connect).getBody(),
				partCache);
		jointDef.collideConnected = false;
		jointDef.referenceAngle = referenceAngle * DEG_TO_RAD;
	}

	private void setJointMotor(float motorSpeed, float maxTorque) {
		jointDef.enableMotor = true;
		jointDef.motorSpeed = motorSpeed * DEG_TO_RAD;
		jointDef.maxMotorTorque = maxTorque;
	}

	private void setJointAngleLimits(float rotationLimitLower, float rotationLimitUpper) {
		jointDef.enableLimit = true;

		jointDef.lowerAngle = rotationLimitLower * DEG_TO_RAD;
		jointDef.upperAngle = rotationLimitUpper * DEG_TO_RAD;
	}

	private void addJoint(World world) {
		Joint joint = world.createJoint(jointDef);
		joints.add(joint);
	}

	/** Constructs a CharacterModel
	 *
	 * @param textures the texture map of the character
	 * @param w    the world
	 * @param initialPositionX where character's chest should be to start (x value)
	 * @param initialPositionY where character's chest should be to start (y value)
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 */
	public CharacterModel(TextureRegion[] textures, World w, float initialPositionX,
						  float initialPositionY, Vector2 drawPositionScale){
		parts = new Array<PartModel>();
		joints = new Array<Joint>();
		partTextures = textures;
		init(w, initialPositionX, initialPositionY, drawPositionScale);
		energy = 100f;
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
