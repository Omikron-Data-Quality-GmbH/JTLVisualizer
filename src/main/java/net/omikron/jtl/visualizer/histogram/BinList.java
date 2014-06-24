package net.omikron.jtl.visualizer.histogram;

import java.util.ArrayList;
import java.util.List;

public class BinList {

	private List<Bin>		bins	= new ArrayList<Bin>();

	private transient int	maxFrequency;
	private transient int	totalFrequency;

	public BinList() {
		// default constructor
	}

	public BinList(final BinList otherList) {
		this.bins = new ArrayList<Bin>(otherList.getBins());
		updateFrequencies();
	}

	public int getMaxFrequency() {
		return this.maxFrequency;
	}

	/**
	 * @return The totalFrequency.
	 */
	public int getTotalFrequency() {
		return totalFrequency;
	}

	/**
	 * @return The bins.
	 */
	public List<Bin> getBins() {
		return bins;
	}

	/**
	 * @param binNum The number of the bin to return.
	 * @return The bins.
	 */
	public Bin getBin(final int binNum) {
		if (binNum >= 0 && binNum < bins.size()) return bins.get(binNum);
		else return null;
	}

	/**
	 * @return The number of bins in the list.
	 */
	public int getNumBins() {
		return bins.size();
	}

	/**
	 * @param bins The bins to set.
	 */
	public void setBins(final List<Bin> bins) {
		this.bins = bins;
		updateFrequencies();
	}

	public void addBin(final Bin bin) {
		if (bin != null) {
			this.bins.add(bin);
		}
		updateFrequencies();
	}

	private void updateFrequencies() {
		maxFrequency = Integer.MIN_VALUE;
		totalFrequency = 0;
		for (final Bin bin : this.bins) {
			int curFrequency = bin.getFrequency();
			totalFrequency += curFrequency;
			if (curFrequency > maxFrequency) maxFrequency = curFrequency;
		}
	}
}
