package beigegang.mountsputnik;

import beigegang.util.ScreenListener;
import static beigegang.mountsputnik.Constants.*;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

public class GameEngine extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas  canvas;
	/** Controllers for loading, menu, game, pause */
	private ModeController[] controllers;
	
	public GameEngine() {
		// Start loading with the asset manager
		manager = new AssetManager();
		
		// Add font support to the asset manager
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
	}
	
	@Override
	public void create() {
		canvas = new GameCanvas();
		controllers = new ModeController[4];
		controllers[LOADING_SCREEN] = new LoadingMode(manager);
		controllers[MENU_SCREEN] = new MenuMode();
		controllers[GAME_SCREEN] = new GameMode();
		controllers[PAUSE_SCREEN] = new PauseMode();
		for(int ii = 1; ii < controllers.length; ii++) {
			controllers[ii].preLoadContent(manager);
		}
		controllers[LOADING_SCREEN].setCanvas(canvas);
		controllers[LOADING_SCREEN].setScreenListener(this);
		setScreen(controllers[LOADING_SCREEN]);
		MenuMode m = (MenuMode) controllers[MENU_SCREEN]; 
		m.unlockLevel(0); 
		m.unlockLevel(1);
		m.unlockLevel(2);
		m.unlockLevel(5);
	}
	
	@Override
	public void dispose() {
		setScreen(null);

		for(int ii = 0; ii < controllers.length; ii++) {
			controllers[ii].unloadContent(manager);
			controllers[ii].dispose();
		}

		canvas.dispose();
		canvas = null;
		
		manager.clear();
		manager.dispose();
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == controllers[LOADING_SCREEN]) {
			for(int controllerIndex = 1; controllerIndex < controllers.length; controllerIndex++) {
				controllers[controllerIndex].loadContent(manager);
				controllers[controllerIndex].setScreenListener(this);
				controllers[controllerIndex].setCanvas(canvas);
			}
			controllers[MENU_SCREEN].reset();
			setScreen(controllers[MENU_SCREEN]);
		} else if (exitCode == EXIT_MENU) {
			controllers[MENU_SCREEN].reset();
			setScreen(controllers[MENU_SCREEN]);
		} else if (exitCode == EXIT_GAME_RESTART_LEVEL) {
			controllers[GAME_SCREEN].reset();
			setScreen(controllers[GAME_SCREEN]);
		} else if (exitCode == EXIT_GAME_NEXT_LEVEL) {
			GameMode gameMode = (GameMode) controllers[GAME_SCREEN];
			MenuMode menuMode = (MenuMode) controllers[MENU_SCREEN];
			gameMode.nextLevel();
			gameMode.reset();
			menuMode.unlockLevel(gameMode.getCurrLevel());
			setScreen(controllers[GAME_SCREEN]);
		} else if (exitCode == EXIT_PAUSE) {
			PauseMode pause = (PauseMode) controllers[PAUSE_SCREEN];
			GameMode game = (GameMode) controllers[GAME_SCREEN];
			pause.setGameBackground(game.getGameBackground());
			pause.setGameObjects(game.getGameObjects());
			controllers[PAUSE_SCREEN].reset();
			setScreen(controllers[PAUSE_SCREEN]);
		} else if (exitCode == EXIT_GAME_RESUME_LEVEL) {
			setScreen(controllers[GAME_SCREEN]);
		} else if (exitCode == EXIT_LEVEL_SELECT){
			MenuMode menu = (MenuMode) controllers[MENU_SCREEN];
			menu.changeView(LEVEL_SELECT);
			setScreen(controllers[MENU_SCREEN]);
		} else if (exitCode == EXIT_SETTINGS){
			MenuMode menu = (MenuMode) controllers[MENU_SCREEN];
			menu.changeView(SETTINGS);
			setScreen(controllers[MENU_SCREEN]);
		}
		else if (exitCode == EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		}
	}

	public void exitLevelSelect(Screen screen, int level){
		GameMode gameMode = (GameMode) controllers[GAME_SCREEN];
		gameMode.setLevel(level);
		exitScreen(screen, EXIT_GAME_RESTART_LEVEL);
	}
}
