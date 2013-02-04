package edu.utdallas.d2rqrw.util;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import edu.utdallas.d2rqrw.ontology.MapAsOntModel;

/**
 * <p>A utility class that processes triples to extract database related parameters</p> 
 */
public class TripleUtils 
{
	/** <p>A private constructor</p> **/
	private TripleUtils() { }
	
	/**
	 * <p>Method that returns the table name from the given triple</p>
	 * @param - the current triple
	 * @return the relation name as a string
	 */
	public static String getTableName( Triple t )
	{ 
		String string = t.getSubject().toString(); 
		if( string.contains( "#" ) ) return string.substring( string.indexOf( '#' ) + 1, string.indexOf( '/', string.indexOf( '#' ) ) );
	  	else
	  	{
	  		String[] splitStr = string.split( "/" );
	  		return splitStr[splitStr.length - 2];
	  	}
	}
	
	/**
	 * <p>Method that returns the cell value as a string given the current triple</p>
	 * @param t - the current triple
	 * @return the cell value as a string from the input triple
	 */
	public static String getCellValue( Triple t )
	{
		Node object = t.getObject();
		if ( object.isLiteral() ) return object.getLiteralValue().toString();
		else if( object.isURI() ) return object.toString().substring( object.toString().lastIndexOf( '/' ) );
		else if( object.isBlank() ) return object.toString().split( "@@" )[1];
		else return null;
	}

	/**
	 * <p>Method that returns the column name from the given triple</p>
	 * @param t - the current triple
	 * @return the column name
	 */
	public static String getColumnName( Triple t )
	{ 
		String tablename = TripleUtils.getTableName( t );
		return t.getPredicate().getLocalName().substring( tablename.length() + 1 ); 
	} 
	
	/**
	 * <p>Method that returns the column value from the current triple</p>
	 * @param t - the current triple
	 * @param tableName - the relation name as a string
	 * @param isInsert - determines if the calling function is adding or removing the current triple
	 * @return the column value
	 */
	public static String getColumnValue( Triple t, String tableName, String datatype, boolean isInsert )
	{
		String columnValue = null;
		if( isInsert )
		{
			columnValue = getCellValue( t );
			if ( datatype == null )	columnValue = "'" + columnValue + "'";			
		}
		else
			columnValue = "NULL";
		return columnValue;
	}

	/**
	 * <p>Method that returns an array of column names and values
	 * @param t - the current triple
	 * @param blankNodeFields - the relational database columns corresponding to the current blank node
	 * @param tableName - the relation name
	 * @param isInsert - determines if the calling function is adding or removing the current triple 
	 * @return a string array containing the column names and values
	 */
	public static String[] getColumnNamesAndValues( Triple t, String blankNodeFields, String tableName, MapAsOntModel mapOntModel, boolean isInsert )
	{
		String[] columnNamesAndValues = new String[2];
		String columnName = "", columnValue = "";
		String[] columnNameArr = blankNodeFields.replace( " ", ", " ).split( ", " ), objectValueArr = getCellValue( t ).split( "/" );
		for( int i = 0; i < columnNameArr.length; i++ )
		{
			columnName += columnNameArr[columnNameArr.length - 1 - i].replace( tableName + "_", tableName + "." ) + ", ";
			if( isInsert ) 
			{ 
				if ( mapOntModel != null && mapOntModel.getColumnDatatype( tableName, columnNameArr[i] ) == null ) columnValue += "'" + objectValueArr[i] + "', ";	
				else columnValue += objectValueArr[i] + ", "; 
			}
			else columnValue += "NULL, ";
		}
		columnName = columnName.substring( 0, columnName.length() - 2 );
		columnValue = columnValue.substring( 0, columnValue.length() - 2 );
		columnNamesAndValues[0] = columnName; columnNamesAndValues[1] = columnValue;
		return columnNamesAndValues;
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