package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import static beigegang.mountsputnik.Constants.*;
import beigegang.util.FilmStrip;

/**
 * Created by Apurv on 5/11/16.
 *
 * Warning for obstacles, contains all information necessary to update and
 * change warning with opacity
 */
public class ObstacleWarning extends WarningModel {

    /** Timestep count for warning, activates after spawn is true */
    private float opacity;
    /** Number of timesteps that this obstacle warning has been present for */
    private float timesteps;
    /** x velocity of the obstacle, keep track of obstacle position */
    private float xVelocity;
    /** Max height that the warning can reach */
    private float maxHeight;
    /**spawn frequency of obstacle zone connected */
    private float spawnFrequency;
    /** Whether the warning is currently being displayed */
    private boolean spawned;
    /** Color that this texture should be drawn at */
    private Color color;

    /**
     * Set the xVelocity that this warning should move at
     *
     * @param xVelocity x-velocity that this warning should move at
     */
    public void setVX(float xVelocity) {
        this.xVelocity = xVelocity / 60;
    }

    /**
     * @return maximum height that warning can be at
     */
    public float getMaxHeight() {
        return maxHeight;
    }

    /**
     * @return number of timesteps that warning has been made for
     */
    public float getTimesteps() {
        return timesteps;
    }

    /**
     * @return whether the obstacle warning should currently be displayed
     */
    public boolean getSpawned() {
        return spawned;
    }

    /**
     * Set the obstacle warning to be shown
     */
    public void setSpawned() {
        this.spawned = true;
    }

    /**
     * Constructs a handhold warning
     *
     * @param filmStrip the texture of this object
     * @param position position of the warning in box2d coordinates
     * @param dimensions size scale for texture drawing
     * @param drawPositionScale position scale for box2d units to screen units
     * @param maxHeight maximum height that the warning can reach
     * @param spawnFrequency spawn frequency of obstacle zone connected
     */
    public ObstacleWarning(FilmStrip filmStrip, Vector2 position,
                           Vector2 dimensions, Vector2 drawPositionScale,
                           float maxHeight, float spawnFrequency) {
        this(filmStrip, position, dimensions.x, dimensions.y,
                drawPositionScale, maxHeight, spawnFrequency);
    }

    /**
     * Constructs a handhold warning
     *
     * @param filmStrip the texture of this object
     * @param position position of the warning in box2d coordinates
     * @param xDimension x dimension of size scale for texture drawing
     * @param yDimension y dimension of size scale for texture drawing
     * @param drawPositionScale position scale for box2d units to screen units
     * @param maxHeight maximum height that the warning can reach
     * @param spawnFrequency spawn frequency of obstacle zone connected
     */
    public ObstacleWarning(FilmStrip filmStrip, Vector2 position,
                           float xDimension, float yDimension,
                           Vector2 drawPositionScale, float maxHeight,
                           float spawnFrequency) {
        super(filmStrip, xDimension, yDimension, drawPositionScale);
        setPosition(position);
        opacity = 0f;
        timesteps = 0f;
        xVelocity = 0f;
        this.maxHeight = maxHeight;
        this.spawnFrequency = spawnFrequency;
        this.color = new Color(Color.WHITE);
    }

    /**
     * Update the warning's opacity based on the time until the warning appears
     */
    public void update() {
        timesteps++;
        setX(getX() + xVelocity);
        if (spawned) opacity++;
    }

    /**
     * Draw method making use of opacity for the warning
     */
    @Override
    public void draw(GameCanvas canvas) {
        if (spawned) {
            setY((canvas.getCamera().position.y + canvas.getHeight() / 2
                    - canvas.getHeight() / 20) / getDrawPositionScale().y);
            color.a = Math.min(1, opacity /
                    Math.min(TIME_TO_WARN, spawnFrequency));
            draw(canvas, color);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        color = null;
    }
}
