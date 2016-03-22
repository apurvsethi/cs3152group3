package beigegang.mountsputnik;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;

import java.util.ArrayList;


public class GameMode extends ModeController {

	/**
	 * Track asset loading from all instances and subclasses
	 */
	protected AssetState assetState = AssetState.EMPTY;
	/**
	 * used for tracking the game timestep - used to snap limbs to handholds on original timestep
	 */
	private static int timestep = 0;
/**	both are updated every timestep with horizontal and vertical input from player */
	private static float inx = 0f;
	private static float iny = 0f;
	/**
	 * Strings for files used, string[] for parts, etc.
	 */
	private static final String BACKGROUND_FILE = "background.png";
	private static final String FOREGROUND_FILE = "preliminaryCharacterFilmStrip.png";
	private static final String HANDHOLD_TEXTURES[] = {"handholds.png", "handholdsglow.png", "handholdsgrabbed.png"};
	private static final String PART_TEXTURES[] = {"Ragdoll/Corrected/Head.png", "Ragdoll/Corrected/Torso.png", "Ragdoll/Corrected/Hips.png",
			"Ragdoll/Corrected/ArmLeft.png", "Ragdoll/Corrected/ArmRight.png", "Ragdoll/Corrected/ForearmLeft.png", "Ragdoll/Corrected/ForearmRight.png",
			"Ragdoll/Corrected/HandLeftUngripped.png", "Ragdoll/Corrected/HandRightUngripped.png", "Ragdoll/Corrected/ThighLeft.png",
			"Ragdoll/Corrected/ThighRight.png", "Ragdoll/Corrected/CalfLeft.png", "Ragdoll/Corrected/CalfRight.png", "Ragdoll/Corrected/FeetShoeLeft.png",
			"Ragdoll/Corrected/FeetShoeRight.png", "Ragdoll/Corrected/HandLeftGripped.png", "Ragdoll/Corrected/HandRightGripped.png"};

	/**
	 * font for displaying debug values to screen
	 */
	private static BitmapFont font = new BitmapFont();
	//says if character is upside down
	private static boolean upsideDown = false;
	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion foreground;
	private static TextureRegion[] holdTextures = new TextureRegion[HANDHOLD_TEXTURES.length];
	private static TextureRegion[] partTextures = new TextureRegion[PART_TEXTURES.length];

	/**
	 * Preloads the assets for this controller.
	 * <p/>
	 * Opted for nonstatic loaders, but still want the assets themselves to be
	 * static. So AssetState determines the current loading state, only load if
	 * assets are not already loaded.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		if (assetState != AssetState.EMPTY) return;

		assetState = AssetState.LOADING;
		manager.load(BACKGROUND_FILE, Texture.class);
		assets.add(BACKGROUND_FILE);
		manager.load(FOREGROUND_FILE, Texture.class);
		assets.add(FOREGROUND_FILE);
		for (int i = 0; i < HANDHOLD_TEXTURES.length; i++) {
			manager.load(HANDHOLD_TEXTURES[i], Texture.class);
			assets.add(HANDHOLD_TEXTURES[i]);
		}
		for (int i = 0; i < PART_TEXTURES.length; i++) {
			manager.load(PART_TEXTURES[i], Texture.class);
			assets.add(PART_TEXTURES[i]);
		}
	}

	/**
	 * Loads the assets for this controller.
	 * <p/>
	 * Opted for nonstatic loaders, but still want the assets themselves to be
	 * static. So AssetState determines the current loading state, only load if
	 * assets are not already loaded.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (assetState != AssetState.LOADING) return;

		background = createTexture(manager, BACKGROUND_FILE, false);
		//if (background == null) System.out.println("Wrong");
		foreground = createTexture(manager, FOREGROUND_FILE, false);
		for (int i = 0; i < HANDHOLD_TEXTURES.length; i++) {
			holdTextures[i] = createTexture(manager, HANDHOLD_TEXTURES[i], false);
		}
		for (int i = 0; i < PART_TEXTURES.length; i++) {
			partTextures[i] = createTexture(manager, PART_TEXTURES[i], false);
		}
		assetState = AssetState.COMPLETE;
	}

	/**
	 * Whether we have completed this level
	 */
	private boolean complete;
	/**
	 * Whether we have failed at this level (and need a reset)
	 */
	private boolean failed;

