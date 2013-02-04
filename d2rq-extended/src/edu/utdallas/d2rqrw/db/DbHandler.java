package edu.utdallas.d2rqrw.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <p>A class that handles database operations</p> 
 */
public class DbHandler 
{
	/** <p>The parameters for the database: driver, url, user, and password</p> **/
	private String dbDriver = null, dbUrl = null, dbUser = null, dbPassword = null;
	
	/** <p>The database connection</p> **/
	private Connection dbConn = null;

	/**
	 * <p>Constructor</p>
	 * @param dbDriver - the database driver
	 * @param dbUrl - the database url
	 * @param dbUser - the database user
	 * @param dbPassword - the database password
	 */
	public DbHandler( String dbDriver, String dbUrl, String dbUser, String dbPassword ) 
	{ this.dbDriver = dbDriver; this.dbUrl = dbUrl; this.dbUser = dbUser; this.dbPassword = dbPassword; }
	
	/**
	 * <p>Method that uses the Singleton pattern to create and use database connections
	 * @return a db connection
	 */
	public Connection getDatabaseConnection()
	{
		return Singleton.getInstance( dbDriver, dbUrl, dbUser, dbPassword );
	}
	
	/**
	 * <p>Method that creates a database connection</p>
	 * @return a db connection
	 */
	public Connection getDbConnection()
	{
		try { Class.forName( dbDriver ).newInstance (); dbConn = DriverManager.getConnection ( dbUrl, dbUser, dbPassword ); }
		catch( Exception e ) { e.printStackTrace(); }
		return dbConn;
	}
	
	/**
	 * <p>Method that closes the database connection if it is open</p>
	 */
	public void closeDbConnection()
	{
		if ( dbConn != null )
		{
			try { dbConn.close (); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else
		{
			try { throw new SQLException( "Connection is already closed" ); }
			catch( Exception e ) { e.printStackTrace(); }
		}			
	}
	
	/**
	 * <p>Method that inserts a new triple into the relational database as a new tuple</p>
	 * @param dbConn - the database connection
	 * @param tableName - the relation name
	 * @param pkColumnsList - the primary key fields for this relation name 
	 * @param columnName - the column name in which we need to insert the current triple
	 * @param pkColumnsValueList - the values to be inserted in the primary key fields
	 * @param columnValue - the new cell in which we need to insert the current triple
	 */
	public void executeInsert( Connection dbConn, String tableName, String pkColumnsList, String columnName, String pkColumnsValueList, String columnValue )
	{
		Statement s = null;
		try
		{
			s = dbConn.createStatement();
			String insertStatement = null;
			if( pkColumnsList.equalsIgnoreCase( "" ) && pkColumnsValueList.equalsIgnoreCase( "" ) )
				insertStatement = "INSERT INTO " + tableName + "(" + columnName + ") VALUES (" + columnValue + ")";
			else
				insertStatement = "INSERT INTO " + tableName + "(" + pkColumnsList + ", " + columnName + ") VALUES (" + pkColumnsValueList + ", " + columnValue + ")";
			int rowsInserted = s.executeUpdate( insertStatement );
			if ( rowsInserted == 0 ) throw new SQLException( "Insert failed" );
		}
		catch( Exception e ) { e.printStackTrace(); }
		finally
		{
			try{ s.close(); }
			catch( Exception e ){ e.printStackTrace(); }
		}
	}
	
	/**
	 * <p>Method that updates the value of a cell for an existing tuple in the relational database</p>
	 * @param dbConn - the database connection
	 * @param tableName - the relation name
	 * @param columnName - the column name in which a value is updated 
	 * @param columnValue - the cell value
	 * @param pkForWhereClause - the primary keys of this relation; part of the WHERE clause in the UPDATE statement
	 */
	public void executeUpdate( Connection dbConn, String tableName, String columnName, String columnValue, String pkForWhereClause )
	{
		Statement s = null;
		try
		{
			s = dbConn.createStatement();			
			String updateStatement = null;
			if( !columnName.contains( "," ) )
				updateStatement = "UPDATE " + tableName + " SET " + columnName + " = " + columnValue + " WHERE" + pkForWhereClause;
			else
			{
				String multipleAttrStmt = "";
				String[] columns = columnName.split( ", " );
				String[] values = columnValue.split( ", " );
				updateStatement = "UPDATE " + tableName + " SET ";
				for( int i = 0; i < columns.length; i++ )
					multipleAttrStmt += columns[i] + " = " + values[i] + ", ";
				updateStatement += multipleAttrStmt.substring( 0, multipleAttrStmt.length() - 2 );
				updateStatement += " WHERE" + pkForWhereClause;
			}

			int rowsInserted = s.executeUpdate( updateStatement );
			if ( rowsInserted == 0 ) throw new SQLException( "Update failed" );
		}
		catch( Exception e ) { e.printStackTrace(); }
		finally
		{
			try{ s.close(); }
			catch( Exception e ){ e.printStackTrace(); }
		}
	}
	
	/**
	 * <p>Method that deletes a tuple from the relational database</p>
	 * @param dbConn - the database connection
	 * @param tableName - the relation name
	 * @param columnName - the column name for which the tuple needs to be deleted
	 * @param columnValue - the cell value
	 * @param pkForWhereClause - the primary keys of this relation; part of the WHERE clause in the DELETE statement
	 */
	public void executeDelete( Connection dbConn, String tableName, String pkForWhereClause )
	{
		Statement s = null;
		try
		{
			s = dbConn.createStatement();
			String deleteStatement = "DELETE FROM " + tableName + " WHERE " + pkForWhereClause;
			
			int rowsDeleted = s.executeUpdate( deleteStatement );
			if ( rowsDeleted == 0 )	throw new SQLException( "Delete failed" );
		}
		catch( Exception e ) { e.printStackTrace(); }
		finally
		{
			try{ s.close(); }
			catch( Exception e ){ e.printStackTrace(); }
		}		
	}
	
	/**
	 * <p>A Singleton class used to create and destroy database connections</p>
	 */
	public static class Singleton
    {
		/** The database connection **/
    	private static Connection conn = null;

    	/** A protected constructor **/
    	protected Singleton() {}

    	/**
    	 * Method that creates a database connection
    	 * @param dbDriver - the db driver
    	 * @param dbUrl - the db url
    	 * @param dbUser - the db user
    	 * @param dbPassword - the db password
    	 * @return a db connection
    	 */
    	public static Connection getInstance( String dbDriver, String dbUrl, String dbUser, String dbPassword )
    	{
    		try { Class.forName( dbDriver ).newInstance (); conn = DriverManager.getConnection ( dbUrl, dbUser, dbPassword ); }
    		catch( Exception e ) { e.printStackTrace(); }
    		return conn;
    	}

    	/**
    	 * Method that destroys a database connection
    	 */
    	public void finalize() throws Throwable
    	{
    		try { conn.close(); }
    		finally { super.finalize(); }
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