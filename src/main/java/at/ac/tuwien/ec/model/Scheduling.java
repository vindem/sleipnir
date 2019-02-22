package at.ac.tuwien.ec.model;

import java.util.LinkedHashMap;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;

public class Scheduling extends LinkedHashMap<SoftwareComponent, NetworkedNode> {
	
	 private int hashCode = Integer.MIN_VALUE;
	 
	public Scheduling()
	{
		
	}

	public Scheduling(Scheduling deployment) {
		// TODO Auto-generated constructor stub
	}

	public String toString(){
	        String result ="";

	        for (SoftwareComponent s : super.keySet()){
	            result+="["+s.getId()+"->" +super.get(s).getId()+"]" ;
	        }
	        
	        return result;   
	    }
	    
	    @Override
	    public boolean equals(Object o){
	        boolean result = true;
	        OffloadScheduling d = (OffloadScheduling) o;
	        result = this.hashCode() == d.hashCode();
	        /*for (SoftwareComponent s : this.keySet()){
	            if (!this.get(s).equals(d.get(s))){
	                result = false;
	                break;
	            }
	        }*/
	        return result;
	    }

	    @Override
	    public int hashCode() {
	        if(this.hashCode == Integer.MIN_VALUE)
	        {
	        	int hash = 7;
	        	String s = this.toString();
	        	this.hashCode = 47 * hash + s.hashCode();
	        }
	        return this.hashCode;
	    }

}
