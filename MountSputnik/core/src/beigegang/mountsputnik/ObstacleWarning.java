package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import static beigegang.mountsputnik.Constants.OBSTACLE_BITS;
import static beigegang.mountsputnik.Constants.PART_BITS;

/**
 * Created by jacobcooper on 4/23/16.
 */
public class ObstacleWarning extends GameObject {

    public ObstacleWarning(Texture obstacleWarningTexture, float f, Vector2 scale) {
        super(obstacleWarningTexture, f, scale);
    }

    @Override
    public ObjectType getType() {
        return ObjectType.OBSTACLE_WARNING;
    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }
        fixtureDef.shape = shape;
        fixtureDef.filter.maskBits = PART_BITS;
        fixtureDef.filter.categoryBits = OBSTACLE_BITS;
        geometry = body.createFixture(fixtureDef);

        markDirty(false);
    }

    @Override
    protected void releaseFixtures() {

    }



}
