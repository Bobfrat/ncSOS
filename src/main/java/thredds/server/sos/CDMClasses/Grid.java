package thredds.server.sos.CDMClasses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.nc2.units.DateFormatter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import thredds.server.sos.getObs.SOSObservationOffering;
import ucar.ma2.Array;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.Station;

/**
 * RPS - ASA
 * @author abird
 * @version 
 *
 * 
 *
 */
public class Grid extends baseCDMClass implements iStationData {

    public static final String LAT = "lat";
    public static final String LON = "lon";
    private List<String> stationNameList;
    private List<String> stationDescripList;
    private final String[] variableNames;
    private final ArrayList<String> eventTimes;
    private GridDataset GridData;
    private final Map<String, String> latLonRequest;
    DateFormatter dateFormatter = new DateFormatter();

    public Grid(String[] requestedStationNames, String[] eventTime, String[] variableNames, Map<String, String> latLonRequest) {
        startDate = null;
        endDate = null;
        this.variableNames = (variableNames);

        this.reqStationNames = new ArrayList<String>();
        this.reqStationNames.addAll(Arrays.asList(requestedStationNames));
        this.eventTimes = new ArrayList<String>();
        eventTimes.addAll(Arrays.asList(eventTime));
        this.latLonRequest = latLonRequest;
        this.stationNameList = new ArrayList<String>();
        this.stationDescripList = new ArrayList<String>();
    }

    public void addDateEntry(StringBuilder builder, Date[] dates) {
        builder.append(dateFormatter.toDateTimeStringISO(dates[0]));
        builder.append(",");
    }

    public void addVariableEntrys(StringBuilder builder, double[] latDbl, Map<String, Integer> latlon, double[] lonDbl, GridDatatype grid, Array data) {
    }

    public void appendEndOfEntry(StringBuilder builder) {
        builder.append(" ");
        builder.append("\n");
    }

    public double[] getLatCoordData() {
        CoordinateAxis1D latData = (CoordinateAxis1D) GridData.getDataVariable(LAT);
        double[] latDbl = latData.getCoordValues();
        return latDbl;
    }

    public double[] getLonCoordData() {
        CoordinateAxis1D lonData = (CoordinateAxis1D) GridData.getDataVariable(LON);
        double[] lonDbl = lonData.getCoordValues();
        return lonDbl;
    }

    @Override
    public void setData(Object griddedDataset) throws IOException {
        this.GridData = (GridDataset) griddedDataset;

        setStartDate(df.toDateTimeStringISO(GridData.getCalendarDateStart().toDate()));
        setEndDate(df.toDateTimeStringISO(GridData.getCalendarDateEnd().toDate()));

        //check and only add stations that are of interest
        int stCount = 0;
        for (int i = 0; i < GridData.getGrids().size(); i++) {
            if (!GridData.getGrids().get(i).getFullName().equalsIgnoreCase("cloud_land_mask")) {
                stCount++;
                stationNameList.add(GridData.getGrids().get(i).getFullName());
                stationDescripList.add(GridData.getGrids().get(i).getDescription());
            }
        }

        setNumberOfStations(stCount);

        CoordinateAxis1D lonData = (CoordinateAxis1D) GridData.getDataVariable(LON);
        CoordinateAxis1D latData = (CoordinateAxis1D) GridData.getDataVariable(LAT);
        double[] lonDbl = lonData.getCoordValues();
        double[] latDbl = latData.getCoordValues();

        upperLat = (latDbl[latDbl.length - 1]);
        lowerLat = (latDbl[0]);
        upperLon = (lonDbl[lonDbl.length - 1]);
        lowerLon = (lonDbl[0]);

    }

