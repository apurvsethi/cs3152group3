package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.utils.Array;

import static beigegang.mountsputnik.Constants.*;

/**
 * Created by Apurv on 4/20/16.
 *
 * Movement controller to move the player based on exact position. Calculates
 * constraints as needed to ensure that the player's wanted movement is
 * allowed, does it if possible.
 */
public class MovementController {
    // Sensitivity for moving crosshair with gameplay
    private static final float GP_ACCELERATE = 1.1f;
    private static final float GP_MAX_SPEED  = 11.0f;
    private static final float GP_THRESHOLD  = 0.20f;

    private CharacterModel character;
    private Vector2 originalCache;
    private Vector2 posCache;
    private Vector2 torsoCache;
    private Vector2 crossCache;
    private Vector2 scale;

    private float armLength;
    private float forearmLength;
    private float thighLength;
    private float shinLength;
    private float momentum;

    public MovementController(CharacterModel character, Vector2 scale) {
        this.character = character;
        originalCache = new Vector2();
        posCache = new Vector2();
        torsoCache = new Vector2();
        crossCache = new Vector2();
        this.scale = scale;

        armLength = (character.getJoint(ARM_LEFT, FOREARM_LEFT).getAnchorA().
                sub(character.getInnerJoint(ARM_LEFT).getAnchorA())).len();
        forearmLength = character.parts.get(HAND_LEFT).getPosition().
                sub(character.getJoint(ARM_LEFT, FOREARM_LEFT).getAnchorA()).len();
        thighLength = (character.getJoint(THIGH_LEFT, SHIN_LEFT).getAnchorA().
                sub(character.getInnerJoint(THIGH_LEFT).getAnchorA())).len();
        shinLength = character.parts.get(FOOT_LEFT).getPosition().
                sub(character.getJoint(THIGH_LEFT, SHIN_LEFT).getAnchorA()).len();
        momentum = 0f;
    }

    public void moveCharacter(float horizontalL, float verticalL, float horizontalR, float verticalR,
                              Array<Integer> nextToPress, Array<Integer> justReleased) {
        boolean np = nextToPress.size > 0;
        boolean jr = justReleased.size > 0;

        if (np && nextToPress.get(0) == HAND_LEFT)
            moveLimb(ARM_LEFT, FOREARM_LEFT, HAND_LEFT, horizontalL, verticalL, true, true);
        else if (jr && justReleased.contains(HAND_LEFT,false)) lockLimb(ARM_LEFT, FOREARM_LEFT);
        if (np && nextToPress.get(0) == HAND_RIGHT)
            moveLimb(ARM_RIGHT, FOREARM_RIGHT, HAND_RIGHT, horizontalL, verticalL, true, false);
        else if (jr && justReleased.contains(HAND_RIGHT,false)) lockLimb(ARM_RIGHT, FOREARM_RIGHT);
        if (np && nextToPress.get(0) == FOOT_LEFT)
            moveLimb(THIGH_LEFT, SHIN_LEFT, FOOT_LEFT, horizontalL, verticalL, false, true);
        else if (jr && justReleased.contains(FOOT_LEFT,false)) lockLimb(THIGH_LEFT, SHIN_LEFT);
        if (np && nextToPress.get(0) == FOOT_RIGHT)
            moveLimb(THIGH_RIGHT, SHIN_RIGHT, FOOT_RIGHT, horizontalL, verticalL, false, false);
        else if (jr && justReleased.contains(FOOT_RIGHT,false)) lockLimb(THIGH_RIGHT, SHIN_RIGHT);

        moveTorso(horizontalR, verticalR);
    }

    private void moveLimb(int root, int mid, int extremity,
                          float horizontal, float vertical,
                          boolean isArm, boolean isLeft) {
        PartModel extremityPart = character.parts.get(extremity);
        RevoluteJoint rootJoint = (RevoluteJoint) character.getInnerJoint(root);
        RevoluteJoint midJoint = (RevoluteJoint) character.getJoint(root, mid);

        originalCache.set(extremityPart.getPosition());
        crossCache.set(horizontal, vertical);
        if (crossCache.len2() > GP_THRESHOLD && inLimits(rootJoint, midJoint)) {
            momentum += GP_ACCELERATE;
            momentum = Math.min(momentum, GP_MAX_SPEED);
            crossCache.scl(momentum);
            crossCache.scl(1 / scale.x, 1 / scale.y);
            posCache.set(originalCache);
            posCache.add(crossCache);

            adjustToRadius(rootJoint, isArm);
            extremityPart.setPosition(posCache, extremityPart.getAngle());
        }
        else momentum = 0;

        adjustTowardsLimits(rootJoint, midJoint, isArm, isLeft);
    }

    private boolean inLimits(RevoluteJoint rootJoint, RevoluteJoint midJoint) {
        return !(rootJoint.getJointAngle() < rootJoint.getLowerLimit() - Math.toRadians(10)
                || rootJoint.getJointAngle() > rootJoint.getUpperLimit() + Math.toRadians(10)
                || midJoint.getJointAngle() < midJoint.getLowerLimit() - Math.toRadians(10)
                || midJoint.getJointAngle() > midJoint.getUpperLimit() + Math.toRadians(10));
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

    private void adjustTowardsLimits(RevoluteJoint rootJoint, RevoluteJoint midJoint,
                                     boolean isArm, boolean isLeft) {
        moveTowardLimits(rootJoint);
        moveTowardLimits(midJoint);
        if (midJoint.getMotorSpeed() != 0 && rootJoint.getMotorSpeed() == 0) {
            float motorSpeed = ((isArm && isLeft) || (!isArm && !isLeft)) ? -5f : 5f;
            if ((isArm && posCache.y - originalCache.y > 0)
                    || (!isArm && isLeft && posCache.x - originalCache.x > 0)
                    || (!isArm && !isLeft && posCache.x - originalCache.x < 0))
                motorSpeed = -motorSpeed;
            rootJoint.setMotorSpeed(motorSpeed);
        }
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
        if (counter > 2) counter += 2;
        torsoCache.scl(counter);

        if (leftHand|| rightHand || leftLeg || rightLeg) {
            character.parts.get(CHEST).body.applyForceToCenter(torsoCache.x * horizontalJoystick, 0, true);
            character.parts.get(CHEST).body.applyForceToCenter(0, torsoCache.y * verticalJoystick, true);
        }
    }

    public void dispose() {
        character = null;
        originalCache = null;
        posCache = null;
        torsoCache = null;
        crossCache = null;
        scale = null;
    }
}
