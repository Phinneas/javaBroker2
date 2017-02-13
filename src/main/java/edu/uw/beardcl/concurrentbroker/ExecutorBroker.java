package edu.uw.beardcl.concurrentbroker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.beardcl.broker.MarketDispatchFilter;
import edu.uw.beardcl.broker.SimpleBroker;
import edu.uw.beardcl.broker.StockTraderOrderProcessor;
import edu.uw.ext.framework.order.Order;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.broker.OrderManager;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.exchange.StockExchange;

/**
 * An extension of AbstractBroker that uses a  ThreadedOrderManager and
 * ThreadeOrderQueue for the market order queue.
 *
 * @author Chester Beard
 */
public final class ExecutorBroker extends SimpleBroker {
    /** The thread pool size. */
    private static final int POOL_SIZE = 4;

    /** This class' logger */
    private static final Logger logger =
                         LoggerFactory.getLogger(ExecutorBroker.class);

    /** Executor to be used to process all orders. */
    private final ExecutorService orderExecutor;

    /**
     *  Constructor.
     *
     * @param brokerName name of the broker
     * @param acctMgr the account manager to be used by the broker
     * @param exchg the stock exchange to be used by the broker
     */
    public ExecutorBroker(final String brokerName, final AccountManager acctMgr,
                          final StockExchange exchg) {
        super(brokerName, exchg, acctMgr);

        orderExecutor = Executors.newFixedThreadPool(POOL_SIZE);

        // Create the market order queue, & order processor
        final MarketDispatchFilter filter = new MarketDispatchFilter(exchg.isOpen());
        setMarketDispatchFilter(filter);

        // Using the ThreadedOrderQueue for the market queue allows more control
        // over it
        //OrderQueue<Order> marketQueue = new ExecutorOrderQueue<Order>(filter, orderExecutor);
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
        return new ExecutorOrderManager(ticker, initialPrice, orderExecutor);
    }

    /**
     * Release broker resources.
     *
     * @exception BrokerException if the operation fails
     */
    public void close() throws BrokerException {
        orderExecutor.shutdown();
        try {
            orderExecutor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            logger.warn("Executor shutdown interrupted.");
            // just continue closing the broker
        }
        super.close();
    }
}

