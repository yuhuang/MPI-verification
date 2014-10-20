# RUN = mpiexec -machinefile ~/mpd.hosts
RUN = mpiexec
FLAGS = -I/opt/local/include -L/opt/local/lib -lgd


######################### Sequential Version #########################
spec1: spec1x
	rm -f specout/out*
	./spec1x config1.txt

spec1x: diffusion2d_spec.c Makefile
	cc -o spec1x  $(FLAGS) -DK=0.1 -DM=100 -DNSTEPS=15000 \
          -DWSTEP=150 -DNX=200 -DPWIDTH=2 diffusion2d_spec.c

spec2: spec2x
	rm -f specout/out*
	./spec2x config2.txt

spec2x: diffusion2d_spec.c Makefile
	cc -o spec2x  $(FLAGS) -DDEBUG -DK=0.1 -DM=100 -DNSTEPS=150 \
          -DWSTEP=10 -DNX=10 -DPWIDTH=2 diffusion2d_spec.c

######################### Parallel Version #########################

par1: par1x
	rm -f parout/out*
	$(RUN) -n 4 ./par1x config1.txt 2 2

par1x: diffusion2d_par.c Makefile
	mpicc -o par1x  $(FLAGS) -DK=0.1 -DM=100 -DNSTEPS=15000 \
-DWSTEP=150 -DNX=200 -DPWIDTH=2 diffusion2d_par.c

par2: par2x
	rm -f parout/out*
	$(RUN) -n 4 ./par2x config2.txt 2 2

par2x: diffusion2d_par.c Makefile
	mpicc -o par2x  $(FLAGS) -DDEBUG -DK=0.1 -DM=100 -DNSTEPS=150 \
-DWSTEP=10 -DNX=10 -DPWIDTH=2 diffusion2d_par.c

########################### Bad Version #############################
bad1: bad1x
	rm -f badout/out*
	./bad1x config1.txt

bad1x: diffusion2d_bad.c Makefile
	cc -o bad1x  $(FLAGS) -DK=0.1 -DM=100 -DNSTEPS=15000 \
          -DWSTEP=150 -DNX=200 -DPWIDTH=2 diffusion2d_bad.c

bad2: bad2x
	rm -f badout/out*
	./bad2x config2.txt

bad2x: diffusion2d_bad.c Makefile
	cc -o bad2x  $(FLAGS) -DDEBUG -DK=0.1 -DM=100 -DNSTEPS=150 \
          -DWSTEP=10 -DNX=10 -DPWIDTH=2 diffusion2d_bad.c


clean: clean_spec clean_par clean_bad

clean_spec:
	rm -f spec*x *.o *~ specout/out*

clean_par:
	rm -f par*x *.o *~ parout/out*

clean_bad: 
	rm -f bad*x *.o *~ badout/out*
