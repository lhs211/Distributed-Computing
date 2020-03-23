import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class MandelbrotServer {
    public static void main(String args[]) {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(6789);
            byte[] buffer = new byte[1000];
            while (true) {
                // register client
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                // send coords
                int[] data = {1000, 1000};
                ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
                IntBuffer intBuffer = byteBuffer.asIntBuffer();
                intBuffer.put(data[0]);
                intBuffer.put(data[1]);
                byte[] coords = byteBuffer.array();
                DatagramPacket toCalculate = new DatagramPacket(coords, coords.length, request.getAddress(), request.getPort());
                aSocket.send(toCalculate);

                // receive result
                DatagramPacket reply = new DatagramPacket(coords, coords.length);
                aSocket.receive(reply);
                int[] ints = new int[reply.getData().length / 4];
                ByteBuffer.wrap(reply.getData()).asIntBuffer().get(ints);
                int k = ints[0];
                System.out.println("Reply: " + k);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) aSocket.close();
        }
    }
}