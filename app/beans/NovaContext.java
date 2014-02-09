package beans;

import beans.config.CloudProvider;
import clouds.base.CloudApi;
import clouds.base.CloudServerApi;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.rest.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ApplicationContext;
import utils.CloudifyUtils;

import java.util.Properties;

/**
 * User: evgenyf
 * Date: 1/28/14
 */
public class NovaContext{
    final ComputeServiceContext context;
    final String zone;

    CloudServerApi api = null;
    //        RestContext<NovaApi, NovaAsyncApi> nova = null;
//        RestContext cloud = null;
    Object cloudApi;
    ComputeService computeService = null;
    final CloudProvider cloudProvider;

    private static Logger logger = LoggerFactory.getLogger(NovaContext.class);

    public NovaContext(NovaCloudCredentials cloudCredentials) {
        logger.info("initializing bootstrapper with cloudCredentials [{}]", cloudCredentials.toString());
        Properties overrides = new Properties();
        if (cloudCredentials.apiCredentials) {
            overrides.put("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
        }
        cloudProvider = cloudCredentials.cloudProvider;
        context = CloudifyUtils.computeServiceContext(cloudCredentials.cloudProvider.label, cloudCredentials.getIdentity(), cloudCredentials.getCredential(), true);


        this.zone = cloudCredentials.zone;
    }

    public NovaContext( CloudProvider cloudProvider, String project, String key, String secretKey, String zone, boolean apiCredentials )
    {
        // todo : ugly - we should resort to "credentials factory" - will be required once we support other platforms other than Nova.
        this(ApplicationContext.getNovaCloudCredentials()
                .setCloudProvider(cloudProvider)
                .setProject(project)
                .setKey(key)
                .setApiCredentials(apiCredentials)
                .setZone(zone)
                .setSecretKey(secretKey));
    }

    private Object getCloudApi(){
        if( cloudApi == null ){
//        		cloudApi = CloudifyFactory.createCloudApi( getComputeService(), cloudProvider, getCloudApi() );

            RestContext restContext = context.unwrap();
            cloudApi = restContext.getApi();
//        		cloudApi = contextBuilder.buildApi(
//        				(Class)( ( RestApiMetadata )contextBuilder.getApiMetadata() ).getApi() );
        }

        return cloudApi;
    }

    public CloudServerApi getApi(){
        if( api == null ){
//            	RestContext cloudRestContext = getCloud();
//            	Object cloudRestContextApi = cloudRestContext.getApi();
            CloudApi cloudApiLocal = ApplicationContext.getCloudifyFactory().
                    createCloudApi( getComputeService(), cloudProvider, getCloudApi() );
            CloudServerApi serverApiForZone = cloudApiLocal.getServerApiForZone( zone );
            api = serverApiForZone;
        }
        return api;
    }
    public ComputeService getComputeService(){
        if ( computeService == null ){
            computeService = context.getComputeService();
        }
        return computeService;
    }

    public void close()
    {
        context.close();
    }
}
