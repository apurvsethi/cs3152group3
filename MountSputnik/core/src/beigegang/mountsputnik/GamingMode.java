package beigegang.mountsputnik;

import beigegang.util.FilmStrip;
import beigegang.util.JsonAssetManager;
import beigegang.util.PooledList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import static beigegang.mountsputnik.Constants.*;

public class GamingMode extends ModeController {

    protected int currLevel = LEVEL_TUTORIAL;
    protected boolean flashing = false;
    protected int flashing1 = 10;
    protected int flashing2 = 10;
    /** A string representing the name of the current level */
    protected String levelName = "canyon";
    /** leeway for grabbing green holds */
    Array<HandholdModel> glowingHandholds = new Array<>();
    Array<HandholdModel> glowingHandholds1 = new Array<>();
    Array<HandholdModel> glowingHandholds2 = new Array<>();
    Array<HandholdModel> glowingHandholds3 = new Array<>();
    Array<HandholdModel> glowingHandholds4 = new Array<>();
    Array<HandholdModel> glowingHandholds5 = new Array<>();
    /**
     * left foot, then right foot, then left hand, then right hand
     */
    Array<HandholdModel> snappedHandholds = new Array<>(4);
    /**
     * Track asset loading from all instances and subclasses
     */
    protected AssetState assetState = AssetState.EMPTY;
    /**
     * used for tracking the game timestep - used to snap limbs to handholds on original timestep
     */
    protected static int timestep = 0;
    protected static int checkpointTimestep = 0;
    /**	both are updated every timestep with horizontal and vertical input left joystick from player */
    protected static float inx = 0f;
    protected static float iny = 0f;
    /**	both are updated every timestep with horizontal and vertical input right stick from player */
    public static boolean writtenToFile = false;
    protected static float rinx = 0f;
    protected static float riny = 0f;
    /** int used in every single loop in the program */
    protected static int counterInt;
    /**
     * Strings for files used, string[] for parts, etc.
     */
    protected static final String GAUGES_FILE = "assets/gauges.png";
    protected static final String KREMLIN_FILE = "Fonts/kremlin.ttf";
    protected static final String MASTODON_FILE = "Fonts/mastodon.ttf";
    protected static final String LEVEL_NAMES[] = {"tutorial", "canyon", "waterfall", "volcano", "mountain", "sky", "space"}; 
    protected static final String LAVA_FILE = "assets/volcano/Lava.png";
    protected static final String LAVA_GLOW_FILE = "assets/volcano/LavaGlow.png";
    protected static final String LAVA_CONT_FILE = "assets/volcano/LavaCont.png";
    protected static final String UI_FILE = "assets/HUD4Background.png";
    protected static final String LOGO_FILE = "Menu/StartMenu/Logo Only.png";
    protected static final String GLOW_FILE = "assets/glow2.png";
//    protected static final String SNAP_FILE = "assets/snap.png";
//    protected static final String CRUMBLY_FILE = "assets/crumbly.png";
//    protected static final String SLIPPERY_FILE = "assets/slippery.png";
    protected static final String RUSSIAN_FLAG_FILE = "RussianFlag.png";
    protected static final HashMap<String,Integer> NUM_HANDHOLDS = new HashMap<String,Integer>();
    protected static final String PART_TEXTURES[] = {"Ragdoll/Torso.png", "Ragdoll/Head.png", "Ragdoll/Hips.png",
            "Ragdoll/ArmLeft.png", "Ragdoll/ArmRight.png", "Ragdoll/ForearmLeft.png", "Ragdoll/ForearmRight.png",
            "Ragdoll/HandLeft.png", "Ragdoll/HandRight.png", "Ragdoll/ThighLeft.png",
            "Ragdoll/ThighRight.png", "Ragdoll/CalfLeft.png", "Ragdoll/CalfRight.png", "Ragdoll/FeetShoeLeft.png",
            "Ragdoll/FeetShoeRight.png"};
    protected static final String AMERICA_PART_TEXTURES[] = {"Ragdoll/murica/Torso.png","Ragdoll/murica/Head.png",
    		"Ragdoll/murica/Hips.png","Ragdoll/ArmLeft.png", "Ragdoll/ArmRight.png", "Ragdoll/ForearmLeft.png", "Ragdoll/ForearmRight.png",
            "Ragdoll/HandLeft.png", "Ragdoll/HandRight.png","Ragdoll/murica/ThighLeft.png","Ragdoll/murica/ThighRight.png", 
            "Ragdoll/CalfLeft.png", "Ragdoll/CalfRight.png", "Ragdoll/FeetShoeLeft.png","Ragdoll/FeetShoeRight.png" }; 
    protected static final String SHADOW_TEXTURES[] = {"Ragdoll/shadow/Torso.png", "Ragdoll/shadow/Head.png", "Ragdoll/shadow/Hips.png",
            "Ragdoll/shadow/ArmLeft.png", "Ragdoll/shadow/ArmRight.png", "Ragdoll/shadow/ForearmLeft.png", "Ragdoll/shadow/ForearmRight.png",
            "Ragdoll/shadow/HandLeftGripped.png", "Ragdoll/shadow/HandRightGripped.png", "Ragdoll/shadow/ThighLeft.png",
            "Ragdoll/shadow/ThighRight.png", "Ragdoll/shadow/CalfLeft.png", "Ragdoll/shadow/CalfRight.png", "Ragdoll/shadow/FeetShoeLeft.png",
            "Ragdoll/shadow/FeetShoeRight.png"};
    protected static final String TUTORIAL_TEXTURES[] = {
            "Ragdoll/controls/360_LB.png",
            "Ragdoll/controls/360_RB.png",
            "Ragdoll/controls/360_LT.png",
            "Ragdoll/controls/360_RT.png",
            "Ragdoll/controls/360_LB_selected.png",
            "Ragdoll/controls/360_RB_selected.png",
            "Ragdoll/controls/360_LT_selected.png",
            "Ragdoll/controls/360_RT_selected.png"
    };
    protected static final String HANDHOLD_WARNING = "assets/HandholdGauge.png";
    protected static final String OBSTACLE_WARNING = "assets/Obstacle Warning.png";
    protected static final String TUTORIAL_OVERLAY_TEXTURE = "assets/tutorial/TutorialOverlay.png";
    protected static final String TUTORIAL_RING = "assets/tutorial/YellowRing.png"; 
    protected Random rand = new Random();
    protected float cposYAtTime0 = 0;

    //	protected static TextureRegion[] energyTextures;
    //first element is an empty drawing
    protected static final String ENERGY_TEXTURES[] = new String[10];
    protected static final String PROGRESS_FILE = "assets/chalk.png";

    protected static TextureRegion[] energyTextures =  new TextureRegion[ENERGY_TEXTURES.length];
    /**
     * font for displaying debug values to screen
     */
    protected static BitmapFont font = new BitmapFont();
    protected static boolean upsideDown = false;
    /**
     * Texture asset for files used, parts, etc.
     */
    protected static TextureRegion progress;
    protected static TextureRegion gauges;
    protected static TextureRegion background;
    protected static TextureRegion midground;
    protected static TextureRegion foreground; 
    protected static TextureRegion tile;
    protected static TextureRegion UI;
    protected static TextureRegion CANYON;
    protected static TextureRegion LOGO;
    protected static BitmapFont mastodon;
    protected static BitmapFont kremlin;
    protected static TextureRegion RUSSIAN_FLAG;
    protected static TextureRegion edge;
    protected static TextureRegion ground;
    protected static TextureRegion lavaTexture;
    protected static TextureRegion lavaGlowTexture; 
    protected static TextureRegion lavaContTexture;
    protected static TextureRegion glowTexture;
    protected static TextureRegion snapTexture;
    protected static TextureRegion crumblyTexture;
    protected static TextureRegion slipperyTexture;
    protected static TextureRegion staticObstacle;
    protected static FilmStrip fallingObstacle;
    protected static FilmStrip handholdWarning;
    protected static FilmStrip obstacleWarning;
    protected static FilmStrip[] partTextures = new FilmStrip[PART_TEXTURES.length];
    protected static FilmStrip[] americaPartTextures = new FilmStrip[AMERICA_PART_TEXTURES.length]; 
    protected static TextureRegion[] shadowTextures = new TextureRegion[SHADOW_TEXTURES.length];
    protected static TextureRegion[] tutorialTextures = new TextureRegion[TUTORIAL_TEXTURES.length];
    protected static TextureRegion[] handholdTextures;
    protected static TextureRegion tutorialOverlay;
    protected static TextureRegion tutorialRing; 

