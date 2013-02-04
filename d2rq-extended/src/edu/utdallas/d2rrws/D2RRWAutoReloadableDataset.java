package edu.utdallas.d2rrws;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.util.iterator.NullIterator;

import de.fuberlin.wiwiss.d2rq.vocab.D2RQ;
import de.fuberlin.wiwiss.d2rs.AutoReloadableDataset;
import edu.utdallas.d2rqrw.GraphD2RQRW;
import edu.utdallas.d2rqrw.ModelD2RQRW;
import edu.utdallas.d2rqrw.engine.D2RQRWDatasetGraph;

public class D2RRWAutoReloadableDataset implements Dataset
{
	private static Log log = LogFactory.getLog(AutoReloadableDataset.class);

	/** only reload any this mili seconds */
	private static long RELOAD_FREQUENCY_MS = 1000;

	private D2RRWServer server;
	private D2RQRWDatasetGraph datasetGraph = null;

	private String mappingFile;
	private long lastModified = Long.MAX_VALUE;
	private long lastReload = Long.MIN_VALUE;
	
	/** true if resultSizeLimit is used */
	private boolean hasTruncatedResults;

	/** (localFile) => auto-reloadable */
	private boolean localFile;

	private ModelD2RQRW internalModel = null;
	
	private Model defaultModel;

	public D2RRWAutoReloadableDataset(String mappingFile, boolean localFile, D2RRWServer server) 
	{
		this.mappingFile = mappingFile;
		this.localFile = localFile;	
		this.server = server;
	}

	/** re-init dsg */
	public void forceReload() {
		initD2RQDatasetGraph();		
	}

	/** re-init dsg if mapping file has changed */
	public void checkMappingFileChanged() 
	{
		if (!localFile || this.mappingFile == null || !server.getConfig().getAutoReloadMapping()) return;

		// only reload again if lastReload is older than CHECK_FREQUENCY_MS
		long now = System.currentTimeMillis();
		if (now < this.lastReload + RELOAD_FREQUENCY_MS) return;

		long lastmod = new File(this.mappingFile).lastModified();
		if (lastmod == this.lastModified)
			return;

		initD2RQDatasetGraph();
	}

	private void initD2RQDatasetGraph() {
		if (this.datasetGraph != null)
			log.info("Reloading mapping file");

		Model mapModel = ModelFactory.createDefaultModel();
		mapModel.read((this.localFile) ? "file:" + this.mappingFile : this.mappingFile, server.resourceBaseURI(), "N3");

		this.hasTruncatedResults = mapModel.contains(null, D2RQ.resultSizeLimit, (RDFNode) null);
		ModelD2RQRW result = new ModelD2RQRW(mapModel, server.resourceBaseURI());
		this.internalModel = result;

		//For testing only !!
		//Test adding a triple to this model
		internalModel.add( internalModel.createResource( "http://localhost:2020/resource/employee/2" ), 
				   internalModel.createProperty( "http://localhost:2020/vocab/resource/employee_empName" ), 
				   "XYZ" );
		
		GraphD2RQRW graph = (GraphD2RQRW) result.getGraph();
		graph.connect();
		graph.initInventory(server.baseURI() + "all/");
		this.datasetGraph = new D2RQRWDatasetGraph(graph);
		this.defaultModel = ModelFactory.createModelForGraph(datasetGraph.getDefaultGraph());		

		if (localFile) {
			this.lastModified = new File(this.mappingFile).lastModified();
			this.lastReload = System.currentTimeMillis();
		}
	}

	public ModelD2RQRW getD2RQRWModel() { return internalModel; }
	
	public Capabilities getCapabilities() {
		//checkMappingFileChanged();
		return this.datasetGraph.getDefaultGraph().getCapabilities();
	}

	public PrefixMapping getPrefixMapping() {
		//checkMappingFileChanged();
		return this.datasetGraph.getDefaultGraph().getPrefixMapping();
	}

	public boolean hasTruncatedResults() {
		//checkMappingFileChanged();
		return hasTruncatedResults;
	}

	public QueryHandler queryHandler() {
		checkMappingFileChanged();
		return this.datasetGraph.getDefaultGraph().queryHandler();
	}

	public DatasetGraph asDatasetGraph() {
		// check already done by servlets before getting the graph
		//checkMappingFileChanged();
		return datasetGraph;
	}

	public Model getDefaultModel() {
		// check already done earlier, don't care
		//checkMappingFileChanged();
		return defaultModel;
	}

	public boolean containsNamedModel(String uri) {
		return false;
	}

	public Lock getLock() {
		return datasetGraph.getLock();
	}

	public Model getNamedModel(String uri) {
		return null;
	}

	public Iterator<String> listNames() {
		return NullIterator.instance();
	}

	public void close() {
		datasetGraph.close();
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