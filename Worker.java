import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Generic Worker for worker/master versions using sockets.
 */
public class Worker {
    // variables/constants
    private String host;
    private int port;
    private static final String usageMessage = "parameters:  host portNum";

    /**
     * Construct a new worker instance.
     * 
     * @param host the address of the master
     * @param port the port number of the master
     */
    public Worker(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Main method.
     */
    public static void main(String[] args) {

        String host = null;
        int port = 0;

        host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println(usageMessage);
            System.exit(1);
        }

        new Worker(host, port).process();
    }

    /**
     * Execute the tasks given by the manager, writing the results as
     * Tasks.TaskResult
     */
    protected void runTasks(ObjectInputStream input, ObjectOutputStream output, int myID)
            throws IOException, ClassNotFoundException {

        /* run while there are tasks to process */
        Object fromManager = null;
        while ((fromManager = input.readObject()) instanceof Tasks) {
            Tasks task = (Tasks) fromManager;
            Tasks.TaskResult taskResult = task.execute();
            System.out.printf("worker %d received %s\n", myID, task.toString());
            output.writeObject(taskResult);
        }
    }

    /**
     * Connect to the master process and receive tasks to compute.
     */
    public void process() {

        Socket socket = null;
        ObjectInputStream input = null;
        ObjectOutputStream output = null;

        try {
            /* connect to master process */
            socket = new Socket(host, port);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());

            /* receive worker ID */
            int myID = (Integer) input.readObject();

            System.out.printf("worker %d starting\n", myID);

            runTasks(input, output, myID);

            /* shut things down */
            socket.shutdownOutput();
            output.close();
            input.close();
            socket.close();
            System.out.printf("worker %d done\n", myID);
        } catch (ClassNotFoundException e) {
            System.err.println("worker error:\n\t" + e);
        } catch (IOException e) {
            System.err.println("worker error:\n\t" + e);
        }
    }
}
