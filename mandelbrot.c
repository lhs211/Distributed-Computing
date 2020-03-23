/* Program to calculate the Mandlebrot set 
   D. Acreman January 2016 */

#include <stdio.h>
#include <complex.h>
#include <stdbool.h>
#include <math.h>

int main(){

  /* Declare variables */
  /* Loop indices for real and imaginary axes*/
  int i, j;
  /* Loop index for iteration*/
  int k;
  /* Maximum number of iterations*/
  const int maxIter=100;

  /* Number of points on real and imaginary axes*/
  const int nRe = 3000;
  const int nIm = 2000;

  /* Domain size */
  const float z_Re_min = -2.0;
  const float z_Re_max =  1.0;
  const float z_Im_min = -1.0;
  const float z_Im_max =  1.0;
  
  /* Real and imaginary components of z*/
  float z_Re, z_Im;
  /* Value of Z at current iteration*/
  float complex z;
  /*Value of z at iteration zero*/
  float complex z0;

  int nIter[nRe+1][nIm+1];

  const bool doIO = true; 

  printf("Starting calculation\n");

  /* Loop over real and imaginary axes */
  for (i=0; i<nRe+1; i++){
    for (j=0; j<nIm+1; j++){
      z_Re = ( ( (float) i)/ ( (float) nRe)) * (z_Re_max - z_Re_min) + z_Re_min;
      z_Im = ( ( (float) j)/ ( (float) nIm)) * (z_Im_max - z_Im_min) + z_Im_min;
      z0 = z_Re + z_Im*I;
      z  = z0;

      /* Iterate up to a maximum number or bail out if mod(z) > 2 */
      k=0;
      while(k<maxIter){
        nIter[i][j] = k;
	      if (cabs(z) > 2.0)
	        break;
	      z = z*z + z0;
	    k++;
      }
    }
  }
  
  /* Write out results */
  if (doIO){
      printf("Writing out results\n");
      FILE *outfile;
      outfile=fopen("mandelbrot.dat","w");

      for (i=0; i<nRe+1; i++){
          for (j=0; j<nIm+1; j++){
	            z_Re = ( ( (float) i)/ ( (float) nRe)) * (z_Re_max - z_Re_min) + z_Re_min;
	            z_Im = ( ( (float) j)/ ( (float) nIm)) * (z_Im_max - z_Im_min) + z_Im_min;
	            fprintf(outfile,"%f %f %d \n",z_Re, z_Im, nIter[i][j]);
          }
      }
      fclose(outfile);
  }
  return 0;

}
