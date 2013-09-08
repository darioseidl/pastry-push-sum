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

import rice.environment.random.RandomSource;
import univie.cs.pps.PPSSetup;

/**
 * An implementation of the {@link ValueReader} interface, simulating variable
 * values. After the value is read, it is changed by a random amount.
 * 
 * @author Dario Seidl
 * 
 */
public class VariableValueReader implements ValueReader
{
	private final RandomSource randomSource;
	private final double std;
	private double value;

	/**
	 * @param initialValue
	 *            The initial value, which will be returned by the first call to
	 *            <code>getCurrentValue()</code>.
	 * @param std
	 *            After each call of <code>getCurrentValue()</code> the value is
	 *            changed by a number chosen at random from a normal
	 *            distribution with mean 0 and standard deviation
	 *            <code>std</code>.
	 * @param randomSource
	 *            An instance of {@link RandomSource} used for generating the
	 *            random numbers in this class.
	 */
	public VariableValueReader(double initialValue, double std, RandomSource randomSource)
	{
		this.randomSource = randomSource;
		this.value = initialValue;
		this.std = std;
	}

	@Override
	public double getCurrentValue()
	{
		double currentValue = value;

		//change value
		double variation = randomSource.nextGaussian() * std;
		value += variation;

		//send to PPSSetup for plotting
		PPSSetup.addVariation(variation);

		return currentValue;
	}
}
