package beigegang.mountsputnik;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;


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
	
	public GameMode() {
		super(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GRAVITY);
	}

	@Override
	public void reset() {
		populateLevel();
		
	}
	
	public void populateLevel() {
		character = new CharacterModel(partTextures, world);
		objects.add(character);
		// TODO: Populate level with whatever pieces and part are necessary (handholds, etc)
		// Will probably do through a level generator later, level model access
		for (int i = 0; i < HANDHOLD_NUMBER; i++){
			handhold = new HandholdModel(holdTextures[0].getTexture(), 100*i, 200*i);
			objects.add(handhold);
		}
	}
	
	public void update(float dt) {
		InputController input = InputController.getInstance();
		
		//debug code for testing if physics activated
		if(input.didLeftLeg()){
			character.parts.get(3).setVX(111f);
		}
		// TODO: Use inputController methods to select limbs, 
		//       horizontal and vertical to move them
		if(input.didLeftArm()){
			canvas.translateCamera(-1, 1);
		}
		if(input.didLeftLeg()){
			canvas.translateCamera(1, -1);
		}
		if(input.didRightArm()){
			canvas.translateCamera(1, 1);
		}
		if(input.didRightLeg()){
			canvas.translateCamera(-1, -1); 
		}
		
		// TODO: Movements of other objects (obstacles, eventually)
		
		// TODO: Interactions between limbs and handholds
		
		// TODO: Update energy quantity
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
