package beigegang.mountsputnik;

import beigegang.util.PooledList;
import beigegang.util.ScreenListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static beigegang.mountsputnik.Constants.*;

public class VictoryMode extends ModeController {

    /**
     * Track asset loading from all instances and subclasses
     */
    protected AssetState assetState = AssetState.EMPTY;

    private static final String FOREGROUND_FILE = "Menu/PauseMenu/Foreground.png";
    private static final String WIN_FILE = "Menu/PauseMenu/LevelComplete.png";
    private static final String MENU_OPTION_FILES[] = {"Menu/PauseMenu/NextLevel.png", "Menu/PauseMenu/Restart.png","Menu/PauseMenu/Menu.png","Menu/PauseMenu/Quit.png"};
    private static final String STAT_OPTION_FILES[] = {"Menu/PauseMenu/TimeTaken.png","Menu/PauseMenu/DistanceClimbed.png","Menu/PauseMenu/LowestEnergy.png","Menu/PauseMenu/CheckpointRestarts.png","Menu/PauseMenu/TotalFalls.png"};
    /**
     * Texture asset for files used, parts, etc.
     */
    private static TextureRegion foreground;
    private static TextureRegion winMessage;
    private static TextureRegion[] menuOptions = new TextureRegion[MENU_OPTION_FILES.length];

    private static int exitCodes[] ={EXIT_GAME_NEXT_LEVEL,EXIT_GAME_RESTART_LEVEL,EXIT_MENU,EXIT_QUIT};

    private AssetManager assetManager;


    private int currSelection = 0;
    private int changeCooldown = 0;

    public void preLoadContent(AssetManager manager) {
        assetManager = manager;
        if (assetState != AssetState.EMPTY) return;

        assetState = AssetState.LOADING;
        manager.load(FOREGROUND_FILE, Texture.class);
        assets.add(FOREGROUND_FILE);
        manager.load(WIN_FILE, Texture.class);
        assets.add(WIN_FILE);
        for (String MENU_OPTION_FILE : MENU_OPTION_FILES) {
            manager.load(MENU_OPTION_FILE, Texture.class);
            assets.add(MENU_OPTION_FILE);
        }
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

        foreground = createTexture(manager, FOREGROUND_FILE, false);
        winMessage = createTexture(manager, WIN_FILE, false);

        for (int i = 0; i < MENU_OPTION_FILES.length; i++) {
            menuOptions[i] = createTexture(manager, MENU_OPTION_FILES[i], false);
        }
        assetState = AssetState.COMPLETE;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        currSelection = 0;
        SoundController.get(SoundController.DECLINE_SOUND).play();
    }

    @Override
    public void update(float dt){};

    public void update(float dt, ScreenListener listener) {
        // TODO Auto-generated method stub
        InputController input = InputController.getInstance();

        if (changeCooldown > 0) changeCooldown --;

        if (changeCooldown == 0 && Math.abs(input.getVerticalL()) > 0.5) {
            SoundController.get(SoundController.SCROLL_SOUND).play();
            currSelection = (currSelection + (input.getVerticalL() > 0.5 ? -1 : (input.getVerticalL() < -0.5 ? 1 : 0))+ menuOptions.length) % menuOptions.length;
            changeCooldown = MENU_CHANGE_COOLDOWN;
        }
        if (input.didSelect()){
            SoundController.get(SoundController.SELECT_SOUND).play();
            listener.exitScreen(this, exitCodes[currSelection]);
        }

    }

    public void draw(GameCanvas canvas) {

        float y = canvas.getCamera().position.y - canvas.getHeight() / 2;

        canvas.begin();
        canvas.draw(foreground, Color.WHITE, 0, y,canvas.getWidth(),canvas.getHeight());
        canvas.end();
        canvas.begin();
        float drawY = canvas.getHeight() * 5f/11f + (MENU_ITEM_HEIGHT * menuOptions.length / 2) + y;
        canvas.draw(winMessage, Color.WHITE, winMessage.getRegionWidth() / 2,
                winMessage.getRegionHeight()/ 2, (canvas.getWidth() / 2), drawY + MENU_ITEM_HEIGHT, 0, 1f, 1f);
        for (int i = 0; i < menuOptions.length; i++){
            if (i == currSelection){
                canvas.draw(menuOptions[i], Color.TEAL, menuOptions[i].getRegionWidth() / 2,
                        menuOptions[i].getRegionHeight()/ 2, (canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
            } else {
                canvas.draw(menuOptions[i], Color.WHITE,  menuOptions[i].getRegionWidth() / 2,
                        menuOptions[i].getRegionHeight()/ 2,(canvas.getWidth() / 2), drawY, 0, 0.75f, 0.75f);
            }
            drawY -= MENU_ITEM_HEIGHT;
        }

        //draw menu objects
        canvas.end();

    }


    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

}
