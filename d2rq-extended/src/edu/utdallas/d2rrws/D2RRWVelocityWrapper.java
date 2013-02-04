package edu.utdallas.d2rrws;

import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import de.fuberlin.wiwiss.pubby.negotiation.ContentTypeNegotiator;
import de.fuberlin.wiwiss.pubby.negotiation.MediaRangeSpec;

public class D2RRWVelocityWrapper 
{
	private final static String VELOCITY_ENGINE_INSTANCE = 
		"de.fuberlin.wiwiss.d2rs.VelocityHelper.VELOCITY_ENGINE_INSTANCE";
	private final static String VELOCITY_DEFAULT_CONTEXT = 
		"de.fuberlin.wiwiss.d2rs.VelocityHelper.VELOCITY_DEFAULT_CONTEXT";

	private final static String TEXTHTML_CONTENTTYPE = "text/html; charset=utf-8";
	private final static String APPLICATIONXML_CONTENTTYPE = "application/xhtml+xml; charset=utf-8";

	private final static ContentTypeNegotiator xhtmlNegotiator;

	static {
		xhtmlNegotiator = new ContentTypeNegotiator();

		// for clients that send nothing
		xhtmlNegotiator.setDefaultAccept(TEXTHTML_CONTENTTYPE);

		// for MSIE that sends */* without q
		xhtmlNegotiator.addUserAgentOverride(Pattern.compile("MSIE"), null, TEXTHTML_CONTENTTYPE);

		xhtmlNegotiator.addVariant(APPLICATIONXML_CONTENTTYPE + "; q=0.9");
		xhtmlNegotiator.addVariant(TEXTHTML_CONTENTTYPE + "; q=0.8");
	}


	public static synchronized void initEngine(D2RRWServer d2r, ServletContext servletContext) {
		try {
			VelocityEngine engine = new VelocityEngine(servletContext.getRealPath("/WEB-INF/velocity.properties"));
			engine.init();
			servletContext.setAttribute(VELOCITY_ENGINE_INSTANCE, engine);
			servletContext.setAttribute(VELOCITY_DEFAULT_CONTEXT, initDefaultContext(d2r));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static Context initDefaultContext(D2RRWServer server) {
		Context context = new VelocityContext();
		context.put("truncated_results", new Boolean(server.hasTruncatedResults()));
		context.put("server_name", server.serverName());
		context.put("home_link", server.baseURI());
		return context;
	}

	private final VelocityEngine engine;
	private final Context context;
	private final HttpServletRequest request;
	private final HttpServletResponse response;

	public D2RRWVelocityWrapper(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response) {
		engine = (VelocityEngine) servlet.getServletContext().getAttribute(VELOCITY_ENGINE_INSTANCE);
		// TODO: Init context with default variables shared by all/many servlets
		Context defaultContext = (Context) servlet.getServletContext().getAttribute(VELOCITY_DEFAULT_CONTEXT);
		context = new VelocityContext(defaultContext);
		this.request = request;
		this.response = response;
	}

	public Context getContext() {
		return context;
	}

	public void mergeTemplateXHTML(String templateName) {
		MediaRangeSpec bestMatch = xhtmlNegotiator.getBestMatch(
				request.getHeader("Accept"), request.getHeader("User-Agent"));
		response.addHeader("Content-Type", bestMatch != null ? bestMatch.getMediaType() : TEXTHTML_CONTENTTYPE);
		response.addHeader("Vary", "Accept, User-Agent");

		response.addHeader("Cache-Control", "no-cache");
		response.addHeader("Pragma", "no-cache");
		try {
			engine.mergeTemplate(templateName, context, response.getWriter());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
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