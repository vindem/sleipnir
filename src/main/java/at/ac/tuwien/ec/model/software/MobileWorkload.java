package at.ac.tuwien.ec.model.software;

import java.util.ArrayList;
import java.util.HashMap;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.Hardware;


public class MobileWorkload extends MobileApplication {

	private ArrayList<MobileApplication> workload;
					
	public MobileWorkload()
	{
		this.componentList  = new HashMap<String,MobileSoftwareComponent>();
		this.taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		this.workload = new ArrayList<MobileApplication>();
		
	}
	
	

	public void joinParallel(MobileApplication app)
	{
		workload.add(app);
		Graphs.addGraph(this.taskDependencies, app.getTaskDependencies());
		this.componentList.putAll(app.componentList);
		ArrayList<MobileSoftwareComponent> sources = new ArrayList<MobileSoftwareComponent>();
		
		if(getComponentById("SOURCE") == null)
			addComponent("SOURCE", new Hardware(0, 0, 0), "mobile_0" , 0, true);
		if(getComponentById("SINK") == null)
			addComponent("SINK", new Hardware(0, 0, 0), "mobile_0" , 0, true);
		
		for(MobileSoftwareComponent msc : app.getTaskDependencies().vertexSet())
			if(app.getTaskDependencies().incomingEdgesOf(msc).isEmpty())
				sources.add(msc);
		
		for(MobileSoftwareComponent msc : sources)
			addLink(getComponentById("SOURCE"),msc,Double.MAX_VALUE,0);
						
		ArrayList<MobileSoftwareComponent> sink = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : app.getTaskDependencies().vertexSet())
			if(app.getTaskDependencies().outgoingEdgesOf(msc).isEmpty())
				sink.add(msc);
		
		for(MobileSoftwareComponent msc : sink)
			addLink(msc,getComponentById("SINK"),Double.MAX_VALUE,0);
		
		
	}
	
	public void joinSequentially(MobileApplication app)
	{
		ArrayList<MobileSoftwareComponent> sinks = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : this.taskDependencies.vertexSet())
			if(taskDependencies.outgoingEdgesOf(msc).isEmpty())
				sinks.add(msc);
		
		ArrayList<MobileSoftwareComponent> sources = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : app.getTaskDependencies().vertexSet())
			if(app.getTaskDependencies().incomingEdgesOf(msc).isEmpty())
				sources.add(msc);
		
		Graphs.addGraph(this.taskDependencies, app.getTaskDependencies());
		this.componentList.putAll(app.componentList);
		
		for(MobileSoftwareComponent mscSrc : sinks)
			for(MobileSoftwareComponent mscTrg: sources )
				if(!taskDependencies.containsEdge(mscSrc, mscTrg))
					addLink(mscSrc.getId(), mscTrg.getId(),Double.MAX_VALUE,0);
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
		
	}

	@Override
	public void setupLinks() {
		
	}

	public ArrayList<MobileApplication> getWorkload() {
		return workload;
	}
	
	
}
