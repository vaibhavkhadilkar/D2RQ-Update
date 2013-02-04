package edu.utdallas.d2rrws;

import java.util.Map;

import org.joseki.DatasetDesc;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;

public class D2RQRWDatasetDesc extends DatasetDesc 
{
	private D2RRWAutoReloadableDataset dataset;
	
	public D2RQRWDatasetDesc(D2RRWAutoReloadableDataset dataset) 
	{
		super(null);
		this.dataset = dataset;
	}

	public Dataset getDataset() 
	{
		dataset.checkMappingFileChanged();
		return this.dataset;
	}

	public void clearDataset() { this.dataset = null; }

	public void freeDataset() { this.dataset = null; }

	public void setDefaultGraph(Resource dftGraph) 
	{ throw new RuntimeException("D2RQDatasetDecl.setDefaultGraph is not implemented"); }
	
	public Resource getDefaultGraph() 
	{ throw new RuntimeException("D2RQDatasetDecl.getDefaultGraph is not implemented"); }

	public void addNamedGraph(String uri, Resource r) 
	{ throw new RuntimeException("D2RQDatasetDecl.addNamedGraph is not implemented"); }

	@SuppressWarnings("unchecked")
	public Map getNamedGraphs() 
	{ throw new RuntimeException("D2RQDatasetDecl.getNamedGraphs is not implemented"); }

	public String toString() { return "D2RQDatasetDecl(" + this.dataset + ")"; }
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