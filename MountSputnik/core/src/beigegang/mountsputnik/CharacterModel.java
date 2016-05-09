package beigegang.mountsputnik;

import beigegang.util.FilmStrip;
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
	private FilmStrip[] partTextures;
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
			part.geometry.setUserData(part);
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
		if (connect != NONE) {
			partCache.set(-partX, -partY);
			partCache.add(parts.get(connect).getPosition());
			partCache.add(connectX, connectY);
		}
		else partCache.set(partX, partY);

		PartModel partModel = new PartModel(partCache.x, partCache.y, partTextures[part],
				CHARACTER_DRAW_SIZE_SCALE, drawPositionScale, this);
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
		if (connect != NONE) {
			partCache.set(-partX, -partY);
			partCache.add(parts.get(connect).getPosition());
			partCache.add(connectX, connectY);
		}
		else partCache.set(partX, partY);

		ExtremityModel extremityModel = new ExtremityModel(partCache.x, partCache.y,
				partTextures[part], CHARACTER_DRAW_SIZE_SCALE,
				drawPositionScale, this);
		extremityModel.setBodyType(BodyDef.BodyType.DynamicBody);
		parts.add(extremityModel);
		return extremityModel;
	}

	private void makeJoints(World world) {
		makeJoint(CHEST, HEAD, 0, CHEST_HEAD_OFFSET, 0);
		setJointAngleLimits(-10, 10);
		setJointMotor(0, 50);
		addJoint(world);

		makeJoint(HIPS, CHEST, 0, HIP_CHEST_OFFSET, 0);
		setJointAngleLimits(-45, 45);
		setJointMotor(0, 50);
		addJoint(world);

		makeJoint(ARM_LEFT, CHEST, ARM_X_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0);
		setJointAngleLimits(-90, 90);
		setJointMotor(0, 50);
		addJoint(world);
		makeJoint(ARM_RIGHT, CHEST, -ARM_X_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, -180);
		setJointAngleLimits(-90, 90);
		setJointMotor(0, 50);
		addJoint(world);

		makeJoint(FOREARM_LEFT, ARM_LEFT, FOREARM_X_ARM_OFFSET, FOREARM_Y_ARM_OFFSET, 0);
		setJointAngleLimits(2, 135);
		setJointMotor(0, 50);
		addJoint(world);
		makeJoint(FOREARM_RIGHT, ARM_RIGHT, -FOREARM_X_ARM_OFFSET, FOREARM_Y_ARM_OFFSET, 0);
		setJointAngleLimits(-135, -2);
		setJointMotor(0, 50);
		addJoint(world);

		makeJoint(HAND_LEFT, FOREARM_LEFT, HAND_X_OFFSET, HAND_Y_OFFSET, 0);
		setJointAngleLimits(-20, 60);
		setJointMotor(0, 50);
		addJoint(world);
		makeJoint(HAND_RIGHT, FOREARM_RIGHT, -HAND_X_OFFSET, HAND_Y_OFFSET, 0);
		setJointAngleLimits(-20, 60);
		setJointMotor(0, 50);
		addJoint(world);

		makeJoint(THIGH_LEFT, HIPS, THIGH_X_HIP_OFFSET, THIGH_Y_HIP_OFFSET, 0);
		setJointAngleLimits(-45, 90);
		setJointMotor(0, 50);
		addJoint(world);
		makeJoint(THIGH_RIGHT, HIPS, THIGH_X_HIP_OFFSET, THIGH_Y_HIP_OFFSET, 0);
		setJointAngleLimits(-90, 45);
		setJointMotor(0, 50);
		addJoint(world);

		makeJoint(SHIN_LEFT,  THIGH_LEFT, SHIN_X_THIGH_OFFSET, SHIN_Y_THIGH_OFFSET, 0);
		setJointAngleLimits(-150, -2);
		setJointMotor(0, 50);
		addJoint(world);
		makeJoint(SHIN_RIGHT, THIGH_RIGHT, -SHIN_X_THIGH_OFFSET, SHIN_Y_THIGH_OFFSET, 0);
		setJointAngleLimits(2, 150);
		setJointMotor(0, 50);
		addJoint(world);

		makeJoint(FOOT_LEFT, SHIN_LEFT, FOOT_X_OFFSET, FOOT_Y_OFFSET, 0);
		setJointAngleLimits(-90, 90);
		setJointMotor(0, 50);
		addJoint(world);
		makeJoint(FOOT_RIGHT, SHIN_RIGHT, -FOOT_X_OFFSET, FOOT_Y_OFFSET, 0);
		setJointAngleLimits(-90, 90);
		setJointMotor(0, 50);
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
	public CharacterModel(FilmStrip[] textures, World w, float initialPositionX,
						  float initialPositionY, Vector2 drawPositionScale){
		parts = new Array<PartModel>();
		joints = new Array<Joint>();
		partTextures = textures;
		init(w, initialPositionX, initialPositionY, drawPositionScale);
		energy = 100f;
	}

	public Joint getInnerJoint(int partA) {
		Joint result = getJoint(CHEST, partA);
		if (result == null)
			return getJoint(HIPS, partA);
		else return result;
	}

	public Joint getJoint(int partA, int partB) {
		Joint result = joints.get(Math.max(partA, partB) - 1);
		if (isCorrectJoint(partA, partB, result)) return result;
		else
			for (Joint joint : joints)
				if (isCorrectJoint(partA, partB, joint)) return joint;
		return null;
	}

	private boolean isCorrectJoint(int partA, int partB, Joint result) {
		return (result.getBodyA() == parts.get(partA).getBody()
				&& result.getBodyB() == parts.get(partB).getBody())
				|| (result.getBodyA() == parts.get(partB).getBody()
				&& result.getBodyB() == parts.get(partA).getBody());
	}

	/**
	 * @author Daniel
	 * Calculates new energy
	 * 
	 * @param gainModifier Environmental Gain Modifier
	 * @param lossModifier Environmental Loss Modifier
	 * @param rotationGain Whether or not rotation affects gain (would be false if in space or places with low gravity)
	 * @param exertion Current force being exerted by character
	 */
	public void updateEnergy(float gainModifier, float lossModifier, float exertion, boolean rotationGain){
		if(gainModifier == 0) return;
		int b = rotationGain ? 1 : 0;
		float angle = parts.get(CHEST).getAngle();
		float chesty = parts.get(CHEST).getPosition().y;
		
		int feet = ((ExtremityModel)(parts.get(FOOT_LEFT))).isGripping() ? 1 : 0;
		feet += ((ExtremityModel)(parts.get(FOOT_RIGHT))).isGripping() ? 1 : 0;
		int hands = ((ExtremityModel)(parts.get(HAND_LEFT))).isGripping()? 1 : 0;
		hands += ((ExtremityModel)(parts.get(HAND_RIGHT))).isGripping()? 1 : 0;
		int attached = feet+hands;
		
		float gain =0; float loss = 0;
		switch(attached){
			case 4: 
				gain = (float) (gainModifier*(1-Math.abs(b*Math.sin(angle/2)))*40);
				loss = lossModifier+exertion;
				break;
			case 3:
				if(((ExtremityModel)(parts.get(HAND_LEFT))).isGripping()
						&&((ExtremityModel)(parts.get(HAND_RIGHT))).isGripping()
						&& chesty < parts.get(HAND_LEFT).getPosition().y
						&& chesty < parts.get(HAND_RIGHT).getPosition().y){
					gain = (float) (gainModifier*(1-Math.abs(b*Math.sin(angle/2)))*40);
				}
				else{
					gain = (float) (gainModifier*(1-Math.abs(b*Math.sin(angle/2)))*5);
				}
				loss = lossModifier+exertion;
				break;
			case 2:
				if(((ExtremityModel)(parts.get(HAND_LEFT))).isGripping()
						&&((ExtremityModel)(parts.get(HAND_RIGHT))).isGripping()
						&& chesty < parts.get(HAND_LEFT).getPosition().y
						&& chesty < parts.get(HAND_RIGHT).getPosition().y){
					gain = (float) (gainModifier*(1-Math.abs(b*Math.sin(angle/2)))*5);
					loss = lossModifier+exertion;
				}
				else{
					gain = (float) (gainModifier*(1-Math.abs(b*Math.sin(angle/2)))/4);
					loss = lossModifier+exertion*5;
				}
				break;
			case 1:
				if((((ExtremityModel)(parts.get(HAND_LEFT))).isGripping()  && chesty < parts.get(HAND_LEFT).getPosition().y ) ||
			       (((ExtremityModel)(parts.get(HAND_RIGHT))).isGripping() && chesty < parts.get(HAND_RIGHT).getPosition().y)){
					gain = (float) (gainModifier*(1-Math.abs(b*Math.sin(angle/2)))/4);
					loss = lossModifier+exertion*5;
				}
				else{
					loss = lossModifier*10+exertion*10;
				}
				break;
			default:
				break;
		}
		float dEdt = (gain - loss)/60;
		float newEnergy = getEnergy() < 0 ? 0 : getEnergy() > 100 ? 100 : getEnergy() + dEdt;
		setEnergy(newEnergy);
	}
}
