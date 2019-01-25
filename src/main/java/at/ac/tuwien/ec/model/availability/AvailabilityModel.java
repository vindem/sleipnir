package at.ac.tuwien.ec.model.availability;

/**
 * 
 * @author vincenzo
 * This abstract class is used as reference implementation for availability models. All availability models MUST
 * implement this class.
 */
public abstract class AvailabilityModel {
	
	protected double availabilityLevel;
	
	/**
	 * 
	 * @param availabilityLevel: double between 0.0-1.0, modelling availability of physical nodes.
	 */
	public AvailabilityModel(double availabilityLevel)
	{
		setAvailabilityLevel(availabilityLevel);
	}
	
	/**
	 * 
	 * @param runtime: the runtime at which we want to know availability of the node.
	 * @return true if physical node is available at the given timestep
	 */
	public abstract boolean isAvailableAt(double runtime);
	
	/**
	 * 
	 * @param runtime: the runtime at which we want to know availability of the node.
	 * @return availability of the node at given runtime.
	 */
	public abstract double availabilityAt(double runtime);

	public double getAvailabilityLevel() {
		return availabilityLevel;
	}

	public void setAvailabilityLevel(double availabilityLevel) {
		this.availabilityLevel = availabilityLevel;
	}

}
