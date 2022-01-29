package jc.devops.console;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.DefaultDockerClient;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
// --- <<IS-END-IMPORTS>> ---

public final class _priv

{
	// ---( internal utility methods )---

	final static _priv _instance = new _priv();

	static _priv _newInstance() { return new _priv(); }

	static _priv _cast(Object o) { return (_priv)o; }

	// ---( server methods )---




	public static final void cloneGitRepo (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(cloneGitRepo)>> ---
		// @sigtype java 3.5
		// [i] field:0:required uri
		// [i] field:0:optional repoName
		// [i] field:0:required user
		// [i] field:0:required password
		// [i] field:0:required localDir
		// pipeline in
		
		IDataCursor cursor = pipeline.getCursor();
		String uri = IDataUtil.getString(cursor, "uri");
		String repo = IDataUtil.getString(cursor, "repoName");
		String user = IDataUtil.getString(cursor, "user");
		String password = IDataUtil.getString(cursor, "password");
		
		String localDirStr = IDataUtil.getString(cursor, "localDir");
		
		// process
		
		if (repo != null) {
			if (uri.endsWith("/"))
				uri += repo + ".git";
			else
				uri += "/" + repo + ".git";
		}
		
		File localDir = new File(localDirStr);
		
		if (!localDir.exists()) {
			localDir.mkdirs();
		} else if (!localDir.isDirectory()) {
			throw new ServiceException("BuildDir must be a directory: " + localDirStr);
		} else {
			
			System.out.println("Delete existing directory " + localDirStr);
		
			deleteDir(localDir);
			
			localDir.mkdir();
		}
				
		System.out.println("Cloning from " + uri);
		
		CloneCommand c = new CloneCommand();
		c.setURI(uri);
		c.setCloneSubmodules(true);
		
		if (user != null && password != null)
			c.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password));
		
		c.setDirectory(localDir);
		
		try {
			c.call();
		} catch (InvalidRemoteException e) {
			e.printStackTrace();
			throw new ServiceException("Invalid GIT source: " + e.getMessage());
		} catch (TransportException e) {
			e.printStackTrace();
			throw new ServiceException("Got an exception connecting to GIT repo: " + e.getMessage());
		} catch (JGitInternalException e) {
			e.printStackTrace();
			throw new ServiceException("Got an internal exception from GIT API: This might be caused by a badly referenced submodule, " + e.getMessage());
		} catch (GitAPIException e) {
			e.printStackTrace();
			throw new ServiceException("Got an exception from GIT API: " + e.getMessage());
		}
		
		// pipeline out
		
		cursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void commitAndPushGitRepo (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(commitAndPushGitRepo)>> ---
		// @sigtype java 3.5
		// [i] field:0:required dir
		// [i] field:0:required message
		// [i] field:1:optional files
		// [i] field:0:optional remove {"false","true"}
		// [i] field:0:required gitUri
		// [i] field:0:required gitUser
		// [i] field:0:required gitPassword
		// pipleine in
		
		IDataCursor c = pipeline.getCursor();
		String dir = IDataUtil.getString(c, "dir");
		String files[] = IDataUtil.getStringArray(c, "files");
		String message = IDataUtil.getString(c, "message");
		String remove = IDataUtil.getString(c, "remove");
		String gitUri = IDataUtil.getString(c, "gitUri");
		String gitUser = IDataUtil.getString(c, "gitUser");
		String gitPassword = IDataUtil.getString(c, "gitPassword");
				
		c.destroy();
				
		// process
						
		try {
			
			Git git = commitLocalRepo(new File(dir), files, message, remove != null &&  remove.equalsIgnoreCase("true"));
		
			if (git != null) {
				git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUser, gitPassword)).call();
			} else {
				throw new ServiceException("Couldn't establish local repo at " + dir);
			}
		} catch (Exception e) {
			throw new ServiceException(e);
		}
			
		// --- <<IS-END>> ---

                
	}



	public static final void createGitRepo (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(createGitRepo)>> ---
		// @sigtype java 3.5
		// [i] field:0:required name
		// [i] field:0:required dir
		// [i] field:0:required gitUri
		// [i] field:0:required gitUser
		// [i] field:0:required gitPassword
		// [o] field:0:required isNew
		// pipleine in
		
		IDataCursor c = pipeline.getCursor();
		String name = IDataUtil.getString(c, "name");
		String dir = IDataUtil.getString(c, "dir");
		String gitUri = IDataUtil.getString(c, "gitUri");
		String gitUser = IDataUtil.getString(c, "gitUser");
		String gitPassword = IDataUtil.getString(c, "gitPassword");
				
		// process
				
		boolean isNew = true;
		
		try {
			
		    File gitDir = new File(dir);
		    
		    if (!gitDir.exists()) {
		    	
		    	// directory doesn't exist
		    	
		    	if (!gitDir.mkdir())
		    		throw new ServiceException("Could create local repo directory: " + dir);
		    }  
		    
		    if (!new File(gitDir, ".git").exists()) {
		    	
		    	// directory exists, but it isn't a git repo
		    		
		    	setupAsGitRepo(gitDir, gitUri, gitUser, name, gitPassword);
		    } else {
		    	// exists and is a  repo
		    	
		    	isNew =  false;
		    }
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new ServiceException(e);
		}
		
		// pipeline out
		
		IDataUtil.put(c, "isNew", "" + isNew);
		c.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void zipIt (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(zipIt)>> ---
		// @sigtype java 3.5
		// [i] field:0:required sourceFolder
		// [i] field:0:required zipFile
		// [o] field:0:required zipFile
		// pipeline in
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		String sourceFolder = IDataUtil.getString(pipelineCursor, "sourceFolder");
		String zipFile = IDataUtil.getString(pipelineCursor, "zipFile");
		
		// process
		
		System.out.println("build dir is " + sourceFolder + ", preparing zip file " + zipFile);
		
		try {
			if (!zipFile.startsWith("/") && !zipFile.startsWith("./"))
				zipFile = new File(new File(sourceFolder).getParent(), zipFile).getAbsolutePath();
		
			if (!zipFile.endsWith(".zip"))
				zipFile += ".zip";
		
			new Zipper().zipIt(sourceFolder, zipFile);
		
			byte[] contents;
		
			contents = Files.readAllBytes(FileSystems.getDefault().getPath(zipFile));
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new ServiceException("Cannot read zipped file '" + zipFile + "' " + e.getLocalizedMessage());
		}
		
		// pipeline out
		
		//IDataUtil.put(pipelineCursor, "contents", contents);
		
		if (zipFile != null)
			IDataUtil.put(pipelineCursor, "zipFile", new File(zipFile).getName());
		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static void setupAsGitRepo(File gitDir, String gitUri, String account, String name, String gitPassword) throws IllegalStateException, GitAPIException, IOException, URISyntaxException {
	 
		 if  (new File(".", ".git").exists()) {
			 setupLocalSubmodule(gitDir, gitUri, account, name, gitPassword);
		 } else {
			 setupLocalRepo(gitDir, gitUri, account, name, gitPassword);
		 }
	}
	
	private static Git commitLocalRepo(File dir, String[] files, String message, boolean remove) throws GitAPIException, IOException {
		
		Git git =  null;
			
		if  (new File(".", ".git").exists()) {
			// submodule
		
			SubmoduleWalk walk = SubmoduleWalk.forIndex(Git.open(new File(".", ".git")).getRepository());
			
			while( walk.next() ) {
			    Repository submoduleRepository = walk.getRepository();
			    
			    System.out.println("checking " + dir.getName() + " == " + submoduleRepository.getDirectory().getName());
			    
			    if (submoduleRepository.getDirectory().getName().equals(dir.getName())) {
			    	// matches
			    	System.out.println("matched");
			    	git = Git.wrap(submoduleRepository);
			    }
			    
			    submoduleRepository.close();
			 }
			
			walk.close();
			
			//git = Git.open(new File(new File(new File(".", ".git"), "modules"), dir.getPath()));
	
		} 
		
		if (git == null && new File(dir, ".git").exists()) {
			git = Git.open(dir);
		}
		
		// add or remove files		
		
		if (git != null) {
			
			if (files != null) {
				for (int i = 0; i < files.length; i++)
					if (remove) {
						System.out.println("Removing " + files[0] + " from git");
						git.rm().addFilepattern(files[0]).call();
					} else {
						
						System.out.println("Adding " + files[0] + " from git");
						git.add().setUpdate(true).addFilepattern(files[0]);
					}
			} else {
				if (remove)
					git.rm().addFilepattern(".").call();
				else
					git.add().addFilepattern(".").call();
			}
		
			git.commit().setMessage(message).call();
		}
						
		return git;
	}
	
	private static Git setupLocalSubmodule(File dir, String gitUri, String account, String name, String password) throws IllegalStateException, GitAPIException, IOException {
		
		// already have a local repo, add ours as a submodule, linked to new git
		
		System.out.println("Creating remote origin for git: " + gitUri + "/" + name);
		
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(account, password);
		
		Git parent = Git.init().setDirectory(new File(".")).call();
		//parent.submoduleAdd().setURI(gitUri + "/" + name).setName(name).setPath(dir.getPath()).setCredentialsProvider(credentialsProvider).call();
		parent.submoduleAdd().setURI(gitUri).setName(name).setPath(dir.getPath()).setCredentialsProvider(credentialsProvider).call();
	
		return Git.open(new File(new File(new File(".", ".git"), "modules"), dir.getPath()));
	}
	
	private static Git setupLocalRepo(File dir, String gitUri, String account, String name, String password) throws GitAPIException, IOException, URISyntaxException {
		
		// ensure that it is recognized as a submodule repo by parent
		
		Git git = Git.init().setDirectory(dir).call();
		
		// add remote repo
				
		//CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(account, password);
	
		RemoteAddCommand remoteAddCommand = git.remoteAdd();
		remoteAddCommand.setName("origin");
		remoteAddCommand.setUri(new URIish(gitUri + "/" + name));
		
		// you can add more settings here if needed
		
		remoteAddCommand.call();
		
		return git;
	}
	
	private static class Zipper {
		
	private String _sourceFolder;
	private List <String> fileList = new ArrayList<String>();
	
	public void zipIt(String sourceFolder, String zipFile) {
		
		this._sourceFolder = sourceFolder;
	
		this.generateFileList(new File(sourceFolder));
			
	    byte[] buffer = new byte[1024];
	    String source = new File(sourceFolder).getName();
	    FileOutputStream fos = null;
	    ZipOutputStream zos = null;
	    try {
	        fos = new FileOutputStream(zipFile);
	        zos = new ZipOutputStream(fos);
	
	        System.out.println("Output to Zip : " + zipFile);
	        FileInputStream in = null;
	
	        for (String file: this.fileList) {
	            System.out.println("File Added : " + file);
	            ZipEntry ze = new ZipEntry(source + File.separator + file);
	            zos.putNextEntry(ze);
	            try {
	                in = new FileInputStream(this._sourceFolder + File.separator + file);
	                int len;
	                while ((len = in .read(buffer)) > 0) {
	                    zos.write(buffer, 0, len);
	                }
	            } finally {
	                in.close();
	            }
	        }
	
	        zos.closeEntry();
	        System.out.println("Folder successfully compressed");
	
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        try {
	            zos.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public void generateFileList(File node) {
	    // add file only
	    if (node.isFile()) {
	        fileList.add(generateZipEntry(node.toString()));
	    }
	
	    if (node.isDirectory()) {
	        String[] subNote = node.list();
	        for (String filename: subNote) {
	            generateFileList(new File(node, filename));
	        }
	    }
	}
	
	private String generateZipEntry(String file) {
				
	    return file.substring(_sourceFolder.length() + 1, file.length());
	}
	}
	
	private static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            if (! Files.isSymbolicLink(f.toPath())) {
	                deleteDir(f);
	            }
	        }
	    }
	    file.delete();
	}
	// --- <<IS-END-SHARED>> ---
}

