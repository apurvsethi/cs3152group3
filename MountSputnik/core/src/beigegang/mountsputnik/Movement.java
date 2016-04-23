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
public class Movement {

    public static CharacterModel character;
    /**
     * last limb controlled by player.
     */
    public static int lastLimb = 0;

    public static void setCharacter(CharacterModel c) {
        character = c;
    }


    /**
     * Applies torso force directed by player if body is hooked to 1+ handholds
     * @param force - strength of force to apply (not directed)
     * @author Jacob
     */
    public static void applyTorsoForceIfApplicable(Vector2 force) {
        if (TORSO_MODE){
            if (isGripping(FOOT_LEFT)|| isGripping(FOOT_RIGHT) || isGripping(HAND_LEFT) || isGripping(HAND_RIGHT)){
                InputController input = InputController.getInstance();
                float h = input.getHorizontalR();
                float v = input.getVerticalR();
                applyIfUnderLimit(CHEST,new Vector2(force.x,force.y),h,v);
            }

        }
    }
    private static void applyIfUnderLimit(int part, Vector2 force, float h, float v) {
        Vector2 vect = character.parts.get(part).body.getLinearVelocity();
        character.parts.get(part).body.applyForceToCenter(force.x*h, 0, true);
        character.parts.get(part).body.applyForceToCenter(0, force.y*v, true);
    }
    /**
     *
     * @param part - ungripped part to apply the forces to
     * @param v    - vertical direction given by player (-1.0 -> 1.0 f) (ints if keyboard)
     * @param h    - horizontal direction given by player (-1.0 -> 1.0f) (ints if keyboard)
     * @returns nothing - applies forces directly in method
     * @author Jacob
     */

    public static void findAndApplyForces(int part, float v, float h) {
        RevoluteJoint upperJoint;
        resetLimbSpeedsTo0();

        switch (part) {
            case HAND_LEFT:
                upperJoint = ((RevoluteJoint) character.joints.get(ARM_LEFT - 1));
                upperJoint.setMaxMotorTorque(100);
//                //- is to waist. + to head.
                rotateLimbPiece(v, h, ARM_LEFT, FOREARM_LEFT);
                rotateLeftForearm(v, h);

                break;

            case HAND_RIGHT:
                upperJoint = ((RevoluteJoint) character.joints.get(ARM_RIGHT - 1));
                upperJoint.setMaxMotorTorque(100);
                //- is to head. + to waist.
                rotateLimbPiece(v, h, ARM_RIGHT, FOREARM_RIGHT);
                rotateRightForearm(v, h);

                break;

            case FOOT_LEFT:
                upperJoint = ((RevoluteJoint) character.joints.get(THIGH_LEFT - 1));
                upperJoint.setMaxMotorTorque(100);
                //+ is to waist. - to opposite of waist.
                rotateLimbPiece(v, h, THIGH_LEFT, SHIN_LEFT);

                rotateLeftShin(v, h);

                break;

            case FOOT_RIGHT:
                upperJoint = ((RevoluteJoint) character.joints.get(THIGH_RIGHT - 1));
                upperJoint.setMaxMotorTorque(100);
                //- is to waist. + to opposite of waist.
                rotateLimbPiece(v, h, THIGH_RIGHT, SHIN_RIGHT);

                rotateRightShin(v, h);

                break;

            default:
                //do nothing
                break;

        }

        lastLimb = part != 0 ? part : 0;
    }

