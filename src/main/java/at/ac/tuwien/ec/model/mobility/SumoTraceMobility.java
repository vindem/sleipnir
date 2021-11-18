package at.ac.tuwien.ec.model.mobility;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import at.ac.tuwien.ec.model.Coordinates;

public class SumoTraceMobility implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -546869441881275833L;
	private ArrayList<Coordinates> trace, predicted;
	private RealMatrix F,B,H,Q,R,P;
	private RealVector x;
	private int lastPredictedTimeStep; 
	
		
	public SumoTraceMobility(ArrayList<Coordinates> trace)
	{
		this.trace = trace;
		this.lastPredictedTimeStep = 0;
		//testPrediction();
		double dt = 1.0d;
		F = new Array2DRowRealMatrix(new double[][] { 
			{1, 0d, dt, 0d},
			{0d, 1, 0d, dt},
			{0d, 0d, 1, 0d},
			{0d, 0d, 0d, 1} 
			} );
		B = null;
		// H: measurement matrix
		H = new Array2DRowRealMatrix(new double[][] { 
			{1, 0, 0, 0}, 
			{1, 0, 0, 0},
			{0, 0, 0, 0}, 
			{0, 0, 0, 0},
			});
		
		RealMatrix tmp = new Array2DRowRealMatrix(new double[][]
				{
					{0,0,0,0},
					{0,0,0,0},
					{0,0,0,0},
					{0,0,0,0}
				}
		);
		Q = tmp.scalarMultiply(Math.pow(0.2d,2));
		// R: measurement noise covariance matrix
		R = new Array2DRowRealMatrix(new double[][] {
			{1,0,1,0},
			{0,0,1,0},
			{1,1,1,0},
			{0,0,0,0},
			});
		// P: Error covariance matrix
		P = new Array2DRowRealMatrix(new double[][] {
			{1,1,1,1},
			{1,1,1,1},
			{1,1,1,1},
			{1,1,1,1}
			});  
		//testPrediction();
	} 

	private void testPrediction() {
		for(int i = 0; i < 10; i++)
			System.out.println("Real: "+trace.get(i) + " Prediction: "+predictCoordinatesForTimestep((double)i));
	}

	public Coordinates getCoordinatesForTimestep(double timestep) {
		int index = (int) Math.floor(timestep);
		index = (index) % trace.size();
		if(index >= trace.size() || index < 0)
			index = 0;
		
		return trace.get(index);
	}

	public Coordinates predictCoordinatesForTimestep(double timestep) {
		// TODO Auto-generated method stub
		int index = (int) Math.floor(timestep);
		index = (index) % trace.size();
		if(index >= trace.size() || index < 0)
			index = 0;
		
		return kalmanPrediction(index);
	}

	
	private RealVector getMeasurement(int index) {
		// TODO Auto-generated method stub
		Coordinates coord = trace.get(index);
		double x,y,angle,speed,vx,vy;
		x = coord.getLatitude();
		y = coord.getLongitude();
		angle = coord.getAngle();
		speed = coord.getSpeed();
		vx = Math.cos(Math.toRadians(angle)) * speed;
		vy = Math.sin(Math.toRadians(angle)) * speed; 
		return new ArrayRealVector(new double[] 
				{trace.get(index).getLatitude(), trace.get(index).getLongitude(),vx,vy}); 
						
	}

	private Coordinates kalmanPrediction(double timestep)
	{
		if(timestep == 0.0)
			return trace.get(0);
		
		double startx, starty, startangle, startspeed,vx,vy;
		
		//Initial state for current vehicle
		
		Coordinates prevCoord = this.trace.get(lastPredictedTimeStep);
		startx = prevCoord.getLatitude();
		starty = prevCoord.getLongitude();
		startangle = prevCoord.getAngle();
		startspeed = prevCoord.getSpeed();
		vx = Math.cos(Math.toRadians(startangle)) * startspeed;
		vy = Math.sin(Math.toRadians(startangle)) * startspeed;
		x = new ArrayRealVector(new double[] {startx, starty, vx, vy});
		
		ProcessModel pm
		   = new DefaultProcessModel(F, B, Q, x, null);
		MeasurementModel mm = new DefaultMeasurementModel(H, R);
		KalmanFilter filter = new KalmanFilter(pm, mm);
		Coordinates coords = new Coordinates(startx, starty);
		coords.setAngle(startangle);
		coords.setSpeed(startspeed);
		for(int i = lastPredictedTimeStep; i < timestep; i++)
		{
			filter.predict();
			RealVector z = getMeasurement(i);
			
			//filter.correct(z);
			double[] stateEstimate = filter.getStateEstimation();
			
			coords = new Coordinates(stateEstimate[0]+stateEstimate[2]/10.0, stateEstimate[1]+stateEstimate[3]/10.0);
		}
		if((int)timestep != lastPredictedTimeStep)
			lastPredictedTimeStep = (int)timestep;
		return coords;
	}	
}
