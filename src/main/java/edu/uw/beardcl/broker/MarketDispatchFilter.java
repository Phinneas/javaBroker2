package edu.uw.beardcl.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.order.Order;

/**
 * Dispatch filter that dispatches orders as long as the market is open.  The
 * threshold object will be a Boolean object indicating the state of the market.
 *
 * @author Chester Beard
 */
public final class MarketDispatchFilter
             extends OrderDispatchFilter<Boolean, Order> {

    /** This class' logger. */
    private static final Logger log =
                         LoggerFactory.getLogger(MarketDispatchFilter.class);

    /**
     * Constructor.
     *
     * @param initState the initial state of the market.
     */
    public MarketDispatchFilter(final boolean initState) {
        setThreshold(initState);
    }

    /**
     * Test if the order may be dispatched, all orders are dispatchable if the
     * market is open.
     *
     * @param order the order to be tested for dispatch
     *
     * @return true if the market is open, the threshold is object is
     *         Boolean.true
     */
    public boolean check(final Order order) {
        log.trace(String.format("Market order is dispatchable: %s", getThreshold()));

        return getThreshold();
    }
}

