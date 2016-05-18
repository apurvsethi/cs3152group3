package beigegang.mountsputnik;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static beigegang.mountsputnik.Constants.*;

import java.io.FileWriter; 

public class MenuMode extends ModeController {

	/**
	 * Track asset loading from all instances and subclasses
	 */
	private AssetState assetState = AssetState.EMPTY;

	private int currView = MAIN_MENU;
	private JsonReader jsonReader = new JsonReader();
	private JsonValue saveGame = (jsonReader.parse(Gdx.files.internal("savegame.json"))).get("levels");
	private boolean levelSelectAllowed [] = {true, saveGame.getBoolean("waterfall"), saveGame.getBoolean("mountain"), saveGame.getBoolean("space"), saveGame.getBoolean("canyon"), saveGame.getBoolean("volcano"), saveGame.getBoolean("sky")};

	private static final String TITLE_FONT_FILE = "Fonts/kremlin.ttf";
	private static final String FONT_FILE = "Fonts/mastodon.ttf";
	private static BitmapFont titleFont;
	private static BitmapFont fontNormal;
	private static BitmapFont levelSelectFontNormal;
	private static BitmapFont fontSelected;
	private static BitmapFont levelSelectFontSelected;

	private static final String BACKGROUND_FILE = "Menu/StartMenu/Background.png";
	private static final String BARE_BACKGROUND_FILE = "Menu/StartMenu/LevelSelect/BareBackground.png";
	private static final String TEXTBOX_FILE = "Menu/Text Box.png";
	private static final String LEVEL_LOCKED_FILE = "Menu/StartMenu/LevelSelect/Locked.png";
	protected static final String LAVA_FILE = "assets/volcano/Lava.png";
	protected static final String LAVA_GLOW_FILE = "assets/volcano/LavaGlow.png";
	protected static final String LEVEL_NAMES[] = {"tutorial", "waterfall", "mountain", "space", "canyon", "volcano", "sky"};

	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion bareBackground;
	private static TextureRegion textbox;
	private static TextureRegion levelLocked;
	protected static TextureRegion lavaTexture;
	protected static TextureRegion lavaGlowTexture;
	private static TextureRegion[] levelSelectGrounds = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectBackgrounds = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectMidgrounds = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectForegrounds = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectSurfaces = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectEdges = new TextureRegion[LEVEL_NAMES.length];

	private static String[] menuOptions = {"Start", "Race Mode", "Settings", "Quit"};
	private static int exitCodes[] = {EXIT_LEVEL_SELECT, EXIT_RACE_LEVEL_SELECT, EXIT_SETTINGS, EXIT_QUIT};
	private static String[][] levelSelectOptions = {{"Tutorial", "Canyon"}, {"Waterfall", "Volcano"}, {"Snowy Mountain", "Sky"}, {"Space", "Menu"}};
	private static int levelSelectCodes[] = {LEVEL_TUTORIAL, LEVEL_WATERFALL, LEVEL_SNOWY_MOUNTAIN, LEVEL_SPACE, LEVEL_CANYON, LEVEL_VOLCANO, LEVEL_SKY};
	private static String[] settingsOptions = {"Trigger Scheme", "Stick Scheme", "Menu"};
	private static String[] currentSchemes = {"Classic", "Classic"};
	private static int backCode = EXIT_MENU;


	private int currSelection = 0;
	private int changeCooldown = 0;
	private boolean race;

