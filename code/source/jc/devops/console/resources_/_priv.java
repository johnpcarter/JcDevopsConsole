package jc.devops.console.resources_;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.ibm.icu.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// --- <<IS-END-IMPORTS>> ---

public final class _priv

{
	// ---( internal utility methods )---

	final static _priv _instance = new _priv();

	static _priv _newInstance() { return new _priv(); }

	static _priv _cast(Object o) { return (_priv)o; }

	// ---( server methods )---




	public static final void cleanStrings (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(cleanStrings)>> ---
		// @sigtype java 3.5
		// [i] field:1:required in
		// [o] field:1:required out
		// pipeline in
		IDataCursor pipelineCursor = pipeline.getCursor();
		String[] in = IDataUtil.getStringArray(pipelineCursor, "in");
		pipelineCursor.destroy();
		
		// process
		
		List<String> out = new ArrayList<String>();
		
		for (int i = 0; i < in.length; i++) {
			
			String outStr = in[i].replace("-", " ");
			
			if (outStr.indexOf(".") != -1)
				outStr = outStr.substring(0, outStr.lastIndexOf("."));
			
			if  (outStr != null && outStr.length() > 0)
				out.add(outStr);
		}
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "out", out.toArray(new String[out.size()]));
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
		// [i] field:0:required baseDir
		// [o] field:0:required filename
		// [o] field:0:required ext
		IDataCursor pipelineCursor = pipeline.getCursor();
		String label = IDataUtil.getString(pipelineCursor, "label");
		String type = IDataUtil.getString(pipelineCursor, "type");
		String baseDir = IDataUtil.getString(pipelineCursor, "baseDir");
		
		// process
		
		String filename = null;
		String group = "files";
		String ext = null;
		
		
		if (label != null && label.startsWith("form-data")) {
			label = label.substring(label.lastIndexOf("=")+2, label.length()-1);
		}
		
		if (label != null)
			filename = label.replace(" ", "-");
		
		if (type.equalsIgnoreCase("install") || type.equalsIgnoreCase("deployments") || type.equalsIgnoreCase("builds") || type.equalsIgnoreCase("runs") || type.equalsIgnoreCase("projects") || type.equalsIgnoreCase("registries")) {
			group = "configuration";
		
			if (label != null)
				filename += ".json";
		
			ext = ".json";
		
		} else if (type.equalsIgnoreCase("properties")) {
		
			if (label != null && !label.endsWith(".properties"))
				filename += ".properties";
		
			ext = ".properties";
		
		} else if (!type.equals("dockerfiles") && !type.equals("other") && label != null && filename.indexOf(".") == -1){ 
		
			if (label != null && !label.endsWith(".xml"))
				filename += ".xml";
		
			ext = ".xml";
			filename = label.replace(" ", "-");
		
			if (type.equalsIgnoreCase("install") || type.equalsIgnoreCase("deployments") || type.equalsIgnoreCase("builds") || type.equalsIgnoreCase("runs") || type.equalsIgnoreCase("projects")) {
				group = "configuration";
				filename += ".json";
			} else if (type.equalsIgnoreCase("properties")) {
		
				if (!label.endsWith(".properties"))
					filename += ".properties";
		
			} else if (!type.equals("other") && filename.indexOf(".") == -1){ 
		
				if (!label.endsWith(".xml"))
					filename += ".xml"; 
			}
		} else if (type.equals("sag")) {
			type = "install";
		}
		
		if (type.equals("sag") || type.equals("support") || type.equals("product"))
			baseDir = "./packages/JcDevopsConsole/resources";
		
		String out = baseDir + "/" + group + "/" + type;
		
		if (filename != null)
			out += "/" + filename;
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "filename", out);
		
		if (ext != null)
			IDataUtil.put(pipelineCursor, "ext", ext);
		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void readDockerfile (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(readDockerfile)>> ---
		// @sigtype java 3.5
		// [i] field:0:required pathForDockerfile
		// [o] field:0:required imageName
		// [o] record:1:optional buildCommands
		// [o] - field:0:required commandType
		// [o] - field:0:optional options
		// [o] - field:0:optional fileType
		// [o] - field:0:required source
		// [o] - field:0:optional target
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String pathForDockerfile = IDataUtil.getString(pipelineCursor, "pathForDockerfile");
		
		// process
		String imageName = null;
		ArrayList<IData> buildCommands = new ArrayList<IData>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(pathForDockerfile)));
			String line = null;
			while ((line=reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (line.startsWith("FROM") || line.startsWith("from")) {
						int spacer = line.indexOf(" ");
						
						if (spacer != -1)
							imageName = line.substring(spacer+1);					
					} else if (line.startsWith("COPY") || line.startsWith("copy") || 
							line.startsWith("ADD") || line.startsWith("ADD")) {
						
						StringTokenizer t = new StringTokenizer(line, " ");
						
						String commandType = t.nextToken().toLowerCase();
						String src = t.nextToken();
						String options =null;
						if (src.startsWith("--chown")) {
							options = src;
							src = t.nextToken();
						}
						
						String target = t.nextToken();
						
						buildCommands.add(makeBuildCommand(commandType, options, src, "resource", target));
					} else if (line.startsWith("run") || line.startsWith("RUN") 
							|| line.startsWith("ENTRYPOINT") || line.startsWith("entrypoint")) {
						
						int index = line.indexOf(" ");
						String type = line.substring(0, index).toLowerCase();
						String cmd = line.substring(index+1);
						
						buildCommands.add(makeBuildCommand(type, null, null, null, cmd));
					} else if (line.startsWith("HEALTHCHECK") || line.startsWith("healthcheck")) {
					
						// TODO
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "imageName", imageName);
		IDataUtil.put(pipelineCursor, "buildCommands", buildCommands.toArray(new IData[buildCommands.size()]));
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	public static IData makeBuildCommand(String commandType, String options, String source, String fileType, String target) {
	
		IData b = IDataFactory.create();
		IDataCursor bc = b.getCursor();
		IDataUtil.put(bc, "commandType", commandType);
		
		if (options != null)
			IDataUtil.put(bc, "options", options);
	
		if (fileType != null) 
			IDataUtil.put(bc, "fileType", fileType);
	
		IDataUtil.put(bc, "source", source);
		
		if (target != null)
			IDataUtil.put(bc, "target", target);
		
		bc.destroy();
		
		return b;
	}
		
	// --- <<IS-END-SHARED>> ---
}

