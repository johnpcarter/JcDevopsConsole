package jc.devops.console.jenkins_.services;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// --- <<IS-END-IMPORTS>> ---

public final class _priv

{
	// ---( internal utility methods )---

	final static _priv _instance = new _priv();

	static _priv _newInstance() { return new _priv(); }

	static _priv _cast(Object o) { return (_priv)o; }

	// ---( server methods )---




	public static final void addStep (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(addStep)>> ---
		// @sigtype java 3.5
		// [i] record:0:required step
		// [i] - field:0:required name
		// [i] - field:0:required when
		// [i] - field:0:required step
		// [i] record:1:required steps
		// [i] - field:0:required name
		// [i] - field:0:required when
		// [i] - field:1:required steps
		// [o] record:1:required steps
		// [o] - field:0:required name
		// [o] - field:0:required when
		// [o] - field:1:required steps
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		IData step = IDataUtil.getIData(pipelineCursor, "step");
		IData[]	steps = IDataUtil.getIDataArray(pipelineCursor, "steps" );
		
		// process
		
		IDataCursor stepCursor = step.getCursor();
		String name = IDataUtil.getString(stepCursor, "name");
		String when = IDataUtil.getString(stepCursor, "when");
		String stepDetail = IDataUtil.getString(stepCursor, "step");
		stepCursor.destroy();
		
		steps = addStepToSteps(name, when, stepDetail, steps);
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "steps", steps);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void collateAPIsIntoList (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(collateAPIsIntoList)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [i] field:0:required apiContainerHost
		// [i] field:0:required apiContainerPort
		// [i] recref:1:required services jc.devops.console.configuration_.docTypes:RunDeployment
		// [o] record:0:required api
		// [o] - field:0:required src
		// [o] - field:0:required tgt
		// [o] record:0:required swagger
		// [o] - field:0:required src
		// [o] - field:0:required tgt
		// pipeline in
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
		String apiContainerHost = IDataUtil.getString(pipelineCursor, "apiContainerHost");
		String apiContainerPort = IDataUtil.getString(pipelineCursor, "apiContainerPort");
		
		IData[]	services = IDataUtil.getIDataArray(pipelineCursor, "services");
			
		String apiOut = null;
		String swaggerOut = null;
		
		for ( int i = 0; i < services.length; i++ ) {
			
			IDataCursor  servicesCursor = services[i].getCursor();
			IData[]	apis = IDataUtil.getIDataArray(servicesCursor, "apis");
			
			if (apis != null) {
				for ( int z = 0; z < apis.length; z++ ) {
					
					IDataCursor apisCursor = apis[z].getCursor();
					String apiName = IDataUtil.getString(apisCursor, "name");
					String swagger = IDataUtil.getString(apisCursor, "swaggerEndPoint");
		
					apisCursor.destroy();
					
					if (apiOut == null)
						apiOut = apiName;
					else
						apiOut = apiOut + "," + apiName;
					
					if (apiContainerHost != null) {
						
						int start = swagger.indexOf("//");
						String rst = swagger.substring(start+2);
						int end = rst.indexOf("/");
						
						if (apiContainerPort != null)
							swagger = swagger.substring(0,start)  + "//" + apiContainerHost + ":" + apiContainerPort + swagger.substring(start+2+end);
						else
							swagger = apiContainerHost + swagger.substring(start+2+end);
					}
					if (swaggerOut == null)
						swaggerOut = swagger;
					else
						swaggerOut = swaggerOut + "," + swagger;
				}
			}
		}
		
		// pipeline out
		
		if (apiOut != null) {
			IDataUtil.put(pipelineCursor, "api", makeArg(name, "api", apiOut, null));
			IDataUtil.put(pipelineCursor, "swagger", makeArg(name, "api_swagger", swaggerOut, null));
		
		}
		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void setupEnvVarsForStage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(setupEnvVarsForStage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [i] field:0:required server
		// [i] field:0:required build
		// [i] field:0:optional apiGrouping
		// [i] field:0:optional apiApp
		// [i] recref:1:required deployments jc.devops.console.configuration_.docTypes:RunDeployment
		// [i] record:1:required env
		// [o] record:1:required env
		// [o] - field:0:required src
		// [o] - field:0:required tgt
		// [o] field:0:required stageId
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String stageName = IDataUtil.getString(pipelineCursor, "name");
		String server = IDataUtil.getString(pipelineCursor, "server");
		String build = IDataUtil.getString(pipelineCursor, "build");
		String apiApp = IDataUtil.getString(pipelineCursor, "apiApp");
		String apiGrouping = IDataUtil.getString(pipelineCursor, "apiGrouping");
		
		IData[]	deployments = IDataUtil.getIDataArray( pipelineCursor, "deployments");
		IData[]	env = IDataUtil.getIDataArray( pipelineCursor, "env");
			
		// process
		ArrayList<IData> out;
		
		if (env != null)
			out = convertToIDataList(env);
		else
			out = new ArrayList<IData>();
		
		String stageId = stageName.replaceAll(" +", "_");
		
		if (server == null || server.equals("localhost"))
			server = "host.docker.internal";
		
		if (server.indexOf(":") == -1)
			out.add(makeArg(stageId, "docker", server + ":1234", "host"));
		else
			out.add(makeArg(stageId, "docker", server, "host")); // ENABLE before building console image*/
		//	out.add(makeArg(stageId, "docker", "localhost:1234", "host"));
		
		out.add(makeArg(stageId, "docker", "null", "cert"));
		
		out.add(makeArg(stageId, "docker", "EDIT ME", "email"));
		out.add(makeArg(stageId, "docker", "EDIT ME", "user"));
		out.add(makeArg(stageId, "docker", "EDIT ME", "password"));
		
		if (build != null && build.equals("true"))
			out.add(makeArg("BUILD", "SRC", "true", stageId));
		
		if (apiApp != null)
			out.add(makeArg(stageId, "app", apiApp, "id"));
		else
			out.add(makeArg(stageId, "app", "null", "id"));
		
		if (apiGrouping != null)
			out.add(makeArg(stageId, "api", apiApp, "grouping"));
		else
			out.add(makeArg(stageId, "api", "default", "grouping"));
		
		out.add(makeArg(stageId, "swagger", "microservice-runtime", "AUTH"));
		
		out.add(makeArg(stageId, "msr", "wm-msr:5555", "endpoint"));
		
		out.add(makeArg(stageId, "PORTAL", "Default", "NAME"));
		out.add(makeArg(stageId, "PORTAL", "Public Community", "COMMUNITY"));
		
		String containerStringList = null;
		
		if (deployments != null) {
		
			for ( int i = 0; i < deployments.length; i++ ) {
				
				IDataCursor servicesCursor = deployments[i].getCursor();
				String serviceName = IDataUtil.getString(servicesCursor, "name");
				String hostname = IDataUtil.getString(servicesCursor, "hostname");
			
				IData[]	containers = IDataUtil.getIDataArray(servicesCursor, "containers");
				
				if (containers != null) {
									
					for ( int z = 0; z < containers.length; z++ ) {
				
						String name = makeRefForContainer(out, stageName, server, containers[z]);
						
						if (name != null) {
							if (containerStringList == null)
								containerStringList = name;
							else
								containerStringList += "," + name;
						}
					}				
				}
				
				servicesCursor.destroy();
			}
		}
		
		if (containerStringList != null)
			out.add(makeArg(stageId, "containers", containerStringList, null));
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "env", out.toArray(new  IData[out.size()]));
		IDataUtil.put(pipelineCursor, "stageId", stageId);
		pipelineCursor.destroy();
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void setupInitialEnvVars (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(setupInitialEnvVars)>> ---
		// @sigtype java 3.5
		// [i] record:1:required env
		// [i] field:0:required targetImage
		// [i] field:0:required version
		// [o] record:1:required env
		// [o] - field:0:required src
		// [o] - field:0:required tgt
		// [o] field:0:required stageId
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		IData[]	env = IDataUtil.getIDataArray(pipelineCursor, "env");
		String version = IDataUtil.getString(pipelineCursor, "version");
		String targetImage = IDataUtil.getString(pipelineCursor, "targetImage");
		
		// process
		
		ArrayList<IData> out;
		
		if (env != null)
			out = convertToIDataList(env);
		else
			out = new ArrayList<IData>();
		
		out.add(makeArg("devops", "container", "http://host.docker.internal:5555", null));		
		
		out.add(makeArg("API", "IMAGE", targetImage, null));
		out.add(makeArg("API", "VERSION", version, null));
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "env", out.toArray(new  IData[out.size()]));
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static String findAPIEntryPoint(IData[] services) {
		
		String endPoint = null;
		
		for (int i = 0; i < services.length; i++) {
					
			IDataCursor servicesCursor = services[i].getCursor();
			IData[]	containers = IDataUtil.getIDataArray(servicesCursor, "containers");
			
			if ( containers != null) {
		
				for ( int z = 0; z < containers.length; z++ ) {
				
					IDataCursor containersCursor = containers[z].getCursor();
					String name = IDataUtil.getString(containersCursor, "name");
					String type = IDataUtil.getString(containersCursor, "type");
					String active = IDataUtil.getString(containersCursor, "active");
					
					IData ports = IDataUtil.getIDataArray(containersCursor, "ports")[0];
					IDataCursor portsCursor = ports.getCursor();
					String	external = IDataUtil.getString( portsCursor, "external" );
					String	publicPort = IDataUtil.getString( portsCursor, "publicPort" );
					String	portName = IDataUtil.getString( portsCursor, "name" );
					portsCursor.destroy();
					
					if (type.equals("MicroGateway"))
						endPoint = name + ":" + publicPort;
					else if (name.contains("API Gateway") && endPoint != null)
						endPoint = name + ":" + publicPort;
					
					containersCursor.destroy();
				}
			}
		
			servicesCursor.destroy();
		}
		
		return endPoint;
	}
	
	private static String makeRefForContainer(ArrayList<IData> out, String stageName, String server, IData container) {
		
		IDataCursor containerCursor = container.getCursor();
		String containerName = IDataUtil.getString(containerCursor, "name");
		String hostName = IDataUtil.getString(containerCursor, "hostname");
		String type = IDataUtil.getString(containerCursor, "type");
		String isActive = IDataUtil.getString(containerCursor, "active");
		
		containerName = containerName.replaceAll("-", "_");
				
		IDataUtil.put(containerCursor, "name", containerName);
		
		containerCursor.destroy();
		
		System.out.println("prep container " + containerName + " is active " + isActive + " or " + hostName);
		
		if ((isActive != null && isActive.equalsIgnoreCase("true")) || hostName != null) {
		
				if (isActive != null && isActive.equalsIgnoreCase("true")) {
					out.add(makeArg(stageName, containerName, "http://"  + server + ":" + getPublicPortFromContainer(container), null));
				}   else {
					out.add(makeArg(stageName, containerName, "http://" + hostName + ":" + getPublicPortFromContainer(container), null));
				}
				
				out.add(makeArg(stageName, containerName, type.replace(" ", "-").toLowerCase(), "AUTH"));
		}
		
		if (isActive != null && isActive.equalsIgnoreCase("true")) 
			return containerName;
		else
			return null;
	}
	
	private static IData makeArg(String prefix, String id, String value, String postfix) {
	
		IData arg = IDataFactory.create();
		IDataCursor c = arg.getCursor();
		
		if (postfix != null)
			IDataUtil.put(c, "src", prefix.replaceAll("=","_") + "_" + id.replaceAll("=","_") + "_" + postfix.replaceAll("=","_"));
		else
			IDataUtil.put(c, "src", prefix.replaceAll("=","_") + "_" + id.replaceAll("=","_"));
		
		IDataUtil.put(c, "tgt", value);
		
		c.destroy();
		
		return arg;
	}
	
	private static String getPublicPortFromContainer(IData container) {
	
		IDataCursor c = container.getCursor();
		IData[] ports = IDataUtil.getIDataArray(c, "ports");
		c.destroy();
		
		c = ports[0].getCursor();
		String ext = IDataUtil.getString(c, "external");
		String publicPort = IDataUtil.getString(c, "publicPort");
		
		c.destroy();
		
		return ext;
	}
	
	private static IData[] addStepToSteps(String name, String when, String stepDetail, IData[] steps) {
	
		Map<String, IData> stepsz = convertToMap(steps);
		
		IData step = null;
		
		if ((step=stepsz.get(name)) != null) {
			addStepToStep(step, stepDetail);
		} else {
			steps = addStepToList(name, when, stepDetail, steps);
		}
		
		return steps;
	}
	
	private static Map<String, IData> convertToMap(IData[] steps) {
	
		Map<String, IData> out = new HashMap<String, IData>();
		
		if (steps != null) {
			for ( int i = 0; i < steps.length; i++ )
			{
				IDataCursor stepsCursor = steps[i].getCursor();
				String	name = IDataUtil.getString( stepsCursor, "name");
				stepsCursor.destroy();
			
				out.put(name, steps[i]);
			}
		}
		
		return out;
	}
	
	private static IData[] addStepToList(String name, String when, String detail, IData[] steps) {
		
		if (steps != null) {
			IData out[] = new IData[steps.length+1];
		
			for  (int i = 0; i < steps.length; i++) {
				out[i] = steps[i];
			}
		
			out[steps.length] = makeStep(name, when, detail);
			
			return out;
	
		} else {
			
			IData out[] = new IData[1];
			out[0] = makeStep(name, when, detail);
			
			return out;
		}
		
	}
	
	private static IData makeStep(String name, String when, String detail) {
		
		IData step = IDataFactory.create();
		IDataCursor c = step.getCursor();
		IDataUtil.put(c, "name", name);
		
		if (when != null)
			IDataUtil.put(c,  "when", when);
		
		String[] details = new String[1];
		details[0] = detail;
		
		IDataUtil.put(c, "steps", details);
		c.destroy();
		
		return step;
	}
	
	private static void addStepToStep(IData step, String detail)  {
		
		IDataCursor stepsCursor = step.getCursor();
		ArrayList<String> steps = convertToStringList(IDataUtil.getStringArray(stepsCursor, "steps"));
		
		
		if (steps.size() > 2 &&  steps.get(0).startsWith("script {")) {
			steps.add(steps.size()-1, detail);
			
			System.out.println("detailzz will be " + detail);
		} else {
			
			if (steps.get(0).startsWith("script {")) {
				
				// remove script wrapper from string
				
				String script = steps.get(0);
				int strt = script.indexOf("{");
				int end = script.lastIndexOf("}");
				
				steps.clear();
				
				System.out.println(" step was " + script);
				System.out.println(" step will be " + script.substring(strt+1, end-1));
				
				steps.add(script.substring(strt+1, end-1));
			}
			
			System.out.println("detail will be " + detail);
			
			steps.add(0, "script {");
			steps.add(detail);
			steps.add("}");
		}
		
		IDataUtil.put(stepsCursor, "steps", steps.toArray(new String[steps.size()]));
		stepsCursor.destroy();
	}
	
	private static ArrayList<IData> convertToIDataList(IData[] in) {
		
		ArrayList<IData> out = new  ArrayList<IData>();
		
		for (int i = 0; i < in.length; i++) {
			out.add(replaceSlash(in[i]));
		}
		
		return out;
	}
	
	private static IData replaceSlash(IData in) {
	
		IDataCursor c = in.getCursor();
		IDataUtil.put(c, "src", IDataUtil.getString(c, "src").replaceAll("-(?=[^\\[\\]]*\\])", "_"));
		c.destroy();
		
		return in;
	}
	
	private static ArrayList<String> convertToStringList(String[] in) {
		
		ArrayList<String> out = new  ArrayList<String>();
		
		for (int i = 0; i < in.length; i++) {
			out.add(in[i]);
		}
		
		return out;
	}
	
	private static IData makeEnv(String name, String value) {
		
		IData env = IDataFactory.create();
		
		IDataCursor c = env.getCursor();
		IDataUtil.put(c, name.replaceAll("-", "_"), value);
		c.destroy();
		
		return env;
	}
	// --- <<IS-END-SHARED>> ---
}

