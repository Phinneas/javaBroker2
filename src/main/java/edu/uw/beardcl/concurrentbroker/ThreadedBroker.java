package edu.uw.beardcl.concurrentbroker;

import edu.uw.beardcl.broker.MarketDispatchFilter;
import edu.uw.beardcl.broker.SimpleBroker;
import edu.uw.beardcl.broker.StockTraderOrderProcessor;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.order.Order;

/**
 * An extension of AbstractBroker that uses a  ThreadedOrderManager and
 * ThreadeOrderQueue for the market order queue.
 *
 * @author Chester Beard
 */
public final class ThreadedBroker extends SimpleBroker {

    /**
     *  Constructor.
     *
     * @param brokerName name of the broker
     * @param acctMgr the account manager to be used by the broker
     * @param exchg the stock exchange to be used by the broker
     */
    public ThreadedBroker(final String brokerName, final AccountManager acctMgr,
                          final StockExchange exchg) {
        super(brokerName, exchg, acctMgr);

        // Create the market order queue, & order processor
        final MarketDispatchFilter filter = new MarketDispatchFilter(exchg.isOpen());
        setMarketDispatchFilter(filter);

        final ThreadedOrderQueue<Order> marketQueue = new ThreadedOrderQueue<Order>("MARKET", filter);
        marketQueue.setPriority(Thread.MAX_PRIORITY);
        final OrderProcessor tradeProc = new StockTraderOrderProcessor(acctMgr, exchg);
        marketQueue.setOrderProcessor(tradeProc);
        setMarketOrderQueue(marketQueue);

        // Create the order managers
        initializeOrderManagers();

        exchg.addExchangeListener(this);
    }
    
    /**
     * Create an appropriate order manager for this broker.
     *
     * @param ticker the ticker symbol of the stock
     * @param initialPrice current price of the stock
     *
     * @return a new OrderManager for the specified stock
     */
    @Override
    protected OrderManager createOrderManager(final String ticker, final int initialPrice) {
        return new ThreadedOrderManager(ticker, initialPrice);
    }
}
