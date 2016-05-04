package beigegang.mountsputnik;

import beigegang.util.XBox360Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

import static beigegang.mountsputnik.Constants.*;

public class InputController {
	private boolean triggerScheme;
	private boolean stickScheme;
	
	/** Singleton instance for the controller */
	private static InputController theController = null;

	/**
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) theController = new InputController();
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
	/** if animation help wanted */
	public boolean animationPressed;

	/** Whether the left arm action button was pressed. */
	private boolean leftArmPressed;
	private boolean leftArmPrevious;
	/** Whether the right arm action button was pressed. */
	private boolean rightArmPressed;
	private boolean rightArmPrevious;
	/** Whether the left leg action button was pressed. */
	private boolean leftLegPressed;
	private boolean leftLegPrevious;
	/** Whether the right leg action button was pressed. */
	private boolean rightLegPressed;
	private boolean rightLegPrevious;

	/** How much did we move horizontally left stick? */
	private float horizontalL;
	/** How much did we move vertically left stick? */
	private float verticalL;

	/** How much did we move horizontally right stick? */
	private float horizontalR;
	/** How much did we move vertically right stick? */
	private float verticalR;

	/** An X-Box controller (if it is connected) */
	private XBox360Controller xbox;

	/**
	 * holds any extremities who's buttons are pressed during this timestep. keeps order of pressing intact
	 */
	private Array<Integer> orderPressed = new Array<Integer>();

	public Array<Integer> getOrderPressed(){
		return orderPressed;
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
	
	public boolean getStickScheme(){
		return stickScheme;
	}
	
	public boolean getTriggerScheme(){
		return triggerScheme;
	}
	
	public void swapTriggerScheme(){
		triggerScheme = !triggerScheme;
	}
	
	public void swapStickScheme(){
		stickScheme = !stickScheme;
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
	 * Returns true if the left arm action button was released.
	 *
	 * @return true if the left arm action button was just released.
	 */
	public boolean releasedLeftArm() {
		return leftArmPrevious && ! leftArmPressed;
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
	 * Returns true if the left arm action button was released.
	 *
	 * @return true if the left arm action button was just released.
	 */
	public boolean releasedRightArm() {
		return rightArmPrevious && ! rightArmPressed;
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
	 * Returns true if the left arm action button was released.
	 *
	 * @return true if the left arm action button was just released.
	 */
	public boolean releasedLeftLeg() {
		return leftLegPrevious && ! leftLegPressed;
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
	 * Returns true if the left arm action button was released.
	 *
	 * @return true if the left arm action button was just released.
	 */
	public boolean releasedRightLeg() {
		return rightLegPrevious && ! rightLegPressed;
	}

	/**
	 * Returns true if the menu button was pressed.
	 *
	 * @return true if the menu button was pressed.
	 */
	public boolean didMenu() {
		return menuPressed && !menuPrevious;
	}
	public boolean watchAnimation(){
		return animationPressed;
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
	private InputController() {
		// If we have a game-pad for id, then use it.
		xbox = new XBox360Controller(0);
		triggerScheme = false; //TODO read in values from file
		stickScheme = false;
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 */
	public void readInput() {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		menuPrevious = menuPressed;
		debugPrevious = debugPressed;
		selectPrevious = selectPressed;
		backPrevious = backPressed;
		leftArmPrevious = leftArmPressed;
		rightArmPrevious = rightArmPressed;
		leftLegPrevious = leftLegPressed;
		rightLegPrevious = rightLegPressed;

		// Check to see if a GamePad is connected
		if (xbox.isConnected()) {
			readGamepad();
			readKeyboard(true); // Read as a back-up
		} else readKeyboard(false);

		adjustOrderPressed(leftArmPressed, HAND_LEFT);
		adjustOrderPressed(rightArmPressed, HAND_RIGHT);
		adjustOrderPressed(leftLegPressed, FOOT_LEFT);
		adjustOrderPressed(rightLegPressed, FOOT_RIGHT);
	}

	private void adjustOrderPressed(boolean pressed, int part) {
		if (pressed && !orderPressed.contains(part, true))
			orderPressed.add(part);
		else if (!pressed) orderPressed.removeValue(part, true);
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 */
	private void readGamepad() {
		menuPressed = xbox.getStart();
		debugPressed  = xbox.getBack();
		selectPressed = xbox.getA();
		backPressed = xbox.getB();
		animationPressed = xbox.getY();

		leftArmPressed = triggerScheme ? xbox.getLeftTrigger() > 0.5 : xbox.getLB();
		rightArmPressed = triggerScheme ? 
				(xbox.getRightTrigger() < -0.5 && xbox.getRightTrigger() != -1.0f) : xbox.getRB();
		leftLegPressed = triggerScheme ? xbox.getLB() : xbox.getLeftTrigger() > 0.5;
		rightLegPressed = triggerScheme ? xbox.getRB() :
				xbox.getRightTrigger() < -0.5 && xbox.getRightTrigger() != -1.0f;

		// Movement based on change
		horizontalL  =  stickScheme  ?   xbox.getRightX()  :   xbox.getLeftX();
		verticalL    =  stickScheme  ?  -xbox.getRightY()  :  -xbox.getLeftY();
		horizontalR  =  stickScheme  ?   xbox.getLeftX()   :   xbox.getRightX();
		verticalR    =  stickScheme  ?  -xbox.getLeftY()   :  -xbox.getRightY();
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
	private void readKeyboard(boolean secondary) {
		// Give priority to gamepad results
		menuPressed = (secondary && menuPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
		debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.D));
		selectPressed  = (secondary && selectPressed) || (Gdx.input.isKeyPressed(Input.Keys.ENTER));
		backPressed = (secondary && backPressed) || (Gdx.input.isKeyPressed(Input.Keys.B));
		
		leftArmPressed = (secondary && leftArmPressed) || (Gdx.input.isKeyPressed(Input.Keys.Q));
		rightArmPressed = (secondary && rightArmPressed) || (Gdx.input.isKeyPressed(Input.Keys.W));
		leftLegPressed = (secondary && leftLegPressed) || (Gdx.input.isKeyPressed(Input.Keys.A));
		rightLegPressed = (secondary && rightLegPressed) || (Gdx.input.isKeyPressed(Input.Keys.S));

		horizontalL = (secondary ? horizontalL : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			horizontalL += 1.0f;
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
			horizontalL -= 1.0f;

		verticalL = (secondary ? verticalL : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.UP))
			verticalL += 1.0f;
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
			verticalL -= 1.0f;

		horizontalR = (secondary? horizontalR :0);
		verticalR = (secondary? verticalR :0);
	}
}
