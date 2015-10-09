package bytecodeNav;

/**
 * Priority heap for jump point search.
 * @author david
 */
public class Heap {
    
    JumpPoint[] points;     // Array used to store this heap's nodes.
    
    int[] goal;             // Coordinates of the goal.
    int[] visited;          // Record of locations that have been visited.
    
    int size;               // Size of this heap.
    
    /**
     * Constructor. Creates four JumpPoint objects and prioritizes them based on
     * their directions of search relative to the goal. A 16-direction compass is
     * used to do this prioritization, which is then reduced to the 8-direction
     * compass used is all other areas of the Navigation package.
     * @param start start coordinates.
     * @param g end coordinates.
     */
    public Heap(int[] start, int[] g) {
        visited = new int[65536];           // Track which nodes have been visited  
        points = new JumpPoint[65536];      // Memory for the priority heap
        goal = new int[] { g[0], g[1] };    // End coordinates
        JumpPoint.goalX = g[0];
        JumpPoint.goalY = g[1];
        
        // Calculate the Chebyshev distance to the goal
        int score = Math.max(Math.abs(g[0] - start[0]), Math.abs(g[1] - start[1]));
        
        // Create four new JumpPoint objects at this location
        JumpPoint front = new JumpPoint(start[0],start[1], score);
        JumpPoint next = new JumpPoint(start[0],start[1], score+1);
        JumpPoint next2 = new JumpPoint(start[0],start[1], score+2);
        JumpPoint next3 = new JumpPoint(start[0],start[1], score+3);
        
        // Mark the initial coordinates as visited
        visited[start[1]*256+start[0]] = -1;
        
        switch (front.directionTo(g[0],g[1])) {
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
        points[1] = front;  // top of the heap
        points[2] = next;   // top's left node
        points[3] = next2;  // top's right node
        points[4] = next3;  // top left's left node 
        
        size = 4;
    }
    
    /**
     * Remove the jump point at the top of the priority heap
     * @return the jump point on the heap with the lowest f(n) = h(n) + g(n)
     */
    public JumpPoint remove() {
        JumpPoint top = points[1];  // Reference the top object of the heap (smallest heuristuc)
        points[1] = points[size];   // Send the bottom object to the top (largest heuristic)
        points[size--] = null;      // Shrink the heap by 1
        
        int stop = size-1;          // Stop condition for the following loop.
        
        // Iterate from the top of the heap to the bottom
        for(int i = 2; i < stop; i*=2) {  // i = index of the left child
            int rightIndex = i+1;           // i+1 = index of the right child
            int index = i/2;                // i/2 = index of the current parent
            
            JumpPoint parent = points[index];       // current parent
            JumpPoint left = points[i];             // left of parent
            JumpPoint right = points[rightIndex];   // right of parent
            int parentScore = parent.score;         // parent's heuristic score
            int leftScore = left.score;             // left's heuristic score
            int rightScore = right.score;           // right's heuristic score
            
            // Find the object with the smallest heuristic score and push it up
            // the heap, swapping it with the object with the largest score.
            if (leftScore < rightScore && leftScore < parentScore) {
                points[i] = parent;         // left = parent
                points[index] = left;       // parent = left
            } else if (rightScore < parentScore) {
                points[rightIndex] = parent;// right = parent    
                points[index] = right;      // parent = right
                i++; // We need to check the right's children next; increment i.
                // i is now the index of the right object.
                // i*2 will be the index of the right's left child.
                // i*2+1 will be the index of the right's right child.
                // i*2/2 will be the index of the right object.
            } else {// Nothing needed to be swapped; our heap is ordered correctly.
                break;  // Done reordering the heap.
            }
        }
        
        return top; // Return the JumpPoint with the smallest heuristic score.
    }
    
    /**
     * Create a new jump point and insert it into the priority heap.
     * @param newNode   coordinates of the new jump point
     * @param direction directionality of search from the new jump point
     * @param waypoint  point between last jump point and the new jump point
     * @param lastJP    the last jump point
     * @param distance the distance traveled to get to the new jump point
     */
    public void insert(JumpPoint newNode) {
        int index = newNode.y*256+newNode.x;  // Linear index of newNode coordinates.
        int visitedDirection = visited[index];  // Previous direction of search from the new point, if any.
        
        if (visitedDirection == 0) {    // If this node has not been visited...
            visited[index] = newNode.direction; // Mark it as visited with the current direction of search.
            
            
            // Increase the size of the heap and create the new JumpPoint object.
            //points[++size] = new JumpPoint(newNode, waypoint, lastJP, direction, distance,
            //        Math.max(Math.abs(goal[0]-newNode[0]),Math.abs(goal[1]-newNode[1]))+distance);
            // Increase the size of the heap and create the new JumpPoint object.
            points[++size] = newNode;
            
            int next = size-1;  // Index of the first JumpPoint.
            
            int score1 = -1;
            
            if (points[next] != null) {
                score1 = points[next].score;
            }
            // Iterate from the bottom of the heap to the top.
            for (int i = next; i > 2; i=next) { // i = the current JumpPoint.
                next /= 2;  // Index of the next JumpPoint (the current's parent).
                int score2 = points[next].score;
                // If the current JumpPoint's heuristic score is less than the next's...
                if (score1 < score2) {
                    JumpPoint temp = points[next];  // Reference the next JumpPoint.
                    points[next] = points[i];       // Swap in the current JumpPoint.
                    points[i] = temp;               // Swap in the next JumpPoint.
                } else {// Nothing needed to be swapped; our heap is ordered correctly.
                    break;  // End of insertion.
                }
                score1 = score2;
            }
        // else if this node has been visited and the direction of search is different...
        } else if (visitedDirection != newNode.direction && visitedDirection > 0) {
            visited[index] = -1;    // No need to visit this location again, mark as closed.
            
            // Increase the size of the heap and create the new JumpPoint object.
            points[++size] = newNode;
            
            int next = size-1;  // Index of the first JumpPoint.
            int score1 = points[next].score;
            // Iterate from the bottom of the heap to the top.
            for (int i = next; i > 2; i=next) { // i = the current JumpPoint.
                next /= 2;  // Index of the next JumpPoint (the current's parent).
                int score2 = points[next].score;
                // If the current JumpPoint's heuristic score is less than the next's...
                if (score1 < score2) {
                    JumpPoint temp = points[next];  // Reference the next JumpPoint.
                    points[next] = points[i];       // Swap in the current JumpPoint.
                    points[i] = temp;               // Swap in the next JumpPoint.
                } else {// Nothing needed to be swapped; our heap is ordered correctly.
                    break;  // End of insertion.
                }
                score1 = score2;
            }
        }
    }
    
    /**
     * Insert an existing JumpPoint into the priority heap.
     * @param jp existing JumpPoint
     */
    public void insert(JumpPoint jp, boolean existing) {
        points[++size] = jp;
        int next = size-1;  // Index of the first JumpPoint.
        // Iterate from the bottom of the heap to the top.
        for (int i = next; i > 2; i=next) { // i = the current JumpPoint.
            next /= 2;  // Index of the next JumpPoint (the current's parent).
                
            // If the current JumpPoint's heuristic score is less than the next's...
            if (points[i].score < points[next].score) {
                JumpPoint temp = points[next];  // Reference the next JumpPoint.
                points[next] = points[i];       // Swap in the current JumpPoint.
                points[i] = temp;               // Swap in the next JumpPoint.
            } else {// Nothing needed to be swapped; our heap is ordered correctly.
                break;  // End of insertion.
            }
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
