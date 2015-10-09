package bytecodeNav;

/**
 * Navigation based on Jump Point Search (a flavor of A*) utilizing arrays of
 * bits to store the map in memory. This enables the use of efficient bit
 * manipulation to determine the number of trailing zeroes, leading zeroes,
 * trailing ones, and leading ones between any two points. The number of
 * trailing or leading zeroes represents the walkable distance from one point in
 * the direction of another point, and the number of trailing or leading ones
 * represents the number of non-walkable spaces from one point in the direction
 * to another point.
 *
 * Jump Point Search can be more efficient than vanilla A* when the cost of
 * creating, adding and removing nodes to a queue is high and the cost of
 * checking many spaces is low. In the case of Battlecode, it should be far more
 * efficient in terms of bytecode use.
 *
 * https://graphics.stanford.edu/~seander/bithacks.html#ZerosOnRightModLookup  
 * @author David
 */
 
import battlecode.common.*;

public class Navigation {

    public static void run(RobotController rc) {
    
        int[] start = new int[]{121, 121};   // Search from these coordinates
        int[] end = new int[]{133,133};     // Search to these coordinates

        Navigation n = new Navigation(128, 128, start[0], start[1]);

        // Populate two bit arrays based on a String representation of the map in the TestMaps class.
        map.mapX = TestMaps.getXMap();            // Bit array to represent rows.
        map.mapY = TestMaps.getYMap(map.mapX);  // Bit array to represent columns.

        int roundNum = Clock.getRoundNum();
        
        // Try to move once per round.
        searching = true;
        while (searching) {
            boolean moved = tryMove(end);
            if (moved) {
                
                System.out.println("nextPt: ("+ nextPt[0] +"," + nextPt[1]+")");
            }
            rc.yield();
        }
        
        // Print out the map with the jump points of the path marked by their directions of search.
        for (int i = 119; i < 137; i++) {
            for (int j = 119; j < 137; j++) {
            
                // If this location exists in the path, print the direction taken from this point.
                boolean notVisited = true;
                if (pathStart != null) {
                    JumpPoint path = new JumpPoint(pathStart);
                    while (path != null) {
                        if (path.x == j && path.y == i) {
                            System.out.print(path.direction + " ");
                            notVisited = false;
                            break;
                        } else {
                            path = path.mapNext;
                        }
                    }
                }
            
            
            
                // Print out the map; does not use any objects related to the path.
                if ((((map.mapX[i][(j) / 64] >>> (63 - (j % 64))) & 1L) == 1)
                        && (((map.mapY[j][(i) / 64] >>> (63 - (i % 64))) & 1L) == 1)) {
                    System.out.print("X ");
                } else if (notVisited) {
                    System.out.print("- ");
                }
            }
            System.out.println();
        }
    }

    static Map map;    // Map object to hold the bit arrays and the origin.
    static HashQueue heap;  // Priority heap for the current search.
    static JumpPoint pathStart;
    static int[] nextPt;
    static boolean searching, reachedGoal;
    public static final int bytecodeLimit = 2000;

    /**
     * Constructor. Origin will be shifted to the center of a 256x256 map to
     * prevent indices from going out of bounds for maps up to 128x128 bits
     * regardless of where the actual center of the map is.
     *
     * @param x coordinate of the origin of the map.
     * @param y coordinate of the origin of the map.
     * @param myX current location's x-coordinate.
     * @param myY current location's y-coordinate.
     */
    public Navigation(int x, int y, int myX, int myY) {
        map = new Map(x, y);
        nextPt = new int[]{myX,myY};
        reachedGoal = false;
        searching = false;
        pathStart = null;
        heap = null;
    }
    
    public static boolean tryMove(int[] goal) {
        boolean moved;
        JumpPoint path;
         if (heap != null) {
            // Continue a previous search
            path = getPath(null,null);
        } else if (!reachedGoal) {
            path = getPath(nextPt,goal);
        } else {
            path = null;
        }
        if (reachedGoal) {
            pathStart = path;
        }
        return false;
    }
    
