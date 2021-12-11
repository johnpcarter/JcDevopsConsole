
// common funcs

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

def build(dockerHost, certsLoc, buildName, includeTestPackage) {

	def body = """ {
    	"name": "${buildName}",
    	"dockerHost": "${dockerHost}",
    	"httpsCert": "${certsLoc}"
	}"""

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST',
				requestBody: body,
				ignoreSslErrors: true, 
				url: "${devops_container}/rad/jc.devops:api/docker/build?increment=true&includeTestPackage=${includeTestPackage}", 
				validResponseCodes: '200:201'

	jsn = readJSON file: '', text: "${response.content}"

	API_IMAGE = jsn.targetTag

	return jsn.newVersion
}

def startContainers(dockerHost, certsLoc, runName, version, runTests, limitToContainers) {

	def alias = java.net.URLEncoder.encode(runName, "UTF-8")

	def url = "${devops_container}/rad/jc.devops:api/docker/run/${alias}/${version}"

	if (dockerHost) {
		url = url + "&dockerHost=" + java.net.URLEncoder.encode(dockerHost, "UTF-8")

		if (certsLoc)
		url = url + "&httpsCert=" + java.net.URLEncoder.encode(certsLoc, "UTF-8")
	}

	if (limitToContainers) {
		url = url + "&containers=" + java.net.URLEncoder.encode(limitToContainers, "UTF-8")
	}
	
	if (runTests) {
		url = url + "&test=${runTests}"
	}

	httpRequest acceptType: 'APPLICATION_JSON', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				url: url, 
				validResponseCodes: '200:201'
}

def startK8sContainers(runName, stage, version, runTests, limitToContainers) {

	def alias = java.net.URLEncoder.encode(runName, "UTF-8")

	def url = "${devops_container}/rad/jc.devops:api/k8s/run/${alias}/${version}"

	if (limitToContainers) {
		url = url + "&containers=" + java.net.URLEncoder.encode(limitToContainers, "UTF-8")
	}

	if (runTests) {
		url = url + "&test=${runTests}"
	}
	
	httpRequest acceptType: 'APPLICATION_JSON', 
		contentType: 'APPLICATION_JSON', 
		httpMode: 'GET', 
		ignoreSslErrors: true, 
		url: url, 
		validResponseCodes: '200:201'
}

def stopContainers(dockerHost, certsLoc, runName, version) {

	def alias = java.net.URLEncoder.encode(runName, "UTF-8")

	def url = "${devops_container}/rad/jc.devops:api/docker/stop/${alias}";

	if (dockerHost) {
		url = url + "?dockerHost=${dockerHost}";

		if (certsLoc)
		url = url + "&httpsCert=certsLoc";
	}

	httpRequest acceptType: 'APPLICATION_JSON', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				url: url, 
				validResponseCodes: '200:201'
}

def tagImage(dockerHost, certsLoc, imageName, newName, newVersion) {

		def body = """ {
		"oldName": "${imageName}",
		"newName": "${newName}",
		"newVersion": "${newVersion}",
		"dockerHost": "${dockerHost}",
		"httpsCert": "${certsLoc}"
	}"""

	println("tagging image " + imageName + " to " + newName + ":"  + newVersion)

	def response = httpRequest acceptType: 'APPLICATION_JSON', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${devops_container}/rad/jc.devops:api/docker/image", 
				validResponseCodes: '200:201'

	def jsn = readJSON file: '', text: "${response.content}"

	return jsn.tag;
}

def pushImage(dockerHost, certsLoc, imageName, imageVersion, email, user, password) {

	if (imageName.indexOf(":") == -1 && imageName.indexOf("-${imageVersion}") == -1)
		imageName = imageName + ":" + imageVersion

	def body = """ {
		"imageName": "${imageName}",
		"dockerHost": "${dockerHost}",
		"httpsCert": "${certsLoc}",
		"registryEmail": "${email}",
		"registryUser": "${user}",
		"registryPassword": "${password}"
	}"""

	println("pushing image " + imageName + " to repository")

	httpRequest acceptType: 'APPLICATION_JSON', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${devops_container}/rad/jc.devops:api/docker/image", 
				validResponseCodes: '200:201'
}

// repoUrl - e.g. https://github.com/johnpcarter/api-deployment.git
def checkoutAPIs(repo, repoAccount) {
	def repoUrl = "https://github.com/${repoAccount}/${repo}.git"

	checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: repoAccount, url: repoUrl]]])
}

