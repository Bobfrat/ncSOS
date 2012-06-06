/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.service;

import thredds.server.sos.getObs.SOSGetObservationRequestHandler;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import thredds.server.sos.service.StationData;
import static org.junit.Assert.*;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author abird
 */
public class StationDataTest {

    //***********************************************    
    //TIMESERIES
    //***********************************************
    @Test
    public void testCanGetCorrectStationList() {

        String[] stNames = {"station1", "station2", "station3"};
        StationData std = new StationData(stNames, null, null);
        List<String> names = std.getStationNames();
        assertTrue(names != null);

        assertEquals("station2", names.get(1));
        assertEquals("station3", names.get(2));
    }
    public static final String timeSeriesIncompleteMultiStation = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9,Station-8&eventtime=1990-02-01T00:00:00Z/1990-05-01T10:00:00Z";
    private static String tsIncompleteMultiDimensionalMultipleStations = "tests/main/resources/datasets/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2.nc";

    @Test
    public void testTIMESERIESGetCorrectUpperLonLatOneStation() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station-9", "Station-8"}, new String[]{"temperature"}, new String[]{"1990-02-01T00:00:00Z", "1990-05-01T10:00:00Z"},null);

        assertEquals(82.0, handler.getStationData().getBoundLowerLat(), 0);
        assertEquals(132.0, handler.getStationData().getBoundUpperLat(), 0);
        assertEquals(45.0, handler.getStationData().getBoundUpperLon(), 0);
        assertEquals(20.0, handler.getStationData().getBoundLowerLon(), 0);
    }

    @Test
    public void testTIMESERIESGetCorrectUpperLonLatTwoStations() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station-9", "Station-8"}, new String[]{"temperature"}, new String[]{"1990-02-01T00:00:00Z", "1990-05-01T10:00:00Z"},null);

        assertEquals(82.0, handler.getStationData().getBoundLowerLat(), 0);
        assertEquals(132.0, handler.getStationData().getBoundUpperLat(), 0);
        assertEquals(45.0, handler.getStationData().getBoundUpperLon(), 0);
        assertEquals(20.0, handler.getStationData().getBoundLowerLon(), 0);
    }

    @Test
    public void testTIMESERIESCanGetAndSetStartDate() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station-9", "Station-8"}, new String[]{"temperature"}, new String[]{"1990-02-01T00:00:00Z", "1990-05-01T10:00:00Z"},null);
        assertEquals("1921-12-13T20:45:53Z", handler.getStationData().getBoundTimeBegin());
    }

    @Test
    public void testTIMESERIESCanGetAndSetEndDate() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station-9", "Station-8"}, new String[]{"temperature"}, new String[]{"1990-02-01T00:00:00Z", "1990-05-01T10:00:00Z"},null);
        assertEquals("1990-01-01T11:00:00Z", handler.getStationData().getBoundTimeEnd());
    }

    @Test
    public void testTIMESERIESCanGetData() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station-9", "Station-8"}, new String[]{"temperature"}, new String[]{"1990-01-01T00:00:00Z", "1990-01-01T07:00:00Z"},null);


        assertTrue(handler.getStationData().getDataResponse(0).contains("1990-01-01T00:00:00Z"));
        assertTrue(handler.getStationData().getDataResponse(0).contains("1990-01-01T02:00:00Z"));
        assertTrue(handler.getStationData().getDataResponse(0).contains("1990-01-01T05:00:00Z"));
        assertTrue(handler.getStationData().getDataResponse(0).contains("1990-01-01T06:00:00Z"));
        assertTrue(handler.getStationData().getDataResponse(0).contains("1990-01-01T08:00:00Z"));
    }
    //***********************************************    
    //TIMESERIESPROFILE
    //***********************************************
    private static String MultiDimensionalSingleStations = "tests/main/resources/datasets/timeSeriesProfile-Multidimensional-SingleStation-H.5.2/timeSeriesProfile-Multidimensional-SingleStation-H.5.2.nc";
    private static String MultiDimensionalMultiStations = "tests/main/resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";

    @Test
    public void testTIMESERIESPROFILEGetCorrectUpperLonLatOneStation() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalSingleStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station1"}, new String[]{"temperature"}, new String[]{"1990-02-01T00:00:00Z", "1990-05-01T10:00:00Z"},null);

        assertEquals(37.5, handler.getStationData().getBoundLowerLat(), 0);
        assertEquals(37.5, handler.getStationData().getBoundUpperLat(), 0);
        assertEquals(-76.5, handler.getStationData().getBoundUpperLon(), 0);
        assertEquals(-76.5, handler.getStationData().getBoundLowerLon(), 0);
    }

    @Test
    public void testTIMESERIESPROFILEGetCorrectStartEndDateOneStation() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalSingleStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station1"}, new String[]{"temperature"}, new String[]{"1990-02-01T00:00:00Z", "1990-05-01T10:00:00Z"},null);

        assertEquals("1990-01-01T00:00:00Z", handler.getStationData().getBoundTimeBegin());
        assertEquals("1990-01-01T03:00:00Z", handler.getStationData().getBoundTimeEnd());
    }

    @Test
    public void testTIMESERIESPROFILEGetCorrectStartEndDateTwoStations() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station1", "Station2"}, new String[]{"temperature"}, new String[]{"1990-02-01T00:00:00Z", "1990-05-01T10:00:00Z"},null);

        assertEquals("1990-01-01T00:00:00Z", handler.getStationData().getBoundTimeBegin());
        assertEquals("1990-01-01T03:00:00Z", handler.getStationData().getBoundTimeEnd());
    }

    @Test
    public void testTIMESERIESPROFILEGetCorrectUpperLonLatTwoStation() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"Station1", "Station2"}, new String[]{"temperature"}, new String[]{"1990-02-01T00:00:00Z", "1990-05-01T10:00:00Z"},null);

        assertEquals(32.5, handler.getStationData().getBoundLowerLat(), 0);
        assertEquals(37.5, handler.getStationData().getBoundUpperLat(), 0);
        assertEquals(-76.5, handler.getStationData().getBoundUpperLon(), 0);
        assertEquals(-78.3, handler.getStationData().getBoundLowerLon(), 0);
    }
    //***********************************************    
    //PROFILE
    //***********************************************
    private static String ContiguousRaggedMultipleProfiles = "tests/main/resources/datasets/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";

    @Test
    public void testPROFILEGetCorrectUpperLonLatOneStation() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"1"}, new String[]{"temperature"}, new String[]{"1990-01-01T00:00:00Z", "1990-01-01T10:00:00Z"},null);

        assertEquals(13, handler.getStationData().getBoundLowerLat(), 0);
        assertEquals(104, handler.getStationData().getBoundUpperLat(), 0);
        assertEquals(134, handler.getStationData().getBoundUpperLon(), 0);
        assertEquals(15, handler.getStationData().getBoundLowerLon(), 0);
    }

    @Test
    public void testPROFILEGetCorrectStartEndDate() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset, new String[]{"1"}, new String[]{"temperature"}, new String[]{"1990-01-01T00:00:00Z", "1990-01-01T10:00:00Z"},null);

        assertEquals("1990-01-01T00:00:00Z", handler.getStationData().getBoundTimeBegin());
        assertEquals("1990-01-01T03:00:00Z", handler.getStationData().getBoundTimeEnd());
    }
}
