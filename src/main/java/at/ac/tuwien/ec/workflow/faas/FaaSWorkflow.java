package at.ac.tuwien.ec.workflow.faas;

import at.ac.tuwien.ec.model.software.MobileApplication;

public class FaaSWorkflow extends MobileApplication {

	String[] publisherTopics, subscribersTopic;
	
	public FaaSWorkflow(String[] publisherTopics, String[] subscribersTopics)
	{
		this.publisherTopics = publisherTopics;
		this.subscribersTopic = subscribersTopics;
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
