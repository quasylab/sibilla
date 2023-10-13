package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import static it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.Common.*;

@SuppressWarnings("ManualArrayCopy")
public class LTDirection {

    public record BlVectorAndiHat(
            int[] blVector,
            int iHat
    ) {}

    BlVectorAndiHat memorizedBLVectorAndiHat;
    int lc;

    public LTDirection() {

    }

    public int[] getRowPermutation(int n, int iHat){
        int[] rowPermutationArray = getIntArrayWithoutValue(getSequentialIntArray(0,n-1),iHat);
        shuffle(rowPermutationArray);
        return rowPermutationArray;
    }

    public int[] getColumnPermutation(int n){
        int[] columnPermutationArray = getSequentialIntArray(0,n-1);
        shuffle(columnPermutationArray);
        return columnPermutationArray;
    }


    public BlVectorAndiHat getBlVectorAndCapI(int n, double deltaMash){
        double l = (-1)*log(deltaMash,4);
        if(lc > l)
            return memorizedBLVectorAndiHat;
        lc += 1;
        int iHat = getRandomIntBetween(0,n-1);
        int[] blVector = new int[n];
        blVector[iHat] = getRandomElementInTheArray(new int[]{ (int) ((-1)*Math.pow(2.0,l)) , (int) Math.pow(2.0,l) });
        for (int i = 0; i < n; i++) {
            if(iHat!=i)
                blVector[i] = getRandomIntBetween((int) ((-1)*Math.pow(2.0,l)+1),(int) (Math.pow(2.0,l)-1));
        }
        this.memorizedBLVectorAndiHat = new BlVectorAndiHat(blVector,iHat);
        return new BlVectorAndiHat(blVector,iHat);
    }

    /**
     * Basis Construction in R^(n-1)
     *
     * Let L a lower triangular (n-1)x(n-1) matrix where each term on the diagonal is
     * either plus or minus 2^(l) and the lower components are randomly chosen in :<br>
     * [ -(2^(l)) + 1 , -(2^(l)) + 2 , ... , 2^(l)-1 ]
     *
     * @param n dimension
     * @param l the l value
     * @return L a lower triangular (n-1)x(n-1) matrix
     */
    public int[][] getL(int n, int l){
        n -= 1;
        int[][] lMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if(i==j)
                    lMatrix[i][j] = getRandomElementInTheArray(new int[]{ (int) ((-1)*Math.pow(2.0,l)) , (int) Math.pow(2.0,l) });
                if(i>j)
                    lMatrix[i][j] = getRandomIntBetween((int) ((-1)*Math.pow(2.0,l)+1),(int) (Math.pow(2.0,l)-1));
            }
        }
        return lMatrix;
    }


    public int[][] getBasis(int n, double deltaMesh){
        BlVectorAndiHat blAndiHat = getBlVectorAndCapI(n,deltaMesh);
        int[] blVector = blAndiHat.blVector;
        int iHat = blAndiHat.iHat;

        int l = (int) ((-1)*log(deltaMesh,4));

        int[][] lMatrix = getL(n,l);
        int[] p = getRowPermutation(n,iHat);
        int[][] b = new int[n][n];

        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < lMatrix.length; j++) {
                b[p[i]][j] = lMatrix[i][j];
            }
        }

        for (int i = 0; i < n; i++) {
            b[i][n-1] = blVector[i];
        }

        int[][] bQuoted = new int[n][n];
        int[] q = getColumnPermutation(n);

        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b.length; j++) {
                bQuoted[i][q[j]] = b[i][j];
            }
        }

        return bQuoted;
    }

    public int[][] getMinimalPositiveBasis(int n, double deltaMesh){
        int[][] basis = getBasis(n,deltaMesh);
        int[][] positiveSpanningSet = new int[n][n+1];
        for (int i = 0; i < positiveSpanningSet.length; i++) {
            int rowSum = 0;
            for (int j = 0; j < positiveSpanningSet[0].length-1; j++) {
                positiveSpanningSet[i][j] = basis[i][j];
                rowSum += basis[i][j];
            }
            positiveSpanningSet[i][positiveSpanningSet[0].length-1] =  rowSum * -1;
        }
        return positiveSpanningSet;
    }



    public int[][] getMaximalPositiveBasis(int n, double deltaMesh){
        int[][] basis = getBasis(n,deltaMesh);
        int[][] positiveSpanningSet = new int[n][n*2];
        for (int i = 0; i < positiveSpanningSet.length; i++) {
            for (int j = 0; j < basis[0].length; j++) {
                positiveSpanningSet[i][j] = basis[i][j];
            }
        }
        for (int i = 0; i < positiveSpanningSet.length; i++) {
            for (int j = 0; j < basis[0].length; j++) {
                positiveSpanningSet[i][j+basis[0].length] = -basis[i][j];
            }
        }
        return positiveSpanningSet;
    }
}
