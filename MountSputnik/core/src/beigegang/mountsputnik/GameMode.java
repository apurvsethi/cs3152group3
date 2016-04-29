package beigegang.mountsputnik;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;

import beigegang.util.*;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.*;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class GameMode extends ModeController {

	private int currLevel = LEVEL_TUTORIAL;
	private boolean flashing = false;
	private int flashing2 = 10;
	/** A string representing the name of the current level */
	private String levelName = "canyon"; 
	
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
	//We need to preload every single texture, regardless of which level we're currently using. Loading can't be
	//dynamically
	private static final String LEVEL_NAMES[] = {"tutorial", "canyon", "canyon", "canyon", "canyon", "sky", "canyon"};//,"mountain","sky","space"}; <-- Add the rest of these in as they are given assets
	private static final String LAVA_FILE = "assets/testlavatexture.png"; //TODO: make this a better texture
	private static final String UI_FILE = "assets/HUD4.png";
	private static final String[] LEVEL_LABEL_FILES = {"assets/Tutorial.png", "assets/Canyon.png", "assets/Canyon.png", "assets/Canyon.png", "assets/Canyon.png", "assets/Skycloud.png", "assets/Canyon.png"};
	private static final String LOGO_FILE = "Menu/StartMenu/Logo Only.png";
	private static final String GLOW_FILE = "assets/glow.png"; 
	private static final HashMap<String,Integer> NUM_HANDHOLDS = new HashMap<String,Integer>();  
	//private static final String HANDHOLD_TEXTURES[] = {"assets/canyon/Handhold1.png", "assets/canyon/Handhold2.png"};
	private static final String PART_TEXTURES[] = {"Ragdoll/Torso.png", "Ragdoll/Head.png", "Ragdoll/Hips.png",
			"Ragdoll/ArmLeft.png", "Ragdoll/ArmRight.png", "Ragdoll/ForearmLeft.png", "Ragdoll/ForearmRight.png",
			"Ragdoll/HandLeftUngripped.png", "Ragdoll/HandRightUngripped.png", "Ragdoll/ThighLeft.png",
			"Ragdoll/ThighRight.png", "Ragdoll/CalfLeft.png", "Ragdoll/CalfRight.png", "Ragdoll/FeetShoeLeft.png",
			"Ragdoll/FeetShoeRight.png", "Ragdoll/HandLeftGripped.png", "Ragdoll/HandRightGripped.png"};
	private static final String TUTORIAL_TEXTURES[] = {
			"Ragdoll/controls/360_LB.png", 
			"Ragdoll/controls/360_RB.png", 
			"Ragdoll/controls/360_LT.png", 
			"Ragdoll/controls/360_RT.png", 
			"Ragdoll/controls/360_LB_selected.png", 
			"Ragdoll/controls/360_RB_selected.png", 
			"Ragdoll/controls/360_LT_selected.png", 
			"Ragdoll/controls/360_RT_selected.png"
	};
	private static final String TUTORIAL_OVERLAY_TEXTURE = "assets/tutorial/TutorialOverlay.png";
	private Random rand = new Random();
	private float cposYAtTime0 = 0;

	//	private static TextureRegion[] energyTextures;
	//first element is an empty drawing
	private static final String ENERGY_TEXTURES[] = new String[11];
	private static final String PROGRESS_TEXTURES[] = new String[6];

//	private static final String ENERGY_TEXTURES2[] = new String[]
//			{"Energy/e1.png","Energy/e2.png","Energy/e3.png","Energy/e4.png","Energy/e5.png","Energy/e6.png","Energy/e7.png","Energy/e8.png","Energy/e9.png","Energy/e10.png"};
	private static TextureRegion[] energyTextures =  new TextureRegion[ENERGY_TEXTURES.length];
	private static TextureRegion[] progressTextures =  new TextureRegion[PROGRESS_TEXTURES.length];
	/**
	 * font for displaying debug values to screen
	 */
	private static BitmapFont font = new BitmapFont();
	private static boolean upsideDown = false;
	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion midground;
	private static TextureRegion tile;
	private static TextureRegion UI;
	private static TextureRegion CANYON;
	private static TextureRegion LOGO;
	private static TextureRegion edge;
	private static TextureRegion ground;
	private static TextureRegion lavaTexture;
	private static TextureRegion glowTexture; 
	private static TextureRegion staticObstacle; 
	private static TextureRegion fallingObstacle; 
	private static TextureRegion[] partTextures = new TextureRegion[PART_TEXTURES.length];
	private static TextureRegion[] tutorialTextures = new TextureRegion[TUTORIAL_TEXTURES.length];
	private static TextureRegion[] handholdTextures;
	private static TextureRegion[] levelLabels = new TextureRegion[LEVEL_LABEL_FILES.length];
	private static TextureRegion tutorialOverlay;
	
	private static TextureRegion blackoutTexture;
	private static String BLACKOUT = "assets/blackout.png";
	private static String FATIGUE_BAR = "Energy/Fatigue Gauge.png";
	private static TextureRegion fatigueTexture;
	private static String WARNING_TEXTURE = "assets/Falling Rock Warning.png";
	private static TextureRegion warningTexture;
	private static String PROGRESS_BACKGROUND= "assets/Progress Bar.png";
	private static TextureRegion progressBackgroundTexture;
	private static String LOW_ENERGY_HALO= "assets/redhalo.png";
	private static TextureRegion lowEnergyHalo;
	private static String PROGRESS_BAR= "Progress Chalk Bar.png";
	private static TextureRegion progressBarTexture;
	private Sprite progressSprite = new Sprite(new Texture(PROGRESS_BAR));
	private Sprite warningSprite = new Sprite(new Texture(WARNING_TEXTURE));
	private Sprite lowEnergySprite = new Sprite(new Texture(LOW_ENERGY_HALO));
	private static SpriteBatch batch = new SpriteBatch();
	private static int progressLevel = 0;
	/** The reader to process JSON files */
	private JsonReader jsonReader;
	/** The JSON defining the level model */
	private JsonValue levelFormat;
	/** The level's oxygen concentration (environmental energy gain modifier"*/
	private float oxygen;
	/** which energy bar to display, 1-10 */
	private int energyLevel;
	/** not sure if needed yet - could be for displaying low energy warning*/
	private int energyTimer;
	/** AssetManager for loading textures for Handholds*/
	private AssetManager assetManager;
	private float maxLevelHeight;
	// ************************************START CONTENT LOADING*********************************************** //

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
		assetManager = manager;
		if (assetState != AssetState.EMPTY) return;
		
		assetState = AssetState.LOADING;
		
		for(String name : LEVEL_NAMES){
			manager.load("assets/"+name+"/background.png", Texture.class);
			assets.add("assets/"+name+"/background.png");
			manager.load("assets/"+name+"/Midground.png", Texture.class);
			assets.add("assets/"+name+"/Midground.png");
			manager.load("assets/"+name+"/Surface.png", Texture.class);
			assets.add("assets/"+name+"/Surface.png");
			manager.load("assets/"+name+"/SurfaceEdge.png", Texture.class);
			assets.add("assets/"+name+"/SurfaceEdge.png");
			manager.load("assets/"+name+"/LevelStart.png", Texture.class);
			assets.add("assets/"+name+"/LevelStart.png");
			manager.load("assets/"+name+"/StaticObstacle.png", Texture.class);
			assets.add("assets/"+name+"/StaticObstacle.png");
			manager.load("assets/"+name+"/FallingRock.png", Texture.class);
			assets.add("assets/"+name+"/FallingRock.png");
		

		}
		for (String name:LEVEL_LABEL_FILES){
			manager.load(name,Texture.class);
			assets.add(name);
		}
		for (int i = 0; i<ENERGY_TEXTURES.length; i++){
			String name = "Energy/e" + i + ".png";
			manager.load(name, Texture.class);
			assets.add(name);
			ENERGY_TEXTURES[i] = name;
		}
		for (int i = 1; i<=PROGRESS_TEXTURES.length; i++){
			String name = "Progress/p" + i + ".png";
			manager.load(name, Texture.class);
			assets.add(name);
			PROGRESS_TEXTURES[i-1] = name;
		}
		manager.load(UI_FILE, Texture.class);
		assets.add(UI_FILE);

		manager.load(LOGO_FILE, Texture.class);
		assets.add(LOGO_FILE);
		manager.load(LAVA_FILE, Texture.class);
		assets.add(LAVA_FILE);
		manager.load(GLOW_FILE, Texture.class);
		assets.add(GLOW_FILE);

		for (Entry<String, Integer> entry : NUM_HANDHOLDS.entrySet()){
			for(int i = 1; i <= entry.getValue(); i++){
				manager.load("assets/"+entry.getKey()+"/Handhold"+i+".png",Texture.class);
				assets.add("assets/"+entry.getKey()+"/Handhold"+i+".png");
			}
		}
		for (String PART_TEXTURE : PART_TEXTURES) {
			manager.load(PART_TEXTURE, Texture.class);
			assets.add(PART_TEXTURE);
		}
		for(String TUTORIAL_TEXTURE : TUTORIAL_TEXTURES){
			manager.load(TUTORIAL_TEXTURE, Texture.class);
			assets.add(TUTORIAL_TEXTURE);
		}
		manager.load(BLACKOUT,Texture.class);
		assets.add(BLACKOUT);
		manager.load(FATIGUE_BAR,Texture.class);
		assets.add(FATIGUE_BAR);
		manager.load(PROGRESS_BACKGROUND,Texture.class);
		assets.add(PROGRESS_BACKGROUND);
		manager.load(PROGRESS_BAR,Texture.class);
		assets.add(PROGRESS_BAR);
		manager.load(WARNING_TEXTURE,Texture.class);
		assets.add(WARNING_TEXTURE);
		manager.load(LOW_ENERGY_HALO,Texture.class);
		assets.add(LOW_ENERGY_HALO);
		manager.load(TUTORIAL_OVERLAY_TEXTURE, Texture.class); 
		assets.add(TUTORIAL_OVERLAY_TEXTURE);
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

		background = createTexture(manager, "assets/"+levelName+"/background.png", false);
		midground = createTexture(manager, "assets/"+levelName+"/Midground.png", false);
		tile = createTexture(manager, "assets/"+levelName+"/Surface.png", false);
		UI = createTexture(manager, UI_FILE, false);
		LOGO = createTexture(manager, LOGO_FILE, false);
		edge = createTexture(manager, "assets/"+levelName+"/SurfaceEdge.png", false);
		ground = createTexture(manager, "assets/"+levelName+"/LevelStart.png", false);
		lavaTexture = createTexture(manager, LAVA_FILE, false);
		glowTexture = createTexture(manager, GLOW_FILE, false); 
		staticObstacle = createTexture(manager, "assets/"+levelName+"/StaticObstacle.png", false); 
		fallingObstacle = createTexture(manager, "assets/"+levelName+"/FallingRock.png", false); 

		for (int i = 0;  i < LEVEL_LABEL_FILES.length; i++){
			levelLabels[i] = createTexture(manager, LEVEL_LABEL_FILES[i], false);

		}

		for (int i = 0; i < PART_TEXTURES.length; i++) {
			partTextures[i] = createTexture(manager, PART_TEXTURES[i], false);
		}
		
		for (int i = 0; i < TUTORIAL_TEXTURES.length; i++) {
			tutorialTextures[i] = createTexture(manager, TUTORIAL_TEXTURES[i], false);
		}
		
		handholdTextures = new TextureRegion[NUM_HANDHOLDS.get(levelName)];
		for (int i = 1; i <= NUM_HANDHOLDS.get(levelName); i++) {
			handholdTextures[i-1] = createTexture(manager, "assets/"+levelName+"/Handhold"+i+".png", false);
		}

		for (int i = 0; i < ENERGY_TEXTURES.length; i++) {
			energyTextures[i] = createTexture(manager, ENERGY_TEXTURES[i], false);
		}
		for (int i = 0; i < PROGRESS_TEXTURES.length; i++) {
			progressTextures[i] = createTexture(manager, PROGRESS_TEXTURES[i], false);
		}
		blackoutTexture = createTexture(manager,BLACKOUT,false);
		fatigueTexture = createTexture(manager,FATIGUE_BAR,false);
		progressBackgroundTexture = createTexture(manager,PROGRESS_BACKGROUND,false);
		progressBarTexture = createTexture(manager,PROGRESS_BAR,false);
		lowEnergyHalo = createTexture(manager,LOW_ENERGY_HALO,false);
		assetState = AssetState.COMPLETE;
		tutorialOverlay = createTexture(manager, TUTORIAL_OVERLAY_TEXTURE, false); 

	}

	/** 
	 * Unloads the assets for this game.
	 * 
	 * This method erases the static variables.  It also deletes the associated textures 
	 * from the asset manager. If no assets are loaded, this method does nothing.
	 */
	public void unloadContent() {
		JsonAssetManager.getInstance().unloadDirectory();
		JsonAssetManager.clearInstance();
	}
	// ************************************END CONTENT LOADING*********************************************** //

	public void changeLevel(int level){
		listener.exitScreen(this, EXIT_GAME_NEXT_LEVEL);
	}
	/** private class for obstacleWarnings
	 * @author Jacob
	 * */
	private class warningsClass{
		float center;
		float maxHeight;
		ObstacleModel o;
		public warningsClass(float center, float maxHeightToDisplay,ObstacleModel ob){
			this.center = center;
			this.maxHeight = Math.min (maxLevelHeight,maxHeightToDisplay);
			o=ob;
		}
	}

	private ObstacleZone obstacleZone;
	private RisingObstacle risingObstacle = null;
	private ObstacleModel obstacle;
	private Array<ObstacleZone> queuedObstacles = new Array<ObstacleZone>();
	/** all obstacle zones in level */
	private Array<ObstacleZone> obstacles = new Array<ObstacleZone>();
	/** Current obstacle warnings to display */
	private Array<warningsClass> obstacleWarnings = new Array<warningsClass>();
	/**
	 * Whether we have completed this level
	 */
	private boolean complete = false; 
	private boolean failed;
	private float maxHandhold = 0f;

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
	private Array<Integer> justReleased = new Array<Integer>();
	/**
	 * holds any extremities who's buttons are pressed during this timestep. keeps order of pressing intact
	 */
	private Array<Integer> nextToPress = new Array<Integer>();
	/**
	 * A list of all the blocks that were chosen for this generated level. Allows for debugging 
	 */
	private Array<String> levelBlocks = new Array<String>(); 
	
	/** A boolean indicating the toggle of the tutorial view, where limbs have their corresponding buttons shown*/ 
	private boolean tutorialToggle = false; 

	private Array<Float> checkpoints = new Array();



	public GameMode() {
		super(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GRAVITY);

		//create debug font
		font.setColor(Color.RED);
		font.getData().setScale(5);

		NUM_HANDHOLDS.put("canyon", 1); 
		NUM_HANDHOLDS.put("tutorial",4); 
		NUM_HANDHOLDS.put("sky", 1); 

	}

	@Override
	public void reset() {
		levelName = LEVEL_NAMES[currLevel];
		complete = false;
		failed = false;
		assetState = AssetState.LOADING;
		loadContent(assetManager);
		
		for (GameObject obj : objects) {
			obj.deactivatePhysics(world);
		}
		risingObstacle = null;
		objects.clear();
		obstacles.clear();
		obstacleWarnings.clear();
		addQueue.clear();
		world.dispose();
		timestep = 0;
		populateLevel();
		progressSprite.setBounds(0,0,canvas.getWidth()/4,canvas.getHeight());
		lowEnergySprite.setBounds(0, 0, canvas.getWidth() / 4, canvas.getHeight());
		queuedObstacles.clear();

	}
	// ************************************START LEVELS*********************************************** //

	/**
	 * Creates the character, and then generates the level according to specified environment. 
	 * Currently, all level assets should be stored in the appropriate location according to this 
	 * path within the assets folder: the general level description will be in "Levels/[levelName]/level.json" 
	 * and the individual blocks will be in "Levels/[levelName]/block[x].json" where x is a whole number.
	 * 
	 * The general level description contains such things as the physics constants like gravity or oxygen
	 * the desired height of the level, the total number of different blocks usable in generation, and 
	 * descriptors of certain level-wide obstacles like rising lava, if they exist. 
	 * 
	 * The blocks will describe individual building blocks. They will contain a height in meters, to be 
	 * determined by their size when created by the level editor, and a difficulty rating. They will also contain two 
	 * important objects:
	 * 
	 * 1) Handholds will be a list of handhold objects, which in turn will contain location, friction, restitution,  
	 * crumble and size values, as well as a texture.
	 * 
	 * 2) Obstacles will be a list of locations in which falling obstacles like rocks or meteors will be described. Each obstacle
	 * object will include a specific region in which falling obstacles can spawn (they will only spawn when they are offscreen to 
	 * the player
	 * 
	 * Levels are generated by adding randomly selected building blocks on top of the previous level generated until the desired 
	 * height of the level (in units) is reached.
	 * 
	 * @author Daniel
	 * 
//	 *
	 */
