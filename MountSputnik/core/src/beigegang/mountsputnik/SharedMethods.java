package beigegang.mountsputnik;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
    public static void drawBackgrounds(GameCanvas canvas, TextureRegion ground, TextureRegion background, TextureRegion midground, TextureRegion foreground, TextureRegion tile, TextureRegion edge, int levelName){
        float y = canvas.getCamera().position.y - canvas.getHeight() / 2;
        float h; 
        if (levelName == LEVEL_VOLCANO || levelName == LEVEL_TUTORIAL) 
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
            canvas.draw(edge, Color.WHITE, (canvas.getWidth()-1) * 4 / 5, tileY + canvas.getHeight()/8, canvas.getWidth() / 16, canvas.getWidth()/4);
            edge.flip(true,false);
            canvas.draw(edge, Color.WHITE, canvas.getWidth() / 5 - canvas.getWidth() / 16, tileY + canvas.getHeight()/8, canvas.getWidth() / 16, canvas.getWidth()/4);
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

    public static void drawUI(GameCanvas canvas, float xStart, Texture UISprite, float alpha) {
    	Color t = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
        canvas.draw(UISprite, t, xStart, canvas.getCamera().position.y-canvas.getHeight()/2, canvas.getWidth()/5, canvas.getHeight());
    }

    public static void drawProgress(GameCanvas canvas, TextureRegion[] progressTextures, TextureRegion progressBackgroundTexture,int progressLevel, float xStart,float yStart) {
        if (progressLevel > 0) {
            canvas.draw(progressTextures[progressLevel-1], Color.WHITE, xStart, yStart, canvas.getWidth() / 5, canvas.getHeight());
        }
        canvas.draw(progressBackgroundTexture, Color.WHITE, xStart, yStart, canvas.getWidth() / 5, canvas.getHeight());

    }

    public static int drawEnergy(GameCanvas canvas, CharacterModel character,TextureRegion[] energyTextures, TextureRegion fatigueTexture, Texture lowEnergySprite, int energyLevel, int x, float y,int flashing2) {
        //draw flashing for bar.
        float f = character.getEnergy();

        if (f<= 30){

            flashing2 --;
            canvas.begin();

            canvas.draw(lowEnergySprite, Color.WHITE, x, canvas.getCamera().position.y-canvas.getHeight()/2, canvas.getWidth()/5, canvas.getHeight());
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
