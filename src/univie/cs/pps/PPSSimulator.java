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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package univie.cs.pps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

import rice.selector.TimerTask;
import univie.cs.pps.utils.ValueReaderFactory;
import univie.cs.pps.utils.VariableValueReaderFactory;
import univie.cs.pps.validators.AnyDouble;
import univie.cs.pps.validators.NonNegativeDouble;
import univie.cs.pps.validators.NonNegativeInteger;
import univie.cs.pps.validators.PositiveInteger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/**
 * This class is used to study the behavior of the {@link PastryPushSum}
 * application.
 * 
 * @author Dario Seidl
 * 
 */
@Parameters(separators = "=")
public final class PPSSimulator
{
	/**
	 * Redirects the standard output to a file, while maintaining the output to
	 * standard out.
	 * 
	 * @param file
	 *            The file to redirect to.
	 * @throws FileNotFoundException
	 *             If the file is a path to a directory or cannot be created or
	 *             opened.
	 */
	public static void teeOut(File file) throws FileNotFoundException
	{
		if (!file.getParentFile().exists())
		{
			file.getParentFile().mkdirs();
		}

		System.setOut(new PrintStream(new TeeOutputStream(System.out, new FileOutputStream(file))));
	}

	/**
	 * Main method; starts a simulation of nodes running the
	 * {@link PastryPushSum} application in a Pastry ring. The results are
	 * printed to the standard output or written to a file.
	 * 
	 * @param args
	 *            Use with '-h' to see the full list of available arguments.
	 */
	public static void main(String[] args)
	{
		PPSSimulator sim = new PPSSimulator();
		JCommander jc = new JCommander(sim);
		jc.setProgramName(PPSSimulator.class.getCanonicalName());
		jc.setColumnSize(Integer.MAX_VALUE);

		try
		{
			jc.parse(args);

			if (sim.help)
			{
				jc.usage();
			}
			else
			{
				System.out.println(PPSSimulator.class.getCanonicalName() + " " + StringUtils.join(args, " "));
				System.out.println(sim + "\n");

				if (sim.outFile != null)
				{
					teeOut(new File(sim.outFile));
				}

				sim.start();
			}
		}
		catch (ParameterException e)
		{
			System.err.println(e.getMessage());

			jc.usage();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	@Parameter(names = { "-a", "--bootAddress" }, description = "The IP address to use for booting the nodes. Only used is --bootPort is set. If --bootPort is set, and --bootAddress is not given, localhost will be used.")
	private final String bootAddress = null;

	@Parameter(names = { "-b", "--bootPort" }, description = "If set, use networking instead of the simulator, with this as the port of the boot node. The other nodes will be bound to subsequent ports.", validateWith = PositiveInteger.class)
	private final Integer port = null;

	@Parameter(names = { "-n", "--nodes" }, description = "Number of nodes.", validateWith = PositiveInteger.class)
	private final Integer nodes = 10;

	@Parameter(names = { "-s", "--steps" }, description = "Number of steps. The simulation stops after simulation time is larger than steps times step size.", validateWith = PositiveInteger.class)
	private final Integer steps = 100;

	@Parameter(names = { "--stepSize" }, description = "Step size (i.e. time between notification messages).", validateWith = PositiveInteger.class)
	private final Integer stepSize = 1000;

	@Parameter(names = { "--leafsetSize" }, description = "Maximum size of the leafsets.", validateWith = PositiveInteger.class)
	private final Integer leafsetSize = 24;

	@Parameter(names = { "--mean" }, description = "Mean of node values.", validateWith = AnyDouble.class)
	private final Double mean = 2.;

	@Parameter(names = { "--std" }, description = "Standard devation of node values.", validateWith = NonNegativeInteger.class)
	private final Double std = 1.;

	@Parameter(names = { "--min" }, description = "The domain-specific minimum possible value, used as a lower bound for the estimates.", validateWith = AnyDouble.class)
	private final Double min = 0.;

	@Parameter(names = { "--max" }, description = "The domain-specific maximum possible value, used as an upper bound for the estimates.", validateWith = AnyDouble.class)
	private final Double max = Double.MAX_VALUE;

	@Parameter(names = { "-u", "--updateInterval" }, description = "Interval at which node values are updated.", validateWith = NonNegativeInteger.class)
	private final Integer updateInterval = 100;

	@Parameter(names = { "--variateStd" }, description = "Node values variate at each update step by an amount chosen from a normal distribution with mean 0 and this as standard deviation.", validateWith = NonNegativeDouble.class)
	private final Double variateStd = 1.;

	@Parameter(names = { "-j", "--joinInterval" }, description = "Interval at which new nodes join the ring. Set to zero to disable joining of nodes after the initial setup.", validateWith = NonNegativeInteger.class)
	private final Integer joinInterval = 0;

	@Parameter(names = { "-l", "--leaveInterval" }, description = "Interval at which nodes leave (i.e. stop participating). Set to zero to disable leaving of nodes.", validateWith = NonNegativeInteger.class)
	private final Integer leaveInterval = 0;

	@Parameter(names = { "--resetInterval" }, description = "Interval at which a broadcast is sent to all node to initiate a reset of the algorithm. Set to zero to disable resetting.", validateWith = NonNegativeInteger.class)
	private final Integer resetInterval = 0;

	@Parameter(names = { "-r", "--randomSeed" }, description = "If set to a value different from 0, use this as the random seed for the simulator. Not used if --bootPort is set.")
	private final Integer randomSeed = null;

	@Parameter(names = { "-v", "--verbose" }, description = "The log level: \n\t 0 = quiet,\n\t 1 = print estimate,\n\t 2 = print one-line stats,\n\t 3 = print detailed stats.", validateWith = NonNegativeInteger.class)
	private final Integer verbosity = 3;

	@Parameter(names = { "-t", "--trace" }, description = "If set, the nodes will log all sent and received messages.")
	private final Boolean traceMessages = false;

	@Parameter(names = { "-o", "--outFile" }, description = "If set, redirect (tee) output to file.")
	private final String outFile = null;

	@Parameter(names = { "-h", "--help" }, description = "Print this usage message.", help = true)
	private boolean help;

	@Override
	public String toString()
	{
		return ReflectionToStringBuilder.toString(this, StandardToStringStyle.MULTI_LINE_STYLE);
	}

	private void start()
	{
		try
		{
			//create the environment
			final PPSSetup ppss = (port != null) ? new PPSSetup(bootAddress, port, port) : new PPSSetup(randomSeed);

			ppss.getEnvironment().getParameters().setInt("pastry_lSetSize", leafsetSize);

			//simulate variable values
			final ValueReaderFactory valueReaderFactory = new VariableValueReaderFactory(mean, std, variateStd, ppss.getEnvironment()
					.getRandomSource());

			//add initial nodes
			ppss.scheduleJoiningNodes(stepSize, nodes, valueReaderFactory, stepSize, updateInterval, min, max, traceMessages,
					new TimerTask()
					{
						@Override
						public void run()
						{
							//periodically add more nodes
							if (joinInterval > 0)
							{
								ppss.scheduleJoiningNodes(joinInterval * stepSize, valueReaderFactory, stepSize, updateInterval, min, max,
										traceMessages);
							}

							//periodically stop nodes
							if (leaveInterval > 0)
							{
								ppss.scheduleLeavingNodes(leaveInterval * stepSize);
							}

							//periodically broadcast from root to initiate resets
							if (resetInterval > 0)
							{
								ppss.scheduleReset(resetInterval * stepSize);
							}

							//logging
							ppss.scheduleObservation(0, stepSize, verbosity);

							//terminate after the given number of steps
							ppss.scheduleTermination(steps * stepSize);
						}
					});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
