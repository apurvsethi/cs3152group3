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



public class GameMode extends ModeController {

	/** Track asset loading from all instances and subclasses */
	protected AssetState assetState = AssetState.EMPTY;
	
	private static int timestep = 0;
	/** Strings for files used, string[] for parts, etc. */
	private static final String BACKGROUND_FILE = "background.png";
	private static final String FOREGROUND_FILE = "preliminaryCharacterFilmStrip.png";
	private static final String HANDHOLD_TEXTURES[] = {"handholds.png", "handholdsglow.png", "handholdsgrabbed.png"};
	private static final String PART_TEXTURES[] = {"Ragdoll/Corrected/Head.png","Ragdoll/Corrected/Torso.png","Ragdoll/Corrected/Hips.png",
				"Ragdoll/Corrected/ArmLeft.png", "Ragdoll/Corrected/ArmRight.png", "Ragdoll/Corrected/ForearmLeft.png","Ragdoll/Corrected/ForearmRight.png",
				"Ragdoll/Corrected/HandLeftUngripped.png","Ragdoll/Corrected/HandRightUngripped.png","Ragdoll/Corrected/ThighLeft.png",
				"Ragdoll/Corrected/ThighRight.png", "Ragdoll/Corrected/CalfLeft.png", "Ragdoll/Corrected/CalfRight.png", "Ragdoll/Corrected/FeetShoeLeft.png",
				"Ragdoll/Corrected/FeetShoeRight.png", "Ragdoll/Corrected/HandLeftGripped.png","Ragdoll/Corrected/HandRightGripped.png"};

	/**font for displaying debug values to screen */
	private static BitmapFont font = new BitmapFont();
	
	/** Texture asset for files used, parts, etc. */
	private static TextureRegion background;
	private static TextureRegion foreground;
	private static TextureRegion[] holdTextures = new TextureRegion[HANDHOLD_TEXTURES.length];
	private static TextureRegion[] partTextures = new TextureRegion[PART_TEXTURES.length];

	/**
	 * Preloads the assets for this controller.
	 *
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
	 *
	 * Opted for nonstatic loaders, but still want the assets themselves to be 
	 * static. So AssetState determines the current loading state, only load if
	 * assets are not already loaded.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (assetState != AssetState.LOADING) return;
		
		background = createTexture(manager, BACKGROUND_FILE, false);
		if (background == null) System.out.println("Wrong");
		foreground = createTexture(manager, FOREGROUND_FILE, false);
		for (int i = 0; i < HANDHOLD_TEXTURES.length; i++) {
			holdTextures[i] = createTexture(manager, HANDHOLD_TEXTURES[i], false);
		}
		for (int i = 0; i < PART_TEXTURES.length; i++) {
			partTextures[i] = createTexture(manager, PART_TEXTURES[i], false);
		}
		assetState = AssetState.COMPLETE;
	}

	/** Whether we have completed this level */
	private boolean complete;
	/** Whether we have failed at this level (and need a reset) */
	private boolean failed;
	
	/** Character of game */
	private CharacterModel character;
	/** A handhold */
	private HandholdModel handhold; 
	private int lastPressed = NONE;
	private int pressContinued = 0;
	private int nextToPress = NONE;
	public GameMode() {
		super(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GRAVITY);
		
		//create debug font
		font.setColor(Color.RED);
		font.getData().setScale(5);
	}

