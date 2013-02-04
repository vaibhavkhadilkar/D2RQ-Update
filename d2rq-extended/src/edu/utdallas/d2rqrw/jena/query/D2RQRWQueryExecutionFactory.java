package edu.utdallas.d2rqrw.jena.query;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Context;

import edu.utdallas.d2rqrw.ModelD2RQRW;

/**
 * <p>A factory class that constructs SPARQL queries based on the given model</p>
 */
public class D2RQRWQueryExecutionFactory 
{
	/** <p>Private constructor</p> **/
	private D2RQRWQueryExecutionFactory() { }
	
	/**
	 * <p>Method that creates a query execution given the query string and the model to query against</p>
	 * @param queryStr - the query as a string
	 * @param model - the model to be queried
	 * @return a query execution object
	 * 
	 * @see com.hp.hpl.jena.query.QueryExecutionFactory#create(String, Model)
	 */
	public static QueryExecution create( String queryStr, Model model )
    {
        checkArg( queryStr ) ;
        if( ( ( ModelD2RQRW ) model ).getIsRDBModelCreated() )
        {
        	Model union = model.union( ( ( ModelD2RQRW ) model ).getRDBModel() );
        	checkArg( union ) ;
        	return create( makeQuery( queryStr ), union ) ;
        }
        else
        {
        	checkArg( model ) ;
        	return create( makeQuery( queryStr ), model ) ;        	
        }
    }

	/**
	 * <p>Method that creates a Query given the query as a string</p>
	 * @param queryStr - the query as a string
	 * @return a Query object that represents the input query
	 */
    private static Query makeQuery( String queryStr )
    { return QueryFactory.create( queryStr ); }

    /**
	 * <p>Method that creates a query execution given the query and the model to query against</p>
	 * @param query - the input query
	 * @param model - the model to be queried
	 * @return a query execution object
	 * 
     * @see com.hp.hpl.jena.query.QueryExecutionFactory#create(Query, Model)
     */
    public static QueryExecution create( Query query, Model model )
    {
        checkArg( query ) ;
        checkArg( model ) ;
        return make( query, new DatasetImpl( model ) ) ;
    }

    /**
     * <p>Method that constructs a QueryExecution object from the given query and dataset</p>
     * @param query - the Query object
     * @param dataset - the input dataset for the given model
     * @return a QueryExecution object
     */
    private static QueryExecution make( Query query, Dataset dataset )
    { return make( query, dataset, null ) ; }

    /**
     * <p>Method that constructs a QueryExecution object from the given query, dataset, and context</p>
     * @param query - the Query object
     * @param dataset - the input dataset for the given model
     * @param context - the context for this query
     * @return a QueryExecution object
     */
    private static QueryExecution make( Query query, Dataset dataset, Context context )
    {
        query.validate() ;
        if ( context == null ) context = ARQ.getContext().copy();
        DatasetGraph dsg = null ;
        if ( dataset != null ) dsg = dataset.asDatasetGraph() ;
        QueryEngineFactory f = findFactory( query, dsg, context );
        if ( f == null )
        {
            ALog.warn( QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: " + query );
            return null ;
        }
        return new QueryExecutionBase( query, dataset, context, f );
    }

    /**
     * <p>Method that constructs a QueryExecution object from the given query, dataset graph, and context</p>
     * @param query - the Query object
     * @param dataset - the input dataset graph for the given model
     * @param context - the context for this query
     * @return the query engine factory
     */
    private static QueryEngineFactory findFactory( Query query, DatasetGraph dataset, Context context )
    {
        return QueryEngineRegistry.get().find( query, dataset, context );
    }

    /**
     * <p>Method that checks if the given object is null</p>
     * @param obj - the object to be checked
     * @param msg - the error message
     */
    private static void checkNotNull( Object obj, String msg )
    { if ( obj == null ) throw new IllegalArgumentException( msg ) ; }
    
    /**
     * <p>Method to check if the model is null</p>
     * @param model - the input model
     */
    private static void checkArg( Model model )
    { checkNotNull( model, "Model is a null pointer" ); }

    /**
     * <p>Method to check if the query is null</p>
     * @param queryStr - the input query as a string
     */
    private static void checkArg( String queryStr )
    { checkNotNull( queryStr, "Query string is null" ); }

    /**
     * <p>Method to check if the query is null</p>
     * @param query - the input query
     */
    static private void checkArg( Query query )
    { checkNotNull( query, "Query is null" ); }
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