package io.github.compilerstuck.control.config;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.model.CancellationToken;
import io.github.compilerstuck.control.model.OperationReporter;
import io.github.compilerstuck.control.render.ProcessingContext;

/**
 * Strategy interface for array shuffle behaviours.
 * <p>
 * Each implementation defines how an {@link ArrayModel} is shuffled and how
 * visual progress is reported.  The delay between frames is driven by
 * {@link ProcessingContext#delay(int)} so the strategy itself is independent
 * of the Processing runtime.
 */
public interface ShuffleStrategy {

    /**
     * Shuffle (or otherwise arrange) the array held by {@code model} and call
     * {@code ctx.delay(ms)} at appropriate intervals to produce a visible
     * animation.
     *
     * @param model    the array to operate on
     * @param ctx      the Processing context used for frame delays
     * @param reporter reports progress labels for the UI
     * @param token    cancellation signal; stop early when cancelled
     */
    void shuffle(ArrayModel model, ProcessingContext ctx, OperationReporter reporter, CancellationToken token);
}
