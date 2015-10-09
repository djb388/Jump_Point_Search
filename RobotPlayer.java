package bytecodeNav;

import battlecode.common.*;

public class RobotPlayer {

    static RobotController rc;
    public static void run(RobotController controller) {
        rc = controller;
        while (rc.getType() != RobotType.HQ) { rc.yield(); }
        Navigation.run(rc);
        
    }
}
