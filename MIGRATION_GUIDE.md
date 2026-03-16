# SvelteKit Migration - Implementation Guide

This document provides a detailed roadmap for completing the migration from Java/Processing to SvelteKit.

## Current Status

### ✅ Completed Foundation
1. **SvelteKit Project Setup**
   - Initialized with `npx sv create` (modern approach)
   - TypeScript configuration
   - Proper directory structure: `src/routes/`, `src/lib/`, `static/`
   - All dependencies in `devDependencies` as per SvelteKit best practices

2. **Core Data Models**
   - `ArrayModel.ts`: Array management, metrics tracking, markers
   - `ArrayController.ts`: Extends ArrayModel with shuffle strategies
   - Shuffle strategies: Random, Reverse, Almost Sorted, Sorted

3. **Algorithm Infrastructure**
   - `SortingAlgorithm.ts`: Base class with delay mechanism for visualization
   - Async/await pattern for browser-friendly execution
   - Delay callback system for visualization updates

## Remaining Work

### Phase 1: Algorithm Porting (22 algorithms)

Port each algorithm from Java to TypeScript following this pattern:

**Java to TypeScript Translation Guide:**
- `arrayController.get(i)` → `this.arrayController.getValue(i)`
- `arrayController.set(i, val)` → `this.arrayController.setValue(i, val)`
- `arrayController.swap(i, j)` → `this.arrayController.swap(i, j)`
- `arrayController.addComparisons(1)` → Direct comparison calls handle this
- `delay(new int[]{i, j})` → `await this.doDelay([i, j])`
- `while (...&& run)` → Use cancellation tokens/AbortSignal
- `MainController.setCurrentOperation(name)` → Emit event/callback

**Algorithms to Port:**
1. Bubble Sort
2. Quick Sort (Middle Pivot)
3. Quick Sort (Dual Pivot)
4. Merge Sort
5. Shell Sort
6. Selection Sort
7. Double Selection Sort
8. Insertion Sort
9. Heap Sort
10. Gravity Sort
11. Radix Sort (LSD, Base 10)
12. Gnome Sort
13. Comb Sort
14. Odd Even Sort
15. Cocktail Shaker Sort
16. Cycle Sort
17. Counting Sort
18. American Flag Sort
19. Bucket Sort
20. Pigeonhole Sort
21. Tim Sort
22. Bogo Sort

### Phase 2: Visualization System

#### Color Gradient System
Port from `java-legacy/src-main/io/github/compilerstuck/Visual/Gradient/`
- `ColorGradient.java` → Create interpolation system
- `MultiGradient.java` → Support multiple color stops

#### 2D Canvas Visualizations (20 total)
Create `src/lib/visualizations/2d/` with base class:

```typescript
export abstract class Visualization2D {
  protected canvas: HTMLCanvasElement;
  protected ctx: CanvasRenderingContext2D;
  protected arrayController: ArrayController;
  protected colorGradient: ColorGradient;

  abstract render(): void;
}
```

**Visualizations to port:**
1. Bars
2. Scatter Plot
3. Scatter Plot Linked
4. Number Plot
5. Disparity Graph
6. Disparity Graph Mirrored
7. Horizontal Pyramid
8. Color Gradient Graph
9. Circle
10. Disparity Circle
11. Disparity Circle Scatter
12. Disparity Circle Scatter Linked
13. Disparity Chords
14. Disparity Square Scatter
15. Swirl Dots
16. Phyllotaxis
17. Image Vertical
18. Image Horizontal
19. Hoops
20. Morphing Shell
21. Mosaic Squares

#### 3D WebGL Visualizations (10 total)
Use Three.js for 3D rendering. Create `src/lib/visualizations/3d/`:

```typescript
import * as THREE from 'three';

export abstract class Visualization3D {
  protected scene: THREE.Scene;
  protected camera: THREE.PerspectiveCamera;
  protected renderer: THREE.WebGLRenderer;
  protected arrayController: ArrayController;

  abstract update(): void;
}
```

**3D Visualizations:**
1. Sphere
2. Sphere Hoops
3. Spheric Disparity Lines
4. Disparity Sphere Hoops
5. Cube
6. Cubic Lines
7. Pyramid
8. Plane
9. Disparity Plane
10. (MorphingShell can be 2D or 3D)

