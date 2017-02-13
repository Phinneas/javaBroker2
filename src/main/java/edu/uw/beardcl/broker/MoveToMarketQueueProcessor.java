package edu.uw.beardcl.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * Moves orders to a brokers market order queue.
 *
 * @author Chester Beard
 */
public final class MoveToMarketQueueProcessor implements OrderProcessor {
    /** The class' logger */
    private static final Logger log =
                         LoggerFactory.getLogger(MoveToMarketQueueProcessor.class);

    /** The  market queue */
    private OrderQueue<Order> marketQueue;

    /**
     * Constructor.
     *
     * @param marketQueue the queue the orders will be moved to
     */
    public MoveToMarketQueueProcessor(final OrderQueue<Order> marketQueue) {
        this.marketQueue = marketQueue;
    }

    /**
     * Enques the order into the market order queue.
     *
     * @param order the order to process
     */
    public void process(final Order order) {
        log.info(String.format("### Moving order to market queue: %s", order));
        marketQueue.enqueue(order);
    }
}

