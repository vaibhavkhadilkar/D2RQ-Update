package edu.utdallas.d2rqrw;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.BindingQueryPlan;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.graph.query.TreeQueryPlan;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.Map1Iterator;

import de.fuberlin.wiwiss.d2rq.engine.QueryEngineD2RQ;
import edu.utdallas.d2rqrw.engine.D2RQRWDatasetGraph;

/**
 * <p>A class that implements a D2RQ-RW query handler</p>
 */
public class D2RQRWQueryHandler extends SimpleQueryHandler 
{
	/** The D2RQ-RW dataset graph **/
	private D2RQRWDatasetGraph dataset;
	
	/** A set of nodes **/
	private Node[] variables;
	
	@SuppressWarnings("unchecked")
	/** A map of indices **/
	private Map indexes;

	/**
	 * <p>Constructor</p>
	 * @param graph - the D2RQ++ graph
	 * @param dataset - the D2RQ++ dataset
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.D2RQQueryHandler#D2RQQueryHandler(de.fuberlin.wiwiss.d2rq.GraphD2RQ, de.fuberlin.wiwiss.d2rq.engine.D2RQDatasetGraph)
	 */
	public D2RQRWQueryHandler(GraphD2RQRW graph, D2RQRWDatasetGraph dataset) 
	{
		super(graph);
		this.dataset = dataset;
	}     

	/**
	 * <p>Method that prepares a query tree plan given a pattern</p>
	 * @param pattern - the given pattern as a graph
	 * @return a query tree plan
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.D2RQQueryHandler#prepareTree(Graph)
	 */
	public TreeQueryPlan prepareTree(Graph pattern) 
	{ throw new RuntimeException("prepareTree - Andy says Chris says this will not be called"); }

	/**
	 * <p>Method that prepares the bindings for a query plan</p>
	 * @param q - the input query
	 * @param variables - the variables in the given query
	 * @return a binding query plan
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.D2RQQueryHandler#prepareBindings(Query, Node[])
	 */
	@SuppressWarnings("unchecked")
	public BindingQueryPlan prepareBindings(Query q, Node[] variables) 
	{   
		this.variables = variables;
		this.indexes = new HashMap();
		for (int i = 0; i < variables.length; i++) { indexes.put(variables[i], new Integer(i)); }
		BasicPattern pattern = new BasicPattern();
		Iterator it = q.getPattern().iterator();
		while (it.hasNext()) 
		{
			Triple t = (Triple) it.next();
			pattern.add(t);
		}
		Plan plan = QueryEngineD2RQ.getFactory().create(new OpBGP(pattern), dataset, null, null);
		final ExtendedIterator queryIterator = new Map1Iterator(new BindingToDomain(), plan.iterator());
		return new BindingQueryPlan() 
		{
			public ExtendedIterator executeBindings() { return queryIterator; }
		};
	}

	@SuppressWarnings("unchecked")
	private class BindingToDomain implements Map1 
	{
		public Object map1(Object o) 
		{
			Binding binding = (Binding) o;
			Domain d = new Domain(variables.length);
			for (int i = 0; i < variables.length; i++) 
			{
				Var v = Var.alloc(variables[i]);
				Node value = binding.get(v);
				int index = ((Integer) indexes.get(v)).intValue();
				d.setElement(index, value);
			}
			return d;
		}
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