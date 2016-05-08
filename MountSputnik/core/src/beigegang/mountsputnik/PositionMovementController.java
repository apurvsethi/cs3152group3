package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;

import static beigegang.mountsputnik.Constants.*;

/**
 * Created by Apurv on 4/20/16.
 *
 * Movement controller to move the player based on exact position. Calculates
 * constraints as needed to ensure that the player's wanted movement is
 * allowed, does it if possible.
 */
public class PositionMovementController {
    // Sensitivity for moving crosshair with gameplay
    private static final float GP_ACCELERATE = 1.0f;
    private static final float GP_MAX_SPEED  = 10.0f;
    private static final float GP_THRESHOLD  = 0.50f;

    private CharacterModel character;
    private Vector2 posCache;
    private Vector2 torsoCache;
    private Vector2 crossCache;
    private Vector2 scale;

    private float armLength;
    private float forearmLength;
    private float thighLength;
    private float shinLength;
    private float momentum;

    public PositionMovementController(CharacterModel character, Vector2 scale) {
        this.character = character;
        posCache = new Vector2();
        torsoCache = new Vector2();
        crossCache = new Vector2();
        this.scale = scale;

        armLength = (character.parts.get(HAND_LEFT).getPosition().
                sub(character.getInnerJoint(ARM_LEFT).getAnchorA())).len() / 2;
        forearmLength = armLength;
        thighLength = (character.parts.get(FOOT_LEFT).getPosition().
                sub(character.getInnerJoint(THIGH_LEFT).getAnchorA())).len() / 2;
        shinLength = thighLength;
        momentum = 0f;
    }

    public void moveCharacter() {
        InputController input = InputController.getInstance();
        float horizontalL = input.getHorizontalL();
        float verticalL = input.getVerticalL();

        if (input.didLeftArm() && input.getOrderPressed().first() == HAND_LEFT)
            moveLimb(ARM_LEFT, FOREARM_LEFT, HAND_LEFT, horizontalL, verticalL, true);
        else if (input.releasedLeftArm()) lockLimb(ARM_LEFT, FOREARM_LEFT);
        if (input.didRightArm() && input.getOrderPressed().first() == HAND_RIGHT)
            moveLimb(ARM_RIGHT, FOREARM_RIGHT, HAND_RIGHT, horizontalL, verticalL, true);
        else if (input.releasedRightArm()) lockLimb(ARM_RIGHT, FOREARM_RIGHT);
        if (input.didLeftLeg() && input.getOrderPressed().first() == FOOT_LEFT)
            moveLimb(THIGH_LEFT, SHIN_LEFT, FOOT_LEFT, horizontalL, verticalL, false);
        else if (input.releasedLeftLeg()) lockLimb(THIGH_LEFT, SHIN_LEFT);
        if (input.didRightLeg() && input.getOrderPressed().first() == FOOT_RIGHT)
            moveLimb(THIGH_RIGHT, SHIN_RIGHT, FOOT_RIGHT, horizontalL, verticalL, false);
        else if (input.releasedRightLeg()) lockLimb(THIGH_RIGHT, SHIN_RIGHT);

        moveTorso(input.getHorizontalR(), input.getVerticalR());
    }

    private void moveLimb(int root, int mid, int extremity,
                          float horizontalJoystick, float verticalJoystick,
                          boolean isArm) {
        PartModel extremityPart = character.parts.get(extremity);
        RevoluteJoint rootJoint = (RevoluteJoint) character.getInnerJoint(root);
        RevoluteJoint midJoint = (RevoluteJoint) character.getJoint(root, mid);

        Vector2 initial = extremityPart.getPosition();
        posCache.set(initial);

        crossCache.set(horizontalJoystick, verticalJoystick);
        if (crossCache.len2() > GP_THRESHOLD) {
            momentum += GP_ACCELERATE;
            momentum = Math.min(momentum, GP_MAX_SPEED);
            crossCache.scl(momentum);
            crossCache.scl(1/scale.x,1/scale.y);
            posCache.add(crossCache);

            adjustToRadius(rootJoint, isArm);
            extremityPart.setPosition(posCache);
        }
        else momentum = 0;

        adjustTowardsLimits(rootJoint, midJoint);
    }

    private void adjustToRadius(RevoluteJoint rootJoint, boolean isArm) {
        float radius = isArm ? armLength + forearmLength : thighLength + shinLength;
        if (!(Math.pow((posCache.x - rootJoint.getAnchorA().x), 2)
                + Math.pow((posCache.y - rootJoint.getAnchorA().y), 2)
                < Math.pow(radius, 2))) {
            float vx = posCache.x - rootJoint.getAnchorA().x;
            float vy = posCache.y - rootJoint.getAnchorA().y;
            float magV = (float)Math.sqrt(vx*vx + vy*vy);
            posCache.set(rootJoint.getAnchorA().x + vx / magV * radius,
                    rootJoint.getAnchorA().y + vy / magV * radius);
        }
    }

    private void adjustTowardsLimits(RevoluteJoint rootJoint, RevoluteJoint midJoint) {
        moveTowardLimits(rootJoint);
        moveTowardLimits(midJoint);
        if (midJoint.getMotorSpeed() != 0 && rootJoint.getMotorSpeed() == 0)
            rootJoint.setMotorSpeed(-midJoint.getMotorSpeed() / 2);
    }

    private void moveTowardLimits(RevoluteJoint joint) {
        if (joint.getJointAngle() < joint.getLowerLimit())
            joint.setMotorSpeed(5);
        else if (joint.getJointAngle() > joint.getUpperLimit())
            joint.setMotorSpeed(-5);
        else joint.setMotorSpeed(0);
    }

    private void lockLimb(int root, int mid) {
        RevoluteJoint rootJoint = (RevoluteJoint) character.getInnerJoint(root);
        RevoluteJoint midJoint = (RevoluteJoint) character.getJoint(root, mid);
        rootJoint.setMotorSpeed(0);
        midJoint.setMotorSpeed(0);
    }

    private void moveTorso(float horizontalJoystick, float verticalJoystick) {
        torsoCache.set(CONSTANT_X_FORCE * 2f, CONSTANT_X_FORCE * 2f);
        boolean leftHand = ((ExtremityModel) (character.parts.get(HAND_LEFT))).isGripped();
        boolean rightHand = ((ExtremityModel) (character.parts.get(HAND_RIGHT))).isGripped();
        boolean leftLeg = ((ExtremityModel) (character.parts.get(FOOT_LEFT))).isGripped();
        boolean rightLeg = ((ExtremityModel) (character.parts.get(FOOT_RIGHT))).isGripped();

        int counter = 0;
        if (leftHand) counter++;
        if (rightHand) counter++;
        if (leftLeg) counter++;
        if (rightLeg) counter++;
        if (counter > 2) counter +=2;
        torsoCache.scl(counter);

        if (leftHand|| rightHand || leftLeg || rightLeg) {
            character.parts.get(CHEST).body.applyForceToCenter(torsoCache.x * horizontalJoystick, 0, true);
            character.parts.get(CHEST).body.applyForceToCenter(0, torsoCache.y * verticalJoystick, true);
        }
    }

    public void dispose() {
        character = null;
        posCache = null;
        torsoCache = null;
        crossCache = null;
        scale = null;
    }
}
