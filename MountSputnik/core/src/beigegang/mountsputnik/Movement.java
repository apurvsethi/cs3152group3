package beigegang.mountsputnik;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;

import static beigegang.mountsputnik.Constants.*;
/**
 * Created by jacobcooper on 4/11/16.
 */
public class Movement {
    public static CharacterModel character;
    public static void setCharacter(CharacterModel c){
        character = c;
    }


    private static Vector2 forearmTo90(boolean left){
        //if left forearm we are controlling:
        float forcex = 0f;
        float forcey = 0f;
        if (left){
            //depending on the angle of the arm itself.
            //so you need to know the angle of the forearm relative to the up direction to be able to apply force.
            //get absolute angle of the forearm itself relative to the world: so
            Vector2 root = character.joints.get(FOREARM_LEFT - 1).getAnchorA();
            Vector2 shoot = character.joints.get(HAND_LEFT - 1).getAnchorA();
            float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);
            if (absoluteAngle >= -180 && absoluteAngle <= -90) {
                //forcex is 1 at -180 (moves right) and 0 at -90.
                forcex = -1* (absoluteAngle + 90f)/ 90f;
                //forcey is 0 at -180  and 1 at -90 (moves up).
                forcey = 1 - forcex;
                //nope
            }else if (absoluteAngle > -90 && absoluteAngle<=0){
                //forcex is 0 at -90 and -1 at 0 ((moves right)
                forcex = -1 * (absoluteAngle+90f)/90f;
                //forcey is 1 at -90 (moves up) and 0 at 0.
                forcey = 1 + forcex;
                //nope
            }else if (absoluteAngle >0 && absoluteAngle <=90){
                //forcex is -1 at 0 and 0 at 90
                forcex = (absoluteAngle-90f)/90f;
                //forcey is 0 at 0 and -1 at 90 (moves down).
                forcey = -1 - forcex;
            }else{ //absoluteAngle >90 && absoluteAngle <=180
                //forcex is 0 at 90 and 1 at 180
                forcex = (absoluteAngle-90f)/90f;
                //forcey is  -1 at 90 (moves down) and 0 at 180 or -180
                forcey = -1 + forcex;
            }
        }else{//right arm
            Vector2 root = character.joints.get(FOREARM_RIGHT - 1).getAnchorA();
            Vector2 shoot = character.joints.get(HAND_RIGHT - 1).getAnchorA();
            float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);
            System.out.println("abs angle" + absoluteAngle);
            if (absoluteAngle >= -180 && absoluteAngle <= -90) {
                //forcex is -1 at -180 (moves left) and 0 at -90.
                forcex = (absoluteAngle + 90f)/ 90f;
                //forcey is 0 at 180 or -180 and -1 at -90 (moves down)
                forcey = -1 - forcex;
                //!
            }else if (absoluteAngle >-90 && absoluteAngle<=0){
                //forcex is 0 at -90 and 1 at 0 ((moves right)
                forcex = -1 * (absoluteAngle+90f)/90f;
                //forcey is -1 at -90 (moves down) and 0 at 0
                forcey = -1 - forcex;
            }else if (absoluteAngle >0 && absoluteAngle <=90){
                //forcex is 1 at 0 and 0 at 90
                forcex = (90f - absoluteAngle)/90f;
                //forcey is 0 at 0 and 1 at 90 (moves up)
                forcey = 1 - forcex;
                //changed!
            }else{ //absoluteAngle >90 && absoluteAngle <=180
                //forcex is 0 at 90 and -1 at 180
                forcex = -(absoluteAngle-90f)/90f;
                //forcey is 1 at 90 (moves up) and 0 at -180/180
                forcey = 1 + forcex;
            }
        }
        return new Vector2(forcex,forcey);
    }

    /**
     *
     * @param rootAnchor
     * @param shootAnchor
     * @return absolute angle, from -180 to 180, of part on the board/screen - arm pointing up has angle of 0 degrees.
     * arm pointing to the right has an angle of 90 degrees
     */
    //TODO double triple check everything in here
    private static float findAbsoluteAngleOfPart(Vector2 rootAnchor, Vector2 shootAnchor) {
        double dy = rootAnchor.y - shootAnchor.y;
        double dx = rootAnchor.x - shootAnchor.x;
        //pretends like -x axis is root axis of atan2
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

    public static float[] getPhysicallyCorrectForceMultipliersRightForearm(float jointAngle, float v, float h){
//		jointAngle *= -1;
//		might have to add above line in, depending on angle testing.
        float armJointAngle = ((RevoluteJoint) character.joints.get(ARM_RIGHT-1)).getJointAngle() * RAD_TO_DEG;
//		System.out.println(armJointAngle + " right arm joint angle");
        armJointAngle *= -1;
        System.out.println(armJointAngle + " new right arm joint angle");

        //assuming forearm joint limitations are from 0 to 120 degrees or whatever.
        float forcexV = 0f;
        float forceyV = 0f;
        float forcexH = 0f;
        float forceyH = 0f;
        Vector2 vect;
        boolean left = false;
        if(((RevoluteJoint)character.joints.get(FOREARM_RIGHT-1)).getUpperLimit()*RAD_TO_DEG == FOREARM_PULLING_UPPER_LIMIT){
//			System.out.println("truth");
            if (v>0){ //up direction
                if (armJointAngle<45f){
                    vect = forearmTo90(left);
                    forcexV = vect.x;
                    forceyV = vect.y;
                }else{
                    //move joint to 0 degree angle
                    vect = forearmTo90(left);
                    forcexV = -vect.x;
                    forceyV = -vect.y;
                }
            }else{ //down direction
                if (armJointAngle>-45f){
                    vect = forearmTo90(left);
                    forcexV = vect.x;
                    forceyV = vect.y;
                }else{
                    //move joint to 0 degree angle
                    vect = forearmTo90(left);
                    forcexV = -vect.x;
                    forceyV = -vect.y;
//					probably necessary eventually.
//					changeLimitsIfNecessary(FOREARM_LEFT,FOREARM_SWITCHING_EITHER_WAY,FOREARM_PUSHING_LOWER_LIMIT,FOREARM_PUSHING_UPPER_LIMIT);
                    //rotate joints to about 5 degrees, then set forearm joint limits from -100 to 5 degrees
                }
            }
            if (h>0){//right direction
                System.out.println("here...");
                if (armJointAngle>-45f){
                    vect = forearmTo90(left);
                    forcexH = -vect.x;
                    forceyH = -vect.y;
                }else{
                    vect = forearmTo90(left);
                    forcexH = vect.x;
                    forceyH = vect.y;
                    //might want to add in a "pushing the same way typa thing"
                }
                //otherwise do nothing
            }else {//left direction
                if (armJointAngle < -10f) {
                    vect = forearmTo90(left);
                    //move joint to 0 degree angle
                    forcexH = -vect.x;
                    forceyH = -vect.y;
                }
                else {
                    vect = forearmTo90(left);
                    //move joint to 0 degree angle
                    forcexH = vect.x;
                    forceyH = vect.y;
                }
                //else do nothing.
            }
        }
        //values
        else{
            //Eventually write code detailing the times when the arm gets to this angle.
        }
        return new float[]{forcexV,forceyV,forcexH,forceyH};

    }



    public static float[] getPhysicallyCorrectForceMultipliersLeftForearm(float jointAngle, float v, float h,boolean left){
        float armJointAngle = ((RevoluteJoint) character.joints.get(ARM_LEFT-1)).getJointAngle() * RAD_TO_DEG;
        //assuming forearm joint limitations are from 0 to 120 degrees or whatever.
        float forcexV = 0f;
        float forceyV = 0f;
        float forcexH = 0f;
        float forceyH = 0f;
        Vector2 vect;
        //99% of the time
//		System.out.println(((RevoluteJoint)character.joints.get(FOREARM_LEFT-1)).getUpperLimit()*RAD_TO_DEG);

        if(((RevoluteJoint)character.joints.get(FOREARM_LEFT-1)).getUpperLimit()*RAD_TO_DEG == FOREARM_PULLING_UPPER_LIMIT){
//			System.out.println("truth");
            if (v>0){ //up direction
                System.out.println(armJointAngle + "armjoint");
                if (armJointAngle<45f){
                    vect = forearmTo90(left);
                    forcexV = vect.x;
                    forceyV = vect.y;
                }else{
                    //move joint to 0 degree angle
                    vect = forearmTo90(left);
                    forcexV = -vect.x;
                    forceyV = -vect.y;
                }
            }else{ //down direction
                if (armJointAngle>0f){
                    vect = forearmTo90(left);
                    forcexV = vect.x;
                    forceyV = vect.y;
                }else{
                    //move joint to 0 degree angle
                    vect = forearmTo90(left);
                    forcexV = -vect.x;
                    forceyV = -vect.y;
//					probably necessary eventually.
//					changeLimitsIfNecessary(FOREARM_LEFT,FOREARM_SWITCHING_EITHER_WAY,FOREARM_PUSHING_LOWER_LIMIT,FOREARM_PUSHING_UPPER_LIMIT);
                    //rotate joints to about 5 degrees, then set forearm joint limits from -100 to 5 degrees
                }
            }
            if (h>0){//right direction
//                System.out.println("HERE");
                if (armJointAngle>-45f){
                    vect = forearmTo90(left);
                    forcexH = vect.x;
                    forceyH = vect.y;
                }
                else{
                    vect = forearmTo90(left);
                    forcexH = -vect.x;
                    forceyH = -vect.y;
                }
                //otherwise do nothing
            }else {//left direction
                if (armJointAngle < 45f) {
                    vect = forearmTo90(left);
                    //move joint to 0 degree angle
                    forcexH = -vect.x;
                    forceyH = -vect.y;
                    System.out.println(forcexH + " " + forceyH);
                }
                else {
                    vect = forearmTo90(left);
                    //move joint to 0 degree angle
                    forcexH = vect.x;
                    forceyH = vect.y;
                }
                //else do nothing.
            }
        }
        //values
        else{
            //Eventually write code detailing the times when the arm gets to this angle.
        }
        return new float[]{forcexV,forceyV,forcexH,forceyH};

    }

    private void changeLimitsIfNecessary(int part,float boundary,float lowerLimit,float upperLimit) {
        float jointAngle = ((RevoluteJoint) character.joints.get(part-1)).getJointAngle() * RAD_TO_DEG;
        //avoiding any floating point weirdness
        if (jointAngle<boundary - .1){
            ((RevoluteJoint) character.joints.get(part-1)).setLimits(lowerLimit,upperLimit);
        }

    }


    public static float[] getPhysicallyCorrectForceMultipliersRightArm(float jointAngle, float v, float h) {
//		System.out.println(jointAngle);
        //necessary to make it line up with angles of left arm.
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
    public static float[] getPhysicallyCorrectForceMultipliersLeftArm(float jointAngle, float v, float h){

        Vector2 root = character.joints.get(ARM_LEFT - 1).getAnchorA();
        Vector2 shoot = character.joints.get(FOREARM_LEFT - 1).getAnchorA();
        float forcexV = 0f;
        float forceyV = 0f;
        float forcexH = 0f;
        float forceyH = 0f;
        float absoluteAngle = findAbsoluteAngleOfPart(root,shoot);
        System.out.println(absoluteAngle + " abs angle ");
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


//		float jointAngle = ((RevoluteJoint) character.joints.get(ARM_LEFT-1)).getJointAngle() * RAD_TO_DEG;
//		System.out.println(jointAngle);
        //left arm (upper) only.
        //applying force vertically upwards: - based on sketches i did.
        //not sure if this is the case, but this is assuming both f.x and f.y are positive.
//		probably a misplaced assumption which means that something would need to be changed - negative sign
//		in calculating forcexV.
        //
        //SHIT SHIT SHIT -
        // this "joint angle" thing i think is relevant to the absolute position of the arm on the screen.
        //I'm not sure how it would work if the chest was, say, sorta sideways for example.
        //something to ponder further.


        return new float[]{forcexV,forceyV,forcexH,forceyH};
    }
}
