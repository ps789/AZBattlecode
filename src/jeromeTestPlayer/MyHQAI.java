package jeromeTestPlayer;
import battlecode.common.*;

public class MyHQAI extends RobotPlayer {
	private Direction initialSpawnDir = Direction.SOUTH;
	
	public MyHQAI() throws GameActionException {
		sendInitialMessage();
	}

	// Tries to spawn miners going clockwise from initialSpawnDir
	// After either spawning a miner or failing to in all 8 directions, sets initialSpawnDir to be
	// 45 deg right of last successful spawn
	public void run() throws GameActionException {
		Direction spawnDir = initialSpawnDir;

		while (true) {
			updateTurnCount();

			for (int i = 0; i < 8; i++) {
				if (tryBuild(RobotType.MINER, spawnDir))
					break;

				else
					spawnDir = spawnDir.rotateRight();

			}

			initialSpawnDir = spawnDir.rotateRight();

			Clock.getBytecodeNum();
			Clock.yield();

		}
	}


}