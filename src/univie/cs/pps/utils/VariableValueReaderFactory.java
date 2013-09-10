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

package univie.cs.pps.utils;

import rice.environment.random.RandomSource;

/**
 * A Factory for creating {@link VariableValueReader} instances.
 * 
 * @author Dario Seidl
 * 
 */
// TODO rename
public class VariableValueReaderFactory implements ValueReaderFactory
{
	private final double mean;
	private final double std;
	private final double variateStd;
	private final RandomSource randomSource;

	/**
	 * Sets up a factory for creating {@link ValueReader} instances, with the
	 * initial value drawn from a normal distribution with mean {@code mean} and
	 * standard deviation {@code std}.
	 * 
	 * @param mean
	 *            the mean of the normal distribution from which the initial
	 *            value is chosen.
	 * @param std
	 *            the standard deviation of the normal distribution from which
	 *            the initial value is chosen.
	 * @param variateStd
	 *            after each call of {@code ValueReader.getCurrentValue()} the
	 *            value is changed by a number chosen at random from a normal
	 *            distribution with mean 0 and standard deviation {@code std}.
	 * @param randomSource
	 *            an instance of {@link RandomSource} used for generating the
	 *            random numbers in this class.
	 */
	public VariableValueReaderFactory(double mean, double std, double variateStd, RandomSource randomSource)
	{
		this.mean = mean;
		this.std = std;
		this.variateStd = variateStd;
		this.randomSource = randomSource;
	}

	@Override
	public ValueReader createValueReader()
	{
		return new VariableValueReader(randomSource.nextGaussian() * std + mean, variateStd, randomSource);
	}

}
