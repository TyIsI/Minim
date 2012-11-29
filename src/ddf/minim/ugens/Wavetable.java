package ddf.minim.ugens;

import java.util.Random;

/**
 * Wavetable wraps a float array of any size and lets you sample array using a normalized value [0,1].
 * This means that if you have an array that is 2048 samples long, then value(0.5) will give you the 
 * 1024th sample. You will most often use Wavetables as the Waveform in an Oscil, but other uses are 
 * also possible. Additionally, Wavetable provides a set of methods for transforming the samples it
 * contains.
 *  
 * @author Mark Godfrey <mark.godfrey@gatech.edu>
 */

public class Wavetable implements Waveform
{

	private float[]	waveform;
	// precalculate this since we use it alot
	private float   lengthForValue;

	/**
	 * Construct a Wavetable that contains <code>size</code> entries.
	 *  
	 * @param size
	 */
	public Wavetable(int size)
	{
		waveform = new float[size];
		lengthForValue = size - 1;
	}

	/**
	 * Construct a Wavetable that will use <code>waveform</code> as the float array to sample from. 
	 * This <em>will not</em> copy <code>waveform</code>, it will use it directly.
	 * 
	 * @param waveform
	 */
	public Wavetable(float[] waveform)
	{
		this.waveform = waveform;
		lengthForValue = waveform.length - 1;
	}
	
	/**
	 * Make a new Wavetable that has the same waveform values as <code>wavetable</code>. This will 
	 * <em>copy</em> the values from the provided Wavetable into this Wavetable's waveform.
	 * 
	 * @param wavetable
	 */
	public Wavetable( Wavetable wavetable )
	{
		waveform = new float[ wavetable.waveform.length ];
		System.arraycopy(wavetable.waveform, 0, waveform, 0, waveform.length);
		lengthForValue = waveform.length - 1;
	}

	/**
	 * Sets this Wavetable's waveform to the one provided. This <em>will not</em> copy the values 
	 * from the provided waveform, it will use the waveform directly.
	 * 
	 * @param waveform
	 */
	public void setWaveform(float[] waveform)
	{
		this.waveform = waveform;
		lengthForValue = waveform.length - 1;
	}

	/**
	 * Returns the value of the i<sup>th</sup> entry in this Wavetable's waveform.
	 * 
	 */
	public float get(int i)
	{
		return waveform[i];
	}
	
	/**
	 * Sample the Wavetable using a value in the range [0,1]. 
	 * For instance, if the Wavetable has 1024 values in its 
	 * float array, then calling value(0.5) will return the 
	 * 512th value in the array. If the result is that it needs 
	 * say the 456.65th value, this will interpolate between 
	 * the surrounding values.
	 * 
	 * @shortdesc Sample the Wavetable using a value in the range [0,1].
	 * 
	 * @param at 
	 * 		float: a value in the range [0, 1]
	 * 
	 * @return float: this Wavetable sampled at the requested interval
	 */
	public float value(float at)
	{	
		float whichSample = lengthForValue * at;
		
		// linearly interpolate between the two samples we want.
		int lowSamp = (int)whichSample;
		int hiSamp = lowSamp + 1;
		// lowSamp might be the last sample in the waveform
		// we need to make sure we wrap.
		if ( hiSamp >= waveform.length )
		{
			hiSamp -= waveform.length;
		}
		
		float rem = whichSample - lowSamp;
		
		return waveform[lowSamp] + rem*(waveform[hiSamp] - waveform[lowSamp]);

		// This was here for testing.  
		// Causes non-interpolation, but adds max # of oscillators
		// return get(lowSamp);
	}

	/**
	 * Returns the underlying waveform, <em>not</em> a copy of it.
	 * 
	 */
	public float[] getWaveform()
	{
		return waveform;
	}

	/**
	 * Sets the i<sup>th</sup> entry of the underlying waveform to <code>value</code>.
	 * This is equivalent to:
	 * <p>
	 * <code>getWaveform()[i] = value;</code>
	 * 
	 */
	public void set(int i, float value)
	{
		waveform[i] = value;
	}

	/**
	 * Returns the length of the underlying waveform. This is equivalent to:
	 * <p>
	 * <code>getWaveform().length</code>
	 * 
	 * @return int: the length of the underlying float array 
	 */
	public int size()
	{
		return waveform.length;
	}

	/**
	 * Multiplies each value of the underlying waveform by <code>scale</code>.
	 */
	public void scale(float scale)
	{
		for (int i = 0; i < waveform.length; i++)
		{
			waveform[i] *= scale;
		}
	}
	
	/**
	 * Apply a DC offset to this Wavetable. In other words, add <code>amount</code> to every 
	 * sample.
	 * @param amount the amount to add to every sample in the table
	 */
	public void offset( float amount )
	{
		for(int i = 0; i < waveform.length; ++i)
		{
			waveform[i] += amount;
		}
	}