    public static int directionTo(int[] location, int x, int y) {
        int nextDirection;
        x = location[0] - x;
        y = location[1] - y;
        boolean dx = x <= 0;
        boolean dy = y <= 0;
        if(dx && dy) {
            if (x == 0) {
                if (y == 0) {
                    nextDirection = -1;
                } else {
                    nextDirection = 2;
                }
            } else if (y == 0) {
                nextDirection = 4;
            } else {
                nextDirection = 3;
            }
        } else if (dx) {
            if (x == 0) {
                nextDirection = 0;
            } else {
                nextDirection = 1;
            }
        } else if (dy) {
            if (y == 0) {
                nextDirection = 6;
            } else {
                nextDirection = 5;
            }
        } else {
            nextDirection = 7;
        }
        return nextDirection;
    }
    
    public static int[] move(int[] location, int direction) {
        switch(direction) {
            case 0:
                location[1]--;
                break;
            case 1:
                location[0]++;
                location[1]--;
                break;
            case 2:
                location[0]++;
                break;
            case 3:
                location[0]++;
                location[1]++;
                break;
            case 4:
                location[1]++;
                break;
            case 5:
                location[0]--;
                location[1]++;
                break;
            case 6:
                location[0]--;
                break;
            case 7:
                location[0]--;
                location[1]--;
                break;
        }
        return location;
    }
    
    /**
     * Resume the previous search.
     *
     * @return
     */
    public static JumpPoint resume() {
        return getPath(null, null);
    }

