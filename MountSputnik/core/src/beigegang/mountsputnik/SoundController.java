package beigegang.mountsputnik;

import java.util.HashMap;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;

/**
 *  Static controller class for managing sound.
 *
 *  Menu sounds created by user broumbroum on www.freesound.org
 *  Ungrip sound created by user LittleRobotSoundFactory on www.freesound.org
 */
public class SoundController {
    // Static names to the sounds
    /** Selecting a menu item*/
    public static final String SELECT_SOUND = "select";
    /** Scrolling through menu items */
    public static final String SCROLL_SOUND = "scroll";
    /** Backing out of a submenu or choosing an invalid option */
    public static final String DECLINE_SOUND = "decline";
    /** Gripping a handhold */
    public static final String GRIP_SOUND = "grip";
    /** Ungripping a handhold*/
    public static final String UNGRIP_SOUND = "ungrip";
    /** Handhold crumbling*/
    public static final String CRUMBLE_SOUND = "crumble";
    /** Slipping from handhold */
    public static final String SLIP_SOUND = "slip";
    /** Colliding with falling obstacle*/
    public static final String COLLIDE_SOUND = "collide";

    /** Hash map storing references to sound assets (after they are loaded) */
    private static HashMap<String, Sound> soundBank;

    // Files storing the sound references
    /** Selecting a menu item file*/
    public static final String SELECT_FILE = "Sound/select.wav";
    /** Scrolling through menu items file*/
    public static final String SCROLL_FILE = "Sound/scroll.wav";
    /** Backing out of a submenu or choosing an invalid option file*/
    public static final String DECLINE_FILE = "Sound/decline.wav";
    /** Gripping a handhold file*/
    public static final String GRIP_FILE = "Sound/grip.mp3";
    /** Ungripping a handhold file*/
    public static final String UNGRIP_FILE = "Sound/grip.wav";
    /** Handhold crumbling*/
    public static final String CRUMBLE_FILE = "Sound/crumble.wav";
    /** Slipping from handhold */
    public static final String SLIP_FILE = "Sound/slip.wav";
    /** Colliding with falling obstacle*/
    public static final String COLLIDE_FILE = "Sound/collide.wav";

    /**
     * Preloads the assets for this Sound controller.
     *
     * The asset manager for LibGDX is asynchronous.  That means that you
     * tell it what to load and then wait while it loads them.  This is
     * the first step: telling it what to load.
     *
     * @param manager Reference to global asset manager.
     */
    public static void PreLoadContent(AssetManager manager) {
        manager.load(SELECT_FILE,Sound.class);
        manager.load(SCROLL_FILE,Sound.class);
        manager.load(DECLINE_FILE,Sound.class);
        manager.load(GRIP_FILE,Sound.class);
        manager.load(UNGRIP_FILE,Sound.class);
        manager.load(CRUMBLE_FILE,Sound.class);
        manager.load(SLIP_FILE,Sound.class);
        //manager.load(COLLIDE_FILE,Sound.class);
    }

    /**
     * Loads the assets for this Sound controller.
     *
     * The asset manager for LibGDX is asynchronous.  That means that you
     * tell it what to load and then wait while it loads them.  This is
     * the second step: extracting assets from the manager after it has
     * finished loading them.
     *
     * @param manager Reference to global asset manager.
     */
    public static void LoadContent(AssetManager manager) {
        soundBank = new HashMap<String, Sound>();
        if (manager.isLoaded(SELECT_FILE)) {
            soundBank.put(SELECT_SOUND,manager.get(SELECT_FILE,Sound.class));
        }
        if (manager.isLoaded(SCROLL_FILE)) {
            soundBank.put(SCROLL_SOUND,manager.get(SCROLL_FILE,Sound.class));
        }
        if (manager.isLoaded(DECLINE_FILE)) {
            soundBank.put(DECLINE_SOUND,manager.get(DECLINE_FILE,Sound.class));
        }
        if (manager.isLoaded(GRIP_FILE)) {
            soundBank.put(GRIP_SOUND,manager.get(GRIP_FILE,Sound.class));
        }
        if (manager.isLoaded(UNGRIP_FILE)) {
            soundBank.put(UNGRIP_SOUND,manager.get(UNGRIP_FILE,Sound.class));
        }
        if (manager.isLoaded(CRUMBLE_FILE)) {
            soundBank.put(CRUMBLE_SOUND,manager.get(CRUMBLE_FILE,Sound.class));
        }
        if (manager.isLoaded(SLIP_FILE)) {
            soundBank.put(SLIP_SOUND,manager.get(SLIP_FILE,Sound.class));
        }
//        if (manager.isLoaded(COLLIDE_FILE)) {
//            soundBank.put(COLLIDE_SOUND,manager.get(COLLIDE_FILE,Sound.class));
//        }
    }

    /**
     * Unloads the assets for this GameCanvas
     *
     * This method erases the static variables.  It also deletes the
     * associated textures from the assert manager.
     *
     * @param manager Reference to global asset manager.
     */
    public static void UnloadContent(AssetManager manager) {
        if (soundBank != null) {
            soundBank.clear();
            soundBank = null;
            manager.unload(SELECT_FILE);
            manager.unload(SCROLL_FILE);
            manager.unload(DECLINE_FILE);
            manager.unload(GRIP_FILE);
            manager.unload(UNGRIP_FILE);
            manager.unload(CRUMBLE_FILE);
            manager.unload(SLIP_FILE);
            //manager.unload(COLLIDE_FILE);
        }
    }

    /**
     * Returns the sound for the given name
     *
     * @return the sound for the given name
     */
    public static Sound get(String key) {
        return soundBank.get(key);
    }
}