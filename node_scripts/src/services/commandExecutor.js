'use strict';

/**
 * @module commandExecutor
 *
 * @description
 *
 * executes commands in cloudify.
 */
var logger = require('log4js').getLogger('commandExecutor');
var path = require('path');
var spawn = require('child_process').spawn;


/**
 *
 * @param {object} opts
 * @param {string} opts.arguments CSV of arguments to run the command with "bootstrap-cloud,softlayer"
 * @param {string} opts.executable cloudify.sh location
 * @param {string} opts.cloudifyHome the cloudify home directory
 *
 * @param {object} handlers
 * @param {function} handlers.onErr on error <pre>function(error){}</pre>
 * @param {function} handlers.onStdout on std out data <pre>function(data){}</pre>
 * @param {function} handlers.onStderr on err data <pre>function(data){}</pre>
 * @param {function} handlers.onClose <pre>function(exitCode){}</pre>
 */

exports.execute = function( opts , handlers ){

    logger.info('executing', opts);

    var executable = opts.executable;
    var argumentsArray = [].concat(opts.arguments);

    var cloudifyHome = opts.cloudifyHome;


    logger.info( 'executable=' , path.resolve(executable) );
    logger.info( 'splitted arguments=' , argumentsArray );
    logger.info( 'cloudifyHome=' , cloudifyHome );

    process.env.CLOUDIFY_HOME = cloudifyHome;

    var myCmd = spawn( 	executable, argumentsArray );

    if ( !handlers.onStdout ){
        throw 'stdout handler is missing!';
    }
    myCmd.stdout.on('data', handlers.onStdout );

    if ( !handlers.onStderr ){
        throw 'stderr handler is missing';
    }
    myCmd.stderr.on('data', handlers.onStderr );

    if ( !handlers.onErr ){
        throw 'onerr handler is missing';
    }
    myCmd.on('error', handlers.onErr );

    if ( !handlers.onClose ){
        throw 'onclose handler is missing';
    }
    myCmd.on('exit', handlers.onClose);
};