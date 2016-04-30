package beigegang.mountsputnik;

import beigegang.util.XBox360Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static beigegang.mountsputnik.Constants.*;
import static beigegang.mountsputnik.Constants.HAND_RIGHT;

public class InputController {	
	/** Singleton instance for the controller */
	private static InputController theController = null;
	
	/** 
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) {
			theController = new InputController();
		}
		return theController;
	}

	// Fields to manage buttons
	/** Whether the menu button was pressed. */
	private boolean menuPressed;
	private boolean menuPrevious;
	/** Whether the debug button was pressed. */
	private boolean debugPressed;
	private boolean debugPrevious;
	/** Whether the select button was pressed (menus). */
	private boolean selectPressed;
	private boolean selectPrevious;
	/** Whether the back button was pressed (menus). */
	private boolean backPressed;
	private boolean backPrevious;
	
	/** Whether the left arm action button was pressed. */
	private boolean leftArmPressed;
	/** Whether the right arm action button was pressed. */
	private boolean rightArmPressed;
	/** Whether the left leg action button was pressed. */
	private boolean leftLegPressed;
	/** Whether the right leg action button was pressed. */
	private boolean rightLegPressed;
	
	/** How much did we move horizontally left stick? */
	private float horizontalL;
	/** How much did we move vertically left stick? */
	private float verticalL;

	/** How much did we move horizontally right stick? */
	private float horizontalR;
	/** How much did we move vertically right stick? */
	private float verticalR;

	/** An X-Box controller (if it is connected) */
	XBox360Controller xbox;
	/**
	 * holds any extremities who's buttons were just released this timestep. cleared out every timestep
	 */
	private Array<Integer> justReleased = new Array<Integer>();
	/**
	 * holds any extremities who's buttons are pressed during this timestep. keeps order of pressing intact
	 */
	private Array<Integer> nextToPress = new Array<Integer>();

	public Array<Integer> getJustReleased(){
		return justReleased;
	}
	public Array<Integer> getButtonsPressed(){
		return nextToPress;
	}
	/**
	 * if button was not pressed on the previous turn but is pressed now, add to nextToPress
	 * note: cannot use set because order must be preserved for accurate/predictive control by player
	 * @param part
	 * @return true if part was just pressed, false otherwise
	 * @author Jacob
	 */
	public boolean addToButtonsPressed(int part) {
		boolean notRedundant = !nextToPress.contains(part, true);
		if(notRedundant)
			nextToPress.add(part);
		return notRedundant;
	}

	/**
	 * checks if that extremity was released on this timestep by player, if so adds to justReleased
	 * @param part
	 * @return true if part was just released, false otherwise
	 * @author Jacob
	 */
	public boolean checkIfJustReleased(int part) {
		boolean present = nextToPress.removeValue(part, true);
		if (present)
			justReleased.add(part);
		return present;
	}

	/**
	 * Returns the amount of sideways movement from left stick.
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement from left stick.
	 */
	public float getHorizontalL() {
		return horizontalL;
	}
	
	/**
	 * Returns the amount of vertical movement from left stick.
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement from left stick.
	 */
	public float getVerticalL() {
		return verticalL;
	}
	/**
	 * Returns the amount of sideways movement from right stick.
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement from right stick.
	 */
	public float getHorizontalR() {
		return horizontalR;
	}

	/**
	 * Returns the amount of vertical movement from right stick.
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement from right stick.
	 */
	public float getVerticalR() {
		return verticalR;
	}

	/**
	 * Returns true if the left arm action button was pressed.
	 *
	 * This is a sustained button. It will returns true as long as the player
	 * holds it down.
	 *
	 * @return true if the left arm action button was pressed.
	 */
	public boolean didLeftArm() {
		return leftArmPressed;
	}

	/**
	 * Returns true if the right arm action button was pressed.
	 *
	 * This is a sustained button. It will returns true as long as the player
	 * holds it down.
	 *
	 * @return true if the left arm action button was pressed.
	 */
	public boolean didRightArm() {
		return rightArmPressed;
	}

	/**
	 * Returns true if the left leg action button was pressed.
	 *
	 * This is a sustained button. It will returns true as long as the player
	 * holds it down.
	 *
	 * @return true if the left leg action button was pressed.
	 */
	public boolean didLeftLeg() {
		return leftLegPressed;
	}

	/**
	 * Returns true if the right leg action button was pressed.
	 *
	 * This is a sustained button. It will returns true as long as the player
	 * holds it down.
	 *
	 * @return true if the right leg action button was pressed.
	 */
	public boolean didRightLeg() {
		return rightLegPressed;
	}

