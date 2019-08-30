package at.ac.tuwien.ec.scheduling.offloading.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.infrastructure.MobileBlockchainInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import scala.Tuple2;

public class Z3PoSBroker extends PoSOffloadingAlgorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4979418341371131767L;
	private MobileDevice currentVehicle;

	public Z3PoSBroker(ArrayList<DataEntry> a, MobileBlockchainInfrastructure I) {
		super();
		//setMobileApplication(A);
		setInfrastructure(I);
	}

	public Z3PoSBroker(Tuple2<ArrayList<DataEntry>,MobileBlockchainInfrastructure> t) {
		super();
		loadLibrary();
		//setMobileApplication(t._1());
		setInfrastructure(t._2());
	}

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<ValidationOffloadScheduling> scheduling = new ArrayList<ValidationOffloadScheduling>();
		for(MobileDevice vehicle : this.currentInfrastructure.getMobileDevices().values())
		{
			ArrayList<MobileSoftwareComponent> candidateBlock = createCandidateBlock(vehicle);
			ArrayList<ComputationalNode> candidateNodes = selectCandidateNodes(vehicle);

			final int blockSize = candidateBlock.size();
			final int nodeNum = candidateNodes.size();
			final double[] goals = calculateGoals(vehicle);

			ValidationOffloadScheduling currScheduling= Z3Solver(candidateBlock, candidateNodes, nodeNum, nodeNum, goals);

			scheduling.add(currScheduling);
		}
		return scheduling;
	}

	private ValidationOffloadScheduling Z3Solver(ArrayList<MobileSoftwareComponent> candidateBlock, ArrayList<ComputationalNode> candidateNodes,int blockSize, int nodeNum, double[] goals)
	{
		ValidationOffloadScheduling currScheduling = new ValidationOffloadScheduling();

		Map<String, String> config = new HashMap<String, String>();
		config.put("model", "true");
		Context ctx = new Context(config);
		Solver solver = ctx.mkSolver();

		IntExpr[][] assVar = new IntExpr[blockSize][nodeNum];

		for(int i = 0; i < blockSize; i++)
			for(int j = 0; j < nodeNum; j++)
				assVar[i][j] = ctx.mkIntConst("x"+i+""+j);

		BoolExpr[] assignment = new BoolExpr[blockSize];

		for(int i = 0; i < blockSize; i++)
		{
			BoolExpr domain = ctx.mkBool(true);
			ArithExpr sum = ctx.mkInt(0);
			for(int j = 0; j < nodeNum; j++) 
			{
				domain = ctx.mkAnd(domain,
						ctx.mkOr(
								ctx.mkEq(assVar[i][j].simplify(),ctx.mkInt(0)),
								ctx.mkEq(assVar[i][j].simplify(),ctx.mkInt(1))
								)
						);
				sum = ctx.mkAdd(sum,assVar[i][j]);
			}
			assignment[i] = ctx.mkAnd(domain,ctx.mkEq(sum, ctx.mkInt(1)));
		}

		RealExpr[][] runtimes = new RealExpr[blockSize][nodeNum];
		RealExpr[][] costMatrix = new RealExpr[blockSize][nodeNum];
		RealExpr[][] energyMatrix = new RealExpr[blockSize][nodeNum];

		//System.out.println("RUNTIME");
		for(int i = 0; i < blockSize; i++)
		{
			for(int j = 0; j < nodeNum; j++)
			{
				MobileSoftwareComponent curr = candidateBlock.get(i);

				runtimes[i][j] = ctx.mkReal(""+curr.getRuntimeOnNode(this.currentVehicle, candidateNodes.get(j), this.currentInfrastructure));
				costMatrix[i][j] = ctx.mkReal(""+candidateNodes.get(j).computeCost(curr, this.currentInfrastructure));
				energyMatrix[i][j] = ctx.mkReal(""+computeEnergyFor(this.currentVehicle,candidateNodes.get(j),this.currentInfrastructure));
			}
			//System.out.println("");
		}

		ArithExpr[] runtimeForTask = new ArithExpr[blockSize];
		BoolExpr[] rtConstraints = new BoolExpr[blockSize];
		ArithExpr[] costForTask = new ArithExpr[blockSize];
		BoolExpr[] costConstraints = new BoolExpr[blockSize];
		ArithExpr[] energyForTask = new ArithExpr[blockSize];
		BoolExpr[] energyConstraints = new BoolExpr[blockSize];
		for(int i = 0; i < blockSize; i++)
		{
			ArithExpr rt = ctx.mkMul(runtimes[i][0],assVar[i][0]);
			runtimeForTask[i] = ctx.mkAdd(rt);
			ArithExpr cost = ctx.mkMul(costMatrix[i][0],assVar[i][0]);
			costForTask[i] = ctx.mkAdd(cost);
			ArithExpr energy = ctx.mkMul(energyMatrix[i][0],assVar[i][0]);
			energyForTask[i] = ctx.mkAdd(energy);
			for(int j = 1; j < nodeNum; j++)
			{
				rt = ctx.mkMul(runtimes[i][j],assVar[i][j]);
				runtimeForTask[i] = ctx.mkAdd(runtimeForTask[i],rt);
				cost = ctx.mkMul(costMatrix[i][j],assVar[i][j]);
				costForTask[i] = ctx.mkAdd(runtimeForTask[i],cost);
				energy = ctx.mkMul(energyMatrix[i][j],assVar[i][j]);
				energyForTask[i] = ctx.mkAdd(energyForTask[i],energy);
			}

			rtConstraints[i] = ctx.mkLe(runtimeForTask[i], ctx.mkReal(""+goals[0]));
			costConstraints[i] = ctx.mkLe(costForTask[i], ctx.mkReal(""+goals[1]));
			energyConstraints[i] = ctx.mkLe(energyForTask[i], ctx.mkReal(""+goals[2]));
			//System.out.println(rtConstraints[i]);

		}

		BoolExpr taskConstraints[] = new BoolExpr[blockSize];
		for(int i = 0; i < blockSize; i++)
			taskConstraints[i] = ctx.mkAnd(rtConstraints[i],costConstraints[i],energyConstraints[i]);

		solver.add(assignment);
		solver.add(taskConstraints);

		Status status = solver.check();


		Model model = null;
		if(status == Status.SATISFIABLE)
		{
			model = solver.getModel();
			//System.out.println("Satisfiable!");
			for(int i = 0; i < blockSize; i++)
				for(int j = 0; j < nodeNum; j++)
				{
					if(model.getConstInterp(assVar[i][j]).equals(ctx.mkInt(1))) 
					{
						currScheduling.put(candidateBlock.get(i), candidateNodes.get(j));
						currScheduling.addRuntime(candidateBlock.get(i), candidateNodes.get(j), this.currentInfrastructure);
						currScheduling.addCost(candidateBlock.get(i), candidateNodes.get(j), this.currentInfrastructure);
						currScheduling.addEnergyConsumption(candidateBlock.get(i), candidateNodes.get(j), this.currentInfrastructure);
					}
				}
			return currScheduling;
		}
		return null;
	}

	private double[] calculateGoals(MobileDevice currentVehicle) {
		// TODO Auto-generated method stub
		return null;
	}

	private double computeEnergyFor(MobileDevice currentVehicle, ComputationalNode computationalNode,
			MobileCloudInfrastructure currentInfrastructure) {
		// TODO Auto-generated method stub
		return 0;
	}

	private ArrayList<ComputationalNode> selectCandidateNodes(MobileDevice vehicle) {
		ArrayList<ComputationalNode> candidateNodes = new ArrayList<ComputationalNode>();

		candidateNodes.add(currentVehicle);

		for(ComputationalNode cn : this.currentInfrastructure.getEdgeNodes().values()) 
			candidateNodes.add(cn);

		return candidateNodes;
	}

	private ArrayList<MobileSoftwareComponent> createCandidateBlock(MobileDevice vehicle) {
		// TODO Auto-generated method stub
		return null;
	}

	private void loadLibrary()
	{
		System.err.println("java.library.path = " + System.getProperty("java.library.path"));
		System.err.println("trying to load lib z3java");
		try { 
			System.load("/home/vincenzo/z3/build/libz3.so");
			System.load("/home/vincenzo/z3/build/libz3java.so"); 
		} catch (UnsatisfiedLinkError ex1) { 
			ex1.printStackTrace();
			try { 
				System.err.println("Trying to load lib libz3java");
				System.loadLibrary("libz3java"); 
			} catch (UnsatisfiedLinkError ex2) { 
				ex2.printStackTrace();
			}
		}
	}

	private static double doubleValue(RealExpr realExpr) {
		Expr value = realExpr.simplify();
		if(value.isRatNum())
		{
			RatNum rational = (RatNum) value;
			IntNum num = rational.getNumerator();
			IntNum den = rational.getDenominator();
			return ((double) num.getInt64() / den.getInt64());
		}
		return ((IntNum)value.simplify()).getInt64();
	}

}
