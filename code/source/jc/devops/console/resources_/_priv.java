package jc.devops.console.resources_;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
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
		
		if (type.equalsIgnoreCase("install") || type.equalsIgnoreCase("deployments") || type.equalsIgnoreCase("builds") || type.equalsIgnoreCase("runs") || type.equalsIgnoreCase("projects")) {
			group = "configuration";
		
			if (label != null)
				filename += ".json";
		
			ext = ".json";
		
		} else if (type.equalsIgnoreCase("properties")) {
		
			if (label != null && !label.endsWith(".properties"))
				filename += ".properties";
		
			ext = ".properties";
		
		} else if (!type.equals("other") && label != null && filename.indexOf(".") == -1){ 
		
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
}

