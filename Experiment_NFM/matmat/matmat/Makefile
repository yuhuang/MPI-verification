# on a Mac:
RUN = mpiexec
FLAGS = -I/opt/local/include -L/opt/local/lib
MPICC = mpicc
CC = cc

all: spec vec mw tile bad

spec: matmat_spec
	./matmat_spec 5 5 5 > specout

vec: matmat_vec
	./matmat_vec 5 5 5 > vecout

mw: matmat_mw
	$(RUN) -n 2 ./matmat_mw 5 5 5 > mwout

tile: matmat_tile
	./matmat_tile 5 5 5 > tileout

bad: matmat_bad
	$(RUN) -n 2 ./matmat_bad 5 5 5 > badout

matmat_spec: matmat_spec.c Makefile
	$(CC) -o matmat_spec $(FLAGS) matmat_spec.c

matmat_vec: matmat_vec.c Makefile
	$(CC) -o matmat_vec $(FLAGS) matmat_vec.c

matmat_mw: matmat_mw.c Makefile
	$(MPICC) -o matmat_mw $(FLAGS) matmat_mw.c

matmat_tile: matmat_tile.c Makefile
	$(CC) -o matmat_tile $(FLAGS) matmat_tile.c

matmat_bad: matmat_bad.c Makefile
	$(MPICC) -o matmat_bad $(FLAGS) matmat_bad.c

clean:
	rm -f *~ matmat_spec matmat_vec matmat_mw matmat_tile matmat_bad specout vecout mwout tileout badout
