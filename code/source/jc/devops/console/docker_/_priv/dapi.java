package jc.devops.console.docker_._priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.UnknownServiceException;
import com.wm.app.b2b.server.ServerAPI;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
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
import java.util.concurrent.atomic.AtomicReference;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.AttachParameter;
import com.spotify.docker.client.DockerClient.BuildParam;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.NetworkingConfig;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.HostConfig.Builder;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.EndpointConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkCreation;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.VolumeList;
import com.jc.devops.docker.type.ContainerWrapper;
import com.jc.devops.docker.DockerBuilder;
import com.jc.devops.docker.DockerComposeImpl;
import com.jc.devops.docker.DockerConnectionUtil;
import com.jc.devops.docker.ImageRegistry;
import com.jc.devops.docker.WebSocketContainerLogger;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
// --- <<IS-END-IMPORTS>> ---

public final class dapi

{
	// ---( internal utility methods )---

	final static dapi _instance = new dapi();

	static dapi _newInstance() { return new dapi(); }

	static dapi _cast(Object o) { return (dapi)o; }

	// ---( server methods )---




	public static final void attachSessionToContainer (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(attachSessionToContainer)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional dockerHost
		// [i] field:0:optional httpsCert
		// [i] field:0:required containerId
		// [i] field:0:required sessionId
		// pipeline in
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	dockerHost = IDataUtil.getString(pipelineCursor, "dockerHost");
		String	httpsCert = IDataUtil.getString(pipelineCursor, "httpsCert");
		String	containerId = IDataUtil.getString(pipelineCursor, "containerId");
		String	sessionId = IDataUtil.getString(pipelineCursor, "sessionId");
		pipelineCursor.destroy();
		
		// process
				
		try {
			WebSocketContainerLogger.defaultInstance(sessionId).attachContainerOutput(DockerConnectionUtil.createDockerClient(dockerHost, httpsCert), containerId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e);
		}
			
		// --- <<IS-END>> ---

                
	}



