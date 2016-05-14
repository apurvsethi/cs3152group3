package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;

import static beigegang.mountsputnik.Constants.*;
import beigegang.util.FilmStrip;

/**
 * Created by Apurv on 5/11/16.
 *
 * Controller for all warnings in game, used to connect warnings with the
 * models that they are for, update as necessary
 */
public class WarningController {

    /**
     * Class to allow for obstacle warnings to spawn the time during which
     * an obstacle zone has not yet spawned an obstacle to when the obstacle
     * is available, connect warning throughout
     */
    private class TotalObstacle {
        /** Specific obstacle connected to obstacle zone and warning */
        private ObstacleModel obstacle;
        /** Obstacle zone tht the warning would be connected to */
        private ObstacleZone obstacleZone;

        /**
         * Constructor to make a TotalObstacle, used to keep track of warnings
         * from beginning of creation, before obstacle made, to end
         *
         * @param obstacleZone obstacle zone that the total obstacle refers to
         * @param obstacle obstacle from obstacle zone that total obstacle
         *                 refers to
         */
        public TotalObstacle(ObstacleZone obstacleZone,
                             ObstacleModel obstacle) {
            this.obstacleZone = obstacleZone;
            this.obstacle = obstacle;
        }
    }

    /** Scaling factor for box2d to world coordinates */
    private Vector2 scale;
    /** Cache for vector positions */
    private Vector2 positionCache;

    /** Map for handholds and the warnings for them */
    private ObjectMap<HandholdWarning, HandholdModel> handholdWarnings;
    /** Film strip used for all handhold warnings */
    private FilmStrip handholdStrip;
    /** Map for obstacles and the warnings for them */
    private ObjectMap<ObstacleWarning, TotalObstacle> obstacleWarnings;
    /** Film strip used for all obstacle warnings */
    private FilmStrip obstacleStrip;

    /**
     * Constructor for WarningController, set up object maps for each of the
     * warnings
     *
     * @param drawPositionScale scale to convert between screen and box2d
     *                          coordinates
     * @param handholdStrip warning asset for handholds
     * @param obstacleStrip warning asset for obstacles
     */
    public WarningController(Vector2 drawPositionScale,
                             FilmStrip handholdStrip,
                             FilmStrip obstacleStrip) {
        this.scale = drawPositionScale;
        positionCache = new Vector2();

        handholdWarnings = new ObjectMap<>();
        this.handholdStrip = handholdStrip;

        obstacleWarnings = new ObjectMap<>();
        this.obstacleStrip = obstacleStrip;
    }

    /**
     * Add warning for the provided handhold model if necessary
     *
     * @param handholdModel handhold that may need a warning
     */
    public void addHandholdWarning(HandholdModel handholdModel) {
        float totalTimesteps = handholdModel.getCrumble() * 60;
        if (totalTimesteps <= 0) totalTimesteps = handholdModel.getSlip() * 60;

        if (totalTimesteps > 0) {
            HandholdWarning handholdWarning = new HandholdWarning(
                    handholdStrip, handholdModel.getPosition(),
                    0.25f, 0.25f, scale, totalTimesteps);
            handholdWarnings.put(handholdWarning, handholdModel);
        }
    }

    /**
     * Add warning for the provided obstacle zone if necessary
     *
     * @param obstacleZone obstacle zone fro which warning is needed
     * @param maxLevelHeight the maximum height that the level can reach
     */
    public void addObstacleWarning(ObstacleZone obstacleZone,
                                   float maxLevelHeight) {
        positionCache.x = obstacleZone.getObstX();

        ObstacleWarning obstacleWarning = new ObstacleWarning(obstacleStrip,
                positionCache, 0.5f, 0.5f, scale,
                obstacleZone.getObstacle().getVX(),
                Math.min(maxLevelHeight, obstacleZone.getBounds().y),
                obstacleZone.getSpawnFrequency());
        TotalObstacle totalObstacle = new TotalObstacle(obstacleZone,
                obstacleZone.getObstacle());
        obstacleWarnings.put(obstacleWarning, totalObstacle);
    }

    /**
     * Update each of the warnings based on the model that they are for, remove
     * them if necessary
     *
     * @param topOfScreen current top of screen
     * @param gravity gravity on the current screen applied to obstacles
     */
    public void update(float topOfScreen, float gravity) {
        for (ObjectMap.Entry<HandholdWarning, HandholdModel> entry
                : handholdWarnings) {
            if (entry.key.getRemove() || !entry.value.getGripped()) {
                entry.key.dispose();
                handholdWarnings.remove(entry.key);
            }
            else entry.key.update();
        }

        topOfScreen /= scale.y;
        for (ObjectMap.Entry<ObstacleWarning, TotalObstacle> entry
                : obstacleWarnings) {
            float timeFromScreen = getTimeFromScreen(topOfScreen, gravity,
                    entry.value.obstacleZone);

            if (!entry.key.getSpawned() &&
                    (entry.value.obstacleZone.getSpawnFrequency() -
                    entry.key.getTimesteps() + timeFromScreen < TIME_TO_WARN
                    || (entry.value.obstacleZone.getSpawnFrequency() <
                    entry.key.getTimesteps() && timeFromScreen < TIME_TO_WARN)))
                entry.key.setSpawned();
            else if (!entry.key.getSpawned() &&
                    (entry.value.obstacle == null
                    || topOfScreen > entry.value.obstacle.getY())) {
                entry.key.dispose();
                obstacleWarnings.remove(entry.key);
            }
            else if (entry.key.getSpawned() &&
                    (topOfScreen > entry.value.obstacle.getY()
                    || topOfScreen > entry.key.getMaxHeight()
                    || entry.key.getRemove())) {
                entry.key.dispose();
                obstacleWarnings.remove(entry.key);
            }
            else entry.key.update();
        }
    }

    /**
     * Returns the time it would take for obstacle to get to screen
     *
     * @param topOfScreen top of current screen in box2d units
     * @param gravity gravity in current level
     * @param obstacleZone obstacle zone that contains obstacle
     * @return time of obstacle from reaching top of screen
     */
    private float getTimeFromScreen(float topOfScreen, float gravity,
                                    ObstacleZone obstacleZone) {
        if (obstacleZone.getObstVY() < 0) return 60 * (obstacleZone.getObstY()
                - topOfScreen) / obstacleZone.getObstVY();
        else return 60 * (float)Math.sqrt((2 * (obstacleZone.getObstY()
                - topOfScreen)) / -gravity);
    }

    /**
     * Draw each of the warnings
     */
    public void draw(GameCanvas canvas) {
        for (HandholdWarning handholdWarning : handholdWarnings.keys())
            handholdWarning.draw(canvas);

        for (ObstacleWarning obstacleWarning : obstacleWarnings.keys())
            obstacleWarning.draw(canvas);
    }

    /**
     * Dispose method to get rid of all instantiated objects
     */
    public void dispose() {
        scale = null;
        positionCache = null;
        for (HandholdWarning handholdWarning : handholdWarnings.keys())
            handholdWarning.dispose();
        handholdWarnings.clear();
        handholdStrip = null;
        for (ObstacleWarning obstacleWarning : obstacleWarnings.keys())
            obstacleWarning.dispose();
        obstacleWarnings.clear();
        obstacleStrip = null;
    }
}
