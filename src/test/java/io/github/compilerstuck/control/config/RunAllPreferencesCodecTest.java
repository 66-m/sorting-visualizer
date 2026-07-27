package io.github.compilerstuck.control.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RunAllPreferencesCodecTest {

  @Test
  void encodeDecodeRoundTrip() {
    List<RunAllEntryPref> input =
        List.of(new RunAllEntryPref("a", true), new RunAllEntryPref("b", false));
    String encoded = RunAllPreferencesCodec.encode(input);
    assertEquals("a:1,b:0", encoded);
    assertEquals(input, RunAllPreferencesCodec.decode(encoded));
  }

  @Test
  void emptyAndNull() {
    assertEquals("", RunAllPreferencesCodec.encode(List.of()));
    assertTrue(RunAllPreferencesCodec.decode(null).isEmpty());
    assertTrue(RunAllPreferencesCodec.decode("").isEmpty());
  }
}
