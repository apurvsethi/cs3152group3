
package beigegang.mountsputnik;

import beigegang.util.JsonAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.FileWriter;

import static beigegang.mountsputnik.Constants.*;

public class GameMode extends GamingMode {
	@Override
	public void changeLevel(int level){
		listener.exitScreen(this, EXIT_GAME_NEXT_LEVEL);
	}

	public GameMode() {
		super(GAME_MODE);
	}
	public void makeJsonForAnimation(){
		try{

			animationToFile = new FileWriter(("tutorialAnimations/animationsW.json"));
		}catch(Exception e){System.out.println("OHNO" + e );}
	}
	public void writeNextStepJsonForAnimation(float lx, float ly, float rx, float ry, Array<Integer> pressed){
		String e = "";
		for (counterInt = 0; counterInt <pressed.size; counterInt++){
			e = e + " " + pressed.get(counterInt) + ",";
		}
		String s = "\"" + animationTimestep + "\":[" + lx + "," + ly + "," + rx + "," + ry + ",[" + e + "],";
		input = InputController.getInstance(0);
		String released = "[";
		released += (input.releasedLeftArm()) ? String.valueOf(HAND_LEFT) + ", " :"";
		released += (input.releasedRightArm()) ? String.valueOf(HAND_RIGHT) + ", ":"";
		released += (input.releasedLeftLeg()) ? String.valueOf(FOOT_LEFT) + ", ":"";;
		released += (input.releasedRightLeg()) ? String.valueOf(FOOT_RIGHT) + ", ":"";;

		released += "]]";

		fullJson += s;
		fullJson += released;
		fullJson += ",";
		animationTimestep ++;
	}
	public void writeJsonToFile(){
		fullJson += "}";
		if (!writtenToFile){
			try{
				animationToFile.write(fullJson);

			}catch(Exception e){System.out.println("here" + e); }
			try{animationToFile.flush();
			}catch(Exception e){System.out.println("two" + e);}
		}
	}


	public void setAnimationReader(){
		writtenToFile = true;
		jsonReader = new JsonReader();
		animationFormat = jsonReader.parse(Gdx.files.internal("tutorialAnimations/animationsW.json"));
		JsonAssetManager.getInstance().loadDirectory(levelFormat);
		JsonAssetManager.getInstance().allocateDirectory();
	}
	public void getAnimationInformation(){
		if (animationFormat == null) setAnimationReader();
		//0 will be animationTimestep in the future.
		JsonValue timestepInfo = animationFormat.get(animationTimestep);
		animationLX = timestepInfo.get(0).asFloat();
		animationLY = timestepInfo.get(1).asFloat();
		animationRX = timestepInfo.get(2).asFloat();
		animationRY = timestepInfo.get(3).asFloat();

		animationNextToPress.clear();
		animationJustReleased.clear();

		ints = timestepInfo.get(4).asIntArray();
		for (counterInt = 0;counterInt<ints.length; counterInt++){
			animationNextToPress.add(ints[counterInt]);
		}

		ints = timestepInfo.get(5).asIntArray();
		for (counterInt = 0;counterInt<ints.length; counterInt++){
			animationJustReleased.add(ints[counterInt]);
		}

		animationTimestep++;

	}

	@Override
	public void reset() {
		if (timestep != 0)		writeJsonToFile();
		resetAllButCheckpoints();
		checkpointLevelBlocks.clear();
		checkpointLevelJsons.clear();
		checkpoints.clear();
		lastReachedCheckpoint = 0;
		populateLevel();


	}
	public void restartLastCheckpoint(){
		resetAllButCheckpoints();
		populateLevelAtLastCheckpoint();

	}


	public void populateLevelAtLastCheckpoint() {
		readLevelStats();
		int counter = 0;
		used.clear();
		maxHandhold = remainingHeight;
		maxLevelHeight = remainingHeight;
		while(counter < checkpointLevelBlocks.size){
			//TODO: account for difficulty
			int blockNumber = checkpointLevelBlocks.get(counter);
			//			blockNumber = 11;
			used.add(blockNumber);
			levelBlocks.add("Levels/"+levelName+"/block"+blockNumber+".json");
			JsonValue levelPiece = jsonReader.parse(Gdx.files.internal("Levels/"+levelName+"/block"+blockNumber+".json"));
			addChunk(levelPiece, currentHeight, levelName);
			currentHeight += levelPiece.getFloat("size");
			if(!levelName.equals("volcano")) checkpoints.add(currentHeight);
			//filler stuff not currently used.
			//			for(counterInt = 0; counterInt < filler; counterInt++){
			//				blockNumber = ((int) (Math.random() * fillerSize)) + 1;
			//				levelPiece = jsonReader.parse(Gdx.files.internal("Levels/general/block"+blockNumber+".json"));
			//				levelBlocks.add("Levels/general/block"+blockNumber+".json");
			//				addChunk(levelPiece, currentHeight, levelName);
			//				currentHeight += levelPiece.getInt("size");
			//			}
			counter ++;
		}
		System.out.println(levelBlocks);



		character1 = new CharacterModel(partTextures, world, DEFAULT_WIDTH / 2, Math.max(DEFAULT_HEIGHT/2, checkpoints.get(lastReachedCheckpoint)), scale);
		addCharacterToGame(character1);

		movementController1 = new PositionMovementController(character1, scale);
		makeHandholdsToGripAtStart(character1);

	}



	@Override
	protected void checkHasCompleted(CharacterModel c){
		this.complete =  c.parts.get(HAND_RIGHT).getPosition().y >= levelFormat.getFloat("height")
				||c.parts.get(HAND_LEFT).getPosition().y >= levelFormat.getFloat("height")
				||c.parts.get(FOOT_RIGHT).getPosition().y >= levelFormat.getFloat("height")
				||c.parts.get(FOOT_LEFT).getPosition().y >= levelFormat.getFloat("height");
	}
}


