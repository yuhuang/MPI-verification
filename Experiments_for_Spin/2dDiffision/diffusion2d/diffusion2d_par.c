/* FEVS: A Functional Equivalence Verification Suite for High-Performance
 * Scientific Computing
 *
 * Copyright (C) 2007-2010, Andrew R. Siegel, Stephen F. Siegel,
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


/* diffusion_par.c: parallel 2d-diffusion simulation.
 * Author: Andrew R. Siegel
 * Modified by Stephen F. Siegel <siegel@cis.udel.edu>, August 2007
 * for EuroPVM/MPI 2007 tutorial
 */

#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "mpi.h"
#include "gd.h"
#define SQUARE(x) ((x)*(x))

/* Constants: the following should be defined at compilation:
 *
 *       D = diffusion constant
 *      dt = time step
 *      dx = distance between two lattice points
 *  radius = radius of initial circle in center
 *  nsteps = number of time steps
 *      nx = number of points in x direction
 *      ny = number of points in y direction
 * nprocsx = number of procs in the x direction
 * nprocsy = number of procs in the y direction
 *     nxl = horizontal extent of one process; divides nprocsx
 *     nyl = vertical extent of one processl; divides nprocsy
 */

/* Global variables */

int nx = -1;              /* number of discrete points including endpoints */
int ny = -1;			  /* number of rows of points, including endpoints */
double k = -1;            /* D*dt/(dx*dx) */
int nsteps = -1;          /* number of time steps */
int wstep = -1;			  /* write frame every this many time steps */
int nprocsx;
int nprocsy;
int nxl;
int nyl;
int nprocs;    /* number of processes */
int rank;      /* the rank of this process */
int xcoord;    /* the horizontal coordinate of this proc */
int ycoord;    /* the vertical coordinate of this proc */
int up;        /* rank of upper neighbor on torus */
int down;      /* rank of lower neighbor on torus */
int left;      /* rank of left neighbor on torus */
int right;     /* rank of right neighbor on torus */
double **u;//[nxl+2][nyl+2];  /* values of the discretized function */

void quit() {
  printf("Input file must have format:\n\n");
  printf("nx = <INTEGER>\n");
  printf("ny = <INTEGER>\n");
  printf("k = <DOUBLE>\n");
  printf("nsteps = <INTEGER>\n");
  printf("wstep = <INTEGER>\n");
  printf("<DOUBLE> <DOUBLE> ...\n\n");
  printf("where there are ny rows of nx doubles at the end.\n");
  fflush(stdout);
  exit(1);
}

void readint(FILE *file, char *keyword, int *ptr) {
  char buf[101];
  int value;
  int returnval;

  returnval = fscanf(file, "%100s", buf);
  if (returnval != 1) quit();
  if (strcmp(keyword, buf) != 0) quit();
  returnval = fscanf(file, "%10s", buf);
  if (returnval != 1) quit();
  if (strcmp("=", buf) != 0) quit();
  returnval = fscanf(file, "%d", ptr);
  if (returnval != 1) quit();
}

void readdouble(FILE *file, char *keyword, double *ptr) {
  char buf[101];
  int value;
  int returnval;

  returnval = fscanf(file, "%100s", buf);
  if (returnval != 1) quit();
  if (strcmp(keyword, buf) != 0) quit();
  returnval = fscanf(file, "%10s", buf);
  if (returnval != 1) quit();
  if (strcmp("=", buf) != 0) quit();
  returnval = fscanf(file, "%lf", ptr);
  if (returnval != 1) quit();
}

