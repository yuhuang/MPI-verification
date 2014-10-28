/* compute pi using Monte Carlo method */ 
#include <math.h> 
#include <stdio.h>
#include "mpi.h" 
#include "assert.h"
#define CHUNKSIZE      1000 
/* We'd like a value that gives the maximum value returned by the function 
   random, but no such value is *portable*.  RAND_MAX is available on many  
   systems but is not always the correct value for random (it isn't for  
   Solaris).  The value ((unsigned(1)<<31)-1) is common but not guaranteed */ 
#define INT_MAX 1000000000 
 
/* 
   svs: how to run monte carlo with isp?
        isp -n #procs ./monte.exe  -b 0.05[=error-tolerance]
*/


/* message tags */ 
#define REQUEST  1 
#define REPLY    2 
int main( int argc, char *argv[] ) 
{ 
    int iter; 
    int in, out, i, iters, max, ix, iy, ranks[1], done, temp; 
    double x, y, Pi, error, epsilon; 
    int numprocs, myid, server, totalin, totalout, workerid; 
    int rands[CHUNKSIZE], request; 
    MPI_Comm world, workers; 
    MPI_Group world_group, worker_group; 
    MPI_Status status; 
 
    MPI_Init(&argc,&argv); 
    world  = MPI_COMM_WORLD; 
    MPI_Comm_size(world,&numprocs); 
    MPI_Comm_rank(world,&myid); 
    server = numprocs-1;	/* last proc is server */ 

    int c = 0;
char address[100];
sprintf(address, "%d.tr",myid);
FILE* fp = fopen(address,"w");	
int ssa_barrier = 0;
int ssa_request = 0;
int ssa_rand = 0;

    if (myid == 0) 
      sscanf( argv[1], "%lf", &epsilon ); 
    MPI_Bcast( &epsilon, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD ); 
    
fprintf(fp,"T%d_%d: Barrier(%d)\n",myid,c++,ssa_barrier++);
    

    MPI_Comm_group( world, &world_group ); 
    ranks[0] = server; 
    MPI_Group_excl( world_group, 1, ranks, &worker_group ); 
    MPI_Comm_create( world, worker_group, &workers ); 
    MPI_Group_free(&worker_group); 
    if (myid == server) {	/* I am the rand server */ 
	do { 
	    MPI_Recv(&request, 1, MPI_INT, MPI_ANY_SOURCE, REQUEST, 
		     world, &status); 

	    fprintf(fp, "T%d_%d: rcv(%d, req_%d_%d)\n", myid,c++,myid, myid, ssa_request++);
	    fprintf(fp, "T%d_%d: wait(T%d_%d)\n", myid, c, myid, c-1);
	    c++;

	    if (request) { 
		
		assert(request>0);
		fprintf(fp, "T%d_%d: assert(> req_%d_%d 0)\n",myid,c++,myid,ssa_request-1);
		for (i = 0; i < CHUNKSIZE; ) { 
		        rands[i] = random(); 
			if (rands[i] <= INT_MAX) i++; 
		} 
		MPI_Send(rands, CHUNKSIZE, MPI_INT, 
                         status.MPI_SOURCE, REPLY, world); 

	        fprintf(fp, "T%d_%d: snd(%d, rand_%d_%d)\n", myid,c++,status.MPI_SOURCE, myid,ssa_rand);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", myid, c, myid, c-1);
		c++;
	    } 
	} while( request>0 ); 
	
    } 
    else {			/* I am a worker process */ 
        request = 1; 
	fprintf(fp,"T%d_%d: assume(= req_%d_%d 1)\n", myid,c++,myid,ssa_request++);
	done = in = out = 0; 
	max  = INT_MAX;         /* max int, for normalization */ 
        MPI_Send( &request, 1, MPI_INT, server, REQUEST, world ); 
 fprintf(fp, "T%d_%d: snd(%d, req_%d_%d)\n", myid,c++,server, myid,ssa_request-1);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", myid, c, myid, c-1);
		c++;
        MPI_Comm_rank( workers, &workerid ); 
	iter = 0; 
	while (!done) { 
	    iter++; 
	    request = 1; 
	fprintf(fp,"T%d_%d: assume(= req_%d_%d 1)\n", myid,c++,myid,ssa_request++);
	    MPI_Recv( rands, CHUNKSIZE, MPI_INT, server, REPLY, 
		     world, &status ); 
fprintf(fp, "T%d_%d: rcv(%d, rand_%d_%d)\n", myid,c++,myid, myid, ssa_rand++);
	    fprintf(fp, "T%d_%d: wait(T%d_%d)\n", myid, c, myid, c-1);
	    c++;
	    for (i=0; i<CHUNKSIZE-1; ) { 
	        x = (((double) rands[i++])/max) * 2 - 1; 
		y = (((double) rands[i++])/max) * 2 - 1; 
		if (x*x + y*y < 1.0) 
		    in++; 
		else 
		    out++; 
	    } 
	    MPI_Allreduce(&in, &totalin, 1, MPI_INT, MPI_SUM, 
			  workers); 
	    MPI_Allreduce(&out, &totalout, 1, MPI_INT, MPI_SUM, 
			  workers); 

fprintf(fp,"T%d_%d: Barrier(%d)\n",myid,c++,ssa_barrier++);

	    Pi = (4.0*totalin)/(totalin + totalout); 
	    error = fabs( Pi-3.141592653589793238462643); 
	    done = (error < epsilon || (totalin+totalout) > 1000000); 
	    request = (done) ? 0 : 1; 
	    if (myid == 0) { 
	      //printf( "\rpi = %23.20f", Pi ); 
		MPI_Send( &request, 1, MPI_INT, server, REQUEST, 
			 world ); 
fprintf(fp, "T%d_%d: snd(%d, req_%d_%d)\n", myid,c++,server, myid,ssa_request-1);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", myid, c, myid, c-1);
		c++;
	    } 
	    else { 
		if (request) {
			assert(request>0);
		    	fprintf(fp, "T%d_%d: assert(> req_%d_%d 0)\n",myid,c++,myid,ssa_request-1);
		    MPI_Send(&request, 1, MPI_INT, server, REQUEST, 
			     world);
		fprintf(fp, "T%d_%d: snd(%d, req_%d_%d)\n", myid,c++,server, myid,ssa_request-1);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", myid, c, myid, c-1);
		c++; 
		}
	    } 
	} 
	MPI_Comm_free(&workers); 
    } 
 
    if (myid == 0) { 
      //printf( "\npoints: %d\nin: %d, out: %d, <ret> to exit\n", 
      //	       totalin+totalout, totalin, totalout ); 
    } 
    MPI_Finalize(); 
} 
