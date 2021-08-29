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
 * Identifier of a facility (resource) in the 'smpl' simulation subsystem.
 * <p>
 * This class is part of the Java implementation of the discrete event simulation environment 'smpl'. The original 'smpl' library was developed by Myron H. MacDougall. This version is mostly based on the C implementation of the library, which was released on October 22, 1987. This version is also based on the C version with bugfixes provided by Elias Procópio Duarte Júnior, and on the C version provided by Teemu Kerola.
 * <p>
 * In the original C version of 'smpl', facilities were identified by plain 'int' values.
 * <p>
 * This is a <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html">value-based</a> class, meaning its instances are immutable and should be compared using the {@link #equals(Object) equals} method only.
 *
 * @author Felipe Michels Fontoura
 */
public final class FacilityIdentifier
{
	private final int blockNumber;

	/**
	 * Creates a facility identifier.
	 *
	 * @param blockNumber The initial block number of the facility.
	 */
	public FacilityIdentifier(int blockNumber)
	{
		this.blockNumber = blockNumber;
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

	@Override
	public final int hashCode()
	{
		return blockNumber;
	}

	@Override
	public final boolean equals(Object obj)
	{
		boolean result;
		if (this == obj)
		{
			result = true;
		}
		else if (obj == null)
		{
			result = false;
		}
		else if (getClass() != obj.getClass())
		{
			result = false;
		}
		else
		{
			FacilityIdentifier other = (FacilityIdentifier) obj;
			result = (blockNumber == other.blockNumber);
		}
		return result;
	}

	@Override
	public final String toString()
	{
		return "FacilityIdentifier(" + blockNumber + ")";
	}
}