/**
 * opts = {
         *      'sendEmail' : true,
         *      'applicationName' : '__recipe_application_name',
         *      'managerIp' : '__manager_ip',
         *      'serviceName' : '__recipe_service_name',
         *      'mandrill' : {
         *          'to' : [ { 'email': , 'name' : , 'type' : }]
         *
         *          'apiKey' : '__apiKey',
         *         'templateName' : '__templateName',
         *         'data' : [
         *              {
         *                  'name': '__name__',
         *                  'content' : '__content__'
         *              },
         *              ... // expecting : 'name' , 'publicIp'
         *         }
         *      }
 * }
 *
 *
 * The method does the following assuming 'sendEmail' is true and mandrill details are available
 *
 *   + resolve service IP
 *   + replace placeholder $HOST on link
 *   + replace data.link value to `<a href='__link__'>__linkTitle__</a>
 *
 *
 * In mandrill, users should define an email with the following placeholder (example) :
 *
 *
 *
 * hello <span mc:edit="name"> __name__ </span>,
 *
 * You service is available at <span mc:edit="link"> __link_html__ </span>
 *
 * Yours,
 * Team
 *
 *
 *
 * NOTE: Mandrill does not provide placeholders for TEXT format emails (support pending) so
 * the text format email should be something like "please revisit widget page __hardcoded_url__" to check the status
 *
 *
 *
 */

var MandrillMailSender = require('./MandrillMailSender');
var cloudifyRestClient = require('./cloudifyRestClient');
var logger = require('log4js').getLogger('PostExecution');
var _ = require('lodash');


/**
 *
 * @param opts
 * @param values : { 'serviceIp' : '__serviceIp__' }
 */
exports.modifyMandrillData = function( opts, values ){
    try {
        var data = opts.mandrill.data;



        function getData(name) {
            return _.find(data, { 'name': name });
        }

        var publicIp = getData('publicIp');
        if ( !publicIp ) {
            data.push({ 'name': 'publicIp', 'content': values.serviceIp});
        }else{
            publicIp.content = values.serviceIp;
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
 *
 * @param opts
 *
 *          {
 *              'managerIp (string)' : '__managerIp__',
 *              'applicationName (string)' : '__applicationName__',  // "default" ,
  *             'serviceName (string)' : '__serviceName__', // blusolo
  *             'sendEmail (boolean)' : '__sendEmail__', //true
  *             'action (enum: install/bootstrap): '__action__', //install
 *          }
 *
 * @param callback  - function( erro, resolvedIp )
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
 *
 * @param opts
 *
 *          {
 *                  'mandrill' : ' see documentation in main.js file '
 *          }
 *
 * @param values
 *                  {   'serviceIp' : '__resolved_ip_for_service___' }
 * @param callback
 */
exports.sendEmail = function( opts, values, callback ){
    logger.info('sending email');
    // replace placeholder on link
    exports.modifyMandrillData( opts, values );

    // small model changes
    opts.mandrill.bcc = opts.bcc;
    opts.mandrill.privateKey = opts.privateKey;

    MandrillMailSender.sendEmail( opts.mandrill , callback )

};

