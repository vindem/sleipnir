package at.ac.tuwien.ec.workflow.faas;

import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class FaaSWorkflow extends MobileApplication {

	private String[] publisherTopics, subscribersTopic;
	private MobileSoftwareComponent source, sink;
	
	public FaaSWorkflow(String[] publisherTopics, String[] subscribersTopics)
	{
		super();
		this.publisherTopics = publisherTopics;
		this.subscribersTopic = subscribersTopics;
	}
	
	public String[] getPublisherTopics() {
		return publisherTopics;
	}

	public void setPublisherTopics(String[] publisherTopics) {
		this.publisherTopics = publisherTopics;
	}

	public String[] getSubscribersTopic() {
		return subscribersTopic;
	}

	public void setSubscribersTopic(String[] subscribersTopic) {
		this.subscribersTopic = subscribersTopic;
	}

	public MobileSoftwareComponent getSource() {
		return source;
	}

	public void setSource(MobileSoftwareComponent source) {
		this.source = source;
	}

	public MobileSoftwareComponent getSink() {
		return sink;
	}

	public void setSink(MobileSoftwareComponent sink) {
		this.sink = sink;
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
