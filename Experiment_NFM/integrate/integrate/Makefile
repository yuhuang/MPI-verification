# on a Mac:
RUN = mpiexec
FLAGS = -I/opt/local/include -L/opt/local/lib
MPICC = mpicc
CC = cc

all: spec  mw nb bad

spec: integrate_spec
	./integrate_spec > specout

mw: integrate_mw
	$(RUN) -n 4 ./integrate_mw > mwout

nb: integrate_nb
	$(RUN) -n 4 ./integrate_nb > nbout

bad: integrate_bad
	$(RUN) -n 4 ./integrate_bad > badout

integrate_spec: integrate_spec.c Makefile
	$(CC) -o integrate_spec $(FLAGS) integrate_spec.c

integrate_mw: integrate_mw.c Makefile
	$(MPICC) -o integrate_mw $(FLAGS) integrate_mw.c

integrate_nb: integrate_nb.c Makefile
	$(MPICC) -o integrate_nb $(FLAGS) integrate_nb.c

integrate_bad: integrate_bad.c Makefile
	$(MPICC) -o integrate_bad $(FLAGS) integrate_bad.c

clean:
	rm -f *~ integrate_spec integrate_mw integrate_nb integrate_bad specout mwout nbout badout
