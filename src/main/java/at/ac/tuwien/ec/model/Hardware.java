package at.ac.tuwien.ec.model;

/**
 *
 * @author Stefano
 */
public final class Hardware {
    
    //currently available resources
    int cores;
    double ram,storage;
	
    public Hardware(int cores, double ram, double storage) {
		this.cores = cores;
		this.ram = ram;
		this.storage = storage;
	}

	public int getCores() {
		return cores;
	}

	public void setCores(int cores) {
		this.cores = cores;
	}

	public double getRam() {
		return ram;
	}

	public void setRam(double ram) {
		this.ram = ram;
	}

	public double getStorage() {
		return storage;
	}

	public void setStorage(double storage) {
		this.storage = storage;
	} 
    
    public Hardware clone()
    {
    	return new Hardware(cores,ram,storage);
    }
    
}
