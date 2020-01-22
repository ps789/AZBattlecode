package jeromeTestPlayer;
import battlecode.common.*;

public class MinerAI extends RobotPlayer {



	public MinerAI() throws GameActionException {
		readInitialMessage();
		updatePos();
	}

	public void run() throws GameActionException {

		if (findMyHQ().directionTo(pos) == Direction.SOUTHEAST) {
			System.out.println("I am a builder!");
			runAsBuilder();
		}

		else {
			System.out.println("I am a miner!");
			runAsMiner();
		}

	}

	private void runAsBuilder() {
		System.out.println("I am a builder!");
	}

	private void runAsMiner() {
		System.out.println("I am a miner!");
	}
}