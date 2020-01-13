package jeromeTestPlayer;

import battlecode.common.*;

//TODO: new struct - no inf loop on run()
// for every runType():
//      initType()
//      while (true) updateType();

public strictfp class RobotPlayer {
    static RobotController rc;

    //TODO: Is modifying static vars against the rules?

    // Tracks locations of both own hq and enemy hq
    static MapLocation myHQ;
    static int[] enemyHQ = new int[]{-1, -1};
    static Direction mySide;
    static int directionInt;

    static int dronesBuilt = 0;

    static Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")


    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        jeromeTestPlayer.RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");

        System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());


        try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                 switch (rc.getType()) {
                    case HQ:
                        runHQ();
                       
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:    runDeliveryDrone(); break;
                    case NET_GUN:            runNetGun();            break;
                }

                System.out.println(Clock.getBytecodeNum());

            }
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                
        catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }

        System.out.println("ERROR: It was a good run. *BOOM* ");
    }

    static void initHQ() throws GameActionException {
        myHQ = rc.getLocation();
        if(myHQ.x < rc.getMapWidth()/2) {
            mySide = Direction.WEST;
            directionInt = 3;
        } else {
            mySide = Direction.EAST;
            directionInt = 1;
        }

        rc.submitTransaction(new int[] {myHQ.x, myHQ.y, 11211999, 5963, directionInt}, 8);
        System.out.println(Clock.getBytecodeNum());

    }



    static void runHQ() throws GameActionException {
        initHQ();
        System.out.println(Clock.getBytecodeNum());

        while (true) {
            turnCount++;
            updateHQ();
        }
        
    }

    static void updateHQ() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.MINER, dir);
    }

    static void runMiner() throws GameActionException {
        tryBlockchain();
        tryMove(randomDirection());
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dir : directions)
            tryBuild(RobotType.FULFILLMENT_CENTER, dir);
        for (Direction dir : directions)
            if (tryRefine(dir))
                System.out.println("I refined soup! " + rc.getTeamSoup());
        for (Direction dir : directions)
            if (tryMine(dir))
                System.out.println("I mined soup! " + rc.getSoupCarrying());
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {

    }

    /*

    Assuming fulfillment center (1) has enough soup to build a drone and (2) has built < 12 drones,
    Looks to build a drone either northwest or southeast of itself whenever possible.

    Updates dronesBuilt count accordingly

    */


    static void runFulfillmentCenter() throws GameActionException {

        // Which corner F is on (usu. SW or NE)
        Direction cornerLoc = initFulfillmentCenter();

        while (true) {
            turnCount++;

            updateFulfillmentCenter(cornerLoc);
            Clock.getBytecodeNum();
            Clock.yield();
        }

        

    }

    static Direction initFulfillmentCenter() throws GameActionException{
        readInitialMessage();

        MapLocation pos = rc.getLocation();

        // if myHQ is right of F, then F is probably on SW corner - build SW
        if (myHQ.x - pos.x > 0)
            return Direction.SOUTHWEST;

        // if myHQ is left of F, then F is probably on NE corner - build NE
        return Direction.NORTHEAST;
    }

     static void updateFulfillmentCenter(Direction cornerLoc) throws GameActionException {
        int droneLimit = 12;
        boolean maxDrones = dronesBuilt < droneLimit;
        boolean enoughSoup = rc.getTeamSoup() >= RobotType.DELIVERY_DRONE.cost;

        if (enoughSoup && !maxDrones)
            if (tryBuild(RobotType.DELIVERY_DRONE, cornerLoc))
                dronesBuilt++;
        }

    static void runLandscaper() throws GameActionException {

    }

    static void runDeliveryDrone() throws GameActionException {
        Direction dir = initDeliveryDrone();
        MapLocation pos;

        // whether or not the robot couldn't move last turn, true by default
        boolean wasStuck = true;
        boolean onPath = true;

        while (true) {
            turnCount++;

            pos = rc.getLocation();

            if (onPath)
                wasStuck = patrol(pos, dir, wasStuck);

            else if (!onPath)
                returnToPath(pos);

            Clock.getBytecodeNum();
            Clock.yield();
        }


/*


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
        } */
    }

    static Direction initDeliveryDrone() throws GameActionException{
        readInitialMessage();

        MapLocation pos = rc.getLocation();

        // If myHQ is to right of drone, prob on NW corner - should go up
        if (myHQ.x > pos.x)
            return Direction.NORTH;

        // If myHQ is to left of drone, prob on SE corner - should go down
        return Direction.SOUTH;
    }

    // updates delivery drone, and tells drone whether it has moved this turn
    // true = has moved this turn, false = has not moved (stuck in spot)
    static boolean patrol(MapLocation pos, Direction dir, boolean wasStuck) throws GameActionException {
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

    static void returnToPath(MapLocation pos) throws GameActionException {
        Direction newDir = pos.directionTo(myHQ);

        if (!tryMove(newDir) && tryMove(newDir.rotateRight()) && tryMove(newDir.rotateLeft()))
             System.out.println("returnToPath(): I'm stuck!");
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


    static void tryBlockchain() throws GameActionException {
        if (turnCount < 3) {
            int[] message = new int[7];
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        }
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }
/*

    static void runMiner() throws GameActionException {
        while(true) {
            turnCount++;
            System.out.println(turnCount);
            if(rc.getRoundNum() > 1) {
                readInitialMessage();
            }
            if(rc.isReady()) {
                tryBuild(RobotType.VAPORATOR,Direction.NORTH);
            }
            Clock.yield();
        }
    }
*/
        static void readInitialMessage() throws GameActionException {
        int round = 1;
        Transaction[] roundBlock;

        while(round < rc.getRoundNum()) {
            roundBlock = rc.getBlock(round);

            for(Transaction t : roundBlock) {

                if(t.getMessage().length == 5 && t.getMessage()[2] == 11211999) {

                    //TODO: Is this necessary?
                    for (int i = 0; i < t.getMessage().length; i++) {
                        myHQ = new MapLocation(t.getMessage()[0], t.getMessage()[1]);
                        mySide = directions[t.getMessage()[4]];
                    }

                    // TODO: Should this be outside the loop?
                    round += rc.getRoundNum();
                }
            }
            round++;
        }
    }
}