    protected static TextureRegion blackoutTexture;
    protected static String BLACKOUT = "assets/blackout.png";
    protected static String LOW_ENERGY_HALO= "assets/redhalo.png";
    protected static TextureRegion lowEnergyHalo;
    protected static TextureRegion progressBarTexture;
    protected Texture UITexture = new Texture(UI_FILE);
    protected static int progressLevel = 0;
    /** The reader to process JSON files */
    protected JsonReader jsonReader;
    /** The JSON defining the level model */
    protected JsonValue levelFormat;
    protected JsonValue animationFormat;
    /** The level's oxygen concentration (environmental energy gain modifier"*/
    protected float oxygen;
    /** which energy bar to display, 1-10 */
    protected int energyLevel;
    /** whether the player has moved yet */;
    protected boolean moved;

    public static float maxLevelHeight;

    protected PauseMode pauseMode;
    protected VictoryMode victoryMode;
    protected DeadMode deadMode;

    protected boolean isPaused = false;
    protected boolean isDead = false;
    protected boolean isVictorious = false;
    // ************************************START CONTENT LOADING*********************************************** //
    protected BitmapFont kremlinS;
    protected BitmapFont mastodonS;

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
        snappedHandholds.add(null);
        snappedHandholds.add(null);
        snappedHandholds.add(null);
        snappedHandholds.add(null);
        assetManager = manager;
        if (assetState != AssetState.EMPTY) return;

        assetState = AssetState.LOADING;

        pauseMode.preLoadContent(manager);
        deadMode.preLoadContent(manager);
        victoryMode.preLoadContent(manager);

        for (String name : LEVEL_NAMES) {
            loadAddTexture("assets/"+name+"/Background.png");
            loadAddTexture("assets/"+name+"/Midground.png");
            loadAddTexture("assets/"+name+"/Foreground.png");
            loadAddTexture("assets/"+name+"/Surface.png");
            loadAddTexture("assets/"+name+"/SurfaceEdge.png");
            loadAddTexture("assets/"+name+"/LevelStart.png");
            loadAddTexture("assets/"+name+"/StaticObstacle.png");
            loadAddTexture("assets/"+name+"/Rockbust_Animation.png");
        }
        for (counterInt = 0; counterInt<ENERGY_TEXTURES.length; counterInt++){
            String name = "Energy/e" + counterInt + ".png";
            loadAddTexture(name);
            ENERGY_TEXTURES[counterInt] = name;
        }
        loadAddTexture(PROGRESS_FILE);
        for (Entry<String, Integer> entry : NUM_HANDHOLDS.entrySet())
            for (counterInt = 1; counterInt <= entry.getValue(); counterInt++)
                loadAddTexture("assets/"+entry.getKey()+"/Handhold"+counterInt+".png");
        for (String PART_TEXTURE : PART_TEXTURES) loadAddTexture(PART_TEXTURE);
        for (String PART_TEXTURE: AMERICA_PART_TEXTURES) loadAddTexture(PART_TEXTURE); 
        for (String SHADOW_TEXTURE: SHADOW_TEXTURES) loadAddTexture(SHADOW_TEXTURE);
        for (String TUTORIAL_TEXTURE : TUTORIAL_TEXTURES) loadAddTexture(TUTORIAL_TEXTURE);
        
        FreetypeFontLoader.FreeTypeFontLoaderParameter k = makeFont(KREMLIN_FILE, 55, Color.ORANGE);
		loadAddFont("Game" + KREMLIN_FILE, k);
		FreetypeFontLoader.FreeTypeFontLoaderParameter m = makeFont(MASTODON_FILE, 55, Color.ORANGE);
		loadAddFont("Game" + MASTODON_FILE, m);
		FreetypeFontLoader.FreeTypeFontLoaderParameter ks = makeFont(KREMLIN_FILE, 40, Color.ORANGE);
		loadAddFont("GameSmall" + KREMLIN_FILE, ks);
		FreetypeFontLoader.FreeTypeFontLoaderParameter ms = makeFont(MASTODON_FILE, 40, Color.ORANGE);
		loadAddFont("GameSmall" + MASTODON_FILE, ms);
    
        loadAddTexture(UI_FILE);
        loadAddTexture(GAUGES_FILE);
        loadAddTexture(LOGO_FILE);
        loadAddTexture(LAVA_FILE);
        loadAddTexture(LAVA_GLOW_FILE);
        loadAddTexture(LAVA_CONT_FILE);
        loadAddTexture(GLOW_FILE);
        loadAddTexture(BLACKOUT);
        loadAddTexture(LOW_ENERGY_HALO);
        loadAddTexture(TUTORIAL_OVERLAY_TEXTURE);
        loadAddTexture(TUTORIAL_RING); 
        loadAddTexture(RUSSIAN_FLAG_FILE);
        loadAddTexture(HANDHOLD_WARNING);
        loadAddTexture(OBSTACLE_WARNING);
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

        pauseMode.loadContent(manager);
        deadMode.loadContent(manager);
        victoryMode.loadContent(manager);
        
        background = createTexture(manager, "assets/"+levelName+"/Background.png", false);
        midground = createTexture(manager, "assets/"+levelName+"/Midground.png", false);
        foreground = createTexture(manager, "assets/"+levelName+"/Foreground.png", false); 
        tile = createTexture(manager, "assets/"+levelName+"/Surface.png", false);
        UI = createTexture(manager, UI_FILE, false);
        LOGO = createTexture(manager, LOGO_FILE, false);
        edge = createTexture(manager, "assets/"+levelName+"/SurfaceEdge.png", false);
        ground = createTexture(manager, "assets/"+levelName+"/LevelStart.png", false);
        lavaTexture = createTexture(manager, LAVA_FILE, false);
        lavaGlowTexture = createTexture(manager, LAVA_GLOW_FILE, false);
        lavaContTexture = createTexture(manager, LAVA_CONT_FILE, false);
        glowTexture = createTexture(manager, GLOW_FILE, false);
//        snapTexture = createTexture(manager, SNAP_FILE, false);
//        crumblyTexture = createTexture(manager, CRUMBLY_FILE, false);
//        slipperyTexture = createTexture(manager, SLIPPERY_FILE, false);
        staticObstacle = createTexture(manager, "assets/"+levelName+"/StaticObstacle.png", false);
        fallingObstacle = createFilmStrip(manager, "assets/"+levelName+"/Rockbust_Animation.png", 1, 5, 5);
        kremlin = manager.get("Game" + KREMLIN_FILE, BitmapFont.class);
        mastodon = manager.get("Game" + MASTODON_FILE, BitmapFont.class);
        kremlinS = manager.get("GameSmall" + KREMLIN_FILE, BitmapFont.class);
        mastodonS = manager.get("GameSmall" + MASTODON_FILE, BitmapFont.class);
        
        for (counterInt = 0; counterInt < PART_TEXTURES.length; counterInt++) {
            partTextures[counterInt] = createFilmStrip(manager, PART_TEXTURES[counterInt], 1,
                    BODY_PART_ANIMATION_FRAMES[counterInt], BODY_PART_ANIMATION_FRAMES[counterInt]);
        }
        
