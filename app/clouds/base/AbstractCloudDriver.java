package clouds.base;

import akka.dispatch.Create;
import beans.NovaContext;
import beans.config.CloudProvider;
import beans.config.Conf;
import beans.pool.PoolEvent;
import beans.pool.PoolEventListener;
import clouds.softlayer.SoftlayerCloudServer;
import com.google.common.base.Predicate;
import models.ServerNode;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import server.ApplicationContext;
import utils.CloudifyUtils;
import utils.CollectionUtils;
import views.html.demos.template;

import javax.inject.Inject;
import java.util.*;

/**
 * User: evgenyf
 * Date: 1/28/14
 */
abstract public class AbstractCloudDriver implements CloudDriver{

    protected static Logger logger = LoggerFactory.getLogger(AbstractCloudDriver.class);

    @Inject
    protected PoolEventListener poolEventManager;

    @Inject
    protected Conf conf;

    @Override
    public void deleteServer(String serverId, NovaContext novaContext ) {
        try{
            CloudServerApi api = novaContext.getApi();
            CloudServer server = api.get( serverId );
            if ( server != null ){
                api.delete(serverId);
                server = api.get( serverId );
                logger.info("Server id: {} was terminated.", serverId);
            }
            poolEventManager.handleEvent(new PoolEvent.MachineStateEvent().setType(PoolEvent.Type.DELETE)/*.setResource(server)TODO?*/);
        }catch(Exception e){
            logger.error("unable to delete server [{}]", serverId);
        }
    }

    @Override
    public ServerNode bootstrapCloud( ServerNode serverNode )
    {
        CloudProvider cloudProvider = conf.server.cloudProvider;
        BootstrapCloudHandler bootstrapCloudHandler =
                ApplicationContext.getBootstrapCloudHandler( cloudProvider );
        return bootstrapCloudHandler.bootstrapCloud( serverNode, conf );
    }

    @Override
    public void rebuild( ServerNode serverNode, NovaContext novaContext ){
        logger.info("rebuilding machine");
        CloudServerApi serverApi = novaContext.getApi();
        try {
            serverApi.rebuild( serverNode.getNodeId() );
        } catch (RuntimeException e) {
            logger.error("error while rebuilding machine [{}]", serverNode, e);
        }
    }

    @Override
    public ServerNode createMachine( NovaContext novaContext, long nodeId ) throws RunNodesException {

        final String name = conf.server.bootstrap.serverNamePrefix;
        logger.info( "Starting to create new instance with name=[{}], tags [{}] ", name, conf.server.bootstrap.tags );//conf.server.bootstrap.imageId, conf.server.bootstrap.flavorId );

        String advancedParams = createAdvancedParams();

        ComputeService computeService = novaContext.getComputeService();
        Template template = createTemplate( computeService );
        Set<? extends NodeMetadata> newNodes = computeService.createNodesInGroup( name, 1, template );

        if( newNodes.isEmpty() ){
            throw new RuntimeException( "Failed to create new compute instance" );
        }
        if( newNodes.size() > 1 ){
            throw new RuntimeException( "More than one planned new compute instance created" );
        }

        NodeMetadata newNodeMetadata = newNodes.iterator().next();

        String advancedParamsStr = createAdvancedParams();

        ServerNode serverNode = new ServerNode( newNodeMetadata, advancedParamsStr );

        return bootstrapCloud( serverNode );
    }

    private Template createTemplate( ComputeService computeService ) {
        TemplateBuilder templateBuilder = computeService.templateBuilder();
        ///*.osFamily(OsFamily.CENTOS)*//*.hardwareId("1640,2238,13899").locationId("37473")*/.build();
        String hardwareId = conf.server.cloudBootstrap.hardwareId;
        String locationId = conf.server.cloudBootstrap.locationId;
        if( !StringUtils.isEmpty( hardwareId )){
            templateBuilder.hardwareId( hardwareId );
        }
        if( !StringUtils.isEmpty( locationId ) ){
            templateBuilder.locationId(locationId);
        }

        return templateBuilder.build();
    }

    @Override
    public List<ServerNode> recoverUnmonitoredMachines( NovaContext novaContext){
        List<ServerNode> result = new ArrayList<ServerNode>(  );
        logger.info( "recovering all list machines" );
        List<CloudServer> allMachinesWithTag = getAllMachinesWithTag( novaContext );
        logger.info( "found [{}] total machines with matching tags filtering lost", CollectionUtils.size(allMachinesWithTag)  );
        if ( !CollectionUtils.isEmpty( allMachinesWithTag )){
            for ( CloudServer server : allMachinesWithTag ) {
                ServerNode serverNode = ServerNode.getServerNode( server.getId() );
                if ( serverNode == null ){
                    ServerNode newServerNode = new ServerNode( server );
                    logger.info( "found an unmonitored machine - I should add it to the DB [{}]", newServerNode );
                    result.add( newServerNode );
                }
            }
        }
        return result;
    }
}