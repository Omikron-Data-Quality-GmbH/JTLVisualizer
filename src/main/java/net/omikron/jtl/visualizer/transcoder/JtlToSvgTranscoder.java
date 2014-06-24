package net.omikron.jtl.visualizer.transcoder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.omikron.jtl.visualizer.io.JtlReader;
import net.omikron.jtl.visualizer.sample.Sample;
import net.omikron.jtl.visualizer.transcoder.SampleHistogramRenderer.DiagramType;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.DefaultErrorHandler;
import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.TranscodingHints.Key;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.SAXException;

public class JtlToSvgTranscoder implements Transcoder {

	public static final TranscodingHints.Key	KEY_NUM_BINS			= new StringKey();
	public static final TranscodingHints.Key	KEY_EXCLUDES			= new StringKey();
	public static final TranscodingHints.Key	KEY_TITLE				= new StringKey();
	public static final TranscodingHints.Key	KEY_DIAGRAM_TYPE		= new StringKey();
	public static final TranscodingHints.Key	KEY_SEGMENT_PARAM		= new StringKey();
	public static final TranscodingHints.Key	KEY_GROUP_SEGMENTS		= new BooleanKey();
	public static final TranscodingHints.Key	KEY_X_AXIS_MIN			= new StringKey();
	public static final TranscodingHints.Key	KEY_X_AXIS_MAX			= new StringKey();
	public static final TranscodingHints.Key	KEY_Y_AXIS_MAX			= new StringKey();
	public static final TranscodingHints.Key	KEY_TOOLTIPS			= new BooleanKey();
	public static final TranscodingHints.Key	KEY_LOG_SCALE_X			= new BooleanKey();
	public static final TranscodingHints.Key	KEY_LOG_SCALE_Y			= new BooleanKey();
	public static final TranscodingHints.Key	KEY_RELATIVE_DATA_LABEL	= new BooleanKey();
	public static final TranscodingHints.Key	KEY_RESULT_COUNT		= new BooleanKey();
	public static final TranscodingHints.Key	KEY_VERSION				= new StringKey();	;
	public static final TranscodingHints.Key	KEY_BUILD_NR			= new StringKey();	;

	private static final int					INDENT_AMOUNT			= 4;

	private TranscodingHints					hints;

	private ErrorHandler						errorHandler;

	/**
	 * Creates a new JtlToSvgTranscoder.
	 */
	public JtlToSvgTranscoder() {
		setErrorHandler(new DefaultErrorHandler());
		hints = new TranscodingHints();
	}

	public void addTranscodingHint(final Key key, final Object value) {
		this.hints.put(key, value);
	}

	public TranscodingHints getTranscodingHints() {
		return hints;
	}

	public void removeTranscodingHint(final Key hintKey) {
		hints.remove(hintKey);
	}

