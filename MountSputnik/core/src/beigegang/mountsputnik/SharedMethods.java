package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static beigegang.mountsputnik.Constants.*;

/**
 * Created by jacobcooper on 5/10/16.
 */
public class SharedMethods {
    /*public void updateHelper(){
        if (isPaused) pauseMode.update(dt, listener);
        else if (isDead) deadMode.update(dt, listener);
        else if (isVictorious){
            if (!writtenToFile) {
                try {
                    makeJsonForAnimation();

                    writeJsonToFile();

                    writtenToFile = true;
                } catch (Exception e) {
                    System.out.println("here3" + e);
                }
            }
            victoryMode.update(dt, listener,true);
        }
        else {
            doingAnimation = input.watchAnimation() && currLevel == LEVEL_TUTORIAL;
            if (doingAnimation) {
                getAnimationInformation();
                inx = animationLX;
                iny = animationLY;
                rinx = animationRX;
                riny = animationRY;
                nextToPress = animationNextToPress;
                justReleased = animationJustReleased;
            } else {
                inx = input.getHorizontalL();
                iny = input.getVerticalL();
                rinx = input.getHorizontalR();
                riny = input.getVerticalR();
                nextToPress = input.getOrderPressed();
                justReleased.clear();
                if (input.releasedLeftArm()) justReleased.add(HAND_LEFT);
                if (input.releasedRightArm()) justReleased.add(HAND_RIGHT);
                if (input.releasedLeftLeg()) justReleased.add(FOOT_LEFT);
                if (input.releasedRightLeg()) justReleased.add(FOOT_RIGHT);

            }
            //don't uncomment createAnimation unless you know what you are doing!!
    //		createAnimation();
    //
            if (checkIfReachedCheckpoint(character)) {
                lastReachedCheckpoint++;
            }
            if (checkIfDied(character)) {
                listener.exitScreen(this, EXIT_DIED);

            }
    //		upsideDown = character.parts.get(HEAD).getPosition().y - character.parts.get(CHEST).getPosition().y <= 0;


            if (input.didSelect()) {
                if (id == 0) tutorialToggle1 = !tutorialToggle1;
                else tutorialToggle2 = !tutorialToggle2;
            }

            if (input.didMenu()) listener.exitScreen(this, EXIT_PAUSE);

            movementController.moveCharacter(inx,iny,rinx,riny,nextToPress,justReleased);
    //            if (nextToPress.size > 0)
    //                oldMovementController.findAndApplyForces(nextToPress.get(0),iny,inx);

            if (nextToPress.size > 0) {
                for (int i : nextToPress) {
                    ((ExtremityModel) (character.parts.get(i))).ungrip();
                    ungrip(((ExtremityModel) (character.parts.get(i))));
                }
            }
            //bounding velocities
            boundBodyVelocities(character);
            HandholdModel[] glowingHandholds = glowHandholds(character);

            snapLimbsToHandholds(glowingHandholds,character,justReleased);

            cameraWork();

            dealWithSlipperyAndCrumblyHandholds(character);

            spawnObstacles();

            for (GameObject g : objects) {

                if (g instanceof ObstacleModel &&
                        ((g.getBody().getPosition().y < (canvas.getCamera().position.y - canvas.getWidth()) / scale.y &&
                                g.getBody().getType() != BodyDef.BodyType.StaticBody) || ((ObstacleModel)g).broken)) {
                    objects.remove(g);
                }
                if (g instanceof HandholdModel && ((HandholdModel) (g)).getStartPoint() != null) {
                    HandholdModel h = (HandholdModel) g;
                    h.updateSnapPoints();
                    if (withinBounds(h.getBody().getPosition(), h.getEndPoint()) ||
                            withinBounds(h.getBody().getPosition(), h.getStartPoint())) {
                        h.getBody().setLinearVelocity(h.getBody().getLinearVelocity().x * -1, h.getBody().getLinearVelocity().y * -1);
                    }
                }
            }

            // TODO: Update energy quantity (fill in these values)
            vector = new Vector2(character.parts.get(CHEST).getVX(), character.parts.get(CHEST).getVY());
            character.updateEnergy(oxygen, 1, vector.len(), true);

            if (risingObstacle != null) {
                risingObstacle.setHeight(risingObstacle.getHeight() + risingObstacle.getSpeed());
                for (PartModel p : character.parts) {
                    if (risingObstacle.getHeight() >= p.getPosition().y) {
                        character.setEnergy(0);
                        failed = true;
                    }
                }
                float yToSet = Math.min(canvas.getCamera().position.y/character.parts.get(CHEST).drawPositionScale.y, character.parts.get(CHEST).getPosition().y);
                if(risingObstacle.getHeight() < yToSet - DEFAULT_HEIGHT/2 -1){
                    risingObstacle.setHeight(yToSet - DEFAULT_HEIGHT/2 -1);
                }
            }

            if (character.getEnergy() <= 0) {
                failed = true;
                for (int e : EXTREMITIES) {
                    ExtremityModel extremity = (ExtremityModel) character.parts.get(e);
                    ungrip(extremity);
                    extremity.ungrip();
                    extremity.body.setType(BodyDef.BodyType.DynamicBody);
                    extremity.setTexture(partTextures[e].getTexture());
                }
            }
            checkHasCompleted(character);
            if (complete) {
                listener.exitScreen(this, EXIT_VICTORY_RACE);
            }
            if (checkpointTimestep == 0) cposYAtTime0 = character.parts.get(HEAD).getY();

        }
    }*/












