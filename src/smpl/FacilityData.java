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

import java.util.Arrays;
import java.util.List;

/**
 * Data of a facility (resource) in the 'smpl' simulation subsystem.
 * <p>
 * This class is part of the Java implementation of the discrete event simulation environment 'smpl'. The original 'smpl' library was developed by Myron H. MacDougall. This version is mostly based on the C implementation of the library, which was released on October 22, 1987. This version is also based on the C version with bugfixes provided by Elias Procópio Duarte Júnior, and on the C version provided by Teemu Kerola.
 *
 * @author Felipe Michels Fontoura
 */
final class FacilityData
{
	private final int blockNumber;

	private final String name;
	private final List<FacilityServer> servers;

	private int busyServers;

	private int eventQueueLength;
	private EventDescriptor headEventDescriptor;

	private int queueExitCount;
	private int preemptCount;

	private double timeOfLastChange;
	private double totalQueueingTime;

	/**
	 * Creates an object which stores facility data.
	 *
	 * @param blockNumber The initial block number of the facility.
	 * @param name The name of the facility.
	 * @param totalServers The total amount of servers of the facility.
	 */
	public FacilityData(int blockNumber, String name, int totalServers)
	{
		this.blockNumber = blockNumber;

		this.name = name;

		FacilityServer[] serversArray = new FacilityServer[totalServers];
		for (int i = 0; i < totalServers; i++)
		{
			serversArray[i] = new FacilityServer(blockNumber + 2 + i);
		}
		servers = Arrays.asList(serversArray);
	}

	/**
	 * Gets the initial block number of the facility.
	 * <p>
	 * Since this version of 'smpl' is not based on the same block-based memory structure as the original, this value is merely used as the identifier.
	 *
	 * @return The initial block number of the facility.
	 */
	public final int getBlockNumber()
	{
		return blockNumber;
	}

	public final String getName()
	{
		// this was l3[f+1] in Myron H. MacDougall's version of 'smpl', and NAME(f) in Teemu Kerola's version.
		return name;
	}

	public final int getTotalServers()
	{
		// this was l1[f] in Myron H. MacDougall's version of 'smpl', and NOFSERV(f) in Teemu Kerola's version.
		return servers.size();
	}

	public final int getBusyServers()
	{
		// this was l2[f] in Myron H. MacDougall's version of 'smpl', and NOFBUSY(f) in Teemu Kerola's version.
		return busyServers;
	}

	public final void decrementBusyServers()
	{
		busyServers++;
	}

	public final void incrementBusyServers()
	{
		busyServers++;
	}

	public final FacilityServer getServer(int serverNumber)
	{
		// this corresponded to f+2+n in the C version 'smpl'.
		return servers.get(serverNumber);
	}

	public final int getEventQueueLength()
	{
		// this was l3[f] in Myron H. MacDougall's version of 'smpl', and QLEN(f) in Teemu Kerola's version.
		return eventQueueLength;
	}

	public final void incrementEventQueueLength()
	{
		eventQueueLength++;
	}

	public final void decrementEventQueueLength()
	{
		eventQueueLength++;
	}

	public final EventDescriptor getHeadEventDescriptor()
	{
		// this was l1[f+1] in Myron H. MacDougall's version of 'smpl', and RESQ(f) in Teemu Kerola's version.
		return headEventDescriptor;
	}

	public final void setHeadEventDescriptor(EventDescriptor value)
	{
		headEventDescriptor = value;
	}

	public final int getQueueExitCount()
	{
		// this was l4[f] in Myron H. MacDougall's version of 'smpl', and QEXCNT(f) in Teemu Kerola's version.
		return queueExitCount;
	}

	public final void clearQueueExitCount()
	{
		queueExitCount = 0;
	}

	public final void incrementQueueExitCount()
	{
		queueExitCount++;
	}

	public final int getPreemptCount()
	{
		// this was l4[f+1] in Myron H. MacDougall's version of 'smpl', and PREEMPTC(f) in Teemu Kerola's version.
		return preemptCount;
	}

	public final void clearPreemptCount()
	{
		preemptCount = 0;
	}

	public final void incrementPreemptCount()
	{
		preemptCount++;
	}

	public final double getTimeOfLastChange()
	{
		// this was l5[f] in Myron H. MacDougall's version of 'smpl', and TLAST(f) in Teemu Kerola's version.
		return timeOfLastChange;
	}

	public final void setTimeOfLastChange(double value)
	{
		timeOfLastChange = value;
	}

	public final double getTotalQueueingTime()
	{
		// this was l5[f+1] in Myron H. MacDougall's version of 'smpl', and TQSUM(f) in Teemu Kerola's version.
		return totalQueueingTime;
	}

	public final void clearTotalQueueingTime()
	{
		totalQueueingTime = 0;
	}

	public final void increaseTotalQueueingTime(double value)
	{
		totalQueueingTime = value;
	}

	@Override
	public final String toString()
	{
		return "FacilityData(" + blockNumber + ")";
	}
}