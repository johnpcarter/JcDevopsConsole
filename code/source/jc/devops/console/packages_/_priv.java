package jc.devops.console.packages_;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.util.coder.IDataBinCoder;
import com.wm.util.coder.XMLCoder;
import com.jc.wm.introspection.PackageInfo;
import com.jc.wm.introspection.PackageIntrospector;
import com.wm.app.b2b.server.ServerAPI;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
// --- <<IS-END-IMPORTS>> ---

public final class _priv

{
	// ---( internal utility methods )---

	final static _priv _instance = new _priv();

	static _priv _newInstance() { return new _priv(); }

	static _priv _cast(Object o) { return (_priv)o; }

	// ---( server methods )---




	public static final void getAPIs (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getAPIs)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// pipeline  in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
			
		// process
		
		String[] apis = PackageIntrospector.defaultInstance(name).apiSummary();
		
		// pipeline out
		
		IDataCursor c = pipeline.getCursor();
		IDataUtil.put(c, "apis", apis);
		// --- <<IS-END>> ---

                
	}



	public static final void getPackageInfo (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getPackageInfo)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [i] field:0:required packageName
		// [o] recref:0:required package jc.devops.console.packages_.docTypes:PackageInfo
		// Input
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
		String packageName = IDataUtil.getString(pipelineCursor, "packageName");
		
		// process
		
		PackageInfo pckg = PackageIntrospector.defaultInstance(name).packageInfo(packageName);
		
		// output
		
		if (pckg != null)
			IDataUtil.put(pipelineCursor, "package", pckg.toIData(true, true, null));
		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void getPackages (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getPackages)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [o] recref:1:required packages jc.devops.console.packages_.docTypes:PackageInfo
		// Input
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
		
		// process
		
		IData[] pckgs = PackageIntrospector.defaultInstance(name).packageDetails();
		
		// output
		
		if (pckgs != null)
			IDataUtil.put(pipelineCursor, "packages", pckgs);
		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void indexPackages (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(indexPackages)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [i] field:0:required sourceDirectory
		// [i] record:1:required repositories
		// [i] - field:0:required server
		// [i] - field:0:required name
		// [i] - field:0:optional path
		// [o] recref:1:required packages jc.devops.console.packages_.docTypes:PackageInfo
		// pipeline  in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
		String sourceDir = IDataUtil.getString(pipelineCursor, "sourceDirectory");
		IData[] repos = IDataUtil.getIDataArray(pipelineCursor, "repositories");
		
		// process
		
		PackageIntrospector p = PackageIntrospector.defaultInstance(name, sourceDir, convertToMap(repos), true);
		
		// output
		
		IDataUtil.put(pipelineCursor, "packages", p.allPackages());
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void indexedPackages (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(indexedPackages)>> ---
		// @sigtype java 3.5
		// [o] recref:1:required packages jc.devops.console.packages_.docTypes:PackageInfo
		// Input
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
		
		// process
		
		IData[] packages = PackageIntrospector.defaultInstance(name).allPackages();
		
		// output
		
		IDataUtil.put(pipelineCursor, "packages", packages);
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void isIndexed (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(isIndexed)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [o] field:0:required isIndexed {"true","false"}
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
		
		// process
		
		boolean isIndexed = PackageIntrospector.haveInstance(name);
		
		// output
		
		IDataUtil.put(pipelineCursor, "isIndexed", ""  + isIndexed);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void packagesDependencies (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(packagesDependencies)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [i] field:1:required packageNames
		// [o] recref:1:required packages jc.devops.console.packages_.docTypes:PackageInfo
		// [o] recref:1:required dependencies jc.devops.console.packages_.docTypes:PackageInfo
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
		String[] packageNames = IDataUtil.getStringArray(pipelineCursor, "packageNames");
			
		// process
		
		PackageIntrospector p = PackageIntrospector.defaultInstance(name);
		
		List<IData> results = p.dependencies(packageNames);
		
		IData[] packages = new IData[packageNames.length];
		
		for (int i = 0; i < packageNames.length; i++) {
			PackageInfo packageInfo = p.packageInfo(packageNames[i]);
			
			if (packageInfo != null)
				packages[i] = packageInfo.toIData(true, true, null);
		}
		
		// out
		
		IDataUtil.put(pipelineCursor, "dependencies", results.toArray(new IData[results.size()]));
		IDataUtil.put(pipelineCursor, "packages", packages);
		
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void servicesForPackage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(servicesForPackage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [i] field:0:optional packagesDir
		// [o] field:1:required services
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String name = IDataUtil.getString(pipelineCursor, "name");
		String packageName = IDataUtil.getString(pipelineCursor, "packageName");
		pipelineCursor.destroy();
		
		String[] services = PackageIntrospector.defaultInstance(name).servicesForPackage(packageName);
		
		// pipeline out 
		
		IDataUtil.put(pipelineCursor, "services", services);
		pipelineCursor.destroy();
		
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---

	private static Map<String, String> convertToMap(IData[] in) {
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		for (IData i : in) {
			IDataCursor c = i.getCursor();
			String server = IDataUtil.getString(c, "server");
			String name = IDataUtil.getString(c, "name");
			String path = IDataUtil.getString(c, "path");
			c.destroy();
			
			String key = new File(server, name).getPath();
			
			if (path != null && !path.isEmpty()) {
				map.put(key, path);
			} else {
				map.put(key, ".");
			}
		}
		
		return map;
		
	}
	// --- <<IS-END-SHARED>> ---
}

