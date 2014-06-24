package net.omikron.jtl.visualizer.report;

import java.text.NumberFormat;

/**
 * The Class SpeedUp.
 */
public class SpeedUp implements Comparable<SpeedUp> {

	private static final NumberFormat	percentFormat	= NumberFormat.getPercentInstance();
	static {
		percentFormat.setMinimumFractionDigits(1);
	}

	private int							oldTime;
	private int							newTime;

	private String						url;
	private int							pos;

	/**
	 * Instantiates a new speed up.
	 * 
	 * @param speedUp the speed up
	 * @param url the url
	 * @param pos the pos
	 */
	public SpeedUp(final int oldTime, final int newTime, final String url, final int pos) {
		super();
		this.oldTime = oldTime;
		this.newTime = newTime;
		this.url = url;
		this.pos = pos;
	}

	/**
	 * Instantiates a new speed up.
	 */
	public SpeedUp() {
		this(0, 0, "", 0);
	}

	/**
	 * @return the oldTime
	 */
	public int getOldTime() {
		return oldTime;
	}

	/**
	 * @param oldTime the oldTime to set
	 */
	public void setOldTime(final int oldTime) {
		this.oldTime = oldTime;
	}

	/**
	 * @return the newTime
	 */
	public int getNewTime() {
		return newTime;
	}

	/**
	 * @param newTime the newTime to set
	 */
	public void setNewTime(final int newTime) {
		this.newTime = newTime;
	}

	/**
	 * Gets the speed up.
	 * 
	 * @return the speedUp
	 */
	public double getSpeedUp() {
		return ((double) oldTime / (double) newTime) - 1.0;
	}

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url the url to set
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * @return the pos
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(final int pos) {
		this.pos = pos;
	}

	@Override
	public String toString() {
		return "SpeedUp [oldTime=" + oldTime + ", newTime=" + newTime + ", speedUp=" + percentFormat.format(getSpeedUp()) + ", url=" + url + ", pos=" + pos
				+ "]";
	}

	public int compareTo(final SpeedUp other) {
		if (getSpeedUp() > other.getSpeedUp()) return 1;
		if (getSpeedUp() < other.getSpeedUp()) return -1;

		final int urlCompare = getUrl().compareTo(other.getUrl());
		if (urlCompare != 0) return urlCompare;

		if (getPos() > other.getPos()) return 1;
		if (getPos() < other.getPos()) return -1;
		return 0;
	}

}
