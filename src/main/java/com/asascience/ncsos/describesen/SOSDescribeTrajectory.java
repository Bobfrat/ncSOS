/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import ucar.ma2.Array;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarPeriod;
import ucar.nc2.units.DateFormatter;

/**
 * Handles Describe Sensor requests for Trajectory feature datasets.
 * Describe Sensor requests to Trajectory datasets for response format "sensorML/1.0.1"
 * output the following xml subroots:
 * *Description
 * *Identification
 * *Classification
 * *Contact(s)
 * *History
 * *Position
 * *Component(s)
 * @author SCowan
 * @version 1.0.0
 */
public class SOSDescribeTrajectory extends SOSDescribeStation implements SOSDescribeIF {
    
    private Variable indexDescriptor;
    private int stationIndex;
    private int stationObsIndexLower, stationObsIndexUpper;
    private Integer[] stationObsIndices;
    private CalendarDate startDate;
    
    /**
     * Creates a new instance that collects, from the dataset, information needed
     * for a Describe Sensor response.
     * @param dataset a netcdf dataset of a Trajectory feature type
     * @param procedure request procedure (station urn)
     * @param startDate start date of the dataset (elapsed time is 0)
     */
    public SOSDescribeTrajectory( NetcdfDataset dataset, String procedure, CalendarDate startDate ) throws IOException {
        super(dataset, procedure);
        // ignore errors from parent constructor
        errorString = null;
        
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            // look for our var for either index or rowSize vars
            if (varName.contains("rowsize") || varName.contains("index")) {
                indexDescriptor = var;
            }
        }
        
        this.startDate = startDate;
        
        // get our station index
        stationIndex = getStationIndex(stationName);
        
        if (stationIndex < 0) {
            errorString = "Station not part of dataset: " + stationName;
        }
        
        // get our observation indices
        if (indexDescriptor.getFullName().toLowerCase().contains("rowsize")) {
            stationObsIndexLower = 0;
            stationObsIndexUpper = 0;
            try {
                // our obs is contiguous, need to find where it starts and ends
                Array rowArray = indexDescriptor.read();
                for (int i=0; i<rowArray.getSize(); i++) {
                    if (i < stationIndex) {
                        stationObsIndexLower += rowArray.getInt(i);
                    } else if (i == stationIndex) {
                        stationObsIndexUpper = stationObsIndexLower + rowArray.getInt(i);
                        break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("exception " + ex.getMessage());
            }
        } else {
            try {
                // our observation is not contiguous, so we need to get the indices of each observation that coresponds to our station
                Array indexArray;
                indexArray = indexDescriptor.read();
                ArrayList<Integer> indexBuilder = new ArrayList<Integer>();
                for (int i=0; i<indexArray.getSize(); i++) {
                    if (indexArray.getInt(i) == stationIndex) {
                        indexBuilder.add(i);
                    }
                }
                stationObsIndices = indexBuilder.toArray(new Integer[indexBuilder.size()]);
            } catch (IOException ex) {
                System.out.println("exception " + ex.getMessage());
            }
        }
    }

    /*********************/
    /* Interface Methods */
    /**************************************************************************/
    
    @Override
    public void setupOutputDocument(DescribeSensorFormatter output) {
        if (errorString == null) {
            // system node
            output.setSystemId("station-" + stationName);
            // set description
            formatSetDescription(output);
            // identification node
            formatSetIdentification(output);
            // classification node
            formatSetClassification(output);
            // contact node
            formatSetContactNodes(output);
            // history node
            formatSetHistoryNodes(output);
            // position node
            formatSetPositionNode(output);
            // remove unwanted nodes
            removeUnusedNodes(output);
        } else {
            output.setupExceptionOutput(errorString);
        }
    }
    
    /**************************************************************************/
    
    /*******************
     * Private Methods *
     *******************/
    
    private void removeUnusedNodes(DescribeSensorFormatter output) {
        output.deleteLocationNode();
        output.deletePositions();
        // time position requires data not obtained here
        output.deleteTimePosition();
    }

    private void formatSetPositionNode(DescribeSensorFormatter output) {
        // set our position name
        output.setPositionName("stationTrajectory");
        // set our data definition
        // first get our contentmap from our vars
        HashMap<String, String> varMap;
        HashMap<String, HashMap<String, String>> mapMap = new LinkedHashMap<String, HashMap<String, String>>();
        // add time, lat and lon in that order
        // time
        varMap = new HashMap<String, String>();
        varMap.put("definition", "OGC:time");
        varMap.put("xlink:href", "ISO####:date");
        mapMap.put("time", varMap);
        // lat
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", "deg");
        mapMap.put("lat", varMap);
        // lon
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", "deg");
        mapMap.put("lon", varMap);
        
        output.setPositionDataDefinition(mapMap, ".", " ", ",");
        
        // now we need our values
        StringBuilder strB = new StringBuilder();
        try {
            DateFormatter formatter = new DateFormatter();
            Array timeArray = timeVariable.read();
            Array latArray = latVariable.read();
            Array lonArray = lonVariable.read();
            
            if (stationObsIndices != null) {
                for (int i=0; i<stationObsIndices.length; i++) {
                    int obsIndex = stationObsIndices[i].intValue();
                    strB.append(formatter.toDateTimeStringISO(startDate.add(timeArray.getDouble(obsIndex), CalendarPeriod.Field.Second).toDate())).append(",");
                    strB.append(latArray.getDouble(obsIndex)).append(",");
                    strB.append(lonArray.getDouble(obsIndex)).append(" ");
                }
            } else {
                for (int j=stationObsIndexLower; j<stationObsIndexUpper; j++) {
                    strB.append(formatter.toDateTimeStringISO(startDate.add(timeArray.getDouble(j), CalendarPeriod.Field.Second).toDate())).append(",");
                    strB.append(latArray.getDouble(j)).append(",");
                    strB.append(lonArray.getDouble(j)).append(" ");
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception " + ex.getMessage());
        } 
        
        // print out our values
        output.setPositionValue(strB.toString());
    }
    
}