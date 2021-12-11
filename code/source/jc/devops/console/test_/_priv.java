package jc.devops.console.test_;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
// --- <<IS-END-IMPORTS>> ---

public final class _priv

{
	// ---( internal utility methods )---

	final static _priv _instance = new _priv();

	static _priv _newInstance() { return new _priv(); }

	static _priv _cast(Object o) { return (_priv)o; }

	// ---( server methods )---




	public static final void replacePortPostfix (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(replacePortPostfix)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional server
		// [i] field:0:required port
		// [i] field:0:optional replaceDockerInternal {"false","true"}
		// [o] field:0:required server
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String server = IDataUtil.getString(pipelineCursor, "server");
		String port = IDataUtil.getString(pipelineCursor, "port");
		String replace = IDataUtil.getString(pipelineCursor, "replaceDockerInternal");
		
		//process
		
		int index = -1;
		
		if (server == null) {
			server = "localhost:" + (port != null ? port : "5555");
		}
		else if ((index=server.indexOf(":")) != -1) {
			
			server = server.substring(0, index+1);
			
			if (replace != null && replace.equalsIgnoreCase("true") && server.equals("host.docker.internal"))
				server = "localhost" + port;
			else
				server += port;
		} else {
			
			if (replace != null && replace.equalsIgnoreCase("true") && server.equals("host.docker.internal"))
				server = "localhost:" + port;
			else
				server += ":" + port;
		}
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "server", "http://" + server);
		pipelineCursor.destroy();
		
			
		// --- <<IS-END>> ---

                
	}
}

