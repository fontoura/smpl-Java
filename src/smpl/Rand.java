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
 * A Java implementation of the pseudo-random number generator of 'smpl'.
 * <p>
 * This class is part of the a Java implementation of the discrete event simulation environment 'smpl'. The Java implementation is based on the C implementation of 'smpl' version 1.10, which was developed by Myron H. MacDougall and released on October 22, 1987. This version is also based on the C version with bugfixes provided by Elias Proc�pio Duarte J�nior, and on the C version provided by Teemu Kerola.
 *
 * @author Felipe Michels Fontoura
 */
public final class Rand
{
	/**
	 * Default seeds for streams 1-15.
	 */
	private static final int[] DEFAULT_STREAMS = new int[] { 1973272912, 747177549, 20464843, 640830765, 1098742207, 78126602, 84743774, 831312807, 124667236, 1172177002, 1124933064, 1223960546, 1878892440, 1449793615, 553303732 };

	/**
	 * Multiplier (7**5) for 'ranf'.
	 */
	private static final int A = 16807;

	/**
	 * Modulus (2**31-1) for 'ranf'.
	 */
	private static final int M = 2147483647;

	/**
	 * Seed for current stream.
	 */
	private int I;

	private double normal_z2 = 0.0;

	/**
	 * Change the current generator stream.
	 * <p>
	 * Valid stream numbers range from 1 to 15.
	 *
	 * @param n The generator stream number.
	 */
	public final void stream(int n)
	{
		if ((n < 1) || (n > 15))
		{
			throw new IllegalArgumentException("Illegal random number generator stream!");
		}

		I = DEFAULT_STREAMS[n - 1];

		normal_z2 = 0;
	}

	/**
	 * Change the seed for current stream.
	 *
	 * @param Ik The seed.
	 */
	public final void seed(int Ik)
	{
		I = Ik;
	}

	/**
	 * Gets the seed for current stream.
	 *
	 * @return The seed.
	 */
	public final int seed()
	{
		return I;
	}

	/**
	 * Generates a pseudo-random value from an uniform distribution ranging from 0 to 1.
	 *
	 * @return The generated pseudo-random number.
	 */
	public final double ranf()
	{
		// The comments below are based on the original comments of 'smpl'.
		// In the comments, The lower short of I is called 'L', an the higher short of I is called 'H'.

		int k;
		int Hi;
		int Lo;

		// 16807*H->Hi
		// [C] p=(short *)&I;
		// [C] Hi=*(p+1)*A;
		// (p is pointer to I)
		Hi = getShort1(I) * A;

		// 16807*L->Lo
		// [C] *(p+1)=0;
		// (p is pointer to I)
		I = setShort1(I, 0);

		// [C] Lo=I*A;
		// (p is pointer to I)
		Lo = I * A;

		// add high-order bits of Lo to Hi
		// [C] p=(short *)&Lo;
		// [C] Hi+=*(p+1);
		// (p is pointer to Lo)
		Hi += getShort1(Lo);

		// low-order bits of Hi->LO
		// [C] q=(short *)&Hi;
		// (q is pointer to Hi)

		// clear sign bit
		// [C] *(p+1)=*q&0X7FFF;
		// (p is pointer to Lo, q is pointer to Hi)
		Lo = setShort1(Lo, getShort0(Hi) & 0x7FFF);

		// Hi bits 31-45->K
		// [C] k=*(q+1)<<1;
		// [C] if (*q&0x8000) { k++; }
		// (q is pointer to Hi)
		k = getShort1(Hi) << 1;
		if (0 != (getShort0(Hi) & 0x8000))
		{
			k++;
		}

		// form Z + K [- M] (where Z=Lo): presubtract M to avoid overflow
		Lo -= M;
		Lo += k;
		if (Lo < 0)
		{
			Lo += M;
		}
		I = Lo;

		// Lo x 1/(2**31-1)
		return (Lo * 4.656612875E-10);
	}

	/**
	 * Generates a pseudo-random value from an uniform distribution.
	 *
	 * @param a The lower boundary, inclusive.
	 * @param b The upper boundary, inclusive.
	 * @return The generated pseudo-random number.
	 */
	public final double uniform(double a, double b)
	{
		if (a > b)
		{
			throw new IllegalArgumentException("For the uniform pseudo-random generator, the lower boundary must not exceed the higher boundary");
		}
		return (a + (b - a) * ranf());
	}

