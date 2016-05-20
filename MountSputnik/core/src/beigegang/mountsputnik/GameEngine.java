package beigegang.mountsputnik;

import beigegang.util.ScreenListener;
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

import static beigegang.mountsputnik.Constants.*;

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
		controllers[LOADING_SCREEN] = new LoadingMode(canvas, manager);
		controllers[MENU_SCREEN] = new MenuMode();
		controllers[GAME_SCREEN] = new GameMode();
		controllers[RACE_SCREEN] = new RaceMode();
		SoundController.PreLoadContent(manager);
		for(int ii = 1; ii < controllers.length; ii++) {
			controllers[ii].preLoadContent(manager);
		}
		controllers[LOADING_SCREEN].setCanvas(canvas);
		controllers[LOADING_SCREEN].setScreenListener(this);
		setScreen(controllers[LOADING_SCREEN]);
		MenuMode m = (MenuMode) controllers[MENU_SCREEN]; 
//		m.unlockLevel(0);
//		m.unlockLevel(1);
//		m.unlockLevel(5);
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
		SoundController.UnloadContent(manager);
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
			SoundController.LoadContent(manager);
			controllers[MENU_SCREEN].reset();
			setScreen(controllers[MENU_SCREEN]);
		} else if (exitCode == EXIT_MENU) {
			controllers[MENU_SCREEN].reset();
			setScreen(controllers[MENU_SCREEN]);
		}else if (exitCode == EXIT_DIED) {
			GameMode game = (GameMode) controllers[GAME_SCREEN];
			game.dead();
		} else if (exitCode == EXIT_VICTORY) {
			GameMode gameMode = (GameMode) controllers[GAME_SCREEN];
			MenuMode menuMode = (MenuMode) controllers[MENU_SCREEN];
			menuMode.unlockLevel(gameMode.getNextLevel());
			gameMode.victorious();
		}
		else if (exitCode == EXIT_GAME_RESTART_LEVEL) {
			controllers[GAME_SCREEN].reset();
			setScreen(controllers[GAME_SCREEN]);
		} else if (exitCode == EXIT_GAME_NEXT_LEVEL) {
			GameMode gameMode = (GameMode) controllers[GAME_SCREEN];
			//MenuMode menuMode = (MenuMode) controllers[MENU_SCREEN];
			gameMode.nextLevel();
			gameMode.reset();
			//menuMode.unlockLevel(gameMode.getCurrLevel());
			setScreen(controllers[GAME_SCREEN]);
		} else if (exitCode == EXIT_PAUSE) {
			GameMode game = (GameMode) controllers[GAME_SCREEN];
			game.pause();
		} else if (exitCode == EXIT_GAME_RESUME_LEVEL) {
			GameMode game = (GameMode) controllers[GAME_SCREEN];
			game.resume();
		} else if (exitCode == EXIT_LEVEL_SELECT){
			MenuMode menu = (MenuMode) controllers[MENU_SCREEN];
			menu.changeView(LEVEL_SELECT, false);
			setScreen(controllers[MENU_SCREEN]);
		} else if (exitCode == EXIT_SETTINGS){
			MenuMode menu = (MenuMode) controllers[MENU_SCREEN];
			menu.changeView(SETTINGS, false);
			setScreen(controllers[MENU_SCREEN]);
		} else if (exitCode == EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		} else if (exitCode == EXIT_GAME_RESTART_LAST_CHECKPOINT){
			((GameMode) controllers[GAME_SCREEN]).restartLastCheckpoint();
			setScreen(controllers[GAME_SCREEN]);
		} else if (exitCode == EXIT_RACE_LEVEL_SELECT) {
			MenuMode menu = (MenuMode) controllers[MENU_SCREEN];
			menu.changeView(LEVEL_SELECT, true);
			setScreen(controllers[MENU_SCREEN]);
		}
		else if (exitCode == EXIT_VICTORY_RACE){
			RaceMode raceMode = (RaceMode) controllers[RACE_SCREEN];
			MenuMode menuMode = (MenuMode) controllers[MENU_SCREEN];
			menuMode.unlockLevel(raceMode.getNextLevel());
			raceMode.victorious();

		}
		else if (exitCode == EXIT_GAME_RESTART_RACE_LEVEL) {
			controllers[RACE_SCREEN].reset();
			setScreen(controllers[RACE_SCREEN]);
		}
		else if (exitCode == EXIT_RACE_DIED) {
			RaceMode game = (RaceMode) controllers[RACE_SCREEN];
			game.dead();
		}
		else if (exitCode == EXIT_GAME_NEXT_RACE_LEVEL) {
			RaceMode g = (RaceMode) controllers[RACE_SCREEN];
			//MenuMode menuMode = (MenuMode) controllers[MENU_SCREEN];
			g.nextLevel();
			g.reset();
			//menuMode.unlockLevel(gameMode.getCurrLevel());
			setScreen(controllers[RACE_SCREEN]);
		}
		else if (exitCode == EXIT_GAME_RESUME_RACE_LEVEL) {
			RaceMode race = (RaceMode) controllers[RACE_SCREEN];
			race.resume();
		}
		else if (exitCode == EXIT_RACE_PAUSE) {
			RaceMode race = (RaceMode) controllers[RACE_SCREEN];
			race.pause();
		}
		else if (exitCode == EXIT_INSTRUCTIONS) {
			GameMode game = (GameMode) controllers[GAME_SCREEN];
			game.instruct();
		}
	}

	public void exitLevelSelect(Screen screen, int level, boolean race) {
		if (race) {
			RaceMode raceMode = (RaceMode) controllers[RACE_SCREEN];
			raceMode.setLevel(level);
			exitScreen(screen, EXIT_GAME_RESTART_RACE_LEVEL);
		}
		else {
			GameMode gameMode = (GameMode) controllers[GAME_SCREEN];
			gameMode.setLevel(level);
			exitScreen(screen, EXIT_GAME_RESTART_LEVEL);
		}
	}
}