    /**
     * Dampening method:
     * basically freezes limbs once they aren't being controlled any longer.
     * @author Jacob
     */
    public static void resetLimbSpeedsTo0() {
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
    private static void rotateLimbPiece(float v, float h, int upperJoint, int lowerJoint) {
        Vector2 root = character.joints.get(upperJoint - 1).getAnchorA();
        Vector2 shoot = character.joints.get(lowerJoint - 1).getAnchorA();
        float absoluteAngle = findAbsoluteAngleOfPart(root, shoot);
        boolean posJointSpeedV = false;
        boolean posJointSpeedH = false;
        //- is to waist. + to head.

        if (absoluteAngle > -180 && absoluteAngle <= -90) {
            posJointSpeedV = true;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            posJointSpeedV = true;
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            posJointSpeedV = false;

        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            posJointSpeedV = false;
        }
        if (v < 0) {
            posJointSpeedV = !posJointSpeedV;
        }


        if (absoluteAngle > -180 && absoluteAngle <= -90) {
            posJointSpeedH = true;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            posJointSpeedH = false;
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            posJointSpeedH = false;

        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            posJointSpeedH = true;
        }
        if (h > 0) {
            posJointSpeedH = !posJointSpeedH;
        }
        if (!isForceDiagonal(v, h)) {
            if (Math.abs(v) > .2) {
                int a = posJointSpeedV ? 1 : -1;
                ((RevoluteJoint) character.joints.get(upperJoint - 1)).setMotorSpeed(a * FOREARM_JOINT_SPEED);
            }
            if (Math.abs(h) > .2) {
                int a = posJointSpeedH ? 1 : -1;
                ((RevoluteJoint) character.joints.get(upperJoint - 1)).setMotorSpeed(a * FOREARM_JOINT_SPEED);
            }
        } else {
            int aV = posJointSpeedV ? 1 : -1;
            int aH = posJointSpeedH ? 1 : -1;
            //for left and up:
            float desiredAngle = findAbsoluteAngleOfPart(new Vector2(0, 0), new Vector2(h, v));
            //works for v>0
            if (v > 0) {
                if (absoluteAngle < 0) {
                    if (desiredAngle > absoluteAngle) {
                        setSpeedPart(upperJoint, FOREARM_JOINT_SPEED, aV);
                    } else {
                        setSpeedPart(upperJoint, FOREARM_JOINT_SPEED, aH);
                    }
                } else {
                    if (desiredAngle < absoluteAngle) {
                        setSpeedPart(upperJoint, FOREARM_JOINT_SPEED, aV);
                    } else {
                        setSpeedPart(upperJoint, FOREARM_JOINT_SPEED, aH);
                    }
                }
            } else {
                if (absoluteAngle < 0) {
                    if (desiredAngle > absoluteAngle) {
                        setSpeedPart(upperJoint, FOREARM_JOINT_SPEED, aH);
                    } else {
                        setSpeedPart(upperJoint, FOREARM_JOINT_SPEED, aV);
                    }
                } else {
                    if (desiredAngle < absoluteAngle) {
                        setSpeedPart(upperJoint, FOREARM_JOINT_SPEED, aH);
                    } else {
                        setSpeedPart(upperJoint, FOREARM_JOINT_SPEED, aV);
                    }
                }
            }
        }

    }

    private static void setSpeedPart(int part, float speed, int a) {
        ((RevoluteJoint) character.joints.get(part - 1)).setMotorSpeed(a * speed);

    }


    private static boolean isGripping(int part) {
        return ((ExtremityModel) (character.parts.get(part))).isGripped();
    }


    /**
     * @param rootAnchor
     * @param shootAnchor
     * @return absolute angle, from -180 to 180, of part on the board/screen - arm pointing up has angle of 0 degrees.
     * arm pointing to the right has an angle of 90 degrees
     * @author Jacob
     */
    private static float findAbsoluteAngleOfPart(Vector2 rootAnchor, Vector2 shootAnchor) {
        double dy = rootAnchor.y - shootAnchor.y;
        double dx = rootAnchor.x - shootAnchor.x;
        //atan2 pretends like -x axis is root axis, need to modify to make it thing positive y axis
        double theta = Math.atan2(dy, dx) * RAD_TO_DEG;
        if (theta <= 0 && theta > -90) {
            theta = -90 - theta;
        } else if (theta > 0 && theta < 90) {
            theta = -(90 + theta);
        } else if (theta > 90 && theta < 180) {
            theta = 270 - theta;
        } else { // (theta > -180 && theta < -90)
            theta = -(90 + theta);
        }

        return (float) theta;
    }


    private static float[] rotateLeftForearm(float v, float h) {

        RevoluteJoint forearmJoint = ((RevoluteJoint) character.joints.get(FOREARM_LEFT - 1));
        forearmJoint.setMaxMotorTorque(10);
        Vector2 root = character.joints.get(ARM_LEFT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(FOREARM_LEFT - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root, shoot);


        if (isForceDiagonal(v, h)) {
            //do calculations for diagonal direction
            rotateLimbPiece(v, h, FOREARM_LEFT, HAND_LEFT);

            return null;

        }
        //-speed = straightening...?
        if (v > .2) {//up
            if (aa >= 0f) {
                if (aa > 45f) {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                } else { //special box case
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
                }
            } else {
                //aa<0
                //special box case
                //correct nt
                if (aa > -45f) {
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
                } else {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                    forearmJoint.setMaxMotorTorque(100);
                }
            }
        } else if (v < -.2) { //down

            if (aa <= 0f) {
                if (aa > -135f) {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                } else { //special box case <-135f
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
                }
            } else {//aa<0
                //special box case
                if (aa > 135f) {
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
                } else {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
//
                }
            }
        }
        if (h < -.2) {//left
            if (aa < 0f) {
                forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
            } else {
                forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
            }
        } else if (h > .2) {
            if (aa < 0f) {
                if (aa > -90f) {
                    //rotate?
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);

                } else if (aa > -135f) {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                } else {
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);

                }
            } else {
                if (aa < 45) forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                else if (aa > 135) forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                else {
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
                    forearmJoint.setMaxMotorTorque(100);

                }
            }
        }

        return null;

    }


