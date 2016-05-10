package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static beigegang.mountsputnik.Constants.*;
import static beigegang.mountsputnik.Constants.FOOT_LEFT;
import static beigegang.mountsputnik.Constants.FOOT_RIGHT;

/**
 * Created by jacobcooper on 5/10/16.
 */
public class DrawingMethods {
    public static void drawBackgrounds(GameCanvas canvas, TextureRegion ground, TextureRegion background, TextureRegion midground, TextureRegion tile, TextureRegion edge){
        float y = canvas.getCamera().position.y - canvas.getHeight() / 2;
        float tileY = y - (y % (canvas.getWidth() / 4));
        canvas.draw(background, Color.WHITE, canvas.getWidth() * 3 / 4, y, canvas.getWidth() / 4, canvas.getHeight());
        background.flip(true,false);
        canvas.draw(background, Color.WHITE, 0, y, canvas.getWidth() / 4, canvas.getHeight());
        background.flip(true,false);

        canvas.draw(midground, Color.WHITE, canvas.getWidth() * 3 / 4, y * MIDGROUND_SCROLL, canvas.getWidth() / 4, canvas.getHeight());
        midground.flip(true,false);
        canvas.draw(midground, Color.WHITE, 0, y * MIDGROUND_SCROLL, canvas.getWidth() / 4, canvas.getHeight());
        midground.flip(true,false);

        for (int counterInt = 0; counterInt < 5; counterInt++) {
            canvas.draw(tile, Color.WHITE, canvas.getWidth() / 4, tileY, canvas.getWidth() / 4, canvas.getWidth() / 4);
            canvas.draw(tile, Color.WHITE, canvas.getWidth() / 2, tileY, canvas.getWidth() / 4, canvas.getWidth() / 4);
            canvas.draw(edge, Color.WHITE, canvas.getWidth() * 3 / 4, tileY, canvas.getWidth() / 16, canvas.getHeight());
            edge.flip(true,false);
            canvas.draw(edge, Color.WHITE, canvas.getWidth() / 4 - canvas.getWidth() / 16, tileY, canvas.getWidth() / 16, canvas.getHeight());
            edge.flip(true,false);

            tileY += canvas.getWidth() / 4;
        }
        canvas.draw(ground, Color.WHITE, canvas.getWidth() / 4, 0, canvas.getWidth() / 2, canvas.getHeight() / 8);

    }

    public static void drawShadow(CharacterModel character, TextureRegion[] shadowTextures, GameCanvas canvas) {
        for (int i = 0; i < character.parts.size; i++){
            character.parts.get(i).drawShadow(shadowTextures[i], canvas);
        }
    }

    public static void drawUI(GameCanvas canvas, float xStart, Sprite UISprite, SpriteBatch batch) {
        UISprite.setBounds(xStart, 0, canvas.getWidth() / 4, canvas.getHeight());
        UISprite.setAlpha(.7f); //value can be changed.
        batch.begin();
        UISprite.draw(batch);
        batch.end();

    }

    public static void drawProgress(GameCanvas canvas, TextureRegion[] progressTextures, TextureRegion progressBackgroundTexture,int progressLevel, float xStart,float yStart) {
        if (progressLevel > 0) {
            canvas.draw(progressTextures[progressLevel-1], Color.WHITE, xStart, yStart, canvas.getWidth() / 4, canvas.getHeight());
        }
        canvas.draw(progressBackgroundTexture, Color.WHITE, xStart, yStart, canvas.getWidth() / 4, canvas.getHeight());

    }

    public static int drawEnergy(GameCanvas canvas, CharacterModel character,TextureRegion[] energyTextures, TextureRegion fatigueTexture, Sprite lowEnergySprite,SpriteBatch batch, int energyLevel, int x, float y,int flashing2) {
        //draw flashing for bar.
        float f = character.getEnergy();

        if (f<= 30){
            lowEnergySprite.setBounds(x,0,canvas.getWidth()/4,canvas.getHeight());
            lowEnergySprite.setAlpha(.5f + Math.min((30-f)/f,.5f));
            batch.begin();
            lowEnergySprite.draw(batch);
            batch.end();

            flashing2 --;
            canvas.begin();
            if (flashing2<f/4){
                canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.BLACK, x, y, canvas.getWidth() / 4, canvas.getHeight());

                if (flashing2<=0)
                    flashing2 = Math.round(f/2);
            }else {
                canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.WHITE, x, y, canvas.getWidth() / 4, canvas.getHeight());

            }
        }else{
            canvas.begin();
            canvas.draw(energyTextures[Math.min(energyLevel,energyTextures.length - 1)], Color.WHITE, x, y, canvas.getWidth() / 4, canvas.getHeight());
        }

        canvas.draw(fatigueTexture, Color.WHITE, x, y, canvas.getWidth() / 4, canvas.getHeight());
        return flashing2;
    }

    public static void drawObstacleWarnings(GameCanvas canvas, Array<GameMode.warningsClass> obstacleWarnings,Sprite warningSprite,SpriteBatch batch,Vector2 scale,float y) {
        for (GameMode.warningsClass wc : obstacleWarnings) {
            //hack to allow warning to move with obstacle for space!
            wc.center = wc.o.getX() + wc.o.width/2;
            warningSprite.setBounds(wc.center * scale.x -  1.5f * scale.x, y/scale.y + canvas.getHeight()*9f/10f, 3f * scale.x , canvas.getHeight()/10f);
            warningSprite.setAlpha(Math.min(1,(wc.opacity)/(Math.min(TIME_TO_WARN,wc.oz.getSpawnFrequency()))));
            wc.opacity ++;
            batch.begin();
            warningSprite.draw(batch);
            batch.end();
        }
    }
    public static void drawObstacleWarnings(GameCanvas canvas, Array<RaceMode.warningsClass> obstacleWarnings,Sprite warningSprite,SpriteBatch batch,Vector2 scale,float y,float nothing) {
        for (RaceMode.warningsClass wc : obstacleWarnings) {
            //hack to allow warning to move with obstacle for space!
            wc.center = wc.o.getX() + wc.o.width/2;
            warningSprite.setBounds(wc.center * scale.x -  1.5f * scale.x, y/scale.y + canvas.getHeight()*9f/10f, 3f * scale.x , canvas.getHeight()/10f);
            warningSprite.setAlpha(Math.min(1,(wc.opacity)/(Math.min(TIME_TO_WARN,wc.oz.getSpawnFrequency()))));
            wc.opacity ++;
            batch.begin();
            warningSprite.draw(batch);
            batch.end();
        }
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
