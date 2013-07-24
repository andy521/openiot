/*******************************************************************************
 * Copyright (c) 2011-2014, OpenIoT
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it either under the terms of the GNU Lesser General Public
 *  License version 2.1 as published by the Free Software Foundation
 *  (the "LGPL"). If you do not alter this
 *  notice, a recipient may use your version of this file under the LGPL.
 *  
 *  You should have received a copy of the LGPL along with this library
 *  in the file COPYING-LGPL-2.1; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 *  This software is distributed on an "AS IS" basis, WITHOUT WARRANTY
 *  OF ANY KIND, either express or implied. See the LGPL  for
 *  the specific language governing rights and limitations.
 *  
 *  Contact: OpenIoT mailto: info@openiot.eu
 ******************************************************************************/
package org.openiot.ui.request.definition.web.model.nodes.impl.sinks;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.openiot.ui.request.commons.annotations.Endpoint;
import org.openiot.ui.request.commons.annotations.Endpoints;
import org.openiot.ui.request.commons.annotations.GraphNodeClass;
import org.openiot.ui.request.commons.annotations.NodeProperties;
import org.openiot.ui.request.commons.annotations.NodeProperty;
import org.openiot.ui.request.commons.models.ObservableMap;
import org.openiot.ui.request.commons.nodes.base.DefaultGraphNode;
import org.openiot.ui.request.commons.nodes.base.DefaultGraphNodeEndpoint;
import org.openiot.ui.request.commons.nodes.base.DefaultGraphNodeProperty;
import org.openiot.ui.request.commons.nodes.enums.AnchorType;
import org.openiot.ui.request.commons.nodes.enums.ConnectorType;
import org.openiot.ui.request.commons.nodes.enums.EndpointType;
import org.openiot.ui.request.commons.nodes.enums.PropertyType;
import org.openiot.ui.request.commons.nodes.interfaces.GraphNodeEndpoint;
import org.openiot.ui.request.commons.nodes.interfaces.GraphNodeProperty;

/**
 * 
 * @author Achilleas Anagnostopoulos (aanag) email: aanag@sensap.eu
 */
@GraphNodeClass(label = "LineChart", type = "SINK", scanProperties = true)
@Endpoints({ @Endpoint(type = EndpointType.Input, anchorType = AnchorType.Left, scope = "avg_Number avg_Integer avg_Long, avg_Float avg_Double", label = "x", required = true), })
@NodeProperties({ 
	@NodeProperty(type = PropertyType.Writable, javaType = java.lang.String.class, name = "TITLE", required = true), 
	@NodeProperty(type = PropertyType.Writable, javaType = java.lang.String.class, name = "SERIES", required = true, allowedValues = { "1", "2", "3", "4", "5" }), 
	@NodeProperty(type = PropertyType.Writable, javaType = java.lang.String.class, name = "X_AXIS_TYPE", required = true, allowedValues = { "Number", "Date (result set)", "Date (observation)" }), 
	@NodeProperty(type = PropertyType.Writable, javaType = java.lang.String.class, name = "X_AXIS_LABEL", required = true), 
	@NodeProperty(type = PropertyType.Writable, javaType = java.lang.String.class, name = "Y_AXIS_LABEL", required = true) 
	})
public class LineChart extends DefaultGraphNode implements Serializable, Observer {
	private static final long serialVersionUID = 1L;

	public LineChart() {
		super();

		// Setup some defaults
		setProperty("TITLE", LineChart.class.getSimpleName());
		setProperty("SERIES", "1");
		setProperty("X_AXIS_TYPE", "Number");
		setProperty("X_AXIS_LABEL", "x axis");
		setProperty("Y_AXIS_LABEL", "y axis");

		addPropertyChangeObserver(this);
		validateSeries();
	}

