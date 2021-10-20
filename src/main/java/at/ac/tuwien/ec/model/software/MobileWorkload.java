package at.ac.tuwien.ec.model.software;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.Hardware;


public class MobileWorkload extends MobileApplication {

	private ArrayList<MobileApplication> workload;
					
	public MobileWorkload()
	{
		this.componentList  = new LinkedHashMap<String,MobileSoftwareComponent>();
		this.taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		this.workload = new ArrayList<MobileApplication>();
		
	}
	
	public MobileWorkload(ArrayList<MobileApplication> workload)
	{
		this.componentList  = new LinkedHashMap<String,MobileSoftwareComponent>();
		this.taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		this.workload = new ArrayList<MobileApplication>();
		for(MobileApplication app:workload)
			joinSequentially(app);
	}
	
	public MobileWorkload copy()
	{
		MobileWorkload w1 = new MobileWorkload();
		for(String s : this.componentList.keySet())
			w1.componentList.put(s,this.componentList.get(s));
		for(MobileSoftwareComponent msc : taskDependencies.vertexSet())
			w1.taskDependencies.addVertex(msc);
		for(ComponentLink edge : taskDependencies.edgeSet())
			w1.taskDependencies.addEdge(edge.getSource(),edge.getTarget(),edge);
		w1.workload.addAll(this.workload);
		return w1;
	}
	
	public void joinParallel(MobileApplication app)
	{
		workload.add(app);
		Graphs.addGraph(this.taskDependencies, app.getTaskDependencies());
		this.componentList.putAll(app.componentList);
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
