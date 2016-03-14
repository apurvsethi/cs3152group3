package beigegang.mountsputnik;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.graphics.*;
import beigegang.util.*;


//TODO: garbage collection D:

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
		/*A body part of the character*/
		PART,
		/*An extremity of the character**/
		EXTREMITY,
		/**A handhold, which is a static (at this point) object that
		 * can be grabbed */
		HANDHOLD,
		/**An obstacle, which is to be defined later */
		OBSTACLE,
	}

	// Attributes for all game objects
		/** Object position (centered on the texture middle) */
		protected Vector2 position;
		/** Object velocity vector */
		protected Vector2 velocity;
		/** Reference to texture origin */
		protected Vector2 origin;
		/** Radius of the object (used for collisions) */
		protected float radius;
		/** CURRENT image for this object. May change over time. */
		protected FilmStrip animator;
		/** Drawing scale to convert physics units to pixels */
		protected Vector2 drawScale = new Vector2(1.0f,1.0f);
		//TODO: determine if all joints should be revolute
		/** Revolute Joint definition for Joint creation*/
		protected static RevoluteJointDef jointDef = new RevoluteJointDef();
		/** Body definition for Body creation*/
		protected static BodyDef bDef = new BodyDef();
		
		// ACCESSORS
		/**
		 * Sets the texture of this object 
		 *
		 * @param texture the texture of this object 
		 */
		public void setTexture(Texture texture) {
			animator = new FilmStrip(texture,1,1,1);
			radius = animator.getRegionHeight() / 2.0f;
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
		 * Returns the position of this object (e.g. location of the center pixel)
		 *
		 * The value returned is a reference to the position vector, which may be
		 * modified freely.
		 *
		 * @return the position of this object 
		 */
		public Vector2 getPosition() {
			return position;
		}

		/**
		 * Returns the x-coordinate of the object position (center).
		 *
		 * @return the x-coordinate of the object position
		 */
		public float getX() {
			return position.x;
		}

		/**
		 * Sets the x-coordinate of the object position (center).
		 *
		 * @param value the x-coordinate of the object position
		 */
		public void setX(float value) {
			position.x = value;
		}

		/**
		 * Returns the y-coordinate of the object position (center).
		 *
		 * @return the y-coordinate of the object position
		 */
		public float getY() {
			return position.y;
		}
		
		/**
		 * Sets the y-coordinate of the object position (center).
		 *
		 * @param value the y-coordinate of the object position
		 */
		public void setY(float value) {
			position.y = value;
		}
		
		/**
		 * Returns the velocity of this object in pixels per animation frame.
		 *
		 * The value returned is a reference to the velocity vector, which may be
		 * modified freely.
		 *
		 * @return the velocity of this object 
		 */
		public Vector2 getVelocity() {
			return velocity;
		}

		/**
		 * Returns the x-coordinate of the object velocity.
		 *
		 * @return the x-coordinate of the object velocity.
		 */
		public float getVX() {
			return velocity.x;
		}

		/**
		 * Sets the x-coordinate of the object velocity.
		 *
		 * @param value the x-coordinate of the object velocity.
		 */
		public void setVX(float value) {
			velocity.x = value;
		}

		/**
		 * Sets the y-coordinate of the object velocity.
		 *
		 * @param value the y-coordinate of the object velocity.
		 */
		public float getVY() {
			return velocity.y;
		}

		/**
		 * Sets the y-coordinate of the object velocity.
		 *
		 * @param value the y-coordinate of the object velocity.
		 */
		public void setVY(float value) {
			velocity.y = value;
		}
		

		/**
		 * Returns the radius of this object.
		 *
		 * All of our objects are circles, to make collision detection easy.
		 *
		 * @return the radius of this object.
		 */
		public float getRadius() { 
			return radius;
		}

		/**
		 * Returns the type of this object.
		 *
		 * We use this instead of runtime-typing for performance reasons.
		 *
		 * @return the type of this object.
		 */
		public abstract ObjectType getType();

		/**
		 * Constructs a trivial game object
		 *
		 * The created object has no position or size.  These should be set by the subclasses.
		 */
		public GameObject() {
			position = new Vector2(0.0f, 0.0f);
			velocity = new Vector2(0.0f, 0.0f);
			radius = 0.0f;
		}
		
		/**
		 * Constructs a game object
		 *
		 * @param texture the texture of this object
		 */
		public GameObject(Texture texture){
			setTexture(texture);
			position = new Vector2(0.0f, 0.0f);
			velocity = new Vector2(0.0f, 0.0f);
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
			position.add(velocity);
		}

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
						position.x, position.y, 0.0f, 1.0f, 1.f);
		}
		
}
	
	

