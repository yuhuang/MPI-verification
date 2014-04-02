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


#include<stdlib.h>
#include<stdio.h>
#include<assert.h>
#include<math.h>
#include<float.h>
#include<mpi.h>

#define WORK_TAG 1
#define TERMINATE_TAG 2
#define RESULT_TAG 3

double epsilon = DBL_MIN*10;
double pi = M_PI;
int count = 0;
int nprocs, rank, numWorkers, numTasks;
double subintervalLength, subtolerance;

double f(double x) { return sin(x); }

/* Sequential function to recursively estimate integral of f from a to
 * b.  fa=f(a), fb=f(b), area=given estimated integral of f from a to
 * b.  The interval [a,b] is divided in half and the areas of the two
 * pieces are estimated using Simpson's rule.  If the sum of those two
 * areas is within tolerance of given area, convergence has been
 * achieved and the result returned is the sum of the two areas.
 * Otherwise the function is called recursively on the two
 * subintervals and the sum of the results returned.
 */
double integrate_seq(double a, double b, double fa, double fb, double area,
		     double tolerance) {
  double delta = b - a;
  double c = a+delta/2;
  double fc = f(c);
  double leftArea = (fa+fc)*delta/4;
  double rightArea = (fc+fb)*delta/4;

  count++;
  if (tolerance < epsilon) {
    printf("Tolerance may not be possible to obtain.\n");
    return leftArea+rightArea;
  }
  if (fabs(leftArea+rightArea-area)<=tolerance) {
    return leftArea+rightArea;
  }
  return integrate_seq(a, c, fa, fc, leftArea, tolerance/2) +
    integrate_seq(c, b, fc, fb, rightArea, tolerance/2);
}

/* Sequential algorithm to estimate integral of f from a to b within
 * given tolerance. */
double integral_seq(double a, double b, double tolerance) {
  count = 0;
  return integrate_seq(a, b, f(a), f(b), (f(a)+f(b))*(b-a)/2, tolerance);
}

/* Worker function: called by procs of non-0 rank only.  Accepts
 * task from manager.   Each task is represented by a single integer
 * i, corresponding to the i-th subinterval of [a,b].  The task is
 * to compute the integral of f(x) over that subinterval within
 * tolerance of subtolerance.   At the end, a Bcast is used to
 * get final answer from manager. */
double worker(double a) {
  double left, right, answer;
  int taskCount = 0, curr = 0, next = 1;
  double results[2];
  int tasks[2];
  MPI_Request recv_reqs[2], send_req = MPI_REQUEST_NULL;
  MPI_Status status;

  // Start waiting for current task
  MPI_Irecv(&tasks[curr], 1, MPI_INT, 0, MPI_ANY_TAG, MPI_COMM_WORLD, &recv_reqs[curr]);
  while (1) {
    // Block until we get our current task
    MPI_Wait(&recv_reqs[curr], &status);

    next = !curr;
    // Start waiting for next task
    MPI_Irecv(&tasks[next], 1, MPI_INT, 0, MPI_ANY_TAG, MPI_COMM_WORLD, &recv_reqs[next]);

    if (status.MPI_TAG == WORK_TAG) {
      taskCount++;
      left = a+tasks[curr]*subintervalLength;
      right = left+subintervalLength;
      results[curr] = integral_seq(left, right, subtolerance);

      // Make sure that our last send (if any) completed before starting next one
      MPI_Wait(&send_req, MPI_STATUS_IGNORE);

      // Send results asynchronously back to the root
      MPI_Isend(&results[curr], 1, MPI_DOUBLE, 0, RESULT_TAG, MPI_COMM_WORLD, &send_req);
    } else {
      break;
    }
    // The "next" task from iteration i is the "current" task for iteration i+1
    curr = next;
  }

  MPI_Bcast(&answer, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  return answer;
}

/* manager: this function called by proc 0 only.  Distributes tasks
 * to workers and accumulates results.   Each task is represented
 * by an integer i.  The integer i represents a subinterval
 * of the interval [a,b].   Returns the final result.  */
double manager() {
  int i, task = 0, workers = nprocs-1, src = 0;
  MPI_Status status;
  MPI_Request* sends = (MPI_Request*) malloc(sizeof(sends[0]) * workers);
  MPI_Request* recvs = (MPI_Request*) malloc(sizeof(recvs[0]) * workers);
  double result, answer = 0.0;
  int* tasks = (int*) malloc(sizeof(tasks[0]) * workers);
  double* results = (double*) malloc(sizeof(results[0]) * workers);
  int some_completed, completedWorkers = 0;

  for (i=1; i<nprocs; i++) {
    int t = i-1;
    task = tasks[t] = t;
    MPI_Isend(&tasks[t], 1, MPI_INT, i, WORK_TAG, MPI_COMM_WORLD, &sends[t]);
    MPI_Irecv(&results[t], 1, MPI_DOUBLE, i, RESULT_TAG, MPI_COMM_WORLD, &recvs[t]);
  }
  while (++task < numTasks || completedWorkers < workers) {
    // Wait for one worker to finish
    MPI_Waitany(workers, recvs, &src, &status);
    answer += results[src];
    MPI_Wait(&sends[src], MPI_STATUS_IGNORE);
    if (task < numTasks) {
      // Send a new task to that worker
      tasks[src] = task;
      // src corresponds to worker src+1
      MPI_Isend(&tasks[src], 1, MPI_INT, src+1, WORK_TAG, MPI_COMM_WORLD, &sends[src]);

      // And, start listening for a response
      MPI_Irecv(&results[src], 1, MPI_DOUBLE, src+1, RESULT_TAG, MPI_COMM_WORLD, &recvs[src]);
    } else {
      // All tasks are distributed, so tell the worker to finish
      MPI_Isend(NULL, 0, MPI_INT, src+1, TERMINATE_TAG, MPI_COMM_WORLD, &sends[src]);
      ++completedWorkers;
    }
  }
  // Make sure all termination sends have completed
  MPI_Waitall(workers, sends, MPI_STATUSES_IGNORE);
  MPI_Bcast(&answer, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD);
  free(sends); free(recvs); free(results); free(tasks);
  return answer;
}

/* called in collective fashion */
double integral(double a, double b, double tolerance) {
  MPI_Comm_size(MPI_COMM_WORLD, &nprocs);
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  numWorkers = nprocs - 1;
  numTasks = numWorkers*100;
  subintervalLength = (b-a)/numTasks;
  subtolerance = tolerance/numTasks;
  if (rank == 0) {
    return manager();
  } else {
    return worker(a);
  }
}

int main(int argc, char *argv[]) {
  double result;
  int rank;

  MPI_Init(&argc, &argv);
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  result = integral(0, pi, .0000000001);
  MPI_Finalize();
  if (rank == 0) {  
    printf("%4.20lf\n", result);
  }
  return 0;
}