    @Override
    public void setInitialLatLonBounaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDataResponse(int stNum) {
        if (GridData != null) {
            StringBuilder builder = new StringBuilder();
            Array data = null;
            double[] lonDbl = getLonCoordData();
            double[] latDbl = getLatCoordData();
            Map<String, Integer> latlon = findDataIndexs(lonDbl, latDbl, latLonRequest);

            GridDatatype grid = GridData.getGrids().get(0);
            GridCoordSystem gcs = grid.getCoordinateSystem();
            //CoordinateAxis xAxis = gcs.getXHorizAxis();
            //CoordinateAxis yAxis = gcs.getYHorizAxis();
            CoordinateAxis1D zAxis = gcs.getVerticalAxis(); // may be null
            
            int depthHeight=-1;
            if (zAxis ==null){
                depthHeight =0;
            }
            
            java.util.Date[] dates = null;
            if (gcs.hasTimeAxis1D()) {
                CoordinateAxis1DTime tAxis1D = gcs.getTimeAxis1D();
                dates = tAxis1D.getTimeDates();

            } else if (gcs.hasTimeAxis()) {
                CoordinateAxis tAxis = gcs.getTimeAxis();
            }

            int dateIndex =0;
            
            
            
            addDateEntry(builder, dates);
            for (int j = 0; j < variableNames.length; j++) {
                if (variableNames[j].equalsIgnoreCase(LAT)) {
                    builder.append(latDbl[latlon.get(LAT)]);
                } else if (variableNames[j].equalsIgnoreCase(LON)) {
                    builder.append(lonDbl[latlon.get(LON)]);
                } else {
                    //if it is not lat/lon look through all available grids to find a variable/name match then get data at that location
                    for (int i = 0; i < GridData.getGrids().size(); i++) {
                        if (GridData.getGrids().get(i).getName().equalsIgnoreCase(variableNames[j])) {
                            //get the grid of interest
                            grid = GridData.getGrids().get(i);
                            //get the data and add it to the response using data index's
                            try {
                                data = grid.readDataSlice(dateIndex, depthHeight, latlon.get(LAT), latlon.get(LON));
                                builder.append(data.getFloat(0));
                            } catch (Exception e) {
                            }
                            //break out of loop when set
                            break;
                        }
                    }
                }
                if (j < variableNames.length - 1) {
                    builder.append(",");
                }
            }
            appendEndOfEntry(builder);
            return builder.toString();
        }
        return DATA_RESPONSE_ERROR + Grid.class;

    }

    /*
    @Override
    public String getDataResponse(int stNum) {


        if (GridData != null) {
            StringBuilder builder = new StringBuilder();
            //for (int i = 0; i < GridData.getGrids().size(); i++) {
            //    if (!GridData.getGrids().get(i).getFullName().equalsIgnoreCase("cloud_land_mask")) {
            double[] lonDbl = getLonCoordData();
            double[] latDbl = getLatCoordData();

            Map<String, Integer> latlon = findDataIndexs(lonDbl, latDbl, latLonRequest);

            GridDatatype grid = GridData.getGrids().get(0);
            GridCoordSystem gcs = grid.getCoordinateSystem();
            CoordinateAxis xAxis = gcs.getXHorizAxis();
            CoordinateAxis yAxis = gcs.getYHorizAxis();
            CoordinateAxis1D zAxis = gcs.getVerticalAxis(); // may be null

            java.util.Date[] dates = null;
            if (gcs.hasTimeAxis1D()) {
                CoordinateAxis1DTime tAxis1D = gcs.getTimeAxis1D();
                dates = tAxis1D.getTimeDates();

            } else if (gcs.hasTimeAxis()) {
                CoordinateAxis tAxis = gcs.getTimeAxis();
            }

            Array data = null;
            if (dates != null) {

                //add in date loop
                addDateEntry(builder, dates);
                for (int j = 0; j < variableNames.length; j++) {
                    if (variableNames[j].equalsIgnoreCase(LAT)) {
                        builder.append(latDbl[latlon.get(LAT)]);
                    } else if (variableNames[j].equalsIgnoreCase(LON)) {
                        builder.append(lonDbl[latlon.get(LON)]);
                    } else {
                        try {
                            data = grid.readDataSlice(0, 0, latlon.get(LAT), latlon.get(LON));
                            builder.append(data.getFloat(0));
                        } catch (Exception e) {
                        }
                    }
                    if (j < variableNames.length - 1) {
                        builder.append(",");
                    }
                }
                appendEndOfEntry(builder);
                //    }

                //}
            }
            return builder.toString();
        }

        return DATA_RESPONSE_ERROR + Grid.class;
    }

     * 
     */
    @Override
    public String getStationName(int idNum) {
        return stationNameList.get(idNum);
    }

    @Override
    public String getTimeEnd(int stNum) {
        return getBoundTimeEnd();
    }

    @Override
    public String getTimeBegin(int stNum) {
        return getBoundTimeBegin();
    }

