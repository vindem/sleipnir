package at.ac.tuwien.ec.faas.workflows;

import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class FaaSWorkflow extends MobileApplication {

	private MobileSoftwareComponent source, target;
	private String dataTopic;
	
	
	
	public String getDataTopic() {
		return dataTopic;
	}

	public void setDataTopic(String dataTopic) {
		this.dataTopic = dataTopic;
	}

	public MobileSoftwareComponent getTarget() {
		return target;
	}

	public void setTarget(MobileSoftwareComponent target) {
		this.target = target;
	}

	public void setSource(MobileSoftwareComponent source) {
		this.source = source;
	}

	public MobileSoftwareComponent getSource()
	{
		return source;
	}
	
	@Override
	public void sampleTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sampleLinks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupLinks() {
		// TODO Auto-generated method stub
		
	}

}
