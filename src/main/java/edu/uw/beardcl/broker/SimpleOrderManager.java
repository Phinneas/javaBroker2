package edu.uw.beardcl.broker;

import java.util.Comparator;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Maintains queues to different types of orders and requests the execution of
 * orders when price conditions allow their execution.
 *
 * @author Chester Beard
 */
public class SimpleOrderManager implements OrderManager {
    /** The symbol of the stock this order manager is for */
    private String stockTickerSymbol;

    /** Queue for stop buy orders */
    private OrderQueue<StopBuyOrder> stopBuyOrderQueue;

    /** Queue for stop sell orders */
    private OrderQueue<StopSellOrder> stopSellOrderQueue;

    /** Dispatch filter for the ascending order queue */
    private OrderDispatchFilter<Integer, StopBuyOrder> stopBuyOrderFilter;

    /** Dispatch filter for the descending order queue */
    private OrderDispatchFilter<Integer, StopSellOrder> stopSellOrderFilter;

    /**
     * Constructor.  Constructor to be used by sub classes to finish initialization.
     * 
     * @param stockTickerSymbol the ticker symbol of the stock this instance is
     *                          manage orders for
     */
    protected SimpleOrderManager(final String stockTickerSymbol) {
        this.stockTickerSymbol = stockTickerSymbol;
    }

    /**
     * Constructor.
     *
     * @param stockTickerSymbol the ticker symbol of the stock this instance is
     *                          manage orders for
     * @param price the current price of stock to be managed
     */
    public SimpleOrderManager(final String stockTickerSymbol, final int price) {
        this(stockTickerSymbol);

        // Create the stop buy order queue and associate pieces
        final Comparator<StopBuyOrder> ascending = new StopBuyOrderComparator();
        final OrderDispatchFilter<Integer, StopBuyOrder> localStopBuyFilter =
              new StopBuyOrderDispatchFilter(price);
        final OrderQueue<StopBuyOrder> localStopBuyQueue =
              new SimpleOrderQueue<StopBuyOrder>(ascending, localStopBuyFilter);
        setStopBuyOrderFilter(localStopBuyFilter);
        setStopBuyOrderQueue(localStopBuyQueue);

        // This would work instead of the local variable and setters.
        //stopBuyOrderFilter = new StopBuyOrderDispatchFilter(price);
        //stopBuyOrderQueue = new SimpleOrderQueue<StopBuyOrder>(ascending, stopBuyOrderFilter);
        
        // Create the stop sell order queue ...
        final Comparator<StopSellOrder> decending = new StopSellOrderComparator();
        final OrderDispatchFilter<Integer, StopSellOrder> localStopSellFilter =
              new StopSellOrderDispatchFilter(price);
        final OrderQueue<StopSellOrder> localStopSellQueue =
              new SimpleOrderQueue<StopSellOrder>(decending, localStopSellFilter);
        setStopSellOrderFilter(localStopSellFilter);
        setStopSellOrderQueue(localStopSellQueue);
        
        // Again, this would work instead of the local variable and setters.
        //stopSellOrderFilter = new StopSellOrderDispatchFilter(price);
        //stopSellOrderQueue = new SimpleOrderQueue<StopSellOrder>(decending, stopSellOrderFilter);
    }

    /**
     * @param stockTickerSymbol the stockTickerSymbol to set
     */
    protected final void setStockTickerSymbol(final String stockTickerSymbol) {
        this.stockTickerSymbol = stockTickerSymbol;
    }

    /**
     * @param stopBuyOrderQueue the stopBuyOrderQueue to set
     */
    protected final void setStopBuyOrderQueue(
            final OrderQueue<StopBuyOrder> stopBuyOrderQueue) {
        this.stopBuyOrderQueue = stopBuyOrderQueue;
    }

    /**
     * @param stopSellOrderQueue the stopSellOrderQueue to set
     */
    protected final void setStopSellOrderQueue(
            final OrderQueue<StopSellOrder> stopSellOrderQueue) {
        this.stopSellOrderQueue = stopSellOrderQueue;
    }

    /**
     * @param stopBuyOrderFilter the stopBuyOrderFilter to set
     */
    protected final void setStopBuyOrderFilter(
            final OrderDispatchFilter<Integer, StopBuyOrder> stopBuyOrderFilter) {
        this.stopBuyOrderFilter = stopBuyOrderFilter;
    }

    /**
     * @param stopSellOrderFilter the stopSellOrderFilter to set
     */
    protected final void setStopSellOrderFilter(
            final OrderDispatchFilter<Integer, StopSellOrder> stopSellOrderFilter) {
        this.stopSellOrderFilter = stopSellOrderFilter;
    }

    /**
     * Gets the stock ticker symbol for the stock managed by this stock manager.
     *
     * @return the stock ticker symbol
     */
    public final String getSymbol() {
        return stockTickerSymbol;
    }

    /**
     * Respond to a stock price adjustment by setting threshold on dispatch
     * filters.
     *
     * @param price the new price
     */
    public final void adjustPrice(final int price) {
        stopBuyOrderFilter.setThreshold(price);
        stopSellOrderFilter.setThreshold(price);
    }

    /**
     * Queue a stop buy order.
     *
     * @param order the order to be queued
     */
    public final void queueOrder(final StopBuyOrder order) {
        stopBuyOrderQueue.enqueue(order);
    }

    /**
     * Queue a stop sell order.
     *
     * @param order the order to be queued
     */
    public final void queueOrder(final StopSellOrder order) {
        stopSellOrderQueue.enqueue(order);
    }

    /**
     * Registers the processor to be used during order processing.  This will be
     * passed on to the order queues as the dispatch callback.
     *
     * @param processor the callback to be registered
     */
    public final void setOrderProcessor(final OrderProcessor processor) {
        stopSellOrderQueue.setOrderProcessor(processor);
        stopBuyOrderQueue.setOrderProcessor(processor);
    }
}

