package jc.devops.console.gateway_;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
// --- <<IS-END-IMPORTS>> ---

public final class _priv

{
	// ---( internal utility methods )---

	final static _priv _instance = new _priv();

	static _priv _newInstance() { return new _priv(); }

	static _priv _cast(Object o) { return (_priv)o; }

	// ---( server methods )---




	public static final void getIdForAPI (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getIdForAPI)>> ---
		// @sigtype java 3.5
		// [i] field:0:required apiName
		// [i] field:0:optional apiVersion
		// [o] field:0:required id
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String apiName = IDataUtil.getString(pipelineCursor, "apiName");
		String apiVersion = IDataUtil.getString(pipelineCursor, "apiVersion");
		
		pipelineCursor.destroy();
			
		// process
			
		if (apiVersion == null)
			apiVersion = "1.0";
		
		Xref ref = xref.get(apiName);
						
		// pipeline out
				
		if (xref != null)
			IDataUtil.put(pipelineCursor, "id", ref.getId(apiVersion));
				
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void getIdsForAPI (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getIdsForAPI)>> ---
		// @sigtype java 3.5
		// [i] field:0:required apiName
		// [o] record:1:required results
		// [o] - field:0:required id
		// [o] - field:0:required version
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String apiName = IDataUtil.getString(pipelineCursor, "apiName");
		pipelineCursor.destroy();
		
		// process
		
		Xref ref = xref.get(apiName);
				
		// pipeline out
		
		if (xref != null)
			IDataUtil.put(pipelineCursor, "results", ref.toIData());
		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void replaceServerAddress (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(replaceServerAddress)>> ---
		// @sigtype java 3.5
		// [i] field:0:required url
		// [i] field:0:optional host
		// [i] field:0:optional port
		// [o] field:0:required url
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String host = IDataUtil.getString(pipelineCursor, "host");
		String port = IDataUtil.getString(pipelineCursor, "port");
		String url = IDataUtil.getString(pipelineCursor, "url");
		
		if (host != null) {
			
			int start = url.indexOf("//");
			String remain = url.substring(start+2);
			int end = remain.indexOf("/");
			
			if (host.indexOf(":") != -1) {
				host = host.substring(0, host.indexOf(":"));
			}
			
			if (port != null) {
				
				url = url.substring(0, start) + "//" + host + ":" + port + remain.substring(end);
		
			} else {
				
				end = remain.indexOf(":");
				url = url.substring(0, start) + "//" + host + remain.substring(end);
			}
		}
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "url", url);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void setIdForAPI (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(setIdForAPI)>> ---
		// @sigtype java 3.5
		// [i] field:0:required id
		// [i] field:0:required apiName
		// [i] field:0:required version
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String id = IDataUtil.getString(pipelineCursor, "id");
		String apiName = IDataUtil.getString(pipelineCursor, "apiName");
		String version = IDataUtil.getString(pipelineCursor, "version");
		pipelineCursor.destroy();
		
		// process
		
		Xref ref = xref.get(apiName);
		
		if (ref == null) {
			ref = new Xref();
			xref.put(apiName, ref);
		}
		
		ref.add(version, id);
		
		// pipeline out
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static Map<String, Xref> xref = new HashMap<String, Xref>();
	
	private static class Xref {
		
		private Map<String, String> ids = new HashMap<String, String>();
		
		public void add(String version, String id) {
			
			ids.put(version,  id);
		}
		
		public String getId(String version) {
			
			return this.ids.get(version);
		}
		
		public IData[] toIData() {
			
			ArrayList<IData> out = new ArrayList<IData>();
			
			this.ids.keySet().forEach((k) -> {
				IData r = IDataFactory.create();
				
				IDataCursor c = r.getCursor();
				IDataUtil.put(c, "version", k);
				IDataUtil.put(c, "id", this.ids.get(k));
				c.destroy();
				
				out.add(r);
			});
			
			return out.toArray(new IData[out.size()]);
		}
		
		public IData toIData(String version) {
			
			String id = this.ids.get(version);
			IData r = null;
			
			if (id != null) {
				r = IDataFactory.create();
				
				IDataCursor c = r.getCursor();
				IDataUtil.put(c, "version", version);
				IDataUtil.put(c, "id", id);
				c.destroy();
			};
			
			return r;
		}
	}
	// --- <<IS-END-SHARED>> ---
}

