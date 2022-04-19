package jc.devops.console.docker_._priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.jc.devops.docker.ImageRegistry;
import com.wm.app.b2b.server.ServerAPI;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
// --- <<IS-END-IMPORTS>> ---

public final class tools

{
	// ---( internal utility methods )---

	final static tools _instance = new tools();

	static tools _newInstance() { return new tools(); }

	static tools _cast(Object o) { return (tools)o; }

	// ---( server methods )---




	public static final void convertArgsTableToArgsDocAndEnv (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(convertArgsTableToArgsDocAndEnv)>> ---
		// @sigtype java 3.5
		// [i] field:2:required values
		// [i] record:0:optional args
		// [i] record:1:optional env
		// [i] - field:0:required key
		// [i] - field:0:required value
		// [o] record:0:required args
		// [o] record:1:required env
		// [o] - field:0:required key
		// [o] - field:0:required value
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String[][] values = IDataUtil.getStringTable(pipelineCursor, "values");
		IData args = IDataUtil.getIData(pipelineCursor, "args");
		IData[] envIn = IDataUtil.getIDataArray(pipelineCursor, "env");
		
		// process
		
		if (args == null)
			args = IDataFactory.create();
		
		List<IData> env = new ArrayList<IData>();
		
		if (envIn!= null) {
			for (IData i : envIn) {
				env.add(i);
			}
		}
		
		if (values != null) {
			
			IDataCursor c = args.getCursor();
			
			for (int i = 0; i < values.length; i++) {
			
				if (values[i][0].startsWith("env_")) {
					
					values[i][0] = values[i][0].substring(values[i][0].indexOf("_") + 1);
					
					IData e = IDataFactory.create();
					IDataCursor ec = e.getCursor();
					IDataUtil.put(ec, "key", values[i][0]);
					IDataUtil.put(ec, "value", values[i][1]);
					ec.destroy();
					
					env.add(e);
				}
				
				// include in args, this is used for var substitution in installer/download scripts
				
				//try {
					//IDataUtil.put(c, values[i][0], URLEncoder.encode(values[i][1], StandardCharsets.UTF_8.toString()));
					IDataUtil.put(c, values[i][0], values[i][1]);
		
					IDataUtil.put(c, "_" + values[i][0], values[i][1]);
		
				//} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
			}
			
			c.destroy();
		}
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "args", args);
		IDataUtil.put(pipelineCursor, "env", env.toArray(new IData[env.size()]));
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void deleteAllOtherFilesFromSource (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(deleteAllOtherFilesFromSource)>> ---
		// @sigtype java 3.5
		// [i] field:0:required sourceDir
		// [i] record:1:required filesToKeep
		// [i] - field:0:required source
		// [i] - field:0:optional fileType
		// [i] field:0:required pathToPackages
		// [i] field:0:required pathToConfig
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String sourceDir = IDataUtil.getString(pipelineCursor, "sourceDir");
		String pathToPackages = IDataUtil.getString(pipelineCursor, "pathToPackages");
		String pathToConfig = IDataUtil.getString(pipelineCursor, "pathToConfig");
		IData[]	filesToKeep = IDataUtil.getIDataArray(pipelineCursor, "filesToKeep");
		
		// process
		
		List<String> files = new ArrayList<String>();
			
		if (filesToKeep != null)
		{			
			for ( int i = 0; i < filesToKeep.length; i++ )
			{
				IDataCursor filesToKeepCursor = filesToKeep[i].getCursor();
				String n = IDataUtil.getString(filesToKeepCursor, "source");
				String t = IDataUtil.getString(filesToKeepCursor, "fileType");
				
				if (n != null) {
					
					if (n.startsWith("./"))
						n = n.substring(2);
					
					String fileToKeep;
					
					if (t != null && t.equalsIgnoreCase("config")) {
						fileToKeep = new File(sourceDir, new File(pathToConfig, new File(n).getName()).getPath()).getPath();
		
					} else {
						fileToKeep = new File(sourceDir, new File(pathToPackages, new File(n).getName()).getPath()).getPath();
					}
										
					files.add(fileToKeep);
				}
				
				filesToKeepCursor.destroy();
			}
		}
			
		deleteFilesInDir(new File(sourceDir), files, true);
		
		// pipeline out
		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void excludePatchFromVersion (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(excludePatchFromVersion)>> ---
		// @sigtype java 3.5
		// [i] field:0:required version
		// [o] field:0:required version
		// pipeline in
		IDataCursor c = pipeline.getCursor();
		String v = IDataUtil.getString(c, "version");
		
		// process
		
		String lv = v;
		
		if (v != null) {
			if (v.contains(".")) {
				int dot = v.indexOf(".");
				String major = v.substring(0, dot);
				String minor = v.substring(dot+1);
				
				if (minor.contains(".")) {
					// ignore patches
					
					minor = minor.substring(0, minor.indexOf("."));
				}
				
				lv = major + "." + minor;
			} else {
				lv = v;
			}
		}
				
		// pipeline out
		
		IDataUtil.put(c, "version", lv);
		c.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void getNextVersionForImage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getNextVersionForImage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required tag
		// [i] object:0:optional isDedicatedRepo
		// [i] field:0:required buildType
		// [i] recref:1:required images jc.devops.console.docker_.docTypes:DockerImage
		// [o] field:0:required version
		// [o] field:0:required tag
		// [o] field:0:required tagLatest
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String buildType = IDataUtil.getString(pipelineCursor, "buildType");
		String tag = IDataUtil.getString(pipelineCursor, "tag");
		boolean isDedicatedRepo = IDataUtil.getBoolean(pipelineCursor, "isDedicatedRepo");
		
		try {
					
			IData[]	images = IDataUtil.getIDataArray(pipelineCursor, "images");
					
			String latest = null;
				
			for ( int i = 0; i < images.length; i++ )
			{
				IDataCursor imagesCursor = images[i].getCursor();
				String id = IDataUtil.getString(imagesCursor, "id");
				String _version = IDataUtil.getString(imagesCursor, "_version");
				imagesCursor.destroy();
					
				if (_version != "latest" && (i == 0 || latest == null)) {
					latest = _version;
				}
			}
								
			if (latest != null) {
				latest = incrementVersion(latest, buildType);
			} else {
				latest = "0.0.1";
			}
				
			String tagWithoutVersion = "";
			int index = -1;
			
			if (isDedicatedRepo) {
				index = tag.lastIndexOf(":");
			} else {
				index = tag.lastIndexOf("-");
			}
			
			tagWithoutVersion = tag.substring(0, index);
			
				
			// pipeline out
				
			if (isDedicatedRepo) {
				IDataUtil.put(pipelineCursor, "tag", tagWithoutVersion + ":" + latest);
				IDataUtil.put(pipelineCursor, "tagLatest", tagWithoutVersion + ":latest");
			} else {
				IDataUtil.put(pipelineCursor, "tag", tagWithoutVersion + "-" + latest);
				IDataUtil.put(pipelineCursor, "tagLatest", tagWithoutVersion + "-latest");
			}
		
				
			IDataUtil.put(pipelineCursor, "version", latest);
			} catch(Exception e) {
				e.printStackTrace();
				throw new ServiceException(e);
			}
		
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void getNextVersionFromImage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getNextVersionFromImage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required tag
		// [i] object:0:optional isDedicatedRepo
		// [o] field:0:required version
		// [o] field:0:required tagLatest
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String tag = IDataUtil.getString(pipelineCursor, "tag");
		Object isDedicatedRepo = IDataUtil.get(pipelineCursor, "isDedicatedRepo");
		
		// process
		
		String version = "1.0";
		String tagWithoutVersion = "";
		int index = -1;
		boolean isDedicated = false;
		
		if (isDedicatedRepo == null) {
			if (tag.indexOf(":") != -1) {
				isDedicated = true;
			}
		} else if ((Boolean) isDedicatedRepo) {
			isDedicated = true;
		}
		
		if (isDedicated) {
			index = tag.lastIndexOf(":");
		} else {
			index = tag.lastIndexOf("-");
		}
		
		tagWithoutVersion = tag.substring(0, index);
		version = tag.substring(index+1);
		
		// pipeline out
		
		if (isDedicated) {
			IDataUtil.put(pipelineCursor, "tagLatest", tagWithoutVersion + ":latest");
		} else {
			IDataUtil.put(pipelineCursor, "tagLatest", tagWithoutVersion + "-latest");
		}
		
		IDataUtil.put(pipelineCursor, "version", version);
		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void makeFileName (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(makeFileName)>> ---
		// @sigtype java 3.5
		// [i] field:0:required label
		// [i] field:0:required type
		// [o] field:0:required filename
		IDataCursor pipelineCursor = pipeline.getCursor();
		String label = IDataUtil.getString(pipelineCursor, "label");
		String type = IDataUtil.getString(pipelineCursor, "type");
				
		// process
		
		String filename = null;
		
		if (type.equalsIgnoreCase("properties")) {
			filename = label.replace(" ", "-") + ".properties";
		} else if (type.equalsIgnoreCase("licence")) {
			filename = label.replace(" ", "-") + ".xml";
		} else { 
			filename = label.replace(" ", "-") + ".txt"; 
		}
		
		// pipeline out
		IDataUtil.put(pipelineCursor, "filename", filename);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void removeEmptyLists (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(removeEmptyLists)>> ---
		// @sigtype java 3.5
		// [i] record:0:required doc
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		IData doc = IDataUtil.getIData(pipelineCursor, "doc");
		
		if ( doc != null)
		{
			IDataCursor c = doc.getCursor();
			c.first();
			
			do {
				String key = c.getKey();
				Object value = c.getValue();
				
				if (value instanceof Object[] && ((Object[]) value).length == 0) {
					c.delete();
				}
				
				c.next();
			} while (c.hasMoreData());
			
			c.destroy();
		}
		
		pipelineCursor.destroy();
		
		// pipeline
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void removeSlashes (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(removeSlashes)>> ---
		// @sigtype java 3.5
		// [i] field:0:required inString
		// [o] field:0:required outString
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String inString = IDataUtil.getString(pipelineCursor, "inString");
		
		// process
		
		String outString = inString;
		
		if (inString != null && inString.startsWith("/")) {
			outString = inString.substring(1);
		}
		
		if (outString != null && outString.endsWith("/")) {
			outString = outString.substring(0, outString.length()-2);
		}
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "outString", outString);
		pipelineCursor.destroy();		
			
		// --- <<IS-END>> ---

                
	}



	public static final void runInBackground (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(runInBackground)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional name
		// [i] field:0:optional version
		// [i] field:0:optional stageName
		// [i] field:0:optional dockerHost
		// [i] field:0:optional containers
		// [i] field:0:optional httpsCert
		// [i] recref:0:optional run jc.devops.console.configuration_.docTypes:Run
		// [i] record:0:optional apiServer
		// [i] - field:0:required host
		// [i] - field:0:optional user
		// [i] - field:0:optional password
		// [i] - field:0:optional maturity
		// [i] - field:0:optional version
		// [i] - field:0:optional grouping
		// [i] field:0:optional sync {"false","true"}
		try{
			IDataCursor c = pipeline.getCursor();
			String sync = IDataUtil.getString(c, "sync");
			c.destroy();
			
			IData copy = IDataUtil.clone(pipeline);
			
			if (sync !=  null && sync.equalsIgnoreCase("true"))
				Service.doInvoke("jc.devops.console.docker_._priv", "run", copy);
			else
				Service.doThreadInvoke("jc.devops.console.docker_._priv", "run", copy);
			
		}catch( Exception e){
			throw new ServiceException(e);
		}
		// --- <<IS-END>> ---

                
	}



	public static final void separateFilenameAndPath (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(separateFilenameAndPath)>> ---
		// @sigtype java 3.5
		// [i] field:0:required baseDir
		// [i] field:0:required filename
		// [o] field:0:required path
		// [o] field:0:required name
		// pipeline in 
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String baseDir = IDataUtil.getString(pipelineCursor, "baseDir");
		String filename = IDataUtil.getString(pipelineCursor, "filename");
		
		// processs
		
		String separator = System.getProperty("file.separator");
		String srcDir = baseDir;
		
		if (srcDir == null) {
			srcDir = "./";
		}
		
		if (filename != null && filename.contains(separator)) {
			File f = new File(filename);
			srcDir = new File(srcDir, f.getParent()).getPath();
			filename = f.getName();
		}
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "path", srcDir);
		IDataUtil.put(pipelineCursor, "name", filename);
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void updateVersionInAPIList (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(updateVersionInAPIList)>> ---
		// @sigtype java 3.5
		// [i] field:0:required apiName
		// [i] field:0:required newVersion
		// [i] field:0:required apiList
		// [o] field:0:required apiList
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String apiName = IDataUtil.getString(pipelineCursor, "apiName");
		String newVersion = IDataUtil.getString(pipelineCursor, "newVersion");
		String apiList = IDataUtil.getString(pipelineCursor, "apiList");
		
		// process
		
		if (apiList == null || apiList.equals(""))
			return;
		
		StringTokenizer tk = new StringTokenizer(apiList, ",");
		String out = null;
		String apiNameTrunc = null;
		
		if (apiName != null && apiName.contains("-")) {
			apiNameTrunc = apiName.substring(0, apiName.lastIndexOf("-")).toLowerCase();
		}
		
		while (tk.hasMoreTokens()) {
			String e = tk.nextToken();
			
			if (e.contains("/")) {
				e = e.substring(0, e.indexOf("/"));
			}
						
			if (e.toLowerCase().equals(apiName.toLowerCase()) || (apiNameTrunc != null && e.toLowerCase().equals(apiNameTrunc))) {	
				e += "/" + newVersion;				
			}
			
			if (out == null) {
				out = e;
			} else {
				out += "," + e;
			}
		}
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "apiList", out);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static String incrementVersion(String version, String buildType) {
		
		String nextVersion = null;
		double flatV = -1;
		
		try {
			
			flatV = ((double) Math.round(Double.parseDouble(version)*100)) / 100;
		} catch(Exception e) {
			flatV = -1;
		};
		
		if (flatV != -1) {
			// simple increment
	
			if (buildType != null && buildType.equals("major"))
				nextVersion = String.format("%.1f", flatV + 1);
			else
				nextVersion = String.format("%.1f", flatV + .1);
	
		} else if (version.matches("^\\d{1,3}\\.\\d{1,3}(?:\\.\\d{1,6})?$")) {
	
			// have major and minor indicator
	
			int major, minor, patch;
		    String[] versionArray = version.split(".");
	
		    major = Integer.parseInt(versionArray[0]);
		    minor =Integer. parseInt(versionArray[1]);
		    patch = Integer.parseInt(versionArray[2]);
	
		    if (buildType != null && buildType.equals("major")) {
		        major = major + 1;
		    }
		    else if (buildType != null && buildType.equals("minor")) {
		        minor = minor + 1;
		    }
		    else {
		        patch = patch + 1;
		    }
	
		    nextVersion = "" + (major + "." + minor + "." + patch);
		} else {
			nextVersion = "0.0.0";
		}
		
		return nextVersion;
	}
	
	private static void deleteFilesInDir(File element, List<String> exclude, boolean top) throws ServiceException {
	    		
		if (exclude.contains(element.getPath()))
			return;
		
		if (element.isDirectory()) {
	    	if (element.listFiles() != null) {
	    		for (File sub : element.listFiles()) {
	    			deleteFilesInDir(sub, exclude, false);
	    		}
	    	}
	    }
	    				
		if (!top && !exclude.contains(element.getPath())) {			
			element.delete();
		}
	}
	// --- <<IS-END-SHARED>> ---
}

