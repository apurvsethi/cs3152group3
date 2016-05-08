package beigegang.mountsputnik;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import static beigegang.mountsputnik.Constants.*;

public class MenuMode extends ModeController {

	/**
	 * Track asset loading from all instances and subclasses
	 */
	protected AssetState assetState = AssetState.EMPTY;

	private int currView = MAIN_MENU;
	//TODO: read in from file so these don't reset each time
	private boolean levelSelectAllowed [] = {true, true, true, true, true, true, true, true};

	private static final String BACKGROUND_FILE = "Menu/StartMenu/Background.png";
	private static final String MENU_OPTION_FILES[] = {"Menu/StartMenu/Start.png","Menu/StartMenu/Levels.png","Menu/StartMenu/Settings.png","Menu/StartMenu/Quit.png"};
	private static final String LEVEL_SELECT_OPTION_FILES[] = {"Menu/StartMenu/LevelSelect/Tutorial.png","Menu/StartMenu/LevelSelect/Canyon.png","Menu/StartMenu/LevelSelect/Waterfall.png",
			"Menu/StartMenu/LevelSelect/Snowy.png","Menu/StartMenu/LevelSelect/Volcano.png","Menu/StartMenu/LevelSelect/Sky.png","Menu/StartMenu/LevelSelect/Space.png"};
	private static final String LEVEL_LOCKED_FILE = "Menu/StartMenu/LevelSelect/Locked.png";
	private static final String MENU_BACK_FILE = "Menu/StartMenu/Menu.png";
	private static final String CLASSIC_FILE = "Menu/StartMenu/classic.png";
	private static final String SWAPPED_FILE = "Menu/StartMenu/swapped.png";
	private static final String TRIGGER_SCHEME_FILE = "Menu/StartMenu/trigger_scheme.png";
	private static final String STICK_SCHEME_FILE = "Menu/StartMenu/stick_scheme.png";
			
	/**
	 * Texture asset for files used, parts, etc.
	 */
	private static TextureRegion background;
	private static TextureRegion[] menuOptions = new TextureRegion[MENU_OPTION_FILES.length];
	private static TextureRegion[] levelSelectOptions = new TextureRegion[LEVEL_SELECT_OPTION_FILES.length];
	private static TextureRegion menuBack;
	private static TextureRegion levelLocked;
	private static TextureRegion classic;
	private static TextureRegion swapped;
	private static TextureRegion triggerScheme;
	private static TextureRegion stickScheme;
	private static TextureRegion[] settingsOptions = {triggerScheme, stickScheme, menuBack};
	private static int exitCodes[] ={EXIT_GAME_RESTART_LEVEL, EXIT_LEVEL_SELECT, EXIT_SETTINGS, EXIT_QUIT};
	private static int levelSelectCodes[] = {LEVEL_TUTORIAL, LEVEL_CANYON, LEVEL_WATERFALL, LEVEL_SNOWY_MOUNTAIN, LEVEL_VOLCANO, LEVEL_SKY, LEVEL_SPACE};
	private static int backCode = EXIT_MENU;


	private AssetManager assetManager;

	private int currSelection = 0;
	private int changeCooldown = 0;

