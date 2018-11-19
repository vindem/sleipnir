package at.ac.tuwien.ec.model;

import java.io.Serializable;

/**
 *
 * @author stefano
 */
public class QoS implements Serializable{
    private double latency;
    private double bandwidth;
    
    public QoS (double latency, double bandwidth){
        this.latency = latency;
        this.bandwidth = bandwidth;
    }
    
    public void setLatency (int latency){
        this.latency = latency;
    }
    
    public void setBandwidth(double bandwidth){
        this.bandwidth = bandwidth;
    }
    
    public double getLatency(){
        return this.latency;
    }
    
    public double getBandwidth(){
        return this.bandwidth;
    }
    
    @Override
    public String toString(){
        return "<" + latency + ", " + bandwidth + ">";
    }
    
    public boolean supports(QoS q){
        boolean result = false;
        if (latency <= q.getLatency() && bandwidth >= q.getBandwidth())
            result = true;
        return result;
    }

}
