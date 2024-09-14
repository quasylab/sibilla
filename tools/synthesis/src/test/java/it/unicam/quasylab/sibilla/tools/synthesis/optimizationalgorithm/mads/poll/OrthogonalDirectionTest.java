package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll.OrthogonalDirection;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class OrthogonalDirectionTest {


    @Test
    void testOrthogonalBasis() {
        OrthogonalDirection o = new OrthogonalDirection();

        int[][] expectedOrthogonalBasis1 = new int[][]{
                {1, 0, 0, 0, -1, 0, 0, 0},
                {0, 1, 0, 0, 0, -1, 0, 0},
                {0, 0, 1, 0, 0, 0, -1, 0},
                {0, 0, 0, -1, 0, 0, 0, 1}
        };
        int[][] actualOrthogonalBasis1 = o.generateOrthogonalBasis(4, 7, 0);
        //assertArrayEquals(expectedOrthogonalBasis1, actualOrthogonalBasis1);
        System.out.println("- - - - - - - - - - -");
        plotMatrix(actualOrthogonalBasis1);

        int[][] expectedOrthogonalBasis2 = new int[][]{
                {3, 0, 0, 0, -3, 0, 0, 0},
                {0, 1, 2, -2, 0, -1, -2, 2},
                {0, 2, 1, 2, 0, -2, -1, -2},
                {0, -2, 2, 1, 0, 2, -2, -1}
        };
        int[][] actualOrthogonalBasis2 = o.generateOrthogonalBasis(4, 9, 2);
        //assertArrayEquals(expectedOrthogonalBasis2, actualOrthogonalBasis2);
        System.out.println("- - - - - - - - - - -");
        plotMatrix(actualOrthogonalBasis2);

        int[][] expectedOrthogonalBasis3 = new int[][]{
                {4, -2, -4, 0, -4, 2, 4, 0},
                {-2, 4, -4, 0, 2, -4, 4, 0},
                {-4, -4, -2, 0, 4, 4, 2, 0},
                {0, 0, 0, 6, 0, 0, 0, -6}
        };

        plotMatrix(o.generateOrthogonalBasis(5,8,1));
        int[][] actualOrthogonalBasis3 = o.generateOrthogonalBasis(4, 10, 3);
        //assertArrayEquals(expectedOrthogonalBasis3, actualOrthogonalBasis3);
        System.out.println("- - - - - - - - - - -");
        plotMatrix(actualOrthogonalBasis3);

        int[][] expectedOrthogonalBasis4 = new int[][]{
                {124, 10, 12, -16, -124, -10, -12, 16},
                {10, 76, -60, 80, -10, -76, 60, -80},
                {12, -60, 54, 96, -12, 60, -54, -96},
                {-16, 80, 96, -2, 16, -80, -96, 2}
        };
        int[][] actualOrthogonalBasis4 = o.generateOrthogonalBasis(4, 14, 7);
        //assertArrayEquals(expectedOrthogonalBasis4, actualOrthogonalBasis4);
        System.out.println("- - - - - - - - - - -");
        plotMatrix(actualOrthogonalBasis4);

        int[][] expectedOrthogonalBasis5 = new int[][]{
                {3, -4, -3, 4},
                {-4, -3, 4, 3}
        };
        int[][] actualOrthogonalBasis5 = o.generateOrthogonalBasis(2, 6, 3);
        //assertArrayEquals(expectedOrthogonalBasis5, actualOrthogonalBasis5);
        System.out.println("- - - - - - - - - - -");
        plotMatrix(actualOrthogonalBasis5);

        System.out.println();
        System.out.println();
        System.out.println();
        plotMatrix(o.generateOrthogonalBasis(4,7,0));
        System.out.println("  -- -- -- -- -- -- -- ");
        plotMatrix(o.generateOrthogonalBasis(4,8,1));
        System.out.println("  -- -- -- -- -- -- -- ");
        plotMatrix(o.generateOrthogonalBasis(4,9,2));
        System.out.println("  -- -- -- -- -- -- -- ");
        plotMatrix(o.generateOrthogonalBasis(4,10,3));
        System.out.println("  -- -- -- -- -- -- -- ");
        plotMatrix(o.generateOrthogonalBasis(4,11,4));
        System.out.println("  -- -- -- -- -- -- -- ");
        plotMatrix(o.generateOrthogonalBasis(4,12,5));
        System.out.println("  -- -- -- -- -- -- -- ");
        plotMatrix(o.generateOrthogonalBasis(4,13,6));
        System.out.println("  -- -- -- -- -- -- -- ");
        plotMatrix(o.generateOrthogonalBasis(4,14,7));
        System.out.println("  -- -- -- -- -- -- -- ");

    }



    public  void plotMatrix(int[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            System.out.println("Empty or invalid matrix");
            return;
        }

        int rows = matrix.length;
        int cols = matrix[0].length;

        // Find the maximum number of digits in any element
        int maxDigits = getMaxDigits(matrix);

        // Print the matrix
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.printf("%" + (maxDigits + 1) + "d", matrix[i][j]);
            }
            System.out.println();
        }
    }

    private int getMaxDigits(int[][] matrix) {
        int maxDigits = 1;
        for (int[] row : matrix) {
            for (int num : row) {
                int digits = String.valueOf(Math.abs(num)).length();
                if (num < 0) digits++; // Account for negative sign
                maxDigits = Math.max(maxDigits, digits);
            }
        }
        return maxDigits;
    }


}
