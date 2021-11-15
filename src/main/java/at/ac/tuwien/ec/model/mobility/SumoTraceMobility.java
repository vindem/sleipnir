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
	
	
		
	public SumoTraceMobility(ArrayList<Coordinates> trace)
	{
		this.trace = trace;
		
		//timestep
		
		
		
		
		
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
		return new ArrayRealVector(new double[] {trace.get(index).getLatitude(), trace.get(index).getLongitude()});
	}

	private Coordinates kalmanPrediction(double timestep)
	{
		if(timestep == 0.0)
			return trace.get(0);
		
		RealMatrix A,B,H,Q,R,P;
		double dt = 1.0d;
		// A: state transition matrix
		A = new Array2DRowRealMatrix(new double[][] { {1d, 0d, dt, 0d}, {0d, 1d, 0d, dt}, {0d, 0d, 1d, 0d}, {0d, 0d, 0d, 1d} } );
		// B: control input matrix
		B = null;
		// H: measurement matrix
		H = new Array2DRowRealMatrix(new double[][] { {1d, 0d, 1d, 0d}, {0d, 1d, 0d, 1d} });
		
		double startx, starty;
		Coordinates coord = this.trace.get(0);
		startx = coord.getLatitude();
		starty = coord.getLongitude();
		//Initial state for current vehicle
		RealVector x = new ArrayRealVector(new double[] {startx, starty, 0d, 0d});
		
		
		// Q: Process noise covariance matrix
		RealMatrix tmp = new Array2DRowRealMatrix(new double[][]
				{
					{1.0,1.0,1.0,1.0},
					{1.0,1.0,1.0,1.0},
					{1.0,1.0,1.0,1.0},
					{1.0,1.0,1.0,1.0}
				}
		);
		Q = tmp.scalarMultiply(Math.pow(0.1,2));
		// R: measurement noise covariance matrix
		R = new Array2DRowRealMatrix(new double[][] {{0.1, 0.1},{0.1,0.1}});
		// P: Error covariance matrix
		P = new Array2DRowRealMatrix(new double[][] {{1,1,1,1}, {1,1,1,1}, {1,1,1,1}, {1,1,1,1}});  

		ProcessModel pm
		   = new DefaultProcessModel(A, B, Q, x, null);
		MeasurementModel mm = new DefaultMeasurementModel(H, R);
		KalmanFilter filter = new KalmanFilter(pm, mm);
		Coordinates coords = null;
		for(int i = 0; i < timestep; i++)
		{
			filter.predict();
			RealVector z = getMeasurement(i);
			
			//filter.correct(z);
			double[] stateEstimate = filter.getStateEstimation();
			coords = new Coordinates(stateEstimate[0], stateEstimate[1]);
		}
		return coords;
	}	
}
