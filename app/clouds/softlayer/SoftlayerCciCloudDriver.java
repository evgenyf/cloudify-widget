package clouds.softlayer;

import beans.NovaContext;
import beans.config.Conf;
import clouds.base.AbstractCloudDriver;
import clouds.base.CloudServer;
import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import play.libs.Json;
import utils.CollectionUtils;

import java.util.*;

/**
 * User: evgenyf
 * Date: 1/28/14
 */
public class SoftlayerCciCloudDriver extends AbstractCloudDriver{

    @Override
    public List<CloudServer> getAllMachinesWithTag( NovaContext novaContext ){
        String confTags =  conf.server.bootstrap.tags;
        logger.info( "getting all machines with tag [{}]", confTags );
        List<CloudServer> servers = new LinkedList<CloudServer>();
        if ( StringUtils.isEmpty(confTags) ){
            logger.info( "confTags is null, not finding all machines" );
            return servers;
        }

        logger.info( "Before retrieving computeService" );
        ComputeService computeService = novaContext.getComputeService();
        logger.info( "Before retrieving listNodesDetailsMatching" );
        Set<? extends NodeMetadata> nodeMetadataSet =
                computeService.listNodesDetailsMatching(( new ComputeServerNamePredicate( conf.server.bootstrap.serverNamePrefix ) ) );
            logger.info( "After retrieving listNodesDetailsMatching, list size {[]}", nodeMetadataSet.size() );
        for( NodeMetadata nodeMetadata : nodeMetadataSet ){
            CloudServer cloudServer = new SoftlayerCloudServer( nodeMetadata );
            servers.add( cloudServer );
        }

        return servers;
        // get all servers with tags matching my configuration.
        //return CloudifyUtils.getAllMachinesWithPredicate( new ServerTagPredicate( conf ), context );
    }

    @Override
    public String createAdvancedParams() {

        ObjectNode advancedParamsJsonObject = Json.newObject();
        advancedParamsJsonObject.put( "type", conf.server.cloudProvider.label );
        ObjectNode paramsJsonObject = Json.newObject();
        paramsJsonObject.put( "userId", conf.server.bootstrap.api.key );
        paramsJsonObject.put( "apiKey", conf.server.bootstrap.api.secretKey );
        advancedParamsJsonObject.put( "params", paramsJsonObject );

        return advancedParamsJsonObject.toString();
    }


    class ComputeServerNamePredicate implements Predicate<ComputeMetadata> {

        final String prefix;

        public ComputeServerNamePredicate( String prefix ){
            this.prefix = prefix;
        }

        @Override
        public boolean apply( ComputeMetadata computeMetadata )
        {
            if ( computeMetadata == null ) {
                return false;
            }

            String name = computeMetadata.getName();
            return StringUtils.startsWith(name, prefix);
        }

        @Override
        public String toString()
        {
            return "ComputeServerNamePredicate, prefix =" + prefix;
        }
    }
}

