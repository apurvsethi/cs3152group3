package beigegang.mountsputnik;
import com.badlogic.gdx.math.Vector2;

public final class Constants {

	/**Constants for the window size*/
	public static final int GAME_WIDTH = 1920;
	public static final int GAME_HEIGHT = 1080;
	/**Game block size in pixels*/
	public static final float BLOCK_SIZE = 48;
	
	/**Total grid size */
	public static final float X_BLOCKS = GAME_WIDTH / BLOCK_SIZE;
	public static final float Y_BLOCKS = GAME_HEIGHT / BLOCK_SIZE;
	/**Max number of blocks for width of climbing wall*/
	public static final int WALL_WIDTH = 30;
	
	/** pixel radius around a handhold
	 * if character extremity is within this radius and handhold is
	 * within max reach, releasing the trigger / bumper will snap
	 * to the handhold*/
	public static final int SNAP_RADIUS = 30;
	
	/** The number of handholds on the given level. TODO: remove. For testing purposes only  */
	public static final int HANDHOLD_NUMBER = 5; 
	
	/** Initial onscreen location of the head */
	public static final float HEAD_X = GAME_WIDTH / 2;
	public static final float HEAD_Y = GAME_HEIGHT * 0.75f;

	public static final float DEG_TO_RAD = 0.0174532925199432957f;
	public static final float RAD_TO_DEG = 57.295779513082320876f;

	/** Parameters to pass into extremities*/
	//TODO: determine actual push / pull factor through playtesting
	public static final float HAND_PUSH = 100.0f;
	public static final float HAND_PULL = 100.0f;
	public static final float FOOT_PUSH = 100.0f;
	public static final float FOOT_PULL = 100.0f;
	public static final float MAX_PULLFORCE_LEG = 150f;
	public static final float MAX_PULLFORCE_ARM = 150f;
	public static final float MAX_PUSHFORCE_ARM = 100f;
	public static final float MAX_PUSHFORCE_LEG = 250f;
	public static final float CONSTANT_X_FORCE = 200f;
	/** Array of the number of animation frames for each part*/
	public static final int[] PART_FRAMES = {1,1,1,1,1,1,1,2,2,1,1,1,1,1,1};
	
	/** The number of DISTINCT body parts */
	public static final int BODY_TEXTURE_COUNT = 15;
	
	/** Indices of specific part locations in the array*/
//	public static final int PART_NONE = -1;
//	public static final int PART_BODY = 0;
//	public static final int PART_HEAD = 1;
//	public static final int PART_LEFT_ARM  = 2;
//	public static final int PART_RIGHT_ARM = 3;
//	public static final int PART_LEFT_FOREARM  = 4;
//	public static final int PART_RIGHT_FOREARM = 5;
//	public static final int PART_LEFT_THIGH  = 6;
//	public static final int PART_RIGHT_THIGH = 7;
//	public static final int PART_LEFT_SHIN  = 8;
//	public static final int PART_RIGHT_SHIN = 9;
//	public static final int PART_LEFT_HAND = 10;
//	public static final int PART_RIGHT_HAND = 11;
//	public static final int PART_LEFT_FOOT = 12;
//	public static final int PART_RIGHT_FOOT = 13;
	public static final int HEAD = 0;
	public static final int CHEST = 1;
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

	public static final int[] EXTREMITIES = new int[]{FOOT_LEFT,FOOT_RIGHT,HAND_LEFT,HAND_RIGHT};
	public static final String[] ENAMES = new String[]{"HEAD","CHEST","HIPS","ARM_LEFT","ARM_RIGHT","FOREARM_LEFT","FOREARM_RIGHT","HAND_LEFT","HAND_RIGHT","THIGH_LEFT","THIGH_RIGHT","SHIN_LEFT","SHIN_RIGHT","FOOT_LEFT","FOOT_RIGHT"};
	public static final float PULL_A = 1;
	public static final float PULL_C = 0;
	public static final float PUSH_A = 1;
	public static final float PUSH_C = 0;
	public static final float DAMPENING_Y = .2f;
	public static final float DAMPENING_X = .2f;