	/**
	 * Normalizes the Wavetable by finding the largest amplitude in the table and scaling 
	 * the table by the inverse of that amount. The result is that the largest value in the 
	 * table will now have an amplitude of 1 and everything else is scaled proportionally.
	 */
	public void normalize()
	{
		float max = Float.MIN_VALUE;
		for (int i = 0; i < waveform.length; i++)
		{
			if (Math.abs(waveform[i]) > max)
				max = Math.abs(waveform[i]);
		}
		scale(1 / max);
	}

	/**
	 * Flips the table around 0. Equivalent to <code>flip(0)</code>.
	 *
	 */
	public void invert()
	{
		flip(0);
	}

	/**
	 * Flip the values in the table around a particular value. For example, if you flip around 2, values 
	 * greater than 2 will become less than two by the same amount and values less than 2 will become greater 
	 * than 2 by the same amount. 3 -> 1, 0 -> 4, etc.
	 * 
	 * @param in
	 * 		float: the value to flip the table around
	 */
	public void flip(float in)
	{
		for (int i = 0; i < waveform.length; i++)
		{
			if (waveform[i] > in)
				waveform[i] = in - (waveform[i] - in);
			else
				waveform[i] = in + (in - waveform[i]);
		}
	}

	/**
	 * Adds gaussian noise to the waveform scaled by <code>sigma</code>.
	 * 
	 * @param sigma
	 * 			float: the amount to scale the random values by, in effect how 
	 * 			"loud" the added noise will be.
	 */
	public void addNoise(float sigma)
	{
		Random rgen = new Random();
		for (int i = 0; i < waveform.length; i++)
		{
			waveform[i] += ((float)rgen.nextGaussian()) * sigma;
		}
	}

	/**
	 * Inverts all values in the table that are less than zero. -1 -> 1, -0.2 -> 0.2, etc.
	 */
	public void rectify()
	{
		for (int i = 0; i < waveform.length; i++)
		{
			if (waveform[i] < 0)
				waveform[i] *= -1;
		}
	}

	/**
	 * Smooth out the values in the table by using a moving average window.
	 * 
	 * @param windowLength 
	 * 			int: how many samples large the window should be
	 */
	public void smooth(int windowLength)
	{
		if (windowLength < 1)
			return;
		float[] temp = (float[])waveform.clone();
		for (int i = windowLength; i < waveform.length; i++)
		{
			float avg = 0;
			for (int j = i - windowLength; j <= i; j++)
			{
				avg += temp[j] / windowLength;
			}
			waveform[i] = avg;
		}
	}
	
	/**
	 * Warping works by choosing a point in the waveform, 
	 * the warpPoint, and then specifying where it should move to,
	 * the warpTarget. Both values should be normalized 
	 * (i.e. in the range [0,1]). What will happen is that 
	 * the waveform data in front of and behind the warpPoint 
	 * will be squashed or stretch to fill the space defined 
	 * by where the warpTarget is. For instance, if you took 
	 * Waves.SQUARE and called warp( 0.5, 0.2 ), you would wind 
	 * up with a square wave with a 20 percent duty cycle, the 
	 * same as using Waves.square( 0.2 ). This is because the 
	 * crossover point of a square wave is halfway through and 
	 * warping it such that the crossover is moved to 20% 
	 * through the waveform is equivalent to changing the duty cycle.
	 * Or course, much more interesting things happen when warping 
	 * a more complex waveform, such as one returned by the 
	 * Waves.randomNHarms method, especially if it is warped more 
	 * than once.
	 * 
	 * @shortdesc Warping works by choosing a point in the waveform, 
	 * the warpPoint, and then specifying where it should move to,
	 * the warpTarget.
	 * 
	 * @param warpPoint
	 * 			float: the point in the wave for to be moved, expressed 
	 * 					as a normalized value.
	 * @param warpTarget
	 * 			float: the point in the wave to move the warpPoint to, 
	 * 					expressed as a normalized value.
	 */
	public void warp( float warpPoint, float warpTarget )
	{
		float[] newWave = new float[waveform.length];
		for( int s = 0; s < newWave.length; ++s )
		{
			float lookup = (float)s / newWave.length;
			if ( lookup <= warpTarget )
			{
				// normalize look up to [0,warpTarget], expand to [0,warpPoint]
				lookup = (lookup/warpTarget) * warpPoint;
			}
			else
			{
				// map (warpTarget,1] to (warpPoint,1]
				lookup = warpPoint + (1 - (1-lookup)/(1-warpTarget))*(1-warpPoint);
			}
			newWave[s] = value(lookup);
		}
		waveform = newWave;
	}

}