	@Override
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );
		
		for(GameObject obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		timestep = 0;
		world = new World(gravity,false);
		contactListener = new ListenerClass();
		world.setContactListener(contactListener);
		populateLevel();
	}
	
	public void populateLevel() {
		character = new CharacterModel(partTextures, world);
		objects.add(character);
		for(PartModel p : character.parts){
			objects.add(p);
		}
		// TODO: Populate level with whatever pieces and part are necessary (handholds, etc)
		// Will probably do through a level generator later, level model access
		for (int i = 0; i < HANDHOLD_NUMBER; i++){
//			handhold = new HandholdModel(holdTextures[0].getTexture(), holdTextures[1].getTexture(), 50, 50, 100*i+500, 20*i+500);

			handhold = new HandholdModel(holdTextures[0].getTexture(),holdTextures[1].getTexture(),holdTextures[2].getTexture(), 50, 50, 150*i+500, 500);
			handhold.activatePhysics(world);
			handhold.setBodyType(BodyDef.BodyType.StaticBody);
			handhold.geometry.setUserData("handhold");
			objects.add(handhold);
		}

//		for (int i: EXTREMITIES) {
//			PartModel i1 = character.parts.get(i);
//
//			handhold = new HandholdModel(holdTextures[0].getTexture(),holdTextures[1].getTexture(), 50, 50, i1.getX(), i1.getY());
//			handhold.activatePhysics(world);
//			handhold.setBodyType(BodyDef.BodyType.StaticBody);
//			handhold.geometry.setUserData("handhold");
//			objects.add(handhold);
//		}
	}
	
	public void update(float dt) {
		//System.out.println("UPDATE");

		InputController input = InputController.getInstance();
		snapLimbsToHandholds(input);
		glowHandholds();
//		if(input.getHorizontal()!=0){
//		character.parts.get(HEAD).setVX(-100f);
//		}
//		if(input.getVertical()!=0){
//			character.parts.get(HEAD).setVY(100f);
//		}
		
		if(input.getHorizontal()!=0){
			character.parts.get(HAND_LEFT).setVX(input.getHorizontal()*100f);
		}
		if(input.getVertical()!=0){
			character.parts.get(HAND_LEFT).setVY(input.getVertical()*100f);
		}
		
		pressContinued = 0;
//		float force = 0f;
		if (input.didLeftLeg()){
			System.out.println("LEFT LEG");
			if (lastPressed == NONE){
				lastPressed = FOOT_LEFT;
				pressContinued = 1;
			}
			else if (lastPressed != FOOT_LEFT){
				if (nextToPress == NONE){
					nextToPress = FOOT_LEFT;
				}
			}
			else{
				pressContinued = 1;
			}
		}
		if (input.didRightLeg()){
			System.out.println("RIGHT LEG");

			if (lastPressed == NONE){
				lastPressed = FOOT_RIGHT;
				pressContinued = 1;
			}
			else if (lastPressed != FOOT_RIGHT){
				if (nextToPress == NONE){
					nextToPress = FOOT_RIGHT;
				}
			}
			else{
				pressContinued = 1;
			}
		}
		if (input.didLeftArm()){
			System.out.println("LEFT ARM");

			if (lastPressed == NONE){
				lastPressed = HAND_LEFT;
				pressContinued = 1;
			}else if (lastPressed != HAND_LEFT){
				if (nextToPress == NONE){
					nextToPress = HAND_LEFT;
				}
			}
			else{
				pressContinued = 1;
			}
		}

		if (input.didRightArm()){
			System.out.println("RIGHT ARM");

			if (lastPressed == NONE){
				lastPressed = HAND_RIGHT;
				pressContinued = 1;
			}else if (lastPressed != HAND_RIGHT){
				if (nextToPress == NONE){
					nextToPress = HAND_RIGHT;
				}
			}
			else{
				pressContinued = 1;
			}
		}
		float y = input.getVertical();
		
		Vector2 force = new Vector2(0,0);
		if (pressContinued == 1){
			System.out.println("PRESS CONT");
			force.set(0,calculateForce(lastPressed,input));
			float threshold = 1f;
			//able to apply force if its greater than the threshold (minimum needed to have effect on the body)
			//wont apply dampening if its > 0
			System.out.println(force.y);
			if (force.y > threshold) {
				//can't have too high of a force!
				if (force.y > MAX_FORCE_THRESHOLD.y) force = MAX_FORCE_THRESHOLD;
				//apply the force to the body part.
				character.parts.get(lastPressed).setVY(force.scl(Math.signum(y)).y);
				character.parts.get(lastPressed).setVX(input.getHorizontal() * 100f);


			}
		}
//			force wasn't strong enough to move limb. apply dampening - force
		else{
			Vector2 vel = character.parts.get(HEAD).getLinearVelocity();
			System.out.println(vel.x + " " + vel.y);
			//if pos both
			float thisDampX = DAMPENING_X;
			float thisDampY = DAMPENING_Y;
			//applying dampening
			character.parts.get(HEAD).setVY(5f);
			character.parts.get(HEAD).setVX(5f);

			if (vel.y > 0) {
//					if (y > 0 && vel.y > 0) {
				if (vel.y - DAMPENING_Y < 0) thisDampY = vel.y;
				character.parts.get(HEAD).setVY(vel.y - thisDampY);
			}
			if (vel.x > 0) {
				if (vel.x - DAMPENING_X < 0) thisDampX = vel.x;
				character.parts.get(HEAD).setVX(vel.x - thisDampX);
				System.out.println("HERE NOW DAMP");

			}
				//if neg both
			if (vel.y < 0){
				if (vel.y + DAMPENING_Y > 0) thisDampY = -1 * vel.y;
				character.parts.get(HEAD).setVY(vel.y + thisDampY);
//					character.parts.get(lastPressed).body.applyForceToCenter(DAMPENING_Y.sub(force),true);
			}
			if (vel.x < 0) {
				if (vel.x + DAMPENING_X > 0) thisDampX = -1 * vel.x;
				character.parts.get(HEAD).setVX(vel.x + thisDampX);
			}
				//
			timestep+=1;

		}


//			character.parts.get(lastPressed).body.applyForceToCenter(0,force,false);


// else if (nextToPress != NONE) {
//			force = calculateForce(nextToPress,input);
//		}


		// TODO: Use inputController methods to select limbs, 
		//       horizontal and vertical to move them

		//move camera with character
		canvas.setCameraPosition(GAME_WIDTH/2,
				character.parts.get(CHEST).getBody().getPosition().y);
		
		// TODO: Movements of other objects (obstacles, eventually)
		
		// TODO: Interactions between limbs and handholds
		
		// TODO: Update energy quantity (fill in these values)
		float dEdt = calculateEnergyChange(1,1, force, true);
		character.setEnergy(character.getEnergy()+dEdt);
	}

	private void glowHandholds() {
		for (GameObject obj:objects) {
			if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
				HandholdModel h = (HandholdModel) obj;
				h.unglow();
//				go through extremities
				for (int e : EXTREMITIES) {
					for (Vector2 snapPoint : h.snapPoints) {
						if (closeEnough(e, snapPoint)) {
							h.glow();
							System.out.println("CLOSE ENOUGH");
							break;
						}
					}
				}
			}
		}
	}

	private void snapLimbsToHandholds(InputController input) {
		switch(lastPressed){
			case FOOT_LEFT:
				if (!input.didLeftLeg()){
					snapIfPossible(FOOT_LEFT);
				}
				break;
			case FOOT_RIGHT:
				if (!input.didRightLeg()){
					snapIfPossible(FOOT_RIGHT);
				}
				break;
			case HAND_LEFT:
				if (!input.didLeftArm()){
					snapIfPossible(HAND_LEFT);
				}
				break;
			case HAND_RIGHT:
				if (!input.didRightArm()){
					snapIfPossible(HAND_RIGHT);
				}
				break;
			case -1:
				if (timestep == 0){
					snapIfPossible(FOOT_LEFT);
					snapIfPossible(HAND_LEFT);
					snapIfPossible(HAND_RIGHT);
					snapIfPossible(FOOT_RIGHT);

				}
		}
	}

	private void snapIfPossible(int limb) {
		for (GameObject obj:objects){
			if (obj.getType() == GameObject.ObjectType.HANDHOLD){
				HandholdModel h = (HandholdModel)obj;
				for (Vector2 snapPoint:h.snapPoints){
					if (closeEnough(limb,snapPoint)){
						character.parts.get(limb).setPosition(snapPoint);
						((ExtremityModel)character.parts.get(limb)).grip();
						character.parts.get(limb).body.setType(BodyDef.BodyType.StaticBody);
						System.out.println("SNAPDADDY");
					}
				}

				}
			}
		}
