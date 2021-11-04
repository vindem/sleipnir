package at.ac.tuwien.ec.workflow.faas;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.sleipnir.configurations.IoTFaaSSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class IRWorkflow extends FaaSWorkflow {

	public IRWorkflow(int wId, String[] publisherTopics, String[] subscribersTopics) {
		super(wId,publisherTopics, subscribersTopics);
	}
	
	public IRWorkflow(String[] publisherTopics, String[] subscribersTopics) {
		super(publisherTopics, subscribersTopics);
		// TODO Auto-generated constructor stub
	}

	public void setupTasks() {
		double img_size = IoTFaaSSetup.dataMultiplier;
		addComponent("EXTRACT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				//,5.0 + ExponentialDistributionGenerator.getNext(1.0/5.0)
        		,this.getUserId()
				,94
				,img_size
        		,img_size
        		);
		addComponent("PROCESS"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,96
				,img_size
        		,img_size
        		);
		addComponent("RECOGNIZE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,475
				,img_size
        		,img_size
        		);
		addComponent("RESIZE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,16.0 + ExponentialDistributionGenerator.getNext(1.0/16.0)
        		,174
				,img_size
        		,img_size
        		);
		setSource(this.getComponentById("EXTRACT"+"_"+getWorkloadId()+","+getUserId()));
		setSink(this.getComponentById("RESIZE"+"_"+getWorkloadId()+","+getUserId()));
	}

	@Override
	public void setupLinks() {
		addLink("EXTRACT"+"_"+getWorkloadId()+","+getUserId(),
				"PROCESS"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("PROCESS"+"_"+getWorkloadId()+","+getUserId(),
				"RECOGNIZE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("RECOGNIZE"+"_"+getWorkloadId()+","+getUserId(),
				"RESIZE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8278434293290262537L;

}
