package edu.uw.beardcl.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Dispatch filter that dispatches any orders having a price above the current
 * market price (threshold).
 *
 * @author Chester Beard
 */
public final class StopSellOrderDispatchFilter
             extends OrderDispatchFilter<Integer, StopSellOrder> {

     /** This class' logger. */
    private static final Logger log =
                         LoggerFactory.getLogger(OrderDispatchFilter.class);

    /**
     * Constructor.
     *
     * @param initPrice the initial price
     */
    public StopSellOrderDispatchFilter(final int initPrice) {
        setThreshold(initPrice);
    }

    /**
     * Test the provided order against the threshold.
     *
     * @param order the order to be tested for dispatch
     *
     * @return true if the order price is above or equal the threshold
     */
    public boolean check(final StopSellOrder order) {
        final int threshold = getThreshold();
        final int price = order.getPrice();
        final boolean dispatch = price >= threshold;

        log.trace(String.format("StopSellOrderDispatchFilter: desiredPrice = %s curr price = %s dispatch = %s",
                  price, threshold, dispatch));

        return dispatch;
    }
}

