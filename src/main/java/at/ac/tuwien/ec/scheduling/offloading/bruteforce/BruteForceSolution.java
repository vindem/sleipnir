package at.ac.tuwien.ec.scheduling.offloading.bruteforce;

import java.util.ArrayList;
import java.util.PriorityQueue;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import scala.Tuple2;


public class BruteForceSolution extends OffloadScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1818240099064268186L;
	private double runTime, userCost, batteryLifetime;
	private ArrayList<Tuple2<MobileSoftwareComponent,ComputationalNode>> variables;
	
	public BruteForceSolution(MobileApplication a, MobileCloudInfrastructure i)
	{
		setMobileApplication(a);
		setInfrastructure(i);
		variables = new ArrayList<Tuple2<MobileSoftwareComponent,ComputationalNode>>();
	}
	
	private BruteForceSolution(MobileApplication a, MobileCloudInfrastructure i,ArrayList<Tuple2<MobileSoftwareComponent,ComputationalNode>> variables)
	{
		setMobileApplication(a);
		setInfrastructure(i);
		this.variables = (ArrayList<Tuple2<MobileSoftwareComponent, ComputationalNode>>) variables.clone();
	}
	
	public void addTuple(MobileSoftwareComponent msc, ComputationalNode n)
	{
		variables.add(new Tuple2<MobileSoftwareComponent,ComputationalNode>(msc,n));
	}
	
	public OffloadScheduling evaluate(MobileApplication A, MobileCloudInfrastructure I)
	{
		OffloadScheduling d = new OffloadScheduling();
		
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
		for(int i = 0; i < variables.size();i++)
		{
			//We already have the pairs (MobileSoftwareComponent,ComputationalNode)
			Tuple2<MobileSoftwareComponent,ComputationalNode> t = variables.get(i);
			ComputationalNode target = t._2();
			MobileSoftwareComponent currTask = t._1();
			
			//Check capacity constraints
			if(!scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent firstScheduled = scheduledNodes.peek();
				while(firstScheduled != null && target.getESTforTask(currTask)>=firstScheduled.getRunTime())
				{
					scheduledNodes.remove(firstScheduled);
					((ComputationalNode) d.get(firstScheduled)).undeploy(firstScheduled);
					firstScheduled = scheduledNodes.peek();
				}
			}
			if(!isValid(d,currTask,target)){
				d.setRunTime(Double.MAX_VALUE);
				d.setUserCost(Double.MAX_VALUE);
				d.setBatteryLifetime(0.0);
				return d;
			}
			else
			{
				deploy(d,currTask,target);
				scheduledNodes.add(currTask);
				
			}
			
			if(OffloadingSetup.mobility)
				postTaskScheduling(d);	
		}
		return d;
	}
	
	public double getRunTime() {
		return runTime;
	}
	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}
	public double getUserCost() {
		return userCost;
	}
	public void setUserCost(double userCost) {
		this.userCost = userCost;
	}
	public double getBatteryLifetime() {
		return batteryLifetime;
	}
	public void setBatteryLifetime(double batteryLifetime) {
		this.batteryLifetime = batteryLifetime;
	}

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComputationalNode findTarget(OffloadScheduling s, MobileSoftwareComponent msc) {
		// TODO Auto-generated method stub
		return null;
	}

	public BruteForceSolution copy() {
		return new BruteForceSolution(currentApp, currentInfrastructure,variables);
	}
	
	
}
