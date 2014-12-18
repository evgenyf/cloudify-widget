'use strict';
/**
 * @module MandrillMailSender
 * @description This service uses MailChimp to send email to users that their installation is ready.
 **/

var logger = require('log4js').getLogger('MandrillMailSender');


var mandrill = require('mandrill-api/mandrill');


/**
 * @param {object} opts
 * @param {string} opts.apiKey
 * @param {string} opts.templateName
 * @param {object} opts.data templateContent
 * @param {string} opts.to
 * @param {string} opts.bcc
 * @param {string} opts.privateKey
 */
function sendEmail( opts ){
    /*jshint camelcase: false */
    var mandrill_client = new mandrill.Mandrill(opts.apiKey);
    var template_name = opts.templateName;
    var template_content = opts.data;
    var message = {
        'to': opts.to
    };

    try {
        if (!!opts.bcc) {
            logger.info('adding bcc address', opts.bcc);
            message.bcc_address = opts.bcc;
        }else{
            logger.info('bcc address not specified. skipping');
        }
    }catch(e){
        logger.error('unable to add bcc address');
    }


    try{
        if ( !!opts.privateKey ){
            logger.info('adding private key as attachment');
            var content64 = new Buffer(opts.privateKey).toString('base64');
            message.attachments = [
                {
                    'type' : 'text/plain',
                    'name' : 'privateKey.pem',
                    'content' : content64
                }
            ];
        }else{
            logger.info('no private key. skipping addition as attachment');
        }

    }catch(e){
        logger.error('unable to add private key as attachment');
    }

    mandrill_client.messages.sendTemplate({'template_name': template_name, 'template_content': template_content, 'message': message}, function(result) {
        logger.info(result);
        /*
         [{
         'email': 'recipient.email@example.com',
         'status': 'sent',
         'reject_reason': 'hard-bounce',
         '_id': 'abc123abc123abc123abc123abc123'
         }]
         */
    }, function(e) {
        // Mandrill returns the error as an object with name and message keys
        console.log('A mandrill error occurred: ' + e.name + ' - ' + e.message);
        // A mandrill error occurred: Unknown_Subaccount - No subaccount exists with the id 'customer-123'
    });

    logger.info('after sending the email');
}


exports.sendEmail = function ( opts , callback ){
    try {
        logger.info('sending emails', opts);
        sendEmail( opts );
    }catch(e){
        callback(e);
    }
};