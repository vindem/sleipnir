package at.ac.tuwien.ec.model.infrastructure.network;

import java.io.Serializable;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;

public class ConnectionMap extends DefaultDirectedWeightedGraph<NetworkedNode, NetworkConnection> implements Serializable{

	
	final int maxHops = FGCSSetup.cloudMaxHops;
	//int cloudHops = SimulationSetup.cloudMaxHops;
	NormalDistribution nDistr = new NormalDistribution(SimulationSetup.MAP_M, 0.5);
	final double MILLISECONDS_PER_SECONDS = 1000.0;
	final double BYTES_PER_MEGABIT = 125000.0;
	//final double BYTES_PER_MEGABIT = 1e6;
	
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
	
	public void setCostlessWeights(MobileDataDistributionInfrastructure I)
	{
		double minLatency = Double.MAX_VALUE,minCost = Double.MAX_VALUE;
		for(NetworkConnection nwConn : edgeSet())
		{
			if(nwConn.getLatency() * computeDistance(nwConn.getSource(), nwConn.getTarget())
					< minLatency)
				minLatency = nwConn.getLatency() * computeDistance(nwConn.getSource(), nwConn.getTarget());
		}
		
		for(NetworkedNode node : vertexSet())
		{
			double tmp = computeCost(node, I);
			if(tmp < minCost)
				minCost = tmp;
		}
		
		for(NetworkConnection nwConn : edgeSet())
		{
			double mips0,mips1;
			mips0 = nwConn.getSource().getCapabilities().getMipsPerCore();
			mips1 = nwConn.getTarget().getCapabilities().getMipsPerCore();
			mips0 = (mips0 == 0)? Double.MAX_VALUE : mips0;
			mips1 = (mips1 == 0)? Double.MAX_VALUE : mips1;		
			setEdgeWeight(nwConn, 
					(nwConn.getLatency() * computeDistance(nwConn.getSource(), nwConn.getTarget())) / minLatency 
					+ computeCost(((nwConn.getSource() instanceof ComputationalNode)? nwConn.getSource() : nwConn.getTarget()) ,I)
							* computeDistance(nwConn.getSource(), nwConn.getTarget()));
		}
	}
	
	public double getTransmissionTime(MobileSoftwareComponent msc, NetworkedNode u, NetworkedNode v) throws IllegalArgumentException
	{
		if(u == null)
			throw new IllegalArgumentException("First argument null");
		if(v == null)
			throw new IllegalArgumentException("Second argument null");
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
	
	public double getDataTransmissionTime(double dataSize, NetworkedNode u, NetworkedNode v) throws IllegalArgumentException
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
		
		return (((dataSize)/(profile.getBandwidth()*BYTES_PER_MEGABIT) + 
				((profile.getLatency()*computeDistance(u,v))/MILLISECONDS_PER_SECONDS)) );

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
		//return (((msc.getInData() + msc.getOutData())/(profile.getBandwidth()*BYTES_PER_MEGABIT) + 
			//	((profile.getLatency()*computeDistance(u,v))/MILLISECONDS_PER_SECONDS)) ); //*SimulationConstants.offloadable_part_repetitions;
		//System.out.println(u.getId() + "," + v.getId() + " : " + profile.getLatency() + " , " + profile.getBandwidth() );
		return ((msc.getInData())/(profile.getBandwidth()*BYTES_PER_MEGABIT)) + 
				(profile.getLatency()*computeDistance(u,v))/MILLISECONDS_PER_SECONDS;
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
		//return 1.0;
		Coordinates c1,c2;
		if(u.equals(v))
			return 0.0;
		if( u instanceof CloudDataCenter || v instanceof CloudDataCenter )
			return 10.0;
		//mapping coordinates to cells
		double size_x = SimulationSetup.x_max/SimulationSetup.MAP_M;;
		double size_y = SimulationSetup.y_max/(SimulationSetup.MAP_N*2);
		
		c1 = u.getCoords();
		c2 = v.getCoords();
		
		int u_i = (int) ((2*c1.getLatitude() - size_x)/size_x);
		int u_j = (int) ((2*c1.getLongitude() - size_y)/size_y);
		int v_i = (int) ((2*c2.getLatitude() - size_x)/size_x);
		int v_j = (int) ((2*c2.getLongitude() - size_x)/size_x);
		//double dist1 = (Math.abs(Math.round(c1.getLatitude()/size_x)-Math.round(c2.getLatitude()/size_x))
			//					- Math.abs(Math.round(c1.getLongitude()/size_y)-Math.round(c2.getLongitude()/size_y)) )/2;
		double dist2 = Math.abs(u_i - v_i) + Math.max(0,(Math.abs(u_i-v_i)- Math.abs(u_j-v_j) )/2);
		return dist2;
	}
	
	private double computeCost(NetworkedNode n, MobileDataDistributionInfrastructure I)
	{
		MobileSoftwareComponent msc = 
				new MobileSoftwareComponent("test0",
						new Hardware(1, 1, 1),
						1, "user0", 1.0, 1.0);
		if(n instanceof ComputationalNode)
			return ((ComputationalNode) n).computeCost(msc, I);
		return Double.MAX_VALUE;
	}
	
	private static final long serialVersionUID = 1L;

}
