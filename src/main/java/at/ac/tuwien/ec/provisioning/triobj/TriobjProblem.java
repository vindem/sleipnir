package at.ac.tuwien.ec.provisioning.triobj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.math.RandomUtils;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.provisioning.DefaultCloudPlanner;
import at.ac.tuwien.ec.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.provisioning.edge.mo.EdgePlanningSolution;
import at.ac.tuwien.ec.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.sleipnir.ListBasedIoTPlanner;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class TriobjProblem implements Problem<TriobjSolution> {
	
	protected static int MAP_M = SimulationSetup.MAP_M;
	protected static int MAP_N = SimulationSetup.MAP_N;
	static final double MILLISECONDS_PER_SECONDS = 1000.0;
	static final double BYTES_PER_MEGABIT = 125000.0;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8457768991238369613L;
	
	public OverallConstraintViolation<EdgePlanningSolution> overallConstraintViolationDegree = 
			new OverallConstraintViolation<EdgePlanningSolution>();
	public NumberOfViolatedConstraints<EdgePlanningSolution> numberOfViolatedConstraints =
			new NumberOfViolatedConstraints<EdgePlanningSolution>();
	
		
	public TriobjProblem()
	{
		super();		
	}
	
	@Override
	public int getNumberOfVariables() {
		// TODO Auto-generated method stub
		if(SimulationSetup.admissibleEdgeCoordinates != null)
			return SimulationSetup.admissibleEdgeCoordinates.size();
		return SimulationSetup.edgeNodeLimit;
	}

	@Override
	public int getNumberOfObjectives() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ARES First Stage";
	}

	public void evaluate(TriobjSolution solution) {
		
		//DefaultNetworkPlanner.setupNetworkConnections(infrastructure);
		
		double maxMinDistance = 0.0, totalEnergy = 0.0;
		BigDecimal failureProb = new BigDecimal(1.0);
		
		maxMinDistance = computeMaxMinDistance(solution);
		totalEnergy = computeEnergyConsumption(solution);
		failureProb = computeFailureProb(solution);
		
		solution.setObjective(0, maxMinDistance);
		solution.setObjective(1, totalEnergy);
		solution.setObjective(2, failureProb.doubleValue());
	}

	public static double computeEnergyConsumption(TriobjSolution sol) {
		//todo: include active energy
		double idleEnergy = 0.0;
		
		double maxMinDist = computeMaxMinDistance(sol);
		double lifeTime = 60.0*60.0;
		double latency = DefaultNetworkPlanner.qosUL.getLatency();
		double bandwidth = DefaultNetworkPlanner.qosUL.getBandwidth();
		double activeEnergy = 0.0;
		for(Coordinates iotCoords : SimulationSetup.iotDevicesCoordinates)
		{
			double minDist = Double.MAX_VALUE;
			int index = 0;
			for(Coordinates edgeCoords : SimulationSetup.admissibleEdgeCoordinates) 
			{
				if(sol.getVariableValue(index)) {
					double currDist = computeTransmissionTime(iotCoords,edgeCoords);
					if(Double.isFinite(currDist) && Double.compare(currDist, minDist) < 0)
						minDist = currDist;
				}
				index++;
			}
			activeEnergy += (minDist * latency * 2)
					* (0.025e-3 * bandwidth + 3.5e-3);
			idleEnergy += (maxMinDist - minDist) * 3.5e-3;
		}
		
		for(int i = 0; i < sol.getNumberOfVariables(); i++)
			if(sol.getVariableValue(i))
				idleEnergy += 804.0 * lifeTime;
		
		
		return idleEnergy + activeEnergy;
	}

	public static double computeMaxMinDistance(TriobjSolution sol) {
		double maxMinDist = Double.MIN_VALUE;
		for(Coordinates iotCoords : SimulationSetup.iotDevicesCoordinates)
		{
			double minDist = Double.MAX_VALUE;
			int index = 0;
			for(Coordinates edgeCoords : SimulationSetup.admissibleEdgeCoordinates) 
			{
				if(index >= SimulationSetup.edgeNodeLimit)
					break;
				if(sol.getVariableValue(index)) {
					double currDist = computeTransmissionTime(iotCoords,edgeCoords);
					if(Double.isFinite(currDist) && Double.compare(currDist, minDist) < 0)
						minDist = currDist;
				}
				index++;
			}
			if(Double.compare(minDist, maxMinDist) > 0)
				maxMinDist = minDist;
		}
		//return averageDistance / infrastructure.getIotDevices().size();
		return maxMinDist;
	}


	private static double computeTransmissionTime(Coordinates iotCoords, Coordinates edgeCoords) {
		double iotX = getXCoord(iotCoords.getLatitude());
		double iotY = getYCoord(iotCoords.getLongitude());
		double edgeX = getXCoord(edgeCoords.getLatitude());
		double edgeY = getYCoord(edgeCoords.getLongitude());
		
		double distance = Math.abs(iotX - edgeX) + Math.max(0,(Math.abs(iotX-edgeX)- Math.abs(iotY-edgeY) )/2.0);
		
		double latency = DefaultNetworkPlanner.qosUL.getLatency();
		double bandwidth = DefaultNetworkPlanner.qosUL.getBandwidth();
		
		double transmissionTime = 1/(bandwidth * BYTES_PER_MEGABIT) + (latency*distance)/MILLISECONDS_PER_SECONDS;
		return transmissionTime;
	}

	private static int getYCoord(double longitude) {
		double min = 48.12426368;
		double max = 48.30119579;
		double cellNIndex = ((longitude - min)/(max-min))*(SimulationSetup.MAP_N);  
		return (int) cellNIndex;
	}

	private static int getXCoord(double latitude) {
		double min = 16.21259754;
		double max = 16.52969867;
		double cellMIndex = ((latitude - min)/(max-min))*(SimulationSetup.MAP_M);  
		return (int) cellMIndex;
	}

	@Override
	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 2;
	}

	public void evaluateConstraints(TriobjSolution solution) {
				
	}

	@Override
	public TriobjSolution createSolution() {
		Random rand = new Random();
		BitSet bs = new BitSet(SimulationSetup.edgeNodeLimit);
		for(int i = 0; i < bs.size(); i++)
			bs.set(i,rand.nextBoolean());
		
		return new TriobjSolution(bs);
	}

	public static BigDecimal computeFailureProb(TriobjSolution triobjSolution) {
		BigDecimal failureProb = new BigDecimal(1.0);
		for(int i = 0; i < triobjSolution.getNumberOfVariables(); i++)
		{
			if(triobjSolution.getVariableValue(i)) 
				failureProb = failureProb.multiply(new BigDecimal(SimulationSetup.failureProbList.get(i)));
			
		}
		return failureProb;
	}

	

	

}
