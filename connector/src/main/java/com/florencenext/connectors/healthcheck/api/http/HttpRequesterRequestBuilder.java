package com.florencenext.connectors.healthcheck.api.http;

import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;

import org.mule.extension.http.api.HttpMessageBuilder;
import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

public class HttpRequesterRequestBuilder extends HttpMessageBuilder {
	 /**
	   * The body of the response message
	   */
	  @Parameter
	  @Optional
	  @Content(primary = true)
	  private TypedValue<Object> body;

	  /**
	   * HTTP headers the message should include.
	   */
	  @Parameter
	  @Optional
	  @Content
	  @NullSafe
	  protected MultiMap<String, String> headers = emptyMultiMap();

	  /**
	   * URI parameters that should be used to create the request.
	   */
	  @Parameter
	  @Optional
	  @Content
	  @DisplayName("URI Parameters")
	  private MultiMap<String, String> uriParams = emptyMultiMap();

	  /**
	   * Query parameters the request should include.
	   */
	  @Parameter
	  @Optional
	  @Content
	  @DisplayName("Query Parameters")
	  private MultiMap<String, String> queryParams = emptyMultiMap();


	  
	  @Override
	  public TypedValue<Object> getBody() {
	    return body;
	  }

	  @Override
	  public void setBody(TypedValue<Object> body) {
	    this.body = body;
	  }

	  @Override
	  public MultiMap<String, String> getHeaders() {
	    return headers;
	  }

	  @Override
	  public void setHeaders(MultiMap<String, String> headers) {
	    this.headers = headers != null ? headers : emptyMultiMap();
	  }

	  public MultiMap<String, String> getQueryParams() {
	    return queryParams.toImmutableMultiMap();
	  }

	  public MultiMap<String, String> getUriParams() {
	   // return unmodifiableMap(uriParams);
		  return this.uriParams;
	  }

	  public void setQueryParams(MultiMap<String, String> queryParams) {
	    this.queryParams = queryParams;
	  }

	  public void setUriParams(MultiMap<String, String> uriParams) {
	    this.uriParams = uriParams;
	  }

	  public HttpRequestBuilder configure(HttpRequesterConfig config) {
	    return HttpRequest.builder(PRESERVE_HEADER_CASE || config.isPreserveHeadersCase())
	        .headers(headers)
	        .queryParams(queryParams);
	  }
	  
}
