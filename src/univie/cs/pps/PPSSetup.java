/*
 * Copyright (c) 2013 Faculty of Computer Science, University of Vienna
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package univie.cs.pps;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import rice.environment.Environment;
import rice.p2p.commonapi.rawserialization.RawMessage;
import rice.pastry.JoinFailedException;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.direct.DirectNodeHandle;
import rice.pastry.direct.DirectPastryNodeFactory;
import rice.pastry.direct.EuclideanNetwork;
import rice.pastry.direct.NetworkSimulator;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.selector.TimerTask;
import univie.cs.pps.utils.FormattedStatistics;
import univie.cs.pps.utils.GaussianValueReader;
import univie.cs.pps.utils.ValueReader;
import univie.cs.pps.utils.ValueReaderFactory;

/**
 * The PPSSetup class is used by {@link PPSSimulator} and {@link PPSPeer} to
 * create the pastry environment, boot the nodes, and observe the results.
 * 
 * @author Dario Seidl
 * 
 */
public class PPSSetup
{
	/**
	 * Used to keep track of value variations from the
	 * {@link GaussianValueReader} objects, for logging and plotting.
	 */
	public static void addVariation(double v)
	{
		variation += Math.abs(v);
	}

	private static boolean join;
	private static boolean leave;
	private static double variation;

	private final Environment environment;
	private final NetworkSimulator<DirectNodeHandle, RawMessage> simulator;
	private final NodeIdFactory nodeIdFactory;
	private final PastryNodeFactory nodeFactory;
	private final InetSocketAddress socketBootAddress;

	private final Vector<PastryPushSum> apps = new Vector<PastryPushSum>();

	private Object bootHandle;
	private long start = 0;
	private long time;

	/**
	 * Creates an environment for using the simulator.
	 */
	public PPSSetup()
	{
		this(null);
	}

	/**
	 * Creates an environment for using the simulator, with a fixed random seed.
	 * 
	 * @param randomSeed
	 *            if not null or 0, use this as the random seed for the
	 *            simulator. In that case the results will always be the same.
	 */
	public PPSSetup(Integer randomSeed)
	{
		if (randomSeed != null && randomSeed != 0)
		{
			environment = Environment.directEnvironment(randomSeed);
		}
		else
		{
			environment = Environment.directEnvironment();
		}

		simulator = new EuclideanNetwork<DirectNodeHandle, RawMessage>(environment);

		socketBootAddress = null;
		nodeIdFactory = new RandomNodeIdFactory(environment);
		nodeFactory = new DirectPastryNodeFactory(nodeIdFactory, simulator, environment);
	}

	/**
	 * Creates an environment for using the network.
	 * 
	 * @param bootAddress
	 *            the IP address of the boot node.
	 * @param bootPort
	 *            the port of the boot node.
	 * @param bindPort
	 *            the port to bind for this node. In case multiple nodes are
	 *            booted by this class, this port will be used for the first
	 *            node, and incremented by 1 for each node thereafter.
	 * @throws IOException
	 *             If the bootAddress cannot be parsed or the construction of
	 *             the {@link SocketPastryNodeFactory} fails.
	 */
	public PPSSetup(String bootAddress, int bootPort, int bindPort) throws IOException
	{
		environment = new Environment();
		environment.getParameters().setString("nat_search_policy", "never");

		simulator = null;

		socketBootAddress = new InetSocketAddress(InetAddress.getByName(bootAddress), bootPort);
		nodeIdFactory = new RandomNodeIdFactory(environment);
		nodeFactory = new SocketPastryNodeFactory(nodeIdFactory, InetAddress.getByName(bootAddress), bindPort, environment);
	}

