package net.omikron.jtl.visualizer.sample;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.w3c.dom.Node;

public class Sample {

	private static final String	LABEL				= "lb";
	private static final String	SUCCESS				= "s";
	private static final String	RESPONSE_CODE		= "rc";
	private static final String	RESPONSE_MESSAGE	= "rm";
	private static final String	ELAPSED_TIME		= "t";
	private static final String	TIMESTAMP			= "ts";
	private static final String	THREAD_NAME			= "tn";
	private static final String	DATA_TYPE			= "dt";
	private static final String	BYTES				= "by";
	private static final String	RESULT_COUNT		= "RESULTCOUNT";
	private static final String	CACHE_AGE			= "CACHEAGE";
	private static final String	TIMEOUT				= "TIMEOUT";

	private URL					url;

	private boolean				success;
	private String				responseCode;
	private String				responseMessage;

	private int					responseTime;
	private Date				startOfRequest;

	private String				threadName;

	private String				dataType;
	private int					bytes;

	/** FACT-Finder specific custom variables */
	private int					resultCount			= -1;
	private int					cacheAge			= 0;
	private boolean				timeOut				= false;

	/**
	 * Constructs a sample object from the given parameters.
	 * 
	 * @param url
	 * @param success
	 * @param failureMessage
	 * @param responseTime
	 * @param startOfRequest
	 * @param responseCode
	 * @param responseMessage
	 * @param threadName
	 * @param dataType
	 * @param bytes
	 */
	public Sample(final URL url, final boolean success, final String responseCode, final String responseMessage, final int responseTime,
			final Date startOfRequest, final String threadName, final String dataType, final int bytes) {
		super();
		this.url = url;
		this.success = success;
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.responseTime = responseTime;
		this.startOfRequest = startOfRequest;
		this.threadName = threadName;
		this.dataType = dataType;
		this.bytes = bytes;
	}

	/**
	 * Constructs a sample object from the given parameters.
	 * 
	 * @param url
	 * @param success
	 * @param failureMessage
	 * @param responseTime
	 * @param startOfRequest
	 * @param responseCode
	 * @param responseMessage
	 * @param threadName
	 * @param dataType
	 * @param bytes
	 * @throws MalformedURLException
	 */
	public Sample(final String url, final boolean success, final String responseCode, final String responseMessage, final int responseTime,
			final long startOfRequest, final String threadName, final String dataType, final int bytes) throws MalformedURLException {
		this(new URL(url), success, responseCode, responseMessage, responseTime, new Date(startOfRequest), threadName, dataType, bytes);
	}

	/**
	 * Constructs a sample object by parsing the provided XML JDOM Element.
	 * 
	 * <p>
	 * Expects the following structure (see <a href="http://jmeter.apache.org/usermanual/listeners.html#attributes">JMeter Sample Attributes</a>):
	 * </p>
	 * 
	 * <code>
	 * 		<httpSample t="82" lt="81" ts="1362757947185" s="true" lb="http://172.22.18.11:8080/FACT-Finder/Search.ff?username=username&amp;password=passwordhash&amp;sid=session1&amp;channel=en&amp;page=1&amp;filterCategory=0415035&amp;format=json&amp;sid=2268D8F2A64E8E778D2F3791E72CBB25.ASTPCEN27&amp;noArticleNumberSearch=false&amp;noCampaign=false&amp;catalog=true&amp;idsOnly=false" rc="200" rm="OK" tn="Thread-Gruppe 1-7" dt="text" by="57960"/>
	 * </code>
	 * 
	 * @param sampleElement The XML JDom Element.
	 * @throws MalformedURLException Thrown in case the label attribute "lb" does not contain a valid URL.
	 */
	public Sample(final Element sampleElement) throws MalformedURLException {
		this(sampleElement.getAttributeValue(LABEL), /**/
		Boolean.parseBoolean(sampleElement.getAttributeValue(SUCCESS)), /**/
		sampleElement.getAttributeValue(RESPONSE_CODE), /**/
		sampleElement.getAttributeValue(RESPONSE_MESSAGE), /**/
		Integer.parseInt(sampleElement.getAttributeValue(ELAPSED_TIME)), /**/
		Long.parseLong(sampleElement.getAttributeValue(TIMESTAMP)), /**/
		sampleElement.getAttributeValue(THREAD_NAME), /**/
		sampleElement.getAttributeValue(DATA_TYPE), /**/
		Integer.parseInt(sampleElement.getAttributeValue(BYTES)));
		final String resultCountString = sampleElement.getAttributeValue(RESULT_COUNT);
		if (StringUtils.isNotEmpty(resultCountString)) {
			try {
				this.resultCount = Integer.parseInt(resultCountString);
			} catch (final NumberFormatException e) {
				// do nothing...do not set result count
			}
		}
		final String cacheAgeString = sampleElement.getAttributeValue(CACHE_AGE);
		if (StringUtils.isNotEmpty(cacheAgeString)) {
			try {
				this.cacheAge = Integer.parseInt(cacheAgeString);
			} catch (final NumberFormatException e) {
				// do nothing...do not set cache age
			}
		}
		final String timeoutString = sampleElement.getAttributeValue(TIMEOUT);
		if (StringUtils.isNotEmpty(timeoutString)) {
			this.timeOut = Boolean.parseBoolean(timeoutString);
		}
	}

	/**
	 * Constructs a sample object by parsing the provided XML JDOM Element.
	 * 
	 * <p>
	 * Expects the following structure (see <a href="http://jmeter.apache.org/usermanual/listeners.html#attributes">JMeter Sample Attributes</a>):
	 * </p>
	 * 
	 * <code>
	 * 		<httpSample t="82" lt="81" ts="1362757947185" s="true" lb="http://172.22.18.11:8080/FACT-Finder/Search.ff?username=username&amp;password=passwordhash&amp;sid=session1&amp;channel=en&amp;page=1&amp;filterCategory=0415035&amp;format=json&amp;sid=2268D8F2A64E8E778D2F3791E72CBB25.ASTPCEN27&amp;noArticleNumberSearch=false&amp;noCampaign=false&amp;catalog=true&amp;idsOnly=false" rc="200" rm="OK" tn="Thread-Gruppe 1-7" dt="text" by="57960"/>
	 * </code>
	 * 
	 * @param sampleNode The XML JDom Element.
	 * @throws MalformedURLException Thrown in case the label attribute "lb" does not contain a valid URL.
	 */
	public Sample(final Node sampleNode) throws MalformedURLException {
		this(sampleNode.getAttributes().getNamedItem(LABEL).getNodeValue(), /**/
		Boolean.parseBoolean(sampleNode.getAttributes().getNamedItem(SUCCESS).getNodeValue()), /**/
		sampleNode.getAttributes().getNamedItem(RESPONSE_CODE).getNodeValue(), /**/
		sampleNode.getAttributes().getNamedItem(RESPONSE_MESSAGE).getNodeValue(), /**/
		Integer.parseInt(sampleNode.getAttributes().getNamedItem(ELAPSED_TIME).getNodeValue()), /**/
		Long.parseLong(sampleNode.getAttributes().getNamedItem(TIMESTAMP).getNodeValue()), /**/
		sampleNode.getAttributes().getNamedItem(THREAD_NAME).getNodeValue(), /**/
		sampleNode.getAttributes().getNamedItem(DATA_TYPE).getNodeValue(), /**/
		Integer.parseInt(sampleNode.getAttributes().getNamedItem(BYTES).getNodeValue()));
		Node namedItem = sampleNode.getAttributes().getNamedItem(RESULT_COUNT);
		if (namedItem != null && StringUtils.isNotEmpty(namedItem.getNodeValue())) {
			if (namedItem.getNodeValue().equals("leider keine")) {
				this.resultCount = 0;
			} else {
				try {
					this.resultCount = Integer.parseInt(namedItem.getNodeValue());
				} catch (final NumberFormatException e) {
					// do nothing...do not set result count
				}
			}
		}
		namedItem = sampleNode.getAttributes().getNamedItem(CACHE_AGE);
		if (namedItem != null && StringUtils.isNotEmpty(namedItem.getNodeValue())) {
			try {
				this.cacheAge = Integer.parseInt(namedItem.getNodeValue());
			} catch (final NumberFormatException e) {
				// do nothing...do not set cache age
			}
		}
		namedItem = sampleNode.getAttributes().getNamedItem(TIMEOUT);
		if (namedItem != null && StringUtils.isNotEmpty(namedItem.getNodeValue())) {
			this.timeOut = Boolean.parseBoolean(namedItem.getNodeValue());
		}
	}

	/**
	 * @return The url.
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(final URL url) {
		this.url = url;
	}

	/**
	 * @param url The url to set.
	 * @throws MalformedURLException
	 */
	public void setUrl(final String url) throws MalformedURLException {
		this.url = new URL(url);
	}

	/**
	 * @return The success.
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success The success to set.
	 */
	public void setSuccess(final boolean success) {
		this.success = success;
	}

