package beigegang.mountsputnik.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import beigegang.mountsputnik.GameEngine;
import static beigegang.mountsputnik.Constants.*;


public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = SCREEN_WIDTH;
		config.height = SCREEN_HEIGHT;
		config.fullscreen = false;
		config.resizable = false;
		new LwjglApplication(new GameEngine(), config);
	}
}

