package at.ac.tuwien.ec.workflow.faas;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class OFWorkflow extends FaaSWorkflow {

	final double taskMultiplier = 1.0;
	final double dataMultiplier = 1.0;
	
	public OFWorkflow(int wId, String[] publisherTopics, String[] subscribersTopics) {
		super(wId,publisherTopics, subscribersTopics);
	}
	
	public OFWorkflow(String[] publisherTopics, String[] subscribersTopics) {
		super(publisherTopics, subscribersTopics);
		// TODO Auto-generated constructor stub
	}

	public void setupTasks() {
		double img_size = 5e3;
		addComponent("IOT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				//,5.0 + ExponentialDistributionGenerator.getNext(1.0/5.0)
        		,this.getUserId()
				,1.0e3 * taskMultiplier
				,5e3 * taskMultiplier
        		,1e3*taskMultiplier
        		);
		addComponent("ANALYZE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,8.0e3*taskMultiplier
				,dataMultiplier*taskMultiplier
        		,dataMultiplier*taskMultiplier
        		);
		addComponent("SERVICE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,1.0e3*taskMultiplier
				,dataMultiplier*taskMultiplier
        		,dataMultiplier*taskMultiplier
        		,false
        		);
		addComponent("CREATE_ORDER"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,16.0 + ExponentialDistributionGenerator.getNext(1.0/16.0)
        		,1.0e3*taskMultiplier
				,dataMultiplier*taskMultiplier
        		,dataMultiplier*taskMultiplier
        		);
		addComponent("ORDER"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,1.0e3*taskMultiplier
				,dataMultiplier*taskMultiplier
        		,dataMultiplier*taskMultiplier
        		,false
        		);
		addComponent("SEND_ALERT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,1.0e3*taskMultiplier
				,dataMultiplier*taskMultiplier
        		,dataMultiplier*taskMultiplier
        		);
		setSource(this.getComponentById("IOT"+"_"+getWorkloadId()+","+getUserId()));
		setSink(this.getComponentById("SEND_ALERT"+"_"+getWorkloadId()+","+getUserId()));
	}

	@Override
	public void setupLinks() {
		addLink("IOT"+"_"+getWorkloadId()+","+getUserId(),
				"ANALYZE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("ANALYZE"+"_"+getWorkloadId()+","+getUserId(),
				"SERVICE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("SERVICE"+"_"+getWorkloadId()+","+getUserId(),
				"CREATE_ORDER"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("CREATE_ORDER"+"_"+getWorkloadId()+","+getUserId(),
				"ORDER"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("ORDER"+"_"+getWorkloadId()+","+getUserId(),
				"SEND_ALERT"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8278434293290262537L;

}
