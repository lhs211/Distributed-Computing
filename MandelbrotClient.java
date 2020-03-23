import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.io.*;

public class MandelbrotClient{
    public static void main(String args[]){
        // arguments give message content and server hostname
        DatagramSocket aSocket=null;
        try{
            String hostname = "localhost";
            String content = "register me";

            aSocket = new DatagramSocket();
            byte[] m = content.getBytes();
            InetAddress aHost = InetAddress.getByName(hostname);
            int serverPort = 6789;

            // register client on server
            DatagramPacket request = new DatagramPacket(m, content.length(), aHost, serverPort);
            aSocket.send(request);

            // receive i,j values to calculate
            int[] data = { 0,0 };
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
            int k = MandelbrotIter(i,j);

            // send calculated k
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(k-1);
            DatagramPacket result = new DatagramPacket(b.array(), b.array().length, aHost, serverPort);
            aSocket.send(result);

        } catch (SocketException e) {System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {System.out.println("IO: " + e.getMessage());
        } finally {if (aSocket != null) aSocket.close();}
    }

    public static int MandelbrotIter(int i, int j){
        int k = 0;
        int maxIter = 200;

        /* Number of points on real and imaginary axes*/
        final int nRe = 3000;
        final int nIm = 2000;

        /* Domain size */
        final float z_Re_min = -2.0f;
        final float z_Re_max =  1.0f;
        final float z_Im_min = -1.0f;
        final float z_Im_max =  1.0f;
          
        /* Real and imaginary components of z at iteration 0*/
        float z0_Re, z0_Im;
        /* Real and imaginary components of z */
        float z_Re, z_Im;

        z0_Re = ( ( (float) i)/ ( (float) nRe)) * (z_Re_max - z_Re_min) + z_Re_min;
        z0_Im = ( ( (float) j)/ ( (float) nIm)) * (z_Im_max - z_Im_min) + z_Im_min;
        z_Re = z0_Re;
        z_Im = z0_Im;

        while (k < maxIter){
            float z_sq_re = (z_Re*z_Re) - (z_Im*z_Im); // Re(z^2)
            float z_sq_im = (float) 2.0 * z_Re * z_Im; // Im (z^2)
            float mod_z_sq_sq = (z_sq_re * z_sq_re) + (z_sq_im * z_sq_im); // | z^2|^2
            if ( mod_z_sq_sq > 4) break; // Equivalent to |z^2|>2
            z_Re = z_sq_re + z0_Re; // update z to value at next iteration
            z_Im = z_sq_im + z0_Im;
            k++; // update iteration counter
        }
        System.out.println(k);
        return k;
    }
}