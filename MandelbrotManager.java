import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * Class for hosting a Mandelbrot Manager. Contains the parameters for the
 * calculation. Stores the end result. Writes the end result to a file.
 */
public class MandelbrotManager extends Manager {
    /* Number of points on real and imaginary axes */
    private static final int NUM_REAL_POINTS = 3000;
    private static final int NUM_IMAGINARY_POINTS = 2000;

    /* Maximum and minimum values for each axis */
    private static final double REAL_MIN_Z = -2.0f;
    private static final double REAL_MAX_Z = 1.0f;
    private static final double IMAGINARY_MIN_Z = -1.0f;
    private static final double IMAGINARY_MAX_Z = 1.0f;

    /* The calculated grid points */
    private final int[][] mandelbrotGrid = new int[NUM_REAL_POINTS + 1][NUM_IMAGINARY_POINTS + 1];

    /* Maximum number of iterations for each grid point */
    private static final int MAX_ITERATIONS = 200;

    /* Real and imaginary components of z */
    private double zReal, zImaginary;

    /* Constant string values */
    private static final String SPACE = " ";
    private static final String USAGE_MESSAGE = "parameters: portNum";
    private static final String FILENAME = "mandelbrot.dat";
    private static final String ENCODING = "UTF-8";

    /**
     * Construct a Mandelbrot manager instance.
     * 
     * @param port the port number to host on.
     */
    public MandelbrotManager(int port) {
        super(port);
    }

    /**
     * Main Method
     */
    public static void main(String[] args) {
        int port = 0;

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println(USAGE_MESSAGE);
            System.exit(1);
        }

        MandelbrotManager manager = new MandelbrotManager(port);
        manager.createTaskQueue();
        manager.process();
        manager.writeResults();
    }

    /**
     * Build a queue of tasks for processing by workers.
     */
    protected void createTaskQueue() {
        taskQueue = new SynchTaskQueue<>(MandelbrotTask.makeTasks(MAX_ITERATIONS, NUM_REAL_POINTS, NUM_IMAGINARY_POINTS,
                REAL_MIN_Z, REAL_MAX_Z, IMAGINARY_MIN_Z, IMAGINARY_MAX_Z));
    }

    /**
     * Write out the results of the calculation to a ".dat" file.
     */
    public void writeResults() {
        System.out.println("Writing Results...");

        try {
            final PrintWriter writer = new PrintWriter(FILENAME, ENCODING);

            for (int i = 0; i < NUM_REAL_POINTS + 1; i++) {
                for (int j = 0; j < NUM_IMAGINARY_POINTS + 1; j++) {
                    zReal = (((double) i) / ((double) NUM_REAL_POINTS)) * (REAL_MAX_Z - REAL_MIN_Z) + REAL_MIN_Z;
                    zImaginary = (((double) j) / ((double) NUM_IMAGINARY_POINTS)) * (IMAGINARY_MAX_Z - IMAGINARY_MIN_Z)
                            + IMAGINARY_MIN_Z;

                    writer.println(zReal + SPACE + zImaginary + SPACE + mandelbrotGrid[i][j]);
                }
            }

            writer.close();
            System.out.println("Results written successfully!");
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create an instance of a thread containing a MandelbrotWorkerTalker.
     */
    @Override
    protected Thread newWorkerTalker(SynchTaskQueue<? extends Tasks> taskQueue, int workerID, Socket socket) {
        return new Thread(new MandelbrotWorkerTalker(taskQueue, workerID, socket));
    }

    /**
     * Class for communicating with MandelbrotWorker instances.
     */
    protected class MandelbrotWorkerTalker extends WorkerTalker {

        /**
         * Construct a new MandelbrotWorkerTalker.
         */
        public MandelbrotWorkerTalker(SynchTaskQueue<? extends Tasks> taskQueue, int workerID, Socket socket) {
            super(taskQueue, workerID, socket);
        }

        /**
         * Add the results of a task result to the array[][] of overall points.
         */
        @Override
        public void addResults(Tasks.TaskResult taskResult) {
            MandelbrotTask.MandelbrotTaskResult MTaskResult = (MandelbrotTask.MandelbrotTaskResult) taskResult;
            mandelbrotGrid[MTaskResult.rowIndex] = MTaskResult.rowResult;
        }
    }
}