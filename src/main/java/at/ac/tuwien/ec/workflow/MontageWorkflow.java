package at.ac.tuwien.ec.workflow;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class MontageWorkflow extends MobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = 739162783366447018L;

	public MontageWorkflow()
	{
		super();
	}
	
	public MontageWorkflow(int wId)
	{
		super(wId);
	}
	
	public MontageWorkflow(int wId, String uid)
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
		addComponent("ROOT"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T4"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//END OF FIRST LEVEL
		addComponent("T5"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T6"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T7"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T8"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T9"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T10"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T11"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T12"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//END OF SECOND LEVEL
		addComponent("T13"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//END OF THIRD LEVEL
		addComponent("T14"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//END OF FOURTH LEVEL
		addComponent("T15"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T16"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T17"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T18"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T19"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//END OF FIFTH LEVEL
		addComponent("T20"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T21"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T22"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T23"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("T24"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
	}

	@Override
	public void setupLinks() {
		addLink("ROOT"+"_"+getWorkloadId()+","+getUserId()
		, "T0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("ROOT"+"_"+getWorkloadId()+","+getUserId()
		, "T1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("ROOT"+"_"+getWorkloadId()+","+getUserId()
		, "T2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("ROOT"+"_"+getWorkloadId()+","+getUserId()
		, "T3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("ROOT"+"_"+getWorkloadId()+","+getUserId()
		, "T4"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LINKS FROM T0
		addLink("T0"+"_"+getWorkloadId()+","+getUserId()
		, "T5"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T0"+"_"+getWorkloadId()+","+getUserId()
		, "T5"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T0"+"_"+getWorkloadId()+","+getUserId()
		, "T6"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T0"+"_"+getWorkloadId()+","+getUserId()
		, "T8"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T0"+"_"+getWorkloadId()+","+getUserId()
		, "T16"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LINKS FROM T1
		addLink("T1"+"_"+getWorkloadId()+","+getUserId()
		, "T5"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T1"+"_"+getWorkloadId()+","+getUserId()
		, "T6"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T1"+"_"+getWorkloadId()+","+getUserId()
		, "T7"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T1"+"_"+getWorkloadId()+","+getUserId()
		, "T9"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T1"+"_"+getWorkloadId()+","+getUserId()
		, "T10"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T1"+"_"+getWorkloadId()+","+getUserId()
		, "T11"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T1"+"_"+getWorkloadId()+","+getUserId()
		, "T17"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LINKS FROM T2
		addLink("T2"+"_"+getWorkloadId()+","+getUserId()
		, "T8"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T2"+"_"+getWorkloadId()+","+getUserId()
		, "T18"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T2"+"_"+getWorkloadId()+","+getUserId()
		, "T11"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LINK FROM T3
		addLink("T3"+"_"+getWorkloadId()+","+getUserId()
		, "T11"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T3"+"_"+getWorkloadId()+","+getUserId()
		, "T19"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LINKS FROM T4
		addLink("T4"+"_"+getWorkloadId()+","+getUserId()
		, "T12"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T4"+"_"+getWorkloadId()+","+getUserId()
		, "T13"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T4"+"_"+getWorkloadId()+","+getUserId()
		, "T20"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LINKS OF THIRD LEVEL
		addLink("T5"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T6"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T7"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T8"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T9"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T10"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T11"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T12"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T13"+"_"+getWorkloadId()+","+getUserId()
		, "T14"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//fourth level
		addLink("T14"+"_"+getWorkloadId()+","+getUserId()
		, "T15"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//fifth level
		addLink("T15"+"_"+getWorkloadId()+","+getUserId()
		, "T16"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T15"+"_"+getWorkloadId()+","+getUserId()
		, "T17"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T15"+"_"+getWorkloadId()+","+getUserId()
		, "T18"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T15"+"_"+getWorkloadId()+","+getUserId()
		, "T19"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T15"+"_"+getWorkloadId()+","+getUserId()
		, "T20"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//tail
		addLink("T18"+"_"+getWorkloadId()+","+getUserId()
		, "T21"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T21"+"_"+getWorkloadId()+","+getUserId()
		, "T22"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T22"+"_"+getWorkloadId()+","+getUserId()
		, "T23"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("T23"+"_"+getWorkloadId()+","+getUserId()
		, "T24"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
	}

}
