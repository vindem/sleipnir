package at.ac.tuwien.ec.model.software;

import java.util.ArrayList;

import org.jgrapht.Graphs;


public class MobileWorkload extends MobileApplication {

	private ArrayList<MobileApplication> workload;
	private MobileSoftwareComponent workloadRoot;
				
	public MobileWorkload()
	{
		super();
		this.workload = new ArrayList<MobileApplication>();
	}
	
	public MobileWorkload(ArrayList<MobileApplication> workload)
	{
		super();
		this.workload = workload;
		setupTasks();
		setupLinks();
	}

	public void joinParallel(MobileApplication app)
	{
		workload.add(app);
		Graphs.addGraph(this.taskDependencies, app.getTaskDependencies());
	}
	
	public void joinSequentially(MobileApplication app)
	{
		ArrayList<MobileSoftwareComponent> sinks = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : taskDependencies.vertexSet())
			if(taskDependencies.outgoingEdgesOf(msc).isEmpty())
				sinks.add(msc);
		
		ArrayList<MobileSoftwareComponent> sources = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : app.getTaskDependencies().vertexSet())
			if(app.getTaskDependencies().incomingEdgesOf(msc).isEmpty())
				sources.add(msc);
		
		Graphs.addGraph(this.taskDependencies, app.getTaskDependencies());
		
		for(MobileSoftwareComponent mscSrc : sinks)
			for(MobileSoftwareComponent mscTrg: sources )
				taskDependencies.addEdge(mscSrc, mscTrg);
		
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
	
	
}
