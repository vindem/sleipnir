package at.ac.tuwien.ec.provisioning.edge.mo;



import java.util.ArrayList;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.heftbased.HEFTResearch;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;



public class EdgePlanningProblem implements Problem<EdgePlanningSolution>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3668908307776025515L;
	public OverallConstraintViolation<EdgePlanningSolution> overallConstraintViolationDegree = 
			new OverallConstraintViolation<EdgePlanningSolution>();
	public NumberOfViolatedConstraints<EdgePlanningSolution> numberOfViolatedConstraints =
			new NumberOfViolatedConstraints<EdgePlanningSolution>();
	
	private MobileCloudInfrastructure currentInfrastructure;
	private MobileApplication currentApplication;
	
	

	public EdgePlanningProblem(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		this.currentInfrastructure = I;
		this.currentApplication = A;
	}

	@Override
	public EdgePlanningSolution createSolution() {
		//boolean wifi = SimulationSetup.wifiAvailableProbability;
		boolean wifi = true;
		getCurrentInfrastructure().setupEdgeNodes(SimulationSetup.edgeCoreNum,
				SimulationSetup.timezoneData,
				"random",
				wifi);
		return new EdgePlanningSolution(getCurrentInfrastructure());
	}

	@Override
	public void evaluate(EdgePlanningSolution arg0) {
		HEFTResearch rs = new HEFTResearch(this.getCurrentApplication(),arg0.getInfrastructure());
		OffloadScheduling dep = new OffloadScheduling();
		ArrayList<OffloadScheduling> ds = (ArrayList<OffloadScheduling>) rs.findScheduling();
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
		return SimulationSetup.MAP_M * SimulationSetup.MAP_N;
	}

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
				if(!cnHardware.supports(msc.getHardwareRequirements()))
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
				//undeploy(temp,msc,cn);
			}

			boolean offloadabilityConstraintViolation = false;
			for(int i = 0; i < comps.length; i++)
			{
				MobileSoftwareComponent msc = ((MobileSoftwareComponent)comps[i]);
				ComputationalNode cn = (ComputationalNode) arg0.getOffloadScheduling().get(msc);
				if(!msc.isOffloadable() && !(cn.equals(getCurrentInfrastructure()
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

	public MobileApplication getCurrentApplication() {
		return currentApplication;
	}

	public void setCurrentApplication(MobileApplication currentApplication) {
		this.currentApplication = currentApplication;
	}

	public MobileCloudInfrastructure getCurrentInfrastructure() {
		return currentInfrastructure;
	}

	public void setCurrentInfrastructure(MobileCloudInfrastructure currentInfrastructure) {
		this.currentInfrastructure = currentInfrastructure;
	}
	
	@Override
	public int getNumberOfConstraints() {
		return 6;
	}

}
