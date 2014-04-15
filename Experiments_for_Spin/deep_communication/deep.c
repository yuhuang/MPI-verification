#include "mpi.h"
#include <stdlib.h>
#include <stdio.h>

#define THREADS 5
#define LOOP 80

int main (int argc, char *argv[])
{
	
	int myrank, numprocs;
	int* message_r = (int*)malloc(sizeof(int));
	int* message_s = (int*)malloc(sizeof(int));
	
	MPI_Status status;
	MPI_Request request;
	MPI_Init(&argc, &argv);
	MPI_Comm_rank(MPI_COMM_WORLD, &myrank);
	MPI_Comm_size(MPI_COMM_WORLD, &numprocs);	
	
	char address[100];
	sprintf(address, "%d.tr",myrank);
	FILE* fp = fopen(address,"w");
	int c = 0;
	int ite = 0;
	int i =0;

	if(numprocs != THREADS) {
        if(myrank == 0) {
            fprintf(stderr, "This test requires exactly %d processes\n", THREADS);
        }

        MPI_Finalize();

        return EXIT_FAILURE;
    }
	
	*message_r = 0;
	*message_s = 0;
   if(myrank ==0){
		
		
for(i = 0; i<LOOP;i++){
		MPI_Irecv(message_r, 10000, MPI_INT, MPI_ANY_SOURCE, 123, MPI_COMM_WORLD, &request);
		
		MPI_Wait(&request, &status);
    char var_check[20];
    sprintf(var_check,"m_%d_%d", myrank,ite);
fprintf(fp, "T%d_%d: rcv(%d, m_%d_%d)\n", myrank, c++, myrank, myrank, ite++);
		fprintf(fp, "T%d_%d: wait(T%d_%d)\n", myrank, c, myrank, c-1);
		c++;
    fprintf(fp, "T%d_%d: assert(= %s %d)\n", myrank, c++, var_check,*message_r);
		}
	}
	else {
      for(i = 0; i<LOOP/(numprocs-1);i++){
		*message_s = 2000+2*i;
		MPI_Send(message_s, 1, MPI_INT, 0, 123, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, %d)\n", myrank,c++,0, *message_s);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", myrank, c, myrank, c-1);
		c++;
		}
	}
	
		MPI_Finalize();
		fclose(fp);
	return 0;
}
