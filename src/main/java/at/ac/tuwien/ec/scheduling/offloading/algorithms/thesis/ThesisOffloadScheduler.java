package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

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

      MobileSoftwareComponent currTask;
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

      while ((currTask = readyTasks.poll()) != null) {

        ComputationalNode target = null;

        if (!currTask.isOffloadable()) {
          if (isValid(
              scheduling,
              currTask,
              (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()))) {
            target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
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

    double end = System.nanoTime();
    scheduling.setExecutionTime(end - start);
    ArrayList<OffloadScheduling> result = new ArrayList<>();
    result.add(scheduling);
    return result;
  }

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

  protected synchronized void deploy(
      double currentRuntime,
      OffloadScheduling deployment,
      MobileSoftwareComponent s,
      ComputationalNode n) {
    super.deploy(deployment, s, n);
    s.setStartTime(currentRuntime);
  }

  protected abstract ComputationalNode findTarget(
      MobileSoftwareComponent currTask, OffloadScheduling scheduling);

  protected abstract void prepareAlgorithm();
}