    /**
     * get capabilities response
     * @param dataset
     * @param document
     * @param GMLName
     * @param format
     * @return 
     */
    public static Document getResponse(GridDataset dataset, Document document, String GMLName, String format) {
        System.out.println("grid");
        List<GridDatatype> gridData = dataset.getGrids();
        //dataset.getCalendarDateStart();
        //dataset.getCalendarDateEnd();

        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        DateTime dt;

        for (int i = 0; i < gridData.size(); i++) {
            //check that it is not a cloud land mask
            if (!gridData.get(i).getFullName().equalsIgnoreCase("cloud_land_mask")) {

                CoordinateAxis1D lonData = (CoordinateAxis1D) dataset.getDataVariable(LON);
                CoordinateAxis1D latData = (CoordinateAxis1D) dataset.getDataVariable(LAT);
                double[] lonDbl = lonData.getCoordValues();
                double[] latDbl = latData.getCoordValues();

                SOSObservationOffering newOffering = new SOSObservationOffering();
                newOffering.setObservationStationLowerCorner(Double.toString(latDbl[0]), Double.toString(lonDbl[0]));
                newOffering.setObservationStationUpperCorner(Double.toString(latDbl[latDbl.length - 1]), Double.toString(lonDbl[lonDbl.length - 1]));

                dt = new DateTime(dataset.getCalendarDateStart().toDate());
                newOffering.setObservationTimeBegin(fmt.print(dt));

                dt = new DateTime(dataset.getCalendarDateEnd().toDate());
                newOffering.setObservationTimeEnd(fmt.print(dt));


                newOffering.setObservationStationDescription(gridData.get(i).getDescription());
                newOffering.setObservationFeatureOfInterest(gridData.get(i).getFullName());
                newOffering.setObservationName(GMLName + (gridData.get(i).getName()));
                newOffering.setObservationStationID((gridData.get(i).getName()));
                newOffering.setObservationProcedureLink(GMLName + ((gridData.get(i).getName())));
                newOffering.setObservationSrsName("EPSG:4326");  // TODO?  
                List<String> obsProperty = new ArrayList<String>();
                obsProperty.add(gridData.get(i).getDescription());

                newOffering.setObservationObserveredList(obsProperty);
                newOffering.setObservationFormat(format);

                /*
                System.out.println(gridData.get(i).getDescription());
                System.out.println(gridData.get(i).getFullName());
                System.out.println(gridData.get(i).getDataType());
                System.out.println(gridData.get(i).getInfo());
                System.out.println(gridData.get(i).getName());
                System.out.println(gridData.get(i).getUnitsString());
                 * 
                 */
                document = CDMUtils.addObsOfferingToDoc(newOffering, document);
            }
        }
        return document;
    }

    @Override
    public double getLowerLat(int stNum) {
        return Double.parseDouble(latLonRequest.get(LAT));
    }

    @Override
    public double getLowerLon(int stNum) {
        return Double.parseDouble(latLonRequest.get(LON));
    }

    @Override
    public double getUpperLat(int stNum) {
        return Double.parseDouble(latLonRequest.get(LAT));
    }

    @Override
    public double getUpperLon(int stNum) {
        return Double.parseDouble(latLonRequest.get(LON));
    }

    /**
     * find lat lon index's in X|Y axis of requested locations
     * @param lonDbl
     * @param latDbl
     * @param latLonRequest
     * @return 
     */
    private Map<String, Integer> findDataIndexs(double[] lonDbl, double[] latDbl, Map<String, String> latLonRequest) {

        Map<String, Integer> latLonIndex = new HashMap<String, Integer>();
        String lonVal = latLonRequest.get(LON);
        String latVal = latLonRequest.get(LAT);
        double requestedLon = Double.parseDouble(lonVal);
        double requestedLat = Double.parseDouble(latVal);

        int maxArrayLength = 0;
        if (lonDbl.length > maxArrayLength) {
            maxArrayLength = lonDbl.length;
        } else if (latDbl.length > maxArrayLength) {
            maxArrayLength = latDbl.length;
        }

        int latIdx = -1;
        int lonIdx = -1;
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;

        double Lat;
        double Lon;

        for (int i = 0; i < maxArrayLength; i++) {
            //array


            try {
                Lat = latDbl[i];
                double diffLat = (Lat - requestedLat);

                if (Math.abs(diffLat) < minLat) {
                    minLat = Math.abs(diffLat);
                    latIdx = i;
                }

            } catch (Exception e) {
            }

            //double Lon = Double.NaN;

            try {
                Lon = lonDbl[i];
                double diffLon = (Lon - requestedLon);
                if (Math.abs(diffLon) < minLon) {
                    minLon = Math.abs(diffLon);
                    lonIdx = i;
                }
            } catch (Exception e) {
            }

        }

        latLonIndex.put(LAT, latIdx);
        latLonIndex.put(LON, lonIdx);
        return latLonIndex;
    }

    @Override
    public String getDescription(int stNum) {
        return stationDescripList.get(stNum);
    }
}
