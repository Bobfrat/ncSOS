package com.asascience.ncsos.outputformatter.ds;

import java.util.List;

import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Element;

public class IoosNetwork10Formatter extends IoosPlatform10Formatter {

    public static final String CF_CONVENTIONS = "http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html#discrete-sampling-geometries";
    
    public IoosNetwork10Formatter() {
        super();
    }

    public void addSmlComponent(String componentName) {
        // add a new component to the ComponentList
        /*
         * <sml:component name='componentName'>
         *   <sml:System>
         *     <sml:identification>
         *       <sml:IdentifierList />
         *     </sml:identification>
         *     <sml:validTime />
         *     <sml:location />
         *     <sml:outputs>
         *       <sml:OutputList />
         *     </sml:outputs>
         *   </sml:System>
         * </sml:component>
         */
        Element parent = XMLDomUtils.getNestedChild(this.getRoot(), COMPONENT_LIST, SML_NS);
        parent = addNewNode(parent, COMPONENT, SML_NS, NAME, componentName);
        parent = addNewNode(parent, SYSTEM, SML_NS);
        addNewNode(addNewNode(parent, IDENTIFICATION, SML_NS),  IDENTIFIER_LIST, SML_NS);
        addNewNode(parent, SML_CAPABILITIES, SML_NS, NAME, "observationTimeRange");
        addNewNode(parent, LOCATION, SML_NS);
        addNewNode(addNewNode(parent, OUTPUTS, SML_NS), OUTPUT_LIST, SML_NS);
    }
    
    public void addIdentifierToComponent(String componentName, String identName, String identDef, String identVal) {
        // add identifier to component
        /*
         * <sml:identifier name='identName'>
         *   <sml:Term definition='definition'>
         *     <sml:value>'identVal'</sml:value>
         *   </sml:Term>
         * </sml:identifier>
         */
        // find our component
        Element parent = getComponent(componentName);
        if (parent == null) {
            return;
        }
        // create an identifier in the component
        parent = (Element) parent.getChild(IDENTIFIER_LIST, SML_NS);
        parent = addNewNode(parent, IDENTIFIER, SML_NS, NAME, identName);
        parent = addNewNode(parent, TERM, SML_NS, DEFINITION, identDef);
        addNewNode(parent, SML_VALUE, SML_NS, identVal);
    }
    
    public void setComponentValidTime(String componentName, String beginPosition, String endPosition) {
        /*
         * <gml:TimePeriod>
         *   <gml:beginPosition>'beginPosition'</gml:beginPosition>
         *   <gml:endPosition>'endPosition'</gml:endPosition>
         * </gml:TimePeriod>
         */
        // find our component
        Element parent = getComponent(componentName);
        if (parent == null)
            return;
        //CDM TO_DO
//        // set valid time
        parent = (Element) parent.getChild(SML_CAPABILITIES, SML_NS);
        setValidTime(parent, beginPosition, endPosition);
        //        parent = addNewNode(parent, TIME_PERIOD, GML_NS);
//        addNewNode(parent, BEGIN_POSITION, GML_NS, beginPosition);
//        addNewNode(parent, END_POSITION, GML_NS, endPosition);
    }
    
    public void setComponentLocation(String componentName, String srs, String pos) {
        /*
         * <gml:Point srsName='srs'>
         *   <gml:pos>'pos'</gml:pos>
         * </gml:Point>
         */
        // find our component
        Element parent = getComponent(componentName);
        if (parent == null)
            return;
        
        // set location
        parent = (Element) parent.getChild(LOCATION, SML_NS);
        parent = addNewNode(parent, POINT, GML_NS, SRS_NAME, srs);
        addNewNode(parent, POS, GML_NS, pos);
    }
    
    public void setComponentLocation(String componentName, String srs, List<String> pos) {
        /*
         * <gml:LineString srsName='srs'>
         *   <gml:pos>'pos0'</gml:pos>
         *   <gml:pos>'pos1'</gml:pos>
         *   ...
         * </gml:LineString>
         */
        // find our component
        Element parent = getComponent(componentName);
        if (parent == null)
            return;
        
        // set location
        parent = (Element) parent.getChild(LOCATION, SML_NS);
        parent = addNewNode(parent, LINE_STRING, GML_NS, SRS_NAME, srs);
        for (String str : pos) {
            addNewNode(parent, POS, GML_NS, str);
        }
    }
    
    public void setComponentLocation(String componentName, String srs, String lowerCorner, String upperCorner) {
        /*
         * <gml:boundedBy srsName='srs'>
         *   <gml:lowerCorner>'lowerCorner'</gml:lowerCorner>
         *   <gml:upperCorner>'upperCorner'</gml:upperCorner>
         * </gml:boundedBy>
         */
        // find the component
        Element parent = getComponent(componentName);
        if (parent == null)
            return;
        
        // set location
        parent = (Element) parent.getChild(LOCATION, SML_NS);
        parent = addNewNode(parent, BOUNDED_BY, GML_NS, SRS_NAME, srs);
        addNewNode(parent, LOWER_CORNER, GML_NS, lowerCorner);
        addNewNode(parent, UPPER_CORNER, GML_NS, upperCorner);
    }
    
    public void addComponentOutput(String componentName, String outName, String outURN, String outDef, String featureType, String units) {
        /*
         * <sml:output name='outName' xlink:title='outURN'>
         *   <sml:Quantity definition='outDef'>
         *     <gml:metaDataProperty>
         *       <gml:name codeSpace='CF_CONVENTIONS'>'featureType'</gml:name>
         *     </gml:metaDataProperty>
         *     <swe:uom code='units' />
         *   </sml:Quantity>
         * </sml:output>
         */
        Element parent = getComponent(componentName);
        if (parent == null) {
            return;
        }
        // add output
        parent = (Element) parent.getChild(OUTPUT_LIST, SML_NS);
        parent = addNewNode(parent, OUTPUT, SML_NS, NAME, outName);
        parent.setAttribute(TITLE, outURN, XLINK_NS);
        parent = addNewNode(parent, QUANTITY, SWE_NS, DEFINITION, outDef);
        addNewNode(addNewNode(parent, META_DATA_PROP, GML_NS), NAME, GML_NS, CODE_SPACE, CF_CONVENTIONS);
        addNewNode(parent, UOM, SWE_NS, CODE, units);
    }
    
    
    private Element getComponent(String componentName) {
        Element parent = this.getRoot().getChild(COMPONENT_LIST, SML_NS);
        List<Element> nl = parent.getChildren(COMPONENT, SML_NS);
        for (Element p : nl) {
            if (p.getAttribute(NAME) == null ? componentName == null : p.getAttribute(NAME).equals(componentName)) {
                parent = p;
                break;
            }
            parent = null;
        }
        return parent;
    }
}
