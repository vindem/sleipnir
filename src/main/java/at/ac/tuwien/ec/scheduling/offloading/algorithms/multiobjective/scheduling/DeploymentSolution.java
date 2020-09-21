package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;

import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;



public class DeploymentSolution implements PermutationSolution<ComputationalNode>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OffloadScheduling deployment;
	private HashMap<Object,Object> solutionAttributes;
	private MobileApplication A;
	private MobileCloudInfrastructure I;
	private double runTime = 0.0, cost = 0.0, battery = 0.0, providerCost = 0.0;
		
	public DeploymentSolution(OffloadScheduling deployment, MobileApplication A, MobileCloudInfrastructure I) throws CloneNotSupportedException
	{
		this.deployment = (OffloadScheduling) deployment.clone();
		this.A = (MobileApplication) A.clone();
		this.I = (MobileCloudInfrastructure) I.clone();
		solutionAttributes = new HashMap<Object,Object>();
		solutionAttributes.put("feasible", true);
	}
	
	@Override
	public Object getAttribute(Object arg0) {
		return solutionAttributes.get(arg0);
	}
	@Override
	public int getNumberOfObjectives() {
		return 4;
	}
	@Override
	public int getNumberOfVariables() {
		return deployment.size();
	}
	@Override
	public double getObjective(int arg0) {
		switch(arg0)
		{
		case 0: return runTime;
		case 1: return cost;
		case 2: return battery;
		case 3: return providerCost;
		default: return runTime;
		}
	}
	
	public String getVariableValueString(int arg0) {
		return deployment.get(deployment.keySet().toArray()[arg0]).toString();
	}
	@Override
	public void setAttribute(Object arg0, Object arg1) {
		solutionAttributes.put(arg0, arg1);
	}
	
	@Override
	public void setObjective(int arg0, double arg1) {
		switch(arg0)
		{
		case 0: deployment.setRunTime(arg1);
				runTime = arg1;
				break;
		case 1: deployment.setUserCost(arg1);
				cost = arg1;
				break;
		case 2: deployment.setBatteryLifetime(arg1);
				battery = SimulationSetup.batteryCapacity - arg1;
				break;
		case 3: deployment.setProviderCost(arg1);
				providerCost = arg1;
		}		
	}
	
	public ComputationalNode getVariableValue(int arg0) {
		return (ComputationalNode) deployment.get(deployment.keySet().toArray()[arg0]);
	}

	public void setVariableValue(int arg0, ComputationalNode arg1) {
		MobileSoftwareComponent sc = (MobileSoftwareComponent) deployment.keySet().toArray()[arg0];
		ComputationalNode actual = (ComputationalNode) deployment.get(sc);
		if(actual!=null)
			replace(deployment,sc,actual);
		else
			deploy(deployment,sc,arg1);
		//sc.setRuntime(sc.getRuntimeOnNode(arg1, I));
	}

	@Override
	public DeploymentSolution copy() {
		DeploymentSolution s;
		try {
			s = new DeploymentSolution(deployment,A,I);
			return s;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public OffloadScheduling getDeployment()
	{
		return deployment;
	}
	
	private void deploy(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
        //SimulationSetup.logger.info("Adding " + s.getId() + " = " + deployment.containsKey(s));
    	deployment.put(s, n);
        deployment.addCost(s,n, I);
        deployment.addEnergyConsumption(s, n, I);
        deployment.addProviderCost(s,n,I);
        //deployment.addRuntime(s, n, I);
        //System.out.println(deployment + " " + deployment.size());
        n.deploy(s);
        deployLinks(deployment, s, n);
    }

    private void replace(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
        if (deployment.containsKey(s)) {
        	n.undeploy(s);
        	//deployment.removeRuntime(s, n, I);
        	//deployment.removeCost(s, n, I);
            //deployment.removeEnergyConsumption(s, n, I);
        	undeployLinks(deployment, s, n);
        	deployment.replace(s,n);
        	//deployment.addCost(s,n, I);
            //deployment.addEnergyConsumption(s, n, I);
            n.deploy(s);
            deployLinks(deployment, s, n);
      }
       // System.out.println("UNDEP"+deployment);
    }
    
    private void deployLinks(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
        for (SoftwareComponent c : deployment.keySet()) {
            ComputationalNode m = (ComputationalNode) deployment.get(c);
            Tuple2<SoftwareComponent,SoftwareComponent> couple1 
            	= new Tuple2<SoftwareComponent,SoftwareComponent>(c, s);
            Tuple2<SoftwareComponent,SoftwareComponent> couple2 = new Tuple2<SoftwareComponent,SoftwareComponent>(s, c);

            if (A.hasDependency(couple1._1(), couple1._2()) 
            		&& A.hasDependency(couple1._2(), couple1._1())) {
                QoSProfile req1 = A.getDependency((MobileSoftwareComponent) c, (MobileSoftwareComponent) s).getDesiredQoS(); //c,s
                QoSProfile req2 = A.getDependency((MobileSoftwareComponent) s, (MobileSoftwareComponent) c).getDesiredQoS();
                Tuple2<String,String> c1 = 
                		new Tuple2<String,String>(m.getId(), n.getId()); // m,n
                Tuple2<String,String> c2 =
                		new Tuple2<String,String>(n.getId(), m.getId()); // n,m
                if (I.getLink(c1) != null) {
                    QoSProfile pl1 = I.getLink(c1).getQoSProfile();
                    QoSProfile pl2 = I.getLink(c2).getQoSProfile();
                    pl1.setBandwidth(pl1.getBandwidth() - req1.getBandwidth());
                    pl2.setBandwidth(pl2.getBandwidth() - req2.getBandwidth());
                }
            }
        }

        /*for (ThingRequirement t : s.Theta) {
            ExactThing e = (ExactThing) t;
            if (n.isReachable(e.getId(), I, e.getQNodeThing(), e.getQThingNode())) {
                Couple c1 = new Couple(n.getId(), e.getId()); //c1 nodeThing

                QoSProfile pl1 = I.L.get(c1);
                QoSProfile pl2 = I.L.get(new Couple(e.getId(), n.getId()));

                pl1.setBandwidth(pl1.getBandwidth() - e.getQNodeThing().getBandwidth());
                pl2.setBandwidth(pl2.getBandwidth() - e.getQThingNode().getBandwidth());

            }
        }*/
    }
    
    private void undeployLinks(OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
        for (SoftwareComponent c : deployment.keySet()) {
            ComputationalNode m = (ComputationalNode) deployment.get(c);
            Tuple2<MobileSoftwareComponent,MobileSoftwareComponent> couple1 
        	= new Tuple2<MobileSoftwareComponent,MobileSoftwareComponent>((MobileSoftwareComponent) c, s);
        Tuple2<MobileSoftwareComponent,MobileSoftwareComponent> couple2 = 
        		new Tuple2<MobileSoftwareComponent,MobileSoftwareComponent>(s, (MobileSoftwareComponent) c);

            if (A.hasDependency(couple1._1(),couple2._2()) 
            		&& A.hasDependency(couple2._1(),couple1._2())) {
                QoSProfile al1 = A.getDependency(couple1._1(),couple1._2()).getDesiredQoS();
                QoSProfile al2 = A.getDependency(couple2._1(),couple2._2()).getDesiredQoS();
                Tuple2<String,String> c1 = new Tuple2<String,String>(m.getId(), n.getId());
                Tuple2<String,String> c2 = new Tuple2<String,String>(n.getId(), m.getId());
                if (I.getLink(c1) != null) {
                    QoSProfile pl1 = I.getLink(c1).getQoSProfile();
                    QoSProfile pl2 = I.getLink(c2).getQoSProfile();

                    pl1.setBandwidth(pl1.getBandwidth() + al1.getBandwidth());
                    pl2.setBandwidth(pl2.getBandwidth() + al2.getBandwidth());
                }
            }

        }

       
    }

	public MobileApplication getApplication() {
		return A;
	}

	public MobileCloudInfrastructure getInfrastructure() {
		return I;
	}
	
	public Number getLowerBound(int index) {
		return 0.0;
	}

	public double[] getObjectives() {
		// TODO Auto-generated method stub
		return null;
	}

	public ComputationalNode getVariable(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ComputationalNode> getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setVariable(int index, ComputationalNode variable) {
		// TODO Auto-generated method stub
		
	}

	public double[] getConstraints() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getConstraint(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setConstraint(int index, double value) {
		// TODO Auto-generated method stub
		
	}

	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean hasAttribute(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	public Map<Object, Object> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

		
}
