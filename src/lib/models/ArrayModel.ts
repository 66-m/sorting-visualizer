/**
 * ArrayModel - Core array data structure for sorting visualization
 *
 * Manages the array being sorted and tracks metrics like comparisons, swaps, writes.
 * This is a TypeScript port of the Java ArrayModel class.
 */

export enum Marker {
	NONE = 0,
	SET = 1,
	COMPARE = 2
}

export interface ArrayMetrics {
	comparisons: number;
	swaps: number;
	writes: number;
	writesAux: number;
	realTime: number;
	sortedPercentage: number;
	segments: number;
}

export class ArrayModel {
	private array: number[];
	private markers: Marker[];
	private comparisons = 0;
	private swaps = 0;
	private writes = 0;
	private writesAux = 0;
	private realTime = 0;

	constructor(size: number) {
		this.array = new Array(size);
		this.markers = new Array(size).fill(Marker.NONE);
		this.initialize();
	}

	/**
	 * Initializes the array with values from 1 to size
	 */
	private initialize(): void {
		for (let i = 0; i < this.array.length; i++) {
			this.array[i] = i + 1;
		}
	}

	/**
	 * Gets the value at the specified index
	 */
	getValue(index: number): number {
		return this.array[index];
	}

	/**
	 * Sets the value at the specified index and increments write count
	 */
	setValue(index: number, value: number): void {
		this.array[index] = value;
		this.writes++;
	}

	/**
	 * Swaps two elements in the array
	 */
	swap(i: number, j: number): void {
		const temp = this.array[i];
		this.array[i] = this.array[j];
		this.array[j] = temp;
		this.swaps++;
		this.writes += 2;
	}

	/**
	 * Compares two elements and increments comparison count
	 */
	compare(i: number, j: number): number {
		this.comparisons++;
		return this.array[i] - this.array[j];
	}

	/**
	 * Sets a marker at the specified index for visualization
	 */
	setMarker(index: number, marker: Marker): void {
		if (index >= 0 && index < this.markers.length) {
			this.markers[index] = marker;
		}
	}

	/**
	 * Gets the marker at the specified index
	 */
	getMarker(index: number): Marker {
		return this.markers[index];
	}

	/**
	 * Clears all markers
	 */
	clearMarkers(): void {
		this.markers.fill(Marker.NONE);
	}

	/**
	 * Gets the length of the array
	 */
	getLength(): number {
		return this.array.length;
	}

	/**
	 * Gets a copy of the array
	 */
	getArray(): number[] {
		return [...this.array];
	}

	/**
	 * Sets the array to a new array
	 */
	setArray(newArray: number[]): void {
		this.array = [...newArray];
		this.markers = new Array(newArray.length).fill(Marker.NONE);
	}

	/**
	 * Resizes the array
	 */
	resize(newSize: number): void {
		this.array = new Array(newSize);
		this.markers = new Array(newSize).fill(Marker.NONE);
		this.initialize();
		this.resetMeasurements();
	}

	/**
	 * Resets all measurement counters
	 */
	resetMeasurements(): void {
		this.comparisons = 0;
		this.swaps = 0;
		this.writes = 0;
		this.writesAux = 0;
		this.realTime = 0;
	}

	/**
	 * Calculates the percentage of the array that is sorted
	 */
	getSortedPercentage(): number {
		let sorted = 0;
		for (let i = 0; i < this.array.length - 1; i++) {
			if (this.array[i] <= this.array[i + 1]) {
				sorted++;
			}
		}
		return sorted / (this.array.length - 1);
	}

	/**
	 * Calculates the number of sorted segments
	 */
	getSegments(): number {
		let segments = 1;
		for (let i = 0; i < this.array.length - 1; i++) {
			if (this.array[i] > this.array[i + 1]) {
				segments++;
			}
		}
		return segments;
	}

	/**
	 * Gets all metrics
	 */
	getMetrics(): ArrayMetrics {
		return {
			comparisons: this.comparisons,
			swaps: this.swaps,
			writes: this.writes,
			writesAux: this.writesAux,
			realTime: this.realTime,
			sortedPercentage: this.getSortedPercentage(),
			segments: this.getSegments()
		};
	}

	// Getters for individual metrics
	getComparisons(): number { return this.comparisons; }
	getSwaps(): number { return this.swaps; }
	getWrites(): number { return this.writes; }
	getWritesAux(): number { return this.writesAux; }
	getRealTime(): number { return this.realTime; }

	// Increment auxiliary writes
	incrementWritesAux(): void {
		this.writesAux++;
	}

	// Add to real time
	addRealTime(nanos: number): void {
		this.realTime += nanos;
	}
}
