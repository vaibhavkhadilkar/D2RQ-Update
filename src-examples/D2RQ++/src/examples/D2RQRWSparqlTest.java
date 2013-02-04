package examples;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

import edu.utdallas.d2rqrw.ModelD2RQRW;
import edu.utdallas.d2rqrw.jena.query.D2RQRWQueryExecutionFactory;

/**
 * <p>Class used for testing the D2RQ-RW extension with SPARQL queries</p> 
 */
public class D2RQRWSparqlTest 
{
	public static void main( String[] args )
	{
		//Create a D2RQ-RW model
		ModelD2RQRW model = new ModelD2RQRW( args[0] );

		//Test adding a triple to this model, right now this triple goes to the RDB store
		model.add( model.createResource( "file:///base#employee/2" ), 
				   model.createProperty( "http://base/employee_empname" ), "XYZ" );

		//Query to select all statements
		String queryString = " SELECT ?x ?y ?z WHERE { ?x ?y ?z } ";
		QueryExecution qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		ResultSet rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Statement: " + rs.next().toString() ); }
		
		System.out.println();
		queryString = null; qexec = null; rs = null;
		
		//Query to select subjects for a particular property from the relational database
		queryString = " PREFIX uri: <http://localhost:2020/vocab/resource/> SELECT ?x WHERE { ?x uri:employee_empname ?z } ";
		qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Subject from the relational database: " + rs.next().toString() ); }

		System.out.println();
		queryString = null; qexec = null; rs = null;

		//Query to select subjects for a particular property from the RDB model
		queryString = " PREFIX uri: <http://base/> SELECT ?x WHERE { ?x uri:employee_empname ?z } ";
		qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Subject from the RDB model: " + rs.next().toString() ); }

		System.out.println();
		queryString = null; qexec = null; rs = null;

		//Query to select subjects for a particular property and object from the relational database
		queryString = " PREFIX uri: <http://localhost:2020/vocab/resource/> SELECT ?x WHERE { ?x uri:employee_empname \"ABC\" } ";
		qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Subject with a specific property and object from the relational database: " + rs.next().toString() ); }

		System.out.println();
		queryString = null; qexec = null; rs = null;

		//Query to select subjects for a particular property and object from the RDB model
		queryString = " PREFIX uri: <http://base/> SELECT ?x WHERE { ?x uri:employee_empname \"XYZ\" } ";
		qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Subject with a specific property and object from the RDB model: " + rs.next().toString() ); }

		System.out.println();
		queryString = null; qexec = null; rs = null;

		//Query to select objects for a particular property from the relational database
		queryString = " PREFIX uri: <http://localhost:2020/vocab/resource/> SELECT ?z WHERE { ?x uri:employee_empname ?z } ";
		qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Object from the relational database: " + rs.next().toString() ); }
		
		System.out.println();
		queryString = null; qexec = null; rs = null;
		
		//Query to select objects for a particular property from the RDB model
		queryString = " PREFIX uri: <http://base/> SELECT ?z WHERE { ?x uri:employee_empname ?z } ";
		qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Object from the RDB model: " + rs.next().toString() ); }

		System.out.println();
		queryString = null; qexec = null; rs = null;
		
		//Query to select objects for a particular resource and property from the relational database
		queryString = " PREFIX uri: <file:///home/vaibhav/workspace-d2rq-jena/employeeFeb08.n3#employee/> " +
					  " PREFIX prop: <http://localhost:2020/vocab/resource/> " +
					  " SELECT ?z WHERE { uri:1 prop:employee_empname ?z } ";
		qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Object from the relational database: " + rs.next().toString() ); }
		
		System.out.println();
		queryString = null; qexec = null; rs = null;

		//Query to select objects for a particular resource and property from the RDB model
		queryString = " PREFIX uri: <file:///base#employee/> " +
					  " PREFIX prop: <http://base/> " +
					  " SELECT ?z WHERE { uri:2 prop:employee_empname ?z } ";
		qexec = D2RQRWQueryExecutionFactory.create( queryString, model );
		rs = qexec.execSelect();
		while( rs.hasNext() )
		{ System.out.println( "Object from the RDB model: " + rs.next().toString() ); }
		
		System.out.println();
		queryString = null; qexec = null; rs = null;
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