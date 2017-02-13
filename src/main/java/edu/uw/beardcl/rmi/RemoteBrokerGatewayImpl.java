package edu.uw.beardcl.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import edu.uw.beardcl.account.SimpleAccountManager;
import edu.uw.beardcl.broker.SimpleBroker;
import edu.uw.beardcl.dao.JsonAccountDao;
import edu.uw.ext.exchange.TestExchange;
import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.exchange.StockExchange;

public class RemoteBrokerGatewayImpl extends UnicastRemoteObject implements RemoteBrokerGateway{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Broker broker;
	private StockExchange stockExchange;
	private AccountManager acctMgr;
	private String brokerName;
	
	
	public RemoteBrokerGatewayImpl() throws RemoteException, AccountException {
		super();		//call to UnicastRemoteObject constructor
	
			this.acctMgr = new SimpleAccountManager(new JsonAccountDao());
		
		 HashMap<String, Integer> tickers = new HashMap<String, Integer>();
		 tickers.put("BA", 100);
		 tickers.put("TX", 78);
		 tickers.put("C", 98);
		 
		this.stockExchange = new TestExchange(tickers);
		this.brokerName = "Broker2014";
		this.broker = new SimpleBroker(this.brokerName, this.acctMgr, this.stockExchange);
	}
	
	private RemoteBrokerSession CreateBrokerSession(Account acct, Broker broker, StockExchange exchange) throws RemoteException{
		return new RemoteBrokerSessionImpl(acct, broker, exchange);
	/*	String id = null;
		try {
	         RemoteBrokerSessionImpl server = new RemoteBrokerSessionImpl(acct, broker, exchange);
	         id = "edu.uw.java.beardcl.RemoteBrokerSession" + server.toString();
	         Registry reg = LocateRegistry.createRegistry(1099);
	         reg.bind(id, (Remote)server );
	      }
	      catch( Exception ex ) {
	         System.out.println( ex.getMessage() );
	         ex.printStackTrace();
	          */
	      }
	     

		
	@Override
	public RemoteBrokerSession createAccount(String userName, String passWord,
			int balance) throws AccountException, BrokerException, RemoteException {
			Account acct = 	broker.createAccount(userName, passWord, balance);

		return  CreateBrokerSession(acct, broker, stockExchange);
	}

	@Override
	public RemoteBrokerSession login(String userName, String passWord) throws BrokerException, RemoteException {
			Account acct = this.broker.getAccount(userName, passWord);
		return  CreateBrokerSession(acct, broker, stockExchange);
	}	


}
