package edu.utdallas.d2rqrw.util;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

/**
 * <p>A class that specifies various maps used in the construction of SQL INSERT, UPDATE,
 * and DELETE queries</p> 
 */
public class DbParameterMaps 
{
	/**
	 * <p>A map between the database type obtained from the mapping file to the database type 
	 * used by Jena</p>
	 */
	public static final Map<String, String> dbTypeMap;
	static
	{
		dbTypeMap = new HashMap<String, String>();	
		
		dbTypeMap.put( "mysql", "MySQL" );
	}
	
	/**
	 * <p>A map between the datatypes in MySQL</p>
	 */
	public static final Map<String, String> mysqlDatatypeMap;
	static
	{
		mysqlDatatypeMap = new HashMap<String, String>();
		
		mysqlDatatypeMap.put( "http://www.w3.org/2001/XMLSchema#int", "INTEGER" );
		mysqlDatatypeMap.put( "http://www.w3.org/2001/XMLSchema#date", "DATE" );
	}

	/**
	 * <p>A map between a database and its associated datatype map</p>
	 */
	public static final Map<String, Map<String, String>> dbDatatypeMap;
	static
	{
		dbDatatypeMap = new HashMap<String, Map<String, String>>();
		
		dbDatatypeMap.put( "MySQL", mysqlDatatypeMap );
	}
	
	public static final Map<String, XSDDatatype> xsdDatatypeMap;
	static
	{
		xsdDatatypeMap = new HashMap<String, XSDDatatype>();
		
		xsdDatatypeMap.put( "INTEGER", XSDDatatype.XSDint );
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