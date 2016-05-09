package beigegang.mountsputnik;

import beigegang.util.FilmStrip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import static beigegang.mountsputnik.Constants.*;

public class PartModel extends GameObject{

	@Override
	public ObjectType getType() {
		return ObjectType.PART;
	}
	
	private CharacterModel character;
	
	public CharacterModel getCharacter(){
		return character;
	}
	
	
	/** Contructs a PartModel
	 *
	 * @param x horizontal positioning of part
	 * @param y vertical positioning of part
	 * @param t	the texture of this part
	 * @param drawSizeScale the scaling between object size and drawn size
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 * @param character the character that this is a part of
	 */
	public PartModel(float x, float y, FilmStrip t, float drawSizeScale, Vector2 drawPositionScale, CharacterModel character){
		super(t, drawSizeScale, drawPositionScale);
		setX(x);
		setY(y);
		this.character = character;
	}
	
	public void drawShadow(TextureRegion shadowTexture, GameCanvas canvas){
		canvas.draw(shadowTexture, Color.WHITE, origin.x, origin.y,
				(getX()+SHADOW_X_OFFSET) * drawPositionScale.x, (getY()+SHADOW_Y_OFFSET) * drawPositionScale.y,
				getAngle(), drawSizeScale.x / SCREEN_WIDTH * canvas.getWidth(),
				drawSizeScale.y / SCREEN_HEIGHT * canvas.getHeight());
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
		fixtureDef.density = 0.22f;
		fixtureDef.filter.maskBits = OBSTACLE_BITS;
		fixtureDef.filter.categoryBits = PART_BITS;
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
