package beigegang.mountsputnik;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class GameplayController {
	
	protected Array<String> assets;
	
	/** Strings for files used, string[] for parts, etc. */
	private static final String BACKGROUND_FILE = "";
	private static final String FOREGROUND_FILE = "";
	private static final String[] PART_TEXTURES = {""};

	/** Texture asset for files used, parts, etc. */
	private static TextureRegion background;
	private static TextureRegion foreground;
	private static TextureRegion[] partTextures = new TextureRegion[PART_TEXTURES.length];

	/**
	 * Preloads the assets for this controller.
	 *
	 * Opted for nonstatic loaders, but still want the assets themselves to be 
	 * static. So AssetState determines the current loading state, only load if
	 * assets are not already loaded.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void PreLoadContent(AssetManager manager) {
		manager.load(BACKGROUND_FILE, Texture.class);
		assets.add(BACKGROUND_FILE);
		manager.load(FOREGROUND_FILE, Texture.class);
		assets.add(FOREGROUND_FILE);
		for (int i = 0; i < PART_TEXTURES.length; i++) {
			manager.load(PART_TEXTURES[i], Texture.class);
			assets.add(PART_TEXTURES[i]);
		}
	}
	
	/**
	 * Loads the assets for this controller.
	 *
	 * Opted for nonstatic loaders, but still want the assets themselves to be 
	 * static. So AssetState determines the current loading state, only load if
	 * assets are not already loaded.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void LoadContent(AssetManager manager) {
		background = createTexture(manager, BACKGROUND_FILE, false);
		foreground = createTexture(manager, FOREGROUND_FILE, false);
		for (int i = 0; i < PART_TEXTURES.length; i++) {
			partTextures[i] = createTexture(manager, PART_TEXTURES[i], false);
		}
	}
	
	/**
	 * Returns a newly loaded texture region for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * whether or not the texture should repeat) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param repeat	Whether the texture should be repeated
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
		if (manager.isLoaded(file)) {
			TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
			region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			if (repeat) {
				region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
			}
			return region;
		}
		return null;
	}
	
	/** 
	 * Unloads the assets for this game.
	 * 
	 * This method erases the static variables.  It also deletes the associated textures 
	 * from the asset manager. If no assets are loaded, this method does nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void UnloadContent(AssetManager manager) {
		for (String s : assets)
			if (manager.isLoaded(s)) manager.unload(s);
	}
	
	/** All game objects present on screen */
	private Array<GameObject> objects;
	
	public GameplayController(float width, float height) {
		
	}
	
	public void update() {
		InputController input = InputController.getInstance();
		
		// TODO: Use inputController methods to select limbs, 
		//       horizontal and vertical to move them
		
		// TODO: Movements of other objects (obstacles, evenutally)
		
		// TODO: Interactions between limbs and handholds
	}
	
	public void draw(GameCanvas canvas) {
		canvas.clear();
		
		// Draw background unscaled.
		canvas.begin();
		canvas.draw(background, Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());
		canvas.end();
		
		canvas.begin();
		for(GameObject obj : objects) {
			obj.draw(canvas);
		}
		canvas.end();
		
		// TODO: Add debug drawing for objects
		
		// TODO: Draw foreground
	}
	
	public void dispose() {
		// TODO: Dispose of created, currently nothing
	}
}