/* init: initializes global variables. */
void init(char* infilename,int* line,int* ite,FILE* fp_trace) {
  int i, j; 
  double *buf;   /* temporary buffer */
  FILE *infile = fopen(infilename, "r");

  
    
  if (rank == 0) {
    assert(infile);
    readint(infile, "nx", &nx);
    readint(infile, "ny", &ny);
    readdouble(infile, "k", &k);
    readint(infile, "nsteps", &nsteps);
    readint(infile, "wstep", &wstep);
    printf("Diffusion2d with k=%f, nx=%d, ny=%d, nsteps=%d, wstep=%d\n",
	   k, nx, ny, nsteps, wstep);
    fflush(stdout);
  }
  MPI_Bcast(&nx, 1, MPI_INT, 0, MPI_COMM_WORLD);
  MPI_Bcast(&ny, 1, MPI_INT, 0, MPI_COMM_WORLD);
  MPI_Bcast(&k, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  MPI_Bcast(&nsteps, 1, MPI_INT, 0, MPI_COMM_WORLD);
  MPI_Bcast(&wstep, 1, MPI_INT, 0, MPI_COMM_WORLD);
    //printing trace
//    fprintf(fp_trace,"T%d_%d: barrier(comm)\n",rank,(*line)++);
  nxl = nx/nprocsx;
  nyl = ny/nprocsy;
  assert(nprocs == nprocsx*nprocsy);
  assert(nx == nxl*nprocsx);
  assert(ny == nyl*nprocsy);
  xcoord = rank % nprocsx;
  ycoord = rank / nprocsx;
  up = xcoord + nprocsx*((ycoord + 1)%nprocsy);
  down = xcoord + nprocsx*((ycoord + nprocsy - 1)%nprocsy);
  left = (xcoord + nprocsx - 1)%nprocsx + nprocsx*ycoord;
  right = (xcoord + 1)%nprocsx + nprocsx*ycoord;
	u = malloc((nxl+2)*sizeof(double *));
	assert(u);
	for (i=0; i<nxl+2; i++) {
		u[i] = malloc((nyl+2)*sizeof(double*));
	}
	buf = (double*)malloc(nxl*sizeof(double));
	if(rank == 0){
		buf = (double*)malloc(nxl*sizeof(double));
		for (i = 0; i < ny; i++){
			for (j = 0; j < nx; j++) {
				if (fscanf(infile, "%lf", buf+(j%nxl)) != 1) quit();
				if (i < nyl && j < nxl) {
					printf("in the if, buf[%d] is %f\n", j%nxl, buf[j%nxl]);
					u[j+1][i+1] = buf[j%nxl];
					printf("set u[%d][%d]\n",j+1,i+1);
				}
				else if (j % nxl == 0 && (j > 0 || i != 0)) {
					printf("sending row %d to process %d\n", i%nyl, i/nyl);
					MPI_Send(buf, nxl, MPI_DOUBLE, (i / nyl)*nprocsx + j/nxl, i % nyl, MPI_COMM_WORLD);
                    //printing trace
//                    fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,(*line)++,(i / nyl)*nprocsx + j/nxl,(int)*buf);
//                    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//                    (*line)++;
				}
			}
			printf("Done line %d\n", i);
		}
	}
	else {
		for (i = 0; i < nyl; i++) {
			MPI_Recv(buf, nxl, MPI_DOUBLE, 0, i, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
//            //printing trace,need to trace deterministic receive
//            fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,(*line)++,rank,rank,(*ite)++);
//            fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//            (*line)++;
			for (j = 0; j < nxl; j++)
				u[j+1][i] = buf[j];
		}
	}
	printf("finished init\n");
}

//#ifndef WRITE2
///* write_frame: writes current values of u to file */
//void write_frame(int time,int* line,int* ite,FILE* fp_trace) {
//  double buf[nxl];
//  int i, j;
//
//  if (rank != 0) {
//    for (j = 1; j <= nyl; j++) {
//      for (i = 1; i <= nxl; i++) buf[i-1] = u[i][j];
//      MPI_Send(buf, nxl, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD);
////        //printing trace
////        fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,(*line)++,0,(int)*buf);
////        fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
////        (*line)++;
//    }
//  } else {
//    int k, m, n, from;
//    char filename[50];
//    FILE *file = NULL;
//
//    sprintf(filename, "./parout/out_%d", time);
//    file = fopen(filename, "w");
//    assert(file);
//    for (n = 0; n < nprocsy; n++) {
//      for (j = 1; j <= nyl; j++) {
//	for (m = 0; m < nprocsx; m++) {
//	  from = n*nprocsx + m;
//        if (from != 0){
//	    MPI_Recv(buf, nxl, MPI_DOUBLE, from, 0, MPI_COMM_WORLD,
//                 MPI_STATUS_IGNORE);
//            //printing trace,need to trace deterministic receive
////            fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,(*line)++,rank,rank,(*ite)++);
////            fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
////            (*line)++;
//        }
//	  else
//	    for (i = 1; i <= nxl; i++) buf[i-1] = u[i][j];
//	  for (i = 1; i <= nxl; i++) fprintf(file, "%8.2f", buf[i-1]);
//	}
//	fprintf(file, "\n");
//      }
//    }
//    fclose(file);
//  }
//}
//#else
/* write_frame: writes current values of u to file */
void write_frame(int time,int* line,int* ite,FILE* fp_trace) {
  int i, j;
  double buf[nxl];

  if (rank != 0) {
    for (j = 1; j <= nyl; j++) {
      for (i = 1; i <= nxl; i++) buf[i-1] = u[i][j];
      MPI_Send(buf, nxl, MPI_DOUBLE, 0, j-1, MPI_COMM_WORLD);
        //printing trace
        fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,(*line)++,0,(int)*buf);
        fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
        (*line)++;
    }
  } else {
    MPI_Status status;
    char filename[50];
    FILE *file;
    int linelen = nprocsx*nxl*8+1; /* length of one line, incl. \n */
    int k, m, n, from;

    sprintf(filename, "./parout/out_%d", time);
    file = fopen(filename, "w");
    assert(file);
    for (n = 0; n < nprocsy; n++) {
      for (j = 1; j <= nyl; j++) {
	for (m = 0; m < nprocsx; m++) {
	  from = n*nprocsx + m;
	  if (from != 0)
	    for (i = 1; i <= nxl; i++) fprintf(file, "        ");
	  else
	    for (i = 1; i <= nxl; i++) fprintf(file, "%8.2f", u[i][j]);
	}
	fprintf(file, "\n");
      }
    }
    for (k = 0; k < nyl*(nprocs - 1); k++) {
      int procx, procy, row;

      MPI_Recv(buf, nxl, MPI_DOUBLE, MPI_ANY_SOURCE, MPI_ANY_TAG,
	       MPI_COMM_WORLD, &status);
        //printing trace
        fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,(*line)++,rank,rank,(*ite)++);
        fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
        (*line)++;
        
      procx = status.MPI_SOURCE % nprocsx;
      procy = status.MPI_SOURCE / nprocsx;
      row = procy*nyl + status.MPI_TAG;
      fseek(file, row*linelen + procx*nxl*8, SEEK_SET);
      for (i = 1; i <= nxl; i++) fprintf(file, "%8.2f", buf[i-1]);
    }
    fclose(file);
  }
}
//#endif

