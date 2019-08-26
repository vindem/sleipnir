/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ec.model.infrastructure;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.*;
import at.ac.tuwien.ec.model.infrastructure.costs.ElectricityPriceTrace;
import at.ac.tuwien.ec.model.infrastructure.costs.PriceMap;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
/**
 *
 * @author Vincenzo
 */
public class MobileCloudInfrastructure implements Serializable{
    
	protected HashMap<String, MobileDevice> mobileDevices;
	protected HashMap<String, EdgeNode> edgeNodes;
	protected HashMap<String, CloudDataCenter> cloudNodes;
	private HashMap<String, EntryPoint> terminals;
	protected ConnectionMap connectionMap;
	protected PriceMap priceMap;

	private static final long serialVersionUID = 1L;
	
	public MobileCloudInfrastructure()
	{
		terminals = new HashMap<String,EntryPoint>();
		mobileDevices = new HashMap<String,MobileDevice>();
		edgeNodes = new HashMap<String,EdgeNode>();
		cloudNodes = new HashMap<String,CloudDataCenter>();
		connectionMap = new ConnectionMap(NetworkConnection.class);
		this.priceMap = new PriceMap();
	}
	
	public void addTerminal(EntryPoint terminal) throws IllegalArgumentException
	{
		terminals.put(terminal.getId(),terminal);
		connectionMap.addVertex(terminal);
	}
	
	public void addMobileDevice(MobileDevice device) throws IllegalArgumentException
	{
		mobileDevices.put(device.getId(),device);
		connectionMap.addVertex(device);
	}
	
	public void addEdgeNode(EdgeNode edge) throws IllegalArgumentException
	{
		edgeNodes.put(edge.getId(),edge);
		connectionMap.addVertex(edge);
	}
	
	public void addCloudDataCenter(CloudDataCenter cloudDC) throws IllegalArgumentException
	{
		cloudNodes.put(cloudDC.getId(),cloudDC);
		connectionMap.addVertex(cloudDC);
	}

	public void addLink(NetworkedNode u, NetworkedNode v, QoSProfile profile) throws IllegalArgumentException
	{
		if(u == null)
			throw new IllegalArgumentException("Node 1 is null");
		if(v == null)
			throw new IllegalArgumentException("Node 2 is null");
		if(profile == null)
			throw new IllegalArgumentException("Profile is null");
		if(!connectionMap.containsVertex(u) || !connectionMap.containsVertex(v))
			throw new IllegalArgumentException((connectionMap.containsVertex(u)? v : u).getId() + ": No such vertex");
		connectionMap.addEdge(u, v, profile);
	}
	
	public void addLink(NetworkedNode u, NetworkedNode v, double latency, double bandwidth) throws IllegalArgumentException
	{
		
		if(latency < 0.0) throw new IllegalArgumentException("Invalid latency: " + latency);
		if(bandwidth < 0.0) throw new IllegalArgumentException("Invalid bandwidth: " + bandwidth);
		addLink(u, v, new QoSProfile(latency,bandwidth));
	}
	
	public void sampleInfrastructure() {
		for(NetworkConnection n: connectionMap.edgeSet())
			n.sampleLink();
		for(CloudDataCenter cdc: cloudNodes.values())
			cdc.sampleNode();
		for(EdgeNode en: edgeNodes.values())
			en.sampleNode();
		for(MobileDevice m: mobileDevices.values())
			m.sampleNode();
	}
	
	public NetworkedNode getNodeById(String id)
	{
		if(mobileDevices.containsKey(id))
			return mobileDevices.get(id);
		if(edgeNodes.containsKey(id))
			return edgeNodes.get(id);
		if(cloudNodes.containsKey(id))
			return cloudNodes.get(id);
		if(terminals.containsKey(id))
			return terminals.get(id);
		return null;
				
	}
	
	public HashMap<String,EntryPoint> getEntryPoints()
	{
		return terminals;
	}
	
	public void setTerminals(HashMap<String,EntryPoint> terminals){
		this.terminals = terminals;
	}
	
	public HashMap<String, MobileDevice> getMobileDevices() {
		return mobileDevices;
	}

	public void setMobileDevices(HashMap<String, MobileDevice> mobileDevices) {
		this.mobileDevices = mobileDevices;
	}

	public HashMap<String, EdgeNode> getEdgeNodes() {
		return edgeNodes;
	}

	public void setEdgeNodes(HashMap<String, EdgeNode> edgeNodes) {
		this.edgeNodes = edgeNodes;
	}

	public HashMap<String, CloudDataCenter> getCloudNodes() {
		return cloudNodes;
	}

	public void setCloudNodes(HashMap<String, CloudDataCenter> cloudNodes) {
		this.cloudNodes = cloudNodes;
	}
	
	public double getTransmissionTime(MobileSoftwareComponent sc, NetworkedNode networkedNode, NetworkedNode n)
	{
		//return 0.0;
		return connectionMap.getTransmissionTime(sc, networkedNode, n);
	}
	
	public double getDesiredTransmissionTime(MobileSoftwareComponent sc, NetworkedNode networkedNode, NetworkedNode n, QoSProfile profile) 
	{
		return connectionMap.getDesiredTransmissionTime(sc, networkedNode, n, profile);
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

	public NetworkConnection getLink(String srcId, String trgId) {
		NetworkedNode src,trg;
		src = getNodeById(srcId);
		trg = getNodeById(trgId);
		if(src == null || trg == null)
			return null;
		return getLink(src, trg);
	}

	public NetworkConnection getLink(NetworkedNode src, NetworkedNode trg) {
		if(!connectionMap.containsVertex(src) || !connectionMap.containsVertex(trg))
			return null;
		return connectionMap.getEdge(src, trg);
	}

	public double getPriceForLocation(Coordinates coords, double runTime) {
		if(priceMap == null)
			return RandomUtils.nextDouble();
		return priceMap.getPriceForTimeAtLocation(coords, runTime);
	}
	
	public ArrayList<ComputationalNode> getAllNodes()
	{
		ArrayList<ComputationalNode> allNodes = new ArrayList<ComputationalNode>();
		//allNodes.addAll(mobileDevices.values());
		allNodes.addAll(edgeNodes.values());
		allNodes.addAll(cloudNodes.values());
		return allNodes;
	}

	public Set<NetworkConnection> getNetworkLinks() {
		// TODO Auto-generated method stub
		return connectionMap.edgeSet();
	}

	public Set<NetworkConnection> getOutgoingLinksFrom(NetworkedNode networkedNode) {
		// TODO Auto-generated method stub
		return connectionMap.outgoingEdgesOf(networkedNode);
	}

	public Set<NetworkConnection> getIncomingLinksTo(ComputationalNode cn) {
		// TODO Auto-generated method stub
		if(!connectionMap.containsVertex(cn))
			System.err.println("cn.getId()");
		return connectionMap.incomingEdgesOf(cn);
	}
}
