package edu.utdallas.d2rrws;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joseki.RDFServer;
import org.joseki.Registry;
import org.joseki.Service;
import org.joseki.ServiceRegistry;
import org.joseki.processors.SPARQL;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerFactory;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry;

import de.fuberlin.wiwiss.d2rs.ConfigLoader;
import edu.utdallas.d2rqrw.GraphD2RQRW;

public class D2RRWServer
{
	private final static String SPARQL_SERVICE_NAME = "sparql";
	
	/* These service names should match the mappings in web.xml */
	private final static String RESOURCE_SERVICE_NAME = "resource";
	private final static String DATA_SERVICE_NAME = "data";
	private final static String PAGE_SERVICE_NAME = "page";
	private final static String VOCABULARY_STEM = "vocab/";
	
	private final static String DEFAULT_BASE_URI = "http://localhost";
	private final static String DEFAULT_SERVER_NAME = "D2R++ Server";
	private final static String SERVER_INSTANCE = "D2RRWServer.SERVER_INSTANCE";
	private static final Log log = LogFactory.getLog(D2RRWServer.class);
	
	/** d2rq mapping file */
	private String configFile;
	
	/** config file parser and Java representation */
	private ConfigLoader config = null;
	
	/** server port from command line, overrides port in config file */
	private int overridePort = -1;
	
	/** base URI from command line */
	private String overrideBaseURI = null;
	
	/** base URI from command line */
	private boolean overrideUseAllOptimizations = false;

	/** the dataset, auto-reloadable in case of local mapping files */
	private D2RRWAutoReloadableDataset dataset;

	
	public void putIntoServletContext(ServletContext context) { context.setAttribute(SERVER_INSTANCE, this); }
	
	public static D2RRWServer fromServletContext(ServletContext context) 
	{ return (D2RRWServer) context.getAttribute(SERVER_INSTANCE); }
	
	public void overridePort(int port) 
	{
		log.info("using port " + port);
		this.overridePort = port;
	}

	public void overrideBaseURI(String baseURI) 
	{

		// This is a hack to allow hash URIs to be used at least in the
		// SPARQL endpoint. It will not work in the Web interface.
		if (!baseURI.endsWith("/") && !baseURI.endsWith("#")) {	baseURI += "/"; }
		if (baseURI.indexOf('#') != -1) { log.warn("Base URIs containing '#' may not work correctly!"); }
		log.info("using custom base URI: " + baseURI);
		this.overrideBaseURI = baseURI;
	}
	
	public void overrideUseAllOptimizations(boolean overrideAllOptimizations) 
	{ this.overrideUseAllOptimizations = overrideAllOptimizations; }	
	
	public void setConfigFile(String configFileURL) { configFile = configFileURL; }

	public String baseURI() 
	{
		if (this.overrideBaseURI != null) { return this.overrideBaseURI; }
		if (this.config.baseURI() != null) { return this.config.baseURI(); }
		if (this.port() == 80) { return D2RRWServer.DEFAULT_BASE_URI + "/"; }
		return D2RRWServer.DEFAULT_BASE_URI + ":" + this.port() + "/";
	}

	public int port() 
	{
		if (this.overridePort != -1) { return this.overridePort; }
		if (this.config.port() != -1) { return this.config.port(); }
		return D2RRWJettyLauncher.DEFAULT_PORT;
	}
	
	public String serverName() 
	{
		if (this.config.serverName() != null) { return this.config.serverName(); }
		return D2RRWServer.DEFAULT_SERVER_NAME;
	}
	
	public boolean hasTruncatedResults() { return dataset.hasTruncatedResults(); }
	
	public String resourceBaseURI(String serviceStem) 
	{
		// This is a hack to allow hash URIs to be used at least in the
		// SPARQL endpoint. It will not work in the Web interface.
		if (this.baseURI().endsWith("#")) { return this.baseURI(); }
		return this.baseURI() + serviceStem + D2RRWServer.RESOURCE_SERVICE_NAME + "/";
	}
	
