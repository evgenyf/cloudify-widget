package clouds.hp;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jclouds.openstack.nova.v2_0.domain.Address;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.Server.Status;

import clouds.base.CloudAddress;
import clouds.base.CloudServer;
import clouds.base.CloudServerStatus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * HP implementation of CloudServer
 * @author evgenyf
 * Date: 10/7/13
 */
public class HPCloudServer implements CloudServer{

	private final Server server;
    private final String publicIp;
    private final String privateIp;
	
	public HPCloudServer( Server server ){
		this.server = server;

        Multimap<String,CloudAddress> addressesMap = getAddresses();
        Collection<CloudAddress> aPrivate = addressesMap.get( "private" );
        //support hp cloud
        if( !aPrivate.isEmpty() ){
            CloudAddress[] addresses = aPrivate.toArray( new CloudAddress[ aPrivate.size() ] );
            publicIp = addresses[ 1 ].getAddr();
            privateIp = addresses[ 0 ].getAddr();
        }
        else{
            publicIp = "";
            privateIp = "";
        }
	}

//	@Override
	private Multimap<String, CloudAddress> getAddresses() {
		Multimap<String, Address> addresses = server.getAddresses();
		Multimap<String, CloudAddress> resultMultimap = ArrayListMultimap.create();
		
		Set<String> keySet = addresses.keySet();
		for( String key : keySet ){
			Collection<Address> addressesCollection = addresses.get( key );
			List<CloudAddress> newAddresses = new LinkedList<CloudAddress>();
			for( Address address : addressesCollection ){
				CloudAddress cloudAddress = createCloudAddress( address );
				newAddresses.add( cloudAddress );
			}
			resultMultimap.putAll( key, newAddresses );
		}
		
		return resultMultimap;
	}

    @Override
    public String getPublicAddress() {
        return publicIp;
    }

    @Override
    public String getPrivateAddress() {
        return privateIp;
    }

    @Override
	public String getId() {
		return server.getId();
	}

	@Override
	public Map<String, String> getMetadata() {
		return server.getMetadata();
	}

	@Override
	public String getName() {
		return server.getName();
	}
	
	private static CloudAddress createCloudAddress( Address address ){
		return new HPCloudAddress( address );
	}

	@Override
	public CloudServerStatus getStatus() {
		Status status = server.getStatus();
		CloudServerStatus retValue = null;
		switch( status ){
		case ACTIVE:
			retValue = CloudServerStatus.ACTIVE;
			break;
		case BUILD:
			retValue = CloudServerStatus.BUILD;
			break;
		case DELETED:
			retValue = CloudServerStatus.DELETED;
			break;
		case ERROR:
			retValue = CloudServerStatus.ERROR;
			break;
		case HARD_REBOOT:
			retValue = CloudServerStatus.HARD_REBOOT;
			break;
		case PASSWORD:
			retValue = CloudServerStatus.PASSWORD;
			break;
		case PAUSED:
			retValue = CloudServerStatus.PAUSED;
			break;
		case REBOOT:
			retValue = CloudServerStatus.REBOOT;
			break;
		case REBUILD:
			retValue = CloudServerStatus.REBUILD;
			break;
		case RESIZE:
			retValue = CloudServerStatus.RESIZE;
			break;
		case REVERT_RESIZE:
			retValue = CloudServerStatus.REVERT_RESIZE;
			break;
		case STOPPED:
			retValue = CloudServerStatus.STOPPED;
			break;
		case SUSPENDED:
			retValue = CloudServerStatus.SUSPENDED;
			break;
		case UNKNOWN:
			retValue = CloudServerStatus.UNKNOWN;
			break;
		case VERIFY_RESIZE:
			retValue = CloudServerStatus.VERIFY_RESIZE;
			break;
		case UNRECOGNIZED:
		default:
			retValue = CloudServerStatus.UNRECOGNIZED;
			break;
		
		}
		
		return retValue;
	}
	
	@Override
	public String toString() {
		return "HPCloudServer [server=" + server + "]";
	}
}
