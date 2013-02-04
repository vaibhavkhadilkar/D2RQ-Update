package edu.utdallas.d2rqrw;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import de.fuberlin.wiwiss.d2rq.D2RQException;
import de.fuberlin.wiwiss.d2rq.GraphD2RQ;
import de.fuberlin.wiwiss.d2rq.map.ClassMap;
import de.fuberlin.wiwiss.d2rq.map.Mapping;
import edu.utdallas.d2rqrw.db.DbHandler;
import edu.utdallas.d2rqrw.engine.D2RQRWDatasetGraph;
import edu.utdallas.d2rqrw.ontology.MapAsOntModel;
import edu.utdallas.d2rqrw.parser.D2RQRWMapParser;
import edu.utdallas.d2rqrw.util.TripleUtils;

/**
 * <p>A class that implements a D2RQ++ graph</p>
 */
public class GraphD2RQRW extends GraphD2RQ implements Graph
{	
	/** <p>A boolean that tells the performAdd(Triple) method whether to use a SQL insert or update</p> **/
	protected static boolean isInsert = false;

	/** <p>The ontology model for the mapping file</p> **/
	private MapAsOntModel mapOntModel = null;

	/** <p>The database handler</p> **/
	private DbHandler dbHandler = null;

	/** <p>The D2RQRW parser</p> **/
	private static D2RQRWMapParser parser = null;
	
	/** <p>The D2RQRW dataset graph</p> **/
	private final D2RQRWDatasetGraph dataset = new D2RQRWDatasetGraph(this);

	/**
	 * <p>Constructor</p>
	 * @param mapModel - the D2RQ++ mapping as a Jena model
	 * @param baseURIForData - the base URI to be used, a default is generated if none is provided
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.GraphD2RQ#GraphD2RQ(Model, String)
	 */
	public GraphD2RQRW( Model mapModel, String baseURIForData ) throws D2RQException 
	{
		this( ( parser = new D2RQRWMapParser( mapModel, ( baseURIForData == null ) ? "http://localhost/resource/" : baseURIForData ) ).parse() );
		this.mapOntModel = new MapAsOntModel( parser.getOntModel() );
		this.dbHandler = new DbHandler( mapOntModel.getDatabaseDriver(), mapOntModel.getDatabaseUrl(), mapOntModel.getDatabaseUser(), mapOntModel.getDatabasePassword() );		
	}

	/**
	 * <p>Constructor</p>
	 * @param mapping - a mapping object
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.GraphD2RQ#GraphD2RQ(Mapping)
	 */
	public GraphD2RQRW( Mapping mapping ) throws D2RQException 
	{
		super( mapping );
		//TODO Create the mapOntModel and dbHandler, they will not work as above !!
	}

	/**
	 * <p>Method that returns the class name from the given resource name</p>
	 * @param name - the input resource name
	 * @return a ClassMap that contains the given resource name
	 */
	public ClassMap getClassNameFromResourceName( Resource name ) { return parser.getClassNameFromResourceName(name); }
	
	/**
	 * <p>Method that returns the underlying ontology model</p>
	 * @return a MapAsOntModel object that represents the underlying ontology model
	 */
	public MapAsOntModel getMapOntModel()
	{ 
		if( mapOntModel != null ) return mapOntModel;
		else return null;
	}
		
	/**
	 * <p>Method that returns the underlying query handler for this D2RQ++ graph</p>
	 * @return a QueryHandler representing this D2RQ++ graph
	 * 
	 * @see de.fuberlin.wiwiss.d2rq.GraphD2RQ#queryHandler()
	 */
	public QueryHandler queryHandler() 
	{
		checkOpen();
		return new D2RQRWQueryHandler(this, dataset);
	}
	
	/**
	 * <p>Method that generates the relational database columns corresponding to a given blank node</p>
	 * @param blankNodeColumn - the input blank node column name
	 * @return string representing the mapping between the given blank node and the columns in the relational database
	 */
	public String getBlankNodeColumns( String blankNodeColumn )
	{  return mapOntModel.getBlankNodeFields( blankNodeColumn ); }
	
	/**
	 * <p>Method that deletes a triple from this D2RQ++ graph</p>
	 * @param t - the input triple
	 * 
	 * @see com.hp.hpl.jena.graph.impl.GraphBase#performDelete(Triple)
	 */
	public void performDelete( Triple t )
	{
		String tableName = TripleUtils.getTableName( t );
		String blankNodeFields = mapOntModel.getBlankNodeFields( t.getPredicate().getLocalName() );
		String columnName = null, columnValue = null;
		if( blankNodeFields == null ) { columnName = TripleUtils.getColumnName( t ); TripleUtils.getColumnValue( t, tableName, null, false ); }
		else { String[] columnNamesAndValues = TripleUtils.getColumnNamesAndValues( t, blankNodeFields, tableName, null, false ); columnName = columnNamesAndValues[0]; columnValue = columnNamesAndValues[1]; }

		boolean isDelete = false;
		String[] pkColumns = mapOntModel.getPrimaryKeyColumns( tableName );
		for( int i = 0; i < pkColumns.length; i++ )
		{ if( pkColumns[i].equalsIgnoreCase( tableName + "." + columnName ) ) { isDelete = true; break; } }
		
		if( isDelete )
		{
			dbHandler.executeDelete( dbHandler.getDatabaseConnection(), tableName, mapOntModel.getPKForWhereClause( t.getSubject().toString(), tableName ) );
			//dbHandler.closeDbConnection();			
		}
		else
		{
			dbHandler.executeUpdate( dbHandler.getDatabaseConnection(), tableName, columnName, columnValue, mapOntModel.getPKForWhereClause( t.getSubject().toString(), tableName ) );
			//dbHandler.closeDbConnection();
		}
	}
		
	/**
	 * <p>Method that adds a triple to this D2RQ++ graph</p>
	 * @param t - the input triple
	 * 
	 * @see com.hp.hpl.jena.graph.impl.GraphBase#performAdd(Triple)
	 */
	public void performAdd( Triple t )
	{		
		String tableName = TripleUtils.getTableName( t );
		String blankNodeFields = mapOntModel.getBlankNodeFields( t.getPredicate().getLocalName() );
		String columnName = null, columnValue = null;
		if( blankNodeFields == null ) { columnName = TripleUtils.getColumnName( t ); columnValue = TripleUtils.getColumnValue( t, tableName, mapOntModel.getColumnDatatype( tableName, columnName ), true ); }
		else { String[] columnNamesAndValues = TripleUtils.getColumnNamesAndValues( t, blankNodeFields, tableName, mapOntModel, true ); columnName = columnNamesAndValues[0]; columnValue = columnNamesAndValues[1]; }

		if( isInsert )
		{
			String[] pkForInsert = mapOntModel.getPKForInsert( t.getSubject().toString(), tableName ).split( "~" );
			if( ( tableName + "_" + pkForInsert[0].split( "\\." )[1] ).contains( t.getPredicate().getLocalName().toString() ) )
				dbHandler.executeInsert( dbHandler.getDatabaseConnection(), tableName, "", columnName, "", t.getObject().getLiteral().getValue().toString() );
			else
				dbHandler.executeInsert( dbHandler.getDatabaseConnection(), tableName, pkForInsert[0], columnName, pkForInsert[1], columnValue );
			//dbHandler.closeDbConnection();
		}
		else
		{
			dbHandler.executeUpdate( dbHandler.getDatabaseConnection(), tableName, columnName, columnValue, mapOntModel.getPKForWhereClause( t.getSubject().toString(), tableName ) );
			//dbHandler.closeDbConnection();
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