//	@param levelName: the level to be generated
	public void populateLevel() {
		jsonReader = new JsonReader();
		levelFormat = jsonReader.parse(Gdx.files.internal("Levels/"+levelName+"/level.json"));
		JsonAssetManager.getInstance().loadDirectory(levelFormat);
		JsonAssetManager.getInstance().allocateDirectory();
		Vector2 gravity = new Vector2(0,levelFormat.getFloat("gravity"));
		oxygen = levelFormat.getFloat("oxygen");
		float remainingHeight = levelFormat.getFloat("height");
		float currentHeight=0f;
		int diffBlocks = levelFormat.getInt("uniqueBlocks");
		int filler = levelFormat.getInt("generalFillerSize");
		int fillerSize = levelFormat.getInt("fillerBlocks");

		world = new World(gravity, false);
		contactListener = new ListenerClass();
		world.setContactListener(contactListener);

		levelBlocks.clear();
		int counter = 0;
		Array<Integer> used = new Array<Integer>();
		maxHandhold = remainingHeight;
		maxLevelHeight = remainingHeight;
		checkpoints.add(0f);
		while(currentHeight < remainingHeight){
			//TODO: account for difficulty
			int blockNumber = ((int) (Math.random() * diffBlocks)) + 1;
			while(used.contains(blockNumber, true)&&!levelName.equals("tutorial"))
				blockNumber = ((int) (Math.random() * diffBlocks)) + 1;
			used.add(blockNumber);
			levelBlocks.add("Levels/"+levelName+"/block"+blockNumber+".json"); 
			JsonValue levelPiece = jsonReader.parse(Gdx.files.internal("Levels/"+levelName+"/block"+blockNumber+".json"));
			
			addChunk(levelPiece, currentHeight, levelName);
			currentHeight += levelPiece.getFloat("size");
			for(int i = 0; i < filler; i++){
				blockNumber = ((int) (Math.random() * fillerSize)) + 1;
				levelPiece = jsonReader.parse(Gdx.files.internal("Levels/general/block"+blockNumber+".json"));
				levelBlocks.add("Levels/general/block"+blockNumber+".json"); 
				addChunk(levelPiece, currentHeight, levelName);
				currentHeight += levelPiece.getInt("size");
			}
		}
		System.out.println(levelBlocks);

		JsonValue lava = levelFormat.get("lava");
		if(lava.getBoolean("present")){
			risingObstacle = new RisingObstacle(lavaTexture, lava.getFloat("speed"));
		}

		character = new CharacterModel(partTextures, world, DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 2, scale);
			//arms
		objects.add(character.parts.get(ARM_LEFT));
		objects.add(character.parts.get(ARM_RIGHT));
		objects.add(character.parts.get(FOREARM_LEFT));
		objects.add(character.parts.get(FOREARM_RIGHT));
		objects.add(character.parts.get(HAND_LEFT));
		objects.add(character.parts.get(HAND_RIGHT));
		//legs
		objects.add(character.parts.get(THIGH_LEFT));
		objects.add(character.parts.get(THIGH_RIGHT));
		objects.add(character.parts.get(SHIN_LEFT));
		objects.add(character.parts.get(SHIN_RIGHT));
		objects.add(character.parts.get(FOOT_LEFT));
		objects.add(character.parts.get(FOOT_RIGHT));
		//rest
		objects.add(character.parts.get(HEAD));
		objects.add(character.parts.get(HIPS));
		objects.add(character.parts.get(CHEST));

		Movement.setCharacter(character);
		
		handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(),glowTexture.getTexture(), 
				character.parts.get(HAND_LEFT).getPosition().x, character.parts.get(HAND_LEFT).getPosition().y,
				new Vector2(.3f, .3f), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);
		handhold.geometry.setUserData(handhold);
		handhold.geometry.setRestitution(1);
		handhold.geometry.setFriction(1);
		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		objects.add(handhold);
		
		handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(),glowTexture.getTexture(), 
				character.parts.get(HAND_RIGHT).getPosition().x, character.parts.get(HAND_RIGHT).getPosition().y,
				new Vector2(.3f, .3f), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);
		handhold.geometry.setUserData(handhold);
		handhold.geometry.setRestitution(1);
		handhold.geometry.setFriction(1);
		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		objects.add(handhold);
		
		handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(),glowTexture.getTexture(), 
				character.parts.get(FOOT_LEFT).getPosition().x, character.parts.get(FOOT_LEFT).getPosition().y,
				new Vector2(.3f, .3f), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);
		handhold.geometry.setUserData(handhold);
		handhold.geometry.setRestitution(1);
		handhold.geometry.setFriction(1);
		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		objects.add(handhold);
		
		handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(), glowTexture.getTexture(), 
				character.parts.get(FOOT_RIGHT).getPosition().x, character.parts.get(FOOT_RIGHT).getPosition().y,
				new Vector2(.3f, .3f), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);
		handhold.geometry.setUserData(handhold);
		handhold.geometry.setRestitution(1);
		handhold.geometry.setFriction(1);
		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		objects.add(handhold);
	}
	
	/** 
	 * Adds blocks to the level based on JSON block description
	 * 
	 * @param levelPiece: The block description
	 * @param currentHeight: y offset from the bottom of the screen
	 * @param levelName: the name of the level
	 * @author Daniel
	 */
	private void addChunk(JsonValue levelPiece, float currentHeight, String levelName){
		
		JsonValue handholdDesc = levelPiece.get("handholds").child();

		Random rand = new Random();
		while(handholdDesc != null){
			handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(), glowTexture.getTexture(), 
					handholdDesc.getFloat("positionX"), handholdDesc.getFloat("positionY")+currentHeight,
					new Vector2(handholdDesc.getFloat("width"), handholdDesc.getFloat("height")), scale);
			handhold.fixtureDef.filter.maskBits = 0;
			handhold.activatePhysics(world);
			handhold.geometry.setUserData(handhold);
			handhold.geometry.setRestitution(handholdDesc.getFloat("restitution"));
			handhold.geometry.setFriction(handholdDesc.getFloat("friction"));
			try{
				handhold.setBodyType(BodyDef.BodyType.KinematicBody);
				JsonValue movement = handholdDesc.get("movement");
				handhold.setStartPoint(movement.getFloat("startX"),movement.getFloat("startY")+currentHeight);
				handhold.setEndPoint(movement.getFloat("endX"),movement.getFloat("endY")+currentHeight);
				float speed = movement.getFloat("speed"), 
					tx = handhold.getEndPoint().x - handhold.getStartPoint().x,
					ty = handhold.getEndPoint().y - handhold.getStartPoint().y,
					dist = (float) Math.sqrt(tx*tx+ty*ty);
				handhold.setLinearVelocity(new Vector2((tx/dist)*speed, (ty/dist)*speed));
				handhold.setPosition((handhold.getStartPoint().x+handhold.getEndPoint().x)/2,
						(handhold.getStartPoint().y+handhold.getEndPoint().y)/2);
			}
			catch(Exception e){handhold.setBodyType(BodyDef.BodyType.StaticBody);}
			objects.add(handhold);

			try{handhold.setCrumble(handholdDesc.getFloat("crumble"));}
			catch(Exception e){handhold.setCrumble(0);}
			try{handhold.setSlip(handholdDesc.getFloat("slip"));}
			catch(Exception e){handhold.setSlip(0);}
			
			handholdDesc = handholdDesc.next();
		}

		JsonValue obstacleDesc;
		try{
			obstacleDesc = levelPiece.get("obstacles").child();}
		catch(Exception e){return;}
		Rectangle bound;
		while(obstacleDesc != null){
			bound = new Rectangle(obstacleDesc.getFloat("originX"), obstacleDesc.getFloat("originY")+currentHeight,
					obstacleDesc.getFloat("width"),obstacleDesc.getFloat("height"));
			//TODO: Set texture to something other than null once we have textures for obstacles
			obstacleZone = new ObstacleZone(fallingObstacle.getTexture(), null, currentHeight, obstacleDesc.getInt("frequency"), bound);
			obstacles.add(obstacleZone);
			obstacleDesc = obstacleDesc.next();
		}
		


		JsonValue staticDesc;
		try{
			staticDesc = levelPiece.get("static").child();
			ObstacleModel obstacle;
			while(staticDesc != null){
				//TODO: Set texture to something other than null once we have textures for obstacles

				obstacle = new ObstacleModel(staticObstacle.getTexture(), staticDesc.getFloat("size"), scale);
				obstacle.setX(staticDesc.getFloat("x"));
				obstacle.setY(staticDesc.getFloat("y")+currentHeight);

				obstacle.setBodyType(BodyType.StaticBody);
				obstacle.activatePhysics(world);
				objects.add(obstacle);
				staticDesc = staticDesc.next();
			}
		}
		catch(Exception e){}
	}

	/*private void makeTestLevel() {
		obstacleZone = new ObstacleZone(handholdTextures[0].getTexture(), warningTexture, 0, 200, new Rectangle(9,30,2,5));
		obstacles.add(obstacleZone);
	}*/
	// ************************************END LEVELS*********************************************** //


	/**
	 * This method computes an order for the selected limbs based on previous timesteps and the first limb in nextToPress
	 *           is the limb that can be controlled.
	 *           this method ungrips all selected limbs and then calculates the force that can be imparted on the main selected
	 *           limb based on the forces the other limbs can impart with plenty of heuristics
	 *           if no force is imparted, it uses dampening on all of the limbs that are not gripping a handhold.
	 *           it then snaps limbs to a viable handhold within HANDHOLD_SNAP_RADIUS that were just released this timestep
	 *           <p/>
	 *           special case: on the zeroth timestep/very first call to update at start of game,
	 *           it snaps limbs to any handhold in radius.
	 * 
	 * @param dt
	 * @author Jacob, Daniel
	 */
	public void update(float dt) {
		InputController input = InputController.getInstance();
		inx = input.getHorizontalL();
		iny = input.getVerticalL();


		upsideDown = character.parts.get(HEAD).getPosition().y - character.parts.get(CHEST).getPosition().y <= 0;

		nextToPress = input.getButtonsPressed();
		justReleased = input.getJustReleased();
		if(input.didSelect()) tutorialToggle = !tutorialToggle;

		Movement.makeHookedJointsMovable(nextToPress);

		if (input.didMenu()) listener.exitScreen(this, EXIT_PAUSE);

		Movement.resetLimbSpeedsTo0();
		if (nextToPress.size > 0) {
			for (int i : nextToPress) {
				((ExtremityModel) (character.parts.get(i))).ungrip();
				ungrip(((ExtremityModel) (character.parts.get(i)))); 
			}
			float v = input.getVerticalL();
			float h = input.getHorizontalL();
			Movement.findAndApplyForces(nextToPress.get(0),v,h);


		}
		Movement.applyTorsoForceIfApplicable();
		//bounding velocities
		boundBodyVelocities();
		HandholdModel[] glowingHandholds = glowHandholds();

		if (justReleased.size > 0 || timestep == 0) {
			snapLimbsToHandholds(input,glowingHandholds);
		}
		

		cameraWork();
		
		dealWithSlipperyAndCrumblyHandholds();
		
		spawnObstacles();

		for(GameObject g : objects){

			if(g instanceof ObstacleModel &&
					g.getBody().getPosition().y  < (canvas.getCamera().position.y-canvas.getWidth())/scale.y &&
					g.getBody().getType() != BodyDef.BodyType.StaticBody){
				objects.remove(g);
			}
			if(g instanceof HandholdModel && ((HandholdModel) (g)).getStartPoint() != null){
				HandholdModel h = (HandholdModel) g;
				h.updateSnapPoints();
				if(withinBounds(h.getBody().getPosition(),  h.getEndPoint()) || 
				   withinBounds(h.getBody().getPosition(),  h.getStartPoint())){
					h.getBody().setLinearVelocity(h.getBody().getLinearVelocity().x*-1, h.getBody().getLinearVelocity().y*-1);
				}
			}
		}
		
		// TODO: Update energy quantity (fill in these values)
		Vector2 force = new Vector2(character.parts.get(CHEST).getVX(), character.parts.get(CHEST).getVY());
		character.updateEnergy(oxygen, 1, force.len(), true);

		if(risingObstacle != null){
			risingObstacle.setHeight(risingObstacle.getHeight()+risingObstacle.getSpeed());
			for(PartModel p : character.parts){
				if(risingObstacle.getHeight() >= p.getPosition().y){
					character.setEnergy(0);
					failed = true;
				}
			}
		}
		
		if (character.getEnergy() <= 0){
			failed = true;
			for(int e : EXTREMITIES){
				 ExtremityModel extremity = (ExtremityModel) character.parts.get(e);
				 ungrip(extremity);
				 extremity.ungrip();
				 extremity.body.setType(BodyDef.BodyType.DynamicBody);
				 extremity.setTexture(partTextures[e].getTexture());
			}
		}
		energyLevel = Math.round(Math.abs(character.getEnergy()-100)/10);
		checkHasCompleted(); 
		if(complete){
			//TODO: properly change level
			changeLevel(currLevel);
		}
		if (timestep == 0) cposYAtTime0 = character.parts.get(HEAD).getY();

		timestep += 1;
	}

	// ************************************START MISCELLANEOUS*********************************************** //


	private void dealWithSlipperyAndCrumblyHandholds() {
		for(int e : EXTREMITIES){
			ExtremityModel extremity = (ExtremityModel) character.parts.get(e);
			if(extremity.isGripped()){
				extremity.updateGripTime();
				if(extremity.getJoint().getBodyB() == null){
					ungrip(extremity);
				}
				else{
					HandholdModel h = (HandholdModel) extremity.getJoint().getBodyB().getFixtureList().get(0).getUserData();
					if(extremity.getGripTime() > h.getSlip()*60 && h.getSlip() > 0){
						ungrip(extremity);
					}
					if(extremity.getGripTime() > h.getCrumble()*60 && h.getCrumble() > 0){
						//TODO add crumble animation
						ungripAllFrom(h);
						objects.remove(h);
						h.deactivatePhysics(world);
					}
				}

//				 if((e == HAND_LEFT || e == HAND_RIGHT) &&
//						 character.parts.get(CHEST).getPosition().sub(extremity.getPosition()).len() > ARM_UNGRIP_LENGTH)
//					 ungrip(extremity);
//				 else if((e == FOOT_LEFT || e == FOOT_RIGHT) &&
//						 character.parts.get(CHEST).getPosition().sub(extremity.getPosition()).len() > LEG_UNGRIP_LENGTH)
//					 ungrip(extremity);
			}

		}
	}



	private void cameraWork() {
		canvas.setCameraPosition(canvas.getWidth() / 2,
				character.parts.get(CHEST).getBody().getPosition().y*scale.y);
		if(canvas.getCamera().position.y < canvas.getHeight()/2){
			canvas.setCameraPosition(canvas.getWidth()/2, canvas.getHeight()/2);
		}
		if(canvas.getCamera().position.y + canvas.getHeight()/2 > levelFormat.getFloat("height")*scale.y){
			canvas.setCameraPosition(canvas.getWidth()/2, levelFormat.getFloat("height")*scale.y-canvas.getHeight()/2);
		}
	}

	private void ungripAllFrom(HandholdModel h){
		 for(int e : EXTREMITIES){
			 ExtremityModel extremity = (ExtremityModel) character.parts.get(e);
			 if(extremity.isGripped() && extremity.getJoint().getBodyB().getFixtureList().get(0).getUserData() == h){
				 ungrip(extremity);
			 }
		 }
	}
	
	private boolean withinBounds(Vector2 position, Vector2 target) {
		float xError = Math.abs(position.x - target.x);
		float yError = Math.abs(position.y - target.y);
		return xError < .01f && yError < .01f;
	}



	private void boundBodyVelocities() {
		if (isGripping(HAND_LEFT) || isGripping(HAND_RIGHT)|| isGripping(FOOT_LEFT)|| isGripping(FOOT_RIGHT)){
			for (PartModel p:character.parts){
				Vector2 vect =  p.getLinearVelocity();
				p.setLinearVelocity(boundVelocity(vect));
			}
		}
	}



	/** Grips a handhold by adding a revolute joint between the handhold and the extremity **/ 
	public void grip(ExtremityModel e, HandholdModel h){
		if (e.getJoint() == null){
			RevoluteJointDef jointD = new RevoluteJointDef(); 
			jointD.initialize(e.getBody(), h.getBody(), e.getPosition());
			jointD.collideConnected = false;
			setJointMotor(jointD,0,10);
			Joint j = world.createJoint(jointD);
			e.setJoint(j);
		}
		e.grip();
	}
	private void setJointMotor(RevoluteJointDef jd, float motorSpeed, float maxTorque) {
		jd.enableMotor = true;
		jd.motorSpeed = motorSpeed * DEG_TO_RAD;
		jd.maxMotorTorque = maxTorque;
	}
	public void ungrip(ExtremityModel e){
		if (e.getJoint() != null){
			world.destroyJoint(e.getJoint());
			e.setJoint(null); 
			
		}
		e.ungrip();
	}

	// ************************************END MISCELLANEOUS*********************************************** //


