package at.ac.tuwien.ec.workflow.faas;

import java.util.ArrayList;

import org.jgrapht.Graphs;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class FaaSWorkflow extends MobileApplication {

	private String[] publisherTopics, subscribersTopic;
	private MobileSoftwareComponent source, sink;
	
	public FaaSWorkflow(String[] publisherTopics, String[] subscribersTopics)
	{
		super();
		this.publisherTopics = publisherTopics;
		this.subscribersTopic = subscribersTopics;
		addComponent("SOURCE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				//,5.0 + ExponentialDistributionGenerator.getNext(1.0/5.0)
        		,this.getUserId()
				,1.0
				,1.0
        		,1.0
        		);
		addComponent("SINK"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				//,5.0 + ExponentialDistributionGenerator.getNext(1.0/5.0)
        		,this.getUserId()
				,1.0
				,1.0
        		,1.0
        		);
		setSource(this.getComponentById("SOURCE"+"_"+getWorkloadId()+","+getUserId()));
		setSink(this.getComponentById("SINK"+"_"+getWorkloadId()+","+getUserId()));
	}
	
	
	public FaaSWorkflow(int wId, String[] publisherTopics, String[] subscribersTopics)
	{
		super(wId);
		this.publisherTopics = publisherTopics;
		this.subscribersTopic = subscribersTopics;
		/*addComponent("SOURCE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				//,5.0 + ExponentialDistributionGenerator.getNext(1.0/5.0)
        		,this.getUserId()
				,1.0
				,1.0
        		,1.0
        		,false
        		);
		addComponent("SINK"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				//,5.0 + ExponentialDistributionGenerator.getNext(1.0/5.0)
        		,this.getUserId()
				,1.0
				,1.0
        		,1.0
        		,false
        		);
		setSource(this.getComponentById("SOURCE"+"_"+getWorkloadId()+","+getUserId()));
		setSink(this.getComponentById("SINK"+"_"+getWorkloadId()+","+getUserId()));*/
	}
	
	/*public String[] getPublisherTopics() {
		return publisherTopics;
	}*/

	public void setPublisherTopics(String[] publisherTopics) {
		this.publisherTopics = publisherTopics;
	}

	/*public String[] getSubscribersTopic() {
		return subscribersTopic;
	}*/

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
	
	public void joinParallel(FaaSWorkflow wflow)
	{
		this.componentList.putAll(wflow.componentList);
		Graphs.addGraph(this.getTaskDependencies(), wflow.getTaskDependencies());
		addLink(this.getSource(), wflow.getSource(), new QoSProfile(Double.MAX_VALUE, 0.0));
		for(MobileSoftwareComponent msc : wflow.componentList.values())
		{
			if(this.getTaskDependencies().outDegreeOf(msc) == 0)
				addLink(msc, this.getSink(), new QoSProfile(Double.MAX_VALUE, 0.0));
		}
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


	public void joinSequential(FaaSWorkflow faasWorkflowNew) {
		this.addComponent(faasWorkflowNew.getSource());
		
		if(this.getTaskDependencies().outDegreeOf(this.getSource()) == 0)
			addLink(this.getSource(),faasWorkflowNew.getSource(),Double.MAX_VALUE,0.0);
		else 
		{
			this.addComponent(faasWorkflowNew.getSource());
			
			ArrayList<ComponentLink> inSinkEdges = new ArrayList<ComponentLink>();
			inSinkEdges.addAll(this.getTaskDependencies().incomingEdgesOf(getSink()));
		
			for(ComponentLink link : inSinkEdges)
				this.getTaskDependencies().removeEdge(link);
			
			ArrayList<ComponentLink> outSinkEdges = new ArrayList<ComponentLink>();
			outSinkEdges.addAll(this.getTaskDependencies().incomingEdgesOf(getSink()));
		
			for(ComponentLink link : outSinkEdges)
				this.getTaskDependencies().removeEdge(link);
			
			for(MobileSoftwareComponent msc : this.componentList.values())
			{
				if(this.getTaskDependencies().outDegreeOf(msc) == 0 && !msc.getId().contains("SINK"))
					addLink(msc,faasWorkflowNew.getSource(),Double.MAX_VALUE,0.0);
				
			}
						
		}
		this.componentList.putAll(faasWorkflowNew.componentList);
		Graphs.addGraph(this.getTaskDependencies(), faasWorkflowNew.getTaskDependencies());
		
		addLink(faasWorkflowNew.getSink(),this.getSink(), Double.MAX_VALUE,0.0);
	}

}
