/* FEVS: A Functional Equivalence Verification Suite for High-Performance
 * Scientific Computing
 *
 * Copyright (C) 2010, Stephen F. Siegel, Timothy K. Zirkel,
 * University of Delaware
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */


#include<stdio.h>

#ifndef TILE_SIZE
#define TILE_SIZE 2
#endif

int main(int argc, char* argv[]) {
  int i, j, k, ii, jj, kk;
  int hi1, hi2, hi3;
  int L = atoi(argv[1]);
  int M = atoi(argv[2]);
  int N = atoi(argv[3]);
  double A[L][M];
  double B[M][N];
  double C[L][N];
  FILE *fp =fopen("data", "r");

  for (i = 0; i < L; i++)
    for (j = 0; j < M; j++)
      fscanf(fp,"%lf", &A[i][j]);
  for(i = 0; i < M; i++)
    for(j = 0; j < N; j++)
      fscanf(fp,"%lf",&B[i][j]);
  for (i = 0; i < L; i++)
    for (j = 0; j < N; j++)
      C[i][j] = 0.0;
  for (ii = 0; ii < L; ii+=TILE_SIZE) {
    for (jj = 0; jj < N; jj+=TILE_SIZE) {
      for (kk = 0; kk < M; kk+=TILE_SIZE) {
	hi1 = (ii + TILE_SIZE < L ? ii+TILE_SIZE : L);
        for (i = ii; i < hi1; i++) {
	  hi2 = (jj + TILE_SIZE < N ? jj + TILE_SIZE : N);
          for (j = jj; j < hi2; j++) {
	    hi3 = (kk + TILE_SIZE < M ? kk + TILE_SIZE : M);
            for (k = kk; k < hi3; k++)
              C[i][j] = C[i][j] + A[i][k] * B[k][j]; 
          }
        }
      }
    }
  }
  for (i = 0; i < L; i++) {
    for (j = 0; j < N; j++)
      printf("%f ", C[i][j]);
    printf("\n");
  }
  fclose(fp);
  return 0;
}
