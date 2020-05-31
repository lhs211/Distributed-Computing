import java.io.Serializable;

/**
 * Generic Tasks class to be sent from master to worker.
 */
public abstract class Tasks {

    /**
     * Provides information about the task as a string
     */
    public abstract String toString();

    public abstract TaskResult execute();

    /**
     * Class for task results. Serializable so they can be sent over sockets.
     */
    public static abstract class TaskResult implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
