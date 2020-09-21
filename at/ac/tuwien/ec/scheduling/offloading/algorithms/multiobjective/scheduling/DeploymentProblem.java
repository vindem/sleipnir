package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling;

import java.util.ArrayList;
import java.util.Collections;

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
import at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.RandomScheduler;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;



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
		OffloadScheduling dep = new OffloadScheduling();
		RandomScheduler rs = new RandomScheduler(this.currentApp,this.currentInfrastructure);
		ArrayList<OffloadScheduling> deps;
		do deps = rs.findScheduling();
		while(deps.size()==0);
		try {
			return new DeploymentSolution(deps.get(0),this.currentApp,this.currentInfrastructure);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void evaluate(DeploymentSolution currDep) {
		double cost = 0;
		double batteryBudget = SimulationSetup.batteryCapacity;
		OffloadScheduling d = currDep.getDeployment();
		double runTime = 0.0;
		double providerCost = 0.0;
		for(int i = 0; i < d.keySet().size(); i++){
			MobileSoftwareComponent sc = (MobileSoftwareComponent) d.keySet().toArray()[i];
			ComputationalNode n = currDep.getVariableValue(i);
			ArrayList<MobileSoftwareComponent> preds = currentApp.getPredecessors(sc);
			double mPreds = 0.0;
			for(MobileSoftwareComponent ps:preds)
			{
				if(ps.getRunTime() > mPreds)
					mPreds = ps.getRunTime();
			}
			sc.setRunTime(mPreds + sc.getRuntimeOnNode(n, currentInfrastructure));
			runTime = sc.getRunTime();
			cost += n.computeCost(sc, 
					(MobileDevice) currentInfrastructure.getNodeById(sc.getUserId()) ,
					currentInfrastructure);
			batteryBudget -= (currentInfrastructure.getMobileDevices().containsValue(n))? 
					((ComputationalNode) currentInfrastructure.getNodeById(sc.getUserId())).getCPUEnergyModel().computeCPUEnergy(sc, n, currentInfrastructure):
					currentInfrastructure.getNodeById(sc.getUserId()).getNetEnergyModel().computeNETEnergy(sc, n, currentInfrastructure);	
			if(!currentInfrastructure.getMobileDevices().containsValue(n))
				providerCost += n.getCPUEnergyModel().computeCPUEnergy(sc, n, currentInfrastructure) * 
					currentInfrastructure.getPriceForLocation(n.getCoords(), runTime);
			for(CloudDataCenter c : currentInfrastructure.getCloudNodes().values() )
				if(!c.getId().equals(n.getId()))
					providerCost += n.getCPUEnergyModel().getIdlePower(sc, c, currentInfrastructure) * 
						currentInfrastructure.getPriceForLocation(c.getCoords(), runTime);
			for(EdgeNode fn : currentInfrastructure.getEdgeNodes().values())
				if(!fn.getId().equals(n.getId()))
					providerCost += n.getCPUEnergyModel().getIdlePower(sc, fn, currentInfrastructure) * 
						currentInfrastructure.getPriceForLocation(fn.getCoords(), runTime);
		}
		currDep.setObjective(0, runTime);
		currDep.setObjective(1, cost);
		currDep.setObjective(2, batteryBudget);
		currDep.setObjective(3, providerCost);
	}

	public void evaluateConstraints(DeploymentSolution arg0) {
		int violatedConstraints = 0;
		double overAllConstraintViolation = 0.0;

		Object[] comps = arg0.getDeployment().keySet().toArray();

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
	}

	@Override
	public String getName() {
		return "FirstHopDagOffloading";
	}

	@Override
	public int getNumberOfObjectives() {
		return 4;
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

	protected boolean isValid(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
        return n.isCompatible(s) && isOffloadPossibleOn(s, n)
        		&& checkLinks(deployment,s,n);
    }

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