	public String resourceBaseURI() { return resourceBaseURI(""); }
	
	public String graphURLDescribingResource(String resourceURI) 
	{
		if (resourceURI.indexOf(":") == -1) { resourceURI = resourceBaseURI() + resourceURI; }
		String query = "DESCRIBE <" + resourceURI + ">";
		try { return this.baseURI() + D2RRWServer.SPARQL_SERVICE_NAME + "?query=" + URLEncoder.encode(query, "utf-8"); } 
		catch (UnsupportedEncodingException ex) { throw new RuntimeException(ex); }
	}
	
	public static String getResourceServiceName() { return RESOURCE_SERVICE_NAME; }

	public static String getDataServiceName() { return DATA_SERVICE_NAME; }
	
	public static String getPageServiceName() { return PAGE_SERVICE_NAME; }
	
	public String dataURL(String serviceStem, String relativeResourceURI) 
	{ return this.baseURI() + serviceStem + DATA_SERVICE_NAME + "/" + relativeResourceURI; }
	
	public String pageURL(String serviceStem, String relativeResourceURI) 
	{ return this.baseURI() + serviceStem + PAGE_SERVICE_NAME + "/" + relativeResourceURI; }
	
	public boolean isVocabularyResource(Resource r) 
	{ return r.getURI().startsWith(resourceBaseURI(VOCABULARY_STEM)); }

	public void addDocumentMetadata(Model document, Resource documentResource) 
	{ this.config.addDocumentMetadata(document, documentResource); }

	/**
	 * @return the auto-reloadable dataset which contains a GraphD2RQ as its default graph, no named graphs
	 */
	public D2RRWAutoReloadableDataset dataset() { return this.dataset; }

	/**
	 * @return The graph currently in use; will change to a new instance on auto-reload
	 */
	public GraphD2RQRW currentGraph() { return (GraphD2RQRW) this.dataset.asDatasetGraph().getDefaultGraph(); }
	
	/**
	 * delegate to auto-reloadable dataset, will reload if necessary
	 */
	public void checkMappingFileChanged() { dataset.checkMappingFileChanged(); }
	
	/** 
	 * delegate to auto-reloadable dataset	 * 
	 * @return prefix mappings for the d2rq base graph
	 */
	public PrefixMapping getPrefixes() { return dataset.getPrefixMapping(); }
	
	public void start() 
	{
		log.info("using config file: " + configFile);
		this.config = new ConfigLoader(configFile);
		this.config.load();
		
		if (config.isLocalMappingFile())
			this.dataset = new D2RRWAutoReloadableDataset(config.getLocalMappingFilename(), true, this);
		else
			this.dataset = new D2RRWAutoReloadableDataset(config.getMappingURL(), false, this);
		this.dataset.forceReload();
		
		if (this.overrideUseAllOptimizations)
			currentGraph().getConfiguration().setUseAllOptimizations(true);
		
		if (currentGraph().getConfiguration().getUseAllOptimizations()) {
			log.info("Fast mode (all optimizations)");
		} else {
			log.info("Safe mode (launch using --fast to use all optimizations)");
		}
		
		DescribeHandlerRegistry.get().clear();
		DescribeHandlerRegistry.get().add(new FindDescribeHandlerFactory());

		Registry.add(RDFServer.ServiceRegistryName,
				createJosekiServiceRegistry());
	}
	
	protected ServiceRegistry createJosekiServiceRegistry() 
	{
		ServiceRegistry services = new ServiceRegistry();
		Service service = new Service(new SPARQL(), D2RRWServer.SPARQL_SERVICE_NAME, new D2RQRWDatasetDesc(this.dataset));
		services.add(D2RRWServer.SPARQL_SERVICE_NAME, service);
		return services;
	}
	
	private class FindDescribeHandlerFactory implements DescribeHandlerFactory 
	{ public DescribeHandler create() { return new FindDescribeHandler(D2RRWServer.this); } }
	
	public ConfigLoader getConfig() { return config; }
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