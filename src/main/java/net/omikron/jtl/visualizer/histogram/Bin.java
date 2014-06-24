package net.omikron.jtl.visualizer.histogram;

public class Bin {

	private int	bound;
	private int	frequency;

	/**
	 * Instantiates a new bin.
	 * 
	 * @param bound the bound
	 * @param frequency the frequency
	 */
	public Bin(final int bound, final int frequency) {
		super();
		this.bound = bound;
		this.frequency = frequency;
	}

	/**
	 * @return The bound.
	 */
	public int getBound() {
		return bound;
	}

	/**
	 * @param bound The bound to set.
	 */
	public void setBound(final int bound) {
		this.bound = bound;
	}

	/**
	 * @return The frequency.
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency The frequency to set.
	 */
	public void setFrequency(final int frequency) {
		this.frequency = frequency;
	}

}
