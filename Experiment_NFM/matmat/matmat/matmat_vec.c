#include <stdlib.h>
#include <stdio.h>

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
  int i, j, k;
  N = atoi(argv[1]);
  L = atoi(argv[2]);
  M = atoi(argv[3]);
  double a[N][L], b[L][M], c[N][M];

  FILE *fp =fopen("data", "r");
  for (i = 0; i < N; i++)
    for (j = 0; j < L; j++)
      fscanf(fp,"%lf", &a[i][j]);
  for (i = 0; i < L; i++)
    for (j = 0; j < M; j++)
      fscanf(fp,"%lf",&b[i][j]);
  for (i = 0; i < N; i++)
    vecmat(a[i], b, c[i]);
  printMatrix(N, M, &c[0][0]);
  fclose(fp);
  return 0;
}
