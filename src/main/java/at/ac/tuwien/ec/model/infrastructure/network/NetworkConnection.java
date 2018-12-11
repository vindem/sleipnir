package at.ac.tuwien.ec.model.infrastructure.network;

import org.jgrapht.graph.DefaultEdge;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;

public class NetworkConnection extends DefaultEdge {

	QoSProfile qosProfile;
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
	
	public ComputationalNode getTarget()
	{
		return (ComputationalNode) super.getTarget();
	}
	
	public ComputationalNode getSource()
	{
		return (ComputationalNode) super.getSource();
	}
	
}
