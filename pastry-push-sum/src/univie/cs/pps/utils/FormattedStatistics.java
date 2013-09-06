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

package univie.cs.pps.utils;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * A simple subclass of {@link SummaryStatistics} for convenient printing of a
 * table of statistics.
 * 
 * @author Dario Seidl
 * 
 */
public class FormattedStatistics extends SummaryStatistics
{
	/**
	 * Returns a string containing a character <code>c</code> repeated
	 * <code>repeat</code> times.
	 */
	public static String repeatChar(char c, int repeat)
	{
		return (new String(new char[repeat])).replace('\0', c);
	}

	/**
	 * Returns a header with column names, matching the format of this class'
	 * toString implementation.
	 */
	public static String header()
	{
		return String.format("%8s %16s %16s %16s %16s %16s", "n", "min", "max", "mean", "std", "rms");
	}

	/**
	 * Returns a header with column names followed by a suffix, matching the
	 * format of this class' toString implementation.
	 */
	public static String header(String suffix)
	{
		return String.format("%8s %16s %16s %16s %16s %16s", "n" + suffix, "min" + suffix, "max" + suffix, "mean" + suffix, "std" + suffix,
				"rms" + suffix);
	}

	/**
	 * Returns the root-mean-square of values that have been added.
	 * */
	public double getRMS()
	{
		return Math.sqrt(getSumsq() / getN());
	}

	@Override
	public String toString()
	{
		return String.format("%8d %16e %16e %16e %16e %16e", getN(), getMin(), getMax(), getMean(), getStandardDeviation(), getRMS());
	}
}