package phillipTestPlayer;
import battlecode.common.*;
import org.omg.CORBA.MARSHAL;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {
        Direction.NORTHWEST,
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;

    static MapLocation myHQ;
    static Direction mySide;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController paramRC) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = paramRC;
        turnCount = 0;
        System.out.println("I'm a " + rc.getType() + " and I just got created!");

        System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
        switch (rc.getType()) {
            case HQ:                 runHQ();                break;
            case MINER:              runMiner();             break;
            case REFINERY:           runRefinery();          break;
            case VAPORATOR:          runVaporator();         break;
            case DESIGN_SCHOOL:      runDesignSchool();      break;
            case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
            case LANDSCAPER:         runLandscaper();        break;
            case DELIVERY_DRONE:     runDeliveryDrone();     break;
            case NET_GUN:            runNetGun();            break;
        }

    }
    static Direction getLeft(Direction current) {
    	switch(current) {
    	case NORTH: return Direction.NORTHWEST;
    	case NORTHWEST: return Direction.WEST;
    	case WEST: return Direction.SOUTHWEST;
    	case SOUTHWEST: return Direction.SOUTH;
    	case SOUTH: return Direction.SOUTHEAST;
    	case SOUTHEAST: return Direction.EAST;
    	case EAST: return Direction.NORTHEAST;
    	default: return Direction.NORTH;
    	}
    }
    static Direction getRight(Direction current) {
    	switch(current) {
    	case NORTH: return Direction.NORTHEAST;
    	case NORTHWEST: return Direction.NORTH;
    	case WEST: return Direction.NORTHWEST;
    	case SOUTHWEST: return Direction.WEST;
    	case SOUTH: return Direction.SOUTHWEST;
    	case SOUTHEAST: return Direction.SOUTH;
    	case EAST: return Direction.SOUTHEAST;
    	default: return Direction.EAST;
    	}
    }
    static void runHQ() throws GameActionException {
        myHQ = rc.getLocation();
        int directionInt;
        if(myHQ.x < rc.getMapWidth()/2) {
            mySide = Direction.WEST;
            directionInt = 3;
        } else {
            mySide = Direction.EAST;
            directionInt = 1;
        }

        rc.submitTransaction(new int[] {myHQ.x, myHQ.y, 11211999, 5963, directionInt}, 8);
        System.out.println(Clock.getBytecodeNum());
        while(true) {
            turnCount++;

            if(rc.isReady() && turnCount < 100) {
                tryBuild(RobotType.MINER, mySide.opposite());
            }
            Clock.yield();
            
        }
    }
    static Direction bugMoveMine(MapLocation targetLocation, Direction bugDirection) throws GameActionException{
    	if(rc.getLocation().isAdjacentTo(targetLocation)) {
    		tryMine(rc.getLocation().directionTo(targetLocation));
    		return null;
    	}else {
    		if(bugDirection == null) {
    			if(rc.canMove(rc.getLocation().directionTo(targetLocation)) && !rc.senseFlooding(rc.adjacentLocation(rc.getLocation().directionTo(targetLocation)))) {
    				tryMove(rc.getLocation().directionTo(targetLocation));
    				return null;
    			}else {
    				Direction currDir = rc.getLocation().directionTo(targetLocation);
    				while(!rc.canMove(currDir) || (rc.canSenseLocation(rc.adjacentLocation(currDir)) && rc.senseFlooding(rc.adjacentLocation(currDir)))) {
    					currDir = getLeft(currDir);
    				}
    				tryMove(currDir);
    				return currDir;
    			}
    		}
    		if(rc.canMove(getRight(getRight(bugDirection))) && rc.canSenseLocation(rc.adjacentLocation(getRight(getRight(bugDirection)))) && !rc.senseFlooding(rc.adjacentLocation(getRight(getRight(bugDirection))))){
    			tryMove(rc.getLocation().directionTo(targetLocation));
    			return null;
    		}
    		if(rc.getLocation().directionTo(targetLocation) == getLeft(bugDirection) && rc.canMove(getLeft(bugDirection))){
    			tryMove(getLeft(bugDirection));
    			return null;
    		}
    		if(rc.getLocation().directionTo(targetLocation) == bugDirection && rc.canMove(bugDirection)){
    			tryMove(bugDirection);
    			return null;
    		}
    		if(rc.canMove(getRight(bugDirection)) && rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && !rc.senseFlooding(rc.adjacentLocation(bugDirection))) {
    			bugDirection = getRight(bugDirection);
    			if(rc.canMove(getRight(bugDirection)) && rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && !rc.senseFlooding(rc.adjacentLocation(bugDirection))) {
        			tryMove(getRight(bugDirection));
        			if(getRight(bugDirection).equals(rc.getLocation().directionTo(targetLocation))) {
        				return null;
        			}
        			return getRight(bugDirection);
        		}
    			if(getRight(bugDirection).equals(rc.getLocation().directionTo(targetLocation))) {
    				return null;
    			}
    			return bugDirection;
    		}else {
    			while(!rc.canMove(bugDirection) || (rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && rc.senseFlooding(rc.adjacentLocation(bugDirection)))) {
					bugDirection = getLeft(bugDirection);
				}
    			tryMove(bugDirection);
    			return bugDirection;
    		}
    	}
    }
    static Direction bugMoveReturn(Direction bugDirection) throws GameActionException{
    	if(rc.getLocation().isAdjacentTo(myHQ)) {
    		if(rc.isReady()) {
        		rc.depositSoup(rc.getLocation().directionTo(myHQ), rc.getSoupCarrying());
        	}
    		return null;
    	}else {
    		if(bugDirection == null) {
    			if(rc.canMove(rc.getLocation().directionTo(myHQ)) && !rc.senseFlooding(rc.adjacentLocation(rc.getLocation().directionTo(myHQ)))) {
    				tryMove(rc.getLocation().directionTo(myHQ));
    				return null;
    			}else {
    				Direction currDir = rc.getLocation().directionTo(myHQ);
    				while(!rc.canMove(currDir) || (rc.canSenseLocation(rc.adjacentLocation(currDir)) && rc.senseFlooding(rc.adjacentLocation(currDir)))) {
    					currDir = getLeft(currDir);
    				}
    				tryMove(currDir);
    				return currDir;
    			}
    		}
    		if(rc.canMove(getRight(getRight(bugDirection))) && rc.canSenseLocation(rc.adjacentLocation(getRight(getRight(bugDirection)))) && !rc.senseFlooding(rc.adjacentLocation(getRight(getRight(bugDirection))))){
    			tryMove(rc.getLocation().directionTo(myHQ));
    			return null;
    		}
    		if(rc.getLocation().directionTo(myHQ) == getLeft(bugDirection) && rc.canMove(getLeft(bugDirection))){
    			tryMove(getLeft(bugDirection));
    			return null;
    		}
    		if(rc.getLocation().directionTo(myHQ) == bugDirection && rc.canMove(bugDirection)){
    			tryMove(bugDirection);
    			return null;
    		}
    		if(rc.canMove(getRight(bugDirection)) && rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && !rc.senseFlooding(rc.adjacentLocation(bugDirection))) {
    			bugDirection = getRight(bugDirection);
    			if(rc.canMove(getRight(bugDirection)) && rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && !rc.senseFlooding(rc.adjacentLocation(bugDirection))) {
        			tryMove(getRight(bugDirection));
        			if(getRight(bugDirection).equals(rc.getLocation().directionTo(myHQ))) {
        				return null;
        			}
        			return getRight(bugDirection);
        		}
    			if(getRight(bugDirection).equals(rc.getLocation().directionTo(myHQ))) {
    				return null;
    			}
    			return bugDirection;
    		}else {
    			while(!rc.canMove(bugDirection) || (rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && rc.senseFlooding(rc.adjacentLocation(bugDirection)))) {
					bugDirection = getLeft(bugDirection);
				}
    			tryMove(bugDirection);
    			return bugDirection;
    		}
    	}
    }
    static void runMiner() throws GameActionException {
    	myHQ = rc.adjacentLocation(Direction.WEST);
        MapLocation targetLocation = rc.getLocation();
        Direction setDirection = directions[(int)(Math.random()*8)];
        boolean foundSoup = false;
        Direction bugDirection = null;
        Direction bugDirection2 = null;
        while(true) {
        	if(rc.isReady()) {
	            turnCount++;
	            //If full return to base
//	            if(rc.getSoupCarrying() > 0 && rc.getLocation().isAdjacentTo(myHQ)) {
//	            	bugDirection2 = bugMoveReturn(bugDirection2);
//	            }
	            if(rc.getSoupCarrying()>=RobotType.MINER.soupLimit) {
	            	bugDirection2 = bugMoveReturn(bugDirection2);
	            }else {
		            if(!foundSoup || (foundSoup && rc.canSenseLocation(targetLocation) && rc.senseSoup(targetLocation)<=0)) {
		            	foundSoup = false;
		            	for(int i = -5; i<6; i++) {
		            		for(int j = -5; j < 6; j++) {
		            			if(rc.canSenseLocation(rc.getLocation().translate(i,  j))) {
			            			if(rc.senseSoup(rc.getLocation().translate(i,  j))>0){
			            				foundSoup = true;
			            				targetLocation = rc.getLocation().translate(i, j);
			            			}
		            			}
		            		}
		            	}
		            }
		            if(foundSoup) {
		            	bugDirection = bugMoveMine(targetLocation, bugDirection);
		            }else {
		            	if (turnCount%10==0) {
		            		setDirection = directions[(int)(Math.random()*8)];
		            	}
		        		while((rc.canSenseLocation(rc.adjacentLocation(setDirection)) && rc.senseFlooding(rc.adjacentLocation(setDirection))) || !rc.canMove(setDirection))
		        			setDirection = directions[(int)(Math.random()*8)];
		        		tryMove(setDirection);
		            }
	            }
	            
	//            if(rc.getRoundNum() > 1) {
	//                readInitialMessage();
	//            }
	//            if(rc.isReady()) {
	//                tryBuild(RobotType.VAPORATOR,Direction.NORTH);
	//            }
	            Clock.yield();
        	}
        }
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {
        while (true) {
            Clock.yield();
        }
    }

    static void runDesignSchool() throws GameActionException {

    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {

    }

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection());
        }
    }

    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }

    static void readInitialMessage() throws GameActionException {
        int round = 1;
        Transaction[] roundBlock;

        while(round < rc.getRoundNum()) {
            roundBlock = rc.getBlock(round);
            for(Transaction t : roundBlock) {
                if(t.getMessage().length == 5 && t.getMessage()[2] == 11211999) {
                    for (int i = 0; i < t.getMessage().length; i++) {
                        myHQ = new MapLocation(t.getMessage()[0], t.getMessage()[1]);
                        mySide = directions[t.getMessage()[4]];
                    }
                    round += rc.getRoundNum();
                }
            }
            round++;
        }
    }
}
