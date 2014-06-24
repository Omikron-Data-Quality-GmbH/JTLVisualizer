package net.omikron.jtl.visualizer.histogram;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Histogram {

	public static final String		NO_SEGMENT_PARAM	= "__NoSegmentParamSet__";

	private Map<String, BinList>	bins				= new HashMap<String, BinList>();

	private int						min;
	private int						quartLow;
	private int						median;
	private int						quartUp;
	private int						quantil95;
	private int						max;

	private int						average;

	private long					startOfFirstRequest;
	private long					endOfLastRequest;

	private transient int			maxFrequency;

	public int getMaxFrequency() {
		return this.maxFrequency;
	}

	/**
	 * @return The bins.
	 */
	public Map<String, BinList> getBins() {
		return bins;
	}

	/**
	 * @param bins The bins to set.
	 */
	public void setBins(final Map<String, BinList> bins) {
		this.bins = bins;
		updateMaxFrequency();
	}

	public void addBin(final String filterName, final Bin bin) {
		String filter = filterName;
		if (StringUtils.isEmpty(filterName)) {
			filter = NO_SEGMENT_PARAM;
		}
		BinList binsForFilter = bins.get(filter);
		if (binsForFilter == null) {
			binsForFilter = new BinList();
			bins.put(filter, binsForFilter);
		}

		binsForFilter.addBin(bin);
		updateMaxFrequency();
	}

	/**
	 * @return The min.
	 */
	public int getMin() {
		return min;
	}

	/**
	 * @param min The min to set.
	 */
	public void setMin(final int min) {
		this.min = min;
	}

	/**
	 * @return The quartLow.
	 */
	public int getQuartLow() {
		return quartLow;
	}

	/**
	 * @param quartLow The quartLow to set.
	 */
	public void setQuartLow(final int quartLow) {
		this.quartLow = quartLow;
	}

	/**
	 * @return The median.
	 */
	public int getMedian() {
		return median;
	}

	/**
	 * @param median The median to set.
	 */
	public void setMedian(final int median) {
		this.median = median;
	}

	/**
	 * @return The quartUp.
	 */
	public int getQuartUp() {
		return quartUp;
	}

	/**
	 * @param quartUp The quartUp to set.
	 */
	public void setQuartUp(final int quartUp) {
		this.quartUp = quartUp;
	}

	/**
	 * @return The quantil95.
	 */
	public int getQuantil95() {
		return quantil95;
	}

	/**
	 * @param quantil95 The quantil95 to set.
	 */
	public void setQuantil95(final int quantil95) {
		this.quantil95 = quantil95;
	}

	/**
	 * @return The max.
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max The max to set.
	 */
	public void setMax(final int max) {
		this.max = max;
	}

	/**
	 * @return the average
	 */
	public int getAverage() {
		return average;
	}

	/**
	 * @param average the average to set
	 */
	public void setAverage(final int average) {
		this.average = average;
	}

	/**
	 * @return The startOfFirstRequest.
	 */
	public long getStartOfFirstRequest() {
		return startOfFirstRequest;
	}

	/**
	 * @param startOfFirstRequest The startOfFirstRequest to set.
	 */
	public void setStartOfFirstRequest(final long startOfFirstRequest) {
		this.startOfFirstRequest = startOfFirstRequest;
	}

	/**
	 * @return The endOfLastRequest.
	 */
	public long getEndOfLastRequest() {
		return endOfLastRequest;
	}

	/**
	 * @param endOfLastRequest The endOfLastRequest to set.
	 */
	public void setEndOfLastRequest(final long endOfLastRequest) {
		this.endOfLastRequest = endOfLastRequest;
	}

	private void updateMaxFrequency() {
		maxFrequency = Integer.MIN_VALUE;
		for (final BinList binsForFilter : this.bins.values()) {
			if (binsForFilter.getMaxFrequency() > maxFrequency) maxFrequency = binsForFilter.getMaxFrequency();
		}
	}

}