def isISRunningInRemoteServer(server, error) {
	
	status=false;	
	
	try {

		println("pinging " + server);

		response = httpRequest acceptType: 'APPLICATION_JSON', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
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

// API Gateway functions

// deploy API from one API Gateway to another via Promotion
// Remote gateway is configure via Stage properties.
def promoteAPI(apigwUrl, apigwAuth, stage, apis, maturity) {

	apiString = null;

	apis.each{ a -> 

		if (apiString == null) {
			apiString = "[\"${a}\""
		} else {		
			apiString = apiString + ",\"${a}\""
		}

		if (maturity)
			setAPIMaturity(apigwUrl, apigwAuth, a, maturity)
	}

	apiString = apiString + "]"

	def body = """ {
		"description": "Tested APIS ${apis}",
		"name": "CDI-${BUILD_NUMBER}",
		"destinationStages": ["${stage}"],
		"promotedAssets": {
    		"api": ${apiString}
  		}
	}"""

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/promotion", 
				validResponseCodes: '200:201'
}

// publish the API to the given API Portal and community
// Determine id's for stage, portal and communitiy via API
def publishAPI(apigwUrl, apigwAuth, stage, id, portalName, communityName) {

	if (stage != null) {

		response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/stages/" + stage, 
				validResponseCodes: '200'


		jsn = readJSON file: '', text: "${response.content}"

		//def url = jsn.stages[0].url;
		//def name = jsn.stages[0].name;

		// publish from API Gateway indicated by staging (NOT master!)
		url = jsn.stage.url;
		auth = jsn.stage.name;

	} else {

		url = apigwUrl;
		auth = apigwAuth
	}

	portalId = getPortalId(url, auth,  portalName)
	communityId = getPortalCommunityId(url, auth, portalId, communityName)

	def body = """ {
		"portalGatewayId": "${portalId}",
		"communities": ["${communityId}"],
		"endpoints": ["${apigwUrl}/rad/${id}"]
	}"""

	println("Publishing API's via "+url+" from "+auth+" to " + portalName)

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: auth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${url}/rest/apigateway/apis/" + id + "/publish", 
				validResponseCodes: '200'
}

// Removes given API from API Portal
def unpublishAPI(apigwUrl, apigwAuth, apiName, id, portalName, communityName) {

	portalId = getPortalId(url, apigwAuth, portalName)
	communityId = getPortalCommunityId(url, apigwAuth, portalId, communityName)

	def body = """ {
		"portalGatewayId": "${portalId}",
		"communities": ["${communityId}"]
	}"""

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/" + id + "/unpublish", 
				validResponseCodes: '200'

}

// Change maturity level for given API.
// maturity labels are set via extended property 'apiMaturityStatePossibleValues'

def setAPIMaturity(apigwUrl, apigwAuth, id, maturity) {

   apiWrapper = getAPI(apigwUrl, apigwAuth, id);

   println("Setting maturity to " + maturity + " for version " + apiWrapper.apiResponse.api.apiVersion);

    //pain, have to deactivate before update, really need option to only update attribute, NOT apiDefinition
    if (apiWrapper.apiResponse.api.isActive)
    	deactivateAPI(apigwUrl, apigwAuth, id);
	
	// fixed in 10.4
	// bug in response, means the type is a string list, whereas it should be a string!!
	//apiWrapper.apiResponse.api.apiDefinition.type = apiWrapper.apiResponse.api.apiDefinition.type[0];

	raw = apiWrapper.apiResponse.api.apiDefinition.toString();

	def body = """{
		"maturityState": "${maturity}",
		"apiVersion": "${apiWrapper.apiResponse.api.apiVersion}",
  		"apiDefinition": ${raw}
	}"""

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}", 
				validResponseCodes: '200:402'

	return response.content;
}

def getPortalId(apigwUrl, apigwAuth, portalName) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				requestBody: "", 
				url: "${apigwUrl}/rest/apigateway/portalGateways", 
				validResponseCodes: '200'

	def jsn = readJSON file: '', text: "${response.content}"

	def id = null;

	jsn.portalGatewayResponse.each { portal -> 
		
		if (portal.gatewayName == portalName) {

			id = portal.id;

			println("found id ${id} for portal name ${portalName}");
		}
	}
	
	return id;
}

