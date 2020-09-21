package at.ac.tuwien.ec.model.availability;

import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 * This class is used to model constant availability during all the time of the simulation.
 * @author vincenzo
 *
 */
public class ConstantAvailabilityModel extends AvailabilityModel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6131317981776181272L;
	private UniformRealDistribution uniformDistribution = new UniformRealDistribution(0.0, 1.0);

	/**
	 * 
	 * @param availabilityLevel availability level for the node
	 */
	public ConstantAvailabilityModel(double availabilityLevel)
	{	
		super(availabilityLevel);
	}
		
	@Override
	/**
	 * @param runtime the current runtime (Not used for computing availability, in this case)
	 * 
	 * To decide if node is available or not, we generate a random value between 0-1 and check
	 * whether is less or equal than our constant availability level. The random value is generated
	 * using the sample() method in org.apache.commons.math3.distribution.UniformRealDistribution.
	 */
	public boolean isAvailableAt(double runtime) {
		return uniformDistribution.sample() <= availabilityLevel;
	}
	@Override
	/*
	 * (non-Javadoc)
	 * @see at.ac.tuwien.ec.model.availability.AvailabilityModel#availabilityAt(double)
	 */
	public double availabilityAt(double runtime) {
		// Availability is constant, therefore we just return the availability level
		return this.availabilityLevel;
	}

}