### Phase 3: Sound System

Create `src/lib/sound/AudioSystem.ts` using Web Audio API:

```typescript
export class AudioSystem {
  private audioContext: AudioContext;
  private oscillator: OscillatorNode;
  private gainNode: GainNode;

  playNote(value: number, maxValue: number): void {
    // Map array value to frequency
    const frequency = 200 + (value / maxValue) * 800;
    // Play tone
  }
}
```

### Phase 4: Main Page (+page.svelte)

Create the main visualization page following SvelteKit patterns:

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { ArrayController, ShuffleType } from '$lib/models/ArrayController';
  import Settings from '$lib/components/Settings.svelte';

  let canvas: HTMLCanvasElement;
  let arrayController = new ArrayController(100);
  let isRunning = $state(false);

  onMount(() => {
    // Initialize visualization
  });
</script>

<div class="container">
  <canvas bind:this={canvas}></canvas>
  <Settings bind:arrayController bind:isRunning />
</div>
```

**Key Features:**
- Canvas for visualization
- Real-time metrics display
- Control buttons (Start, Stop, Reset)
- Algorithm progress indicator

### Phase 5: Settings Component

Create `src/lib/components/Settings.svelte` matching the Java UI:

**Controls needed:**
- Array size slider (10-2000)
- Shuffle type selector
- Speed control
- Sound on/off toggle
- Algorithm selection (checkboxes)
- Visualization selector
- Color gradient picker
- Display metrics toggle
- Display comparison table toggle
- Start/Stop/Reset buttons

### Phase 6: State Management

Create `src/lib/state/SortingSession.svelte.ts` using Svelte 5 runes:

```typescript
export class SortingSession {
  running = $state(false);
  currentAlgorithm = $state<string>('');
  showResults = $state(false);

  async runAlgorithms(algorithms: SortingAlgorithm[]) {
    this.running = true;
    for (const alg of algorithms) {
      if (!this.running) break;
      this.currentAlgorithm = alg.getName();
      await alg.sort();
    }
    this.showResults = true;
    this.running = false;
  }
}
```

## SvelteKit Best Practices Checklist

- ✅ Use filesystem-based routing (`src/routes/`)
- ✅ Server code in `+page.server.ts` or `+server.ts` (if needed)
- ✅ Client code in `+page.svelte`
- ✅ Shared code in `src/lib/` (accessible via `$lib` alias)
- ✅ Static assets in `static/`
- ✅ Use Svelte 5 runes (`$state`, `$derived`, `$effect`)
- ✅ All framework deps in `devDependencies`
- ❌ Never import server-only code into client code
- ✅ Use `onMount` for browser-only initialization
- ✅ SSR-safe (visualizations only run client-side)

## Testing Strategy

1. **Algorithm Correctness**: Verify each algorithm correctly sorts arrays
2. **Visual Parity**: Compare screenshots with Java version
3. **Performance**: Ensure smooth 60fps rendering
4. **Metrics Accuracy**: Validate comparison/swap/write counts match Java
5. **Cross-browser**: Test in Chrome, Firefox, Safari

## Deployment

```bash
npm run build
```

Output will be in `build/` directory, ready for deployment to:
- Vercel (recommended for SvelteKit)
- Netlify
- Static hosting (with adapter-static)

## Notes

- **Java legacy code** preserved in `java-legacy/` for reference
- **Processing API** replaced with Canvas/WebGL
- **Swing UI** replaced with Svelte components
- **Java MIDI** replaced with Web Audio API
- **Multi-threading** replaced with async/await + requestAnimationFrame

## Next Steps

1. Port 3-5 sample algorithms to establish pattern
2. Create base visualization classes
3. Implement one 2D visualization (Bars) as proof of concept
4. Build minimal working UI to test integration
5. Iterate on remaining algorithms and visualizations

---

**Total Remaining Work Estimate:**
- ~22 sorting algorithms × 30min = 11 hours
- ~30 visualizations × 1hr = 30 hours
- Sound system: 3 hours
- Main page UI: 4 hours
- Settings component: 3 hours
- State management: 2 hours
- Testing & refinement: 10 hours

**Total: ~63 hours of development work**
