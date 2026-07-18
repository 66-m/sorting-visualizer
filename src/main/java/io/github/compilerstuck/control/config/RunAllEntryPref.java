package io.github.compilerstuck.control.config;

/** One persisted run-all list row: algorithm id + selected flag. */
public record RunAllEntryPref(String id, boolean selected) {}
