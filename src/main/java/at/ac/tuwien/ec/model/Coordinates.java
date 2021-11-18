/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ec.model;

import java.io.Serializable;

public class Coordinates implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2016575881393139663L;
	private double lat, lng;
	private double angle, speed;

    public Coordinates (double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }
    @Override
    public boolean equals(Object c)
    {
    	if(c.getClass() ==  Coordinates.class){
    		Coordinates k = (Coordinates) c;
    		return k.getLatitude() == this.getLatitude() 
    			&& k.getLongitude() == this.getLongitude();
    	}
    	return false;
    }
    
    public double distance (Coordinates coords){
        final int R = 6371; // Radius of the earth
        double lat1 = this.lat;
        double lat2 = coords.getLatitude();
        double lon1 = this.lng;
        double lon2 = coords.getLongitude();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    public double getLatitude() {
        return this.lat;
    }

    public double getLongitude() {
        return this.lng;
    }
    
    public String toString(){
        return "("+ this.lat + ", " + this.lng + ")";
    }
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
}
