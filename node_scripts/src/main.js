
var path = require('path');
var fs = require('fs-extra');
var async = require('async');
var logger = require('log4js').getLogger('main');
var conf = require('./services/conf');
var services = require('./services');

logger.setLevel(conf.log.level);
logger.trace('conf',JSON.stringify(conf));



exports.initialize = function(){
    for ( var i in conf.directories ){
        var absPath = path.resolve(conf.directories[i]);
        logger.trace('making sure all directories exist',i, absPath );
        fs.mkdirsSync(absPath);
    }
};




exports.doMain = function(){
    logger.trace('running main');
    var outputWriter = null;
    var opts  = null;
    async.waterfall([

        function initialize( callback ) {
            logger.info('initializing');
            exports.initialize();
            callback();
        },
        function getNextTask( callback ){
            logger.trace('getting next task');
            services.taskReader.getNextTask( conf.directories.newDirectory , callback )
        },
        function createExecutionDir( _opts, callback ){
            opts = _opts;
            logger.info('creating execution dir');
            // change logger name
            logger = require('log4js').getLogger('executor-' +  opts.serverNodeId);
            outputWriter = new services.taskOutputWriter.Writer( conf.directories.executingDirectory, opts.serverNodeId, opts.action );

            outputWriter.createDir();
            callback();
        },
        function writeConfObj(  callback  ){
            logger.info('writing configuration file');
            outputWriter.writeConfigFile( opts );
            callback(  );
        },
        function executeCommand(  callback  ){
            logger.info('executing command');

            var logAppender = outputWriter.getLogAppender( );
            var handlers = {
                'onStdout' : function( data ){
                    process.stdout.write(data);
                    logAppender.write(data);
                },
                'onStderr' : function( data ){
                    process.stdout.write(data);
                     logAppender.write(data);
                },
                'onClose' : function( exitCode ){
                    if ( exitCode >= 0) {
                        logger.info('exited with code ', exitCode, arguments);
                        outputWriter.writeStatus({
                            'exitCode': exitCode || 1

                        });
                    }

//                    if ( exitCode === 0 ){
                        callback();
//                    }
                },
                'onErr' : function( err ){
                    logger.error(err);
                    outputWriter.writeStatus(
                        {
                            'exitCode' : 1,
                            'error' :  err

                    });


                }
            };
            services.commandExecutor.execute( opts, handlers  );
        },

        function postExecution( callback ){

            opts.mandrill.data = services.postExecution.convertMandrillData( opts.mandrill.data );

            services.postExecution.resolveIp( opts, function( error, resolvedIp ){

                services.postExecution.sendEmail( opts, { 'serviceIp' : resolvedIp }, callback );

            });

        },

        function postEmailSent ( err ) {
            if ( !! err ){
                logger.error(err);
                return;
            }

            logger.info('email sent successfully');
        }

    ], function( err ){
        if ( !!err ){
            logger.error('got an error',err);
            throw err;
        }
        logger.info('finished');
    });


};


if ( require.main === module ){
   exports.doMain();
}

