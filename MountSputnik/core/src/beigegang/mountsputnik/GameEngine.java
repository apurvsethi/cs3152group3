package beigegang.mountsputnik;

import beigegang.util.ScreenListener;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

public class GameEngine extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetManager manager;
	
	/** Drawing context to display graphics (VIEW CLASS) */
	GameCanvas  canvas;
	/** Controllers for loading, menu, game, pause */
	ModeController[] controllers;
	/** Current controller being used */
	int current;
	
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
		controllers[0] = new LoadingMode(manager);
		controllers[1] = new MenuMode();
		controllers[2] = new GameMode();
		controllers[3] = new PauseMode();
		for(int ii = 1; ii < controllers.length; ii++) {
			controllers[ii].preLoadContent(manager);
		}
		current = 0;
		controllers[current].setCanvas(canvas);
		controllers[current].setScreenListener(this);
		setScreen(controllers[current]);
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
		if (screen == controllers[0]) {
			for(int ii = 1; ii < controllers.length; ii++) {
				controllers[ii].loadContent(manager);
				controllers[ii].setScreenListener(this);
				controllers[ii].setCanvas(canvas);
			}
			current = 2;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (exitCode == ModeController.EXIT_MENU) {
			current = 1;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (exitCode != ModeController.EXIT_GAME_NEW) {
			current = 2;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (exitCode == ModeController.EXIT_PAUSE) {
			current = 3;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (exitCode == ModeController.EXIT_GAME_RESUME) {
			current = 2;
			setScreen(controllers[current]);
		}
		else if (exitCode == ModeController.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		}
	}
}
