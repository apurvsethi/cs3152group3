package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
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
	public void draw(GameCanvas canvas){
		if (getX() * drawPositionScale.x > canvas.getWidth() / 4 && getX() * drawPositionScale.x < canvas.getWidth() * 3 / 4) {		
				canvas.draw(animator, Color.WHITE, origin.x, origin.y,
						getX() * drawPositionScale.x, getY() * drawPositionScale.y,
						getAngle(), drawSizeScale.x, drawSizeScale.y);
			
		}
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
