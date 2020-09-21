package at.ac.tuwien.ec.model.software;

import org.jgrapht.graph.DefaultEdge;

import at.ac.tuwien.ec.model.QoSProfile;

public class ComponentLink extends DefaultEdge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	QoSProfile profile;
	
	public ComponentLink(QoSProfile profile){
		super();
		setDesiredQoS(profile);
	}
		
	public ComponentLink(double latency, double bandwidth){
		super();
		profile = new QoSProfile(latency,bandwidth);
	}
	
	public QoSProfile getDesiredQoS(){
		return profile;		
	}
	
	private void setDesiredQoS(QoSProfile profile){
		this.profile = profile;
	}

	public MobileSoftwareComponent getSource()
	{
		return (MobileSoftwareComponent) super.getSource();
	}
	
	public MobileSoftwareComponent getTarget()
	{
		return (MobileSoftwareComponent) super.getTarget();
	}
}
