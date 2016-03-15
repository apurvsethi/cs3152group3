package beigegang.mountsputnik;

import beigegang.mountsputnik.GameObject.ObjectType;

public class ObstacleModel extends GameObject{
	
	@Override
	public ObjectType getType() {
		return ObjectType.OBSTACLE;
	}

}
