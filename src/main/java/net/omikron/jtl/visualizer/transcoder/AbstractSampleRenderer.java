package net.omikron.jtl.visualizer.transcoder;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.omikron.jtl.visualizer.histogram.Histogram;
import net.omikron.jtl.visualizer.sample.Sample;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public abstract class AbstractSampleRenderer {

	protected static final int				OFFSET_AXIS				= 4;
	protected static final int				CANVAS_WIDTH			= 1000;
	protected static final int				CANVAS_HEIGHT			= 600;
	protected static final int				CANVAS_MARGIN			= 100;
	protected static final int				DATA_LABEL_SEP			= 6;

	protected static final int				OFFSET_X				= 0;
	protected static final int				OFFSET_Y				= 0;

	protected static final int				NUMBER_OF_AXIS_LABELS	= 6;

	protected static final String			FONT_DEF				= "font-family: Helvetica, Arial, sans; font-size: 10pt;";
	protected static final String			TEXT_STYLE_P			= "font-size: 10pt; fill: #111111";
	protected static final String			TEXT_STYLE_T			= "font-size: 8pt; fill: #111111";
	protected static final String			TEXT_STYLE_H1			= "font-size: 20pt; fill: #333333";
	protected static final String			LINE_STYLE				= "fill:none;stroke-width:2.0;";

	protected static final String			SVG_WIDTH_ATTRIBUTE		= "width";
	protected static final String			SVG_HEIGHT_ATTRIBUTE	= "height";
	protected static final String			SVG_VIEW_BOX_ATTRIBUTE	= "viewBox";

	protected final List<Sample>			samples;

	protected String						title;
	protected String						version;
	protected String						date;
	protected boolean						useLogScaleXAxis		= false;
	protected boolean						useLogScaleYAxis		= false;
	protected int							xAxisMin				= -1;
	protected int							xAxisMax				= -1;
	protected int							yAxisMax				= -1;

	protected transient SVGDocument			svgDocument;
	protected transient Element				svgRoot;

	protected transient final NumberFormat	numberFormat0Digits;
	protected transient final NumberFormat	numberFormat2Digits;
	protected transient final NumberFormat	numberFormatPercent;

	/**
	 * Constructs a sample painter which holds the samples used by implementing painters.
	 * 
	 * @param samples
	 */
	public AbstractSampleRenderer(final List<Sample> samples) {
		super();
		this.samples = samples;
		Collections.sort(samples, Sample.ORDER_BY_RESPONSE_TIME_ASC);

		this.numberFormat0Digits = NumberFormat.getInstance(Locale.US);
		this.numberFormat0Digits.setMinimumFractionDigits(0);
		this.numberFormat0Digits.setMaximumFractionDigits(0);
		this.numberFormat2Digits = NumberFormat.getInstance(Locale.US);
		this.numberFormat2Digits.setMinimumFractionDigits(2);
		this.numberFormat2Digits.setMaximumFractionDigits(2);
		this.numberFormatPercent = NumberFormat.getPercentInstance(Locale.US);
		this.numberFormatPercent.setMinimumFractionDigits(2);
	}

	public abstract void render(final SVGDocument svgDocument);

	/**
	 * Sets yAxisMax
	 * 
	 * @param yAxisMax
	 */
	protected void setYAxisMax(final int yAxisMax) {
		this.yAxisMax = yAxisMax;
	}

	/**
	 * @return yAxisMax
	 */
	protected int getYAxisMax() {
		return yAxisMax;
	}

	/**
	 * @return The title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * @return The version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(final String version) {
		this.version = version;
	}

	/**
	 * @return The date.
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date The date to set.
	 */
	public void setDate(final String date) {
		this.date = date;
	}

	/**
	 * @return The useLogScaleXAxis.
	 */
	public boolean isUseLogScaleXAxis() {
		return useLogScaleXAxis;
	}

	/**
	 * @param useLogScaleXAxis The useLogScaleXAxis to set.
	 */
	public void setUseLogScaleXAxis(final boolean useLogScaleXAxis) {
		this.useLogScaleXAxis = useLogScaleXAxis;
	}

	/**
	 * @return The useLogScaleYAxis.
	 */
	public boolean isUseLogScaleYAxis() {
		return useLogScaleYAxis;
	}

	/**
	 * @param useLogScaleYAxis The useLogScaleYAxis to set.
	 */
	public void setUseLogScaleYAxis(final boolean useLogScaleYAxis) {
		this.useLogScaleYAxis = useLogScaleYAxis;
	}

	/**
	 * @return The xAxisMin.
	 */
	public int getXAxisMin() {
		return xAxisMin;
	}

	/**
	 * @param xAxisMin The xAxisMin to set.
	 */
	public void setXAxisMin(final int xAxisMin) {
		this.xAxisMin = xAxisMin;
	}

	/**
	 * @return The yAxisMax.
	 */
	public int getXAxisMax() {
		return xAxisMax;
	}

	/**
	 * @param yAxisMax The yAxisMax to set.
	 */
	public void setXAxisMax(final int xAxisMax) {
		this.xAxisMax = xAxisMax;
	}

	protected abstract String getEmptyMsg();

	protected void renderEmptyMsg() {
		svgRoot.appendChild(getText(getEmptyMsg(), CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, TEXT_STYLE_P, "middle"));
	}

	protected void addCSS() {
		final Element cssElement = svgDocument.createElement("style");
		cssElement.setAttribute("type", "text/css");
		final StringBuilder css = new StringBuilder();
		css.append("\ntext {\n  ");
		css.append(FONT_DEF);
		css.append("\n}\n");
		final CDATASection cssData = svgDocument.createCDATASection(css.toString());
		cssElement.appendChild(cssData);
		svgRoot.appendChild(cssElement);
	}

	protected void addMarkers() {
		final Element markerTriangle = svgDocument.createElement("marker");
		markerTriangle.setAttribute("id", "arrowHeadTriangle");
		markerTriangle.setAttribute("viewBox", "0 0 10 10");
		markerTriangle.setAttribute("refX", "0");
		markerTriangle.setAttribute("refY", "5");
		markerTriangle.setAttribute("markerUnits", "strokeWidth");
		markerTriangle.setAttribute("markerWidth", "10");
		markerTriangle.setAttribute("markerHeight", "10");
		markerTriangle.setAttribute("orient", "auto");

		final Element patTriangle = svgDocument.createElement("path");
		patTriangle.setAttribute("d", "M 0 0 L 10 5 L 0 10 z");

		markerTriangle.appendChild(patTriangle);
		svgRoot.appendChild(markerTriangle);

		final Element markerDagger = svgDocument.createElement("marker");
		markerDagger.setAttribute("id", "arrowHeadDagger");
		markerDagger.setAttribute("viewBox", "0 0 10 10");
		markerDagger.setAttribute("refX", "10");
		markerDagger.setAttribute("refY", "5");
		markerDagger.setAttribute("markerUnits", "strokeWidth");
		markerDagger.setAttribute("markerWidth", "10");
		markerDagger.setAttribute("markerHeight", "10");
		markerDagger.setAttribute("orient", "auto");

		final Element pathDagger = svgDocument.createElement("path");
		pathDagger.setAttribute("d", "M 0 0 L 10 5 L 0 10");
		pathDagger.setAttribute("style", "fill:none;stroke:black;stroke-width:1.0;");

		markerDagger.appendChild(pathDagger);
		svgRoot.appendChild(markerDagger);
	}

	protected void renderFrame(final String title) {
		final Element bg = svgDocument.createElement("g");
		bg.setAttribute("id", "background");

		bg.appendChild(getRectangle(OFFSET_X, OFFSET_Y, CANVAS_WIDTH - 1, CANVAS_HEIGHT - 1, "fill: white; stroke: black; stroke-width: 1.0"));
		bg.appendChild(getText(title, CANVAS_WIDTH / 2, CANVAS_MARGIN / 3, TEXT_STYLE_H1, "middle"));

		svgRoot.appendChild(bg);
	}

	protected void setDimensions() {
		svgRoot.setAttribute(SVG_WIDTH_ATTRIBUTE, Integer.toString(CANVAS_WIDTH));
		svgRoot.setAttribute(SVG_HEIGHT_ATTRIBUTE, Integer.toString(CANVAS_HEIGHT));
		svgRoot.setAttribute(SVG_VIEW_BOX_ATTRIBUTE, String.valueOf(OFFSET_X) + ' ' + OFFSET_Y + ' ' + CANVAS_WIDTH + ' ' + CANVAS_HEIGHT);
	}

	protected void calculateDescriptiveStats(final Histogram histogram) {
		long startOfFirstRequest = Long.MAX_VALUE;
		long endOfLastRequest = Long.MIN_VALUE;

		final int min = 0;
		final int max = samples.size() - 1;
		final int quartLow = (max - min) / 4;
		final int median = (max - min) / 2;
		final int quartUp = ((max - min) * 3) / 4;
		final int quantil95 = ((max - min) * 95) / 100;

		histogram.setMax(samples.get(max).getResponseTime());

		int total = 0;

		for (int i = 0; i < samples.size(); i++) {
			final Sample sample = samples.get(i);
			final int curResponseTime = sample.getResponseTime();
			total += curResponseTime;

			if (i == min) histogram.setMin(curResponseTime);
			if (i == quartLow) histogram.setQuartLow(curResponseTime);
			if (i == median) histogram.setMedian(curResponseTime);
			if (i == quartUp) histogram.setQuartUp(curResponseTime);
			if (i == quantil95) histogram.setQuantil95(curResponseTime);
			if (i == max) histogram.setMax(curResponseTime);

			final long timestamp = sample.getStartOfRequest().getTime();
			final int responseTime = sample.getResponseTime();
			if (timestamp < startOfFirstRequest) startOfFirstRequest = timestamp;
			if (timestamp + responseTime > endOfLastRequest) endOfLastRequest = timestamp + responseTime;
		}

		histogram.setAverage(total / samples.size());

		histogram.setStartOfFirstRequest(startOfFirstRequest);
		histogram.setEndOfLastRequest(endOfLastRequest);
	}

	protected int getOptimalNumberOfLabelsForAxis(final double maxValue, final int minStep) {
		double dist = Double.MAX_VALUE;
		int optimalNumber = NUMBER_OF_AXIS_LABELS;
		for (int i = NUMBER_OF_AXIS_LABELS - 2; i < NUMBER_OF_AXIS_LABELS + 2; i++) {
			final int tempStepFrac = ((int) (maxValue / i)) / minStep;
			final int step = Math.max(1, tempStepFrac) * minStep;
			int sum = 0;
			int curNum = 0;
			do {
				sum += step;
				curNum++;
			} while (sum <= maxValue - step);
			if (maxValue - sum < dist || (maxValue - sum == dist && Math.abs(NUMBER_OF_AXIS_LABELS - optimalNumber) > Math.abs(NUMBER_OF_AXIS_LABELS - i))) {
				dist = maxValue - sum;
				optimalNumber = curNum;
			}
		}

		return optimalNumber;
	}

	protected int getStepForAxis(final int optimalNumber, final double maxValue, final int minStep) {
		return (int) Math.max(1, (maxValue / optimalNumber) / minStep) * minStep;
	}

	protected Element getText(final String text, final double x, final double y, final String style, final String textAnchor) {
		final Element textElement = svgDocument.createElement("text");
		textElement.setAttribute("x", Double.toString(x));
		textElement.setAttribute("y", Double.toString(y));
		if (StringUtils.isNotEmpty(style)) {
			textElement.setAttribute("style", style);
		}
		if (StringUtils.isNotEmpty(textAnchor)) {
			textElement.setAttribute("text-anchor", textAnchor);
		}
		textElement.appendChild(svgDocument.createTextNode(text));
		return textElement;
	}

	protected Element getRectangle(final double x, final double y, final double width, final double height, final String style) {
		final Element rectangle = svgDocument.createElement("rect");
		rectangle.setAttribute("x", Double.toString(x));
		rectangle.setAttribute("y", Double.toString(y));
		rectangle.setAttribute("width", Double.toString(width));
		rectangle.setAttribute("height", Double.toString(height));
		if (StringUtils.isNotEmpty(style)) {
			rectangle.setAttribute("style", style);
		}
		return rectangle;
	}

	protected Element getCircle(final double cx, final double cy, final double r, final String style) {
		final Element circle = svgDocument.createElement("circle");
		circle.setAttribute("cx", Double.toString(cx));
		circle.setAttribute("cy", Double.toString(cy));
		circle.setAttribute("r", Double.toString(r));
		if (StringUtils.isNotEmpty(style)) {
			circle.setAttribute("style", style);
		}
		return circle;
	}

	protected Element getArrow(final double x1, final double y1, final double x2, final double y2, final String fill, final String stroke,
			final double strokeWidth) {
		final Element arrow = getLine(x1, y1, x2, y2, fill, stroke, strokeWidth);
		arrow.setAttribute("marker-end", "url(#arrowHeadDagger");
		return arrow;
	}

	protected Element getLine(final double x1, final double y1, final double x2, final double y2, final String fill, final String stroke,
			final double strokeWidth) {
		final Element line = svgDocument.createElement("line");
		line.setAttribute("x1", Double.toString(x1));
		line.setAttribute("y1", Double.toString(y1));
		line.setAttribute("x2", Double.toString(x2));
		line.setAttribute("y2", Double.toString(y2));
		if (StringUtils.isNotEmpty(fill)) {
			line.setAttribute("fill", fill);
		}
		if (StringUtils.isNotEmpty(stroke)) {
			line.setAttribute("stroke", stroke);
		}
		if (strokeWidth > 0.0) {
			line.setAttribute("stroke-width", Double.toString(strokeWidth));
		}
		return line;
	}

	protected Element getPath(final String pathData, final String stroke, final String style) {
		final Element path = svgDocument.createElement("path");
		path.setAttribute("d", pathData);
		if (StringUtils.isNotEmpty(stroke)) {
			path.setAttribute("stroke", stroke);
		}
		if (StringUtils.isNotEmpty(style)) {
			path.setAttribute("style", style);
		}

		return path;
	}
}
