# Sorting Algorithm Visualizer - SvelteKit Migration

This repository is currently undergoing a **major architectural refactor** from a Java/Processing desktop application to a **modern SvelteKit web application**.

## Migration Status

### ✅ Completed
- [x] SvelteKit project structure initialized with TypeScript
- [x] Core data models ported (ArrayModel, ArrayController)
- [x] Shuffle strategies implemented
- [x] Base SortingAlgorithm class created

### 🚧 In Progress
- [ ] Port 22 sorting algorithms to TypeScript
- [ ] Create canvas-based 2D visualization system (20 visualizations)
- [ ] Create WebGL/Three.js 3D visualization system (10 visualizations)
- [ ] Implement Web Audio API sound system
- [ ] Build main visualization page with controls
- [ ] Build settings UI panel
- [ ] Implement state management for sorting sessions

### 📋 Architecture

#### SvelteKit Structure (Following Official Best Practices)

```
src/
├── routes/                    # Filesystem-based routing
│   ├── +page.svelte          # Main visualization page
│   ├── +layout.svelte        # App layout
│   └── +page.ts              # Page load function (if needed)
├── lib/                      # Shared code ($lib alias)
│   ├── models/               # Data models
│   │   ├── ArrayModel.ts
│   │   └── ArrayController.ts
│   ├── algorithms/           # Sorting algorithms (22 total)
│   │   ├── SortingAlgorithm.ts
│   │   ├── BubbleSort.ts
│   │   ├── QuickSort.ts
│   │   └── ...
│   ├── visualizations/       # Canvas/WebGL visualizations (30 total)
│   │   ├── 2d/              # Canvas-based 2D visualizations
│   │   └── 3d/              # WebGL/Three.js 3D visualizations
│   ├── sound/                # Web Audio API
│   └── components/           # Svelte components
│       └── Settings.svelte
static/                       # Static assets (images, etc.)
java-legacy/                  # Original Java source (preserved for reference)
```

#### Key Technical Decisions

1. **TypeScript** for type safety and better development experience
2. **Canvas API** for 2D visualizations (replacing Processing 2D)
3. **Three.js + WebGL** for 3D visualizations (replacing Processing P3D)
4. **Web Audio API** for sound synthesis (replacing Java MIDI/Minim)
5. **Svelte 5 Runes** for reactive state management
6. **SvelteKit** for SSR + CSR hybrid rendering (SSR for initial load, CSR for interactivity)

### 🎯 Critical Requirements

Per the SvelteKit migration specification, this refactor MUST preserve:

- ✓ Identical visible UI and layout
- ✓ Identical component structure for rendering
- ✓ Identical routes and URLs
- ✓ Identical form behavior and interactions
- ✓ Identical state behavior from user's perspective
- ✓ All 22 sorting algorithms with identical behavior
- ✓ All 30 visualizations with identical appearance
- ✓ All controls and settings
- ✓ Metrics tracking (comparisons, swaps, writes, time)
- ✓ Sound/audio visualization

**User-facing functionality must be 1:1 identical** to the original Java application.

## Original Java Application

The original application features:
- **22 Sorting Algorithms**: Quick Sort, Merge Sort, Bubble Sort, Shell Sort, Heap Sort, etc.
- **30 Visualizations**: 20 2D (Bars, Scatter Plot, Circle, etc.) + 10 3D (Sphere, Cube, Pyramid, etc.)
- **Real-time Audio**: MIDI-based sound synthesis tied to array values
- **Metrics Dashboard**: Live tracking of comparisons, swaps, writes, sorted percentage
- **Settings Panel**: Array size, shuffle type, speed control, color gradients, algorithm selection
- **Results Table**: Comparison of algorithm performance after execution

### Running the Java Application (Legacy)

```bash
cd java-legacy
./mvnw clean package
java -jar target/sorting-visualizer.jar
```

## Development (SvelteKit App)

### Prerequisites
- Node.js 18+ and npm
- Modern browser with Canvas and WebGL support

### Setup

```bash
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173)

### Build

```bash
npm run build
npm run preview
```

## Project History

- **v1.4.1 and earlier**: Java/Processing desktop application
- **Current (refactor branch)**: Migration to SvelteKit web application

## License

MIT License - See [LICENSE](LICENSE)

## Acknowledgements

Thanks to [w0rthy](https://www.youtube.com/c/w0rthyA) and [Musicombo](https://www.youtube.com/c/Musicombo) for inspiring this project.

Thanks to [@micycle1](https://github.com/micycle1) for the Processing 4 core library mirror.
