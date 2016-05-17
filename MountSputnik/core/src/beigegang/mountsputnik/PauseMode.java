package beigegang.mountsputnik;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import static beigegang.mountsputnik.Constants.*;
import beigegang.util.ScreenListener;

public class PauseMode extends ModeController {

	/**
	 * Track asset loading from all instances and subclasses
	 */
	protected AssetState assetState = AssetState.EMPTY;

	private static final String FONT_FILE = "Fonts/mastodon.ttf";
	private static BitmapFont fontNormal;
	private static BitmapFont fontSelected;

	private static final String OVERLAY_FILE = "Menu/Overlay.jpg";
	private static final String TEXTBOX_FILE = "Menu/Text Box.png";

	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion overlay;
	private static TextureRegion textbox;

	private static boolean race = false;
	private static String[] menuOptions = {"Resume", "Restart Last Checkpoint", "Restart Level", "Menu", "Quit"};
	private static int exitCodes[] ={EXIT_GAME_RESUME_LEVEL,EXIT_GAME_RESTART_LAST_CHECKPOINT,EXIT_GAME_RESTART_LEVEL,EXIT_MENU,EXIT_QUIT};
	private static String[] menuOptions2 = {"Resume", "Restart Race", "Menu", "Quit"};
	private static int exitCodes2[] ={EXIT_GAME_RESUME_RACE_LEVEL,EXIT_GAME_RESTART_RACE_LEVEL,EXIT_MENU,EXIT_QUIT};
	private static Color overlayColor = new Color(Color.WHITE);

	private AssetManager assetManager;


	private int currSelection = 0;
	private int changeCooldown = 0;

	public void preLoadContent(AssetManager manager) {
		assetManager = manager;
		if (assetState != AssetState.EMPTY) return;

		assetState = AssetState.LOADING;

		FreetypeFontLoader.FreeTypeFontLoaderParameter normal = new
				FreetypeFontLoader.FreeTypeFontLoaderParameter();
		normal.fontFileName = FONT_FILE;
		normal.fontParameters.size = (int)(45 * Gdx.graphics.getDensity());
		normal.fontParameters.color = Color.BROWN;
		assetManager.load("Normal" + FONT_FILE, BitmapFont.class, normal);
		assets.add("Normal" + FONT_FILE);
		FreetypeFontLoader.FreeTypeFontLoaderParameter selected = new
				FreetypeFontLoader.FreeTypeFontLoaderParameter();
		selected.fontFileName = FONT_FILE;
		selected.fontParameters.size = (int)(52 * Gdx.graphics.getDensity());
		selected.fontParameters.color = Color.FIREBRICK;
		assetManager.load("Selected" + FONT_FILE, BitmapFont.class, selected);
		assets.add("Selected" + FONT_FILE);

		manager.load(TEXTBOX_FILE, Texture.class);
		assets.add(TEXTBOX_FILE);
		manager.load(OVERLAY_FILE, Texture.class);
		assets.add(OVERLAY_FILE);
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

		fontNormal = manager.get("Normal" + FONT_FILE, BitmapFont.class);
		fontSelected = manager.get("Selected" + FONT_FILE, BitmapFont.class);

		overlay = createTexture(manager, OVERLAY_FILE, false);
		textbox = createTexture(manager, TEXTBOX_FILE, false);
		assetState = AssetState.COMPLETE;
	}

	@Override
	public void reset() {
		overlayColor.a = 0.3f;
		currSelection = 0;
		SoundController.get(SoundController.DECLINE_SOUND).play();
	}

	@Override
	public void update(float dt){};

	public void update(float dt, ScreenListener listener,boolean r) {
		race = r;
		InputController input = InputController.getInstance(0);

		if (changeCooldown > 0) changeCooldown --;

		if (changeCooldown == 0 && Math.abs(input.getVerticalL()) > 0.5) {
			SoundController.get(SoundController.SCROLL_SOUND).play();
			currSelection += input.getVerticalL() > 0.5 ? -1 : 1;
			if (race) currSelection = (currSelection + menuOptions2.length) % menuOptions2.length;
			else currSelection = (currSelection + menuOptions.length) % menuOptions.length;
			changeCooldown = MENU_CHANGE_COOLDOWN;
			if (race) changeCooldown *= 2;
		}
		if (input.didSelect()){
			SoundController.get(SoundController.SELECT_SOUND).play();
			if (race) listener.exitScreen(this, exitCodes2[currSelection]);
			else listener.exitScreen(this, exitCodes[currSelection]);
		}

	}

	public void draw(GameCanvas canvas) {
		canvas.begin();
		float bottomOfScreen = canvas.getCamera().position.y - canvas.getHeight() / 2;
		float textboxWidth = race ? canvas.getWidth() * 0.35f : canvas.getWidth() * 0.5f;
		float textboxHeight = race ? canvas.getHeight() * 0.42f : canvas.getHeight() * 0.5f;
		canvas.draw(overlay, overlayColor, 0, bottomOfScreen, canvas.getWidth(), canvas.getHeight());
		canvas.draw(textbox, Color.WHITE, (canvas.getWidth() - textboxWidth) / 2,
				bottomOfScreen + (canvas.getHeight() - textboxHeight) / 2,
				textboxWidth, textboxHeight);
		String[] menu = race ? menuOptions2 : menuOptions;
		BitmapFont font;
		float drawY = bottomOfScreen;
		drawY += race ? canvas.getHeight() * 0.085f : canvas.getHeight() * 0.11f;

		for (int i = 0; i < menu.length; i++) {
			font = currSelection == i ? fontSelected : fontNormal;
			canvas.drawTextCentered(menu[i], font, drawY);
			drawY -= canvas.getHeight() * 0.055;
		}
		canvas.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

}
