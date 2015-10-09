package bytecodeNav;

/**
 * Map data represented as two 2D arrays of longs.
 * 
 * - Two 2D arrays are used to facilitate bit-manipulation along both the X-axis
 *   and the Y-axis.
 * 
 * - Assumes a maximum grid size of 128x128.
 * 
 * - The set bits in each long represent non-walkable squares in a grid.
 * 
 * - "Real coords" refers to the actual grid's coordinates.
 * - "Map coords" refers to the real coords mapped to a normalized 128x128 grid.
 * - "Indices" or "index" typically refers to indices of bits in the long arrays.
 * 
 * - "Origin" refers to a reference point used to convert between map coords and 
 *    real coords, enabling the user of the map to gather data of a grid up to 
 *    128x128 without knowing the real dimensions.
 * 
 *    Origin in real coords - parameter required by the constructor.
 *    Origin in map coords  - (128,128)
 *    Origin rowMajor index - [128][4]
 *    Origin colMajor index - [4][128]
 * 
 * @author David Bell
 */
public class Map {
    
    public long[][] mapY;   // Column-major storage of the map.
    public long[][] mapX;   // Row-major storage of the map.
    public final int[] origin;  // Origin in real coordinates.
    
    /**
     * Constructor.
     * @param originX - Best approximation for the X-midpoint of the real map.
     * @param originY - Best approximation for the Y-midpoint of the real map.
     */
    public Map(int originX, int originY) {
        origin = new int[] { originX, originY };
        mapX = new long[256][4];
        mapY = new long[256][4];
    }
    
    public int[] mapToArray(int x, int y) {
        return new int[]{x-origin[0]+128, y-origin[1]+128};
    }
    
    public int[] arrayToMap(int[] location) {
        return new int[]{location[0]-128+origin[0],location[1]-128+origin[1]};
    }
}
