package clouds.base;

import beans.NovaContext;
import models.ServerNode;
import org.jclouds.compute.RunNodesException;

import java.util.List;

/**
 * User: evgenyf
 * Date: 1/28/14
 */
public interface CloudDriver {
    void deleteServer( String serverId, NovaContext novaContext );

    ServerNode bootstrapCloud( ServerNode serverNode );

    void rebuild( ServerNode serverNode, NovaContext novaContext );

    ServerNode createMachine( NovaContext novaContext, long nodeId ) throws RunNodesException;

    List<CloudServer> getAllMachinesWithTag( NovaContext context);

    List<ServerNode> recoverUnmonitoredMachines( NovaContext novaContext );

    String createAdvancedParams();
}