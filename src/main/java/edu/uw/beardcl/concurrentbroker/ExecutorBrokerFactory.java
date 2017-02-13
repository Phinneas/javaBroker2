package edu.uw.beardcl.concurrentbroker;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerFactory;
import edu.uw.ext.framework.exchange.StockExchange;


/**
 *
 * Implementations of this class must provide a no argument constructor.
 *
 * @author Chester Beard
 */
public final class ExecutorBrokerFactory implements BrokerFactory {
    /**
     * Instantiates a new SimpleBroker.
     *
     * @param name the broker's name
     * @param acctMngr the account manager to be used by the broker
     * @param exch the exchange to be used by the broker
     *
     * @return a newly created SimpleBroker instance
      */
    public Broker newBroker(final String name, final AccountManager acctMngr,
                            final StockExchange exch) {
        return new ExecutorBroker(name, acctMngr, exch);
    }
}

