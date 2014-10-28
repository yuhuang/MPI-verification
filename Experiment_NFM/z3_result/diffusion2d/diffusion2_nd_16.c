/* Simulation of diffusion equation.
 * By Andrew Siegel and Stephen Siegel
 */
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include "mpi.h"

#define SQUARE(x) ((x)*(x))
#define nprocsx 4   /* number of processes, x direction           */
#define nprocsy 4   /* number of processes, y direction           */
#define nxl     2   /* extent of x coordinates for local piece    */
#define nyl    2   /* extent of y coordinates for local piece    */
#define nxlg    6   /* nxl + 2 (ghost cells)                      */
#define nylg   6   /* nyl + 2 (ghost cells)                      */
#define nsteps  1   /* number of time steps                       */
#define D       0.1 /* Diffusion constant                         */
#define dt      1.0 /* time step                                  */
#define dx      1.0 /* distance between two lattice points        */
#define r       3.0 /* radius of initial circle in center         */

#define write_frame write_frame_2

int np, rank, xcoord, ycoord, up, down, left, right;
double u[nxlg][nylg];

int ssa_u[nxlg][nylg];
int c = 0;

void initdata(FILE* fp) { 
  int i, j; 
  double d;

  xcoord = rank % nprocsx; ycoord = rank / nprocsx;
  up = xcoord + nprocsx*((ycoord + 1)%nprocsy);
  down = xcoord + nprocsx*((ycoord + nprocsy - 1)%nprocsy);
  left = (xcoord + nprocsx - 1)%nprocsx + nprocsx*ycoord;
  right = (xcoord + 1)%nprocsx + nprocsx*ycoord;

  for (i = 1; i <= nxl; i++) 
    for (j = 1; j <= nyl; j++) { 
      d = SQUARE(1.0*nxl*xcoord+i-1 - (1.0*nprocsx*nxl - 1)/2)
        + SQUARE(1.0*nyl*ycoord+j-1 - (1.0*nprocsy*nyl - 1)/2);
      u[i][j] = (d < (1.0)*r*r ? 100 : 0);

	ssa_u[i][j] = 0;

fprintf(fp,"T%d_%d: assume(= u_%d_%d_%d_%d %d)\n", rank,c++,i,j,rank,ssa_u[i][j]++,(int)u[i][j]);

    }
} 


void write_frame_2(int time,FILE* fp) {
  int i, j, k, m, n, from;
  double buf[nxl];

  int ssa_buf[nylg];

  if (rank != 0) {
    for (j = 1; j <= nyl; j++) {
      for (i = 1; i <= nxl; i++) buf[i-1] = u[i][j];
   	ssa_buf[j] = 0;

	fprintf(fp,"T%d_%d: assume(= buf0_%d_%d_%d u_%d_%d_%d_%d)\n", rank,c++,j,rank,ssa_buf[j]++,1,j,rank,ssa_u[1][j]-1);
      MPI_Send(buf, nxl, MPI_DOUBLE, 0, j-1, MPI_COMM_WORLD);
 fprintf(fp, "T%d_%d: snd(%d, buf0_%d_%d_%d)\n", rank,c++,0, j,rank,ssa_buf[j]-1);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
		c++;
    }
  } else {
    MPI_Status status;
    char filename[50];
    FILE *file;
    int linelen = nprocsx*nxl*8+1; /* length of one line, incl. \n */
    int numrecvs = nyl*(nprocsx*nprocsy - 1);

    sprintf(filename, "./diffusion2_%d.out", time);
    file = fopen(filename, "w");
    assert(file);
    for (n = 0; n < nprocsy; n++)
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
    for (k = 0; k < numrecvs; k++) {
      int procx, procy, row;
      
      MPI_Recv(buf, nxl, MPI_DOUBLE, MPI_ANY_SOURCE, MPI_ANY_TAG,
               MPI_COMM_WORLD, &status);
 fprintf(fp, "T%d_%d: rcv(%d, buf0_1_%d_%d)\n", rank,c++,rank, rank, ssa_buf[1]++);
	    fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
	    c++;
      procx = status.MPI_SOURCE % nprocsx;
      procy = status.MPI_SOURCE / nprocsx;
      row = procy*nyl + status.MPI_TAG;
      fseek(file, row*linelen + procx*nxl*8, SEEK_SET);
      for (i = 1; i <= nxl; i++) fprintf(file, "%8.2f", buf[i-1]);
    }
    fclose(file);
  }
}

void write_frame_1(int time) {
  int i, j, k, m, n, from;
  double buf[nxl];

  if (rank != 0) {
    for (j = 1; j <= nyl; j++) {
      for (i = 1; i <= nxl; i++) buf[i-1] = u[i][j];
      MPI_Send(buf, nxl, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD);
    }
  } else {
    char filename[50];
    FILE *file;

    sprintf(filename, "./diffusion1_%d.out", time);
    file = fopen(filename, "w");
    assert(file);
    for (n = 0; n < nprocsy; n++)
      for (j = 1; j <= nyl; j++) {
        for (m = 0; m < nprocsx; m++) {
          from = n*nprocsx + m;
          if (from != 0)
            MPI_Recv(buf, nxl, MPI_DOUBLE, from, 0, MPI_COMM_WORLD,
                     MPI_STATUS_IGNORE);
          else
            for (i = 1; i <= nxl; i++) buf[i-1] = u[i][j];
          for (i = 1; i <= nxl; i++) fprintf(file, "%8.2f", buf[i-1]);
        }
        fprintf(file, "\n");
      }
    fclose(file);
  }
}

void exchange_ghost_cells(FILE* fp) {
  int i, j;
  double sbufx[nxl], rbufx[nxl], sbufy[nyl], rbufy[nyl];

  int ssa_sbufx = 0;
  int ssa_rbufx = 0;
  int ssa_sbufy = 0;
  int ssa_rbufy = 0;

  for (i = 1; i <= nxl; ++i) sbufx[i-1] = u[i][1];
  fprintf(fp,"T%d_%d: assume(= sbufx0_%d_%d u_%d_%d_%d_%d)\n", rank,c++,rank,ssa_sbufx++,1,1,rank,ssa_u[1][1]-1);
  MPI_Send(sbufx, nxl, MPI_DOUBLE, down, 0, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, sbufx0_%d_%d)\n", rank,c++,down, rank,ssa_sbufx-1);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
		c++;
  MPI_Recv(rbufx, nxl, MPI_DOUBLE, up, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  fprintf(fp, "T%d_%d: rcv(%d, rbufx0_%d_%d)\n", rank,c++,rank, rank, ssa_rbufx++);
	    fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
	    c++;

  for (i = 1; i <= nxl; ++i) u[i][nyl+1] = rbufx[i-1];
  fprintf(fp,"T%d_%d: assume(= u_%d_%d_%d_%d rbufx0_%d_%d)\n", rank,c++,1,nyl+1,rank,ssa_u[1][nyl+1]++,rank,ssa_rbufx-1);

    printf("%d: u[1][%d]: %d\n",rank, nyl+1,(int)u[1][nyl+1]);

  for (i = 1; i <= nxl; ++i) sbufx[i-1] = u[i][nyl];

fprintf(fp,"T%d_%d: assume(= sbufx0_%d_%d u_%d_%d_%d_%d)\n", rank,c++,rank,ssa_sbufx++,1,nyl,rank,ssa_u[1][nyl]-1);
  /* MPI_Sendrecv(sbufx, nxl, MPI_DOUBLE, up, 0, rbufx, nxl, */
  /*              MPI_DOUBLE, down, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE); */
  MPI_Send(sbufx, nxl, MPI_DOUBLE, up, 0, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, sbufx0_%d_%d)\n", rank,c++,up, rank,ssa_sbufx-1);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
		c++;
  MPI_Recv(rbufx, nxl, MPI_DOUBLE, down, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
fprintf(fp, "T%d_%d: rcv(%d, rbufx0_%d_%d)\n", rank,c++,rank, rank, ssa_rbufx++);
	    fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
	    c++;  


  for (i = 1; i <= nxl; ++i) u[i][0] = rbufx[i-1];
fprintf(fp,"T%d_%d: assume(= u_%d_%d_%d_%d rbufx0_%d_%d)\n", rank,c++,1,0,rank,ssa_u[1][0]++,rank,ssa_rbufx-1);

    printf("%d: u[1][0]: %d\n",rank,(int)u[1][0]);

  for (j = 1; j <= nyl; ++j) sbufy[j-1] = u[1][j];  
fprintf(fp,"T%d_%d: assume(= sbufy0_%d_%d u_%d_%d_%d_%d)\n", rank,c++,rank,ssa_sbufy++,1,1,rank,ssa_u[1][1]-1);
  /* MPI_Sendrecv(sbufy, nyl, MPI_DOUBLE, left, 0, rbufy, nyl, */
  /*              MPI_DOUBLE, right, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE); */
  MPI_Send(sbufy, nyl, MPI_DOUBLE, left, 0, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, sbufy0_%d_%d)\n", rank,c++,left, rank,ssa_sbufy-1);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
		c++;
  MPI_Recv(rbufy, nyl, MPI_DOUBLE, right, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  fprintf(fp, "T%d_%d: rcv(%d, rbufy0_%d_%d)\n", rank,c++,rank, rank, ssa_rbufy++);
	    fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
	    c++;  

  for (j = 1; j <= nyl; ++j) u[nxl+1][j] = rbufy[j-1];
fprintf(fp,"T%d_%d: assume(= u_%d_%d_%d_%d rbufy0_%d_%d)\n", rank,c++,nxl+1,1,rank,ssa_u[nxl+1][1]++,rank,ssa_rbufy-1);

    printf("%d: u[%d][1]: %d\n",rank, nxl+1,(int)u[nxl+1][1]);

  for (j = 1; j <= nyl; ++j) sbufy[j-1] = u[nxl][j];
fprintf(fp,"T%d_%d: assume(= sbufy0_%d_%d u_%d_%d_%d_%d)\n", rank,c++,rank,ssa_sbufy++,nxl,1,rank,ssa_u[nxl][1]-1);
  /* MPI_Sendrecv(sbufy, nyl, MPI_DOUBLE, right, 0, rbufy, nyl, */
  /*              MPI_DOUBLE, left, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE); */
  MPI_Send(sbufy, nyl, MPI_DOUBLE, right, 0, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, sbufy0_%d_%d)\n", rank,c++,right, rank,ssa_sbufy-1);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
		c++;
  MPI_Recv(rbufy, nyl, MPI_DOUBLE, left, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
fprintf(fp, "T%d_%d: rcv(%d, rbufy0_%d_%d)\n", rank,c++,rank, rank, ssa_rbufy++);
	    fprintf(fp, "T%d_%d: wait(T%d_%d)\n", rank, c, rank, c-1);
	    c++;

  for (j = 1; j <= nyl; ++j) u[0][j] = rbufy[j-1];
fprintf(fp,"T%d_%d: assume(= u_%d_%d_%d_%d rbufy0_%d_%d)\n", rank,c++,0,1,rank,ssa_u[0][1]++,rank,ssa_rbufy-1);


  printf("%d: u[0][1]: %d\n",rank, (int)u[0][1]);

  assert(u[0][1]==0 || u[0][1] == 100);

fprintf(fp,"T%d_%d: assert( or (= u_%d_%d_%d_%d 0) (= u_%d_%d_%d_%d 100))\n", rank,c++,0,1,rank,ssa_u[0][1]-1,0,1,rank,ssa_u[0][1]-1);
}


void update() {
  int i, j;
  double k = D*dt/(dx*dx);
  double u_new[nxlg][nylg];

  for (i = 1; i <= nxl; i++)
    for (j = 1; j <= nyl; j++)
      u_new[i][j] = u[i][j]
        + k*(u[i+1][j]+u[i-1][j]+u[i][j+1]+u[i][j-1]-4*u[i][j]);
  for (i = 1; i <= nxl; i++)
    for (j = 1; j <= nyl; j++) 
      u[i][j] = u_new[i][j];
}

int main(int argc,char *argv[]) {
  int iter;

  MPI_Init(&argc, &argv);

char address[100];
FILE* fp;

MPI_Comm_size(MPI_COMM_WORLD, &np);
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
int ssa_barrier = 0;

    sprintf(address,"%d.tr",rank);
    fp = fopen(address,"w");
    
  initdata(fp);

 

  //  MPI_Barrier(MPI_COMM_WORLD);
  write_frame(0,fp);
  MPI_Barrier(MPI_COMM_WORLD);
fprintf(fp,"T%d_%d: Barrier(%d)\n",rank,c++,ssa_barrier++);
  for (iter = 1; iter <= nsteps; iter++) {
    exchange_ghost_cells(fp);
    //MPI_Barrier(MPI_COMM_WORLD);
    update();
    //MPI_Barrier(MPI_COMM_WORLD); 
    // write_frame(iter);
    //MPI_Barrier(MPI_COMM_WORLD); 
  }
  MPI_Finalize();
}
