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


#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>

#define comm MPI_COMM_WORLD

int N, L, M;


void printMatrix(int numRows, int numCols, double *m) {
  int i, j;
  for (i = 0; i < numRows; i++) {
    for (j = 0; j < numCols; j++)
      printf("%f ", m[i*numCols + j]);
    printf("\n");
  }
  printf("\n");
  fflush(stdout);
}

void vecmat(double vector[L], double matrix[L][M], double result[M]) {
  int j, k;
  for (j = 0; j < M; j++)
    for (k = 0, result[j] = 0.0; k < L; k++)
      result[j] += vector[k]*matrix[k][j];
}

int main(int argc, char *argv[]) {
  int rank, nprocs, i, j;
  MPI_Status status;
  
  N = atoi(argv[1]);
  L = atoi(argv[2]);
  M = atoi(argv[3]);
  MPI_Init(&argc, &argv);
  MPI_Comm_rank(comm, &rank);
  MPI_Comm_size(comm, &nprocs);
   
    //used for printing trace
    int line = 0;
    int ite = 0;
    char address[100];
    sprintf(address,"%d.tr",rank);
    
    FILE* fp_trace = fopen(address,"w");
    
  if (rank == 0) {
    double a[N][L], b[L][M], c[N][M], tmp[M];
    int count;
    
    FILE *fp =fopen("data", "r");
    for (i = 0; i < N; i++)
      for (j = 0; j < L; j++)
	fscanf(fp,"%lf", &a[i][j]);
    for (i = 0; i < L; i++)
      for (j = 0; j < M; j++)
	fscanf(fp,"%lf",&b[i][j]);
    MPI_Bcast(b, L*M, MPI_DOUBLE, 0, comm);
      //printing trace
      fprintf(fp_trace,"T%d_%d: barrier(comm)\n",rank,line++);
      for (count = 0; count < nprocs-1 && count < N; count++){
          MPI_Send(&a[count][0], L, MPI_DOUBLE, count+1, count+1, comm);
          //printing trace
          fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,line++,count+1,(int)a[count][0]);
          fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,line,rank,line-1);
          line++;
      }
    for (i = 0; i < N; i++) {
      MPI_Recv(tmp, M, MPI_DOUBLE, MPI_ANY_SOURCE, MPI_ANY_TAG, comm, &status);
        //printing trace
        fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,line++,rank,rank,ite++);
        fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,line,rank,line-1);
        line++;
        
      for (j = 0; j < M; j++) c[status.MPI_TAG-1][j] = tmp[j];
      if (count < N) {
	MPI_Send(&a[count][0], L, MPI_DOUBLE, status.MPI_SOURCE, count+1, comm);
          //printing trace, what is mpi_source
          fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,line++,status.MPI_SOURCE,(int)a[count][0]);
          fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,line,rank,line-1);
          line++;
	count++;
      }
    }
      for (i = 1; i < nprocs; i++)
      {
          MPI_Send(NULL, 0, MPI_INT, i, 0, comm);
          //printing trace,-1 represents null
          fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,line++,i,-1);
          fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,line,rank,line-1);
          line++;
      }
    printMatrix(N, M, &c[0][0]);
    fclose(fp);
  } else {
    double b[L][M], in[L], out[M];

    MPI_Bcast(b, L*M, MPI_DOUBLE, 0, comm);
      //printing trace
      fprintf(fp_trace,"T%d_%d: barrier(comm)\n",rank,line++);
    while (1) {
      MPI_Recv(in, L, MPI_DOUBLE, 0, MPI_ANY_TAG, comm, &status);
        //printing trace
        fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,line++,rank,rank,ite++);
        fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,line,rank,line-1);
        line++;
      if (status.MPI_TAG == 0) break;
      vecmat(in, b, out);
      MPI_Send(out, M, MPI_DOUBLE, 0, status.MPI_TAG, comm);
        //printing trace,
        fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,line++,0,(int)out[0]);
        fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,line,rank,line-1);
        line++;
    }
  }
  MPI_Finalize();
  return 0;
}
