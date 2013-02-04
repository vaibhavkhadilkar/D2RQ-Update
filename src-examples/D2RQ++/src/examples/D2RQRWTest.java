package examples;

import java.io.FileReader;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.utdallas.d2rqrw.ModelD2RQRW;

/**
 * <p>Class used for testing the D2RQ-RW extension with basic functions such as add, remove, list etc</p>
 */
public class D2RQRWTest 
{
	public static void main(String[] args)
	{
		try
		{
			//Create a D2RQ-RW model
			ModelD2RQRW model = new ModelD2RQRW( args[0] );

			FileReader fr = new FileReader("/home/vaibhav/testInsertTriple.n3");
			model.read(fr, null, "N-TRIPLE");
			System.out.println("Model reading complete...");

			model.remove( model.createResource( "file:///home/vaibhav/employee.n3#employee/3" ), 
					model.createProperty( "http://localhost:2020/vocab/resource/employee_empid" ),
					model.asRDFNode( Node.createLiteral( "3", null, XSDDatatype.XSDint ) ) );
			System.out.println("Delete complete");

			//Test adding a triple to this model
			model.add( model.createResource( "file:///home/vaibhav/employee.n3#employee/2" ), 
					model.createProperty( "http://localhost:2020/vocab/resource/employee_empName" ), 
			"ABC" );

			//Try deleting the existing triple
			model.remove( model.createResource( "file:///home/vaibhav/employee.n3#employee/2" ), 
					model.createProperty( "http://localhost:2020/vocab/resource/employee_empName" ),
					model.asRDFNode( Node.createLiteral( "ABC" ) ) );

			//List all statements
			StmtIterator stmtIter = model.listStatements();	
			while (stmtIter.hasNext()) 
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

			//List all subjects with a specific property from the relational database
			ResIterator resIter = model.listSubjectsWithProperty( model.createProperty( "http://localhost:2020/vocab/resource/employee_empname" ) );
			while( resIter.hasNext() )
			{
				Resource sub = resIter.nextResource();
				System.out.println( "Subject from relational db: " + sub );
			}
			System.out.println();

			//List all subjects with a specific property from the RDB model
			resIter = model.listSubjectsWithProperty( model.createProperty( "http://base/employee_empname" ) );
			while( resIter.hasNext() )
			{
				Resource sub = resIter.nextResource();
				System.out.println( "Subject from RDB model: " + sub );
			}
			System.out.println();

			//List all resources with a specific property from the relational database
			resIter = model.listResourcesWithProperty( model.createProperty( "http://localhost:2020/vocab/resource/employee_empname" ) );
			while( resIter.hasNext() )
			{
				Resource sub = resIter.nextResource();
				System.out.println( "Resource from relational db: " + sub );
			}
			System.out.println();

			//List all resources with a specific property from the RDB model
			resIter = model.listResourcesWithProperty( model.createProperty( "http://base/employee_empname" ) );
			while( resIter.hasNext() )
			{
				Resource sub = resIter.nextResource();
				System.out.println( "Resource from RDB model: " + sub );
			}
			System.out.println();

			//List all resources with a specific property and object from the relational database
			resIter = model.listSubjectsWithProperty( model.createProperty( "http://localhost:2020/vocab/resource/employee_empname" ), "ABC" );
			while( resIter.hasNext() )
			{
				Resource sub = resIter.nextResource();
				System.out.println( "Subject from relational db with prop and obj: " + sub );
			}
			System.out.println();

			//List all resources with a specific property and object from the RDB model
			resIter = model.listSubjectsWithProperty( model.createProperty( "http://base/employee_empname" ), "XYZ" );
			while( resIter.hasNext() )
			{
				Resource sub = resIter.nextResource();
				System.out.println( "Subject from RDB model with prop and obj: " + sub );
			}
			System.out.println();

			//List all subjects
			resIter = model.listSubjects();
			while( resIter.hasNext() )
			{
				Resource sub = resIter.nextResource();
				System.out.println( "Subjects from relational database and RDB model: " + sub );
			}
			System.out.println();

			//List all objects for a specific property from the relational database
			NodeIterator nodeIter = model.listObjectsOfProperty( model.createProperty( "http://localhost:2020/vocab/resource/employee_empname" ) );
			while( nodeIter.hasNext() )
			{
				RDFNode obj = nodeIter.nextNode();
				System.out.println( "Object from relational database: " + obj );
			}
			System.out.println();

			//List all objects for a specific property from the RDB model
			nodeIter = model.listObjectsOfProperty( model.createProperty( "http://base/employee_empname" ) );
			while( nodeIter.hasNext() )
			{
				RDFNode obj = nodeIter.nextNode();
				System.out.println( "Object from RDB model: " + obj );
			}
			System.out.println();

			//List all objects with a specific resource and property from the relational database
			nodeIter = model.listObjectsOfProperty( model.createResource( "file:///home/vaibhav/workspace-d2rq-jena/employeeFeb08.n3#employee/1" ), model.createProperty( "http://localhost:2020/vocab/resource/employee_empname" ) );
			while( nodeIter.hasNext() )
			{
				RDFNode obj = nodeIter.nextNode();
				System.out.println( "Object from relational db with prop and obj: " + obj );
			}
			System.out.println();

			//List all objects with a specific resource and property from the RDB model
			nodeIter = model.listObjectsOfProperty( model.createResource( "file:///base#employee/2" ), model.createProperty( "http://base/employee_empname" ) );
			while( nodeIter.hasNext() )
			{
				RDFNode obj = nodeIter.nextNode();
				System.out.println( "Object from RDB model with prop and obj: " + obj );
			}
			System.out.println();

			//List all objects
			nodeIter = model.listObjects();
			while( nodeIter.hasNext() )
			{
				RDFNode obj = nodeIter.nextNode();
				System.out.println( "Objects from relational database and RDB model: " + obj );
			}
			System.out.println();
		}
		catch( Exception e ) { e.printStackTrace(); }	
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