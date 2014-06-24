package net.omikron.jtl.visualizer;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;

import net.omikron.jtl.visualizer.exceptions.JtlMinRequirementsException;
import net.omikron.jtl.visualizer.io.JtlReader;
import net.omikron.jtl.visualizer.sample.Sample;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Will run a set of minimum requirements tests on a JTL and throw exceptions if they are not met. An example of a minimum requirement is that not more than 10%
 * of the requests can be unsuccessful. This can be used to fail a maven build.
 * 
 * @author Kai Stapel
 */
public class JtlMinRequirementsTester {

	private static final String	ARG_NAME_MIN_SUCCESS_RATIO	= "minSuccessRatio";
	private static final String	ARG_NAME_MIN_SAMPLES		= "minSamples";

	private static final String	TEST_RESULT_PATH			= "jtl/";

	private static final double	DEFAULT_MIN_SUCCESS_RATIO	= 90.0;

	private final CommandLine	commandLine;
	private final String		jtlFileName;

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

		if (commandLine.hasOption("h")) {
			printUsageInfo(formatter, commandOptions);
		} else {
			String[] remainingArgs = commandLine.getArgs();
			if (remainingArgs == null || remainingArgs.length == 0) {
				remainingArgs = new String[1];
				remainingArgs[0] = TEST_RESULT_PATH + "result.jtl";
				System.out.println("No JTL file name provided. Using '" + (new File(remainingArgs[0])).getAbsolutePath() + "' instead.");
			}
			final String jtlFileName = remainingArgs[0];

			final JtlMinRequirementsTester jtlTester = new JtlMinRequirementsTester(commandLine, jtlFileName);
			jtlTester.testJtlMinimumRequirements();
		}
	}

	private void testJtlMinimumRequirements() {
		final JtlReader jtlReader = new JtlReader();
		// read all samples including the unsuccessful ones
		final List<Sample> samples = jtlReader.readSamples(jtlFileName, null, false);

		testMinSamples(samples);
		testMinSuccessRatio(samples);
	}

	private void testMinSamples(final List<Sample> samples) {
		// only test for minimum number of samples if option was specified
		if (commandLine.hasOption(ARG_NAME_MIN_SAMPLES)) {
			final String minSamplesOptionValue = commandLine.getOptionValue(ARG_NAME_MIN_SAMPLES);
			final int minSamples = Integer.parseInt(minSamplesOptionValue);
			if (samples.size() < minSamples) {
				final String errMsg = "Minimum Requirements for JTL '" + jtlFileName + "' were not met. JTL only contains " + samples.size()
						+ " samples, but a minimum of " + minSamples + " samples is required.";
				System.err.println(errMsg);
				throw new JtlMinRequirementsException(errMsg);
			}
		}
	}

	private void testMinSuccessRatio(final List<Sample> samples) {
		int successfulSamples = 0;
		for (final Sample sample : samples) {
			if (sample.isSuccess()) {
				successfulSamples++;
			}
		}

		final double minSuccessRatio = getMinSuccessRatio();
		final double actualSuccessRatio = (double) successfulSamples / (double) samples.size();

		final NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMinimumFractionDigits(0);

		System.out.println("Total samples:      " + samples.size());
		System.out.println("Successful samples: " + successfulSamples);
		System.out.println("Success ratio:      " + percentFormat.format(actualSuccessRatio));

		if (actualSuccessRatio < minSuccessRatio) {
			final String errMsg = "Minimum Requirements for JTL '" + jtlFileName + "' were not met. Only " + percentFormat.format(actualSuccessRatio)
					+ " of the samples were successful, but a minimum of " + percentFormat.format(minSuccessRatio) + " successful samples is required.";
			System.err.println(errMsg);
			throw new JtlMinRequirementsException(errMsg);
		}
	}

	private double getMinSuccessRatio() {
		double minSuccessRatio = DEFAULT_MIN_SUCCESS_RATIO / 100.0;

		if (commandLine.hasOption(ARG_NAME_MIN_SUCCESS_RATIO)) {
			final String minSuccessRatioOptionValue = commandLine.getOptionValue(ARG_NAME_MIN_SUCCESS_RATIO);
			minSuccessRatio = Double.parseDouble(minSuccessRatioOptionValue) / 100.0;
		}

		return minSuccessRatio;
	}

	private static void printUsageInfo(final HelpFormatter formatter, final Options commandOptions) {
		formatter.setWidth(120);
		formatter.printHelp(JtlToSvg.class.getSimpleName() + " [options] [JTL file]", commandOptions);
	}

	public JtlMinRequirementsTester(final CommandLine commandLine, final String jtlFileName) {
		this.commandLine = commandLine;
		this.jtlFileName = jtlFileName;
	}

	/**
	 * @return The commandLine.
	 */
	public CommandLine getCommandLine() {
		return commandLine;
	}

	private static Options setupOptions() {
		final Options options = new Options();

		final Option minSuccessRatio = OptionBuilder.withArgName(ARG_NAME_MIN_SUCCESS_RATIO).hasArg()
				.withDescription("Sets the minimum success ratio. The default value is " + DEFAULT_MIN_SUCCESS_RATIO + ".").create(ARG_NAME_MIN_SUCCESS_RATIO);
		options.addOption(minSuccessRatio);
		final Option minSamples = OptionBuilder
				.withArgName(ARG_NAME_MIN_SAMPLES)
				.hasArg()
				.withDescription(	"Sets the minimum number of samples expected in the jtl. If not specified no minimum number of samples test will be performed.")
				.create(ARG_NAME_MIN_SAMPLES);
		options.addOption(minSamples);

		options.addOption("h", false, "Print this help message.");

		return options;
	}
}
