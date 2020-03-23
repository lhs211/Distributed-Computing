import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Serial {

    /* Maximum number of iterations*/
    private static final int MAX_ITERATIONS = 200;

    /* Number of points on real and imaginary axes*/
    private static final int NUM_REAL_POINTS = 3000;
    private static final int NUM_IMAGINARY_POINTS = 2000;

    /* Domain size */
    private static final double REAL_MIN_Z = -2.0f;
    private static final double REAL_MAX_Z = 1.0f;
    private static final double IMAGINARY_MIN_Z = -1.0f;
    private static final double IMAGINARY_MAX_Z = 1.0f;

    private static final String SPACE = " ";

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(1);

    /* Real and imaginary components of z at iteration 0 */
    private double z0Real, z0Imaginary;

    /* Real and imaginary components of z */
    private double zReal, zImaginary;

    private int[][] numIterations;

    public Serial() {
        this.numIterations = new int[NUM_REAL_POINTS + 1][NUM_IMAGINARY_POINTS + 1];
    }

    public static void main(String args[]) {
        Serial serial = new Serial();
        serial.run();
    }

    public void run() {
        Future<?> calculations = SERVICE.submit(() -> serial);
        
        calculations.get();

        writeResults();
    }

    public void startCalculations() {
        System.out.println("Starting calculation\n");

        /* Loop over real and imaginary axes */
        for (int i = 0; i < nRe + 1; i++) {
            for (int j = 0; j < nIm + 1; j++) {
                //z0 = z_Re + z_Im*I;
                //z  = z0;
                z0Real = (((float) i) / ((float) NUM_REAL_POINTS)) * (REAL_MAX_Z - REAL_MIN_Z) + REAL_MIN_Z;
                z0Imaginary = (((float) j) / ((float) NUM_IMAGINARY_POINTS)) * (IMAGINARY_MAX_Z - IMAGINARY_MIN_Z) + IMAGINARY_MIN_Z;

                zReal = z0Real;
                zImaginary = z0Imaginary;

                for (int k = 0; k < MAX_ITERATIONS; k++) {
                    double zSquaredReal = (Math.pow(zReal, 2)) - (Math.pow(zImaginary, 2)); // Re(z^2)
                    double zSquaredImaginary = (double) 2.0 * zReal * zImaginary; // Im (z^2)

                    double magnitudeZSquaredSquared = (Math.pow(zSquaredReal, 2)) + (Math.pow(zSquaredImaginary, 2)); // | z^2|^2
                    if (magnitudeZSquaredSquared > 4) {
                        break; // Equivalent to |z^2|>2
                    }

                    numIterations[i][j] = k;

                    zReal = zSquaredReal + z0Real; // update z to value at next iteration
                    zImaginary = zSquaredImaginary + z0Imaginary;
                }
            }
        }
    }

    public void writeResults() {
        System.out.println("Writing Results");

        try {
            PrintWriter writer = new PrintWriter("mandelbrot.dat", "UTF-8");

            for (int i = 0; i < NUM_REAL_POINTS + 1; i++) {
                for (int j = 0; j < NUM_IMAGINARY_POINTS + 1; j++) {
                    zReal = (((double) i) / ((double) NUM_REAL_POINTS)) * (REAL_MAX_Z - REAL_MIN_Z) + REAL_MIN_Z;
                    zImaginary = (((double) j) / ((double) NUM_IMAGINARY_POINTS)) * (IMAGINARY_MAX_Z - IMAGINARY_MIN_Z) + IMAGINARY_MIN_Z;

                    writer.println(zReal + SPACE + zImaginary + SPACE + numIterations[i][j]);
                }
            }

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}