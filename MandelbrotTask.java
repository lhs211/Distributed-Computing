import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for creating specific MandelbrotTasks.
 */
public class MandelbrotTask extends Tasks implements Serializable {

    private static final long serialVersionUID = 1L;

    /* Number of points on real and imaginary axes */
    private final int NUM_REAL_POINTS;
    private final int NUM_IMAGINARY_POINTS;

    /* Maximum and minimum values for each axis */
    private final double REAL_MIN_Z;
    private final double REAL_MAX_Z;
    private final double IMAGINARY_MIN_Z;
    private final double IMAGINARY_MAX_Z;

    /* Maximum number of iterations for each grid point */
    private final int MAX_ITERATIONS;

    /* The value of the grid point */
    private static int k;

    /* The index of the row to calculate in this task */
    private int rowIndex;

    /**
     * 
     * @param rowIndex             the index of the row being calculated
     * @param MAX_ITERATIONS       the maximum number of iterations to perform at
     *                             each point
     * @param NUM_REAL_POINTS      the size of the real axis
     * @param NUM_IMAGINARY_POINTS the size of the imaginary axis
     * @param REAL_MIN_Z           the minimum point on the real axis
     * @param REAL_MAX_Z           the maximum point on the real axis
     * @param IMAGINARY_MIN_Z      the minium point on the imaginary axis
     * @param IMAGINARY_MAX_Z      the maximum point on the imaginary axis
     */
    public MandelbrotTask(int rowIndex, int MAX_ITERATIONS, int NUM_REAL_POINTS, int NUM_IMAGINARY_POINTS,
            double REAL_MIN_Z, double REAL_MAX_Z, double IMAGINARY_MIN_Z, double IMAGINARY_MAX_Z) {
        this.rowIndex = rowIndex;
        this.MAX_ITERATIONS = MAX_ITERATIONS;
        this.NUM_REAL_POINTS = NUM_REAL_POINTS;
        this.NUM_IMAGINARY_POINTS = NUM_IMAGINARY_POINTS;
        this.REAL_MIN_Z = REAL_MIN_Z;
        this.REAL_MAX_Z = REAL_MAX_Z;
        this.IMAGINARY_MIN_Z = IMAGINARY_MIN_Z;
        this.IMAGINARY_MAX_Z = IMAGINARY_MAX_Z;
    }

    /**
     * Execute the calculations for this mandelbrot task.
     * 
     * @return the result of the computation.
     */
    @Override
    public TaskResult execute() {
        int[] rowResult = new int[NUM_IMAGINARY_POINTS + 1];

        double z0Real, z0Imaginary;
        double zReal, zImaginary;

        for (int j = 0; j < NUM_IMAGINARY_POINTS + 1; j++) {
            z0Real = (((double) rowIndex) / ((double) NUM_REAL_POINTS)) * (REAL_MAX_Z - REAL_MIN_Z) + REAL_MIN_Z;
            z0Imaginary = (((double) j) / ((double) NUM_IMAGINARY_POINTS)) * (IMAGINARY_MAX_Z - IMAGINARY_MIN_Z)
                    + IMAGINARY_MIN_Z;

            zReal = z0Real;
            zImaginary = z0Imaginary;

            k = 0;

            while (k < MAX_ITERATIONS) {
                double zSquaredReal = (Math.pow(zReal, 2)) - (Math.pow(zImaginary, 2)); // Re(z^2)
                double zSquaredImaginary = (double) 2.0 * zReal * zImaginary; // Im (z^2)

                double magnitudeZSquaredSquared = (Math.pow(zSquaredReal, 2)) + (Math.pow(zSquaredImaginary, 2)); // |
                                                                                                                  // z^2|^2
                if (magnitudeZSquaredSquared > 4) {
                    break; // Equivalent to |z^2|>2
                }

                zReal = zSquaredReal + z0Real; // update z to value at next iteration
                zImaginary = zSquaredImaginary + z0Imaginary;

                k++; // update iteration counter
            }
            rowResult[j] = k;
        }
        return new MandelbrotTaskResult(rowIndex, rowResult);
    }

    /**
     * Builds a queue of MandelbrotTasks.
     */
    public static List<MandelbrotTask> makeTasks(int MAX_ITERATIONS, int NUM_REAL_POINTS, int NUM_IMAGINARY_POINTS,
            double REAL_MIN_Z, double REAL_MAX_Z, double IMAGINARY_MIN_Z, double IMAGINARY_MAX_Z) {
        List<MandelbrotTask> taskQueue = new ArrayList<>(NUM_REAL_POINTS + 1);
        for (int i = 0; i < NUM_REAL_POINTS; i++) {
            taskQueue.add(new MandelbrotTask(i, MAX_ITERATIONS, NUM_REAL_POINTS, NUM_IMAGINARY_POINTS, REAL_MIN_Z,
                    REAL_MAX_Z, IMAGINARY_MIN_Z, IMAGINARY_MAX_Z));
        }
        return taskQueue;
    }

    /**
     * Provides information about the task as a string.
     */
    @Override
    public String toString() {
        return "task with row index = " + rowIndex;
    }

    /**
     * A class for storing the result of a MandelbrotTask.
     */
    public static class MandelbrotTaskResult extends TaskResult {

        private static final long serialVersionUID = 1L;

        public final int[] rowResult;
        public final int rowIndex;

        /**
         * Constructs a new MandelbrotTaskResult.
         * 
         * @param row    the index of the row which was computed
         * @param result the result of the computation at the row given by the rowIndex
         */
        public MandelbrotTaskResult(int rowIndex, int[] result) {
            this.rowIndex = rowIndex;
            this.rowResult = result;
        }
    }
}