package beans.scripts;

import java.io.File;
import java.util.*;

import beans.config.Conf;
import cloudify.widget.allclouds.executiondata.ExecutionDataModel;
import cloudify.widget.common.MandrillSender;
import cloudify.widget.common.asyncscriptexecutor.*;
import models.ServerNode;

import org.apache.commons.exec.CommandLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ApplicationContext;

public class FileBasedScriptExecutor implements ScriptExecutor{
	
/*    @Inject
    private Conf conf;*/
	
	private static Logger logger = LoggerFactory.getLogger( FileBasedScriptExecutor.class );

    private Conf conf;
	
	public FileBasedScriptExecutor(){
		logger.info( "---Initializing FileBasedScriptExecutor---" );
	}

	/**
	 * Used for bootstrapping
     * @param cmdLine
     * @param serverNode
//	 * @param cloudBootstrapConfiguration
     */
	@Override
	public IAsyncExecution runBootstrapScript(CommandLine cmdLine, ServerNode serverNode) {

        ExecuteData executeData = addCommonProps(cmdLine, serverNode);
        executeData.action = "bootstrap";

        IAsyncExecution impl = new AsyncExecutionImpl();


        IAsyncExecutionDetails executionDetails = getExecutionDetails(serverNode, executeData.action);
        impl.setDetails(executionDetails);
        impl.writeTask( executeData );
        logger.info("new bootstrap task written to [{}]", executionDetails.getTaskFile());

        return impl;
	}




    @Override
    public IAsyncExecution getBootstrapExecution( ServerNode serverNode){
        ExecuteData executeData = addCommonProps(new ExecuteData(), serverNode);
        executeData.action = "bootstrap";
        IAsyncExecution impl = new AsyncExecutionImpl();

        impl.setDetails(getExecutionDetails( serverNode, executeData.action ));
        return impl;
    }

    @Override
    public IAsyncExecution getInstallExecution( ServerNode serverNode ){
        ExecuteData executeData = addCommonProps( new ExecuteData(), serverNode);
        executeData.action = "install";

        IAsyncExecution impl = new AsyncExecutionImpl();

        impl.setDetails(getExecutionDetails( serverNode, executeData.action ));
        return impl;
    }
	
	
	/**
	 * used for running install and uninstall of applications
	 * @param cmdLine
//	 * @param server
	 */
    @Override
	public void runInstallationManagementScript( CommandLine cmdLine, ServerNode serverNode ){
    	

    	logger.debug("running install script for serverNode [{}]" , serverNode.toDebugString() );

        ExecuteData executeData = addCommonProps(cmdLine, serverNode);
        executeData.action = "install";
		
        IAsyncExecution impl = new AsyncExecutionImpl();

        impl.setDetails(getExecutionDetails( serverNode, executeData.action ));
        impl.writeTask( executeData );
    }



    @Override
    public void runTearDownCommand(CommandLine cmdLine) {

    }

    private static ExecuteData addCommonProps( CommandLine cmdLine, ServerNode serverNode ) {
        ExecuteData result = new ExecuteData();
        addCommonProps( result, cmdLine);
        addCommonProps( result, serverNode );
        return result;
    }
    private static ExecuteData addCommonProps( ExecuteData result, CommandLine cmdLine ) {
        result.executable =cmdLine.getExecutable();
        result.arguments = Arrays.asList(cmdLine.getArguments());
        return result;
    }
    private static ExecuteData addCommonProps( ExecuteData result, ServerNode serverNode ){

        result.serverNodeId = String.valueOf(serverNode.getId());
        result.cloudifyHome = ApplicationContext.get().conf().server.environment.cloudifyHome;
        result.managerIp = serverNode.getPublicIP();

        // add application name and service for mail sending

        ExecutionDataModel executionDataModel = serverNode.getExecutionDataModel();
        if ( serverNode.getWidget() != null && serverNode.getWidget().installFinishedEmailDetails != null && serverNode.getWidget().installFinishedEmailDetails.isEnabled() && executionDataModel.has(ExecutionDataModel.JsonKeys.LOGIN_DETAILS)  ) {
            logger.info("adding properties for sending email");
            try {
                result.serviceName = serverNode.getWidget().getConsoleUrlService();
                result.applicationName = serverNode.getWidget().getRecipeName();

                MandrillSender.MandrillEmailDetails mandrillDetails = serverNode.getWidget().installFinishedEmailDetails.getMandrillDetails();
                result.mandrill.apiKey = mandrillDetails.mandrillApiKey;
                result.mandrill.templateName = mandrillDetails.templateName;

                ExecutionDataModel.LoginDetails loginDetails = executionDataModel.getLoginDetails();

                if ( result.mandrill.data == null ){
                    result.mandrill.data = new ExecuteData.MandrillData();
                }

                result.mandrill.data.name.setContent(loginDetails.name + ' ' + loginDetails.lastName);
                result.mandrill.data.firstName.setContent( loginDetails.name  );
                result.mandrill.data.lastName.setContent(loginDetails.lastName);
                result.mandrill.data.publicIp.setContent(serverNode.getPublicIP());
                result.mandrill.data.link.setContent( serverNode.getWidget().getConsoleURL() );
                result.mandrill.data.linkTitle.setContent( serverNode.getWidget().getConsoleName() );


                result.mandrill.to.add(
                        new ExecuteData.MandrillEmailAddressItem(serverNode.widgetInstanceUserDetails.getEmail(), serverNode.widgetInstanceUserDetails.getName() , "to")
                );

                result.sendEmail = true;
            }catch(Exception e){
                logger.info("error while trying to set properties for sending email",e);
            }
        }
        return result;
    }

    public IAsyncExecutionDetails getExecutionDetails( ServerNode serverNode, String action ){
        IAsyncExecutionDetails details = new AsyncExecutionDetails();

        details.setNewScriptsDir( conf.asyncExecution.newScriptsDir);
        String nodeId = String.valueOf(serverNode.getId());
        details.setTaskFile( new File(conf.asyncExecution.newScriptsDir, String.format("%s_%s.json", nodeId, action)));
        details.setOutputFile( new File(conf.asyncExecution.executingScriptsDir, String.format("%s/output.log", nodeId)));
        details.setStatusFile( new File(conf.asyncExecution.executingScriptsDir, String.format("%s/%s.status", nodeId,action)));

        return details;
    }

// String outputFileName = EXECUTING_SCRIPTS_FOLDER_PATH +
//    nodeId + File.separator + "output.log";

    public Conf getConf() {
        return conf;
    }

    public void setConf(Conf conf) {
        this.conf = conf;
    }
}