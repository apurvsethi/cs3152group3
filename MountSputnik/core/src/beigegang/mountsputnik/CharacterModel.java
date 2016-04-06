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
	 */
	private void init(World world, float initialPositionX, float initialPositionY) {
		makeParts(initialPositionX, initialPositionY);

		for (PartModel part : parts) {
			part.activatePhysics(world);
		}

//		makeJoints(world);
	}

	private void makeParts(float initialPositionX, float initialPositionY) {
		// CHEST
		makePart(CHEST, NONE, initialPositionX, 0, initialPositionY, 0);

		// HEAD
		makePart(HEAD, CHEST, 0, 0, HEAD_OFFSET, CHEST_HEAD_OFFSET);

		//HIPS
		makePart(HIPS, CHEST, 0, 0, HIP_CHEST_OFFSET, CHEST_HIP_OFFSET);

		// ARMS
		makePart(ARM_LEFT, CHEST, ARM_X_CHEST_OFFSET, CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, CHEST_Y_ARM_OFFSET);
//		makePart(ARM_RIGHT, CHEST, ARM_X_CHEST_OFFSET, CHEST_X_ARM_OFFSET,
//				ARM_Y_CHEST_OFFSET, CHEST_Y_ARM_OFFSET);
//
//		// FOREARMS
//		makePart(FOREARM_LEFT, ARM_LEFT, -FOREARM_X_ARM_OFFSET, -ARM_X_FOREARM_OFFSET,
//				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET);
//		makePart(FOREARM_RIGHT, ARM_RIGHT, FOREARM_X_ARM_OFFSET, ARM_X_FOREARM_OFFSET,
//				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET);
//
//		//HANDS
//		makeExtremity(HAND_LEFT, FOREARM_LEFT, -HAND_X_OFFSET, -FOREARM_X_HAND_OFFSET,
//				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET);
//		makeExtremity(HAND_RIGHT, FOREARM_RIGHT, HAND_X_OFFSET, FOREARM_X_HAND_OFFSET,
//				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET);
//
//		// THIGHS
//		makePart(THIGH_LEFT, HIPS, -THIGH_X_HIP_OFFSET, -HIP_X_THIGH_OFFSET,
//				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET);
//		makePart(THIGH_RIGHT, HIPS, THIGH_X_HIP_OFFSET, HIP_X_THIGH_OFFSET,
//				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET);
//
//		// SHINS
//		makePart(SHIN_LEFT,  THIGH_LEFT, -SHIN_X_THIGH_OFFSET, -THIGH_X_SHIN_OFFSET,
//				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET);
//		makePart(SHIN_RIGHT, THIGH_RIGHT, SHIN_X_THIGH_OFFSET, THIGH_X_SHIN_OFFSET,
//				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET);
//
//		//FEET
//		makeExtremity(FOOT_LEFT, SHIN_LEFT, -FOOT_X_OFFSET, -SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
//				SHIN_Y_FOOT_OFFSET);
//		makeExtremity(FOOT_RIGHT, SHIN_RIGHT, FOOT_X_OFFSET, SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
//				SHIN_Y_FOOT_OFFSET);
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
	 */
	private void makePart(int part, int connect, float partX, float connectX,
							   float partY, float connectY) {
		TextureRegion texture = partTextures[part];

		if (connect != NONE) {
			partCache.set(-partX, -partY);
			partCache.add(parts.get(connect).getPosition());
			partCache.add(connectX, connectY);
		}
		else partCache.set(partX, partY);

		PartModel partModel = new PartModel(partCache.x, partCache.y, texture.getTexture());
		partModel.setBodyType(BodyDef.BodyType.DynamicBody);
		parts.add(partModel);
	}

	private void makeExtremity(int part, int connect, float partX, float connectX,
							   float partY, float connectY) {
		TextureRegion texture = partTextures[part];

		if (connect != NONE) {
			partCache.set(-partX, -partY);
			partCache.add(parts.get(connect).getPosition());
			partCache.add(connectX, connectY);
		}
		else partCache.set(partX, partY);

		ExtremityModel extremityModel = new ExtremityModel(partCache.x, partCache.y, texture.getTexture());
		extremityModel.setBodyType(BodyDef.BodyType.DynamicBody);
		// TODO: Do this better, most likely with film strip
		TextureRegion grippedTexture = part + 8 > partTextures.length ? texture : partTextures[part + 8];
		extremityModel.setGripTexture(grippedTexture.getTexture());
		parts.add(extremityModel);
	}

	private void makeJoints(World world) {
		makeJoint(CHEST, HEAD, 0, 0, CHEST_HEAD_OFFSET, HEAD_OFFSET, 0);
		setJointAngleLimits(-10, 10);
		addJoint(world);

		makeJoint(HIPS, CHEST, 0, 0, HIP_CHEST_OFFSET, CHEST_HIP_OFFSET, 0);
		setJointAngleLimits(-45, 45);
		addJoint(world);

		makeJoint(ARM_LEFT, CHEST, ARM_X_CHEST_OFFSET, CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0);
		setJointAngleLimits(-90, 90);
		addJoint(world);

		makeJoint(ARM_RIGHT, CHEST, ARM_X_CHEST_OFFSET, CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 180);
		setJointAngleLimits(-90, 90);
		addJoint(world);

		makeJoint(FOREARM_LEFT, ARM_LEFT, FOREARM_X_ARM_OFFSET, ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, 0);
		setJointAngleLimits(-45, 90);
		setJointMotor(0, 30);
		addJoint(world);

		makeJoint(FOREARM_RIGHT, ARM_RIGHT, FOREARM_X_ARM_OFFSET, ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, 0);
		setJointAngleLimits(-45, 90);
		setJointMotor(0, 30);
		addJoint(world);

		makeJoint(HAND_LEFT, FOREARM_LEFT, HAND_X_OFFSET, FOREARM_X_HAND_OFFSET,
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, 0);
		setJointAngleLimits(-20, 60);
		setJointMotor(0, 30);
		addJoint(world);

		makeJoint(HAND_RIGHT, FOREARM_RIGHT, HAND_X_OFFSET, FOREARM_X_HAND_OFFSET,
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, 0);
		setJointAngleLimits(-20, 60);
		setJointMotor(0, 30);
		addJoint(world);

		makeJoint(THIGH_LEFT, HIPS, THIGH_X_HIP_OFFSET, HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, 0);
		setJointAngleLimits(-45, 90);
		addJoint(world);

		makeJoint(THIGH_RIGHT, HIPS, THIGH_X_HIP_OFFSET, HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, 0);
		setJointAngleLimits(-90, 45);
		addJoint(world);

		makeJoint(SHIN_LEFT,  THIGH_LEFT, SHIN_X_THIGH_OFFSET, THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, 0);
		setJointAngleLimits(-150, 20);
		setJointMotor(0, 30);
		addJoint(world);

		makeJoint(SHIN_RIGHT, THIGH_RIGHT, SHIN_X_THIGH_OFFSET, THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, 0);
		setJointAngleLimits(-20, 150);
		setJointMotor(0, 30);
		addJoint(world);

		makeJoint(FOOT_LEFT, SHIN_LEFT, FOOT_X_OFFSET, SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, 0);
		setJointAngleLimits(-90, 90);
		setJointMotor(0, 30);
		addJoint(world);

		makeJoint(FOOT_RIGHT, SHIN_RIGHT, FOOT_X_OFFSET, SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, 0);
		setJointAngleLimits(-90, 90);
		setJointMotor(0, 30);
		addJoint(world);
	}

	/**
	 * Helper method to make a single body joint
	 *
	 * @param part The part to make
	 * @param connect The part to connect to
	 * @param partX The x-offset of the part RELATIVE to the connecting part's offset
	 * @param connectX The x-offset of the connecting part RELATIVE to the part's offset
	 * @param partY    The y-offset of the part RELATIVE to the connecting part's offset
	 * @param connectY The y-offset of the connecting part RELATIVE to the part's offset
	 * @param referenceAngle The reference angle for the joint to use
	 */
	private void makeJoint(int part, int connect, float partX, float connectX,
						   float partY, float connectY, float referenceAngle) {
		jointDef.bodyA = parts.get(connect).getBody();
		partCache.set(connectX, connectY);
		jointDef.localAnchorA.set(partCache);

		jointDef.bodyB = parts.get(part).getBody();
		partCache.set(partX, partY);
		jointDef.localAnchorB.set(partCache);

		jointDef.collideConnected = false;
		jointDef.referenceAngle = referenceAngle * DEG_TO_RAD;
	}

	private void setJointMotor(float motorSpeed, float maxTorque) {
		jointDef.motorSpeed = motorSpeed * DEG_TO_RAD;
		jointDef.maxMotorTorque = maxTorque;
		jointDef.enableMotor = true;
	}

	private void setJointAngleLimits(float rotationLimitLower, float rotationLimitUpper) {
		jointDef.lowerAngle = rotationLimitLower * DEG_TO_RAD;
		jointDef.upperAngle = rotationLimitUpper * DEG_TO_RAD;
		jointDef.enableLimit = true;
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
	 */
	public CharacterModel(TextureRegion[] textures, World w, float initialPositionX, float initialPositionY){
		parts = new Array<PartModel>();
		joints = new Array<Joint>();
		partTextures = textures;
		init(w, initialPositionX, initialPositionY);
		energy = 100f;
	}
}