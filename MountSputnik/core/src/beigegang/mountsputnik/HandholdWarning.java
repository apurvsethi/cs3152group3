package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;

import beigegang.util.FilmStrip;

/**
 * Created by Apurv on 5/10/16.
 *
 * Gauge warning for handholds, specifically counting down time until player
 * slips off and/or the handhold itself breaks
 */
public class HandholdWarning extends WarningModel {

    /** Number of timesteps before animator update */
    private int changeTimesteps;
    /** Current timesteps */
    private int currentTimesteps;

    /**
     * Constructs a handhold warning
     *
     * @param filmStrip the texture of this object
     * @param position position of the warning in box2d coordinates
     * @param dimensions size scale for texture drawing
     * @param drawPositionScale position scale for box2d units to screen units
     * @param totalTimesteps number of timesteps that the warning covers
     */
    public HandholdWarning(FilmStrip filmStrip, Vector2 position,
                           Vector2 dimensions, Vector2 drawPositionScale,
                           float totalTimesteps) {
        this(filmStrip, position, dimensions.x, dimensions.y,
                drawPositionScale, totalTimesteps);
    }

    /**
     * Constructs a handhold warning
     *
     * @param filmStrip the texture of this object
     * @param position position of the warning in box2d coordinates
     * @param xDimension x dimension of size scale for texture drawing
     * @param yDimension y dimension of size scale for texture drawing
     * @param drawPositionScale position scale for box2d units to screen units
     * @param totalTimesteps number of timesteps that the warning covers
     */
    public HandholdWarning(FilmStrip filmStrip, Vector2 position,
                           float xDimension, float yDimension,
                           Vector2 drawPositionScale, float totalTimesteps) {
        super(filmStrip, xDimension, yDimension, drawPositionScale);
        setPosition(position);
        changeTimesteps = (int)totalTimesteps / (filmStrip.getSize() - 1);
    }

    /**
     * Update the warning based on the time and number of animation strips
     * allowed.
     */
    public void update() {
        currentTimesteps = (currentTimesteps + 1) % changeTimesteps;
        if (currentTimesteps == 0) stepAnimationForward();
    }
}
