package at.ac.tuwien.ec.model.infrastructure.network;

import org.jgrapht.graph.DefaultEdge;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;

public class NetworkConnection extends DefaultEdge {

	QoSProfile qosProfileULDL;
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
		qosProfileULDL.sampleQoS();
	}
	
	private void setQoSProfile(QoSProfile profile) 
	{
		this.qosProfileULDL = profile;
	}
	
	public QoSProfile getQoSProfile()
	{
		return qosProfileULDL;
	}
	
	public double getLatency()
	{
		return qosProfileULDL.getLatency();
	}
	
	public double getBandwidth()
	{
		return qosProfileULDL.getBandwidth();
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
