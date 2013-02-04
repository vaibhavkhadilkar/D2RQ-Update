package edu.utdallas.d2rrw;

import jena.cmdline.ArgDecl;
import jena.cmdline.CommandLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.shared.JenaException;

import d2r.server;
import de.fuberlin.wiwiss.d2rs.ConfigLoader;
import edu.utdallas.d2rrws.D2RRWJettyLauncher;

/**
 * <p>The server to be used is described by this class</p>
 */
public class ExtendedServer 
{
	/** The usage string **/
	private final static String usage = "usage: d2rrw-server [-p port] [-b serverBaseURI] [--fast] mappingFileName";
	
	/** The Jetty launcher **/
	private static D2RRWJettyLauncher server;
	
	/** The logger to be used **/
	private final static Log log = LogFactory.getLog(server.class);
	
	public static void main(String[] args) 
	{
		CommandLine cmd = new CommandLine();
		cmd.setUsage(usage);
		ArgDecl portArg = new ArgDecl(true, "p", "port");
		cmd.add(portArg);
		ArgDecl baseURIArg = new ArgDecl(true, "b", "base");
		cmd.add(baseURIArg);
		ArgDecl fastArg = new ArgDecl(false, "fast");
		cmd.add(fastArg);
		cmd.process(args);
		
		if (cmd.numItems() == 0) {
			System.err.println(usage);
			System.exit(1);
		}
		if (cmd.numItems() > 2) {
			System.err.println("too many arguments");
			System.err.println(usage);
			System.exit(1);
		}
		server = new D2RRWJettyLauncher();
		if (cmd.contains(portArg)) {
			setPort(Integer.parseInt(cmd.getArg(portArg).getValue()));
		}
		if (cmd.contains(baseURIArg)) {
			setServerBaseURI(cmd.getArg(baseURIArg).getValue());
		}
		if (cmd.contains(fastArg)) {
			setUseAllOptimizations(true);
		}
		String mappingFileName = cmd.getItem(0);
		setMappingFileName(mappingFileName);
		startServer();
	}
	
	/**
	 * Method to set the port if specified by the user
	 * @param port - the port if specified as an argument
	 */
	public static void setPort(int port) { server.overridePort(port); }

	/**
	 * Method to set the server base URI if specified by the user
	 * @param baseURI - the base URI if specified as an argument
	 */
	public static void setServerBaseURI(String baseURI) { server.overrideBaseURI(baseURI); }
	
	/**
	 * Method that sets a flag to use all optimizations
	 * @param useAllOptimizations - true, iff all optimizations are to be used, false otherwise
	 */
	public static void setUseAllOptimizations(boolean useAllOptimizations) 
	{
		server.overrideUseAllOptimizations(useAllOptimizations);
	}
	
	/**
	 * Method that sets the input mapping file to be used
	 * @param mappingFileName - the filename to be used
	 */
	public static void setMappingFileName(String mappingFileName) 
	{
		try { server.setConfigFile(ConfigLoader.toAbsoluteURI(mappingFileName)); } 
		catch (JenaException ex) 
		{
			Throwable t = ex;
			if (ex.getCause() != null) { t = ex.getCause(); }
			System.err.println(mappingFileName + ": " + t.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Method to start the server
	 */
	public static void startServer() 
	{
		server.start();
		log.info("[[[ Server started at " + server.getHomeURI() + " ]]]");
	}
}
/** Copyright (c) 2010, The University of Texas at Dallas
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the The University of Texas at Dallas nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY The University of Texas at Dallas ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL The University of Texas at Dallas BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */