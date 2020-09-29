package at.ac.tuwien.ec.workflow.faas;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class OFWorkflow extends FaaSWorkflow {

	public OFWorkflow(int wId, String[] publisherTopics, String[] subscribersTopics) {
		super(wId,publisherTopics, subscribersTopics);
	}
	
	public OFWorkflow(String[] publisherTopics, String[] subscribersTopics) {
		super(publisherTopics, subscribersTopics);
		// TODO Auto-generated constructor stub
	}

	public void setupTasks() {
		double img_size = SimulationSetup.facerecImageSize;
		addComponent("IOT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				//,5.0 + ExponentialDistributionGenerator.getNext(1.0/5.0)
        		,this.getUserId()
				,1.0e3*SimulationSetup.task_multiplier
				,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("ANALYZE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,8.0e3*SimulationSetup.task_multiplier
				,1e6*SimulationSetup.task_multiplier
        		,1e6*SimulationSetup.task_multiplier
        		);
		addComponent("SERVICE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,1.0e3*SimulationSetup.task_multiplier
				,1e6*SimulationSetup.task_multiplier
        		,1e6*SimulationSetup.task_multiplier
        		,false
        		);
		addComponent("CREATE_ORDER"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,16.0 + ExponentialDistributionGenerator.getNext(1.0/16.0)
        		,1.0e3*SimulationSetup.task_multiplier
				,1e6*SimulationSetup.task_multiplier
        		,1e6*SimulationSetup.task_multiplier
        		);
		addComponent("ORDER"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,1.0e3*SimulationSetup.task_multiplier
				,1e6*SimulationSetup.task_multiplier
        		,1e6*SimulationSetup.task_multiplier
        		,false
        		);
		addComponent("SEND_ALERT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,1.0e3*SimulationSetup.task_multiplier
				,10e6*SimulationSetup.task_multiplier
        		,10e6*SimulationSetup.task_multiplier
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
