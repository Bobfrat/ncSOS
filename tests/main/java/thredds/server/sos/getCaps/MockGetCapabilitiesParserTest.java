/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thredds.server.sos.getCaps;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import thredds.server.sos.bean.Extent;
import thredds.server.sos.service.DatasetMetaData;
import thredds.server.sos.util.ThreddsExtentUtil;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * get capabilities class : parses the get caps
 * @author Abird
 */
public class MockGetCapabilitiesParserTest {

    @Test
    public void testTemplateFileLocation() throws Exception {
     String location = getClass().getClassLoader().getResource("datasets/VIMS.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);

     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);

     String serverLocation = MockGetCapP.getTemplateLocation();
     String templateFileLocation = getClass().getClassLoader().getResource(serverLocation).getPath();
     templateFileLocation = templateFileLocation.replaceAll("%20", " ");
     assertTrue(templateFileLocation.contains("templates/sosGetCapabilities.xml"));

     assertTrue(MockGetCapP.getTemplateStream()!=null);

    }

    @Test
    public void TestLoadXMLGetCapabilitiesIntoDom(){
        MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
        MockGetCapP.parseTemplateXML();
        assertTrue(MockGetCapP.getDom()!=null);

    }
   
    @Test
    public void TestGetandSetServiceIdentification(){
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
       MockGetCapP.parseTemplateXML();
       
       MockGetCapP.parseServiceIdentification();

         String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testFile.xml";
    fileName = fileName.replaceAll("%20", " ");
       
       String location = "ows:Title";
       MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
       String a =  loadCreatedTempXMLFileAndCheckLocation(fileName,location);

        assertEquals(a, MockGetCapP.getTitleSI());
       
    }


     @Test
    public void TestGetMockTitle(){
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
       MockGetCapP.parseTemplateXML();

       String title = MockGetCapP.getTitleSI();
       assertEquals(title, "Title");
    }

     @Test
    public void TestGetMockAbstract(){
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
       MockGetCapP.parseTemplateXML();

       String History = MockGetCapP.getAbstractSI();
       assertEquals(History, "History");
    }


      @Test
    public void TestGetMockProviderName(){
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
       MockGetCapP.parseTemplateXML();

       String title = MockGetCapP.getProviderNameSP();
       assertEquals(title, "Source");
    }

       @Test
    public void TestGetMockProviderSite(){
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
       MockGetCapP.parseTemplateXML();

       String title = MockGetCapP.getProviderSiteSP();
       assertEquals(title, "ASA");
    }

     @Test
    public void TestGetNETCDFAbstract() throws Exception{
       String location = getClass().getClassLoader().getResource("datasets/VIMS.nc").getPath();
       location = location.replaceAll("%20", " ");
       NetcdfDataset dataset = NetcdfDataset.openDataset(location);
       Extent ext = ThreddsExtentUtil.getExtent(dataset);
       DatasetMetaData dst = new DatasetMetaData(ext, dataset);
       dst.extractData();
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
       MockGetCapP.parseTemplateXML();

       String History = MockGetCapP.getAbstractSI();
       assertEquals("Converted from IMEDS Generic Format to netcdf by Applied Science Associates - Jan 18, 2011",History);
    }

     @Test
    public void TestGetNETCDFProvider() throws Exception{
       String location = getClass().getClassLoader().getResource("datasets/VIMS.nc").getPath();
       location = location.replaceAll("%20", " ");
       NetcdfDataset dataset = NetcdfDataset.openDataset(location);Extent ext = ThreddsExtentUtil.getExtent(dataset);
       DatasetMetaData dst = new DatasetMetaData(ext, dataset);
       dst.extractData();
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
       MockGetCapP.parseTemplateXML();

       String History = MockGetCapP.getProviderNameSP();
       assertEquals("Sura IOOS Model Testbed Server",History);
    }