	/**
	 * @return The responseCode.
	 */
	public String getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode The responseCode to set.
	 */
	public void setResponseCode(final String responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return The responseMessage.
	 */
	public String getResponseMessage() {
		return responseMessage;
	}

	/**
	 * @param responseMessage The responseMessage to set.
	 */
	public void setResponseMessage(final String responseMessage) {
		this.responseMessage = responseMessage;
	}

	/**
	 * @return The responseTime.
	 */
	public int getResponseTime() {
		return responseTime;
	}

	/**
	 * @param responseTime The responseTime to set.
	 */
	public void setResponseTime(final int responseTime) {
		this.responseTime = responseTime;
	}

	/**
	 * @return The startOfRequest.
	 */
	public Date getStartOfRequest() {
		return startOfRequest;
	}

	/**
	 * @param startOfRequest The startOfRequest to set.
	 */
	public void setStartOfRequest(final Date startOfRequest) {
		this.startOfRequest = startOfRequest;
	}

	/**
	 * @param timeStamp The timeStamp to set.
	 */
	public void setStartOfRequest(final long timeStamp) {
		this.startOfRequest = new Date(timeStamp);
	}

	/**
	 * @return The threadName.
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * @param threadName The threadName to set.
	 */
	public void setThreadName(final String threadName) {
		this.threadName = threadName;
	}

	/**
	 * @return The dataType.
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType The dataType to set.
	 */
	public void setDataType(final String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return The bytes.
	 */
	public int getBytes() {
		return bytes;
	}

	/**
	 * @param bytes The bytes to set.
	 */
	public void setBytes(final int bytes) {
		this.bytes = bytes;
	}

	/**
	 * @return The resultCount.
	 */
	public int getResultCount() {
		return resultCount;
	}

	/**
	 * @param resultCount The resultCount to set.
	 */
	public void setResultCount(final int resultCount) {
		this.resultCount = resultCount;
	}

	/**
	 * @return The cacheAge.
	 */
	public int getCacheAge() {
		return cacheAge;
	}

	/**
	 * @param cacheAge The cacheAge to set.
	 */
	public void setCacheAge(final int cacheAge) {
		this.cacheAge = cacheAge;
	}

	/**
	 * @return The timeOut.
	 */
	public boolean isTimeOut() {
		return timeOut;
	}

	/**
	 * @param timeOut The timeOut to set.
	 */
	public void setTimeOut(final boolean timeOut) {
		this.timeOut = timeOut;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bytes;
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((responseCode == null) ? 0 : responseCode.hashCode());
		result = prime * result + ((responseMessage == null) ? 0 : responseMessage.hashCode());
		result = prime * result + responseTime;
		result = prime * result + ((startOfRequest == null) ? 0 : startOfRequest.hashCode());
		result = prime * result + (success ? 1231 : 1237);
		result = prime * result + ((threadName == null) ? 0 : threadName.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Sample other = (Sample) obj;
		if (bytes != other.bytes) return false;
		if (dataType == null) {
			if (other.dataType != null) return false;
		} else if (!dataType.equals(other.dataType)) return false;
		if (responseCode == null) {
			if (other.responseCode != null) return false;
		} else if (!responseCode.equals(other.responseCode)) return false;
		if (responseMessage == null) {
			if (other.responseMessage != null) return false;
		} else if (!responseMessage.equals(other.responseMessage)) return false;
		if (responseTime != other.responseTime) return false;
		if (startOfRequest == null) {
			if (other.startOfRequest != null) return false;
		} else if (!startOfRequest.equals(other.startOfRequest)) return false;
		if (success != other.success) return false;
		if (threadName == null) {
			if (other.threadName != null) return false;
		} else if (!threadName.equals(other.threadName)) return false;
		if (url == null) {
			if (other.url != null) return false;
		} else if (!url.equals(other.url)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "Sample [url=" + url + ", success=" + success + ", responseCode=" + responseCode + ", responseMessage=" + responseMessage + ", responseTime="
				+ responseTime + ", startOfRequest=" + startOfRequest + ", threadName=" + threadName + ", dataType=" + dataType + ", bytes=" + bytes + "]";
	}

	public static final Comparator<Sample>	ORDER_BY_START_TIME_ASC		= new Comparator<Sample>() {
																			public int compare(final Sample sample1, final Sample sample2) {
																				return sample1.getStartOfRequest().compareTo(sample2.getStartOfRequest());
																			}
																		};
	public static final Comparator<Sample>	ORDER_BY_START_TIME_DESC	= new Comparator<Sample>() {
																			public int compare(final Sample sample1, final Sample sample2) {
																				return sample2.getStartOfRequest().compareTo(sample1.getStartOfRequest());
																			}
																		};
	public static final Comparator<Sample>	ORDER_BY_RESPONSE_TIME_ASC	= new Comparator<Sample>() {
																			public int compare(final Sample sample1, final Sample sample2) {
																				return sample1.getResponseTime() > sample2.getResponseTime() ? +1 : sample1
																						.getResponseTime() < sample2.getResponseTime() ? -1 : 0;
																			}
																		};
	public static final Comparator<Sample>	ORDER_BY_RESPONSE_TIME_DESC	= new Comparator<Sample>() {
																			public int compare(final Sample sample1, final Sample sample2) {
																				return sample1.getResponseTime() < sample2.getResponseTime() ? +1 : sample1
																						.getResponseTime() > sample2.getResponseTime() ? -1 : 0;
																			}
																		};
}
