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

/* integral_seq.c: numerical integration of function of a single variable.
 * Algorithm works on list of "interval" structures.  Each interval specifies
 * a little interval [a,b] the stores the values (b-a)*f(a) (left area), 
 * (b-a)*f(b) (right area), and the desired tolerance for that interval.
 * If abs(leftArea-rightArea)<=tolerance, the interval is considered "converged"
 * and the estimated integral of the interval is returned as the average of
 * leftArea and rightArea.  If the the interval is not converged, it is split,
 * i.e., replaced by two intervals by cutting [a,b] in half, and cutting
 * the tolerance in half. 
 *  */
#include<stdlib.h>
#include<stdio.h>
#include<assert.h>
#include<math.h>
#include<float.h>

double epsilon = DBL_MIN*10;
double pi = M_PI;
int count = 0;

double f1(double x) { return sin(x); }

double f2(double x) { return cos(x); }

double integrate(double a, double b, double fa, double fb, double area,
		 double tolerance, double f(double)) {
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
  return integrate(a, c, fa, fc, leftArea, tolerance/2, f) +
    integrate(c, b, fc, fb, rightArea, tolerance/2, f);
}

double integral(double a, double b, double tolerance, double f(double)) {
  count = 0;
  return integrate(a, b, f(a), f(b), (f(a)+f(b))*(b-a)/2, tolerance, f);
}

int main(int argc, char *argv[]) {
  printf("%4.20lf\n", integral(0, pi, .0000000001, f1));
  printf("%4.20lf\n", integral(0, pi, .0000000001, f2));
  return 0;
}
