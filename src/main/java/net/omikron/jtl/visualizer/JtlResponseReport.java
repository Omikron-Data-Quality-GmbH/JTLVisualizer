package net.omikron.jtl.visualizer;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import net.omikron.jtl.visualizer.io.JtlReader;
import net.omikron.jtl.visualizer.report.SpeedUp;
import net.omikron.jtl.visualizer.sample.Sample;

public class JtlResponseReport {

	private static final String	JTL_6_9_PATH	= "C:/Users/name/Documents/tmp/performancetests/results_jenkins-performance-tests-R6.9-150_6.9_6.9.2.108/";
	private static final String	JTL_6_9_NAME	= "execute-parallel-search_2013-10-31_14-27-generic_accesslog";

	private static final String	JTL_6_10_PATH	= "C:/Users/name/Documents/tmp/performancetests/results_jenkins-performance-tests-trunk-425_6.10_6.10.2-SNAPSHOT/";
	private static final String	JTL_6_10_NAME	= "execute-parallel-search_2013-10-31_16-02-generic_accesslog";

	public static void main(final String[] args) {
		final JtlResponseReport jtlReport = new JtlResponseReport();
		jtlReport.comparePairedResponseTimes();
	}

	private void comparePairedResponseTimes() {
		final JtlReader jtlReader = new JtlReader();

		final List<Sample> samples69 = jtlReader.readSamples(JTL_6_9_PATH + JTL_6_9_NAME + ".jtl", null, false);
		final Map<String, List<Integer>> responseTimes69 = analyzeSamples(samples69);

		final List<Sample> samples610 = jtlReader.readSamples(JTL_6_10_PATH + JTL_6_10_NAME + ".jtl", null, false);
		final Map<String, List<Integer>> responseTimes610 = analyzeSamples(samples610);

		final SortedSet<SpeedUp> speedUps = compareResponseTimes(responseTimes69, responseTimes610);

		printSpeedUps(speedUps);
	}

	private Map<String, List<Integer>> analyzeSamples(final List<Sample> samples) {
		final Map<String, List<Integer>> responseTimesMapping = new TreeMap<String, List<Integer>>();

		long minTimestamp = Long.MAX_VALUE;
		long maxTimestamp = Long.MIN_VALUE;
		for (final Sample sample : samples) {
			final String url = getShortenedURL(sample);
			// final String url = sample.getUrl().getQuery();
			List<Integer> responseTimes = responseTimesMapping.get(url);
			if (responseTimes == null) {
				responseTimes = new LinkedList<Integer>();
				responseTimesMapping.put(url, responseTimes);
			}
			responseTimes.add(sample.getResponseTime());
			// Collections.sort(responseTimes, Collections.reverseOrder());

			final long start = sample.getStartOfRequest().getTime();
			if (start < minTimestamp) {
				minTimestamp = start;
			}

			final long end = sample.getStartOfRequest().getTime() + sample.getResponseTime();
			if (end > maxTimestamp) {
				maxTimestamp = end;
			}
		}

		System.out.println("Total runtime of the test: " + formatMillisToMinutes(maxTimestamp - minTimestamp));
		System.out.println();

		return responseTimesMapping;
	}

	private void printReport(final Map<String, List<Integer>> report) {
		int numMultipleRequests = 0;
		for (final Map.Entry<String, List<Integer>> entry : report.entrySet()) {
			if (entry.getValue().size() > 1) {
				numMultipleRequests++;
				System.out.println(entry.getKey() + ": " + entry.getValue().toString());
			}
		}
		System.out.println("There were " + numMultipleRequests + " requests that were issued more than once.");
		System.out.println();

	}

	private SortedSet<SpeedUp> compareResponseTimes(final Map<String, List<Integer>> responseTimes69, final Map<String, List<Integer>> responseTimes610) {
		final SortedSet<SpeedUp> speedUps = new TreeSet<SpeedUp>();

		if (responseTimes69 == null || responseTimes610 == null || responseTimes69.size() != responseTimes610.size()) return speedUps;

		final NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMinimumFractionDigits(1);

		final Set<String> urls = responseTimes69.keySet();

		int numSlower = 0;
		int totalDiffs = 0;
		for (final String url : urls) {
			final List<Integer> speedUps69 = responseTimes69.get(url);
			final List<Integer> speedUps610 = responseTimes610.get(url);
			for (int i = 0; i < speedUps69.size(); i++) {
				final int t69 = speedUps69.get(i);
				final int t610 = speedUps610.get(i);
				final int diff = t69 - t610;
				final SpeedUp speedUp = new SpeedUp(t69, t610, url, i);
				speedUps.add(speedUp);
				totalDiffs += diff;

				if (t69 < t610) {
					numSlower++;
				}
				// System.out.println("times: 6.9: " + t69 + " 6.10: " + t610 + " speedUp: " + percentFormat.format(speedUp) + " url: " + url);
			}
		}
		final SpeedUp[] array = speedUps.toArray(new SpeedUp[speedUps.size()]);
		System.out.println("Median speedUp: " + percentFormat.format(array[speedUps.size() / 2].getSpeedUp()));
		System.out.println("Total diffs: " + formatMillisToMinutes(totalDiffs));
		System.out.println("Average diffs: " + (totalDiffs / speedUps.size()) + " ms");
		System.out.println("Number of samples that were slower in 6.10: " + numSlower);
		System.out.println();

		return speedUps;
	}

	private void printSpeedUps(final SortedSet<SpeedUp> speedUps) {
		System.out.println("Number of data points: " + speedUps.size());
		System.out.println();

		printSpeedUps(speedUps, 20);
		System.out.println();

		final TreeSet<SpeedUp> reverseSpeedUps = new TreeSet<SpeedUp>(Collections.reverseOrder());
		reverseSpeedUps.addAll(speedUps);
		printSpeedUps(reverseSpeedUps, 20);
		System.out.println();

	}

	private void printSpeedUps(final SortedSet<SpeedUp> speedUps, final int top) {
		System.out.println("Top " + top + " speedups.");
		int i = 0;
		for (final SpeedUp speedUp : speedUps) {
			System.out.println(speedUp);
			if (i > top) break;
			i++;
		}
	}

	private String getShortenedURL(final Sample sample) {
		String shortenedURL = "";
		if (sample != null && sample.getUrl() != null && sample.getUrl().getQuery() != null) {
			// remove sessionID, username and password from URL query
			shortenedURL = sample.getUrl().getQuery();
			shortenedURL = removeFromUrl("sid", shortenedURL);
			shortenedURL = removeFromUrl("username", shortenedURL);
			shortenedURL = removeFromUrl("password", shortenedURL);
			shortenedURL = removeFromUrl("format", shortenedURL);
			shortenedURL = removeFromUrl("noArticleNumberSearch", shortenedURL);
			shortenedURL = removeFromUrl("noCampaign", shortenedURL);
			shortenedURL = removeFromUrl("idsOnly", shortenedURL);
		}
		return shortenedURL;
	}

	private String removeFromUrl(final String toReplace, final String shortenedURL) {
		return shortenedURL.replaceAll(toReplace + "=[^&]*&?", "");
	}

	public static String formatMillisToSeconds(final long millis) {
		return String.format("%d.%03d sec", TimeUnit.MILLISECONDS.toSeconds(millis), millis % 1000);
	}

	public static String formatMillisToMinutes(final long millis) {
		return String.format(	"%d:%02d.%03d min", TimeUnit.MILLISECONDS.toMinutes(millis),
								TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MILLISECONDS.toMinutes(millis) * 60, millis % 1000);
	}
}
