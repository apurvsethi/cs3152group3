package beigegang.mountsputnik;

public final class Constants {
	/** Screen pixel width and height */
	public static final int SCREEN_WIDTH = 1920;
	public static final int SCREEN_HEIGHT = 1080;
	public static final int GAME_MODE = -1;
	public static final int RACE_MODE = 1;
	public static final int CONTROLLER_1 = 0;
	public static final int CONTROLLER_2 = 1;
	/** Placement of each screen within controllers of GameEngine */
	public static final int LOADING_SCREEN = 0;
	public static final int MENU_SCREEN = 1;
	public static final int GAME_SCREEN = 2;
	public static final int RACE_SCREEN = 3;

	/** Exit codes */
	public static final int EXIT_GAME_RESTART_LEVEL = 0;
	public static final int EXIT_GAME_NEXT_LEVEL = 1;
	public static final int EXIT_MENU = 2;
	public static final int EXIT_GAME_RESUME_LEVEL = 3;
	public static final int EXIT_PAUSE = 4;
	public static final int EXIT_QUIT = 5;
	public static final int EXIT_LEVEL_SELECT = 6;
	public static final int EXIT_SETTINGS = 7;
	public static final int EXIT_GAME_RESTART_LAST_CHECKPOINT = 8;
	public static final int EXIT_DIED = 9;
	public static final int EXIT_VICTORY = 10;
	public static final int EXIT_RACE_LEVEL_SELECT = 11;
	public static final int EXIT_VICTORY_RACE = 12;
	public static final int EXIT_GAME_RESTART_RACE_LEVEL = 13;
	public static final int EXIT_RACE_DIED = 14;
	public static final int EXIT_GAME_NEXT_RACE_LEVEL = 15;
	public static final int EXIT_RACE_PAUSE = 16;
	public static final int EXIT_GAME_RESUME_RACE_LEVEL = 17;

	/**Level codes*/
	public static final int NUM_LEVELS = 7;
	public static final int LEVEL_TUTORIAL = 0;
	public static final int LEVEL_CANYON = 1;
	public static final int LEVEL_SNOWY_MOUNTAIN = 4;
	public static final int LEVEL_WATERFALL = 2;
	public static final int LEVEL_VOLCANO = 3;
	public static final int LEVEL_SKY = 5;
	public static final int LEVEL_SPACE = 6;

	/**Menu Codes*/
	public static final int MAIN_MENU = 0;
	public static final int LEVEL_SELECT = 1;
	public static final int SETTINGS = 2;


	/** The amount of time for a physics engine step. */
	public static final float WORLD_STEP = 1 / 60.0f;
	/** Number of velocity iterations for the constrain solvers */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	public static final int WORLD_POSIT = 2;

	/** Width of the game world in Box2d units */
	public static final float DEFAULT_WIDTH = 32.0f;
	/** Height of the game world in Box2d units */
	public static final float DEFAULT_HEIGHT = 18.0f;
	/** The default value of gravity (going down) */
	protected static final float DEFAULT_GRAVITY = -5f;

	public static final int HANDHOLD_SNAP_RADIUS = 1;

	public static final int MENU_CHANGE_COOLDOWN = 12;

	/** Scrolling speeds for parallax. Higher floats scroll SLOWER*/
	public static final float MIDGROUND_SCROLL = 0.90f;
	public static final float FOREGROUND_SCROLL = 0.05f;

	public static final float DEG_TO_RAD = 0.0174532925199432957f;
	public static final float RAD_TO_DEG = 57.295779513082320876f;

	public static final float CONSTANT_X_FORCE = 70f;

	public static final int[] BODY_PART_ANIMATION_FRAMES = {1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1};
	public static final int BODY_TEXTURE_COUNT = 15;

	/** Indices of specific part locations in the array */
	public static final int CHEST = 0;
	public static final int HEAD = 1;
	public static final int HIPS = 2;
	public static final int ARM_LEFT = 3;
	public static final int ARM_RIGHT = 4;
	public static final int FOREARM_LEFT = 5;
	public static final int FOREARM_RIGHT = 6;
	public static final int HAND_LEFT = 7;
	public static final int HAND_RIGHT = 8;
	public static final int THIGH_LEFT = 9;
	public static final int THIGH_RIGHT = 10;
	public static final int SHIN_LEFT = 11;
	public static final int SHIN_RIGHT = 12;
	public static final int FOOT_LEFT = 13;
	public static final int FOOT_RIGHT = 14;
	public static final int NONE = -1;

	public static final int[] EXTREMITIES = new int[]{FOOT_LEFT, FOOT_RIGHT, HAND_LEFT, HAND_RIGHT};

	public static final float CHARACTER_DRAW_SIZE_SCALE = 0.3f;
	public static final float CHARACTER_DRAW_SIZE_SCALE_TWO = 0.3f;
	/** Hard coded offsets.
	 *  Style: part_x/y_connect, meaning part x/y offset from connecting
	 *  If connect does not exist, then part only connects to 1 piece
	 */
	//TODO: once character assets complete, determine actual offsets
	public static final float HEAD_OFFSET = -2.2f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float CHEST_HEAD_OFFSET = 2.2f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float CHEST_HIP_OFFSET = -1.8f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float CHEST_X_ARM_OFFSET = -2.2f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float CHEST_Y_ARM_OFFSET = 0.75f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float ARM_X_CHEST_OFFSET = 2.2f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float ARM_Y_CHEST_OFFSET = 0 * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float ARM_X_FOREARM_OFFSET = -1.7f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float ARM_Y_FOREARM_OFFSET = 0 * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float FOREARM_X_ARM_OFFSET = 2.1f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float FOREARM_Y_ARM_OFFSET = 0 * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float FOREARM_X_HAND_OFFSET = -2.4f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float FOREARM_Y_HAND_OFFSET = 0 * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float HAND_X_OFFSET = 0.6f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float HAND_Y_OFFSET = 0 * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float HIP_CHEST_OFFSET = 1.8f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float HIP_X_THIGH_OFFSET = -1.1f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float HIP_Y_THIGH_OFFSET = -2.1f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float THIGH_X_HIP_OFFSET = 0 * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float THIGH_Y_HIP_OFFSET = 2.1f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float THIGH_X_SHIN_OFFSET = 0 * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float THIGH_Y_SHIN_OFFSET = -2.3f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float SHIN_X_THIGH_OFFSET = 0 * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float SHIN_Y_THIGH_OFFSET = 2.4f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float SHIN_X_FOOT_OFFSET = -0.2f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float SHIN_Y_FOOT_OFFSET = -2.0f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float FOOT_X_OFFSET = 0.7f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float FOOT_Y_OFFSET = 0.7f * CHARACTER_DRAW_SIZE_SCALE_TWO;
	public static final float SHADOW_X_OFFSET = 0.2f * CHARACTER_DRAW_SIZE_SCALE_TWO; 
	public static final float SHADOW_Y_OFFSET = -0.4f * CHARACTER_DRAW_SIZE_SCALE_TWO;

	public static final float ARM_UNGRIP_LENGTH = 3.75f;
	public static final float LEG_UNGRIP_LENGTH = 5.2f;

	public static final float PART_MAX_X_VELOCITY = -DEFAULT_GRAVITY;
	public static final float PART_MAX_Y_VELOCITY = -DEFAULT_GRAVITY;

	public static short PART_BITS = 8;
	public static short OBSTACLE_BITS = 4;
	public static short HANDHOLD_BITS = 0;
	/** amount of time (in 1/60 seconds) to put the warning up for an obstacle before it spawns.
	 * currently at 3 seconds */
	public static int TIME_TO_WARN = 180;
}
