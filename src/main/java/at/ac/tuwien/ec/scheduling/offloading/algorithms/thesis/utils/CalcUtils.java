package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.utils;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;

public class CalcUtils {
  public static double calcEST(
      MobileSoftwareComponent currTask,
      OffloadScheduling scheduling,
      ComputationalNode cn,
      MobileApplication currentApp,
      MobileCloudInfrastructure currentInfrastructure) {
    double readyTime = Double.MIN_VALUE;

    for (MobileSoftwareComponent cmp : currentApp.getPredecessors(currTask)) {
      double otherReadyTime =
          cmp.getFinishTime()
              + currentInfrastructure.getTransmissionTime(cmp, scheduling.get(cmp), cn);
      if (otherReadyTime > readyTime) {
        readyTime = cmp.getRunTime();
      }
    }

    double avail =
        cn.getDeployedComponents().stream()
            .map(MobileSoftwareComponent::getFinishTime)
            .max(Double::compareTo)
            .orElse(0.0);

    return Math.max(avail, readyTime);
  }

  public static double calcAverageComputationalCost(
      MobileSoftwareComponent msc, MobileCloudInfrastructure I) {
    double w_computational_cost = 0;
    int numberOfNodes = I.getAllNodes().size() + 1;
    for (ComputationalNode cn : I.getAllNodes()) {
      w_computational_cost += msc.getLocalRuntimeOnNode(cn, I);
    }
    w_computational_cost +=
        msc.getLocalRuntimeOnNode((ComputationalNode) I.getNodeById(msc.getUserId()), I);
    w_computational_cost = w_computational_cost / numberOfNodes;

    return w_computational_cost;
  }

  public static double calcAverageCommunicationCost(
      MobileSoftwareComponent msc, NetworkedNode from, MobileCloudInfrastructure I) {
    if (msc.isOffloadable()) {
      double c_communication_cost = 0;
      for (ComputationalNode cn : I.getAllNodes()) {
        double time = I.getTransmissionTime(msc, from, cn);
        if (time != Double.MAX_VALUE) {
          c_communication_cost += time;
        }
      }
      c_communication_cost = c_communication_cost / (I.getAllNodes().size());

      return c_communication_cost;
    }

    return 0;
  }
}
