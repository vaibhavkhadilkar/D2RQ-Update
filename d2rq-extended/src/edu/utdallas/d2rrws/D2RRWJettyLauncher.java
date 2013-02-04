package edu.utdallas.d2rrws;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.webapp.WebAppContext;

import de.fuberlin.wiwiss.d2rs.ConfigLoader;

public class D2RRWJettyLauncher
{
	public final static int DEFAULT_PORT = 2020;

	private String configFile;
	private int cmdLinePort = -1;
	private int configFilePort = -1;
	private String baseURI = null;
	private String homeURI;
	private boolean useAllOptimizations = false;
	
	public void setConfigFile(String configFile) 
	{
		this.configFile = configFile;
		// We must parse the config file here to check if there's a port
		// specified inside, because we need to start up the Jetty on that
		// port.
		ConfigLoader config = new ConfigLoader(configFile);
		config.load();
		if (config.port() != -1) { configFilePort = config.port(); }
	}
	
	public void overridePort(int port) { cmdLinePort = port; }

	public void overrideBaseURI(String baseURI) { this.baseURI = baseURI; }
	
	public void overrideUseAllOptimizations(boolean useAllOptimizations) { this.useAllOptimizations = useAllOptimizations; }

	public void start() 
	{
		Server jetty = new Server(getPort());
		
		// use Random (/dev/urandom) instead of SecureRandom to generate session keys - otherwise Jetty may hang during startup waiting for enough entropy
		// see http://jira.codehaus.org/browse/JETTY-331 and http://docs.codehaus.org/display/JETTY/Connectors+slow+to+startup
		jetty.setSessionIdManager(new HashSessionIdManager(new Random()));
		WebAppContext context = new WebAppContext(jetty, "webapp", "");
		context.setInitParams(getInitParams());
		try { jetty.start(); } 
		catch (Exception ex) { throw new RuntimeException(ex); }
		homeURI = D2RRWServer.fromServletContext(context.getServletContext()).baseURI();
	}

	public String getHomeURI() { return homeURI; }

	public int getPort() 
	{
		if (cmdLinePort != -1) { return cmdLinePort; }
		if (configFilePort != -1) { return configFilePort; }
		return DEFAULT_PORT;
	}
	
	private Map<String,String> getInitParams() 
	{
		Map<String,String> result = new HashMap<String,String>();
		if (cmdLinePort != -1) { result.put("port", Integer.toString(cmdLinePort)); }
		if (baseURI != null) { result.put("baseURI", baseURI); }
		if (useAllOptimizations) { result.put("useAllOptimizations", "true"); }
		result.put("overrideConfigFile", configFile);
		return result;
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