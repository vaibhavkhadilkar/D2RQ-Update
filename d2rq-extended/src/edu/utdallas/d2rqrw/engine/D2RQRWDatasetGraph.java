package edu.utdallas.d2rqrw.engine;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.LockNone;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.util.iterator.NullIterator;

import edu.utdallas.d2rqrw.GraphD2RQRW;

/**
 * <p>The D2RQ++ dataset graph</p>
 */
public class D2RQRWDatasetGraph implements DatasetGraph 
{
	/** <p>A lock instance</p> **/
	private final static Lock LOCK_INSTANCE = new LockNone();
	
	/** <p>The D2RQ++ graph used in this dataset graph</p> **/
	private final GraphD2RQRW graph;
	
	/** 
	 * <p>Constructor</p>
	 * @param graph - the D2RQ++ graph
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph#D2RQDatasetGraph(de.fuberlin.wiwiss.d2rq.GraphD2RQ) 
	 */
	public D2RQRWDatasetGraph(GraphD2RQRW graph) { this.graph = graph; }
	
	/**
	 * <p>Method that checks this dataset graph contains the given node</p>
	 * @param graphNode - the input node to be checked
	 * @return false
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph#containsGraph(Node)
	 */
	public boolean containsGraph(Node graphNode) { return false; }

	/**
	 * <p>Method that returns the underlying D2RQ++ graph</p>
	 * @return the underlying graph
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph#getDefaultGraph()
	 */
	public Graph getDefaultGraph() { return graph; }

	/**
	 * <p>Method that gets the graph that contains the given input node</p>
	 * @param graphNode - the input node to be checked
	 * @return the underlying graph
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph#getGraph(Node)
	 */
	public Graph getGraph(Node graphNode) { return null; }

	/**
	 * <p>Method that returns the current Lock instance </p>
	 * @return the current Lock instance
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph#getLock()
	 */
	public Lock getLock() { return LOCK_INSTANCE; }

	/**
	 * <p>Method that returns an iterator over the nodes in this graph </p>
	 * @return an iterator of nodes contained in this graph
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph#listGraphNodes()
	 */
	public Iterator<Node> listGraphNodes() { return NullIterator.instance(); }

	/**
	 * <p>Method that returns the size of this dataset graph </p>
	 * @return the size of this dataset graph
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph#size()
	 */
	public int size() { return 0; }

	/**
	 * <p>Method that closes this dataset graph </p>
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph#close()
	 */
	public void close() { graph.close(); }
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