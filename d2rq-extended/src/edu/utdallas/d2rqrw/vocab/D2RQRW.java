package edu.utdallas.d2rqrw.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.fuberlin.wiwiss.d2rq.vocab.D2RQ;

/**
 * <p>Vocabulary extensions to include arbitrary blank nodes</p>
 */
public class D2RQRW extends D2RQ
{
    /** <p>The RDF model that holds the vocabulary terms</p> **/
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> **/
    public static final String NS = "http://cs.utdallas.edu/semanticweb/D2RQRW/0.1#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS **/
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> **/
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>Maps an RDF property to one or more database columns using a blank node.</p> **/
    public static final Resource BlankNodePropertyBridge = m_model.createResource( "http://cs.utdallas.edu/semanticweb/D2RQRW/0.1#BlankNodePropertyBridge" );
    
    /** <p>Maps one or more relational database columns to a blank node.</p> **/
    public static final Property belongsToBlankNode = m_model.createProperty( "http://cs.utdallas.edu/semanticweb/D2RQRW/0.1#belongsToBlankNode" );
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