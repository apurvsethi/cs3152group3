package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class CharacterModel extends GameObject{

	/** An array of all of the body's parts*/
	protected Array<PartModel> parts;
	/** An array of all of the body's joints*/
	protected Array<Joint> joints;
	/** Cache vector for organizing body parts */
	private Vector2 partCache = new Vector2();
	/** Texture assets for the body parts */
	private TextureRegion[] partTextures;
	
	/** Initial onscreen location of the head */
	//TODO: determine actual position of head
	private static final float HEAD_X = 4.0f;
	private static final float HEAD_Y = 4.0f;
	
	/** Parameters to pass into extremities*/
	//TODO: determine actual push / pull factor through playtesting
	private static final float HAND_PUSH = 1.0f;
	private static final float HAND_PULL = 1.0f;
	private static final float FOOT_PUSH = 1.0f;
	private static final float FOOT_PULL = 1.0f;
	
	/** The number of DISTINCT body parts */
	private static final int BODY_TEXTURE_COUNT = 10;
	
	/** Indices of specific part locations in the array*/
	private static final int HEAD = 0;
	private static final int CHEST = 1;
	private static final int ABDOMEN = 2;
	private static final int HIPS = 3;
	private static final int ARM_LEFT = 4;
	private static final int ARM_RIGHT = 5;
	private static final int FOREARM_LEFT = 6;
	private static final int FOREARM_RIGHT = 7;
	private static final int HAND_LEFT = 8;
	private static final int HAND_RIGHT = 9;
	private static final int THIGH_LEFT = 10;
	private static final int THIGH_RIGHT = 11;
	private static final int SHIN_LEFT = 12;
	private static final int SHIN_RIGHT = 13;
	private static final int FOOT_LEFT = 14;
	private static final int FOOT_RIGHT = 15;
	private static final int NONE = -1;
	
	/**
	 * Returns the texture index for the given body part 
	 *
	 * As some body parts are symmetrical, we reuse textures.
	 *
	 * @returns the texture index for the given body part 
	 */
	private static int partToAsset(int part) {
		switch (part) {
		case HEAD:
			return 0;
		case CHEST:
			return 1;
		case ABDOMEN: 
			return 2;
		case HIPS: 
			return 3;
		case ARM_LEFT:
		case ARM_RIGHT: 
			return 4;
		case FOREARM_LEFT:
		case FOREARM_RIGHT: 
			return 5;
		case HAND_LEFT: 
		case HAND_RIGHT: 
			return 6;
		case THIGH_LEFT: 
		case THIGH_RIGHT: 
			return 7;
		case SHIN_LEFT: 
		case SHIN_RIGHT: 
			return 8;
		case FOOT_LEFT: 
		case FOOT_RIGHT: 
			return 9;
		default:
			return -1;
		}
	}
	
	//TODO: once character assets complete, determine actual offsets
	/** Distance between chest center and face center */
	private static final float CHEST_OFFSET   = 3.8f;
	/** Distance between abdomen center and chest center*/
	private static final float ABDOMEN_OFFSET = 3.8f;
	/** Distance between hip center and abdomen center*/
	private static final float HIP_OFFSET = 3.8f;
	/** Y-distance between chest center and arm center */
	private static final float ARM_YOFFSET    = 1.75f;  
	/** X-distance between chest center and arm center */
	private static final float ARM_XOFFSET    = 3.15f;  
	/** Distance between center of arm and center of forearm */
	private static final float FOREARM_OFFSET = 2.75f; 
	/** Distance between center of forearm and center of hand */
	private static final float HAND_OFFSET = 2.75f; 
	/** X-distance from center of hips to center of leg */
	private static final float THIGH_XOFFSET  = 0.75f;  
	/** Y-distance from center of hips to center of thigh */
	private static final float THIGH_YOFFSET  = 3.5f;  
	/** Distance between center of thigh and center of shin */
	private static final float SHIN_OFFSET    = 2.75f;
	/** Distance between center of shin and center of foot */
	private static final float FOOT_OFFSET    = 2.75f;
	
	
	@Override
	public ObjectType getType() {
		return ObjectType.CHARACTER;
	}
	
    /**
     * Sets the drawing scale for this physics object
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param x the x-axis scale for this physics object
     * @param y the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y, World w) {
    	super.setDrawScale(x,y);
    	
    	if (partTextures != null && parts.size == 0) {
    		init(w);
    	}
    }
	
	/**
     * Returns the array of textures for the individual body parts.
     *
     * Modifying this array will have no affect on the physics objects.
     *
     * @return the array of textures for the individual body parts.
     */
    public TextureRegion[] getPartTextures() {
    	return partTextures;
    }
    
    /**
     * Sets the array of textures for the individual body parts.
     *
     * The array should be BODY_TEXTURE_COUNT in size.
     *
     * @param textures the array of textures for the individual body parts.
     */
    public void setPartTextures(TextureRegion[] textures, World w) {
    	assert textures != null && textures.length > BODY_TEXTURE_COUNT : "Texture array is not large enough";
    	
    	partTextures = new TextureRegion[BODY_TEXTURE_COUNT];
    	System.arraycopy(textures, 0, partTextures, 0, BODY_TEXTURE_COUNT);
    	if (parts.size == 0) {
    		init(w);
    	} else {
    		for(int ii = 0; ii <=FOOT_RIGHT; ii++) {
    			parts.get(ii).setTexture(partTextures[partToAsset(ii)].getTexture());
    		}
    	}
    }
    
    /**Initializes the character, with all of their body parts
     * 
     * @param w			The world*/
	protected void init(World w) {
		// TODO: Look at PartModel setAngle / setFixedRotation
		PartModel part;
		
		// HEAD
	    makePart(HEAD, NONE, getX(), getY(), 0.0f, 0.0f, w);
		
		// CHEST
		makePart(CHEST, HEAD, 0, CHEST_OFFSET, 0.0f, 0.0f, w);
		
		//ABDOMEN
		makePart(ABDOMEN, CHEST, 0, ABDOMEN_OFFSET, 0.0f, 0.0f, w);
		
		//HIPS
		makePart(HIPS, ABDOMEN, 0, HIP_OFFSET, 0.0f, 0.0f, w);
		
		// ARMS
		makePart(ARM_LEFT, CHEST, -ARM_XOFFSET, ARM_YOFFSET, 0.0f, 0.0f, w);
		part = makePart(ARM_RIGHT, CHEST, ARM_XOFFSET, ARM_YOFFSET, 0.0f, 0.0f, w);
		//part.setAngle((float)Math.PI);
		
		// FOREARMS
		makePart(FOREARM_LEFT, ARM_LEFT, -FOREARM_OFFSET, 0, 0.0f, 0.0f, w);
		part = makePart(FOREARM_RIGHT, ARM_RIGHT, FOREARM_OFFSET, 0, 0.0f, 0.0f, w);
		//part.setAngle((float)Math.PI);
		
		//HANDS
		makePart(HAND_LEFT, FOREARM_LEFT, -HAND_OFFSET, 0 , HAND_PUSH, HAND_PULL, w);
		part = makePart(HAND_RIGHT, FOREARM_RIGHT, HAND_OFFSET, 0 , HAND_PUSH, HAND_PULL, w);
		//part.setAngle((float)Math.PI);
		
		// THIGHS
		makePart(THIGH_LEFT, HIPS, -THIGH_XOFFSET, -THIGH_YOFFSET, 0.0f, 0.0f, w);
		makePart(THIGH_RIGHT, HIPS, THIGH_XOFFSET, -THIGH_YOFFSET, 0.0f, 0.0f, w);
		
		// SHINS
		makePart(SHIN_LEFT,  THIGH_LEFT, 0, -SHIN_OFFSET, 0.0f, 0.0f, w);
		makePart(SHIN_RIGHT, THIGH_RIGHT, 0, -SHIN_OFFSET, 0.0f, 0.0f, w);
		
		//FEET
		makePart(FOOT_LEFT, SHIN_LEFT, -FOOT_OFFSET, 0, FOOT_PUSH, FOOT_PULL, w);
		part = makePart(FOOT_RIGHT, SHIN_RIGHT, FOOT_OFFSET, 0, FOOT_PUSH, FOOT_PULL, w);
		//part.setAngle((float)Math.PI);
		
	}
    
	/**
	 * Helper method to make a single body part
	 * 
	 * @param part The part to make
	 * @param connect The part to connect to
	 * @param x The x-offset RELATIVE to the connecting part
	 * @param y	The y-offset RELATIVE to the connecting part
	 * @param w	The world this part is created in
	 * 
	 * @return the newly created part
	 */
	private PartModel makePart(int part, int connect, float x, float y, float push, float pull, World w) {
		TextureRegion texture = partTextures[partToAsset(part)];
		
		partCache.set(x,y);
		if (connect != NONE) {
			partCache.add(parts.get(connect).getPosition());
		}

		//TODO: set body origin to be the actual origin
		bDef.type = BodyDef.BodyType.DynamicBody;
		bDef.position.set(partCache.x, partCache.y);
		bDef.angle = 0;
		
		Body body = w.createBody(bDef);
		
		PartModel partModel = (push == 0.0f? 
				new PartModel(body, texture.getTexture())
				: new ExtremityModel(push, pull, body, texture.getTexture()));
		
		partModel.setDrawScale(drawScale);
		//TODO: set density in individual parts
		//partModel.setDensity(DENSITY);
		parts.add(partModel);
		
		if (connect != NONE){
			//create joint
			//TODO: split x into partx and connectx, same with y
			partCache.set(x/2, -y/2); 
			jointDef.localAnchorA.set(partCache);
			
			partCache.set(-x/2, y/2); 
			jointDef.localAnchorB.set(partCache);
	
			jointDef.bodyA = parts.get(connect).getBody();
			jointDef.bodyB = parts.get(part).getBody();
			jointDef.collideConnected = false;
			Joint joint = w.createJoint(jointDef);
			joints.add(joint);
		}
		
		return partModel;
	}
	
	/** Contructs a CharacterModel
	 * 
	 * @param w					the world*/
	public CharacterModel(World w){
		init(w);
		setX(HEAD_X);
		setY(HEAD_Y);
	}
	
	/** Contructs a CharacterModel
	 * 
	 * @param textures the texture map of the character
	 * @param w	the world*/
	public CharacterModel(TextureRegion[] textures, World w){
		init(w);
		setPartTextures(textures, w);
		setX(HEAD_X);
		setY(HEAD_Y);
	}

}
