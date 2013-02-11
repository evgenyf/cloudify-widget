package beans.config;

import beans.GsMailer;
import utils.Utils;

import java.io.File;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 1:32 PM
 */
public class Conf {

    public ApplicationConfiguration application = new ApplicationConfiguration();

    public SmtpConf smtp = new SmtpConf();

    // who is sending the mail?
    public GsMailer.Mailer mailer = new GsMailer.Mailer();

    @Config( ignoreNullValues =  true )
    public String demoUserEmailSuffix = "_demo@gigaspaces.com";

    public ServerConfig server = new ServerConfig();

    public FeaturesConfig features = new FeaturesConfig();

    public SettingsConfig settings = new SettingsConfig();

    @Config(ignoreNullValues = true)
    public boolean sendErrorEmails = false;

    @Config( playKey = "spring.context")
    public String springContext = null;

    public CloudifyConfiguration cloudify = new CloudifyConfiguration();

    public String mixpanelApiKey = null;


    public static class SettingsConfig{
        @Config( ignoreNullValues = true )
        public boolean expireSession = false; // do not use the session expired mechanism.
    }

    public static class FeaturesConfigItem {
        public String users = ".*"; // all users by default
        @Config( ignoreNullValues = true )
        public boolean on = false ;

        public FeaturesConfigItem setUsers(String users) {
            this.users = users;
            return this;
        }

        public FeaturesConfigItem setOn(boolean on) {
            this.on = on;
            return this;
        }
    }

    public static class FeaturesConfig {
        public FeaturesConfigItem socialLogin = new FeaturesConfigItem();
    }


    public static class WidgetConfiguration{

        public String serverId;

        public long stopTimeoutMillis;
    }

    public static class CloudifyConfiguration{

        public long deployWatchDogProcessTimeoutMillis = Utils.parseTimeToMillis( "2mn" );
        
        public long bootstrapCloudWatchDogProcessTimeoutMillis = Utils.parseTimeToMillis( "2mn" );

        public File deployScript=Utils.getFileByRelativePath( "/bin/deployer.sh" );

        public String removeOutputLines = "";

        public String removeOutputString = "";

        @Config(ignoreNullValues = true)
        public long verifyLoginUserIdTimeout = Utils.parseTimeToMillis("20s");
    }


}
