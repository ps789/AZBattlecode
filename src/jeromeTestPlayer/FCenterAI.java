package jeromeTestPlayer;
import battlecode.common.*;

public class FCenterAI extends RobotPlayer {
	final int DRONE_LIMIT = 10;
	private int dronesBuilt = 0;

	private Direction cornerLoc;
	
	public FCenterAI() throws GameActionException {
		readInitialMessage();

		if (findMyHQ().x > getPos().x) 
			cornerLoc = Direction.SOUTHWEST;

		else
			cornerLoc = Direction.NORTHEAST;
	}

	public void run() throws GameActionException {
		 while (true) {

            updateTurnCount();

            update(cornerLoc);

            Clock.getBytecodeNum();
            Clock.yield();
        }
	}

	private void update(Direction cornerLoc) throws GameActionException {
   
        boolean maxDrones = dronesBuilt < DRONE_LIMIT;
        boolean enoughSoup = rc.getTeamSoup() >= RobotType.DELIVERY_DRONE.cost;

        if (enoughSoup && !maxDrones)
            if (tryBuild(RobotType.DELIVERY_DRONE, cornerLoc))
                dronesBuilt++;
    }



}