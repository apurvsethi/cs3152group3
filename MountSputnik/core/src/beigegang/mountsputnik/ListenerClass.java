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
        
        if(objA instanceof ObstacleModel && objB instanceof PartModel && !((ObstacleModel)objA).breaking){
        	CharacterModel c = ((PartModel) objB).getCharacter();
        	c.setEnergy(c.getEnergy()-40);
        	c.setStun(5f);
            ((ObstacleModel) objA).breakObstacle();
        }
        else if(objB instanceof ObstacleModel && objA instanceof PartModel && !((ObstacleModel)objB).breaking){
        	CharacterModel c = ((PartModel) objA).getCharacter();
        	c.setEnergy(c.getEnergy()-40);
        	c.setStun(5f);
            ((ObstacleModel) objB).breakObstacle();
        }
    }

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        GameObject objA = (GameObject) fixtureA.getUserData();
        GameObject objB = (GameObject) fixtureB.getUserData();
        
        if(objA instanceof HandholdModel && objB instanceof PartModel || 
        		objB instanceof HandholdModel && objA instanceof PartModel)
        	contact.setEnabled(false);
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}
}
