# obtain y min max values
set terminal unknown
plot DATA using ($1/STEPSIZE):5:6 with yerrorbars pointtype 2 pointsize 0.2
Y0_MIN=GPVAL_Y_MIN
Y0_MAX=GPVAL_Y_MAX

# obtain y min max values
set logscale y
plot DATA using ($1/STEPSIZE):13 with line
Y1_MIN=GPVAL_Y_MIN
Y1_MAX=GPVAL_Y_MAX
unset logscale y

set term TERM size 800,600
set out IMG

set datafile missing "-"
set lmargin 10
set ytics nomirror
unset key

set style line 1 pointtype 2 pointsize 0.2 linecolor 1	#estimates
set style line 2 linecolor 2							#true value
set style line 3 linetype 0 linecolor 3					#join
set style line 4 linetype 0 linecolor 6					#leave
set style line 5 linecolor 10							#variation
set style line 6 linecolor 6							#rmse

set multiplot layout KEY+2, 1 title TITLE

#plot estimates and true value
unset xtics
set xrange [0:STEPS]
set bmargin 0.5

plot DATA using ($1/STEPSIZE):5:6 with yerrorbars linestyle 1, \
     DATA using ($1/STEPSIZE):14 with line, \
     DATA using ($1/STEPSIZE):($15*Y0_MAX) with impulses linestyle 3, \
     DATA using ($1/STEPSIZE):($15*Y0_MIN) with impulses linestyle 3, \
     DATA using ($1/STEPSIZE):($16*Y0_MAX) with impulses linestyle 4, \
     DATA using ($1/STEPSIZE):($16*Y0_MIN) with impulses linestyle 4, \
     DATA using ($1/STEPSIZE):17 axes x1y2 with impulses linestyle 5
	
#plot rmse
set xlabel "steps (time/stepsize)"
set xrange [0:STEPS]
set xtics auto
set logscale y
set format y "%.0e"
set tmargin 0.5
set bmargin 3

plot DATA using ($1/STEPSIZE):13 with line linestyle 6, \
     DATA using ($1/STEPSIZE):($15*Y1_MAX) with impulses linestyle 3, \
     DATA using ($1/STEPSIZE):($15*Y1_MIN) with impulses linestyle 3, \
     DATA using ($1/STEPSIZE):($16*Y1_MAX) with impulses linestyle 4, \
     DATA using ($1/STEPSIZE):($16*Y1_MIN) with impulses linestyle 4, \
     DATA using ($1/STEPSIZE):17 axes x1y2 with impulses linestyle 5 

#plot key
set key center right
unset tics
unset xlabel
unset ylabel
unset logscale
set yrange [0:1]

plot DATA using (2):(2):(2) with yerrorbars linestyle 1 title "estimates (mean ± std)", \
     2 linestyle 2 title "true mean", \
     2 linestyle 6 title "rmse", \
     2 linestyle 3 title "node joining", \
     2 linestyle 4 title "node leaving", \
     2 linestyle 5 title "value variation"

unset multiplot
set term pop
