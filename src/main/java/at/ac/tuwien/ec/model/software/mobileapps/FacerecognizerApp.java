package at.ac.tuwien.ec.model.software.mobileapps;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class FacerecognizerApp extends MobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8209655232158209461L;

	public FacerecognizerApp(){
		super();
	}
	
	public FacerecognizerApp(int wId) {
		super(wId);
	}
	
	public FacerecognizerApp(int wId,String uid)
	{
		super(wId,uid);
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

}
