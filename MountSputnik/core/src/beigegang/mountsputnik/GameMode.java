package beigegang.mountsputnik;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;
import com.badlogic.gdx.math.Vector2;


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
			objects.add(handhold);
		}
	}
	
	public void update(float dt) {
		InputController input = InputController.getInstance();
		pressContinued = 0;
		if (input.didLeftLeg()){
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
		if (pressContinued == 1){
			calculateForce(lastPressed,input);
		}else if (nextToPress != NONE) {
			calculateForce(nextToPress,input);
		}
		else{
			//no movement required.
		}

		// TODO: Use inputController methods to select limbs, 
		//       horizontal and vertical to move them

		if(input.didLeftArm()){
			character.parts.get(ARM_LEFT).body.applyForceToCenter(100f,0,false);
		}
		System.out.println("");
		//move camera with character
		canvas.translateCamera(0, character.parts.get(CHEST).getBody().getLinearVelocity().y * 18f/GAME_HEIGHT);
		
		// TODO: Movements of other objects (obstacles, eventually)
		
		// TODO: Interactions between limbs and handholds
		
		// TODO: Update energy quantity (fill in these values)
		float dEdt = calculateEnergyChange(0,0, true);
		character.setEnergy(character.getEnergy()+dEdt);
	}

	private void calculateForce(int currentLimb,InputController input) {
	/*	ExtremityModel curPart = ((ExtremityModel)(character.parts.get(currentLimb)));
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

				//check distance from center of mass for Y - its own function
				//check distance from for X -



		for (int ex: EXTREMITIES){
			ExtremityModel e = (ExtremityModel)(character.parts.get(ex));
			float eFactor = 0;
			boolean pull = false;
			boolean isLeft = false;
			if (e.isGripping()){
				isLeft = ex == HAND_LEFT || ex == FOOT_LEFT;
				pull =  e.getY() > curY;
				eFactor = pull ? e.getPush():e.getPull();
			}
			if (pull){
				Vector2 distance = e.getPosition().sub(character.parts.get(CHEST).getPosition());
				//calculate distance from shoulder for width (closer the 0 the better)
				//
				//what side it's on.
				float distFromShoulder = distance.x - character.getPosition().x;

			}else{
				Vector2 distance = e.getPosition().sub(character.parts.get(CHEST).getPosition());
				float distFromShoulder = distance.x - character.getPosition().x;

			}
		}
		double tan1 = Math.atan(y/x);

*/
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
	 * 
	 * @return change in energy value
	 */
	private float calculateEnergyChange(float gainModifier, float lossModifier, boolean rotationGain){
		//TODO: FILL IN THIS FUNCTION
		int b = rotationGain ? 1 : 0;
		return 0;
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
}
