package at.ac.tuwien.ec.scheduling;

import java.util.LinkedHashMap;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class Scheduling extends LinkedHashMap<MobileSoftwareComponent, ComputationalNode> {
	
	public Scheduling()
	{
		super();
	}
	
	public Scheduling(Scheduling s)
	{
		super(s);
	}

}
