package clouds.base;

import com.google.common.collect.Multimap;

import java.util.Map;

/**
 * 
 * @author evgenyf
 * Date: 10/7/13
 */
public interface CloudServer {

	   /**
	    * @return the ip addresses assigned to the server
	    */
//	   Multimap<String, CloudAddress> getAddresses();

       String getPublicAddress();
       String getPrivateAddress();
	   /**
	    * 
	    * @return id
	    */
	   String getId();
	   Map<String, String> getMetadata();
	   String getName();
	   CloudServerStatus getStatus();
}