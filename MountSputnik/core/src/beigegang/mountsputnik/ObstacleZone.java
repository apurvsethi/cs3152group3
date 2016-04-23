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
	private float maxSpawnHeight;
	private int spawnFrequency;
	private Rectangle bounds;
	private Texture obstacleTexture;
	private Texture warningTexture;
	private boolean triggered = false;
	private boolean triggeredAlready = false;
	private int ticksSinceLastSpawn;
	private boolean releasedOnce = false;
	//make sure an obstacle is released from this zone at least once!
	public void releasedAnObstacle(){
		releasedOnce = true;
	}
	public Texture getWarningTexture() {
		return warningTexture;
	}
	//returns if this obstaclezone should be trying to release an obstacle
	public boolean isTriggered(){
		return triggered && triggeredAlready;
	}
	//sets triggeredAlready and triggered to v until v = true passed in, then it does that
	//but once v = false passed in again (cpos >= chestHeight) it sets triggered to false.
	//then, if the chest height dips below the zone again, it can't start to release obstacles again.
	//however, every obstacleZone needs to release at least one obstacle :)
	public void setTriggered(boolean v){
		if ((triggeredAlready && triggered)) {
			triggered = v;
			if (v == true){
				triggeredAlready = v;
			}
		}
		else if (!triggeredAlready){
			triggeredAlready = v;
			triggered = v;
		}
	}
	/**
	 * @return the minSpawnHeight
	 */
	public float getMinSpawnHeight() {
		return minSpawnHeight;
	}

	public float getMaxSpawnHeight() {
		return minSpawnHeight + bounds.getHeight();
	}
	public float getMinX() {
		return bounds.x;
	}

	public float getMaxX() {
		return bounds.x + bounds.getWidth();
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
	public ObstacleZone(Texture t, Texture tr, float minHeight, int freq, Rectangle bounds){
		minSpawnHeight = minHeight;
		maxSpawnHeight = minSpawnHeight + SCREEN_HEIGHT;
		spawnFrequency = freq;
		this.bounds = bounds;
		ticksSinceLastSpawn = 0;
		obstacleTexture  = t;
		warningTexture = tr;

	}
	
	/**
	 * Generates an ObstacleZone in the level
	 * 
	 * @param t the Texture of obstacles generated here
	 * @param minHeight minimumHeight of character at which to spawn obstacles
	 * @param freq obstacle spawn frequency (in seconds)
	 * @param bounds the bounds of this zone
	 */
	public ObstacleZone(Texture t, Texture tr, float minHeight, float freq, Rectangle bounds){

		minSpawnHeight = minHeight;
		spawnFrequency = (int)(freq/WORLD_STEP);
		this.bounds = bounds;
		ticksSinceLastSpawn = 0;
		obstacleTexture  = t;
		warningTexture = tr;

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
