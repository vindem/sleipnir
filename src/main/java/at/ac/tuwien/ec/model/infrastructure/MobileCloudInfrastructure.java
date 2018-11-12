/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ec.model.infrastructure;



import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.*;
import at.ac.tuwien.ec.model.infrastructure.costs.ElectricityPriceTrace;
import at.ac.tuwien.ec.model.infrastructure.costs.PriceMap;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
/**
 *
 * @author Vincenzo
 */
public class MobileCloudInfrastructure {
    
	private ArrayList<MobileDevice> mobileDevices;
	private ArrayList<EdgeNode> edgeNodes;
	private ArrayList<CloudDataCenter> cloudNodes;
	private ConnectionMap connectionMap;
	private PriceMap priceMap;
	
	public MobileCloudInfrastructure()
	{
		mobileDevices = new ArrayList<MobileDevice>();
		edgeNodes = new ArrayList<EdgeNode>();
		cloudNodes = new ArrayList<CloudDataCenter>();
		connectionMap = new ConnectionMap(NetworkConnection.class);
	}
	
	public void addMobileDevice(MobileDevice device)
	{
		mobileDevices.add(device);
		connectionMap.addVertex(device);
	}
	
	public void addEdgeNode(EdgeNode edge)
	{
		edgeNodes.add(edge);
		connectionMap.addVertex(edge);
	}
	
	public void addCloudDataCenter(CloudDataCenter cloudDC)
	{
		cloudNodes.add(cloudDC);
		connectionMap.addVertex(cloudDC);
	}

	public void addLink(ComputationalNode u, ComputationalNode v, QoSProfile profile) 
	{
		connectionMap.addEdge(u, v, profile);
	}
	
	public void addLink(ComputationalNode u, ComputationalNode v, double latency, double bandwidth)
	{
		connectionMap.addEdge(u, v, new QoSProfile(latency,bandwidth));
	}
	
	public void sampleInfrastructure() {
		for(NetworkConnection n: connectionMap.edgeSet())
			n.sampleLink();
		for(CloudDataCenter cdc: cloudNodes)
			cdc.sampleNode();
		for(EdgeNode en: edgeNodes)
			en.sampleNode();
		for(MobileDevice m: mobileDevices)
			m.sampleNode();
	}
	
	public ArrayList<MobileDevice> getMobileDevices() {
		return mobileDevices;
	}

	public void setMobileDevices(ArrayList<MobileDevice> mobileDevices) {
		this.mobileDevices = mobileDevices;
	}

	public ArrayList<EdgeNode> getEdgeNodes() {
		return edgeNodes;
	}

	public void setEdgeNodes(ArrayList<EdgeNode> edgeNodes) {
		this.edgeNodes = edgeNodes;
	}

	public ArrayList<CloudDataCenter> getCloudNodes() {
		return cloudNodes;
	}

	public void setCloudNodes(ArrayList<CloudDataCenter> cloudNodes) {
		this.cloudNodes = cloudNodes;
	}

	public String toString(){
		String tmp = "";
		tmp += "CLOUD NODES:\n";
		tmp += cloudNodes + " ;\n";
		tmp += "EDGE NODES:\n";		
		tmp += edgeNodes + " ;\n";
		tmp += "MOBILE DEVICES:\n";
		tmp += mobileDevices + ".\n";
		return tmp;
	}
	
	public void addPrices(Coordinates coordinates, ElectricityPriceTrace trace) {
		priceMap.put(coordinates, trace);
	}

		
}
