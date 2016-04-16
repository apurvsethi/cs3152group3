package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

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
		geometry = body.createFixture(fixtureDef);
		markDirty(false);
	}

	@Override
	protected void releaseFixtures() {

	}

}
