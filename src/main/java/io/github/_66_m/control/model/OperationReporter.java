package io.github._66_m.control.model;

@FunctionalInterface
public interface OperationReporter {
  void report(String operation);

  OperationReporter NOOP = operation -> {};
}
