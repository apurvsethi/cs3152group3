package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import static beigegang.mountsputnik.Constants.*;

public class ObstacleModel extends GameObject{
	
	public ObstacleModel(Texture obstacleTexture, float f, Vector2 scale) {
		super(obstacleTexture, f, scale);
	}

	@Override
	public ObjectType getType() {
		return ObjectType.OBSTACLE;
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
