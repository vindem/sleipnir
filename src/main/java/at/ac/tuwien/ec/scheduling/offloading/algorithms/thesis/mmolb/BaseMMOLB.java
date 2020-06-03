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

      MobileSoftwareComponent[] tasks =
          readyTasks.stream()
              .filter(MobileSoftwareComponent::isOffloadable)
              .toArray(MobileSoftwareComponent[]::new);

      if (tasks.length > 0) {
        List<ComputationalNode> allNodes = currentInfrastructure.getAllNodes();
        allNodes.addAll(currentInfrastructure.getMobileDevices().values());
        HashMap<ComputationalNode, List<Integer>> assignments = new HashMap<>();

        allNodes.forEach(
            cn -> {
              assignments.putIfAbsent(cn, new ArrayList<>());
            });

        double[][] assignmentValues = new double[tasks.length][allNodes.size()];
        boolean[] taskAssigned = new boolean[tasks.length];

        for (int i = 0; i < tasks.length; i++) {
          MobileSoftwareComponent currTask = tasks[i];
          for (int j = 0; j < allNodes.size(); j++) {
            ComputationalNode node = allNodes.get(j);
            if (this.currentInfrastructure.getMobileDevices().containsValue(node)
                && !node.getId().equals(currTask.getUserId())) {
              assignmentValues[i][j] = Double.NaN;
            } else {
              assignmentValues[i][j] = calcAssignmentValue(scheduling, currTask, allNodes.get(j));
            }
          }
        }

        for (int i = 0; i < tasks.length; i++) {
          int taskIndex = findMaxTask(assignmentValues, taskAssigned);
          MobileSoftwareComponent msc = tasks[taskIndex];
          int nodeIndex = findMinNode(assignmentValues[taskIndex], msc, scheduling, allNodes);
          if (nodeIndex < 0) {
            return;
          }

          ComputationalNode cn = allNodes.get(nodeIndex);
          assignments.get(cn).add(taskIndex);

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
                values.remove(Integer.valueOf(minMscIndex));
                assignments.get(allNodes.get(nodeIndex)).add(minMscIndex);
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
                // System.out.println(cn.getId() + "->" + msc.getId());
                deploy(currentRuntime, scheduling, msc, entry.getKey());
                scheduledNodes.add(tasks[index]);
              }
            }
          }
        }
      }
    }
  }

  protected abstract double calcAssignmentValue(
      OffloadScheduling scheduling, MobileSoftwareComponent currTask, ComputationalNode cn);

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
        if (Double.isFinite(tempMax) && tempMax > max) {
          max = tempMax;
          index = i;
        }
      }
    }

    return index;
  }

  private int findMinNode(
      double[] times,
      MobileSoftwareComponent msc,
      OffloadScheduling scheduling,
      List<ComputationalNode> allNodes) {

    int index;
    List<Integer> ignoreList = new ArrayList<>();

    while (ignoreList.size() < times.length) {

      index =
          IntStream.range(0, times.length)
              .filter(id -> !ignoreList.contains(id))
              .reduce(0, (acc, j) -> times[acc] > times[j] ? j : acc);

      if (isValid(scheduling, msc, allNodes.get(index))) {
        return index;
      }

      ignoreList.add(index);
    }
    return -1;
  }

  private int findMaxNode(double[] times) {
    return IntStream.range(0, times.length).reduce(0, (i, j) -> times[i] > times[j] ? i : j);
  }
}
