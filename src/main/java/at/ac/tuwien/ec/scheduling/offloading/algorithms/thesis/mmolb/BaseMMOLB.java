package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.stream.IntStream;
import scala.Tuple2;

public abstract class BaseMMOLB extends ThesisOffloadScheduler {
  public BaseMMOLB(MobileApplication A, MobileCloudInfrastructure I) {
    super();
    setMobileApplication(A);
    setInfrastructure(I);
  }

  public BaseMMOLB(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  protected void prepareAlgorithm() {}

  @Override
  protected void processReadyTasks(
      double currentRuntime,
      PriorityQueue<MobileSoftwareComponent> readyTasks,
      OffloadScheduling scheduling,
      PriorityQueue<MobileSoftwareComponent> scheduledNodes) {

    if (processNonOffloadableTasks(currentRuntime, readyTasks, scheduling, scheduledNodes)) {
      List<ComputationalNode> allNodes = currentInfrastructure.getAllNodesWithMobile();
      MobileSoftwareComponent[] tasks =
          readyTasks.stream()
              .filter(MobileSoftwareComponent::isOffloadable)
              .toArray(MobileSoftwareComponent[]::new);
      HashMap<ComputationalNode, List<Integer>> assignments = new HashMap<>();

      double[][] assignmentValues = new double[tasks.length][allNodes.size()];
      boolean[] taskAssigned = new boolean[tasks.length];

      for (int i = 0; i < tasks.length; i++) {
        MobileSoftwareComponent currTask = tasks[i];
        for (int j = 0; j < allNodes.size(); j++) {
          assignmentValues[i][j] = calcAssignmentValue(scheduling, currTask, allNodes.get(j));
        }
      }

      for (int i = 0; i < tasks.length; i++) {
        int taskIndex = findMaxTask(assignmentValues, taskAssigned);
        int nodeIndex = findMinNode(assignmentValues[taskIndex]);

        ComputationalNode cn = allNodes.get(nodeIndex);

        assignments.putIfAbsent(cn, new ArrayList<>());
        assignments.get(cn).add(taskIndex);

        double newAssignmentValue = calcNewAssignmentValue(cn, assignments, tasks);
        for (int time_i = 0; time_i < assignmentValues.length; time_i++) {
          assignmentValues[time_i][nodeIndex] += newAssignmentValue;
        }

        taskAssigned[taskIndex] = true;
      }

      for (Entry<ComputationalNode, List<Integer>> entry : assignments.entrySet()) {
        List<Integer> values = entry.getValue();
        if (values.size() > 0) {
          List<Integer> invalid = new ArrayList<>();
          while (invalid.size() < values.size()) {
            double makespan =
                values.stream()
                    .reduce(0.0, (time, index) -> time + tasks[index].getRunTime(), Double::sum);
            int minMscIndex =
                values.stream()
                    .filter(msc -> !invalid.contains(msc))
                    .reduce((a, b) -> tasks[a].getRunTime() > tasks[b].getRunTime() ? a : b)
                    .get();
            int nodeIndex = findMaxNode(assignmentValues[minMscIndex]);

            if (assignmentValues[minMscIndex][nodeIndex] < makespan) {
              values.remove(minMscIndex);
              assignments.get(allNodes.get(nodeIndex)).add(nodeIndex);
              break;
            } else {
              invalid.add(minMscIndex);
            }
          }
        }
      }

      for (Entry<ComputationalNode, List<Integer>> entry : assignments.entrySet()) {
        List<Integer> values = entry.getValue();
        if (values.size() > 0) {
          for (Integer index : values) {
            MobileSoftwareComponent msc = tasks[index];
            ComputationalNode cn = entry.getKey();

            if (isValid(scheduling, msc, cn)) {
              deploy(currentRuntime, scheduling, msc, entry.getKey());
              scheduledNodes.add(tasks[index]);
            }
          }
        }
      }
    }
  }

  protected abstract double calcAssignmentValue(
      OffloadScheduling scheduling, MobileSoftwareComponent msc, ComputationalNode node);

  protected abstract double calcNewAssignmentValue(
      ComputationalNode node,
      HashMap<ComputationalNode, List<Integer>> assignments,
      MobileSoftwareComponent[] tasks);

  private boolean processNonOffloadableTasks(
      double currentRuntime,
      PriorityQueue<MobileSoftwareComponent> readyTasks,
      OffloadScheduling scheduling,
      PriorityQueue<MobileSoftwareComponent> scheduledNodes) {

    MobileSoftwareComponent[] tasks =
        readyTasks.stream().filter(t -> !t.isOffloadable()).toArray(MobileSoftwareComponent[]::new);

    for (MobileSoftwareComponent currTask : tasks) {
      ComputationalNode userNode =
          (ComputationalNode) this.currentInfrastructure.getNodeById(currTask.getUserId());

      if (isValid(scheduling, currTask, userNode)) {
        deploy(currentRuntime, scheduling, currTask, userNode);
        scheduledNodes.add(currTask);
      } else {
        return false;
      }
    }

    return true;
  }

  private int findMaxTask(double[][] times, boolean[] taskAssigned) {
    int index = 0;
    double max = Double.MIN_VALUE;

    for (int i = 0; i < times.length; i++) {
      if (taskAssigned[i]) {
        continue;
      }

      for (int j = 0; j < times[i].length; j++) {
        double tempMax = times[i][j];
        if (tempMax > max) {
          max = tempMax;
          index = i;
        }
      }
    }

    return index;
  }

  private int findMinNode(double[] times) {
    return IntStream.range(0, times.length).reduce(0, (i, j) -> times[i] > times[j] ? j : i);
  }

  private int findMaxNode(double[] times) {
    return IntStream.range(0, times.length).reduce(0, (i, j) -> times[i] > times[j] ? i : j);
  }
}
