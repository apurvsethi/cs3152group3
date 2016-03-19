package beigegang.mountsputnik;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;


public class GameMode extends ModeController {

	/** Track asset loading from all instances and subclasses */
	protected AssetState assetState = AssetState.EMPTY;
	
	/** Strings for files used, string[] for parts, etc. */
	private static final String BACKGROUND_FILE = "background.png";
	private static final String FOREGROUND_FILE = "preliminaryCharacterFilmStrip.png";
	private static final String HANDHOLD_TEXTURES[] = {"handholds.png"};
	private static final String PART_TEXTURES[] = {"Ragdoll/Corrected/Head.png","Ragdoll/Corrected/Torso.png","Ragdoll/Corrected/Hips.png",
				"Ragdoll/Corrected/ArmLeft.png", "Ragdoll/Corrected/ArmRight.png", "Ragdoll/Corrected/ForearmLeft.png","Ragdoll/Corrected/ForearmRight.png",
				"Ragdoll/Corrected/HandLeftUngripped.png","Ragdoll/Corrected/HandRightUngripped.png","Ragdoll/Corrected/ThighLeft.png",
				"Ragdoll/Corrected/ThighRight.png", "Ragdoll/Corrected/CalfLeft.png", "Ragdoll/Corrected/CalfRight.png", "Ragdoll/Corrected/FeetShoeLeft.png",
				"Ragdoll/Corrected/FeetShoeRight.png"};

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
		font.setColor(Color.RED);
		font.getData().setScale(5);
	}

	@Override
	public void reset() {
		System.out.println("RESET");
		if(character!=null)
			character.deactivatePhysics(world);
		character = null;
		objects.clear();
		populateLevel();
		canvas.resetCamera();
		
	}
	
	public void populateLevel() {
		character = new CharacterModel(partTextures, world);
		objects.add(character);
		// TODO: Populate level with whatever pieces and part are necessary (handholds, etc)
		// Will probably do through a level generator later, level model access
		for (int i = 0; i < HANDHOLD_NUMBER; i++){
			handhold = new HandholdModel(holdTextures[0].getTexture(), 50, 50, 10*i+300, 20*i+300);
			handhold.activatePhysics(world);
			handhold.setBodyType(BodyDef.BodyType.StaticBody);
			objects.add(handhold);
		}
	}
	
	public void update(float dt) {
		//System.out.println("UPDATE");
		InputController input = InputController.getInstance();
		snapLimbsToHandholds(input);
		
		if(input.getHorizontal()!=0){
			character.parts.get(HEAD).body.setAngularVelocity(5*input.getHorizontal());
		}
		System.out.println(character.parts.get(HEAD).getAngle());
		
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
			if (force.y > threshold){
				//can't have too high of a force!
				if (force.y > MAX_FORCE_THRESHOLD.y) force = MAX_FORCE_THRESHOLD;
				//apply the force to the body part.
				character.parts.get(lastPressed).body.applyForceToCenter(force.scl(Math.signum(y)),true);

			}
//			force wasn't strong enough to move limb. apply dampening - force
			else{
				Vector2 vel = character.parts.get(lastPressed).getLinearVelocity();
				//if pos both
				if (y > 0 && vel.y > 0) {
					character.parts.get(lastPressed).body.applyForceToCenter(DAMPENING_Y.scl(-1).sub(force.scl(-1)) ,true);
				}
				//if neg both
				else if (y < 0 && vel.y < 0){
					character.parts.get(lastPressed).body.applyForceToCenter(DAMPENING_Y.sub(force),true);
				}
				//
				else{

				}
			}


//			character.parts.get(lastPressed).body.applyForceToCenter(0,force,false);

		}
// else if (nextToPress != NONE) {
//			force = calculateForce(nextToPress,input);
//		}
		else{
			//no movement required.
		}

		// TODO: Use inputController methods to select limbs, 
		//       horizontal and vertical to move them

//		if(input.didLeftArm()){
//			character.parts.get(ARM_LEFT).body.applyForceToCenter(100f,0,false);
//		}
//		character.parts.get()
		System.out.println("");
		//move camera with character
		canvas.translateCamera(0, character.parts.get(CHEST).getBody().getLinearVelocity().y * 18f/GAME_HEIGHT);
		
		// TODO: Movements of other objects (obstacles, eventually)
		
		// TODO: Interactions between limbs and handholds
		
		// TODO: Update energy quantity (fill in these values)
		float dEdt = calculateEnergyChange(1,1, force, true);
		character.setEnergy(character.getEnergy()+dEdt);
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
		}
	}

	private void snapIfPossible(int footLeft) {
//		for (GameObject)
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
			if (e.isGripping()){
				isLeft = ex == HAND_LEFT || ex == FOOT_LEFT;
				isArm = ex == HAND_LEFT || ex == HAND_RIGHT;
				pull =  e.getY() > curY;
				eFactor = pull ? e.getPush():e.getPull();

				if (pull){

					Vector2 distance = e.getPosition().sub(character.parts.get(CHEST).getPosition());
					//calculate distance from shoulder for width (closer the 0 the better)
					//
					//what side it's on.
					float shoulderOffset = isLeft? -1 * CHEST_X_ARM_OFFSET: CHEST_X_ARM_OFFSET;
					float distFromShoulder = Math.abs(distance.x - (character.getPosition().x + shoulderOffset));
					float distFromYCenter = Math.abs(distance.y - character.getPosition().y);


					float pullPercY = character.calcPullPercentageY(distFromYCenter,isArm);

					float pullPercX = character.calcPullPercentageX(distFromShoulder,isArm);
					totalForce += pullPercX * pullPercY * e.getPull();




				}else{
					Vector2 distance = e.getPosition().sub(character.parts.get(CHEST).getPosition());
					float distFromShoulder = Math.abs(distance.x - character.getPosition().x);
					float distFromYCenter = Math.abs(distance.y - character.getPosition().y);

					float pushPercY = character.calcPushPercentageY(distFromYCenter,isArm);

					float pushPercX = character.calcPushPercentageX(distFromShoulder,isArm);
					totalForce += pushPercX * pushPercY * e.getPush();

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
		canvas.drawText(((Float)character.getEnergy()).toString(), font, 0f, GAME_HEIGHT-50f);
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
