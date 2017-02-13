package edu.uw.beardcl.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.broker.BrokerException;

public interface RemoteBrokerGateway extends Remote{

	public RemoteBrokerSession createAccount(String userName, String passWord,int balance) 
			throws AccountException, RemoteException, BrokerException;	//return reference to a remote session
	public RemoteBrokerSession login(String userName, String passWord) 
			throws BrokerException, RemoteException;			//return reference to a remote session
	
}
