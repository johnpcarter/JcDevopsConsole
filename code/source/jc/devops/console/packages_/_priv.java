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
		// [i] field:0:required packagesDir
		// pipeline  in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String packagesDir = IDataUtil.getString(pipelineCursor, "packagesDir");
			
		// process
		
		String[] apis = PackageIntrospector.defaultInstance(packagesDir, false).apiSummary();
		
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
		// [i] field:0:required packagesDir
		// [i] field:0:required name
		// [o] recref:0:required package jc.devops.console.packages_.docTypes:PackageInfo
		// Input
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String packagesDir = IDataUtil.getString(pipelineCursor, "packagesDir");
		String packageName = IDataUtil.getString(pipelineCursor, "name");
		
		// process
		
		PackageInfo pckg = null;
		
		if (packagesDir != null)
			pckg = PackageIntrospector.defaultInstance(packagesDir, false).packageInfo(packageName);
		else
			pckg = PackageIntrospector.defaultInstance(true).packageInfo(packageName);
		
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
		// [i] field:0:required packagesDir
		// [o] recref:1:required packages jc.devops.console.packages_.docTypes:PackageInfo
		// Input
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String packagesDir = IDataUtil.getString(pipelineCursor, "packagesDir");
		
		// process
		
		IData[] pckgs = null;
		
		if (packagesDir != null)
			pckgs = PackageIntrospector.defaultInstance(packagesDir, false).packageDetails();
		else
			pckgs = PackageIntrospector.defaultInstance(false).packageDetails();
		
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
		// [i] field:0:required packagesDir
		// [i] field:0:required repository
		// [o] recref:1:required packages jc.devops.console.packages_.docTypes:PackageInfo
		// pipeline  in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String packagesDir = IDataUtil.getString(pipelineCursor, "packagesDir");
		String repo = IDataUtil.getString(pipelineCursor, "repository");
		
		// process
		
		if (packagesDir != null && !packagesDir.startsWith("./") && repo != null) {
			
			if (!packagesDir.startsWith("/"))
				packagesDir = "/" + packagesDir;
			
			packagesDir = "./packages/JcDevopsConsole/resources/source/" + repo + packagesDir;
		}
		
		PackageIntrospector p = PackageIntrospector.defaultInstance(packagesDir, true);
		
		// output
		
		IDataUtil.put(pipelineCursor, "packages", p.allPackages(false));
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void indexedPackages (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(indexedPackages)>> ---
		// @sigtype java 3.5
		// [i] field:0:required packagesDir
		// [o] recref:1:required packages jc.devops.console.packages_.docTypes:PackageInfo
		// Input
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String packagesDir = IDataUtil.getString(pipelineCursor, "packagesDir");
		
		IData[] packages = PackageIntrospector.defaultInstance(packagesDir, false).allPackages(true);
		
		IDataUtil.put(pipelineCursor, "packages", packages);
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void isIndexed (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(isIndexed)>> ---
		// @sigtype java 3.5
		// [i] field:0:required packagesDir
		// [o] field:0:required isIndexed {"true","false"}
		IDataCursor pipelineCursor = pipeline.getCursor();
		String packagesDir = IDataUtil.getString(pipelineCursor, "packagesDir");
		
		// process
		
		boolean isIndexed = PackageIntrospector.haveInstance(packagesDir);
		
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
		// [i] field:0:required packagesDir
		// [i] field:1:required packageNames
		// [o] recref:1:required packages jc.devops.console.packages_.docTypes:PackageInfo
		// [o] recref:1:required dependencies jc.devops.console.packages_.docTypes:PackageInfo
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String packagesDir = IDataUtil.getString(pipelineCursor, "packagesDir");
		String[] packageNames = IDataUtil.getStringArray(pipelineCursor, "packageNames");
			
		// process
		
		PackageIntrospector p = PackageIntrospector.defaultInstance(packagesDir, false);
		
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
		// [i] field:0:optional packagesDir
		// [i] field:0:required packageName
		// [o] field:1:required services
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String packagesDir = IDataUtil.getString(pipelineCursor, "packagesDir");
		String packageName = IDataUtil.getString(pipelineCursor, "packageName");
		pipelineCursor.destroy();
		
		String[] services = null;
		
		if (packagesDir != null)
			services = PackageIntrospector.defaultInstance(packagesDir, false).servicesForPackage(packageName);
		else
			services = PackageIntrospector.defaultInstance(false).servicesForPackage(packageName);
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "services", services);
		pipelineCursor.destroy();
		
			
		// --- <<IS-END>> ---

                
	}
}

