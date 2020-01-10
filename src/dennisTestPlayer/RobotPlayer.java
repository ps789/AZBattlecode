package dennisTestPlayer;
import battlecode.common.*;
import org.omg.CORBA.MARSHAL;

import java.awt.*;
import java.util.Map;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST,
        Direction.SOUTHEAST,
        Direction.SOUTHWEST,
        Direction.NORTHEAST,
        Direction.NORTHWEST
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;

    static MapLocation myHQ;
    static Direction mySide = Direction.CENTER;

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

    static void runHQ() throws GameActionException {
        myHQ = rc.getLocation();

        sendInitialMessage();
        System.out.println(Clock.getBytecodeNum());
        while(true) {
            if(rc.isReady() && turnCount == 0) {
                tryBuild(RobotType.MINER, mySide.opposite());
                turnCount++;
            }
            Clock.yield();
        }
    }

    static void runMiner() throws GameActionException {
        while(mySide == Direction.CENTER) {
            if(rc.getRoundNum() > 1) {
                readInitialMessage();
            }
        }

        if(mySide == Direction.EAST)
            System.out.println("EAST");
        else
            System.out.println("WEST");

        while(true) {
            if(rc.isReady()) {
                switch (turnCount) {
                    case 0:
                    case 1:
                        tryMove(mySide.opposite());
                        turnCount++;
                        break;
                    case 2:
                        if(rc.getTeamSoup() > 150) {
                            tryBuild(RobotType.DESIGN_SCHOOL, mySide.opposite());
                            turnCount++;
                        }
                        break;
                    default:
                        if(rc.canMove(Direction.NORTHEAST))
                            rc.move(Direction.NORTHEAST);
                            turnCount ++;
                        break;
                }

                System.out.println(turnCount);
            }
            Clock.yield();
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
        while(mySide == Direction.CENTER) {
            readInitialMessage();
        }

        while(true) {
            if(rc.getTeamSoup() > 100 && rc.isReady()) {
                switch (turnCount) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7: tryBuild(RobotType.LANDSCAPER, mySide); break;
                    default: tryBuild(RobotType.LANDSCAPER, mySide.opposite()); break;
                }

                turnCount++;
                System.out.println(turnCount);
            }
            Clock.yield();
        }
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

    static void sendInitialMessage() throws GameActionException {
        int directionInt;
        if(myHQ.x < rc.getMapWidth()/2) {
            mySide = Direction.WEST;
            directionInt = 3;
        } else {
            mySide = Direction.EAST;
            directionInt = 1;
        }

        rc.submitTransaction(new int[] {(698 + (2 * myHQ.x)), 15, (myHQ.y + 420), 5963, directionInt}, 4);
    }

    static void readInitialMessage() throws GameActionException {
        int round = 1;
        Transaction[] roundBlock;

        while(round < rc.getRoundNum()) {
            roundBlock = rc.getBlock(round);
            for(Transaction t : roundBlock) {
                if( t.getCost()             == 4 &&
                    t.getMessage().length   == 5 &&
                    t.getMessage()[3]       == 5963) {
                    System.out.println("password found by: " + rc.getType());
                    for (int i = 0; i < t.getMessage().length; i++) {
                        myHQ = new MapLocation((t.getMessage()[0] - 698) / 2, t.getMessage()[1] - 420);
                        mySide = directions[t.getMessage()[4]];
                    }
                    round += rc.getRoundNum();
                }
            }
            round++;
        }
    }

    /**
     * Gets the location of the unit's creator.
     * Should only be called by units (landscapers, miners, drones)
     */
    static Direction findCreator() throws GameActionException {
        RobotType creator;
        Direction creatorDir = Direction.CENTER;
        switch (rc.getType()) {
            case LANDSCAPER:        creator = RobotType.DESIGN_SCHOOL;      break;
            case DELIVERY_DRONE:    creator = RobotType.FULFILLMENT_CENTER; break;
            default:             creator = RobotType.HQ;                 break;
        }

        RobotInfo[] adjacentBots = rc.senseNearbyRobots(3,rc.getTeam());
        for(RobotInfo ri : adjacentBots) {

        }

        while (creatorDir == Direction.CENTER) {
            for(Direction dir : directions) {
                if (rc.canSenseLocation(rc.adjacentLocation(dir)))
                    if (rc.senseRobotAtLocation(rc.adjacentLocation(dir)) != null)
                        if (rc.senseRobotAtLocation(rc.adjacentLocation(dir)).getType() == creator)
                            creatorDir = dir;
            }
        }

        return creatorDir;
    }
}