def getPortalCommunityId(apigwUrl, auth, portalId, communityName) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: auth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				requestBody: "", 
				url: "${apigwUrl}/rest/apigateway/portalGateways/communities?portalGatewayId="+portalId, 
				validResponseCodes: '200'

	def jsn = readJSON file: '', text: "${response.content}"

	def id = null;

	jsn.portalGatewayResponse.communities.portalCommunities.each { c -> 
		
		if (c.name == communityName) {

			id = c.id;

			println("found id ${id} for community name ${communityName}");
		}
	}
	
	return id;
}

def getStageId(apigwUrl, apigwAuth, stageName) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				requestBody: "", 
				url: "${apigwUrl}/rest/apigateway/stages", 
				validResponseCodes: '200'

	def jsn = readJSON file: '', text: "${response.content}"

	def id = null;

	jsn.stages.each { s -> 
		
		if (s.name == stageName) {

			id = s.id;

			println("found id ${id} for stage name ${stageName}");
		}
	}
	
	return id;
}

def getApplicationId(apigwUrl, apigwAuth, appName, createApp) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				requestBody: "", 
				url: "${apigwUrl}/rest/apigateway/applications", 
				validResponseCodes: '200'

	def jsn = readJSON file: '', text: "${response.content}"

	def id = null;

	jsn.applications.each { a -> 
		
		if (a.name == appName) {

			id = a.id;

			println("found id ${id} for application name ${appName}");
		}
	}
	
	if (id == null && createApp) {

		println("creating app for ${appName}");

		def body = """{
			"name": "${appName}",
			"description": "Autocreated for devops pipeline"
		}"""

		response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/applications", 
				validResponseCodes: '200:201'

		def responseContent = readJSON file: '', text: "${response.content}"

		id = responseContent.id;
	} else {

		println("No app found for ${appName}");
	}

	return id;
}

def getSwaggerDefinition(swaggerUrl, swaggerAuth) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: swaggerAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				requestBody: "", 
				url: swaggerUrl, 
				validResponseCodes: '200'

	def jsn = readJSON file: '', text: "${response.content}"

	return jsn;
}

// Fetch API details from API Gateway
def getAPI(apigwUrl, apigwAuth, id) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				requestBody: "", 
				url: "${apigwUrl}/rest/apigateway/apis/" + id, 
				validResponseCodes: '200'

	//if (raw) {
	//	return response.content;
	//} else {
		def jsn = readJSON file: '', text: "${response.content}"

		return jsn;
	//}
}

// Activate API for use
def activateAPI(apigwUrl, apigwAuth, id) {

	println("Activating API with id " + id);

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/apis/" + id + "/activate", 
				validResponseCodes: '200'

}

// Deactivate API on API gateway, will no longer be available, i.e. produce 404 error code
def deactivateAPI(apigwUrl, apigwAuth, id) {

	println("Deactivating API with id " + id);

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/apis/" + id + "/deactivate", 
				validResponseCodes: '200'

}

// fetch APIs with given name and maturity status
def queryAPIs(apigwUrl, apigwAuth, apiName, maturityState) {

	if (maturityState != "") {
		key = "maturityState"
		value = maturityState
	} else {
		key = "apiName"
		value = apiName
	}

	def body = """ {
	  "types": [
	    "api"
	  ],
	  "condition": "or",
	  "scope": [
	    {
	      "attributeName": "${key}",
	      "keyword": "${value}"
	    }
	  ],
	  "responseFields": [
	    "apiName",
	    "id",
	    "name",
	    "apiVersion"
	  ],
	  "from": 0,
	  "size": -1
	}"""

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/search", 
				validResponseCodes: '200'

	println("query got back: "+response.content);

	def content = readJSON file: '', text: "${response.content}"

	return content;
}

// Link given API with application i.e. Application Key
def linkApiWithApp(apigwUrl, apigwAuth, apiRef, appRef) {

	println("linking app with api "+apiRef);

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/applications/" + appRef + "/apis", 
				validResponseCodes: '200:404'

	println("got back:" + response.content)

	def reqContent = readJSON file: '', text: "${response.content}"

	if (reqContent.apiIDs)
		reqContent.apiIDs = reqContent.apiIDs << apiRef
	else
		reqContent.apiIDs = [apiRef];

	println("will resend "+reqContent.toString())

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: reqContent.toString(), 
				url: "${apigwUrl}/rest/apigateway/applications/" + appRef + "/apis", 
				validResponseCodes: '200'

	println("query got back: "+response.content);

	def content = readJSON file: '', text: "${response.content}"

	return content;
}