	/**
	 * Character of game
	 */
	private CharacterModel character;
	/**
	 * A handhold
	 */
	private HandholdModel handhold;
	/**
	 * holds any extremities who's buttons were just released this timestep. cleared out every timestep
	 */
	private ArrayList<Integer> justReleased = new ArrayList<Integer>();
	/**
	 * holds any extremities who's buttons are pressed during this timestep. keeps order of pressing intact
	 */
	private ArrayList<Integer> nextToPress = new ArrayList<Integer>();

	public GameMode() {
		super(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GRAVITY);

		//create debug font
		font.setColor(Color.RED);
		font.getData().setScale(5);
	}

	@Override
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity());

		for (GameObject obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		timestep = 0;
		world = new World(gravity, false);
		contactListener = new ListenerClass();
		world.setContactListener(contactListener);
		populateLevel();
	}

	public void populateLevel() {
		character = new CharacterModel(partTextures, world);
		objects.add(character);
		for (PartModel p : character.parts) {
			objects.add(p);
		}
		// TODO: Populate level with whatever pieces and part are necessary (handholds, etc)
		// Will probably do through a level generator later, level model access
//		for (int i = 0; i < HANDHOLD_NUMBER; i++) {
////			handhold = new HandholdModel(holdTextures[0].getTexture(), holdTextures[1].getTexture(), 50, 50, 100*i+500, 20*i+500);
//
//			handhold = new HandholdModel(holdTextures[0].getTexture(), holdTextures[1].getTexture(), holdTextures[2].getTexture(), 50, 50, 150 * i + 500, 500);
//			handhold.activatePhysics(world);
//			handhold.setBodyType(BodyDef.BodyType.StaticBody);
//			handhold.geometry.setUserData("handhold");
//			objects.add(handhold);
//		}
//
//		for (int i : EXTREMITIES) {
//			PartModel i1 = character.parts.get(i);
//
//			handhold = new HandholdModel(holdTextures[0].getTexture(), holdTextures[1].getTexture(), holdTextures[2].getTexture(), 50, 50, i1.getX(), i1.getY());
//			handhold.activatePhysics(world);
//			handhold.setBodyType(BodyDef.BodyType.StaticBody);
//			handhold.geometry.setUserData("handhold");
//			objects.add(handhold);
//		}
	}


	public int addToButtonsPressed(int part) {
		if (!nextToPress.contains(part)) {
			nextToPress.add(part);
			return 1;
		}
		return 0;
	}

	public int checkIfJustReleased(int part) {
		if (nextToPress.remove((Integer) part)) {
			justReleased.add(part);
			return 1;
		}
		return 0;
	}

	/**
	 * IMPORTANT METHOD. A TON OF STUFF GOES DOWN IN THIS METHOD
	 *
	 * @param dt This method computes an order for the selected limbs based on previous timesteps and the first limb in nextToPress
	 *           is the limb that can be controlled.
	 *           this method ungrips all selected limbs and then calculates the force that can be imparted on the main selected
	 *           limb based on the forces the other limbs can impart with plenty of heuristics
	 *           if no force is imparted, it uses dampening on all of the limbs that are not gripping a handhold.
	 *           it then snaps limbs to a viable handhold within SNAP_RADIUS that were just released this timestep
	 *           <p/>
	 *           special case: on the zeroth timestep/very first call to update at start of game,
	 *           it snaps limbs to any handhold in radius.
	 */
	public void update(float dt) {
//		//System.out.println("UPDATE");
//		InputController input = InputController.getInstance();
//		inx = input.getHorizontal();
//		iny = input.getVertical();
//		//System.out.println(input.didLeftLeg() + " " + input.didRightLeg() + " " + input.didRightArm() + " " + input.didLeftArm());
////clear all in justReleased
//		justReleased.clear();
////		see if character is upsideDown
//		upsideDown = character.parts.get(HEAD).getPosition().y - character.parts.get(CHEST).getPosition().y >= 0;
////		if(input.getHorizontal()!=0){
////		character.parts.get(HEAD).setVX(-100f);
////		}
////		if(input.getVertical()!=0){
////			character.parts.get(HEAD).setVY(100f);
////		}
//
////		if(input.getHorizontal()!=0){
////			character.parts.get(HAND_LEFT).setVX(input.getHorizontal()*100f);
////		}
////		if(input.getVertical()!=0){
////			character.parts.get(HAND_LEFT).setVY(input.getVertical()*100f);
////		}
//
//		if (input.getHorizontal() != 0) {
//			character.parts.get(HAND_LEFT).setVX(input.getHorizontal() * 100f);
//		}
//		if (input.getVertical() != 0) {
//			character.parts.get(HAND_LEFT).setVY(input.getVertical() * 100f);
//		}
//
//		//figure out whats pressed and whats been released this timestep (next 4 lines)
//		int a = input.didLeftLeg() == true ? addToButtonsPressed((FOOT_LEFT)) : checkIfJustReleased(FOOT_LEFT);
//		int b = input.didRightLeg() == true ? addToButtonsPressed((FOOT_RIGHT)) : checkIfJustReleased(FOOT_RIGHT);
//		int c = input.didLeftArm() == true ? addToButtonsPressed((HAND_LEFT)) : checkIfJustReleased(HAND_LEFT);
//		int d = input.didRightArm() == true ? addToButtonsPressed((HAND_RIGHT)) : checkIfJustReleased(HAND_RIGHT);
//
//		float y = input.getVertical();
//
//		Vector2 force = new Vector2(0, 0);
////		ungrip all selected limbs (safety measures) and apply force to limb.
//		if (nextToPress.size() > 0) {
////			next two lines ungrip all selected extremities.
//			ExtremityModel curPart = ((ExtremityModel) (character.parts.get(nextToPress.get(0))));
//
//			for (int i : nextToPress) {
//				((ExtremityModel) (character.parts.get(i))).ungrip();
//				//System.out.println("ungripped " + ENAMES[i]);
//
//			}
//			for (int ext:EXTREMITIES){
//				if (((ExtremityModel) (character.parts.get(ext))).isGripping()){
//					force.add(calcForce(ext,nextToPress.get(0)));
//				}
//			}
//			/*
//			No threshold AS OF now
//			float threshold = 1f;
//			//able to apply force if its greater than the threshold (minimum needed to have effect on the body)
//			//wont apply dampening if its > 0
//			if (force.y > threshold) {
//			//can't have too high of a force!
//			//needs to be in there.
//			//				if (force.y > MAX_FORCE_THRESHOLD.y) force = MAX_FORCE_THRESHOLD;
//			//apply the force to the body part.
//			curPart.setVY(force.scl(Math.signum(y)).y);
//			curPart.setVX(input.getHorizontal() * 100f);
//			*/
////apply force to center of the body part.
//			curPart.body.applyForceToCenter(force,true);
//		}
////Nothing pressed! Apply dampening to any ungripping limbs. Right now it's using velocity though.
// 		else {
////			nextToPress is empty - apply dampening to any limbs that aren't on a rock right now.
//			applyDampening();
//		}
////			character.parts.get(lastPressed).body.applyForceToCenter(0,force,false);
//// else if (nextToPress != NONE) {
////			force = calculateForce(nextToPress,input);
////		}
//		if (justReleased.size() > 0 || timestep == 0) {
//			snapLimbsToHandholds(input);
//		}
//		glowHandholds();
//		timestep += 1;
//
//		// TODO: Use inputController methods to select limbs,
//		//       horizontal and vertical to move them
//
//		//move camera with character
//		canvas.setCameraPosition(GAME_WIDTH / 2,
//				character.parts.get(CHEST).getBody().getPosition().y);
//
//		// TODO: Movements of other objects (obstacles, eventually)
//		// NOWHERE CLOSE
//
//		// TODO: Interactions between limbs and handholds
////		 DONE
//
//		// TODO: Update energy quantity (fill in these values)
////		not tested yet
//		float dEdt = calculateEnergyChange(1, 1, force, true);
//		character.setEnergy(character.getEnergy() + dEdt);
	}

	private void applyDampening() {
		for (int ext : EXTREMITIES){
			if (!((ExtremityModel)(character.parts.get(ext))).isGripping()){
				float thisDampX = DAMPENING_X;
				float thisDampY = DAMPENING_Y;
				Vector2 vel = character.parts.get(ext).getLinearVelocity();

				if (vel.y > 0) {
					if (vel.y - DAMPENING_Y < 0) thisDampY = vel.y;
					character.parts.get(ext).setVY(vel.y - thisDampY);
				}
				if (vel.x > 0) {
					if (vel.x - DAMPENING_X < 0) thisDampX = vel.x;
					character.parts.get(ext).setVX(vel.x - thisDampX);

				}
				//if neg both
				if (vel.y < 0) {
					if (vel.y + DAMPENING_Y > 0) thisDampY = -1 * vel.y;
					character.parts.get(ext).setVY(vel.y + thisDampY);
					//					character.parts.get(lastPressed).body.applyForceToCenter(DAMPENING_Y.sub(force),true);
				}
				if (vel.x < 0) {
					if (vel.x + DAMPENING_X > 0) thisDampX = -1 * vel.x;
					character.parts.get(ext).setVX(vel.x + thisDampX);
				}
			}
		}
	}

	/**
	 * this method glows any handholds close enough for the person's extremity to grab.
	 * //TODO there will be a possible issue if person at full extension and snaps to a handhold out of their reach.
	 * //	 * implement a calculation which says that handhold distance <= MAX_ARM_DIST or MAX_LEG_DIST.
	 */
	private void glowHandholds() {
		for (GameObject obj : objects) {
			if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
				HandholdModel h = (HandholdModel) obj;
				h.unglow();
//				go through extremities
				for (int e : EXTREMITIES) {
					for (Vector2 snapPoint : h.snapPoints) {
						if (closeEnough(e, snapPoint)) {
							h.glow();
							break;
						}
					}
				}
			}
		}
	}

	private void snapLimbsToHandholds(InputController input) {
		for (int i : justReleased) {
			switch (i) {
				case FOOT_LEFT:

					snapIfPossible(FOOT_LEFT);

					break;
				case FOOT_RIGHT:
					snapIfPossible(FOOT_RIGHT);

					break;
				case HAND_LEFT:
					snapIfPossible(HAND_LEFT);

					break;
				case HAND_RIGHT:
					snapIfPossible(HAND_RIGHT);

					break;

				default:
					//should never get here
					break;
			}
		}
		if (timestep == 0) {
			snapIfPossible(FOOT_LEFT);
			snapIfPossible(HAND_LEFT);
			snapIfPossible(HAND_RIGHT);
			snapIfPossible(FOOT_RIGHT);

		}
	}

	private void snapIfPossible(int limb) {
		for (GameObject obj : objects) {
			if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
				HandholdModel h = (HandholdModel) obj;
				for (Vector2 snapPoint : h.snapPoints) {
					if (closeEnough(limb, snapPoint)) {
						character.parts.get(limb).setPosition(snapPoint);
						((ExtremityModel) character.parts.get(limb)).grip(h);
						character.parts.get(limb).body.setType(BodyDef.BodyType.StaticBody);
//						System.out.println("SNAPDADDY");
					}
				}

			}
		}
	}

	/**
	 * helper function to check if limb is close enough to a snapPoint on a handhold
	 * used for both snapping limbs to handholds and glowing handholds showing player they're close enough to snap
	 */
	private boolean closeEnough(int limb, Vector2 snapPoint) {
		Vector2 dist = character.parts.get(limb).getPosition().sub(snapPoint);
		return (Math.sqrt(dist.x * dist.x + dist.y * dist.y) <= SNAP_RADIUS);
	}


	/**
	 * @param hookedPart - calculation of force for an extremity attached to a handhold
	 * @param freePart - part that the force will be applied to
	 * calculates X and Y force player can use to propel their selected limb.
	 * should use the size of the handhold in the calculation although it does not.
	 */
	private Vector2 calcForce(int hookedPart, int freePart) {


		//does him going down actually matter to the physics??
		//I mean, like if he reaches down
		float forcex = 0f;
		float forcey = 0f;
		//calculate X aspect, I think Horizontal force should simply be a constant.

		forcex = inx * CONSTANT_X_FORCE;

//normal calculations.
		//these are calculations for the Y aspect of the deal
		if (!upsideDown) {

			//leg forces.
			//push force or pull force depending on extremity location to the moving part's location
			//then use angle of hip and angle of knee to figure out force compared
			Vector2 hp = character.parts.get(hookedPart).getPosition();
			Vector2 fp = character.parts.get(freePart).getPosition();
//			if (hp.y - fp.y < 0) {//lower than it
			//first focus on if he needs it to push up.
			if (hookedPart == FOOT_LEFT || hookedPart == FOOT_RIGHT) {
				if (iny > 0) {
					float angleKnee = 0f;
					float footToHip = 0f;
					float angleKneeModifier = 0f;
					float distanceModifier = 0f;
					float totalModifier = 0f;
					//get angles in degrees.
//					use y distance of foot from hip to do calculations instead of using hip angle.
//					still want to use knee angle.
					//angleknee positive if hip is rotated inward for left leg, positive if rotated out for right leg?
//					could just ABS angleKnee for now/mod by 180.
					if (hookedPart == FOOT_LEFT) {
						angleKnee = ((RevoluteJoint) (character.joints.get(SHIN_LEFT - 1))).getJointAngle() * RAD_TO_DEG;
						//TODO modify this because not sure how angles actually work with knees.
						angleKnee = Math.abs(angleKnee%270);
						angleKneeModifier = angleKnee <= 90f ? angleKnee / 90f : 90f / angleKnee;
					} else {
						angleKnee = ((RevoluteJoint) (character.joints.get(SHIN_RIGHT - 1))).getJointAngle() * RAD_TO_DEG;
//						//TODO modify this as necessary because not sure how angles actually work with knees.
						angleKnee = Math.abs(angleKnee%270);
						angleKneeModifier = angleKnee <= 90f ? angleKnee / 90f : 90f / angleKnee;
					}
					//in case.
					angleKneeModifier = Math.abs(angleKneeModifier);
					//calculate x foot distance from hip
					footToHip = Math.abs(hp.x - character.parts.get(HIPS).getPosition().x);

					distanceModifier = (Math.abs(MAX_LEG_DIST) - footToHip)/Math.abs(MAX_LEG_DIST);
//					should probably be a more complex modifier!
					totalModifier = (angleKneeModifier + distanceModifier) / 2f;
					//always a positive force up if input vertical is up
					forcey = totalModifier * MAX_PUSHFORCE_LEG;
				} else {
					//I think that this should be basically unlimited amount force if you're lowering a limb then
//						it should be easy, there's not much force the legs use for this but this way it always happens.
//						return a negative number because that way the force for the limb can be channeled easily.
					forcey = -1 * MAX_PUSHFORCE_LEG;
				}


			} else {
				if (iny > 0) {
					float angleElbow = 0f;
					Vector2 armToShoulder;
					float angleElbowModifier = 0f;
					//the Y here is correct. need to subtract  ARM_OFFSET for x position for right hand,
 					//add it for left hand.
					armToShoulder = hp.sub(character.parts.get(CHEST).getPosition());
					armToShoulder.y = armToShoulder.y - ARM_Y_CHEST_OFFSET;
					//absolute x distance of hand to shoulder.
					if (hookedPart == HAND_LEFT) {
						angleElbow = ((RevoluteJoint) (character.joints.get(HAND_LEFT - 1))).getJointAngle() * RAD_TO_DEG;
						angleElbowModifier = angleElbow <= 90f ? angleElbow / 90f : 90f / angleElbow;
						//add because hand left + offset - chest = 0 in best case.
						armToShoulder.x = armToShoulder.x + ARM_X_CHEST_OFFSET;
					}
					else{
						angleElbow = ((RevoluteJoint) (character.joints.get(HAND_RIGHT - 1))).getJointAngle() * RAD_TO_DEG;
						angleElbowModifier = angleElbow <= 180f ? angleElbow / 180f : 180f / angleElbow;
						armToShoulder.x = armToShoulder.x - ARM_X_CHEST_OFFSET;
					}
					armToShoulder.x = Math.abs(armToShoulder.x);
					//just in case - will be something needing debugging.
					angleElbowModifier = Math.abs(angleElbowModifier);

					//TODO modify this because not sure how angles actually work with elbows.
					//closer to 1 = the closer the hand is to the shoulder
					float distanceModifier = (Math.abs(MAX_ARM_DIST) - armToShoulder.x) / Math.abs(MAX_ARM_DIST);
					float totalModifier = (angleElbowModifier + distanceModifier) / 2f;
					//if the arm is going to impart pull or push force (legs cant really do that)
					//always a positive force up if input vertical is up

					forcey = armToShoulder.y > 0 ?
							totalModifier * MAX_PULLFORCE_ARM : totalModifier * MAX_PUSHFORCE_ARM;
					}
					//working with ARMS NOW!!!!
				else {
					//I think that this should be basically unlimited amount force if you're lowering a limb then
//						it should be easy, there's not much force the legs use for this but this way it always happens.
//						return a negative number because that way the force for the limb can be channeled easily.
					forcey = -1 * MAX_PULLFORCE_ARM;


				}
			}
		}

		else {
			//feet are only pull (if you push, your feet would fall off the handholds if you're upside-down lol
			//
			//JUST do nothing now - a.k.a please don't go upside down lol.

		}

	return new Vector2(forcex,forcey);

}





	private float calculateForce(ExtremityModel curPart,InputController input) {
//		ExtremityModel curPart = ((ExtremityModel)(character.parts.get(currentLimb)));
		curPart.ungrip();
		//force NOT based off function of how far tilting joystick
		//based on how long joystick in certain direction
		float x = input.getHorizontal();
		float y = input.getVertical();
//		if (y > -.2 && y < .2) y = 0;
//		if (x > -.2 && x < .2) y = 0;
//compare where each extremity is in comparison to hand for y value
		float curY = curPart.getY();
		//if extremity above
		float forceFactor = 0.0f;
//		each arm
		float totalForce = 0f;
				//check distance from center of mass for Y - its own function
				//check distance from for X -



		for (int ex: EXTREMITIES){
			ExtremityModel e = (ExtremityModel)(character.parts.get(ex));
			float eFactor = 0;
			boolean pull = false;
			boolean isLeft = false;
			boolean isArm = false;
//			if (e.isGripping()){
			if (1+2 < 4){

				isLeft = ex == HAND_LEFT || ex == FOOT_LEFT;
				isArm = ex == HAND_LEFT || ex == HAND_RIGHT;
				pull =  e.getY() > curY;
				eFactor = pull ? e.getPull():e.getPush();

				if (pull){

					Vector2 distance = e.getPosition().sub(character.parts.get(CHEST).getPosition());
					//calculate distance from shoulder for width (closer the 0 the better)
					//
					//what side it's on.
//					incorrect code here - does not compute distances properly in the least bit.
					float shoulderOffset = isLeft? -1 * CHEST_X_ARM_OFFSET: CHEST_X_ARM_OFFSET;
					float distFromShoulder = Math.abs(distance.x - (character.getPosition().x + shoulderOffset));
					float distFromYCenter = Math.abs(distance.y - character.getPosition().y);

//					System.out.println(distFromShoulder + " " + distFromYCenter);
//					System.out.println(MAX_ARM_DIST + " " + MAX_LEG_DIST);

					float pullPercY = character.calcPullPercentageY(distFromYCenter,isArm);

					float pullPercX = character.calcPullPercentageX(distFromShoulder,isArm);
//					System.out.println(pullPercX + " " + pullPercY);
//					System.out.println(totalForce + " before Pull" );
					totalForce += pullPercX * pullPercY * e.getPull();
//					System.out.println(totalForce + " After pull" );


				}else{
					Vector2 distance = e.getPosition().sub(character.parts.get(CHEST).getPosition());
					float distFromShoulder = Math.abs(distance.x - character.getPosition().x);
					float distFromYCenter = Math.abs(distance.y - character.getPosition().y);

					float pushPercY = character.calcPushPercentageY(distFromYCenter,isArm);

					float pushPercX = character.calcPushPercentageX(distFromShoulder,isArm);
//					System.out.println(pushPercX + " " + pushPercY);
//					System.out.println(totalForce + " before Push" );
					totalForce += pushPercX * pushPercY * e.getPush();
//					System.out.println(totalForce + " After push" );

				}
			}
		}
		return totalForce;

//		double tan1 = Math.atan(y/x);

//		boolean up = 0;
//		boolean right = 0;


	}

	/**
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
	 * 
	 * @return change in energy value
	 */
	private float calculateEnergyChange(float gainModifier, float lossModifier, Vector2 force, boolean rotationGain){
		int b = rotationGain ? 1 : 0;
		float angle = character.parts.get(CHEST).getAngle();
		float exertion = force.y; //TODO figure out what this value should be
		
		//Determine how many limbs are currently attached
		int feet = character.parts.get(FOOT_LEFT).getBody().getType() == BodyDef.BodyType.StaticBody ? 1 : 0;
		feet += character.parts.get(FOOT_RIGHT).getBody().getType() == BodyDef.BodyType.StaticBody ? 1 : 0;
		int hands = character.parts.get(HAND_LEFT).getBody().getType() == BodyDef.BodyType.StaticBody ? 1 : 0;
		hands += character.parts.get(HAND_RIGHT).getBody().getType() == BodyDef.BodyType.StaticBody ? 1 : 0;
		
		//Refer to equation in Javadoc comment
		float gain = (float) (ENERGY_GAIN_MULTIPLIER * (1-b*Math.sin(angle/2.0)) * BASE_ENERGY_GAIN * gainModifier);
		float loss = ENERGY_LOSS_MULTIPLIER * (exertion + 1) * lossModifier * (3-feet) * (3 - hands);
		//you don't lose energy if you're just falling
		loss = feet == 0 && hands == 0 ? 0 : loss;
		float change = gain - loss - ENERGY_LOSS;
		
		//Energy only ranges from 0 to 100
		//Do you still like ternary operators Meg?
		change = character.getEnergy()>=100 && change > 0 ? 0 : character.getEnergy()<=0 && change < 0 ? 0 : change;
		return change;
	}
	
	public void draw() {
		canvas.clear();
		canvas.setBackground(background.getTexture());
//		canvas.begin();
//		canvas.draw(background, Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());
//		canvas.end();
//		
//		canvas.begin();
//		canvas.draw(foreground, Color.WHITE, canvas.getWidth()/5, 0,canvas.getWidth()*3/5,canvas.getHeight());
//		canvas.end();
		
		canvas.begin();
		for(GameObject obj : objects) {
			obj.draw(canvas);
		}
		
		//debug energy text in top left of screen
		canvas.drawText(((Integer)(Math.round(character.getEnergy()))).toString(), font, 0f, 
				canvas.getCamera().position.y+GAME_HEIGHT/2-50f);
		canvas.end();
		
		if (debug) {
			canvas.beginDebug();
			for(GameObject obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		listener.exitScreen(this, EXIT_PAUSE);
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// Shouldn't need to do anything to resume for now, can change focus of screen
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}
	
	public void dispose(){
		font.dispose();
		super.dispose();
	}
}
