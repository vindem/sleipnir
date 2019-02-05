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
		addComponent("SOURCE"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
		//LEVEL 1
		addComponent("retrieveImageList"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//FIRST BARRIER
		addComponent("BARRIER0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
		//LEVEL 2
		addComponent("calculateOverlaps"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("downloadAndProject0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("downloadAndProject1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("downloadAndProject2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("downloadAndProject3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//SECOND BARRIER
		addComponent("BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//LEVEL 3
		addComponent("calcDiffFitMulti0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("calcDiffFitMulti1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("calcDiffFitMulti2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("calcDiffFitMulti3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("calcDiffFitMulti4"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("calcDiffFitMulti5"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("calcDiffFitMulti6"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("calcDiffFitMulti7"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//THIRD BARRIER
		addComponent("BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//LEVEL 4
		addComponent("calcBackgroundModel"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("calcTiles"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("bgCorrectionMulti0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("bgCorrectionMulti1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("bgCorrectionMulti2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("bgCorrectionMulti3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("bgCorrectionMulti4"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		//FOURTH BARRIER
		addComponent("BARRIER3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
		//LEVEL 5
		addComponent("addAndShrink0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("addAndShrink1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("addAndShrink2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("addAndShrink3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("addTiles"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,2.0e3*SimulationSetup.task_multiplier
        		,5e3*SimulationSetup.task_multiplier
        		,1e3*SimulationSetup.task_multiplier
        		);
		addComponent("SINK"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
	}

	@Override
	public void setupLinks() {
		addLink("SOURCE"+"_"+getWorkloadId()+","+getUserId()
		, "retrieveImageList"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 1
		addLink("retrieveImageList"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 2
		addLink("BARRIER0"+"_"+getWorkloadId()+","+getUserId()
		, "calculateOverlaps"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER0"+"_"+getWorkloadId()+","+getUserId()
		, "downloadAndProject0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER0"+"_"+getWorkloadId()+","+getUserId()
		, "downloadAndProject1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER0"+"_"+getWorkloadId()+","+getUserId()
		, "downloadAndProject2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER0"+"_"+getWorkloadId()+","+getUserId()
		, "downloadAndProject3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calculateOverlaps"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("downloadAndProject0"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("downloadAndProject1"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("downloadAndProject2"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("downloadAndProject3"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 3
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "calcDiffFitMulti0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "calcDiffFitMulti1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "calcDiffFitMulti2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "calcDiffFitMulti3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "calcDiffFitMulti4"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "calcDiffFitMulti5"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "calcDiffFitMulti6"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER1"+"_"+getWorkloadId()+","+getUserId()
		, "calcDiffFitMulti7"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti0"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti0"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti1"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti2"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti3"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti4"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti5"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti6"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcDiffFitMulti7"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 4
		addLink("BARRIER2"+"_"+getWorkloadId()+","+getUserId()
		, "calcBackgroundModel"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER2"+"_"+getWorkloadId()+","+getUserId()
		, "calcTiles"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcBackgroundModel"+"_"+getWorkloadId()+","+getUserId()
		, "bgCorrectionMulti0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcBackgroundModel"+"_"+getWorkloadId()+","+getUserId()
		, "bgCorrectionMulti1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcBackgroundModel"+"_"+getWorkloadId()+","+getUserId()
		, "bgCorrectionMulti2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcBackgroundModel"+"_"+getWorkloadId()+","+getUserId()
		, "bgCorrectionMulti3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcBackgroundModel"+"_"+getWorkloadId()+","+getUserId()
		, "bgCorrectionMulti4"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("calcTiles"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bgCorrectionMulti0"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bgCorrectionMulti1"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bgCorrectionMulti2"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bgCorrectionMulti3"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("bgCorrectionMulti4"+"_"+getWorkloadId()+","+getUserId()
		, "BARRIER3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		//LEVEL 5
		addLink("BARRIER3"+"_"+getWorkloadId()+","+getUserId()
		, "addAndShrink0"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER3"+"_"+getWorkloadId()+","+getUserId()
		, "addAndShrink1"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER3"+"_"+getWorkloadId()+","+getUserId()
		, "addAndShrink2"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("BARRIER3"+"_"+getWorkloadId()+","+getUserId()
		, "addAndShrink3"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("addAndShrink0"+"_"+getWorkloadId()+","+getUserId()
		, "addTiles"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("addAndShrink1"+"_"+getWorkloadId()+","+getUserId()
		, "addTiles"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("addAndShrink2"+"_"+getWorkloadId()+","+getUserId()
		, "addTiles"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("addAndShrink3"+"_"+getWorkloadId()+","+getUserId()
		, "addTiles"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
		addLink("addTiles"+"_"+getWorkloadId()+","+getUserId()
		, "SINK"+"_"+getWorkloadId()+","+getUserId(),
		sampleLatency(),
		0.1);
	}

}
