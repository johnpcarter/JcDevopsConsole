package com.jc.devops.apigateway


// API Gateway functions

def promoteAPI(apigwUrl, stage, apis) {

	apiString = null;

	apis.each{ a -> 

		if (apiString == null) {
			apiString = "[\"${a}\""
		} else {		
			apiString = apiString + ",\"${a}\""
		}

		setMaturity(apigwUrl, a, "UAT")
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
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/promotion", 
				validResponseCodes: '200:201'
}

def publishAPI(apigwUrl, stage, id, portalId, communityId) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/stages/" + stage, 
				validResponseCodes: '200'


	jsn = readJSON file: '', text: "${response.content}"

	//def url = jsn.stages[0].url;
	//def name = jsn.stages[0].name;

	def url = jsn.stage.url;
	def name = jsn.stage.name;

	def body = """ {
		"portalGatewayId": "${portalId}",
		"communities": ["${communityId}"],
		"endpoints": ["${apigwUrl}/rad/${id}"]
	}"""

	println("Publishing API's via "+url+" for " + name)

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: name, 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${url}/rest/apigateway/apis/" + id + "/publish", 
				validResponseCodes: '200'
}

def unpublishAPI(apigwUrl, apiName, id, portalId, communityId) {

	def body = """ {
		"portalGatewayId": "${portalId}",
		"communities": ["${communityId}"]
	}"""

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/" + id + "/unpublish", 
				validResponseCodes: '200'

}

def setMaturity(apigwUrl, id, maturity) {

	apiWrapper = getAPI(apigwUrl, id, false);

    //pain, have to deactivate before update, really need option to only update attribute, NOT apiDefinition
    deactivateAPI(apigwUrl, id);

	// bug in response, means the type is a string list, whereas it should be a string!!
	apiWrapper.apiResponse.api.apiDefinition.type = apiWrapper.apiResponse.api.apiDefinition.type[0];

	raw = apiWrapper.apiResponse.api.apiDefinition.toString();

	def body = """{
		"maturityState": "${maturity}",
		"apiVersion": "${apiWrapper.apiResponse.api.apiVersion}",
  		"apiDefinition": ${raw}
	}"""

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}", 
				validResponseCodes: '200:402'

	activateAPI(apigwUrl, id);

	return response.content;
}

def getAPI(apigwUrl, id, raw) {

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				requestBody: "", 
				url: "${apigwUrl}/rest/apigateway/apis/" + id, 
				validResponseCodes: '200'

	if (raw) {
		return response.content;
	} else {
		def jsn = readJSON file: '', text: "${response.content}"

		return jsn;
	}
}

def activateAPI(apigwUrl, id) {

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/apis/" + id + "/activate", 
				validResponseCodes: '200'

}

def deactivateAPI(apigwUrl, id) {

	httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/apis/" + id + "/deactivate", 
				validResponseCodes: '200'

}

def queryAPIs(apigwUrl, apiName, maturityState) {

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
				authentication: 'wm-apigateway', 
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

def linkApiWithApp(apigwUrl, apiRef, appRef) {

	println("linking app with api "+apiRef);

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'GET', 
				ignoreSslErrors: true, 
				url: "${apigwUrl}/rest/apigateway/applications/" + appRef + "/apis", 
				validResponseCodes: '200:400'

	def reqContent = readJSON file: '', text: "${response.content}"

	reqContent.apiIDs = reqContent.apiIDs << apiRef;

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: reqContent.toString(), 
				url: "${apigwUrl}/rest/apigateway/applications/" + appRef + "/apis", 
				validResponseCodes: '200'

	def content = readJSON file: '', text: "${response.content}"

	return content;
}

def testAPI(apigwServer, testServer, apiRef) {

	apiWrapper = getAPI(apigwServer, apiRef, false)

	def api = apiWrapper.apiResponse.api.apiName.toLowerCase()
	def gatewayEndpoint = java.net.URLEncoder.encode(apiWrapper.apiResponse.gatewayEndPoints[0], "UTF-8")

	def url = "${testServer}/rest/jc/api/${api}_/test/${gatewayEndpoint}"

	println("Using test stub at "+url)

	httpRequest acceptType: 'APPLICATION_JSON', 
					authentication: 'wm-apigateway', 
					contentType: 'APPLICATION_JSON', 
					httpMode: 'GET', 
					customHeaders: [[maskValue: false, name: 'api-key', value: API_TEST_APP]],
					ignoreSslErrors: true, 
					url: url, 
					validResponseCodes: '200'
}

def postApigwService(apigwUrl, apiName, repoUrl, repoUser, repoPassword, version) {

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
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis", 
				validResponseCodes: '200:201'

	return response.content;
}

// maturityState "Beta"
def putApigwService(apigwUrl, id, repoUrl, repoUser, repoPassword, version, maturityState) {

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
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'PUT', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}", 
				validResponseCodes: '200:201'

	return response.content;
}

def postNewApiVersiongwService(apigwUrl, id, newVersion) {

	def body = """{
  		"newApiVersion": "${newVersion}",
  		"retainApplications": "true"
	}"""

	print("body: "+body)

	response = httpRequest acceptType: 'APPLICATION_JSON', 
				authentication: 'wm-apigateway', 
				contentType: 'APPLICATION_JSON', 
				httpMode: 'POST', 
				ignoreSslErrors: true, 
				requestBody: body, 
				url: "${apigwUrl}/rest/apigateway/apis/${id}/versions", 
				validResponseCodes: '200:201'

	return response.content;
}

