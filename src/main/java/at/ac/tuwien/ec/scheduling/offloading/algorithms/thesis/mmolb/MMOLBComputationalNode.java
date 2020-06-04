package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MMOLBComputationalNode {
  private double makespan = 0;

  private final List<MobileSoftwareComponent> assignments = new ArrayList<>();
  private final Map<MobileSoftwareComponent, Double> times = new HashMap<>();
  private final ComputationalNode node;
  private final OffloadScheduling scheduling;
  private final MobileApplication currentApp;
  private final MobileCloudInfrastructure currentInfrastructure;
  private final BaseMMOLB algorithm;

  public MMOLBComputationalNode(
      ComputationalNode node,
      OffloadScheduling scheduling,
      MobileApplication currentApp,
      MobileCloudInfrastructure currentInfrastructure,
      BaseMMOLB algorithm) {
    this.node = node;
    this.scheduling = scheduling;
    this.currentApp = currentApp;
    this.currentInfrastructure = currentInfrastructure;
    this.algorithm = algorithm;
  }

  public double getValue(MobileSoftwareComponent currTask) {
    if (times.containsKey(currTask)) {
      return times.get(currTask);
    }

    return calcValue(currTask);
  }

  public MobileSoftwareComponent getMinMsc(List<MobileSoftwareComponent> notRemoveAble) {
    return this.times.keySet().stream()
        .filter(msc -> !notRemoveAble.contains(msc))
        .reduce((a, b) -> times.get(a) < times.get(b) ? a : b)
        .orElse(null);
  }

  public double getMakeSpan() {
    if (Double.isFinite(makespan)) {
      return makespan;
    }

    return calcMakeSpan();
  }

  public ComputationalNode getNode() {
    return node;
  }

  public List<MobileSoftwareComponent> getAssignments() {
    return assignments;
  }

  public void add(MobileSoftwareComponent currTask) {
    this.assignments.add(currTask);
    this.refresh();
  }

  public void remove(MobileSoftwareComponent currTask) {
    this.assignments.remove(currTask);
    this.refresh();
  }

  private void refresh() {
    calcMakeSpan();
    times.keySet().forEach(this::calcValue);
  }

  private double calcMakeSpan() {
    makespan =
        Collections.max(assignments.stream().map(this.times::get).collect(Collectors.toList()));
    return makespan;
  }

  private double calcValue(MobileSoftwareComponent currTask) {
    double value = Double.NaN;
    if (!this.currentInfrastructure.getMobileDevices().containsValue(node)
        || node.getId().equals(currTask.getUserId())) {
      value = algorithm.calcAssignmentValue(scheduling, currTask, this);
    }

    times.put(currTask, value);
    return value;
  }
}
