package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;

import static beigegang.mountsputnik.Constants.*;
/**
 * Created by jacobcooper on 4/11/16.
 * This class is full of static helper functions for movement because GameMode was too crowded
 * @author Jacob
 */
public class Movement {

    public static CharacterModel character;


    public static void setCharacter(CharacterModel c){
        character = c;
    }

    private static void applyNewTestForces(float[] fs, Vector2 forceL,int part,float v, float h) {
        float x = forceL.x;
        float y = forceL.y;
        float forcexV = fs[0];
        float forceyV = fs[1];
        float forcexH = fs[2];
        float forceyH = fs[3];
        v = Math.abs(v);
        h = Math.abs(h);
        character.parts.get(part).body.applyForceToCenter(forcexV*x*v,forceyV*y*v,true);
        character.parts.get(part).body.applyForceToCenter(forcexH*x*h,forceyH*y*h,true);
    }

    /**
     *
     * @param part - ungripped part to apply the forces to
     * @param v - vertical direction given by player (-1.0 -> 1.0 f) (ints if keyboard)
     * @param h - horizontal direction given by player (-1.0 -> 1.0f) (ints if keyboard)
     * @returns nothing - applies forces directly in method
     * @author Jacob
     */

    public static void findAndApplyForces(int part,float v, float h){
        Vector2 forceL;
        float[] fs;
        float jointAngle;

        switch(part){
            case HAND_LEFT:
                forceL = new Vector2(CONSTANT_X_FORCE,CONSTANT_X_FORCE);
                jointAngle = ((RevoluteJoint) character.joints.get(ARM_LEFT - 1)).getJointAngle();
                fs = Movement.getPhysicallyCorrectForceMultipliersLeftArm(v,h);
                Movement.applyNewTestForces(fs,forceL,ARM_LEFT,v,h);
                Movement.getMultipliersLeftForearm(v,h);
                break;

            case HAND_RIGHT:
                forceL = new Vector2(CONSTANT_X_FORCE, CONSTANT_X_FORCE);
                jointAngle = ((RevoluteJoint) character.joints.get(ARM_RIGHT - 1)).getJointAngle();
                fs = Movement.getPhysicallyCorrectForceMultipliersRightArm(v, h);
                Movement.applyNewTestForces(fs, forceL, ARM_RIGHT, v, h);
                Movement.getMultipliersRightForearm(v, h);
                break;

            case FOOT_LEFT:
                forceL = new Vector2(CONSTANT_X_FORCE,CONSTANT_X_FORCE);
                jointAngle = ((RevoluteJoint) character.joints.get(THIGH_LEFT - 1)).getJointAngle();
                fs = Movement.getPhysicallyCorrectForceMultipliersLeftLeg(v,h);
                Movement.applyNewTestForces(fs,forceL,THIGH_LEFT,v,h);
                Movement.getMultipliersLeftShin(v,h);
                break;

            case FOOT_RIGHT:
                forceL = new Vector2(CONSTANT_X_FORCE,CONSTANT_X_FORCE);
                jointAngle = ((RevoluteJoint) character.joints.get(THIGH_RIGHT - 1)).getJointAngle();
                fs = Movement.getPhysicallyCorrectForceMultipliersRightLeg(v,h);
                Movement.applyNewTestForces(fs,forceL,THIGH_RIGHT,v,h);
                Movement.getMultipliersRightShin(v,h);
                break;

            default:
                //do nothing
                break;

        }
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
        double theta = Math.atan2(dy,dx) * RAD_TO_DEG;
        if (theta <= 0 && theta > -90){
            theta = -90 - theta;
        }else if (theta > 0 && theta < 90){
            theta = -(90 + theta);
        }else if (theta > 90 && theta < 180){
            theta = 270 - theta;
        }else { // (theta > -180 && theta < -90)
            theta = -(90 + theta);
        }

        return (float) theta;
    }



