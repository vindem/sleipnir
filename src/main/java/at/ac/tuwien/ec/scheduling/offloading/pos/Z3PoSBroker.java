package at.ac.tuwien.ec.scheduling.offloading.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.ojalgo.random.LogNormal;

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

import at.ac.tuwien.ec.blockchain.Transaction;
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
import at.ac.tuwien.ec.scheduling.utils.blockchain.TransactionPool;
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

	public Z3PoSBroker(Tuple2<TransactionPool,MobileBlockchainInfrastructure> t) {
		super();
		loadLibrary();
		//setMobileApplication(t._1());
		t._2().setTransPool(t._1());
		setInfrastructure(t._2());
		
	}

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<ValidationOffloadScheduling> scheduling = new ArrayList<ValidationOffloadScheduling>();
			
		((MobileBlockchainInfrastructure)this.currentInfrastructure).getTransactionPool().generateTransactions();
		ValidationOffloadScheduling currScheduling= Z3Solver(((MobileBlockchainInfrastructure)this.currentInfrastructure)
				.getTransactionPool().getTransactionCount(),
				this.currentInfrastructure.getAllNodes().size());
		scheduling.add(currScheduling);
		return scheduling;
	}

	private ValidationOffloadScheduling Z3Solver(int transactionsNum, int nodeNum)
	{
		ValidationOffloadScheduling currScheduling = new ValidationOffloadScheduling();
		
		class ObjEntry
		{
			public int blockSize;
			public ArrayList<ComputationalNode> candidateNodes;
			public double goals[] = new double[3];
		}
		
		HashMap<Integer,ObjEntry> objectivesPerVehicle = new HashMap<Integer,ObjEntry>();
		
		int[] candidateBlockSizes = new int[this.currentInfrastructure.getMobileDevices().size()];
		
		
		
		//determining base parameters per vehicle
		int v = 0;
		int numberOfVehicles = this.currentInfrastructure.getMobileDevices().values().size();
		for(MobileDevice vehicle : this.currentInfrastructure.getMobileDevices().values())
		{
			ObjEntry vehicleEntry = new ObjEntry();
			int blockSize = calculateBlockSize(vehicle);
			ArrayList<ComputationalNode> candidateNodes = selectCandidateNodes(vehicle);
			double goals[] = calculateGoals(vehicle);
			
			vehicleEntry.blockSize = blockSize;
			vehicleEntry.candidateNodes = candidateNodes;
			vehicleEntry.goals = goals;
			
			
			objectivesPerVehicle.put(v, vehicleEntry);
			v++;
			
		}	
			Map<String, String> config = new HashMap<String, String>();
			config.put("model", "true");
			Context ctx = new Context(config);
			Solver solver = ctx.mkSolver();

			int numberOfTransactions = ((MobileBlockchainInfrastructure)this.currentInfrastructure).getTransPool()
            .getTransactionCount();
			
			IntExpr[][] assVar = new IntExpr[numberOfTransactions][numberOfVehicles];
			IntExpr[][] offVar = new IntExpr[numberOfVehicles][nodeNum];

			for(int i = 0; i < numberOfTransactions; i++)
				for(int j = 0; j < numberOfVehicles; j++)
					assVar[i][j] = ctx.mkIntConst("x"+i+""+j);
			
			for(int i = 0; i < numberOfVehicles; i++)
				for(int j = 0; j < nodeNum; j++)
					offVar[i][j] = ctx.mkIntConst("o"+i+""+j);

			BoolExpr[] assignment = new BoolExpr[numberOfTransactions];

			for(int i = 0; i < numberOfTransactions; i++)
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
				//each value is 0,1, each task is assigned at most at 1 node
				assignment[i] = ctx.mkAnd(domain,ctx.mkLe(sum, ctx.mkInt(1)));
			}
			
			BoolExpr[] offload = new BoolExpr[numberOfVehicles];
			IntExpr[] offInd = new IntExpr[numberOfVehicles];
			
			for(int i = 0; i < numberOfVehicles; i++)
			{
				BoolExpr domain = ctx.mkBool(true);
				ArithExpr sum = ctx.mkInt(0);
				for(int j = 0; j < nodeNum; j++) 
				{
					domain = ctx.mkAnd(domain,
							ctx.mkOr(
									ctx.mkEq(offVar[i][j].simplify(),ctx.mkInt(0)),
									ctx.mkEq(offVar[i][j].simplify(),ctx.mkInt(1))
									)
							);
					sum = ctx.mkAdd(sum,offVar[i][j]);
				}
				//each value is 0,1, each task is assigned at most at 1 node
				offInd[i] = (IntExpr) sum.simplify();
				offload[i] = ctx.mkAnd(domain,ctx.mkLe(sum, ctx.mkInt(1)));
			}
			
			//each vehicle does not get more transactions than its candidate block size
			BoolExpr[] boundTrans = new BoolExpr[nodeNum];
			
			for(int j = 0; j < nodeNum; j++)
			{
				ArithExpr sum = ctx.mkInt(0);
				for(int i = 0; i < transactionsNum; i++)
					sum = ctx.mkAdd(sum,assVar[i][j]);
				boundTrans[j] = ctx.mkLe(sum, ctx.mkInt(objectivesPerVehicle.get(j).blockSize));
					
			}
			RealExpr[][] runtimes = new RealExpr[numberOfTransactions][numberOfVehicles];
			RealExpr[][] costMatrix = new RealExpr[numberOfTransactions][numberOfVehicles];
			RealExpr[][] energyMatrix = new RealExpr[numberOfTransactions][numberOfVehicles];
			
			RealExpr[][] runtimesOffload = new RealExpr[numberOfVehicles][nodeNum];
			RealExpr[][] costMatrixOffload = new RealExpr[numberOfVehicles][nodeNum];
			RealExpr[][] energyMatrixOffload = new RealExpr[numberOfVehicles][nodeNum];
			
			

			ArrayList<Transaction> transactionPool = ((MobileBlockchainInfrastructure)this.currentInfrastructure).getTransPool().getTransactions();
			
			//System.out.println("RUNTIME");
			int i = 0;
			for(Transaction curr : transactionPool)
			{
				for(int j = 0; j < numberOfVehicles; j++)
				{
					LogNormal lnDistr = new LogNormal(0.1,0.3);
					curr.setMillionsOfInstruction(lnDistr.doubleValue());
					curr.setQuantileOfMI(lnDistr.getQuantile(0.95));
					runtimes[i][j] = ctx.mkReal(""+computeQRuntime(curr,j));
					costMatrix[i][j] = ctx.mkReal(""+computeQCost(curr,j));
					energyMatrix[i][j] = ctx.mkReal(""+computeQEnergy(curr,j));
				}
				i++;
				//System.out.println("");
			}
			
			for(i = 0; i < numberOfVehicles; i++) 
			{
				for(int j = 0; j < nodeNum; j++) 
				{
					ArithExpr rtTransSum = ctx.mkReal(""+0.0);
					ArithExpr costTransSum = ctx.mkReal(""+0.0);
					ArithExpr energyTransSum = ctx.mkReal(""+0.0);
					for(int k = 0; k < numberOfTransactions; k++) 
					{
						rtTransSum = ctx.mkAdd(rtTransSum,ctx.mkMul(assVar[k][i],
								ctx.mkReal(""+computeQRuntimeOffload(i,j,transactionPool.get(k)))));
						costTransSum = ctx.mkAdd(costTransSum,ctx.mkMul(assVar[k][i],
								ctx.mkReal(""+computeQCostOffload(i,j,transactionPool.get(k)))));
						energyTransSum = ctx.mkAdd(energyTransSum,ctx.mkMul(assVar[k][i],
								ctx.mkReal(""+computeQEnergyOffload(i,j,transactionPool.get(k)))));
					}
					runtimesOffload[i][j] = (RealExpr) rtTransSum.simplify();
					costMatrixOffload[i][j] = (RealExpr) costTransSum.simplify();
					energyMatrixOffload[i][j] = (RealExpr) energyTransSum.simplify();
				}
				
			}
			
			RealExpr[] runtimeBlockOffload,costBlockOffload,energyBlockOffload;
			
			runtimeBlockOffload = new RealExpr[numberOfVehicles];
			costBlockOffload = new RealExpr[numberOfVehicles];
			energyBlockOffload = new RealExpr[numberOfVehicles];
			
			for(i = 0; i < numberOfVehicles; i++) 
				for(int j = 0; j < nodeNum; j++) 
				{
					runtimeBlockOffload[i] = ctx.mkReal("0.0");
					costBlockOffload[i] = ctx.mkReal("0.0");
					energyBlockOffload[i] = ctx.mkReal("0.0");
					if(offVar[i][j].simplify().equals(ctx.mkInt(1)))
					{
						runtimeBlockOffload[i] = runtimesOffload[i][j];
						costBlockOffload[i] = costMatrixOffload[i][j];
						energyBlockOffload[i] = energyMatrixOffload[i][j];
						break;
					}
					
				}
				
			

			ArithExpr[] runtimeForVehicle = new ArithExpr[numberOfVehicles];
			BoolExpr[] rtConstraints = new BoolExpr[numberOfVehicles];
			ArithExpr[] costForVehicle = new ArithExpr[numberOfVehicles];
			BoolExpr[] costConstraints = new BoolExpr[numberOfVehicles];
			ArithExpr[] energyForVehicle = new ArithExpr[numberOfVehicles];
			BoolExpr[] energyConstraints = new BoolExpr[numberOfVehicles];
			
			for(int j = 0; j < numberOfVehicles; j++)
			{
				ArithExpr rt = ctx.mkMul(runtimes[0][j],assVar[0][j]);
				runtimeForVehicle[j] = ctx.mkAdd(rt);
				ArithExpr cost = ctx.mkMul(costMatrix[0][j],assVar[0][j]);
				costForVehicle[j] = ctx.mkAdd(cost);
				ArithExpr energy = ctx.mkMul(energyMatrix[0][j],assVar[0][j]);
				energyForVehicle[j] = ctx.mkAdd(energy);
				for(i = 1; i < numberOfTransactions; i++)
				{
					rt = ctx.mkMul(runtimes[i][j],assVar[i][j]);
					runtimeForVehicle[j] = ctx.mkAdd(runtimeForVehicle[j],rt);
					cost = ctx.mkMul(costMatrix[i][j],assVar[i][j]);
					costForVehicle[j] = ctx.mkAdd(runtimeForVehicle[j],cost);
					energy = ctx.mkMul(energyMatrix[i][j],assVar[i][j]);
					energyForVehicle[j] = ctx.mkAdd(energyForVehicle[j],energy);
				}
				
				double[] goals = objectivesPerVehicle.get(j).goals;

				rtConstraints[j] = ctx.mkLe(
						ctx.mkAdd(
								ctx.mkMul(ctx.mkSub(ctx.mkInt(1),offInd[j]),runtimeForVehicle[j]),
								ctx.mkMul(offInd[j],runtimeBlockOffload[j])
								),
						ctx.mkReal(""+goals[0]));
				costConstraints[j] = ctx.mkLe(
						ctx.mkAdd(
								ctx.mkMul(ctx.mkSub(ctx.mkInt(1),offInd[j]),costForVehicle[j]),
								ctx.mkMul(offInd[j],costBlockOffload[j])
								),
						ctx.mkReal(""+goals[0]));
				energyConstraints[j] = ctx.mkLe(
						ctx.mkAdd(
								ctx.mkMul(ctx.mkSub(ctx.mkInt(1),offInd[j]),energyForVehicle[j]),
								ctx.mkMul(offInd[j],energyBlockOffload[j])
								),
						ctx.mkReal(""+goals[0]));
				//System.out.println(rtConstraints[i]);

			}

			BoolExpr vehicleConstraints[] = new BoolExpr[nodeNum];
			for(i = 0; i < nodeNum; i++)
				vehicleConstraints[i] = ctx.mkAnd(rtConstraints[i],costConstraints[i],energyConstraints[i]);

			solver.add(assignment);
			solver.add(vehicleConstraints);

			Status status = solver.check();

			ArrayList<ComputationalNode> compNodes = this.currentInfrastructure.getAllNodes();
			
			Model model = null;
			if(status == Status.SATISFIABLE)
			{
				model = solver.getModel();
				//System.out.println("Satisfiable!");
				for(i = 0; i < transactionsNum; i++)
					for(int j = 0; j < nodeNum; j++)
					{
						if(model.getConstInterp(assVar[i][j]).equals(ctx.mkInt(1))) 
						{
							transactionPool.get(i).setOffloadTarget(this.currentInfrastructure.getAllNodes().get(j));
							currScheduling.put(transactionPool.get(i), compNodes.get(j));
							currScheduling.addRuntime(transactionPool.get(i),compNodes.get(j), this.currentInfrastructure);
							//currScheduling.addCost(transactionPool.get(i), transactionPool.get(i).getOffloadTarget(), this.currentInfrastructure);
							//currScheduling.addEnergyConsumption(transactionPool.get(i), transactionPool.get(i).getOffloadTarget(), this.currentInfrastructure);
						}
					}
				
			}
		
		return null;
	}

	private String computeQEnergyOffload(int i, int j, Transaction transaction) {
		// TODO Auto-generated method stub
		return "0.0";
	}

	private String computeQCostOffload(int i, int j, Transaction transaction) {
		// TODO Auto-generated method stub
		return "0.0";
	}

	private String computeQRuntimeOffload(int i, int j, Transaction transaction) {
		// TODO Auto-generated method stub
		return "0.0";
	}

	private String computeQEnergy(Transaction curr, int j) {
		// TODO Auto-generated method stub
		return "0.0";
	}

	private String computeQCost(Transaction curr, int j) {
		// TODO Auto-generated method stub
		return "0.0";
	}

	private String computeQRuntime(Transaction curr, int j) {
		// TODO Auto-generated method stub
		return "0.0";
	}

	private double[] calculateGoals(MobileDevice currentVehicle) {
		// TODO Auto-generated method stub
		double[] goals= {100.0, 200.0, 300.0};
		return goals;
	}

	private ArrayList<ComputationalNode> selectCandidateNodes(MobileDevice vehicle) {
		ArrayList<ComputationalNode> candidateNodes = new ArrayList<ComputationalNode>();

		candidateNodes.add(currentVehicle);

		for(ComputationalNode cn : this.currentInfrastructure.getEdgeNodes().values()) 
			candidateNodes.add(cn);

		return candidateNodes;
	}

	private int calculateBlockSize(MobileDevice vehicle) {
		// TODO Auto-generated method stub
		return 3;
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
