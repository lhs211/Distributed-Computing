import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic Manager for worker/manager using sockets.
 *
 * Client/server interaction is as follows:
 *
 * (1) The server sends a Tasks.Task object to client. (2) The client sends back
 * Task.TaskResult. (3) Repeat Steps (1) and (2) while there are more tasks to
 * assign. (4) When all tasks have been assigned, server sends a ShutdownTask
 * object to the client.
 */
public abstract class Manager {

    protected int port;
    protected SynchTaskQueue<? extends Tasks> taskQueue;
    protected ServerSocket serverSocket;
    protected volatile boolean emptyQueueShutDown = false;
    protected List<Thread> workerThreads = new LinkedList<Thread>();

    /**
     * Construct a manager instance
     * 
     * @param _port the port to host manager on
     */
    public Manager(int port) {
        this.port = port;
    }

    /**
     * Workers request tasks from the task queue
     */
    public void process() {

        /* thread can accept connections from other workers */
        Thread serverSocketThread = new Thread(new Runnable() {
            public void run() {
                try {
                    /* create the server socket */
                    serverSocket = new ServerSocket(port);
                    System.out.println("waiting for workers to request tasks");

                    /* accept connections until the main thread closes the serverSocket */
                    while (true) {
                        acceptConnection();
                    }
                } catch (IOException e) {
                    if (emptyQueueShutDown) {
                        System.out.println("Queue is empty, server socket closed");
                    } else {
                        System.err.println("error with server socket:\n\t" + e);
                        System.exit(1);
                    }
                }
            }
        });
        serverSocketThread.start();

        /* wait for task queue to become empty */
        System.out.println("waiting for task queue to become empty");
        taskQueueEmpty();

        /* close server socket to force shutdown of associated thread */
        try {
            emptyQueueShutDown = true;
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("error shutting down server socket:\n\t" + e);
        }
        waitForThreadToComplete(serverSocketThread);

        /* wait for worker threads to finish */
        System.out.println("waiting for worker threads to finish");
        for (Thread t : workerThreads) {
            waitForThreadToComplete(t);
        }

        System.out.printf("number of workers = %d\n", workerThreads.size());
    }

    /**
     * Handle connections from incoming clients.
     * 
     * @throws IOException
     */
    protected void acceptConnection() throws IOException {
        Socket socket = serverSocket.accept();

        synchronized (workerThreads) {
            int workerID = workerThreads.size();
            System.out.printf("connection from %s, worker %d\n", clientAddress(socket), workerID);
            Thread workerTalker = newWorkerTalker(taskQueue, workerID, socket);
            workerThreads.add(workerTalker);
            workerTalker.start();
        }
    }

    /**
     * Create a new thread containing a sub type of WorkerTalker.
     */
    protected abstract Thread newWorkerTalker(SynchTaskQueue<? extends Tasks> taskQueue, int workerID, Socket s);

    /**
     * Wait until the queue of tasks becomes empty.
     */
    protected void taskQueueEmpty() {
        synchronized (taskQueue) {
            while (!taskQueue.isEmpty()) {
                try {
                    taskQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Wait for the execution of a thread to complete.
     * 
     * @param thread
     */
    protected void waitForThreadToComplete(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the hostname of the given socket.
     */
    protected static String clientAddress(Socket socket) {
        return socket.getInetAddress().getHostName();
    }

    /**
     * Class for communicating with the Worker instances.
     */
    public abstract class WorkerTalker implements Runnable {
        private int myID;
        private Socket socket;
        private SynchTaskQueue<? extends Tasks> taskQueue;
        protected List<Tasks.TaskResult> results;

        /**
         * Constructor to make a WorkerTalker from a list of tasks.
         */
        public WorkerTalker(SynchTaskQueue<? extends Tasks> taskQueue, int workerID, Socket socket) {
            this.taskQueue = taskQueue;
            this.myID = workerID;
            this.socket = socket;
            results = new ArrayList<>();
        }

        /**
         * Collects the results from the worker.
         * 
         * @param taskResult an object storing the result calculated by the manager.
         */
        protected abstract void addResults(Tasks.TaskResult taskResult);

        /**
         * Send tasks to the worker.
         */
        public void run() {

            ObjectInputStream input = null;
            ObjectOutputStream output = null;

            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());

                output.writeObject(Integer.valueOf(myID));
                Tasks t;

                /* retrieve tasks from queue until no more */
                while ((t = taskQueue.get()) != null) {
                    /* send task to worker process, receive result */
                    output.writeObject(t);
                    addResults((Tasks.TaskResult) input.readObject());
                }

                /* shut things down */
                output.writeObject(new Manager.ShutdownTask());
                socket.shutdownOutput();
                output.close();
                input.close();
                socket.close();
            } catch (ClassNotFoundException e) {
                System.err.println("error in client " + myID + ":\n\t" + e);
            } catch (IOException e) {
                System.err.println("error in client " + myID + ":\n\t" + e);
            }
        }
    }

    /**
     * Class for shutdown task. (Anything that's serializable and not a
     * Tasks.MandelbrotTask would work.)
     */
    public static class ShutdownTask implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Class for storing a synchronised queue of tasks.
     * 
     * @param <T> parameterised to store any subclass T of Tasks
     */
    public class SynchTaskQueue<T extends Tasks> {
        private List<T> tasks;

        /**
         * Initialise with given tasks.
         * 
         * @param tasks the contents of the queue
         */
        public SynchTaskQueue(List<T> tasks) {
            this.tasks = tasks;
        }

        /**
         * Check whether the queue is empty.
         * 
         * @return {@code true} if empty, {@code false} otherwise.
         */
        public synchronized boolean isEmpty() {
            return tasks.isEmpty();
        }

        /**
         * Get a task from the queue.
         * 
         * @return the next task, return null if the queue is empty.
         */
        public synchronized Tasks get() {
            if (tasks.isEmpty()) {
                notifyAll();
                return null;
            } else {
                return tasks.remove(0);
            }
        }
    }
}
