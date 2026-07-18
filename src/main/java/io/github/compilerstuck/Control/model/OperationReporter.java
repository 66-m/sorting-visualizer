package io.github.compilerstuck.Control.model;

@FunctionalInterface
public interface OperationReporter {
    void report(String operation);

    OperationReporter NOOP = operation -> {};
}
