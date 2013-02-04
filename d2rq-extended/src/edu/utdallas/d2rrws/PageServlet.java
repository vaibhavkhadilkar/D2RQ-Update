package edu.utdallas.d2rrws;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.fuberlin.wiwiss.d2rq.map.PropertyBridge;
import de.fuberlin.wiwiss.d2rs.VelocityWrapper;
import edu.utdallas.d2rqrw.GraphD2RQRW;
import edu.utdallas.d2rqrw.jena.query.D2RQRWQueryExecutionFactory;

public class PageServlet extends HttpServlet 
{
	private PrefixMapping prefixes;
	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
	{
		D2RRWServer server = D2RRWServer.fromServletContext(getServletContext());
		server.checkMappingFileChanged();
		this.prefixes = server.getPrefixes(); // model();

		String relativeResourceURI = request.getRequestURI().substring(request.getContextPath().length() + request.getServletPath().length());
		
		// Some servlet containers keep the leading slash, some don't
		if (!"".equals(relativeResourceURI) && "/".equals(relativeResourceURI.substring(0, 1)))
			relativeResourceURI = relativeResourceURI.substring(1);
		if (request.getQueryString() != null)
			relativeResourceURI = relativeResourceURI + "?" + request.getQueryString();

		/* Determine service stem, i.e. vocab/ in /[vocab/]page */
		int servicePos;
		if (-1 == (servicePos = request.getServletPath().indexOf("/" + D2RRWServer.getPageServiceName())))
				throw new ServletException("Expected to find service path /" + D2RRWServer.getPageServiceName());
		String serviceStem = request.getServletPath().substring(1, servicePos + 1);		
		
		String resourceURI = server.resourceBaseURI(serviceStem) + relativeResourceURI;

		//Create a result model
		Model description = ModelFactory.createDefaultModel();

		//Add a triple
		String addRequest = request.getParameter( "group2" );
		if( addRequest != null )
		{
			String[] splitStr = addRequest.split( "~~" );
			resourceURI = resourceURI.substring( 0, resourceURI.indexOf( "?" ) );
			Resource sub = description.createResource( resourceURI );
			
			String[] splitProp = splitStr[0].split( ":" ); 
			com.hp.hpl.jena.rdf.model.Property pred = null;
			if( prefixes.getNsPrefixURI( splitProp[0] ) == null )
				pred = description.createProperty( splitStr[0] );
			else
				pred = description.createProperty( prefixes.getNsPrefixURI( splitProp[0] ) + splitProp[1] );
			
			RDFNode obj = null;
			if( splitStr[1].contains( "@@" ) )
				obj = description.asRDFNode( Node.createAnon( AnonId.create( splitStr[1] ) ) );
			else
				obj = description.asRDFNode( Node.createLiteral( splitStr[1] ) );
			server.dataset().getD2RQRWModel().add( sub, pred, obj );
		}
		
		//Delete a triple
		String deleteRequest = request.getParameter( "group1" );
		if( deleteRequest != null )
		{
			resourceURI = resourceURI.substring( 0, resourceURI.indexOf( "?" ) );
			String[] splitStr = deleteRequest.split("~~");
			Resource sub = description.createResource( resourceURI );
			com.hp.hpl.jena.rdf.model.Property pred = description.createProperty( splitStr[0] );
			RDFNode obj = null;
			if( splitStr[2].equalsIgnoreCase( "blank" ) )
				obj = description.asRDFNode( Node.createAnon( AnonId.create( splitStr[1] ) ) );
			else
				if( splitStr[2].equalsIgnoreCase( "uri" ) )
					obj = description.asRDFNode( Node.createURI( splitStr[1] ) );
				else
					if( splitStr[2].equalsIgnoreCase( "literal" ) )
					{
						if( splitStr[3].equalsIgnoreCase( "null" ) && splitStr[4].equalsIgnoreCase( "null" ) )
							obj = description.asRDFNode( Node.createLiteral( splitStr[1] ) );
						else
							if( splitStr[3].equalsIgnoreCase( "null" ) && !splitStr[4].equalsIgnoreCase( "null" ) )
								obj = description.asRDFNode( Node.createLiteral( splitStr[1], splitStr[4], null ) );
							else
								if( !splitStr[3].equalsIgnoreCase( "null" ) && splitStr[4].equalsIgnoreCase( "null" ) )
									obj = description.asRDFNode( Node.createLiteral( splitStr[1], null, Node.getType( splitStr[3] ) ) );
								else
									obj = description.asRDFNode( Node.createLiteral( splitStr[1], splitStr[4], Node.getType( splitStr[3] ) ) );
					}
			server.dataset().getD2RQRWModel().remove( sub, pred, obj );
		}

		
		//TODO: Don't know why DESCRIBE does not get triples from RDB model
		//Model description = D2RQRWQueryExecutionFactory.create( "DESCRIBE <" + resourceURI + ">", server.dataset().getD2RQRWModel()).execDescribe();
		
		//Execute a select query to get all triples for this resource
		ResultSet iter = D2RQRWQueryExecutionFactory.create( 
				" PREFIX uri: <" + resourceURI.substring( 0, resourceURI.lastIndexOf( '/' ) + 1 ) + "> " +
				" SELECT ?y ?z WHERE { uri:" + resourceURI.substring( resourceURI.lastIndexOf( '/' ) + 1 ) + " ?y ?z }", 
				server.dataset().getD2RQRWModel()).execSelect();
		
		while( iter.hasNext() )
		{
			QuerySolution qs = iter.nextSolution(); 
			description.add( description.createResource( resourceURI ), description.createProperty( qs.getResource( "?y" ).toString() ), qs.get( "?z" ) );
		}
		
		//Add the other properties for this resource with blanks
		if( relativeResourceURI.contains( "/" ) )
		{
			Iterator<String> classMapsIter = server.currentGraph().classMapNames().iterator();
			while( classMapsIter.hasNext() )
			{
				String classMap = classMapsIter.next();
				if( classMap.equalsIgnoreCase( relativeResourceURI.substring( 0, relativeResourceURI.lastIndexOf( '/' ) ) ) )
				{
					Iterator<PropertyBridge> propBridges = server.currentGraph().getClassNameFromResourceName( description.createResource( server.getConfig().getMappingURL() + "#" + classMap ) ).propertyBridges().iterator();
					while( propBridges.hasNext() )
					{
						PropertyBridge propBridge = propBridges.next();
						Iterator propIter = propBridge.properties().iterator();
						while( propIter.hasNext() )
						{
							PropertyImpl property = (PropertyImpl) propIter.next();
							if( !description.contains( description.createResource( resourceURI ), property ) )
								description.add( description.createResource( resourceURI ), property, description.asRDFNode( Node.createLiteral("null") ) );
						}
					}
				}
			}
		}
		if (description.size() == 0) { response.sendError(404); return; }
		
		Resource resource = description.getResource(resourceURI);
		VelocityWrapper velocity = new VelocityWrapper(this, request, response);
		Context context = velocity.getContext();
		context.put("prefixes", prefixes.getNsPrefixMap() );
		context.put("uri", resourceURI);
		context.put("rdf_link", server.dataURL(serviceStem, relativeResourceURI));
		context.put("label", resource.getProperty(RDFS.label));
		context.put("properties", collectProperties(description, resource));
		context.put("classmap_links", classmapLinks(resource));
		velocity.mergeTemplateXHTML("resource_page.vm");
	}