	/**
	 * Generates a pseudo-random integer in a range from an uniform distribution.
	 *
	 * @param i The lower boundary, inclusive.
	 * @param n The upper boundary, inclusive.
	 * @return The generated pseudo-random integer number.
	 */
	public final int random(int i, int n)
	{
		if (i > n)
		{
			throw new IllegalArgumentException("For the uniform pseudo-random generator, the lower boundary must not exceed the higher boundary");
		}
		int m = n - i;
		int d = (int) ((m + 1.0) * ranf());
		return (i + d);
	}

	/**
	 * Generates a pseudo-random value from an exponential distribution.
	 *
	 * @param x The mean value.
	 * @return The generated pseudo-random number.
	 */
	public final double expntl(double x)
	{
		return (-x * Math.log(ranf()));
	}

	/**
	 * Generates a pseudo-random value from an Erlang distribution.
	 * <p>
	 * The standard deviation MUST NOT be larger than the mean.
	 *
	 * @param x The mean value.
	 * @param s The standard deviation.
	 * @return The generated pseudo-random number.
	 * @throws IllegalArgumentException If the standard deviation is larger than the mean value.
	 */
	public final double erlang(double x, double s)
	{
		if (s > x)
		{
			throw new IllegalArgumentException("For the Erlang pseudo-random generator, the standard deviation must not be larger than the mean value");
		}

		double z1 = x / s;
		int k = (int) (z1 * z1);
		double z2 = 1.0;
		for (int i = 0; i < k; i++)
		{
			z2 *= ranf();
		}
		return (-(x / k) * Math.log(z2));
	}

	/**
	 * Generates a pseudo-random value from a Morse's two-stage hyperexponential distribution.
	 * <p>
	 * The standard deviation MUST be larger than the mean.
	 *
	 * @param x The mean value.
	 * @param s The standard deviation.
	 * @return The generated pseudo-random number.
	 * @throws IllegalArgumentException If the standard deviation is not larger than the mean value.
	 */
	public final double hyperx(double x, double s)
	{
		if (s <= x)
		{
			throw new IllegalArgumentException("For the hyperexponential pseudo-random generator, the standard deviation must be larger than the mean value");
		}

		double cv = s / x;
		double z1 = cv * cv;
		double p = 0.5 * (1.0 - (Math.sqrt((z1 - 1.0) / (z1 + 1.0))));
		double z2 = (ranf() > p) ? (x / (1.0 - p)) : (x / p);
		return (-0.5 * z2 * Math.log(ranf()));
	}

	/**
	 * Generates a pseudo-random value from a normal distribution.
	 *
	 * @param x The mean value.
	 * @param s The standard deviation.
	 * @return The generated pseudo-random number.
	 */
	public final double normal(double x, double s)
	{
		double v1;
		double v2;
		double w;
		double z1;
		if (normal_z2 != 0.0)
		{
			// use value from previous call
			z1 = normal_z2;
			normal_z2 = 0.0;
		}
		else
		{
			do
			{
				v1 = 2.0 * ranf() - 1.0;
				v2 = 2.0 * ranf() - 1.0;
				w = v1 * v1 + v2 * v2;
			}
			while (w >= 1.0);
			w = (Math.sqrt((-2.0 * Math.log(w)) / w));
			z1 = v1 * w;
			normal_z2 = v2 * w;
		}
		return (x + z1 * s);
	}

	/**
	 * Sets the least significant short of an integer.
	 *
	 * @param x The integer.
	 * @param value The short.
	 * @return The integer with the least significant short replaced.
	 */
	protected static final int setShort0(int x, int value)
	{
		return (x & 0xFFFF0000) | (value & 0x0000FFFF);
	}

	/**
	 * Sets the most significant short of an integer.
	 *
	 * @param x The integer.
	 * @param value The short.
	 * @return The integer with the most significant short replaced.
	 */
	protected static final int setShort1(int x, int value)
	{
		return (x & 0x0000FFFF) | (((value) << 16) & 0xFFFF0000);
	}

	/**
	 * Gets the least significant short of an integer.
	 *
	 * @param x The integer.
	 * @return The least significant short of the integer.
	 */
	protected static final int getShort0(int x)
	{
		return (short) (x & 0x0000FFFF);
	}

	/**
	 * Gets the most significant short of an integer.
	 *
	 * @param x The integer.
	 * @return The most significant short of the integer.
	 */
	protected static final int getShort1(int x)
	{
		return (short) ((x >> 16) & 0x0000FFFF);
	}
}