/* exchange_ghost_cells: updates ghost cells using MPI communication */
void exchange_ghost_cells(int* line,int* ite,FILE* fp_trace) {
  int i, j;
  double sbufx[nxl], rbufx[nxl], sbufy[nyl], rbufy[nyl];

  for (i = 1; i <= nxl; ++i) sbufx[i-1] = u[i][1];
  MPI_Sendrecv(sbufx, nxl, MPI_DOUBLE, down, 0, rbufx, nxl,
	       MPI_DOUBLE, up, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
//    //printing trace
//    fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,(*line)++,down,(int)sbufx[0]);
//    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//    (*line)++;
//    fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,(*line)++,rank,rank,(*ite)++);
//    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//    (*line)++;
  for (i = 1; i <= nxl; ++i) u[i][nyl+1] = rbufx[i-1];
  for (i = 1; i <= nxl; ++i) sbufx[i-1] = u[i][nyl];
  MPI_Sendrecv(sbufx, nxl, MPI_DOUBLE, up, 0, rbufx, nxl,
	       MPI_DOUBLE, down, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
//    //printing trace
//    fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,(*line)++,up,(int)sbufx[0]);
//    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//    (*line)++;
//    fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,(*line)++,rank,rank,(*ite)++);
//    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//    (*line)++;
  for (i = 1; i <= nxl; ++i) u[i][0] = rbufx[i-1];
  for (j = 1; j <= nyl; ++j) sbufy[j-1] = u[1][j];  
  MPI_Sendrecv(sbufy, nyl, MPI_DOUBLE, left, 0, rbufy, nyl,
	       MPI_DOUBLE, right, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
//    //printing trace
//    fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,(*line)++,left,(int)sbufx[0]);
//    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//    (*line)++;
//    fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,(*line)++,rank,rank,(*ite)++);
//    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//    (*line)++;
  for (j = 1; j <= nyl; ++j) u[nxl+1][j] = rbufy[j-1];
  for (j = 1; j <= nyl; ++j) sbufy[j-1] = u[nxl][j];
  MPI_Sendrecv(sbufy, nyl, MPI_DOUBLE, right, 0, rbufy, nyl,
	       MPI_DOUBLE, left, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
//    //printing trace
//    fprintf(fp_trace,"T%d_%d: snd(%d, %d)\n",rank,(*line)++,right,(int)sbufx[0]);
//    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//    (*line)++;
//    fprintf(fp_trace,"T%d_%d: rcv(%d, rB_%d_%d)\n",rank,(*line)++,rank,rank,(*ite)++);
//    fprintf(fp_trace,"T%d_%d: wait(T%d_%d)\n",rank,*line,rank,(*line)-1);
//    (*line)++;
  for (j = 1; j <= nyl; ++j) u[0][j] = rbufy[j-1];
}

/* update: updates u.  Uses ghost cells.  Purely local operation. */
void update() {
  int i, j;
  double u_new[nxl+2][nyl+2];

  for (i = 1; i <= nxl; i++)
    for (j = 1; j <= nyl; j++)
     u_new[i][j] = u[i][j]
       + k*(u[i+1][j]+u[i-1][j]+u[i][j+1]+u[i][j-1]-4*u[i][j]);
  for (i = 1; i <= nxl; i++)
    for (j = 1; j <= nyl; j++) 
      u[i][j] = u_new[i][j];
}

/* main: executes simulation, creates one output file for each time
 * step */
int main(int argc,char *argv[]) {
  int iter;

  assert(argc==4);
  nprocsx = atoi(argv[2]);
  nprocsy = atoi(argv[3]);
    
   
    
  MPI_Init(&argc, &argv);
    //used for printing trace
    
    int lineint = 0;
    int iteint =0;
    int* line = &lineint;
    int* ite = &iteint;
    int comm = 0;
    
    
    FILE* fp_trace = NULL;
    
    MPI_Comm_size(MPI_COMM_WORLD, &nprocs);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    
    //used for printing trace
    char address[100];
    sprintf(address,"%d.tr",rank);
    fp_trace = fopen(address,"w");
    
  init(argv[1],line,ite,fp_trace);
  write_frame(0,line,ite,fp_trace);
    fprintf(fp_trace,"T%d_%d: Barrier(%d)\n",rank,(*line)++,comm++);
  for (iter = 1; iter <= nsteps; iter++) {
    exchange_ghost_cells(line,ite,fp_trace);
//      fprintf(fp_trace,trace);
//      trace = "";
    update();
write_frame(iter,line,ite,fp_trace);
      fprintf(fp_trace,"T%d_%d: Barrier(%d)\n",rank,(*line)++,comm++);
  }
  MPI_Finalize();
  return 0;
}
