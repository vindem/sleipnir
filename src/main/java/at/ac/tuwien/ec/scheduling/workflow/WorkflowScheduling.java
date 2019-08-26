package at.ac.tuwien.ec.scheduling.workflow;

import java.util.LinkedHashMap;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;

public class WorkflowScheduling extends Scheduling {
	
	private double runTime;
	private double reliability;
	private double userCost;
	private int hashCode = Integer.MIN_VALUE;
	private double wallClock;

	public WorkflowScheduling()
	{
		super();
		runTime = 0.0;
		reliability = 1.0;
		userCost = 0.0;
	}
	
	public WorkflowScheduling(WorkflowScheduling scheduling)
	{
		super(scheduling);
	}
	
	public String toString(){
        String result ="";

        for (SoftwareComponent s : super.keySet()){
            result+="["+s.getId()+"->" +super.get(s).getId()+"]" ;
        }
        
        return result;   
    }
    
    @Override
    public boolean equals(Object o){
        boolean result = true;
        WorkflowScheduling d = (WorkflowScheduling) o;
        result = this.hashCode() == d.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        if(this.hashCode == Integer.MIN_VALUE)
        {
        	int hash = 7;
        	String s = this.toString();
        	this.hashCode = 47 * hash + s.hashCode();
        }
        return this.hashCode;
    }

    public void addRuntime(MobileSoftwareComponent s, double runtime){
    	//s.setRunTime(runtime);
    	if(this.runTime < runtime)
    		this.runTime = runtime;
    }
    
    public void removeRuntime(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	this.runTime -= s.getRuntimeOnNode((ComputationalNode) super.get(s), I);
    }
    
    public void addCost(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I) {
        this.userCost += n.computeCost(s, I);
    }
    
    public void removeCost(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	this.userCost -= n.computeCost(s, I);
    }

	public double getRunTime() {
		return runTime;
	}

	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}

	public double getReliability() {
		return reliability;
	}

	public void setReliability(double reliability) {
		this.reliability = reliability;
	}

	public double getUserCost() {
		return userCost;
	}

	public void setUserCost(double userCost) {
		this.userCost = userCost;
	}

	public void addReliability(MobileSoftwareComponent s, ComputationalNode n,
			MobileCloudInfrastructure currentInfrastructure) {
		double availability = 1.0;
		if(n instanceof CloudDataCenter) 
		{
			availability = ((CloudDataCenter)n).getAvailabilityAt(0.0);
			//System.out.println(s.getId() + "\t" + n.getId() + "\t" + availability);
		}
		else if(n instanceof EdgeNode)
		{
			availability = ((EdgeNode)n).getAvailabilityAt(0.0);
			//System.out.println(s.getId() + "\t" + n.getId() + "\t" + availability);
		}
		else
			availability = 1.0;
		//System.out.println(availability);
		this.reliability = this.reliability * availability;
		
	}

	public void removeReliability(MobileSoftwareComponent s, ComputationalNode n,
			MobileCloudInfrastructure currentInfrastructure) {
		double availability = 1.0;
		if(n instanceof CloudDataCenter)
			availability = ((CloudDataCenter)n).getAvailabilityAt(0.0);
		else if(n instanceof EdgeNode)
			availability = ((EdgeNode)n).getAvailabilityAt(0.0);
		else
			availability = 1.0;
		this.reliability = this.reliability / availability;
	}

	public void addWallClock(double d) {
		this.wallClock = d;
		
	}
	
	public double getWallClock() 
	{
		return this.wallClock;
	}

}
