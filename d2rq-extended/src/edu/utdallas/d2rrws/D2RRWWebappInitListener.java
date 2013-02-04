package edu.utdallas.d2rrws;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import de.fuberlin.wiwiss.d2rs.ConfigLoader;

public class D2RRWWebappInitListener implements ServletContextListener
{
	public void contextInitialized(ServletContextEvent event) 
	{
		ServletContext context = event.getServletContext();
		D2RRWServer server = new D2RRWServer();
		String configFile = context.getInitParameter("overrideConfigFile");
		if (configFile == null) 
		{
			if (context.getInitParameter("configFile") == null)
				throw new RuntimeException("No configFile configured in web.xml");
			configFile = absolutize(context.getInitParameter("configFile"), context);
		}
		if (context.getInitParameter("port") != null)
			server.overridePort(Integer.parseInt(context.getInitParameter("port")));
		if (context.getInitParameter("baseURI") != null)
			server.overrideBaseURI(context.getInitParameter("baseURI"));
		if (context.getInitParameter("useAllOptimizations") != null)
			server.overrideUseAllOptimizations(context.getInitParameter("useAllOptimizations").equalsIgnoreCase("true"));
		server.setConfigFile(configFile);
		server.start();
		server.putIntoServletContext(context);
		D2RRWVelocityWrapper.initEngine(server, context);
	}

	public void contextDestroyed(ServletContextEvent event) 
	{ 
		// Do nothing
	}
	
	private String absolutize(String fileName, ServletContext context) 
	{
		if (!fileName.matches("[a-zA-Z0-9]+:.*"))
			fileName = context.getRealPath("WEB-INF/" + fileName);
		return ConfigLoader.toAbsoluteURI(fileName);
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