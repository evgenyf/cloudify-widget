var assert = require('assert');
var PostExecution = require('../src/services/PostExecution');
var logger = require('log4js').getLogger('testPostExecution');
var _ = require('lodash');



function testModify(){
    logger.info('testing modify mandrill details');
    assert.equal(typeof(PostExecution.modifyMandrillData), 'function', 'modify should be a function');

    function getServerIp( opts ){

        PostExecution.modifyMandrillData(
            opts,
            { 'serviceIp' : '1.1.1.1'}
        );

        logger.info(JSON.stringify(opts));
        var serverIp = _.find( opts.mandrill.data, { 'name' : 'publicIp'}).content;
        return serverIp;
    }

    var serverIp = getServerIp({ 'mandrill' : { 'data' : [ { 'name' : 'myName' } ] }});
    assert.equal(serverIp, '1.1.1.1', 'ip should be on mandrill data');

    serverIp = getServerIp({ 'mandrill' : { 'data' : [ { 'name' : 'myName' }, { 'name' : 'publicIp'} ] }});
    assert.equal(serverIp, '1.1.1.1', 'publicIp value on mandrill data should be modified if already exists');

}


function testConvertMandrillData(){
    logger.info('testing convert mandrill data');

    var mandrillData = { 'guy' : { 'name' : 'guy' , 'content' : 'this is content' }};
    mandrillData = PostExecution.convertMandrillData( mandrillData );
    logger.info(mandrillData);
    assert.equal(mandrillData.length, 1, 'should be an array of size one');
    assert.equal(mandrillData[0].name, 'guy', 'first element should have name guy');
    assert.equal(mandrillData[0].content, 'this is content', 'first element should have content this is content');

    mandrillData = PostExecution.convertMandrillData( mandrillData );
    assert.equal(mandrillData, mandrillData, 'should be untouched if was a list to begin with');
}


function testResolveServiceIp(){
    logger.info('testing resolve serivce ip');
    PostExecution.resolveIp( {
        'managerIp' : '54.173.135.230',
        'applicationName' : 'default',
        'serviceName' : 'blustratus' ,
        'sendEmail' : true,
        'action' :'install'

    } , function( error, result ){
        console.log('ip is resolved', result );
    })
}


function testSendEmail(){
    var testJson = require('../src/conf/dev/test.json');
    PostExecution.sendEmail( testJson, { 'serviceIp' : '1.1.1.1'}, function(){
        logger.info('email was sent', arguments);
    })
}





testModify();
testResolveServiceIp();
testSendEmail();
testConvertMandrillData();


