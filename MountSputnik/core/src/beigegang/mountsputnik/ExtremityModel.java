package beigegang.mountsputnik;

import beigegang.util.FilmStrip;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import static beigegang.mountsputnik.SoundController.*;
import static beigegang.mountsputnik.Constants.*;

public class ExtremityModel extends PartModel {
	/** Whether or not this extremity is gripping*/
	private boolean gripped;
	/** A revolute joint for when this extremity is gripping a hold **/ 
	private Joint joint; 
	/** Updated for how long the extremity has gripped the handhold it is on */
	private int gripTime; 
	
	@Override
	public ObjectType getType() {
		return ObjectType.EXTREMITY;
	}
	
	/**
	 * Returns the whether or not this extremity is gripping
	 *
	 * @return Returns the whether or not this extremity is gripping
	 */
	public boolean isGripping(){
		return gripped;
	}
	
	/** Sets the value of isGripping to true*/
	public void grip(){
		if (GRIP_SOUNDS_ON) SoundController.get(SoundController.GRIP_SOUND).play();
		gripped = true;
		gripTime = 0;
		if (animator.getSize() != 1) animator.setFrame(1);
	}

	public boolean isGripped(){
		return gripped;
	}
	/** Sets the value of isGripping to false*/
	public void ungrip(){
		if (gripped && GRIP_SOUNDS_ON) SoundController.get(SoundController.UNGRIP_SOUND).play();
		gripped = false;
		gripTime = 0;
		animator.setFrame(0);
	}
	
	public int getGripTime(){
		return gripTime;
	}

	public void setGripTime(int gripTime){
		this.gripTime = gripTime;
	}
	
	public void updateGripTime(){
		if(gripped)
			gripTime++;
	}
	
	public Joint getJoint(){
		return joint; 
	}
	
	public void setJoint(Joint j){
		joint = j; 
	}
	/** Contructs an ExtremityModel
	 * 
	 * @param x the x position of this extremity
	 * @param y the y position of this extremity
	 * @param texture	the texture of this extremity, film strip with second
	 *                     frame gripped
	 * @param drawSizeScale the scaling between object size and drawn size
	 * @param drawPositionScale the scaling between box2d coordinates and world coordinates
	 * @param character the character this is a part of
	 */
	public ExtremityModel(float x, float y, FilmStrip texture, float drawSizeScale,
						  Vector2 drawPositionScale, CharacterModel character){
		super(x, y, texture, drawSizeScale, drawPositionScale, character);
		gripped = false;
	}
}
