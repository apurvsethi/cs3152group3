package beigegang.mountsputnik;

import beigegang.util.FilmStrip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import static beigegang.mountsputnik.Constants.*;

public class ObstacleModel extends GameObject{
	public float width;
	public boolean breaking;
	public boolean broken;
	private int timestepsBrokenCurrent;
	private CircleShape circleShape = new CircleShape();
	private PolygonShape extraShape = new PolygonShape();
	private Fixture extraGeometry;

	public ObstacleModel(Texture obstacleTexture, float f, Vector2 scale) {
		super(obstacleTexture, f, scale);
		width = getX() * drawPositionScale.x;
		positionCache.setZero();
		circleShape.setPosition(positionCache);
		circleShape.setRadius(obstacleTexture.getWidth() * 0.4f * this.drawSizeScale.x / this.drawPositionScale.x);
	}

	public ObstacleModel(FilmStrip fallingObstacleTexture, float f, Vector2 scale) {
		super(fallingObstacleTexture, f, scale);
		width = getX() * drawPositionScale.x;
		positionCache.setZero();
		circleShape.setPosition(positionCache);
		circleShape.setRadius(fallingObstacleTexture.getRegionWidth() * 0.15f * this.drawSizeScale.x / this.drawPositionScale.x);
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
	public void drawDebug(GameCanvas canvas) {
		if (circleShape != null)
			canvas.drawPhysics(circleShape, Color.YELLOW, getX(), getY(),
					drawPositionScale.x, drawPositionScale.y);
		else {
			canvas.drawPhysics(shape, Color.YELLOW, getX(), getY(),
					getAngle(), drawPositionScale.x, drawPositionScale.y);
			canvas.drawPhysics(extraShape, Color.YELLOW, getX(), getY(),
					getAngle(), drawPositionScale.x, drawPositionScale.y);
		}
	}

	public void setSkyFixtures() {
		circleShape = null;
		float xScale = drawSizeScale.x / drawPositionScale.x;
		float yScale = drawSizeScale.y / drawPositionScale.y;
		shape.set(new float[] {-animator.getRegionWidth() * 0.5f * xScale, animator.getRegionHeight() * 0.49f * yScale,
				-animator.getRegionWidth() * 0.17f * xScale, 0,
				animator.getRegionWidth() * 0.13f * xScale, 0,
				animator.getRegionWidth() * 0.4f * xScale, animator.getRegionHeight() * 0.49f * yScale});
		extraShape.set(new float[] {animator.getRegionWidth() * 0.55f * xScale, -animator.getRegionHeight() * 0.49f * yScale,
				animator.getRegionWidth() * 0.13f * xScale, 0,
				-animator.getRegionWidth() * 0.17f * xScale, 0,
				-animator.getRegionWidth() * 0.3f * xScale, -animator.getRegionHeight() * 0.49f * yScale});
	}

	@Override
	protected void createFixtures() {
		if (body == null) return;
		if (circleShape != null) {
			fixtureDef.shape = circleShape;
			fixtureDef.filter.maskBits = PART_BITS;
			fixtureDef.filter.categoryBits = OBSTACLE_BITS;
			geometry = body.createFixture(fixtureDef);
		}
		else {
			fixtureDef.shape = shape;
			fixtureDef.filter.maskBits = PART_BITS;
			fixtureDef.filter.categoryBits = OBSTACLE_BITS;
			geometry = body.createFixture(fixtureDef);
			fixtureDef.shape = extraShape;
			fixtureDef.filter.maskBits = PART_BITS;
			fixtureDef.filter.categoryBits = OBSTACLE_BITS;
			extraGeometry = body.createFixture(fixtureDef);
		}
		
		markDirty(false);
	}

	@Override
	protected void releaseFixtures() {
		if (geometry != null) {
			body.destroyFixture(geometry);
			geometry = null;
		}
		if (extraGeometry != null) {
			body.destroyFixture(extraGeometry);
			extraGeometry = null;
		}
	}

	public void breakObstacle() {
		breaking = true;
	}
}
