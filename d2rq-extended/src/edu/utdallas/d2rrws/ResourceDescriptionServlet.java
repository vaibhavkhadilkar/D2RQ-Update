package edu.utdallas.d2rrws;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.fuberlin.wiwiss.d2rs.ModelResponse;
import de.fuberlin.wiwiss.d2rs.RequestParamHandler;
import de.fuberlin.wiwiss.d2rs.vocab.FOAF;
import edu.utdallas.d2rqrw.jena.query.D2RQRWQueryExecutionFactory;

public class ResourceDescriptionServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		D2RRWServer server = D2RRWServer.fromServletContext(getServletContext());
		server.checkMappingFileChanged();
		String relativeResourceURI = request.getRequestURI().substring(
				request.getContextPath().length() + request.getServletPath().length());
		// Some servlet containers keep the leading slash, some don't
		if (!"".equals(relativeResourceURI) && "/".equals(relativeResourceURI.substring(0, 1))) {
			relativeResourceURI = relativeResourceURI.substring(1);
		}
		if (request.getQueryString() != null) {
			relativeResourceURI = relativeResourceURI + "?" + request.getQueryString();
		}
		
		/* Determine service stem, i.e. vocab/ in /[vocab/]data */
		int servicePos;
		if (-1 == (servicePos = request.getServletPath().indexOf("/" + D2RRWServer.getDataServiceName())))
				throw new ServletException("Expected to find service path /" + D2RRWServer.getDataServiceName());
		String serviceStem = request.getServletPath().substring(1, servicePos + 1);		
				
		String resourceURI = RequestParamHandler.removeOutputRequestParam(
				server.resourceBaseURI(serviceStem) + relativeResourceURI);
		String documentURL = server.dataURL(serviceStem, relativeResourceURI);

		//Model description = D2RQRWQueryExecutionFactory.create( "DESCRIBE <" + resourceURI + ">", server.dataset()).execDescribe();
		Model description = ModelFactory.createDefaultModel();
		ResultSet iter = D2RQRWQueryExecutionFactory.create( 
				" PREFIX uri: <" + resourceURI.substring( 0, resourceURI.lastIndexOf( '/' ) + 1 ) + "> " +
				" SELECT ?y ?z WHERE { uri:" + resourceURI.substring( resourceURI.lastIndexOf( '/' ) + 1 ) + " ?y ?z }", 
				server.dataset().getD2RQRWModel()).execSelect();
		
		while( iter.hasNext() )
		{
			QuerySolution qs = iter.nextSolution(); 
			description.add( description.createResource( resourceURI ), description.createProperty( qs.getResource( "?y" ).toString() ), qs.get( "?z" ));
		}

		if (description.size() == 0) {
			response.sendError(404);
		}
		if (description.qnameFor(FOAF.primaryTopic.getURI()) == null
				&& description.getNsPrefixURI("foaf") == null) {
			description.setNsPrefix("foaf", FOAF.NS);
		}
		Resource resource = description.getResource(resourceURI);
		Resource document = description.getResource(documentURL);
		document.addProperty(FOAF.primaryTopic, resource);
		Statement label = resource.getProperty(RDFS.label);
		if (label != null) {
			document.addProperty(RDFS.label, "RDF Description of " + label.getString());
		}
		server.addDocumentMetadata(description, document);
// TODO: Add a Content-Location header
		new ModelResponse(description, request, response).serve();
//		Resource resource = description.getResource(resourceURI);
	}

	private static final long serialVersionUID = -4898674928803998210L;
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