	/**Giant monster section of hard coded offsets. Disgusting, but necessary*/
	//TODO: once character assets complete, determine actual offsets
	/** Face offset*/
	public static final float HEAD_OFFSET = -75;
	/** Chest offset from face*/
	public static final float CHEST_HEAD_OFFSET   = -50;
	/** Chest offset from hips*/
	public static final float CHEST_HIP_OFFSET   = -50;
	/** Chest x offset from arm*/
	public static final float CHEST_X_ARM_OFFSET   = 10;
	/** Chest y offset from arm*/
	public static final float CHEST_Y_ARM_OFFSET   = -25;
	/** Arm x offset from chest*/
	public static final float ARM_X_CHEST_OFFSET = 75;
	/** Arm y offset from chest*/
	public static final float ARM_Y_CHEST_OFFSET = -10;
	/** Arm  x offset from forearm*/
	public static final float ARM_X_FOREARM_OFFSET = 0;
	/** Arm  y offset from forearm*/
	public static final float ARM_Y_FOREARM_OFFSET = -50;
	/** Forearm  x offset from arm*/
	public static final float FOREARM_X_ARM_OFFSET = 0;
	/** Forearm  y offset from arm*/
	public static final float FOREARM_Y_ARM_OFFSET = -50;
	/** Forearm  x offset from hand*/
	public static final float FOREARM_X_HAND_OFFSET = 0;
	/** Forearm  y offset from hand*/
	public static final float FOREARM_Y_HAND_OFFSET = -25;
	/**Hand x offset from forearm*/
	public static final float HAND_X_OFFSET = 0;
	/**Hand y offset from forearm */
	public static final float HAND_Y_OFFSET = -25;
	/** Hip offset from chest*/
	public static final float HIP_CHEST_OFFSET = -50;
	/** Hip x offset from thigh */
	public static final float HIP_X_THIGH_OFFSET  = 20;
	/** Hip y offset from thigh */
	public static final float HIP_Y_THIGH_OFFSET  = -75;
	/** Thigh x offset from hip */
	public static final float THIGH_X_HIP_OFFSET  = 20;
	/** Thigh y offset from hip */
	public static final float THIGH_Y_HIP_OFFSET  = -50;
	/** Thigh x offset from shins */
	public static final float THIGH_X_SHIN_OFFSET  = 0;
	/** Thigh y offset from shins */
	public static final float THIGH_Y_SHIN_OFFSET  = -50;
	/** Shin x offset from thigh */
	public static final float SHIN_X_THIGH_OFFSET    = 0;
	/** Shin y offset from thigh */
	public static final float SHIN_Y_THIGH_OFFSET    = -50;
	/** Shin x offset from foot */
	public static final float SHIN_X_FOOT_OFFSET    = 0;
	/** Shin y offset from foot */
	public static final float SHIN_Y_FOOT_OFFSET    = -50;
	/** Foot x offset from shin */
	public static final float FOOT_X_OFFSET    = 30;
	/** Foot x offset from shin */
	public static final float FOOT_Y_OFFSET    = -35;

	public static final float MAX_ARM_DIST = ARM_Y_CHEST_OFFSET + ARM_Y_FOREARM_OFFSET + FOREARM_Y_ARM_OFFSET + FOREARM_Y_HAND_OFFSET + HAND_Y_OFFSET;
	public static final float MAX_LEG_DIST = THIGH_Y_HIP_OFFSET + THIGH_Y_SHIN_OFFSET + SHIN_Y_THIGH_OFFSET + SHIN_Y_FOOT_OFFSET + FOOT_Y_OFFSET * 2;

	//TODO: Figure out what these should be
	/** Constant Multiplier on energy gain */
	public static final float ENERGY_GAIN_MULTIPLIER = 1;
	/** Constant Multiplier on energy loss */
	public static final float ENERGY_LOSS_MULTIPLIER = 1;
	/** Constant of energy loss */
	public static final float ENERGY_LOSS = .1f;
	/** Base energy gain */
	public static final float BASE_ENERGY_GAIN = 1f;

	public static final float TORSO_OFFSET   = 3.8f;
	/** Y-distance between torso center and arm center */
	public static final float ARM_YOFFSET    = 1.75f;
	/** X-distance between torso center and arm center */
	public static final float ARM_XOFFSET    = 3.15f;
	/** Distance between center of arm and center of forearm */
	public static final float FOREARM_OFFSET = 2.75f;
	/** X-distance from center of torso to center of leg */
	public static final float THIGH_XOFFSET  = 0.75f;
	/** Y-distance from center of torso to center of thigh */
	public static final float THIGH_YOFFSET  = 3.5f;
	/** Distance between center of thigh and center of shin */
	public static final float SHIN_OFFSET    = 2.75f;
	/** Distance between center of forearm and center of hand */
	public static final float HAND_OFFSET    = 1.80f;
	/** Distance between center of shin and center of foot */
	public static final float FOOT_OFFSET    = 1.80f;
	/** max X velocity of any extremity. used for bounding purposes */
	public static final float MAX_X_VELOCITY = 20f;
	/** max Y velocity of any extremity. used for bounding purposes */
	public static final float MAX_Y_VELOCITY = 20f;


















}




