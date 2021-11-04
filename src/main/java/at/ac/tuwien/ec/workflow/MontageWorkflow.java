package at.ac.tuwien.ec.workflow;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.ConstantRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;

public class MontageWorkflow extends MobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = 739162783366447018L;
	private static ExponentialDistribution inDataDistr = new ExponentialDistribution(5);
	private static ExponentialDistribution outDataDistr = new ExponentialDistribution(5);
	private static ExponentialDistribution miDistr = new ExponentialDistribution(2);
	//private static ExponentialDistribution inDataDistr = new ExponentialDistribution(500000);
	//private static ExponentialDistribution outDataDistr = new ExponentialDistribution(500000);
	//private static ExponentialDistribution miDistr = new ExponentialDistribution(20000);
	//private static ConstantRealDistribution inDataDistr = new ConstantRealDistribution(500000);
	//private static ConstantRealDistribution outDataDistr = new ConstantRealDistribution(500000);
	//private static ConstantRealDistribution miDistr = new ConstantRealDistribution(20000);
	public MontageWorkflow()
	{
		super();
		this.setUserId("entry0");
		
	}
	
	public MontageWorkflow(int wId)
	{
		super(wId);
		this.setUserId("entry0");
	}
	
	public MontageWorkflow(int wId, String uid)
	{
		super(wId,uid);
		this.setUserId("entry0");
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
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
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
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("downloadAndProject0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("downloadAndProject1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("downloadAndProject2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("downloadAndProject3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		//SECOND BARRIER
		addComponent("BARRIER1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
		//LEVEL 3
		addComponent("calcDiffFitMulti0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("calcDiffFitMulti1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("calcDiffFitMulti2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("calcDiffFitMulti3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("calcDiffFitMulti4"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("calcDiffFitMulti5"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("calcDiffFitMulti6"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("calcDiffFitMulti7"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		//THIRD BARRIER
		addComponent("BARRIER2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,0
        		,0
        		,0
        		);
		//LEVEL 4
		addComponent("calcBackgroundModel"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("calcTiles"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("bgCorrectionMulti0"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("bgCorrectionMulti1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("bgCorrectionMulti2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("bgCorrectionMulti3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("bgCorrectionMulti4"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
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
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("addAndShrink1"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("addAndShrink2"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("addAndShrink3"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
        		);
		addComponent("addTiles"+"_"+getWorkloadId()+","+getUserId(),
				new Hardware(1, 1, 1)
				,this.getUserId()				
				,FGCSSetup.workflowMips + miDistr.sample()
        		,FGCSSetup.workflowIndata + inDataDistr.sample()
        		,FGCSSetup.workflowOutData + outDataDistr.sample()
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

	public void joinSequentially(MobileApplication app)
	{
		
		if(taskDependencies.vertexSet().isEmpty())
		{
			taskDependencies = (DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink>) ((AbstractBaseGraph)app.taskDependencies).clone();
			this.componentList.putAll(app.componentList);
		}
		else 
		{
			ArrayList<MobileSoftwareComponent> sinks = new ArrayList<MobileSoftwareComponent>();
			for(MobileSoftwareComponent msc : this.taskDependencies.vertexSet())
				if(taskDependencies.outgoingEdgesOf(msc).isEmpty())
					sinks.add(msc);
		
			this.componentList.putAll(app.componentList);
			Graphs.addGraph(this.taskDependencies, app.getTaskDependencies());
						
			ArrayList<MobileSoftwareComponent> sources = new ArrayList<MobileSoftwareComponent>();
			for(MobileSoftwareComponent msc : app.getTaskDependencies().vertexSet())
				if(app.getTaskDependencies().incomingEdgesOf(msc).isEmpty())
					sources.add(msc);
			
			for(MobileSoftwareComponent mscSrc : sinks)
				for(MobileSoftwareComponent mscTrg: sources )
					if(!taskDependencies.containsEdge(mscSrc, mscTrg)) 
						addLink(mscSrc.getId(), mscTrg.getId(),Double.MAX_VALUE,0);
		}
		
	}
	
}
