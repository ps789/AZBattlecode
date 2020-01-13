package dennisTestPlayer;
import battlecode.common.*;

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

    // array of positions around the hq
    static MapLocation[] positions = new MapLocation[49];
    static int myPosition;
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
            case MINER:              {
                if(rc.getRoundNum()==2)
                    runMinerAttack();
                else if(rc.getRoundNum()==3){
                    runMinerAttack2();
                }else {
                    runMiner();
                }
            }
            case REFINERY:           runRefinery();          break;
            case VAPORATOR:          runVaporator();         break;
            case DESIGN_SCHOOL:      runDesignSchool();      break;
            case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
            case LANDSCAPER:         runLandscaper();        break;
            case DELIVERY_DRONE:     runDeliveryDrone();     break;
            case NET_GUN:            runNetGun();            break;
        }

    }
    static boolean shouldIBuildRefinery() throws GameActionException{
        int currentSoup = 0;
        for(int i = -5; i<6; i++) {
            for(int j = -5; j < 6; j++) {
                if(rc.canSenseLocation(rc.getLocation().translate(i,  j))) {
                    currentSoup += rc.senseSoup(rc.getLocation().translate(i,  j));
                }
            }
        }
        return (currentSoup >= 300 && (rc.getLocation().distanceSquaredTo(myHQ) > 25));
    }
    static MapLocation hasRefinery() throws GameActionException{
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(35, rc.getTeam());
        for(RobotInfo robot : nearbyRobots) {
            if(robot.getType().equals(RobotType.REFINERY)) {
                return robot.getLocation();
            }
        }
        return null;
    }
    static boolean hasDesignSchool() throws GameActionException{
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(35, rc.getTeam());
        for(RobotInfo robot : nearbyRobots) {
            if(robot.getType().equals(RobotType.DESIGN_SCHOOL)) {
                return true;
            }
        }
        return false;
    }
    static void runHQ() throws GameActionException {
        myHQ = rc.getLocation();
        sendInitialMessage();

        System.out.println(Clock.getBytecodeNum());
        while(true) {
            // int directionInt;
            if(myHQ.x < rc.getMapWidth()/2) {
                mySide = Direction.WEST;
                // directionInt = 3;
            } else {
                mySide = Direction.EAST;
                // directionInt = 4;
            }
            if(turnCount < 4 && rc.isReady() && rc.getTeamSoup() >= 70) {
                if (tryBuild(RobotType.MINER, mySide.opposite())) {
                    turnCount++;
                } else if (tryBuild(RobotType.MINER, mySide)) {
                    turnCount++;
                } else if (tryBuild(RobotType.MINER, Direction.SOUTH)) {
                    turnCount++;
                } else if (tryBuild(RobotType.MINER, Direction.NORTH)) {
                    turnCount++;
                }
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

        MapLocation schoolLocation = myHQ.add(mySide.rotateRight().rotateRight().rotateRight())
                .add(mySide.rotateRight().rotateRight().rotateRight());
        MapLocation targetLocation = rc.getLocation();
        Direction setDirection = randomDirection();
        boolean foundSoup = false;
        boolean schoolBuilt = false;
        boolean refineryBuilt = false;
        Direction bugDirection = null;
        Direction bugDirection2 = null;

        setPositionsAroundHQ(myHQ);

        while(true) {
            if(!foundSoup || (rc.canSenseLocation(targetLocation) && rc.senseSoup(targetLocation)<=0)) {
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

            if(rc.isReady()) {
                turnCount++;
                MapLocation refinery = hasRefinery();
                if(refinery != null) {
                    myHQ = refinery;
                }else {
                    if(shouldIBuildRefinery() ) {
                        for(Direction direction : directions) {
                            if(tryBuild(RobotType.REFINERY, direction)){
                                continue;
                            }
                        }
                    }
                }
                if(!schoolBuilt && rc.getLocation().isAdjacentTo(schoolLocation) &&
                        rc.getTeamSoup() >= 150 && rc.getRoundNum() > 15) {
                    RobotInfo ri = rc.senseRobotAtLocation(schoolLocation);
                    if(ri == null) {
                        if(tryBuild(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(schoolLocation)))
                            schoolBuilt = true;
                    } else if (ri.getType() == RobotType.DESIGN_SCHOOL) {
                        schoolBuilt = true;
                    }

                } else {
                    // If full return to base
                    if(rc.getSoupCarrying()>=RobotType.MINER.soupLimit) {
                        bugDirection2 = bugMoveReturn(bugDirection2);
                    }else {
                        for(Direction dir : directions) {
                            if(rc.canSenseLocation(rc.adjacentLocation(dir)) && rc.senseSoup(rc.adjacentLocation(dir))>0) {
                                foundSoup = true;
                                targetLocation = rc.adjacentLocation(dir);
                            }
                        }
                        if(foundSoup) {
                            bugDirection = bugMoveMine(targetLocation, bugDirection);
                        } else {
                            if (turnCount%10==0) {
                                Direction newDirection = randomDirection();
                                while(newDirection == setDirection) {
                                    newDirection = randomDirection();
                                }
                                setDirection = newDirection;
                            }
                            while((rc.canSenseLocation(rc.adjacentLocation(setDirection)) && rc.senseFlooding(rc.adjacentLocation(setDirection))) || !rc.canMove(setDirection))
                                setDirection = directions[(int)(Math.random()*8)];
                            tryMove(setDirection);
                        }
                    }
                }
            }
            Clock.yield();
        }
    }
    static Direction bugMoveMine(MapLocation targetLocation, Direction bugDirection) throws GameActionException{
        if(rc.getLocation().isAdjacentTo(targetLocation) && !rc.adjacentLocation(mySide).equals(myHQ)) {
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
                        currDir = currDir.rotateLeft();
                    }
                    tryMove(currDir);
                    return currDir;
                }
            }
            if(rc.canMove(bugDirection.rotateRight().rotateRight()) && rc.canSenseLocation(rc.adjacentLocation(bugDirection.rotateRight().rotateRight())) && !rc.senseFlooding(rc.adjacentLocation(bugDirection.rotateRight().rotateRight()))){
                tryMove(rc.getLocation().directionTo(targetLocation));
                return null;
            }
            if(rc.getLocation().directionTo(targetLocation) == bugDirection.rotateLeft() && rc.canMove(bugDirection.rotateLeft())){
                tryMove(bugDirection.rotateLeft());
                return null;
            }
            if(rc.getLocation().directionTo(targetLocation) == bugDirection && rc.canMove(bugDirection)){
                tryMove(bugDirection);
                return null;
            }
            if(rc.canMove(bugDirection.rotateRight()) && rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && !rc.senseFlooding(rc.adjacentLocation(bugDirection))) {
                bugDirection = bugDirection.rotateRight();
                if(rc.canMove(bugDirection.rotateRight()) && rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && !rc.senseFlooding(rc.adjacentLocation(bugDirection))) {
                    tryMove(bugDirection.rotateRight());
                    if(bugDirection.rotateRight().equals(rc.getLocation().directionTo(targetLocation))) {
                        return null;
                    }
                    return bugDirection.rotateRight();
                }
                if(bugDirection.rotateRight().equals(rc.getLocation().directionTo(targetLocation))) {
                    return null;
                }
                return bugDirection;
            }else {
                while(!rc.canMove(bugDirection) || (rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && rc.senseFlooding(rc.adjacentLocation(bugDirection)))) {
                    bugDirection = bugDirection.rotateLeft();
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
                        currDir = currDir.rotateLeft();
                    }
                    tryMove(currDir);
                    return currDir;
                }
            }
            if(rc.canMove(bugDirection.rotateRight().rotateRight()) && rc.canSenseLocation(rc.adjacentLocation(bugDirection.rotateRight().rotateRight())) && !rc.senseFlooding(rc.adjacentLocation(bugDirection.rotateRight().rotateRight()))){
                tryMove(rc.getLocation().directionTo(myHQ));
                return null;
            }
            if(rc.getLocation().directionTo(myHQ) == bugDirection.rotateLeft() && rc.canMove(bugDirection.rotateLeft())){
                tryMove(bugDirection.rotateLeft());
                return null;
            }
            if(rc.getLocation().directionTo(myHQ) == bugDirection && rc.canMove(bugDirection)){
                tryMove(bugDirection);
                return null;
            }
            if(rc.canMove(bugDirection.rotateRight()) && rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && !rc.senseFlooding(rc.adjacentLocation(bugDirection))) {
                bugDirection = bugDirection.rotateRight();
                if(rc.canMove(bugDirection.rotateRight()) && rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && !rc.senseFlooding(rc.adjacentLocation(bugDirection))) {
                    tryMove(bugDirection.rotateRight());
                    if(bugDirection.rotateRight().equals(rc.getLocation().directionTo(myHQ))) {
                        return null;
                    }
                    return bugDirection.rotateRight();
                }
                if(bugDirection.rotateRight().equals(rc.getLocation().directionTo(myHQ))) {
                    return null;
                }
                return bugDirection;
            }else {
                while(!rc.canMove(bugDirection) || (rc.canSenseLocation(rc.adjacentLocation(bugDirection)) && rc.senseFlooding(rc.adjacentLocation(bugDirection)))) {
                    bugDirection = bugDirection.rotateLeft();
                }
                tryMove(bugDirection);
                return bugDirection;
            }
        }
    }
    static void runMinerAttack2() throws GameActionException{
        while(mySide == Direction.CENTER) {
            if(rc.getRoundNum() > 1) {
                readInitialMessage();
            }
        }
        runMinerAttackTemplate(new MapLocation[]{(new MapLocation(myHQ.x, rc.getMapHeight()-myHQ.y-1)),
                (new MapLocation(rc.getMapWidth()-myHQ.x-1, rc.getMapHeight()-myHQ.y-1))});
    }

    static void runMinerAttack() throws GameActionException{
        while(mySide == Direction.CENTER) {
            if(rc.getRoundNum() > 1) {
                readInitialMessage();
            }
        }
        runMinerAttackTemplate(new MapLocation[]{new MapLocation(rc.getMapWidth()-myHQ.x-1, myHQ.y)});
    }

    static void runMinerAttackTemplate(MapLocation[] targetLocations) throws GameActionException{
        Direction bugDirection = null;
        int currentChecking = 0;
        boolean foundHQ = false;
        while(true) {
            if(rc.isReady()) {
                turnCount++;
                if(turnCount%80 == 0 && !foundHQ) {
                    currentChecking++;
                }
                if(foundHQ && rc.getLocation().isAdjacentTo(targetLocations[currentChecking])) {
                    if(!hasDesignSchool()){
                        Direction currDir = rc.getLocation().directionTo(targetLocations[currentChecking]);
                        while(!tryBuild(RobotType.DESIGN_SCHOOL, currDir)){
                            currDir = currDir.rotateRight();
                        }
                    }
                    Clock.yield();
                    runMiner();
                }else {
                    if(currentChecking >= targetLocations.length) {
                        runMiner();
                    }else {
                        if(rc.canSenseLocation(targetLocations[currentChecking]) && !(rc.senseRobotAtLocation(targetLocations[currentChecking]) == null) && rc.senseRobotAtLocation(targetLocations[currentChecking]).getType().equals(RobotType.HQ)) {
                            foundHQ = true;
                        }
                        else if(rc.canSenseLocation(targetLocations[currentChecking]) && (rc.senseRobotAtLocation(targetLocations[currentChecking]) == null || !rc.senseRobotAtLocation(targetLocations[currentChecking]).getType().equals(RobotType.HQ))) {
                            currentChecking++;
                        }
                        if(currentChecking >= targetLocations.length) {
                            runMiner();
                        }else {
                            bugDirection = bugMoveMine(targetLocations[currentChecking], bugDirection);
                        }
                    }
                }
            }
        }

    }
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    static void runRefinery() throws GameActionException {
        while (true) {
            Clock.yield();
        }
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
            if(rc.getTeamSoup() > 155 && rc.isReady()) {
                if(tryBuild(RobotType.LANDSCAPER, mySide)) {
                    System.out.println("This design school has made landscaper " + turnCount);
                    turnCount++;
                }
            }

            Clock.yield();
        }
    }

    static void runLandscaper() throws GameActionException {
        while(mySide == Direction.CENTER) {
            if(rc.getRoundNum() > 1) {
                readInitialMessage();
            }
        }

        boolean foundEnemyHQ = false;
        RobotInfo[] startScan = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo ri : startScan) {
            if(ri.getType() == RobotType.HQ)
                foundEnemyHQ = true;
        }

        if (foundEnemyHQ) {
            runLandscaperOffense();
        } else {
            runLandscaperDefense();
        }
    }
    static void runLandscaperDefense() throws GameActionException {
        System.out.println("I am on defense!");
        setPositionsAroundHQ(myHQ);

        MapLocation destination = positions[1];
        while (!destination.equals(rc.getLocation())) {
            System.out.println("going to my room");
            // check if location is occupied by a landscaper
            RobotInfo rf = rc.senseRobotAtLocation(destination);
            if (rf != null) {
                if (rf.type == RobotType.LANDSCAPER && rf.getTeam() == rc.getTeam() && rf.getID() != rc.getID())
                    destination = myHQ.add(myHQ.directionTo(destination).rotateLeft());
            }
            if (rc.isReady() && pathTowards(destination))
                turnCount++;

            System.out.println("I am here: " + rc.getLocation().x + "," + rc.getLocation().y);
            System.out.println("My Room is here: " + destination.x + "," + destination.y);
        }

        System.out.println("I should dig now!");
        myPosition = getMyPosition(myHQ.directionTo(rc.getLocation()));
        while(true) {
            if (rc.isReady()) {
                System.out.println("defend.");
                if (rc.getDirtCarrying() < RobotType.LANDSCAPER.dirtLimit) {
                    System.out.println("Collecting Dirt");
                    tryDigDefensive();
                }
                else
                    rc.depositDirt(Direction.CENTER);
            } else {
                Clock.yield();
            }
        }
    }
    static void runLandscaperOffense() throws GameActionException {
        System.out.println("I am on offense!");
        ;
        // find enemy base and attack it
        String targetFound = "_";
        MapLocation enemySchool = null;
        MapLocation enemyHQ = null;;
        Direction setDirection = null;

        while(true) {
            if(targetFound.equals("_")) {
                RobotInfo[] enemyList = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                for(RobotInfo ri : enemyList) {
                    switch (ri.getType()) {
                        case DESIGN_SCHOOL:
                            enemySchool = ri.getLocation();
                            if(targetFound.equals("hq"))
                                targetFound = "school";
                            break;
                        case HQ:
                            enemyHQ = ri.getLocation();
                            setPositionsAroundHQ(enemyHQ);
                            targetFound = "hq";
                            break;
                    }
                }
                System.out.println("I found a " + targetFound);
            }

            if(rc.isReady()) {
                boolean abort = false;
                switch (targetFound) {
                    case "school":
                        // check if the design school is still there
                        if(rc.canSenseLocation(enemySchool)) {
                            RobotInfo ri = rc.senseRobotAtLocation(enemySchool);
                            if(ri == null || ri.getType() != RobotType.DESIGN_SCHOOL) {
                                abort = true;
                                targetFound = "_";
                            }
                        }

                        if(!abort) {
                            if (!rc.getLocation().isAdjacentTo(enemySchool))
                                pathTowards(enemySchool);
                            else {
                                if (rc.getDirtCarrying() > 0) {
                                    if (rc.canDepositDirt(rc.getLocation().directionTo(enemySchool)))
                                        rc.depositDirt(rc.getLocation().directionTo(enemySchool));
                                } else {
                                    if (rc.canDigDirt(rc.getLocation().directionTo(enemySchool).rotateLeft()))
                                        rc.digDirt(rc.getLocation().directionTo(enemySchool).rotateLeft());
                                    else if (rc.canDigDirt(rc.getLocation().directionTo(enemySchool).rotateRight()))
                                        rc.digDirt(rc.getLocation().directionTo(enemySchool).rotateRight());
                                    else if (rc.canDigDirt(enemySchool.directionTo(rc.getLocation())))
                                        rc.digDirt(enemySchool.directionTo(rc.getLocation()));
                                }
                            }
                        }
                        break;

                    case "hq":
                        if (rc.getLocation().isAdjacentTo(enemyHQ)) {
                            while (rc.getLocation().isAdjacentTo(enemyHQ)) {
                                if(rc.canDepositDirt(rc.getLocation().directionTo(enemyHQ))) {
                                    rc.depositDirt(rc.getLocation().directionTo(enemyHQ));
                                } else {
                                    rc.digDirt(Direction.CENTER);
                                }
                            }
                        } else {

                            if(rc.canSenseLocation(positions[1]))
                                if(rc.senseRobotAtLocation(positions[1]) != null)
                            if(rc.canSenseLocation(positions[2]))
                                if(rc.senseRobotAtLocation(positions[2]) != null)
                            if(rc.canSenseLocation(positions[3]))
                                if(rc.senseRobotAtLocation(positions[3]) != null)
                            if(rc.canSenseLocation(positions[4]))
                                if(rc.senseRobotAtLocation(positions[4]) != null)
                            if(rc.canSenseLocation(positions[5]))
                                if(rc.senseRobotAtLocation(positions[5]) != null)
                            if(rc.canSenseLocation(positions[6]))
                                if(rc.senseRobotAtLocation(positions[6]) != null)
                            if(rc.canSenseLocation(positions[7]))
                                if(rc.senseRobotAtLocation(positions[7]) != null)
                            if(rc.canSenseLocation(positions[8]))
                                if(rc.senseRobotAtLocation(positions[8]) != null) {
                                    if(enemySchool != null)
                                        targetFound = "school";
                                    else
                                        targetFound = "_";
                                }

                            pathTowards(enemyHQ);
                        }
                        break;

                    default:
                        if (setDirection == null) {
                            setDirection = directions[(int)(Math.random()*8)];
                        } else if (turnCount % 10 == 0) {
                            Direction newDirection = directions[(int)(Math.random()*8)];
                            while(newDirection == setDirection) {
                                newDirection = directions[(int) (Math.random() * 8)];
                            }
                        }
                        while((rc.canSenseLocation(rc.adjacentLocation(setDirection)) && rc.senseFlooding(rc.adjacentLocation(setDirection))) || !rc.canMove(setDirection))
                            setDirection = directions[(int)(Math.random()*8)];
                        tryMove(setDirection);
                        break;
                }
            }

            Clock.yield();
        }
    }
    static void tryDigDefensive() throws GameActionException {
        switch (myPosition) {
            case 6:
            case 7:
            case 8:
                if(rc.canDigDirt(rc.getLocation().directionTo(myHQ)))
                    rc.digDirt(rc.getLocation().directionTo(myHQ));
                else if (rc.canDigDirt(rc.getLocation().directionTo(positions[22])))
                    rc.digDirt(rc.getLocation().directionTo(positions[22]));
                break;
            case 5:
                if(rc.canDigDirt(rc.getLocation().directionTo(myHQ)))
                    rc.digDirt(rc.getLocation().directionTo(myHQ));
                else if (rc.canDigDirt(rc.getLocation().directionTo(positions[18])))
                    rc.digDirt(rc.getLocation().directionTo(positions[18]));
                break;
            case 1:
                if(rc.canDigDirt(rc.getLocation().directionTo(myHQ)))
                    rc.digDirt(rc.getLocation().directionTo(myHQ));
                else if (rc.canDigDirt(rc.getLocation().directionTo(positions[10])))
                    rc.digDirt(rc.getLocation().directionTo(positions[10]));
                break;
            case 2:
            case 3:
            case 4:
                if(rc.canDigDirt(rc.getLocation().directionTo(myHQ)))
                    rc.digDirt(rc.getLocation().directionTo(myHQ));
                else if (rc.canDigDirt(rc.getLocation().directionTo(positions[14])))
                    rc.digDirt(rc.getLocation().directionTo(positions[14]));
                break;
        }
    }
    static int getMyPosition(Direction dir) {
        switch (dir) {
            case WEST:          return 1;
            case SOUTHWEST:     return 2;
            case SOUTH:         return 3;
            case SOUTHEAST:     return 4;
            case EAST:          return 5;
            case NORTHEAST:     return 6;
            case NORTH:         return 7;
            case NORTHWEST:     return 8;
            default:            return 0;
        }
    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
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

    static void setPositionsAroundHQ(MapLocation mp) {
        int i = 0;

        // hq
        positions[0] = mp;

        // inner ring
        positions[1] = mp.translate(-1,0);
        positions[2] = mp.translate(-1,-1);
        positions[3] = mp.translate(0 ,-1);
        positions[4] = mp.translate(1 ,-1);
        positions[5] = mp.translate(1 ,0);
        positions[6] = mp.translate(1 ,1);
        positions[7] = mp.translate(0 ,1);
        positions[8] = mp.translate(-1,1);

        // outer ring (separated into sides)
        positions[9] = mp.translate(-2,1);
        positions[10] = mp.translate(-2,0);
        positions[11] = mp.translate(-2,-1);
        positions[12] = mp.translate(-2,-2);

        positions[13] = mp.translate(-1,-2);
        positions[14] = mp.translate(0 ,-2);
        positions[15] = mp.translate(1 ,-2);
        positions[16] = mp.translate(2 ,-2);

        positions[17] = mp.translate(2 ,-1);
        positions[18] = mp.translate(2 ,0);
        positions[19] = mp.translate(2 ,1);
        positions[20] = mp.translate(2 ,2);

        positions[21] = mp.translate(1 ,2);
        positions[22] = mp.translate(0 ,2);
        positions[23] = mp.translate(-1,2);
        positions[24] = mp.translate(-2,2);

        // 3rd ring (also separated by side)
        positions[25] = mp.translate(-3,2);
        positions[26] = mp.translate(-3,1);
        positions[27] = mp.translate(-3,0);
        positions[28] = mp.translate(-3,-1);
        positions[29] = mp.translate(-3,-2);
        positions[30] = mp.translate(-3,-3);

        positions[31] = mp.translate(-2,-3);
        positions[32] = mp.translate(-1,-3);
        positions[33] = mp.translate(0,-3);
        positions[34] = mp.translate(1,-3);
        positions[35] = mp.translate(2,-3);
        positions[36] = mp.translate(3,-3);

        positions[37] = mp.translate(3,-2);
        positions[38] = mp.translate(3,-1);
        positions[39] = mp.translate(3,0);
        positions[40] = mp.translate(3,1);
        positions[41] = mp.translate(3,2);
        positions[42] = mp.translate(3,3);

        positions[43] = mp.translate(2,3);
        positions[44] = mp.translate(1,3);
        positions[45] = mp.translate(0,3);
        positions[46] = mp.translate(-1,3);
        positions[47] = mp.translate(-2,3);
        positions[48] = mp.translate(-3,3);
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

        rc.submitTransaction(new int[] {(698 + myHQ.x), 165, (myHQ.y + 420), 510, directionInt}, 2);
    }

    /**
     * for robots to read the HQ's location and side of play at creation
     * @throws GameActionException
     */
    static void readInitialMessage() throws GameActionException {
        int round = 1;
        Transaction[] roundBlock;

        while(round < rc.getRoundNum()) {
            roundBlock = rc.getBlock(round);
            for(Transaction t : roundBlock) {
                if( t.getMessage().length  == 5     &&
                        t.getCost()            == 2     &&
                        t.getMessage()[1]      == 165   &&
                        t.getMessage()[3]      == 510)  {
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
