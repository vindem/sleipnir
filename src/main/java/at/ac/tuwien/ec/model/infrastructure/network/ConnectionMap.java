package at.ac.tuwien.ec.model.infrastructure.network;

import java.io.Serializable;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class ConnectionMap extends DefaultUndirectedWeightedGraph<NetworkedNode, NetworkConnection> implements Serializable{
	
	//int cloudHops = SimulationSetup.cloudMaxHops;
	NormalDistribution nDistr = new NormalDistribution(3.0, 0.5);
	final double MILLISECONDS_PER_SECONDS = 1000.0;
	final double BYTES_PER_MEGABIT = 125000.0;
	
	public ConnectionMap(Class<? extends NetworkConnection> edgeClass) {
		super(edgeClass);
		// TODO Auto-generated constructor stub
	}
	
	public void addNode(NetworkedNode node){
		addVertex(node);
	}
	
	public void addEdge(NetworkedNode u,NetworkedNode v,QoSProfile p)
	{
		NetworkConnection conn = new NetworkConnection(p);
		conn.setSource(u);
		conn.setTarget(v);
		addEdge(u,v,conn);
	}
	
	public void setEdgeWeights()
	{
		double tmp;
		for(NetworkConnection nwConn : edgeSet())
		{
			double mips0,mips1;
			mips0 = nwConn.getSource().getCapabilities().getMipsPerCore();
			mips1 = nwConn.getTarget().getCapabilities().getMipsPerCore();
			mips0 = (mips0 == 0)? Double.MAX_VALUE : mips0;
			mips1 = (mips1 == 0)? Double.MAX_VALUE : mips1;		
			setEdgeWeight(nwConn, 1.0/nwConn.getBandwidth() + 
					(nwConn.getLatency() * computeDistance(nwConn.getSource(), nwConn.getTarget())) + 
					1.0/Math.min(mips0,mips1));
		}
	}
	
	public double getTransmissionTime(MobileSoftwareComponent msc, NetworkedNode u, NetworkedNode v) throws IllegalArgumentException
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
		
		return getDesiredTransmissionTime(msc,u,v,profile);

	}
	
	public double getInDataTransmissionTime(MobileSoftwareComponent msc, NetworkedNode u, NetworkedNode v) throws IllegalArgumentException
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
		
		return getInDataTransmissionTime(msc,u,v,profile);

	}
	
	public double getOutDataTransmissionTime(MobileSoftwareComponent msc, NetworkedNode u, NetworkedNode v) throws IllegalArgumentException
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
		
		return getOutDataTransmissionTime(msc,u,v,profile);

	}
	
	public double getDesiredTransmissionTime(MobileSoftwareComponent cmp, NetworkedNode u, NetworkedNode v, ComponentLink link)
	{
		QoSProfile profile = link.getDesiredQoS();
		return getDesiredTransmissionTime(cmp, u,v, profile);
	}
	
	public double getDesiredTransmissionTime(MobileSoftwareComponent msc, NetworkedNode u, NetworkedNode v, QoSProfile profile)
	{
		return (((msc.getInData() + msc.getOutData())/(profile.getBandwidth()*BYTES_PER_MEGABIT) + 
				((profile.getLatency()*computeDistance(u,v))/MILLISECONDS_PER_SECONDS)) ); //*SimulationConstants.offloadable_part_repetitions;
	}
	
	public double getInDataTransmissionTime(MobileSoftwareComponent msc, NetworkedNode u, NetworkedNode v, QoSProfile profile){
		return (((msc.getInData())/(profile.getBandwidth()*BYTES_PER_MEGABIT) + 
				((profile.getLatency()*computeDistance(u,v))/MILLISECONDS_PER_SECONDS)) ); //*SimulationConstants.offloadable_part_repetitions;
	}
	
	public double getOutDataTransmissionTime(MobileSoftwareComponent msc, NetworkedNode u, NetworkedNode v, QoSProfile profile)
	{
		return ((msc.getOutData())/(profile.getBandwidth()*BYTES_PER_MEGABIT) + 
				((profile.getLatency()*computeDistance(u,v))/MILLISECONDS_PER_SECONDS)); //*SimulationConstants.offloadable_part_repetitions;
	}
	
	public double computeDistance(NetworkedNode u, NetworkedNode v)
	{
		Coordinates c1,c2;
		if(u.equals(v))
			return 0.0;
		if( u instanceof CloudDataCenter || v instanceof CloudDataCenter )
			return nDistr.sample();
		//mapping coordinates to cells
		double size_x = SimulationSetup.x_max/SimulationSetup.MAP_M;;
		double size_y = SimulationSetup.y_max/(SimulationSetup.MAP_N*2);
		
		c1 = u.getCoords();
		c2 = v.getCoords();
		return (Math.abs((c1.getLatitude()/size_x)-(c2.getLatitude()/size_x)) 
				+ Math.max(0, 
						(Math.abs((c1.getLatitude()/size_x)-(c2.getLatitude()/size_x))
								- Math.abs((c1.getLongitude()/size_y)-(c2.getLongitude()/size_y)) )/2));
	}
	
	private static final long serialVersionUID = 1L;

}