	public static final void build (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(build)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional dockerHost
		// [i] field:0:optional httpsCert
		// [i] field:0:optional dockerEmail
		// [i] field:0:optional dockerUser
		// [i] field:0:optional dockerPassword
		// [i] field:0:required buildDir
		// [i] field:0:required fromImage
		// [i] field:1:required toImages
		// [i] field:0:optional toImageLatest
		// [i] record:1:optional buildParams
		// [i] - field:0:required name
		// [i] - field:0:required value
		// [i] field:1:optional extractFiles
		// [o] field:0:required imageId
		// [o] field:0:required log
		// pipeline in
		
		IDataCursor cursor = pipeline.getCursor();
		
		String buildDirStr = IDataUtil.getString(cursor, "buildDir");
		String fromImage = IDataUtil.getString(cursor, "fromImage");
		String[] toImage = IDataUtil.getStringArray(cursor, "toImages");
		String toImageLatest = IDataUtil.getString(cursor, "toImageLatest");
		IData[] buildParams = IDataUtil.getIDataArray(cursor, "buildParams");
		String[] extractDirs = IDataUtil.getStringArray(cursor, "extractFiles");
		String dockerHost = IDataUtil.getString(cursor, "dockerHost");
		String httpsCert = IDataUtil.getString(cursor, "httpsCert");
		String registryEmail = IDataUtil.getString(cursor, "dockerEmail");
		String registryUser = IDataUtil.getString(cursor, "dockerUser");
		String registryPassword = IDataUtil.getString(cursor, "dockerPassword");
		
		// process
		
		try {
			
			if (registryEmail != null) {
				WebSocketContainerLogger.log("Connecting to docker registry with " + registryEmail + " / " + registryUser);
			} else {
				WebSocketContainerLogger.log("Using your default docker registry, hope you have logged in?");
			}
			
			ImageRegistry.setDefaultRegistry(DockerConnectionUtil.createDockerClient(dockerHost, httpsCert), registryEmail, registryUser, registryPassword);
		
			if (fromImage != null && !ImageRegistry.defaultRemoteRegistry().haveImage(fromImage)) {
				ImageRegistry.defaultRemoteRegistry().pull(fromImage);
			}
			
		} catch ( Exception e) {
			e.printStackTrace();
			ServerAPI.logError(e);
			WebSocketContainerLogger.log(e.getLocalizedMessage());
		}
		
		try {
		StringBuilder feedback = new StringBuilder();
		File buildDir = new File(buildDirStr);
		
		if (!buildDir.isDirectory()) {
			WebSocketContainerLogger.log("Invalid build directory: " + buildDirStr);
			throw new ServiceException("BuildDir must be a directory: " + buildDirStr);
		}
		
		if (toImage == null) {
			WebSocketContainerLogger.log("target image not specified (toImage param)");
			throw new ServiceException("toImage param missing");
		}
		
		if (!new File(buildDir, "Dockerfile").exists()) {
			WebSocketContainerLogger.log("Dockerfile not found in build directory: " + buildDirStr);
			throw new ServiceException("No Dockerfile in build directory: " + buildDirStr);
		}
		
		String[] ids = new String[toImage.length];
		String[] logs = new String[toImage.length];
		
		// initial build
		
		String to = toImage[0].toLowerCase();
		
		DockerBuilder builder = new DockerBuilder(dockerHost, httpsCert, to, toImageLatest);
			
		String returnedImageId;
		
		WebSocketContainerLogger.log("Building with Dockerfile in " + buildDirStr + " for " + toImageLatest);
		
		if (extractDirs != null)
			returnedImageId = builder.buildAndExtract(buildDirStr, convertIDataParams(buildParams), extractDirs);
		else
			returnedImageId = builder.build(buildDirStr, convertIDataParams(buildParams));
		
		String log = builder.log;
		
		WebSocketContainerLogger.log("Got back image reference: " + returnedImageId);
		
		if (toImage.length > 0) {
			for(int z = 1; z < toImage.length; z++) {
				builder.tag(returnedImageId, toImage[z]);
			}
		}
		
		// pipeline out
		
		IDataUtil.put(cursor, "imageId", returnedImageId);
		IDataUtil.put(cursor, "log", log);
		} catch(Exception e) {
			e.printStackTrace();
			throw new ServiceException(e);
		}
		cursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void containers (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(containers)>> ---
		// @sigtype java 3.5
		// [i] field:0:required dockerHost
		// [i] field:0:optional httpsCert
		// [o] recref:1:required containers jc.devops.console.docker_.docTypes:DockerContainer
		// pipeline in
		
		IDataCursor c = pipeline.getCursor();
		String dockerHost = IDataUtil.getString(c, "dockerHost");
		String httpsCert = IDataUtil.getString(c, "httpsCert");
		
		// process
		
		List<IData> containers = new  ArrayList<IData>();
		
		try {
			List<com.spotify.docker.client.messages.Container> out = DockerConnectionUtil.createDockerClient(dockerHost, null).listContainers(ListContainersParam.allContainers());
			
			out.forEach((container) -> {
			
				containers.add(new ContainerWrapper(container).toIData());
			});
			
		} catch (DockerException | InterruptedException | DockerCertificateException e) {
			e.printStackTrace();
			throw new ServiceException(e);
		}
		
		// pipeline out
		
		IDataUtil.put(c, "containers", containers.toArray(new IData[containers.size()]));
		c.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void detachContainer (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(detachContainer)>> ---
		// @sigtype java 3.5
		// [i] field:0:required sessionId
		// pipeline in
		IDataCursor pipelineCursor = pipeline.getCursor();
		String sessionId = IDataUtil.getString(pipelineCursor, "sessionId");
		pipelineCursor.destroy();
		
		// process
		
		WebSocketContainerLogger.detachDefaultInstance();
			
		// --- <<IS-END>> ---

                
	}



	public static final void getFileFromContainer (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getFileFromContainer)>> ---
		// @sigtype java 3.5
		// [i] field:0:required containerId
		// [i] field:0:required remoteDir
		// [i] field:0:required localArchive
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String containerId = IDataUtil.getString(pipelineCursor, "containerId");
		String remoteDir = IDataUtil.getString(pipelineCursor, "remoteDir");
		String localArchive = IDataUtil.getString(pipelineCursor, "localArchive");
		
		try {
			DockerBuilder.copyContentsFromContainer(DockerConnectionUtil.createDockerClient(), containerId, remoteDir, new File(localArchive));
		} catch (DockerException | InterruptedException | IOException | DockerCertificateException e) {
			throw new ServiceException(e);
		}
		// --- <<IS-END>> ---

                
	}



	public static final void images (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(images)>> ---
		// @sigtype java 3.5
		// [i] field:0:required filter
		// [i] field:0:required dockerHost
		// [i] field:0:required httpsCert
		// [o] recref:1:required images jc.devops.console.docker_.docTypes:DockerImage
		// pipeline in 
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String filter = IDataUtil.getString(pipelineCursor, "filter");
		String dockerHost = IDataUtil.getString(pipelineCursor, "dockerHost");
		String httpsCert = IDataUtil.getString(pipelineCursor, "httpsCert");
		
		// process
		
		if (filter != null) {
			
			if (filter.endsWith("-latest")) {
				
				filter = filter.substring(0, filter.indexOf("-latest"));
				
			} else if (filter.endsWith(":latest")) {
				
				filter = filter.substring(0, filter.indexOf(":latest"));
			} else if (filter.indexOf(":") != -1) {
				
				int index = filter.indexOf(":");
				String b4 = filter.substring(0, index);
				String after = filter.substring(index+1);
				
				if (ImageRegistry.isVersion(after)) {
					filter = b4;
				} else if (after.indexOf("-") != -1) {
					int nindex = after.indexOf("-");
					String name = after.substring(0, nindex);
					
					filter = b4 + ":" + name;
				}
			} else if (filter.indexOf("-") != -1) {
				
				int index = filter.indexOf("-");
				String b4 = filter.substring(0, index);
				String after = filter.substring(index+1);
				
				if (ImageRegistry.isVersion(after)) {
					filter = b4;
				}
			}
		}
				
		List<IData> images;
		try {
			images = new ImageRegistry(DockerConnectionUtil.createDockerClient(dockerHost, httpsCert)).images(filter, filter == null);
		} catch (DockerCertificateException e) {
			throw new ServiceException(e);
		}
		
		
		// pipeline out
		
		IDataUtil.put(pipelineCursor, "images", images.toArray(new IData[images.size()]));
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void logMessage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(logMessage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required message
		IDataCursor c = pipeline.getCursor();
		String message = IDataUtil.getString(c, "message");
		
		WebSocketContainerLogger.log(message);
			
		// --- <<IS-END>> ---

                
	}



	public static final void pull (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(pull)>> ---
		// @sigtype java 3.5
		// [i] field:0:required tag
		// [i] field:0:optional dockerHost
		// [i] field:0:optional httpsCert
		// [i] field:0:optional dockerEmail
		// [i] field:0:optional dockerUser
		// [i] field:0:optional dockerPassword
		// [o] record:0:required image
		// pipeline in 
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String tag = IDataUtil.getString(pipelineCursor, "tag");
		String email = IDataUtil.getString(pipelineCursor, "dockerEmail");
		String user = IDataUtil.getString(pipelineCursor, "dockerUser");
		String password = IDataUtil.getString(pipelineCursor, "dockerPassword");
		
		String dockerHost = IDataUtil.getString(pipelineCursor, "dockerHost");
		String httpsCert = IDataUtil.getString(pipelineCursor, "httpsCert");
				
		// process
		
		IData image = null;
		
		try {
					
			if (email != null)
				image = new ImageRegistry(DockerConnectionUtil.createDockerClient(dockerHost, httpsCert), email, user, password).pull(tag);
			else 
				image = new ImageRegistry(DockerConnectionUtil.createDockerClient(dockerHost, httpsCert)).pull(tag);
		
		} catch (DockerCertificateException | DockerException | InterruptedException e) {
			e.printStackTrace();
			throw new ServiceException(e);
		}
		
		IDataUtil.put(pipelineCursor, "image", image);
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void push (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(push)>> ---
		// @sigtype java 3.5
		// [i] field:0:required tag
		// [i] field:0:optional dockerHost
		// [i] field:0:optional httpsCert
		// [i] field:0:optional dockerEmail
		// [i] field:0:optional dockerUser
		// [i] field:0:optional dockerPassword
		// [o] record:0:required image
		// pipeline in 
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String tag = IDataUtil.getString(pipelineCursor, "tag");
		String email = IDataUtil.getString(pipelineCursor, "dockerEmail");
		String user = IDataUtil.getString(pipelineCursor, "dockerUser");
		String password = IDataUtil.getString(pipelineCursor, "dockerPassword");
		
		String dockerHost = IDataUtil.getString(pipelineCursor, "dockerHost");
		String httpsCert = IDataUtil.getString(pipelineCursor, "httpsCert");
				
		// process
		
		IData image = null;
		
		if (tag.indexOf("/") == -1) {
			
			// no remote repository has been specified, don't do anything
			
			return;
		}
		
		try {
			if (email != null)
				new ImageRegistry(DockerConnectionUtil.createDockerClient(dockerHost, httpsCert), email, user, password).push(tag);
			else 
				new ImageRegistry(DockerConnectionUtil.createDockerClient(dockerHost, httpsCert)).push(tag);
		
		} catch (DockerCertificateException | DockerException | InterruptedException e) {
			throw new ServiceException(e);
		}
		
		IDataUtil.put(pipelineCursor, "image", image);
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void run (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(run)>> ---
		// @sigtype java 3.5
		// [i] field:0:required environment
		// [i] field:0:optional buildNo
		// [i] field:0:optional stageName
		// [i] record:0:required compose
		// [i] - field:0:required name
		// [i] - record:1:required services
		// [i] -- field:0:required name
		// [i] -- field:0:required appName
		// [i] -- field:0:required replicas
		// [i] -- field:0:required appSelector
		// [i] -- field:0:required hostname
		// [i] -- recref:1:required apis jc.devops.console.gateway_.docTypes:ApiDefinition
		// [i] -- recref:1:required containers jc.devops.console.configuration_.docTypes:Container
		// [i] field:0:optional containers
		// [i] field:0:optional dockerHost
		// [i] field:0:optional httpsCert
		// [i] field:0:optional dockerEmail
		// [i] field:0:optional dockerUser
		// [i] field:0:optional dockerPassword
		// pipeline in
		
		IDataCursor c = pipeline.getCursor();
		
		String environment = IDataUtil.getString(c, "environment");
		
		IData compose = IDataUtil.getIData(c, "compose");
		String buildno = IDataUtil.getString(c, "buildNo");
		String containers = IDataUtil.getString(c, "containers");
		String stageName = IDataUtil.getString(c, "stageName");
		
		String dockerHost = IDataUtil.getString(c, "dockerHost");
		String httpsCert = IDataUtil.getString(c, "httpsCert");
		
		String registryEmail = IDataUtil.getString(c, "dockerEmail");
		String registryUser = IDataUtil.getString(c, "dockerUser");
		String registryPassword = IDataUtil.getString(c, "dockerPassword");
		
		// process 
				
		if (dockerHost != null && dockerHost.length() == 0)
			dockerHost = null;
		
		if (httpsCert != null && httpsCert.length() == 0)
			httpsCert = null;
		
		try {
			ImageRegistry.setDefaultRegistry(DockerConnectionUtil.createDockerClient(dockerHost, httpsCert), registryEmail, registryUser, registryPassword);
						
		} catch (Exception e) {
			e.printStackTrace();
			ServerAPI.logError(e);
			WebSocketContainerLogger.log(e.getLocalizedMessage());
		}
		
		try {
			DockerComposeImpl dk = new DockerComposeImpl(dockerHost, httpsCert, compose, stageName);
			dk.run(buildno, containers, environment);
			_comps.put(dk.name, dk);
		
			System.out.println("Running composition for " + dk.name);
			
		} catch(Exception e) {
			e.printStackTrace();
			throw new ServiceException(e);
		}
			
		// pipeline  out
		
		c.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void setWsSessionID (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(setWsSessionID)>> ---
		// @sigtype java 3.5
		// [i] field:0:required sessionId
		IDataCursor c = pipeline.getCursor();
		String sessionId = IDataUtil.getString(c, "sessionId");
		
		// process
		
		System.out.println("setting session id for web socket to " + sessionId);
		
		WebSocketContainerLogger.defaultInstance(sessionId != null && sessionId.length() > 0 ? sessionId : null);
		
		c.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void stop (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(stop)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [i] field:0:optional dockerHost
		// [i] field:0:optional httpsCert
		// pipeline in
		
		IDataCursor c = pipeline.getCursor();
		String name = IDataUtil.getString(c, "name");
		String dockerHost = IDataUtil.getString(c, "dockerHost");
		String httpsCert = IDataUtil.getString(c, "httpsCert");
				
		// process
		
		DockerComposeImpl dk = _comps.get(name);
		
		if (dk != null) {
			try {
				dk.stop();
				_comps.remove(name);
		
			} catch (DockerException e) {
				throw new ServiceException(e);
			} catch (InterruptedException e) {
				throw new ServiceException(e);
			} catch (DockerCertificateException e) {
				throw new ServiceException(e);
			}
		} else {
			throw new ServiceException("No running deployment for '" + name + "' found");
		}
		
		// pipeline out
		
		c.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void stopContainer (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(stopContainer)>> ---
		// @sigtype java 3.5
		// [i] field:0:required containerId
		// [i] field:0:optional dockerHost
		// [i] field:0:optional httpsCert
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String containerId = IDataUtil.getString(pipelineCursor, "containerId");
		String dockerHost = IDataUtil.getString(pipelineCursor, "dockerHost");
		String httpsCert = IDataUtil.getString(pipelineCursor, "httpsCert");
		
		try {
			DockerClient client = DockerConnectionUtil.createDockerClient(dockerHost, httpsCert);
			client.stopContainer(containerId, 0);
			client.removeContainer(containerId);
			
		} catch (DockerCertificateException | DockerException | InterruptedException e) {
			throw new ServiceException(e);
		}
		
		pipelineCursor.destroy();
		
		// pipeline
		// --- <<IS-END>> ---

                
	}



	public static final void tag (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(tag)>> ---
		// @sigtype java 3.5
		// [i] field:0:required tag
		// [i] field:0:required newTag
		// [i] field:0:optional dockerHost
		// [i] field:0:optional httpsCert
		// pipeline in 
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String tag = IDataUtil.getString(pipelineCursor, "tag");
		String newTag = IDataUtil.getString(pipelineCursor, "newTag");
		
		String dockerHost = IDataUtil.getString(pipelineCursor, "dockerHost");
		String httpsCert = IDataUtil.getString(pipelineCursor, "httpsCert");
				
		// process		
		try {
			
			DockerClient client = DockerConnectionUtil.createDockerClient(dockerHost, httpsCert);
		
			client.tag(tag, newTag);
		
		} catch (DockerCertificateException | DockerException | InterruptedException e) {
			throw new ServiceException(e);
		}
		
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static Map<String, DockerComposeImpl> _comps = new HashMap<String, DockerComposeImpl>();
		
	private static BuildParam[] convertIDataParams(IData[] params) {
	
		if (params == null || params.length == 0)
			return new BuildParam[0];
		
		BuildParam[] bparams = new BuildParam[params.length];
		int i = 0;
		
		for (IData param : params) {
			
			IDataCursor c = param.getCursor();
			String name = IDataUtil.getString(c, "name");
			String value = IDataUtil.getString(c, "value");
	
			bparams[i++] = new BuildParam(name, value);
			c.destroy();
		}
		
		return bparams;
	}
	// --- <<IS-END-SHARED>> ---
}

