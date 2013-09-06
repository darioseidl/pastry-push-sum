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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;

/**
 * A message carrying the value and weight for the {@link PastryPushSum}
 * application.
 * 
 * @author Dario Seidl
 * 
 */
public class ValueWeightMessage implements Message
{
	private static final StandardToStringStyle toStringStyle = new StandardToStringStyle();
	{
		toStringStyle.setUseShortClassName(true);
	}

	private final Id sender;
	private final Id receiver;
	private final double value;
	private final double weight;

	public ValueWeightMessage(Id sender, Id receiver, double value, double weight)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.value = value;
		this.weight = weight;
	}

	public Id getSender()
	{
		return sender;
	}

	public Id getReceiver()
	{
		return receiver;
	}

	public double getValue()
	{
		return value;
	}

	public double getWeight()
	{
		return weight;
	}

	@Override
	public int getPriority()
	{
		return Message.LOW_PRIORITY;
	}

	@Override
	public String toString()
	{
		return ReflectionToStringBuilder.toString(this, ValueWeightMessage.toStringStyle);
	}
}
