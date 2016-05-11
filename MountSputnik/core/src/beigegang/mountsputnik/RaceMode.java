package beigegang.mountsputnik;

import static beigegang.mountsputnik.Constants.RACE_MODE;

public class RaceMode extends GamingMode {

    public RaceMode() {
        super(RACE_MODE);
    }
    @Override
    public void reset() {
        resetAllButCheckpoints();
        checkpointLevelBlocks.clear();
        checkpointLevelJsons.clear();
        checkpoints.clear();
        lastReachedCheckpoint = 0;
        populateLevel();


    }




}


