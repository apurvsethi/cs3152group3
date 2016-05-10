package beigegang.mountsputnik;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static beigegang.mountsputnik.Constants.*;

public class LoadingMode extends ModeController {
	// Textures necessary to support the loading screen 
	private static final String BACKGROUND_FILE = "Menu/StartMenu/Background.png";
	private static final String PROGRESS_FILE = "progressbar.png";
	private static final String PLAY_BTN_FILE = "play.png";

	/** Background texture for start-up */
	private Texture background;
	/** Texture atlas to support a progress bar */
	private Texture statusBar;
	/** Play button to display when done */
	private Texture playButton;
	
	// statusBar is a "texture atlas." Break it up into parts.
	/** Left cap to the status background (grey region) */
	private TextureRegion statusBkgLeft;
	/** Middle portion of the status background (grey region) */
	private TextureRegion statusBkgMiddle;
	/** Right cap to the status background (grey region) */
	private TextureRegion statusBkgRight;
	/** Left cap to the status forground (colored region) */
	private TextureRegion statusFrgLeft;
	/** Middle portion of the status forground (colored region) */
	private TextureRegion statusFrgMiddle;
	/** Right cap to the status forground (colored region) */
	private TextureRegion statusFrgRight;	

	/** Default budget for asset loader (do nothing but load 60 fps) */
	private static int DEFAULT_BUDGET = 15;
	/** Standard window size (for scaling) */
	private static int STANDARD_WIDTH  = 800;
	/** Standard window height (for scaling) */
	private static int STANDARD_HEIGHT = 700;
	/** Ratio of the bar width to the screen */
	private static float BAR_WIDTH_RATIO  = 0.66f;
	/** Ration of the bar height to the screen */
	private static float BAR_HEIGHT_RATIO = 0.25f;	
	/** Height of the progress bar */
	private static int PROGRESS_HEIGHT = 30;
	/** Width of the rounded cap on left or right */
	private static int PROGRESS_CAP    = 15;
	/** Width of the middle portion in texture atlas */
	private static int PROGRESS_MIDDLE = 200;
	/** Amount to resScale the play button */
	private static float BUTTON_resScale  = 0.75f;
	
	/** AssetManager to load assets, check on progress for other screens */
	private AssetManager manager;

	/** The width of the progress bar */	
	private int width;
	/** The y-coordinate of the center of the progress bar */
	private int centerY;
	/** The x-coordinate of the center of the progress bar */
	private int centerX;
	/** The height of the canvas window (necessary since sprite origin != screen origin) */
	private int heightY;
	/** Scaling factor for when the student changes the resolution. */
	private float resScale;
	
	/** Budget that the loading screen is allowed to use for loading */
	private int budget;
	/** Progress made towards loading */
	private float progress;
	
	public LoadingMode(AssetManager manager) {
		this(manager, DEFAULT_BUDGET);
	}
	