    @Test
    public void TestGetNETCDFTitle() throws Exception{
       String location = getClass().getClassLoader().getResource("datasets/VIMS.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
       DatasetMetaData dst = new DatasetMetaData(ext, dataset);
       dst.extractData();
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
       MockGetCapP.parseTemplateXML();

       String title = MockGetCapP.getTitleSI();
       assertEquals("VIMS Selfe Predictions - Demo Test Case",title);
    }

    @Test
    public void testGetRouteElementisSosCapabilities(){
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
       MockGetCapP.parseTemplateXML();

     String a = MockGetCapP.getRouteElement();
     assertEquals(a, "Capabilities");
    }

@Test
public void testGetServiceProvider(){
 MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
       MockGetCapP.parseTemplateXML();


}

    private String loadCreatedTempXMLFileAndCheckLocation(String fileName,String Location) {
      String nodeTitle = null;
      try {
        File file = new File(fileName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList nodeLst = doc.getElementsByTagName(Location);
        Node fstNode = nodeLst.item(0);

        Element fstElmnt = (Element) fstNode;

        try {
        nodeTitle = fstElmnt.getFirstChild().getNodeValue();
        } catch (NullPointerException npe) {
          // do nothing, the element was null
        }
        
      } catch (Exception e) {
        e.printStackTrace();
      }
      return nodeTitle;

     }

@Test
public void testGetandSetServiceDescription(){
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseServiceDescription();

    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    String location = "ows:ProviderName";
    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
    String a =  loadCreatedTempXMLFileAndCheckLocation(fileName,location);

    assertEquals(a, MockGetCapP.getProviderNameSP());

}

@Test
public void testCanGetEnteredMockPhoneNumber(){
     MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseServiceDescription();

    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    String location = "ows:Voice";
    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
    String expectedNumber =  loadCreatedTempXMLFileAndCheckLocation(fileName,location);

    //expect nothing ass there is no number entered!
    assertEquals("Nothing expected as nothing is set in the phone number section","",MockGetCapP.getPhoneNoSP());
}

 private String loadCreatedTempXMLFileAndCheckAttribute(String fileName, String location,String attribute) {
        String nodeAttribute = null;

        try {
        File file = new File(fileName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList nodeLst = doc.getElementsByTagName(location);
        Node fstNode = nodeLst.item(0);

       Element fstElmnt = (Element) fstNode;

       nodeAttribute = fstElmnt.getAttribute(attribute);


        }
        catch (Exception e)
        {
         e.printStackTrace();
        }
        return nodeAttribute;

    }

@Test
public void testGetandSetOperationsGetCaps(){
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();
    MockGetCapP.parseOperationsMetaData();
    

    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    String location = "ows:Get";
    String attribute = "xlink:href";

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
    String a =  loadCreatedTempXMLFileAndCheckAttribute(fileName,location,attribute);

    assertEquals(MockGetCapP.getHTTPGetAddress(),a);


}

    @Test
    public void testGetAndSetOperationsPostCaps() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();
    MockGetCapP.parseOperationsMetaData();


    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    String location = "ows:Post";
    String attribute = "xlink:href";

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
    String a =  loadCreatedTempXMLFileAndCheckAttribute(fileName,location,attribute);

    assertEquals(MockGetCapP.getHTTPGetAddress(),a);
    assertEquals("Location", MockGetCapP.getHTTPGetAddress());
    }

    @Test
    public void testGetAndSetOperationsGetObs() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();
    MockGetCapP.parseOperationsMetaData();


    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    String location = "ows:Get";
    String attribute = "xlink:href";
    String NodeName = "GetObservation";
    
    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
    String a =  loadCreatedTempXMLFileAndSpecifNodeAttribute(fileName,location,attribute,NodeName);

    assertEquals(MockGetCapP.getHTTPGetAddress(),a);
    assertEquals("Location", MockGetCapP.getHTTPGetAddress());


    }

    @Test
    public void testGetAndSetOperationsPostObs() {
          MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();
    MockGetCapP.parseOperationsMetaData();


    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    String location = "ows:Post";
    String attribute = "xlink:href";
    String NodeName = "GetObservation";

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
    String a =  loadCreatedTempXMLFileAndSpecifNodeAttribute(fileName,location,attribute,NodeName);

    assertEquals(MockGetCapP.getHTTPGetAddress(),a);
    assertEquals("Location", MockGetCapP.getHTTPGetAddress());
  
    }

    @Test
    public void testGetAndSetOperationsGetSensor() {
          MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();
    MockGetCapP.parseOperationsMetaData();


   String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    String location = "ows:Get";
    String attribute = "xlink:href";
    String NodeName = "DescribeSensor";

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
    String a =  loadCreatedTempXMLFileAndSpecifNodeAttribute(fileName,location,attribute,NodeName);

    assertEquals(MockGetCapP.getHTTPGetAddress(),a);
    assertEquals("Location", MockGetCapP.getHTTPGetAddress());

    }

    @Test
    public void testGetAndSetOperationsPostSensor() {
          MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();
    MockGetCapP.parseOperationsMetaData();


    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    String location = "ows:Post";
    String attribute = "xlink:href";
    String NodeName = "DescribeSensor";

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);
    String a =  loadCreatedTempXMLFileAndSpecifNodeAttribute(fileName,location,attribute,NodeName);

    assertEquals(MockGetCapP.getHTTPGetAddress(),a);
    assertEquals("Location", MockGetCapP.getHTTPGetAddress());

    }


@Test
public void testGetandSetObservationOfferings(){
 MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    assertTrue(offering!=null);
    assertEquals(offering.getObservationName(), "urn:ioos:station:NOAA.NOS.CO-OPS:aa0101");

    }

@Test
public void testAddOfferingInformationToDocFileNotNull(){
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    MockGetCapP.addObsOfferingToDoc(offering);

    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testFileNewNode.xml";
    fileName = fileName.replaceAll("%20", " ");

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);

