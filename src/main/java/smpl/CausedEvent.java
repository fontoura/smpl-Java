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
 * An event that has happened in the simulated environment.
 * <p>
 * This class is part of the Java implementation of the discrete event simulation environment 'smpl'. The original 'smpl' library was developed by Myron H. MacDougall. This version is mostly based on the C implementation of the library, which was released on October 22, 1987. This version is also based on the C version with bugfixes provided by Elias Procópio Duarte Júnior, and on the C version provided by Teemu Kerola.
 * <p>
 * In the original C version of 'smpl', event data was returned into variables passed by reference to the 'cause' function. Because of that, the C version contained no such structure as this class.
 * <p>
 * This is a <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html">value-based</a> class, meaning its instances are immutable and should be compared using the {@link #equals(Object) equals} method only.
 *
 * @author Felipe Michels Fontoura
 */
public final class CausedEvent
{
	private final int eventCode;
	private final Object token;

	/**
	 * Creates a caused event.
	 *
	 * @param eventCode A number which identifies the event type.
	 * @param token An object which identifies the event target.
	 */
	public CausedEvent(int eventCode, Object token)
	{
		if (token == null)
		{
			throw new IllegalArgumentException("The token must be provided!");
		}

		this.eventCode = eventCode;
		this.token = token;
	}

	/**
	 * Gets a number which identifies the event type.
	 *
	 * @return A number which identifies the event type.
	 */
	public final int getEventCode()
	{
		return eventCode;
	}

	/**
	 * Gets an object which identifies the event target.
	 *
	 * @return An object which identifies the event target.
	 */
	public final Object getToken()
	{
		return token;
	}

	@Override
	public final int hashCode()
	{
		return eventCode + token.hashCode();
	}

	@Override
	public final boolean equals(Object obj)
	{
		boolean result;
		if (this == obj)
		{
			result = true;
		}
		if (obj == null)
		{
			result = false;
		}
		if (getClass() != obj.getClass())
		{
			result = false;
		}
		else
		{
			CausedEvent other = (CausedEvent) obj;
			result = (eventCode == other.eventCode && token.equals(token));
		}
		return result;
	}

	@Override
	public final String toString()
	{
		return "CausedEvent(" + eventCode + ", " + token + ")";
	}
}