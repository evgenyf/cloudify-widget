package clouds.base;

import beans.config.Conf;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CollectionUtils;
import utils.StringUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: evgenyf
 * Date: 1/28/14
 */
public class ServerTagPredicate implements Predicate<CloudServer> {

    private static Logger logger = LoggerFactory.getLogger(ServerTagPredicate.class);

    final List<String> confTagsList;
    private final String confTags;

    public ServerTagPredicate( Conf conf ){
        confTags =  conf.server.bootstrap.tags;
        if ( !StringUtils.isEmpty(confTags)){
            confTagsList = Arrays.asList(StringUtils.stripAll(confTags.split(",")));
        }
        else{
            confTagsList = null;
        }
    }

    @Override
    public boolean apply( CloudServer server )
    {
        if ( server == null ) {
            return false;
        }

        Map<String, String> metadata = server.getMetadata();
        if ( !CollectionUtils.isEmpty(metadata) && metadata.containsKey( "tags" ) ) {
            String tags = metadata.get( "tags" );
            if ( !StringUtils.isEmpty( tags ) && !CollectionUtils.isEmpty( confTagsList ) ) {
                logger.info( "comparing tags [{}] with confTags [{}]", tags, confTags );
                List<String> tagList = Arrays.asList( StringUtils.stripAll( tags.split( "," ) ) );
                return CollectionUtils.isSubCollection( confTagsList, tagList );
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("has tags [%s]",confTags);
    }
}