    File xmlFile = new File(fileName);
    assertTrue(xmlFile!=null);
}

@Test
public void testConstructObsOfferingNodesDescription(){
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    NodeList lstNmElmntLst = el.getElementsByTagName("gml:description");
    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
    NodeList lstNm = lstNmElmnt.getChildNodes();
    String nodeVal = lstNm.item(0).getNodeValue();

    assertEquals(nodeVal,"OSTEP1");
}

@Test
public void testConstructObsOfferingNodesSrSName(){
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    NodeList lstNmElmntLst = el.getElementsByTagName("gml:srsName");
    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
    NodeList lstNm = lstNmElmnt.getChildNodes();
    String nodeVal = lstNm.item(0).getNodeValue();

    assertEquals(nodeVal,"urn:ogc:def:crs:epsg::4326");
}




@Test
public void testConstructObsOfferingNodesName(){
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    NodeList lstNmElmntLst = el.getElementsByTagName("gml:name");
    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
    NodeList lstNm = lstNmElmnt.getChildNodes();
    String nodeVal = lstNm.item(0).getNodeValue();

    assertEquals(nodeVal,"urn:ioos:station:NOAA.NOS.CO-OPS:aa0101");
}

    @Test
    public void testCanGetBeginTime() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    NodeList lstNmElmntLst = el.getElementsByTagName("gml:beginPosition");
    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
    NodeList lstNm = lstNmElmnt.getChildNodes();
    String nodeVal = lstNm.item(0).getNodeValue();

    assertEquals(nodeVal,"2009-03-24T19:18:00Z");
    }

    @Test
    public void testCanGetCorrectEndPositionAttribute() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    NodeList lstNmElmntLst = el.getElementsByTagName("gml:endPosition");
    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
    String attribVal = lstNmElmnt.getAttribute("indeterminatePosition");

    assertEquals("unknown",attribVal);



    }

    @Test
    public void testCanGetStationID() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();
    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    String attribVal = el.getAttribute("gml:id");

    assertEquals(attribVal,"station-aa0101");
    }



    @Test
    public void testCanGetLowerCornerValue() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    NodeList lstNmElmntLst = el.getElementsByTagName("gml:lowerCorner");
    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
    NodeList lstNm = lstNmElmnt.getChildNodes();
    String nodeVal = lstNm.item(0).getNodeValue();

    assertEquals(nodeVal,offering.getObservationStationLowerCorner());
    }

    @Test
    public void testCanGetUpperCornerValue() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    NodeList lstNmElmntLst = el.getElementsByTagName("gml:upperCorner");
    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
    NodeList lstNm = lstNmElmnt.getChildNodes();
    String nodeVal = lstNm.item(0).getNodeValue();

    assertEquals(nodeVal,offering.getObservationStationUpperCorner());
    }

    @Test
    public void testCanGetEnvelopeSrsName() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();

    MockGetCapP.parseMockObservationOffering();

    ObservationOffering offering = MockGetCapP.getMockObservationOffering();

    Element el = MockGetCapP.constructObsOfferingNodes(offering);

    NodeList lstNmElmntLst = el.getElementsByTagName("gml:Envelope");
    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
    String attribVal = lstNmElmnt.getAttribute("srsName");

    assertEquals(attribVal,"urn:ogc:def:crs:epsg::4326");

    }

    @Test
    public void testCanGetTimeNode() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();
    MockGetCapP.parseMockObservationOffering();
    ObservationOffering offering = MockGetCapP.getMockObservationOffering();
    Element el = MockGetCapP.constructObsOfferingNodes(offering);
    NodeList lstNmElmntLst = el.getElementsByTagName("time");
    assertTrue(lstNmElmntLst!=null);
    }

     @Test
    public void testCanGetTimePeriodNode() {
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser();
    MockGetCapP.parseTemplateXML();
    MockGetCapP.parseMockObservationOffering();
    ObservationOffering offering = MockGetCapP.getMockObservationOffering();
    Element el = MockGetCapP.constructObsOfferingNodes(offering);
    NodeList lstNmElmntLst = el.getElementsByTagName("gml:TimePeriod");
    assertTrue(lstNmElmntLst!=null);
    }