def deployNewAPIToAPIGateway(apigwUrl, apigwAuth, apiName, version, apiGroup, maturityState, apiDefinition) {

	def body = """{
		"apiName": "${apiName}",
  		"apiVersion": "${version}",
  		"apiGroups": ["${apiGroup}"],
		"maturityState": "${maturityState}",
  		"type": "swagger",
  		"apiDefinition": ${apiDefinition}
	}"""

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis", 
				validResponseCodes: '200:201'

	return response.content;
}

// maturityState "Beta"
def replaceAPIForAPIGateway(apigwUrl, apigwAuth, id, version, maturityState, apiDefinition) {

	def body = """{
		"apiVersion": "${version}",
		"maturityState": "${maturityState}",
  		"type": "swagger",
  		 "apiDefinition": ${apiDefinition}
	}"""

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}", 
				validResponseCodes: '200:201'

	return response.content;
}

// Duplicates existing API with new version reference
def createNewVersionForAPIForAPIGateway(apigwUrl, apigwAuth, id, newVersion) {

	def body = """{
  		"newApiVersion": "${newVersion}",
  		"retainApplications": "true"
	}"""

	print("body: "+body)

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}/versions", 
				validResponseCodes: '200:201'

	return response.content;
}

// Deploys APIs found in git source directory to API Gateway
def deployAPIsFromGitHubToAPIGateway(apigwUrl, apigwAuth, repoAccount, repo, repoUser, repoPassword, directory, appId, apiHost) {

	return deployAPIsToAPIGateway(apigwUrl, apigwAuth, "https://raw.githubusercontent.com/${repoAccount}/${repo}/master/apis/", repoUser, repoPassword, directory, appId, apiHost)
}

// Deploys each API definition found in sub-directory '{directory}/src/apis' to API Gateway
// assumes that the name of the API is given in the fiest part of the filename postfixed with '-' e.g. "HelloWorld-api-1.0.swagger" where "HelloWorld" is the name of the API
def deployAPIsToAPIGateway(apigwUrl, apigwAuth, swaggerEndPoint, swaggerAuth, directory, appId, apiHost) {

	def dir = new File(directory)
	def refs = [];

	println("Will upload API definitions found in :"+dir.getAbsolutePath())

	def files = new File(dir, "src/apis").list()

	files.each { file -> 
		
		id = deployAPIToAPIGateway(apigwUrl, apigwAuth, file.split('-')[0], "Devops", swaggerEndPoint + file, swaggerAuth, true, appId, apiHost)

		refs = refs << id
	}

	return refs;
}

// Deploys API at given end-point to API Gateway
def deployAPIToAPIGateway(apigwUrl, apigwAuth, apiName, apiGroup, swaggerEndPoint, swaggerAuth, replaceCurrentVersion, appId, apiHost) {

	def version = 1
	def newVersion = 1
	def apiRef = ""

	println("Processing API with name "+apiName)

	def results = queryAPIs(apigwUrl, apigwAuth, apiName, "")
	
	if (results != null && results.api != null) {

		def lv = -1
		results.api.each { api ->

			def v = api.apiVersion ? api.apiVersion.toInteger() : 1

			if (v > lv) {

				println("current version is " + v)

				if (v == null)
					version = 1
				else
					version = api.apiVersion.toInteger()

				apiRef = api.id
				newVersion = version + 1
				lv = version
			}
		}
	}

	def data = null

	if (newVersion == 1) {

		println("Importing as new API "+apiName+":1 (Beta) via "+swaggerEndPoint)

		api = getSwaggerDefinition(swaggerEndPoint, swaggerAuth);

		if (apiHost) {

			println("Changing api host from " + api.host + " to " + apiHost);
			api.host = apiHost;
		}

		data = deployNewAPIToAPIGateway(apigwUrl, apigwAuth, apiName, version, apiGroup, "Beta", groovy.json.JsonOutput.toJson(api))

		def content = readJSON file: '', text: "${data}"

		println("Will now link with app " + appId);

		if (appId)
			linkApiWithApp(apigwUrl, apigwAuth, content.apiResponse.api.id, getApplicationId(apigwUrl, apigwAuth, appId, true));

	} else if (!replaceCurrentVersion) {

		println("Importing as new version of existing API "+apiName+":"+newVersion+" (Beta) - "+apiRef + "via " + swaggerEndPoint)
		
		data = createNewVersionForAPIForAPIGateway(apigwUrl, apigwAuth, apiRef, newVersion)

		versionResponse = readJSON file: '', text: "${data}"
		apiRef = versionResponse.apiResponse.api.id
		api = getSwaggerDefinition(swaggerEndPoint, swaggerAuth);

		if (apiHost) 
			api.host = apiHost;
	
		data = replaceAPIForAPIGateway(apigwUrl, apigwAuth, apiRef, newVersion, "Beta", groovy.json.JsonOutput.toJson(api))
	
	} else {

		println("Updating existing version of API "+apiName+":"+version+" (Beta) - "+apiRef)

		api = getSwaggerDefinition(swaggerEndPoint, swaggerAuth);

		if (apiHost) 
			api.host = apiHost;
	
		data = replaceAPIForAPIGateway(apigwUrl, apigwAuth, apiRef, newVersion, "Beta", groovy.json.JsonOutput.toJson(api))
	}

	def content = readJSON file: '', text: "${data}"

	API_VERSION = newVersion;

	return content.apiResponse.api.id;
}