// ************************************START OBSTACLES*********************************************** //
	/**
	 * Spawns obstacles for active obstacle zones at a random point within the zone. For an obstacle zone to be active,
	 * three conditions must hold true. 1) It must have been at least oz.getSpawnFrequency() frames since the last obstacle
	 * spawn, 2) the top of the screen must be lower than the bottom of the obstacle zone, 3) The character must be in the
	 * level chunk that contains that obstacle zone. 
	 */
	private void spawnObstacles(){
		float cpos = character.parts.get(CHEST).getBody().getPosition().y;
		for(ObstacleZone oz : obstacles){
			float viewHeight = (canvas.getCamera().position.y + canvas.getHeight()/2) / scale.y;
//			if(oz.canSpawnObstacle() && viewHeight < oz.getBounds().y &&
//					oz.isTriggered()){
			//couldnt figure out why viewHeight<oz.getBounds().y was needed...

			if(oz.ticksSinceLastSpawn == 0 && viewHeight < oz.getBounds().y && cpos > oz.getMinSpawnHeight()){
				obstacle = new ObstacleModel(oz.getObstacleTexture(), 1f, scale);
				oz.setObstX(oz.getBounds().x + rand.nextFloat()*oz.getBounds().width);
				oz.setObstY(oz.getBounds().y + rand.nextFloat()*oz.getBounds().height);
				obstacle.setX(oz.getObstX());
				obstacle.setY(oz.getObstY());

				oz.setObstacle(obstacle);
				queuedObstacles.add(oz);
				makeObstacleWarning(oz);
			}

			if ((cpos >= oz.getMinSpawnHeight() || oz.isTriggered()) && (viewHeight < oz.getBounds().y)){
				oz.setTriggered(true);
				oz.incrementSpawnTimer();
				spawnObstacle(oz);

			}else{
				oz.ticksSinceLastSpawn = 0;
				oz.setTriggered(false);
			}


//
		}
		destroyObstacleWarnings();
	}

	private void destroyObstacleWarnings() {
		float viewHeight = (canvas.getCamera().position.y + canvas.getHeight()/2) / scale.y;

		for (warningsClass w:obstacleWarnings){
			if (viewHeight >= w.maxHeight || viewHeight >= w.o.getY()){
				obstacleWarnings.removeValue(w,false);
			}
		}
		for (ObstacleZone qo : queuedObstacles){
			if (qo.getObstacle() == null || viewHeight > qo.getObstacle().getY()){
				queuedObstacles.removeValue(qo,false);
				qo.ticksSinceLastSpawn = 0;
			}

			}
	}

	private void spawnObstacle(ObstacleZone oz) {
		if (oz.canSpawnObstacle() && oz.getObstacle() != null) {
			oz.getObstacle().activatePhysics(world);
			oz.getObstacle().setBodyType(BodyDef.BodyType.DynamicBody);
			oz.getObstacle().geometry.setUserData(obstacle);
			objects.add(oz.getObstacle());
			queuedObstacles.removeValue(oz, false);
			oz.setObstacle(null);
			oz.resetSpawnTimer();
		}

	}

	private void makeObstacleWarning(ObstacleZone oz) {

		obstacleWarnings.add(new warningsClass(
				oz.getObstX() + oz.getObstacle().width/2f,oz.getBounds().y,oz.getObstacle()));

	}
