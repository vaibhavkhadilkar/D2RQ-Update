package edu.utdallas.d2rqrw.rdb;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * <p>A class that returns a new or an existing Jena RDB model</p>
 */
public class RDBModelForD2RQRW 
{		
	/**
	 * <p>Method that returns a new or an existing Jena RDB model</p>
	 * @param dbUrl - the database url
	 * @param dbUser - the database user
	 * @param dbPass - the database password
	 * @param dbType - the database type
	 * @param create - true iff a new model is to be created, false otherwise
	 * @return a new or an existing Jena RDB model
	 */
	public Model getRDBModel( String dbUrl, String dbUser, String dbPass, String dbType, boolean create )
	{
		//Create a database model maker
		ModelMaker rdbModelMaker = ModelFactory.createModelRDBMaker( new DBConnection( dbUrl, dbUser, dbPass, dbType ) );
		
		//If the model does not exist create a new model, else return the existing model
		if( !rdbModelMaker.hasModel( "rdbModel" ) )
		{
			if ( create )
				return rdbModelMaker.createModel( "rdbModel" );
			else
				return null;
		}
		else
			return rdbModelMaker.openModel( "rdbModel" );
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