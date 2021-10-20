package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.scheduling.algorithms.multiobjective.RandomScheduler;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;



public class DeploymentProblem extends OffloadScheduler implements Problem<DeploymentSolution>
{
	/**
	 * 
	 */
	public OverallConstraintViolation<DeploymentSolution> overallConstraintViolationDegree = 
			new OverallConstraintViolation<DeploymentSolution>();
	public NumberOfViolatedConstraints<DeploymentSolution> numberOfViolatedConstraints =
			new NumberOfViolatedConstraints<DeploymentSolution>();
	
	private static final long serialVersionUID = 1L;
		
	
	public DeploymentProblem(MobileCloudInfrastructure I, MobileApplication A)
	{
		super();
		setMobileApplication(A);
		setInfrastructure(I);
	}
	
	@Override
	public DeploymentSolution createSolution() {
		DeploymentSolution sol =  new DeploymentSolution(this.currentApp,this.currentInfrastructure);
		sol.randomInitialize();
		
		return sol;
	}

	@Override
	public void evaluate(DeploymentSolution currDep) {
		OffloadScheduling d = new OffloadScheduling();
		
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
		int i = 0;
		while(i < currDep.getNumberOfVariables())
		{
			//We already have the pairs (MobileSoftwareComponent,ComputationalNode)
			Tuple2<MobileSoftwareComponent,ComputationalNode> t = currDep.getVariableValue(i);
			ComputationalNode target = t._2();
			MobileSoftwareComponent currTask = t._1();
			
			deploy(d,currTask,target);
			scheduledNodes.add(currTask);
			i++;
			
			
			if(OffloadingSetup.mobility)
				postTaskScheduling(d);	
		}
		currDep.setDeployment(d);
		currDep.setObjective(0, d.getRunTime());
		currDep.setObjective(1, d.getUserCost());
		currDep.setObjective(2, d.getBatteryLifetime());
		
	}

	public void evaluateConstraints(DeploymentSolution arg0) {
		int violatedConstraints = 0;
		double overAllConstraintViolation = 0.0;

		/*

		boolean rankConstraintViolation = false;
		for(int i = 0; i < comps.length - 1; i++)
		{
			if(((MobileSoftwareComponent)comps[i]).getRank() < ((MobileSoftwareComponent)comps[i+1]).getRank())
			{
				if(!rankConstraintViolation){
					rankConstraintViolation = true;
					violatedConstraints++;
				}
				overAllConstraintViolation -= 10;
				break;
			}
		}
		
		OffloadScheduling temp = new OffloadScheduling();
		boolean hardwareConstraintViolation = false;
		for(int i = 0; i < arg0.getNumberOfVariables(); i++)
		{
			ComputationalNode cn = arg0.getVariableValue(i);
			MobileSoftwareComponent msc = (MobileSoftwareComponent) comps[i];
			
			HardwareCapabilities cnHardware = cn.getCapabilities();
			if(!isValid(temp,msc,cn))
			{
				if(!hardwareConstraintViolation)
				{
					hardwareConstraintViolation = true;
					violatedConstraints++;					
				}
				overAllConstraintViolation += cn.getCapabilities().getAvailableCores() -
						msc.getHardwareRequirements().getCores();
				break;
			}
			//else
				//deploy(temp,msc,cn);
		}
		
		for(int i = 0; i < arg0.getNumberOfVariables(); i++)
		{
			ComputationalNode cn = arg0.getVariableValue(i);
			MobileSoftwareComponent msc = (MobileSoftwareComponent) comps[i];
			undeploy(temp,msc,cn);
		}
		
		boolean offloadabilityConstraintViolation = false;
		for(int i = 0; i < arg0.getNumberOfVariables(); i++)
		{
			MobileSoftwareComponent msc = ((MobileSoftwareComponent)comps[i]);
			ComputationalNode cn = (ComputationalNode) arg0.getDeployment().get(msc);
			if(!msc.isOffloadable() && !(cn.equals(currentInfrastructure.getNodeById(msc.getUserId()))))
			{
				if(!offloadabilityConstraintViolation)
				{
				violatedConstraints++;
				offloadabilityConstraintViolation = true;
				}
				overAllConstraintViolation -= 100.0;
			}
		}
		
		if(arg0.getDeployment().getBatteryLifetime() < 0.0)
		{
			violatedConstraints++;
			overAllConstraintViolation = arg0.getDeployment().getBatteryLifetime();
		}
		
		if(!(Double.isFinite(arg0.getObjective(0)))
				|| !Double.isFinite(arg0.getObjective(1))
				|| !Double.isFinite(arg0.getObjective(2)))
		{
			violatedConstraints++;
		}
		numberOfViolatedConstraints.setAttribute(arg0, violatedConstraints);
		overallConstraintViolationDegree.setAttribute(arg0,overAllConstraintViolation);
		*/
	}

	@Override
	public String getName() {
		return "FirstHopDagOffloading";
	}

	@Override
	public int getNumberOfObjectives() {
		return 3;
	}

	@Override
	public int getNumberOfVariables() {
		// TODO Auto-generated method stub
		return currentApp.getComponentNum();
	}

	@Override
	public int getNumberOfConstraints() {
		return 6;
	}

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ComputationalNode findTarget(OffloadScheduling d, MobileSoftwareComponent c)
	{
		return null;
	}
	
}