// ************************************END OBSTACLES*********************************************** //

	/**
	 * @param vect - current linear velocity vector of any body part
     * @return bounded linear velocity vector
	 * @author Jacob

     */
	private Vector2 boundVelocity(Vector2 vect) {
		if (Math.abs(vect.x) > 1 || Math.abs(vect.y) > 1) {
		}
		vect.x = (vect.x>0) ? Math.min(PART_MAX_X_VELOCITY,vect.x) : Math.max(-1 * PART_MAX_X_VELOCITY,vect.x);
		vect.y = (vect.y>0) ? Math.min(PART_MAX_Y_VELOCITY,vect.y) : Math.max(-1 * PART_MAX_Y_VELOCITY,vect.y);


		return vect;
	}
	/**
	 * !!!currently not functioning!!!
	 * will apply dampening to any limb not currently controlled by the player & are unattached to a handhold
	 * will help limbs not swing around wildly after player releases them.
	 * @author Jacob
	 *
 	 */
	private void applyDampening() {
		for (int ext : EXTREMITIES){
			if (!isGripping(ext)){
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
	 * this method glows any handholds close enough for the person's extremity to grab.
	 * //TODO there will be a possible issue if person at full extension and snaps to a handhold out of their reach.
	 * //	 * implement a calculation which says that handhold distance <= MAX_ARM_DIST or MAX_LEG_DIST.
	 * @author Jacob
	 */
	private HandholdModel[] glowHandholds() {
		HandholdModel[] newGlowingHandholds = new HandholdModel[4];
		for (int i = 0; i<EXTREMITIES.length; i++) {
			HandholdModel closest = null;
			double dist;
			double closestdist = HANDHOLD_SNAP_RADIUS + 1;

			for (GameObject obj : objects) {
				if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
					HandholdModel h = (HandholdModel) obj;
					if (i<1)
						h.unglow();

					for (Vector2 snapPoint : h.snapPoints) {
						dist = distanceFrom(EXTREMITIES[i], snapPoint);
						if (dist<=HANDHOLD_SNAP_RADIUS) {
							closest = closestdist > dist ? h:closest;
							closestdist = closestdist > dist ? dist:closestdist;

						}
					}

				}
			}

			newGlowingHandholds[i] = closest;

		}
		for (HandholdModel g : newGlowingHandholds){
			if (g != null) g.glow();
		}
		return newGlowingHandholds;

	}

	/**
	 * snaps any limbs in justReleased (limbs player controlled last timestep but no longer does) to closest handhold
	 * if possible
	 * @author Jacob

	 * @param input
     */
	private void snapLimbsToHandholds(InputController input, HandholdModel[] hs) {
		for (int i : justReleased) {
			snapIfPossible(i, hs);
		}
		if (timestep == 0) {
			snapIfPossible(FOOT_LEFT,hs);
			snapIfPossible(HAND_LEFT,hs);
			snapIfPossible(HAND_RIGHT,hs);
			snapIfPossible(FOOT_RIGHT,hs);

		}
	}
	
	/**
	 * snaps limb to handhold if possible.
	 * @param limb - limb to snap if possible
	 * @author Jacob
	 * */
	private void snapIfPossible(int limb, HandholdModel[] hs) {
//		for (GameObject obj : objects) {
//			if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
//				HandholdModel h = (HandholdModel) obj;
		HandholdModel closest = null;
		double dist;
		double closestdist = HANDHOLD_SNAP_RADIUS + 1;
		Vector2 closestSnapPoint = new Vector2(0,0);
		for (HandholdModel h: hs) {
			if (h == null) continue;
 			for (Vector2 snapPoint : h.snapPoints) {
				dist = distanceFrom(limb, snapPoint);
				if (dist <= HANDHOLD_SNAP_RADIUS){
					closest = closestdist > dist ? h:closest;
					closestdist = closestdist > dist ? dist:closestdist;
					closestSnapPoint = snapPoint;
				}
			}
		}
		if (closest !=null){
			character.parts.get(limb).setPosition(closestSnapPoint);
			((ExtremityModel) character.parts.get(limb)).grip();
			grip(((ExtremityModel) character.parts.get(limb)), closest);
		}
//			}
//		}
	}

	/**
	 * helper function to check if limb is close enough to a snapPoint on a handhold
	 * used for both snapping limbs to handholds and glowing handholds showing player they're close enough to snap
	 *
	 * @param limb - limb to check for closeness
	 * @param snapPoint - point on handhold to check distance to
	 * @author Jacob
	 *
	 *
	*/

	private boolean closeEnough(int limb, Vector2 snapPoint) {
		Vector2 dist = new Vector2 (character.parts.get(limb).getPosition().sub(snapPoint));
		return (Math.sqrt(dist.x * dist.x + dist.y * dist.y) <= HANDHOLD_SNAP_RADIUS);
	}
	private double distanceFrom(int limb, Vector2 snapPoint) {
		Vector2 dist = new Vector2 (character.parts.get(limb).getPosition().sub(snapPoint));
		return Math.sqrt(dist.x * dist.x + dist.y * dist.y);
	}

	public PooledList<GameObject> getGameObjects(){
		return objects;
	}

	public TextureRegion getGameBackground() { return background; }

	private boolean isGripping(int part) {
		return ((ExtremityModel)(character.parts.get(part))).isGripped();
	}


	private void drawToggles(){
		Vector2 pos = character.parts.get(HAND_LEFT).getPosition(); 
		TextureRegion t; 
		if (nextToPress.contains(HAND_LEFT, true))
			t = tutorialTextures[4]; 
		else
			t = tutorialTextures[0]; 
		canvas.draw(t, Color.WHITE, (pos.x*scale.x)-10, (pos.y*scale.y),50,50);
		
		pos = character.parts.get(HAND_RIGHT).getPosition(); 
		if (nextToPress.contains(HAND_RIGHT, true))
			t = tutorialTextures[5]; 
		else
			t = tutorialTextures[1]; 
		canvas.draw(t, Color.WHITE, (pos.x*scale.x)+10, (pos.y*scale.y),50,50);
		
		pos = character.parts.get(FOOT_LEFT).getPosition(); 
		if (nextToPress.contains(FOOT_LEFT, true))
			t = tutorialTextures[6]; 
		else
			t = tutorialTextures[2]; 
		canvas.draw(t, Color.WHITE, (pos.x*scale.x)-10, (pos.y*scale.y),40,40);
		
		pos = character.parts.get(FOOT_RIGHT).getPosition(); 
		if (nextToPress.contains(FOOT_RIGHT, true))
			t = tutorialTextures[7]; 
		else
			t = tutorialTextures[3]; 
		canvas.draw(t, Color.WHITE, (pos.x*scale.x)+10, (pos.y*scale.y),40,40);
	}
	
	private void checkHasCompleted(){
		this.complete =  character.parts.get(HAND_RIGHT).getPosition().y >= levelFormat.getFloat("height")
				||character.parts.get(HAND_LEFT).getPosition().y >= levelFormat.getFloat("height")
				||character.parts.get(FOOT_RIGHT).getPosition().y >= levelFormat.getFloat("height")
				||character.parts.get(FOOT_LEFT).getPosition().y >= levelFormat.getFloat("height");  
	}


	//	a Draw Note: If two parts are crossing each other, and one part is on a handhold, the other part
//	should be drawn ON TOP of the hooked part.
	//TODO needs to be corrected in many cases - its just a matter of drawing anything attached to a handhold
	//first, then if an arm crosses underneath the chest/head draw it first, same with legs.

	public void draw() {
		canvas.clear();
		canvas.begin();
		Vector2 v = character.parts.get(CHEST).getPosition();
		Vector2 v2 = character.parts.get(HEAD).getPosition();
		float y = canvas.getCamera().position.y - canvas.getHeight() / 2;
		float tileY = y - (y % (canvas.getWidth() / 4));
		canvas.draw(background, Color.WHITE, canvas.getWidth() * 3 / 4, y, canvas.getWidth() / 4, canvas.getHeight());
		canvas.draw(midground, Color.WHITE, canvas.getWidth() * 3 / 4, y * MIDGROUND_SCROLL, canvas.getWidth() / 4, canvas.getHeight());

		for (int i = 0; i < 5; i++) {
			canvas.draw(tile, Color.WHITE, canvas.getWidth() / 4, tileY, canvas.getWidth() / 4, canvas.getWidth() / 4);
			canvas.draw(tile, Color.WHITE, canvas.getWidth() / 2, tileY, canvas.getWidth() / 4, canvas.getWidth() / 4);
			canvas.draw(edge, Color.WHITE, canvas.getWidth() * 3 / 4, tileY, canvas.getWidth() / 16, canvas.getHeight());
			tileY += canvas.getWidth() / 4;
		}
		float a = (v2.y - cposYAtTime0)/(maxHandhold - cposYAtTime0);
		if (timestep%60 == 0 && character.getEnergy() != 0)
			progressLevel = Math.min(6,Math.max(0,Math.round(a * 6 - .1f)));
		canvas.draw(ground, Color.WHITE, canvas.getWidth() / 4, 0, canvas.getWidth() / 2, canvas.getHeight() / 8);
		canvas.draw(UI, Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
		canvas.draw(levelLabels[currLevel], Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
		canvas.end();


		canvas.begin();
		if (progressLevel > 0) {
			canvas.draw(progressTextures[progressLevel-1], Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
		}

		canvas.draw(progressBackgroundTexture, Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());

		float f = character.getEnergy();
		canvas.end();

		if ( f<= 30){
			lowEnergySprite.setAlpha(.5f + Math.min((30-f)/f,.5f));
			batch.begin();
			lowEnergySprite.draw(batch);
			batch.end();

			flashing2 --;
			canvas.begin();
			if (flashing2<f/4){
				canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.BLACK, 0, y, canvas.getWidth() / 4, canvas.getHeight());

				if (flashing2<=0)
					flashing2 = Math.round(f/2);
			}else {
				canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());

			}
		}else{
			canvas.begin();

			canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());

//			canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());

		}
		canvas.draw(fatigueTexture, Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());

		if (currLevel == LEVEL_TUTORIAL)
		canvas.draw(tutorialOverlay, Color.WHITE, canvas.getWidth()/4, canvas.getHeight()/8, canvas.getWidth()/2, levelFormat.getFloat("height")*scale.y);




		canvas.end();

		canvas.begin();
		for (GameObject obj : objects) obj.draw(canvas);
		if (tutorialToggle) drawToggles();
		canvas.end();

		canvas.begin();
		if (complete) {
			canvas.drawText("YOU WIN", font, canvas.getWidth() / 2, canvas.getCamera().position.y);
		}

		if (risingObstacle != null) {
			float lavaOrigin = risingObstacle.getHeight() * scale.y -
					canvas.getHeight();
			canvas.draw(risingObstacle.getTexture(), Color.WHITE, canvas.getWidth() / 4, lavaOrigin, canvas.getWidth() * 3 / 4, canvas.getHeight());
		}
		canvas.end();
		//draw the obstacle warnings.
		if (queuedObstacles.size > 0) {
			for (warningsClass oz : obstacleWarnings) {
				warningSprite.setBounds(oz.center * scale.x -  1 * scale.x, y/scale.y + canvas.getHeight()*9f/10f, 2f * scale.x , canvas.getHeight()/10f);
				warningSprite.setAlpha(1f);
				batch.begin();
				warningSprite.draw(batch);
				batch.end();
			}
		}


		if (debug) {
			canvas.beginDebug();
			for(GameObject obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
	}

	public void setLevel(int level){
		if (level >= 0 && level < NUM_LEVELS){
			currLevel = level;
		}
	}

	public void nextLevel(){
		if (currLevel != NUM_LEVELS - 1){
			currLevel += 1;
		}
	}

	public int getCurrLevel(){
		return currLevel;
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
	}
	
	public void dispose(){
		font.dispose();
		super.dispose();
	}


}



