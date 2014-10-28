#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <string.h>
#include "assert.h"

#include "mpi.h"

#define SIZE 10
#define PORTNUM 1000
#define NDIST 1.0
#define COLINDEX(a,b) (a==b)

/*
typedef struct{
	int src;
	double dest[SIZE];
}DistVect;
*/

int getNextNode(int i){
	if(i + 1 >= SIZE){
		return 0;
	}
	return i + 1;
	
}

int getPrevNode(int i){
	if(i - 1 < 0){
		return SIZE - 1;
	}
	return i - 1;
	
}


int main(int argc, char *argv[]){
	int numprocs;
	int nodenum;
	MPI_Status status;
	MPI_Request request;
	MPI_Init(&argc, &argv);
	MPI_Comm_rank(MPI_COMM_WORLD, &nodenum);
	MPI_Comm_size(MPI_COMM_WORLD, &numprocs);

	int nextNode = (nodenum+1)%numprocs;
	int prevNode;
	if(nodenum == 0)
	prevNode = numprocs-1;
	else prevNode = nodenum-1;
	//
	//double distTable[2][SIZE];
	int out;
	//double distVector[SIZE];
	/*int i;
	for(i = 0; i < SIZE; i++){
		distTable[0][i] = INFINITY;
		distTable[1][i] = INFINITY;
		out.dest[i] = INFINITY;	
	}*/
	//distTable[1][nodenum] = 0;
	/*out.src = nodenum;
	out.dest[nodenum] = 0;
	out.dest[nextNode] = 1;
	out.dest[prevNode] = 1;*/
	//int count  = 0;
	
	out = nodenum;

	int updated = 1;
	size_t size;
	int in = 0;
	int c = 0;
	int ite = 0;
	char address[100];
	sprintf(address, "%d.tr",nodenum);
	FILE* fp = fopen(address,"w");	

	int ssa_out = 0;
	int ssa_in = 0;

	//int count = 0;
	//while(count < 4){
		//printf("[%d]count:%d\n",nodenum,count);
		//if(updated){
			MPI_Send(&out, 1, MPI_BYTE, prevNode, 1, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, out_%d_%d)\n", nodenum,c++,prevNode, nodenum,ssa_out);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", nodenum, c, nodenum, c-1);
c++;
			MPI_Send(&out, 1, MPI_BYTE, nextNode, 1, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, out_%d_%d)\n", nodenum,c++,nextNode, nodenum,ssa_out);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", nodenum, c, nodenum, c-1);
c++;

			//printf("Node %d updated\n", nodenum);
		//}
		
		MPI_Irecv(&in, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &request);

		MPI_Wait(&request, &status);
fprintf(fp, "T%d_%d: rcv(%d, in_%d_%d)\n", nodenum,c++,nodenum, nodenum, ssa_in++);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", nodenum, c, nodenum, c-1);
c++;

		//printf("%d\n",in);
		assert(in<1000);
		fprintf(fp,"T%d_%d: assert(< in_%d_%d 1000)\n", nodenum,c++,nodenum,ssa_in-1);


		out = in + 100;
		fprintf(fp,"T%d_%d: assert(= out_%d_%d (+ in_%d_%d 1000))\n", nodenum,c++,nodenum,ssa_out++, nodenum, ssa_in-1);

			MPI_Send(&out, 1, MPI_BYTE, prevNode, 1, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, out_%d_%d)\n", nodenum,c++,prevNode, nodenum,ssa_out);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", nodenum, c, nodenum, c-1);
c++;
			MPI_Send(&out, 1, MPI_BYTE, nextNode, 1, MPI_COMM_WORLD);
fprintf(fp, "T%d_%d: snd(%d, out_%d_%d)\n", nodenum,c++,nextNode, nodenum,ssa_out);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", nodenum, c, nodenum, c-1);
c++;

			//printf("Node %d updated\n", nodenum);
		//}
		
		MPI_Irecv(&in, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &request);

		MPI_Wait(&request, &status);
fprintf(fp, "T%d_%d: rcv(%d, in_%d_%d)\n", nodenum,c++,nodenum, nodenum, ssa_in++);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", nodenum, c, nodenum, c-1);
c++;





		MPI_Irecv(&in, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &request);

		MPI_Wait(&request, &status);
fprintf(fp, "T%d_%d: rcv(%d, in_%d_%d)\n", nodenum,c++,nodenum, nodenum, ssa_in++);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", nodenum, c, nodenum, c-1);
c++;



MPI_Irecv(&in, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &request);
		int timeout = 0;
		MPI_Wait(&request, &status);
fprintf(fp, "T%d_%d: rcv(%d, in_%d_%d)\n", nodenum,c++,nodenum, nodenum, ssa_in++);
fprintf(fp, "T%d_%d: wait(T%d_%d)\n", nodenum, c, nodenum, c-1);
c++;

		//printf("%d\n",in);
		assert(in>=100);
		fprintf(fp,"T%d_%d: assert(>= in_%d_%d 1000)\n", nodenum,c++,nodenum,ssa_in-1);
		
//printf("[%d]recving complete\n", nodenum);
//		memcpy(&distTable[COLINDEX(in.src, nextNode)], &in.dest, sizeof(in.dest));

		//col = COLINDEX(in.src, nextNode);
		/*for(i = 0; i < SIZE; i++){
			if(i != nodenum){
				int col = (distTable[0][i] < distTable[1][i]) ? 0 : 1;
				if(out.dest[i] != distTable[col][i] + NDIST){
					out.dest[i] = distTable[col][i] + NDIST;
					updated = 1;
				}
				
			}
		}*/


		
//printf("[%d]countcomplete:%d\n",nodenum,count);
		//count++;
	//}
	MPI_Finalize();
	
}

