package at.ac.tuwien.ec.provisioning.edge.mo;



import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.configurations.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;


public class EdgePlanningSolution implements PermutationSolution<Boolean>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8651522970154107582L;
	private boolean[][] edgeNodeMap;
	private MobileCloudInfrastructure I;
	private OffloadScheduling D;
	private HashMap<Object,Object> solutionAttributes;
	private double runTime = 0.0, cost = 0.0, battery = 0.0, providerCost = 0.0;
	
	public EdgePlanningSolution(MobileCloudInfrastructure I) {
		solutionAttributes = new HashMap<Object,Object>();
		this.I = I;
		edgeNodeMap = new boolean[SimulationSetup.MAP_M][SimulationSetup.MAP_N];
	}

		
	@Override
	public Solution<Boolean> copy() {
		boolean[][] targetMap = new boolean[SimulationSetup.MAP_M][SimulationSetup.MAP_N];
		for(int i = 0; i < edgeNodeMap.length; i++)
			System.arraycopy(edgeNodeMap[i], 0, targetMap[i], 0, edgeNodeMap[i].length);
		
		return new EdgePlanningSolution(I);
	
	}

	@Override
	public Object getAttribute(Object arg0) {
		return solutionAttributes.get(arg0);
	}

	@Override
	public int getNumberOfObjectives() {
		return 4;
	}

	@Override
	public int getNumberOfVariables() {
		return SimulationSetup.MAP_M * SimulationSetup.MAP_N;
	}

	@Override
	public double getObjective(int arg0) {
		switch(arg0)
		{
		case 0: return runTime;
		case 1: return cost;
		case 2: return battery;
		case 3: return providerCost;
		default: return runTime;
		}
	}

	public Boolean getVariableValue(int arg0) {
		int i,j;
		i = arg0 / SimulationSetup.MAP_N;
		j = arg0 % SimulationSetup.MAP_N;
		return edgeNodeMap[i][j];
	}

	public String getVariableValueString(int arg0) {
		int i,j;
		i = arg0 / SimulationSetup.MAP_N;
		j = arg0 % SimulationSetup.MAP_N;
		return Boolean.toString(edgeNodeMap[i][j]);
	}

	@Override
	public void setAttribute(Object arg0, Object arg1) {
		solutionAttributes.put(arg0, arg1);
	}

	@Override
	public void setObjective(int arg0, double arg1) {
		switch(arg0)
		{
		case 0: D.setRunTime(arg1);
				runTime = arg1;
				break;
		case 1: D.setUserCost(arg1);
				cost = arg1;
				break;
		case 2: D.setBatteryLifetime(arg1);
				battery = OffloadingSetup.batteryCapacity - arg1;
				break;
		case 3: D.setProviderCost(arg1);
				providerCost = arg1;
		}		
	}

	public void setVariableValue(int arg0, Boolean arg1) {
		int i,j;
		i = arg0 / SimulationSetup.MAP_N;
		j = arg0 % SimulationSetup.MAP_N;
		edgeNodeMap[i][j] = arg1;
		I.setupEdgeNodes(SimulationSetup.edgeCoreNum,
				SimulationSetup.timezoneData,
				edgeNodeMap);
	}

	public void setMap(boolean[][] map){
		this.edgeNodeMap = map;
	}
	
	public boolean[][] getMap(){
		return edgeNodeMap;
	}


	public OffloadScheduling getOffloadScheduling() {
		return D;
	}


	public void setOffloadScheduling(OffloadScheduling d2) {
		this.D = d2;
	}


	public MobileCloudInfrastructure getInfrastructure() {
		return I;
	}


	public double[] getObjectives() {
		// TODO Auto-generated method stub
		return null;
	}


	public Map<Object, Object> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}


	public double getConstraint(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public double[] getConstraints() {
		// TODO Auto-generated method stub
		return null;
	}


	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}


	public Boolean getVariable(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public List<Boolean> getVariables() {
		// TODO Auto-generated method stub
		return null;
	}


	public boolean hasAttribute(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	public void setConstraint(int arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}


	public void setVariable(int arg0, Boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
