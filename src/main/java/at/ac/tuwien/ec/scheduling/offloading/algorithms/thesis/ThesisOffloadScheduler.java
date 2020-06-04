package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class ThesisOffloadScheduler extends OffloadScheduler {

  @Override
  public ArrayList<OffloadScheduling> findScheduling() {
    double start = System.nanoTime();

    prepareAlgorithm();

    OffloadScheduling scheduling = new OffloadScheduling();
    PriorityQueue<MobileSoftwareComponent> scheduledNodes =
        new PriorityQueue<>(new FinishTimeComparator());

    int totalTaskNum = currentApp.getComponentNum();
    int totalFinishedTasks = 0;
    double currentRuntime = 0;

    while (totalFinishedTasks < totalTaskNum) {
      if (!scheduledNodes.isEmpty()) {
        MobileSoftwareComponent finishedTask = scheduledNodes.remove();
        currentApp.removeEdgesFrom(finishedTask);
        currentApp.removeTask(finishedTask);

        ((ComputationalNode) scheduling.get(finishedTask)).undeploy(finishedTask);
        currentRuntime += finishedTask.getRunTime();
        totalFinishedTasks++;
      }

      PriorityQueue<MobileSoftwareComponent> readyTasks =
          new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator()) {
            {
              addAll(
                  currentApp.readyTasks().stream()
                      .filter(rt -> !scheduledNodes.contains(rt))
                      .collect(Collectors.toSet()));
            }
          };

      if (readyTasks.isEmpty() && !scheduledNodes.isEmpty()) {
        continue;
      }

      processReadyTasks(currentRuntime, readyTasks, scheduling, scheduledNodes);
    }

    double end = System.nanoTime();
    scheduling.setExecutionTime(end - start);
    ArrayList<OffloadScheduling> result = new ArrayList<>();
    result.add(scheduling);
    return result;
  }

  protected void processReadyTasks(
      double currentRuntime,
      PriorityQueue<MobileSoftwareComponent> readyTasks,
      OffloadScheduling scheduling,
      PriorityQueue<MobileSoftwareComponent> scheduledNodes) {
    MobileSoftwareComponent currTask;

    while ((currTask = readyTasks.poll()) != null) {
      ComputationalNode target = null;

      if (!currTask.isOffloadable()) {
        ComputationalNode userNode = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
        if (isValid(
            scheduling,
            currTask,
            userNode)) {
          target = userNode;
        }
      } else {
        target = findTarget(currTask, scheduling);
      }

      if (target == null) {
        break;
      }

      // System.out.println(currTask.getId() + "->" + target.getId());
      deploy(currentRuntime, scheduling, currTask, target);
      scheduledNodes.add(currTask);
    }
  }

  @Override
  protected boolean isValid(
      OffloadScheduling deployment, MobileSoftwareComponent s, ComputationalNode n) {
    if (s.getMillionsOfInstruction() == 0) return true;
    boolean compatible = n.isCompatible(s);
    boolean offloadPossible = isOffloadPossibleOn(s, n);
    boolean linksOk = checkLinks(deployment,s,n);
    // check links
    return compatible && offloadPossible && linksOk;
  }

  protected synchronized void deploy(
      double currentRuntime,
      OffloadScheduling deployment,
      MobileSoftwareComponent s,
      ComputationalNode n) {
    super.deploy(deployment, s, n);
    s.setStartTime(currentRuntime);
  }

  protected ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling) {
    throw new NotImplementedException();
  };

  protected abstract void prepareAlgorithm();
}