        for (counterInt = 0; counterInt < AMERICA_PART_TEXTURES.length; counterInt++) {
            americaPartTextures[counterInt] = createFilmStrip(manager, AMERICA_PART_TEXTURES[counterInt], 1,
                    BODY_PART_ANIMATION_FRAMES[counterInt], BODY_PART_ANIMATION_FRAMES[counterInt]);
        }

        for (counterInt = 0; counterInt < SHADOW_TEXTURES.length; counterInt++){
            shadowTextures[counterInt] = createTexture(manager, SHADOW_TEXTURES[counterInt], false);
        }

        for (counterInt = 0; counterInt < TUTORIAL_TEXTURES.length; counterInt++) {
            tutorialTextures[counterInt] = createTexture(manager, TUTORIAL_TEXTURES[counterInt], false);
        }

        handholdTextures = new TextureRegion[NUM_HANDHOLDS.get(levelName)];
        for (counterInt = 1; counterInt <= NUM_HANDHOLDS.get(levelName); counterInt++) {
            handholdTextures[counterInt-1] = createTexture(manager, "assets/"+levelName+"/Handhold"+counterInt+".png", false);
        }

        for (counterInt = 0; counterInt < ENERGY_TEXTURES.length; counterInt++) {
            energyTextures[counterInt] = createTexture(manager, ENERGY_TEXTURES[counterInt], false);
        }

