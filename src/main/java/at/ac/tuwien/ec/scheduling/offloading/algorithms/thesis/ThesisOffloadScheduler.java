package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;

public abstract class ThesisOffloadScheduler extends OffloadScheduler {

  // TODO: What should happen if the device has no battery anymore?
  @Override
  protected boolean isValid(
      OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
    if (s.getMillionsOfInstruction() == 0) return true;
    boolean compatible = n.isCompatible(s);
    boolean offloadPossible = isOffloadPossibleOn(s, n);
    // check links
    return compatible && offloadPossible;
  }

  protected synchronized void deploy(double currentRuntime, OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
    super.deploy(deployment, s, n);
    s.setStartTime(currentRuntime);
  }
}
