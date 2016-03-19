package beigegang.mountsputnik;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.graphics.*;

import beigegang.util.*;
import static beigegang.mountsputnik.Constants.*;


/** Base model class that is the parent class to all other models
 *  In charge of general getting / setting textures, physics 
 *  parameters, and position
 *  
 *  Based off of GameObject code for Optimization Lab*/

public abstract class GameObject {
	
	/**
	 * Enum specifying the type of this game object.
	 * 
	 * This Enum is not strictly necessary.  We could use runtime-time
	 * typing instead.  However, enums can be used in switch statements
	 * (which are very fast), which types cannot. That is the motivation
	 * for this Enum.
	 * If you add new subclasses of GameObject, you will need to add
	 * to this Enum as well.
	 */
	public enum ObjectType {
		/**The character*/
		CHARACTER,
		/**A body part of the character*/
		PART,
		/**An extremity of the character**/
		EXTREMITY,
		/**A handhold, which is a static (at this point) object that
		 * can be grabbed */
		HANDHOLD,
		/**An obstacle, which is to be defined later */
		OBSTACLE,
	}

	// Attributes for all game objects
	/** Reference to texture origin */
	protected Vector2 origin;
	/** CURRENT image for this object. May change over time. */
	protected FilmStrip animator;
	/** Drawing scale to convert physics units to pixels */
	protected Vector2 drawScale = new Vector2(0.1f,0.1f);
	/** Shape information for this object */
	protected PolygonShape shape;
	/** Cache of the polygon vertices (for resizing) */
	protected float[] vertices;
	/** A root body for this box 2d. */
	protected Body body;
	/** A cache value for the fixture (for resizing) */
	public Fixture geometry;
	//TODO: determine if all joints should be revolute
	/** Revolute Joint definition for Joint creation*/
	protected static RevoluteJointDef jointDef = new RevoluteJointDef();
	/** Body definition for Body creation*/
	protected static BodyDef bDef = new BodyDef();
	/** Stores the fixture information for this shape */
	public FixtureDef fixtureDef = new FixtureDef();

	/// Track garbage collection status
	/** Whether the object should be removed from the world on next pass */
	private boolean toRemove;
	/** Whether the object has changed shape and needs a new fixture */
	private boolean isDirty;

	/// Caching objects
	/** A cache value for when the user wants to access the body position */
	protected Vector2 positionCache = new Vector2();
	/** A cache value for when the user wants to access the linear velocity */
	protected Vector2 velocityCache = new Vector2();
	/** A cache value for when the user wants to access the center of mass */
	protected Vector2 centroidCache = new Vector2();
	/** A cache value for when the user wants to access the drawing scale */
	protected Vector2 scaleCache = new Vector2();
	