	public void preLoadContent(AssetManager manager) {
		assetManager = manager;
		if (assetState != AssetState.EMPTY) return;

		assetState = AssetState.LOADING;
		manager.load(BACKGROUND_FILE, Texture.class);
		assets.add(BACKGROUND_FILE);
		manager.load(MENU_BACK_FILE, Texture.class);
		assets.add(MENU_BACK_FILE);
		manager.load(LEVEL_LOCKED_FILE, Texture.class);
		assets.add(LEVEL_LOCKED_FILE);
		for (String MENU_OPTION_FILE : MENU_OPTION_FILES) {
			manager.load(MENU_OPTION_FILE, Texture.class);
			assets.add(MENU_OPTION_FILE);
		}
		for (String LEVEL_SELECT_OPTION_FILE : LEVEL_SELECT_OPTION_FILES) {
			manager.load(LEVEL_SELECT_OPTION_FILE, Texture.class);
			assets.add(LEVEL_SELECT_OPTION_FILE);
		}
		manager.load(CLASSIC_FILE, Texture.class);
		assets.add(CLASSIC_FILE);
		manager.load(SWAPPED_FILE, Texture.class);
		assets.add(SWAPPED_FILE);
		manager.load(TRIGGER_SCHEME_FILE, Texture.class);
		assets.add(TRIGGER_SCHEME_FILE);
		manager.load(STICK_SCHEME_FILE, Texture.class);
		assets.add(STICK_SCHEME_FILE);
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
		menuBack = createTexture(manager, MENU_BACK_FILE, false);
		classic = createTexture(manager, CLASSIC_FILE, false);
		swapped = createTexture(manager, SWAPPED_FILE, false);
		triggerScheme = createTexture(manager, TRIGGER_SCHEME_FILE, false);
		stickScheme = createTexture(manager, STICK_SCHEME_FILE, false);
		levelLocked = createTexture(manager, LEVEL_LOCKED_FILE, false);
		for (int i = 0; i < MENU_OPTION_FILES.length; i++) {
			menuOptions[i] = createTexture(manager, MENU_OPTION_FILES[i], false);
		}
		for (int i = 0; i < LEVEL_SELECT_OPTION_FILES.length; i++) {
			levelSelectOptions[i] = createTexture(manager, LEVEL_SELECT_OPTION_FILES[i], false);
		}
		assetState = AssetState.COMPLETE;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		currSelection = 0;
		currView = MAIN_MENU;
	}

	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub

		if (changeCooldown > 0) changeCooldown --;

		InputController input = InputController.getInstance();
		

		if (changeCooldown == 0 &&

				((Math.abs(input.getVerticalL()) > 0.5 && !input.getStickScheme()) || 
				 (Math.abs(input.getVerticalR()) > 0.5 && input.getStickScheme()))) {
			SoundController.get(SoundController.SCROLL_SOUND).play();
			int selectionLength = currView == MAIN_MENU? menuOptions.length : 
				(currView == LEVEL_SELECT ? levelSelectOptions.length + 1 : 1);
			if (currView == SETTINGS){
				selectionLength = settingsOptions.length; 
			}
			currSelection = (currSelection + (input.getVerticalL() > 0.5 ? -1 : (input.getVerticalL() < -0.5 ? 1 : 0))+ selectionLength) % selectionLength;
			if(input.getStickScheme())
				currSelection = (currSelection + (input.getVerticalR() > 0.5 ? -1 : (input.getVerticalR() < -0.5 ? 1 : 0))+ selectionLength) % selectionLength;
			changeCooldown = MENU_CHANGE_COOLDOWN;
		}

