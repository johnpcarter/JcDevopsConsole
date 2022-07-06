package jc.devops.console.k8s_;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.jc.devops.docker.type.Build;
import com.jc.devops.docker.type.Deployment;
import java.util.List;
// --- <<IS-END-IMPORTS>> ---

public final class _priv

{
	// ---( internal utility methods )---

	final static _priv _instance = new _priv();

	static _priv _newInstance() { return new _priv(); }

	static _priv _cast(Object o) { return (_priv)o; }

	// ---( server methods )---




	public static final void formatNameForKS8Compatibility (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(formatNameForKS8Compatibility)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional prefix
		// [i] field:0:required value
		// [i] field:0:optional maxLen
		// [i] field:0:optional replaceDots {"false","true"}
		// [i] object:0:optional addUniqueIndex
		// [o] field:0:required value
		// pipeline in
		IDataCursor pipelineCursor = pipeline.getCursor();
		String prefix = IDataUtil.getString(pipelineCursor, "prefix");
		String value = IDataUtil.getString(pipelineCursor, "value");
		String maxLen = IDataUtil.getString(pipelineCursor, "maxLen");
		String replaceDots = IDataUtil.getString(pipelineCursor, "replaceDots");
		boolean addUniqueIndex = IDataUtil.getBoolean(pipelineCursor, "addUniqueIndex");
		
		int max = 0;
		
		try { max = Integer.parseInt(maxLen); } catch(Exception e) {} // do now't
		
		if (value == null || value.length() == 0 || addUniqueIndex) {
			if (value == null)
				value = "" + index++;
			else
				value += index++;
			
			if (index > 99) 
				index = 0;
		}
		
		if (value != null) {
		
			if (prefix != null && prefix.length() > 0)
				value = prefix + value;
		
			value = value.toLowerCase().replace(" ", "-");
			value = value.replace("_", "-");
			
			if (max > 0 && value.length() > max)
				value = value.substring(0, max);
			
			if (replaceDots != null && replaceDots.equalsIgnoreCase("true")) {
				value = value.replace(".", "-");
			}
		}
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "value", value);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void replaceVersionNumberInImage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(replaceVersionNumberInImage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required imageTag
		// [i] field:0:required newVersion
		// [o] field:0:required imageTag
		// --- <<IS-END>> ---

                
	}



	public static final void stringAtIteration (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(stringAtIteration)>> ---
		// @sigtype java 3.5
		// [i] field:1:required strings
		// [i] field:0:required $iteration
		// [o] field:0:required string
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String[] strings = IDataUtil.getStringArray(pipelineCursor, "strings");
		String iteration = IDataUtil.getString(pipelineCursor, "$iteration");
		
		// process 
		
		String str = null;
		int i = Integer.parseInt(iteration);
		
		if (i > 0 && i <= strings.length) {
			str = strings[i-1];
		} else {
			throw new ServiceException("Iteration out of bounds (" + i + "/" + strings.length + ")");
		}
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "string", str);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void updateContainerReferences (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(updateContainerReferences)>> ---
		// @sigtype java 3.5
		// [i] field:0:required buildDir
		// [i] field:0:required appPrefix
		// [i] field:0:required environment
		// [i] recref:0:required run jc.devops.console.configuration_.docTypes:Run
		// [i] field:0:required dirForResourceFiles
		// [i] field:0:required defaultNamespace
		// [o] recref:0:required run jc.devops.console.configuration_.docTypes:Run
		// [o] object:0:required requiresBuild
		// pipeline in
		
		IDataCursor c = pipeline.getCursor();
		String buildDir = IDataUtil.getString(c, "buildDir");
		String appPrefix = IDataUtil.getString(c, "appPrefix");
		String environment = IDataUtil.getString(c, "environment");
		String dirForResourceFiles = IDataUtil.getString(c, "dirForResourceFiles");
		String defaultNamespace = IDataUtil.getString(c, "defaultNamespace");
		
		IData run = IDataUtil.getIData(c, "run");
		
		// process
				
		IDataCursor rc = run.getCursor();
		IData[] buildsIData = IDataUtil.getIDataArray(rc, "builds");
		IData[] deploymentsIData = IDataUtil.getIDataArray(rc, "deployments");
		
		
		boolean requiresBuild = false;
		
		try {
			IData[] updatedDeploymentsIData = new IData[deploymentsIData.length];
			Deployment[] deployments = deployments(deploymentsIData, dirForResourceFiles);
			Build[] builds =  builds(buildsIData);
			
			try {
				requiresBuild = Deployment.updateContainerReferences(deployments, builds, appPrefix, buildDir, environment, defaultNamespace);
			} catch(Exception e) {
				e.printStackTrace();
				throw new ServiceException(e);
			}
			
			int i = 0;
			for (Deployment d : deployments) {
				
				updatedDeploymentsIData[i++] = d.toIData();
			}
		
			IDataUtil.put(rc, "deployments", updatedDeploymentsIData);
			rc.destroy();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new ServiceException(e);
		}
		
		// pipeline out
		
		IDataUtil.put(c, "requiresBuild", requiresBuild);
		IDataUtil.put(c, "run", run);
		c.destroy();
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static int index = 0;
	
	public static Build[] builds(IData[] builds) {
		
		if (builds == null)
			return null;
		
		Build[] out = new Build[builds.length];
		
		for (int i = 0; i < builds.length; i++) {
			out[i] = new Build(builds[i]);
		}
		
		return out;
	}
		
	public static Deployment[] deployments(IData[] deployments, String dirForResourceFiles) {
		
		Deployment[] out = new Deployment[deployments.length];
		
		for (int i = 0; i < deployments.length; i++) {
			out[i] = new Deployment(deployments[i], dirForResourceFiles);
		}
		
		return out;
	}
	// --- <<IS-END-SHARED>> ---
}

