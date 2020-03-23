import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class MandelbrotClient {

    private static final String REGISTER_ME_MSG = "register me";

    private static final int MAX_ITERATIONS = 200;
    private static final int NUM_REAL_POINTS = 3000;
    private static final int NUM_IMAGINARY_POINTS = 2000;

    private static final double REAL_MIN_Z = -2.0f;
    private static final double REAL_MAX_Z = 1.0f;
    private static final double IMAGINARY_MIN_Z = -1.0f;
    private static final double IMAGINARY_MAX_Z = 1.0f;

    private final String hostName;

    private double z0Real, z0Imaginary;
    private double zReal, zImaginary;
    private String content;

    public MandelbrotClient(String hostName) {
        this.hostName = hostName;
        this.content = REGISTER_ME_MSG;
    }

    public static void main(String args[]) {
        MandelbrotClient client = new MandelbrotClient("localhost");
        client.run();
    }

    public int compute(int i, int j) {
        int k = 0;

        z0Real = (((double) i) / ((double) NUM_REAL_POINTS)) * (REAL_MAX_Z - REAL_MIN_Z) + REAL_MIN_Z;
        z0Imaginary = (((double) j) / ((double) NUM_IMAGINARY_POINTS)) * (IMAGINARY_MAX_Z - IMAGINARY_MIN_Z) + IMAGINARY_MIN_Z;

        zReal = z0Real;
        zImaginary = z0Imaginary;

        while (k < MAX_ITERATIONS) {
            double zSquaredReal = (Math.pow(zReal, 2)) - (Math.pow(zImaginary, 2)); // Re(z^2)
            double zSquaredImaginary = (double) 2.0 * zReal * zImaginary; // Im (z^2)

            double magnitudeZSquaredSquared = (Math.pow(zSquaredReal, 2)) + (Math.pow(zSquaredImaginary, 2)); // | z^2|^2
            if (magnitudeZSquaredSquared > 4) {
                break; // Equivalent to |z^2|>2
            }

            zReal = zSquaredReal + z0Real; // update z to value at next iteration
            zImaginary = zSquaredImaginary + z0Imaginary;

            k++; // update iteration counter
        }

        System.out.println(k);
        return k;
    }

    public void run() {
        // arguments give message content and server hostname
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            byte[] m = content.getBytes();
            InetAddress aHost = InetAddress.getByName(hostname);
            int serverPort = 6789;

            // register client on server
            DatagramPacket request = new DatagramPacket(m, content.length(), aHost, serverPort);
            aSocket.send(request);

            // receive i,j values to calculate
            int[] data = {0, 0};
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);
            byte[] coords = byteBuffer.array();
            DatagramPacket reply = new DatagramPacket(coords, coords.length);
            aSocket.receive(reply);
            int[] ints = new int[reply.getData().length / 4];
            ByteBuffer.wrap(reply.getData()).asIntBuffer().get(ints);
            int i = ints[0];
            int j = ints[1];
            System.out.println("Reply: " + i + "+" + j);
            int k = compute(i, j);

            // send calculated k
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(k - 1);
            DatagramPacket result = new DatagramPacket(b.array(), b.array().length, aHost, serverPort);
            aSocket.send(result);

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) aSocket.close();
        }
    }
}