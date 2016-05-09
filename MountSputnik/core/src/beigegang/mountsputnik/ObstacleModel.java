package beigegang.mountsputnik;

import beigegang.util.FilmStrip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import static beigegang.mountsputnik.Constants.*;

public class ObstacleModel extends GameObject{
	public float width;
	public boolean breaking;
	public boolean broken;
	private int timestepsBrokenCurrent;

	public ObstacleModel(Texture obstacleTexture, float f, Vector2 scale) {
		super(obstacleTexture, f, scale);
		width = getX() * drawPositionScale.x;
	}

	public ObstacleModel(FilmStrip fallingObstacleTexture, float f, Vector2 scale) {
		super(fallingObstacleTexture, f, scale);
		width = getX() * drawPositionScale.x;
		shape.setAsBox(animator.getRegionWidth() * this.drawSizeScale.x / (7 * this.drawPositionScale.x),
				animator.getRegionHeight() * this.drawSizeScale.y / (7 * this.drawPositionScale.y));
		timestepsBrokenCurrent = 0;
	}

	@Override
	public ObjectType getType() {
		return ObjectType.OBSTACLE;
	}

	@Override
	public void draw(GameCanvas canvas){
		if (breaking) timestepsBrokenCurrent = (timestepsBrokenCurrent + 1) % 4;

		if (breaking && timestepsBrokenCurrent == 3 && animator.getFrame() == animator.getSize() - 1)
			broken = true;
		else if (breaking && timestepsBrokenCurrent == 3)
			animator.setFrame(animator.getFrame() + 1);

		if (drawPositionScale.x < canvas.getWidth() * 3 / 4) {
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

	public void breakObstacle() {
		breaking = true;
	}
}
