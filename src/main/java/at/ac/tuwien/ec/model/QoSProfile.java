package at.ac.tuwien.ec.model;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.ec.model.QoS;
import scala.Tuple2;

/**
 *
 * @author Stefano
 */
public class QoSProfile {
    private double latency;
    private double bandwidth;
    
    public ArrayList<QoS> QoS;
    public double[] probabilities;
    
    public QoSProfile(double d, double bandwidth) {
        probabilities = new double[1];
        QoS = new ArrayList<QoS>(1);
        
        this.latency = d;
        this.bandwidth = bandwidth;
        
        QoS.add(0, new QoS(d, bandwidth));
        probabilities[0] = 1;
    }
    
    public QoSProfile(List<Tuple2<QoS, Double>> qos){
        int size = qos.size();
        probabilities = new double[size];
        QoS = new ArrayList<QoS>(size);
        int i = 0;
        for (Tuple2<QoS, Double> c : qos){
            QoS.add(i, c._1);
            probabilities[i] = c._2;
            i++;
        }
        
        this.sampleQoS();
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
    
    
    public final void sampleQoS(){
        int sample = StdRandom.discrete(probabilities);
        latency = QoS.get(sample).getLatency();
        bandwidth = QoS.get(sample).getBandwidth();
        //System.out.println("Sampling: " + this);
    }
    
    @Override
    public String toString(){
        return "<" + latency + ", " + bandwidth + ">";
    }
    
    public boolean supports(QoSProfile q){
        boolean result = false;
        if (latency <= q.getLatency() && bandwidth >= q.getBandwidth())
            result = true;
        return result;
    }
    
}
