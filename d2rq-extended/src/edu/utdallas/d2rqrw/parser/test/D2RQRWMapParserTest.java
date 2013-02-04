package edu.utdallas.d2rqrw.parser.test;

import java.util.Collections;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import de.fuberlin.wiwiss.d2rq.D2RQException;
import de.fuberlin.wiwiss.d2rq.algebra.AliasMap;
import de.fuberlin.wiwiss.d2rq.algebra.TripleRelation;
import de.fuberlin.wiwiss.d2rq.map.Mapping;
import de.fuberlin.wiwiss.d2rq.map.TranslationTable;
import de.fuberlin.wiwiss.d2rq.sql.SQL;
import de.fuberlin.wiwiss.d2rq.values.Translator;
import de.fuberlin.wiwiss.d2rq.vocab.D2RQ;
import edu.utdallas.d2rqrw.parser.D2RQRWMapParser;
import edu.utdallas.d2rqrw.test.D2RQRWTestSuite;

import junit.framework.TestCase;

/**
 * A JUnit test class for the D2RQ++ Map Parser
 * @author vaibhav
 *
 */
public class D2RQRWMapParserTest extends TestCase
{
	/** A dummy resource **/
	private final static String TABLE_URI = "http://example.org/map#table1";

	/** A default model used in tests **/
	private Model model;

	/**
	 * Sets up the model for all tests
	 */
	protected void setUp() throws Exception 
	{
		this.model = ModelFactory.createDefaultModel();
	}

	/**
	 * Test for an empty translation table
	 */
	public void testEmptyTranslationTable() 
	{
		Resource r = addTranslationTableResource();
		Mapping mapping = new D2RQRWMapParser( this.model, null ).parse();
		TranslationTable table = mapping.translationTable( r );
		assertNotNull( table );
		assertEquals( 0, table.size() );
	}

	/**
	 * Test for getting the same translation table repeatedly
	 */	
	public void testGetSameTranslationTable() 
	{
		Resource r = addTranslationTableResource();
		addTranslationResource( r, "foo", "bar" );
		Mapping mapping = new D2RQRWMapParser( this.model, null ).parse();
		TranslationTable table1 = mapping.translationTable( r );
		TranslationTable table2 = mapping.translationTable( r );
		assertSame( table1, table2 );
	}

	/**
	 * Test to parse a translation table
	 */
	public void testParseTranslationTable() 
	{
		Resource r = addTranslationTableResource();
		addTranslationResource( r, "foo", "bar" );
		Mapping mapping = new D2RQRWMapParser( this.model, null ).parse();
		TranslationTable table = mapping.translationTable( r );
		assertEquals( 1, table.size() );
		Translator translator = table.translator();
		assertEquals( "bar", translator.toRDFValue( "foo" ) );
	}

	/**
	 * Test for parsing aliases
	 */
	public void testParseAlias() 
	{
		Mapping mapping = parse( "parser/test/alias.n3" ).parse();
		assertEquals( 1, mapping.compiledPropertyBridges().size() );
		TripleRelation bridge = (TripleRelation) mapping.compiledPropertyBridges().iterator().next();
		assertTrue( bridge.baseRelation().condition().isTrue() );
		AliasMap aliases = bridge.baseRelation().aliases();
		AliasMap expected = new AliasMap( Collections.singleton( SQL.parseAlias( "People AS Bosses" ) ) );
		assertEquals( expected, aliases );
	}

	/**
	 * Test for parsing resources instead of literals
	 */
	public void testParseResourceInsteadOfLiteral() 
	{
		try { parse( "parser/test/resource-instead-of-literal.n3" ).parse(); } 
		catch ( D2RQException ex ) { assertEquals( D2RQException.MAPPING_RESOURCE_INSTEADOF_LITERAL, ex.errorCode() ); }
	}

	/**
	 * Test for parsing literals instead of resources
	 */
	public void testParseLiteralInsteadOfResource() 
	{
		try { parse( "parser/test/literal-instead-of-resource.n3" ).parse(); } 
		catch ( D2RQException ex ) { assertEquals( D2RQException.MAPPING_LITERAL_INSTEADOF_RESOURCE, ex.errorCode() ); }
	}

	/**
	 * Test for checking if a translation table's RDF value can be a literal
	 */	
	public void testTranslationTableRDFValueCanBeLiteral() 
	{
		Mapping m = parse( "parser/test/translation-table.n3" ).parse();
		TranslationTable tt = m.translationTable( ResourceFactory.createResource( "http://example.org/tt" ) );
		assertEquals( "http://example.org/foo", tt.translator().toRDFValue( "literal" ) );
	}

	/**
	 * Test for checking if a translation table's RDF value can be a URI
	 */		
	public void testTranslationTableRDFValueCanBeURI() 
	{
		Mapping m = parse("parser/test/translation-table.n3").parse();
		TranslationTable tt = m.translationTable(ResourceFactory.createResource("http://example.org/tt"));
		assertEquals("http://example.org/foo", tt.translator().toRDFValue("uri"));
	}

	/**
	 * Test for checking if a conflict between a class map and property bridge is detected
	 */	
	public void testTypeConflictClassMapAndBridgeIsDetected() 
	{
		try { parse( "parser/test/type-classmap-and-propertybridge.n3" ).parse(); } 
		catch ( D2RQException ex ) { assertEquals( D2RQException.MAPPING_TYPECONFLICT, ex.errorCode() ); }
	}

	/**
	 * Method that returns a D2RQ++ map parser based on an input file
	 * @param testFileName - the input filename
	 * @return - a D2RQ++ map parser
	 */
	private D2RQRWMapParser parse( String testFileName ) 
	{
		Model m = ModelFactory.createDefaultModel();
		m.read( D2RQRWTestSuite.DIRECTORY_URL + testFileName, "N3" );
		D2RQRWMapParser result = new D2RQRWMapParser( m, null );
		result.parse();
		return result;
	}

	/**
	 * Method for adding a translation table resource
	 */	
	private Resource addTranslationTableResource() 
	{
		return this.model.createResource( TABLE_URI, D2RQ.TranslationTable );
	}

	/**
	 * Method that returns a resource representing a translation based on a database and RDF value
	 * @param table - the input resource table
	 * @param dbValue - the database value
	 * @param rdfValue - the RDF value
	 * @return a resource representing a translation based on a database and RDF value
	 */
	private Resource addTranslationResource( Resource table, String dbValue, String rdfValue ) 
	{
		Resource translation = this.model.createResource();
		translation.addProperty( D2RQ.databaseValue, dbValue );
		translation.addProperty( D2RQ.rdfValue, rdfValue );
		table.addProperty( D2RQ.translation, translation );
		return translation;
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