	/**
	 * Creates new nodes and boot them into the ring.
	 * 
	 * @param period
	 *            the time to wait between creating each new node.
	 * @param nodes
	 *            the number of new nodes.
	 * @param valueReaderFactory
	 *            a {@link ValueReaderFactory} that creates {@link ValueReader}
	 *            instances for the new nodes.
	 * @param stepSize
	 *            the time between sending messages to the node to signal the
	 *            start of the next step in the Push-Sum protocol.
	 * @param updateInterval
	 *            the number of steps between updating node values. If set to 0,
	 *            the node values will never be updated.
	 * @param min
	 *            the domain-specific minimum possible value, used as a lower
	 *            bound for the estimates.
	 * @param max
	 *            the domain-specific maximum possible value, used as an upper
	 *            bound for the estimates.
	 * @param traceMessages
	 *            if set to true, the new nodes will log all sent and received
	 *            messages.
	 * @param doAfter
	 *            a {@link rice.selector.TimerTask} to execute after all nodes
	 *            have been booted.
	 */
	public void scheduleJoiningNodes(long period, final int nodes, final ValueReaderFactory valueReaderFactory, final int stepSize,
			final int updateInterval, final double min, final double max, final boolean traceMessages, final TimerTask doAfter)
	{
		// stop simulator to get deterministic results
		if (simulator != null)
		{
			simulator.stop();
		}

		environment.getSelectorManager().getTimer().schedule(new TimerTask()
		{
			private int currentNode = 0;

			@Override
			public void run()
			{
				try
				{
					join = true;

					// create node
					final PastryNode node = nodeFactory.newNode();

					log("Create new node " + node + ".");

					// create push sum application
					PastryPushSum app = new PastryPushSum(node, stepSize, updateInterval, valueReaderFactory.createValueReader(), min, max,
							traceMessages);

					apps.add(app);

					final int lastNode = ++currentNode;

					// track when the node is finished booting
					node.addObserver(new Observer()
					{
						@Override
						public void update(Observable o, Object arg)
						{
							try
							{
								if (arg instanceof Boolean)
								{
									log("Finished booting node " + node + ".");

									// run task after booting all nodes
									if (doAfter != null && lastNode == nodes)
									{
										doAfter.run();
									}
								}
								else if (arg instanceof JoinFailedException)
								{
									JoinFailedException e = (JoinFailedException) arg;
									e.printStackTrace();
									throw new RuntimeException(e);
								}
							}
							finally
							{
								o.deleteObserver(this);
							}
						}
					});

					// boot node
					if (simulator != null)
					{
						node.boot(bootHandle);
						bootHandle = node.getLocalHandle();
					}
					else
					{
						node.boot(socketBootAddress);
					}

					// cancel task after nodes have been created
					if (currentNode >= nodes)
					{
						cancel();
					}
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}, period, period);

		if (simulator != null)
		{
			simulator.start();
		}
	}

	/**
	 * Periodically creates new nodes and boot them into the ring.
	 * 
	 * @param period
	 *            the time to wait between creating each new node.
	 * @param valueReaderFactory
	 *            a {@link ValueReaderFactory} that creates {@link ValueReader}
	 *            instances for the new nodes.
	 * @param stepSize
	 *            the time between sending messages to the node to signal the
	 *            start of the next step in the Push-Sum protocol.
	 * @param updateInterval
	 *            the number of steps between updating node values. If set to 0,
	 *            the node values will never be updated.
	 * @param min
	 *            the minimum possible value, used as a lower bound for the
	 *            estimates.
	 * @param max
	 *            the maximum possible value, used as an upper bound for the
	 *            estimates.
	 * @param traceMessages
	 *            if set to true, the new nodes will log all sent and received
	 *            messages.
	 */
	public void scheduleJoiningNodes(long period, final ValueReaderFactory valueReaderFactory, final int stepSize,
			final int updateInterval, final double min, final double max, final boolean traceMessages)
	{
		scheduleJoiningNodes(period, Integer.MAX_VALUE, valueReaderFactory, stepSize, updateInterval, min, max, traceMessages, null);
	}

	/**
	 * Periodically tells a random node to stop participating.
	 */
	public void scheduleLeavingNodes(long period)
	{
		environment.getSelectorManager().getTimer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				int node = environment.getRandomSource().nextInt(apps.size() - 1);
				apps.get(node).stop();

				leave = true;
			}
		}, period, period);
	}

	/**
	 * Broadcast a message from the first node to all nodes, initiating a reset
	 * of the Push-Sum protocol.
	 */
	public void scheduleReset(long period)
	{
		environment.getSelectorManager().getTimer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				apps.get(0).broadcastReset();
			}
		}, period, period);
	}

	/**
	 * Schedules a task for logging.
	 * 
	 * @param delay
	 *            Time before the task is run for the first time.
	 * @param period
	 *            Interval between subsequent invocations.
	 */
	public void scheduleObservation(long delay, long period, final int verbosity)
	{
		environment.getSelectorManager().getTimer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (start == 0)
				{
					start = environment.getTimeSource().currentTimeMillis();
				}

				time = environment.getTimeSource().currentTimeMillis();

				if (verbosity >= 3)
				{
					printDetailedStats();
				}
				else if (verbosity == 2)
				{
					printPlotStats();
				}
				else if (verbosity == 1)
				{
					printEsitmate();
				}

				join = false;
				leave = false;
				variation = 0.;
			}
		}, delay, period);
	}

	/**
	 * Destroys the environment after the given delay.
	 * 
	 * @param delay
	 *            the time before the environment is destroyed.
	 */
	public void scheduleTermination(long delay)
	{
		environment.getSelectorManager().getTimer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				environment.destroy();
			}
		}, delay);
	}

	/**
	 * Returns the Pastry {@link Environment}.
	 */
	public Environment getEnvironment()
	{
		return environment;
	}

	private void printDetailedStats()
	{
		if (time == start)
		{
			String line = FormattedStatistics.repeatChar('-', 51 + FormattedStatistics.header().length());
			System.out.format("%n%16s %16s %16s %s %n%s%n", "time", "reltime", "", FormattedStatistics.header(), line);
		}

		FormattedStatistics trueValues = new FormattedStatistics();

		for (int i = 0; i < apps.size(); i++)
		{
			if (apps.get(i).isActive())
			{
				trueValues.addValue(apps.get(i).getTrueValue());
			}
		}

		FormattedStatistics values = new FormattedStatistics();
		FormattedStatistics weights = new FormattedStatistics();
		FormattedStatistics estimates = new FormattedStatistics();
		FormattedStatistics errors = new FormattedStatistics();

		for (int i = 0; i < apps.size(); i++)
		{
			if (apps.get(i).isActive())
			{
				values.addValue(apps.get(i).getValue());
				weights.addValue(apps.get(i).getWeight());
				estimates.addValue(apps.get(i).getEstimate());
				errors.addValue(apps.get(i).getEstimate() - trueValues.getMean());
			}
		}

		System.out.format("%16d %16d %16s %s%n%16s %16s %16s %s%n%16s %16s %16s %s%n%16s %16s %16s %s%n%16s %16s %16s %s%n%n", time, time
				- start, "true:", trueValues, "", "", "value:", values, "", "", "weight:", weights, "", "", "estimate:", estimates, "", "",
				"errors:", errors);
	}

	private void printPlotStats()
	{
		if (time == start)
		{
			String line = FormattedStatistics.repeatChar('-',
					69 + FormattedStatistics.header().length() + FormattedStatistics.header("(err)").length());
			System.out.format("#%15s %s %s %16s %8s %8s %16s%n#%s%n", "reltime", FormattedStatistics.header(),
					FormattedStatistics.header("(err)"), "true", "join", "leave", "variation", line);
		}

		FormattedStatistics trueValues = new FormattedStatistics();

		for (int i = 0; i < apps.size(); i++)
		{
			if (apps.get(i).isActive())
			{
				trueValues.addValue(apps.get(i).getTrueValue());
			}
		}

		FormattedStatistics estimates = new FormattedStatistics();
		FormattedStatistics errors = new FormattedStatistics();

		for (int i = 0; i < apps.size(); i++)
		{
			if (apps.get(i).isActive())
			{
				estimates.addValue(apps.get(i).getEstimate());
				errors.addValue(apps.get(i).getEstimate() - trueValues.getMean());
			}
		}

		System.out.format("%16d %s %s %16e %8s %8s %16s%n", time - start, estimates, errors, trueValues.getMean(), join ? "1" : "-",
				leave ? "1" : "-", variation != 0. || time == start ? String.format("%e", variation) : "-");
	}

	private void printEsitmate()
	{
		if (time == start)
		{
			String line = FormattedStatistics.repeatChar('-', 66);
			System.out.format("#%31s %16s %16s%n#%s%n", "boot node", "time", "estimate", line);
		}

		FormattedStatistics estimates = new FormattedStatistics();

		for (int i = 0; i < apps.size(); i++)
		{
			estimates.addValue(apps.get(i).getEstimate());
		}

		System.out.format("%32s %16d %16e%n", apps.get(0), time, estimates.getMean());
	}

	private void log(String text)
	{
		System.out.format("# [%d] %s%n", environment.getTimeSource().currentTimeMillis(), text);
	}
}
