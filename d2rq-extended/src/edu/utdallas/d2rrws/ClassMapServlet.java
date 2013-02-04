package edu.utdallas.d2rrws;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.fuberlin.wiwiss.d2rs.ModelResponse;
import edu.utdallas.d2rqrw.GraphD2RQRW;

public class ClassMapServlet extends HttpServlet 
{
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		D2RRWServer server = D2RRWServer.fromServletContext(getServletContext());
		server.checkMappingFileChanged();
		if (request.getPathInfo() == null) 
		{
			new ModelResponse(classMapListModel(), request, response).serve();
			return;
		}
		String classMapName = request.getPathInfo().substring(1);
		Model resourceList = graphD2RQRW().classMapInventory(classMapName);
		if (resourceList == null) 
		{
			response.sendError(404, "Sorry, class map '" + classMapName + "' not found.");
			return;
		}
    	Resource classMap = resourceList.getResource(server.baseURI() + "all/" + classMapName);
    	Resource directory = resourceList.createResource(server.baseURI() + "all");
    	classMap.addProperty(RDFS.seeAlso, directory);
    	classMap.addProperty(RDFS.label, "List of all instances: " + classMapName);
    	directory.addProperty(RDFS.label, "D2R Server contents");
    	server.addDocumentMetadata(resourceList, classMap);
		new ModelResponse(resourceList, request, response).serve();
	}

	private GraphD2RQRW graphD2RQRW() 
	{
		return (GraphD2RQRW) D2RRWServer.fromServletContext(getServletContext()).currentGraph();
	}
	
	@SuppressWarnings("unchecked")
	private Model classMapListModel() 
	{
		D2RRWServer server = D2RRWServer.fromServletContext(getServletContext());
		Model result = ModelFactory.createDefaultModel();
		Resource list = result.createResource(server.baseURI() + "all");
		list.addProperty(RDFS.label, "D2R Server contents");
		Iterator it = graphD2RQRW().classMapNames().iterator();
		while (it.hasNext()) 
		{
			String classMapName = (String) it.next();
			Resource instances = result.createResource(server.baseURI() + "all/" + classMapName);
			list.addProperty(RDFS.seeAlso, instances);
			instances.addProperty(RDFS.label, "List of all instances: " + classMapName);
		}
		server.addDocumentMetadata(result, list);
		return result;
	}
	
	private static final long serialVersionUID = 6467361762380096163L;
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