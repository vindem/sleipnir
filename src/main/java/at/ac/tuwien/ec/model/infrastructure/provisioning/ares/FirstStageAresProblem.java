package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import org.uma.jmetal.problem.ConstrainedProblem;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;

public class FirstStageAresProblem implements ConstrainedProblem<FirstStageAresSolution> {

	private MobileCloudInfrastructure infrastructure;
	
	public FirstStageAresProblem(MobileCloudInfrastructure inf)
	{
		this.infrastructure = inf;
	}
	
	@Override
	public int getNumberOfVariables() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfObjectives() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void evaluate(FirstStageAresSolution solution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FirstStageAresSolution createSolution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void evaluateConstraints(FirstStageAresSolution solution) {
		// TODO Auto-generated method stub
		
	}

}
