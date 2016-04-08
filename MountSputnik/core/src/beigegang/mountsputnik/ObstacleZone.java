package beigegang.mountsputnik;

import static beigegang.mountsputnik.Constants.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

/**
 * A class to contain information about obstacle spawning zones
 * in levels
 * 
 * @author Daniel
 */
public class ObstacleZone {
	
	private float minSpawnHeight;
	private int spawnFrequency;
	private Rectangle bounds;
	private Texture obstacleTexture;
	
	private int ticksSinceLastSpawn;

	/**
	 * @return the minSpawnHeight
	 */
	public float getMinSpawnHeight() {
		return minSpawnHeight;
	}

	/**
	 * @param minSpawnHeight the minSpawnHeight to set
	 */
	public void setMinSpawnHeight(float minSpawnHeight) {
		this.minSpawnHeight = minSpawnHeight;
	}

	/**
	 * @return the spawnFrequency (in frames)
	 */
	public int getSpawnFrequency() {
		return spawnFrequency;
	}

	/**
	 * @param spawnFrequency the spawnFrequency to set (in frames)
	 */
	public void setSpawnFrequency(int spawnFrequency) {
		this.spawnFrequency = spawnFrequency;
	}
	
	/**
	 * Generates an ObstacleZone in the level
	 * 
	 * @param t the Texture of obstacles generated here
	 * @param minHeight minimumHeight of character at which to spawn obstacles
	 * @param freq obstacle spawn frequency (in frames)
	 * @param bounds the bounds of this zone
	 */
	public ObstacleZone(Texture t, float minHeight, int freq, Rectangle bounds){
		minSpawnHeight = minHeight;
		spawnFrequency = freq;
		this.bounds = bounds;
		ticksSinceLastSpawn = 0;
	}
	
	/**
	 * Generates an ObstacleZone in the level
	 * 
	 * @param t the Texture of obstacles generated here
	 * @param minHeight minimumHeight of character at which to spawn obstacles
	 * @param freq obstacle spawn frequency (in seconds)
	 * @param bounds the bounds of this zone
	 */
	public ObstacleZone(Texture t, float minHeight, float freq, Rectangle bounds){
		minSpawnHeight = minHeight;
		spawnFrequency = (int)(freq/WORLD_STEP);
		this.bounds = bounds;
		ticksSinceLastSpawn = 0;
	}

	/**
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * @param bounds the bounds to set
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	/**
	 * @return the obstacleTexture
	 */
	public Texture getObstacleTexture() {
		return obstacleTexture;
	}

	/**
	 * @param obstacleTexture the obstacleTexture to set
	 */
	public void setObstacleTexture(Texture obstacleTexture) {
		this.obstacleTexture = obstacleTexture;
	}
	
	/**
	 * @return whether a new obstacle can be spawned
	 */
	public boolean canSpawnObstacle(){
		return ticksSinceLastSpawn >= spawnFrequency;
	}
	
	/**
	 * resets the spawnTimer
	 */
	public void resetSpawnTimer(){
		ticksSinceLastSpawn = 0;
	}
	
	/**
	 * increments the spawnTimer
	 */
	public void incrementSpawnTimer(){
		ticksSinceLastSpawn++;
	}
}