    private static boolean isForceDiagonal(float v, float h) {
        return Math.abs(v) > .2 && Math.abs(h) > .2;
    }

    private static float[] rotateRightForearm(float v, float h) {

        RevoluteJoint forearmJoint = ((RevoluteJoint) character.joints.get(FOREARM_RIGHT - 1));
        forearmJoint.setMaxMotorTorque(10);
        Vector2 root = character.joints.get(ARM_RIGHT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(FOREARM_RIGHT - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root, shoot);
        if (isForceDiagonal(v, h)) {
            rotateLimbPiece(v, h, FOREARM_RIGHT, HAND_RIGHT);
        }

        if (v > .2) {//up
            if (aa >= 0f) {
                if (aa > 45f) {
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
                    forearmJoint.setMaxMotorTorque(100);

                } else { //special box case
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                }
            } else {//aa<0
                //special box case
                if (aa > -45f) {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                } else {
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
                }
            }
        } else if (v < -.2) { //down
            if (aa <= 0f) {
                if (aa > -135f) {
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
                } else { //special box case.
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                }
            } else {//aa<0
                //special box case
                if (aa > 135f) {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                } else {
                    forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
//
                }
            }
        }
        if (h > .2) {//right
            if (aa < 0f) {
                forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
            } else {
                forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
            }
        } else if (h < -.2) {
            if (aa < 0f) {
                if (aa > -90f) {
                    //rotate?
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);

                } else if (aa > -135f) {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                } else {
                    forearmJoint.setMotorSpeed(FOREARM_JOINT_SPEED);
                }
            } else {
                forearmJoint.setMotorSpeed(-FOREARM_JOINT_SPEED);
            }
        }
        return null;
    }


