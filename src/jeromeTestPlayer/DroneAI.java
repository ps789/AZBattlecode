package jeromeTestPlayer;
import battlecode.common.*;

public class DroneAI extends RobotPlayer{
	Direction dir;

	boolean wasStuck = false;
	boolean onPath = true;

	public DroneAI() throws GameActionException {
		readInitialMessage();

		MapLocation myHQ = findMyHQ();
		MapLocation pos = getPos();

		if (myHQ.x > pos.x)
			dir = Direction.NORTH;

		else
			dir = Direction.SOUTH;
	}

	public void run() throws GameActionException {
		while (true) {
			updateTurnCount();
			updatePos();

			if (onPath)
				wasStuck = patrol();

			else if (!onPath) {
				returnToPath();
			}

			Clock.getBytecodeNum();
			Clock.yield();
		}
	}

	   // updates delivery drone, and tells drone whether it has moved this turn
    // true = has moved this turn, false = has not moved (stuck in spot)
   private boolean patrol() throws GameActionException {
    	MapLocation pos = getPos();
    	MapLocation myHQ = findMyHQ();

        boolean isAtCorner = pos.distanceSquaredTo(myHQ) == 18;
        Direction newDir = dir;

        // Don't try to turn drone if it was stuck on current location
        // If drone begins its turn at corner of its path, and it's its first turn on
        // the corner, turn it clockwise by 90 deg (rotateRight() twice)
        if (!wasStuck && isAtCorner)
            newDir = dir.rotateRight().rotateRight();

        if (tryMove(newDir))
            return false;

        return true;


    }


    // Attempts to move towards myHQ, first straight, then slightly angled right, then slightly angled left
    // If it can't move in any of these cases, cries

    private void returnToPath() throws GameActionException {
    	MapLocation pos = getPos();
    	MapLocation myHQ = findMyHQ();
    	
        Direction newDir = pos.directionTo(myHQ);

        if (!tryMove(newDir) && tryMove(newDir.rotateRight()) && tryMove(newDir.rotateLeft()))
             System.out.println("returnToPath(): I'm stuck!");
    }






}