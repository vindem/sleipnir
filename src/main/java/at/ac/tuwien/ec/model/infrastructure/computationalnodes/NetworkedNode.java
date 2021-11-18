package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import java.io.Serializable;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.Timezone;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public abstract class NetworkedNode implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8325571055039111575L;
	protected String id;
	protected Coordinates coords;
	protected HardwareCapabilities capabilities;
	protected NETEnergyModel netEnergyModel;
	protected double maxDistance = Double.MAX_VALUE;
	private double outData = 0.0;
	
	public double getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	public NetworkedNode(String id, HardwareCapabilities capabilities)
	{
		this.id = id;
		setCapabilities(capabilities);
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public double getChannelUtilization()
	{
		return capabilities.getAvailableCPUResources();
	}
	
	public void setCapabilities(HardwareCapabilities capabilities)
	{
		this.capabilities = capabilities;
	}
	
	public HardwareCapabilities getCapabilities()
	{
		return this.capabilities;
	}
	
	public Coordinates getCoords() {
		return coords;
	}

	public void setCoords(Coordinates coords) {
		this.coords = coords;
	}
	
	public void setCoords(Timezone tz) 
	{
		setCoords(tz.getX(),tz.getY());
	}
	
	public void setCoords(double x, double y){
		this.coords = new Coordinates(x,y);
	}
	
	public NETEnergyModel getNetEnergyModel() {
		return netEnergyModel;
	}

	public void setNetEnergyModel(NETEnergyModel netEnergyModel) {
		this.netEnergyModel = netEnergyModel;
	}
		
	public boolean isCompatible(SoftwareComponent sc)
	{
		return capabilities.supports(sc.getHardwareRequirements());
	}
	
	public double getOutData()
	{
		return this.outData;
	}
	
	public void setOutData(double oD)
	{
		this.outData = oD;
	}

}