	public void setTranscodingHints(final Map hints) {
		for (final Object entryObj : hints.entrySet()) {
			final Entry<Object, Object> entry = (Entry<Object, Object>) entryObj;
			hints.put(entry.getKey(), entry.getValue());
		}
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(final ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public void setTranscodingHints(final TranscodingHints hints) {
		this.hints = hints;
	}

	public void transcode(final TranscoderInput input, final TranscoderOutput output) throws TranscoderException {
		List<Sample> samples = null;
		try {
			final JtlReader jtlReader = new JtlReader();
			samples = jtlReader.readSamples(loadDocument(input), (String) hints.get(KEY_EXCLUDES), true);
			renderAndSaveSVG(samples, output);
		} catch (final Exception e) {
			throw new TranscoderException(e);
		}
	}

	protected Document loadDocument(final TranscoderInput input) throws ParserConfigurationException, SAXException, IOException {
		Document document = input.getDocument();
		if (document == null) {
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			document = dBuilder.parse(input.getURI());
		}
		return document;
	}

	private void renderAndSaveSVG(final List<Sample> samples, final TranscoderOutput output) throws TranscoderException, TransformerException,
			URISyntaxException, IOException {
		// Use SVGDOMImplementation to generate SVG content
		final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		final DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		final Document svgDocument = impl.createDocument(svgNS, "svg", null);

		final AbstractSampleRenderer renderer;
		if (hints.containsKey(KEY_DIAGRAM_TYPE) && "stats".equals(hints.get(KEY_DIAGRAM_TYPE))) {
			renderer = new DescriptiveStatisticsPrinter(samples);
		} else if (hints.containsKey(KEY_DIAGRAM_TYPE) && "scatter".equals(hints.get(KEY_DIAGRAM_TYPE))) {
			renderer = new SampleScatterRenderer(samples);
		} else {
			renderer = new SampleHistogramRenderer(samples);
		}
		setRenderHints(renderer);

		renderer.render((SVGDocument) svgDocument);

		final Source xmlInput = new DOMSource(svgDocument);

		final StreamResult xmlOutput = new StreamResult(output.getOutputStream());

		final Transformer transformer = setupTransformer();

		transformer.transform(xmlInput, xmlOutput);
	}

	protected Transformer setupTransformer() throws TransformerFactoryConfigurationError, TransformerConfigurationException {
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(INDENT_AMOUNT));
		return transformer;
	}

	private void setRenderHints(final AbstractSampleRenderer renderer) {

		if (hints.containsKey(KEY_TITLE)) {
			renderer.setTitle(((String) hints.get(KEY_TITLE)));
		}
		if (hints.containsKey(KEY_VERSION)) {
			renderer.setVersion(((String) hints.get(KEY_VERSION)));
		}
		if (hints.containsKey(KEY_BUILD_NR)) {
			renderer.setDate(((String) hints.get(KEY_BUILD_NR)));
		}
		if (hints.containsKey(KEY_LOG_SCALE_X)) {
			renderer.setUseLogScaleXAxis(((Boolean) hints.get(KEY_LOG_SCALE_X)).booleanValue());
		}
		if (hints.containsKey(KEY_LOG_SCALE_Y)) {
			renderer.setUseLogScaleYAxis(((Boolean) hints.get(KEY_LOG_SCALE_Y)).booleanValue());
		}
		if (hints.containsKey(KEY_X_AXIS_MIN)) {
			final String xAxisMinString = (String) hints.get(KEY_X_AXIS_MIN);
			try {
				renderer.setXAxisMin(Integer.parseInt(xAxisMinString));
			} catch (final NumberFormatException e) {
				System.err.println("The provided xAxisMin parameter '" + xAxisMinString
						+ "' is not a valid integer. The default will be used (i.e. the minimum response time of the dataset).");
			}
		}
		if (hints.containsKey(KEY_X_AXIS_MAX)) {
			final String xAxisMaxString = (String) hints.get(KEY_X_AXIS_MAX);
			if (renderer instanceof SampleHistogramRenderer && SampleHistogramRenderer.X_AXIS_OPTION_DOUBLE_UP_QUART.equals(xAxisMaxString)) {
				((SampleHistogramRenderer) renderer).setUseXAxisDoubleUpQuart(true);
			} else {
				try {
					renderer.setXAxisMax(Integer.parseInt(xAxisMaxString));
				} catch (final NumberFormatException e) {
					System.err.println("The provided xAxisMax parameter '" + xAxisMaxString + "' neither is a valid integer nor is it the option '"
							+ SampleHistogramRenderer.X_AXIS_OPTION_DOUBLE_UP_QUART
							+ "'. The default will be used (i.e. the maximum response time of the dataset).");
				}
			}
		}
		if (hints.containsKey(KEY_Y_AXIS_MAX)) {
			final String yAxisMax = (String) hints.get(KEY_Y_AXIS_MAX);
			try {
				renderer.setYAxisMax(Integer.parseInt(yAxisMax));
			} catch (final NumberFormatException e) {
				System.err.println("The provided yAxisMax parameter '" + yAxisMax
						+ "' is not a valid integer. The default will be used (the maximum frequency).");
			}
		}
		if (renderer instanceof SampleHistogramRenderer) {
			final SampleHistogramRenderer histogramRenderer = (SampleHistogramRenderer) renderer;
			if (hints.containsKey(KEY_NUM_BINS)) {
				final String numBinsString = (String) hints.get(KEY_NUM_BINS);
				try {
					histogramRenderer.setNumBins(Integer.parseInt(numBinsString));
				} catch (final NumberFormatException e) {
					System.err.println("The provided bins parameter '" + numBinsString + "' is not a valid integer. The default ("
							+ SampleHistogramRenderer.DEFAULT_NUMBER_OF_BINS + ") will be used.");
				}
			}
			if (hints.containsKey(KEY_DIAGRAM_TYPE)) {
				final String diagramTypeString = (String) hints.get(KEY_DIAGRAM_TYPE);
				if (diagramTypeString.equals("line")) {
					histogramRenderer.setDiagramType(DiagramType.LineGraph);
				}
				// else: do nothing...use default diagram type, i.e. draw a histogram
			}
			if (hints.containsKey(KEY_SEGMENT_PARAM)) {
				final String segmentParamArg = (String) hints.get(KEY_SEGMENT_PARAM);
				final String[] segments = segmentParamArg.split("=");
				if (segments.length >= 1) {
					histogramRenderer.setSegmentParamName(segments[0]);
					if (segments.length >= 2) {
						final Set<String> paramValues = new TreeSet<String>(Arrays.asList(segments[1].split(",")));
						histogramRenderer.setSegmentParamValues(paramValues);
					}
				}
			}
			if (hints.containsKey(KEY_GROUP_SEGMENTS)) {
				histogramRenderer.setGroupSegments(((Boolean) hints.get(KEY_GROUP_SEGMENTS)).booleanValue());
			}
			if (hints.containsKey(KEY_RELATIVE_DATA_LABEL)) {
				histogramRenderer.setUseRelativeDataLabel(((Boolean) hints.get(KEY_RELATIVE_DATA_LABEL)).booleanValue());
			}
		}
		if (renderer instanceof SampleScatterRenderer) {
			final SampleScatterRenderer scatterRenderer = (SampleScatterRenderer) renderer;
			if (hints.containsKey(KEY_TOOLTIPS)) {
				scatterRenderer.setUseTooltips(((Boolean) hints.get(KEY_TOOLTIPS)).booleanValue());
			}
			if (hints.containsKey(KEY_RESULT_COUNT)) {
				scatterRenderer.setPlotResultCount(((Boolean) hints.get(KEY_RESULT_COUNT)).booleanValue());
			}
		}
	}
}
