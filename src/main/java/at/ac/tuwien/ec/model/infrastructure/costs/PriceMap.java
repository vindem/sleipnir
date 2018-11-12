package at.ac.tuwien.ec.model.infrastructure.costs;

import java.util.HashMap;
import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Timezone;

public class PriceMap extends HashMap<Timezone, ElectricityPriceTrace> {
	
	public PriceMap(){
		super();
	}
	
	public double getPriceForTimeAtLocation(Coordinates currentCoordinates, double currentTime) 
	{
		Timezone tz = getTimezoneFromCoordinates(currentCoordinates);
		ElectricityPriceTrace currTrace = get(tz);
		if(currTrace == null)
			return Double.MAX_VALUE;
		int index = (int) Math.floor(currentTime / 3600);
		
		int tOffset = tz.getTZOffset();
		index = ((index+tOffset) < 0) ? currTrace.size() - (tOffset+index) 
					: (index+tOffset)%currTrace.size();
		return (currTrace.get(index) * 3.60e-9);
	}
	
	public void put(Coordinates coord, ElectricityPriceTrace tr)
	{
		Timezone tz = getTimezoneFromCoordinates(coord);
		super.put(tz,tr);
	}
	
	private Timezone getTimezoneFromCoordinates(Coordinates crd) {
		for(Timezone tz : keySet())
			if(crd.getLatitude() == tz.getX()
			&& crd.getLatitude() == tz.getY())
				return tz;
		return Timezone.DUBLIN;
	}

}
