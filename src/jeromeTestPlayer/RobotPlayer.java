package jeromeTestPlayer;

import battlecode.common.*;

//TODO: new struct - no inf loop on run()
// for every runType():
//      initType()
//      while (true) updateType();

public strictfp class RobotPlayer {
    static RobotController rc;

    static MapLocation myHQ;
    static MapLocation pos;
    static Direction mySide;

    static int directionInt;

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

    static int turnCount = 0;

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
                        MyHQAI hq = new myHQAI();
                        hq.run();
                        break;

                       
                    case MINER:
                        MinerAI miner = new MinerAI();
                        miner.run();
                        break;


                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    
                    case FULFILLMENT_CENTER:

                        FCenterAI fCenter = new FCenterAI();
                        fCenter.run();
                        break;


                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:
                        DroneAI drone = new DroneAI();
                        drone.run();
                        break;

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
    public void sendInitialMessage() throws GameActionException {
        pos = rc.getLocation();

        if (pos.x < rc.getMapWidth()/2) {
            mySide = Direction.WEST;
            directionInt = 3;
        }

        else {
            mySide = Direction.EAST;
            directionInt = 1;
        }

        rc.submitTransaction(new int[] {pos.x, pos.y, 11211999, 5963, directionInt}, 8);
        System.out.println(Clock.getBytecodeNum());
    }

    public void readInitialMessage() throws GameActionException {
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


    // Getters and updaters - allow subclasses to interact w/ parent class's variables
    public MapLocation getPos() throws GameActionException {
        return pos;
    }

    // Updates pos to current position

    public void updatePos() throws GameActionException {
        pos = rc.getLocation();
    }

    // Returns location of myHQ
    public MapLocation findMyHQ() throws GameActionException {
        return myHQ;
    }

    public int getTurnCount() throws GameActionException {
        return turnCount;
    }    

    public void updateTurnCount() throws GameActionException {
        turnCount++;
    }

    public Direction getMySide() throws GameActionException {
        return mySide;
    }

    public int getDirectionInt() throws GameActionException {
        return directionInt;
    }

}