        progress = createTexture(manager, PROGRESS_FILE, false);
        blackoutTexture = createTexture(manager,BLACKOUT,false);
        gauges = createTexture(manager,GAUGES_FILE,false);
        lowEnergyHalo = createTexture(manager,LOW_ENERGY_HALO,false);
        tutorialOverlay = createTexture(manager, TUTORIAL_OVERLAY_TEXTURE, false);
        tutorialRing = createTexture(manager, TUTORIAL_RING, false); 
        RUSSIAN_FLAG = createTexture(manager, RUSSIAN_FLAG_FILE, false);
        handholdWarning = createFilmStrip(manager, HANDHOLD_WARNING, 1, 13, 13);
        obstacleWarning = createFilmStrip(manager, OBSTACLE_WARNING, 1, 1, 1);
        assetState = AssetState.COMPLETE;

    }

    /**
     * Unloads the assets for this game.
     *
     * This method erases the static variables.  It also deletes the associated textures
     * from the asset manager. If no assets are loaded, this method does nothing.
     */
    public void unloadContent() {
        super.unloadContent(assetManager);
        JsonAssetManager.getInstance().unloadDirectory();
        JsonAssetManager.clearInstance();
    }
    // ************************************END CONTENT LOADING*********************************************** //

    public void changeLevel(int level){
        listener.exitScreen(this, EXIT_GAME_NEXT_RACE_LEVEL);
    }

    protected PositionMovementController movementController1;
    protected PositionMovementController movementController2;

    protected WarningController warningController;

    protected ObstacleZone obstacleZone;
    /** only for lava so far */
    protected RisingObstacle risingObstacle = null;
    protected ObstacleModel obstacle;
    /** obstacles that have been initialized but not introduced into the world yet*/
    protected Array<ObstacleZone> queuedObstacles = new Array<ObstacleZone>();
    /** all obstacle zones in level */
    protected Array<ObstacleZone> obstacles = new Array<ObstacleZone>();
    /**
     * Whether we have completed this level
     */
    protected boolean complete = false;
    protected boolean failed;
    protected float maxHandhold = 0f;
    protected int lastReachedCheckpoint = 0;
    /** if player is watching "animation" in tutorial */
    protected boolean doingAnimation = false;
    /** which step to show the animation at (starts at 0) */
    protected int animationTimestep = 0;
    /** following 6 values are animation values to override the inputController with*/
    protected float animationLX;
    protected float animationLY;
    protected float animationRX;
    protected float animationRY;
    protected Array<Integer> animationNextToPress = new Array<Integer>();
    protected Array<Integer> animationJustReleased = new Array<Integer>();
    /** writes fullJson to file when animating creator (a dev) finishes creating the animation */
    protected FileWriter animationToFile;
    /** json value to write to file */
    protected String fullJson = "{";
    protected InputController input;
    protected InputController input1 = InputController.getInstance(CONTROLLER_1);
    protected InputController input2 = InputController.getInstance(CONTROLLER_2);
    protected boolean firstUpdate = false;
    protected Vector2 vector;
    /**
     * Character of game
     */
    protected CharacterModel character1 = null;
    protected CharacterModel character2 = null;
    /**
     * A handhold
     */
    protected HandholdModel handhold;
    /**
     * holds any extremities who's buttons are pressed during this timestep. keeps order of pressing intact
     */
    protected Array<Integer> nextToPress = new Array<Integer>();
    /**
     * holds any extremities who's buttons were released during this timestep.
     */
    protected Array<Integer> justReleased = new Array<Integer>();
    /**
     * A list of all the blocks that were chosen for this generated level. Allows for debugging
     */
    protected Array<String> levelBlocks = new Array<String>();
    protected Array<JsonValue> checkpointLevelJsons = new Array<JsonValue>();
    protected Array<Integer> checkpointLevelBlocks = new Array<Integer>();


    /** A boolean indicating the toggle of the tutorial view, where limbs have their corresponding buttons shown*/
    protected boolean tutorialToggle1 = false;
    protected boolean tutorialToggle2 = false;
    
    protected String positionListWrite = "{"; 
    protected JsonValue tutorialGuide; 
    protected JsonValue currentTutorialStep; 
    protected int currentStep = 0; 
    
    /** level-related values*/
    protected Array<Float> checkpoints = new Array<Float>();
    protected Vector2 gravity;
    float remainingHeight = 0f;
    float currentHeight = 0f;
    int diffBlocks;
    int filler;
    int fillerSize;
    Array<Integer> used = new Array<Integer>();
    Array<Integer> intArray = new Array<Integer>();
    int[] ints;
    public int id;

    public GamingMode(int i) {
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_GRAVITY);
        moved = false;
        this.id = i;
        pauseMode = new PauseMode();
        victoryMode = new VictoryMode();
        deadMode = new DeadMode();
        //create debug font
        font.setColor(Color.RED);
        font.getData().setScale(5);

        NUM_HANDHOLDS.put("canyon", 1);
        NUM_HANDHOLDS.put("tutorial",4);
        NUM_HANDHOLDS.put("sky", 1);
        NUM_HANDHOLDS.put("mountain", 1);
        NUM_HANDHOLDS.put("space",1);
        NUM_HANDHOLDS.put("volcano", 1);
        NUM_HANDHOLDS.put("waterfall", 1);

    }
    public void makeJsonForAnimation(){

    }
    public void writeNextStepJsonForAnimation(float lx, float ly, float rx, float ry, Array<Integer> pressed){

    }
    public void writeJsonToFile(){

    }


    public void setAnimationReader(){

    }
    public void getAnimationInformation(){

    }

    @Override
    public void reset() {
//        if (id == GAME_MODE && timestep != 0)		writeJsonToFile();

        moved = false;
        resetAllButCheckpoints();
        checkpointLevelBlocks.clear();
        checkpointLevelJsons.clear();
        checkpoints.clear();
        lastReachedCheckpoint = 0;
        populateLevel();


    }

    protected void resetAllButCheckpoints() {
        levelName = LEVEL_NAMES[currLevel];
        complete = false;
        failed = false;
        isPaused = false;
        isDead = false;
        isVictorious = false;
        assetState = AssetState.LOADING;
        loadContent(assetManager);
        if (id == RACE_MODE){
            if (movementController2 != null) {
                movementController2.dispose();
                movementController2 = null;
            }
        }
        if (movementController1 != null) {
            movementController1.dispose();
            movementController1 = null;
        }
        if (warningController != null) {
            warningController.dispose();
            warningController = null;
        }

        for (GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }
        risingObstacle = null;
        objects.clear();
        obstacles.clear();
        addQueue.clear();
        world.dispose();
        timestep = 0;
        animationTimestep = 0;
        queuedObstacles.clear();
    }





    // ************************************START LEVELS*********************************************** //

    /**
     * Creates the character1, and then generates the level according to specified environment.
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
    public void populateLevel() {
        moved = false;
        readLevelStats();
        used.clear();
        maxHandhold = remainingHeight;
        maxLevelHeight = remainingHeight;
        String levelDiff = levelFormat.getString("difficulty");
        while(currentHeight < remainingHeight){
            int blockNumber = ((int) (Math.random() * diffBlocks)) + 1;
            JsonValue levelPiece = jsonReader.parse(Gdx.files.internal("Levels/"+levelName+"/block"+blockNumber+".json"));
            String blockDiff = levelPiece.getString("difficulty");
            while((used.contains(blockNumber, true)||
                    getDifficultyProb(levelDiff, blockDiff, currentHeight, remainingHeight) > Math.random())
                    &&!levelName.equals("tutorial")){
                blockNumber = ((int) (Math.random() * diffBlocks)) + 1;

                levelPiece = jsonReader.parse(Gdx.files.internal("Levels/"+levelName+"/block"+blockNumber+".json"));
                blockDiff = levelPiece.getString("difficulty");
            }
            used.add(blockNumber);
            levelBlocks.add("Levels/"+levelName+"/block"+blockNumber+".json");
            checkpointLevelJsons.add(levelPiece);
            addChunk(levelPiece, currentHeight, levelName);
            currentHeight += levelPiece.getFloat("size");
            if(!levelName.equals("volcano")) checkpoints.add(currentHeight);
        }

        checkpointLevelBlocks.addAll(used);
        System.out.println(levelBlocks);
        if (id == GAME_MODE)
            character1 = new CharacterModel(partTextures, world, DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 2, scale);
        else {
            character1 = new CharacterModel(partTextures, world, DEFAULT_WIDTH * 1 / 3, DEFAULT_HEIGHT / 2, scale);
            character2 = new CharacterModel(americaPartTextures, world, DEFAULT_WIDTH * 2 / 3, DEFAULT_HEIGHT / 2, scale);
        }
        warningController = new WarningController(scale, handholdWarning, obstacleWarning);

        //arms
        if (id == GAME_MODE)
            checkpoints.insert(0,character1.parts.get(CHEST).getY());

        makeHandholdsToGripAtStart(character1);
        if (id == RACE_MODE)
            makeHandholdsToGripAtStart(character2);
        addCharacterToGame(character1);
        if (id == RACE_MODE)
            addCharacterToGame(character2);
        movementController1 = new PositionMovementController(character1, scale);
        if (id == RACE_MODE)
            movementController2 = new PositionMovementController(character2, scale);
        canvas.setCameraPosition(canvas.getWidth()/2, levelFormat.getFloat("height")*scale.y);
        if(currLevel == LEVEL_TUTORIAL && id == GAME_MODE){
	        //To make tutorial: currentStep = 0; 
	        jsonReader = new JsonReader();
	        tutorialGuide = jsonReader.parse(Gdx.files.internal("Levels/tutorial/tutorialGuide.json"));
	        currentTutorialStep = tutorialGuide.get(currentStep); 
        }
        
    }
    /**
     * Calculates a number for use by the level generator to prioritize difficulty based on height,
     * and relative difficulty to level average
     * @param levelDiff the difficulty of the overall level
     * @param blockDiff the difficulty of the block
     * @param current the current height
     * @param total the total height
     * @return probability estimate for level generator
     */
    protected float getDifficultyProb(String levelDiff, String blockDiff, float current, float total) {
        switch (levelDiff){
            case "easy":
                if(blockDiff.equals("hard")) return 1;
                if(blockDiff.equals("medium") && current < total/2) return 1;
                if(blockDiff.equals("medium")) return (current/total)/2;
                return 0;
            case "medium":
                if(blockDiff.equals("hard") && current < total/2) return 1;
                if(blockDiff.equals("hard")) return (current/total)/2;
                if(blockDiff.equals("easy") && current > total/2) return 1;
                if(blockDiff.equals("easy")) return (1 - current/total)/2;
                return 0;
            case "hard":
                if(blockDiff.equals("easy")) return 1;
                if(blockDiff.equals("medium") && current > total/2) return 1;
                if(blockDiff.equals("medium")) return (1 - current/total)/2;
                return 0;
            default:
                return 1;
        }
    }

    protected void readLevelStats() {
        jsonReader = new JsonReader();
        levelFormat = jsonReader.parse(Gdx.files.internal("Levels/"+levelName+"/level.json"));
        JsonAssetManager.getInstance().loadDirectory(levelFormat);
        JsonAssetManager.getInstance().allocateDirectory();
        gravity = new Vector2(0,levelFormat.getFloat("gravity"));
        oxygen = levelFormat.getFloat("oxygen");
        world = new World(gravity, false);
        contactListener = new ListenerClass();
        world.setContactListener(contactListener);
        levelBlocks.clear();
        remainingHeight = levelFormat.getFloat("height");
        currentHeight=0f;
        diffBlocks = levelFormat.getInt("uniqueBlocks");
        filler = levelFormat.getInt("generalFillerSize");
        fillerSize = levelFormat.getInt("fillerBlocks");
        JsonValue lava = levelFormat.get("lava");
        if(lava.getBoolean("present")){
            risingObstacle = new RisingObstacle(lavaTexture, lava.getFloat("speed"));
        }

    }
    protected void addCharacterToGame(CharacterModel c) {
        objects.add(c.parts.get(ARM_LEFT));
        objects.add(c.parts.get(ARM_RIGHT));
        objects.add(c.parts.get(FOREARM_LEFT));
        objects.add(c.parts.get(FOREARM_RIGHT));
        objects.add(c.parts.get(HAND_LEFT));
        objects.add(c.parts.get(HAND_RIGHT));
        //legs
        objects.add(c.parts.get(THIGH_LEFT));
        objects.add(c.parts.get(THIGH_RIGHT));
        objects.add(c.parts.get(SHIN_LEFT));
        objects.add(c.parts.get(SHIN_RIGHT));
        objects.add(c.parts.get(FOOT_LEFT));
        objects.add(c.parts.get(FOOT_RIGHT));
        //rest
        objects.add(c.parts.get(HEAD));
        objects.add(c.parts.get(HIPS));
        objects.add(c.parts.get(CHEST));

    }

    protected void makeHandholdsToGripAtStart(CharacterModel c) {
        makeAddBasicHandhold(c, HAND_LEFT);
        makeAddBasicHandhold(c, HAND_RIGHT);
        makeAddBasicHandhold(c, FOOT_LEFT);
        makeAddBasicHandhold(c, FOOT_RIGHT);
    }

    private void makeAddBasicHandhold(CharacterModel c, int part) {
        handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(),
                glowTexture.getTexture(), 0, c.parts.get(part).getPosition().x,
                c.parts.get(part).getPosition().y, 0.3f, 0.3f, scale);
        handhold.fixtureDef.filter.maskBits = 0;
        handhold.activatePhysics(world);
        handhold.geometry.setUserData(handhold);
        handhold.geometry.setRestitution(1);
        handhold.geometry.setFriction(1);
        handhold.setBodyType(BodyType.StaticBody);
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
    protected void addChunk(JsonValue levelPiece, float currentHeight, String levelName){

        JsonValue handholdDesc = levelPiece.get("handholds").child();

        Random rand = new Random();
        while(handholdDesc != null){
            int snapper = 0;
            try {
                snapper = handholdDesc.getFloat("slip") == 0 && handholdDesc.getFloat("crumble") == 0 ? 0 : (handholdDesc.getFloat("slip") == 0 ? 1 : 2);
            }catch(Exception e){
                //THERES NO FREAKING SLIP OR CRUMBLE FOR THE HANDHOLDS!!
            }
            handhold = new HandholdModel( handholdTextures[rand.nextInt(handholdTextures.length)].getTexture(), glowTexture.getTexture(), snapper,
                    handholdDesc.getFloat("positionX"), handholdDesc.getFloat("positionY")+currentHeight,
                    handholdDesc.getFloat("width"), handholdDesc.getFloat("height"), scale);
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
                handhold.setLinearVelocity((tx/dist)*speed, (ty/dist)*speed);
                handhold.setVelocity(speed);
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
            obstacleZone = new ObstacleZone(fallingObstacle, null, currentHeight, obstacleDesc.getInt("frequency"), bound);
            obstacles.add(obstacleZone);
            obstacleDesc = obstacleDesc.next();
        }


        JsonValue staticDesc;
        try{
            staticDesc = levelPiece.get("static").child();
            ObstacleModel obstacle;
            while(staticDesc != null){
                obstacle = new ObstacleModel(staticObstacle.getTexture(), staticDesc.getFloat("size"), scale);
                obstacle.setX(staticDesc.getFloat("x"));
                obstacle.setY(staticDesc.getFloat("y")+currentHeight);
                if (levelName == "sky") obstacle.setSkyFixtures();

                obstacle.setBodyType(BodyType.StaticBody);
                obstacle.activatePhysics(world);
                objects.add(obstacle);
                staticDesc = staticDesc.next();
            }
        }
        catch(Exception e){}
    }

    // ************************************END LEVELS*********************************************** //

    @Override
    public void update(float dt){
        firstUpdate = true;
        update(dt,CONTROLLER_1);
        firstUpdate = false;
        if (id == RACE_MODE)
            update(dt,CONTROLLER_2);
        if (id == GAME_MODE) checkpointTimestep += 1;
        timestep += 1;
    };
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

    public void update(float dt, int controller) {
        CharacterModel character;
        PositionMovementController movementController;
        if (id == GAME_MODE || controller == CONTROLLER_1){
            character = character1;
            movementController = movementController1;
            input = InputController.getInstance(CONTROLLER_1);
        }
        else {
            character = character2;
            movementController = movementController2;
            input = InputController.getInstance(CONTROLLER_2);

        }
        doingAnimation = input.watchAnimation() && currLevel == LEVEL_TUTORIAL && id == GAME_MODE;
        if (doingAnimation) {
            getAnimationInformation();
            inx = animationLX;
            iny = animationLY;
            rinx = animationRX;
            riny = animationRY;
            nextToPress = animationNextToPress;
            justReleased = animationJustReleased;
        } else {
            inx = input.getHorizontalL();
            iny = input.getVerticalL();
            rinx = input.getHorizontalR();
            riny = input.getVerticalR();
            if(Math.abs(inx) > .5  || Math.abs(iny) > .5  || Math.abs(rinx) > .5  || Math.abs(riny) > .5 ) moved = true;
            nextToPress = input.getOrderPressed();
            justReleased.clear();
            if (input.releasedLeftArm()) justReleased.add(HAND_LEFT);
            if (input.releasedRightArm()) justReleased.add(HAND_RIGHT);
            if (input.releasedLeftLeg()) justReleased.add(FOOT_LEFT);
            if (input.releasedRightLeg()) justReleased.add(FOOT_RIGHT);

        }
        //don't uncomment createAnimation unless you know what you are doing!!
//    		createAnimation();
//
        if (id == GAME_MODE && checkIfReachedCheckpoint(character)) {
            lastReachedCheckpoint++;
        }
        if (checkIfDied(character)) {
            listener.exitScreen(this, EXIT_DIED);

        }

        if (input.didSelect()) {
            if (id == GAME_MODE || controller == CONTROLLER_1) tutorialToggle1 = !tutorialToggle1;
            else tutorialToggle2 = !tutorialToggle2;
        }

        if (input.didMenu()){
            if (id == GAME_MODE){
                listener.exitScreen(this, EXIT_PAUSE);
            }else{
                listener.exitScreen(this, EXIT_RACE_PAUSE);

            }
        }

        movementController.moveCharacter(inx,iny,rinx,riny,nextToPress,justReleased);

        if (nextToPress.size > 0) {
            for (int i : nextToPress) {
                ungrip(((ExtremityModel) (character.parts.get(i))));
                int ind = 0;

                switch (i) {
                    case FOOT_LEFT:
                        ind = 0;
                        if (snappedHandholds.get(ind)!=null){
                            HandholdModel h = snappedHandholds.get(ind);
                            snappedHandholds.set(ind,null);
                            if (!snappedHandholds.contains(h,false))
                                h.desnap();
                        }
                        break;
                    case FOOT_RIGHT:
                        ind = 1;
                        if (snappedHandholds.get(ind)!=null){
                            HandholdModel h = snappedHandholds.get(ind);
                            snappedHandholds.set(ind,null);
                            if (!snappedHandholds.contains(h,false))
                                h.desnap();
                        }
                        break;
                    case HAND_LEFT:
                        ind = 2;
                        if (snappedHandholds.get(ind)!=null){
                            HandholdModel h = snappedHandholds.get(ind);
                            snappedHandholds.set(ind,null);
                            if (!snappedHandholds.contains(h,false))
                                h.desnap();
                        }
                        break;
                    case HAND_RIGHT:
                        ind = 3;
                        if (snappedHandholds.get(ind)!=null){
                            HandholdModel h = snappedHandholds.get(ind);
                            snappedHandholds.set(ind,null);
                            if (!snappedHandholds.contains(h,false))
                                h.desnap();
                        }
                        break;
                    default:
                        break;
                }

            }
        }
        //bounding velocities
        boundBodyVelocities(character);
        if (controller == CONTROLLER_1) {
            if (timestep == 0) {
                glowingHandholds = glowHandholds(character);
                glowingHandholds1 = glowingHandholds;
                glowingHandholds2 = glowingHandholds;
            } else {
                glowingHandholds1 = glowingHandholds;

                glowingHandholds = glowHandholds(character);

                for (int i = 0; i < glowingHandholds2.size; i++) {
                    glowingHandholds2.set(i, glowingHandholds.get(i));
                    if (glowingHandholds.get(i) == null) glowingHandholds2.set(i, glowingHandholds1.get(i));
                }
            }

            //snapLimbsToHandholds(glowingHandholds,character,justReleased);
            snapLimbsToHandholds(glowingHandholds2, character, justReleased);
        }

        else{
            if (timestep == 0) {
                glowingHandholds3 = glowHandholds(character);
                glowingHandholds4 = glowingHandholds3;
                glowingHandholds5 = glowingHandholds3;
            } else {
                glowingHandholds4 = glowingHandholds3;

                glowingHandholds3 = glowHandholds(character);

                for (int i = 0; i < glowingHandholds3.size; i++) {
                    glowingHandholds5.set(i, glowingHandholds4.get(i));
                    if (glowingHandholds3.get(i) == null) glowingHandholds5.set(i, glowingHandholds4.get(i));
                }
            }
           
            //            snapLimbsToHandholds(glowingHandholds,character,justReleased);
            snapLimbsToHandholds(glowingHandholds5, character, justReleased);
        }
        if( id == GAME_MODE && currLevel == LEVEL_TUTORIAL)
        	advanceTutorial(); 
        
        cameraWork();

        dealWithSlipperyAndCrumblyHandholds(character);

        spawnObstacles();

        for (GameObject g : objects) {

            if (g instanceof ObstacleModel &&
                    ((g.getBody().getPosition().y < (canvas.getCamera().position.y - canvas.getWidth()) / scale.y &&
                            g.getBody().getType() != BodyDef.BodyType.StaticBody) || ((ObstacleModel)g).broken)) {
                objects.remove(g);
            }
            if (g instanceof HandholdModel && ((HandholdModel) (g)).getStartPoint() != null) {
                HandholdModel h = (HandholdModel) g;
                h.updateSnapPoints();
                if (withinBounds(h.getBody().getPosition(), h.getEndPoint()) ||
                        withinBounds(h.getBody().getPosition(), h.getStartPoint())) {
                    h.getBody().setLinearVelocity(h.getBody().getLinearVelocity().x * -1, h.getBody().getLinearVelocity().y * -1);
                }
            }
        }
        vector = new Vector2(character.parts.get(CHEST).getVX(), character.parts.get(CHEST).getVY());
        character.updateEnergy(oxygen, 1, vector.len(), gravity.y != 0, gravity.y == 0);
        character.decrementStun();

        if (risingObstacle != null && moved) {
            float progressPercentage = (character.parts.get(CHEST).getPosition().y)/(maxHandhold - cposYAtTime0);
			risingObstacle.setHeight(risingObstacle.getHeight() + risingObstacle.getSpeed()*(progressPercentage+.5f));
            for (PartModel p : character.parts) {
                if (risingObstacle.getHeight() >= p.getPosition().y) {
                    character.setEnergy(0);
                    failed = true;
                }
            }
            float yToSet = Math.min(canvas.getCamera().position.y/character.parts.get(CHEST).drawPositionScale.y, character.parts.get(CHEST).getPosition().y);
            if(risingObstacle.getHeight() < yToSet - DEFAULT_HEIGHT/2 -1){
                risingObstacle.setHeight(yToSet - DEFAULT_HEIGHT/2 -1);
            }
        }

        if (character.getEnergy() <= 0) {
            failed = true;
            for (int e : EXTREMITIES)
                ungrip((ExtremityModel) character.parts.get(e));

        }
        checkHasCompleted(character);
        if (complete) {
        	positionListWrite += "}"; 
        	System.out.println(positionListWrite);
            if (id == RACE_MODE)
                listener.exitScreen(this, EXIT_VICTORY_RACE);
            else listener.exitScreen(this, EXIT_VICTORY);

        }
        if (checkpointTimestep == 0) cposYAtTime0 = character.parts.get(HEAD).getY();

        warningController.update(canvas.getCamera().position.y + canvas.getHeight()/2, gravity.y);
    }


    protected void createAnimation() {

        writeNextStepJsonForAnimation(inx,iny, rinx, riny, nextToPress);
    }


    protected boolean checkIfDied(CharacterModel c) {
    	boolean onscreen = false;
    	for(PartModel p : c.parts){
    		if(p.getPosition().y > 0 && p.getPosition().x < DEFAULT_WIDTH && p.getPosition().x > 0) {
    			onscreen = true;
    		}
    	}
        return !onscreen || (currLevel == LEVEL_SPACE && c.getEnergy() == 0);

    }

    protected boolean checkIfReachedCheckpoint(CharacterModel c) {
        if (lastReachedCheckpoint == checkpoints.size - 1) return false;
        float nextCheckpoint = checkpoints.get(lastReachedCheckpoint + 1);
        return (c.parts.get(CHEST).getY()> nextCheckpoint);
    }

    // ************************************START MISCELLANEOUS*********************************************** //


    protected void dealWithSlipperyAndCrumblyHandholds(CharacterModel c) {
        for(int e : EXTREMITIES){
            ExtremityModel extremity = (ExtremityModel) c.parts.get(e);
            if(extremity.isGripped()){
                extremity.updateGripTime();
                if(extremity.getJoint().getBodyB() == null){
                    ungrip(extremity);
                }
                else{
                    HandholdModel h = (HandholdModel) extremity.getJoint().getBodyB().getFixtureList().get(0).getUserData();
                    if((e == HAND_LEFT || e == HAND_RIGHT)
                            && h.getVelocity() != 0 &&
                            c.parts.get(CHEST).getPosition().sub(extremity.getPosition()).len() > ARM_UNGRIP_LENGTH)
                        ungrip(extremity);
                    else if((e == FOOT_LEFT || e == FOOT_RIGHT) &&
                            h.getVelocity() != 0 &&
                            c.parts.get(CHEST).getPosition().sub(extremity.getPosition()).len() > LEG_UNGRIP_LENGTH)
                        ungrip(extremity);
                    if(extremity.getGripTime() > h.getSlip()*60 && h.getSlip() > 0){
                        ungrip(extremity);
                    }
                    if(extremity.getGripTime() > h.getCrumble()*60 && h.getCrumble() > 0){
                        //TODO add crumble animation
                        ungripAllFrom(h,c);
                        objects.remove(h);
                        h.deactivatePhysics(world);
                    }
                }
            }

        }
    }



    protected void cameraWork() {
        float character1Pos =(character1.parts.get(CHEST).getBody().getPosition().y)*scale.y;

        if (id == RACE_MODE)
            character1Pos = (character2.parts.get(CHEST).getBody().getPosition().y + character1.parts.get(CHEST).getBody().getPosition().y)/2*scale.y;

        float cameraPos = canvas.getCamera().position.y;
        float cameraDist = (character1Pos - cameraPos);
        canvas.setCameraPosition(canvas.getWidth() / 2,
                cameraPos + 0.01f*cameraDist);
        if(canvas.getCamera().position.y < canvas.getHeight()/2){
            canvas.setCameraPosition(canvas.getWidth()/2, canvas.getHeight()/2);
        }
        if(canvas.getCamera().position.y + canvas.getHeight()/2 > levelFormat.getFloat("height")*scale.y){
            canvas.setCameraPosition(canvas.getWidth()/2, levelFormat.getFloat("height")*scale.y-canvas.getHeight()/2);
        }
    }

    protected void ungripAllFrom(HandholdModel h,CharacterModel c){
        for(int e : EXTREMITIES){
            ExtremityModel extremity = (ExtremityModel) c.parts.get(e);
            if(extremity.isGripped() && extremity.getJoint().getBodyB().getFixtureList().get(0).getUserData() == h){
                ungrip(extremity);
            }
        }
    }

    protected boolean withinBounds(Vector2 position, Vector2 target) {
        float xError = Math.abs(position.x - target.x);
        float yError = Math.abs(position.y - target.y);
        return xError < .1f && yError < .1f;
    }



    protected void boundBodyVelocities(CharacterModel c) {
        if (isGripping(HAND_LEFT,c) || isGripping(HAND_RIGHT,c)|| isGripping(FOOT_LEFT,c)|| isGripping(FOOT_RIGHT,c)){
            for (PartModel p:c.parts){
                vector =  p.getLinearVelocity();
                p.setLinearVelocity(boundVelocity(vector));
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
            warningController.addHandholdWarning(h);
        }
        e.grip();
        //This is code to make a tutorialGuide. Will delete when we have settled on one
//        positionListWrite += "\"" + currentStep + "\" : {"; 
//        positionListWrite += "\"x\": " + h.getPosition().x + ","; 
//        positionListWrite += "\"y\": " + h.getPosition().y + ",";
//        positionListWrite += "\"e\": " + character1.parts.indexOf(e, true) + "},"; 
//        currentStep++; 
    }
    protected void setJointMotor(RevoluteJointDef jd, float motorSpeed, float maxTorque) {
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
     * spawn, 2) the top of the screen must be lower than the bottom of the obstacle zone, 3) The character1 must be in the
     * level chunk that contains that obstacle zone.
     */
    protected void spawnObstacles(){
        float cpos = character1.parts.get(CHEST).getBody().getPosition().y;
        if (id == RACE_MODE)
            cpos = Math.max(character1.parts.get(CHEST).getBody().getPosition().y,character2.parts.get(CHEST).getBody().getPosition().y);
        for(ObstacleZone oz : obstacles){
            float viewHeight = (canvas.getCamera().position.y + canvas.getHeight()/2) / scale.y;

            if(oz.ticksSinceLastSpawn == 0 && viewHeight < oz.getBounds().y && cpos > oz.getMinSpawnHeight()){
                obstacle = new ObstacleModel(oz.getObstacleTexture(), 1f, scale);
                oz.setObstX(oz.getBounds().x + rand.nextFloat()*oz.getBounds().width);
                oz.setObstY(oz.getBounds().y + rand.nextFloat()*oz.getBounds().height);
                obstacle.setX(oz.getObstX());
                obstacle.setY(oz.getObstY());
                if(levelName.equals("space")) {
                    obstacle.setVY((float) (Math.random()*-10));
//                    obstacle.setVY((float) (-5));
                    obstacle.setVX((float) (Math.random()*4-2));
//                    obstacle.setVX((float) 0);
                    oz.setObstVY(obstacle.getVY());
                }

                oz.setObstacle(obstacle);
                queuedObstacles.add(oz);
                warningController.addObstacleWarning(oz, maxLevelHeight);
            }

            if ((cpos >= oz.getMinSpawnHeight() || oz.isTriggered()) && (viewHeight < oz.getBounds().y)){
                oz.setTriggered(true);
                oz.incrementSpawnTimer();
                spawnObstacle(oz);

            }else{
                oz.ticksSinceLastSpawn = 0;
                oz.setTriggered(false);
            }
        }
    }

    protected void spawnObstacle(ObstacleZone oz) {
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
    // ************************************END OBSTACLES*********************************************** //

    /**
     * @param vect - current linear velocity vector of any body part
     * @return bounded linear velocity vector
     * @author Jacob
     */
    protected Vector2 boundVelocity(Vector2 vect) {
        if (Math.abs(vect.x) > 1 || Math.abs(vect.y) > 1) {
        }
        vect.x = (vect.x>0) ? Math.min(PART_MAX_X_VELOCITY,vect.x) : Math.max(-1 * PART_MAX_X_VELOCITY,vect.x);
        vect.y = (vect.y>0) ? Math.min(PART_MAX_Y_VELOCITY,vect.y) : Math.max(-1 * PART_MAX_Y_VELOCITY,vect.y);


        return vect;
    }

    /**
     * this method glows any handholds close enough for the person's extremity to grab.
     * //TODO there will be a possible issue if person at full extension and snaps to a handhold out of their reach.
     * //	 * implement a calculation which says that handhold distance <= MAX_ARM_DIST or MAX_LEG_DIST.
     * @author Jacob
     */
    protected Array<HandholdModel> glowHandholds(CharacterModel c) {
        Array<HandholdModel> newGlowingHandholds = new Array<HandholdModel>();
        for (counterInt = 0; counterInt<EXTREMITIES.length; counterInt++) {
            HandholdModel closest = null;
            double dist;
            double closestdist = HANDHOLD_SNAP_RADIUS + 1;

            for (GameObject obj : objects) {
                if (obj.getType() == GameObject.ObjectType.HANDHOLD) {
                    HandholdModel h = (HandholdModel) obj;
                    if (counterInt<1 && firstUpdate)
                        h.unglow();

                    for (Vector2 snapPoint : h.snapPoints) {
                        dist = distanceFrom(EXTREMITIES[counterInt], snapPoint,c);
                        if (dist<=HANDHOLD_SNAP_RADIUS) {
                            closest = closestdist > dist ? h:closest;
                            closestdist = closestdist > dist ? dist:closestdist;

                        }
                    }

                }
            }

            newGlowingHandholds.add(closest);

        }
        for (HandholdModel g : newGlowingHandholds){
            if (g != null) g.glow();
        }
        return newGlowingHandholds;

    }

    /**
     * snaps any released limbs (limbs player controlled last timestep but no longer does) to closest handhold
     * if possible
     * @author Jacob
     * @param hs - handholds in game
     */
    protected void snapLimbsToHandholds(Array<HandholdModel> hs,CharacterModel c,Array<Integer> jr) {

        if (jr.contains(FOOT_LEFT,false) || timestep < 2)
            snapIfPossible(FOOT_LEFT, hs,c, 0);
        if (jr.contains(FOOT_RIGHT,false)|| timestep < 2)
            snapIfPossible(FOOT_RIGHT, hs,c, 1);
        if (timestep <2 || jr.contains(HAND_LEFT,false))
            snapIfPossible(HAND_LEFT, hs,c, 2);
        if (jr.contains(HAND_RIGHT,false) || timestep < 2 )
            snapIfPossible(HAND_RIGHT, hs,c, 3);

    }

    /**
     * snaps limb to handhold if possible.
     * @param limb - limb to snap if possible
     * @author Jacob
     * */
    protected void snapIfPossible(int limb, Array<HandholdModel> hs, CharacterModel c, int ind) {

        HandholdModel closest = hs.get(ind);
        if (closest !=null){
            Vector2 closestSnapPoint = closest.snapPoints.first();
//            c.parts.get(limb).setPosition(closestSnapPoint);
            ((ExtremityModel) c.parts.get(limb)).grip();
            c.parts.get(limb).setPosition(closest.snapPoints.first(), c.parts.get(limb).getAngle());

            grip(((ExtremityModel) c.parts.get(limb)), closest);
            closest.snap();
            switch(limb){
                case FOOT_LEFT: snappedHandholds.set(0,closest); break;
                case FOOT_RIGHT: snappedHandholds.set(1,closest);break;
                case HAND_LEFT: snappedHandholds.set(2,closest);break;
                case HAND_RIGHT: snappedHandholds.set(3,closest);break;
                default: break;
            }
        }

    }

    protected double distanceFrom(int limb, Vector2 snapPoint, CharacterModel c) {
        vector = new Vector2 (c.parts.get(limb).getPosition().sub(snapPoint));
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    public PooledList<GameObject> getGameObjects(){
        return objects;
    }

    public TextureRegion getGameBackground() { return background; }

    protected boolean isGripping(int part,CharacterModel c) {
        return ((ExtremityModel)(c.parts.get(part))).isGripped();
    }
    
    protected void advanceTutorial(){
    	ExtremityModel e = (ExtremityModel) character1.parts.get(currentTutorialStep.getInt("e")); 
    	float x = currentTutorialStep.getFloat("x"); 
    	float y = currentTutorialStep.getFloat("y"); 
    	Vector2 h = new Vector2(x,y); 
    	if(e.isGripped() && ((HandholdModel) e.getJoint().getBodyB().getFixtureList().get(0).getUserData()).getPosition().equals(h)){
            currentStep++; 
            currentTutorialStep = tutorialGuide.get(currentStep); 
        }
    }
    
    protected void checkHasCompleted(CharacterModel c){
        float viewHeight = canvas.getHeight()*4f/5f / scale.y;

        CharacterModel c0;
        if (c == character1) {
            c0 = character2;
        }
        else{
            c0 = character1;
        }
        float minPart = Math.max( c0.parts.get(HAND_RIGHT).getPosition().y, c0.parts.get(HAND_LEFT).getPosition().y);
        float minPart2 = Math.max(c0.parts.get(FOOT_RIGHT).getPosition().y, c0.parts.get(FOOT_LEFT).getPosition().y );
        minPart = Math.max(minPart,minPart2);
        this.complete =  c.parts.get(HAND_RIGHT).getPosition().y >= levelFormat.getFloat("height")
                ||c.parts.get(HAND_LEFT).getPosition().y >= levelFormat.getFloat("height")
                ||c.parts.get(FOOT_RIGHT).getPosition().y >= levelFormat.getFloat("height")
                ||c.parts.get(FOOT_LEFT).getPosition().y >= levelFormat.getFloat("height")

                || c.parts.get(HAND_RIGHT).getPosition().y >= minPart + viewHeight
                ||c.parts.get(HAND_LEFT).getPosition().y >= minPart + viewHeight
                ||c.parts.get(FOOT_RIGHT).getPosition().y >= minPart + viewHeight
                ||c.parts.get(FOOT_LEFT).getPosition().y >= minPart + viewHeight
                ||c0.parts.get(HEAD).getPosition().y < 0;
    }

    public void draw() {
        canvas.clear();
        canvas.begin();
        vector = character1.parts.get(HEAD).getPosition();
        SharedMethods.drawBackgrounds(canvas,ground,background,midground,foreground,tile,edge,currLevel);

        canvas.end();
        canvas.beginHUD();
        float hudMidX = -canvas.getWidth() * 0.4f;
        float timerTextY = canvas.getHeight() * 0.4f;
        float timerTimeY = timerTextY - canvas.getHeight() * 0.045f;
        int seconds = timestep/60;
        String minuteString = (seconds/60 >= 10) ? seconds/60 + "" : ("0"+seconds/60);
        String secondString = (seconds%60 >= 10) ? seconds%60 + "" : ("0"+seconds%60);
        String time = minuteString+":"+secondString;
        SharedMethods.drawUI(canvas,0, UI,.7f);
        canvas.drawTextCentered("Time", mastodon, hudMidX, timerTextY);
        canvas.drawTextCentered(time, kremlin, hudMidX, timerTimeY);
        if (id == RACE_MODE){
            SharedMethods.drawUI(canvas,canvas.getWidth()*4/5, UI,.7f);
        	canvas.drawTextCentered("Time", mastodon, -hudMidX, timerTextY);
            canvas.drawTextCentered(time, kremlin, -hudMidX, timerTimeY);
        }

        float a = (vector.y - cposYAtTime0)/(maxHandhold - cposYAtTime0);
        if (timestep%60 == 0 && character1.getEnergy() != 0)
            progressLevel = (int)(a*20);
        SharedMethods.drawProgress(canvas,progress,progressLevel,0, 0);
        energyLevel = Math.abs((int) Math.ceil(character1.getEnergy() / 10f));
        flashing1 = SharedMethods.drawEnergy(canvas, character1, energyTextures, lowEnergyHalo, energyLevel, 0, 0, flashing1);
        canvas.draw(gauges, Color.WHITE, 0, 0, canvas.getWidth()*1/5, canvas.getHeight());
        canvas.drawTextCentered("Progress", mastodonS, -845*canvas.getWidth()/SCREEN_WIDTH, canvas.getHeight() * 0.29f);
        canvas.drawTextCentered("Stamina", mastodonS,-685*canvas.getWidth()/SCREEN_WIDTH , canvas.getHeight() * 0.29f);
        canvas.drawTextCentered(levelName, mastodon, hudMidX, -canvas.getHeight() * 0.375f);
        if (id == RACE_MODE) {
            vector = character2.parts.get(HEAD).getPosition();
            a = (vector.y - cposYAtTime0) / (maxHandhold - cposYAtTime0);
            if (timestep % 60 == 0 && character2.getEnergy() != 0)
            	progressLevel = (int)(a*20);
            SharedMethods.drawProgress(canvas, progress, progressLevel, canvas.getWidth()*4/5, 0);
            energyLevel = Math.abs((int) Math.ceil(character2.getEnergy() / 10f));
            flashing2 = SharedMethods.drawEnergy(canvas, character2, energyTextures, lowEnergyHalo, energyLevel, canvas.getWidth() * 4 / 5, 0, flashing2);
            canvas.draw(gauges, Color.WHITE, canvas.getWidth()*4/5, 0, canvas.getWidth()*1/5, canvas.getHeight());
            canvas.drawTextCentered("Stamina", mastodonS, 840*canvas.getWidth()/SCREEN_WIDTH, canvas.getHeight() * 0.29f);
            canvas.drawTextCentered("Progress", mastodonS, 680*canvas.getWidth()/SCREEN_WIDTH , canvas.getHeight() * 0.29f);
            canvas.drawTextCentered(levelName, mastodon, -hudMidX, -canvas.getHeight() * 0.375f);
        }
        canvas.endHUD();
        canvas.begin();
        if (currLevel == LEVEL_TUTORIAL)
            canvas.draw(tutorialOverlay, Color.WHITE, canvas.getWidth()/4, canvas.getHeight()/8, canvas.getWidth()/2, levelFormat.getFloat("height")*scale.y);
        counterInt = 0;

        while (id == GAME_MODE && counterInt <= lastReachedCheckpoint){
            canvas.draw(RUSSIAN_FLAG, Color.WHITE, canvas.getWidth()/4,checkpoints.get(counterInt)*scale.y - canvas.getHeight()/2, canvas.getWidth()/2, canvas.getHeight());
            counterInt++;
        }

        canvas.end();

        canvas.begin();
        if(currLevel != LEVEL_SKY && currLevel != LEVEL_SPACE){
            for (int i = 0; i < character1.parts.size; i++){
                character1.parts.get(i).drawShadow(shadowTextures[i], canvas);
            }
            if (id == RACE_MODE){
                for (int i = 0; i < character2.parts.size; i++){
                character2.parts.get(i).drawShadow(shadowTextures[i], canvas);
                }
            }
        }
        for (GameObject obj : objects) obj.draw(canvas);
        if (tutorialToggle1)
            SharedMethods.drawToggles(canvas, character1, input1, tutorialTextures,  scale);
        if (id == RACE_MODE && tutorialToggle2)
            SharedMethods.drawToggles(canvas, character2, input2, tutorialTextures,  scale);



        canvas.end();

        canvas.begin();
       

        if (risingObstacle != null) {
            float lavaOrigin = risingObstacle.getHeight() * scale.y -
                    canvas.getHeight();
            canvas.draw(lavaGlowTexture, Color.WHITE, canvas.getWidth() * 0.17f, lavaOrigin+canvas.getHeight(), canvas.getWidth() * 0.66f, canvas.getHeight());
            canvas.draw(risingObstacle.getTexture(), Color.WHITE, canvas.getWidth() * 0.17f, lavaOrigin, canvas.getWidth() * 0.66f, canvas.getHeight());
            while((lavaOrigin-=canvas.getHeight())>0){
            	canvas.draw(lavaContTexture, Color.WHITE, canvas.getWidth() * 0.17f, lavaOrigin, canvas.getWidth() * 0.66f, canvas.getHeight());
            }
        }
        
        if (id == GAME_MODE && currLevel == LEVEL_TUTORIAL){
	        Vector2 characterPos = character1.parts.get(currentTutorialStep.getInt("e")).getPosition(); 
	        canvas.draw(tutorialRing, Color.WHITE, characterPos.x*scale.x - 25, characterPos.y * scale.y - 25,50,50);
	        canvas.draw(tutorialRing, Color.WHITE, currentTutorialStep.getFloat("x") * scale.x - 25, currentTutorialStep.getFloat("y") * scale.y - 25, 50,50);
        }
        canvas.end();

        canvas.begin();
        if (warningController != null) warningController.draw(canvas);
        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for(GameObject obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }

        if (isPaused) pauseMode.draw(canvas);
        else if (isDead) deadMode.draw(canvas);
        else if (isVictorious) victoryMode.draw(canvas);
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
    
	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			if (preUpdate(delta)) {
				if(isDead)
					deadMode.update(delta, listener );
				else if(isVictorious){
					 if (id == GAME_MODE && !writtenToFile) {
			                try {
			                    makeJsonForAnimation();

			                    writeJsonToFile();

			                    writtenToFile = true;
			                } catch (Exception e) {
			                }
			            }
					victoryMode.update(delta, listener,id!=GAME_MODE);
				}
				else if(isPaused)
					pauseMode.update(delta,  listener,id==RACE_MODE);
				else{
					this.update(delta);
					postUpdate(delta);
				}
			}
			draw();
		}
	}

    public int getCurrLevel(){
        return currLevel;
    }

    public int getNextLevel(){
        if (currLevel != NUM_LEVELS - 1){
            return currLevel + 1;
        }
        return currLevel;
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        pauseMode.reset();
        isPaused = true;
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // Shouldn't need to do anything to resume for now, can change focus of screen
        isPaused = false;
    }

    public void dead(){
        deadMode.reset();
        isDead = true;
    }

    public void victorious(){

        victoryMode.reset();
        isVictorious = true;
    }

    @Override
    public void resize(int width, int height) {
    }

    public void dispose(){
        font.dispose();
        super.dispose();
    }
}

