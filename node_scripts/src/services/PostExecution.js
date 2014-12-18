'use strict';
/**
 *
 * @module PostExecution
 *
 * @description
 *
 * <pre>opts = {
         'sendEmail' : true,
         'applicationName' : '__recipe_application_name',
         'managerIp' : '__manager_ip',
         'serviceName' : '__recipe_service_name',
         'mandrill' : {
             'to' : [ { 'email': , 'name' : , 'type' : }]

             'apiKey' : '__apiKey',
            'templateName' : '__templateName',
            'data' : [
                 {
                     'name': '__name__',
                     'content' : '__content__'
                 },
                 ... // expecting : 'name' , 'publicIp'
            }
         }
 * }</pre>
 *
 * <p>
 * The method does the following assuming 'sendEmail' is true and mandrill details are available
 * </p>
 *   <ul>
 *      <li>resolve service IP</li>
 *      <li>replace placeholder $HOST on link</li>
 *      <li>replace data.link value to `&lt;a href='__link__'>__linkTitle__&lt;/a></li>
 *   </ul>
 *
 *
 * In mandrill, users should define an email with the following placeholder (example) :
 *
 *
 *
 * <pre>
 * hello &lt;span mc:edit="name"> __name__ &lt;/span>,
 *
 * You service is available at &lt;span mc:edit="link"> __link_html__ &lt;/span>
 *
 * Yours,
 * Team
 * </pre>
 *
 *
 *
 * <b>NOTE</b>: Mandrill does not provide placeholders for TEXT format emails (support pending) so<br/>
 * the text format email should be something like "please revisit widget page __hardcoded_url__" to check the status
 *
 *
 *
 */

var MandrillMailSender = require('./MandrillMailSender');
var cloudifyRestClient = require('./cloudifyRestClient');
var logger = require('log4js').getLogger('PostExecution');
var _ = require('lodash');
var async = require('async');


/**
 *
 * @param opts
 * @param {object} values
 * @param {string} values.serviceIp Ip of service to replace $HOST in links
 * @param {string} values.username username to login to machine
 * @param {string} values.password password to login to machine
 */
exports.modifyMandrillData = function( opts, values ){
    try {
        var data = opts.mandrill.data;



        var getData = function(name) {
            return _.find(data, { 'name': name });
        };

        var publicIp = getData('publicIp');
        if ( !publicIp ) {
            data.push({ 'name': 'publicIp', 'content': values.serviceIp});
        }else{
            publicIp.content = values.serviceIp;
        }
        if ( !!values.username ) {
            data.push({'name': 'username', 'content': values.username});
        }

        if ( !!values.password ){
            data.push({'name' : 'password', 'content' : values.password});
        }


        var linkData = getData('link');
        var linkTitleData = getData('linkTitle');

        if (!!linkTitleData && !!linkTitleData.content) {
            linkTitleData.content = linkTitleData.content.replace('$HOST', values.serviceIp);
            linkData.content = '<a href="' + linkData.content.replace('$HOST', values.serviceIp) + '">' + linkTitleData.content + '</a>';
        }
    }catch(e){
        logger.error('unable to modify mandrill data',e);
    }
};

/**
 * @callback ResolvedIpCallback
 * @param {Error} error
 * @param {string} resolvedIp
 */
/**
 *
 * @param {object} opts
 * @param {string} opts.managerIp
 * @param {string} opts.applicationName
 * @param {string} opts.serviceName
 * @param {string} opts.sendEmail
 * @param {string} opts.action
 * @param {ResolvedIpCallback} callback
 */
exports.resolveIp = function( opts, callback ){
    logger.info('post execution handler');
    if ( opts.sendEmail === true && opts.action === 'install'){
        logger.info('resolving ip');
        cloudifyRestClient.getServiceIp( opts.managerIp, opts.applicationName, opts.serviceName, callback );
    } else{
        logger.info('did not send email since sendEmail or action did not apply', opts.sendEmail, opts.action );
    }
};

