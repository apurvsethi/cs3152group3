package beigegang.mountsputnik;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;

import java.io.File;

import beigegang.util.*;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.utils.*;

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
	private static final String BACKGROUND_FILE = "background.png";
	private static final String FOREGROUND_FILE = "preliminaryCharacterFilmStrip.png";
	
	private static final String HANDHOLD_TEXTURES[] = {"assets/canyon/handhold.png", "assets/canyon/handholdglow.png", "assets/canyon/handholdgrabbed.png"};
	private static final String PART_TEXTURES[] = {"Ragdoll/Corrected/Corrected/Torso.png", "Ragdoll/Corrected/Corrected/Head.png", "Ragdoll/Corrected/Corrected/Hips.png",
			"Ragdoll/Corrected/Corrected/ArmLeft.png", "Ragdoll/Corrected/Corrected/ArmRight.png", "Ragdoll/Corrected/Corrected/ForearmLeft.png", "Ragdoll/Corrected/Corrected/ForearmRight.png",
			"Ragdoll/Corrected/Corrected/HandLeftUngripped.png", "Ragdoll/Corrected/Corrected/HandRightUngripped.png", "Ragdoll/Corrected/Corrected/ThighLeft.png",
			"Ragdoll/Corrected/Corrected/ThighRight.png", "Ragdoll/Corrected/Corrected/CalfLeft.png", "Ragdoll/Corrected/Corrected/CalfRight.png", "Ragdoll/Corrected/Corrected/FeetShoeLeft.png",
			"Ragdoll/Corrected/Corrected/FeetShoeRight.png", "Ragdoll/Corrected/Corrected/HandLeftGripped.png", "Ragdoll/Corrected/Corrected/HandRightGripped.png"};

	/**
	 * font for displaying debug values to screen
	 */
	private static BitmapFont font = new BitmapFont();
	private static boolean upsideDown = false;
	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion foreground;
	private static TextureRegion[] partTextures = new TextureRegion[PART_TEXTURES.length];
	
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
		foreground = createTexture(manager, FOREGROUND_FILE, false);
		
		for (int i = 0; i < PART_TEXTURES.length; i++) {
			partTextures[i] = createTexture(manager, PART_TEXTURES[i], false);
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

		while(currentHeight < remainingHeight){
			//TODO: account for difficulty
			int blockNumber = ((int) (Math.random() * diffBlocks)) + 1;
			JsonValue levelPiece = jsonReader.parse(Gdx.files.internal("Levels/"+levelName+"/block"+blockNumber+".json"));

			addChunk(levelPiece, currentHeight, levelName);
			currentHeight += levelPiece.getFloat("size");

			for(int i = 0; i < filler; i++){
				blockNumber = ((int) (Math.random() * fillerSize)) + 1;
				levelPiece = jsonReader.parse(Gdx.files.internal("Levels/general/block"+blockNumber+".json"));
				addChunk(levelPiece, currentHeight, "general");
				currentHeight += levelPiece.getInt("size");
			}
		}

		character = new CharacterModel(partTextures, world, DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 2, scale, canvas.getSize());
		for (PartModel p : character.parts) {
			objects.add(p);
		}
		
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
		
		makeTestLevel(handholdDesc);
		
		while(handholdDesc != null){
			handhold = new HandholdModel(
					createTexture(assetManager, "assets/"+handholdDesc.getString("texture"), false).getTexture(), 
					createTexture(assetManager, "assets/"+handholdDesc.getString("glowTexture"), false).getTexture(), 
					createTexture(assetManager, "assets/"+handholdDesc.getString("gripTexture"), false).getTexture(),
					handholdDesc.getFloat("positionX"), handholdDesc.getFloat("positionY")+currentHeight,
					new Vector2(handholdDesc.getFloat("width"), handholdDesc.getFloat("height")), scale);
			handhold.fixtureDef.filter.maskBits = 0;
			handhold.activatePhysics(world);
			handhold.setBodyType(BodyDef.BodyType.StaticBody);
			handhold.geometry.setUserData("handhold");
			handhold.geometry.setRestitution(handholdDesc.getFloat("restitution"));
			handhold.geometry.setFriction(handholdDesc.getFloat("friction"));
			objects.add(handhold);
			handholdDesc = handholdDesc.next();
		}

		JsonValue obstacleDesc;
		try{
			obstacleDesc = levelPiece.get("obstacles").child();
		}
		catch(Exception e){
			return;
		}
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
		handhold = new HandholdModel(
				createTexture(assetManager,  "assets/"+handholdDesc.getString("texture"), false).getTexture(),
				createTexture(assetManager,  "assets/"+handholdDesc.getString("glowTexture"), false).getTexture(),
				createTexture(assetManager,  "assets/"+handholdDesc.getString("gripTexture"), false).getTexture(),
				13, 10,
				new Vector2(handholdDesc.getFloat("width"), handholdDesc.getFloat("height")), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);
		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		handhold.geometry.setUserData("handhold");
		handhold.geometry.setRestitution(handholdDesc.getFloat("restitution"));
		handhold.geometry.setFriction(handholdDesc.getFloat("friction"));
		objects.add(handhold);

		handhold = new HandholdModel(
				createTexture(assetManager,  "assets/"+handholdDesc.getString("texture"), false).getTexture(),
				createTexture(assetManager,  "assets/"+handholdDesc.getString("glowTexture"), false).getTexture(),
				createTexture(assetManager,  "assets/"+handholdDesc.getString("gripTexture"), false).getTexture(),
				19, 10,
				new Vector2(handholdDesc.getFloat("width"), handholdDesc.getFloat("height")), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);

		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		handhold.geometry.setUserData("handhold");
		handhold.geometry.setRestitution(handholdDesc.getFloat("restitution"));
		handhold.geometry.setFriction(handholdDesc.getFloat("friction"));
		objects.add(handhold);

		handhold = new HandholdModel(
				createTexture(assetManager,  "assets/"+handholdDesc.getString("texture"), false).getTexture(),
				createTexture(assetManager,  "assets/"+handholdDesc.getString("glowTexture"), false).getTexture(),
				createTexture(assetManager,  "assets/"+handholdDesc.getString("gripTexture"), false).getTexture(),
				16, 5,
				new Vector2(handholdDesc.getFloat("width"), handholdDesc.getFloat("height")), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);
		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		handhold.geometry.setUserData("handhold");
		handhold.geometry.setRestitution(handholdDesc.getFloat("restitution"));
		handhold.geometry.setFriction(handholdDesc.getFloat("friction"));

		objects.add(handhold);

		handhold = new HandholdModel(
				createTexture(assetManager,  "assets/"+handholdDesc.getString("texture"), false).getTexture(),
				createTexture(assetManager,  "assets/"+handholdDesc.getString("glowTexture"), false).getTexture(),
				createTexture(assetManager,  "assets/"+handholdDesc.getString("gripTexture"), false).getTexture(),
				16, 7,
				new Vector2(handholdDesc.getFloat("width"), handholdDesc.getFloat("height")), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);
		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		handhold.geometry.setUserData("handhold");
		handhold.geometry.setRestitution(handholdDesc.getFloat("restitution"));
		handhold.geometry.setFriction(handholdDesc.getFloat("friction"));

		objects.add(handhold);

		handhold = new HandholdModel(
				createTexture(assetManager,  "assets/"+handholdDesc.getString("texture"), false).getTexture(),
				createTexture(assetManager, "assets/"+ handholdDesc.getString("glowTexture"), false).getTexture(),
				createTexture(assetManager, "assets/"+ handholdDesc.getString("gripTexture"), false).getTexture(),
				16, 10,
				new Vector2(handholdDesc.getFloat("width"), handholdDesc.getFloat("height")), scale);
		handhold.fixtureDef.filter.maskBits = 0;
		handhold.activatePhysics(world);
		handhold.setBodyType(BodyDef.BodyType.StaticBody);
		handhold.geometry.setUserData("handhold");
		handhold.geometry.setRestitution(handholdDesc.getFloat("restitution"));
		handhold.geometry.setFriction(handholdDesc.getFloat("friction"));

		objects.add(handhold);

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
		boolean a = input.didLeftLeg() ? addToButtonsPressed((FOOT_LEFT)) : checkIfJustReleased(FOOT_LEFT);
		boolean b = input.didRightLeg() ? addToButtonsPressed((FOOT_RIGHT)) : checkIfJustReleased(FOOT_RIGHT);
		boolean c = input.didLeftArm() ? addToButtonsPressed((HAND_LEFT)) : checkIfJustReleased(HAND_LEFT);
		boolean d = input.didRightArm() ? addToButtonsPressed((HAND_RIGHT)) : checkIfJustReleased(HAND_RIGHT);

		Vector2 forceL = new Vector2(0, 0);
		Vector2 forceR = new Vector2(0, 0);

		if (nextToPress.size > 0) {
			for (int i : nextToPress) {
				((ExtremityModel) (character.parts.get(i))).ungrip();
			}
			for (int ext:EXTREMITIES){
				if (((ExtremityModel) (character.parts.get(ext))).isGripping())
					forceL.add(calcForce(ext,nextToPress.get(0),input.getHorizontalL(),input.getVerticalL()));
				//change so its pretty

			}
			applyForce(nextToPress.get(0),forceL.scl(.5f),true,input.getHorizontalL(),input.getVerticalL());

			if (nextToPress.size > 1 && (TWO_LIMB_MODE)) {
				for (int ext:EXTREMITIES){
					if (((ExtremityModel) (character.parts.get(ext))).isGripping())
						forceR.add(calcForce(ext, nextToPress.get(1),input.getHorizontalR(),input.getVerticalR()));
				}


				 applyForce(nextToPress.get(1),forceR.scl(.5f),true,input.getHorizontalR(),input.getVerticalR());

			}

		}
		if (TORSO_MODE){
			forceR.x = 0;
			forceR.y = 0;
			for (int ext:EXTREMITIES){
				if (((ExtremityModel) (character.parts.get(ext))).isGripping())
					forceR.add(calcForce(ext, CHEST,input.getHorizontalR(),input.getVerticalR()));
			}
			applyTorsoForceIfApplicable(forceR);
		}

		else {
			applyDampening();
		}
		for (PartModel p:character.parts){
			Vector2 vect =  p.getLinearVelocity();
			p.setLinearVelocity(boundVelocity(vect));
		}
		if (justReleased.size > 0 || timestep == 0) {
			snapLimbsToHandholds(input);
		}
		glowHandholds();
		timestep += 1;

		canvas.setCameraPosition(canvas.getWidth() / 2,
						character.parts.get(CHEST).getBody().getPosition().y*SCREEN_HEIGHT/DEFAULT_HEIGHT);
		if(canvas.getCamera().position.y < canvas.getHeight()/2){
			canvas.setCameraPosition(canvas.getWidth()/2, canvas.getHeight()/2);
		}
		
		spawnObstacles();
		for(GameObject g : objects){
			if(g instanceof ObstacleModel && 
					g.getBody().getPosition().y  < character.parts.get(CHEST).getBody().getPosition().y){
				objects.remove(g);
			}
		}
		// TODO: Update energy quantity (fill in these values)
		//TODO : change if character is in TWO_LIMB_MODE?
		character.updateEnergy(oxygen, 1, forceL, true);
//		if (character.getEnergy <= 0){
//			for(int e : EXTREMITIES){
//				 ExtremityModel extremity = (ExtremityModel) character.parts.get(e);
//				 extremity.ungrip();
//				 extremity.body.setType(BodyDef.BodyType.DynamicBody);
//				 extremity.setTexture(partTextures[e].getTexture());
//			}s
//		}
	}

	private void spawnObstacles(){
		for(ObstacleZone oz : obstacles){
			float viewHeight = (canvas.getCamera().position.x + canvas.height/2) * DEFAULT_HEIGHT/SCREEN_HEIGHT;
			if(oz.canSpawnObstacle() && viewHeight < oz.getBounds().y && 
					character.parts.get(CHEST).getBody().getPosition().y >= oz.getMinSpawnHeight()){
				obstacle = new ObstacleModel(oz.getObstacleTexture(), 1f, scale); //TODO: determine obstacle drawSizeScale
				obstacle.activatePhysics(world);
				obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
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
						character.parts.get(limb).body.setType(BodyDef.BodyType.StaticBody);
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


	/**
	 * @param hookedPart - calculation of force for an extremity attached to a handhold
	 * @param freePart - part that the force will be applied to
	 * calculates X and Y force player can use to propel their selected limb.
	 * should use the size of the handhold in the calculation although it does not.
	 * @author Jacob
	 */
	private Vector2 calcForce(int hookedPart, int freePart,float xval,float yval) {
		float forcex = 0f;
		float forcey = 0f;
		forcex = xval * CONSTANT_X_FORCE;

		if (!upsideDown) {
			Vector2 hp = character.parts.get(hookedPart).getPosition();
//			Vector2 fp = character.parts.get(freePart).getPosition();
			if (hookedPart == FOOT_LEFT || hookedPart == FOOT_RIGHT) {
				forcey = calcLegForce(yval,hookedPart);
			}
			else {
				forcey = calcArmForce(yval,hookedPart);
			}
		}
		return new Vector2(forcex,forcey);

	}

	/**
	 * TODO play around with & modify this method
	 * method applies force in a "natural" and theoretically predictable way
	 * on the limb selected. Does need to be modified to find best "natural" balance of moving.
	 * whatever we decide natural should be
	 * @param limb - player's currently selected limb to apply force to
	 * @param force - force to apply
	 * @param wake - boolean to wake limb up (currently always passed in as true)
	 * @author Jacob
	 */
	private void applyForce(int limb,Vector2 force,boolean wake, float h, float v) {



		switch(limb){
			case FOOT_LEFT:
				applyIfUnderLimit(THIGH_LEFT,force, h, v);
				force.y *= .5;
				applyIfUnderLimit(SHIN_LEFT,force, h, v);
				force.y *= .5;
				applyIfUnderLimit(FOOT_LEFT,force, h, v);
				break;
			case FOOT_RIGHT:
				applyIfUnderLimit(THIGH_RIGHT,force, h, v);
				force.y *= .5;
				applyIfUnderLimit(SHIN_RIGHT,force, h, v);
				force.y *= .5;
				applyIfUnderLimit(FOOT_RIGHT,force, h, v);
				break;
			case HAND_LEFT:
				applyIfUnderLimit(HAND_LEFT,force, h, v);
				applyIfUnderLimit(FOREARM_LEFT,force, h, v);
				force.y *= .5;
				applyIfUnderLimit(ARM_LEFT,force, h, v);
				break;
			case HAND_RIGHT:
				applyIfUnderLimit(HAND_RIGHT,force, h, v);
				applyIfUnderLimit(FOREARM_RIGHT,force, h, v);
				force.y *= .5;
				applyIfUnderLimit(ARM_RIGHT,force, h, v);

				break;
			default:
				//do nothing
				break;

		}
	}

	private void applyTorsoForceIfApplicable(Vector2 force) {
		if (TORSO_MODE){
			InputController input = InputController.getInstance();
			float h = input.getHorizontalR();
			float v = input.getVerticalR();
			applyIfUnderLimit(CHEST,new Vector2(force.x,force.y),h,v);

		}
	}

	private void applyIfUnderLimit(int part, Vector2 force, float h, float v){
		Vector2 vect = character.parts.get(part).body.getLinearVelocity();

		if (Math.signum(h) != Math.signum(vect.x) || Math.abs(vect.x) < PART_MAX_X_VELOCITY){
			character.parts.get(part).body.applyForceToCenter(force.x,0,true);

		}
		if (Math.signum(v) != Math.signum(vect.y) || Math.abs(vect.y) < PART_MAX_Y_VELOCITY) {
			character.parts.get(part).body.applyForceToCenter(0,force.y,true);

		}
	}

	//	a Draw Note: If two parts are crossing each other, and one part is on a handhold, the other part
//	should be drawn ON TOP of the hooked part.
	public void draw() {
		canvas.clear();

		canvas.begin();
		canvas.draw(background, Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());
		canvas.end();

		canvas.begin();
		for (GameObject obj : objects) obj.draw(canvas);
		canvas.end();

		canvas.begin();
		canvas.drawText(((Integer)(Math.round(character.getEnergy()))).toString(), font, 0f,
				canvas.getCamera().position.y+canvas.height/2-50f);
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


	private float calcLegForce(float yval, int hookedPart){
		float forcey = 0f;
		float angleKnee = 0f;
		float footToHip = 0f;
		float angleKneeModifier = 0f;
		float distanceModifier = 0f;
		float totalModifier = 0f;
		Vector2 hp = character.parts.get(hookedPart).getPosition();

		if (yval > 0) {
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
			forcey = totalModifier * MAX_PUSHFORCE_LEG * yval;
		} else {
			forcey = MAX_PUSHFORCE_LEG * yval;
		}
		return forcey;
	}



	private float calcArmForce(float yval, int hookedPart) {
		float forcey = 0f;
		float angleElbow = 0f;
		Vector2 armToShoulder;
		float angleElbowModifier = 0f;
		Vector2 hp = character.parts.get(hookedPart).getPosition();
		if (yval > 0) {
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
			forcey *= yval;
		}
		//working with ARMS NOW!!!!
		else {
			//I think that this should be basically unlimited amount force if you're lowering a limb then
//						it should be easy, there's not much force the legs use for this but this way it always happens.
//						return a negative number because that way the force for the limb can be channeled easily.
			forcey =  MAX_PULLFORCE_ARM * yval;


		}
		return forcey;
	}
}



