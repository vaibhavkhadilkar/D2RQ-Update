package edu.utdallas.d2rqrw;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.enhanced.BuiltinPersonalities;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.IteratorFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.fuberlin.wiwiss.d2rq.map.Mapping;
import de.fuberlin.wiwiss.d2rq.vocab.D2RQ;
import edu.utdallas.d2rqrw.rdb.RDBModelForD2RQRW;
import edu.utdallas.d2rqrw.util.TripleUtils;
import edu.utdallas.jena.rdf.model.impl.ExtendedIteratorFactory;

/**
 * <p>A class that implements a D2RQ++ model</p> 
 */
public class ModelD2RQRW extends ModelCom implements Model
{	
	/** <p>The threshold number of triples after which triples from the RDB model are attempted to be consolidated 
	 *  with the relational database</p> **/
	public static final long rdbThreshold = 10L;
	
	/** <p>A Jena RDB model</p> **/
	private Model rdbModel = null;
	
	/** <p>The base URI</p>**/
	private String baseURI = null;

	/**	<p>A boolean that checks if the RDB model is created</p> **/
	private boolean isRDBModelCreated = false;

	/**	<p> Instance that creates a Jena RDB Model when required </p> **/
	private RDBModelForD2RQRW rdbModelForD2RQRW = new RDBModelForD2RQRW();

	/**
	 * <p>Constructor</p>
	 * @param mapURL - the given input URL for the map file
	 *  
	 * @see de.fuberlin.wiwiss.d2rq.ModelD2RQ#ModelD2RQ(String) 
	 */
	public ModelD2RQRW( String mapURL ) 
	{ this( ( FileManager.get().loadModel( mapURL ) ), mapURL + "#" ); }

	/**
	 * <p>Constructor</p>
	 * @param mapURL - the given input URL for the map file
	 * @param serializationFormat - the input format for the map file e.g. N3, RDF/XML etc
	 * @param baseURIForData - the base URI to be used, a default is generated if none is provided
	 *  
	 * @see de.fuberlin.wiwiss.d2rq.ModelD2RQ#ModelD2RQ(String, String, String) 
	 */
	public ModelD2RQRW( String mapURL, String serializationFormat, String baseURIForData ) 
	{ this( ( FileManager.get().loadModel( mapURL, serializationFormat ) ), ( baseURIForData == null ) ? mapURL + "#" : baseURIForData ); }

	/**
	 * <p>Constructor</p>
	 * @param mapModel - the D2RQ++ mapping as a Jena model
	 * @param baseURIForData - the base URI to be used, a default is generated if none is provided
	 *  
	 * @see de.fuberlin.wiwiss.d2rq.ModelD2RQ#ModelD2RQ(Model, String) 
	 */
	public ModelD2RQRW( Model mapModel, String baseURIForData ) 
	{ super( new GraphD2RQRW( mapModel, baseURIForData ), BuiltinPersonalities.model ); this.baseURI = baseURIForData; }

	/** 
	 * <p>Constructor</p>
	 * @param mapping - a mapping object
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.ModelD2RQ#ModelD2RQ(Mapping) 
	 */
	public ModelD2RQRW( Mapping mapping ) 
	{ super( new GraphD2RQRW( mapping ), BuiltinPersonalities.model ); }

	/**
	 * <p>Method that returns if the RDB model is created</p>
	 * @return true, iff the variable is set, false otherwise
	 */
	public boolean getIsRDBModelCreated()
	{ return isRDBModelCreated; }
	
	/**
	 * <p>Method that returns the RDB model if it exists
	 * @return the RDB model
	 */
	public Model getRDBModel()
	{ if( isRDBModelCreated ) return rdbModel; else return null; }
	
