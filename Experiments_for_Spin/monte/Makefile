# on a Mac:
RUN = mpiexec
FLAGS = -I/opt/local/include -L/opt/local/lib
MPICC = mpicc
CC = cc

monte: monte_spec
	$(RUN) -n 4 ./monte 

monte_spec: monte.c Makefile
	$(MPICC) -o monte $(FLAGS) monte.c

clean: rm -f *~ monte