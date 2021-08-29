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
 * An instance of a facility.
 * <p>
 * This class is part of the Java implementation of the discrete event simulation environment 'smpl'. The original 'smpl' library was developed by Myron H. MacDougall. This version is mostly based on the C implementation of the library, which was released on October 22, 1987. This version is also based on the C version with bugfixes provided by Elias Procópio Duarte Júnior, and on the C version provided by Teemu Kerola.
 *
 * @author Felipe Michels Fontoura
 */
final class FacilityServer
{
	private final int blockNumber;

	private Object busyToken;
	private int busyPriority;
	private double busyTime;
	private int releaseCount;
	private double totalBusyTime;

	/**
	 * Creates a facility server.
	 *
	 * @param blockNumber The block number of the facility server.
	 */
	public FacilityServer(int blockNumber)
	{
		this.blockNumber = blockNumber;
	}

	/**
	 * Gets the block number of the facility server.
	 * <p>
	 * Since this version of 'smpl' is not based on the same block-based memory structure as the original, this value is merely used as the identifier.
	 *
	 * @return The block number of the facility server.
	 */
	public final int getBlockNumber()
	{
		return blockNumber;
	}

	public final Object getBusyToken()
	{
		// this was l1[k] in Myron H. MacDougall's version of 'smpl', and USRTKN(k) in Teemu Kerola's version.
		return busyToken;
	}

	public final void setBusyToken(Object value)
	{
		busyToken = value;
	}

	public final int getBusyPriority()
	{
		// this was l2[k] in Myron H. MacDougall's version of 'smpl', and USRPRI(k) in Teemu Kerola's version.
		return busyPriority;
	}

	public final void setBusyPriority(int value)
	{
		busyPriority = value;
	}

	public final double getBusyTime()
	{
		// this was l5[k] in Myron H. MacDougall's version of 'smpl', and BSTART(k) in Teemu Kerola's version.
		return busyTime;
	}

	public final void setBusyTime(double value)
	{
		busyTime = value;
	}

	public final int getReleaseCount()
	{
		// this was l3[k] in Myron H. MacDougall's version of 'smpl', and RELCNT(k) in Teemu Kerola's version.
		return releaseCount;
	}

	public final void clearReleaseCount()
	{
		releaseCount = 0;
	}

	public final void incrementReleaseCount()
	{
		releaseCount++;
	}

	public final double getTotalBusyTime()
	{
		// this was l4[k] in Myron H. MacDougall's version of 'smpl', and BSUM(k) in Teemu Kerola's version.
		return totalBusyTime;
	}

	public final void clearTotalBusyTime()
	{
		totalBusyTime = 0;
	}

	public final void increaseTotalBusyTime(double value)
	{
		totalBusyTime += value;
	}

	@Override
	public final String toString()
	{
		return "FacilityServer(" + blockNumber + ")";
	}
}