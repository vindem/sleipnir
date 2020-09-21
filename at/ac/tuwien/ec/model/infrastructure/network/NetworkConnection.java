package at.ac.tuwien.ec.model.infrastructure.network;

import org.jgrapht.graph.DefaultEdge;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;

public class NetworkConnection extends DefaultEdge {


	QoSProfile qosProfile;
	NetworkedNode source,target;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NetworkConnection(QoSProfile profile) 
	{
		super();
		setQoSProfile(profile);
	}

	public void sampleLink()
	{
		qosProfile.sampleQoS();
	}
	
	private void setQoSProfile(QoSProfile profile) 
	{
		this.qosProfile = profile;
	}
	
	public QoSProfile getQoSProfile()
	{
		return qosProfile;
	}
	
	public double getLatency()
	{
		return qosProfile.getLatency();
	}
	
	public double getBandwidth()
	{
		return qosProfile.getBandwidth();
	}
	
	public NetworkedNode getTarget()
	{
		return (NetworkedNode) this.target;
	}
	
	public NetworkedNode getSource()
	{
		return (NetworkedNode) this.source;
	}
	
	public void setSource(NetworkedNode n)
	{
		this.source = n;
	}
	
	public void setTarget(NetworkedNode n)
	{
		this.target = n;
	}
	
}
