package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningSolution;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class FirstStageAresProblem implements ConstrainedProblem<FirstStageAresSolution> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8457768991238369613L;
	private MobileCloudInfrastructure infrastructure;
	public OverallConstraintViolation<EdgePlanningSolution> overallConstraintViolationDegree = 
			new OverallConstraintViolation<EdgePlanningSolution>();
	public NumberOfViolatedConstraints<EdgePlanningSolution> numberOfViolatedConstraints =
			new NumberOfViolatedConstraints<EdgePlanningSolution>();
	
	public FirstStageAresProblem(MobileCloudInfrastructure inf)
	{
		this.infrastructure = inf;
	}
	
	@Override
	public int getNumberOfVariables() {
		// TODO Auto-generated method stub
		return SimulationSetup.MAP_M * SimulationSetup.MAP_N;
	}

	@Override
	public int getNumberOfObjectives() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ARES First Stage";
	}

	@Override
	public void evaluate(FirstStageAresSolution solution) {
		MobileCloudInfrastructure infrastructure = solution.getInfrastructure();
		
		double averageDistance, averageEnergy;
		averageDistance = computeAverageDistance(infrastructure);
		averageEnergy = computeEnergyConsumption(infrastructure);
		solution.setObjective(0, averageDistance);
		solution.setObjective(1, averageEnergy);
	}

	private double computeEnergyConsumption(MobileCloudInfrastructure infrastructure) {
		double maxTransmissionEnergy = Double.MIN_VALUE;
		MobileSoftwareComponent msc = new MobileSoftwareComponent("dummy", new Hardware(1,1,1),
				1, "anyone", 1, 1);
		for(MobileDevice dev : infrastructure.getMobileDevices().values())
		{
			double maxTransmissionDevEdge = Double.MIN_VALUE;
			double minHops = Integer.MAX_VALUE;
			
			for(EdgeNode edge : infrastructure.getEdgeNodes().values()) 
			{
				double currHops = infrastructure.getDistanceBetweenNodes(dev, edge);
				if(currHops < minHops)
				{
					minHops = currHops;
					maxTransmissionDevEdge = edge.getNetEnergyModel().computeNETEnergy(msc, dev, infrastructure);
				}
				if(currHops == minHops) 
				{
					double currDevEdgeEnergy = edge.getNetEnergyModel().computeNETEnergy(msc, dev, infrastructure);
					if(currDevEdgeEnergy > maxTransmissionDevEdge)
						maxTransmissionDevEdge = currDevEdgeEnergy;
				}
			}
			maxTransmissionEnergy += maxTransmissionDevEdge;
		}
		double instIdlePower = 0;
		for(EdgeNode edge : infrastructure.getEdgeNodes().values())
			instIdlePower += edge.getCPUEnergyModel().getIdlePower(msc, edge, infrastructure); 
		
		return maxTransmissionEnergy + instIdlePower;
	}

	private double computeAverageDistance(MobileCloudInfrastructure infrastructure) {
		double averageDistance;
		averageDistance = 0;
		MobileSoftwareComponent msc = new MobileSoftwareComponent("dummy", new Hardware(1,1,1),
				1, "anyone", 1, 1);
		for(MobileDevice dev : infrastructure.getMobileDevices().values())
		{
			double minDist = Double.MAX_VALUE;
			for(EdgeNode edge : infrastructure.getEdgeNodes().values()) 
			{
				double currDist = infrastructure.getTransmissionTime(msc, dev, edge);
				if(currDist < minDist)
					minDist = currDist;
			}
			averageDistance += minDist;
		}
		return averageDistance / infrastructure.getMobileDevices().size();
	}

	@Override
	public FirstStageAresSolution createSolution() {
		boolean wifi = true;
		infrastructure.setupEdgeNodes(SimulationSetup.edgeCoreNum,
				SimulationSetup.timezoneData,
				"random",
				wifi);
		return new FirstStageAresSolution(this.infrastructure);
	}

	@Override
	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void evaluateConstraints(FirstStageAresSolution solution) {
		// TODO Auto-generated method stub
		
	}

}
