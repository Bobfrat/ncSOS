package thredds.server.sos.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import thredds.server.sos.util.XMLDomUtils;

import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Get Observation Parser
 * @author abird
 */
public class SOSGetObservationRequestHandler extends SOSBaseRequestHandler {

    public final static String TEMPLATE = "templates/sosGetObservation.xml";
    private String stationName;
    private String[] variableNames;
    private String[] eventTime;
    private boolean isMultiTime;
    private final StationData stD;

    public SOSGetObservationRequestHandler(NetcdfDataset netCDFDataset, String[] stationName, String[] variableNames, String[] eventTime) throws IOException {
        super(netCDFDataset);

        //this.stationName = stationName[0];        

        //check for height and add it, if there of course
        CoordinateAxis heightAxis = netCDFDataset.findCoordinateAxis(AxisType.Height);
        this.variableNames = checkNetcdfFileForHeight(heightAxis, variableNames);

        //TODO add this in to code
        /*
        /////////////////////////CHECK for single profile//////////////////////////////////////      
        Variable z = netCDFDataset.findVariable("profile");      
        if (z!=null){ 
        Attribute zz = z.findAttribute("cf_role");
        if(zz!=null){
        System.out.println("profileID" + z.read().toString());
        }
        }
        //////////////////////////////////////////////////////////////////////////////////////        
         * 
         */

        this.eventTime = eventTime;

        //one per request
        stD = new StationData(stationName, eventTime, this.variableNames);

        //Time Series
        if (getFeatureCollection() != null) {
            stD.setData(getFeatureCollection());
        }

        //profile Collection
        //timeseriesprofile
        //get the station profile based on station name
        if (getFeatureProfileCollection() != null) {
            stD.setData(getFeatureProfileCollection());
        }

        //profile
        //gets the profile based on event time
        if (getProfileFeatureCollection() != null) {
            stD.setData(getProfileFeatureCollection());
        }

    }

    public StationData getStationData() {
        return stD;
    }

    /**
     * checks for the presence of height in the netcdf dataset if it finds it but not in the variables selected it adds it
     * @param heightAxis
     * @param variableNames1
     * @return 
     */
    private String[] checkNetcdfFileForHeight(CoordinateAxis heightAxis, String[] variableNames1) {
        if (heightAxis != null) {
            List<String> variableNamesNew = new ArrayList<String>();
            //check to see if Z present
            boolean foundZ = false;
            for (int i = 0; i < variableNames1.length; i++) {
                String zAvail = variableNames1[i];

                if (zAvail.equalsIgnoreCase(heightAxis.getName())) {
                    foundZ = true;
                    break;
                }
            }

            //if it not found add it!
            if (foundZ == false) {
                variableNamesNew = new ArrayList<String>();
                for (int i = 0; i < variableNames1.length; i++) {
                    variableNamesNew.add(variableNames1[i]);
                }
                variableNamesNew.add(heightAxis.getName());
                variableNames1 = new String[variableNames1.length + 1];
                variableNames1 = (String[]) variableNamesNew.toArray(variableNames1);
                //*******************************
            }
        }
        return variableNames1;
    }

    @Override
    public String getTemplateLocation() {
        return TEMPLATE;
    }

    public boolean getIfMultiTime() {
        return isMultiTime;
    }

