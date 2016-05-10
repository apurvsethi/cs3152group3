package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.utils.Array;

import static beigegang.mountsputnik.Constants.*;
/**
 * Created by jacobcooper on 4/11/16.
 * This class is full of static helper functions for movement because GameMode was too crowded
 * @author Jacob
 */
public class MovementController {

    public CharacterModel character;
    /**
     * last limb controlled by player.
     */
    private Vector2 torso_cache;

    public MovementController(CharacterModel character) {
        this.character = character;
        torso_cache = new Vector2();
    }

    /**
     * Applies torso force directed by player if body is hooked to 1+ handholds
     * @author Jacob
     *
     * @param h horizontal force applied
     * @param v vertical force applied
     */
    public void applyTorsoForceIfApplicable(float h, float v) {
        Vector2 force = calcTorsoForce();
        if (isGripping(FOOT_LEFT)|| isGripping(FOOT_RIGHT)
                || isGripping(HAND_LEFT) || isGripping(HAND_RIGHT)) {
            applyIfUnderLimit(CHEST, force, h, v);
        }
    }

    private Vector2 calcTorsoForce() {
        torso_cache.set(CONSTANT_X_FORCE * 2f, CONSTANT_X_FORCE * 2f);

        int counter = 0;
        if (isGripping(HAND_LEFT)) counter++;
        if (isGripping(HAND_RIGHT)) counter++;
        if (isGripping(FOOT_LEFT)) counter++;
        if (isGripping(FOOT_RIGHT)) counter++;
        if (counter > 2) counter +=2;

        torso_cache.scl(counter);
        return torso_cache;
    }

    private void applyIfUnderLimit(int part, Vector2 force, float h, float v) {
        character.parts.get(part).body.applyForceToCenter(force.x*h, 0, true);
        character.parts.get(part).body.applyForceToCenter(0, force.y*v, true);
    }

    /**
     *
     * @param part - ungripped part to apply the forces to
     * @param v    - vertical direction given by player (-1.0 -> 1.0 f) (ints if keyboard)
     * @param h    - horizontal direction given by player (-1.0 -> 1.0f) (ints if keyboard)
     *
     * @author Jacob
     */

    public void findAndApplyForces(int part, float v, float h) {
        RevoluteJoint upperJoint;
//        resetLimbSpeedsTo0();

        switch (part) {
            case HAND_LEFT:
                upperJoint = ((RevoluteJoint) character.joints.get(ARM_LEFT - 1));
                upperJoint.setMaxMotorTorque(25);
                rotateLimbPiece(v, h, ARM_LEFT, FOREARM_LEFT);
//                rotateJoint(v, h, ARM_LEFT, FOREARM_LEFT, HAND_LEFT, true, true);
                break;
            case HAND_RIGHT:
                upperJoint = ((RevoluteJoint) character.joints.get(ARM_RIGHT - 1));
                upperJoint.setMaxMotorTorque(50);
                rotateLimbPiece(v, h, ARM_RIGHT, FOREARM_RIGHT);
//                rotateJoint(v, h, ARM_RIGHT, FOREARM_RIGHT, HAND_RIGHT, true, false);
                break;
            case FOOT_LEFT:
                upperJoint = ((RevoluteJoint) character.joints.get(THIGH_LEFT - 1));
                upperJoint.setMaxMotorTorque(20);
                rotateLimbPiece(v, h, THIGH_LEFT, SHIN_LEFT);
//                rotateJoint(v, h, THIGH_LEFT, SHIN_LEFT, FOOT_LEFT, false, true);
                break;
            case FOOT_RIGHT:
                upperJoint = ((RevoluteJoint) character.joints.get(THIGH_RIGHT - 1));
                upperJoint.setMaxMotorTorque(20);
                rotateLimbPiece(v, h, THIGH_RIGHT, SHIN_RIGHT);
//                rotateJoint(v, h, THIGH_RIGHT, SHIN_RIGHT, FOOT_RIGHT, false, false);
                break;
            default:
                break;
        }
    }

    /**
     * Dampening method:
     * basically freezes limbs once they aren't being controlled any longer.
     * @author Jacob
     */
    public void resetLimbSpeedsTo0() {
        RevoluteJoint joint;
        for (int limbPiece : NON_EXTREMITY_LIMBS) {
            joint = ((RevoluteJoint) character.joints.get(limbPiece - 1));
            joint.setMotorSpeed(0);
        }
    }

