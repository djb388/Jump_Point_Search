package bytecodeNav;

/**
 *
 * @author david
 */
public class HashQueue {

    
    JumpPoint[] points;     // Array used to store this heap's nodes.
    
    // Coordinates of the goal.
    int goalX,goalY;
    int[] visited;          // Record of locations that have been visited.
    
    int size;               // Size of this heap.
    int bestScore;          // Best possible score
    
    /**
     * Constructor. Creates four JumpPoint objects and prioritizes them based on
     * their directions of search relative to the goal. A 16-direction compass is
     * used to do this prioritization, which is then reduced to the 8-direction
     * compass used is all other areas of the Navigation package.
     * @param start start coordinates.
     * @param g end coordinates.
     */
    public HashQueue(int startX, int startY, int goal_X, int goal_Y) {
        visited = new int[65536];           // Track which nodes have been visited  
        points = new JumpPoint[8192];       // Memory for the hash table
        
        JumpPoint.goalX = goal_X;
        JumpPoint.goalY = goal_Y;
        goalX = goal_X;
        goalY = goal_Y;
        
        // Calculate the Chebyshev distance to the goal
        bestScore = Math.max(Math.abs(goalX - startX), Math.abs(goalY - startY));
        
        // Create four new JumpPoint objects at this location
        JumpPoint front = new JumpPoint(startX,startY, bestScore);
        JumpPoint next = new JumpPoint(startX,startY, bestScore);
        JumpPoint next2 = new JumpPoint(startX,startY, bestScore);
        JumpPoint next3 = new JumpPoint(startX,startY, bestScore);
        
        // Mark the initial coordinates as visited
        visited[startY*256+startX] = -1;
        
        switch (front.directionTo(goalX,goalY)) {
            case 1:     // NNE
                front.direction = 1;    // NE
                next.direction = 7;     // NW
                next2.direction = 3;    // SE
                next3.direction = 5;    // SW
                break;
            case 3:     // ENE
                front.direction = 1;    // NE
                next.direction = 3;     // SE
                next2.direction = 7;    // NW
                next3.direction = 5;    // SW
                break;
            case 5:     // ESE
                front.direction = 3;    // SE
                next.direction = 1;     // NE
                next2.direction = 5;    // SW
                next3.direction = 7;    // NW
                break;
            case 7:     // SSE
                front.direction = 3;    // SE
                next.direction = 5;     // SW
                next2.direction = 1;    // NE
                next3.direction = 7;    // NW
                break;
            case 9:     // SSW
                front.direction = 5;    // SW
                next.direction = 3;     // SE
                next2.direction = 7;    // NW
                next3.direction = 1;    // NE
                break;
            case 11:    // WSW
                front.direction = 5;    // SW
                next.direction = 7;     // NW
                next2.direction = 3;    // SE
                next3.direction = 1;    // NE
                break;
            case 13:    // WNW
                front.direction = 7;    // NW
                next.direction = 5;     // SW
                next2.direction = 1;    // NE
                next3.direction = 3;    // SE
                break;
            default:    // NNW
                front.direction = 7;    // NW
                next.direction = 1;     // NE
                next2.direction = 5;    // SW
                next3.direction = 3;    // SE
                break;
        }
        
        // Start at index = 1 for efficient access to objects in the heap 
        points[bestScore] = front;  // top of the heap
        front.hashNext = next;   // top's left node
        next.hashNext = next2;  // top's right node
        next2.hashNext = next3;  // top left's left node 
        
        size = 4;
    }
    
    public int peek() {
        int out;
        if (size == 0) {
            out = bestScore;
        } else {
            int i = bestScore-1;
            while (points[++i] == null) {}
            bestScore = i;
            out = bestScore;
        }
        return out;
    }
    
    public JumpPoint peek(int key) {
        return points[key];
    }
    
    /**
     * Remove the jump point at the top of the gash queue.
     * @return the jump point on the heap with the lowest f(n) = h(n) + g(n)
     */
    public JumpPoint remove() {
        JumpPoint remove;
        if (size == 0) {
            remove = null;
        } else {
            int i = bestScore-1;
            while (points[++i] == null) {}
            remove = points[i];
            points[i] = remove.hashNext;
            bestScore = i;
            size--;
        }
        
        return remove; // Return the JumpPoint with the smallest heuristic score.
    }
    
    /**
     * Insert a JumpPoint into the hash table, and throw its key on the bottom
     * of the heap for later sorting only if it is a new key.
     * @param jp JumpPoint
     */
    public void insert(JumpPoint jp) {
        int index = jp.y*256+jp.x;  // Linear index of newNode coordinates.
        int visitedDirection = visited[index];  // Previous direction of search from the new point, if any.
        
        if (visitedDirection == 0) {    // If this node has not been visited...
            visited[index] = jp.direction; // Mark it as visited with the current direction of search.
            int myScore = jp.score;
            jp.hashNext = points[myScore];
            points[myScore] = jp;
            bestScore = Math.min(myScore,bestScore);
            size++;
        } else if (visitedDirection != jp.direction && visitedDirection > 0) {
            visited[index] = -1;    // No need to visit this location again, mark as closed.
            int myScore = jp.score;
            jp.hashNext = points[myScore];
            points[myScore] = jp;
            bestScore = Math.min(myScore,bestScore);
            size++;
        }
    }
    
    /**
     * Retrace the path from any given jump point to the start.
     * Links nodes to create a bi-directional graph from the starting jump point
     * to the given jump point.
     * @param jp Jump point at the end of the path
     * @return Jump point at the start of the path
     */
    public JumpPoint retrace(JumpPoint jp) {
        JumpPoint next = jp.mapLast;// Previous JumpPoint on jp's path.
        while(next != null) {   // While jp has a previous JumpPoint on its path...
            next.mapNext = jp;  // Link jp and next in the forward direction.
            jp = next;          // Move back along the path by having jp reference its previous JumpPoint.
            next = next.mapLast;// next = the JumpPoint prior to itself.
        }
        return jp;
    }
    
        /**
     * Retrace the path from any given jump point to the start.
     * @param jp Jump point at the end of the path
     * @return Jump point at the start of the path
     */
    public JumpPoint retraceNoLink(JumpPoint jp) {
        JumpPoint next = jp.mapLast;// Previous JumpPoint on jp's path.
        JumpPoint temp = null;
        while(next != null) {   // While jp has a previous JumpPoint on its path...
            temp = jp; 
            jp = next;          // Move back along the path by having jp reference its previous JumpPoint.
            next = temp.mapLast;// next = the JumpPoint prior to itself.
        }
        if (temp != null) {
            return temp;
        }
        return jp;   // Return a copy of the JumpPoint at the start of the path.
    }
}
