package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import smile.math.matrix.Matrix;

import java.util.Arrays;
import java.util.Random;

public class Common {


    public static double log(double value, double base) {
        double num = Math.log(value);
        double den = Math.log(base);
        return Math.log(value)/Math.log(base);
    }

    public static int getRandomIntBetween(int min, int max){
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    public static int getRandomElementInTheArray(int[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }


    public static void shuffle(int[] array){
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }

    public static int[] getSequentialIntArray(int begin, int end){
        int[] sequentialArray = new int[end-begin+1];
        for (int i=begin; i<=end; i++) {
            sequentialArray[i] = i;
        }
        return sequentialArray;
    }

    public static int[] getIntArrayWithoutValue(int[] array, int elementToRemove){
        int[] newArray = new int[array.length-1];
        for(int i=0, k=0;i<array.length;i++){
            if(array[i]!=elementToRemove){
                newArray[k]=array[i];
                k++;
            }
        }
        return newArray;
    }



    public static void print1D(int arr[]){
        System.out.println(Arrays.toString(arr));
    }
    public static void print2D(int mat[][]){
        for (int[] row : mat)
            System.out.println(Arrays.toString(row));
    }



    public static int determinant(int[][] matrix){ //method sig. takes a matrix (two dimensional array), returns determinant.
        int sum=0;
        int s;
        if(matrix.length==1){  //bottom case of recursion. size 1 matrix determinant is itself.
            return(matrix[0][0]);
        }
        for(int i=0;i<matrix.length;i++){ //finds determinant using row-by-row expansion
            int[][]smaller= new int[matrix.length-1][matrix.length-1]; //creates smaller matrix- values not in same row, column
            for(int a=1;a<matrix.length;a++){
                for(int b=0;b<matrix.length;b++){
                    if(b<i){
                        smaller[a-1][b]=matrix[a][b];
                    }
                    else if(b>i){
                        smaller[a-1][b-1]=matrix[a][b];
                    }
                }
            }
            if(i%2==0){ //sign changes based on i
                s=1;
            }
            else{
                s=-1;
            }
            sum+=s*matrix[0][i]*(determinant(smaller)); //recursive step: determinant of larger determined by smaller.
        }
        return(sum); //returns determinant value. once stack is finished, returns final determinant.
    }

    // Function to get determinant of matrix
    static int determinantOfMatrix(int mat[][], int n)
    {
        int num1, num2, det = 1, index,
                total = 1; // Initialize result

        // temporary array for storing row
        int[] temp = new int[n + 1];

        // loop for traversing the diagonal elements
        for (int i = 0; i < n; i++)
        {
            index = i; // initialize the index

            // finding the index which has non zero value
            while (index < n && mat[index][i] == 0)
            {
                index++;
            }
            if (index == n) // if there is non zero element
            {
                // the determinant of matrix as zero
                continue;
            }
            if (index != i)
            {
                // loop for swapping the diagonal element row
                // and index row
                for (int j = 0; j < n; j++)
                {
                    swap(mat, index, j, i, j);
                }
                // determinant sign changes when we shift
                // rows go through determinant properties
                det = (int)(det * Math.pow(-1, index - i));
            }

            // storing the values of diagonal row elements
            for (int j = 0; j < n; j++)
            {
                temp[j] = mat[i][j];
            }

            // traversing every row below the diagonal
            // element
            for (int j = i + 1; j < n; j++)
            {
                num1 = temp[i]; // value of diagonal element
                num2 = mat[j]
                        [i]; // value of next row element

                // traversing every column of row
                // and multiplying to every row
                for (int k = 0; k < n; k++)
                {
                    // multiplying to make the diagonal
                    // element and next row element equal
                    mat[j][k] = (num1 * mat[j][k])
                            - (num2 * temp[k]);
                }
                total = total * num1; // Det(kA)=kDet(A);
            }
        }

        // multiplying the diagonal elements to get
        // determinant
        for (int i = 0; i < n; i++)
        {
            det = det * mat[i][i];
        }
        return (det / total); // Det(kA)/k=Det(A);
    }

    static int[][] swap(int[][] arr, int i1, int j1, int i2,
                        int j2)
    {
        int temp = arr[i1][j1];
        arr[i1][j1] = arr[i2][j2];
        arr[i2][j2] = temp;
        return arr;
    }



    /**
     * implementation D = [ I , -I ]
     *
     * Where I is an identity matrix
     *
     * @param dimension dimensionality
     * @return a positive spanning matrix
     */
    public static Matrix getD(int dimension){
        double[][] d = new double[dimension][dimension*2];
        for (int i = 0; i < dimension; i++) {
            d[i][i] = 1;
            d[i][i +(dimension)] = -1;
        }
        return new Matrix(d);
    }



    public Mesh update(Mesh m, int i){
        if(i==0)
            return m;
        if(i>0){

        }
        return null;
    }

    public double[] generateDirectionBl(double l){
        return null;
    }

    public void m(Matrix m){
        var col = m.col(3);
        var row =m.row(3);
    }
}
