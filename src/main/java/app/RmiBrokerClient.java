package app;
/*
 * @author Chester Beard
 * @version 08-27-2014
 * 
 */
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import edu.uw.beardcl.rmi.RemoteBrokerGateway;
import edu.uw.beardcl.rmi.RemoteBrokerSession;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.MarketBuyOrder;
import edu.uw.ext.framework.order.MarketSellOrder;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;

public class RmiBrokerClient {
	public static void main(String args[]) 
			throws MalformedURLException, RemoteException, NotBoundException, AccountException, BrokerException {

		String host = "localhost";
		String urlPrefix = "//" + host + "/";
		String url = urlPrefix
				+ "edu.uw.beardcl.rmi.RemoteBrokerGateway";

		RemoteBrokerGateway server = (RemoteBrokerGateway) Naming.lookup(url);
		
		int balance = 100000;
		RemoteBrokerSession beardcl =  server.createAccount("beardcl8", "password", balance);
		
		if(beardcl.getBalance() != balance) throw new AccountException();
		beardcl.close();
		beardcl =  server.login("beardcl8", "password");
		if(beardcl.getBalance() != balance)throw new AccountException();
		StockQuote quote = beardcl.getQuote("BA");
		if(quote.getPrice() != 100){
			throw new BrokerException();
		}
		if(quote.getTicker().compareTo("BA") != 0){
			throw new BrokerException();
		}	
				
		beardcl.deleteAccount();
		boolean caught = false;
		try{
		beardcl =  server.login("beardcl8", "password");		//server should throw exception or retunr null
		}catch(BrokerException e){
			caught = true;
		}
		if(!caught){
			throw new BrokerException();
		}
		beardcl.placeMarketBuyOrder(null);
		
			beardcl.placeStopBuyOrder(null);
			beardcl.placeStopSellOrder(null);
			
		beardcl.placeMarketSellOrder(null);
		beardcl.placeMarketBuyOrder(null);
		beardcl.close();
		
	
	}
	
}