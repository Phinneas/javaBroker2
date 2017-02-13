package edu.uw.beardcl.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.order.StopBuyOrder;

/**
 * Dispatch filter that dispatches any orders having a price below the current
 * market price (threshold).
 *
 * @author Chester Beard
 */
public final class StopBuyOrderDispatchFilter
             extends OrderDispatchFilter<Integer, StopBuyOrder> {

    /** This class' logger. */
    private static  final Logger log =
                          LoggerFactory.getLogger(StopBuyOrderDispatchFilter.class);

   /**
     * Constructor.
     *
     * @param initPrice the initial price
     */
    public StopBuyOrderDispatchFilter(final int initPrice) {
        setThreshold(initPrice);
    }

    /**
     * Test the provided order against the threshold.
     *
     * @param order the order to be tested for dispatch
     *
     * @return true if the order price is below or equal the threshold
     */
    public boolean check(final StopBuyOrder order) {
        final int threshold = getThreshold();
        final int price = order.getPrice();
        final boolean dispatch = price <= threshold;

        log.trace(String.format("StopBuyOrderDispatchFilter: desiredPrice = %s curr price = %s dispatch = %s",
                                price, threshold, dispatch));

        return dispatch;
    }
}

