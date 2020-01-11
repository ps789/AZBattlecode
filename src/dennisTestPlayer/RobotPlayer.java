package dennisTestPlayer;
import battlecode.common.*;
import org.omg.CORBA.MARSHAL;

import java.awt.*;
import java.security.DigestException;
import java.util.ArrayList;
import java.util.Map;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {
            Direction.NORTHWEST,
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.WEST,
            Direction.WEST,
            Direction.EAST,
            Direction.SOUTHWEST,
            Direction.SOUTH,
            Direction.SOUTHEAST,
    };
    static Direction[] directions2 = {
            Direction.NORTHWEST,
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.WEST,
            //Direction.WEST,
            Direction.EAST,
            Direction.SOUTHWEST,
            Direction.SOUTH,
            Direction.SOUTHEAST,
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;

    // the location of our hq
    static MapLocation myHQ;

    // the side of the map (EAST/WEST) that we are playing from
    static Direction mySide = Direction.CENTER;

    // the side of the hq a defensive landscaper is on
    static Direction myPosition = Direction.CENTER;

    // soup hotspot, if found
    static MapLocation soupSpot;

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
            if(rc.isReady() && turnCount < 4) {
                tryBuild(RobotType.MINER, mySide.opposite());
                turnCount++;
            }

            if(rc.isReady()) {
                RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                for(RobotInfo ri : enemies) {
                    if (ri.getLocation().distanceSquaredTo(myHQ) < 16)
                        if (ri.getType() == RobotType.DELIVERY_DRONE)
                            rc.shootUnit(ri.getID());
                }
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
                        pathTowards(mySide.opposite());
                        turnCount++;
                        break;
                    case 2:
                        if(rc.getTeamSoup() > 150) {
                            tryBuild(RobotType.DESIGN_SCHOOL, mySide.opposite());
                            turnCount++;
                        }
                        break;
                    default:
                        minerMove();
                        break;
                }

                System.out.println(turnCount);
            }
            Clock.yield();
        }
    }

    static void minerMove() throws GameActionException {
        //Graph newGraph = new Graph();
        while (true) {
            //If full return to base

            if(rc.isReady()) {
                if(rc.getSoupCarrying() > 0 && rc.getLocation().isAdjacentTo(myHQ)) {
                    rc.depositSoup(rc.getLocation().directionTo(myHQ), rc.getSoupCarrying());
                    turnCount++;
                } else if (rc.getSoupCarrying() >= RobotType.MINER.soupLimit) {
                    pathTowards(myHQ);
                } else {
                    pathTowards(soupSpot);
                }

            } else {
                Clock.yield();
            }
            if (rc.getSoupCarrying() > 0 && rc.getLocation().isAdjacentTo(myHQ)) {
                if (rc.isReady()) {
                    rc.depositSoup(rc.getLocation().directionTo(myHQ), rc.getSoupCarrying());
                    turnCount++;
                }
            }

            if (rc.getSoupCarrying() >= RobotType.MINER.soupLimit) {
                pathTowards(myHQ);
            } else {
                boolean foundSoup = false;
                for (int i = 0; i < 8; i++) {
                    try {
                        if (rc.senseSoup(rc.adjacentLocation(Direction.NORTHEAST)) > 100) {
                            foundSoup = true;
                            tryMine(directions2[i]);
                        }
                    } catch (GameActionException ex) {
                        continue;
                    }
                }
                if (!foundSoup)
                    pathTowards(randomDirection());
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
        while(mySide == Direction.CENTER) {
            readInitialMessage();
        }
        while(true) {
            if(rc.getTeamSoup() > 150 && rc.isReady()) {

                switch (turnCount) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                    case 9: if(tryBuild(RobotType.LANDSCAPER, mySide))
                        System.out.println("defensive landscaper created");
                        turnCount++; break;
                    default: if(tryBuild(RobotType.LANDSCAPER, mySide.opposite()))
                        System.out.println("offensive landscaper created");
                        turnCount++; break;
                }

                System.out.println("Design school has made landscaper " + turnCount);
            }
            Clock.yield();
        }
    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {
        while(mySide == Direction.CENTER) {
            if(rc.getRoundNum() > 1) {
                readInitialMessage();
            }
        }

        // defensive landscapers
        if(findCreator() != mySide) {
            System.out.println("I am on defense!");
            while (rc.getLocation().distanceSquaredTo(myHQ) > 3) {
                System.out.println("I want to go home.");
                if (rc.isReady() && pathTowards(myHQ))
                    turnCount++;
            }

            MapLocation destination = myHQ.add(mySide);
            while (destination != rc.getLocation()) {
                System.out.println("I am at " + rc.getLocation().x + "," + rc.getLocation().y);
                System.out.println("I want to go to " + destination.x + "," + destination.y);
                // check if location is occupied by a landscaper
                RobotInfo rf = rc.senseRobotAtLocation(destination);
                if (rf != null) {
                    if (rf.type == RobotType.LANDSCAPER && rf.getTeam() == rc.getTeam() && rf.getID() != rc.getID())
                        destination = myHQ.add(myHQ.directionTo(destination).rotateLeft());
                }
                if (rc.isReady() && pathTowards(destination))
                    turnCount++;
            }

            myPosition = myHQ.directionTo(rc.getLocation());
            while(true) {
                if (rc.isReady()) {
                    if (rc.getDirtCarrying() < RobotType.LANDSCAPER.dirtLimit) {
                        System.out.println("Collecting Dirt");
                        tryDig();
                    }
                    else
                        rc.depositDirt(Direction.CENTER);
                } else {
                    Clock.yield();
                }
            }

        // offensive landscapers
        } else {
            System.out.println("I am on offense!");
            // find enemy base and sit next to it
            while(true) {
                if(rc.isReady()) {
                    pathTowards(mySide.opposite());
                    System.out.println("ATTACK!");
                } else {
                    Clock.yield();
                }
            }
        }
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
            pathTowards(randomDirection());
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
        return directions2[(int) (Math.random() * directions2.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    public strictfp MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        ArrayList<MapLocation> returnLocations = new ArrayList();
        int ceiledRadius = (int)Math.ceil(Math.sqrt((double)radiusSquared)) + 1;
        int minX = Math.max(center.x - ceiledRadius, 0);
        int minY = Math.max(center.y - ceiledRadius, 0);
        int maxX = Math.min(center.x + ceiledRadius, rc.getMapWidth() - 1);
        int maxY = Math.min(center.y + ceiledRadius, rc.getMapHeight() - 1);

        for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
                MapLocation newLocation = new MapLocation(x, y);
                if (center.isWithinDistanceSquared(newLocation, radiusSquared)) {
                    returnLocations.add(newLocation);
                }
            }
        }

        return (MapLocation[])returnLocations.toArray(new MapLocation[0]);
    }

    /**
     * Attempts to move in a given direction, and moves sideways if it can't.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean pathTowards(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        Direction[] tryDirections = {
                dir,
                dir.rotateLeft(),
                dir.rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight()
        };

        for(Direction tryDir: tryDirections) {
            if (rc.canSenseLocation(rc.adjacentLocation(tryDir))) {
                if (!rc.senseFlooding(rc.adjacentLocation(tryDir))) {
                    if (rc.canMove(tryDir)) {
                        rc.move(tryDir);
                        System.out.println(rc.getType() + " moved!");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Attempts to move towards a given location on the map.
     *
     * @param loc The intended target location of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean pathTowards(MapLocation loc) throws GameActionException {
        return pathTowards(rc.getLocation().directionTo(loc));
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
     * Attempts to dig dirt in predefined ditch locations
     * @throws GameActionException
     */
    static void tryDig() throws GameActionException {
        switch (myPosition) {
            case CENTER: break;
            case NORTHWEST:
            case NORTH:
            case NORTHEAST:
                if (rc.canDigDirt(rc.getLocation().directionTo(
                        myHQ.add(Direction.NORTH).add(Direction.NORTH))))
                    rc.digDirt(rc.getLocation().directionTo(
                            myHQ.add(Direction.NORTH).add(Direction.NORTH)));
                break;
            case EAST:
                if (rc.canDigDirt(rc.getLocation().directionTo(
                        myHQ.add(Direction.EAST).add(Direction.EAST))))
                    rc.digDirt(rc.getLocation().directionTo(
                            myHQ.add(Direction.EAST).add(Direction.EAST)));
                break;
            case WEST:
                if (rc.canDigDirt(rc.getLocation().directionTo(
                        myHQ.add(Direction.WEST).add(Direction.WEST))))
                    rc.digDirt(rc.getLocation().directionTo(
                            myHQ.add(Direction.WEST).add(Direction.WEST)));
                break;
            case SOUTHWEST:
            case SOUTH:
            case SOUTHEAST:
                if (rc.canDigDirt(rc.getLocation().directionTo(
                        myHQ.add(Direction.SOUTH).add(Direction.SOUTH))))
                    rc.digDirt(rc.getLocation().directionTo(
                            myHQ.add(Direction.SOUTH).add(Direction.SOUTH)));
                break;
        }
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

    /**
     * for the HQ to send its coords at the start of the game
     * @throws GameActionException
     */
    static void sendInitialMessage() throws GameActionException {
        int directionInt;
        if(myHQ.x < rc.getMapWidth()/2) {
            mySide = Direction.WEST;
            directionInt = 3;
        } else {
            mySide = Direction.EAST;
            directionInt = 4;
        }

        rc.submitTransaction(new int[] {(698 + myHQ.x), 15, (myHQ.y + 420), 510, directionInt, (soupSpot.x - 610), (soupSpot.y + 444)}, 2);
    }

    /**
     * for robots to read the HQ's location and side of play at creation
     * @throws GameActionException
     */
    static void readInitialMessage() throws GameActionException {
        int round = 6;
        Transaction[] roundBlock;

        while(round < rc.getRoundNum()) {
            roundBlock = rc.getBlock(round);
            for(Transaction t : roundBlock) {
                if( t.getCost()             == 2 &&
                    t.getMessage()[1]       == 15 &&
                    t.getMessage()[3]       == 510) {
                    System.out.println("password found by: " + rc.getType());
                    for (int i = 0; i < t.getMessage().length; i++) {
                        myHQ = new MapLocation(t.getMessage()[0] - 698, t.getMessage()[2] - 420);
                        mySide = directions2[t.getMessage()[4]];
                    }
                    System.out.println("HQ: " + myHQ.x + "," + myHQ.y + " / " + t.getMessage()[4]);
                }

                round += rc.getRoundNum();
            }
            round++;
        }
    }

    /**
     * Gets the Direction of the unit's creator.
     * Should only be called by units (landscapers, miners, drones)
     * @return  Direction of the unit's creator
     */
    static Direction findCreator() throws GameActionException {
        RobotType creator;
        Direction creatorDir = Direction.CENTER;
        switch (rc.getType()) {
            case LANDSCAPER:        creator = RobotType.DESIGN_SCHOOL;      break;
            case DELIVERY_DRONE:    creator = RobotType.FULFILLMENT_CENTER; break;
            default:                creator = RobotType.HQ;                 break;
        }

        RobotInfo[] adjacentBots = rc.senseNearbyRobots(3,rc.getTeam());
        for(RobotInfo ri : adjacentBots) {
            if (ri.getType() == creator)
                creatorDir = rc.getLocation().directionTo(ri.getLocation());
        }

        return creatorDir;
    }
}
