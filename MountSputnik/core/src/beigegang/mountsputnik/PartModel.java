package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import static beigegang.mountsputnik.Constants.*;

public class PartModel extends GameObject{

	@Override
	public ObjectType getType() {
		return ObjectType.PART;
	}
	
	/** Contructs a PartModel
	 *
	 * @param x horizontal positioning of part
	 * @param y vertical positioning of part
	 * @param t	the texture of this part
	 * @param drawSizeScale the scaling between object size and drawn size
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 */
	public PartModel(float x, float y, Texture t, float drawSizeScale, Vector2 drawPositionScale){
		super(t, drawSizeScale, drawPositionScale);
		setX(x);
		setY(y);
	}

	/**
	 * Create new fixtures for this body, defining the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected void createFixtures() {
		if (body == null) {
			return;
		}

		// Create the fixture
		fixtureDef.shape = shape;
		fixtureDef.density = 0.5f;
		geometry = body.createFixture(fixtureDef);

		markDirty(false);
	}

	/**
	 * Release the fixtures for this body, reseting the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected void releaseFixtures() {
		if (geometry != null) {
			body.destroyFixture(geometry);
			geometry = null;
		}
	}
}
