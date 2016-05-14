package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import static beigegang.mountsputnik.Constants.*;
import beigegang.util.FilmStrip;

/**
 * Created by Apurv on 5/10/16.
 *
 * Class for all the warnings in the game. Implemented for obstacle warnings,
 * handhold warnings, lava, etc.
 */
public abstract class WarningModel {

    /** Reference to texture origin */
    private Vector2 origin = new Vector2();
    /** CURRENT image for this object. May change over time. */
    private FilmStrip animator;
    /** Drawing scale to convert physics units to pixels */
    private Vector2 drawPositionScale = new Vector2();
    /** Drawing scale to change scaled size of drawn image */
    private Vector2 drawSizeScale = new Vector2();

    /** Position of the warning on screen */
    private Vector2 position = new Vector2();
    /** Whether this warning is done, should be removed */
    private boolean remove;

    /**
     * @return the texture of this object
     */
    public Texture getTexture() {
        return animator == null ? null : animator.getTexture();
    }

    /**
     * Sets the texture of this object
     *
     * @param texture the texture of this object
     */
    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 1, 1, 1);
        origin.set(animator.getRegionWidth() / 2.0f,
                animator.getRegionHeight() / 2.0f);
    }

    /**
     * @return the texture for current frame of this object
     */
    public FilmStrip getFilmStrip() {
        return animator == null ? null : animator;
    }

    /**
     * Sets the filmStrip of this object
     *
     * @param filmStrip the film strip of this object
     */
    public void setFilmStrip(FilmStrip filmStrip) {
        animator = new FilmStrip(filmStrip);
        origin.set(animator.getRegionWidth() / 2.0f,
                animator.getRegionHeight() / 2.0f);
    }

    /**
     * Returns the position of this object
     *
     * This method does NOT return a reference to the position vector.
     * Changes to this vector will not affect the body.  However, it returns
     * the same vector each time its is called, and so cannot be used as
     * an allocator.
     *
     * @return the position of this object
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Sets the current position for this physics body
     *
     * This method does not keep a reference to the parameter.
     *
     * @param value  the current position for this physics body
     */
    public void setPosition(Vector2 value) {
        setPosition(value.x, value.y);
    }

    /**
     * Sets the current position for this physics body
     *
     * @param x  the x-coordinate for this physics body
     * @param y  the y-coordinate for this physics body
     */
    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    /**
     * @return the x-coordinate for this physics body
     */
    public float getX() {
        return position.x;
    }

    /**
     * Sets the x-coordinate for this physics body
     *
     * @param value  the x-coordinate for this physics body
     */
    public void setX(float value) {
        position.x = value;
    }

    /**
     * @return the y-coordinate for this physics body
     */
    public float getY() {
        return position.y;
    }

    /**
     * Sets the y-coordinate for this physics body
     *
     * @param value  the y-coordinate for this physics body
     */
    public void setY(float value) {
        position.y = value;
    }

    /**
     * @return the draw position scale for this physics object
     */
    public Vector2 getDrawPositionScale() {
        return drawPositionScale;
    }

    /**
     * Sets the draw position scale for this physics object
     *
     * The draw position scale is the number of pixels to draw before Box2D
     * unit. Because mass is a function of area in Box2D, we typically want
     * the physics objects to be small.  So we decouple that scale from the
     * physics object.  However, we must track the scale difference to
     * communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param value  the draw position scale for this physics object
     */
    public void setDrawPositionScale(Vector2 value) {
        setDrawPositionScale(value.x,value.y);
    }

    /**
     * Sets the draw position scale for this physics object
     *
     * The draw position scale is the number of pixels to draw before Box2D
     * unit. Because mass is a function of area in Box2D, we typically want
     * the physics objects to be small.  So we decouple that scale from the
     * physics object.  However, we must track the scale difference to
     * communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param x  the x-axis scale for this physics object
     * @param y  the y-axis scale for this physics object
     */
    public void setDrawPositionScale(float x, float y) {
        drawPositionScale.set(x,y);
    }

    /**
     * @return the draw size scale for this physics object
     */
    public Vector2 getDrawSizeScale() {
        return drawSizeScale;
    }

    /**
     * Sets the draw size scale for this physics object
     *
     * @param value  the draw size scale for this physics object
     */
    public void setDrawSizeScale(Vector2 value) {
        setDrawSizeScale(value.x,value.y);
    }

    /**
     * Sets the draw size scale for this physics object
     *
     * @param x  the x-axis scale for this physics object
     * @param y  the y-axis scale for this physics object
     */
    public void setDrawSizeScale(float x, float y) {
        drawSizeScale.set(x,y);
    }

    /**
     * @return whether this model is ready to be removed
     */
    public boolean getRemove() {
        return remove;
    }

    /**
     * Sets remove to true so that this model will be removed in the next
     * timestep by the controller
     */
    public void remove() {
        remove = true;
    }

    public WarningModel() {}

    /**
     * Constructs a warning model
     *
     * @param filmStrip the texture of this object
     * @param xDimension x dimension of size scale for texture drawing
     * @param yDimension y dimension of size scale for texture drawing
     * @param drawPositionScale position scale for box2d units to screen units
     */
    public WarningModel(FilmStrip filmStrip, float xDimension,
                        float yDimension, Vector2 drawPositionScale) {
        setFilmStrip(filmStrip);
        setDrawPositionScale(drawPositionScale);
        setDrawSizeScale(xDimension, yDimension);
    }

    /**
     * Constructs a warning model
     *
     * @param filmStrip the texture of this object
     * @param dimensions size scale for texture drawing
     * @param drawPositionScale position scale for box2d units to screen units
     */
    public WarningModel(FilmStrip filmStrip, Vector2 dimensions,
                        Vector2 drawPositionScale) {
        this(filmStrip, dimensions.x, dimensions.y, drawPositionScale);
    }

    /**
     * Update the warning model as necessary
     */
    public abstract void update();

    /**
     * Change animation, move frame backward if another frame exists
     */
    protected void stepAnimationBackward() {
        if (animator.getFrame() != 0)
            animator.setFrame(animator.getFrame() - 1);
    }

    /**
     * Change animation, move frame forward if another frame exists, sets
     * remove to true otherwise
     */
    protected void stepAnimationForward() {
        if (animator.getFrame() == animator.getSize() - 1) remove = true;
        else animator.setFrame(animator.getFrame() + 1);
    }

    /**
     * Draws this object to the canvas
     *
     * There is only one drawing pass in this application, so you can draw
     * the objects in any order.
     *
     * @param canvas The drawing context
     */
    public void draw(GameCanvas canvas) {
        draw(canvas, Color.WHITE);
    }

    /**
     * Draws this object to the canvas
     *
     * There is only one drawing pass in this application, so you can draw
     * the objects in any order.
     *
     * @param canvas The drawing context
     * @param color The color to use when drawing this object, can be used to
     *              change opacity
     */
    public void draw(GameCanvas canvas, Color color) {
        canvas.draw(animator, color, origin.x, origin.y,
                getX() * drawPositionScale.x, getY() * drawPositionScale.y,
                0, drawSizeScale.x / SCREEN_WIDTH * canvas.getWidth(),
                drawSizeScale.y / SCREEN_HEIGHT * canvas.getHeight());
    }

    /**
     * Dispose of all the objects that have been created by this warning model,
     * needed for optimizing garbage collection
     */
    public void dispose() {
        origin = null;
        animator = null;
        drawPositionScale = null;
        drawSizeScale = null;
        position = null;
    }
}
