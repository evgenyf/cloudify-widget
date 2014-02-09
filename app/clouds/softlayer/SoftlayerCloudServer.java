package clouds.softlayer;

import clouds.base.CloudAddress;
import clouds.base.CloudServer;
import clouds.base.CloudServerStatus;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jclouds.compute.domain.NodeMetadata;

import java.util.Map;
import java.util.Set;

/**
 * HP implementation of CloudServer
 * @author evgenyf
 * Date: 10/7/13
 */
public class SoftlayerCloudServer implements CloudServer{

	private final NodeMetadata nodeMetadata;
    private final String publicAddress;
    private final String privateAddress;

	public SoftlayerCloudServer( NodeMetadata nodeMetadata ){
		this.nodeMetadata = nodeMetadata;

        Set<String> publicAddresses = nodeMetadata.getPublicAddresses();
        Set<String> privateAddresses = nodeMetadata.getPrivateAddresses();

        publicAddress = publicAddresses.isEmpty() ? "" : publicAddresses.iterator().next();
        privateAddress = privateAddresses.isEmpty() ? "" : privateAddresses.iterator().next();
	}
     /*
	@Override
	public Multimap<String, CloudAddress> getAddresses() {

		Multimap<String, CloudAddress> resultMultimap = ArrayListMultimap.create();

		CloudAddress publicCloudAddress = new SoftlayerCloudAddress( publicAddress );
        CloudAddress privateCloudAddress = new SoftlayerCloudAddress( privateAddress );

        resultMultimap.put( publicAddress, publicCloudAddress );
        resultMultimap.put( privateAddress, privateCloudAddress );

		return resultMultimap;
	}          */

    @Override
    public String getPublicAddress() {
        return publicAddress;
    }

    @Override
    public String getPrivateAddress() {
        return privateAddress;
    }

    @Override
	public String getId() {
		return nodeMetadata.getId();
	}

	@Override
	public Map<String, String> getMetadata() {
		return nodeMetadata.getUserMetadata();
	}

	@Override
	public String getName() {
		return nodeMetadata.getName();
	}

	@Override
	public CloudServerStatus getStatus() {
        NodeMetadata.Status status = nodeMetadata.getStatus();
        CloudServerStatus retValue = null;
		switch( status ){
		case ERROR:
			retValue = CloudServerStatus.ERROR;
			break;
		case PENDING:
			retValue = CloudServerStatus.PENDING;
			break;
		case RUNNING:
			retValue = CloudServerStatus.RUNNING;
			break;
		case SUSPENDED:
			retValue = CloudServerStatus.SUSPENDED;
			break;
		case TERMINATED:
			retValue = CloudServerStatus.TERMINATED;
			break;
		case UNRECOGNIZED:
			retValue = CloudServerStatus.UNRECOGNIZED;
			break;

		default:
			retValue = CloudServerStatus.UNRECOGNIZED;
			break;
		
		}
		
		return retValue;
	}
	
	@Override
	public String toString() {
		return "SoftlayerCloudServer [nodeMetadata=" + nodeMetadata + "]";
	}
}