	/**
	 * <p>Method that adds a triple by adding a value to a new cell in the relational database or 
	 * by adding a triple to the RDB model</p>
	 */
	private Model performAdd( Resource s, Property p, RDFNode o )
	{
		if( !isRDBModelCreated )
		{ 
			isRDBModelCreated = true; 
			rdbModel = rdbModelForD2RQRW.getRDBModel( ( (GraphD2RQRW)graph ).getMapOntModel().getDatabaseUrl(), 
													  ( (GraphD2RQRW)graph ).getMapOntModel().getDatabaseUser(),
													  ( (GraphD2RQRW)graph ).getMapOntModel().getDatabasePassword(),
													  ( (GraphD2RQRW)graph ).getMapOntModel().getDatabaseType(), true ); 
		}
		else
		{
			if( rdbModel.size() % ModelD2RQRW.rdbThreshold == 0 )
			{
				RDBToRelationalConsolidator consolidator = new RDBToRelationalConsolidator( rdbModel, this );
				consolidator.run();
			}
		}

		boolean doesInputPredExist = checkForInputPredicate( s, p, o );
		
		ExtendedIterator<Triple> subIter = graph.find( s.asNode(), Node.ANY, Node.ANY );
		if( !subIter.hasNext() )
		{
			ExtendedIterator<Triple> predIter = graph.find( Node.ANY, p.asNode(), Node.ANY );
			if( predIter.hasNext() )
			{
				GraphD2RQRW.isInsert = true;
				graph.add( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
				GraphD2RQRW.isInsert = false;
			}
			else
			{
				if( doesInputPredExist )
				{
					GraphD2RQRW.isInsert = true;
					graph.add( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
					GraphD2RQRW.isInsert = false;
				}
				else
					rdbModel.add( s, p, o );
			}
			predIter.close(); predIter = null;
		}
		else
		{
			ExtendedIterator<Triple> predIter = graph.find( Node.ANY, p.asNode(), Node.ANY );
			if( !predIter.hasNext() ) 
			{
				if( doesInputPredExist )
				{
					GraphD2RQRW.isInsert = true;
					graph.add( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
					GraphD2RQRW.isInsert = false;
				}
				else
					rdbModel.add( s, p, o );
			}
			else
			{
				String blankNodeColumnString = null; String[] blankNodeColumns;
				if( ( blankNodeColumnString = ( (GraphD2RQRW) graph ).getBlankNodeColumns( p.getLocalName() ) ) != null ) 
				{
					String subString = s.toString(), predString = p.toString();
					int indexOfPredURI = predString.lastIndexOf( '/' ) + 1;
					blankNodeColumns = blankNodeColumnString.split( " " );
					String[] blankNodeObjects = o.toString().split( "@@" )[1].split( "/" );
					for( int i = 0; i < blankNodeColumns.length; i++ )
					{
						String prop = predString.substring( 0, indexOfPredURI ) + blankNodeColumns[i];
						ExtendedIterator<Triple> bkColumnIter = graph.find( s.asNode(), Node.createURI( prop ), Node.ANY );
						if( bkColumnIter.hasNext() ) { rdbModel.add( s, p, o ); return this;	}
						
						String tableName = predString.substring( indexOfPredURI, predString.lastIndexOf( '_' ) );
						if( tableName.equalsIgnoreCase( blankNodeColumns[i].substring( 0, blankNodeColumns[i].indexOf( '_' ) ) ) ) continue;

						String sub = subString.substring( 0, subString.lastIndexOf( '#' ) + 1 ) + blankNodeColumns[i].substring( 0, blankNodeColumns[i].indexOf( '_' ) ) + "/" + blankNodeObjects[i];
						ExtendedIterator<Triple> bkObjectIter = graph.find( Node.createURI( sub ), Node.ANY, Node.ANY );
						if( !bkObjectIter.hasNext() ) { rdbModel.add( s, p, o ); return this; }
					}
				}

				ExtendedIterator<Triple> objFKIter = graph.find( o.asNode(), Node.ANY, Node.ANY );
				if( objFKIter.hasNext() || o.isLiteral() || o.isAnon() )
				{
					ExtendedIterator<Triple> objIter = graph.find( s.asNode(), p.asNode(), Node.ANY );
					if( objIter.hasNext() ) rdbModel.add( s, p, o );
					else graph.add( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
					objIter.close(); objIter = null;
				}
				else
					rdbModel.add( s, p, o );
				objFKIter.close(); objFKIter = null;
			}
			predIter.close(); predIter = null;
		}
		subIter.close(); subIter = null;
		return this;
	}

	/**
	 * Method that determines if a given property is a column of any table in the relational database
	 * @param s - the input resource
	 * @param p - the input property
	 * @param o - the input rdf node
	 * @return true, iff the property is a column in the relational database for the input resource's table, false otherwise
	 */
	private boolean checkForInputPredicate( Resource s, Property p, RDFNode o  )
	{
		boolean doesInputPredExist = false; 
		
		String tablename = TripleUtils.getTableName( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
		String columnname = TripleUtils.getColumnName( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
		
		OntModel ontModel = ( (GraphD2RQRW)graph ).getMapOntModel().getOntModel();
		ExtendedIterator<Individual> it = ontModel.listIndividuals( D2RQ.ClassMap );
		while ( it.hasNext() ) 
		{
			Resource className = (Resource) it.next();
			if ( className.getLocalName().equalsIgnoreCase( tablename ) )
			{
				StmtIterator predIter = ontModel.listStatements( null, D2RQ.belongsToClassMap, (RDFNode) className );
				while ( predIter.hasNext() )
				{
					Resource predResource = predIter.nextStatement().getSubject();
					if ( predResource.getLocalName().equalsIgnoreCase( columnname ) )
					{
						doesInputPredExist = true;
						break;
					}
				}
			}
		}
		return doesInputPredExist;
	}
	
	/**
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#add(Resource, Property, RDFNode)}
	 * This method adds a triple to the relational database as a new value for a cell or to the RDB model</p>
	 */
	public Model add( Resource s, Property p, RDFNode o )  
	{
		modelReifier.noteIfReified( s, p, o );
		return performAdd( s, p, o );
	}

	/**
	 * <p>Method that removes a triple by removing a value from a cell in the relational database or 
	 * by removing the triple from the RDB model</p>
	 */
	private Model performRemove( Resource s, Property p, RDFNode o )
	{
		rdbModel = 	rdbModelForD2RQRW.getRDBModel( ( (GraphD2RQRW)graph ).getMapOntModel().getDatabaseUrl(), 
												   ( (GraphD2RQRW)graph ).getMapOntModel().getDatabaseUser(),
												   ( (GraphD2RQRW)graph ).getMapOntModel().getDatabasePassword(),
												   ( (GraphD2RQRW)graph ).getMapOntModel().getDatabaseType(), false );
		if (rdbModel != null) isRDBModelCreated = true;

		ExtendedIterator<Triple> subIter = graph.find( s.asNode(), Node.ANY, Node.ANY );
		if( !subIter.hasNext() )
		{ if( rdbModel != null ) rdbModel.getGraph().delete( Triple.create( s.asNode(), p.asNode(), o.asNode() ) ); }
		else
		{			
			ExtendedIterator<Triple> predIter = graph.find( Node.ANY, p.asNode(), Node.ANY );
			if( !predIter.hasNext() )
			{ if( rdbModel != null ) rdbModel.getGraph().delete( Triple.create( s.asNode(), p.asNode(), o.asNode() ) ); }
			else
			{
				ExtendedIterator<Triple> subFKIter = graph.find( Node.ANY, Node.ANY, s.asNode() );
				if( subFKIter.hasNext() ) throw new DeleteDeniedException( "ModelD2RQRW::remove" );
				else
				{
					//ExtendedIterator<Triple> objIter = graph.find( s.asNode(), p.asNode(), Node.ANY );
					ExtendedIterator<Triple> objIter = graph.find( s.asNode(), p.asNode(), o.asNode() );
					if( objIter.hasNext() ) graph.delete( Triple.create( s.asNode(), p.asNode(), o.asNode() ) );
					else { if( rdbModel != null ) rdbModel.getGraph().delete( Triple.create( s.asNode(), p.asNode(), o.asNode() ) ); }
					objIter.close(); objIter = null;
				}
				subFKIter.close(); subFKIter = null;
			}
			predIter.close(); predIter = null;
		}
		subIter.close(); subIter = null;
		return this;
	}

	/**
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#remove(Resource, Property, RDFNode)}
	 * This method removes a triple from the relational database as an existing value in a cell or from the RDB model</p>
	 */
	public Model remove( Resource s, Property p, RDFNode o ) 
	{ return performRemove( s, p, o ); }

	/**
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#listSubjects()}
	 * Method that lists all subjects from the relational database as well as the RDB model</p>
	 */
	public ResIterator listSubjects()
	{
		if( rdbModel != null ) return ExtendedIteratorFactory.asResIterator( ( super.listSubjects().andThen( rdbModel.listSubjects() ) ), this );
		else return super.listSubjects();    	    	
	}

	/** 
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#listSubjectsWithProperty(Property)}
	 * Method that lists subjects that have a specific property from the relational database as well as the RDB model</p>
	 */
	public ResIterator listSubjectsWithProperty( Property p )
	{ return listResourcesWithProperty( p ); }

	/**
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#listResourcesWithProperty(Property)}
	 * Method that lists resources that have a specific property from the relational database as well as the RDB model</p>
	 */
	public ResIterator listResourcesWithProperty( Property p )
	{ 
		if( rdbModel != null ) return ExtendedIteratorFactory.asResIterator( ( super.listResourcesWithProperty( p ).andThen( rdbModel.listResourcesWithProperty( p ) ) ), this );
		else return super.listResourcesWithProperty( p );    	
	}

	/**
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#listResourcesWithProperty(Property, RDFNode)}
	 * Method that lists resources that have a specific property and object from the relational database as well as the RDB model</p> 
	 */
	public ResIterator listResourcesWithProperty( Property p, RDFNode o )
	{ 
		if( rdbModel != null ) return ExtendedIteratorFactory.asResIterator( ( super.listResourcesWithProperty( p, o ).andThen( rdbModel.listResourcesWithProperty( p, o ) ) ), this );
		else return super.listResourcesWithProperty( p, o );    	    	
	}

	/**
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#listObjects()}
	 * Method that lists all subjects from the relational database as well as the RDB model</p>
	 */
	public NodeIterator listObjects()
	{
		if( rdbModel != null ) return ExtendedIteratorFactory.asRDFNodeIterator( ( super.listObjects().andThen( rdbModel.listObjects() ) ), this );
		else return super.listObjects();
	}

	/**
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#listObjectsOfProperty(Property)}
	 * Method that lists objects that have a specific property from the relational database as well as the RDB model</p>
	 */
	public NodeIterator listObjectsOfProperty( Property p )
	{
		if( rdbModel != null ) return ExtendedIteratorFactory.asRDFNodeIterator( ( super.listObjectsOfProperty( p ).andThen( rdbModel.listObjectsOfProperty( p ) ) ), this );
		else return super.listObjectsOfProperty( p );
	}

	/**
	 * <p>Method that overrides the {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#listObjectsOfProperty(Resource, Property)}
	 * Method that lists objects that have a specific resource and property from the relational database as well as the RDB model</p>
	 */
	public NodeIterator listObjectsOfProperty( Resource s, Property p )
	{
		if( rdbModel != null ) return ExtendedIteratorFactory.asRDFNodeIterator( ( super.listObjectsOfProperty( s, p ).andThen( rdbModel.listObjectsOfProperty( s, p ) ) ), this );
		else return super.listObjectsOfProperty( s, p );    	
	}

	/**
	 * <p>Method that overrides {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#listStatements()}
	 * This allows us to iterate over the relational database and then Jena's RDB model</p>
	 */
	public StmtIterator listStatements()  
	{ 
		List<Triple> d2rqList = new ArrayList<Triple>();
		ExtendedIterator<Triple> d2rqIter =  graph.find( Node.ANY, Node.ANY, Node.ANY );
		while( d2rqIter.hasNext() ) 
		{ 
			Triple t = d2rqIter.next();
			String blankNodeColumnString = null; String[] blankNodeColumns;
			if( ( blankNodeColumnString = ( (GraphD2RQRW) graph ).getBlankNodeColumns( t.getPredicate().getLocalName() ) ) != null ) 
			{
				Node s = t.getSubject(), p = t.getPredicate(), o = t.getObject();
				blankNodeColumns = blankNodeColumnString.split( " " );
				String[] blankNodeObjects = o.getLiteralValue().toString().split( "/" );
				String blankNode = baseURI + p.getLocalName() + "@@" + o.getLiteralValue().toString();
				d2rqList.add( Triple.create( s, p, Node.createAnon( AnonId.create( blankNode ) ) ) );
				
				//TODO need to see how to identify the column type and create the object based on that type
				for( int i = 0; i < blankNodeColumns.length; i++ )
					d2rqList.add( Triple.create( Node.createAnon( AnonId.create( blankNode ) ), 
							                     Node.createURI( p.toString().substring( 0, ( p.toString().lastIndexOf( '/' ) + 1 ) ) + blankNodeColumns[i] ) , 
							                     Node.createLiteral( blankNodeObjects[blankNodeObjects.length - 1 - i] ) ) );
			}
			else
				d2rqList.add( t );
		}
		if( rdbModel != null ) return IteratorFactory.asStmtIterator( rdbModel.getGraph().find( Node.ANY, Node.ANY, Node.ANY).andThen( d2rqList.iterator() ), this );
		else return IteratorFactory.asStmtIterator( d2rqList.iterator(), this );
	}

	/**
	 * <p>Method that overrides {@link com.hp.hpl.jena.rdf.model.impl.ModelCom#contains(Resource, Property, RDFNode)}
	 * This allows us to check for a triple in the relational database or the RDB model</p>
	 */
	public boolean contains( Resource s, Property p, RDFNode o )
	{
		if( rdbModel != null ) return super.contains( s, p, o ) || rdbModel.contains( s, p, o ); 
		else return super.contains( s, p, o );
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