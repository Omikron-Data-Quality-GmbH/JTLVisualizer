package net.omikron;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Quick and dirty class for analyzing the frequency of values of the query URL parameter in webserver access logs.
 */
public class QueryAnalyzer {

	public static void main(final String[] args) {
		final QueryAnalyzer queryAnalyzer = new QueryAnalyzer();

		queryAnalyzer.analyze("C:/temp/path/localhost_access_log.2014-03-01.txt");
	}

	private void analyze(final String accessLogFileName) {
		final List<String> lines = readFile(accessLogFileName);

		final Map<String, Integer> queries = new TreeMap<String, Integer>();

		System.out.println("Log entries: " + lines.size());
		for (final String line : lines) {
			// only de channel
			if (line != null) { // && "de".equals(getChannel(line))) {
				final String query = getQuery(line);
				if (query != null) {
					addQuery(query, queries);
				}
			}
		}

		final SortedSet<Query> sortedQueries = new TreeSet<Query>();
		for (final Map.Entry<String, Integer> entry : queries.entrySet()) {
			sortedQueries.add(new Query(entry.getKey(), entry.getValue()));
		}

		System.out.println("Number of distinct queries: " + sortedQueries.size());
		for (final Query queryObj : sortedQueries) {
			System.out.println("" + queryObj.frequency + ": " + queryObj.query);
		}
	}

	private List<String> readFile(final String accessLogFileName) {
		final List<String> lines = new ArrayList<String>();
		try {
			final BufferedReader br = new BufferedReader(new FileReader(accessLogFileName));
			try {
				String line = null;
				do {
					line = br.readLine();
					if (line != null) {
						lines.add(URLDecoder.decode(line, "UTF-8"));
					}
				} while (line != null);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			} finally {
				br.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return lines;
	}

	private void addQuery(final String query, final Map<String, Integer> queries) {
		final Integer number = queries.get(query);
		if (number == null) {
			queries.put(query, new Integer(1));
		} else {
			queries.put(query, new Integer(number.intValue() + 1));
		}
	}

	private String getQuery(final String line) {
		String query = null;
		final int queryStart = line.indexOf("query=");
		if (queryStart > 0) {
			int queryEnd = line.indexOf('&', queryStart);
			if (queryEnd < queryStart + 6) {
				queryEnd = line.length() - 1;
			}
			query = line.substring(queryStart + 6, queryEnd);
		}
		return query;
	}

	private String getChannel(final String line) {
		String channel = null;
		final int channelStart = line.indexOf("channel=");
		if (channelStart > 0) {
			int channelEnd = line.indexOf('&', channelStart);
			if (channelEnd < channelStart + 8) {
				channelEnd = line.length() - 1;
			}
			channel = line.substring(channelStart + 8, channelEnd);
		}
		return channel;
	}

	private class Query implements Comparable<Query> {
		private final String	query;
		private final int		frequency;

		public Query(final String query, final int frequency) {
			super();
			this.query = query;
			this.frequency = frequency;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + frequency;
			result = prime * result + ((query == null) ? 0 : query.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) { return true; }
			if (obj == null) { return false; }
			if (getClass() != obj.getClass()) { return false; }
			final Query other = (Query) obj;
			if (frequency != other.frequency) { return false; }
			if (query == null) {
				if (other.query != null) { return false; }
			} else if (!query.equals(other.query)) { return false; }
			return true;
		}

		public int compareTo(final Query q) {
			if (this.frequency > q.frequency) {
				return -1;
			} else if (this.frequency < q.frequency) {
				return 1;
			} else {
				return this.query.compareTo(q.query);
			}
		}
	}
}
