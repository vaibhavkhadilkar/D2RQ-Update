package examples;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.utdallas.d2rqrw.ModelD2RQRW;

/**
 * <p>Class used for testing the D2RQ-RW extension with functions for blank nodes</p> 
 */
public class D2RQRWAnonNodeTest 
{
	public static void main(String[] args)
	{
		//Create a D2RQ-RW model
		ModelD2RQRW model = new ModelD2RQRW( args[0] );

		//Test adding a triple to this model
		model.add( model.createResource( "file:///home/vaibhav/employee.n3#employee/2" ), 
				   model.createProperty( "http://localhost:2020/vocab/resource/employee_empName" ), 
				   "ABC" );

		//Test adding a triple to this model
		model.add( model.createResource( "file:///home/vaibhav/employee.n3#employee/2" ), 
				   model.createProperty( "http://localhost:2020/vocab/resource/employee_address" ), 
				   model.asRDFNode( Node.createAnon( AnonId.create( "file:///home/vaibhav/employee.n3#employee_address@@XYZ/ABC/XYZ" ) ) ) );

		//Test adding a triple to this model
		model.add( model.createResource( "file:///home/vaibhav/employee.n3#employee/2" ), 
				   model.createProperty( "http://localhost:2020/vocab/resource/employee_phone" ), 
				   "202-404-6064" );

		//Try deleting the existing triple
		model.remove( model.createResource( "file:///home/vaibhav/employee.n3#employee/2" ), 
				   	  model.createProperty( "http://localhost:2020/vocab/resource/employee_address" ), 
					  model.asRDFNode( Node.createAnon( AnonId.create( "file:///home/vaibhav/employee.n3#employee_address@@XYZ/ABC/XYZ" ) ) ) );

		//Try deleting the existing triple
		model.remove( model.createResource( "file:///home/vaibhav/employee.n3#employee/2" ), 
				   	  model.createProperty( "http://localhost:2020/vocab/resource/employee_phone" ), 
				   	  model.asRDFNode( Node.createLiteral( "ABC" ) ) );

		//List all statements with blank nodes
		StmtIterator stmtIter = model.listStatements();
		while( stmtIter.hasNext() )
		{
			Statement stmt      = stmtIter.nextStatement();
			Resource  subject   = stmt.getSubject();
			Property  predicate = stmt.getPredicate();
			RDFNode   object    = stmt.getObject();

			String subj = subject.toString();
			String pred = predicate.toString();
			String obj  = object.toString();

			System.out.println("Subject : " + subj + ", Predicate : " + pred + ", Object: " + obj);
		}
		System.out.println();
		
		//List all subjects with the blank node property
		ResIterator resIter = model.listSubjectsWithProperty( model.createProperty( "http://localhost:2020/vocab/resource/employee_address" ) );
		while( resIter.hasNext() )
		{
			Resource sub = resIter.nextResource();
			System.out.println( "Subject from relational db: " + sub );
		}
		System.out.println();
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