	private Collection<Property> collectProperties(Model m, Resource r) 
	{
		Collection<Property> result = new TreeSet<Property>();
		StmtIterator it = r.listProperties();
		while (it.hasNext()) 
		{
			Statement s = it.nextStatement();
			result.add(new Property(s, false));
		}
		it = m.listStatements(null, null, r);
		while (it.hasNext())
			result.add(new Property(it.nextStatement(), true));

		return result;
	}

	@SuppressWarnings("unchecked")
	private Map<String,String> classmapLinks(Resource resource) 
	{
		Map<String,String> result = new HashMap<String,String>();
		D2RRWServer server = D2RRWServer.fromServletContext(getServletContext());
		GraphD2RQRW g = server.currentGraph();
		Iterator<String> it = g.classMapNamesForResource(resource.asNode()).iterator();
		while (it.hasNext()) 
		{
			String name = (String) it.next();
			result.put(name, server.baseURI() + "directory/" + name);
		}
		return result;
	}
	
	private static final long serialVersionUID = 2752377911405801794L;
	
	public class Property implements Comparable<Property> 
	{
		private Node property;
		private Node value;
		private boolean isInverse;
		Property(Statement stmt, boolean isInverse) 
		{
			this.property = stmt.getPredicate().asNode();
			if (isInverse)
				this.value = stmt.getSubject().asNode();
			else
				this.value = stmt.getObject().asNode();

			this.isInverse = isInverse;
		}
		public boolean isInverse() { return this.isInverse; }
		
		public String propertyURI() { return this.property.getURI(); }
		
		public String propertyQName() 
		{
			String qname = prefixes.qnameFor(this.property.getURI());
			if (qname == null) { return "<" + this.property.getURI() + ">"; }
			return qname;
		}
		
		public String propertyPrefix() 
		{
			String qname = propertyQName();
			if (qname.startsWith("<")) { return null; }
			return qname.substring(0, qname.indexOf(":") + 1);
		}
		
		public String propertyLocalName() 
		{
			String qname = propertyQName();
			if (qname.startsWith("<")) { return this.property.getLocalName(); }
			return qname.substring(qname.indexOf(":") + 1);
		}
		
		public String getBlankNodeLabel()
		{
			return this.value.getBlankNodeLabel().split( "@@" )[1];
		}
		
		public Node value() { return this.value; }
		
		public String valueQName() 
		{
			if (!this.value.isURI()) { return null; }
			return prefixes.qnameFor(this.value.getURI());
		}
		
		public String datatypeQName() 
		{
			String qname = prefixes.qnameFor(this.value.getLiteralDatatypeURI());
			if (qname == null) { return "<" + this.value.getLiteralDatatypeURI() + ">"; }
			return qname;
		}
		
		public int compareTo(Property otherObject) 
		{
			if (!(otherObject instanceof Property)) { return 0; }
			Property other = (Property) otherObject;
			String propertyLocalName = this.property.getLocalName();
			String otherLocalName = other.property.getLocalName();
			if (propertyLocalName.compareTo(otherLocalName) != 0)
				return propertyLocalName.compareTo(otherLocalName);
			
			if (this.isInverse != other.isInverse)
				return (this.isInverse) ? -1 : 1;
			
			if (this.value.isURI() || other.value.isURI()) 
			{
				if (!other.value.isURI()) { return 1; }
				if (!this.value.isURI()) { return -1; }
				return this.value.getURI().compareTo(other.value.getURI());
			}
			if (this.value.isBlank() || other.value.isBlank()) 
			{
				if (!other.value.isBlank()) { return -1; }
				if (!this.value.isBlank()) { return 1; }
				return this.value.getBlankNodeLabel().compareTo(other.value.getBlankNodeLabel());
			}
			// TODO Typed literals, language literals
			return this.value.getLiteralLexicalForm().compareTo(other.value.getLiteralLexicalForm());
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