package at.ac.tuwien.ec.model.infrastructure.planning.edge.mo;



import java.util.ArrayList;

import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;



public class EdgePlanningProblem extends OffloadScheduler implements ConstrainedProblem<EdgePlanningSolution>{

	public OverallConstraintViolation<EdgePlanningSolution> overallConstraintViolationDegree = 
			new OverallConstraintViolation<EdgePlanningSolution>();
	public NumberOfViolatedConstraints<EdgePlanningSolution> numberOfViolatedConstraints =
			new NumberOfViolatedConstraints<EdgePlanningSolution>();
	
	public EdgePlanningProblem(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
	}

	@Override
	public EdgePlanningSolution createSolution() {
		//boolean wifi = SimulationSetup.wifiAvailableProbability;
		boolean wifi = true;
		getInfrastructure().setupEdgeNodes(SimulationSetup.edgeCoreNum,
				SimulationSetup.timezoneData,
				"random",
				wifi);
		return new EdgePlanningSolution(getInfrastructure());
	}

	@Override
	public void evaluate(EdgePlanningSolution arg0) {
		HEFTResearch rs = new HEFTResearch(this.getMobileApplication(),arg0.getInfrastructure());
		OffloadScheduling dep = new OffloadScheduling();
		ArrayList<OffloadScheduling> ds = rs.findScheduling();
		if(ds == null)
		{
			arg0.setOffloadScheduling(dep);
			arg0.setObjective(0, Double.MAX_VALUE);
			arg0.setObjective(1, Double.MAX_VALUE);
			arg0.setObjective(2, Double.MIN_VALUE);
			arg0.setObjective(3, Double.MAX_VALUE);
		}
		else
		{
			arg0.setOffloadScheduling(ds.get(0));
			arg0.setObjective(0, ds.get(0).getRunTime());
			arg0.setObjective(1, ds.get(0).getUserCost());
			arg0.setObjective(2, ds.get(0).getBatteryLifetime());
			arg0.setObjective(3, ds.get(0).getProviderCost());
		}
	}

	@Override
	public String getName() {
		return "MOEdgePlanning";
	}

	@Override
	public int getNumberOfObjectives() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public int getNumberOfVariables() {
		// TODO Auto-generated method stub
		return getInfrastructure().getEdgeNodes().values().size();
	}

	@Override
	public void evaluateConstraints(EdgePlanningSolution arg0) {
		int violatedConstraints = 0;
		double overAllConstraintViolation = 0.0;

		Object[] comps = arg0.getOffloadScheduling().keySet().toArray();

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
		if(comps.length > 0)
		{
			for(int i = 0; i < comps.length; i++)
			{
				MobileSoftwareComponent msc = (MobileSoftwareComponent) comps[i];
				ComputationalNode cn = (ComputationalNode) arg0.getOffloadScheduling().get(msc);

				HardwareCapabilities cnHardware = cn.getCapabilities();
				if(!isValid(temp,msc,cn))
				{
					if(!hardwareConstraintViolation)
					{
						hardwareConstraintViolation = true;
						violatedConstraints++;					
					}
					overAllConstraintViolation += cn.getCapabilities().getAvailableCores() 
							- msc.getHardwareRequirements().getCores();
					break;
				}
				//else
				//deploy(temp,msc,cn);
			}

			for(int i = 0; i < comps.length; i++)
			{
				MobileSoftwareComponent msc = (MobileSoftwareComponent) comps[i];
				ComputationalNode cn = (ComputationalNode) arg0.getOffloadScheduling().get(msc);
				undeploy(temp,msc,cn);
			}

			boolean offloadabilityConstraintViolation = false;
			for(int i = 0; i < comps.length; i++)
			{
				MobileSoftwareComponent msc = ((MobileSoftwareComponent)comps[i]);
				ComputationalNode cn = (ComputationalNode) arg0.getOffloadScheduling().get(msc);
				if(!msc.isOffloadable() && !(cn.equals(getInfrastructure()
						.getMobileDevices().get(msc.getUserId()))))
				{
					if(!offloadabilityConstraintViolation)
					{
						violatedConstraints++;
						offloadabilityConstraintViolation = true;
					}
					overAllConstraintViolation -= 100.0;
				}
			}

			if(arg0.getOffloadScheduling().getBatteryLifetime() < 0.0)
			{
				violatedConstraints++;
				overAllConstraintViolation = arg0.getOffloadScheduling().getBatteryLifetime();
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
		else
		{
			numberOfViolatedConstraints.setAttribute(arg0, 6);
			overallConstraintViolationDegree.setAttribute(arg0,Double.MAX_VALUE);
		} 
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
	

}