    /**
     * SUPER IMPORTANT METHOD
     * controls all rotations of upper pieces of a limb (upper arm and upper leg) and
     * the rotations of the forearms and shins upon diagonal movement of the joystick
     * somehow it generalizes SUPER well to all limbs in all cases, which was unexpected.
     *
     * @param v - vertical input from joystick
     * @param h - horizontal input from joystick
     * @param upperJoint - where this limb connects to the torso
     * @param lowerJoint - either a knee or elbow joint depending on the limb
     * @author Jacob
     */
    private void rotateLimbPiece(float v, float h, int upperJoint, int lowerJoint) {
        Vector2 root = character.joints.get(upperJoint - 1).getAnchorA();
        Vector2 shoot = character.joints.get(lowerJoint - 1).getAnchorA();
        float absoluteAngle = findAbsoluteAngleOfPart(root, shoot);
        float desiredAngle = findAbsoluteAngle(-v, -h);
        boolean posJointSpeedV, posJointSpeedH;
        float speedToRotate = (upperJoint == THIGH_LEFT ||  upperJoint == SHIN_LEFT
                || upperJoint == THIGH_RIGHT || upperJoint == SHIN_RIGHT) ? SHIN_JOINT_SPEED : FOREARM_JOINT_SPEED;
        posJointSpeedV = absoluteAngle > -180 && absoluteAngle < 0;
        if (v < 0) posJointSpeedV = !posJointSpeedV;

        posJointSpeedH = !(absoluteAngle > -90 && absoluteAngle <= 90);
        if (h > 0) posJointSpeedH = !posJointSpeedH;

        if (!isForceDiagonal(v, h)) {
            if (Math.abs(v) > 0.2)
                setSpeedPart(upperJoint, speedToRotate, posJointSpeedV);
            if (Math.abs(h) > 0.2)
                setSpeedPart(upperJoint, speedToRotate, posJointSpeedH);
        }
        else if ((v > 0 && absoluteAngle < 0 && desiredAngle > absoluteAngle)
                    || (v > 0 && absoluteAngle >= 0 && desiredAngle < absoluteAngle)
                    || (v <= 0 && absoluteAngle < 0 && desiredAngle <= absoluteAngle)
                    || (v <= 0 && absoluteAngle >= 0 && desiredAngle >= absoluteAngle))
            setSpeedPart(upperJoint, speedToRotate, posJointSpeedV);
        else setSpeedPart(upperJoint, speedToRotate, posJointSpeedH);
    }

    private void rotateJoint(float v, float h, int inPart, int midPart,
                                    int outPart, boolean isHand, boolean isLeft) {
        RevoluteJoint joint = ((RevoluteJoint) character.joints.get(midPart - 1));
        joint.setMaxMotorTorque(30);
        Vector2 root = character.joints.get(inPart - 1).getAnchorA();
        Vector2 shoot = character.joints.get(midPart - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root, shoot);

        if (isForceDiagonal(v, h)) {
            rotateLimbPiece(v, h, midPart, outPart);
            return;
        }

        if (isHand) {
            verticalForearmMovement(v, joint, aa, isLeft);
            horizontalForearmMovement(h, joint, aa, isLeft);
        }
        else {
            verticalShinMovement(v, joint, aa, isLeft);
            horizontalShinMovement(h, joint, aa, isLeft);
        }
    }

    private boolean isGripping(int part) {
        return ((ExtremityModel) (character.parts.get(part))).isGripped();
    }

    /**
     * @param rootAnchor
     * @param shootAnchor
     * @return absolute angle, from -180 to 180, of part on the board/screen - arm pointing up has angle of 0 degrees.
     * arm pointing to the right has an angle of 90 degrees
     * @author Jacob
     */
    private float findAbsoluteAngleOfPart(Vector2 rootAnchor, Vector2 shootAnchor) {
        double dy = rootAnchor.y - shootAnchor.y;
        double dx = rootAnchor.x - shootAnchor.x;
        //atan2 pretends like -x axis is root axis, need to modify to make it thing positive y axis
        return findAbsoluteAngle(dy, dx);
    }

    private float findAbsoluteAngle(double dy, double dx) {
        double theta = Math.atan2(dy, dx) * RAD_TO_DEG;
        if (theta > 90 && theta < 180) theta = 270 - theta;
        else theta = -(90 + theta);

        return (float) theta;
    }

