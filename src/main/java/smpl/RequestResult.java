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
 * The result of a request or preempt operation.
 * <p>
 * This class is part of the Java implementation of the discrete event simulation environment 'smpl'. The original 'smpl' library was developed by Myron H. MacDougall. This version is mostly based on the C implementation of the library, which was released on October 22, 1987. This version is also based on the C version with bugfixes provided by Elias Procópio Duarte Júnior, and on the C version provided by Teemu Kerola.
 *
 * @author Felipe Michels Fontoura
 */
public enum RequestResult
{
	/**
	 * A facility server has been reserved for the token.
	 */
	RESERVED,

	/**
	 * A facility server has not been reserved, and the last event has been queued in the facility.
	 */
	QUEUED
}
