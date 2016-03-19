package beigegang.mountsputnik;


import static beigegang.mountsputnik.Constants.*;
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
        String typeA = (String) fixtureA.getUserData();
        String typeB = (String) fixtureB.getUserData();
        if(typeA == null || typeB == null)
        	return;
        
        if((typeA.equals("handhold")&&(typeB.contains("extremity"))))
	    	stickies.add(new StickyInfo(fixtureB.getBody(), fixtureA.getBody(),typeB));
        else if((typeB.equals("handhold")&&(typeA.contains("extremity"))))
        	stickies.add(new StickyInfo(fixtureA.getBody(), fixtureB.getBody(),typeA));
    }

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}
}
