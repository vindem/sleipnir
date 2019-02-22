package at.ac.tuwien.ec.scheduling.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;

public abstract class SimIteration implements Callable<ArrayList<? extends Scheduling>>{

	protected MobileApplication currentApp;
	protected MobileCloudInfrastructure currentInfrastructure;
    private String algorithm;   

    
    public MobileApplication getMobileApplication() {
		return currentApp;
	}

	public void setMobileApplication(MobileApplication a) {
		currentApp = a;
	}

	public MobileCloudInfrastructure getInfrastructure() {
		return currentInfrastructure;
	}

	public void setInfrastructure(MobileCloudInfrastructure i) {
		currentInfrastructure = i;
	}

	public void setChosenAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public ArrayList<? extends Scheduling> call()
	{
		return findScheduling();
	}
	
	public abstract ArrayList<? extends Scheduling> findScheduling();
    

}
