package edu.utdallas.jena.rdf.model.impl;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.NodeIteratorImpl;
import com.hp.hpl.jena.rdf.model.impl.ResIteratorImpl;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

/**
 * <p>Build Jena iterators using maps for a Resource and RDFNode</p>
 */
public final class ExtendedIteratorFactory 
{
	/** <p>A private constructor</p> **/
	private ExtendedIteratorFactory() { }
	
	/**
	 * <p>Method that creates an iterator of resources</p>
	 * @param i - an iterator of resources as input
	 * @param m - the underlying model to use in the mapping
	 * @return a ResIterator
	 */
	public static ResIterator asResIterator( Iterator<Resource> i, final ModelCom m ) 
	{
		Map1<Resource, Resource> asResource = new Map1<Resource, Resource>() 
	    { 
			public Resource map1( Resource o ) 
			{ return (Resource) m.asRDFNode( o.asNode() ); }
		};
	    return new ResIteratorImpl( WrappedIterator.create( i ).mapWith( asResource ), null );
	}

	/**
	 * <p>Method that creates an iterator of rdf nodes</p>
	 * @param i - an iterator of rdf nodes as input
	 * @param m - the underlying model to use in the mapping
	 * @return a NodeIterator
	 */
	static public NodeIterator asRDFNodeIterator( Iterator<RDFNode> i, final ModelCom m ) 
    {      
		Map1<RDFNode, RDFNode> asRDFNode = new Map1<RDFNode, RDFNode>() 
        { 
			public RDFNode map1( RDFNode o ) 
			{ return m.asRDFNode( o.asNode() ); }
		};
		return new NodeIteratorImpl( WrappedIterator.create( i ).mapWith( asRDFNode ), null );
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