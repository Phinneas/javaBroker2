package edu.uw.beardcl.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.MarketBuyOrder;
import edu.uw.ext.framework.order.MarketSellOrder;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

public interface RemoteBrokerSession extends Remote{

	public int getBalance()throws RemoteException;
	public void deleteAccount() throws BrokerException, RemoteException;
	public StockQuote getQuote(String ticker)throws RemoteException;
	public void placeMarketBuyOrder(MarketBuyOrder order)throws BrokerException, RemoteException;
	public void placeMarketSellOrder(MarketSellOrder sellOrder)throws BrokerException, RemoteException;
	public void placeStopBuyOrder(StopBuyOrder buyOrder) throws BrokerException, RemoteException;
	public void placeStopSellOrder(StopSellOrder sellOrder) throws BrokerException, RemoteException;
	public void close()throws RemoteException;
				//need RemoteException to make these methods valid calls
				//all remote functions need RemoteException otherwise not remotable call
}