/**
 *
 * @callback ResolveSoftlayerCredentialsCallback
 * @param {Error} error
 * @param {object} result
 * @param {string} result.username
 * @param {string} result.password
 **/
 /**
 * @param {object} opts
 * @param {object} opts.softlayerIncludePasswordDetails
 * @param {string} opts.softlayerIncludePasswordDetails.username
 * @param {string} opts.softlayerIncludePasswordDetails.apiKey
 * @param {string} opts.softlayerIncludePasswordDetails.enabled "true" or "false"
 * @param {string} opts.softlayerIncludePasswordDetails.serverIp
 * @param {ResolveSoftlayerCredentialsCallback} callback
 */
exports.resolveSoftlayerCredentials = function( opts, callback ){
    logger.info('will get credentials for softlayer machine');
    var details = opts.softlayerIncludePasswordDetails;
    if ( !!details && details.enabled === 'true' ){


        async.waterfall([
            function getAllMachinesFromSoftlayer(_callback){
                var https = require('https');
                var util = require('util');
                var url =  util.format('https://%s:%s@api.softlayer.com/rest/v3/SoftLayer_Account/VirtualGuests.json?objectMask=virtualGuests.operatingSystem.passwords', details.username, details.apiKey);
                var request = https.get(url, function( res ){

                    var data = '';

                    res.on('data', function(_data){
                        data += _data.toString();
                    });

                    res.on('end', function(){
                        _callback(null, data);
                    });

                });
                request.on('error', function(e){
                    _callback(e);
                });
            },
            function findCredentials( allMachines/*, _callback*/ ){
                logger.info('got response from client. will not print allMachines as contains private information.. ');

                if ( typeof(allMachines) === 'string' ){
                    allMachines = JSON.parse(allMachines);
                }

                var server  = _.find(allMachines, function(item){ return item.primaryIpAddress === details.serverIp; });
                if ( !server ){
                    logger.error('unable to find server!!! looking for ', details.serverIp, 'but all I have are', _.map(allMachines, 'primaryIpAddress'));
                    callback(new Error('could not find server!'), null);
                    return;
                }else{
                    logger.info('found relevant server!');
                    try {
                        var credentials = _.pick(server.operatingSystem.passwords[0], ['username','password']);
                        callback(null, credentials);
                        return;
                    }catch(e){
                        logger.error('found the server, but cannot get credentials from it' );
                        callback(new Error('unable to get username/password from server' + e.stack), null);
                        return;
                    }
                }
            }
        ], function(err){
            logger.info('async waterfall failed',err);
            callback(new Error('could not retrieve credentials' + err.stack));
        });
    }else{
        logger.info('softlayer credentials is not enabled. object exists ', !!details , ' and value is ', !!details && details.enabled);
        callback(null,null);
        return;
    }


};


/**
 *
 * This function will make sure mandrill template content is a list and not an associative array.
 *
 * @param mandrillDataFromFile
 */
exports.convertMandrillData = function( mandrillDataFromFile ){

    var result = mandrillDataFromFile;

    if ( !require('util').isArray(result)){ //if not a list, lets turn it into one
        result = [];
        for ( var i in mandrillDataFromFile ){
            if ( mandrillDataFromFile.hasOwnProperty(i)) {
                result.push(mandrillDataFromFile[i]);
            }
        }
    }

    return result;

};


/**
 * @callback SendEmailCallback
 *
 */

/**
 *
 *
 * @param {object} opts
 * @param {object} opts.mandrill see documentation in main.js file
 * @param {object} values
 * @param {string} values.serviceIp
 * @param {string} values.username
 * @param {string} values.password
 * @param {SendEmailCallback} callback
 */
exports.sendEmail = function( opts, values, callback ){
    logger.info('sending email');
    // replace placeholder on link
    exports.modifyMandrillData( opts, values );

    // small model changes
    opts.mandrill.bcc = opts.bcc;
    opts.mandrill.privateKey = opts.privateKey;

    MandrillMailSender.sendEmail( opts.mandrill , callback );

};

