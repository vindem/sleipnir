package at.ac.tuwien.ec.model.infrastructure.network;

import java.io.Serializable;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;

public class ConnectionMap extends DefaultUndirectedWeightedGraph<ComputationalNode, NetworkConnection> implements Serializable{
	
	final int maxHops = FGCSSetup.cloudMaxHops;
	final double MILLISECONDS_PER_SECONDS = 1000.0;
	final double BYTES_PER_MEGABIT = 125000.0;
	//final double BYTES_PER_MEGABIT = 1e6;
	
	public ConnectionMap(Class<? extends NetworkConnection> edgeClass) {
		super(edgeClass);
		// TODO Auto-generated constructor stub
	}
	
	public void addNode(ComputationalNode node){
		addVertex(node);
	}
	
	public void addEdge(ComputationalNode v,ComputationalNode u,QoSProfile p)
	{
		NetworkConnection conn = new NetworkConnection(p);
		addEdge(u,v,conn);		
	}
	
	
	
	public double getTransmissionTime(MobileSoftwareComponent msc, ComputationalNode u, ComputationalNode v) throws IllegalArgumentException
	{
		if(u.equals(v))
			return 0;
		if(!vertexSet().contains(u))
			throw new IllegalArgumentException("Node " + u.getId() + " does not exists.");
		if(!vertexSet().contains(v))
			throw new IllegalArgumentException("Node " + v.getId() + " does not exists.");
		NetworkConnection link = getEdge(u,v);
		if(link == null)
			throw new IllegalArgumentException("No connection between " + u.getId() + " and " + v.getId() + ".");
		QoSProfile profile = getEdge(u,v).qosProfile;
		if(profile == null)
			return Double.MAX_VALUE;
		
		if(profile.getLatency()==Integer.MAX_VALUE)
			return Double.MAX_VALUE;
		//System.out.println("("+u.getId()+","+v.getId()+")"+" latency: " + profile.getLatency() + " bandwidth: " + profile.getBandwidth() );
		return getDesiredTransmissionTime(msc,u,v,profile);

	}
	
	public double getDesiredTransmissionTime(MobileSoftwareComponent cmp, ComputationalNode u, ComputationalNode v, ComponentLink link)
	{
		QoSProfile profile = link.getDesiredQoS();
		return getDesiredTransmissionTime(cmp, u,v, profile);
	}
	
	public double getDesiredTransmissionTime(MobileSoftwareComponent msc, ComputationalNode u, ComputationalNode v, QoSProfile profile)
	{
		//return (((msc.getInData() + msc.getOutData())/(profile.getBandwidth()*BYTES_PER_MEGABIT) + 
			//	((profile.getLatency()*computeDistance(u,v))/MILLISECONDS_PER_SECONDS)) ); //*SimulationConstants.offloadable_part_repetitions;
		return ((msc.getInData())/(profile.getBandwidth()*BYTES_PER_MEGABIT)) + 
				(profile.getLatency()*computeDistance(u,v))/MILLISECONDS_PER_SECONDS;
	}
	
	private double computeDistance(ComputationalNode u, ComputationalNode v)
	{
		return 1.0;
		/*Coordinates c1,c2;
		if(u.equals(v))
			return 0.0;
		if( u instanceof CloudDataCenter || v instanceof CloudDataCenter )
			return maxHops;
		
		c1 = u.getCoords();
		c2 = u.getCoords();
		return (Math.abs(c1.getLatitude()-c2.getLatitude()) 
				+ Math.max(0, 
						(Math.abs(c1.getLatitude()-c2.getLatitude())
								- Math.abs(c1.getLongitude()-c2.getLongitude()) )/2));*/
	}
	
	private static final long serialVersionUID = 1L;

}
