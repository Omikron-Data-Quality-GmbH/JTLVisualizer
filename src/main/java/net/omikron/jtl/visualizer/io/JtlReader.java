package net.omikron.jtl.visualizer.io;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.omikron.jtl.visualizer.exceptions.JtlReaderException;
import net.omikron.jtl.visualizer.sample.Sample;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JtlReader {

	private static final String	SAMPLE_NAME_1	= "sample";
	private static final String	SAMPLE_NAME_2	= "httpSample";

	public List<Sample> readSamples(final String jtlFileName, final String excludeRegExp, final boolean onlyIncludeSuccessful) {
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			return readSamples(dBuilder.parse(jtlFileName), excludeRegExp, onlyIncludeSuccessful);
		} catch (final ParserConfigurationException e) {
			throw new JtlReaderException(e);
		} catch (final SAXException e) {
			throw new JtlReaderException(e);
		} catch (final IOException e) {
			throw new JtlReaderException(e);
		}
	}

	public List<Sample> readSamples(final Document jtlDocument, final String excludeRegExp, final boolean onlyIncludeSuccessful) {
		final List<Sample> samples = new ArrayList<Sample>();

		final Pattern excludePattern = getExcludePattern(excludeRegExp);

		try {
			final NodeList rootNodes = jtlDocument.getChildNodes();
			final Node testResultsTag = rootNodes.item(0);
			final NodeList sampleNodes = testResultsTag.getChildNodes();
			for (int i = 0; i < sampleNodes.getLength(); i++) {
				final Node node = sampleNodes.item(i);
				final String nodeName = node.getNodeName();
				if (SAMPLE_NAME_1.equals(nodeName) || SAMPLE_NAME_2.equals(nodeName)) {
					final Sample sample = new Sample(node);
					if (includeSample(sample, excludePattern, onlyIncludeSuccessful)) {
						samples.add(sample);
					}
				}
			}
		} catch (final IOException io) {
			System.err.println(io.getMessage());
		}

		System.out.println("" + samples.size() + " samples read from JTL.");
		return samples;
	}

	private Pattern getExcludePattern(String excludeRegExp) {
		Pattern excludePattern = null;
		if (StringUtils.isNotBlank(excludeRegExp)) {
			// enable substring matches
			excludeRegExp = ".*" + excludeRegExp + ".*";
			excludePattern = Pattern.compile(excludeRegExp);
		}
		return excludePattern;
	}

	/**
	 * Tests if a sample should be included for analysis. All samples that were successful requests and of which the URL does NOT match the exclude pattern will
	 * be included.
	 * 
	 * @param sample The sample to test.
	 * @param excludePattern The exclusion pattern to test against.
	 * @param onlyIncludeSuccessful the only include successful
	 * @return True if the sample stems from a successful request and the URL does not match the exclusion pattern.
	 */
	private boolean includeSample(final Sample sample, final Pattern excludePattern, final boolean onlyIncludeSuccessful) {
		boolean includeSample = !onlyIncludeSuccessful || sample.isSuccess();
		if (includeSample && excludePattern != null) {
			try {
				final String url = URLDecoder.decode(sample.getUrl().toString(), "UTF-8");
				final Matcher excludeMatcher = excludePattern.matcher(url);
				includeSample = !excludeMatcher.matches();
			} catch (final UnsupportedEncodingException e) {
				System.err
						.println("WTF Error: Your system does not support UTF-8 encoding. The url cannot be decoded. Exclusion test cannot be performed. Sample will be included in the analysis. "
								+ e.getMessage());
			}
		}
		return includeSample;
	}
}
