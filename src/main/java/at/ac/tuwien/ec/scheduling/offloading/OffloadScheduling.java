/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ec.scheduling.offloading;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import java.text.DecimalFormat;
import java.util.TreeMap;

import org.apache.commons.math3.ode.ODEIntegrator;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;



/**
 *
 * @author vincenzo
 */
public class OffloadScheduling extends Scheduling{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5978753101322855324L;
	private double runTime, userCost, providerCost, batteryLifetime, infEnergyConsumption;
	private int hashCode = Integer.MIN_VALUE;
	private double executionTime;
    
    public OffloadScheduling(){
        super();
        batteryLifetime = OffloadingSetup.batteryCapacity;
        runTime = 0.0;
        userCost = 0.0;
        providerCost = 0.0;
        infEnergyConsumption = 0.0;
        executionTime = 0.0;
    }

   public OffloadScheduling(OffloadScheduling deployment) {
        super(deployment);
    }
      
    
    @Override
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

    /**
     * Adds runtime for execution of SoftwareComponent s on node n and infrastructure I
     * @param s the MobileSoftwareComponent modelling the current task
     * @param n the target computational node
     * @param I the target infrastructure
     */
    public void addRuntime(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	double tmp = s.getRuntimeOnNode(n, I);
    	s.setRunTime(tmp);
    	this.runTime += tmp;
    }
    /**
     * Removes runtime of execution of component s on node n and infrastructure I
     * @param s the MobileSoftwareComponent modelling the current task
     * @param n the target computational node
     * @param I the target infrastructure
     */
    public void removeRuntime(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	this.runTime -= s.getRuntimeOnNode((ComputationalNode) super.get(s), I);
    }
    
    /**
     * Adds cost of executing MobileSoftwareComponent s on target node n and infrastructure I
     * @param s the MobileSoftwareComponent modelling the current task
     * @param n the target computational node
     * @param I the target infrastructure
     */
    public void addCost(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I) {
    	//cost is computed according to node cost model
        this.userCost += n.computeCost(s, I);
    }
    /**
     * Adds cost of executing MobileSoftwareComponent s on target node n, using m as predecessor, and infrastructure I
     * @param s the MobileSoftwareComponent modelling the current task
     * @param n the target computational node
     * @param m the predecessor computational node
     * @param I the target infrastructure
     */
    public void addCost(MobileSoftwareComponent s, ComputationalNode n, ComputationalNode m, MobileCloudInfrastructure I) {
    	//cost is computed according to node cost model
    	this.userCost += n.computeCost(s, m, I);
    }
    
    /**
     * Removes cost of executing MobileSoftwareComponent s on target node n
     * @param s the MobileSoftwareComponent modelling the current task
     * @param n the target computational node
     * @param I the target infrastructure
     */
    public void removeCost(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
        this.userCost -= n.computeCost(s, I);
    }