	// ACCESSORS
	/**
	 * Sets the texture of this object 
	 *
	 * @param texture the texture of this object 
	 */
	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture,1,1,1);
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
	}
	
	/**
	 * Returns the texture of this object 
	 *
	 * @return the texture of this object 
	 */
	public Texture getTexture() {
		return animator == null ? null : animator.getTexture();
	}

	/**
	 * Returns the position of this object
	 *
	 * This method does NOT return a reference to the position vector. Changes to this
	 * vector will not affect the body.  However, it returns the same vector each time
	 * its is called, and so cannot be used as an allocator.
	 *
	 * @return the position of this object
	 */
	public Vector2 getPosition() {
		return body != null ? body.getPosition() : bDef.position;
	}

	/**
	 * Sets the current position for this physics body
	 *
	 * This method does not keep a reference to the parameter.
	 *
	 * @param value  the current position for this physics body
	 */
	public void setPosition(Vector2 value) {
		if (body != null) body.setTransform(value, 0);
		else bDef.position.set(value);
	}

	/**
	 * Sets the current position for this physics body
	 *
	 * @param x  the x-coordinate for this physics body
	 * @param y  the y-coordinate for this physics body
	 */
	public void setPosition(float x, float y) {
		if (body != null) body.setTransform(x, y, 0);
		else bDef.position.set(x, y);
	}

	/**
	 * Returns the x-coordinate for this physics body
	 *
	 * @return the x-coordinate for this physics body
	 */
	public float getX() {
		return body != null ? body.getPosition().x : bDef.position.x;
	}

	/**
	 * Sets the x-coordinate for this physics body
	 *
	 * @param value  the x-coordinate for this physics body
	 */
	public void setX(float value) {
		if (body != null) {
			positionCache.set(value,body.getPosition().y);
			body.setTransform(positionCache,body.getAngle());
		} else {
			bDef.position.x = value;
		}
	}

	/**
	 * Returns the y-coordinate for this physics body
	 *
	 * @return the y-coordinate for this physics body
	 */
	public float getY() {
		return body != null ? body.getPosition().y : bDef.position.y;
	}

	/**
	 * Sets the y-coordinate for this physics body
	 *
	 * @param value  the y-coordinate for this physics body
	 */
	public void setY(float value) {
		if (body != null) {
			positionCache.set(body.getPosition().x,value);
			body.setTransform(positionCache,body.getAngle());
		} else {
			bDef.position.y = value;
		}
	}

	/**
	 * Returns the linear velocity for this physics body
	 *
	 * This method does NOT return a reference to the velocity vector. Changes to this
	 * vector will not affect the body.  However, it returns the same vector each time
	 * its is called, and so cannot be used as an allocator.
	 *
	 * @return the linear velocity for this physics body
	 */
	public Vector2 getLinearVelocity() {
		return (body != null ? body.getLinearVelocity() : velocityCache.set(bDef.linearVelocity));
	}

	/**
	 * Sets the linear velocity for this physics body
	 *
	 * This method does not keep a reference to the parameter.
	 *
	 * @param value  the linear velocity for this physics body
	 */
	public void setLinearVelocity(Vector2 value) {
		if (body != null) {
			body.setLinearVelocity(value);
		} else {
			bDef.linearVelocity.set(value);
		}
	}

	/**
	 * Returns the x-velocity for this physics body
	 *
	 * @return the x-velocity for this physics body
	 */
	public float getVX() {
		return (body != null ? body.getLinearVelocity().x : bDef.linearVelocity.x);
	}

	/**
	 * Sets the x-velocity for this physics body
	 *
	 * @param value  the x-velocity for this physics body
	 */
	public void setVX(float value) {
		if (body != null) {
			velocityCache.set(value,body.getLinearVelocity().y);
			body.setLinearVelocity(velocityCache);
		} else {
			bDef.linearVelocity.x = value;
		}
	}

	/**
	 * Returns the y-velocity for this physics body
	 *
	 * @return the y-velocity for this physics body
	 */
	public float getVY() {
		return (body != null ? body.getLinearVelocity().y : bDef.linearVelocity.y);
	}

	/**
	 * Sets the y-velocity for this physics body
	 *
	 * @param value  the y-velocity for this physics body
	 */
	public void setVY(float value) {
		if (body != null) {
			velocityCache.set(body.getLinearVelocity().x,value);
			body.setLinearVelocity(velocityCache);
		} else {
			bDef.linearVelocity.y = value;
		}
	}

	/**
	 * Returns the angle of rotation for this body (about the center).
	 *
	 * The value returned is in radians
	 *
	 * @return the angle of rotation for this body
	 */
	public float getAngle() {
		return body != null ? body.getAngle() : bDef.angle;
	}
	
	/**
	 * Sets the angle of rotation for this body (about the center).
	 *
	 * @param angle The new angle in radians
	 */
	public void setAngle(float angle) {
		bDef.angle = angle;
	}
		
	/**
	 * Returns the type of this body
	 *
	 * @return The type of the body
	 */
	public BodyDef.BodyType getBodyType(){
		if(body != null){
			return body.getType();
		}
		else
			return bDef.type;
	}
	
	/**
	 * Sets the body type for this body.
	 *
	 * @param type The new type
	 */
	public void setBodyType(BodyDef.BodyType type){
		if(body != null){
			body.setType(type);
		}
		else
			bDef.type = type;
	}
	
	/**
	 * Returns the type of this object.
	 *
	 * We use this instead of runtime-typing for performance reasons.
	 *
	 * @return the type of this object.
	 */
	public abstract ObjectType getType();
	
	/// Garbage Collection Methods	
	/**
	 * Returns true if our object has been flagged for garbage collection
	 *
	 * A garbage collected object will be removed from the physics world at
	 * the next time step.
	 *
	 * @return true if our object has been flagged for garbage collection
	 */
	public boolean isRemoved() {
		return toRemove;
	}
	
	/**
	 * Sets whether our object has been flagged for garbage collection
	 *
	 * A garbage collected object will be removed from the physics world at
	 * the next time step.
	 *
	 * @param value  whether our object has been flagged for garbage collection
	 */
	public void markRemoved(boolean value) {
		toRemove = value;
	}
	
	/**
	 * Returns true if the shape information must be updated.
	 *
	 * Attributes tied to the geometry (and not just forces/position) must wait for
	 * collisions to complete before they are reset.  Shapes (and their properties) 
	 * are reset in the update method.
	 *
	 * @return true if the shape information must be updated.
	 */
	public boolean isDirty() {
		return isDirty;
	}
	
	/**
	 * Sets whether the shape information must be updated.
	 *
	 * Attributes tied to the geometry (and not just forces/position) must wait for
	 * collisions to complete before they are reset.  Shapes (and their properties) 
	 * are reset in the update method.
	 *
	 * @param value  whether the shape information must be updated.
	 */
	public void markDirty(boolean value) {
		isDirty = value;
	}

	/**
	 * Constructs a trivial game object
	 *
	 * The created object has no position or size.  These should be set by the subclasses.
	 */
	public GameObject() {
	}
	
	/**
	 * Constructs a game object
	 *
	 * @param texture the texture of this object
	 */
	public GameObject(Texture texture, float width, float height, float box_width, float box_height) {
		vertices = new float[8];
		shape = new PolygonShape();

		setTexture(texture);
	}

	/**
     * @return the drawing scale for this physics object
     */
    public Vector2 getDrawScale() { 
    	return drawScale; 
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
     * @param value  the drawing scale for this physics object
     */
    public void setDrawScale(Vector2 value) { 
    	setDrawScale(value.x,value.y); 
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
     * @param x  the x-axis scale for this physics object
     * @param y  the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y) {
    	drawScale.set(x,y);
    }
    
    /** Returns the height that this texture is drawn at*/
    public float getDrawHeight(){
    	return animator.getTexture().getHeight()/drawScale.y;
    }
    
    /** Returns the width that this texture is drawn at*/
    public float getDrawWidth(){
    	return animator.getTexture().getWidth()/drawScale.x;
    }
	
	/**
	 * Updates the state of this object.
	 *
	 * This method only is only intended to update values that change local state in
	 * well-defined ways, like position or a cooldown value.  It does not handle
	 * collisions (which are determined by the CollisionController).  It is
	 * not intended to interact with other objects in any way at all.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float delta) {
		if (isDirty()) {
			createFixtures();
		}
	}
	
	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * Implementations of this method should NOT retain a reference to World.  
	 * That is a tight coupling that we should avoid.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// Make a body, if possible
		bDef.active = true;
		body = world.createBody(bDef);
		body.setUserData(this);
		body.setActive(true);

		// Only initialize if a body was created.
		if (body != null) {
			createFixtures();
			return true;
		}

		bDef.active = false;
		return false;
	}

	/**
	 * Destroys the physics Body(s) of this object if applicable,
	 * removing them from the world.
	 * 
	 * @param world Box2D world that stores body
	 */
	public void deactivatePhysics(World world) {
		if (body != null) {
			// Snapshot the values
			setBodyState(body);
			world.destroyBody(body);
			body = null;
			bDef.active = false;
		}
	}

	/**
	 * Copies the state from the given body to the body def.
	 *
	 * This is important if you want to save the state of the body before removing
	 * it from the world.
	 */
	protected void setBodyState(Body body) {
		bDef.type   = body.getType();
		bDef.angle  = body.getAngle();
		bDef.active = body.isActive();
		bDef.awake  = body.isAwake();
		bDef.bullet = body.isBullet();
		bDef.position.set(body.getPosition());
		bDef.linearVelocity.set(body.getLinearVelocity());
		bDef.allowSleep = body.isSleepingAllowed();
		bDef.fixedRotation = body.isFixedRotation();
		bDef.gravityScale  = body.getGravityScale();
		bDef.angularDamping = body.getAngularDamping();
		bDef.linearDamping  = body.getLinearDamping();
	}

	/**
	 * Create new fixtures for this body, defining the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected abstract void createFixtures();

	/**
	 * Release the fixtures for this body, reseting the shape
	 *
	 * This is the primary method to override for custom physics objects.
	 */
	protected abstract void releaseFixtures();

	/**
	 * Draws this object to the canvas
	 *
	 * There is only one drawing pass in this application, so you can draw the objects
	 * in any order.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas) {
		canvas.draw(animator, Color.WHITE, origin.x, origin.y,
				getX(), getY(), getAngle(), drawScale.x, drawScale.y);
	}

	/**
	 * Draws the outline of the physics body.
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		canvas.drawPhysics(shape, Color.YELLOW, getX(), getY(), getAngle(),1,1);
	}
}
	
	