/** helper function to check if limb is close enough to a snapPoint on a handhold
 * used for both snapping limbs to handholds and glowing handholds showing player they're close enough to snap */
	private boolean closeEnough(int limb, Vector2 snapPoint) {
		Vector2 dist = character.parts.get(limb).getPosition().sub(snapPoint);
		return (Math.sqrt(dist.x * dist.x + dist.y * dist.y)<= SNAP_RADIUS);
	}



	/**
	 * calculates Y force player can use
	 *
	 * */
	private float calculateForce(int currentLimb,InputController input) {
		ExtremityModel curPart = ((ExtremityModel)(character.parts.get(currentLimb)));
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

					System.out.println(distFromShoulder + " " + distFromYCenter);
					System.out.println(MAX_ARM_DIST + " " + MAX_LEG_DIST);

					float pullPercY = character.calcPullPercentageY(distFromYCenter,isArm);

					float pullPercX = character.calcPullPercentageX(distFromShoulder,isArm);
					System.out.println(pullPercX + " " + pullPercY);
					System.out.println(totalForce + " before Pull" );
					totalForce += pullPercX * pullPercY * e.getPull();
					System.out.println(totalForce + " After pull" );


				}else{
					Vector2 distance = e.getPosition().sub(character.parts.get(CHEST).getPosition());
					float distFromShoulder = Math.abs(distance.x - character.getPosition().x);
					float distFromYCenter = Math.abs(distance.y - character.getPosition().y);

					float pushPercY = character.calcPushPercentageY(distFromYCenter,isArm);

					float pushPercX = character.calcPushPercentageX(distFromShoulder,isArm);
					System.out.println(pushPercX + " " + pushPercY);
					System.out.println(totalForce + " before Push" );
					totalForce += pushPercX * pushPercY * e.getPush();
					System.out.println(totalForce + " After push" );

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
