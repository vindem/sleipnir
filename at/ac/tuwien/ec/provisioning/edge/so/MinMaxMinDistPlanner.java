package at.ac.tuwien.ec.provisioning.edge.so;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.sleipnir.ARESMain;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import lpsolve.*;

public class MinMaxMinDistPlanner {

	LpSolve solver;
	
	public static void main(String args[])
	{
		//System.loadLibrary("liblpsolve55j.so");
		ARESMain.generateSamples(1);
		MinMaxMinDistPlanner planner = new MinMaxMinDistPlanner();
	}
	
	public MinMaxMinDistPlanner()
	{
		try {
			double[][] matrix = new double[SimulationSetup.iotDevicesCoordinates.size()][SimulationSetup.admissibleEdgeCoordinates.size()];
			this.solver = LpSolve.makeLp(0, SimulationSetup.admissibleEdgeCoordinates.size());
			for(int i = 0; i < SimulationSetup.admissibleEdgeCoordinates.size(); i++)
				solver.setBinary(i, true);
			for(int i = 0; i < SimulationSetup.iotDevicesCoordinates.size(); i++) 
			{
				for(int j = 0; j < SimulationSetup.admissibleEdgeCoordinates.size(); j++)
					matrix[i][j] = computeDist(SimulationSetup.iotDevicesCoordinates.get(i),
							SimulationSetup.admissibleEdgeCoordinates.get(j));
				
				solver.addColumn(matrix[i]);
			}
			solver.setMinim();
			solver.solve();
		} catch (LpSolveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private double computeDist(Coordinates iotCoords, Coordinates edgeCoords) {
		double iotX = getXCoord(iotCoords.getLatitude());
		double iotY = getYCoord(iotCoords.getLongitude());
		double edgeX = getXCoord(edgeCoords.getLatitude());
		double edgeY = getYCoord(edgeCoords.getLongitude());
		
		double distance = Math.abs(iotX - edgeX) + Math.max(0,(Math.abs(iotX-edgeX)- Math.abs(iotY-edgeY) )/2.0);
		return distance;
	}

	private static int getYCoord(double longitude) {
		double min = 48.12426368;
		double max = 48.30119579;
		double cellNIndex = ((longitude - min)/(max-min))*(SimulationSetup.MAP_N);  
		return (int) cellNIndex;
	}

	private static int getXCoord(double latitude) {
		double min = 16.21259754;
		double max = 16.52969867;
		double cellMIndex = ((latitude - min)/(max-min))*(SimulationSetup.MAP_M);  
		return (int) cellMIndex;
	}
	
}
