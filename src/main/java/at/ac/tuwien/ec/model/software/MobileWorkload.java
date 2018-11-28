package at.ac.tuwien.ec.model.software;

import java.util.ArrayList;
import java.util.HashMap;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;


public class MobileWorkload extends MobileApplication {

	private ArrayList<MobileApplication> workload;
					
	public MobileWorkload()
	{
		this.componentList  = new HashMap<String,MobileSoftwareComponent>();
		this.taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		this.workload = new ArrayList<MobileApplication>();
	}
	
	public MobileWorkload(ArrayList<MobileApplication> workload)
	{
		this.workload = workload;
		this.componentList  = new HashMap<String,MobileSoftwareComponent>();
		this.taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		for(MobileApplication app: workload)
			this.joinSequentially(app);
		setupTasks();
		setupLinks();
	}

	public void joinParallel(MobileApplication app)
	{
		workload.add(app);
		Graphs.addGraph(this.taskDependencies, app.getTaskDependencies());
		this.componentList.putAll(app.componentList);
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
