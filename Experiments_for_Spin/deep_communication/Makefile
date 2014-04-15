# on a Mac:
RUN = mpiexec
FLAGS = -I/opt/local/include -L/opt/local/lib
MPICC = mpicc
CC = cc

deep: deep_spec
	$(RUN) -n 6 ./deep 

deep_spec: deep.c Makefile
	$(MPICC) -o deep $(FLAGS) deep.c

clean: rm -f *~ deep