    private boolean isForceDiagonal(float v, float h) {
        return Math.abs(v) > .2 && Math.abs(h) > .2;
    }

    private void verticalForearmMovement(float v, RevoluteJoint forearmJoint,
                                                float aa, boolean left) {
        if ((v > 0.2 && aa > -45 && aa <= 45)
                || (v < -0.2 && (aa <= -135 || aa > 135)))
            setSpeedJoint(forearmJoint, -FOREARM_JOINT_SPEED, left);
        else if ((v > 0.2 && (aa <= -45 || aa > 45))
                || (v < -0.2 && aa > -135 && aa <= 135)) {
            setSpeedJoint(forearmJoint, FOREARM_JOINT_SPEED, left);
            if (v > 0.2 && ((left && aa <= -45) || (!left && aa > 45)))
                forearmJoint.setMaxMotorTorque(100);
        }
    }

    private void horizontalForearmMovement(float h, RevoluteJoint forearmJoint,
                                                  float aa, boolean left) {
        if ((left && (h < -0.2 || (h > 0.2 && aa < -135)))
                || (!left && (h > 0.2 || (h < -0.2 && aa < -135))))
            setSpeedJoint(forearmJoint, -FOREARM_JOINT_SPEED, left);
        else if ((left && h > 0.2 && aa > -135)
                || (!left && h < -0.2 && aa > -135))
            setSpeedJoint(forearmJoint, FOREARM_JOINT_SPEED, left);
    }

    private void verticalShinMovement(float v, RevoluteJoint shinJoint,
                                             float aa, boolean isLeft) {
        if ((v > 0.2 && aa > -45 && aa <= 45)
                || (v < -0.2 && (aa <= -135 || aa > 135)))
            setSpeedJoint(shinJoint, SHIN_JOINT_SPEED, isLeft);
        else if ((v > 0.2 && (aa <= -45 || aa > 45))
                || (v < -0.2 && aa > -135 && aa <= 135)) {
            setSpeedJoint(shinJoint, -SHIN_JOINT_SPEED, isLeft);
            if (v > 0.2 && aa > 45)
                shinJoint.setMaxMotorTorque(100);
        }
    }

    private void horizontalShinMovement(float h, RevoluteJoint shinJoint,
                                               float aa, boolean isLeft) {
        if ((isLeft && h > 0.2) || (!isLeft && h < -0.2))
            setSpeedJoint(shinJoint, SHIN_JOINT_SPEED, isLeft);
        else if (((isLeft && h < -0.2) || (!isLeft && h > 0.2))
                && ((aa > -135 && aa < 45) || aa > 135))
            shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
        else if (((isLeft && h < -0.2) || (!isLeft && h > 0.2))
                && ((aa >=45 && aa <= 135) || aa <= -135))
            shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
    }

    private void setSpeedPart(int part, float speed, boolean pos) {
        setSpeedJoint(((RevoluteJoint) character.joints.get(part - 1)), speed, pos);
    }

    private void setSpeedJoint(RevoluteJoint joint, float speed, boolean pos) {
        if (!pos) speed = -speed;
        joint.setMotorSpeed(speed);
    }

    /**
     * attempts to allow limbs that WERE hooked to handholds to rotate and bend more easily allowing for more
     * natural movement
     * @param nextToPress - all limbs not hooked to handholds that are currently selected.
     *
     * @author Jacob
     */
    public void makeHookedJointsMovable(Array<Integer> nextToPress) {
        if (!nextToPress.contains(FOOT_LEFT, true))
            ((RevoluteJoint) character.joints.get(FOREARM_LEFT - 1)).setMaxMotorTorque(100);
        if (!nextToPress.contains(FOOT_RIGHT, true))
            ((RevoluteJoint) character.joints.get(FOREARM_RIGHT - 1)).setMaxMotorTorque(100);
        if (!nextToPress.contains(HAND_LEFT, true))
            ((RevoluteJoint) character.joints.get(SHIN_LEFT - 1)).setMaxMotorTorque(100);
        if (!nextToPress.contains(HAND_RIGHT, true))
            ((RevoluteJoint) character.joints.get(SHIN_RIGHT - 1)).setMaxMotorTorque(100);
    }

    public void dispose() {
        character = null;
        torso_cache = null;
    }
}