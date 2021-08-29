/*
 * Copyright (c) 2020 Felipe Michels Fontoura
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package smpl;

/**
 * Data regarding a scheduled (for execution) or queued (in a facility) event in the 'smpl' simulation subsystem.
 * <p>
 * This class is part of the Java implementation of the discrete event simulation environment 'smpl'. The original 'smpl' library was developed by Myron H. MacDougall. This version is mostly based on the C implementation of the library, which was released on October 22, 1987. This version is also based on the C version with bugfixes provided by Elias Procópio Duarte Júnior, and on the C version provided by Teemu Kerola.
 *
 * @author Felipe Michels Fontoura
 */
final class EventDescriptor
{
	private final int blockNumber;

	private EventDescriptor next;
	private int eventCode;
	private Object token;
	private double remainingTimeToEvent;
	private double triggerTime;
	private int priority;

	/**
	 * Creates an event descriptor.
	 *
	 * @param blockNumber The block number of the event descriptor.
	 */
	public EventDescriptor(int blockNumber)
	{
		this.blockNumber = blockNumber;
	}

	/**
	 * Gets the block number of the event descriptor.
	 * <p>
	 * Since this version of 'smpl' is not based on the same block-based memory structure as the original, this value is merely used as the identifier.
	 *
	 * @return The block number of the event descriptor.
	 */
	public final int getBlockNumber()
	{
		return blockNumber;
	}

	public final EventDescriptor getNext()
	{
		// this was l1[i] in Myron H. MacDougall's version of 'smpl', and QLINK(i) in Teemu Kerola's version.
		return next;
	}

	public final void setNext(EventDescriptor value)
	{
		next = value;
	}

	public final int getEventCode()
	{
		// this was l3[i] in Myron H. MacDougall's version of 'smpl', and EVENT(i) in Teemu Kerola's version.
		return eventCode;
	}

	public final void setEventCode(int value)
	{
		eventCode = value;
	}

	public final Object getToken()
	{
		// this was l2[i] in Myron H. MacDougall's version of 'smpl', and TOKEN(i) in Teemu Kerola's version.
		return token;
	}

	public final void setToken(Object value)
	{
		token = value;
	}

	public final double getRemainingTimeToEvent()
	{
		// this was l4[i] in Myron H. MacDougall's version of 'smpl', and RTIME(i) in Teemu Kerola's version.
		return remainingTimeToEvent;
	}

	public final void setRemainingTimeToEvent(double value)
	{
		remainingTimeToEvent = value;
	}

	public final double getTriggerTime()
	{
		// this was l5[i] in Myron H. MacDougall's version of 'smpl', and PRI(i) in Teemu Kerola's version.
		// this value was stored in the same location as the priority.
		return triggerTime;
	}

	public final void setTriggerTime(double value)
	{
		triggerTime = value;
	}

	public final int getPriority()
	{
		// this was l5[i] in Myron H. MacDougall's version of 'smpl', and PRI(i) in Teemu Kerola's version.
		// this value was stored in the same location as the priority.
		return priority;
	}

	public final void setPriority(int value)
	{
		priority = value;
	}

	@Override
	public final String toString()
	{
		return "EventDescriptor(" + blockNumber + ")";
	}
}