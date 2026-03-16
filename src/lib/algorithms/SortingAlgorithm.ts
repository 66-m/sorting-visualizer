/**
 * Base class for all sorting algorithms
 *
 * TypeScript port of the Java SortingAlgorithm class.
 * Provides common functionality for delay, metrics tracking, and array manipulation.
 */

import type { ArrayController } from '../models/ArrayController';
import { Marker } from '../models/ArrayModel';

export interface DelayStrategy {
	shouldDelay(arrayLength: number, delayFactor: number): boolean;
}

export const DEFAULT_DELAY_STRATEGY: DelayStrategy = {
	shouldDelay(arrayLength: number, delayFactor: number): boolean {
		return Math.random() < delayFactor;
	}
};

export abstract class SortingAlgorithm {
	protected name: string;
	protected delay: boolean = true;
	protected delayTime: number = 1; // milliseconds
	protected delayFactor: number = 1.0;
	protected delayStrategy: DelayStrategy = DEFAULT_DELAY_STRATEGY;
	protected selected: boolean = true;
	protected alternativeSize: number;
	protected startTime: number = 0;
	protected arrayController: ArrayController;

	// Callback for delay/update
	protected delayCallback?: (markers: number[]) => Promise<void>;

	constructor(arrayController: ArrayController, name: string) {
		this.arrayController = arrayController;
		this.name = name;
		this.alternativeSize = arrayController.getLength();
	}

	/**
	 * Main sort method - must be implemented by subclasses
	 */
	abstract sort(): Promise<void>;

	/**
	 * Sets the delay callback function (for visualization updates)
	 */
	setDelayCallback(callback: (markers: number[]) => Promise<void>): void {
		this.delayCallback = callback;
	}

	/**
	 * Delays execution and marks array positions for visualization
	 */
	protected async doDelay(markers: number[] = []): Promise<void> {
		if (this.delay && this.delayStrategy.shouldDelay(this.arrayController.getLength(), this.delayFactor)) {
			// Add elapsed time to metrics
			if (this.startTime) {
				const elapsed = performance.now() - this.startTime;
				this.arrayController.addRealTime(elapsed * 1000000); // Convert to nanoseconds for compatibility
			}

			// Set markers for visualization
			for (const i of markers) {
				this.arrayController.setMarker(i, Marker.SET);
			}

			// Call the delay callback if provided
			if (this.delayCallback) {
				await this.delayCallback(markers);
			}

			// Actually delay
			await new Promise(resolve => setTimeout(resolve, this.delayTime));

			this.startTime = performance.now();
		}
	}

	// Getters and setters
	getName(): string { return this.name; }
	isSelected(): boolean { return this.selected; }
	setSelected(selected: boolean): void { this.selected = selected; }
	getAlternativeSize(): number { return this.alternativeSize; }
	setAlternativeSize(size: number): void { this.alternativeSize = size; }
	setDelayTime(ms: number): void { this.delayTime = ms; }
	setDelayFactor(factor: number): void { this.delayFactor = factor; }
	setDelayStrategy(strategy: DelayStrategy): void { this.delayStrategy = strategy; }
}
