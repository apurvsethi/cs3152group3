package beigegang.mountsputnik;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;

import beigegang.util.*;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.*;

import java.util.Random;

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
	//We need to preload every single texture, regardless of which level we're currently using. Loading can't be
	//dynamically
	private static final String BACKGROUND_FILE = "assets/canyon/background.png";
	private static final String LAVA_FILE = "assets/testlavatexture.png"; //TODO: make this a better texture
	private static final String MIDGROUND_FILE = "assets/canyon/Midground.png";
	private static final String TILE_FILE = "assets/canyon/SurfaceLight.png";
	private static final String UI_FILE = "assets/HUD.png";
	private static final String EDGE_FILE = "assets/canyon/SurfaceEdgeLight.png";
	private static final String GROUND_FILE = "assets/canyon/LevelStart.png";
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
	private static TextureRegion edge;
	private static TextureRegion ground;
	private static TextureRegion lavaTexture;
	private static TextureRegion[] partTextures = new TextureRegion[PART_TEXTURES.length];
	private static TextureRegion[] tutorialTextures = new TextureRegion[TUTORIAL_TEXTURES.length];
	private static TextureRegion[] handholdTextures = new TextureRegion[HANDHOLD_TEXTURES.length];
	
	/** The reader to process JSON files */
	private JsonReader jsonReader;
	/** The JSON defining the level model */
	private JsonValue levelFormat;
	/** The level's oxygen concentration (environmental energy gain modifier"*/
	private float oxygen;
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
		manager.load(BACKGROUND_FILE, Texture.class);
		assets.add(BACKGROUND_FILE);
		manager.load(MIDGROUND_FILE, Texture.class);
		assets.add(MIDGROUND_FILE);
		manager.load(TILE_FILE, Texture.class);
		assets.add(TILE_FILE);
		manager.load(UI_FILE, Texture.class);
		assets.add(UI_FILE);
		manager.load(EDGE_FILE, Texture.class);
		assets.add(EDGE_FILE);
		manager.load(GROUND_FILE, Texture.class);
		assets.add(GROUND_FILE);
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
		midground = createTexture(manager, MIDGROUND_FILE, false);
		tile = createTexture(manager, TILE_FILE, false);
		UI = createTexture(manager, UI_FILE, false);
		edge = createTexture(manager, EDGE_FILE, false);
		ground = createTexture(manager, GROUND_FILE, false);
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
	
	private ObstacleZone obstacleZone;
	private RisingObstacle risingObstacle = null;
	private ObstacleModel obstacle;
	private Array<ObstacleZone> obstacles = new Array<ObstacleZone>();
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
		for (GameObject obj : objects) {
			obj.deactivatePhysics(world);
		}
		risingObstacle = null;
		objects.clear();
		obstacles.clear();
		addQueue.clear();
		world.dispose();
		timestep = 0;
		//TODO: make this based on current level, rather than hardcoded test
		populateLevel("canyon");
	}

	/**
	 * Creates the character, and then generates the level according to specified environment. 
	 * Currently, all level assets should be stored in the appropriate location according to this 
	 * path within the assets folder: the general level description will be in "Levels/[levelName]/level.json" 
	 * and the individual blocks will be in "Levels/[levelName]/block[x].json" where x is a whole number.
	 * 
	 * The general level description contains such things as the phsyics constants like gravity or oxygen
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
	 * @param levelName: the level to be generated
	 */
	public void populateLevel(String levelName) {
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
		
		JsonValue lava = levelFormat.get("lava");
		if(lava.getBoolean("present")){
			risingObstacle = new RisingObstacle(lavaTexture, lava.getFloat("speed"));
		}
		//TODO delete this line as well:
		risingObstacle = null;
		//end this line


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

		//if(currentHeight == 0) makeTestLevel(handholdDesc);
		
		Random rand = new Random();
		while(handholdDesc != null){
			handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(),
					handholdDesc.getFloat("positionX"), handholdDesc.getFloat("positionY")+currentHeight,
					new Vector2(handholdDesc.getFloat("width"), handholdDesc.getFloat("height")), scale);
			handhold.fixtureDef.filter.maskBits = 0;
			handhold.activatePhysics(world);
			handhold.setBodyType(BodyDef.BodyType.StaticBody);
			handhold.geometry.setUserData(handhold);
			handhold.geometry.setRestitution(handholdDesc.getFloat("restitution"));
			handhold.geometry.setFriction(handholdDesc.getFloat("friction"));
			objects.add(handhold);
			
			try{handhold.setCrumble(handholdDesc.getFloat("crumble"));}
			catch(Exception e){handhold.setCrumble(0);}
			try{handhold.setSlip(handholdDesc.getFloat("slip"));}
			catch(Exception e){handhold.setSlip(0);}
			
			handholdDesc = handholdDesc.next();
		}

		JsonValue obstacleDesc;
		try{obstacleDesc = levelPiece.get("obstacles").child();}
		catch(Exception e){return;}
		Rectangle bound;
		while(obstacleDesc != null){
			bound = new Rectangle(obstacleDesc.getFloat("originX"), obstacleDesc.getFloat("originY")+currentHeight,
					obstacleDesc.getFloat("width"),obstacleDesc.getFloat("height"));
			//TODO: Set texture to something other than null once we have textures for obstacles
			obstacleZone = new ObstacleZone(null, currentHeight, obstacleDesc.getInt("frequency"), bound);
			obstacles.add(obstacleZone);
			obstacleDesc = obstacleDesc.next();
		}
	}
	//TODO delete when there are actually levels!!!
	private void makeTestLevel(JsonValue handholdDesc) {
		Random rand = new Random();
		Rectangle bound = new Rectangle(10,20,10,3);
		obstacleZone = new ObstacleZone(handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(),
				10, 2f, bound);
		obstacles.add(obstacleZone);
	}

	/**
	 * if button was not pressed on the previous turn but is pressed now, add to nextToPress
	 * note: cannot use set because order must be preserved for accurate/predictive control by player
	 * @param part
     * @return true if part was just pressed, false otherwise
	 * @author Jacob
     */
	public boolean addToButtonsPressed(int part) {
		boolean notRedundant = !nextToPress.contains(part, true);
		if(notRedundant)
			nextToPress.add(part);
		return notRedundant;
	}

	/**
	 * checks if that extremity was released on this timestep by player, if so adds to justReleased
 	 * @param part
	 * @return true if part was just released, false otherwise
	 * @author Jacob
     */
    public boolean checkIfJustReleased(int part) {
		boolean present = nextToPress.removeValue(part, true);
		if (present)
			justReleased.add(part);
		return present;
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

		justReleased.clear();
		upsideDown = character.parts.get(HEAD).getPosition().y - character.parts.get(CHEST).getPosition().y <= 0;
		//will likely use these eventually but not right now. do not delete!
		boolean a = input.didLeftLeg() ? addToButtonsPressed((FOOT_LEFT)) : checkIfJustReleased(FOOT_LEFT);
		boolean b = input.didRightLeg() ? addToButtonsPressed((FOOT_RIGHT)) : checkIfJustReleased(FOOT_RIGHT);
		boolean c = input.didLeftArm() ? addToButtonsPressed((HAND_LEFT)) : checkIfJustReleased(HAND_LEFT);
		boolean d = input.didRightArm() ? addToButtonsPressed((HAND_RIGHT)) : checkIfJustReleased(HAND_RIGHT);
//		if (! (a || c || b || d)) nextToPress.clear();
		if(input.didSelect()) tutorialToggle = !tutorialToggle;

		Movement.makeHookedJointsMovable(nextToPress);

		if (input.didMenu()) listener.exitScreen(this, EXIT_PAUSE);
		
		Vector2 forceL = new Vector2(0, 0);
		Vector2 forceR = new Vector2(0, 0);
		float[] forces = null;

		Movement.resetLimbSpeedsTo0();
//		if (timestep == 0) nextToPress.clear();
		if (nextToPress.size > 0) {
			for (int i : nextToPress) {
				((ExtremityModel) (character.parts.get(i))).ungrip();
				ungrip(((ExtremityModel) (character.parts.get(i)))); 
			}
			float v = input.getVerticalL();
			float h = input.getHorizontalL();
			forces = Movement.findAndApplyForces(nextToPress.get(0),v,h);

//			if (nextToPress.size > 1 && (TWO_LIMB_MODE)) {
//				v = input.getVerticalR();
//				h = input.getHorizontalR();
//				forces = Movement.findAndApplyForces(nextToPress.get(1),v,h);
//			}

		}
//		applyDampening();
		if (TORSO_MODE){
			applyTorsoForceIfApplicable(calcTorsoForce());
		}
		//bounding velocities
		boundBodyVelocities();

		if (justReleased.size > 0 || timestep == 0) {
			snapLimbsToHandholds(input);
		}
		glowHandholds();
		timestep += 1;

		canvas.setCameraPosition(canvas.getWidth() / 2,
						character.parts.get(CHEST).getBody().getPosition().y*scale.y);
		if(canvas.getCamera().position.y < canvas.getHeight()/2){
			canvas.setCameraPosition(canvas.getWidth()/2, canvas.getHeight()/2);
		}
//		if(canvas.getCamera().position.y + canvas.getHeight()/2 > background.getRegionHeight()){
//			canvas.setCameraPosition(canvas.getWidth()/2, background.getRegionHeight()-canvas.getHeight()/2); 
//		}
		spawnObstacles();
		for(GameObject g : objects){
			if(g instanceof ObstacleModel && 
					g.getBody().getPosition().y  < (canvas.getCamera().position.y-canvas.getWidth())/scale.y){
				objects.remove(g);
			}
		}
		
		// TODO: Update energy quantity (fill in these values)
		float exertion = 0;
		if(forces!=null)
			for(int i = 0; i < forces.length; i++){
				exertion+=Math.abs(forces[i]);
			}
		character.updateEnergy(oxygen, 1, exertion, true);

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
	}

	private void boundBodyVelocities() {
		for (PartModel p:character.parts){
			Vector2 vect =  p.getLinearVelocity();
			p.setLinearVelocity(boundVelocity(vect));
		}
	}

	private Vector2 calcTorsoForce() {
		Vector2 forceR = new Vector2(0,0);
		forceR.x = CONSTANT_X_FORCE * 2f;
		forceR.y = CONSTANT_X_FORCE * 2f;
		int counter = 0;
		counter = isGripping(HAND_LEFT)?counter+1:counter;
		counter = isGripping(HAND_RIGHT)?counter+1:counter;
		counter = isGripping(FOOT_LEFT)?counter+1:counter;
		counter = isGripping(FOOT_RIGHT)?counter+1:counter;
		if (counter > 2) counter +=2;
		forceR.scl(counter);
		return forceR;
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
		Random rand = new Random();
		for(ObstacleZone oz : obstacles){
			float viewHeight = (canvas.getCamera().position.y + canvas.getHeight()/2) / scale.y;
			if(oz.canSpawnObstacle() && viewHeight < oz.getBounds().y && 
					character.parts.get(CHEST).getBody().getPosition().y >= oz.getMinSpawnHeight()){
				obstacle = new ObstacleModel(oz.getObstacleTexture(), 1f, scale); 
				obstacle.setX(oz.getBounds().x+rand.nextFloat()*oz.getBounds().width);
				obstacle.setY(oz.getBounds().y+rand.nextFloat()*oz.getBounds().height);
				obstacle.activatePhysics(world);
				obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
				obstacle.geometry.setUserData(obstacle);
				objects.add(obstacle);
				oz.resetSpawnTimer();
			}
			else
				oz.incrementSpawnTimer();
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
		Vector2 dist = character.parts.get(limb).getPosition().sub(snapPoint);
		return (Math.sqrt(dist.x * dist.x + dist.y * dist.y) <= HANDHOLD_SNAP_RADIUS);
	}


	public PooledList<GameObject> getGameObjects(){
		return objects;
	}

	public TextureRegion getGameBackground() { return background; }



	private void applyTorsoForceIfApplicable(Vector2 force) {
		if (TORSO_MODE){
			if (isGripping(FOOT_LEFT)|| isGripping(FOOT_RIGHT) || isGripping(HAND_LEFT) || isGripping(HAND_RIGHT)){
				InputController input = InputController.getInstance();
				float h = input.getHorizontalR();
				float v = input.getVerticalR();
				applyIfUnderLimit(CHEST,new Vector2(force.x,force.y),h,v);
			}

		}
	}

	private boolean isGripping(int part) {
		return ((ExtremityModel)(character.parts.get(part))).isGripped();
	}

	private void applyIfUnderLimit(int part, Vector2 force, float h, float v) {
		Vector2 vect = character.parts.get(part).body.getLinearVelocity();
		character.parts.get(part).body.applyForceToCenter(force.x*h, 0, true);
		character.parts.get(part).body.applyForceToCenter(0, force.y*v, true);
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


	//	a Draw Note: If two parts are crossing each other, and one part is on a handhold, the other part
//	should be drawn ON TOP of the hooked part.
	//TODO needs to be corrected in many cases - its just a matter of drawing anything attached to a handhold
	//first, then if an arm crosses underneath the chest/head draw it first, same with legs.

	public void draw() {
		canvas.clear();

		float y = canvas.getCamera().position.y - canvas.getHeight() / 2;
		float tileY = y - (y % (canvas.getWidth() / 4));
		canvas.begin();
		canvas.draw(background, Color.WHITE, canvas.getWidth() * 3 / 4, y,canvas.getWidth() / 4,canvas.getHeight());
		canvas.draw(midground, Color.WHITE, canvas.getWidth() * 3 / 4, y * MIDGROUND_SCROLL,canvas.getWidth() / 4,canvas.getHeight());

		for (int i = 0; i < 5; i++){
			canvas.draw(tile,Color.WHITE, canvas.getWidth() / 4, tileY, canvas.getWidth() / 4, canvas.getWidth() / 4);
			canvas.draw(tile,Color.WHITE, canvas.getWidth() / 2, tileY, canvas.getWidth() / 4, canvas.getWidth() / 4);
			canvas.draw(edge, Color.WHITE, canvas.getWidth() * 3 / 4, tileY,canvas.getWidth() / 16,canvas.getHeight());
			tileY += canvas.getWidth() / 4;
		}


		canvas.draw(ground, Color.WHITE, canvas.getWidth() / 4, 0,canvas.getWidth() / 2,canvas.getHeight() / 8);
		canvas.draw(UI, Color.WHITE, 0, y,canvas.getWidth() / 4,canvas.getHeight());
		
		canvas.end();

		canvas.begin();
		for (GameObject obj : objects) obj.draw(canvas);
		if (tutorialToggle) drawToggles(); 
		canvas.end();

		canvas.begin();
		canvas.drawText(((Integer)(Math.round(character.getEnergy()))).toString(), font, 0f,
				canvas.getCamera().position.y+canvas.getHeight()/2-10f);
		
		if(risingObstacle!=null){
			float lavaOrigin = risingObstacle.getHeight()*scale.y -
					canvas.getHeight();
			canvas.draw(risingObstacle.getTexture(), Color.WHITE, canvas.getWidth() / 4, lavaOrigin, canvas.getWidth() * 3/4, canvas.getHeight());
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
	}
	
	public void dispose(){
		font.dispose();
		super.dispose();
	}


}



