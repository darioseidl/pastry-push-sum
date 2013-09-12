Pastry-Push-Sum
================================================================================

Pastry-Push-Sum is an implementation of the Push-Sum [1] algorithm as a 
FreePastry [2] application.

Push-Sum is a gossip-based protocol for calculating aggregates, such as the 
mean, among all nodes in the network. It is fully decentralized, based on 
exchanging messages with randomly selected neighbors. It requires no 
knowledge about the network topology and supports nodes joining the network 
and nodes changing their value at any time. To achieve a high convergence 
speed, the nodes must be able to send to any other nodes in the network.

For that we use the Pastry [3] protocol to construct an overlay network in 
which messages are routed to any destination. The nodes send their messages 
to randomly generated IDs and the messages will be forwarded to the node 
with the numerically closest ID.

In the case of nodes leaving the network, the Push-Sum algorithm is not able 
to determine the mean of only the available nodes, excluding the nodes 
that left the network. In order to estimate the true mean after a node left, 
the algorithm must be reset. All nodes must reset their values at the same 
time. We use multicasts to notify all nodes to periodically reset their 
values. FreePastry supports multicast via Scribe [4].


Usage
--------------------------------------------------------------------------------

You can obtain the source of this project from the
[Github repository](https://github.com/darioseidl/pastry-push-sum).

The Javadocs can be accessed at the 
[Github pages](http://darioseidl.github.io/pastry-push-sum/doc/).

The project comes with two main classes, PPSPeer and PPSSimulator. The 
PPSPeer class is a proof of concept application to estimates the mean value 
among all participating nodes in the ring. To join the ring, all but the 
first peer, must know the address of a node already in the ring. The nodes 
will immediately and continuously estimate the average value among all 
nodes. In the current implementation the node values are simulated. To 
plug-in an actual data source, the nodes obtain their values from a 
ValueReader interface. A data source must provide an implementation of the 
ValueReader and ValueReaderFactory interfaces to feed the nodes with data 
values.    

The PPSSimulator is used to study the behavior of the implementation in the 
Pastry simulator under different parameters. The output is suitable for 
being plotted with Gnuplot.

You can import the project in Eclipse or run the applications using the 
following commands:

For PPSPeer

	java -classpath ./bin:./lib/freepastry-2.1/FreePastry-2.1.jar:./lib/jcommander-1.30/jcommander-1.30.jar:./lib/commons-io-2.4/commons-io-2.4.jar:./lib/commons-lang3-3.1/commons-lang3-3.1.jar:./lib/commons-math3-3.2/commons-math3-3.2.jar univie.cs.pps.PPSPeer -h

For PPSSimulator

	java -classpath ./bin:./lib/freepastry-2.1/FreePastry-2.1.jar:./lib/jcommander-1.30/jcommander-1.30.jar:./lib/commons-io-2.4/commons-io-2.4.jar:./lib/commons-lang3-3.1/commons-lang3-3.1.jar:./lib/commons-math3-3.2/commons-math3-3.2.jar univie.cs.pps.PPSSimulator -h

In both cases, the use of the -h argument will print a list of all available 
arguments.


The project also provides an ant file to build and run the applications.
Note that for ant to work, the JAVA_HOME environment variable needs to be 
set to a JDK 6 or higher.

To see a list of all available targets, use

	ant -projecthelp

To build the project and to generate the javadocs, use

	ant build, docs

An example simulation can be started with

	ant run-simulator

An example of three peers running in parallel, bound to localhost:9001, 
localhost:9002 and localhost:9003, can be started with

	ant run-three-peers

With Gnuplot installed and the binary in your path, the following command 
will run several simulations and plot the results:

	ant all-plots
	
If you have Gnuplot installed, but the binary is not in your path, you can 
specify the location in the gnuplot_bin ant property. For example

	ant -Dgnuplot_bin="C:\Program Files (x86)\gnuplot\bin\gnuplot.exe" all-plots



References
--------------------------------------------------------------------------------

[1] D. Kempe, A. Dobra and J. Gehrke, "Gossip-based computation of aggregate 
information," Foundations of Computer Science, 2003. Proceedings. 44th 
Annual IEEE Symposium on , vol., no., pp.482,491, 11-14 Oct. 2003

[2] http://www.freepastry.org/

[3] A. Rowstron and P. Druschel, "Pastry: Scalable, distributed object 
location and routing for large-scale peer-to-peer systems". IFIP/ACM 
International Conference on Distributed Systems Platforms (Middleware), 
Heidelberg, Germany, pages 329-350, November, 2001. 

[4] http://www.freepastry.org/SCRIBE/default.htm
