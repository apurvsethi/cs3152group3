package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import beigegang.util.FilmStrip;
import static beigegang.mountsputnik.Constants.*;

public class CharacterModel extends GameObject{

	/** An array of all of the body's parts*/
	protected Array<PartModel> parts;
	/** An array of all of the body's joints*/
	protected Array<Joint> joints;
	/** Cache vector for organizing body parts */
	private Vector2 partCache = new Vector2();
	/** Texture assets for the body parts */
	private FilmStrip[] partTextures;
	
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
    public FilmStrip[] getPartTextures() {
    	return partTextures;
    }
    
    /**
     * Sets the array of textures for the individual body parts.
     *
     * The array should be BODY_TEXTURE_COUNT in size.
     *
     * @param textures the array of textures for the individual body parts.
     */
    public void setPartTextures(FilmStrip[] textures, World w) {
    	assert textures != null && textures.length > BODY_TEXTURE_COUNT : "Texture array is not large enough";
    	
    	partTextures = new FilmStrip[BODY_TEXTURE_COUNT];
    	System.arraycopy(textures, 0, partTextures, 0, BODY_TEXTURE_COUNT);
    	if (parts.size == 0) {
    		init(w);
    	} else {
    		for(int ii = 0; ii <=FOOT_RIGHT; ii++) {
    			parts.get(ii).setTexture(partTextures[ii].getTexture());
    		}
    	}
    }
    
    /**Initializes the character, with all of their body parts
     * 
     * @param w			The world*/
	protected void init(World w) {
		
		// HEAD
	    makePart(HEAD, NONE, HEAD_X, 0, HEAD_Y, 0, 0, 0, w);
		
		// CHEST
		makePart(CHEST, HEAD, 0, 0, CHEST_HEAD_OFFSET, HEAD_OFFSET, 0, 0, w);	
		
		//HIPS
		makePart(HIPS, CHEST, 0, 0, HIP_CHEST_OFFSET, CHEST_HIP_OFFSET, 0, 0, w);
		
		// ARMS
		makePart(ARM_LEFT, CHEST, -ARM_X_CHEST_OFFSET, -CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0, 0, w);
		makePart(ARM_RIGHT, CHEST, ARM_X_CHEST_OFFSET, CHEST_X_ARM_OFFSET,
				ARM_Y_CHEST_OFFSET, ARM_Y_CHEST_OFFSET, 0, 0, w);
		
		// FOREARMS
		makePart(FOREARM_LEFT, ARM_LEFT, -FOREARM_X_ARM_OFFSET, -ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, 0, 0, w);
		makePart(FOREARM_RIGHT, ARM_RIGHT, FOREARM_X_ARM_OFFSET, ARM_X_FOREARM_OFFSET,
				FOREARM_Y_ARM_OFFSET, ARM_Y_FOREARM_OFFSET, 0, 0, w);
		
		//HANDS
		makePart(HAND_LEFT, FOREARM_LEFT, -HAND_X_OFFSET, -FOREARM_X_HAND_OFFSET, 
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, HAND_PUSH, HAND_PULL, w);
		makePart(HAND_RIGHT, FOREARM_RIGHT, HAND_X_OFFSET, FOREARM_X_HAND_OFFSET, 
				HAND_Y_OFFSET, FOREARM_Y_HAND_OFFSET, HAND_PUSH, HAND_PULL, w);
		
		// THIGHS
		makePart(THIGH_LEFT, HIPS, -THIGH_X_HIP_OFFSET, -HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, 0, 0, w);
		makePart(THIGH_RIGHT, HIPS, THIGH_X_HIP_OFFSET, HIP_X_THIGH_OFFSET,
				THIGH_Y_HIP_OFFSET, HIP_Y_THIGH_OFFSET, 0, 0, w);
		
		// SHINS
		makePart(SHIN_LEFT,  THIGH_LEFT, -SHIN_X_THIGH_OFFSET, -THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, 0, 0, w);
		makePart(SHIN_RIGHT, THIGH_RIGHT, SHIN_X_THIGH_OFFSET, THIGH_X_SHIN_OFFSET,
				SHIN_Y_THIGH_OFFSET, THIGH_Y_SHIN_OFFSET, 0, 0, w);
		
		//FEET
		makePart(FOOT_LEFT, SHIN_LEFT, -FOOT_X_OFFSET, -SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, FOOT_PUSH, FOOT_PULL, w);
		makePart(FOOT_RIGHT, SHIN_RIGHT, FOOT_X_OFFSET, SHIN_X_FOOT_OFFSET, FOOT_Y_OFFSET,
				SHIN_Y_FOOT_OFFSET, FOOT_PUSH, FOOT_PULL, w);
		
	}
    
	/**
	 * Helper method to make a single body part
	 * 
	 * @param part The part to make
	 * @param connect The part to connect to
	 * @param partX The x-offset of the part RELATIVE to the connecting part's offset
	 * @param partY	The y-offset of the part RELATIVE to the connecting part's offset
	 * @param conenctX The x-offset of the connecting part RELATIVE to the part's offset
	 * @param connnectY	The y-offset of the connecting part RELATIVE to the part's offset
	 * @param w	The world this part is created in
	 * 
	 * @return the newly created part
	 */
	private PartModel makePart(int part, int connect, float partX, float connectX,
			float partY, float connectY, float push, float pull, World w) {
		FilmStrip texture = partTextures[part];
		
		partCache.set(partX,partY);
		if (connect != NONE) {
			partCache.add(parts.get(connect).getPosition());
			partCache.add(connectX,connectY);
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
		
		//create joint if two parts present
		if (connect != NONE){	
			jointDef.bodyA = parts.get(connect).getBody();
			partCache.set(connectX, connectY); 
			jointDef.localAnchorA.set(partCache);
			
			jointDef.bodyB = parts.get(part).getBody();
			partCache.set(partX, partY); 
			jointDef.localAnchorB.set(partCache);

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
	public CharacterModel(FilmStrip[] textures, World w){
		init(w);
		setPartTextures(textures, w);
		setX(HEAD_X);
		setY(HEAD_Y);
	}

}
