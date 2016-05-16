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

public class VictoryMode extends ModeController {

    /**
     * Track asset loading from all instances and subclasses
     */
    protected AssetState assetState = AssetState.EMPTY;

    private static final String GEOMETRIC_FONT_FILE = "Fonts/geometric.ttf";
    private static BitmapFont geometricFontNormal;
    private static BitmapFont geometricFontSelected;

    private static final String OVERLAY_FILE = "Menu/Overlay.jpg";
    private static final String TEXTBOX_FILE = "Menu/Text Box.png";

    /**
     * Texture asset for files used, parts, etc.
     */
    private static TextureRegion overlay;
    private static TextureRegion textbox;

    private static String[] menuOptions = {"Next Level", "Restart Level", "Menu", "Quit"};
    private static int exitCodes[] ={EXIT_GAME_NEXT_LEVEL,EXIT_GAME_RESTART_LEVEL,EXIT_MENU,EXIT_QUIT};
    private static int exitCodes2[] ={EXIT_GAME_NEXT_RACE_LEVEL,EXIT_GAME_RESTART_RACE_LEVEL,EXIT_MENU,EXIT_QUIT};
    private static Color overlayColor = new Color(Color.WHITE);

    private AssetManager assetManager;


    private int currSelection = 0;
    private int changeCooldown = 0;

    public void preLoadContent(AssetManager manager) {
        assetManager = manager;
        if (assetState != AssetState.EMPTY) return;

        assetState = AssetState.LOADING;

        FreetypeFontLoader.FreeTypeFontLoaderParameter geometricNormal = new
                FreetypeFontLoader.FreeTypeFontLoaderParameter();
        geometricNormal.fontFileName = GEOMETRIC_FONT_FILE;
        geometricNormal.fontParameters.size = (int)(45 * Gdx.graphics.getDensity());
        geometricNormal.fontParameters.color = Color.BROWN;
        assetManager.load("Normal" + GEOMETRIC_FONT_FILE, BitmapFont.class, geometricNormal);
        assets.add("Normal" + GEOMETRIC_FONT_FILE);
        FreetypeFontLoader.FreeTypeFontLoaderParameter geometricSelected = new
                FreetypeFontLoader.FreeTypeFontLoaderParameter();
        geometricSelected.fontFileName = GEOMETRIC_FONT_FILE;
        geometricSelected.fontParameters.size = (int)(52 * Gdx.graphics.getDensity());
        geometricSelected.fontParameters.color = Color.FIREBRICK;
        assetManager.load("Selected" + GEOMETRIC_FONT_FILE, BitmapFont.class, geometricSelected);
        assets.add("Selected" + GEOMETRIC_FONT_FILE);

        manager.load(TEXTBOX_FILE, Texture.class);
        assets.add(TEXTBOX_FILE);
        manager.load(OVERLAY_FILE, Texture.class);
        assets.add(OVERLAY_FILE);
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
        if (assetState != AssetState.LOADING) return;

        geometricFontNormal = manager.get("Normal" + GEOMETRIC_FONT_FILE, BitmapFont.class);
        geometricFontSelected = manager.get("Selected" + GEOMETRIC_FONT_FILE, BitmapFont.class);

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

    public void update(float dt, ScreenListener listener) {
        InputController input = InputController.getInstance(0);

        if (changeCooldown > 0) changeCooldown --;

        if (changeCooldown == 0 && Math.abs(input.getVerticalL()) > 0.5) {
            SoundController.get(SoundController.SCROLL_SOUND).play();
            currSelection = (currSelection + (input.getVerticalL() > 0.5 ? -1 : 1)+ menuOptions.length) % menuOptions.length;
            changeCooldown = MENU_CHANGE_COOLDOWN;
        }
        if (input.didSelect()){
            SoundController.get(SoundController.SELECT_SOUND).play();
            listener.exitScreen(this, exitCodes[currSelection]);
        }
    }

    public void update(float dt, ScreenListener listener,boolean race) {
        InputController input = InputController.getInstance(0);

        if (changeCooldown > 0) changeCooldown --;

        if (changeCooldown == 0 && Math.abs(input.getVerticalL()) > 0.5) {
            SoundController.get(SoundController.SCROLL_SOUND).play();
            currSelection = (currSelection + (input.getVerticalL() > 0.5 ? -1 : (input.getVerticalL() < -0.5 ? 1 : 0))+ menuOptions.length) % menuOptions.length;
            changeCooldown = MENU_CHANGE_COOLDOWN;
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
        float textboxWidth = canvas.getWidth() * 0.5f;
        float textboxHeight = canvas.getHeight() * 0.56f;
        canvas.draw(overlay, overlayColor, 0, bottomOfScreen, canvas.getWidth(), canvas.getHeight());
        canvas.draw(textbox, Color.WHITE, (canvas.getWidth() - textboxWidth) / 2,
                bottomOfScreen + (canvas.getHeight() - textboxHeight) / 2,
                textboxWidth, textboxHeight);
        float drawY = bottomOfScreen + canvas.getHeight() * 0.116f;
        canvas.drawTextCentered("YOU WON!", geometricFontNormal, drawY);
        BitmapFont font;
        drawY -= canvas.getHeight() * 0.066f;

        for (int i = 0; i < menuOptions.length; i++) {
            font = currSelection == i ? geometricFontSelected : geometricFontNormal;
            canvas.drawTextCentered(menuOptions[i], font, drawY);
            drawY -= canvas.getHeight() * 0.055;
        }
        canvas.end();
    }


    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

}
