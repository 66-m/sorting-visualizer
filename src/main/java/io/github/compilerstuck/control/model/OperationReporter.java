package io.github.compilerstuck.control.model;

@FunctionalInterface
public interface OperationReporter {
    void report(String operation);

    OperationReporter NOOP = operation -> {};
}