	public void preLoadContent(AssetManager manager) {
		assetManager = manager;
		if (assetState != AssetState.EMPTY) return;
		assetState = AssetState.LOADING;

		FreetypeFontLoader.FreeTypeFontLoaderParameter title = makeFont(TITLE_FONT_FILE, 70, Color.BROWN);
		loadAddFont("LevelSelect" + TITLE_FONT_FILE, title);
		FreetypeFontLoader.FreeTypeFontLoaderParameter normal = makeFont(FONT_FILE, 45, Color.BROWN);
		loadAddFont("Normal" + FONT_FILE, normal);
		FreetypeFontLoader.FreeTypeFontLoaderParameter levelSelectNormal = makeFont(FONT_FILE, 55, Color.BROWN);
		loadAddFont("LargeNormal" + FONT_FILE, levelSelectNormal);
		FreetypeFontLoader.FreeTypeFontLoaderParameter selected = makeFont(FONT_FILE, 52, Color.FIREBRICK);
		loadAddFont("Selected" + FONT_FILE, selected);
		FreetypeFontLoader.FreeTypeFontLoaderParameter levelSelectSelected = makeFont(FONT_FILE, 63, Color.FIREBRICK);
		loadAddFont("LargeSelected" + FONT_FILE, levelSelectSelected);

		loadAddTexture(BARE_BACKGROUND_FILE);
		loadAddTexture(TEXTBOX_FILE);
		loadAddTexture(LEVEL_LOCKED_FILE);
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
	public void loadContent(AssetManager manager) {
		JsonReader jsonReader = new JsonReader(); 
		JsonValue saveGame = (jsonReader.parse(Gdx.files.internal("savegame.json"))).get("levels");
		levelSelectAllowed[LEVEL_TUTORIAL] = true; 
		levelSelectAllowed[LEVEL_CANYON] = saveGame.getBoolean("canyon"); 
		levelSelectAllowed[LEVEL_WATERFALL] = saveGame.getBoolean("waterfall"); 
		levelSelectAllowed[LEVEL_SNOWY_MOUNTAIN] = saveGame.getBoolean("mountain"); 
		levelSelectAllowed[LEVEL_VOLCANO] = saveGame.getBoolean("volcano"); 
		levelSelectAllowed[LEVEL_SKY] = saveGame.getBoolean("sky"); 
		levelSelectAllowed[LEVEL_SPACE] = saveGame.getBoolean("space");
		
		if (assetState != AssetState.LOADING) return;

		titleFont = manager.get("LevelSelect" + TITLE_FONT_FILE, BitmapFont.class);
		fontNormal = manager.get("Normal" + FONT_FILE, BitmapFont.class);
		levelSelectFontNormal = manager.get("LargeNormal" + FONT_FILE, BitmapFont.class);
		fontSelected = manager.get("Selected" + FONT_FILE, BitmapFont.class);
		levelSelectFontSelected = manager.get("LargeSelected" + FONT_FILE, BitmapFont.class);

		background = createTexture(manager, BACKGROUND_FILE, false);
		bareBackground = createTexture(manager, BARE_BACKGROUND_FILE, false);
		textbox = createTexture(manager, TEXTBOX_FILE, false);
		levelLocked = createTexture(manager, LEVEL_LOCKED_FILE, false);
		lavaTexture = createTexture(manager, LAVA_FILE, false);
		lavaGlowTexture = createTexture(manager, LAVA_GLOW_FILE, false);
		for (int i = 0; i < LEVEL_NAMES.length; i++) {
			levelSelectGrounds[i] = createTexture(manager, "assets/" + LEVEL_NAMES[i] + "/LevelStart.png", false);
			levelSelectBackgrounds[i] = createTexture(manager, "assets/" + LEVEL_NAMES[i] + "/Background.png", false);
			levelSelectMidgrounds[i] = createTexture(manager, "assets/" + LEVEL_NAMES[i] + "/Midground.png", false);
			levelSelectForegrounds[i] = createTexture(manager, "assets/" + LEVEL_NAMES[i] + "/Foreground.png", false);
			levelSelectSurfaces[i] = createTexture(manager, "assets/" + LEVEL_NAMES[i] + "/Surface.png", false);
			levelSelectEdges[i] = createTexture(manager, "assets/" + LEVEL_NAMES[i] + "/SurfaceEdge.png", false);
		}
		assetState = AssetState.COMPLETE;
	}

	@Override
	public void reset() {
		currSelection = 0;
		currView = MAIN_MENU;
	}

	@Override
	public void update(float dt) {
		if (changeCooldown > 0) changeCooldown --;

		InputController input = InputController.getInstance(0);

		if (changeCooldown == 0 && (currView == SETTINGS || currView == MAIN_MENU) &&
				Math.abs(input.getVerticalL()) > 0.5) {
			SoundController.get(SoundController.SCROLL_SOUND).play();

			currSelection += input.getVerticalL() > 0.5 ? -1 : 1;
			currSelection = currView == SETTINGS ? (currSelection +
					settingsOptions.length) % settingsOptions.length :
					(currSelection + menuOptions.length) % menuOptions.length;

			changeCooldown = MENU_CHANGE_COOLDOWN;
		}
		else if (changeCooldown == 0 && currView == LEVEL_SELECT &&
				(Math.abs(input.getVerticalL()) > 0.5 || Math.abs(input.getHorizontalL()) > 0.5)) {
			SoundController.get(SoundController.SCROLL_SOUND).play();

			int length = levelSelectOptions.length * levelSelectOptions[0].length;
			if (Math.abs(input.getVerticalL()) > 0.5)
				currSelection += input.getVerticalL() > 0.5 ? -1 : 1;
			if (Math.abs(input.getHorizontalL()) > 0.5) {
				currSelection += input.getHorizontalL() > 0.5 ? levelSelectOptions.length : -levelSelectOptions.length;
				if (currSelection >= length && currSelection < length + levelSelectOptions.length - 1) currSelection++;
				else if (currSelection >= length) currSelection -= (levelSelectOptions.length - 1);
				else if (currSelection < 0 && currSelection > -levelSelectOptions.length) currSelection--;
				else if (currSelection < 0) currSelection += (levelSelectOptions.length - 1);
			}
			currSelection = (currSelection + length) % length;

			changeCooldown = MENU_CHANGE_COOLDOWN;
			if (race) changeCooldown *= 2;
		}

		if (input.didSelect() && currView == LEVEL_SELECT){
			//play sound
			if (currSelection == levelSelectCodes.length){
				SoundController.get(SoundController.DECLINE_SOUND).play();
				listener.exitScreen(this, backCode);
			}
			else if (levelSelectAllowed[currSelection]) {
				SoundController.get(SoundController.SELECT_SOUND).play();
				((GameEngine) listener).exitLevelSelect(this, levelSelectCodes[currSelection], race);
			}
			else SoundController.get(SoundController.DECLINE_SOUND).play();
		}
		else if (input.didSelect() && currView == SETTINGS){
			if (currSelection == 0) {
				input.swapTriggerScheme();
				currentSchemes[0] = input.getTriggerScheme() ? "Swapped" : "Classic";
				SoundController.get(SoundController.SELECT_SOUND).play();
			}
			else if (currSelection == 1) {
				input.swapStickScheme();
				currentSchemes[1] = input.getStickScheme() ? "Swapped" : "Classic";
				SoundController.get(SoundController.SELECT_SOUND).play();
			}
			else {
				SoundController.get(SoundController.DECLINE_SOUND).play();
				listener.exitScreen(this, backCode);
			}
		}
		else if (input.didSelect()) {
			SoundController.get(SoundController.SELECT_SOUND).play();
			listener.exitScreen(this, exitCodes[currSelection]);
		}

	}

	public void draw() {
		canvas.clear();

		float bottomOfScreen = canvas.getCamera().position.y - canvas.getHeight() / 2;

		canvas.begin();
		if (currView == SETTINGS || currView == MAIN_MENU)
			canvas.draw(background, Color.WHITE, 0, bottomOfScreen, canvas.getWidth(), canvas.getHeight());
		else if (currSelection == LEVEL_NAMES.length)
			canvas.draw(bareBackground, Color.WHITE, 0, bottomOfScreen, canvas.getWidth(), canvas.getHeight());
		else drawBackgrounds(levelSelectGrounds[currSelection], levelSelectBackgrounds[currSelection],
				levelSelectMidgrounds[currSelection], levelSelectForegrounds[currSelection],
				levelSelectSurfaces[currSelection], levelSelectEdges[currSelection]);
		canvas.end();

		canvas.begin();
		float drawY = bottomOfScreen;

		if (currView == LEVEL_SELECT) {
			float textboxWidth = canvas.getWidth() * 0.75f;
			float textboxHeight = canvas.getHeight() * 1.45f;
			canvas.draw(textbox, Color.WHITE, (canvas.getWidth() - textboxWidth) / 2,
					bottomOfScreen + (canvas.getHeight() - textboxHeight) / 2 - canvas.getHeight() * 0.01f,
					textboxWidth, textboxHeight);

			float drawX = -canvas.getWidth() * 0.15f;
			BitmapFont font;
			drawY += canvas.getHeight() * 0.31f;

			canvas.drawTextCentered("Level Select", titleFont, drawY);
			drawY -= canvas.getHeight() * 0.16;
			for (int i = 0; i < levelSelectOptions.length; i++) {
				for (int j = 0; j < levelSelectOptions[i].length; j++) {
					font = (currSelection == i + levelSelectOptions.length * j) ? levelSelectFontSelected : levelSelectFontNormal;
					canvas.drawTextCentered(levelSelectOptions[i][j], font, drawX, drawY);
					drawX *= -1;
				}
				drawY -= canvas.getHeight() * 0.15;
			}

			if (currSelection < LEVEL_NAMES.length && LEVEL_NAMES[currSelection].equals("volcano"))
				drawLava(bottomOfScreen);
		}
		else if (currView == SETTINGS) {
			canvas.draw(textbox, Color.WHITE, canvas.getWidth() * 0.2f, bottomOfScreen + canvas.getHeight() * 0.07f,
					canvas.getWidth() * 0.6f, canvas.getHeight() * 0.32f);

			float drawXName = -canvas.getWidth() * 0.12f;
			float drawXScheme = canvas.getWidth() * 0.15f;
			BitmapFont font = currSelection == 0 ? fontSelected : fontNormal;
			drawY -= canvas.getHeight() * 0.211f;

			for (int i = 0; i < currentSchemes.length; i++) {
				canvas.drawTextCentered(settingsOptions[i], font, drawXName, drawY);
				canvas.drawTextCentered(currentSchemes[i], font, drawXScheme, drawY);
				drawY -= canvas.getHeight() * 0.055;
				font = currSelection == i + 1 ? fontSelected : fontNormal;
			}
			canvas.drawTextCentered(settingsOptions[settingsOptions.length - 1], font, drawY);
		}
		else {
			canvas.draw(textbox, Color.WHITE, canvas.getWidth() * 0.3f, bottomOfScreen + canvas.getHeight() * 0.01f,
					canvas.getWidth() * 0.4f, canvas.getHeight() * 0.4f);

			drawY -= canvas.getHeight() * 0.205f;

			for (int i = 0; i < menuOptions.length; i++) {
				if (i == currSelection) canvas.drawTextCentered(menuOptions[i], fontSelected, drawY);
				else canvas.drawTextCentered(menuOptions[i], fontNormal, drawY);
				drawY -= canvas.getHeight() * 0.055f;
			}
		}
		canvas.end();
	}

	private void drawBackgrounds(TextureRegion ground, TextureRegion background, TextureRegion midground, TextureRegion foreground, TextureRegion tile, TextureRegion edge){
		float y = canvas.getCamera().position.y - canvas.getHeight() / 2;
		float tileY = y - (y % (canvas.getWidth() / 4));
		canvas.draw(background, Color.WHITE, 0, y, canvas.getWidth(), canvas.getHeight());

		canvas.draw(midground, Color.WHITE, canvas.getWidth() * 4 / 5, y, canvas.getWidth() / 5, canvas.getHeight());
		midground.flip(true,false);
		canvas.draw(midground, Color.WHITE, 0, y, canvas.getWidth() / 5, canvas.getHeight());
		midground.flip(true,false);

		canvas.draw(foreground, Color.WHITE, canvas.getWidth() * 4 / 5, y - canvas.getHeight() * 0.9f, canvas.getWidth() / 5, foreground.getTexture().getHeight());
		foreground.flip(true,false);
		canvas.draw(foreground, Color.WHITE, 0, y - canvas.getHeight() * 0.45f, canvas.getWidth() / 5, foreground.getTexture().getHeight());
		foreground.flip(true,false);

		for (int counterInt = 0; counterInt < 5; counterInt++) {
			canvas.draw(tile, Color.WHITE, canvas.getWidth() / 5, tileY, 3*canvas.getWidth() / 10, canvas.getWidth() / 4);
			canvas.draw(tile, Color.WHITE, (canvas.getWidth()-1) / 2, tileY, 3*canvas.getWidth() / 10, canvas.getWidth() / 4);
			canvas.draw(edge, Color.WHITE, (canvas.getWidth()-1) * 4 / 5, tileY, canvas.getWidth() / 16, canvas.getHeight());
			edge.flip(true,false);
			canvas.draw(edge, Color.WHITE, canvas.getWidth() / 5 - canvas.getWidth() / 16, tileY, canvas.getWidth() / 16, canvas.getHeight());
			edge.flip(true,false);

			tileY += canvas.getWidth() / 4;
		}
		canvas.draw(ground, Color.WHITE, canvas.getWidth() / 5, 0, 3*canvas.getWidth() / 5, canvas.getHeight() / 8);
	}

	private void drawLava(float bottomOfScreen) {
		canvas.draw(lavaGlowTexture, Color.WHITE, 0, bottomOfScreen - canvas.getHeight() * 0.01f, canvas.getWidth(), canvas.getHeight() * 0.3f);
		canvas.draw(lavaTexture.getTexture(), Color.WHITE, -canvas.getWidth() * 0.01f, bottomOfScreen - canvas.getHeight() * 0.95f, canvas.getWidth() * 1.1f, canvas.getHeight());
	}

	public void changeView(int view, boolean race) {
		currSelection = 0;
		currView = view;
		this.race = race;
	}

	public void unlockLevel(int level){
		levelSelectAllowed[level] = true;
		String json = "{\"levels\": {\"canyon\":" + levelSelectAllowed[LEVEL_CANYON] + 
				                  ", \"mountain\":" + levelSelectAllowed[LEVEL_SNOWY_MOUNTAIN] +
				                  ", \"volcano\":" + levelSelectAllowed[LEVEL_VOLCANO] + 
				                  ", \"sky\":" + levelSelectAllowed[LEVEL_SKY] + 
				                  ", \"space\":" + levelSelectAllowed[LEVEL_SPACE] + 
				                  ", \"waterfall\":" + levelSelectAllowed[LEVEL_WATERFALL] + "}}"; 
//		System.out.println(levelSelectAllowed.toString());
		try {
			FileWriter jsonWriter = new FileWriter(("savegame.json")); 
			jsonWriter.write(json); 
			jsonWriter.flush();
		} catch (Exception e) {}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

}