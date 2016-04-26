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
	private static final String LEVEL_NAMES[] = {"tutorial", "canyon", "canyon", "canyon", "canyon", "canyon", "canyon"};//,"mountain","sky","space"}; <-- Add the rest of these in as they are given assets
	private static final String LAVA_FILE = "assets/testlavatexture.png"; //TODO: make this a better texture
	private static final String UI_FILE = "assets/HUD2.png";
	private static final String LOGO_FILE = "Menu/StartMenu/Logo Only.png";
	private static final String HANDHOLD_TEXTURES[] = {"assets/canyon/Handhold1.png", "assets/canyon/Handhold2.png"};
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
	private Random rand = new Random();


	//	private static TextureRegion[] energyTextures;
private static final String ENERGY_TEXTURES[] = new String[10];
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
	private static TextureRegion LOGO;
	private static TextureRegion edge;
	private static TextureRegion ground;
	private static TextureRegion lavaTexture;
	private static TextureRegion[] partTextures = new TextureRegion[PART_TEXTURES.length];
	private static TextureRegion[] tutorialTextures = new TextureRegion[TUTORIAL_TEXTURES.length];
	private static TextureRegion[] handholdTextures = new TextureRegion[HANDHOLD_TEXTURES.length];
	private static TextureRegion blackoutTexture;
	private static String BLACKOUT = "assets/blackout.png";
	private static String FATIGUE_BAR = "Energy/Fatigue Gauge.png";
	private static TextureRegion fatigueTexture;
	private static String WARNING_TEXTURE = "assets/Falling Rock Warning.png";
	private static TextureRegion warningTexture;
	private static String PROGRESS_BACKGROUND= "assets/Progress Bar.png";
	private static TextureRegion progressBackgroundTexture;
	private static String PROGRESS_BAR= "Progress Chalk Bar.png";
	private static TextureRegion progressBarTexture;
	private Sprite blackoutSprite = new Sprite(new Texture(BLACKOUT));
	private Sprite warningSprite = new Sprite(new Texture(WARNING_TEXTURE));
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
			manager.load("assets/"+name+"/SurfaceLight.png", Texture.class);
			assets.add("assets/"+name+"/SurfaceLight.png");
			manager.load("assets/"+name+"/SurfaceEdgeLight.png", Texture.class);
			assets.add("assets/"+name+"/SurfaceEdgeLight.png");
			manager.load("assets/"+name+"/LevelStart.png", Texture.class);
			assets.add("assets/"+name+"/LevelStart.png");
		}
		for (int i = 1; i<=ENERGY_TEXTURES.length; i++){
			String name = "Energy/e" + i + ".png";
			manager.load(name, Texture.class);
			assets.add(name);
			ENERGY_TEXTURES[i-1] = name;
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

		for (String HANDHOLD_TEXTURE : HANDHOLD_TEXTURES) {
			manager.load(HANDHOLD_TEXTURE, Texture.class);
			assets.add(HANDHOLD_TEXTURE);
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
		tile = createTexture(manager, "assets/"+levelName+"/SurfaceLight.png", false);
		UI = createTexture(manager, UI_FILE, false);
		LOGO = createTexture(manager, LOGO_FILE, false);
		edge = createTexture(manager, "assets/"+levelName+"/SurfaceEdgeLight.png", false);
		ground = createTexture(manager, "assets/"+levelName+"/LevelStart.png", false);
		lavaTexture = createTexture(manager, LAVA_FILE, false);
		
		for (int i = 0; i < PART_TEXTURES.length; i++) {
			partTextures[i] = createTexture(manager, PART_TEXTURES[i], false);
		}
		
		for (int i = 0; i < TUTORIAL_TEXTURES.length; i++) {
			tutorialTextures[i] = createTexture(manager, TUTORIAL_TEXTURES[i], false);
		}

		for (int i = 0; i < HANDHOLD_TEXTURES.length; i++) {
			handholdTextures[i] = createTexture(manager, HANDHOLD_TEXTURES[i], false);
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
		assetState = AssetState.COMPLETE;

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
	
	public void changeLevel(int level){
		listener.exitScreen(this, EXIT_GAME_NEXT_LEVEL);
	}
	
	private ObstacleZone obstacleZone;
	private RisingObstacle risingObstacle = null;
	private ObstacleModel obstacle;
	/** all obstacle zones in level */
	private Array<ObstacleZone> obstacles = new Array<ObstacleZone>();
	/** Current obstacle warnings to display */
	private Array<ObstacleZone> obstacleWarnings = new Array<ObstacleZone>();
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
	
	public GameMode() {
		super(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GRAVITY);

		//create debug font
		font.setColor(Color.RED);
		font.getData().setScale(5);


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
		//TODO: make this based on current level, rather than hardcoded test
		populateLevel();
		blackoutSprite.setBounds(canvas.getWidth()/4,0,canvas.getWidth()*2/4,canvas.getHeight());

	}

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
		maxHandhold = remainingHeight;
		while(currentHeight < remainingHeight){
			//TODO: account for difficulty
			int blockNumber = ((int) (Math.random() * diffBlocks)) + 1;
			levelBlocks.add("Levels/"+levelName+"/block"+blockNumber+".json"); 
			JsonValue levelPiece = jsonReader.parse(Gdx.files.internal("Levels/"+levelName+"/block"+blockNumber+".json"));
			
			addChunk(levelPiece, currentHeight, levelName);
			currentHeight += levelPiece.getFloat("size");
			for(int i = 0; i < filler; i++){
				blockNumber = ((int) (Math.random() * fillerSize)) + 1;
				levelPiece = jsonReader.parse(Gdx.files.internal("Levels/general/block"+blockNumber+".json"));
				levelBlocks.add("Levels/general/block"+blockNumber+".json"); 
				addChunk(levelPiece, currentHeight, "general");
				currentHeight += levelPiece.getInt("size");
			}
		}
		System.out.println("level Blocks: " + levelBlocks.toString());
		
		JsonValue lava = levelFormat.get("lava");
		if(lava.getBoolean("present")){
			risingObstacle = new RisingObstacle(lavaTexture, lava.getFloat("speed"));
		}
		
		character = new CharacterModel(partTextures, world, DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 2, scale, canvas.getSize());
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
		objects.add(character.parts.get(CHEST));
		objects.add(character.parts.get(HEAD));
		objects.add(character.parts.get(HIPS));

		Movement.setCharacter(character);

	}
	
	/** 
	 * Adds blocks to the level based on JSON block description
	 * 
	 * @param levelPiece: The block description
	 * @param currentHeight: y offset from the bottom of the screen
	 * @author Daniel
	 */
	private void addChunk(JsonValue levelPiece, float currentHeight, String levelName){
		JsonAssetManager.getInstance().loadDirectory(levelPiece);
		JsonAssetManager.getInstance().allocateDirectory();
		
		JsonValue handholdDesc = levelPiece.get("handholds").child();

		Random rand = new Random();

		while(handholdDesc != null){
			handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(),
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
			obstacleZone = new ObstacleZone(handholdTextures[0].getTexture(), null, currentHeight, obstacleDesc.getInt("frequency"), bound);
			obstacles.add(obstacleZone);
			obstacleDesc = obstacleDesc.next();
		}
		


		JsonValue staticDesc;
		try{
			staticDesc = levelPiece.get("static").child();
			ObstacleModel obstacle;
			while(staticDesc != null){
				//TODO: Set texture to something other than null once we have textures for obstacles

				obstacle = new ObstacleModel(handholdTextures[0].getTexture(), staticDesc.getFloat("size"), scale);
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
	//TODO delete when there are actually levels!!!
	private void makeTestLevel2(float currentHeight) {
		Random rand = new Random();
		Rectangle bound = new Rectangle(12,20,2,4);
		obstacleZone = new ObstacleZone(handholdTextures[0].getTexture(),warningTexture,
				0, 2f, bound);
	}


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

		if (justReleased.size > 0 || timestep == 0) {
			snapLimbsToHandholds(input);
		}
		
		glowHandholds();

		canvas.setCameraPosition(canvas.getWidth() / 2,
						character.parts.get(CHEST).getBody().getPosition().y*scale.y);
		if(canvas.getCamera().position.y < canvas.getHeight()/2){
			canvas.setCameraPosition(canvas.getWidth()/2, canvas.getHeight()/2);
		}
		if(canvas.getCamera().position.y + canvas.getHeight()/2 > levelFormat.getFloat("height")*scale.y){
			canvas.setCameraPosition(canvas.getWidth()/2, levelFormat.getFloat("height")*scale.y-canvas.getHeight()/2);
		}
		
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
		timestep += 1;
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

			if(oz.canSpawnObstacle()&& viewHeight < oz.getBounds().y){
				obstacle = new ObstacleModel(oz.getObstacleTexture(), 1f, scale);
				obstacle.setX(oz.getObstX());
				obstacle.setY(viewHeight + 1f);
				obstacle.activatePhysics(world);
				obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
				obstacle.geometry.setUserData(obstacle);
				objects.add(obstacle);
				oz.resetSpawnTimer();

			}

			else if (cpos >= oz.getMinSpawnHeight() || oz.isTriggered()){
				oz.setTriggered(true);
				oz.incrementSpawnTimer();
				if ((oz.getMaxSpawnHeight() - cpos)*scale.y>0 && oz.ticksSinceLastSpawn > 15){
					makeObstacleWarning(oz);
				}else if (oz.ticksSinceLastSpawn == 5){
					destroyObstacleWarning((oz));
				}else if ((oz.getMaxSpawnHeight() - cpos)*scale.y<0){
					destroyObstacleWarning(oz);
				}
			}

//			}
		}
	}

	private void destroyObstacleWarning(ObstacleZone oz) {
		obstacleWarnings.removeValue(oz,false);

	}

	private void makeObstacleWarning(ObstacleZone oz) {
		if (obstacleWarnings.lastIndexOf(oz,false) == -1){
			obstacleWarnings.add(oz);
			oz.setObstX(oz.getBounds().x + rand.nextFloat()*oz.getBounds().width);
			oz.setObstY(oz.getBounds().y + rand.nextFloat()*oz.getBounds().height);
		}

	}

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
	 * snaps any limbs in justReleased (limbs player controlled last timestep but no longer does) to closest handhold
	 * if possible
	 * @author Jacob

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
	 * snaps limb to handhold if possible.
	 * @param limb - limb to snap if possible
	 * @author Jacob
	 * */
	private void snapIfPossible(int limb) {
		for (GameObject obj : objects) {
			if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
				HandholdModel h = (HandholdModel) obj;
				for (Vector2 snapPoint : h.snapPoints) {
					if (closeEnough(limb, snapPoint)) {
						character.parts.get(limb).setPosition(snapPoint);
						((ExtremityModel) character.parts.get(limb)).grip();
						grip(((ExtremityModel) character.parts.get(limb)), h); 
					}
				}

			}
		}
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
		float a = v.y/maxHandhold;
		if (timestep%60 == 0 && character.getEnergy() != 0)
			progressLevel = Math.min(6,Math.max(0,Math.round(v.y/maxHandhold * 6 + .5f)));
		canvas.draw(ground, Color.WHITE, canvas.getWidth() / 4, 0, canvas.getWidth() / 2, canvas.getHeight() / 8);
		canvas.draw(UI, Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
		canvas.draw(LOGO, Color.FIREBRICK, 0, canvas.getHeight() * 5.4f/6 + y, canvas.getWidth() / 4, canvas.getHeight() * .5f/6);
		canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
		canvas.draw(fatigueTexture, Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
		if (progressLevel > 0) {
			canvas.draw(progressTextures[progressLevel-1], Color.BLUE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
		}
		canvas.draw(progressBackgroundTexture, Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
		canvas.end();

		canvas.begin();
		for (GameObject obj : objects) obj.draw(canvas);
		if (tutorialToggle) drawToggles();
		canvas.end();

		canvas.begin();
		if (complete) {
			canvas.drawText("YOU WIN", font, canvas.getWidth() / 2, canvas.getCamera().position.y);
		}
//		canvas.drawText(((Integer) (Math.round(character.getEnergy()))).toString(), font, 0f,
//				canvas.getCamera().position.y + canvas.getHeight() / 2 - 10f);

		if (risingObstacle != null) {
			float lavaOrigin = risingObstacle.getHeight() * scale.y -
					canvas.getHeight();
			canvas.draw(risingObstacle.getTexture(), Color.WHITE, canvas.getWidth() / 4, lavaOrigin, canvas.getWidth() * 3 / 4, canvas.getHeight());
		}
		if (obstacles.size > 0) {
			for (ObstacleZone oz : obstacleWarnings) {
				warningSprite.setBounds(oz.getObstX() * scale.x -  1 * scale.x, canvas.getCamera().position.y + canvas.getHeight() / 2 - 100f, 2f * scale.x , 100f);
				warningSprite.setAlpha(.5f + oz.ticksSinceLastSpawn/2f/oz.getSpawnFrequency());
				batch.begin();
				warningSprite.draw(batch);
				batch.end();
			}
		}
		canvas.end();
		float f = character.getEnergy();
		if ( f<= 40){
//			if (f<=10 && flashing2<f){
//				flashing2 --;
//				if (flashing2==0)
//					flashing2 = Math.round(f)*2;
//			}else {
				blackoutSprite.setAlpha((Math.abs(f - 100f) / 100f - .5f) * 1.5f);
				batch.begin();
				blackoutSprite.draw(batch);
				batch.end();
//			}
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



