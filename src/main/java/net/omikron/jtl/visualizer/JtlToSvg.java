package net.omikron.jtl.visualizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import net.omikron.jtl.visualizer.transcoder.JtlToSvgTranscoder;
import net.omikron.jtl.visualizer.transcoder.SampleHistogramRenderer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class JtlToSvg {

	private static final String	TEST_RESULT_PATH	= "jtl/";

	private final CommandLine	commandLine;

	public static void main(final String[] args) throws Exception {
		final HelpFormatter formatter = new HelpFormatter();
		final Options commandOptions = setupOptions();
		final CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(commandOptions, args);
		} catch (final ParseException e1) {
			System.err.println("Error when parsing arguments. " + e1.getMessage());
			printUsageInfo(formatter, commandOptions);
			System.exit(-1);
		}
		final JtlToSvg jtlToSvg = new JtlToSvg(commandLine);

		if (commandLine.hasOption("h")) {
			printUsageInfo(formatter, commandOptions);
		} else {
			String[] remainingArgs = commandLine.getArgs();
			if (remainingArgs == null || remainingArgs.length == 0) {
				remainingArgs = new String[1];
				remainingArgs[0] = TEST_RESULT_PATH + "result.jtl";
				System.out.println("No JTL file name provided. Using '" + (new File(remainingArgs[0])).getAbsolutePath() + "' instead.");
			}
			final String jtlFile = remainingArgs[0];
			final String svgFile;
			if (commandLine.hasOption("out")) {
				svgFile = commandLine.getOptionValue("out");
			} else {
				svgFile = jtlFile + ".svg";
			}
			jtlToSvg.transcodeJtlToSvg(jtlFile, svgFile);
			// jtlToSvg.prettyPrintSvg(TEST_RESULT_PATH + "\\jtl\\result.histogram.svg", TEST_RESULT_PATH + "\\jtl\\result.histogram.svg");
		}
	}

	private static void printUsageInfo(final HelpFormatter formatter, final Options commandOptions) {
		formatter.setWidth(120);
		formatter.printHelp(JtlToSvg.class.getSimpleName() + " [options] [JTL file]", commandOptions);
	}

	public JtlToSvg(final CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	/**
	 * @return The commandLine.
	 */
	public CommandLine getCommandLine() {
		return commandLine;
	}

	private void transcodeJtlToSvg(final String inputFileName, final String outputFileName) throws IOException, TranscoderException {
		OutputStream ostream = null;
		try {
			final String jtlURI = new File(inputFileName).toURI().toString();
			final TranscoderInput input = new TranscoderInput(jtlURI);

			final File outputFile = new File(outputFileName);
			System.out.println(outputFile.getAbsolutePath());
			ostream = new FileOutputStream(outputFile);
			final TranscoderOutput output = new TranscoderOutput(ostream);

			final JtlToSvgTranscoder transcoder = new JtlToSvgTranscoder();

			setTranscodingHints(transcoder);

			transcoder.transcode(input, output);
			System.out.println("SVG histogram saved to " + outputFile);
		} finally {
			if (ostream != null) {
				ostream.flush();
				ostream.close();
			}
		}
	}

	protected void setTranscodingHints(final JtlToSvgTranscoder transcoder) {
		if (commandLine.hasOption("bins")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_NUM_BINS, commandLine.getOptionValue("bins"));
		}
		if (commandLine.hasOption("exclude")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_EXCLUDES, commandLine.getOptionValue("exclude"));
		}
		if (commandLine.hasOption("title")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_TITLE, commandLine.getOptionValue("title"));
		}
		if (commandLine.hasOption("version")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_VERSION, commandLine.getOptionValue("version"));
		}
		if (commandLine.hasOption("date")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_BUILD_NR, commandLine.getOptionValue("date"));
		}
		if (commandLine.hasOption("diagram")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_DIAGRAM_TYPE, commandLine.getOptionValue("diagram"));
		}
		if (commandLine.hasOption("segmentParam")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_SEGMENT_PARAM, commandLine.getOptionValue("segmentParam"));
		}
		if (commandLine.hasOption("groupSegments")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_GROUP_SEGMENTS, Boolean.TRUE);
		}
		if (commandLine.hasOption("min")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_X_AXIS_MIN, commandLine.getOptionValue("min"));
		}
		if (commandLine.hasOption("max")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_X_AXIS_MAX, commandLine.getOptionValue("max"));
		}
		if (commandLine.hasOption("ymax")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_Y_AXIS_MAX, commandLine.getOptionValue("ymax"));
		}
		if (commandLine.hasOption("tooltips")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_TOOLTIPS, Boolean.TRUE);
		}
		if (commandLine.hasOption("logScaleXAxis")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_LOG_SCALE_X, Boolean.TRUE);
		}
		if (commandLine.hasOption("logScaleYAxis")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_LOG_SCALE_Y, Boolean.TRUE);
		}
		if (commandLine.hasOption("relativeDataLabels")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_RELATIVE_DATA_LABEL, Boolean.TRUE);
		}
		if (commandLine.hasOption("plotResultCount")) {
			transcoder.addTranscodingHint(JtlToSvgTranscoder.KEY_RESULT_COUNT, Boolean.TRUE);
		}
	}

	private void prettyPrintSvg(final String inputFileName, final String outputFileName) throws IOException {
		final OutputStream ostream = null;
		try {
			final Reader reader = new BufferedReader(new FileReader(inputFileName));
			final TranscoderInput input = new TranscoderInput(reader);

			final Writer writer = new BufferedWriter(new FileWriter(outputFileName));
			final TranscoderOutput output = new TranscoderOutput(writer);

			final SVGTranscoder transcoder = new SVGTranscoder();
			transcoder.transcode(input, output);
		} catch (final TranscoderException e) {
			e.printStackTrace();
		} finally {
			if (ostream != null) {
				ostream.flush();
				ostream.close();
			}
		}
	}

	private static Options setupOptions() {
		final Options options = new Options();

		final Option svgFile = OptionBuilder.withArgName("file").hasArg()
				.withDescription("Set the output file name for the generated SVG. If not set, the input filename + '.svg' will be used.").create("out");
		options.addOption(svgFile);
		final Option exclude = OptionBuilder
				.withArgName("regexp")
				.hasArg()
				.withDescription(	"Set a regular expression specifying requests to be excluded from analysis. The regexp will be matched against the whole decoded URI string. So e.g. requests containing \"Tracking.ff\" or \"query=FACT-Finder Version\" can be excluded with the following regular expression \"(Tracking\\.ff|query=FACT-Finder Version)\".")
				.create("exclude");
		options.addOption(exclude);
		final Option title = OptionBuilder
				.withArgName("name")
				.hasArg()
				.withDescription(	"Set the title headline of the output diagram. If not set, \"" + SampleHistogramRenderer.DEFAULT_TITLE
											+ "\" will be used as the headline.").create("title");
		options.addOption(title);
		final Option version = OptionBuilder
				.withArgName("version")
				.hasArg()
				.withDescription(	"Set the artefact version shown in the build information box. If not set, \"" + SampleHistogramRenderer.DEFAULT_VERSION
											+ "\" will be shown.").create("version");
		options.addOption(version);
		final Option date = OptionBuilder
				.withArgName("date")
				.hasArg()
				.withDescription("Set the date shown in the build information box. If not set, \"" + SampleHistogramRenderer.DEFAULT_DATE + "\" will be shown.")
				.create("date");
		options.addOption(date);
		final Option xAxisMin = OptionBuilder.withArgName("xAxisMin").hasArg()
				.withDescription("Set minimum value for the x-axis. If not set, the minimum response time of the dataset will be used.").create("min");
		options.addOption(xAxisMin);
		final Option diagramType = OptionBuilder.withArgName("type").hasArg()
				.withDescription("Set the type of diagram to be created. One of: histogram, line, scatter, stats. If not set, a histogram will be created.")
				.create("diagram");
		options.addOption(diagramType);
		final Option xAxisMax = OptionBuilder
				.withArgName("xAxisMax")
				.hasArg()
				.withDescription(	"Set maximum value for the x-axis. If set to \""
											+ SampleHistogramRenderer.X_AXIS_OPTION_DOUBLE_UP_QUART
											+ "\" the maximum will be set to double of the upper quartile response time of the dataset (This can be useful in datasets where most requests have relativly short response times but a few have very long response times.). If not set, the maximum response time of the dataset will be used.")
				.create("max");
		options.addOption(xAxisMax);
		final Option yAxisMax = OptionBuilder.withArgName("yAxisMax").hasArg().withDescription("Set maximum value for the y-axis.").create("ymax");
		options.addOption(yAxisMax);
		final Option numBins = OptionBuilder.withArgName("num").hasArg()
				.withDescription("Set the number of bins to be created in the historgram. If not set, the default number of bins will be set to 20.")
				.create("bins");
		options.addOption(numBins);
		final Option segmentParam = OptionBuilder
				.withArgName("name(=CSV list)|regex")
				.hasArg()
				.withDescription(	"Set the name of the URL parameter to use for segmenting the set of samples. If set, a seperate histogram will be created for each value of the given parameter name. Optionally, a comma separated list of values to include can be specified. A regular expression is also allowed as a segmentParam. If specified, all other values will be ignored (and added to the list of \"others\"). If the segmentParam option is set to \""
											+ SampleHistogramRenderer.SEGMENT_PARAM_OPTION_NUM_FILTERS
											+ "\" the number of paramters which start with \""
											+ SampleHistogramRenderer.FF_FILTER_PARAMETER_PREFIX
											+ "\" will be used for segmentation. If set to \""
											+ SampleHistogramRenderer.SEGMENT_PARAM_OPTION_NUM_QUERY_WORDS
											+ "\" the number of words in the values of the parameter \""
											+ SampleHistogramRenderer.FF_QUERY_PARAMETER_NAME
											+ "\" will be used for segmentation. If set to \""
											+ SampleHistogramRenderer.SEGMENT_PARAM_OPTION_CACHED
											+ "\" segmentation will be done between cached and uncached search results. If set to \""
											+ SampleHistogramRenderer.SEGMENT_PARAM_OPTION_TIMEOUT
											+ "\" segmentation will be done between timed out and complete search results.").create("segmentParam");
		options.addOption(segmentParam);
		options.addOption(	"tooltips",
							false,
							"Display a tooltip containing the query part of the request for long running requests, i.e. requests with response times longer than the 95% quantil of the data set. Only applies for scatter plots.");
		options.addOption("logScaleXAxis", false, "Use a logarithmic scale for the x-axis.");
		options.addOption("logScaleYAxis", false, "Use a logarithmic scale for the y-axis.");
		options.addOption("plotResultCount", false, "Creates a scatter plot with response time vs. result count. Only has an effect for scatter plots.");
		options.addOption("relativeDataLabels", false, "Print data labels as relative percentages instead of absolute numbers.");
		options.addOption(	"groupSegments", false,
							"Group segment parameter values which only have a few results, e.g. search queries with more than 10 words. Only has an effect if segmentParam is set.");
		options.addOption("h", false, "Print this help message.");

		return options;
	}
}
