package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.solution.Solution;

import at.ac.tuwien.ec.sleipnir.SimulationSetup;


public class FirstStageAresSolution implements Solution<Boolean>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2327581162456517813L;

	public double getAverageDistance() {
		return averageDistance;
	}

	public double getEnergyConsumption() {
		return energyConsumption;
	}

	private BitSet edgeNodeMap;
	
	public BitSet getEdgeNodeMap() {
		return edgeNodeMap;
	}
	
	private HashMap<Object,Object> solutionAttributes;
	private double averageDistance;
	private double energyConsumption, activeEnergy, idleEnergy;
	
	public double getActiveEnergy() {
		return activeEnergy;
	}

	public void setActiveEnergy(double activeEnergy) {
		this.activeEnergy = activeEnergy;
	}

	public FirstStageAresSolution() {
		super();
		solutionAttributes = new HashMap<Object,Object>();
		edgeNodeMap = new BitSet(SimulationSetup.admissibleEdgeCoordinates.size());
	}
	
	public FirstStageAresSolution(BitSet bs)
	{
		super();
		solutionAttributes = new HashMap<Object,Object>();
		edgeNodeMap = bs;
	}

	public FirstStageAresSolution(String tmp) {
		double[] energy;
		tmp = tmp.replace('[',' ');
		tmp = tmp.replace(']',' ');
		tmp = tmp.trim();
		String[] assignment = tmp.split(", ");
		edgeNodeMap = new BitSet(SimulationSetup.admissibleEdgeCoordinates.size());
		for(int i = 0; i < assignment.length; i++)
		{
			int idx = Integer.parseInt(assignment[i]);
			edgeNodeMap.set(idx-1,true);
		}
		solutionAttributes = new HashMap<Object,Object>();
		this.averageDistance = FirstStageAresProblem.computeMaxMinDistance(this);
		energy = FirstStageAresProblem.computeEnergyConsumption(this);
		this.idleEnergy = energy[0];
		this.activeEnergy = energy[1];
		this.energyConsumption = this.idleEnergy + this.activeEnergy;
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

	@Override
	public double getObjective(int index) {
		switch(index)
		{
		case 0: return this.averageDistance;
		case 1: return this.energyConsumption;
		default: return 0.0;
		}
	}

	@Override
	public double[] getObjectives() {
		double[] objectives = new double[2];
		objectives[0] = this.averageDistance;
		objectives[1] = this.energyConsumption;
		return objectives;
	}

	
	public Boolean getVariableValue(int index) {
		return edgeNodeMap.get(index);
	}

	
	public void setVariableValue(int index, Boolean value) {
		edgeNodeMap.set(index, value);
	}

	

	@Override
	public int getNumberOfVariables() {
		return SimulationSetup.admissibleEdgeCoordinates.size();
	}

	@Override
	public int getNumberOfObjectives() {
		return 2;
	}

	@Override
	public Solution<Boolean> copy() {
		BitSet bs = (BitSet) this.edgeNodeMap.clone();
		return new FirstStageAresSolution(bs);
		
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

	public String toString()
	{
		String solution = "";
		int cnt = 0;
		for(int i = 0; i < edgeNodeMap.size() && cnt < edgeNodeMap.cardinality(); i++)
		{
			if(edgeNodeMap.get(i))
			{
				solution += (i+1);
				cnt++;
				if(cnt < edgeNodeMap.cardinality())
					solution+=",";
			}
		}
		solution+="\n";
		
		solution += "Edge nodes used: "+edgeNodeMap.cardinality()+"/"+SimulationSetup.admissibleEdgeCoordinates.size()+
				"; Max Min Distance= "+this.averageDistance+"; Total Energy Consumption= "
					+this.energyConsumption + " Average active energy" + this.activeEnergy;
		
		return solution;
	}

	public String getVariableValueString(int index) {
		return ""+edgeNodeMap.get(index);
	}

	@Override
	public boolean equals(Object in)
	{
		FirstStageAresSolution sol = (FirstStageAresSolution) in;
		for(int i = 0; i < sol.getNumberOfVariables(); i++)
		{
			if(this.getVariableValue(i) != sol.getVariableValue(i))
				return false;
		}
		return true;
	}

	@Override
	public Boolean getVariable(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Boolean> getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVariable(int index, Boolean variable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] getConstraints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getConstraint(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setConstraint(int index, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasAttribute(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<Object, Object> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
