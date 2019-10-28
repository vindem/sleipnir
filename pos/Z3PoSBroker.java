package at.ac.tuwien.ec.scheduling.offloading.pos;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.ojalgo.random.LogNormal;

import com.microsoft.z3.ApplyResult;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Goal;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Tactic;

import at.ac.tuwien.ec.blockchain.Transaction;
import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.infrastructure.MobileBlockchainInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
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
	private TransactionPool currentTransactionPool;
	

	public Z3PoSBroker(ArrayList<DataEntry> a, MobileBlockchainInfrastructure I) {
		super();
		//setMobileApplication(A);
		setInfrastructure(I);
	}

	public Z3PoSBroker(Tuple2<TransactionPool,MobileBlockchainInfrastructure> t) {
		super();
		//loadLibrary();
		//setMobileApplication(t._1());
		currentTransactionPool = (TransactionPool) t._1().clone();
		t._2().setTransPool(currentTransactionPool);
		setInfrastructure(t._2());
		
	}

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<ValidationOffloadScheduling> scheduling = new ArrayList<ValidationOffloadScheduling>();
		
		long start = System.currentTimeMillis();
		
		ValidationOffloadScheduling currScheduling= Z3Solver();
		long end = System.currentTimeMillis();
		double time = end - start;
		if(currScheduling != null)
		{
			currScheduling.setSimTime(time);
			scheduling.add(currScheduling);
		}
		return scheduling;
	}

	private ValidationOffloadScheduling Z3Solver()
	{
		ValidationOffloadScheduling currScheduling = new ValidationOffloadScheduling();
		Context ctx;
		class ObjEntry
		{
			public int blockSize;
			public ArrayList<Integer> candidateNodes;
			public double goals[] = new double[3];
		}
		
		HashMap<Integer,ObjEntry> objectivesPerVehicle = new HashMap<Integer,ObjEntry>();
		
		//determining base parameters per vehicle
		int v = 0;
		int numberOfVehicles = this.currentInfrastructure.getMobileDevices().values().size();
		
		ArrayList<ComputationalNode> compNodes = new ArrayList<ComputationalNode>();
		compNodes.addAll(this.currentInfrastructure.getEdgeNodes().values());
		compNodes.addAll(this.currentInfrastructure.getCloudNodes().values());
		
		
		for(MobileDevice vehicle : this.currentInfrastructure.getMobileDevices().values())
		{
			ObjEntry vehicleEntry = new ObjEntry();
			int blockSize = calculateBlockSize(vehicle);
			ArrayList<Integer> candidateNodes = selectCandidateNodes(vehicle, compNodes);
			double goals[] = calculateGoals(vehicle);
			
			vehicleEntry.blockSize = blockSize;
			vehicleEntry.candidateNodes = candidateNodes;
			vehicleEntry.goals = goals;
			
			
			objectivesPerVehicle.put(v, vehicleEntry);
			v++;
			
		}	
			Map<String, String> config = new HashMap<String, String>();
			config.put("model", "true");
			config.put("timeout", "120000");
			config.put("well_sorted_check", "true");
			ctx = new Context(config);
			Solver solver = ctx.mkSolver();
			int nodeNum = this.currentInfrastructure.getEdgeNodes().size() + this.currentInfrastructure.getCloudNodes().size();
			int numberOfTransactions = currentTransactionPool.getTransactionCount();
			
			IntExpr[][] assVar = new IntExpr[numberOfTransactions][numberOfVehicles];
			IntExpr[][] offVar = new IntExpr[numberOfVehicles][nodeNum];

			for(int i = 0; i < numberOfTransactions; i++)
				for(int j = 0; j < numberOfVehicles; j++)
					assVar[i][j] = ctx.mkIntConst("x"+i+""+j);
			
			for(int i = 0; i < numberOfVehicles; i++)
			{
				ArrayList<Integer> currCandidateNodes = objectivesPerVehicle.get(i).candidateNodes;
				for(int j = 0; j < currCandidateNodes.size(); j++)
				{
					int k = currCandidateNodes.get(j);
					offVar[i][k] = ctx.mkIntConst("o"+i+""+k);
				}
			}
			BoolExpr[] assignment = new BoolExpr[numberOfTransactions];

			for(int i = 0; i < numberOfTransactions; i++)
			{
				BoolExpr domain = ctx.mkBool(true);
				ArithExpr sum = ctx.mkInt(0);
				for(int j = 0; j < numberOfVehicles; j++) 
				{
					domain = ctx.mkAnd(domain,
							ctx.mkOr(
									ctx.mkEq(assVar[i][j],ctx.mkInt(0)),
									ctx.mkEq(assVar[i][j],ctx.mkInt(1))
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
				ArrayList<Integer> currCandidateNodes = objectivesPerVehicle.get(i).candidateNodes;
				ArithExpr sumT = ctx.mkAdd(ctx.mkInt(0));
				for(int k = 0; k < numberOfTransactions; k++)
					sumT = ctx.mkAdd(sumT,assVar[k][i]);
				for(int j = 0; j < currCandidateNodes.size(); j++)
				{
					int k = currCandidateNodes.get(j);
					domain = ctx.mkAnd(domain,
							ctx.mkOr(
									ctx.mkEq(offVar[i][k].simplify(),ctx.mkInt(0)),
									ctx.mkEq(offVar[i][k].simplify(),ctx.mkInt(1))
									)
							);
					sum = ctx.mkAdd(sum,offVar[i][k]);
				}
				//each value is 0,1, each task is assigned at most at 1 node
				offInd[i] = (IntExpr) sum.simplify();
				offload[i] = ctx.mkAnd(domain,ctx.mkLe(sum, ctx.mkInt(1)),ctx.mkGt(sumT, ctx.mkInt(0)));//,ctx.mkEq(offInd[i], sum));
			}
			
			//each vehicle does not get more transactions than its candidate block size
			BoolExpr[] boundTrans = new BoolExpr[numberOfVehicles];
			
			for(int j = 0; j < numberOfVehicles; j++)
			{
				ArithExpr sum = ctx.mkInt(0);
				for(int i = 0; i < numberOfTransactions; i++)
					sum = ctx.mkAdd(sum,assVar[i][j]);
				boundTrans[j] = ctx.mkLe(sum, ctx.mkInt(objectivesPerVehicle.get(j).blockSize));
					
			}
			RealExpr[][] runtimes = new RealExpr[numberOfTransactions][numberOfVehicles];
			RealExpr[][] costMatrix = new RealExpr[numberOfTransactions][numberOfVehicles];
			RealExpr[][] energyMatrix = new RealExpr[numberOfTransactions][numberOfVehicles];
			
			RealExpr[][] runtimesOffload = new RealExpr[numberOfVehicles][nodeNum];
			RealExpr[][] costMatrixOffload = new RealExpr[numberOfVehicles][nodeNum];
			RealExpr[][] energyMatrixOffload = new RealExpr[numberOfVehicles][nodeNum];
			
			ArrayList<Transaction> transactionPool = currentTransactionPool.getTransactions();
			
			final double prob = 0.5;
						
			//System.out.println("RUNTIME");
			for(int i = 0; i < transactionPool.size(); i++)
			{
				for(int j = 0; j < numberOfVehicles; j++)
				{
					double MIQuantile = computeQuantile(this.currentInfrastructure.getMobileDevices().get("vehicle_"+j), prob);
					
					runtimes[i][j] = ctx.mkReal(""+computeQRuntime(MIQuantile,this.currentInfrastructure.getMobileDevices().get("vehicle_"+j)));
					costMatrix[i][j] = ctx.mkReal(""+0.0);
					energyMatrix[i][j] = ctx.mkReal(computeQEnergy(this.currentInfrastructure.getMobileDevices().get("vehicle_"+j),this.currentInfrastructure.getMobileDevices().get("vehicle_"+j),MIQuantile));
					//runtimes[i][j] = ctx.mkReal(""+10.0);
					//costMatrix[i][j] = ctx.mkReal(""+10.0);
					//energyMatrix[i][j] = ctx.mkReal(""+10.0);
				}
				
				//System.out.println("");
			}
			
			
				
					
				for(int i = 0; i < numberOfVehicles; i++) 
				{
					ArithExpr rtTransSum = ctx.mkReal(""+0.0);
					ArithExpr costTransSum = ctx.mkReal(""+0.0);
					ArithExpr energyTransSum = ctx.mkReal(""+0.0);
					ArrayList<Integer> currCandidateNodes = objectivesPerVehicle.get(i).candidateNodes;
					for(int j = 0; j < currCandidateNodes.size(); j++) 
					{
						int k = currCandidateNodes.get(j);
						double MIQuantile = computeQuantile(compNodes.get(k), prob);
						String qrt = computeQRuntimeOffload(
								MIQuantile
								,this.currentInfrastructure.getMobileDevices().get("vehicle_"+i)
								,compNodes.get(k));
						if(qrt.equals("NAN"))
							continue;
						try {
						rtTransSum = ctx.mkMul(
								ctx.mkReal(objectivesPerVehicle.get(i).blockSize),
								ctx.mkReal(
										computeQRuntimeOffload(
										MIQuantile
										,this.currentInfrastructure.getMobileDevices().get("vehicle_"+i)
										,compNodes.get(k))
										)
								);
								
						costTransSum = ctx.mkMul(
								ctx.mkReal(objectivesPerVehicle.get(i).blockSize),
								ctx.mkReal(computeQCostOffload(
										MIQuantile
										,this.currentInfrastructure.getMobileDevices().get("vehicle_"+i)
										,compNodes.get(k))) 
								);
						energyTransSum = ctx.mkMul(
								ctx.mkReal(objectivesPerVehicle.get(i).blockSize),
								ctx.mkReal(computeQEnergyOffload(
										MIQuantile
										,this.currentInfrastructure.getMobileDevices().get("vehicle_"+i)
										,compNodes.get(k))));
						}
						catch(Throwable t)
						{
							System.err.println("Error: " + qrt);
							t.printStackTrace();
						}
						runtimesOffload[i][k] = (RealExpr) rtTransSum.simplify();
						costMatrixOffload[i][k] = (RealExpr) costTransSum.simplify();
						energyMatrixOffload[i][k] = (RealExpr) energyTransSum.simplify();
						//runtimesOffload[i][j] = ctx.mkReal(""+0.5);
						//costMatrixOffload[i][j] = ctx.mkReal(""+0.5);
						//energyMatrixOffload[i][j] = ctx.mkReal(""+0.5);
						}
						
					//rtTransSum = ctx.mkDiv(rtTransSum, ctx.mkReal(""+objectivesPerVehicle.get(i).blockSize));
					
					
					 
				//}* 
					
				
			}
			
			RealExpr[] runtimeBlockOffload,costBlockOffload,energyBlockOffload;
			
			runtimeBlockOffload = new RealExpr[numberOfVehicles];
			costBlockOffload = new RealExpr[numberOfVehicles];
			energyBlockOffload = new RealExpr[numberOfVehicles];
			
			for(int i = 0; i < numberOfVehicles; i++) 
			{
				runtimeBlockOffload[i] = ctx.mkReal("0.0");
				costBlockOffload[i] = ctx.mkReal("0.0");
				energyBlockOffload[i] = ctx.mkReal("0.0");
				ArrayList<Integer> currCandidateNodes = objectivesPerVehicle.get(i).candidateNodes;
				for(int j = 0; j < currCandidateNodes.size(); j++) 
				{
					int k = currCandidateNodes.get(j);
					double MIQuantile = computeQuantile(compNodes.get(k), prob);
					String qrt = computeQRuntimeOffload(
							MIQuantile
							,this.currentInfrastructure.getMobileDevices().get("vehicle_"+i)
							,compNodes.get(k));
					if(qrt.equals("NAN"))
						continue;
					runtimeBlockOffload[i] = (RealExpr) ctx.mkAdd(
							ctx.mkMul(offVar[i][k],
									runtimesOffload[i][k]),
							runtimeBlockOffload[i]);
					costBlockOffload[i] = (RealExpr) ctx.mkAdd(
							ctx.mkMul(offVar[i][k],	costMatrixOffload[i][k]),
							costBlockOffload[i]);
					energyBlockOffload[i] = (RealExpr) ctx.mkAdd(
							ctx.mkMul(offVar[i][k],energyMatrixOffload[i][k]),
							energyBlockOffload[i]);
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
								
				for(int i = 1; i < numberOfTransactions; i++)
				{
					rt = ctx.mkMul(runtimes[i][j],assVar[i][j]);
					runtimeForVehicle[j] = ctx.mkAdd(runtimeForVehicle[j],rt);
					cost = ctx.mkMul(costMatrix[i][j],assVar[i][j]);
					costForVehicle[j] = ctx.mkAdd(costForVehicle[j],cost);
					energy = ctx.mkMul(energyMatrix[i][j],assVar[i][j]);
					energyForVehicle[j] = ctx.mkAdd(energyForVehicle[j],energy);
				}
				runtimeForVehicle[j] = (ArithExpr) runtimeForVehicle[j];
				costForVehicle[j] = (ArithExpr) costForVehicle[j];
				energyForVehicle[j] = (ArithExpr) energyForVehicle[j];
			}
			
			
				
			
			for(int j = 0; j < numberOfVehicles; j++)
			{	
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
						ctx.mkReal(""+goals[1]));
				
				energyConstraints[j] = ctx.mkLe(
									ctx.mkAdd(
											ctx.mkMul(ctx.mkSub(ctx.mkInt(1),offInd[j]),energyForVehicle[j]),
											ctx.mkMul(offInd[j],energyBlockOffload[j])),
									ctx.mkReal(""+goals[2])
									);								

			}

			BoolExpr vehicleConstraints[] = new BoolExpr[numberOfVehicles];
			Goal g4 = ctx.mkGoal(true, false, false);
			for(int i = 0; i < numberOfVehicles; i++) 
				vehicleConstraints[i] = ctx.mkAnd(rtConstraints[i],costConstraints[i],energyConstraints[i],offload[i],boundTrans[i]);
				//vehicleConstraints[i] = ctx.mkAnd(rtConstraints[i],costConstraints[i],energyConstraints[i],boundTrans[i]);
				
			g4.add(assignment);
			g4.add(vehicleConstraints);
			
			
			Tactic t0 = ctx.mkTactic("simplify");
			Tactic t1 = ctx.mkTactic("solve-eqs");
			
			ApplyResult ar = t0.apply(g4);
			
			ar = ctx.andThen(t0, t1).apply(g4);
			//solver.add(vehicleConstraints);
			//solver.add(assignment);
			
			for (BoolExpr e : ar.getSubgoals()[0].getFormulas())
	            solver.add(e);
			
			System.out.println("Starting solver!");
			Status status = solver.check();
			System.out.println("Solver ended!");
			
			Model model = null;
			if(status == Status.SATISFIABLE)
			{
				model = solver.getModel();
				System.out.println("Satisfiable!");
				
				for(int i = 0; i < numberOfTransactions; i++)
					for(int j = 0; j < numberOfVehicles; j++)
					{
						Transaction curr = transactionPool.get(i);
						if(model.getConstInterp(assVar[i][j]).equals(ctx.mkInt(1))) 
						{
								boolean offloaded = false;
								ArrayList<Integer> currCandidateNodes = objectivesPerVehicle.get(j).candidateNodes;
								for(int k = 0; k < currCandidateNodes.size(); k++)
								{
									int w = currCandidateNodes.get(k);
									if(model.getConstInterp(offVar[j][w]).equals(ctx.mkInt(1))) 
									{
										//System.out.println("Vehicle "+ j + " offloads on " + k);
										
										curr.setMillionsOfInstruction(computeRealRuntime(curr, compNodes.get(w)));
										curr.setQuantileOfMI(computeQuantile(compNodes.get(w),prob));
										currScheduling.put(curr, compNodes.get(w));
										currScheduling.addRuntime(curr,this.currentInfrastructure.getMobileDevices().get("vehicle_"+j) ,compNodes.get(w),this.currentInfrastructure);
										currScheduling.addCost(curr,this.currentInfrastructure.getMobileDevices().get("vehicle_"+j), compNodes.get(w), this.currentInfrastructure);
										//currScheduling.addQuantileCost(curr,this.currentInfrastructure.getMobileDevices().get("vehicle_"+j), compNodes.get(w), this.currentInfrastructure);
										currScheduling.addEnergyConsumption(curr, this.currentInfrastructure.getMobileDevices().get("vehicle_"+j),compNodes.get(w),this.currentInfrastructure);
										//currScheduling.addQuantileEnergyConsumption(curr, this.currentInfrastructure.getMobileDevices().get("vehicle_"+j),compNodes.get(k),this.currentInfrastructure);
										offloaded = true;
									}
								}
							if(!offloaded)
							{
								//System.out.println("Vehicle " + this.currentInfrastructure.getMobileDevices().get("vehicle_"+j));
								curr.setMillionsOfInstruction(computeRealRuntime(curr, this.currentInfrastructure.getMobileDevices().get("vehicle_"+j)));
								curr.setQuantileOfMI(computeQuantile(this.currentInfrastructure.getMobileDevices().get("vehicle_"+j),prob));
								currScheduling.put(curr, this.currentInfrastructure.getMobileDevices().get("vehicle_"+j));
								currScheduling.addRuntime(curr,this.currentInfrastructure.getMobileDevices().get("vehicle_"+j),this.currentInfrastructure.getMobileDevices().get("vehicle_"+j), this.currentInfrastructure);
								currScheduling.addEnergyConsumption(curr, this.currentInfrastructure.getMobileDevices().get("vehicle_"+j), this.currentInfrastructure.getMobileDevices().get("vehicle_"+j), currentInfrastructure);
								//currScheduling.addQuantileEnergyConsumption(curr, this.currentInfrastructure.getMobileDevices().get("vehicle_"+j),this.currentInfrastructure.getMobileDevices().get("vehicle_"+j),this.currentInfrastructure);
							}
						}

					}
				ctx.close();
				double avgRt = 0.0;
				double avgQtRt = 0.0;
				double avgCost = 0.0;
				double avgQCost = 0.0;
				double avgProfit = 0.0;
				double avgEnergy = 0.0;
				int nDevices = 0;
				for(MobileDevice dev : this.getInfrastructure().getMobileDevices().values())
				{
					if(!Double.isNaN(dev.getAverageRuntime()) && Double.compare(dev.getAverageRuntime(),0.0) >= 0)
					{
						avgRt +=  dev.getAverageRuntime();
						avgQtRt += dev.getAverageQuantileRuntime();
						avgCost += dev.getCost();
						avgQCost += dev.getCost();
						avgProfit += dev.getNumberOfTransactions() * 1.0;
						avgEnergy += dev.getEnergyBudget();
						nDevices++;
					}
					//avgCost += dev.getAverageCost();
					//avgProfit += dev.getAverageProfit();
				}
				currScheduling.setRunTime(avgRt/nDevices);
				currScheduling.setUserCost((avgProfit/nDevices) - (avgCost/nDevices));
				currScheduling.setBatteryLifetime(avgEnergy / nDevices);
				//currScheduling.setUserCost(userCost);
				return currScheduling;
				
			}
			ctx.close();
			return null;
	}

	private String computeQEnergyOffload(double mIQuantile, MobileDevice mobileDevice, ComputationalNode computationalNode) {
		//return "0.0";
		Transaction dummy = new Transaction("dummy", new Hardware(mobileDevice.getCapabilities().getMaxCores(),0.0,0.0),
				mIQuantile, "dummy", 1.0,1.0);
		NETEnergyModel nModel = mobileDevice.getNetEnergyModel();
		double d = nModel.computeNETEnergy(dummy, computationalNode, currentInfrastructure);
		DecimalFormat df = new DecimalFormat("0.000");
		return df.format(d);
	}

	private String computeQCostOffload(double mIQuantile, MobileDevice mobileDevice, ComputationalNode computationalNode) {
		Transaction dummy = new Transaction("dummy", new Hardware(1, 0.0, 0.0),
				mIQuantile, "dummy", 1.0, 1.0);
		DecimalFormat df = new DecimalFormat("0.000");
		return df.format(computationalNode.computeCost(dummy, mobileDevice , currentInfrastructure));
	}
	
	private String computeQRuntime(double mIQuantile, MobileDevice mobileDevice) {
		// TODO Auto-generated method stub
		Transaction dummy = new Transaction("dummy", new Hardware(1, 0.0, 0.0), mIQuantile, "dummy", 1.0, 1.0);
		double t = dummy.getLocalRuntimeOnNode(mobileDevice, this.currentInfrastructure);
		if(Double.isFinite(t) && !Double.isNaN(t)) 
		{
			DecimalFormat df = new DecimalFormat("0.000");
			return df.format(Math.abs(t));
		}
		return "NAN";
	}

	private String computeQRuntimeOffload(double mIQuantile, MobileDevice mobileDevice, ComputationalNode computationalNode) {
		// TODO Auto-generated method stub
		Transaction dummy = new Transaction("dummy", new Hardware(1, 0.0, 0.0), mIQuantile, "dummy", 1.0, 1.0);
		double t = this.currentInfrastructure.getTransmissionTime(dummy, mobileDevice, computationalNode);
		if(Double.isFinite(t) && !Double.isNaN(t)) 
		{
			DecimalFormat df = new DecimalFormat("0.000");
			return df.format(Math.abs(t));
		}
		return "NAN";
	}

	private String computeQEnergy(ComputationalNode src, ComputationalNode n, double rtMI) {
		//return "0.0";
		CPUEnergyModel model = n.getCPUEnergyModel();
		Transaction dummy = new Transaction("dummy", new Hardware(n.getCapabilities().getMaxCores(),0.0,0.0)
				, rtMI, "dummy", 1.0, 1.0);
		double en = model.computeCPUEnergy(dummy, src, n, this.currentInfrastructure);
		DecimalFormat df = new DecimalFormat("0.000");
		return df.format(en);
	}
	
	private double computeQuantile(ComputationalNode n, double prob) {
		// TODO Auto-generated method stub
		//double maxCores = n.getCapabilities().getMaxCores();
		//double availableCores = n.getCapabilities().getAvailableCores();
		//double threadPercent = availableCores / maxCores;
		
		//double mean = 8.4424 * Math.pow(threadPercent, -0.6991);
		//double stDev = 8.6023 * Math.pow(threadPercent, -0.7021);
		
		//LogNormal distr = new LogNormal(mean,stDev);
		//return distr.getQuantile(prob)/1e6;
		BetaDistribution betaD;
		double minx,maxx;
		if(this.currentInfrastructure.getMobileDevices().containsKey(n.getId())) 
		{
			betaD = new BetaDistribution(0.65,4.21);
			minx = 0.055;
			maxx = 564.446;
		}
		else
		{
			minx = 0.074;
			maxx = 102.342;
			betaD = new BetaDistribution(0.788, 1.9633);
		}
		return (betaD.inverseCumulativeProbability(prob)*(maxx-minx) + minx)*1e3;
	}
	
	private double computeRealRuntime(Transaction curr, ComputationalNode n) {
		// TODO Auto-generated method stub
		//double maxCores = n.getCapabilities().getMaxCores();
		//double availableCores = n.getCapabilities().getAvailableCores();
		//double threadPercent = availableCores / maxCores;
		
		//double mean = 8.4424 * Math.pow(threadPercent, -0.6991);
		//double stDev = 8.6023 * Math.pow(threadPercent, -0.7021);
		
		//LogNormal distr = new LogNormal(mean,stDev);
		//return distr.doubleValue()/1e6;
		BetaDistribution betaD;
		double minx,maxx;
		if(this.currentInfrastructure.getMobileDevices().containsKey(n.getId())) 
		{
			betaD = new BetaDistribution(0.65,4.21);
			minx = 0.055;
			maxx = 564.446;
		}
		else
		{
			minx = 0.074;
			maxx = 102.342;
			betaD = new BetaDistribution(0.788, 1.9633);
		}
		double x = Math.random();
		return (betaD.inverseCumulativeProbability(x)*(maxx-minx) + minx)*1e3;
	}

	
	
	private double[] calculateGoals(MobileDevice currentVehicle) {
		// TODO Auto-generated method stub
		//double[] goals = {60.0, 19.0*0.1, Double.MAX_VALUE};
		double[] goals = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		return goals;
	}

	private ArrayList<Integer> selectCandidateNodes(MobileDevice vehicle, ArrayList<ComputationalNode> compNodes) {
		ArrayList<Integer> candidateNodes = new ArrayList<Integer>();
		int threshold = 24;

		for(int i = 0; i < compNodes.size() && candidateNodes.size() < threshold; i++) 
			//if(this.currentInfrastructure.getDistanceBetweenNodes(vehicle, compNodes.get(i))<= 2.0)
		//for(int i = 0; i < compNodes.size(); i++)		
			candidateNodes.add(i);
		//System.out.println(candidateNodes.size());
		return candidateNodes;
	}

	private int calculateBlockSize(MobileDevice vehicle) {
		// TODO Auto-generated method stub
		//Transaction curr = new Transaction("dummy", new Hardware(1,0.0,0.0),0.0, "all", 0.0,0);
		//return (int) vehicle.getCapabilities().getMaxCores();
		return 16;
	}

	public static void loadLibrary()
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
