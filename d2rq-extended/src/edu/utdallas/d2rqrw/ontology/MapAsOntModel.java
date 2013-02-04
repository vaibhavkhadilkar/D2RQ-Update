package edu.utdallas.d2rqrw.ontology;

import java.util.Iterator;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.fuberlin.wiwiss.d2rq.vocab.D2RQ;
import edu.utdallas.d2rqrw.util.DbParameterMaps;
import edu.utdallas.d2rqrw.vocab.D2RQRW;

/**
 * <p>A class that maps the model created based on the mapping file into an ontology model.
 * This allows us to access the underlying database parameters.</p>
 */
public class MapAsOntModel 
{
	/** <p>The ontology model based on the map model</p> **/
	private OntModel ontModel;
			
	/**
	 * <p>Constructor</p>
	 * @param mapModel - the mapping model
	 */
	public MapAsOntModel( OntModel ontModel )
	{ this.ontModel = ontModel; }

	/**
	 * <p>Getter for the current ontology model</p>
	 * @return the ontology model for the current D2RQRW model
	 */
	public OntModel getOntModel()
	{ return ontModel; }
	
	/**
	 * <p>Method to get the database type of the relational database</p>
	 * @return string representing the database type
	 */
	public String getDatabaseType()
	{ return DbParameterMaps.dbTypeMap.get( ( (Resource)ontModel.listIndividuals( D2RQ.Database ).next() ).listProperties( D2RQ.jdbcDSN ).nextStatement().getString().split( ":" )[1] ); }

	/**
	 * <p>Method to get the database user of the relational database</p>
	 * @return string representing the database user
	 */
	public String getDatabaseUser()
	{ return ( (Resource)ontModel.listIndividuals( D2RQ.Database ).next() ).listProperties( D2RQ.username ).nextStatement().getString(); }
	
	/**
	 * <p>Method to get the database password for the relational database</p>
	 * @return string representing the database password
	 */
	public String getDatabasePassword()
	{ return ( (Resource)ontModel.listIndividuals( D2RQ.Database ).next() ).listProperties( D2RQ.password ).nextStatement().getString(); }
	
	/**
	 * <p>Method to get the database url for the relational database</p>
	 * @return string representing the database url
	 */
	public String getDatabaseUrl()
	{ return ( (Resource)ontModel.listIndividuals( D2RQ.Database ).next() ).listProperties( D2RQ.jdbcDSN ).nextStatement().getString(); }
	
	/**
	 * Method to get the Database Driver for the relational database
	 * @return string representing the Database Driver
	 */
	public String getDatabaseDriver()
	{ return ( (Resource)ontModel.listIndividuals( D2RQ.Database ).next() ).listProperties( D2RQ.jdbcDriver ).nextStatement().getString(); }

