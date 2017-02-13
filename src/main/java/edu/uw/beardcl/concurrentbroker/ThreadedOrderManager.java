package edu.uw.beardcl.concurrentbroker;

import java.util.Comparator;

import edu.uw.beardcl.broker.SimpleOrderManager;
import edu.uw.beardcl.broker.StopBuyOrderComparator;
import edu.uw.beardcl.broker.StopBuyOrderDispatchFilter;
import edu.uw.beardcl.broker.StopSellOrderComparator;
import edu.uw.beardcl.broker.StopSellOrderDispatchFilter;
import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;


/**
 * Maintains queues to different types of orders and requests the execution of
 * orders when price conditions allow their execution.
 *
 * @author Chester Beard
 */
public final class ThreadedOrderManager extends SimpleOrderManager {
    /**
     * Constructor.
     *
     * @param stockTickerSymbol the ticker symbol of the stock this instance is
     *                          manage orders for
     * @param price the current price of stock to be managed
     */
    public ThreadedOrderManager(final String stockTickerSymbol,
                                final int price) {
        super(stockTickerSymbol);

        // Create the stop buy order queue and associate pieces
        final Comparator<StopBuyOrder> ascending = new StopBuyOrderComparator();
        final OrderDispatchFilter<Integer, StopBuyOrder> stopBuyFilter = new StopBuyOrderDispatchFilter(price);
        final OrderQueue<StopBuyOrder> stopBuyQueue =
            new ThreadedOrderQueue<StopBuyOrder>(stockTickerSymbol + "-StopBuy", ascending, stopBuyFilter);

        setStopBuyOrderFilter(stopBuyFilter);
        setStopBuyOrderQueue(stopBuyQueue);
        // Create the stop sell order queue ...
        final Comparator<StopSellOrder> decending = new StopSellOrderComparator();
        final OrderDispatchFilter<Integer, StopSellOrder> stopSellFilter = new StopSellOrderDispatchFilter(price);
        final OrderQueue<StopSellOrder> stopSellQueue =
            new ThreadedOrderQueue<StopSellOrder>(stockTickerSymbol + "-StopSell", decending,
                                                  stopSellFilter);

        setStopSellOrderFilter(stopSellFilter);
        setStopSellOrderQueue(stopSellQueue);
    }
}

