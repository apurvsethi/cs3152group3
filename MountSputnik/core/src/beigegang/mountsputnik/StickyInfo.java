package beigegang.mountsputnik;

import com.badlogic.gdx.physics.box2d.Body;
import static beigegang.mountsputnik.Constants.*;

public class StickyInfo{
	public Body bodyA;
    public Body bodyB;
    public String bodypart;
    public StickyInfo(Body bodyA, Body bodyB, String bodypart){
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.bodypart = bodypart;
    }
}
