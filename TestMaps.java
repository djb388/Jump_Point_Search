package bytecodeNav;

/**
 * Create test maps to test the Navigation package.
 * @author david
 */
public class TestMaps {
    
    
    public static long[][] getXMap() {
        long[][] bitArray = new long[256][4];
        
        String[] map = new String[] {
            "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1",
            "1 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1",
            "1 0 1 0 1 1 1 1 1 1 1 1 0 0 0 1",
            "1 0 0 1 1 0 1 0 1 0 1 0 1 0 0 1",
            "1 0 1 1 0 1 0 1 0 1 0 1 0 1 0 1",
            "1 0 1 0 1 0 1 0 1 0 1 0 1 1 0 1",
            "1 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1",
            "1 0 1 0 1 0 1 0 1 0 1 0 1 1 0 1",
            "1 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1",
            "1 0 1 0 1 0 1 0 1 0 1 0 1 1 0 1",
            "1 1 0 1 0 1 0 1 0 1 0 1 1 1 0 1",
            "1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 1",
            "1 0 0 0 0 0 0 0 0 0 0 0 1 1 1 1",
            "1 0 1 1 1 1 1 1 1 1 1 1 1 0 1 1",
            "1 0 0 0 0 0 0 0 0 0 0 0 1 1 0 1",
            "1 1 1 1 1 1 1 1 1 1 1 1 0 1 1 0"
        };
        
        for (int i = -8; i < 8; i++) {
            String nextLine = map[i+8].replace(" ", "");
            for(int j = -8; j < 8; j++) {
                if (nextLine.charAt(j+8) == '1') {
                    bitArray[128+i][(128+j)/64] |= 1L << (63-(j % 64));
                }
            }
        }
        return bitArray;
    }
    
    public static long[][] getYMap(long[][] x) {
        long[][] bitArray = new long[256][4];
        
        for (int i = 0; i < 256; i++) {
            
            for(int j = 0; j < 256; j++) {
                if ((x[i][j/64] & (1L << (63-(j % 64)))) != 0) {
                    bitArray[j][i/64] |= 1L << (63-(i % 64));
                }
            }
        }
        return bitArray;
    }
}
