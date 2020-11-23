package pt.unl.fct.di.novasys.babel.handlers;

import pt.unl.fct.di.novasys.channel.ChannelEvent;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 *
 */
@FunctionalInterface
public interface ChannelEventHandler<T extends ChannelEvent> {

    void handleEvent(T event, int channelId);


}
