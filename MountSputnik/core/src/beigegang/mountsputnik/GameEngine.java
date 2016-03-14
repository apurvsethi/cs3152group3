package beigegang.mountsputnik;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;

public class GameEngine extends ApplicationAdapter {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetManager manager;
	
	/** Drawing context to display graphics (VIEW CLASS) */
	GameCanvas  canvas;
	/** GameplayController to set up and play the game */
	GameplayController controller;
	
	public GameEngine() {}
	
	@Override
	public void create() {
		manager = new AssetManager();
		canvas = new GameCanvas();
		controller = new GameplayController(canvas.getWidth(), canvas.getHeight());
	}

	@Override
	public void render() {
		// Update the game state
		controller.update();
		
		// Draw the game
		Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		canvas.begin();
		controller.draw(canvas);
		canvas.end();
	}
	
	@Override
	public void dispose() {
		controller.UnloadContent(manager);
		manager.clear();
		manager.dispose();
		manager = null;
	}
}
