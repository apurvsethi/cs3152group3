package beigegang.mountsputnik;

import beigegang.util.PooledList;
import beigegang.util.ScreenListener;
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
	private static final String MENU_OPTION_FILES[] = {"Menu/PauseMenu/Resume.png","Menu/PauseMenu/RestartLastCheckpoint.png", "Menu/PauseMenu/Restart.png","Menu/PauseMenu/Menu.png","Menu/PauseMenu/Quit.png"};
	private static final String MENU_OPTION_FILES2[] = {"Menu/PauseMenu/Resume.png", "Menu/PauseMenu/Restart.png","Menu/PauseMenu/Menu.png","Menu/PauseMenu/Quit.png"};
	private static boolean race = false;
	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion foreground;
	private static TextureRegion[] menuOptions = new TextureRegion[MENU_OPTION_FILES.length];
	private static TextureRegion[] menuOptions2 = new TextureRegion[MENU_OPTION_FILES2.length];

	private static int exitCodes[] ={EXIT_GAME_RESUME_LEVEL,EXIT_GAME_RESTART_LAST_CHECKPOINT,EXIT_GAME_RESTART_LEVEL,EXIT_MENU,EXIT_QUIT};
	private static int exitCodes2[] ={EXIT_GAME_RESUME_RACE_LEVEL,EXIT_GAME_RESTART_RACE_LEVEL,EXIT_MENU,EXIT_QUIT};

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
		for (String MENU_OPTION_FILE : MENU_OPTION_FILES2) {
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
		for (int i = 0; i < MENU_OPTION_FILES2.length; i++) {
			menuOptions2[i] = createTexture(manager, MENU_OPTION_FILES2[i], false);
		}
		assetState = AssetState.COMPLETE;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		currSelection = 0;
		SoundController.get(SoundController.DECLINE_SOUND).play();
	}

	@Override
	public void update(float dt){};

	public void update(float dt, ScreenListener listener,boolean r) {
		race = r;
		// TODO Auto-generated method stub
		InputController input = InputController.getInstance(0);

		if (changeCooldown > 0) changeCooldown --;

		if (changeCooldown == 0 && Math.abs(input.getVerticalL()) > 0.5) {
			SoundController.get(SoundController.SCROLL_SOUND).play();
			if (race) currSelection = (currSelection + (input.getVerticalL() > 0.5 ? -1 : (input.getVerticalL() < -0.5 ? 1 : 0))+ menuOptions2.length) % menuOptions2.length;
			else currSelection = (currSelection + (input.getVerticalL() > 0.5 ? -1 : (input.getVerticalL() < -0.5 ? 1 : 0))+ menuOptions.length) % menuOptions.length;
			changeCooldown = MENU_CHANGE_COOLDOWN;
		}
		if (input.didSelect()){
			SoundController.get(SoundController.SELECT_SOUND).play();
			if (race)
				listener.exitScreen(this, exitCodes2[currSelection]);
			else
				listener.exitScreen(this, exitCodes[currSelection]);
		}

	}

	public void draw(GameCanvas canvas) {

		float y = canvas.getCamera().position.y - canvas.getHeight() / 2;

//		canvas.begin();
//		canvas.draw(background,Color.WHITE, canvas.getWidth() * 3 / 4, y,canvas.getWidth() / 4,canvas.getHeight());
//		for (GameObject obj : gameObjects) obj.draw(canvas);
//		canvas.end();
//
//		if (debug) {
//			canvas.beginDebug();
//			for(GameObject obj : gameObjects) obj.drawDebug(canvas);
//			canvas.endDebug();
//		}

		canvas.begin();
		canvas.draw(foreground, Color.WHITE, 0, y,canvas.getWidth(),canvas.getHeight());
		canvas.end();
		canvas.begin();
		float drawY = canvas.getHeight() * 5f/11f + (MENU_ITEM_HEIGHT * menuOptions.length / 2) + y;
		if (!race) {
			for (int i = 0; i < menuOptions.length; i++) {

				if (i == currSelection) {
					canvas.draw(menuOptions[i], Color.TEAL, menuOptions[i].getRegionWidth() / 2,
							menuOptions[i].getRegionHeight() / 2, (canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
				} else {
					canvas.draw(menuOptions[i], Color.WHITE, menuOptions[i].getRegionWidth() / 2,
							menuOptions[i].getRegionHeight() / 2, (canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
				}
				drawY -= MENU_ITEM_HEIGHT;
			}
		}else{
			for (int i = 0; i < menuOptions2.length; i++) {
				if (i == currSelection){
					canvas.draw(menuOptions2[i], Color.TEAL, menuOptions2[i].getRegionWidth() / 2,
							menuOptions2[i].getRegionHeight()/ 2, (canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
				} else {
					canvas.draw(menuOptions2[i], Color.WHITE,  menuOptions2[i].getRegionWidth() / 2,
							menuOptions2[i].getRegionHeight()/ 2,(canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
				}
				drawY -= MENU_ITEM_HEIGHT;
			}
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
