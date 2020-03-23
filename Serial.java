import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

class Serial {
    public static void main(String args[]){

        /* Maximum number of iterations*/
        final int maxIter=200;

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

        int[][] nIter = new int[nRe+1][nIm+1];

        System.out.println("Starting calculation\n");

        /* Loop over real and imaginary axes */
        for (int i=0; i<nRe+1; i++){
            for (int j=0; j<nIm+1; j++){
                z0_Re = ( ( (float) i)/ ( (float) nRe)) * (z_Re_max - z_Re_min) + z_Re_min;
                z0_Im = ( ( (float) j)/ ( (float) nIm)) * (z_Im_max - z_Im_min) + z_Im_min;
                //z0 = z_Re + z_Im*I;
                //z  = z0;

                z_Re = z0_Re;
                z_Im = z0_Im;
                int k = 0;
                while (k < maxIter){
                    float z_sq_re = (z_Re*z_Re) - (z_Im*z_Im); // Re(z^2)
                    float z_sq_im = (float) 2.0 * z_Re * z_Im; // Im (z^2)
                    float mod_z_sq_sq = (z_sq_re * z_sq_re) + (z_sq_im * z_sq_im); // | z^2|^2
                    if ( mod_z_sq_sq > 4) break; // Equivalent to |z^2|>2

                    nIter[i][j] = k;

                    z_Re = z_sq_re + z0_Re; // update z to value at next iteration
                    z_Im = z_sq_im + z0_Im;

                    k++; // update iteration counter
                }
            }
        }

        System.out.println("Writing Results");

        try {
            PrintWriter writer = new PrintWriter("mandelbrot.dat", "UTF-8");
            String line;

            for (int i=0; i<nRe+1; i++){
                for (int j=0; j<nIm+1; j++){
                    z_Re = ( ( (float) i)/ ( (float) nRe)) * (z_Re_max - z_Re_min) + z_Re_min;
                    z_Im = ( ( (float) j)/ ( (float) nIm)) * (z_Im_max - z_Im_min) + z_Im_min;
                    line = z_Re + " " + z_Im + " " + nIter[i][j];
                    writer.println(line);
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