	/**
	 * Returns true if the menu button was pressed.
	 *
	 * @return true if the menu button was pressed.
	 */
	public boolean didMenu() {
		return menuPressed && !menuPrevious;
	}

	/**
	 * Returns true if the player wants to select menu item.
	 *
	 * @return true if the player wants to select menu item.
	 */
	public boolean didSelect() {
		return selectPressed && !selectPrevious;
	}
	
	/**
	 * Returns true if the player wants to go back in menu.
	 *
	 * @return true if the player wants to go back in menu.
	 */
	public boolean didBack() {
		return backPressed && !backPrevious;
	}
	
	/**
	 * Returns true if the player wants to go toggle the debug mode.
	 *
	 * @return true if the player wants to go toggle the debug mode.
	 */
	public boolean didDebug() {
		return debugPressed && !debugPrevious;
	}
	
	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() { 
		// If we have a game-pad for id, then use it.
		xbox = new XBox360Controller(0);
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	public void readInput(Rectangle bounds, Vector2 scale) {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		menuPrevious = menuPressed;
		debugPrevious = debugPressed;
		selectPrevious = selectPressed;
		backPrevious = backPressed;
		justReleased.clear();

		// Check to see if a GamePad is connected
		if (xbox.isConnected()) {
			readGamepad(bounds, scale);
			readKeyboard(bounds, scale, true); // Read as a back-up
		} else {
			readKeyboard(bounds, scale, false);
		}
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	private void readGamepad(Rectangle bounds, Vector2 scale) {
		menuPressed = xbox.getStart();
		debugPressed  = xbox.getBack();
		selectPressed = xbox.getA();
		backPressed = xbox.getB();
		
		leftArmPressed = xbox.getLB();
		rightArmPressed = xbox.getRB();
		leftLegPressed = xbox.getLeftTrigger() > 0.5;
		rightLegPressed = xbox.getRightTrigger() < -0.5 && xbox.getRightTrigger() != -1.0f; //		System.out.println(xbox.getRightTrigger());

		boolean a = leftLegPressed ? addToButtonsPressed((FOOT_LEFT)) : checkIfJustReleased(FOOT_LEFT);
		boolean b = rightLegPressed ? addToButtonsPressed((FOOT_RIGHT)) : checkIfJustReleased(FOOT_RIGHT);
		boolean c = leftArmPressed ? addToButtonsPressed((HAND_LEFT)) : checkIfJustReleased(HAND_LEFT);
		boolean d = rightArmPressed ? addToButtonsPressed((HAND_RIGHT)) : checkIfJustReleased(HAND_RIGHT);
//
		// Movement based on change
		horizontalL = xbox.getLeftX();
		verticalL = -xbox.getLeftY();

		if (Constants.TORSO_MODE || Constants.TWO_LIMB_MODE) {
			horizontalR = xbox.getRightX();
			verticalR = -xbox.getRightY();
		}
	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary) {
		// Give priority to gamepad results
		menuPressed = (secondary && menuPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
		debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.D));
		selectPressed  = (secondary && selectPressed) || (Gdx.input.isKeyPressed(Input.Keys.ENTER));
		backPressed = (secondary && backPressed) || (Gdx.input.isKeyPressed(Input.Keys.B));
		
		leftArmPressed = (secondary && leftArmPressed) || (Gdx.input.isKeyPressed(Input.Keys.Q));
		rightArmPressed = (secondary && rightArmPressed) || (Gdx.input.isKeyPressed(Input.Keys.W));
		leftLegPressed = (secondary && leftLegPressed) || (Gdx.input.isKeyPressed(Input.Keys.A));
		rightLegPressed = (secondary && rightLegPressed) || (Gdx.input.isKeyPressed(Input.Keys.S));

		boolean a = leftLegPressed ? addToButtonsPressed((FOOT_LEFT)) : checkIfJustReleased(FOOT_LEFT);
		boolean b = rightLegPressed ? addToButtonsPressed((FOOT_RIGHT)) : checkIfJustReleased(FOOT_RIGHT);
		boolean c = leftArmPressed ? addToButtonsPressed((HAND_LEFT)) : checkIfJustReleased(HAND_LEFT);
		boolean d = rightArmPressed ? addToButtonsPressed((HAND_RIGHT)) : checkIfJustReleased(HAND_RIGHT);

		horizontalL = (secondary ? horizontalL : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			horizontalL += 1.0f;
		 }
		 if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
		 	horizontalL -= 1.0f;
		 }
		 
		 verticalL = (secondary ? verticalL : 0.0f);
		 if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
		 	verticalL += 1.0f;
		 }
		 if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
		 	verticalL -= 1.0f;
		 }

		horizontalR = (secondary? horizontalR :0);
		verticalR = (secondary? verticalR :0);

	}
}
