package clouds.hp;

import beans.ActiveWait;
import beans.NovaContext;
import beans.Wait;
import beans.config.CloudProvider;
import beans.pool.PoolEvent;
import clouds.base.*;
import models.ServerNode;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.options.TemplateOptions;
import play.libs.Json;
import server.ApplicationContext;
import utils.CloudifyUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: evgenyf
 * Date: 1/28/14
 */
public class HPCloudDriver extends AbstractCloudDriver{

    @Override
    public ServerNode createMachine( NovaContext novaContext, long nodeId ) throws RunNodesException {

        logger.info( "Starting to create new Server [imageId={}, flavorId={}]", conf.server.bootstrap.imageId, conf.server.bootstrap.flavorId );

        final CloudServerApi serverApi = novaContext.getApi();
//        CreateServerOptions serverOptions = new CreateServerOptions();

//        Map<String,String> metadata = new HashMap<String, String>();
//
//        List<String> tags = new LinkedList<String>();
//
//        if ( !StringUtils.isEmpty(conf.server.bootstrap.tags) ){
//            tags.add( conf.server.bootstrap.tags );
//        }
//
//        metadata.put("tags", StringUtils.join(tags, ","));
//        serverOptions.metadata(metadata);
//        serverOptions.keyPairName( conf.server.bootstrap.keyPair );
//        serverOptions.securityGroupNames(conf.server.bootstrap.securityGroup);

        CloudProvider cloudProvider = conf.server.cloudProvider;

        CloudCreateServerOptions serverOpts = ApplicationContext.getCloudifyFactory().
                createCloudCreateServerOptions( cloudProvider, conf );


        ComputeService computeService = novaContext.getComputeService();


        TemplateOptions templateOptions = new TemplateOptions();


//        final ServerCreated serverCreated = serverApi.create(
//        		conf.server.bootstrap.serverNamePrefix + incNodeId.incrementAndGet(),
//        		conf.server.bootstrap.imageId , conf.server.bootstrap.flavorId, serverOpts);

        final CloudServerCreated serverCreated = serverApi.create(
                conf.server.bootstrap.serverNamePrefix + nodeId,
                conf.server.bootstrap.imageId, conf.server.bootstrap.flavorId, serverOpts );

        logger.info("waiting for serverId activation [{}]", serverCreated.getId());
        // start the event
        PoolEvent.MachineStateEvent poolEvent = new PoolEvent.MachineStateEvent().setType(PoolEvent.Type.CREATE)/*.setResource(serverCreated)TODO?*/;
        poolEventManager.handleEvent(poolEvent);
        final ActiveWait wait = new ActiveWait();
        if ( wait
                .setIntervalMillis(TimeUnit.SECONDS.toMillis(5))
                .setTimeoutMillis(TimeUnit.SECONDS.toMillis(120))
                .waitUntil(new Wait.Test() {
                    @Override
                    public boolean resolved() {
                        logger.info("Waiting for a server activation... Left timeout: {} sec", wait.getTimeLeftMillis() / 1000);
                        return serverApi.get(serverCreated.getId()).getStatus().equals(CloudServerStatus.ACTIVE);
                    }
                }))
        {
            CloudServer server = serverApi.get( serverCreated.getId());
            poolEventManager.handleEvent(poolEvent./*setResource(server).TODO?*/setType(PoolEvent.Type.UPDATE));
            logger.info("Server created, public IP{[]}, private IP{[]} ", server.getPublicAddress(), server.getPrivateAddress() );
            return new ServerNode( server );
        }

        logger.info("server did not become active.");
        return null;
    }

    @Override
    public List<CloudServer> getAllMachinesWithTag( NovaContext context){
        String confTags =  conf.server.bootstrap.tags;
        logger.info( "getting all machines with tag [{}]", confTags );
        List<CloudServer> servers = new LinkedList<CloudServer>();
        if ( StringUtils.isEmpty(confTags) ){
            logger.info( "confTags is null, not finding all machines" );
            return servers;
        }

        // get all servers with tags matching my configuration.
        return CloudifyUtils.getAllMachinesWithPredicate( new ServerTagPredicate( conf ), context );
    }

    @Override
    public String createAdvancedParams() {

        ObjectNode advancedParamsJsonObject = Json.newObject();
        advancedParamsJsonObject.put( "type", conf.server.cloudProvider.label );
        ObjectNode paramsJsonObject = Json.newObject();
        paramsJsonObject.put( "userId", conf.server.bootstrap.api.key );
        paramsJsonObject.put( "project", conf.server.bootstrap.api.project );
        paramsJsonObject.put( "secretKey", conf.server.bootstrap.api.secretKey );
        advancedParamsJsonObject.put( "params", paramsJsonObject );

        return advancedParamsJsonObject.toString();
    }

}