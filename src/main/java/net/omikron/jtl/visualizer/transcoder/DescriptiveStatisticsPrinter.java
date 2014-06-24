package net.omikron.jtl.visualizer.transcoder;

import java.util.List;

import net.omikron.jtl.visualizer.JtlResponseReport;
import net.omikron.jtl.visualizer.histogram.Histogram;
import net.omikron.jtl.visualizer.sample.Sample;

import org.w3c.dom.svg.SVGDocument;

public class DescriptiveStatisticsPrinter extends AbstractSampleRenderer {

	public DescriptiveStatisticsPrinter(final List<Sample> samples) {
		super(samples);
	}

	@Override
	public void render(final SVGDocument svgDocument) {
		this.svgDocument = svgDocument;
		this.svgRoot = svgDocument.getDocumentElement();

		renderFrame(title);

		if (samples != null && !samples.isEmpty()) {
			final Histogram histogram = new Histogram();
			calculateDescriptiveStats(histogram);
			printDescriptiveStats(histogram);
		} else {
			printEmptyMessage();
		}
	}

	private void printDescriptiveStats(final Histogram h) {
		final StringBuilder s = new StringBuilder();

		final int numSamples = samples.size();
		final long duration = h.getEndOfLastRequest() - h.getStartOfFirstRequest();

		s.append("Samples: ");
		s.append(numSamples);
		s.append("\tTotal time: ");
		s.append(JtlResponseReport.formatMillisToMinutes(duration));
		s.append("\tThroughput: ");
		s.append(String.format("%1$.2f", (double) (numSamples * 1000) / (double) duration));
		s.append("\tAverage: ");
		s.append(h.getAverage());

		s.append("\tMin: ");
		s.append(h.getMin());
		s.append("\tLow: ");
		s.append(h.getQuartLow());
		s.append("\tMedian: ");
		s.append(h.getMedian());
		s.append("\tUp: ");
		s.append(h.getQuartUp());
		s.append("\tMax: ");
		s.append(h.getMax());

		final String descriptiveStats = s.toString();
		svgRoot.appendChild(getText(descriptiveStats, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, TEXT_STYLE_P, "middle"));
		System.out.println(descriptiveStats);
	}

	private void printEmptyMessage() {
		svgRoot.appendChild(getText(getEmptyMsg(), CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, TEXT_STYLE_P, "middle"));
		System.out.println(getEmptyMsg());
	}

	@Override
	protected String getEmptyMsg() {
		return "The JTL did not contain any samples to calculate descriptive statistics from.";
	}

}
