package edu.uw.beardcl.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.order.Order;
import edu.uw.ext.framework.exchange.StockExchange;

/**
 * OrderProcessor implementation that executes orders through the broker.
 *
 * @author Chester Beard
 */
public final class StockTraderOrderProcessor implements OrderProcessor {
    /** This class' logger */
    private static final Logger logger =
                         LoggerFactory.getLogger(StockTraderOrderProcessor.class);

    /** The account manager managing the accounts */
    private AccountManager acctMgr;
    /** The exchange used to execute trades */
    private StockExchange exchange;

    /**
     * Constructor.
     *
     * @param acctMgr the account manager to be used to update account balances.
     * @param exchange the exchange to be used for the execution of orders
     */
    public StockTraderOrderProcessor(final AccountManager acctMgr,
                                     final StockExchange exchange) {
        this.acctMgr = acctMgr;
        this.exchange = exchange;
    }

    /**
     * Executes the order using the exchange.
     *
     * @param order the order to process
     */
    public void process(final Order order) {
        logger.info(String.format("Executing - %s", order));

        final int sharePrice = exchange.executeTrade(order);

        try {
            final Account acct = acctMgr.getAccount(order.getAccountId());
            acct.reflectOrder(order, sharePrice);
        } catch (final AccountException ex) {
            logger.error(String.format("Unable to update account, %s", order.getAccountId()), ex);
        }
    }
}

