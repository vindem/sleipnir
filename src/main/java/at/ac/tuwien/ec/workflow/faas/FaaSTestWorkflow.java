package at.ac.tuwien.ec.workflow.faas;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class FaaSTestWorkflow extends FaaSWorkflow {

	public FaaSTestWorkflow(String[] publisherTopics, String[] subscribersTopics) {
		super(publisherTopics, subscribersTopics);
		// TODO Auto-generated constructor stub
	}

	public void setupTasks() {
		double img_size = SimulationSetup.facerecImageSize;
		addComponent("FACERECOGNIZER_UI"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				//,5.0 + ExponentialDistributionGenerator.getNext(1.0/5.0)
        		,this.getUserId()
				,5.0e3*SimulationSetup.task_multiplier
				,5e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,false
        		);
		addComponent("FIND_MATCH"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,8.0e3*SimulationSetup.task_multiplier
				,5e3*SimulationSetup.task_multiplier
        		,img_size*SimulationSetup.task_multiplier
        		);
		addComponent("FIND_MATCH_INIT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,8.0e3*SimulationSetup.task_multiplier
				,5e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		);
		addComponent("DETECT_FACE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,16.0 + ExponentialDistributionGenerator.getNext(1.0/16.0)
        		,16.0e3*SimulationSetup.task_multiplier
				,img_size*SimulationSetup.task_multiplier
        		,img_size*SimulationSetup.task_multiplier
        		);
		addComponent("FACERECOGNIZER_OUTPUT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()
				//,8.0 + ExponentialDistributionGenerator.getNext(1.0/8.0)
        		,8.0e3*SimulationSetup.task_multiplier
				,img_size*SimulationSetup.task_multiplier
        		,img_size*SimulationSetup.task_multiplier
        		,false
        		);
		setSource(this.getComponentById("FACERECOGNIZER_UI"+"_"+getWorkloadId()+","+getUserId()));
		setSink(this.getComponentById("FACERECOGNIZER_OUTPUT"+"_"+getWorkloadId()+","+getUserId()));
	}

	@Override
	public void setupLinks() {
		addLink("FACERECOGNIZER_UI"+"_"+getWorkloadId()+","+getUserId(),
				"FIND_MATCH"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("FIND_MATCH"+"_"+getWorkloadId()+","+getUserId(),
				"FIND_MATCH_INIT"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("FIND_MATCH"+"_"+getWorkloadId()+","+getUserId(),
				"DETECT_FACE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("FIND_MATCH_INIT"+"_"+getWorkloadId()+","+getUserId(),
				"DETECT_FACE"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("DETECT_FACE"+"_"+getWorkloadId()+","+getUserId(),
				"FACERECOGNIZER_OUTPUT"+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8278434293290262537L;

}