    /**
     * Search for a path from point a to point b.
     *
     * @param a start of the path.
     * @param b end of the path.
     * @return the JumpPoint object at the start of this path, or null if there
     * is no path.
     */
    public static JumpPoint getPath(int[] a, int[] b) {
        searching = true;
        if (a != null) {
            heap = new HashQueue(a[0], a[1], b[0], b[1]);
            reachedGoal = false;
            pathStart = null;
        }
        
        int goalX = heap.goalX;
        int goalY = heap.goalY;
        // distances from location to next void (x = stepX, y = stepY)
        int stepX, stepY;
        stepX = -1;
        stepY = -1;
        // previous distances (x-1 = lastStepX, y-1 = lastStepY)
        int lastStepX, lastStepY;
        
        try {
            while (heap.size != 0 && Clock.getBytecodesLeft() > bytecodeLimit) {
                // Get the next node
                JumpPoint next = heap.remove();
                
                // Position of the next Node
                int x = next.x;
                int y = next.y;


                switch (next.direction) {    // determine the direction of movement

                    case 1: // direction = NE

                        if (next.distanceX >= 0) {
                            lastStepX = next.distanceX;
                            lastStepY = next.distanceY;
                        } else {
                        // initialize as the distance from this node to x and y voids
                            lastStepX = NavTools.distanceRight(x, y, map.mapX);
                            lastStepY = NavTools.distanceLeft(y, x, map.mapY);
                        }
                        
                        if (lastStepX != 0) {
                        
                            int time1 = Clock.getBytecodesLeft();
                            // check if the goal is directly reachable from this location
                            if (goalX == x) {
                                if (goalY <= y && goalY >= y - NavTools.distanceLeft(y, x, map.mapY)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (goalY >= y && goalY <= y + NavTools.distanceRight(y, x, map.mapY)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            } else if (goalY == y) {
                                if (goalX <= x && goalX >= x - NavTools.distanceLeft(x, y, map.mapX)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (goalX >= x && goalX <= x + NavTools.distanceRight(x, y, map.mapX)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            }

                            // step = distances to x and y voids & properly increment/decrement location
                            stepX = NavTools.distanceRight(++x, --y, map.mapX);
                            stepY = NavTools.distanceLeft(y, x, map.mapY);

                            // difference between last and current distances
                            int dX = stepX - lastStepX; // should be -1

                            if (dX >= 0) { // if x difference is greater than -1

                                // check the number of consecutive voids at previous location + distanceRight of X
                                int voids = NavTools.distanceVoidRight(x + lastStepX - 1, y + 1, map.mapX);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dX - voids >= -1) {
                                    int distance = voids + lastStepX - 2;
                                    // new direction is SE
                                    heap.insert(new JumpPoint(x + distance, y, next, 3, distance + 1 + next.distance));
                                }

                            } else if (dX < -1) { // if x difference is less than -1

                                // check the number of consecutive voids at current location + distanceRight of X
                                int voids = NavTools.distanceVoidRight(x + stepX, y, map.mapX);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dX + voids < 0) {
                                    // new direction is NE
                                    int distance = voids + stepX - 1;
                                    heap.insert(new JumpPoint(x + distance, y + 1, next, 1, distance + 1 + next.distance));
                                }
                            }

                            int dY = stepY - lastStepY; // should be -1

                            if (dY >= 0) { // if y difference is greater than -1

                                // check the number of consecutive voids at previous location - distanceLeft of Y
                                int voids = NavTools.distanceVoidLeft(y - lastStepY + 1, x - 1, map.mapY);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dY - voids >= -1) {
                                    // new direction is SE, need two waypoints
                                    int distance = voids + lastStepY - 2;
                                    heap.insert(new JumpPoint(x, y - distance, next, 7, distance + 1 + next.distance));
                                }

                            } else if (dY < -1) { // if y difference is less than -1

                                // check the number of consecutive voids at current location - distanceLeft of Y
                                int voids = NavTools.distanceVoidLeft(y - stepY, x, map.mapY);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dY + voids < 0) {
                                    // new direction is NW, need two waypoints
                                    int distance = voids + stepY - 1;
                                    heap.insert(new JumpPoint(x - 1, y - distance, next, 1, distance + 1 + next.distance));
                                }
                            }
                            
                            time1 -= Clock.getBytecodesLeft();
                            System.out.println(time1);
                        }
                        break;

                    case 3: // direction = SE
                    
                        if (next.distanceX >= 0) {
                            lastStepX = next.distanceX;
                            lastStepY = next.distanceY;
                        } else {
                            // initialize as the distance from this node to x and y voids
                            lastStepX = NavTools.distanceRight(x, y, map.mapX);
                            lastStepY = NavTools.distanceRight(y, x, map.mapY);
                        }
                        
                        if (lastStepX != 0) {
                        
                            int time1 = Clock.getBytecodesLeft();

                            // check if the goal is directly reachable from this location
                            if (goalX == x) {
                                if (goalY <= y && goalY >= y - NavTools.distanceLeft(y, x, map.mapY)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (goalY >= y && goalY <= y + NavTools.distanceRight(y, x, map.mapY)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            } else if (goalY == y) {
                                if (goalX <= x && goalX >= x - NavTools.distanceLeft(x, y, map.mapX)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (goalX >= x && goalX <= x + NavTools.distanceRight(x, y, map.mapX)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            }

                            // step = distances to x and y voids & properly increment/decrement location
                            stepX = NavTools.distanceRight(++x, ++y, map.mapX);
                            stepY = NavTools.distanceRight(y, x, map.mapY);

                            // difference between last and current distances
                            int dX = stepX - lastStepX; // should be -1

                            if (dX >= 0) { // if x difference is greater than -1

                                // check the number of consecutive voids at previous location + distanceRight of X
                                int voids = NavTools.distanceVoidRight(x + lastStepX - 1, y - 1, map.mapX);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dX - voids >= -1) {
                                    // new direction is SE
                                    int distance = voids + lastStepX - 2;
                                    heap.insert(new JumpPoint(x + distance, y, next, 1, distance + 1 + next.distance));
                                }

                            } else if (dX < -1) { // if x difference is less than -1

                                // check the number of consecutive voids at current location + distanceRight of X
                                int voids = NavTools.distanceVoidRight(x + stepX, y, map.mapX);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dX + voids < 0) {
                                    // new direction is NE
                                    int distance = voids + stepX - 1;
                                    heap.insert(new JumpPoint(x + distance, y - 1, next, 3, distance + 1 + next.distance));
                                }
                            }

                            int dY = stepY - lastStepY; // should be -1

                            if (dY >= 0) { // if y difference is greater than -1

                                // check the number of consecutive voids at previous location + distanceRight of Y
                                int voids = NavTools.distanceVoidRight(y + lastStepY - 1, x - 1, map.mapY);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dY - voids >= -1) {
                                    // new direction is SE
                                    int distance = voids + lastStepY - 2;
                                    heap.insert(new JumpPoint(x, y + distance, next, 5, distance + 1 + next.distance));
                                }

                            } else if (dY < -1) { // if y difference is less than -1

                                // check the number of consecutive voids at current location + distanceRight of Y
                                int voids = NavTools.distanceVoidRight(y + stepY, x, map.mapY);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dY + voids < 0) {
                                    // new direction is NW
                                    int distance = voids + stepY - 1;
                                    heap.insert(new JumpPoint(x - 1, y + distance, next, 3, distance + 1 + next.distance));
                                }
                            }
                            
                            time1 -= Clock.getBytecodesLeft();
                            System.out.println(time1);
                        }
                        break;

                    case 5: // direction = SW
                    
                        if (next.distanceX >= 0) {
                            lastStepX = next.distanceX;
                            lastStepY = next.distanceY;
                        } else {
                            // initialize as the distance from this node to x and y voids
                            lastStepX = NavTools.distanceLeft(x, y, map.mapX);
                            lastStepY = NavTools.distanceRight(y, x, map.mapY);
                        }
                        
                        if (lastStepX != 0) {
                            int time1 = Clock.getBytecodesLeft();

                            // check if the goal is directly reachable from this location
                            if (goalX == x) {
                                if (goalY <= y && goalY >= y - NavTools.distanceLeft(y, x, map.mapY)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (goalY >= y && goalY <= y + NavTools.distanceRight(y, x, map.mapY)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            } else if (goalY == y) {
                                if (goalX <= x && goalX >= x - NavTools.distanceLeft(x, y, map.mapX)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (goalX >= x && goalX <= x + NavTools.distanceRight(x, y, map.mapX)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            }

                            // step = distances to x and y voids & properly increment/decrement location
                            stepX = NavTools.distanceLeft(--x, ++y, map.mapX);
                            stepY = NavTools.distanceRight(y, x, map.mapY);

                            // difference between last and current distances
                            int dX = stepX - lastStepX; // should be -1

                            if (dX >= 0) { // if x difference is greater than -1

                                // check the number of consecutive voids at previous location - distanceLeft of X
                                int voids = NavTools.distanceVoidLeft(x - lastStepX + 1, y - 1, map.mapX);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dX - voids >= -1) {
                                    // new direction is NW
                                    int distance = voids + lastStepX - 2;
                                    heap.insert(new JumpPoint(x - distance, y, next, 7, distance + 1 + next.distance));
                                }

                            } else if (dX < -1) { // if x difference is less than -1

                                // check the number of consecutive voids at current location - distanceLeft of X
                                int voids = NavTools.distanceVoidLeft(x - stepX, y, map.mapX);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dX + voids < 0) {
                                    // new direction is NE
                                    int distance = voids + stepX - 1;
                                    heap.insert(new JumpPoint(x - distance, y - 1, next, 5, distance + 1 + next.distance));
                                }
                            }

                            int dY = stepY - lastStepY; // should be -1

                            if (dY >= 0) { // if y difference is greater than -1

                                // check the number of consecutive voids at previous location + distanceRight of Y
                                int voids = NavTools.distanceVoidRight(y + lastStepY - 1, x + 1, map.mapY);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dY - voids >= -1) {
                                    // new direction is SE, need two waypoints
                                    int distance = voids + lastStepY - 2;
                                    heap.insert(new JumpPoint(x, y + distance, next, 3, distance + 1 + next.distance));
                                }

                            } else if (dY < -1) { // if y difference is less than -1

                                // check the number of consecutive voids at current location + distanceRight of Y
                                int voids = NavTools.distanceVoidRight(y + stepY, x, map.mapY);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dY + voids < 0) {
                                    // new direction is NW, need two waypoints
                                    int distance = voids + stepY - 1;
                                    heap.insert(new JumpPoint(x + 1, y + distance, next, 5, distance + 1 + next.distance));
                                }
                            }
                            
                            time1 -= Clock.getBytecodesLeft();
                            System.out.println(time1);
                        }
                        break;

                    default: // direction = NW
                        if (next.distanceX >= 0) {
                            lastStepX = next.distanceX;
                            lastStepY = next.distanceY;
                        } else {
                            // initialize as the distance from this node to x and y voids
                            lastStepX = NavTools.distanceLeft(x, y, map.mapX);
                            lastStepY = NavTools.distanceLeft(y, x, map.mapY);
                        }
                        
                        if (lastStepX != 0) {
                        
                            int time1 = Clock.getBytecodesLeft();

                            // check if the goal is directly reachable from this location
                            if (goalX == x) {
                                if (goalY <= y && goalY >= y - NavTools.distanceLeft(y, x, map.mapY)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (goalY >= y && goalY <= y + NavTools.distanceRight(y, x, map.mapY)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            } else if (goalY == y) {
                                if (goalX <= x && goalX >= x - NavTools.distanceLeft(x, y, map.mapX)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (goalX >= x && goalX <= x + NavTools.distanceRight(x, y, map.mapX)) {
                                    next.mapNext = new JumpPoint(goalX, goalY, next, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            }

                            // step = distances to x and y voids & properly increment/decrement location
                            stepX = NavTools.distanceLeft(--x, --y, map.mapX);
                            stepY = NavTools.distanceLeft(y, x, map.mapY);

                            // difference between last and current distances
                            int dX = stepX - lastStepX; // should be -1

                            if (dX >= 0) { // if x difference is greater than -1

                                // check the number of consecutive voids at previous location - distanceLeft of X
                                int voids = NavTools.distanceVoidLeft(x - lastStepX + 1, y + 1, map.mapX);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dX - voids >= -1) {
                                    // new direction is NW
                                    int distance = voids + lastStepX - 2;
                                    heap.insert(new JumpPoint(x - distance, y, next, 5, distance + 1 + next.distance));
                                }

                            } else if (dX < -1) { // if x difference is less than -1

                                // check the number of consecutive voids at current location - distanceLeft of X
                                int voids = NavTools.distanceVoidLeft(x - stepX, y, map.mapX);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dX + voids < 0) {
                                    // new direction is NE
                                    int distance = voids + stepX - 1;
                                    heap.insert(new JumpPoint(x - distance, y + 1, next, 7, distance + 1 + next.distance));
                                }
                            }

                            int dY = stepY - lastStepY; // should be -1

                            if (dY >= 0) { // if y difference is greater than -1

                                // check the number of consecutive voids at previous location - distanceLeft of Y
                                int voids = NavTools.distanceVoidLeft(y - lastStepY + 1, x + 1, map.mapY);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dY - voids >= -1) {
                                    // new direction is SE
                                    int distance = voids + lastStepY - 2;
                                    heap.insert(new JumpPoint(x, y - distance, next, 1, distance + 1 + next.distance));
                                }

                            } else if (dY < -1) { // if y difference is less than -1

                                // check the number of consecutive voids at current location - distanceLeft of Y
                                int voids = NavTools.distanceVoidLeft(y - stepY, x, map.mapY);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dY + voids < 0) {
                                    // new direction is NW
                                    int distance = voids + stepY - 1;
                                    heap.insert(new JumpPoint(x + 1, y - distance, next, 7, distance + 1 + next.distance));
                                }
                            }
                            
                            time1 -= Clock.getBytecodesLeft();
                            System.out.println(time1);
                        }
                        break;
                }
                
                if (stepX > 0) {
                    JumpPoint newJumpPoint = new JumpPoint(x,y, next, next.direction,1+next.distance);
                    newJumpPoint.distanceY = stepY;
                    newJumpPoint.distanceX = stepX;
                    heap.insert(newJumpPoint);
                    
                    if (Clock.getBytecodesLeft() < bytecodeLimit + 500) {
                    
                        JumpPoint out = heap.retrace(next);
                    
                        return out;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            if (heap.size != 0) {
                if (Clock.getBytecodesLeft() > bytecodeLimit + 500) {
                    return getPath(null,null);
                } else {
                    return heap.retrace(heap.peek(heap.peek()));
                }
            }
        }
        searching = false;
        return null;
    }
}
