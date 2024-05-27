package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import java.util.Arrays;
@SuppressWarnings("unused")
public class OrthogonalDirection {



    public double[][] generateOrthogonalBasis(int n, int t, int l) {
        double[] h = haltonSequence(n, t);
        double[] q = adjustedHalton(h, n, l);
        double[][] H = householderTransformation(q);
        return formBasisMatrix(H, n);
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

    private double[][] formBasisMatrix(double[][] H, int n) {
        double[][] basis = new double[2 * n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                basis[i][j] = H[i][j];
                basis[i + n][j] = -H[i][j];
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
        double alpha = findOptimalAlpha(d, alphaInitial, Math.pow(2, Math.abs(l) / 2.0));
        return multiplyAndRound(d, alpha);
    }

    private double[] adjustedHaltonFamily(double[] halt) {
        return Arrays.stream(halt).map(v -> 2 * v - 1)
                .toArray();
    }

    private double findOptimalAlpha(double[] d, double initial, double limit) {
        double alpha = initial;
        double step = 0.1;

        while (norm(multiplyAndRound(d, alpha)) < limit) {
            alpha += step;
        }

        return alpha - step; // Return the previous value as the optimal one
    }

    private double norm(double[] vec) {
        return Math.sqrt(Arrays.stream(vec).map(v -> v * v).sum());
    }

    private double[] multiplyAndRound(double[] vec, double alpha) {
        double norm = norm(vec);
        return Arrays.stream(vec)
                .map(v -> Math.round(alpha * (v / norm)))
                .toArray();
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