		if (input.didSelect() && currView == LEVEL_SELECT){
			//play sound
			if (currSelection >= levelSelectOptions.length){
				SoundController.get(SoundController.DECLINE_SOUND).play();
				listener.exitScreen(this, backCode);
			}
			else if (levelSelectAllowed[currSelection]) {
				SoundController.get(SoundController.SELECT_SOUND).play();
				((GameEngine) listener).exitLevelSelect(this, levelSelectCodes[currSelection]);
			}
			else {
				SoundController.get(SoundController.DECLINE_SOUND).play();
			}
		}
		else if (input.didSelect() && currView == SETTINGS){
			if(currSelection == 0){
					input.swapTriggerScheme();
					SoundController.get(SoundController.SELECT_SOUND).play();
				}
			else if(currSelection == 1){
				input.swapStickScheme();
				SoundController.get(SoundController.SELECT_SOUND).play();
			}
			else{
				SoundController.get(SoundController.DECLINE_SOUND).play();
				listener.exitScreen(this, backCode);
			}
		} else if (input.didSelect()){
			SoundController.get(SoundController.SELECT_SOUND).play();
			listener.exitScreen(this, exitCodes[currSelection]);
		}

	}

	public void draw() {
		canvas.clear();

		float y = canvas.getCamera().position.y - canvas.getHeight() / 2;

		canvas.begin();
		canvas.draw(background, Color.WHITE, 0, y,canvas.getWidth(),canvas.getHeight());
		canvas.end();

		canvas.begin();
		float drawY;

		if (currView == LEVEL_SELECT) {
			drawY = LEVEL_SELECT_DRAW_LOCATION * canvas.getHeight() + y;
			int j = 0;
			for (int i = 0; i < LEVEL_SELECT_ROWS; i++){
				int columns = levelSelectOptions.length / LEVEL_SELECT_ROWS + (levelSelectOptions.length % LEVEL_SELECT_ROWS <= i? 1 : 0);
				int drawX = - (columns / 2) * (levelSelectOptions[0].getRegionWidth() + LEVEL_SELECT_SPACING)
						 + (columns % 2 == 1 ? 0 : levelSelectOptions[0].getRegionWidth() / 2 + LEVEL_SELECT_SPACING / 2);
				for (int k = 0; k < columns; k++){
					if (currSelection == j){
						canvas.draw(levelSelectOptions[j], Color.TEAL, levelSelectOptions[j].getRegionWidth() / 2,
								levelSelectOptions[j].getRegionHeight() / 2, (canvas.getWidth() / 2) + drawX, drawY, 0, 0.75f, 0.75f);
					}else {
						canvas.draw(levelSelectOptions[j], Color.WHITE, levelSelectOptions[j].getRegionWidth() / 2,
								levelSelectOptions[j].getRegionHeight() / 2, (canvas.getWidth() / 2) + drawX, drawY, 0, 0.75f, 0.75f);
					}
					if (!levelSelectAllowed[j]){
						canvas.draw(levelLocked, Color.WHITE, levelLocked.getRegionWidth() / 2,
								levelLocked.getRegionHeight() / 2, (canvas.getWidth() / 2) + drawX, drawY, 0, 0.75f, 0.75f);
					}
					drawX += levelSelectOptions[j].getRegionWidth() + LEVEL_SELECT_SPACING;
					j++;
				}
				drawY -= levelSelectOptions[0].getRegionHeight() + LEVEL_SELECT_SPACING;
			}

			if (currSelection == NUM_LEVELS) {
				canvas.draw(menuBack, Color.TEAL, menuBack.getRegionWidth() / 2,
						menuBack.getRegionHeight() / 2, (canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
			} else {
				canvas.draw(menuBack, Color.WHITE, menuBack.getRegionWidth() / 2,
						menuBack.getRegionHeight() / 2, (canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
			}

		} else if (currView == SETTINGS) {
			drawY = SETTINGS_DRAW_LOCATION * canvas.getHeight() + y;
			InputController input = InputController.getInstance();
			//draw settings
			TextureRegion currentTriggerScheme = input.getTriggerScheme() ? swapped : classic;
			TextureRegion currentStickScheme = input.getStickScheme() ? swapped : classic;
			
			canvas.draw(triggerScheme, currSelection == 0 ? Color.TEAL : Color.WHITE, 
					triggerScheme.getRegionWidth() / 2, triggerScheme.getRegionHeight() / 2, 
					(canvas.getWidth() / 2) - classic.getRegionWidth(), drawY, 0, 0.75f, 0.75f);
			canvas.draw(currentTriggerScheme, currSelection == 0 ? Color.TEAL : Color.WHITE, 
					currentTriggerScheme.getRegionWidth() / 2, currentTriggerScheme.getRegionHeight() / 2, 
					(canvas.getWidth() / 2) + classic.getRegionWidth(), drawY, 0, 0.75f, 0.75f);
			
			drawY -= MENU_ITEM_HEIGHT;
			
			canvas.draw(stickScheme, currSelection == 1 ? Color.TEAL : Color.WHITE, 
					stickScheme.getRegionWidth() / 2, stickScheme.getRegionHeight() / 2, 
					(canvas.getWidth() / 2) - classic.getRegionWidth(), drawY, 0, 0.75f, 0.75f);
			canvas.draw(currentStickScheme, currSelection == 1 ? Color.TEAL : Color.WHITE, 
					currentStickScheme.getRegionWidth() / 2, currentStickScheme.getRegionHeight() / 2, 
					(canvas.getWidth() / 2) + classic.getRegionWidth(), drawY, 0, 0.75f, 0.75f);
			
			drawY -= MENU_ITEM_HEIGHT;
			
			canvas.draw(menuBack, currSelection == 2 ? Color.TEAL : Color.WHITE, menuBack.getRegionWidth() / 2,
					menuBack.getRegionHeight() / 2, (canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);

		} else {
			drawY = START_MENU_DRAW_LOCATION * canvas.getHeight() + y;
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
		}
		canvas.end();

	}

	public void changeView(int view){
		currSelection = 0;
		currView = view;
	}

	public void unlockLevel(int level){
		levelSelectAllowed[level] = true;
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

}