def postApigwService(apigwUrl, apigwAuth, apiName, repoUrl, repoUser, repoPassword, version) {

	def body = """{
		"apiName": "${apiName}",
  		"apiVersion": "${version}",
  		"apiGroups": ["Demo"],
		"maturityState": "Beta",
  		"type": "swagger",
  		"url": "${repoUrl}",
  		"authorizationValue": {
    		"keyName": "Authorization",
    		"value": "${authString(repoUser,repoPassword)}",
    		"type": "header"
  		}
	}"""

	print("body: "+body)

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis", 
				validResponseCodes: '200:201'

	return response.content;
}

// maturityState "Beta"
def putApigwService(apigwUrl, apigwAuth, id, repoUrl, repoUser, repoPassword, version, maturityState) {

	def body = """{
		"apiVersion": "${version}",
		"maturityState": "${maturityState}",
		"apiGroups": ["Demo"],
  		"type": "swagger",
  		"url": "${repoUrl}",
  		"authorizationValue": {
    		"keyName": "Authorization",
    		"value": "${authString(repoUser,repoPassword)}",
    		"type": "header"
  		}
	}"""

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}", 
				validResponseCodes: '200:201'

	return response.content;
}

def postNewApiVersiongwService(apigwUrl, apigwAuth, id, newVersion) {

	def body = """{
  		"newApiVersion": "${newVersion}",
  		"retainApplications": "true"
	}"""

	print("body: "+body)

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}/versions", 
				validResponseCodes: '200:201'

	return response.content;
}

def getSwaggerFromAPIGateway(swagger, apigwAuth) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}/versions", 
				validResponseCodes: '200:201'

	def json = readJSON file: '', text: "${response.content}"

	return json;
}

def deployAPIViaDevops(devopsServer, apigwAuth, apigwUrl, user, password, apiEntryPoint, apiName, swagger, version, appName, grouping) {

	def body = """{
		"appName": "${appName}",
  		"apiServer": {
  			"apiGatewayURL": "${apigwUrl}",
  			"apiUser": "${user}",
  			"apiPassword": "${password}",
  			"apiPolicyRoutingEndPoint": "${apiEntryPoint}"
  		},
  		"service": {
  			"name":  "${apiName}",
  			"swaggerEndPoint":  "${swagger}",
  			"version": "${version}",
  			"maturity": "new",
  			"grouping": "${grouping}"
  		}
	}"""

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: apigwAuth, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${devopsServer}/rest/jc.devops.console:gateway/deploy", 
				validResponseCodes: '200:201'

	def content = readJSON file: '', text: "${response.content}"

	return content.id;
}

def awaitTestResults(testServer, testServerAuth) {

	def url = "${testServer}/rad/jc.test.runner:api/status/10/10"

	println("Checking for test result status at "+url)

	try {
		response = httpRequest acceptType: 'APPLICATION_JSON', 
					authentication: testServerAuth, 
					contentType: 'APPLICATION_JSON', 
					httpMode: 'GET', 
					ignoreSslErrors: true, 
					url: url, 
					validResponseCodes: '200'

		def content = readJSON file: '', text: "${response.content}"
		
		return content.status == 'COMPLETED';
	} catch (exc) {

		println("Test failed due to " + exc);
		return false;
	}
}

pipeline {
	agent any
	environment {
		%loop env%%value src% = "%value tgt%"
		%endloop%
		
	}
	stages {
	%loop stages%
		stage('%value name%') {
			%ifvar when%
			when {
				expression { %value when encode(none)% }
			}
			%endifvar%
		
		steps {
			%loop steps%
				%value steps encode(none)%
			%endloop%

			}
		}

	%endloop%
	}
}