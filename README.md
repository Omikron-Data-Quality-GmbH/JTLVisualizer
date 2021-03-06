# JTL Visualizer

JTL Visualizer visualizes response times from JMeter test results as histograms or scatter plots in SVG format.

Brought to you by Omikron's FACT-Finder development team - http://www.fact-finder.com

## Example Usage:
```
java -cp jtl-visualizer.jar;lib/*.jar net.omikron.jtl.visualizer.JtlToSvg -out sample.svg sample.jtl
```

## All usage options: 
```
JtlToSvg [options] [JTL file]
 -bins <num>                             Set the number of bins to be created in the historgram. If not set, the default
                                         number of bins will be set to 20.
 -date <date>                            Set the date shown in the build information box. If not set, "24.06 09:55" will
                                         be shown.
 -diagram <type>                         Set the type of diagram to be created. One of: histogram, line, scatter, stats.
                                         If not set, a histogram will be created.
 -exclude <regexp>                       Set a regular expression specifying requests to be excluded from analysis. The
                                         regexp will be matched against the whole decoded URI string. So e.g. requests
                                         containing "Tracking.ff" or "query=FACT-Finder Version" can be excluded with
                                         the following regular expression "(Tracking\.ff|query=FACT-Finder Version)".
 -groupSegments                          Group segment parameter values which only have a few results, e.g. search
                                         queries with more than 10 words. Only has an effect if segmentParam is set.
 -h                                      Print this help message.
 -logScaleXAxis                          Use a logarithmic scale for the x-axis.
 -logScaleYAxis                          Use a logarithmic scale for the y-axis.
 -max <xAxisMax>                         Set maximum value for the x-axis. If set to "doubleUpQuart" the maximum will be
                                         set to double of the upper quartile response time of the dataset (This can be
                                         useful in datasets where most requests have relativly short response times but
                                         a few have very long response times.). If not set, the maximum response time of
                                         the dataset will be used.
 -min <xAxisMin>                         Set minimum value for the x-axis. If not set, the minimum response time of the
                                         dataset will be used.
 -out <file>                             Set the output file name for the generated SVG. If not set, the input filename
                                         + '.svg' will be used.
 -plotResultCount                        Creates a scatter plot with response time vs. result count. Only has an effect
                                         for scatter plots.
 -relativeDataLabels                     Print data labels as relative percentages instead of absolute numbers.
 -segmentParam <name(=CSV list)|regex>   Set the name of the URL parameter to use for segmenting the set of samples. If
                                         set, a seperate histogram will be created for each value of the given parameter
                                         name. Optionally, a comma separated list of values to include can be specified.
                                         A regular expression is also allowed as a segmentParam. If specified, all other
                                         values will be ignored (and added to the list of "others"). If the segmentParam
                                         option is set to "numFilters" the number of paramters which start with "filter"
                                         will be used for segmentation. If set to "numWordsInQuery" the number of words
                                         in the values of the parameter "query" will be used for segmentation. If set to
                                         "cached" segmentation will be done between cached and uncached search results.
                                         If set to "timeout" segmentation will be done between timed out and complete
                                         search results.
 -title <name>                           Set the title headline of the output diagram. If not set, "Histogram" will be
                                         used as the headline.
 -tooltips                               Display a tooltip containing the query part of the request for long running
                                         requests, i.e. requests with response times longer than the 95% quantil of the
                                         data set. Only applies for scatter plots.
 -version <version>                      Set the artefact version shown in the build information box. If not set,
                                         "Unknown" will be shown.
 -ymax <yAxisMax>                        Set maximum value for the y-axis.
 ```