    private static float[] getMultipliersLeftForearm(float v, float h) {

        RevoluteJoint forearmJoint = ((RevoluteJoint) character.joints.get(FOREARM_LEFT-1));
        forearmJoint.setMaxMotorTorque(10);
        Vector2 root = character.joints.get(ARM_LEFT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(FOREARM_LEFT - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root,shoot);
        if (v>.2){//up
            if (aa>=0f){
                if (aa > 45f){
                    forearmJoint.setMotorSpeed(1);
                }else{ //special box case
                    forearmJoint.setMotorSpeed(-1);
                }
            }else{
                //aa<0
                //special box case
                //correct nt
                if (aa>-45f){
                    forearmJoint.setMotorSpeed(-1);
                }else{
                    forearmJoint.setMotorSpeed(1);
                    forearmJoint.setMaxMotorTorque(100);
                    }
                }
            }
        else if (v<-.2){ //down

            if (aa<=0f){
                if (aa > -135f){
                   forearmJoint.setMotorSpeed(1);
                }else{ //special box case <-135f
                    forearmJoint.setMotorSpeed(-1);
                }
            }else{//aa<0
                //special box case
                if (aa>135f){
                    forearmJoint.setMotorSpeed(-1);
                }else{
                    forearmJoint.setMotorSpeed(1);
//
                }
            }
        }
        if (h<-.2){//left
            if (aa<0f){
                forearmJoint.setMotorSpeed(-1);
            }else{
                forearmJoint.setMotorSpeed(-1);
            }
        }else if (h>.2){
            if (aa<0f){
                if (aa>-90f){
                    //rotate?
                    forearmJoint.setMotorSpeed(1);

                }else if (aa > -135f){
                    forearmJoint.setMotorSpeed(1);
                }else{
                    forearmJoint.setMotorSpeed(-1);

                }
            }else{
                if (aa<45) forearmJoint.setMotorSpeed(1);
                else if (aa > 135) forearmJoint.setMotorSpeed(1);
                else{
                    forearmJoint.setMotorSpeed(-1);
                    forearmJoint.setMaxMotorTorque(100);

                }
            }
        }

        return null;

    }

    private static float[] getMultipliersRightForearm(float v, float h) {

        RevoluteJoint forearmJoint = ((RevoluteJoint) character.joints.get(FOREARM_RIGHT-1));
        forearmJoint.setMaxMotorTorque(10);
        Vector2 root = character.joints.get(ARM_RIGHT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(FOREARM_RIGHT - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root,shoot);
        if (v>.2){//up
            if (aa>=0f){
                if (aa > 45f){
                    forearmJoint.setMotorSpeed(-1);
                    forearmJoint.setMaxMotorTorque(100);

                }else{ //special box case
                    forearmJoint.setMotorSpeed(1);
                }
            }else{//aa<0
                //special box case
                if (aa>-45f){
                    forearmJoint.setMotorSpeed(1);
                }else{
                    forearmJoint.setMotorSpeed(-1);
                }
            }
        }
        else if (v<-.2){ //down
            if (aa<=0f){
                if (aa > -135f){
                    forearmJoint.setMotorSpeed(-1);
                }else{ //special box case.
                    forearmJoint.setMotorSpeed(1);
                }
            }else{//aa<0
                //special box case
                if (aa>135f){
                    forearmJoint.setMotorSpeed(1);
                }else{
                    forearmJoint.setMotorSpeed(-1);
//
                }
            }
        }
        if (h>.2){//right
            if (aa<0f){
                forearmJoint.setMotorSpeed(1);
            }else{
                forearmJoint.setMotorSpeed(1);
            }
        }else if (h<-.2){
            if (aa<0f){
                if (aa>-90f){
                    //rotate?
                    forearmJoint.setMotorSpeed(1);

                }else if (aa > -135f){
                    forearmJoint.setMotorSpeed(1);
                }else{
                    forearmJoint.setMotorSpeed(1);
                }
            }else{
                forearmJoint.setMotorSpeed(-1);
            }
        }
        return null;
    }
//
    //TODO either find a way to use this method (will be necessary when we talk about pushing from
    //arm and switching forearm limits to be able to do that.
    private void changeLimitsIfNecessary(int part,float boundary,float lowerLimit,float upperLimit) {
        float jointAngle = ((RevoluteJoint) character.joints.get(part-1)).getJointAngle() * RAD_TO_DEG;
        //avoiding any floating point weirdness
        if (jointAngle<boundary - .1){
            ((RevoluteJoint) character.joints.get(part-1)).setLimits(lowerLimit,upperLimit);
        }

    }


    private static float[] getPhysicallyCorrectForceMultipliersRightArm(float v, float h) {
        Vector2 root = character.joints.get(ARM_RIGHT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(FOREARM_RIGHT - 1).getAnchorA();
        float forcexV = 0f;
        float forceyV = 0f;
        float forcexH = 0f;
        float forceyH = 0f;
        float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);

        //TODO this doesn't pull the player up much, this just manuevers the arm into the correct position...

        if (absoluteAngle > -180 && absoluteAngle <= -90) {
            forcexV = (absoluteAngle + 90) / 90;
            forceyV = 1 + forcexV;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            forcexV = (absoluteAngle + 90) / 90;
            forceyV = 1 - forcexV;
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            forcexV = (absoluteAngle - 90) / 90;
            forceyV = 1 + forcexV;
        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            forcexV = (absoluteAngle - 90) / 90;
            forceyV = 1 - forcexV;
        }
        if (v<0){
            forcexV *= -1;
            forceyV *= -1;
        }

        //HORIZONTAL DIRECTION
        if (absoluteAngle > -180 && absoluteAngle <= -90) {
            forcexH = (absoluteAngle + 90) / 90;
            forceyH = 1 + forcexH;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            forcexH = -(absoluteAngle + 90) / 90;
            forceyH = -(1 - forcexH);
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            forcexH = (absoluteAngle - 90) / 90;
            forceyH = 1 + forcexH;
        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            forcexH = -(absoluteAngle - 90) / 90;
            forceyH = - (1 - forcexH);
        }
        if (h>0){
            forcexH *= -1;
            forceyH *= -1;
        }
        return new float[]{forcexV,forceyV,forcexH,forceyH};


    }
    //LEFT ARM FINALLY CORRECT
    private static float[] getPhysicallyCorrectForceMultipliersLeftArm(float v, float h){

        Vector2 root = character.joints.get(ARM_LEFT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(FOREARM_LEFT - 1).getAnchorA();
        float forcexV = 0f;
        float forceyV = 0f;
        float forcexH = 0f;
        float forceyH = 0f;
        float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);
        //VERTICAL DIRECTION
        //TODO this doesn't pull the player up at all, this just manuevers the arm into the correct position... is okay?
        if (absoluteAngle > -180 && absoluteAngle <= -90) {

            forcexV = (absoluteAngle + 90) / 90;
            forceyV = 1 + forcexV;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            forcexV = (absoluteAngle + 90) / 90;
            forceyV = 1 - forcexV;
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            forcexV = (absoluteAngle - 90) / 90;
            forceyV = 1 + forcexV;
        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            forcexV = (absoluteAngle - 90) / 90;
            forceyV = 1 - forcexV;
        }
        if (v<0){
            forcexV *= -1;
            forceyV *= -1;
        }

        //HORIZONTAL DIRECTION
        if (absoluteAngle > -180 && absoluteAngle <= -90) {
            forcexH = (absoluteAngle + 90) / 90;
            forceyH = 1 + forcexH;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            forcexH = -(absoluteAngle + 90) / 90;
            forceyH = -(1 - forcexH);
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            forcexH = (absoluteAngle - 90) / 90;
            forceyH = 1 + forcexH;
        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            forcexH = -(absoluteAngle - 90) / 90;
            forceyH = - (1 - forcexH);
        }
        if (h>0){
            forcexH *= -1;
            forceyH *= -1;
        }

        return new float[]{forcexV,forceyV,forcexH,forceyH};
    }

    private static float[] getPhysicallyCorrectForceMultipliersLeftLeg(float v, float h) {
        Vector2 root = character.joints.get(THIGH_LEFT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(SHIN_LEFT- 1).getAnchorA();
        float forcexV = 0f;
        float forceyV = 0f;
        float forcexH = 0f;
        float forceyH = 0f;
        float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);
        //VERTICAL DIRECTION
        System.out.println(absoluteAngle);
        //TODO this doesn't pull the player up at all, this just manuevers the arm into the correct position... is okay?
        if (absoluteAngle > -180 && absoluteAngle <= -90) {

            forcexV = (absoluteAngle + 90) / 90;
            forceyV = 1 + forcexV;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            forcexV = (absoluteAngle + 90) / 90;
            forceyV = 1 - forcexV;
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {

            forcexV = (absoluteAngle - 90) / 90;
            forceyV = 1 + forcexV;
        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            forcexV = (absoluteAngle - 90) / 90;
            forceyV = 1 - forcexV;
        }
        if (v<0){
            forcexV *= -1;
            forceyV *= -1;
        }

        //HORIZONTAL DIRECTION
        if (absoluteAngle > -180 && absoluteAngle <= -90) {
            forcexH = (absoluteAngle + 90) / 90;
            forceyH = 1 + forcexH;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            forcexH = -(absoluteAngle + 90) / 90;
            forceyH = -(1 - forcexH);
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            forcexH = (absoluteAngle - 90) / 90;
            forceyH = 1 + forcexH;
        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            forcexH = -(absoluteAngle - 90) / 90;
            forceyH = - (1 - forcexH);
        }
        if (h>0){
            forcexH *= -1;
            forceyH *= -1;
        }

        return new float[]{forcexV,forceyV,forcexH,forceyH};


    }

    private static float[] getPhysicallyCorrectForceMultipliersRightLeg(float v, float h) {
        Vector2 root = character.joints.get(THIGH_RIGHT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(SHIN_RIGHT - 1).getAnchorA();
        //these are used.
        float forcexV = 0f;
        float forceyV = 0f;
        float forcexH = 0f;
        float forceyH = 0f;
        float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);

        //TODO this doesn't pull the player up much, this just manuevers the arm into the correct position...

        if (absoluteAngle > -180 && absoluteAngle <= -90) {
            forcexV = (absoluteAngle + 90) / 90;
            forceyV = 1 + forcexV;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            forcexV = (absoluteAngle + 90) / 90;
            forceyV = 1 - forcexV;
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            forcexV = (absoluteAngle - 90) / 90;
            forceyV = 1 + forcexV;
        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            forcexV = (absoluteAngle - 90) / 90;
            forceyV = 1 - forcexV;
        }
        if (v<0){
            forcexV *= -1;
            forceyV *= -1;
        }

        //HORIZONTAL DIRECTION
        if (absoluteAngle > -180 && absoluteAngle <= -90) {
            forcexH = (absoluteAngle + 90) / 90;
            forceyH = 1 + forcexH;
        } else if (absoluteAngle > -90 && absoluteAngle < 0) {
            forcexH = -(absoluteAngle + 90) / 90;
            forceyH = -(1 - forcexH);
        } else if (absoluteAngle >= 0 && absoluteAngle <= 90) {
            forcexH = (absoluteAngle - 90) / 90;
            forceyH = 1 + forcexH;
        } else { // (absoluteAngle > 90 && absoluteAngle < 180)
            forcexH = -(absoluteAngle - 90) / 90;
            forceyH = - (1 - forcexH);
        }
        if (h>0){
            forcexH *= -1;
            forceyH *= -1;
        }
        return new float[]{forcexV,forceyV,forcexH,forceyH};


    }

    private static float[] getMultipliersLeftShin(float v, float h) {

        RevoluteJoint shinJoint = ((RevoluteJoint) character.joints.get(SHIN_LEFT-1));
        shinJoint.setMaxMotorTorque(30);
        Vector2 root = character.joints.get(THIGH_LEFT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(SHIN_LEFT - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root,shoot);
        if (v>.2){//up
            if (aa>=0f){
                if (aa > 45f){
                    shinJoint.setMotorSpeed(-1);
//                    cuz this bending works AGAINST gravity
                    shinJoint.setMaxMotorTorque(100);

                }else{ //special box case
                    shinJoint.setMotorSpeed(1);

                }
            }else{//aa<0
                //special box case
                if (aa>-45f){
                    shinJoint.setMotorSpeed(1);
                }else{
                    shinJoint.setMotorSpeed(-1);
                }
//
            }
        }
        //temporary limits for now for testing usage.
        else if (v<-.2){ //down

            if (aa<=0f){
                if (aa > -135f){
                    shinJoint.setMotorSpeed(-1);
                }else{ //special box case <-135f, arm goes straight downward bound ho stuff.
                    shinJoint.setMotorSpeed(1);
                }
            }else{//aa<0
                //special box case
                if (aa>135f){
                    shinJoint.setMotorSpeed(1);
                }else{
                    shinJoint.setMotorSpeed(-1);
//
                }
            }
        }
        if (h>.2){//right
            if (aa<0f){
                shinJoint.setMotorSpeed(1);
            }else{
                shinJoint.setMotorSpeed(1);
            }
        }else if (h<-.2){//left
            if (aa<0f){
                if (aa>-90f){
                    //rotate?
                    shinJoint.setMotorSpeed(1);

                }else if (aa > -135f){
                    shinJoint.setMotorSpeed(1);
                }else{
                    shinJoint.setMotorSpeed(-1);

                }
            }else{
                if (aa<45) shinJoint.setMotorSpeed(1);
                else if (aa > 135) shinJoint.setMotorSpeed(1);
                else{
                    shinJoint.setMotorSpeed(-1);
                }
            }
        }

        return null;

    }
    private static float[] getMultipliersRightShin(float v, float h) {

        RevoluteJoint shinJoint = ((RevoluteJoint) character.joints.get(SHIN_RIGHT-1));
        shinJoint.setMaxMotorTorque(30);
        Vector2 root = character.joints.get(THIGH_RIGHT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(SHIN_RIGHT - 1).getAnchorA();
        float aa = findAbsoluteAngleOfPart(root,shoot);
        if (v>.2){//up
            if (aa>=0f){
                //correct nt.
                if (aa > 45f){
                    //rotate the joint using the MOTOR SPEED.
                    shinJoint.setMotorSpeed(1);
//                    cuz this bending works AGAINST gravity
                    shinJoint.setMaxMotorTorque(100);

                }else{ //special box case
                    shinJoint.setMotorSpeed(-1);

                }
            }else{
                //special box case
                if (aa>-45f){
                    shinJoint.setMotorSpeed(-1);
                }else{ //shin should rotate to max degrees
                    shinJoint.setMotorSpeed(1);
                }
//
            }
        }
        //temporary limits for now for testing usage.
        else if (v<-.2){ //down

            if (aa<=0f){
                //correct nt.
                if (aa > -135f){
                    shinJoint.setMotorSpeed(1);
                    shinJoint.setMaxMotorTorque(30);
                }else{ //special box case <-135f, arm goes straight downward bound ho stuff.
                    shinJoint.setMotorSpeed(-1);
                }
            }else{//aa<0
                //special box case
                if (aa>135f){
                    shinJoint.setMotorSpeed(-1);
                }else{
                    shinJoint.setMotorSpeed(1);
                }
            }
        }
        if (h<-.2){//right
            if (aa<0f){
                shinJoint.setMotorSpeed(-1);
//
            }else{
                shinJoint.setMotorSpeed(-1);
            }
        }else if (h>.2){//right
            if (aa<0f){
                if (aa>-90f){
                    //rotate?
                    shinJoint.setMotorSpeed(-1);

                }else if (aa > -135f){
                    shinJoint.setMotorSpeed(1);
                }else{
                    shinJoint.setMotorSpeed(-1);

                }
            }else{
                if (aa<45) shinJoint.setMotorSpeed(1);
                else if (aa > 135) shinJoint.setMotorSpeed(1);
                else{
                    shinJoint.setMotorSpeed(-1);

                }
            }
        }

        return null;

    }

    /**
     *
     * @param yval - value of vertical direction given by player
     * @param hookedPart - which leg is hooked
     * @return - force in y direction to apply
     * @author Jacob
     */
    //TODO this method is super outdated and only used for probably semi-correct torso-force calculations.
    //TODO completely redo this method for future use
    public static float calcLegForce(float yval, int hookedPart){
        float forcey = 0f;
        float angleKnee = 0f;
        float footToHip = 0f;
        float angleKneeModifier = 0f;
        float distanceModifier = 0f;
        float totalModifier = 0f;
        Vector2 hp = character.parts.get(hookedPart).getPosition();

        if (yval > 0) {
            if (hookedPart == FOOT_LEFT) {
                angleKnee = ((RevoluteJoint) (character.joints.get(SHIN_LEFT - 1))).getJointAngle() * RAD_TO_DEG;
                //TODO modify this because not sure how angles actually work with knees.
                angleKnee = Math.abs(angleKnee%270);
                angleKneeModifier = angleKnee <= 90f ? angleKnee / 90f : 90f / angleKnee;
            } else {
                angleKnee = ((RevoluteJoint) (character.joints.get(SHIN_RIGHT - 1))).getJointAngle() * RAD_TO_DEG;
                //TODO modify this as necessary because not sure how angles actually work with knees.
                angleKnee = Math.abs(angleKnee%270);
                angleKneeModifier = angleKnee <= 90f ? angleKnee / 90f : 90f / angleKnee;
            }
            angleKneeModifier = Math.abs(angleKneeModifier);
            footToHip = Math.abs(hp.x - character.parts.get(HIPS).getPosition().x);

            distanceModifier = (Math.abs(MAX_LEG_DIST) - footToHip)/Math.abs(MAX_LEG_DIST);
            //TODO: should probably be a more complex modifier!
            totalModifier = (angleKneeModifier + distanceModifier) / 2f;
            forcey = totalModifier * MAX_PUSHFORCE_LEG * yval;
        } else {
            forcey = MAX_PUSHFORCE_LEG * yval;
        }
        return forcey;
    }


    /**
     * @param yval - value of vertical direction given by player
     * @param hookedPart - which arm is hooked
     * @return - force in y direction to apply
     * @author Jacob
     */

    //TODO this method is super outdated and only used for probably semi-correct torso-force calculations.
    //TODO completely redo this method for future use
    public static float calcArmForce(float yval, int hookedPart) {
        float forcey = 0f;
        float angleElbow = 0f;
        Vector2 armToShoulder;
        float angleElbowModifier = 0f;
        Vector2 hp = character.parts.get(hookedPart).getPosition();
        if (yval > 0) {
            //the Y here is correct. need to subtract  ARM_OFFSET for x position for right hand,
            //add it for left hand.
            armToShoulder = hp.sub(character.parts.get(CHEST).getPosition());
            armToShoulder.y = armToShoulder.y - ARM_Y_CHEST_OFFSET;
            //absolute x distance of hand to shoulder.
            if (hookedPart == HAND_LEFT) {
                angleElbow = ((RevoluteJoint) (character.joints.get(HAND_LEFT - 1))).getJointAngle() * RAD_TO_DEG;
                angleElbowModifier = angleElbow <= 90f ? angleElbow / 90f : 90f / angleElbow;
                //add because hand left + offset - chest = 0 in best case.
                armToShoulder.x = armToShoulder.x + ARM_X_CHEST_OFFSET;
            }
            else{
                angleElbow = ((RevoluteJoint) (character.joints.get(HAND_RIGHT - 1))).getJointAngle() * RAD_TO_DEG;
                angleElbowModifier = angleElbow <= 180f ? angleElbow / 180f : 180f / angleElbow;
                armToShoulder.x = armToShoulder.x - ARM_X_CHEST_OFFSET;
            }
            armToShoulder.x = Math.abs(armToShoulder.x);
            //just in case - will be something needing debugging.
            angleElbowModifier = Math.abs(angleElbowModifier);

            //TODO modify this because not sure how angles actually work with elbows.
            //closer to 1 = the closer the hand is to the shoulder
            float distanceModifier = (Math.abs(MAX_ARM_DIST) - armToShoulder.x) / Math.abs(MAX_ARM_DIST);
            float totalModifier = (angleElbowModifier + distanceModifier) / 2f;
            //if the arm is going to impart pull or push force (legs cant really do that)
            //always a positive force up if input vertical is up

            forcey = armToShoulder.y > 0 ?
                    totalModifier * MAX_PULLFORCE_ARM : totalModifier * MAX_PUSHFORCE_ARM;
            forcey *= yval;
        }
        //working with ARMS NOW!!!!
        else {
            //I think that this should be basically unlimited amount force if you're lowering a limb then
//						it should be easy, there's not much force the legs use for this but this way it always happens.
//						return a negative number because that way the force for the limb can be channeled easily.
            forcey =  MAX_PULLFORCE_ARM * yval;


        }
        return forcey;
    }
    //I need to use the modifiers from calcArmForce and calcLegForce multiplied by the max force multiplied by these constant things.

//previously used methods that could still be helpful
//    public static float[] getPhysicallyCorrectForceMultipliersRightForearm(float jointAngle, float v, float h){
////		jointAngle *= -1;
////		might have to add above line in, depending on angle testing.
//        float armJointAngle = ((RevoluteJoint) character.joints.get(ARM_RIGHT-1)).getJointAngle() * RAD_TO_DEG;
////		System.out.println(armJointAngle + " right arm joint angle");
//        armJointAngle *= -1;
//        System.out.println(armJointAngle + " new right arm joint angle");
//
//        //assuming forearm joint limitations are from 0 to 120 degrees or whatever.
//        float forcexV = 0f;
//        float forceyV = 0f;
//        float forcexH = 0f;
//        float forceyH = 0f;
//        Vector2 vect;
//        boolean left = false;
//        if(((RevoluteJoint)character.joints.get(FOREARM_RIGHT-1)).getUpperLimit()*RAD_TO_DEG == FOREARM_PULLING_UPPER_LIMIT){
////			System.out.println("truth");
//            if (v>0){ //up direction
//                if (armJointAngle<45f){
//                    vect = forearmTo90(left);
//                    forcexV = vect.x;
//                    forceyV = vect.y;
//                }else{
//                    //move joint to 0 degree angle
//                    vect = forearmTo90(left);
//                    forcexV = -vect.x;
//                    forceyV = -vect.y;
//                }
//            }else{ //down direction
//                if (armJointAngle>-45f){
//                    vect = forearmTo90(left);
//                    forcexV = vect.x;
//                    forceyV = vect.y;
//                }else{
//                    //move joint to 0 degree angle
//                    vect = forearmTo90(left);
//                    forcexV = -vect.x;
//                    forceyV = -vect.y;
////					probably necessary eventually.
////					changeLimitsIfNecessary(FOREARM_LEFT,FOREARM_SWITCHING_EITHER_WAY,FOREARM_PUSHING_LOWER_LIMIT,FOREARM_PUSHING_UPPER_LIMIT);
//                    //rotate joints to about 5 degrees, then set forearm joint limits from -100 to 5 degrees
//                }
//            }
//            if (h>0){//right direction
//                if (armJointAngle>-45f){
//                    vect = forearmTo90(left);
//                    forcexH = -vect.x;
//                    forceyH = -vect.y;
//                }else{
//                    vect = forearmTo90(left);
//                    forcexH = vect.x;
//                    forceyH = vect.y;
//                    //might want to add in a "pushing the same way typa thing"
//                }
//                //otherwise do nothing
//            }else {//left direction
//                if (armJointAngle < -10f) {
//                    vect = forearmTo90(left);
//                    //move joint to 0 degree angle
//                    forcexH = -vect.x;
//                    forceyH = -vect.y;
//                }
//                else {
//                    vect = forearmTo90(left);
//                    //move joint to 0 degree angle
//                    forcexH = vect.x;
//                    forceyH = vect.y;
//                }
//                //else do nothing.
//            }
//        }
//        //values
//        else{
//            //Eventually write code detailing the times when the arm gets to this angle.
//        }
//        return new float[]{forcexV,forceyV,forcexH,forceyH};
//
//    }
//
//
//
//    public static float[] getPhysicallyCorrectForceMultipliersLeftForearm(float jointAngle, float v, float h,boolean left){
//        float armJointAngle = ((RevoluteJoint) character.joints.get(ARM_LEFT-1)).getJointAngle() * RAD_TO_DEG;
//        //assuming forearm joint limitations are from 0 to 120 degrees or whatever.
//        float forcexV = 0f;
//        float forceyV = 0f;
//        float forcexH = 0f;
//        float forceyH = 0f;
//        Vector2 vect;
//        //99% of the time
////		System.out.println(((RevoluteJoint)character.joints.get(FOREARM_LEFT-1)).getUpperLimit()*RAD_TO_DEG);
//
//        if(((RevoluteJoint)character.joints.get(FOREARM_LEFT-1)).getUpperLimit()*RAD_TO_DEG == FOREARM_PULLING_UPPER_LIMIT){
////			System.out.println("truth");
//            if (v>0){ //up direction
//                if (armJointAngle<45f){
//                    vect = forearmTo90(left);
//                    forcexV = vect.x;
//                    forceyV = vect.y;
//                }else{
//                    //move joint to 0 degree angle
//                    vect = forearmTo90(left);
//                    forcexV = -vect.x;
//                    forceyV = -vect.y;
//                }
//            }else{ //down direction
//                if (armJointAngle>0f){
//                    vect = forearmTo90(left);
//                    forcexV = vect.x;
//                    forceyV = vect.y;
//                }else{
//                    //move joint to 0 degree angle
//                    vect = forearmTo90(left);
//                    forcexV = -vect.x;
//                    forceyV = -vect.y;
////					probably necessary eventually.
////					changeLimitsIfNecessary(FOREARM_LEFT,FOREARM_SWITCHING_EITHER_WAY,FOREARM_PUSHING_LOWER_LIMIT,FOREARM_PUSHING_UPPER_LIMIT);
//                    //rotate joints to about 5 degrees, then set forearm joint limits from -100 to 5 degrees
//                }
//            }
//            if (h>0){//right direction
////                System.out.println("HERE");
//                if (armJointAngle>-45f){
//                    vect = forearmTo90(left);
//                    forcexH = vect.x;
//                    forceyH = vect.y;
//                }
//                else{
//                    vect = forearmTo90(left);
//                    forcexH = -vect.x;
//                    forceyH = -vect.y;
//                }
//                //otherwise do nothing
//            }else {//left direction
//                if (armJointAngle < 45f) {
//                    vect = forearmTo90(left);
//                    //move joint to 0 degree angle
//                    forcexH = -vect.x;
//                    forceyH = -vect.y;
//                }
//                else {
//                    vect = forearmTo90(left);
//                    //move joint to 0 degree angle
//                    forcexH = vect.x;
//                    forceyH = vect.y;
//                }
//                //else do nothing.
//            }
//        }
//        //values
//        else{
//            //Eventually write code detailing the times when the arm gets to this angle.
//        }
//        return new float[]{forcexV,forceyV,forcexH,forceyH};
//
//    }
//private static Vector2 forearmTo90(boolean left){
//    //if left forearm we are controlling:
//    float forcex = 0f;
//    float forcey = 0f;
//    if (left){
//        //depending on the angle of the arm itself.
//        //so you need to know the angle of the forearm relative to the up direction to be able to apply force.
//        //get absolute angle of the forearm itself relative to the world: so
//        Vector2 root = character.joints.get(FOREARM_LEFT - 1).getAnchorA();
//        Vector2 shoot = character.joints.get(HAND_LEFT - 1).getAnchorA();
//        float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);
//        if (absoluteAngle >= -180 && absoluteAngle <= -90) {
//            //forcex is 1 at -180 (moves right) and 0 at -90.
//            forcex = -1* (absoluteAngle + 90f)/ 90f;
//            //forcey is 0 at -180  and 1 at -90 (moves up).
//            forcey = 1 - forcex;
//            //nope
//        }else if (absoluteAngle > -90 && absoluteAngle<=0){
//            //forcex is 0 at -90 and -1 at 0 ((moves right)
//            forcex = -1 * (absoluteAngle+90f)/90f;
//            //forcey is 1 at -90 (moves up) and 0 at 0.
//            forcey = 1 + forcex;
//            //nope
//        }else if (absoluteAngle >0 && absoluteAngle <=90){
//            //forcex is -1 at 0 and 0 at 90
//            forcex = (absoluteAngle-90f)/90f;
//            //forcey is 0 at 0 and -1 at 90 (moves down).
//            forcey = -1 - forcex;
//        }else{ //absoluteAngle >90 && absoluteAngle <=180
//            //forcex is 0 at 90 and 1 at 180
//            forcex = (absoluteAngle-90f)/90f;
//            //forcey is  -1 at 90 (moves down) and 0 at 180 or -180
//            forcey = -1 + forcex;
//        }
//    }else{//right arm
//        Vector2 root = character.joints.get(FOREARM_RIGHT - 1).getAnchorA();
//        Vector2 shoot = character.joints.get(HAND_RIGHT - 1).getAnchorA();
//        float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);
//        if (absoluteAngle >= -180 && absoluteAngle <= -90) {
//            //forcex is -1 at -180 (moves left) and 0 at -90.
//            forcex = (absoluteAngle + 90f)/ 90f;
//            //forcey is 0 at 180 or -180 and -1 at -90 (moves down)
//            forcey = -1 - forcex;
//            //!
//        }else if (absoluteAngle >-90 && absoluteAngle<=0){
//            //forcex is 0 at -90 and 1 at 0 ((moves right)
//            forcex = -1 * (absoluteAngle+90f)/90f;
//            //forcey is -1 at -90 (moves down) and 0 at 0
//            forcey = -1 - forcex;
//        }else if (absoluteAngle >0 && absoluteAngle <=90){
//            //forcex is 1 at 0 and 0 at 90
//            forcex = (90f - absoluteAngle)/90f;
//            //forcey is 0 at 0 and 1 at 90 (moves up)
//            forcey = 1 - forcex;
//            //changed!
//        }else{ //absoluteAngle >90 && absoluteAngle <=180
//            //forcex is 0 at 90 and -1 at 180
//            forcex = -(absoluteAngle-90f)/90f;
//            //forcey is 1 at 90 (moves up) and 0 at -180/180
//            forcey = 1 + forcex;
//        }
//    }
//    return new Vector2(forcex,forcey);
//  }


}
