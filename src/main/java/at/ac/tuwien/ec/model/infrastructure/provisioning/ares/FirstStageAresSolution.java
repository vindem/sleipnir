package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import java.util.HashMap;

import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningSolution;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class FirstStageAresSolution implements PermutationSolution<Boolean>{
	
	private MobileCloudInfrastructure infrastructure;
	private boolean[][] edgeNodeMap;
	private HashMap<Object,Object> solutionAttributes;
	private double averageDistance;
	private double energyConsumption;

	public FirstStageAresSolution(MobileCloudInfrastructure infrastructure) {
		this.infrastructure = infrastructure;
		edgeNodeMap = new boolean[SimulationSetup.MAP_M][SimulationSetup.MAP_N];
		solutionAttributes = new HashMap<Object,Object>();
	}

	@Override
	public void setObjective(int index, double value) {
		switch(index)
		{
			case 0: this.averageDistance = value;
				break;
			case 1: this.energyConsumption = value;
				break;
		}
		
	}

	public MobileCloudInfrastructure getInfrastructure()
	{
		return infrastructure;
	}
	
	@Override
	public double getObjective(int index) {
		switch(index)
		{
		case 0: return this.averageDistance;
		case 1: return this.energyConsumption;
		default: return Double.MAX_VALUE;
		}
	}

	@Override
	public double[] getObjectives() {
		double[] objectives = new double[2];
		objectives[0] = this.averageDistance;
		objectives[1] = this.energyConsumption;
		return objectives;
	}

	@Override
	public Boolean getVariableValue(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVariableValue(int index, Boolean value) {
		int i,j;
		i = index / SimulationSetup.MAP_N;
		j = index % SimulationSetup.MAP_N;
		edgeNodeMap[i][j] = value;
		if(value)
			EdgePlanner.addEdgeNodeAt(infrastructure, i, j);
		else
			EdgePlanner.removeEdgeNodeAt(infrastructure,i,j);
			
	}

	@Override
	public String getVariableValueString(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfVariables() {
		return SimulationSetup.MAP_M * SimulationSetup.MAP_N;
	}

	@Override
	public int getNumberOfObjectives() {
		return 2;
	}

	@Override
	public Solution<Boolean> copy() {
		boolean[][] targetMap = new boolean[SimulationSetup.MAP_M][SimulationSetup.MAP_N];
		for(int i = 0; i < edgeNodeMap.length; i++)
			System.arraycopy(edgeNodeMap[i], 0, targetMap[i], 0, edgeNodeMap[i].length);
		
		return new FirstStageAresSolution(this.infrastructure);
	}
	

	@Override
	public void setAttribute(Object id, Object value) {
		solutionAttributes.put(id, value);		
	}

	@Override
	public Object getAttribute(Object id) {
		// TODO Auto-generated method stub
		return solutionAttributes.get(id);
	}

}