@Test
public void testWriteAllDataToTextXMLFileCheckNotNull() throws Exception{
      String location = getClass().getClassLoader().getResource("datasets/VIMS.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
       DatasetMetaData dst = new DatasetMetaData(ext, dataset);
       dst.extractData();
       MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
       MockGetCapP.parseTemplateXML();


    MockGetCapP.parseServiceIdentification();
    MockGetCapP.parseServiceDescription();
    MockGetCapP.parseOperationsMetaData();

     String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testfile.xml";
    fileName = fileName.replaceAll("%20", " ");
    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);

    File xmlFile = new File(fileName);

    assertTrue(xmlFile!=null);
}

@Test
public void testWriteAllDataToTextXMLFileCheckObsOffering() throws Exception{
      String location = getClass().getClassLoader().getResource("datasets/VIMS.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
     MockGetCapP.parseTemplateXML();


    MockGetCapP.parseServiceIdentification();
    MockGetCapP.parseServiceDescription();
    MockGetCapP.parseOperationsMetaData();
    MockGetCapP.parseObservationList();

     String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "testFileObs.xml";
    fileName = fileName.replaceAll("%20", " ");
    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);

    File xmlFile = new File(fileName);

    assertTrue(xmlFile!=null);
}


    @Test
    public void testGetNetCDFTestFileExists01() throws Exception {
      String location = getClass().getClassLoader().getResource("datasets/Station_42080_2008met.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);

     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
     MockGetCapP.parseTemplateXML();


    MockGetCapP.parseServiceIdentification();
    MockGetCapP.parseServiceDescription();
    MockGetCapP.parseOperationsMetaData();
    MockGetCapP.parseObservationList();

     String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "Station_42080_2008met.xml";
    fileName = fileName.replaceAll("%20", " ");
    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);

    File xmlFile = new File(fileName);

    assertTrue(xmlFile!=null);
    }

    @Test
    public void testgetNetCdfTestFileExists02() throws Exception {
       String location = getClass().getClassLoader().getResource("datasets/Station_42059_2008met.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);

     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
     MockGetCapP.parseTemplateXML();


    MockGetCapP.parseServiceIdentification();
    MockGetCapP.parseServiceDescription();
    MockGetCapP.parseOperationsMetaData();
    MockGetCapP.parseObservationList();

    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "Station_42059_2008met.xml";
    fileName = fileName.replaceAll("%20", " ");

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);

    File xmlFile = new File(fileName);

    assertTrue(xmlFile!=null);
    }

    @Test
    public void testgetNetCdfTestFileExists03() throws Exception {
     String location = getClass().getClassLoader().getResource("datasets/epa+seamap_04-08_A04.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);

    Extent ext = ThreddsExtentUtil.getExtent(dataset);
    DatasetMetaData dst = new DatasetMetaData(ext, dataset);
    dst.extractData();
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
    MockGetCapP.parseTemplateXML();


    MockGetCapP.parseServiceIdentification();
    MockGetCapP.parseServiceDescription();
    MockGetCapP.parseOperationsMetaData();
    MockGetCapP.parseObservationList();

    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "epa_seamap_04-08_A04.xml";
    fileName = fileName.replaceAll("%20", " ");

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);

    File xmlFile = new File(fileName);

    assertTrue(xmlFile!=null);
    }

    @Test
    public void testEPAgetNetCdfTestValuesNotNull03() throws Exception {
    String location = getClass().getClassLoader().getResource("datasets/epa+seamap_04-08_A04.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
    DatasetMetaData dst = new DatasetMetaData(ext, dataset);
    dst.extractData();
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
    MockGetCapP.parseTemplateXML();


    MockGetCapP.parseServiceIdentification();
    MockGetCapP.parseServiceDescription();
    MockGetCapP.parseOperationsMetaData();
    MockGetCapP.parseObservationList();

    assertTrue(MockGetCapP.getDom()!=null);
    assertTrue(MockGetCapP.getTitleSI()!=null);
    assertTrue(MockGetCapP.getProviderNameSP() !=null);
    assertTrue(MockGetCapP.getProviderSiteSP() !=null);

    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "epa_seamap_04-08_A04.xml";
    fileName = fileName.replaceAll("%20", " ");

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);

    File xmlFile = new File(fileName);

    assertTrue(xmlFile!=null);
    }

    //return an attribute string based on input params
    private String loadCreatedTempXMLFileAndSpecifNodeAttribute(String fileName, String location, String attribute, String NodeName) {
       String nodeAttribute = null;

        try {
        File file = new File(fileName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList nodeLst = doc.getElementsByTagName("ows:Operation");



        for (int jj = 0; jj < nodeLst.getLength(); jj++) {

            Element fstNmElmnt = (Element) nodeLst.item(jj);
            //String c = fstNmElmnt.getAttribute("name");
            //System.out.println("Name: "  + fstNmElmnt.getAttribute("name"));

            if  (fstNmElmnt.getAttribute("name").contentEquals(NodeName)){
            NodeList fstNm1 = fstNmElmnt.getElementsByTagName(location);
            Element fstNmElmnt1 = (Element) fstNm1.item(0);
            nodeAttribute = fstNmElmnt1.getAttribute(attribute);
            }

        }

        return nodeAttribute;


        }
        catch (Exception e)
        {
         e.printStackTrace();
        }
        return nodeAttribute;
    }



    @Test
    public void testEPAgetNetCdfTestValuesCreated() throws Exception {
     String location = getClass().getClassLoader().getResource("datasets/epa+seamap_04-08_TD26.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);
        
    Extent ext = ThreddsExtentUtil.getExtent(dataset);
    DatasetMetaData dst = new DatasetMetaData(ext, dataset);
    dst.extractData();
    MockGetCapabilitiesParser MockGetCapP= new MockGetCapabilitiesParser(dst);
    MockGetCapP.parseTemplateXML();


    MockGetCapP.parseServiceIdentification();
    MockGetCapP.parseServiceDescription();
    MockGetCapP.parseOperationsMetaData();
    MockGetCapP.parseObservationList();

    assertTrue(MockGetCapP.getDom()!=null);
    assertTrue(MockGetCapP.getTitleSI()!=null);
    assertTrue(MockGetCapP.getProviderNameSP() !=null);
    assertTrue(MockGetCapP.getProviderSiteSP() !=null);

    String fileName = getClass().getClassLoader().getResource("XML/").getPath();
    fileName = fileName + "epa_seamap_04-08_TD26.xml";
    fileName = fileName.replaceAll("%20", " ");

    MockGetCapP.writeXMLDOMToFile(MockGetCapP.getDom(),fileName);

    File xmlFile = new File(fileName);

    assertTrue(xmlFile!=null);
    }

    @Test
    public void testKyleNewDataSet() throws Exception {
     String location = getClass().getClassLoader().getResource("datasets/WT6.1.nc").getPath();
     location = location.replaceAll("%20", " ");
     NetcdfDataset dataset = NetcdfDataset.openDataset(location);

    Extent ext = ThreddsExtentUtil.getExtent(dataset);
    DatasetMetaData dst = new DatasetMetaData(ext, dataset);
    dst.extractData();
    
    assertEquals("WT6.1", dst.getTitle());

    }
  
}