	public void validateSeries() {
		int seriesCount = Integer.valueOf((String) getPropertyValueMap().get("SERIES"));
		int i = 0;
		for (; i < seriesCount; i++) {
			// If we are missing the required endpoints and properties create
			// them now
			String epLabel = "y" + (i + 1);
			GraphNodeEndpoint ep = getEndpointByLabel(epLabel);
			if (ep == null) {
				ep = new DefaultGraphNodeEndpoint();
				ep.setType(EndpointType.Input);
				ep.setAnchor(AnchorType.Left);
				ep.setConnectorType(ConnectorType.Rectangle);
				ep.setScope("agr_Number agr_Integer agr_Long, agr_Float agr_Double");
				ep.setLabel(epLabel);
				ep.setRequired(true);
				getEndpointDefinitions().add(ep);

				GraphNodeProperty prop = new DefaultGraphNodeProperty();
				String propKey = "SERIES_" + i + "_LABEL";
				prop.setType(PropertyType.Writable);
				prop.setName(propKey);
				prop.setJavaType(java.lang.String.class);
				prop.setRequired(true);
				getPropertyDefinitions().add(prop);

				((ObservableMap<String, Object>) getPropertyValueMap()).getWrappedMap().put(propKey, "Series " + (i + 1));
			}
		}

		// If we reduced the number of series, get rid of the old series
		int maxSeries = Integer.valueOf((String) getPropertyByName("SERIES").getAllowedValues()[getPropertyByName("SERIES").getAllowedValues().length - 1]);
		for (; i < maxSeries; i++) {
			String epLabel = "y" + (i + 1);
			GraphNodeEndpoint ep = getEndpointByLabel(epLabel);
			if (ep != null) {
				// If we have a connection to this node, kill it
				disconnectEndpoint(ep);
				getEndpointDefinitions().remove(ep);

				String propKey = "SERIES_" + i + "_LABEL";
				GraphNodeProperty prop = getPropertyByName(propKey);
				if (prop != null) {
					getPropertyDefinitions().remove(prop);
					((ObservableMap<String, Object>) getPropertyValueMap()).getWrappedMap().remove(propKey);
				}
			}
		}
	}
	
	
	private void removeAllXAxisEndpoints(){
		Iterator<GraphNodeEndpoint> endpointIt = this.getEndpointDefinitions().iterator();
		while(endpointIt.hasNext()){
			GraphNodeEndpoint endpoint = endpointIt.next();
			if(endpoint.getLabel().startsWith("X_AXIS_OBS_") || endpoint.getLabel().equals("x")){
				endpointIt.remove();
				disconnectEndpoint(endpoint);				
			}
		}
	}

	private void addNumberModeXAxisEndpoint(){
		GraphNodeEndpoint ep = new DefaultGraphNodeEndpoint();
		ep.setType(EndpointType.Input);
		ep.setAnchor(AnchorType.Left);
		ep.setConnectorType(ConnectorType.Rectangle);
		ep.setScope("agr_Number agr_Integer agr_Long, agr_Float agr_Double");
		ep.setLabel("x");
		ep.setRequired(true);
		getEndpointDefinitions().add(0, ep);
	}
	
	private void addDateModeEndpoints(){
		for( String propName : new String[]{"SEC", "MIN", "HOUR", "DAY", "MONTH", "YEAR"}){
			GraphNodeEndpoint ep = new DefaultGraphNodeEndpoint();
			ep.setType(EndpointType.Input);
			ep.setAnchor(AnchorType.Left);
			ep.setConnectorType(ConnectorType.Rectangle);
			ep.setScope("grp_Number grp_Integer grp_Long, grp_Float grp_Double");
			ep.setLabel("X_AXIS_OBS_" + propName);
			ep.setRequired(false);
			getEndpointDefinitions().add(0, ep);
		}
	}
	
	public void update(Observable o, Object modifiedKey) {
		Map<String, Object> propertyMap = getPropertyValueMap();
		
		// Check for X_AXIS_TYPE modifications		
		if ((modifiedKey != null) && ("X_AXIS_TYPE".equals((String)modifiedKey)) && (propertyMap.get("X_AXIS_TYPE") != null)) {
			String newXAxisType = (String) propertyMap.get("X_AXIS_TYPE");
			removeAllXAxisEndpoints();
			
			if ("Number".equals(newXAxisType)){
				addNumberModeXAxisEndpoint();
			} else if ("Date (observation)".equals(newXAxisType)) {
				addDateModeEndpoints();
			}						
		}

		validateSeries();
	}

}
