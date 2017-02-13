package edu.uw.beardcl.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.MarketBuyOrder;
import edu.uw.ext.framework.order.MarketSellOrder;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

public class RemoteBrokerSessionImpl extends UnicastRemoteObject implements RemoteBrokerSession{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Account acct;
	private Broker broker;
	private StockExchange exchange;
	
	public RemoteBrokerSessionImpl(Account acct, Broker broker, StockExchange exchange)throws RemoteException{
		this.acct = acct;
		this.broker = broker;
		this.exchange = exchange;
	}

	@Override
	public int getBalance() {

		return acct.getBalance();
	}

	@Override
	public void deleteAccount() throws BrokerException, NoSuchObjectException {
		
				broker.deleteAccount(acct.getName());
			close();	//new
		
	}

	@Override
	public StockQuote getQuote(String ticker) {
			
		return exchange.getQuote(ticker);	//delegation
	}

	@Override
	public void placeMarketBuyOrder(MarketBuyOrder order) throws BrokerException {
			
				broker.placeOrder(order);
			
	}

	@Override
	public void placeMarketSellOrder(MarketSellOrder sellOrder)throws BrokerException {
			
				broker.placeOrder(sellOrder);
			
	}

	@Override
	public void placeStopBuyOrder(StopBuyOrder buyOrder) throws BrokerException {
			broker.placeOrder(buyOrder);
	}

	@Override
	public void placeStopSellOrder(StopSellOrder sellOrder) throws BrokerException {
			broker.placeOrder(sellOrder);		
	}

	@Override
	public void close() throws NoSuchObjectException {
/*		String id = null;		//unregisters object
		try {
	         id = "edu.uw.java.beardcl.RemoteBrokerSession" + this.toString();
	         Registry reg = LocateRegistry.createRegistry(1099);
	         reg.unbind(id);
	      }
	      catch( Exception ex ) {
	         System.out.println( ex.getMessage() );
	         ex.printStackTrace();
	      }
		
			//do not shutdown broker, or exchange due to end of single session
	}
	*/
		UnicastRemoteObject.unexportObject(this, true);
	}

}
