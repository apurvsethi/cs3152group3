package beigegang.mountsputnik;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
	private boolean levelSelectAllowed [] = {true, saveGame.getBoolean("canyon"), saveGame.getBoolean("waterfall"), saveGame.getBoolean("mountain"), saveGame.getBoolean("volcano"), saveGame.getBoolean("sky"), saveGame.getBoolean("space")};

	private static final String FONT_FILE = "Fonts/mastodon.ttf";
	private static BitmapFont fontNormal;
	private static BitmapFont fontSelected;

	private static final String BACKGROUND_FILE = "Menu/StartMenu/Background.png";
	private static final String TEXTBOX_FILE = "Menu/Text Box.png";
	private static final String LEVEL_LOCKED_FILE = "Menu/StartMenu/LevelSelect/Locked.png";
	protected static final String LEVEL_NAMES[] = {"tutorial", "canyon", "waterfall", "volcano", "mountain", "sky", "space"};

	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion textbox;
	private static TextureRegion levelLocked;
	private static TextureRegion[] levelSelectGrounds = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectBackgrounds = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectMidgrounds = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectForegrounds = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectSurfaces = new TextureRegion[LEVEL_NAMES.length];
	private static TextureRegion[] levelSelectEdges = new TextureRegion[LEVEL_NAMES.length];

	private static String[] menuOptions = {"Start", "Race Mode", "Level Select", "Settings", "Quit"};
	private static int exitCodes[] = {EXIT_GAME_RESTART_LEVEL, EXIT_RACE, EXIT_LEVEL_SELECT, EXIT_SETTINGS, EXIT_QUIT};
	private static String[][] levelSelectOptions = {{"Tutorial", "Snowy Mountain"}, {"Canyon", "Sky"}, {"Waterfall", "Space"}, {"Volcano", "Menu"}};
	private static int levelSelectCodes[] = {LEVEL_TUTORIAL, LEVEL_CANYON, LEVEL_WATERFALL, LEVEL_VOLCANO, LEVEL_SNOWY_MOUNTAIN, LEVEL_SKY, LEVEL_SPACE};
	private static String[] settingsOptions = {"Trigger Scheme", "Stick Scheme", "Menu"};
	private static String[] currentSchemes = {"Classic", "Classic"};
	private static int backCode = EXIT_MENU;

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

		for(String name : LEVEL_NAMES){
			manager.load("assets/"+name+"/LevelStart.png", Texture.class);
			assets.add("assets/"+name+"/LevelStart.png");
			manager.load("assets/"+name+"/Background.png", Texture.class);
			assets.add("assets/"+name+"/Background.png");
			manager.load("assets/"+name+"/Midground.png", Texture.class);
			assets.add("assets/"+name+"/Midground.png");
			manager.load("assets/"+name+"/Foreground.png", Texture.class);
			assets.add("assets/"+name+"/Foreground.png");
			manager.load("assets/"+name+"/Surface.png", Texture.class);
			assets.add("assets/"+name+"/Surface.png");
			manager.load("assets/"+name+"/SurfaceEdge.png", Texture.class);
			assets.add("assets/"+name+"/SurfaceEdge.png");
		}

		manager.load(TEXTBOX_FILE, Texture.class);
		assets.add(TEXTBOX_FILE);
		manager.load(LEVEL_LOCKED_FILE, Texture.class);
		assets.add(LEVEL_LOCKED_FILE);
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

		fontNormal = manager.get("Normal" + FONT_FILE, BitmapFont.class);
		fontSelected = manager.get("Selected" + FONT_FILE, BitmapFont.class);

		background = createTexture(manager, BACKGROUND_FILE, false);
		textbox = createTexture(manager, TEXTBOX_FILE, false);
		levelLocked = createTexture(manager, LEVEL_LOCKED_FILE, false);
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
			if (Math.abs(input.getVerticalL()) > 0.5) {
				int oldSelection = currSelection;
				currSelection += input.getVerticalL() > 0.5 ? -1 : 1;
				if (oldSelection / levelSelectOptions.length != ((currSelection + length) % length) / levelSelectOptions.length)
					currSelection += levelSelectOptions.length;
			}
			if (Math.abs(input.getHorizontalL()) > 0.5)
				currSelection += input.getHorizontalL() > 0.5 ? levelSelectOptions.length : -levelSelectOptions.length;
			currSelection = (currSelection + length) % length;

			changeCooldown = MENU_CHANGE_COOLDOWN;
		}

		if (input.didSelect() && currView == LEVEL_SELECT){
			//play sound
			if (currSelection == levelSelectCodes.length){
				SoundController.get(SoundController.DECLINE_SOUND).play();
				listener.exitScreen(this, backCode);
			}
			else if (levelSelectAllowed[currSelection]) {
				SoundController.get(SoundController.SELECT_SOUND).play();
				((GameEngine) listener).exitLevelSelect(this, levelSelectCodes[currSelection]);
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
		if (currView == SETTINGS || currView == MAIN_MENU || currSelection == LEVEL_NAMES.length)
			canvas.draw(background, Color.WHITE, 0, bottomOfScreen, canvas.getWidth(), canvas.getHeight());
		else SharedMethods.drawBackgrounds(canvas, levelSelectGrounds[currSelection], levelSelectBackgrounds[currSelection],
				levelSelectMidgrounds[currSelection], levelSelectForegrounds[currSelection], levelSelectSurfaces[currSelection],
				levelSelectEdges[currSelection]);
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
			drawY += canvas.getHeight() * 0.27f;

			for (int i = 0; i < levelSelectOptions.length; i++) {
				for (int j = 0; j < levelSelectOptions[i].length; j++) {
					font = (currSelection == i + levelSelectOptions.length * j) ? fontSelected : fontNormal;
					canvas.drawTextCentered(levelSelectOptions[i][j], font, drawX, drawY);
					drawX *= -1;
				}
				drawY -= canvas.getHeight() * 0.18;
			}
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
			canvas.draw(textbox, Color.WHITE, canvas.getWidth() * 0.25f, bottomOfScreen - canvas.getHeight() * 0.04f,
					canvas.getWidth() * 0.5f, canvas.getHeight() * 0.5f);

			drawY -= canvas.getHeight() * 0.18f;

			for (int i = 0; i < menuOptions.length; i++) {
				if (i == currSelection) canvas.drawTextCentered(menuOptions[i], fontSelected, drawY);
				else canvas.drawTextCentered(menuOptions[i], fontNormal, drawY);
				drawY -= canvas.getHeight() * 0.055f;
			}
		}
		canvas.end();
	}

	public void changeView(int view){
		currSelection = 0;
		currView = view;
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