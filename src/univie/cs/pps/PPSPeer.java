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
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

import rice.selector.TimerTask;
import univie.cs.pps.utils.ValueReaderFactory;
import univie.cs.pps.utils.VariableValueReaderFactory;
import univie.cs.pps.validators.AnyDouble;
import univie.cs.pps.validators.NonNegativeInteger;
import univie.cs.pps.validators.PositiveInteger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/**
 * A proof of concept application to continuously estimate a mean value among
 * the participating peers.
 * 
 * @author Dario Seidl
 */
@Parameters(separators = "=")
public class PPSPeer
{
	private static final int STEP_SIZE = 1000;

	/**
	 * Main method; Joins the Pastry ring and starts exchanging messages to
	 * estimate the mean. It will periodically write the current estimate to the
	 * standard output or to a file.
	 * 
	 * @param args
	 *            Use with '-h' to see the full list of available arguments.
	 */
	public static void main(String[] args)
	{
		PPSPeer peer = new PPSPeer();
		JCommander jc = new JCommander(peer);
		jc.setProgramName(PPSPeer.class.getSimpleName());
		jc.setColumnSize(Integer.MAX_VALUE);

		try
		{
			jc.parse(args);

			if (peer.help)
			{
				jc.usage();
			}
			else
			{
				System.out.println(PPSPeer.class.getCanonicalName() + " " + StringUtils.join(args, " "));
				System.out.println(peer + "\n");

				if (peer.outFile != null)
				{
					PPSSimulator.teeOut(new File(peer.outFile));
				}

				peer.start();
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

	@Parameter(names = { "-a", "--bootAddress" }, description = "The IP address of one node already in the ring. If no node has been booted at this address, this node will create a new ring. If --bootAddress is not given, localhost will be used.")
	private final String bootAddress = null;

	@Parameter(names = { "-b", "--bootPort" }, description = "The port of one node already in the ring", required = true, validateWith = PositiveInteger.class)
	private final Integer bootPort = null;

	@Parameter(names = { "-p", "--bindPort" }, description = "The port to use for binding this node.", required = true, validateWith = PositiveInteger.class)
	private final Integer bindPort = null;

	@Parameter(names = { "--min" }, description = "The domain-specific minimum possible value, used as a lower bound for the estimates.", validateWith = AnyDouble.class)
	private final Double min = 0.;

	@Parameter(names = { "--max" }, description = "The domain-specific maximum possible value, used as an upper bound for the estimates.", validateWith = AnyDouble.class)
	private final Double max = Double.MAX_VALUE;

	@Parameter(names = { "--updateInterval" }, description = "Interval at which node values are updated.", validateWith = NonNegativeInteger.class)
	private final Integer updateInterval = 100;

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
			final PPSSetup ppss = new PPSSetup(bootAddress, bootPort, bindPort);

			//XXX replace this with the actual data source
			ValueReaderFactory valueReaderFactory = new VariableValueReaderFactory(2, 1, 1, ppss.getEnvironment().getRandomSource());

			//boot node
			ppss.scheduleJoiningNodes(STEP_SIZE, 1, valueReaderFactory, STEP_SIZE, updateInterval, min, max, traceMessages, new TimerTask()
			{
				@Override
				public void run()
				{
					//schedule logging
					ppss.scheduleObservation(0, STEP_SIZE, 1);
				}
			});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
