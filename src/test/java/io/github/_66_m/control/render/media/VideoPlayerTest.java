package io.github._66_m.control.render.media;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class VideoPlayerTest {

  @Test
  void parsesProbeOutput() throws IOException {
    String out =
        "width=480\nheight=360\nr_frame_rate=30000/1001\navg_frame_rate=30000/1001\n"
            + "duration=219.0\n";
    VideoPlayer.Info info = VideoPlayer.parseProbe(out, true);
    assertEquals(480, info.width());
    assertEquals(360, info.height());
    assertEquals(29.97, info.fps(), 0.01);
    assertEquals(219.0, info.durationSec(), 1e-9);
    assertTrue(info.hasAudio());
  }

  @Test
  void fallsBackToRFrameRateAndDefaultFps() throws IOException {
    VideoPlayer.Info r =
        VideoPlayer.parseProbe("width=2\nheight=2\navg_frame_rate=0/0\nr_frame_rate=25/1\n", false);
    assertEquals(25.0, r.fps(), 1e-9);
    VideoPlayer.Info none = VideoPlayer.parseProbe("width=2\nheight=2\n", false);
    assertEquals(30.0, none.fps(), 1e-9);
    assertFalse(none.hasAudio());
  }

  @Test
  void rejectsOutputWithoutVideoStream() {
    assertThrows(IOException.class, () -> VideoPlayer.parseProbe("duration=3.0\n", true));
  }

  @Test
  void decodeSizeFitsInsideCapWithEvenDimensions() {
    assertArrayEquals(new int[] {480, 360}, VideoPlayer.decodeSize(480, 360, 1920, 1080));
    assertArrayEquals(new int[] {1920, 1080}, VideoPlayer.decodeSize(3840, 2160, 1920, 1080));
    int[] odd = VideoPlayer.decodeSize(1001, 777, 1920, 1080);
    assertEquals(0, odd[0] % 2);
    assertEquals(0, odd[1] % 2);
  }

  @Test
  void parsesRates() {
    assertEquals(24.0, VideoPlayer.parseRate("24/1"), 1e-9);
    assertEquals(0.0, VideoPlayer.parseRate("0/0"), 1e-9);
    assertEquals(12.5, VideoPlayer.parseRate("12.5"), 1e-9);
  }

  @Test
  void volumeScalesSigned16BitSamples() {
    byte[] pcm = {(byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0xC0}; // +16384, -16384
    VideoPlayer.applyVolume(pcm, 4, 0.5f);
    assertEquals(8192, (short) ((pcm[0] & 0xFF) | (pcm[1] << 8)));
    assertEquals(-8192, (short) ((pcm[2] & 0xFF) | (pcm[3] << 8)));
  }

  @Test
  void ffprobeIsLookedUpNextToAnExplicitFfmpeg() {
    assertEquals("ffprobe", VideoPlayer.siblingTool("ffmpeg", "ffprobe"));
    assertEquals("/opt/ff/ffprobe", VideoPlayer.siblingTool("/opt/ff/ffmpeg", "ffprobe"));
    assertEquals("C:\\ff\\ffprobe.exe", VideoPlayer.siblingTool("C:\\ff\\ffmpeg.exe", "ffprobe"));
  }
}
