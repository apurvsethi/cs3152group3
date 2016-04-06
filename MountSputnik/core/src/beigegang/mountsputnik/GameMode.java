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
	private static final String PART_TEXTURES[] = {"Ragdoll/Corrected/Torso.png", "Ragdoll/Corrected/Head.png", "Ragdoll/Corrected/Hips.png",
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
		for (String HANDHOLD_TEXTURE : HANDHOLD_TEXTURES) {
			manager.load(HANDHOLD_TEXTURE, Texture.class);
			assets.add(HANDHOLD_TEXTURE);
		}
		for (String PART_TEXTURE : PART_TEXTURES) {
			manager.load(PART_TEXTURE, Texture.class);
			assets.add(PART_TEXTURE);
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

	/**
	 * TODO add javadoc
	 */
	private void populateLevel() {
		character = new CharacterModel(partTextures, world, DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 4);
		for (PartModel p : character.parts) {
			p.setDrawPositionScale(scale);
			p.setDrawSizeScale(0.4f, 0.4f);
			objects.add(p);
		}
//		// TODO: Change to a level generator
//		for (int i = 0; i < HANDHOLD_NUMBER; i++) {
//			handhold = new HandholdModel(holdTextures[0].getTexture(), holdTextures[1].getTexture(), holdTextures[2].getTexture(), 50, 50, 150 * i + 500, 500);
//			handhold.activatePhysics(world);
//			handhold.setBodyType(BodyDef.BodyType.StaticBody);
//			handhold.geometry.setUserData("handhold");
//			handhold.setDrawPositionScale(scale);
//			objects.add(handhold);
//		}
//		for (int i : EXTREMITIES) {
//			PartModel i1 = character.parts.get(i);
//			handhold = new HandholdModel(holdTextures[0].getTexture(), holdTextures[1].getTexture(), holdTextures[2].getTexture(), 50, 50, i1.getX(), i1.getY());
//			handhold.activatePhysics(world);
//			handhold.setBodyType(BodyDef.BodyType.StaticBody);
//			handhold.geometry.setUserData("handhold");
//			handhold.setDrawPositionScale(scale);
//			objects.add(handhold);
//		}
	}

	/**
	 * @author Jacob
	 * if button was not pressed on the previous turn but is pressed now, add to nextToPress
	 * note: cannot use set because order must be preserved for accurate/predictive control by player
	 * @param part
     * @return true if part was just pressed, false otherwise
     */
	public boolean addToButtonsPressed(int part) {
		boolean notRedundant = !nextToPress.contains(part);
		if(notRedundant)
			nextToPress.add(part);
		return notRedundant;
	}

	/**
	 * @author Jacob
	 * checks if that extremity was released on this timestep by player, if so adds to justReleased
 	 * @param part
	 * @return true if part was just released, false otherwise
     */
    public boolean checkIfJustReleased(int part) {
		boolean present = nextToPress.remove((Integer) part);
		if (present)
			justReleased.add(part);
		return present;
	}

	/**
	 * @authors Jacob, Daniel
	 * @param dt This method computes an order for the selected limbs based on previous timesteps and the first limb in nextToPress
	 *           is the limb that can be controlled.
	 *           this method ungrips all selected limbs and then calculates the force that can be imparted on the main selected
	 *           limb based on the forces the other limbs can impart with plenty of heuristics
	 *           if no force is imparted, it uses dampening on all of the limbs that are not gripping a handhold.
	 *           it then snaps limbs to a viable handhold within HANDHOLD_SNAP_RADIUS that were just released this timestep
	 *           <p/>
	 *           special case: on the zeroth timestep/very first call to update at start of game,
	 *           it snaps limbs to any handhold in radius.
	 */
	public void update(float dt) {
		//System.out.println("UPDATE");
		InputController input = InputController.getInstance();
		inx = input.getHorizontal();
		iny = input.getVertical();

		justReleased.clear();
		upsideDown = character.parts.get(HEAD).getPosition().y - character.parts.get(CHEST).getPosition().y <= 0;
		//TODO: unused code?, should be removed
		//figure out whats pressed and whats been released this timestep (next 4 lines)
		boolean a = input.didLeftLeg() ? addToButtonsPressed((FOOT_LEFT)) : checkIfJustReleased(FOOT_LEFT);
		boolean b = input.didRightLeg() ? addToButtonsPressed((FOOT_RIGHT)) : checkIfJustReleased(FOOT_RIGHT);
		boolean c = input.didLeftArm() ? addToButtonsPressed((HAND_LEFT)) : checkIfJustReleased(HAND_LEFT);
		boolean d = input.didRightArm() ? addToButtonsPressed((HAND_RIGHT)) : checkIfJustReleased(HAND_RIGHT);

//		float y = input.getVertical();

		Vector2 force = new Vector2(0, 0);
		if (nextToPress.size() > 0) {
//			System.out.println(ENAMES[nextToPress.get(0)]);
//			next two lines ungrip all selected extremities.
			ExtremityModel curPart = ((ExtremityModel) (character.parts.get(nextToPress.get(0))));
			for (int i : nextToPress) {
//				System.out.println(ENAMES[i]);
				((ExtremityModel) (character.parts.get(i))).ungrip();
				//System.out.println("ungripped " + ENAMES[i]);
			}
			for (int ext:EXTREMITIES){
				if (((ExtremityModel) (character.parts.get(ext))).isGripping())
					force.add(calcForce(ext,nextToPress.get(0)));
			}
			applyForce(nextToPress.get(0),force.scl(1000000),true);

			for (int extr :EXTREMITIES){
				Vector2 vect =  character.parts.get(extr).getLinearVelocity();
				character.parts.get(extr).setLinearVelocity(boundVelocity(vect));
			}

		}
 		else {
			applyDampening();
		}
		if (justReleased.size() > 0 || timestep == 0) {
			snapLimbsToHandholds(input);
		}
		glowHandholds();
		timestep += 1;

		// TODO: Use inputController methods to select limbs,
		//       horizontal and vertical to move them

		//move camera with character
		canvas.setCameraPosition(canvas.width / 2,
						character.parts.get(CHEST).getBody().getPosition().y);
//move camera with character

//		canvas.setCameraPosition(canvas.getWidth()/2,
//				character.parts.get(CHEST).getBody().getPosition().y * canvas.getHeight()/DEFAULT_HEIGHT);
//		if(canvas.getCamera().position.y > backgroundTexture.getTexture().getHeight()-canvas.getHeight()){
//			canvas.setCameraPosition(canvas.getWidth()/2, backgroundTexture.getTexture().getHeight()-canvas.getHeight());
//		}
		if(canvas.getCamera().position.y < canvas.getHeight()/2){
			canvas.setCameraPosition(canvas.getWidth()/2, canvas.getHeight()/2);
//			System.out.println("here");
		}

		// TODO: Movements of other objects (obstacles, eventually)

		// TODO: Update energy quantity (fill in these values)
		//TODO: MAKE THIS NOT UNGRIP EVERY LIMB ALL THE TIME
//		float dEdt = calculateEnergyChange(1, 1, force, true);
//		float newEnergy = character.getEnergy() < 0 ? 0 : character.getEnergy() > 100 ? 100 : character.getEnergy() + dEdt;
//		character.setEnergy(newEnergy);
//		if (newEnergy <= 0){
//			for(int e : EXTREMITIES){
//				 ExtremityModel extremity = (ExtremityModel) character.parts.get(e);
//				 extremity.ungrip();
//				 extremity.body.setType(BodyDef.BodyType.DynamicBody);
//				 extremity.setTexture(partTextures[e].getTexture());
//			}
//		}
	}


	/**
	 * @author Jacob
	 * @param vect - current linear velocity vector of an extremity
     * @return bounded linear velocity vector
     */
	private Vector2 boundVelocity(Vector2 vect) {
		if (vect.x>0) vect.x = Math.min(EXTREMITY_MAX_X_VELOCITY,vect.x);
		else vect.x = Math.max(-1 * EXTREMITY_MAX_X_VELOCITY,vect.x);
		if (vect.y>0) vect.y = Math.min(EXTREMITY_MAX_Y_VELOCITY,vect.y);
		else vect.y = Math.max(-1 * EXTREMITY_MAX_Y_VELOCITY,vect.y);
		return vect;
	}
	/**
	 * @author Jacob
	 * !!!currently not functioning!!!
	 * will apply dampening to any limb not currently controlled by the player & are unattached to a handhold
	 * will help limbs not swing around wildly after player releases them.
 	 */
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
				}
				if (vel.x < 0) {
					if (vel.x + DAMPENING_X > 0) thisDampX = -1 * vel.x;
					character.parts.get(ext).setVX(vel.x + thisDampX);
				}
			}
		}
	}

	/**
	 * @author Jacob
	 * this method glows any handholds close enough for the person's extremity to grab.
	 * //TODO there will be a possible issue if person at full extension and snaps to a handhold out of their reach.
	 * //	 * implement a calculation which says that handhold distance <= MAX_ARM_DIST or MAX_LEG_DIST.
	 * 
	 */
	private void glowHandholds() {
		for (GameObject obj : objects) {
			if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
				HandholdModel h = (HandholdModel) obj;
				h.unglow();
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

	/**
	 * @author Jacob
	 * snaps any limbs in justReleased (limbs player controlled last timestep but no longer does) to closest handhold
	 * if possible
	 * @param input
     */
	private void snapLimbsToHandholds(InputController input) {
		for (int i : justReleased) {
			snapIfPossible(i);
		}
		if (timestep == 0) {
			snapIfPossible(FOOT_LEFT);
			snapIfPossible(HAND_LEFT);
			snapIfPossible(HAND_RIGHT);
			snapIfPossible(FOOT_RIGHT);

		}
	}
	
	/**
	 * @author Jacob
	 * snaps limb to handhold if possible.
	 * @param limb - limb to snap if possible
	 * */
	private void snapIfPossible(int limb) {
		for (GameObject obj : objects) {
			if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
				HandholdModel h = (HandholdModel) obj;
				for (Vector2 snapPoint : h.snapPoints) {
					if (closeEnough(limb, snapPoint)) {
						character.parts.get(limb).setPosition(snapPoint);
						((ExtremityModel) character.parts.get(limb)).grip();
						character.parts.get(limb).body.setType(BodyDef.BodyType.StaticBody);
					}
				}

			}
		}
	}

	/**
	 * @author Jacob
	 * helper function to check if limb is close enough to a snapPoint on a handhold
	 * used for both snapping limbs to handholds and glowing handholds showing player they're close enough to snap
	 *
	 * @param limb - limb to check for closeness
	 * @param snapPoint - point on handhold to check distance to
	 *
	 *
	*/

	private boolean closeEnough(int limb, Vector2 snapPoint) {
		Vector2 dist = character.parts.get(limb).getPosition().sub(snapPoint);
		return (Math.sqrt(dist.x * dist.x + dist.y * dist.y) <= HANDHOLD_SNAP_RADIUS);
	}


	/**
	 * @author Jacob
	 * @param hookedPart - calculation of force for an extremity attached to a handhold
	 * @param freePart - part that the force will be applied to
	 * calculates X and Y force player can use to propel their selected limb.
	 * should use the size of the handhold in the calculation although it does not.
	 */

	//TODO: refactor this method, get rid of the nested ifs, I'd do it but im not sure exactly how it works
	private Vector2 calcForce(int hookedPart, int freePart) {
		float forcex = 0f;
		float forcey = 0f;
		forcex = inx * CONSTANT_X_FORCE;
		
		if (!upsideDown) {
			Vector2 hp = character.parts.get(hookedPart).getPosition();
			Vector2 fp = character.parts.get(freePart).getPosition();
			if (hookedPart == FOOT_LEFT || hookedPart == FOOT_RIGHT) {
				if (iny > 0) {
					float angleKnee = 0f;
					float footToHip = 0f;
					float angleKneeModifier = 0f;
					float distanceModifier = 0f;
					float totalModifier = 0f;
					if (hookedPart == FOOT_LEFT) {
						angleKnee = ((RevoluteJoint) (character.joints.get(SHIN_LEFT - 1))).getJointAngle() * RAD_TO_DEG;
						//TODO modify this because not sure how angles actually work with knees.
						angleKnee = Math.abs(angleKnee%270);
						angleKneeModifier = angleKnee <= 90f ? angleKnee / 90f : 90f / angleKnee;
					} else {
						angleKnee = ((RevoluteJoint) (character.joints.get(SHIN_RIGHT - 1))).getJointAngle() * RAD_TO_DEG;
						//TODO modify this as necessary because not sure how angles actually work with knees.
						angleKnee = Math.abs(angleKnee%270);
						angleKneeModifier = angleKnee <= 90f ? angleKnee / 90f : 90f / angleKnee;
					}
					angleKneeModifier = Math.abs(angleKneeModifier);
					footToHip = Math.abs(hp.x - character.parts.get(HIPS).getPosition().x);

					distanceModifier = (Math.abs(MAX_LEG_DIST) - footToHip)/Math.abs(MAX_LEG_DIST);
					//TODO: should probably be a more complex modifier!
					totalModifier = (angleKneeModifier + distanceModifier) / 2f;
					forcey = totalModifier * MAX_PUSHFORCE_LEG;
				} else {
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
	return new Vector2(forcex,forcey);

}

	/**
	 * TODO play around with & modify this method
	 * @author Jacob
	 * method applies force in a "natural" and theoretically predictable way
	 * on the limb selected. Does need to be modified to find best "natural" balance of moving.
	 * whatever we decide natural should be
	 * @param limb - player's currently selected limb to apply force to
	 * @param force - force to apply
	 * @param wake - boolean to wake limb up (currently always passed in as true)
	 */
	private void applyForce(int limb,Vector2 force,boolean wake) {
//		character.parts.get(CHEST).body.applyForceToCenter(force.x,force.y,wake);
		switch(limb){
			case FOOT_LEFT:
				character.parts.get(THIGH_LEFT).body.applyForceToCenter(force.x,force.y,wake);
				character.parts.get(SHIN_LEFT).body.applyForceToCenter(force.x,force.y * (.5f),wake);
				character.parts.get(FOOT_LEFT).body.applyForceToCenter(force.x, force.y * .25f,wake);
				break;
			case FOOT_RIGHT:
				character.parts.get(THIGH_RIGHT).body.applyForceToCenter(force.x,force.y,wake);
				character.parts.get(SHIN_RIGHT).body.applyForceToCenter(force.x,force.y * (.5f),wake);
				character.parts.get(FOOT_RIGHT).body.applyForceToCenter(force.x, force.y * .25f,wake);
				break;
			case HAND_LEFT:
				character.parts.get(HAND_LEFT).body.applyForceToCenter(force.x,force.y,wake);
				character.parts.get(FOREARM_LEFT).body.applyForceToCenter(force.x,force.y,wake);
				character.parts.get(ARM_LEFT).body.applyForceToCenter(force.x, force.y * .25f,wake);
				break;
			case HAND_RIGHT:
				character.parts.get(HAND_RIGHT).body.applyForceToCenter(force.x,force.y,wake);
				character.parts.get(FOREARM_RIGHT).body.applyForceToCenter(force.x,force.y,wake);
				character.parts.get(ARM_RIGHT).body.applyForceToCenter(force.x, force.y * .25f,wake);
				break;
			default:
				//do nothing
				break;

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
	 * 
	 * @return change in energy value
	 */
	private float calculateEnergyChange(float gainModifier, float lossModifier, Vector2 force, boolean rotationGain){
		int b = rotationGain ? 1 : 0;
		float angle = character.parts.get(CHEST).getAngle();
		float exertion = Math.abs(force.y/600); //TODO: value needs adjusting based on new physics
		
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

		canvas.begin();
		canvas.draw(background, Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());
		canvas.end();

		canvas.begin();
		objects.get(2).draw(canvas);
		objects.get(1).draw(canvas);
		objects.get(0).draw(canvas);
		canvas.end();

//		//debug energy text in top left of screen
//		canvas.begin();
//		canvas.drawText(((Integer)(Math.round(character.getEnergy()))).toString(), font, 0f,
//				canvas.getCamera().position.y+canvas.height/2-50f);
//		canvas.end();

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
