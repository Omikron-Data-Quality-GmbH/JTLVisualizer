package net.omikron.jtl.visualizer.transcoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import net.omikron.jtl.visualizer.histogram.Histogram;
import net.omikron.jtl.visualizer.sample.Sample;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public class SampleScatterRenderer extends AbstractSampleRenderer {

	public static final String				DEFAULT_TITLE			= "Scatter Plot";

	private static final String				STYLE_DATA_POINT_COLOR	= "#007bbf";
	private static final String				STYLE_DATA_POINT		= "stroke: " + STYLE_DATA_POINT_COLOR + "; stroke-width: 0.5;opacity: 0.7";
	private static final double				MAX_DOT_RADIUS			= 10.0;

	protected transient final DateFormat	timeFormatMMSS;

	private boolean							useTooltips				= false;
	private boolean							plotResultCount			= false;

	/**
	 * Constructs a sample painter which creates a scatter plot from the provided samples.
	 * 
	 * @param samples
	 */
	public SampleScatterRenderer(final List<Sample> samples) {
		super(samples);
		this.title = DEFAULT_TITLE;
		this.timeFormatMMSS = new SimpleDateFormat("mm:ss");
	}

	/**
	 * Paints a histogram of the samples to the svgGraphics object.
	 * 
	 * @param svgGraphics The SVG Graphics 2D object to paint on.
	 */
	@Override
	public void render(final SVGDocument svgDocument) {
		this.svgDocument = svgDocument;
		this.svgRoot = svgDocument.getDocumentElement();

		addCSS();
		addMarkers();

		renderFrame(title);

		if (samples != null && !samples.isEmpty()) {
			final Histogram histogram = new Histogram();
			calculateDescriptiveStats(histogram);

			final long minTimestamp;
			if (xAxisMin >= 0) {
				minTimestamp = histogram.getStartOfFirstRequest() + xAxisMin * 1000;
			} else {
				minTimestamp = histogram.getStartOfFirstRequest();
			}
			final long maxTimestamp;
			if (xAxisMax > minTimestamp) {
				maxTimestamp = histogram.getStartOfFirstRequest() + xAxisMax * 1000;
			} else {
				maxTimestamp = histogram.getEndOfLastRequest();
			}

			final int yUpperBound;
			if (yAxisMax >= 0) {
				yUpperBound = yAxisMax;
			} else {
				yUpperBound = histogram.getMax();
			}

			renderCoordinateSystem(yUpperBound, minTimestamp, maxTimestamp, useLogScaleXAxis, useLogScaleYAxis, plotResultCount);
			renderScatterPlot(	samples, yUpperBound, histogram.getQuantil95(), minTimestamp, maxTimestamp, useLogScaleXAxis, useLogScaleYAxis, useTooltips,
								plotResultCount);
		} else {
			renderEmptyMsg();
		}

		setDimensions();
	}

	/**
	 * @return The useTooltips.
	 */
	public boolean isUseTooltips() {
		return useTooltips;
	}

	/**
	 * @param useTooltips The useTooltips to set.
	 */
	public void setUseTooltips(final boolean useTooltips) {
		this.useTooltips = useTooltips;
	}

	/**
	 * @return The plotResultCount.
	 */
	public boolean isPlotResultCount() {
		return plotResultCount;
	}

	/**
	 * @param plotResultCount The plotResultCount to set.
	 */
	public void setPlotResultCount(final boolean plotResultCount) {
		this.plotResultCount = plotResultCount;
	}

	@Override
	protected String getEmptyMsg() {
		return "The JTL did not contain any samples to create a scatter plot from.";
	}

	private void renderCoordinateSystem(final int yUpperBound, final long minTimestamp, final long maxTimestamp, final boolean useLogScaleXAxis,
			final boolean useLogScaleYAxis, final boolean plotResultCount) {
		svgRoot.appendChild(renderXAxis(minTimestamp, maxTimestamp, useLogScaleXAxis, plotResultCount));
		svgRoot.appendChild(renderYAxis(yUpperBound, useLogScaleYAxis));
	}

	private Element renderXAxis(final long minTimestamp, final long maxTimestamp, final boolean useLogScaleXAxis, final boolean plotResultCount) {
		final int margin = CANVAS_MARGIN / 2;
		final int maxWidth = (CANVAS_WIDTH - CANVAS_MARGIN) - (useLogScaleXAxis && plotResultCount ? 8 : 0);
		final double sx = margin + (useLogScaleXAxis && plotResultCount ? 8 : 0);
		final double sy = CANVAS_HEIGHT - margin + OFFSET_AXIS;

		final Element xAxis = svgDocument.createElement("g");
		xAxis.setAttribute("id", "xAxis");
		xAxis.appendChild(getArrow(	OFFSET_X + margin - OFFSET_AXIS, CANVAS_HEIGHT - margin + OFFSET_AXIS, CANVAS_WIDTH - margin + OFFSET_AXIS + 18,
									CANVAS_HEIGHT - margin + OFFSET_AXIS, null, "black", 1.0));

		final double maxXVal;
		final long duration = maxTimestamp - minTimestamp;

		final int numXLabels = getOptimalNumberOfLabelsForAxis(duration, 10000);
		double stepLabelXAxis;
		if (plotResultCount) {
			maxXVal = 10000.0;
			stepLabelXAxis = getStepForAxis(numXLabels, maxXVal, 500);
			xAxis.appendChild(getText("result", CANVAS_WIDTH - margin + OFFSET_AXIS + 4, CANVAS_HEIGHT - margin + OFFSET_AXIS + 16, TEXT_STYLE_T, "start"));
			xAxis.appendChild(getText("count", CANVAS_WIDTH - margin + OFFSET_AXIS + 4, CANVAS_HEIGHT - margin + OFFSET_AXIS + 26, TEXT_STYLE_T, "start"));
		} else {
			maxXVal = duration / 1000;
			stepLabelXAxis = getStepForAxis(numXLabels, maxXVal, 10);
			xAxis.appendChild(getText("time of", CANVAS_WIDTH - margin + OFFSET_AXIS + 4, CANVAS_HEIGHT - margin + OFFSET_AXIS + 16, TEXT_STYLE_T, "start"));
			xAxis.appendChild(getText("request", CANVAS_WIDTH - margin + OFFSET_AXIS + 4, CANVAS_HEIGHT - margin + OFFSET_AXIS + 26, TEXT_STYLE_T, "start"));
		}

		double curLabelXAxis;
		String curLabelXAxisFormatted;
		if (useLogScaleXAxis) {
			curLabelXAxis = 1.0;
		} else {
			curLabelXAxis = 0.0;
		}
		if (plotResultCount) {
			curLabelXAxisFormatted = numberFormat0Digits.format(curLabelXAxis);
		} else {
			curLabelXAxisFormatted = timeFormatMMSS.format(curLabelXAxis * 1000);
		}
		renderXAxisLabel(sx, sy, curLabelXAxisFormatted, xAxis);
		if (useLogScaleXAxis && plotResultCount) {
			renderXAxisLabel(sx - 8, sy, numberFormat0Digits.format(0), xAxis);
		}

		final double logBase = Math.exp((Math.log(maxXVal) - Math.log(1.0)) / numXLabels);
		double x = 0.0;
		for (int i = 0; i < numXLabels; i++) {
			if (useLogScaleXAxis) {
				curLabelXAxis = (int) Math.ceil(Math.pow(logBase, i + 1));
				x = sx + Math.log(curLabelXAxis) / Math.log(maxXVal) * maxWidth;
			} else {
				curLabelXAxis += stepLabelXAxis;
				x = sx + curLabelXAxis / maxXVal * maxWidth;
			}

			if (plotResultCount) {
				curLabelXAxisFormatted = numberFormat0Digits.format(curLabelXAxis);
			} else {
				curLabelXAxisFormatted = timeFormatMMSS.format(curLabelXAxis * 1000);
			}

			renderXAxisLabel(x, sy, curLabelXAxisFormatted, xAxis);
		}
		return xAxis;
	}

	private void renderXAxisLabel(final double x, final double sy, final String curLabelXAxisFormatted, final Element xAxis) {
		xAxis.appendChild(getLine(x, sy, x, sy + OFFSET_AXIS, null, "black", 0.5));
		final Element label = getText(curLabelXAxisFormatted, x, (sy + 3 * OFFSET_AXIS), TEXT_STYLE_T, "end");
		label.setAttribute("transform", "rotate(-45 " + x + "," + (sy + 3 * OFFSET_AXIS) + ")");
		xAxis.appendChild(label);
	}

	private Element renderYAxis(final int maxResponseTime, final boolean useLogScaleYAxis) {
		final int margin = CANVAS_MARGIN / 2;
		final double sx = margin;
		final double sy = CANVAS_HEIGHT - margin + OFFSET_AXIS;

		final Element yAxis = svgDocument.createElement("g");
		yAxis.setAttribute("id", "yAxis");
		yAxis.appendChild(getArrow(OFFSET_X + margin - OFFSET_AXIS, CANVAS_HEIGHT - margin + OFFSET_AXIS, OFFSET_X + margin - OFFSET_AXIS, margin - OFFSET_AXIS
				- 22, null, "black", 1.0));
		yAxis.appendChild(getText("response time in ms", OFFSET_X + margin / 2 - OFFSET_AXIS, margin - OFFSET_AXIS - 30, TEXT_STYLE_T, "start"));

		final int maxHeight = (CANVAS_HEIGHT - CANVAS_MARGIN);
		final int numYLabels = getOptimalNumberOfLabelsForAxis(maxResponseTime, 50);
		final int stepLabelYAxis = getStepForAxis(numYLabels, maxResponseTime, 50);

		double curLabelYAxis;
		if (useLogScaleYAxis) {
			curLabelYAxis = 1.0;
		} else {
			curLabelYAxis = 0.0;
		}
		yAxis.appendChild(getLine(sx - OFFSET_AXIS, sy, sx, sy, null, "black", 0.5));
		yAxis.appendChild(getText(numberFormat0Digits.format(curLabelYAxis), sx - CANVAS_MARGIN / 16, sy + 3, TEXT_STYLE_T, "end"));

		final double logBase = Math.exp((Math.log(maxResponseTime) - Math.log(1.0)) / numYLabels);
		double y = 0.0;
		for (int i = 0; i < numYLabels; i++) {
			if (useLogScaleYAxis) {
				curLabelYAxis = (int) Math.ceil(Math.pow(logBase, i + 1));
				y = sy - Math.log(curLabelYAxis) / Math.log(maxResponseTime) * maxHeight;
			} else {
				curLabelYAxis += stepLabelYAxis;
				y = sy - OFFSET_AXIS - curLabelYAxis / maxResponseTime * maxHeight;
			}
			yAxis.appendChild(getLine(sx - OFFSET_AXIS, y, sx, y, null, "black", 0.5));
			yAxis.appendChild(getText(numberFormat0Digits.format(curLabelYAxis), sx - CANVAS_MARGIN / 16, y + 3, TEXT_STYLE_T, "end"));
		}
		return yAxis;
	}

	private void renderScatterPlot(final List<Sample> samples, final int yUpperBound, final int quantil95ResponseTime, final long minTimestamp,
			final long maxTimestamp, final boolean useLogScaleXAxis, final boolean useLogScaleYAxis, final boolean useTooltips, final boolean plotResultCount) {
		final double maxHeight = (CANVAS_HEIGHT - CANVAS_MARGIN);
		final double maxWidth = (CANVAS_WIDTH - CANVAS_MARGIN) - (useLogScaleXAxis && plotResultCount ? 8 : 0);
		final double sx = CANVAS_MARGIN / 2 + (useLogScaleXAxis && plotResultCount ? 8 : 0);
		final double sy = CANVAS_HEIGHT - CANVAS_MARGIN / 2;
		final double maxXVal;
		if (plotResultCount) {
			maxXVal = 10000.0;
		} else {
			maxXVal = (maxTimestamp - minTimestamp) / 1000.0;
		}

		final Element plotElement = svgDocument.createElement("g");
		plotElement.setAttribute("id", "scatterPlot");

		for (final Sample sample : samples) {
			final double responseTime = sample.getResponseTime();
			double xVal;
			if (plotResultCount) {
				if (sample.getResultCount() < 0) {
					// Ignore the sample for undefined values
					continue;
				}
				xVal = sample.getResultCount();
			} else {
				xVal = (sample.getStartOfRequest().getTime() - minTimestamp) / 1000.0;
			}

			double y;
			if (useLogScaleYAxis) {
				y = Math.log(responseTime) / Math.log(yUpperBound) * maxHeight;
			} else {
				y = responseTime / yUpperBound * maxHeight;
			}

			double x;
			if (useLogScaleXAxis) {
				if (xVal == 0) {
					x = -8;
				} else {
					x = Math.log(xVal) / Math.log(maxXVal) * maxWidth;
				}
			} else {
				x = xVal / maxXVal * maxWidth;
			}

			final double r = Math.max(1.0, 1.0 + Math.min(sample.getResultCount(), 10000.0) / 10000.0 * (MAX_DOT_RADIUS - 1.0));
			final Element circle = getCircle(sx + x, sy - y, r, STYLE_DATA_POINT);

			if (useTooltips && responseTime >= quantil95ResponseTime) {
				final Element tooltip = svgDocument.createElement("title");
				tooltip.setTextContent(getShortenedURL(sample));
				circle.setAttribute("r", "2.0");
				circle.setAttribute("fill", STYLE_DATA_POINT_COLOR);
				circle.appendChild(tooltip);
			} else {
				circle.setAttribute("fill", "none");
			}

			plotElement.appendChild(circle);
		}

		svgRoot.appendChild(plotElement);
	}

	protected String getShortenedURL(final Sample sample) {
		String shortenedURL = "";
		if (sample != null && sample.getUrl() != null && sample.getUrl().getQuery() != null) {
			// remove sessionID, username and password from URL query
			shortenedURL = sample.getUrl().getQuery().replaceAll("sid=[^&]*&?", "").replaceAll("username=[^&]*&?", "").replaceAll("password=[^&]*&?", "");
		}
		return shortenedURL;
	}
}
