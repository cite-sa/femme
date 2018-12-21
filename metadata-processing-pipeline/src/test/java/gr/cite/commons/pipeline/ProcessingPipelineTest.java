package gr.cite.commons.pipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import gr.cite.commons.pipeline.config.PipelineConfiguration;
import gr.cite.commons.pipeline.exceptions.ProcessingPipelineException;
import gr.cite.commons.pipeline.exceptions.ProcessingPipelineHandlerException;
import gr.cite.commons.pipelinenew.Pipeline;
import gr.cite.pipelinenew.step.PipelineStep;
import org.junit.Test;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProcessingPipelineTest {
	private static final ObjectMapper mapper = new ObjectMapper();

	String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<wcs:CoverageDescriptions xsi:schemaLocation=\"http://www.opengis.net/wcs/2.0 http://schemas.opengis" +
			".net/wcs/2.0/wcsAll.xsd\" xmlns:wcs=\"http://www.opengis.net/wcs/2.0\" xmlns:xsi=\"http://www" +
			".w3.org/2001/XMLSchema-instance\" xmlns:crs=\"http://www.opengis.net/wcs/service-extension/crs/1.0\" " +
			"xmlns:ows=\"http://www.opengis.net/ows/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" " +
			"xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
			"  <wcs:CoverageDescription gml:id=\"L8_RGB_32631_30\" xmlns=\"http://www.opengis.net/gml/3.2\" " +
			"xmlns:gmlcov=\"http://www.opengis.net/gmlcov/1.0\" xmlns:swe=\"http://www.opengis.net/swe/2.0\">" +
			"    <boundedBy>" +
			"      <Envelope srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost:8080/def/crs/EPSG/0" +
			"/32631&amp;2=http://localhost:8080/def/crs/OGC/0/UnixTime\" axisLabels=\"E N unix\" uomLabels=\"metre " +
			"metre s\" srsDimension=\"3\">" +
			"        <lowerCorner>126885 3234885 1433157189</lowerCorner>" +
			"        <upperCorner>871515 7094115 1489574827</upperCorner>" +
			"      </Envelope>" +
			"    </boundedBy>" +
			"    <wcs:CoverageId>L8_RGB_32631_30</wcs:CoverageId>" +
			"    <gmlcov:metadata>" +
			"      <gmlcov:Extension/>" +
			"    </gmlcov:metadata>" +
			"    <domainSet>" +
			"      <gmlrgrid:ReferenceableGridByVectors dimension=\"3\" gml:id=\"L8_RGB_32631_30-grid\" " +
			"xsi:schemaLocation=\"http://www.opengis.net/gml/3.3/rgrid http://schemas.opengis" +
			".net/gml/3.3/referenceableGrid.xsd\" xmlns:gmlrgrid=\"http://www.opengis.net/gml/3.3/rgrid\">" +
			"        <limits>" +
			"          <GridEnvelope>" +
			"            <low>-4840 -4630 0</low>" +
			"            <high>19980 124010 566</high>" +
			"          </GridEnvelope>" +
			"        </limits>" +
			"        <axisLabels>E N unix</axisLabels>" +
			"        <gmlrgrid:origin>" +
			"          <Point gml:id=\"L8_RGB_32631_30-origin\" " +
			"srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost:8080/def/crs/EPSG/0/32631&amp;" +
			"2=http://localhost:8080/def/crs/OGC/0/UnixTime\" axisLabels=\"E N unix\" uomLabels=\"metre metre s\" " +
			"srsDimension=\"3\">" +
			"            <pos>126900 7094100 1433157189</pos>" +
			"          </Point>" +
			"        </gmlrgrid:origin>" +
			"        <gmlrgrid:generalGridAxis>" +
			"          <gmlrgrid:GeneralGridAxis>" +
			"            <gmlrgrid:offsetVector srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost" +
			":8080/def/crs/EPSG/0/32631&amp;2=http://localhost:8080/def/crs/OGC/0/UnixTime\" axisLabels=\"E N unix\" " +
			"uomLabels=\"metre metre s\" srsDimension=\"3\">30 0 0</gmlrgrid:offsetVector>" +
			"            <gmlrgrid:coefficients/>" +
			"            <gmlrgrid:gridAxesSpanned>E</gmlrgrid:gridAxesSpanned>" +
			"            <gmlrgrid:sequenceRule axisOrder=\"+1\">Linear</gmlrgrid:sequenceRule>" +
			"          </gmlrgrid:GeneralGridAxis>" +
			"        </gmlrgrid:generalGridAxis>" +
			"        <gmlrgrid:generalGridAxis>" +
			"          <gmlrgrid:GeneralGridAxis>" +
			"            <gmlrgrid:offsetVector srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost" +
			":8080/def/crs/EPSG/0/32631&amp;2=http://localhost:8080/def/crs/OGC/0/UnixTime\" axisLabels=\"E N unix\" " +
			"uomLabels=\"metre metre s\" srsDimension=\"3\">0 -30 0</gmlrgrid:offsetVector>" +
			"            <gmlrgrid:coefficients/>" +
			"            <gmlrgrid:gridAxesSpanned>N</gmlrgrid:gridAxesSpanned>" +
			"            <gmlrgrid:sequenceRule axisOrder=\"+1\">Linear</gmlrgrid:sequenceRule>" +
			"          </gmlrgrid:GeneralGridAxis>" +
			"        </gmlrgrid:generalGridAxis>" +
			"        <gmlrgrid:generalGridAxis>" +
			"          <gmlrgrid:GeneralGridAxis>" +
			"            <gmlrgrid:offsetVector srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost" +
			":8080/def/crs/EPSG/0/32631&amp;2=http://localhost:8080/def/crs/OGC/0/UnixTime\" axisLabels=\"E N unix\" " +
			"uomLabels=\"metre metre s\" srsDimension=\"3\">0 0 1</gmlrgrid:offsetVector>" +
			"            <gmlrgrid:coefficients>2015-05-31T10:32:33Z 2015-05-31T10:32:57Z 2015-05-31T10:33:21Z " +
			"2015-05-31T10:33:45Z 2015-05-31T10:34:09Z 2015-05-31T10:34:33Z 2015-05-31T10:34:57Z 2015-05-31T10:35:20Z " +
			"2015-05-31T10:35:44Z 2015-05-31T10:36:08Z 2015-05-31T10:36:32Z 2015-06-01T11:13:09Z 2016-06-27T10:28:16Z " +
			"2016-06-27T10:28:40Z 2016-06-27T10:29:04Z 2016-06-27T10:29:28Z 2016-06-27T10:29:52Z 2016-06-27T10:30:16Z " +
			"2016-06-27T10:30:40Z 2016-06-27T10:31:04Z 2016-06-27T10:31:28Z 2016-06-27T10:31:51Z 2016-06-27T10:32:15Z " +
			"2016-06-27T10:32:39Z 2016-06-28T11:08:04Z 2016-06-28T11:08:22Z 2016-06-29T10:17:55Z 2016-06-29T10:18:19Z " +
			"2016-06-29T10:18:43Z 2016-06-29T10:19:07Z 2016-06-29T10:19:31Z 2016-06-29T10:19:55Z 2016-06-29T10:20:19Z " +
			"2016-06-29T10:20:43Z 2016-06-29T10:21:07Z 2016-06-30T10:55:13Z 2016-06-30T10:55:37Z 2016-06-30T10:56:01Z " +
			"2016-06-30T10:56:25Z 2016-06-30T10:56:49Z 2016-06-30T10:57:13Z 2016-06-30T10:57:37Z 2016-07-01T10:08:46Z " +
			"2016-07-02T10:44:28Z 2016-07-02T10:44:52Z 2016-07-02T10:45:16Z 2016-07-02T10:45:40Z 2016-07-02T10:46:04Z " +
			"2016-07-02T10:46:28Z 2016-07-02T10:46:52Z 2016-07-02T10:47:16Z 2016-07-04T10:33:19Z 2016-07-04T10:33:43Z " +
			"2016-07-04T10:34:07Z 2016-07-04T10:34:31Z 2016-07-04T10:34:55Z 2016-07-04T10:35:19Z 2016-07-04T10:35:43Z " +
			"2016-07-04T10:36:07Z 2016-07-04T10:36:30Z 2016-07-04T10:36:54Z 2016-07-04T10:37:18Z 2016-07-06T10:22:58Z " +
			"2016-07-06T10:23:22Z 2016-07-06T10:23:46Z 2016-07-06T10:24:10Z 2016-07-06T10:24:34Z 2016-07-06T10:24:58Z " +
			"2016-07-06T10:25:22Z 2016-07-06T10:25:45Z 2016-07-06T10:26:09Z 2016-07-06T10:26:33Z 2016-07-06T10:26:57Z " +
			"2016-07-06T10:27:21Z 2016-07-07T11:03:10Z 2016-07-08T10:13:25Z 2016-07-08T10:13:49Z 2016-07-08T10:14:12Z " +
			"2016-07-08T10:14:36Z 2016-07-08T10:15:00Z 2016-07-09T10:49:55Z 2016-07-09T10:50:43Z 2016-07-09T10:51:06Z " +
			"2016-07-09T10:51:30Z 2016-07-09T10:51:54Z 2016-07-09T10:52:18Z 2016-07-11T10:38:46Z 2016-07-11T10:39:10Z " +
			"2016-07-11T10:39:33Z 2016-07-11T10:39:57Z 2016-07-11T10:40:21Z 2016-07-11T10:40:45Z 2016-07-11T10:41:09Z " +
			"2016-07-11T10:41:33Z 2016-07-11T10:41:57Z 2016-07-11T10:42:21Z 2016-07-13T10:28:24Z 2016-07-13T10:28:48Z " +
			"2016-07-13T10:29:12Z 2016-07-13T10:29:36Z 2016-07-13T10:30:00Z 2016-07-13T10:30:24Z 2016-07-13T10:30:48Z " +
			"2016-07-13T10:31:11Z 2016-07-13T10:31:35Z 2016-07-13T10:31:59Z 2016-07-13T10:32:23Z 2016-07-13T10:32:47Z " +
			"2016-07-14T11:08:12Z 2016-07-14T11:08:29Z 2016-07-15T10:18:03Z 2016-07-15T10:18:27Z 2016-07-15T10:18:51Z " +
			"2016-07-15T10:19:14Z 2016-07-15T10:19:38Z 2016-07-15T10:20:02Z 2016-07-15T10:20:26Z 2016-07-15T10:20:50Z " +
			"2016-07-15T10:21:14Z 2016-07-16T10:55:20Z 2016-07-16T10:55:44Z 2016-07-16T10:56:08Z 2016-07-16T10:56:56Z " +
			"2016-07-16T10:57:20Z 2016-07-16T10:57:44Z 2016-07-17T10:08:53Z 2016-07-18T10:44:11Z 2016-07-18T10:44:35Z " +
			"2016-07-18T10:44:59Z 2016-07-18T10:45:23Z 2016-07-18T10:45:47Z 2016-07-18T10:46:11Z 2016-07-18T10:46:34Z " +
			"2016-07-18T10:46:58Z 2016-07-18T10:47:22Z 2016-07-20T10:33:26Z 2016-07-20T10:33:50Z 2016-07-20T10:34:13Z " +
			"2016-07-20T10:34:37Z 2016-07-20T10:35:01Z 2016-07-20T10:35:25Z 2016-07-20T10:35:49Z 2016-07-20T10:36:13Z " +
			"2016-07-20T10:36:37Z 2016-07-20T10:37:01Z 2016-07-20T10:37:25Z 2016-07-22T10:23:04Z 2016-07-22T10:23:28Z " +
			"2016-07-22T10:23:52Z 2016-07-22T10:24:16Z 2016-07-22T10:24:40Z 2016-07-22T10:25:03Z 2016-07-22T10:25:27Z " +
			"2016-07-22T10:25:51Z 2016-07-22T10:26:15Z 2016-07-22T10:26:39Z 2016-07-22T10:27:03Z 2016-07-22T10:27:27Z " +
			"2016-07-23T11:03:15Z 2016-07-24T10:13:30Z 2016-07-24T10:13:54Z 2016-07-24T10:14:18Z 2016-07-24T10:14:42Z " +
			"2016-07-24T10:15:06Z 2016-07-25T10:50:00Z 2016-07-25T10:50:24Z 2016-07-25T10:50:48Z 2016-07-25T10:51:12Z " +
			"2016-07-25T10:51:35Z 2016-07-25T10:51:59Z 2016-07-25T10:52:23Z 2016-07-27T10:38:50Z 2016-07-27T10:39:14Z " +
			"2016-07-27T10:39:38Z 2016-07-27T10:40:02Z 2016-07-27T10:40:26Z 2016-07-27T10:40:50Z 2016-07-27T10:41:14Z " +
			"2016-07-27T10:41:37Z 2016-07-27T10:42:01Z 2016-07-27T10:42:25Z 2016-12-27T10:33:41Z 2016-12-27T10:34:05Z " +
			"2016-12-27T10:34:29Z 2016-12-27T10:34:53Z 2016-12-27T10:35:17Z 2016-12-27T10:35:40Z 2016-12-27T10:36:04Z " +
			"2016-12-27T10:36:28Z 2016-12-27T10:36:52Z 2016-12-27T10:37:16Z 2016-12-27T10:37:40Z 2016-12-29T10:23:19Z " +
			"2016-12-29T10:23:43Z 2016-12-29T10:24:06Z 2016-12-29T10:24:30Z 2016-12-29T10:24:54Z 2016-12-29T10:25:18Z " +
			"2016-12-29T10:25:42Z 2016-12-29T10:26:06Z 2016-12-29T10:26:30Z 2016-12-29T10:26:54Z 2016-12-29T10:27:18Z " +
			"2016-12-29T10:27:41Z 2016-12-30T11:03:30Z 2016-12-31T10:13:44Z 2016-12-31T10:14:08Z 2016-12-31T10:14:32Z " +
			"2016-12-31T10:14:56Z 2016-12-31T10:15:20Z 2017-01-01T10:49:49Z 2017-01-01T10:50:13Z 2017-01-01T10:50:36Z " +
			"2017-01-01T10:51:00Z 2017-01-01T10:51:24Z 2017-01-01T10:51:48Z 2017-01-01T10:52:12Z 2017-01-01T10:52:36Z " +
			"2017-01-03T10:39:02Z 2017-01-03T10:39:26Z 2017-01-03T10:39:50Z 2017-01-03T10:40:14Z 2017-01-03T10:40:38Z " +
			"2017-01-03T10:41:02Z 2017-01-03T10:41:26Z 2017-01-03T10:41:49Z 2017-01-03T10:42:13Z 2017-01-03T10:42:37Z " +
			"2017-01-05T10:28:40Z 2017-01-05T10:29:04Z 2017-01-05T10:29:28Z 2017-01-05T10:29:51Z 2017-01-05T10:30:15Z " +
			"2017-01-05T10:30:39Z 2017-01-05T10:31:03Z 2017-01-05T10:31:27Z 2017-01-05T10:31:51Z 2017-01-05T10:32:15Z " +
			"2017-01-05T10:32:39Z 2017-01-05T10:33:03Z 2017-01-06T11:08:27Z 2017-01-06T11:08:44Z 2017-01-07T10:18:17Z " +
			"2017-01-07T10:18:41Z 2017-01-07T10:19:05Z 2017-01-07T10:19:29Z 2017-01-07T10:19:53Z 2017-01-07T10:20:17Z " +
			"2017-01-07T10:20:40Z 2017-01-07T10:21:04Z 2017-01-07T10:21:28Z 2017-01-08T10:55:41Z 2017-01-08T10:55:58Z " +
			"2017-01-08T10:56:22Z 2017-01-08T10:56:46Z 2017-01-08T10:57:10Z 2017-01-08T10:57:34Z 2017-01-08T10:57:58Z " +
			"2017-01-09T10:09:06Z 2017-01-10T10:44:24Z 2017-01-10T10:44:48Z 2017-01-10T10:45:12Z 2017-01-10T10:45:36Z " +
			"2017-01-10T10:45:59Z 2017-01-10T10:46:23Z 2017-01-10T10:46:47Z 2017-01-10T10:47:11Z 2017-01-10T10:47:35Z " +
			"2017-01-12T10:33:37Z 2017-01-12T10:34:01Z 2017-01-12T10:34:25Z 2017-01-12T10:34:49Z 2017-01-12T10:35:13Z " +
			"2017-01-12T10:35:37Z 2017-01-12T10:36:01Z 2017-01-12T10:36:24Z 2017-01-12T10:36:48Z 2017-01-12T10:37:12Z " +
			"2017-01-12T10:37:36Z 2017-01-14T10:23:15Z 2017-01-14T10:23:38Z 2017-01-14T10:24:02Z 2017-01-14T10:24:26Z " +
			"2017-01-14T10:24:50Z 2017-01-14T10:25:14Z 2017-01-14T10:25:38Z 2017-01-14T10:26:02Z 2017-01-14T10:26:26Z " +
			"2017-01-14T10:26:50Z 2017-01-14T10:27:13Z 2017-01-14T10:27:37Z 2017-01-16T10:13:39Z 2017-01-16T10:14:03Z " +
			"2017-01-16T10:14:27Z 2017-01-16T10:14:51Z 2017-01-16T10:15:15Z 2017-01-17T10:49:45Z 2017-01-17T10:50:09Z " +
			"2017-01-17T10:50:33Z 2017-01-17T10:50:57Z 2017-01-17T10:51:20Z 2017-01-17T10:51:44Z 2017-01-17T10:52:08Z " +
			"2017-01-17T10:52:32Z 2017-01-19T10:38:58Z 2017-01-19T10:39:22Z 2017-01-19T10:39:46Z 2017-01-19T10:40:10Z " +
			"2017-01-19T10:40:34Z 2017-01-19T10:40:58Z 2017-01-19T10:41:21Z 2017-01-19T10:41:45Z 2017-01-19T10:42:09Z " +
			"2017-01-19T10:42:33Z 2017-01-21T10:28:35Z 2017-01-21T10:28:59Z 2017-01-21T10:29:23Z 2017-01-21T10:29:47Z " +
			"2017-01-21T10:30:11Z 2017-01-21T10:30:35Z 2017-01-21T10:30:59Z 2017-01-21T10:31:22Z 2017-01-21T10:31:46Z " +
			"2017-01-21T10:32:10Z 2017-01-21T10:32:34Z 2017-01-21T10:32:58Z 2017-01-22T11:08:22Z 2017-01-22T11:08:39Z " +
			"2017-01-23T10:18:12Z 2017-01-23T10:18:36Z 2017-01-23T10:19:00Z 2017-01-23T10:19:24Z 2017-01-23T10:19:48Z " +
			"2017-01-23T10:20:12Z 2017-01-23T10:20:36Z 2017-01-23T10:20:59Z 2017-01-23T10:21:23Z 2017-01-24T10:55:29Z " +
			"2017-01-24T10:55:53Z 2017-01-24T10:56:17Z 2017-01-24T10:56:41Z 2017-01-24T10:57:05Z 2017-01-24T10:57:29Z " +
			"2017-01-24T10:57:52Z 2017-01-25T10:09:01Z 2017-01-26T10:44:18Z 2017-01-26T10:44:42Z 2017-01-26T10:45:06Z " +
			"2017-01-26T10:45:30Z 2017-01-26T10:45:54Z 2017-01-26T10:46:18Z 2017-01-26T10:46:42Z 2017-01-26T10:47:06Z " +
			"2017-01-26T10:47:29Z 2017-01-28T10:33:31Z 2017-01-28T10:33:55Z 2017-01-28T10:34:19Z 2017-01-28T10:34:43Z " +
			"2017-01-28T10:35:07Z 2017-01-28T10:35:31Z 2017-01-28T10:35:55Z 2017-01-28T10:36:19Z 2017-01-28T10:36:42Z " +
			"2017-01-28T10:37:06Z 2017-01-28T10:37:30Z 2017-01-30T10:23:08Z 2017-01-30T10:23:32Z 2017-01-30T10:23:56Z " +
			"2017-01-30T10:24:20Z 2017-01-30T10:24:44Z 2017-01-30T10:25:08Z 2017-01-30T10:25:32Z 2017-01-30T10:25:55Z " +
			"2017-01-30T10:26:19Z 2017-01-30T10:26:43Z 2017-01-30T10:27:07Z 2017-01-30T10:27:31Z 2017-02-01T10:13:33Z " +
			"2017-02-01T10:13:57Z 2017-02-01T10:14:21Z 2017-02-01T10:14:44Z 2017-02-01T10:15:08Z 2017-02-02T10:49:38Z " +
			"2017-02-02T10:50:02Z 2017-02-02T10:50:26Z 2017-02-02T10:50:50Z 2017-02-02T10:51:13Z 2017-02-02T10:51:37Z " +
			"2017-02-02T10:52:01Z 2017-02-02T10:52:25Z 2017-02-04T10:38:51Z 2017-02-04T10:39:15Z 2017-02-04T10:39:39Z " +
			"2017-02-04T10:40:02Z 2017-02-04T10:40:26Z 2017-02-04T10:40:50Z 2017-02-04T10:41:14Z 2017-02-04T10:41:38Z " +
			"2017-02-04T10:42:02Z 2017-02-04T10:42:26Z 2017-02-06T10:28:27Z 2017-02-06T10:28:51Z 2017-02-06T10:29:15Z " +
			"2017-02-06T10:29:39Z 2017-02-06T10:30:03Z 2017-02-06T10:30:27Z 2017-02-06T10:30:51Z 2017-02-06T10:31:15Z " +
			"2017-02-06T10:31:38Z 2017-02-06T10:32:02Z 2017-02-06T10:32:26Z 2017-02-06T10:32:50Z 2017-02-07T11:08:14Z " +
			"2017-02-07T11:08:31Z 2017-02-08T10:18:04Z 2017-02-08T10:18:28Z 2017-02-08T10:18:52Z 2017-02-08T10:19:16Z " +
			"2017-02-08T10:19:39Z 2017-02-08T10:20:03Z 2017-02-08T10:20:27Z 2017-02-08T10:20:51Z 2017-02-08T10:21:15Z " +
			"2017-02-09T10:55:21Z 2017-02-09T10:55:45Z 2017-02-09T10:56:09Z 2017-02-09T10:56:32Z 2017-02-09T10:56:56Z " +
			"2017-02-09T10:57:20Z 2017-02-09T10:57:44Z 2017-02-10T10:08:53Z 2017-02-11T10:44:10Z 2017-02-11T10:44:34Z " +
			"2017-02-11T10:44:58Z 2017-02-11T10:45:22Z 2017-02-11T10:45:46Z 2017-02-11T10:46:10Z 2017-02-11T10:46:34Z " +
			"2017-02-11T10:46:57Z 2017-02-11T10:47:21Z 2017-02-13T10:33:24Z 2017-02-13T10:33:48Z 2017-02-13T10:34:12Z " +
			"2017-02-13T10:34:35Z 2017-02-13T10:34:59Z 2017-02-13T10:35:23Z 2017-02-13T10:35:47Z 2017-02-13T10:36:11Z " +
			"2017-02-13T10:36:35Z 2017-02-13T10:36:59Z 2017-02-13T10:37:23Z 2017-02-17T10:13:26Z 2017-02-17T10:13:50Z " +
			"2017-02-17T10:14:14Z 2017-02-17T10:14:38Z 2017-02-17T10:15:02Z 2017-02-18T10:49:32Z 2017-02-18T10:49:55Z " +
			"2017-02-18T10:50:19Z 2017-02-18T10:50:43Z 2017-02-18T10:51:07Z 2017-02-18T10:51:31Z 2017-02-18T10:51:55Z " +
			"2017-02-18T10:52:19Z 2017-02-20T10:38:45Z 2017-02-20T10:39:09Z 2017-02-20T10:39:33Z 2017-02-20T10:39:56Z " +
			"2017-02-20T10:40:20Z 2017-02-20T10:40:44Z 2017-02-20T10:41:08Z 2017-02-20T10:41:32Z 2017-02-20T10:41:56Z " +
			"2017-02-20T10:42:20Z 2017-02-22T10:28:22Z 2017-02-22T10:28:46Z 2017-02-22T10:29:10Z 2017-02-22T10:29:34Z " +
			"2017-02-22T10:29:57Z 2017-02-22T10:30:21Z 2017-02-22T10:30:45Z 2017-02-22T10:31:09Z 2017-02-22T10:31:33Z " +
			"2017-02-22T10:31:57Z 2017-02-22T10:32:21Z 2017-02-22T10:32:45Z 2017-02-23T11:08:09Z 2017-02-23T11:08:26Z " +
			"2017-03-01T10:33:18Z 2017-03-01T10:33:42Z 2017-03-01T10:34:06Z 2017-03-01T10:34:30Z 2017-03-01T10:34:54Z " +
			"2017-03-01T10:35:18Z 2017-03-01T10:35:41Z 2017-03-01T10:36:05Z 2017-03-01T10:36:29Z 2017-03-01T10:36:53Z " +
			"2017-03-01T10:37:17Z 2017-03-03T10:22:55Z 2017-03-03T10:23:19Z 2017-03-03T10:23:43Z 2017-03-03T10:24:07Z " +
			"2017-03-03T10:24:31Z 2017-03-03T10:24:54Z 2017-03-03T10:25:18Z 2017-03-03T10:25:42Z 2017-03-03T10:26:06Z " +
			"2017-03-03T10:26:30Z 2017-03-03T10:26:54Z 2017-03-03T10:27:18Z 2017-03-05T10:13:20Z 2017-03-05T10:13:43Z " +
			"2017-03-05T10:14:07Z 2017-03-05T10:14:31Z 2017-03-05T10:14:55Z 2017-03-06T10:49:25Z 2017-03-06T10:49:49Z " +
			"2017-03-06T10:50:13Z 2017-03-06T10:50:36Z 2017-03-06T10:51:00Z 2017-03-06T10:51:24Z 2017-03-06T10:51:48Z " +
			"2017-03-06T10:52:12Z 2017-03-08T10:38:38Z 2017-03-08T10:39:01Z 2017-03-08T10:39:25Z 2017-03-08T10:39:49Z " +
			"2017-03-08T10:40:13Z 2017-03-08T10:40:37Z 2017-03-08T10:41:01Z 2017-03-08T10:41:25Z 2017-03-08T10:41:49Z " +
			"2017-03-08T10:42:13Z 2017-03-10T10:28:14Z 2017-03-10T10:28:38Z 2017-03-10T10:29:02Z 2017-03-10T10:29:26Z " +
			"2017-03-10T10:29:50Z 2017-03-10T10:30:14Z 2017-03-10T10:30:38Z 2017-03-10T10:31:01Z 2017-03-10T10:31:25Z " +
			"2017-03-10T10:31:49Z 2017-03-10T10:32:13Z 2017-03-10T10:32:37Z 2017-03-11T11:08:01Z 2017-03-11T11:08:18Z " +
			"2017-03-12T10:17:51Z 2017-03-12T10:18:15Z 2017-03-12T10:18:39Z 2017-03-12T10:19:02Z 2017-03-12T10:19:26Z " +
			"2017-03-12T10:19:50Z 2017-03-12T10:20:14Z 2017-03-12T10:20:38Z 2017-03-12T10:21:02Z 2017-03-14T10:08:39Z " +
			"2017-03-15T10:43:56Z 2017-03-15T10:44:20Z 2017-03-15T10:44:44Z 2017-03-15T10:45:08Z 2017-03-15T10:45:32Z " +
			"2017-03-15T10:45:56Z 2017-03-15T10:46:20Z 2017-03-15T10:46:44Z " +
			"2017-03-15T10:47:07Z</gmlrgrid:coefficients>" +
			"            <gmlrgrid:gridAxesSpanned>unix</gmlrgrid:gridAxesSpanned>" +
			"            <gmlrgrid:sequenceRule axisOrder=\"+1\">Linear</gmlrgrid:sequenceRule>" +
			"          </gmlrgrid:GeneralGridAxis>" +
			"        </gmlrgrid:generalGridAxis>" +
			"      </gmlrgrid:ReferenceableGridByVectors>" +
			"    </domainSet>" +
			"    <gmlcov:rangeType>" +
			"      <swe:DataRecord>" +
			"        <swe:field name=\"Red\">" +
			"          <swe:Quantity>" +
			"            <swe:nilValues>" +
			"              <swe:NilValues>" +
			"                <swe:nilValue reason=\"\">-9999</swe:nilValue>" +
			"              </swe:NilValues>" +
			"            </swe:nilValues>" +
			"            <swe:uom code=\"10^0\"/>" +
			"          </swe:Quantity>" +
			"        </swe:field>" +
			"        <swe:field name=\"Green\">" +
			"          <swe:Quantity>" +
			"            <swe:uom code=\"10^0\"/>" +
			"          </swe:Quantity>" +
			"        </swe:field>" +
			"        <swe:field name=\"Blue\">" +
			"          <swe:Quantity>" +
			"            <swe:uom code=\"10^0\"/>" +
			"          </swe:Quantity>" +
			"        </swe:field>" +
			"      </swe:DataRecord>" +
			"    </gmlcov:rangeType>" +
			"    <wcs:ServiceParameters>" +
			"      <wcs:CoverageSubtype>ReferenceableGridCoverage</wcs:CoverageSubtype>" +
			"      <CoverageSubtypeParent xmlns=\"http://www.opengis.net/wcs/2.0\">" +
			"        <CoverageSubtype>AbstractDiscreteCoverage</CoverageSubtype>" +
			"        <CoverageSubtypeParent>" +
			"          <CoverageSubtype>AbstractCoverage</CoverageSubtype>" +
			"        </CoverageSubtypeParent>" +
			"      </CoverageSubtypeParent>" +
			"      <wcs:nativeFormat>application/octet-stream</wcs:nativeFormat>" +
			"    </wcs:ServiceParameters>" +
			"  </wcs:CoverageDescription>" +
			"</wcs:CoverageDescriptions>";

	@Test
	public void deserializeTest() throws IOException {
		String config = Resources.toString(Resources.getResource("pipeline-config.json"), Charsets.UTF_8);
		PipelineConfiguration pipelineConfig = null;
		if (!config.trim().isEmpty()) {
			pipelineConfig = mapper.readValue(config, PipelineConfiguration.class);
		}
		System.out.println(mapper.writeValueAsString(pipelineConfig));
	}

	@Test
	public void processTest() throws IOException, ProcessingPipelineException {
		ProcessingPipeline pipeline = new ProcessingPipeline();

		Map<String, Object> output = pipeline.process(input, "xml");
		System.out.println(output);
	}
	
	@Test
	public void newProcessTest() throws ProcessingPipelineHandlerException, OperationNotSupportedException, IOException {
		String config = Resources.toString(Resources.getResource("pipeline-config_new.json"), Charsets.UTF_8);
		
		List<PipelineStep> steps = mapper.readValue(config, new TypeReference<List<PipelineStep>>(){});
		Pipeline pipeline = new Pipeline(steps.toArray(new PipelineStep[0]));
		
		/*MapStep mapStep = new MapStep();
		
		Map<String, String> mappings = new HashMap<>();
		
		mappings.put("name", "//*[local-name()='CoverageId']/text()");
		mappings.put("lowerCorner", "//*[local-name()='lowerCorner']/text()");
		
		mapStep.setMappings(mappings);
		mapStep.setFormat(Format.XML);
		
		Pipeline pipeline1 = new Pipeline(mapStep);*/
		
		Object results = pipeline.process(input);
	}

}
