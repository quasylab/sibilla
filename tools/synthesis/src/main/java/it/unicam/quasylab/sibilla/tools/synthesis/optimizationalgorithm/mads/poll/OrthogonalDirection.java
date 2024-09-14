package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class OrthogonalDirection {

    private static final double EPSILON = 1e-9;
    private static final double GOLDEN_RATIO = (Math.sqrt(5) + 1) / 2;


    public int[][] generateOrthogonalBasis(int n, int t, int l) {
        double[] h = haltonSequence(n, t);
        double[] q = adjustedHalton(h, n, l);
        double[][] H = householderTransformation(q);
        int[][] orthogonalBasis = new int[H.length][H[0].length];
        for (int i = 0; i < orthogonalBasis.length; i++) {
            for (int j = 0; j < orthogonalBasis[i].length; j++) {
                orthogonalBasis[i][j] = (int)  Math.round(Math.round(Math.pow(norm(q), 2)) * H[i][j]);
            }
        }
        return getMaximalPositiveBasis(orthogonalBasis,n);
    }

    private double[][] householderTransformation(double[] q) {
        int n = q.length;
        double[][] H = new double[n][n];
        double normQ = norm(q);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    H[i][j] = 1 - 2 * q[i] * q[j] / (normQ * normQ);
                } else {
                    H[i][j] = -2 * q[i] * q[j] / (normQ * normQ);
                }
            }
        }
        return H;
    }

    private int[][] getMaximalPositiveBasis(int[][] H, int n) {
        int[][] basis = new int[n][2 * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                basis[i][j] = H[i][j]; // Place the original values
                basis[i][j + n] = -H[i][j]; // Place the negated values
            }
        }
        return basis;
    }

    private double[] haltonSequence(int n, int t) {
        int[] bases = getFirstNPrimes(n);
        double[] haltonSeq = new double[n];

        for (int i = 0; i < n; i++) {
            haltonSeq[i] = haltonEntry(bases[i], t);
        }

        return haltonSeq;
    }

    private double haltonEntry(int base, int t) {
        double u = 0.0;
        double f = 1.0 / base;
        int i = t;

        while (i > 0) {
            u += f * (i % base);
            i = i / base;
            f = f / base;
        }

        return u;
    }

    private double[] adjustedHalton(double[] halt, int n, int l) {
        double[] d = adjustedHaltonFamily(halt);

        if (l == 0) {
            // Ensure the vector qt,0 contains a unique nonzero coordinate equal to ±1
            double[] q = new double[n];
            int maxIndex = 0;
            for (int i = 1; i < d.length; i++) {
                if (Math.abs(d[i]) > Math.abs(d[maxIndex])) {
                    maxIndex = i;
                }
            }
            q[maxIndex] = Math.signum(d[maxIndex]);
            return q;
        }

        double alphaInitial = Math.pow(2, Math.abs(l) / 2.0) / Math.sqrt(n) - 0.5;

        double alpha = findOptimalAlphaEfficient(d, alphaInitial, Math.pow(2, Math.abs(l) / 2.0));
        return multiplyAndRound(d, alpha);
    }

    private double[] adjustedHaltonFamily(double[] halt) {
        return Arrays.stream(halt).map(v -> 2 * v - 1)
                .toArray();
    }

    private double findOptimalAlphaEfficient(double[] vector, double initial, double limit) {
        double a = 0;
        double b = initial;

        // Expand the search interval if necessary
        while (norm(multiplyAndRound(vector, b)) < limit) {
            a = b;
            b *= GOLDEN_RATIO;
        }

        double x = b - (b - a) / GOLDEN_RATIO;
        double y = a + (b - a) / GOLDEN_RATIO;

        while (Math.abs(x - y) > EPSILON) {
            if (norm(multiplyAndRound(vector, x)) < limit) {
                a = x;
            } else {
                b = y;
            }

            x = b - (b - a) / GOLDEN_RATIO;
            y = a + (b - a) / GOLDEN_RATIO;
        }

        double result = (b + a) / 2;
        return result;
    }



    private double findOptimalAlphaTest(double[] vector, double initial, double limit) {
        // Calculate the lower bound as per Lemma 3.2
        double lowerBound = Math.pow(2, Math.abs(initial) / 2) / Math.sqrt(vector.length) - 0.5;

        double normVector = norm(subtract(multiply(vector, 2), 1));
        double optimalAlpha = lowerBound;
        double currentNorm = 0;

        while (currentNorm <= limit) {
            double nextAlpha = Double.MAX_VALUE;
            for (int i = 0; i < vector.length; i++) {
                double denominator = Math.abs(2 * vector[i] - 1);
                if (denominator > EPSILON) {
                    double stepPoint = (Math.floor(optimalAlpha * 2 * denominator / normVector) + 1)
                            * normVector / (2 * denominator);
                    if (stepPoint > optimalAlpha && stepPoint < nextAlpha) {
                        nextAlpha = stepPoint;
                    }
                }
            }
            if (nextAlpha == Double.MAX_VALUE) break;

            currentNorm = norm(multiplyAndRound(vector, nextAlpha));
            if (currentNorm <= limit) {
                optimalAlpha = nextAlpha;
            } else {
                break;
            }
        }

        return optimalAlpha;
    }


    private double findOptimalAlpha(double[] vector, double initial, double limit) {

        // Calculate the lower bound as per Lemma 3.2
        double lowerBound = Math.pow(2, Math.abs(initial) / 2) / Math.sqrt(vector.length) - 0.5;

        // Initialize alpha with the lower bound
        double alpha = lowerBound;

        // Calculate the set of possible step points
        Set<Double> stepPoints = new TreeSet<>();
        for (int i = 0; i < vector.length; i++) {
            double denominator = Math.abs(2 * vector[i] - 1);
            if (denominator > EPSILON) {
                for (int j = 0; j < 1000; j++) { // Limit to prevent infinite loop
                    double stepPoint = (2 * j + 1) * norm(subtract(multiply(vector, 2), 1)) / (2 * denominator);
                    if (stepPoint > lowerBound) {
                        stepPoints.add(stepPoint);
                    }
                    if (stepPoint > limit) break;
                }
            }
        }

        // Search for the optimal alpha
        double optimalAlpha = lowerBound;
        for (double stepPoint : stepPoints) {
            if (norm(multiplyAndRound(vector, stepPoint)) <= limit) {
                optimalAlpha = stepPoint;
            } else {
                break;
            }
        }

        return optimalAlpha;
    }

    private double[] subtract(double[] vec, double value) {
        return Arrays.stream(vec).map(v -> v - value).toArray();
    }

    private double[] multiply(double[] vec, double value) {
        return Arrays.stream(vec).map(v -> v * value).toArray();
    }



    private double norm(double[] vec) {
        return Math.sqrt(Arrays.stream(vec).map(v -> v * v).sum());
    }

    private double[] multiplyAndRound(double[] d, double alpha) {
        return Arrays.stream(d).map(v -> Math.round(v * alpha)).toArray();
    }

    private int[] getFirstNPrimes(int n) {
        int[] primes = new int[n];
        int count = 0;
        int num = 2;

        while (count < n) {
            if (isPrime(num)) {
                primes[count] = num;
                count++;
            }
            num++;
        }

        return primes;
    }

    private boolean isPrime(int num) {
        if (num <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}