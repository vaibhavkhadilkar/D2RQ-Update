package edu.utdallas.d2rrws;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

import de.fuberlin.wiwiss.d2rs.VelocityWrapper;
import edu.utdallas.d2rqrw.GraphD2RQRW;

public class RootServlet extends HttpServlet 
{	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
	{
		D2RRWServer server = D2RRWServer.fromServletContext(getServletContext());
		server.checkMappingFileChanged();
		Map classMapLinks = new TreeMap();
		Iterator it = graphD2RQRW().classMapNames().iterator();
		while (it.hasNext()) 
		{
			String name = (String) it.next();
			classMapLinks.put(name, server.baseURI() + "directory/" + name);
		}
		VelocityWrapper velocity = new VelocityWrapper(this, request, response);
		Context context = velocity.getContext();
		context.put("rdf_link", server.baseURI() + "all");
		context.put("classmap_links", classMapLinks);
		velocity.mergeTemplateXHTML("root_page.vm");
	}

	private GraphD2RQRW graphD2RQRW()
	{
		return (GraphD2RQRW) D2RRWServer.fromServletContext(getServletContext()).currentGraph();
	}

	private static final long serialVersionUID = 8398973058486421941L;
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