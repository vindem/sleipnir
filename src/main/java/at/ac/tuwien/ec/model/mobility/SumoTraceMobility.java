package at.ac.tuwien.ec.model.mobility;

import java.util.ArrayList;

import at.ac.tuwien.ec.model.Coordinates;

public class SumoTraceMobility {
	
	private ArrayList<Coordinates> trace;
	
	public SumoTraceMobility(ArrayList<Coordinates> trace)
	{
		this.trace = trace;
	} 

	public Coordinates getCoordinatesForTimestep(double timestep) {
		int index = (int) Math.floor(timestep);
		return trace.get(index);
	}

}
