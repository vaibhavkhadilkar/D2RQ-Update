package edu.utdallas.d2rqrw;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * <p>A class that implements the flush algorithm that periodically checks if triples from the
 * RDB model can be added to the relational database</p>
 */
public class RDBToRelationalConsolidator
{
	/** <p>The RDB model being used</p> **/
	private Model rdbModel = null;
	
	/** <p>The D2RQ++ model being used</p> **/
	private ModelD2RQRW d2rqrwModel = null;
	
	/**
	 * <p>Constructor</p>
	 * @param rdbModel - the RDB model being used
	 * @param d2rqrwModel - the D2RQ++ model being used
	 */
	public RDBToRelationalConsolidator( Model rdbModel, ModelD2RQRW d2rqrwModel )
	{
		this.rdbModel = rdbModel; this.d2rqrwModel = d2rqrwModel;
	}
	
	/**
	 * <p>Method to flush the triples from the RDB model to the relational database</p>
	 */
	public void run()
	{
		//List of statements that can be removed
		List<Statement> stmtList = new ArrayList<Statement>();
		
		//Get the graph for the D2RQ-RW model
		Graph graph = d2rqrwModel.getGraph();
		
		//Iterate over all triples in the RDB model
		StmtIterator rdbIter = rdbModel.listStatements();
		while( rdbIter.hasNext() )
		{
			//Get each triple from the RDB model
			Statement stmt = rdbIter.nextStatement();
			
			//Get the corresponding subject, predicate and object
			Node sub = stmt.getSubject().asNode();
			Node pred = stmt.getPredicate().asNode();
			Node obj = stmt.getObject().asNode();
			
			//Foreign key constraint
			if( obj.isURI() )
			{
				ExtendedIterator<Triple> fkObjIter = d2rqrwModel.getGraph().find( obj, Node.ANY, Node.ANY );
				if( fkObjIter.hasNext() )
				{
					ExtendedIterator<Triple> objIter = d2rqrwModel.getGraph().find( sub, pred, obj );
					if( !objIter.hasNext() )
					{
						graph.add( Triple.create( sub, pred, obj ) );
						stmtList.add( stmt );
					}
				}
			}
			else 
				if( obj.isLiteral() )
				{
					ExtendedIterator<Triple> objIter = d2rqrwModel.getGraph().find( sub, pred, obj );
					if( !objIter.hasNext() )
					{
						graph.add( Triple.create( sub, pred, obj ) );	
						stmtList.add( stmt );
					}
				}
				else
					if( obj.isBlank() )
					{
						String blankNodeColumnString = null; String[] blankNodeColumns;
						if( ( blankNodeColumnString = ( (GraphD2RQRW) graph ).getBlankNodeColumns( pred.getLocalName() ) ) != null ) 
						{
							String subString = sub.toString(), predString = pred.toString();
							int indexOfPredURI = predString.lastIndexOf( '/' ) + 1;
							blankNodeColumns = blankNodeColumnString.split( " " );
							String[] blankNodeObjects = obj.toString().split( "@@" )[1].split( "/" );
							String[] blankNodeObjectType = new String[blankNodeObjects.length];
							int i = 0;
							for( i = 0; i < blankNodeColumns.length; i++ )
							{
								String prop = predString.substring( 0, indexOfPredURI ) + blankNodeColumns[i];
								ExtendedIterator<Triple> bkColumnIter = graph.find( sub, Node.createURI( prop ), Node.ANY );
								if( bkColumnIter.hasNext() ) { blankNodeObjectType[i] = "literal"; break; }

								String tableName = predString.substring( indexOfPredURI, predString.lastIndexOf( '_' ) );
								if( tableName.equalsIgnoreCase( blankNodeColumns[i].substring( 0, blankNodeColumns[i].indexOf( '_' ) ) ) ) continue;

								String subject = subString.substring( 0, subString.lastIndexOf( '#' ) + 1 ) + blankNodeColumns[i].substring( 0, blankNodeColumns[i].indexOf( '_' ) ) + "/" + blankNodeObjects[i];
								ExtendedIterator<Triple> bkObjectIter = graph.find( Node.createURI( subject ), Node.ANY, Node.ANY );
								if( !bkObjectIter.hasNext() ) { blankNodeObjectType[i] = "uri"; break; }
							}
							if( i == blankNodeColumns.length )
							{
								for( i = 0; i < blankNodeColumns.length; i++ )
								{
									String prop = predString.substring( 0, indexOfPredURI ) + blankNodeColumns[i];
									Node object = null;
									if( blankNodeObjectType[i].equalsIgnoreCase( "literal" ) )
										object = Node.createLiteral( blankNodeObjects[i] );
									else
										if( blankNodeObjectType[i].equalsIgnoreCase( "uri" ) )
											object = Node.createURI( blankNodeObjects[i] );
									
									graph.add( Triple.create( sub, Node.createURI( prop ), object ) );										
									stmtList.add( stmt );
								}
							}
						}
					}
		}
		
		//Remove statements from the RDB model if any have been moved to the relational database
		if( stmtList.size() > 0 ) rdbModel.remove( stmtList );
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