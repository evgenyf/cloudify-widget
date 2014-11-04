package controllers.checkers;

import beans.cloudbootstrap.AwsEc2CloudProviderCreator;
import cloudify.widget.ec2.Ec2ConnectDetails;
import cloudify.widget.ec2.Ec2ImageShare;
import cloudify.widget.ec2.Ec2SecurityGroup;
import cloudify.widget.ec2.WidgetSecurityGroupData;
import com.amazonaws.services.ec2.model.SecurityGroup;
import controllers.GsController;
import models.User;
import models.Widget;
import org.codehaus.jackson.JsonNode;
import play.mvc.Result;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/11/14
 * Time: 10:43 AM
 */
public class CheckSecurityGroupController extends GsController {

    public static Result testSecurityGroup( Long widgetId ){
        User user = validateSession();
        Widget widget = Widget.findByUserAndId(user, widgetId);

        WidgetSecurityGroupData securityGroupData = widget.getSecurityGroupData();
        if ( securityGroupData == null ){
            return badRequest("security group details not saved on the widget. please save first.");
        }

        JsonNode jsonNode = request().body().asJson();

        if ( !jsonNode.has("apiKey")){
            return badRequest("missing api key");
        }

        String apiKey = jsonNode.get("apiKey").getTextValue();

        if ( !jsonNode.has("apiSecretKey")){
            return badRequest("missing api secret key");
        }

        String apiSecretKey = jsonNode.get("apiSecretKey").getTextValue();

        if ( !jsonNode.has("name")){
            return badRequest("must specify security group name");
        }

        String name = jsonNode.get("name").getTextValue();


        try {
            Ec2ConnectDetails connectDetails = new Ec2ConnectDetails( apiKey, apiSecretKey);
            Ec2SecurityGroup sg = new Ec2SecurityGroup( connectDetails );
            securityGroupData.setName(name);

            List<SecurityGroup> securityGroups;
            try {
                securityGroups = sg.getSecurityGroups(securityGroupData);
            }catch(Exception e){
                return badRequest("security group does not exist : " + e.getMessage());
            }
            if ( securityGroups != null ) {
                if ( !sg.isSecurityGroupOpen(securityGroupData, securityGroups) ){
                    return badRequest("your security group does not include all ips/ports you requested on the widget");
                }
            }
        }catch(Exception e){
            return internalServerError(e.getMessage());
        }
        return ok("your security group seems valid!");
    }
}
