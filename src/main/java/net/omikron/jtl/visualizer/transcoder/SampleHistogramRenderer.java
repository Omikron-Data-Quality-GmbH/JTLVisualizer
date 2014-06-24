package net.omikron.jtl.visualizer.transcoder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.omikron.jtl.visualizer.histogram.Bin;
import net.omikron.jtl.visualizer.histogram.BinList;
import net.omikron.jtl.visualizer.histogram.Histogram;
import net.omikron.jtl.visualizer.sample.Sample;
import net.omikron.util.NaturalOrderComparator;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public class SampleHistogramRenderer extends AbstractSampleRenderer {

	public static final String		DEFAULT_TITLE							= "Histogram";
	public static final String		DEFAULT_VERSION							= "Unknown";
	public static final String		DEFAULT_DATE							= (new SimpleDateFormat("dd.MM HH:mm")).format(new Date());

	public static final String		X_AXIS_OPTION_DOUBLE_UP_QUART			= "doubleUpQuart";
	public static final String		SEGMENT_PARAM_OPTION_NUM_QUERY_WORDS	= "numWordsInQuery";
	public static final String		SEGMENT_PARAM_OPTION_NUM_FILTERS		= "numFilters";
	public static final String		SEGMENT_PARAM_OPTION_CACHED				= "cached";
	public static final String		SEGMENT_PARAM_OPTION_TIMEOUT			= "timeout";

	public static final String		FF_QUERY_PARAMETER_NAME					= "query";
	public static final String		FF_FILTER_PARAMETER_PREFIX				= "filter";
	public static final String		FF_SEGMENT_PARAM_VALUE_COMPLETE			= "complete";
	public static final String		FF_SEGMENT_PARAM_VALUE_TIMEOUT			= "timed out";
	public static final String		FF_SEGMENT_PARAM_VALUE_UNCACHED			= "uncached";
	public static final String		FF_SEGMENT_PARAM_VALUE_CACHED			= "cached";

	public static final int			DEFAULT_NUMBER_OF_BINS					= 20;
	public static final DiagramType	DEFAULT_DIAGRAM_TYPE					= DiagramType.Histogram;

	// private static final String[] COLOR_PALETTE = {"#9498B2", "#BAF4FC", "#6A5C70", "#ACCEE0", "#404454"};
	private static final String[]	COLOR_PALETTE_FACT_FINDER				= {"#007bbf", "#d43232", "#77b342", "#f0a235", "#ac1a7f", "#20b6e8", "#ed826f",
			"#facd61", "#dbe283", "#c5167e"									};
	private static final String		COLOR_UNSEGMENTED						= "#007bbf";

	private static final int		BAR_SEP									= 5;

	/* Variables used for the placement of information boxes */
	private static final int		BUILDINFO_WIDTH							= CANVAS_WIDTH / 4;
	private static final int		BUILDINFO_USED_HEIGHT					= 40;
	private static final int		BUILDINFO_MARGIN						= 10;
	private static final int		BUILDINFO_HEIGHT						= BUILDINFO_USED_HEIGHT + BUILDINFO_MARGIN;
	private static final int		BUILDINFO_X								= CANVAS_WIDTH - CANVAS_MARGIN / 2 - BUILDINFO_WIDTH;
	private static final int		BUILDINFO_Y								= CANVAS_MARGIN / 4;

	private static final int		DESCRIPTION_WIDTH						= CANVAS_WIDTH / 4;
	private static final int		DESCRIPTION_USED_HEIGHT					= 170;
	private static final int		DESCRIPTION_MARGIN						= 10;
	private static final int		DESCRIPTION_HEIGHT						= DESCRIPTION_USED_HEIGHT + DESCRIPTION_MARGIN;
	private static final int		DESCRIPTION_X							= CANVAS_WIDTH - CANVAS_MARGIN / 2 - DESCRIPTION_WIDTH;
	private static final int		DESCRIPTION_Y							= BUILDINFO_Y + BUILDINFO_HEIGHT + CANVAS_MARGIN / 4;

	private static final int		LEGEND_WIDTH							= CANVAS_WIDTH / 4;
	private static final int		LEGEND_X								= CANVAS_WIDTH - CANVAS_MARGIN / 2 - LEGEND_WIDTH;
	private static final int		LEGEND_Y								= DESCRIPTION_Y + DESCRIPTION_HEIGHT + CANVAS_MARGIN / 4;
	private static final int		LEGEND_MAXIMAL_LINESIZE					= 20;

	/* Variables used to configure the behavior of this renderer (with default values) */
	private int						numBins									= DEFAULT_NUMBER_OF_BINS;
	private DiagramType				diagramType								= DEFAULT_DIAGRAM_TYPE;
	private String					segmentParamName						= null;
	private Set<String>				segmentParamValues						= null;
	private boolean					groupSegments							= false;
	private boolean					useRelativeDataLabel					= false;
	private boolean					useXAxisDoubleUpQuart					= false;

	/**
	 * Constructs a sample painter which creates a histogram from the provided samples.
	 * 
	 * @param samples
	 */
	public SampleHistogramRenderer(final List<Sample> samples) {
		super(samples);
		this.title = DEFAULT_TITLE;
		this.version = DEFAULT_VERSION;
		this.date = DEFAULT_DATE;
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
			final int lowerBound;
			if (xAxisMin >= 0) {
				lowerBound = xAxisMin;
			} else {
				lowerBound = histogram.getMin();
			}
			final int upperBound;
			if (useXAxisDoubleUpQuart) {
				upperBound = 2 * histogram.getQuartUp();
			} else {
				if (xAxisMax > lowerBound) {
					upperBound = xAxisMax;
				} else {
					upperBound = histogram.getMax();
				}
			}
			calculateBins(histogram, lowerBound, upperBound, numBins, useLogScaleXAxis, segmentParamName, segmentParamValues);
			if (StringUtils.isNotEmpty(segmentParamName) && groupSegments) {
				final int sparseBound = histogram.getMaxFrequency() * numBins / 16;
				System.out.println("sparseBound: " + sparseBound);
				mergeSparseBins(histogram, getTotalFrequencyOrdering(histogram, false), sparseBound);
			}
			List<String> orderedSegments;
			if (areSegmentsOrdered(segmentParamName)) {
				orderedSegments = getSegmentOrdering(histogram);
			} else {
				orderedSegments = getTotalFrequencyOrdering(histogram, true);
			}
			final int yUpperBound;
			if (yAxisMax >= 0) {
				yUpperBound = yAxisMax;
			} else {
				yUpperBound = histogram.getMaxFrequency();
			}
			renderCoordinateSystem(histogram, lowerBound, yUpperBound, useLogScaleYAxis);
			final List<String> colors = setupColors(histogram, segmentParamName);
			renderHistogram(histogram, useLogScaleYAxis, yUpperBound, orderedSegments, colors);
			renderLegend(histogram, segmentParamName, orderedSegments, colors);
			renderDescriptiveStatistics(histogram);
			renderBuildinfo(histogram);
		} else {
			renderEmptyMsg();
		}

		setDimensions();
	}

	/**
	 * @return The numBins.
	 */
	public int getNumBins() {
		return numBins;
	}

	/**
	 * @param numBins The numBins to set.
	 */
	public void setNumBins(final int numBins) {
		this.numBins = numBins;
	}

	/**
	 * @return The diagramType.
	 */
	public DiagramType getDiagramType() {
		return diagramType;
	}

	/**
	 * @param diagramType The diagramType to set.
	 */
	public void setDiagramType(final DiagramType diagramType) {
		this.diagramType = diagramType;
	}

	/**
	 * @return The segmentParamName.
	 */
	public String getSegmentParamName() {
		return segmentParamName;
	}

	/**
	 * @param segmentParamName The segmentParamName to set.
	 */
	public void setSegmentParamName(final String segmentParamName) {
		this.segmentParamName = segmentParamName;
	}

	/**
	 * @return The segmentParamValues.
	 */
	public Set<String> getSegmentParamValues() {
		return segmentParamValues;
	}

	/**
	 * @param segmentParamValues The segmentParamValues to set.
	 */
	public void setSegmentParamValues(final Set<String> segmentParamValues) {
		this.segmentParamValues = segmentParamValues;
	}

	/**
	 * @return The groupSegments.
	 */
	public boolean isGroupSegments() {
		return groupSegments;
	}

	/**
	 * @param groupSegments The groupSegments to set.
	 */
	public void setGroupSegments(final boolean groupSegments) {
		this.groupSegments = groupSegments;
	}

	/**
	 * @return The useRelativeDataLabel.
	 */
	public boolean isUseRelativeDataLabel() {
		return useRelativeDataLabel;
	}

	/**
	 * @param useRelativeDataLabel The useRelativeDataLabel to set.
	 */
	public void setUseRelativeDataLabel(final boolean useRelativeDataLabel) {
		this.useRelativeDataLabel = useRelativeDataLabel;
	}

	/**
	 * @return The useXAxisDoubleUpQuart.
	 */
	public boolean isUseXAxisDoubleUpQuart() {
		return useXAxisDoubleUpQuart;
	}

	/**
	 * @param useXAxisDoubleUpQuart The useXAxisDoubleUpQuart to set.
	 */
	public void setUseXAxisDoubleUpQuart(final boolean useXAxisDoubleUpQuart) {
		this.useXAxisDoubleUpQuart = useXAxisDoubleUpQuart;
	}

	private List<String> setupColors(final Histogram histogram, final String segmentParamName) {
		final Map<String, BinList> bins = histogram.getBins();
		int numBinLists = bins.size();
		final List<String> colors = new ArrayList<String>(numBinLists);
		final Element defs = svgDocument.createElement("defs");
		String color;

		final boolean isHeatmap = areSegmentsOrdered(segmentParamName);

		if (bins.keySet().contains(Histogram.NO_SEGMENT_PARAM)) {
			numBinLists--;
		}

		int i = 0;
		for (final String segment : bins.keySet()) {
			if (Histogram.NO_SEGMENT_PARAM.endsWith(segment)) {
				color = COLOR_UNSEGMENTED;
			} else if (isHeatmap) {
				final double heatFrac = i / (double) (numBinLists - 1);
				color = getROYGBGradientColor(heatFrac);
			} else {
				color = COLOR_PALETTE_FACT_FINDER[i % COLOR_PALETTE_FACT_FINDER.length];
			}
			colors.add(color);
			defs.appendChild(getGradient(color, i));

			i++;
		}

		svgRoot.insertBefore(defs, svgRoot.getFirstChild());

		return colors;
	}

	private String getROYGBGradientColor(final double heatFrac) {
		String r, g, b;
		if (heatFrac >= 0.0 && heatFrac <= 0.25) {
			final double localFrac = heatFrac * 4.0;
			r = "00";
			g = String.format("%02x", (int) (255.0 * localFrac));
			b = "ff";
		} else if (heatFrac > 0.25 && heatFrac <= 0.5) {
			final double localFrac = (heatFrac - 0.25) * 4.0;
			r = "00";
			g = "ff";
			b = String.format("%02x", (int) (255.0 * (1.0 - localFrac)));
		} else if (heatFrac > 0.5 && heatFrac <= 0.75) {
			final double localFrac = (heatFrac - 0.5) * 4.0;
			r = String.format("%02x", (int) (255.0 * localFrac));
			g = "ff";
			b = "00";
		} else if (heatFrac > 0.75 && heatFrac <= 1.0) {
			final double localFrac = (heatFrac - 0.75) * 4.0;
			r = "ff";
			g = String.format("%02x", (int) (255.0 * (1.0 - localFrac)));
			b = "00";
		} else {
			r = "00";
			g = "00";
			b = "00";
		}
		return "#" + r + g + b;
	}

	private boolean areSegmentsOrdered(final String segmentParamName) {
		return segmentParamName != null
				&& (segmentParamName.equals(SEGMENT_PARAM_OPTION_NUM_FILTERS) || segmentParamName.equals(SEGMENT_PARAM_OPTION_NUM_QUERY_WORDS));
	}

	private Element getGradient(final String color, final int i) {
		final Element gradient = svgDocument.createElement("radialGradient");
		gradient.setAttribute("id", "verticalGradient" + i);
		gradient.setAttribute("cx", "100%");
		gradient.setAttribute("cy", "100%");
		gradient.setAttribute("r", "100%");
		gradient.setAttribute("fx", "80%");
		gradient.setAttribute("fy", "80%");

		final Element stop1 = svgDocument.createElement("stop");
		stop1.setAttribute("offset", "0%");
		stop1.setAttribute("style", "stop-color:" + color + ";stop-opacity:0.4");
		gradient.appendChild(stop1);
		final Element stop2 = svgDocument.createElement("stop");
		stop2.setAttribute("offset", "100%");
		stop2.setAttribute("style", "stop-color:" + color + ";stop-opacity:0.8");
		gradient.appendChild(stop2);
		return gradient;
	}

	private void renderBuildinfo(final Histogram histogram) {
		final int width = BUILDINFO_WIDTH;
		final int height = BUILDINFO_HEIGHT;
		final int x = BUILDINFO_X;
		final int y = BUILDINFO_Y;

		final Element stats = svgDocument.createElement("g");
		stats.setAttribute("id", "buildinfo");

		stats.appendChild(getRectangle(x, y, width, height, "fill: white; opacity: 0.5; stroke: black; stroke-width: 1.0"));

		final int xLeft = x + width / 2 + 10;
		final int xRight = x + width - 25;

		stats.appendChild(getText("Test Data Version:", xLeft, y + 20, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(this.getVersion(), xRight, y + 20, TEXT_STYLE_P, "end"));

		stats.appendChild(getText("Build Date:", xLeft, y + 40, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(this.getDate(), xRight, y + 40, TEXT_STYLE_P, "end"));

		/*
		 * If you add or remove elements and now need more or less space change BUILDINFO_USED_HEIGHT to shrink or expand the Box around the Description. It
		 * should be equal to the y-Coordinate of your lowest Text.
		 */

		svgRoot.appendChild(stats);
	}

	private void renderDescriptiveStatistics(final Histogram histogram) {
		final int width = DESCRIPTION_WIDTH;
		final int height = DESCRIPTION_HEIGHT;
		final int x = DESCRIPTION_X;
		final int y = DESCRIPTION_Y;

		final Element stats = svgDocument.createElement("g");
		stats.setAttribute("id", "statistics");

		stats.appendChild(getRectangle(x, y, width, height, "fill: white; opacity: 0.5; stroke: black; stroke-width: 1.0"));

		final int xLeft = x + width / 2 + 10;
		final int xRight = x + width - 25;

		stats.appendChild(getText("Requests:", xLeft, y + 20, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(numberFormat0Digits.format(samples.size()), xRight, y + 20, TEXT_STYLE_P, "end"));

		final double throughput = (double) (samples.size() * 1000) / (histogram.getEndOfLastRequest() - histogram.getStartOfFirstRequest());
		stats.appendChild(getText("Throughput:", xLeft, y + 40, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(numberFormat2Digits.format(throughput) + " rps", xRight, y + 40, TEXT_STYLE_P, "end"));

		final int max = histogram.getMax();
		final int quartUp = histogram.getQuartUp();
		final int median = histogram.getMedian();
		final int quartLow = histogram.getQuartLow();
		final int min = histogram.getMin();
		stats.appendChild(getText("Response times:", xLeft, y + 70, TEXT_STYLE_P, "end"));

		stats.appendChild(getText("Minimum:", xLeft, y + 90, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(numberFormat0Digits.format(min) + " ms", xRight, y + 90, TEXT_STYLE_P, "end"));

		stats.appendChild(getText("Lower quartile:", xLeft, y + 110, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(numberFormat0Digits.format(quartLow) + " ms", xRight, y + 110, TEXT_STYLE_P, "end"));

		stats.appendChild(getText("Median:", xLeft, y + 130, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(numberFormat0Digits.format(median) + " ms", xRight, y + 130, TEXT_STYLE_P, "end"));

		stats.appendChild(getText("Upper quartile:", xLeft, y + 150, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(numberFormat0Digits.format(quartUp) + " ms", xRight, y + 150, TEXT_STYLE_P, "end"));

		stats.appendChild(getText("Maximum:", xLeft, y + 170, TEXT_STYLE_P, "end"));
		stats.appendChild(getText(numberFormat0Digits.format(max) + " ms", xRight, y + 170, TEXT_STYLE_P, "end"));

		/*
		 * If you add or remove elements and now need more or less space change DESCRIPTION_USED_HEIGHT to shrink or expand the Box around the Description. It
		 * should be equal to the y-Coordinate of your lowest Text.
		 */

		svgRoot.appendChild(stats);
	}

	private void renderLegend(final Histogram histogram, final String segmentParam, final List<String> orderedSegments, final List<String> colors) {
		if (orderedSegments.size() >= 2) {
			final int dy = 20;
			final int width = LEGEND_WIDTH;
			final int x = LEGEND_X;
			final int y = LEGEND_Y;

			final Element legend = svgDocument.createElement("g");

			// legend.appendChild(getText("Legend", x + 20, y + dy + 10, TEXT_STYLE_P, "start"));
			if (StringUtils.isNotEmpty(segmentParam)) {
				if (SEGMENT_PARAM_OPTION_NUM_QUERY_WORDS.equals(segmentParam)) {
					legend.appendChild(getText("# words in", x + width / 4 + 20, y + dy + 4, TEXT_STYLE_T, "start"));
					legend.appendChild(getText("search query", x + width / 4 + 20, y + dy + 14, TEXT_STYLE_T, "start"));
				} else if (SEGMENT_PARAM_OPTION_NUM_FILTERS.equals(segmentParam)) {
					legend.appendChild(getText("# filters", x + width / 4 + 20, y + dy + 10, TEXT_STYLE_P, "start"));
				} else {
					legend.appendChild(getText(segmentParam, x + width / 4 + 20, y + dy + 10, TEXT_STYLE_P, "start"));
				}
			}
			legend.appendChild(getText("#", x + width - 20, y + dy + 10, TEXT_STYLE_P, "end"));

			/* counts the segment we are currently working on */
			int segmentNr = 0;
			/* counts the line we are writing in */
			int line = 2;

			for (final String segment : orderedSegments) {
				final int color = segmentNr % colors.size();
				// color of bins
				legend.appendChild(getRectangle(x + 20, y + line * dy + 2, width / 4 - 20, 16, "fill: url(#verticalGradient" + color + "); stroke-width: 0.5"));

				// name of bins
				String segmentLabel;
				if (SEGMENT_PARAM_OPTION_NUM_QUERY_WORDS.equals(segmentParam)) {
					segmentLabel = segment.replaceAll(Histogram.NO_SEGMENT_PARAM, "no query");
				} else if (SEGMENT_PARAM_OPTION_NUM_FILTERS.equals(segmentParam)) {
					segmentLabel = segment.replaceAll(Histogram.NO_SEGMENT_PARAM, "no filters");
				} else {
					segmentLabel = segment.replaceAll(Histogram.NO_SEGMENT_PARAM, "other");
				}

				while (segmentLabel.length() > LEGEND_MAXIMAL_LINESIZE) {
					final int lastSpacePosition = segmentLabel.substring(0, LEGEND_MAXIMAL_LINESIZE).lastIndexOf(" ");
					if (lastSpacePosition <= 0) {
						// not dividable
						break;
					}
					legend.appendChild(getText(segmentLabel.substring(0, lastSpacePosition), x + width / 4 + 20, y + line * dy + 16, TEXT_STYLE_P, "start"));
					segmentLabel = segmentLabel.substring(lastSpacePosition);
					line++;
				}

				legend.appendChild(getText(segmentLabel, x + width / 4 + 20, y + line * dy + 16, TEXT_STYLE_P, "start"));

				// total number of bin frequencies
				final int totalFrequency = histogram.getBins().get(segment).getTotalFrequency();
				legend.appendChild(getText(numberFormat0Digits.format(totalFrequency), x + width - 20, y + line * dy + 16, TEXT_STYLE_P, "end"));

				segmentNr++;
				line++;
			}

			final int height = dy * (line + 1);

			legend.setAttribute("id", "legend");
			if (height > CANVAS_HEIGHT / 2) {
				final double scale = CANVAS_HEIGHT / 2.0 / height;
				final double scalePivotX = x + width;
				final double scalePivotY = y;
				legend.setAttribute("transform", "translate(" + (-scalePivotX * (scale - 1)) + ", " + (-scalePivotY * (scale - 1)) + ") scale(" + scale + ")");
			}

			legend.insertBefore((getRectangle(x, y, width, height, "fill: white; opacity: 0.5; stroke: black; stroke-width: 1.0")), legend.getFirstChild());

			svgRoot.appendChild(legend);
		}
	}

	private void renderCoordinateSystem(final Histogram histogram, final int lowerBound, final int yAxisMax, final boolean useLogScaleYAxis) {
		svgRoot.appendChild(renderXAxis(histogram, lowerBound));
		svgRoot.appendChild(renderYAxis(histogram, useLogScaleYAxis, yAxisMax));
	}

	private Element renderXAxis(final Histogram histogram, final int lowerBound) {
		final int margin = CANVAS_MARGIN / 2;
		final double sx = OFFSET_X + margin - BAR_SEP / 2;
		final double sy = CANVAS_HEIGHT - margin;

		final Element xAxis = svgDocument.createElement("g");
		xAxis.setAttribute("id", "xAxis");
		xAxis.appendChild(getArrow(	OFFSET_X + margin - OFFSET_AXIS, CANVAS_HEIGHT - margin + OFFSET_AXIS, CANVAS_WIDTH - margin + OFFSET_AXIS + 18,
									CANVAS_HEIGHT - margin + OFFSET_AXIS, null, "black", 1.0));
		xAxis.appendChild(getText("response", CANVAS_WIDTH - margin + OFFSET_AXIS - 4, CANVAS_HEIGHT - margin + OFFSET_AXIS + 16, TEXT_STYLE_T, "start"));
		xAxis.appendChild(getText("time", CANVAS_WIDTH - margin + OFFSET_AXIS - 4, CANVAS_HEIGHT - margin + OFFSET_AXIS + 26, TEXT_STYLE_T, "start"));
		xAxis.appendChild(getText("in ms", CANVAS_WIDTH - margin + OFFSET_AXIS - 4, CANVAS_HEIGHT - margin + OFFSET_AXIS + 36, TEXT_STYLE_T, "start"));

		final Iterator<BinList> binsIterator = histogram.getBins().values().iterator();
		if (binsIterator.hasNext()) {
			final BinList bins = binsIterator.next();

			final double step = (CANVAS_WIDTH - CANVAS_MARGIN) / bins.getNumBins();

			final int minXValue;
			if (histogram.getMin() < lowerBound) {
				minXValue = 0;
			} else {
				minXValue = lowerBound;
			}

			xAxis.appendChild(getLine(sx, sy + OFFSET_AXIS, sx, sy + 2 * OFFSET_AXIS, null, "black", 0.5));
			Element label = getText(numberFormat0Digits.format(minXValue), sx - step / 8, (sy + 4 * OFFSET_AXIS), TEXT_STYLE_T, "end");
			label.setAttribute("transform", "rotate(-45 " + (sx - step / 8) + "," + (sy + 4 * OFFSET_AXIS) + ")");
			xAxis.appendChild(label);

			for (int i = 0; i < bins.getNumBins(); i++) {
				final Bin bin = bins.getBin(i);
				final double x = sx + (i + 1) * step;

				xAxis.appendChild(getLine(x, sy + OFFSET_AXIS, x, sy + 2 * OFFSET_AXIS, null, "black", 0.5));
				label = getText(numberFormat0Digits.format(bin.getBound()), x - step / 8, (sy + 4 * OFFSET_AXIS), TEXT_STYLE_T, "end");
				label.setAttribute("transform", "rotate(-45 " + (x - step / 8) + "," + (sy + 4 * OFFSET_AXIS) + ")");
				xAxis.appendChild(label);
			}
		}
		return xAxis;
	}

	private Element renderYAxis(final Histogram histogram, final boolean useLogScaleYAxis, final int yAxisMax) {
		final int margin = CANVAS_MARGIN / 2;
		final double sx = OFFSET_X + margin;
		final double sy = CANVAS_HEIGHT - margin;

		final Element yAxis = svgDocument.createElement("g");
		yAxis.setAttribute("id", "yAxis");
		yAxis.appendChild(getArrow(OFFSET_X + margin - OFFSET_AXIS, CANVAS_HEIGHT - margin + OFFSET_AXIS, OFFSET_X + margin - OFFSET_AXIS, margin - OFFSET_AXIS
				- 22, null, "black", 1.0));
		yAxis.appendChild(getText("frequency", OFFSET_X + margin - OFFSET_AXIS, margin - OFFSET_AXIS - 30, TEXT_STYLE_T, "middle"));

		final double maxHeight = (CANVAS_HEIGHT - CANVAS_MARGIN);

		final int numYLabels = getOptimalNumberOfLabelsForAxis(yAxisMax, 50);
		final int stepLabelYAxis = getStepForAxis(numYLabels, yAxisMax, 50);

		double curLabelYAxis;
		if (useLogScaleYAxis) {
			curLabelYAxis = 1.0;
		} else {
			curLabelYAxis = 0.0;
		}
		yAxis.appendChild(getLine(sx - BAR_SEP - OFFSET_AXIS, sy, sx - BAR_SEP, sy, null, "black", 0.5));
		yAxis.appendChild(getText(numberFormat0Digits.format(curLabelYAxis), sx - CANVAS_MARGIN / 16 - BAR_SEP / 2 - OFFSET_AXIS, sy + 4, TEXT_STYLE_T, "end"));

		final double logBase = Math.exp((Math.log(yAxisMax) - Math.log(1.0)) / numYLabels);
		double y = 0.0;
		for (int i = 0; i < numYLabels; i++) {
			if (useLogScaleYAxis) {
				curLabelYAxis = (int) Math.ceil(Math.pow(logBase, i + 1));
				y = sy - Math.log(curLabelYAxis) / Math.log(yAxisMax) * maxHeight;
			} else {
				curLabelYAxis += stepLabelYAxis;
				y = sy - curLabelYAxis / yAxisMax * maxHeight;
			}
			yAxis.appendChild(getLine(sx - BAR_SEP - OFFSET_AXIS, y, sx - BAR_SEP, y, null, "black", 0.5));
			yAxis.appendChild(getText(	numberFormat0Digits.format(curLabelYAxis), sx - CANVAS_MARGIN / 16 - BAR_SEP / 2 - OFFSET_AXIS, y + 4, TEXT_STYLE_T,
										"end"));
		}
		return yAxis;
	}

	private void renderHistogram(final Histogram histogram, final boolean useLogScaleYAxis, final int yAxisMax, final List<String> orderedSegments,
			final List<String> colors) {
		final double maxHeight = (CANVAS_HEIGHT - CANVAS_MARGIN);
		final double sx = CANVAS_MARGIN / 2;
		final double sy = CANVAS_HEIGHT - CANVAS_MARGIN / 2;
		int color = 0;
		int numBinList = 0;

		for (final String segment : orderedSegments) {
			final BinList bins = histogram.getBins().get(segment);

			final double step = (CANVAS_WIDTH - CANVAS_MARGIN) / bins.getNumBins();
			final double dTotal = step / 3 * (2.0 / Math.PI * Math.atan(orderedSegments.size() - 1));
			final double dFrac = (double) numBinList / (double) orderedSegments.size() * dTotal;

			final Element binsElement = svgDocument.createElement("g");
			binsElement.setAttribute("id", "histogram-" + segment);

			final StringBuffer pathData = new StringBuffer();

			double x = sx + dFrac;
			double prevX = 0.0;
			double prevY = 0.0;
			for (int i = 0; i < bins.getNumBins(); i++) {
				final Bin bin = bins.getBin(i);
				final double height;
				final int frequency = bin.getFrequency();
				if (useLogScaleYAxis) {
					height = Math.log(Math.max(frequency, 1)) / Math.log(yAxisMax) * maxHeight;
				} else {
					height = frequency * maxHeight / yAxisMax;
				}
				final double width = step - dTotal - BAR_SEP;
				final double y = sy - height;

				if (diagramType == DiagramType.LineGraph) {
					if (i == 0) {
						pathData.append('M');
					} else {
						final double controlPointDist = width / 2;
						pathData.append('C');
						pathData.append(' ');
						pathData.append(prevX + width / 2 + controlPointDist);
						pathData.append(' ');
						pathData.append(prevY);
						pathData.append(' ');
						pathData.append(x + width / 2 - controlPointDist);
						pathData.append(' ');
						pathData.append(y);
					}
					pathData.append(' ');
					pathData.append(x + width / 2);
					pathData.append(' ');
					pathData.append(y);
					binsElement.appendChild(getLine(x + width / 2, y - 2, x + width / 2, y + 2, null, colors.get(color), 2));

				} else {
					binsElement.appendChild(getRectangle(x, y, width, height, "fill: url(#verticalGradient" + color + "); stroke-width: 0.5"));
				}

				String dataPointLabel;
				if (useRelativeDataLabel) {
					dataPointLabel = numberFormatPercent.format((double) frequency / (double) bins.getTotalFrequency());
				} else {
					dataPointLabel = numberFormat0Digits.format(frequency);
				}
				if (frequency != 0) {
					binsElement.appendChild(getText(dataPointLabel, x + (step - dTotal) / 2 - BAR_SEP / 2, Math.max(y - DATA_LABEL_SEP, CANVAS_MARGIN / 2),
													TEXT_STYLE_T, "middle"));
				}

				prevX = x;
				prevY = y;
				x = x + step;
			}

			if (diagramType == DiagramType.LineGraph) {
				binsElement.appendChild(getPath(pathData.toString(), colors.get(color), LINE_STYLE));
			}

			svgRoot.appendChild(binsElement);
			color = (color + 1) % colors.size();
			numBinList++;
		}
	}

	@Override
	protected String getEmptyMsg() {
		return "The JTL did not contain any samples to create a histogram from.";
	}

	private void calculateBins(final Histogram histogram, final int lowerBound, final int upperBound, final int numBins, final boolean useLogScaleXAxis,
			final String segmentParam, final Set<String> segmentParamValues) {
		Set<String> segments;
		if (segmentParamValues == null) {
			segments = getSegmentParamValues(segmentParam);
		} else {
			segments = segmentParamValues;
		}

		if (segmentParam == null) {
			System.out.println("No segment parameter was set.");
		} else {
			System.out.println("Segment parameter values for segment parameter '" + segmentParam + "' (" + segments.size() + "): " + segments);
		}

		// create a copy of all samples which will then be used to identify samples which do not belong to a segment
		final List<Sample> unsegmentedSamples = new ArrayList<Sample>(samples);

		// initialize map
		final Map<String, BinList> bins = new TreeMap<String, BinList>(new NaturalOrderComparator());
		for (final String segment : segments) {
			final BinList binsForSegment = new BinList();
			bins.put(segment, binsForSegment);
		}

		for (final String curSegmentParamValue : segments) {
			String testSegmentParamValue = curSegmentParamValue;
			if (testSegmentParamValue.endsWith(".*")) {
				testSegmentParamValue = testSegmentParamValue.substring(0, curSegmentParamValue.length() - 2);
			}
			final BinList binsForSegment = calculateBinsForSegment(	testSegmentParamValue, samples, histogram, lowerBound, upperBound, numBins,
																	useLogScaleXAxis, segmentParam, segments, unsegmentedSamples);
			bins.put(curSegmentParamValue, binsForSegment);
		}
		// finally add a set of bins for all samples that are not applying to any segment.
		if (unsegmentedSamples.size() > 0) {
			final BinList binsForSegment = calculateBinsForSegment(	Histogram.NO_SEGMENT_PARAM, unsegmentedSamples, histogram, lowerBound, upperBound, numBins,
																	useLogScaleXAxis, segmentParam, segments, new ArrayList<Sample>());
			bins.put(Histogram.NO_SEGMENT_PARAM, binsForSegment);
		}

		histogram.setBins(bins);
	}

	private BinList calculateBinsForSegment(final String segmentParamValue, final List<Sample> samples, final Histogram histogram, final int lowerBound,
			final int upperBound, final int initialNumBins, final boolean useLogScaleXAxis, final String segmentParam, final Set<String> segments,
			final List<Sample> unsegmentedSamples) {

		final BinList binsForSegment = new BinList();
		int numAppliedSamples = 0;
		final Iterator<Sample> iterator = samples.iterator();

		int curBound = lowerBound;
		int startNormalBins = 0;
		if (histogram.getMin() < lowerBound) {
			int curFrequency = 0;
			int lastResponseTime = 0;

			while (iterator.hasNext() && lastResponseTime < curBound) {
				final Sample curSample = iterator.next();
				lastResponseTime = curSample.getResponseTime();
				if (segmentApplies(segmentParamValue, curSample, segmentParam, segments)) {
					unsegmentedSamples.remove(curSample);
					numAppliedSamples++;
					curFrequency++;
				}
			}
			final Bin bin = new Bin(curBound, curFrequency);

			startNormalBins = 1;
			binsForSegment.addBin(bin);
		}

		final int numBins = initialNumBins + startNormalBins;
		for (int i = startNormalBins; i < numBins; i++) {
			int curFrequency = 0;
			int lastResponseTime = 0;
			curBound = getCurrentBound(i, lowerBound, upperBound, numBins, useLogScaleXAxis);

			while (iterator.hasNext() && lastResponseTime < curBound) {
				final Sample curSample = iterator.next();
				lastResponseTime = curSample.getResponseTime();
				if (segmentApplies(segmentParamValue, curSample, segmentParam, segments)) {
					unsegmentedSamples.remove(curSample);
					numAppliedSamples++;
					curFrequency++;
				}
			}
			final Bin bin = new Bin(curBound, curFrequency);

			binsForSegment.addBin(bin);
		}

		if (curBound < histogram.getMax()) {
			int curFrequency = 0;
			int lastResponseTime = 0;
			curBound = histogram.getMax();

			while (iterator.hasNext() && lastResponseTime < curBound) {
				final Sample curSample = iterator.next();
				lastResponseTime = curSample.getResponseTime();
				if (segmentApplies(segmentParamValue, curSample, segmentParam, segments)) {
					unsegmentedSamples.remove(curSample);
					numAppliedSamples++;
					curFrequency++;
				}
			}
			final Bin bin = new Bin(curBound, curFrequency);

			binsForSegment.addBin(bin);
		}
		System.out.println("Processed " + numAppliedSamples + " samples for segment parameter value " + segmentParamValue);
		return binsForSegment;
	}

	private int getCurrentBound(final int i, final int lowerBound, final int upperBound, final int numBins, final boolean useLogScaleXAxis) {
		int curBound;
		if (useLogScaleXAxis) {
			final int logStart = Math.max(1, lowerBound);
			final double logBase = Math.exp((Math.log(upperBound) - Math.log(logStart)) / numBins);
			curBound = (int) Math.floor(logStart * Math.pow(logBase, i + 1));
		} else {
			final double boundStep = (double) (upperBound - lowerBound) / (double) numBins;
			curBound = lowerBound + (int) Math.ceil((i + 1) * boundStep);
		}
		return curBound;
	}

	private void mergeSparseBins(final Histogram histogram, final List<String> ordering, final int sparseBound) {
		final List<List<String>> groups = new ArrayList<List<String>>();
		List<String> group = new ArrayList<String>();
		int curTotal = 0;
		for (int i = 0; i < ordering.size(); i++) {
			final String segment = ordering.get(i);
			if (Histogram.NO_SEGMENT_PARAM.equals(segment)) {
				groups.add(Arrays.asList(Histogram.NO_SEGMENT_PARAM));
				continue;
			}
			group.add(segment);

			int nextTotalFrequency = 0;
			if (i < ordering.size() - 1) {
				nextTotalFrequency = histogram.getBins().get(ordering.get(i + 1)).getTotalFrequency();
			}

			curTotal += histogram.getBins().get(segment).getTotalFrequency();
			if (curTotal + nextTotalFrequency >= sparseBound) {
				groups.add(group);
				group = new ArrayList<String>();
				curTotal = 0;
			}
		}
		if (!group.isEmpty()) {
			groups.add(group);
		}

		mergeGroups(histogram, groups);
	}

	private List<String> getTotalFrequencyOrdering(final Histogram histogram, final boolean reverse) {
		final Map<Integer, List<String>> totalFrequencyOrderingMap;
		if (reverse) {
			totalFrequencyOrderingMap = new TreeMap<Integer, List<String>>(Collections.reverseOrder());
		} else {
			totalFrequencyOrderingMap = new TreeMap<Integer, List<String>>();
		}
		for (final Entry<String, BinList> binEntry : histogram.getBins().entrySet()) {
			final String segment = binEntry.getKey();
			final BinList bins = binEntry.getValue();
			final Integer key = new Integer(bins.getTotalFrequency());
			List<String> values = totalFrequencyOrderingMap.get(key);
			if (values == null) {
				values = new ArrayList<String>();
			}
			values.add(segment);
			totalFrequencyOrderingMap.put(key, values);
		}

		final List<String> totalFrequencyOrdering = new ArrayList<String>();
		for (final List<String> segments : totalFrequencyOrderingMap.values()) {
			for (final String segment : segments) {
				totalFrequencyOrdering.add(segment);
			}
		}
		return totalFrequencyOrdering;
	}

	private List<String> getSegmentOrdering(final Histogram histogram) {
		final List<String> segmentOrdering = new ArrayList<String>();
		for (final String segment : histogram.getBins().keySet()) {
			segmentOrdering.add(segment);
		}
		return segmentOrdering;
	}

	private void mergeGroups(final Histogram histogram, final List<List<String>> groups) {
		final Map<String, BinList> oldBins = histogram.getBins();
		final Map<String, BinList> mergedBins = new TreeMap<String, BinList>(new NaturalOrderComparator());

		System.out.println("Merging groups...");
		for (final List<String> group : groups) {
			if (group.size() > 1) {
				String currentSegmentName = group.get(0);
				BinList mergedBinList = new BinList(oldBins.get(currentSegmentName));
				final Set<String> mergedSegmentParamNames = new TreeSet<String>(new NaturalOrderComparator());
				mergedSegmentParamNames.add(currentSegmentName);
				for (int i = 1; i < group.size(); i++) {
					currentSegmentName = group.get(i);
					try {
						mergedBinList = mergeBinLists(mergedBinList, oldBins.get(currentSegmentName));
					} catch (final IndexOutOfBoundsException e) {
						System.err.println("Could not merge bin lists because of unequal list sizes. Cancelling merge. Will use unmerged histogram. "
								+ e.getMessage());
						return;
					}
					mergedSegmentParamNames.add(currentSegmentName);
				}
				mergedBins.put(StringUtils.join(mergedSegmentParamNames, ", "), mergedBinList);
				System.out
						.println("Processed " + mergedBinList.getTotalFrequency() + " samples for merged segment parameter values " + mergedSegmentParamNames);
			} else if (group.size() == 1) {
				final String segmentParamName = group.get(0);
				mergedBins.put(segmentParamName, oldBins.get(segmentParamName));
				System.out.println("Processed " + oldBins.get(segmentParamName).getTotalFrequency() + " samples for segment parameter value "
						+ segmentParamName);
			}
		}
		histogram.setBins(mergedBins);
	}

	private BinList mergeBinLists(final BinList list1, final BinList list2) {
		final BinList mergedBinList = new BinList();
		for (int i = 0; i < list1.getNumBins(); i++) {
			final Bin bin1 = list1.getBin(i);
			final Bin bin2 = list2.getBin(i);
			final Bin newBin = new Bin(bin1.getBound(), bin1.getFrequency() + bin2.getFrequency());
			mergedBinList.addBin(newBin);
		}
		return mergedBinList;
	}

	private boolean segmentApplies(final String curSegmentParamValue, final Sample curSample, final String segmentParamName,
			final Set<String> segmentParamValues) {
		boolean segmentParamApplies = false;

		if (Histogram.NO_SEGMENT_PARAM.equals(curSegmentParamValue)) {
			segmentParamApplies = true;
		} else if (SEGMENT_PARAM_OPTION_CACHED.equals(segmentParamName)) {
			if ((curSample.getCacheAge() > 0 && FF_SEGMENT_PARAM_VALUE_CACHED.equals(curSegmentParamValue))
					|| (curSample.getCacheAge() == 0 && FF_SEGMENT_PARAM_VALUE_UNCACHED.equals(curSegmentParamValue))) {
				segmentParamApplies = true;
			}
		} else if (SEGMENT_PARAM_OPTION_TIMEOUT.equals(segmentParamName)) {
			if ((curSample.isTimeOut() && FF_SEGMENT_PARAM_VALUE_TIMEOUT.equals(curSegmentParamValue))
					|| (!curSample.isTimeOut() && FF_SEGMENT_PARAM_VALUE_COMPLETE.equals(curSegmentParamValue))) {
				segmentParamApplies = true;
			}
		} else {
			final String urlQuery = curSample.getUrl().getQuery();
			if (StringUtils.isNotEmpty(urlQuery)) {
				int numFilters = 0;
				final String[] params = urlQuery.split("&");
				for (final String param : params) {
					final String[] keyValuePair = param.split("=");
					if (keyValuePair.length >= 2) {
						final String key = keyValuePair[0];
						final String value = keyValuePair[1];
						if (SEGMENT_PARAM_OPTION_NUM_QUERY_WORDS.equals(segmentParamName) && FF_QUERY_PARAMETER_NAME.equals(key)) {
							final String[] queryWords = getQueryWords(value);
							if (queryWords.length == Integer.parseInt(curSegmentParamValue)) {
								segmentParamApplies = true;
								break;
							}
						} else if (SEGMENT_PARAM_OPTION_NUM_FILTERS.equals(segmentParamName) && key.startsWith(FF_FILTER_PARAMETER_PREFIX)) {
							numFilters++;
						} else if (segmentParamName.equals(key) && value.startsWith(curSegmentParamValue)) {
							segmentParamApplies = true;
							break;
						}
					}
				}
				if (SEGMENT_PARAM_OPTION_NUM_FILTERS.equals(segmentParamName) && numFilters == Integer.parseInt(curSegmentParamValue)) {
					segmentParamApplies = true;
				}
			}
		}

		return segmentParamApplies;
	}

	private Set<String> getSegmentParamValues(final String segmentParamName) {
		final Set<String> segmentParamValues = new TreeSet<String>(new NaturalOrderComparator());
		if (StringUtils.isNotEmpty(segmentParamName)) {
			for (final Sample sample : samples) {
				if (SEGMENT_PARAM_OPTION_CACHED.equals(segmentParamName)) {
					if (sample.getCacheAge() > 0) {
						segmentParamValues.add(FF_SEGMENT_PARAM_VALUE_CACHED);
					} else {
						segmentParamValues.add(FF_SEGMENT_PARAM_VALUE_UNCACHED);
					}
				} else if (SEGMENT_PARAM_OPTION_TIMEOUT.equals(segmentParamName)) {
					if (sample.isTimeOut()) {
						segmentParamValues.add(FF_SEGMENT_PARAM_VALUE_TIMEOUT);
					} else {
						segmentParamValues.add(FF_SEGMENT_PARAM_VALUE_COMPLETE);
					}
				} else {
					final String urlQuery = sample.getUrl().getQuery();
					if (StringUtils.isNotEmpty(urlQuery)) {
						int numFilters = 0;
						final String[] params = urlQuery.split("&");
						for (final String param : params) {
							final String[] keyValuePair = param.split("=");
							if (keyValuePair.length >= 2) {
								final String key = keyValuePair[0];
								final String value = keyValuePair[1];
								if (SEGMENT_PARAM_OPTION_NUM_QUERY_WORDS.equals(segmentParamName) && FF_QUERY_PARAMETER_NAME.equals(key)) {
									final String[] queryWords = getQueryWords(value);
									segmentParamValues.add(Integer.toString(queryWords.length));
								} else if (SEGMENT_PARAM_OPTION_NUM_FILTERS.equals(segmentParamName) && key.startsWith(FF_FILTER_PARAMETER_PREFIX)) {
									numFilters++;
								} else if (segmentParamName.equals(key)) {
									segmentParamValues.add(value);
								}
							}
						}
						if (SEGMENT_PARAM_OPTION_NUM_FILTERS.equals(segmentParamName)) {
							segmentParamValues.add(Integer.toString(numFilters));
						}
					}
				}
			}
		}
		return segmentParamValues;
	}

	private String[] getQueryWords(final String value) {
		try {
			final String ffSearchQuery = URLDecoder.decode(value, "UTF-8").trim();
			return ffSearchQuery.split("\\W+");
		} catch (final UnsupportedEncodingException e) {
			System.err
					.println("WTF: Your system does not support UTF-8 encoding. The url parameter cannot be decoded. Continuing without considering current value. "
							+ e.getMessage());
		}
		return null;
	}

	public enum DiagramType {
		Histogram, LineGraph
	}
}
