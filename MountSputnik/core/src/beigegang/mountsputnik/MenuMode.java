package beigegang.mountsputnik;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;

public class MenuMode extends ModeController {

	/**
	 * Track asset loading from all instances and subclasses
	 */
	protected AssetState assetState = AssetState.EMPTY;

	private static final String BACKGROUND_FILE = "Menu/StartMenu/Background.png";
	private static final String MENU_OPTION_FILES[] = {"Menu/StartMenu/Start.png","Menu/StartMenu/Quit.png"};

	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion[] menuOptions = new TextureRegion[MENU_OPTION_FILES.length];
	private static int exitCodes[] ={EXIT_GAME_NEW_LEVEL,EXIT_QUIT};

	private AssetManager assetManager;

	private int currSelection = 0;
	private int changeCooldown = 0;

	public void preLoadContent(AssetManager manager) {
		assetManager = manager;
		if (assetState != AssetState.EMPTY) return;

		assetState = AssetState.LOADING;
		manager.load(BACKGROUND_FILE, Texture.class);
		assets.add(BACKGROUND_FILE);
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

		background = createTexture(manager, BACKGROUND_FILE, false);

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

		if (changeCooldown > 0) changeCooldown --;

		InputController input = InputController.getInstance();

		if (changeCooldown == 0) {
			currSelection = (currSelection + (input.getVerticalL() > 0.5 ? 1 : (input.getVerticalL() < -0.5 ? -1 : 0))+ menuOptions.length) % menuOptions.length;
			changeCooldown = MENU_CHANGE_COOLDOWN;

			System.out.println(currSelection);
		}
		if (input.didSelect()) listener.exitScreen(this, exitCodes[currSelection]);

	}

	public void draw() {
		canvas.clear();

		canvas.begin();
		canvas.draw(background, 0, 0);
		canvas.end();

		canvas.begin();

		int drawY = START_MENU_DRAW_LOCATION;
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
		canvas.end();

	}


	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

}
