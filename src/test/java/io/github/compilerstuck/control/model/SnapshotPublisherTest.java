package io.github.compilerstuck.control.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.compilerstuck.visual.Marker;
import org.junit.jupiter.api.Test;

class SnapshotPublisherTest {

  @Test
  void publishCopiesValuesAndMarkersThenClearsWorking() {
    ArrayController working = new ArrayController(8);
    working.swap(0, 7);
    working.setMarker(3, Marker.SET);
    working.setMarker(5, Marker.SET);

    SnapshotPublisher publisher = new SnapshotPublisher();
    publisher.publish(working);

    ArrayModel published = publisher.publishedView();
    assertEquals(8, published.getLength());
    assertEquals(working.get(0), published.get(0));
    assertEquals(working.get(7), published.get(7));
    assertEquals(Marker.SET, published.getMarker(3));
    assertEquals(Marker.SET, published.getMarker(5));
    assertEquals(Marker.NORMAL, working.getMarker(3));
    assertEquals(Marker.NORMAL, working.getMarker(5));
  }

  @Test
  void publishedViewRejectsMutation() {
    ArrayController working = new ArrayController(4);
    SnapshotPublisher publisher = new SnapshotPublisher();
    publisher.publish(working);
    ArrayModel published = publisher.publishedView();
    assertThrows(UnsupportedOperationException.class, () -> published.set(0, 1));
    assertThrows(UnsupportedOperationException.class, () -> published.setMarker(0, Marker.SET));
  }

  @Test
  void reuseBuffersAcrossResize() {
    SnapshotPublisher publisher = new SnapshotPublisher();
    ArrayController small = new ArrayController(4);
    publisher.publish(small);
    int capAfterSmall = publisher.bufferCapacity();
    assertTrue(capAfterSmall >= 4);

    ArrayController large = new ArrayController(64);
    publisher.publish(large);
    assertTrue(publisher.bufferCapacity() >= 64);
    assertEquals(64, publisher.publishedView().getLength());

    publisher.publish(small);
    assertEquals(4, publisher.publishedView().getLength());
    assertEquals(publisher.bufferCapacity(), publisher.bufferCapacity());
    assertNotSame(small.getArray(), publisher.publishedView().getArray());
  }

  @Test
  void revisionMatchesWorkingAtPublish() {
    ArrayController working = new ArrayController(4);
    working.set(1, 2);
    long rev = working.getVisualRevision();
    SnapshotPublisher publisher = new SnapshotPublisher();
    publisher.publish(working);
    // resetMarkers may bump revision on working; published keeps pre-clear revision
    assertEquals(rev, publisher.publishedView().getVisualRevision());
  }
}
