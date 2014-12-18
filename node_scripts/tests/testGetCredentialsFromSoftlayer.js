var conf = require(require('path').join(__dirname,'..','src','conf','dev','test.json' ));

var postExecution = require('../src/services/PostExecution');

var logger = require('log4js').getLogger('testGetCredentialsFromSoftlayer');

postExecution.resolveSoftlayerCredentials(conf.getCredentials, function(){
    logger.info(arguments);
});