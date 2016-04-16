package beigegang.mountsputnik;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class ListenerClass implements ContactListener {
    
    public Array<StickyInfo> stickies = new Array<StickyInfo>();;
    
    @Override
    public void endContact(Contact contact) {
    }
    
    @Override
    public void beginContact(Contact contact) {
    	Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        GameObject objA = (GameObject) fixtureA.getUserData();
        GameObject objB = (GameObject) fixtureB.getUserData();
        
        if(objA instanceof ObstacleModel && objB instanceof PartModel){
        	//TODO: DO SOMETHING
        }
        else if(objB instanceof ObstacleModel && objA instanceof PartModel){
        	//TODO: DO SOMETHING
        }
    }

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}
}