	public LoadingMode(AssetManager manager, int budget) {
		super();
		this.manager = manager;
		this.budget = budget;
		progress = 0f;
		
		manager.load(BACKGROUND_FILE, Texture.class);
		manager.load(PROGRESS_FILE, Texture.class);
		manager.load(PLAY_BTN_FILE, Texture.class);
		manager.finishLoading();
		
		background = manager.get(BACKGROUND_FILE, Texture.class);
		statusBar = manager.get(PROGRESS_FILE, Texture.class);
		
		// Break up the status bar texture into regions
		statusBkgLeft   = new TextureRegion(statusBar,0,0,PROGRESS_CAP,PROGRESS_HEIGHT);
		statusBkgRight  = new TextureRegion(statusBar,statusBar.getWidth()-PROGRESS_CAP,0,PROGRESS_CAP,PROGRESS_HEIGHT);
		statusBkgMiddle = new TextureRegion(statusBar,PROGRESS_CAP,0,PROGRESS_MIDDLE,PROGRESS_HEIGHT);

		int offset = statusBar.getHeight()-PROGRESS_HEIGHT;
		statusFrgLeft   = new TextureRegion(statusBar,0,offset,PROGRESS_CAP,PROGRESS_HEIGHT);
		statusFrgRight  = new TextureRegion(statusBar,statusBar.getWidth()-PROGRESS_CAP,offset,PROGRESS_CAP,PROGRESS_HEIGHT);
		statusFrgMiddle = new TextureRegion(statusBar,PROGRESS_CAP,offset,PROGRESS_MIDDLE,PROGRESS_HEIGHT);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(float dt) {
		InputController input = InputController.getInstance(0);
		input.readInput();

		if (playButton != null) {
			listener.exitScreen(this, EXIT_MENU);
		}
		
		if (playButton == null) {
			manager.update(budget);
			progress = manager.getProgress();
			if (progress >= 1.0f) {
				progress = 1.0f;
				playButton = manager.get(PLAY_BTN_FILE, Texture.class);
			}
		}
	}
	
	@Override
	public void render(float dt) {
		update(dt);
		draw();
	}

	/**
	 * Draw the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 */
	public void draw() {
		canvas.begin();
		canvas.draw(background, 0, 0);
		if (playButton == null) {
			drawProgress(canvas);
		} else {
			canvas.draw(playButton, Color.WHITE, playButton.getWidth()/2, playButton.getHeight()/2, 
						centerX, centerY, 0, BUTTON_resScale*resScale, BUTTON_resScale*resScale);
		}
		canvas.end();
	}
	
	/**
	 * Updates the progress bar according to loading progress
	 *
	 * The progress bar is composed of parts: two rounded caps on the end, 
	 * and a rectangle in a middle.  We adjust the size of the rectangle in
	 * the middle to represent the amount of progress.
	 *
	 * @param canvas The drawing context
	 */	
	private void drawProgress(GameCanvas canvas) {	
		canvas.draw(statusBkgLeft,   Color.WHITE, centerX-width/2, centerY, resScale*PROGRESS_CAP, resScale*PROGRESS_HEIGHT);
		canvas.draw(statusBkgRight,  Color.WHITE, centerX+width/2-resScale*PROGRESS_CAP, centerY, resScale*PROGRESS_CAP, resScale*PROGRESS_HEIGHT);
		canvas.draw(statusBkgMiddle, Color.WHITE, centerX-width/2+resScale*PROGRESS_CAP, centerY, width-2*resScale*PROGRESS_CAP, resScale*PROGRESS_HEIGHT);

		canvas.draw(statusFrgLeft,   Color.WHITE, centerX-width/2, centerY, resScale*PROGRESS_CAP, resScale*PROGRESS_HEIGHT);
		if (progress > 0) {
			float span = progress*(width-2*resScale*PROGRESS_CAP)/2.0f;
			canvas.draw(statusFrgRight,  Color.WHITE, centerX-width/2+resScale*PROGRESS_CAP+span, centerY, resScale*PROGRESS_CAP, resScale*PROGRESS_HEIGHT);
			canvas.draw(statusFrgMiddle, Color.WHITE, centerX-width/2+resScale*PROGRESS_CAP, centerY, span, resScale*PROGRESS_HEIGHT);
		} else {
			canvas.draw(statusFrgRight,  Color.WHITE, centerX-width/2+resScale*PROGRESS_CAP, centerY, resScale*PROGRESS_CAP, resScale*PROGRESS_HEIGHT);
		}
	}
	
	/**
	 * Called when this screen should release all resources.
	 */
	public void dispose() {
		 statusBkgLeft = null;
		 statusBkgRight = null;
		 statusBkgMiddle = null;

		 statusFrgLeft = null;
		 statusFrgRight = null;
		 statusFrgMiddle = null;

		 background.dispose();
		 statusBar.dispose();
		 background = null;
		 statusBar  = null;
		 if (playButton != null) {
			 playButton.dispose();
			 playButton = null;
		 }
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

}
