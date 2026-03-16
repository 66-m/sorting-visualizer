/**
 * ArrayController - Manages array state and shuffle operations
 *
 * This is a TypeScript port of the Java ArrayController class.
 * Coordinates array initialization, shuffling, and reset operations.
 */

import { ArrayModel } from './ArrayModel';

export enum ShuffleType {
	RANDOM = 'Random',
	REVERSE = 'Reverse',
	ALMOST_SORTED = 'Almost Sorted',
	SORTED = 'Sorted'
}

export class ArrayController extends ArrayModel {
	private shuffleType: ShuffleType = ShuffleType.RANDOM;

	constructor(size: number) {
		super(size);
		this.resetArray();
	}

	/**
	 * Resets the array by shuffling it according to the current shuffle type
	 */
	resetArray(): void {
		this.clearMarkers();
		this.resetMeasurements();

		// Initialize array with sequential values
		const arr = Array.from({ length: this.getLength() }, (_, i) => i + 1);

		// Apply shuffle strategy
		const shuffled = this.applyShuffle(arr);
		this.setArray(shuffled);
	}

	/**
	 * Applies the appropriate shuffle strategy to the array
	 */
	private applyShuffle(arr: number[]): number[] {
		switch (this.shuffleType) {
			case ShuffleType.RANDOM:
				return this.randomShuffle(arr);
			case ShuffleType.REVERSE:
				return this.reverseShuffle(arr);
			case ShuffleType.ALMOST_SORTED:
				return this.almostSortedShuffle(arr);
			case ShuffleType.SORTED:
				return arr; // Already sorted
			default:
				return this.randomShuffle(arr);
		}
	}

	/**
	 * Fisher-Yates shuffle algorithm
	 */
	private randomShuffle(arr: number[]): number[] {
		const result = [...arr];
		for (let i = result.length - 1; i > 0; i--) {
			const j = Math.floor(Math.random() * (i + 1));
			[result[i], result[j]] = [result[j], result[i]];
		}
		return result;
	}

	/**
	 * Reverse the array
	 */
	private reverseShuffle(arr: number[]): number[] {
		return [...arr].reverse();
	}

	/**
	 * Creates an almost sorted array (90% sorted)
	 */
	private almostSortedShuffle(arr: number[]): number[] {
		const result = [...arr];
		const swapCount = Math.floor(result.length * 0.1);

		for (let i = 0; i < swapCount; i++) {
			const idx1 = Math.floor(Math.random() * result.length);
			const idx2 = Math.floor(Math.random() * result.length);
			[result[idx1], result[idx2]] = [result[idx2], result[idx1]];
		}

		return result;
	}

	/**
	 * Sets the shuffle type
	 */
	setShuffleType(type: ShuffleType): void {
		this.shuffleType = type;
	}

	/**
	 * Gets the current shuffle type
	 */
	getShuffleType(): ShuffleType {
		return this.shuffleType;
	}

	/**
	 * Updates the array (called in render loop)
	 */
	update(): void {
		// Clear markers after each frame
		this.clearMarkers();
	}
}
