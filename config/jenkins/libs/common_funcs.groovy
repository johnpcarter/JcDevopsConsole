package com.jc.devops.common

def ssh(id, server, command) {
	sh("ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $id@$server $command")
}

def ant(id, server, command) {
	ssh(id, server, "ant $command")	
}

def authString(user, password) {

	 def encoded = "${user}:${password}".bytes.encodeBase64().toString()

	 return "Basic $encoded"
}

def runContainer(server, containerName) {

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				url: "${server}/rad/jc.devops:api/docker/${containerName}", 
				validResponseCodes: '200:201'
}

def isISRunningInRemoteServer(server, error) {
	
	status=false;	
	
	try {

		response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${server}/invoke/wm.server:ping", 
				validResponseCodes: '200:201'

		if (response.content)
			status=true;
		else
			status=false;
	} catch (exc) {

		status=false;
	}

	IS_SVR_RUNNING = status;

	if (!status && error)
		throw new Exception();

	return status;
}