    /**
     * Adds energy consumption for execution of MobileSoftwareComponent s on node n and infrastructure i
     * @param s the MobileSoftwareComponent modelling the current task
     * @param n the target computational node
     * @param i the target infrastructure
     */
	public void addEnergyConsumption(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		//TODO: consider idle power
		
		//if target node is a mobile device, we remove energy consumption from its remaining battery lifetime
		if(i.getMobileDevices().containsKey(n.getId()))
		{
			//energy is calculated according to the energy model of the node
			double energy = n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			((MobileDevice)i.getNodeById(s.getUserId())).removeFromBudget(energy);
			this.batteryLifetime -= energy;
		}
		else
		{
			/* since we are not executing the task on the mobile device, we need remove from its battery lifetime
			 * the energy required to offload the task on the network
			 */
			double offloadEnergy = i.getMobileDevices().get(s.getUserId()).getNetEnergyModel().computeNETEnergy(s, n, i);
			i.getMobileDevices().get(s.getUserId()).removeFromBudget(offloadEnergy);
			//we add consumption for task execution to the consumption of the infrastructure (useful in some scenarios)
			this.infEnergyConsumption += n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			this.batteryLifetime -= offloadEnergy;
		}
		
	}
	/**
    * Removes energy consumption for execution of MobileSoftwareComponent s on node n and infrastructure i
    * @param s the MobileSoftwareComponent modelling the current task
    * @param n the target computational node
    * @param i the target infrastructure
    */
	public void removeEnergyConsumption(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		//if target node is a mobile device, we restore energy consumption to its remaining battery lifetime
		if(i.getMobileDevices().containsKey(n.getId()))
		{
			//energy is calculated according to the energy model of the node
			double energy = n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			((MobileDevice)i.getNodeById(s.getUserId())).addToBudget(energy);
			this.batteryLifetime += energy;
		}
		else
		{
			/* since we are not executing the task on the mobile device, we add to its battery lifetime
			 * the energy required to offload the task on the network
			 */
			double offloadEnergy = i.getMobileDevices().get(s.getUserId()).getNetEnergyModel().computeNETEnergy(s, n, i);
			i.getMobileDevices().get(s.getUserId()).addToBudget(offloadEnergy);
			//we remove consumption for task execution to the consumption of the infrastructure (useful in some scenarios)
			this.infEnergyConsumption -= n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			this.batteryLifetime += offloadEnergy;
		}
		
	}

	/**
	 * Adds cost for the provider to execute the task, based on energy consumption
	 * @param s
	 * @param n
	 * @param i
	 */
	public void addProviderCost(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		if(!n.equals(i.getMobileDevices().get(s.getUserId()))) 
		{
			providerCost += n.getCPUEnergyModel().computeCPUEnergy(s, n, i)
					* i.getPriceForLocation(n.getCoords(),runTime);
			
			for(CloudDataCenter dc : i.getCloudNodes().values())
				if(!dc.getId().equals(n.getId()))
					providerCost += dc.getCPUEnergyModel().getIdlePower(s, n, i)
					* i.getPriceForLocation(dc.getCoords(),runTime);

			for(EdgeNode fn : i.getEdgeNodes().values())
				if(!n.getId().equals(fn.getId()))
					providerCost += fn.getCPUEnergyModel().getIdlePower(s, n, i)
					* i.getPriceForLocation(fn.getCoords(),runTime);
		}
		
	}

	public void removeProviderCost(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		for(CloudDataCenter dc : i.getCloudNodes().values())
		{
			if(!n.equals(dc))
				providerCost -= dc.getCPUEnergyModel().getIdlePower(s, n, i)
				* i.getPriceForLocation(dc.getCoords(),runTime);
		}
		for(EdgeNode fn : i.getEdgeNodes().values())
		{
			if(!n.equals(fn))
				providerCost -= fn.getCPUEnergyModel().getIdlePower(s, n, i)
				* i.getPriceForLocation(fn.getCoords(),runTime);
		}
		if(!i.getMobileDevices().containsKey(n.getId())){
			providerCost -= n.getCPUEnergyModel().computeCPUEnergy(s, n, i)
			* i.getPriceForLocation(n.getCoords(),runTime);
		}
	}

	public double getRunTime() {
		return runTime;
	}

	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}

	public double getUserCost() {
		return userCost;
	}

	public void setUserCost(double userCost) {
		this.userCost = userCost;
	}

	public double getProviderCost() {
		return providerCost;
	}

	public void setProviderCost(double providerCost) {
		this.providerCost = providerCost;
	}

	public double getBatteryLifetime() {
		return batteryLifetime;
	}

	public void setBatteryLifetime(double batteryLifetime) {
		this.batteryLifetime = batteryLifetime;
	}

	public double getInfEnergyConsumption() {
		return infEnergyConsumption;
	}

	public void setInfEnergyConsumption(double infEnergyConsumption) {
		this.infEnergyConsumption = infEnergyConsumption;
	}

	public void setExecutionTime(double time)
	{
		this.executionTime = time;
	}
	
	public Double getExecutionTime() {
		return this.executionTime;
	}
	
}
