package at.ac.tuwien.ec.model.software;

import java.util.ArrayList;

import org.jgrapht.Graphs;


public class MobileWorkload extends MobileApplication {

	private ArrayList<MobileApplication> workload;
	private MobileSoftwareComponent workloadRoot;
				
	public MobileWorkload()
	{
		this.workload = new ArrayList<MobileApplication>();
	}
	
	public MobileWorkload(ArrayList<MobileApplication> workload)
	{
		this.workload = workload;
		setupTasks();
		setupLinks();
	}

	public void joinParallel()
	{
		
	}
	
	public void joinSequentially()
	{
		
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
		for(MobileApplication app:workload)
			this.componentList.putAll(app.componentList);
	}

	@Override
	public void setupLinks() {
		for(MobileApplication app:workload)
			Graphs.addGraph(this.taskDependencies, app.taskDependencies);
	}
	
	
}