    private void addDatasetResults(String[] obsProperty, int stationNumber) {
        //TODO Test the following
        //add Data Block Definition
        document = XMLDomUtils.addNode(document, "om:result", "swe:DataArray", stationNumber);
        //element count
        document = XMLDomUtils.addNode(document, "swe:DataArray", "swe:elementCount", stationNumber);
        document = XMLDomUtils.addNode(document, "swe:elementCount", "swe:Count", stationNumber);
        document = XMLDomUtils.addNodeAndValue(document, "swe:Count", "swe:value", "COUNT!!!!", stationNumber);
        //element Type
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataArray", "swe:elementType", "name", "SimpleDataArray", stationNumber);
        //add data record
        document = XMLDomUtils.addNode(document, "swe:elementType", "swe:DataRecord", stationNumber);

        createDataFields(obsProperty, stationNumber);

        //add encoding value
        document = XMLDomUtils.addNode(document, "swe:DataArray", "swe:encoding", stationNumber);
        // text block
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:encoding", "swe:TextBlock", "blockSeparator", " ", stationNumber);
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "decimalSeparator", ".");
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "tokenSeparator", ",");

        try {
            //set the data
            document = XMLDomUtils.addNodeAndValue(document, "swe:DataArray", "swe:values", createObsValuesString(stationNumber), stationNumber);
        } catch (Exception ex) {
            document = XMLDomUtils.addNodeAndValue(document, "swe:DataArray", "swe:values", "ERROR!", stationNumber);
            Logger.getLogger(SOSGetObservationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createDataFields(String[] observedProperties, int stationNumber) {
        int fieldIndex = 0;
        int quantityIndex = 0;

//        //add fields
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataRecord", "swe:field", "name", "time", stationNumber);
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:field", "swe:Time", fieldIndex++, "definition", "urn:ogc:phenomenon:time:iso8601", stationNumber);

        // add observed property
        for (String observedProperty : observedProperties) {

            VariableSimpleIF variable = getFeatureDataset().getDataVariable(observedProperty);
            document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataRecord", "swe:field", "name", observedProperty, stationNumber);
            document = XMLDomUtils.addNodeAndAttribute(document, "swe:field", "swe:Quantity", fieldIndex++, "definition", "urn:ogc:def:phenomenon:mmisw.org:cf:" + observedProperty, stationNumber);

            //added logic abird
            if (variable != null) {
                document = XMLDomUtils.addNodeAndAttribute(document, "swe:Quantity", "swe:uom", quantityIndex++, "code", variable.getUnitsString(), stationNumber);
            }
        }
    }

    public String getSystemGMLID() {
        return XMLDomUtils.getObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id");
    }

    public void setSystemGMLID(String value) {
        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", value);
    }

    void setSystemGMLID() {

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < stD.getNumberOfStations(); i++) {
            b.append(stationName);
            b.append(",");
        }
        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", getGMLID("GML_ID_NAME"));
    }

    public void setCollectionDescription() {
        XMLDomUtils.setNodeValue(document, "om:ObservationCollection", "gml:description", getDescription());
    }

    public String getCollectionDescription() {
        return XMLDomUtils.getNodeValue(document, "om:ObservationCollection", "gml:description");
    }

    public void setCollectionName() {
        XMLDomUtils.setNodeValue(document, "om:ObservationCollection", "gml:name", getTitle());
    }

    public String getCollectionName() {
        return XMLDomUtils.getNodeValue(document, "om:ObservationCollection", "gml:name");
    }

    public void setCollectionSourceName() {
        XMLDomUtils.setAttributeFromNode(document, "gml:boundedBy", "gml:Envelope", "srsName", "EPSG:4326");
    }

    public String getCollectionSourceName() {
        return XMLDomUtils.getAttributeFromNode(document, "gml:boundedBy", "gml:Envelope", "srsName");
    }

    public void setCollectionLowerCornerEnvelope() {
        XMLDomUtils.setNodeValue(document, "gml:Envelope", "gml:lowerCorner", getBoundLowerLatLonStr());
    }

    public String getCollectionLowerCornerEnvelope() {
        return XMLDomUtils.getNodeValue(document, "gml:Envelope", "gml:lowerCorner");
    }

    public void setCollectionUpperCornerEnvelope() {
        XMLDomUtils.setNodeValue(document, "gml:Envelope", "gml:upperCorner", getBoundUpperLatLonStr());
    }

    public String getCollectionUpperCornerEnvelope() {
        return XMLDomUtils.getNodeValue(document, "gml:Envelope", "gml:upperCorner");
    }

    /*
     * Create the observation XML data for getObs
     */
    public void parseObservations() {

        setObsCollectionMetaData();

        //add observation 
        //*********THIS IS FOR NUMBER OF STATIONS!
        for (int stNum = 0; stNum < stD.getNumberOfStations(); stNum++) {
            document = XMLDomUtils.addObservationElement(document);
            //add description
            document = XMLDomUtils.addNodeAndValue(document, "om:Observation", "gml:description", getDescription(), stNum);
            //add name
            document = XMLDomUtils.addNodeAndValue(document, "om:Observation", "gml:name", getDescription(), stNum);
            //add bounded by
            document = XMLDomUtils.addNode(document, "om:Observation", "gml:boundedBy", stNum);
            //add envelope and attribute
            document = XMLDomUtils.addNodeToNodeAndAttribute(document, "om:Observation", "gml:boundedBy", "gml:Envelope", "srsName", getGMLName(stD.getStationName(stNum)), stNum);
            //add lat lon string
            document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:Envelope", "gml:lowerCorner", getStationLowerLatLonStr(stNum), stNum);
            //add Upper GPS coors
            document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:Envelope", "gml:upperCorner", getStationUpperLatLonStr(stNum), stNum);
            //add sampling time
            document = XMLDomUtils.addNode(document, "om:Observation", "om:samplingTime", stNum);
            //add time instant
            document = XMLDomUtils.addNodeToNodeAndAttribute(document, "om:Observation", "om:samplingTime", "gml:TimePeriod", "gml:id", "DATA_TIME", stNum);
            //add time positions (being and end)

            //******NEW DATA MANAGER************
            if (stD != null) {
                document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:beginPosition", stD.getTimeBegin(stNum), stNum);
                document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:endPosition", stD.getTimeEnd(stNum), stNum);
            }

            //add procedure
            document = XMLDomUtils.addNodeAndAttribute(document, "om:Observation", "om:procedure", "xlink:href", getLocation(), stNum);

            //add observedProperties
            for (int i = 0; i < variableNames.length; i++) {
                String variableName = variableNames[i];
                document = XMLDomUtils.addNodeAndAttribute(document, "om:Observation", "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + variableName, stNum);
            }

            //if (isDepthAvailable == true) {
            //doc = XMLDomUtils.addNodeAndAttribute(doc, "om:Observation", "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + "depth");
            //}

            //add feature of interest
            document = XMLDomUtils.addNodeAndAttribute(document, "om:Observation", "om:featureOfInterest", "xlink:href", getFeatureOfInterest(stD.getStationName(stNum)), stNum);
            //add results Node
            document = XMLDomUtils.addNode(document, "om:Observation", "om:result", stNum);

            addDatasetResults(variableNames, stNum);

        }

    }

    public String getObservationDescription() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:description");
    }

    public String getObservationName() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:name");
    }

    public String getObservationEnvelope() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:boundedBy");
    }

    public String getObservationEnvelopeSrsName() {
        return XMLDomUtils.getAttributeFromNode(document, "om:Observation", "gml:Envelope", "srsName");
    }

    String getObservationLowerCorner() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:lowerCorner");
    }

    String getObservationUpperCorner() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:upperCorner");
    }

    String getObservationTimeInstant() {
        return XMLDomUtils.getAttributeFromNode(document, "om:Observation", "gml:TimePeriod", "gml:id");
    }

    String getObservationTimePosition() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:beginPosition");
    }

    String getObservationProcedure() {
        return XMLDomUtils.getAttributeFromNode(document, "om:Observation", "om:procedure", "xlink:href");
    }

    String getObservationObservedProperty() {
        return XMLDomUtils.getAttributeFromNode(document, "om:Observation", "om:observedProperty", "xlink:href");
    }

    public String createObsValuesString(int stNum) throws Exception {
        setCount(variableNames.length+1,stNum);
        return stD.getDataResponse(stNum);
    }

    private void setObsCollectionMetaData() {
        setSystemGMLID();
        setCollectionDescription();
        setCollectionName();
        setCollectionSourceName();
        //added abird
        setCollectionLowerCornerEnvelope();
        setCollectionUpperCornerEnvelope();
    }

    public String getResultValues() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "swe:values");
    }

    private void setBeginTime(String time) {
        XMLDomUtils.setNodeValue(document, "om:Observation", "gml:beginPosition", time);
    }

    private void setEndTime(String time) {
        XMLDomUtils.setNodeValue(document, "om:Observation", "gml:EndPosition", time);
    }

    private void setCount(int count,int StNum) {
        XMLDomUtils.setNodeValue(document, "om:Observation", "swe:value", Integer.toString(count),StNum);
    }

    private String getStationLowerLatLonStr(int stNum) {
        return (new StringBuilder()).append(formatDegree(stD.getLowerLat(stNum))).append(" ").append(formatDegree(stD.getLowerLon(stNum))).append(" ").append("0").toString();
    }

    private String getStationUpperLatLonStr(int stNum) {
        return (new StringBuilder()).append(formatDegree(stD.getUpperLat(stNum))).append(" ").append(formatDegree(stD.getUpperLon(stNum))).append(" ").append("0").toString();
    }

    /**
     * get the upper lat lon string
     * @return 
     */
    private String getBoundUpperLatLonStr() {
        return (new StringBuilder()).append(formatDegree(stD.getBoundUpperLat())).append(" ").append(formatDegree(stD.getBoundUpperLon())).append(" ").append("0").toString();
    }

    /**
     * get the lower lat lon string
     * @return 
     */
    private String getBoundLowerLatLonStr() {
        return (new StringBuilder()).append(formatDegree(stD.getBoundLowerLat())).append(" ").append(formatDegree(stD.getBoundLowerLon())).append(" ").append("0").toString();
    }
}
