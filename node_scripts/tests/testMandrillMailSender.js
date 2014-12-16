var conf = require(require('path').join(__dirname,'..','src','conf','dev','test.json' ));
var logger = require('log4js').getLogger('testMandrillSender');


var mandrillMailSender = require('../src/services/MandrillMailSender');


mandrillMailSender.sendEmail( conf.mandrill , function( err ){
    if ( !!err ){
        throw err;
    }
    logger.info('test ended successfully');
});