    public static void drawBackgrounds(GameCanvas canvas, TextureRegion ground, TextureRegion background, TextureRegion midground, TextureRegion foreground, TextureRegion tile, TextureRegion edge, int levelName){
        float y = canvas.getCamera().position.y - canvas.getHeight() / 2;
        float h; 
        if (levelName == LEVEL_VOLCANO) 
        	h = midground.getTexture().getHeight(); 
        	else h = canvas.getHeight(); 
        float tileY = y - (y % (canvas.getWidth() / 4));
        canvas.draw(background, Color.WHITE, 0, y, canvas.getWidth(), canvas.getHeight());

        canvas.draw(midground, Color.WHITE, canvas.getWidth() * 4 / 5, y * MIDGROUND_SCROLL, canvas.getWidth() / 5, h);
        midground.flip(true,false);
        canvas.draw(midground, Color.WHITE, 0, y * MIDGROUND_SCROLL, canvas.getWidth() / 5, h);
        midground.flip(true,false);
        
        canvas.draw(foreground, Color.WHITE, canvas.getWidth() * 4 / 5, y * FOREGROUND_SCROLL, canvas.getWidth() / 5, foreground.getTexture().getHeight());
        foreground.flip(true,false);
        canvas.draw(foreground, Color.WHITE, 0, y * FOREGROUND_SCROLL, canvas.getWidth() / 5, foreground.getTexture().getHeight());
        foreground.flip(true,false);

        for (int counterInt = 0; counterInt < 5; counterInt++) {
            canvas.draw(tile, Color.WHITE, canvas.getWidth() / 5, tileY, 3*canvas.getWidth() / 10, canvas.getWidth() / 4);
            canvas.draw(tile, Color.WHITE, (canvas.getWidth()-1) / 2, tileY, 3*canvas.getWidth() / 10, canvas.getWidth() / 4);
            canvas.draw(edge, Color.WHITE, (canvas.getWidth()-1) * 4 / 5, tileY, canvas.getWidth() / 16, canvas.getHeight());
            edge.flip(true,false);
            canvas.draw(edge, Color.WHITE, canvas.getWidth() / 5 - canvas.getWidth() / 16, tileY, canvas.getWidth() / 16, canvas.getHeight());
            edge.flip(true,false);

            tileY += canvas.getWidth() / 4;
        }
        canvas.draw(ground, Color.WHITE, canvas.getWidth() / 5, 0, 3*canvas.getWidth() / 5, canvas.getHeight() / 8);

    }

