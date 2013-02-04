package edu.utdallas.d2rrws;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.fuberlin.wiwiss.d2rs.VelocityWrapper;
import edu.utdallas.d2rqrw.GraphD2RQRW;
import edu.utdallas.d2rqrw.util.DbParameterMaps;

public class DirectoryServlet extends HttpServlet 
{
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		D2RRWServer server = D2RRWServer.fromServletContext(getServletContext());
		server.checkMappingFileChanged();

		String classMapName = request.getPathInfo().substring(1);

		PrefixMapping prefixes = server.getPrefixes();
		
		//Add a triple
		String addRequest = request.getParameter( "group2" );
		if( addRequest != null )
		{
			String[] splitStr = addRequest.split( "~~" );
			
			Model temp = ModelFactory.createDefaultModel();
			
			String[] splitSub = splitStr[0].split( ":" );
			Resource sub = null;
			if( prefixes.getNsPrefixURI( splitSub[0] ) == null )
				sub = temp.createResource( splitStr[0] );
			else
				sub = temp.createResource( prefixes.getNsPrefixURI( splitSub[0] ) + splitSub[1] );

			//TODO: Right now assumes only one primary key column
			String vocab = prefixes.getNsPrefixURI( "vocab" );
			String[] pk = graphD2RQRW().getMapOntModel().getPrimaryKeyColumns( classMapName );
			String datatype = graphD2RQRW().getMapOntModel().getColumnDatatype( classMapName, classMapName + "_" + pk[0].split( "\\." )[1] );
			Property pred = temp.createProperty( vocab + classMapName + "_" + pk[0].split( "\\." )[1] );
			
			RDFNode obj = null;
			if( splitStr[1].contains( "@@" ) )
				obj = temp.asRDFNode( Node.createAnon( AnonId.create( splitStr[1] ) ) );
			else
			{
				String[] splitObj1 = splitStr[1].split( "\\^\\^" ); 
				
				obj = temp.asRDFNode( Node.createLiteral( splitObj1[0], null, DbParameterMaps.xsdDatatypeMap.get( datatype )) );
			}
			server.dataset().getD2RQRWModel().add( sub, pred, obj );
		}
		
		//Delete a triple
		String deleteRequest = request.getParameter( "group1" );
		if( deleteRequest != null )
		{
			String map = prefixes.getNsPrefixURI( "map" );
			String res = deleteRequest.substring( map.length() );
			
			Model temp = ModelFactory.createDefaultModel();
			Resource s = temp.createResource( deleteRequest );
			
			//TODO: Right now assumes only one primary key column
			String pkWhereClause = graphD2RQRW().getMapOntModel().getPKForWhereClause( map + res, classMapName );
			String[] propAndObj = pkWhereClause.split( "=" );
			
			Property p = temp.createProperty( prefixes.getNsPrefixURI( "vocab" ) + classMapName + "_" + propAndObj[0].trim() );
			
			String datatype = graphD2RQRW().getMapOntModel().getColumnDatatype( classMapName, classMapName + "_" + propAndObj[0].trim() );
			
			RDFNode o = temp.asRDFNode( Node.createLiteral( propAndObj[1].trim(), null, DbParameterMaps.xsdDatatypeMap.get( datatype ) ) );
			server.dataset().getD2RQRWModel().remove( s, p, o );
		}
		
		Model resourceList = graphD2RQRW().classMapInventory(classMapName);
		if (resourceList == null) { response.sendError(404, "Sorry, class map '" + classMapName + "' not found."); return; }
		if (request.getPathInfo() == null) { response.sendError(404); return; }
		Map<String,String> resources = new TreeMap<String,String>();
		ResIterator subjects = resourceList.listSubjects();
		while (subjects.hasNext()) 
		{
			Resource resource = subjects.nextResource();
			if (!resource.isURIResource()) { continue; }
			String uri = resource.getURI();
			Statement labelStmt = resource.getProperty(RDFS.label);
			String label = (labelStmt == null) ? resource.getURI() : labelStmt.getString();
			resources.put(uri, label);
		}
		Map<String,String> classMapLinks = new TreeMap<String,String>();
		Iterator it = graphD2RQRW().classMapNames().iterator();
		while (it.hasNext()) 
		{
			String name = (String) it.next();
			classMapLinks.put(name, server.baseURI() + "directory/" + name);
		}
		VelocityWrapper velocity = new VelocityWrapper(this, request, response);
		Context context = velocity.getContext();
		context.put("prefixes", prefixes.getNsPrefixMap() );
		context.put("rdf_link", server.baseURI() + "all/" + classMapName);
		context.put("classmap", classMapName);
		context.put("classmap_links", classMapLinks);
		context.put("resources", resources);
		velocity.mergeTemplateXHTML("directory_page.vm");
	}

	private GraphD2RQRW graphD2RQRW() 
	{ return (GraphD2RQRW) D2RRWServer.fromServletContext(getServletContext()).currentGraph(); }

	private static final long serialVersionUID = 8398973058486421941L;
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