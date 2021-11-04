package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.configurations.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;
import scala.Tuple2;



public class DeploymentSolution implements PermutationSolution<Tuple2<MobileSoftwareComponent,ComputationalNode>>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<Object,Object> solutionAttributes;
	private MobileApplication A;
	private MobileCloudInfrastructure I;
	private double runTime = 0.0, cost = 0.0, battery = 0.0;
	private ArrayList<Tuple2<MobileSoftwareComponent,ComputationalNode>> variables; 
	private OffloadScheduling deployment;
	
	public DeploymentSolution(MobileApplication A, MobileCloudInfrastructure I)
	{
		this.A = (MobileApplication) A;
		this.I = (MobileCloudInfrastructure) I;
		variables = new ArrayList<Tuple2<MobileSoftwareComponent,ComputationalNode>>();
		solutionAttributes = new HashMap<Object,Object>();

	}
	public DeploymentSolution(OffloadScheduling deployment, MobileApplication A, MobileCloudInfrastructure I)
	{
		this.A = (MobileApplication) A;
		this.I = (MobileCloudInfrastructure) I;
		ArrayList<SoftwareComponent> components = new ArrayList<SoftwareComponent>();
		ArrayList<NetworkedNode> nodes = new ArrayList<NetworkedNode>();
		variables = new ArrayList<Tuple2<MobileSoftwareComponent,ComputationalNode>>();
		for(SoftwareComponent s: deployment.keySet())
			components.add(s);
		for(NetworkedNode n: deployment.values())
			nodes.add(n);
		for(int i = 0; i < components.size(); i++) 
			setVariableValue(i,new Tuple2<MobileSoftwareComponent,ComputationalNode>((MobileSoftwareComponent)components.get(i),(ComputationalNode) nodes.get(i)));
		
		solutionAttributes = new HashMap<Object,Object>();
		
	}
	
	@Override
	public Object getAttribute(Object arg0) {
		return solutionAttributes.get(arg0);
	}
	@Override
	public int getNumberOfObjectives() {
		return 3;
	}
	@Override
	public int getNumberOfVariables() {
		return variables.size();
	}
	@Override
	public double getObjective(int arg0) {
		switch(arg0)
		{
		case 0: return runTime;
		case 1: return cost;
		case 2: return battery;
		default: return runTime;
		}
	}
	
	public String getVariableValueString(int arg0) {
		return variables.get(arg0)._2().toString();
	}
	@Override
	public void setAttribute(Object arg0, Object arg1) {
		solutionAttributes.put(arg0, arg1);
	}
	
	@Override
	public void setObjective(int arg0, double arg1) {
		switch(arg0)
		{
		case 0: runTime = arg1;
				break;
		case 1: cost = arg1;
				break;
		case 2: battery = OffloadingSetup.batteryCapacity - arg1;
				break;
		}		
	}
	
	public Tuple2<MobileSoftwareComponent,ComputationalNode> getVariableValue(int arg0) {
		return variables.get(arg0);
	}

	public void setVariableValue(int index,Tuple2<MobileSoftwareComponent,ComputationalNode> t)
	{
		if(index < variables.size())
			variables.set(index,t);
		else
			variables.add(index,t);
	}
	
	public void setVariableValue(int index, MobileSoftwareComponent msc ,ComputationalNode node) {
		if(index < variables.size())
			variables.set(index,new Tuple2<MobileSoftwareComponent,ComputationalNode>(msc,node));
		else
			variables.add(index,new Tuple2<MobileSoftwareComponent,ComputationalNode>(msc,node));
	}

	@Override
	public DeploymentSolution copy() {
		DeploymentSolution s;
		s = new DeploymentSolution(A,I);
		int i = 0;
		for(Tuple2<MobileSoftwareComponent,ComputationalNode> t : variables)
		{
			s.setVariableValue(i,t._1(),t._2());
			i++;
		}
		s.setDeployment(this.deployment);
		return s;
	}

	public String toString(){
		String str = "";
		for(Tuple2<MobileSoftwareComponent,ComputationalNode> t : variables)
			str += "(" + t._1() +"," + t._2() + ")";
		return str;
	}
	
   	public MobileApplication getApplication() {
		return A;
	}

	public MobileCloudInfrastructure getInfrastructure() {
		return I;
	}
	
	public Number getLowerBound(int index) {
		return 0.0;
	}
	public OffloadScheduling getDeployment() {
		return this.deployment;
	}
	public void setDeployment(OffloadScheduling deployment) {
		this.deployment = deployment;
	}
	public void randomInitialize() {
		TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink> iter 
			= new TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink>(A.taskDependencies);
		int i = 0;
		while(iter.hasNext())
		{
			MobileSoftwareComponent currTask = iter.next();
			if(!currTask.isOffloadable())
				setVariableValue(i,currTask,(ComputationalNode) I.getNodeById(currTask.getUserId()));
			else 
			{
				ArrayList<ComputationalNode> targetList = new ArrayList<ComputationalNode>();
				if(SimulationSetup.cloudOnly)
					targetList.addAll(I.getCloudNodes().values());
				else
					targetList = I.getAllNodes();
				int idx = RandomUtils.nextInt(targetList.size());
				ComputationalNode target = targetList.get(idx);
				setVariableValue(i,currTask,target);
			}
			i++;
		}
	}
	

	

	

		
}
