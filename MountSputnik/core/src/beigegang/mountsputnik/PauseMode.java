package beigegang.mountsputnik;

import beigegang.util.PooledList;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;

public class PauseMode extends ModeController {

	/**
	 * Track asset loading from all instances and subclasses
	 */
	protected AssetState assetState = AssetState.EMPTY;

	private static final String FOREGROUND_FILE = "Menu/PauseMenu/Foreground.png";
	private static final String MENU_OPTION_FILES[] = {"Menu/PauseMenu/Resume.png","Menu/PauseMenu/Restart.png","Menu/PauseMenu/Menu.png","Menu/PauseMenu/Quit.png"};

	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion foreground;
	private static TextureRegion[] menuOptions = new TextureRegion[MENU_OPTION_FILES.length];

	private static int exitCodes[] ={EXIT_GAME_RESUME_LEVEL,EXIT_GAME_NEW_LEVEL,EXIT_MENU,EXIT_QUIT};

	private AssetManager assetManager;


	private int currSelection = 0;
	private int changeCooldown = 0;

	private PooledList<GameObject> gameObjects;

	public void preLoadContent(AssetManager manager) {
		assetManager = manager;
		if (assetState != AssetState.EMPTY) return;

		assetState = AssetState.LOADING;
		manager.load(FOREGROUND_FILE, Texture.class);
		assets.add(FOREGROUND_FILE);
		for (String MENU_OPTION_FILE : MENU_OPTION_FILES) {
			manager.load(MENU_OPTION_FILE, Texture.class);
			assets.add(MENU_OPTION_FILE);
		}
	}

	/**
	 * Loads the assets for this controller.
	 * <p/>
	 * Opted for nonstatic loaders, but still want the assets themselves to be
	 * static. So AssetState determines the current loading state, only load if
	 * assets are not already loaded.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (assetState != AssetState.LOADING) return;

		foreground = createTexture(manager, FOREGROUND_FILE, false);

		for (int i = 0; i < MENU_OPTION_FILES.length; i++) {
			menuOptions[i] = createTexture(manager, MENU_OPTION_FILES[i], false);
		}
		assetState = AssetState.COMPLETE;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub
		InputController input = InputController.getInstance();

		if (changeCooldown > 0) changeCooldown --;

		if (changeCooldown == 0) {
			currSelection = (currSelection + (input.getVerticalL() > 0.5 ? -1 : (input.getVerticalL() < -0.5 ? 1 : 0))+ menuOptions.length) % menuOptions.length;
			changeCooldown = MENU_CHANGE_COOLDOWN;
		}
		if (input.didSelect()) listener.exitScreen(this, exitCodes[currSelection]);

	}

	public void draw() {
		canvas.clear();

		canvas.begin();
		canvas.draw(background,Color.WHITE, 0, 0,canvas.getWidth(),background.getRegionHeight());
		for (GameObject obj : gameObjects) obj.draw(canvas);
		canvas.end();

		if (debug) {
			canvas.beginDebug();
			for(GameObject obj : gameObjects) obj.drawDebug(canvas);
			canvas.endDebug();
		}

		canvas.begin();
		canvas.draw(foreground, 0,0);
		canvas.end();

		canvas.begin();
		int drawY = canvas.getHeight() / 2 + (MENU_ITEM_HEIGHT * menuOptions.length / 2);
		for (int i = 0; i < menuOptions.length; i++){
			if (i == currSelection){
				canvas.draw(menuOptions[i], Color.TEAL, menuOptions[i].getRegionWidth() / 2,
						menuOptions[i].getRegionHeight()/ 2, (canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
			} else {
				canvas.draw(menuOptions[i], Color.WHITE,  menuOptions[i].getRegionWidth() / 2,
						menuOptions[i].getRegionHeight()/ 2,(canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
			}
			drawY -= MENU_ITEM_HEIGHT;
		}

		//draw menu objects
		canvas.end();

	}

	public void setGameObjects(PooledList<GameObject> objs){
		gameObjects = objs;
	}

	public void setGameBackground(TextureRegion bck) { background = bck; }

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

}
