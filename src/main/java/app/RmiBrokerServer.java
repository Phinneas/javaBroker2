package app;
/**
 * @author chesterbeard
 * @version 08-27-2014
 */
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import edu.uw.beardcl.rmi.RemoteBrokerGatewayImpl;

public class RmiBrokerServer {
	
	 public static void main( String args[] ) {
	      String SERVER_NAME = "edu.uw.beardcl.rmi.RemoteBrokerGateway";
	      									//creates remoteBrokerGateway on this machine, this.machine = server
	      try {
	         RemoteBrokerGatewayImpl server = new RemoteBrokerGatewayImpl();
	         Registry reg = LocateRegistry.createRegistry(1099);
	         reg.bind(SERVER_NAME, (Remote)server );
	      }
	      catch( Exception ex ) {
	         System.out.println( ex.getMessage() );
	         ex.printStackTrace();
	} while(true){
		
	}
	      }

}


