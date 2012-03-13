package thredds.server.sos.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import thredds.server.sos.util.XMLDomUtils;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * SOSParser based on EnhancedMetadataService
 * @author: Andrew Bird
 * Date: 2011
 */
public class SOSParser {

//    private static final Logger _log = Logger.getLogger(SOSParser.class);
    private static String service;
    private static String version;
    private static String request;
    private static String observedProperty;
    private static String[] offering;
    private static String singleEventTime;
    //used for the cases where multiple props are selected
    private static String[] observedProperties;
    //used for the cases where muliple event times are selected
    private static String[] eventTime;
    //used for cacheing getcaps and get obs results
    private static String cache;
    private static String CACHE_TRUE = "true";
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSParser.class);

    /**
     * Enhance NCML with Data Discovery conventions elements if not already in place in the metadata.
     *
     * @param dataset NetcdfDataset to enhance the NCML
     * @param writer writer to send enhanced NCML to
     */
    public static void enhance(final NetcdfDataset dataset, final Writer writer, final String query, String threddsURI) {

        eventTime = null;

        try {
            if (query != null) {
                //if query is not empty
                //set the query params then call on the fly
                splitQuery(query);

                //if all the fields are valid ie not null
                if ((service != null) && (request != null) && (version != null)) {
                    //get caps
                    if (request.equalsIgnoreCase("GetCapabilities")) {

                        //check to see if use cache was set to true if so load the data file DONT PARSE IT
                        if (cache !=null && cache.equalsIgnoreCase(CACHE_TRUE)) {
                            //Check to see if get caps exists, if it does not actual parse the file
                            File f = new File("c:/xmlFile.xml");
                            if (f.exists()) {
                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                DocumentBuilder builder = factory.newDocumentBuilder();
                                Document doc = builder.parse(f);
                                writeDocument(doc, writer);
                                writer.flush();
                                writer.close();
                                    _log.info("CACHING: using cached XML file");
                            } else {
                                //parse the file anyway
                                SOSGetCapabilitiesRequestHandler handler = performSOSGetCaps(dataset, threddsURI, writer);
                                handler.finished();
                                handler = null;
                                //GetCaps file writing if not there
                                writer.flush();
                                writer.close();
                                //write the file
                                fileWriter("c:/", "xmlFile.xml", writer);
                            }
                            //if it is just a regular request.....    
                        } else {
                            SOSGetCapabilitiesRequestHandler handler = performSOSGetCaps(dataset, threddsURI, writer);
                            handler.finished();
                            handler = null;
                            writer.flush();
                            writer.close();
                        }

                    } else if (request.equalsIgnoreCase("DescribeSensor")) {
                        writeErrorXMLCode(writer);
                    } else if (request.equalsIgnoreCase("GetObservation")) {
                        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, offering, observedProperties, eventTime);
                        handler.parseObservations();
                        writeDocument(handler.getDocument(), writer);
                        handler.finished();
                    } else {
                        writeErrorXMLCode(writer);
                    }

                } //else if the above is not true print invalid xml text
                else {
                    writeErrorXMLCode(writer);
                }
            } else if (query == null) {
                //if the entry is null just print out the get caps xml
//                _log.info("Null query string/params: using get caps");
                SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
                handler.parseTemplateXML();
                handler.parseServiceIdentification();
                handler.parseServiceDescription();
                handler.parseOperationsMetaData();
                handler.parseObservationList();
                writeDocument(handler.getDocument(), writer);
                handler.finished();
            }

            //catch
        } catch (NullPointerException e) {
//            _log.error(e);
            Logger.getLogger(SOSParser.class.getName()).log(Level.SEVERE, "Null Pointer in Data?", e);
        } catch (Exception e) {
//            _log.error(e);
            Logger.getLogger(SOSParser.class.getName()).log(Level.SEVERE, "Null", e);
        }
    }

    private static SOSGetCapabilitiesRequestHandler performSOSGetCaps(final NetcdfDataset dataset, String threddsURI, final Writer writer) throws IOException, TransformerException {
        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
        handler.parseServiceIdentification();
        handler.parseServiceDescription();
        handler.parseOperationsMetaData();
        handler.parseObservationList();
        writeDocument(handler.getDocument(), writer);
        return handler;
    }

    private static void fileWriter(String base, String fileName, Writer write) throws IOException {
        try {
            Writer output = null;
            File file = new File(base + fileName);
            output = new BufferedWriter(new FileWriter(file));
            output.write(write.toString());
            output.close();
            _log.info("Your file has been written");
        } catch (IOException ex) {
            _log.info("CACHING:issue with XML file writing");
        }
    }

    private static void writeErrorXMLCode(final Writer writer) throws IOException, TransformerException {
        Document doc = XMLDomUtils.getExceptionDom();
        writeDocument(doc, writer);
    }

    public String getCacheValue() {
        return cache;
    }

    public static void writeDocument(Document dom, final Writer writer) throws TransformerException, IOException {
        DOMSource source = new DOMSource(dom);
        Result result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, result);
    }

    public static void splitQuery(String query) {
        String[] splitQuery = query.split("&");
        service = null;
        version = null;
        request = null;
        cache = null;
        observedProperty = null;

        if (splitQuery.length > 2) {
            for (int i = 0; i < splitQuery.length; i++) {
                String parsedString = splitQuery[i];
                String[] splitServiceStr = parsedString.split("=");
                if (splitServiceStr[0].equalsIgnoreCase("service")) {
                    SOSParser.service = splitServiceStr[1];
                } else if (splitServiceStr[0].equalsIgnoreCase("version")) {
                    SOSParser.version = splitServiceStr[1];
                } else if (splitServiceStr[0].equalsIgnoreCase("useCache")) {
                    SOSParser.cache = splitServiceStr[1];
                } else if (splitServiceStr[0].equalsIgnoreCase("request")) {
                    SOSParser.request = splitServiceStr[1];
                } else if (splitServiceStr[0].equalsIgnoreCase("observedProperty")) {
                    SOSParser.observedProperty = splitServiceStr[1];
                    if (observedProperty.contains(",")) {
                        SOSParser.observedProperties = observedProperty.split(",");
                    } else {
                        SOSParser.observedProperties = new String[]{SOSParser.observedProperty};
                    }
                } else if (splitServiceStr[0].equalsIgnoreCase("offering")) {
                    //replace all the eccaped : with real ones
                    String temp = splitServiceStr[1];
                    String replaceOffer = temp.replaceAll("%3A", ":");

                    //split on ,
                    String[] howManyStation = replaceOffer.split(",");

                    List<String> stList = new ArrayList<String>();

                    for (int j = 0; j < howManyStation.length; j++) {
                        //split on :
                        String[] splitStr = howManyStation[j].split(":");
                        String stationName = splitStr[splitStr.length - 1];
                        stList.add(stationName);
                    }

                    //String[] toArray = (String[] )stList.toArray();
                    //MetadataParser.offering = toArray;

                    Object[] objectArray = stList.toArray();
                    String[] array = (String[]) stList.toArray(new String[stList.size()]);

                    SOSParser.offering = array;

                } else if (splitServiceStr[0].equalsIgnoreCase("eventtime")) {

                    SOSParser.singleEventTime = splitServiceStr[1];
                    if (singleEventTime.contains("/")) {
                        SOSParser.eventTime = singleEventTime.split("/");
                    } else {
                        SOSParser.eventTime = new String[]{SOSParser.singleEventTime};
                    }

                }
            }
        }
    }
}