    public static void drawShadow(CharacterModel character, TextureRegion[] shadowTextures, GameCanvas canvas) {
        for (int i = 0; i < character.parts.size; i++){
            character.parts.get(i).drawShadow(shadowTextures[i], canvas);
        }
    }

    public static void drawUI(GameCanvas canvas, float xStart, Sprite UISprite, SpriteBatch batch) {
        UISprite.setBounds(xStart, 0, canvas.getWidth() / 5, canvas.getHeight());
        UISprite.setAlpha(.7f); //value can be changed.
        batch.begin();
        UISprite.draw(batch);
        batch.end();

    }

    public static void drawProgress(GameCanvas canvas, TextureRegion[] progressTextures, TextureRegion progressBackgroundTexture,int progressLevel, float xStart,float yStart) {
        if (progressLevel > 0) {
            canvas.draw(progressTextures[progressLevel-1], Color.WHITE, xStart, yStart, canvas.getWidth() / 5, canvas.getHeight());
        }
        canvas.draw(progressBackgroundTexture, Color.WHITE, xStart, yStart, canvas.getWidth() / 5, canvas.getHeight());

    }

    public static int drawEnergy(GameCanvas canvas, CharacterModel character,TextureRegion[] energyTextures, TextureRegion fatigueTexture, Sprite lowEnergySprite,SpriteBatch batch, int energyLevel, int x, float y,int flashing2) {
        //draw flashing for bar.
        float f = character.getEnergy();

        if (f<= 30){
            lowEnergySprite.setBounds(x,0,canvas.getWidth()/5,canvas.getHeight());
            lowEnergySprite.setAlpha(.5f + Math.min((30-f)/f,.5f));
            batch.begin();
            lowEnergySprite.draw(batch);
            batch.end();

            flashing2 --;
            canvas.begin();
            if (flashing2<f/4){
                canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.BLACK, x, y, canvas.getWidth() / 5, canvas.getHeight());

                if (flashing2<=0)
                    flashing2 = Math.round(f/2);
            }else {
                canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.WHITE, x, y, canvas.getWidth() / 5, canvas.getHeight());

            }
        }else{
            canvas.begin();
            canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.WHITE, x, y, canvas.getWidth() / 5, canvas.getHeight());
        }

        canvas.draw(fatigueTexture, Color.WHITE, x, y, canvas.getWidth() / 5, canvas.getHeight());
        return flashing2;
    }

    public static void drawToggles(GameCanvas canvas, CharacterModel c,InputController in,TextureRegion[] tutorialTextures, Vector2 scale){
        TextureRegion t;

        Vector2 vector = c.parts.get(HAND_LEFT).getPosition();
        t = in.didLeftArm() ? tutorialTextures[4] : tutorialTextures[0];
        canvas.draw(t, Color.WHITE, (vector.x*scale.x)-10, (vector.y*scale.y),50,50);

        vector = c.parts.get(HAND_RIGHT).getPosition();
        t = in.didRightArm() ? tutorialTextures[5] : tutorialTextures[1];
        canvas.draw(t, Color.WHITE, (vector.x*scale.x)+10, (vector.y*scale.y),50,50);

        vector = c.parts.get(FOOT_LEFT).getPosition();
        t = in.didLeftLeg() ? tutorialTextures[6] : tutorialTextures[2];
        canvas.draw(t, Color.WHITE, (vector.x*scale.x)-10, (vector.y*scale.y),40,40);

        vector = c.parts.get(FOOT_RIGHT).getPosition();
        t = in.didRightLeg() ? tutorialTextures[7] : tutorialTextures[3];
        canvas.draw(t, Color.WHITE, (vector.x*scale.x)+10, (vector.y*scale.y),40,40);
    }

}