    private static float[] rotateLeftShin(float v, float h) {

        RevoluteJoint shinJoint = ((RevoluteJoint) character.joints.get(SHIN_LEFT - 1));
        shinJoint.setMaxMotorTorque(30);
        Vector2 root = character.joints.get(THIGH_LEFT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(SHIN_LEFT - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root, shoot);


        if (isForceDiagonal(v, h)) {
            //do calculations for diagonal direction
            rotateLimbPiece(v, h, SHIN_LEFT, FOOT_LEFT);
        }
        if (v > .2) {//up
            if (aa >= 0f) {
                if (aa > 45f) {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
//                    cuz this bending works AGAINST gravity
                    shinJoint.setMaxMotorTorque(100);

                } else { //special box case
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);

                }
            } else {//aa<0
                //special box case
                if (aa > -45f) {
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                } else {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
                }
//
            }
        }
        //temporary limits for now for testing usage.
        else if (v < -.2) { //down

            if (aa <= 0f) {
                if (aa > -135f) {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
                } else { //special box case <-135f, arm goes straight downward bound ho stuff.
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                }
            } else {//aa<0
                //special box case
                if (aa > 135f) {
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                } else {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
//
                }
            }
        }
        if (h > .2) {//right
            if (aa < 0f) {
                shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
            } else {
                shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
            }
        } else if (h < -.2) {//left
            if (aa < 0f) {
                if (aa > -90f) {
                    //rotate?
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);

                } else if (aa > -135f) {
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                } else {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);

                }
            } else {
                if (aa < 45) shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                else if (aa > 135) shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                else {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
                }
            }
        }

        return null;

    }

    private static float[] rotateRightShin(float v, float h) {

        RevoluteJoint shinJoint = ((RevoluteJoint) character.joints.get(SHIN_RIGHT - 1));
        shinJoint.setMaxMotorTorque(30);
        Vector2 root = character.joints.get(THIGH_RIGHT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(SHIN_RIGHT - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root, shoot);
        if (isForceDiagonal(v, h)) {
            //do calculations for diagonal direction
            rotateLimbPiece(v, h, SHIN_RIGHT, FOOT_RIGHT);

        }
        if (v > .2) {//up
            if (aa >= 0f) {
                //correct nt.
                if (aa > 45f) {
                    //rotate the joint using the MOTOR SPEED.
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
//                    cuz this bending works AGAINST gravity
                    shinJoint.setMaxMotorTorque(100);

                } else { //special box case
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);

                }
            } else {
                //special box case
                if (aa > -45f) {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
                } else { //shin should rotate to max degrees
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                }
//
            }
        }
        //temporary limits for now for testing usage.
        else if (v < -.2) { //down

            if (aa <= 0f) {
                //correct nt.
                if (aa > -135f) {
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                    shinJoint.setMaxMotorTorque(30);
                } else { //special box case <-135f, arm goes straight downward bound ho stuff.
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
                }
            } else {//aa<0
                //special box case
                if (aa > 135f) {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
                } else {
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                }
            }
        }
        if (h < -.2) {//right
            if (aa < 0f) {
                shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
//
            } else {
                shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);
            }
        } else if (h > .2) {//right
            if (aa < 0f) {
                if (aa > -90f) {
                    //rotate?
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);

                } else if (aa > -135f) {
                    shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                } else {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);

                }
            } else {
                if (aa < 45) shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                else if (aa > 135) shinJoint.setMotorSpeed(SHIN_JOINT_SPEED);
                else {
                    shinJoint.setMotorSpeed(-SHIN_JOINT_SPEED);

                }
            }
        }

        return null;

    }

    //TODO this method could use some serious modifying. i dont think it makes sense lol. but since movement is fine rn
    //will modify later
    /**
     * attempts to allow limbs that WERE hooked to handholds to rotate and bend more easily allowing for more
     * natural movement
     * @param nextToPress - all limbs not hooked to handholds that are currently selected.
     *
     * @author Jacob
     */
    public static void makeHookedJointsMovable(Array<Integer> nextToPress) {
        if (!nextToPress.contains(FOOT_LEFT, true)) {
            ((RevoluteJoint) character.joints.get(FOREARM_LEFT - 1)).setMaxMotorTorque(100);
        }
        if (!nextToPress.contains(FOOT_RIGHT, true)) {
            ((RevoluteJoint) character.joints.get(FOREARM_RIGHT - 1)).setMaxMotorTorque(100);
        }
        if (!nextToPress.contains(HAND_LEFT, true)) {
            ((RevoluteJoint) character.joints.get(SHIN_LEFT - 1)).setMaxMotorTorque(100);
        }
        if (!nextToPress.contains(HAND_RIGHT, true)) {
            ((RevoluteJoint) character.joints.get(SHIN_RIGHT - 1)).setMaxMotorTorque(100);
        }
    }
}