package edu.utdallas.d2rrws;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A custom {@link DescribeHandler} that returns the results of a two-way
 * find.
 * 
 * TODO Is this thread-safe? ARQ uses just a single instance of this class.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 * @version $Id: FindDescribeHandler.java,v 1.14 2009/06/12 14:21:23 fatorange Exp $
 */
public class FindDescribeHandler implements DescribeHandler 
{
	private Model resultModel;
	private final D2RRWServer server;
	
	public FindDescribeHandler(D2RRWServer server) 
	{
		this.server = server;
	}
	
	public void start(Model accumulateResultModel, Context qContext) {
		this.resultModel = accumulateResultModel;
		this.resultModel.setNsPrefix("rdfs", RDFS.getURI());
	}
	
	public void describe(Resource resource) {
		try {
		Model description = ModelFactory.createDefaultModel();
		Model seeAlsos = ModelFactory.createDefaultModel();
		Model dataModel = server.dataset().getDefaultModel();
		description.add(dataModel.listStatements(resource, null, (RDFNode) null));

		if (!server.isVocabularyResource(resource) || server.getConfig().getVocabularyIncludeInstances()) {
			description.add(dataModel.listStatements(null, null, resource));
			description.add(dataModel.listStatements(null, (Property) resource.as(Property.class), (RDFNode) null));
		}
		ResIterator rit = description.listSubjects();
		while (rit.hasNext()) {
			addSeeAlsoStatement(rit.nextResource(), seeAlsos, resource.getURI());
		}
		rit.close();
		NodeIterator nit = description.listObjects();
		while (nit.hasNext()) {
			addSeeAlsoStatement(nit.nextNode(), seeAlsos, resource.getURI());
		}
		nit.close();
		resultModel.add(description);
		resultModel.add(seeAlsos);
		} catch (RuntimeException ex) {
			ex.printStackTrace(System.out);
			throw ex;
		}
	}

	public void finish() {
		// do nothing
	}

	private void addSeeAlsoStatement(RDFNode n, Model m, String currentResourceURI) {
		if (!n.isURIResource()) {
			return;
		}
		String resourceURI = n.asNode().getURI();
		if (currentResourceURI.equals(resourceURI)) {
			// Don't add seeAlso for the current resource pointing to its own description 
			return;
		}
		if (resourceURI.startsWith(server.baseURI())) {
			// Don't add seeAlso for dereferenceable URIs
			return;
		}
		String seeAlsoURI = server.graphURLDescribingResource(resourceURI);
		Resource nAsResource = m.getResource(resourceURI);
		nAsResource.addProperty(RDFS.seeAlso, m.createResource(seeAlsoURI));
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