	/**
	 * <p>Method that returns the datatype for the given column in the given relation</p>
	 * @param tableName - the relation name
	 * @param columnName - the column name
	 * @return a string that represents the datatype of the given column in the given relation
	 */
	public String getColumnDatatype( String tableName, String columnName )
	{
		ExtendedIterator<Individual> it = ontModel.listIndividuals( D2RQ.ClassMap );
		while ( it.hasNext() ) 
		{
			Resource className = (Resource) it.next();
			if ( className.getLocalName().equalsIgnoreCase( tableName ) )
			{
				StmtIterator predIter = ontModel.listStatements( null, D2RQ.belongsToClassMap, (RDFNode) className );
				while ( predIter.hasNext() )
				{
					Resource predResource = predIter.nextStatement().getSubject();
					if ( predResource.getLocalName().equalsIgnoreCase( columnName ) )
					{
						StmtIterator predDTypeIter = predResource.listProperties( D2RQ.datatype );
						while ( predDTypeIter.hasNext() ) 
						{
							return ( DbParameterMaps.dbDatatypeMap.get( getDatabaseType() ) ).get( predDTypeIter.nextStatement().getResource().getURI() );
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * <p>Method that determines the primary keys of a given relation</p>
	 * @param tableName - the relation name
	 * @return a string array containing the primary keys for the given relation
	 */
	public String[] getPrimaryKeyColumns( String tableName )
	{
		ExtendedIterator<Individual> it = ontModel.listIndividuals( D2RQ.ClassMap );
		while ( it.hasNext() ) 
		{
			Resource className = (Resource) it.next();
			if ( className.getLocalName().equalsIgnoreCase( tableName ) )
			{
				StmtIterator uriPatternIter = className.listProperties( D2RQ.uriPattern );
				while ( uriPatternIter.hasNext() ) 
				{
					String[] pkColumnsArray = uriPatternIter.nextStatement().getString().split( "@@" );
					String[] pkColumns = new String[pkColumnsArray.length/2];
					int j = 0;
					for ( int i = 1; i < pkColumnsArray.length; i = i+2 )
						pkColumns[j++] = pkColumnsArray[i];
					return pkColumns;
				}
			}
		}
		return null;
	}
	
	/**
	 * <p>Method that returns a mapping of a blank node with the associated relational database columns<p>
	 * @return a map that contains a blank node and its corresponding relational database columns
	 */
	public String getBlankNodeFields( String columnName )
	{
		Iterator<Individual> indIter = ontModel.listIndividuals( D2RQRW.BlankNodePropertyBridge );
		while( indIter.hasNext() )
		{
			Resource res = (Resource)indIter.next();
			if( res.getLocalName().equalsIgnoreCase( columnName ) )
			{
				StmtIterator stmtIter = ontModel.listStatements( null, D2RQRW.belongsToBlankNode, (RDFNode) res );
				String blankNodeFields = "";
				while( stmtIter.hasNext() )
					blankNodeFields += stmtIter.nextStatement().getSubject().getLocalName() + " ";
				
				return blankNodeFields.substring( 0, blankNodeFields.length() - 1 );
			}
		}
		return null;
	}
	
	/**
	 * <p>Method that returns a string of all values of the primary key fields that are supplied as an argument
	 * in the WHERE clause of the UPDATE statement</p>
	 * @param subject - the subject for the current triple as a string
	 * @param tableName - the relation name
	 * @return a string containing the primary key values in the WHERE clause
	 */
	public String getPKForWhereClause( String subject, String tableName )
	{
		String[] pkColumns = getPrimaryKeyColumns( tableName );
		if ( pkColumns.length == 0 ) return null;
		String[] pkColumnValues = null;
		if( subject.contains( "#" ) ) pkColumnValues = subject.substring( subject.indexOf( "#" ) ).split( "/" );
		else { String[] splitStr = subject.split( tableName ); pkColumnValues = splitStr[1].split( "/" ); }
		String whereClause = ""; int j = 1;
		for ( int i = 0; i < pkColumns.length; i++ )
		{
			String pkColumn = pkColumns[i].replaceAll( "\\.", "_" );
			String pkColumnValue = pkColumnValues[j++];
			if ( !getColumnDatatype( tableName, pkColumn ).equalsIgnoreCase( "INTEGER" ) )
				pkColumnValue = "'" + pkColumnValue + "'";
			if ( i != 0 )
				whereClause = whereClause + " AND ";							
			whereClause = whereClause + " " + pkColumns[i].split( "\\." )[1] + " = " + pkColumnValue;
		}
		return whereClause;
	}
	
	/**
	 * <p>Method that determines the primary keys of a relation from the given relation name,
	 * and their values from the subject of the given triple</p>
	 * @param subject - the subject of the current triple
	 * @param tableName - the relation name
	 * @return a concatenated string of the primary keys and their values
	 */
	public String getPKForInsert( String subject, String tableName )
	{
		String pkColumnsList = "", pkColumnsValueList = "";
		String[] pkColumns = getPrimaryKeyColumns( tableName );
		String[] pkColumnValues = subject.substring( subject.indexOf( "#" ) ).split( "/" );

		for( int i = 0; i < pkColumns.length; i++ )
		{
			String columnType = getColumnDatatype(tableName, pkColumns[i].replace( ".", "_" ));
			pkColumnsList = pkColumns[i] + ", ";
			if ( columnType == null ) pkColumnsValueList = "'" + pkColumnValues[i+1] + "', ";
			else pkColumnsValueList = pkColumnValues[i+1] + ", ";
		}
		pkColumnsList = pkColumnsList.substring( 0, pkColumnsList.length() - 2 );
		pkColumnsValueList = pkColumnsValueList.substring( 0, pkColumnsValueList.length() - 2 );
		return pkColumnsList + "~" + pkColumnsValueList;
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