/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activiti.service.activiti;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.activiti.service.AttachmentResponseInfo;
import com.activiti.service.ResponseInfo;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class ActivitiClientService {

	private final static Logger log = LoggerFactory.getLogger(ActivitiClientService.class);

	protected static final String[] PAGING_AND_SORTING_PARAMETER_NAMES = new String[] {"sort", "order", "size"};
	
	public static final String DEFAULT_ACTIVITI_CONTEXT_ROOT = "activiti-rest";
	public static final String DEFAULT_ACTIVITI_REST_ROOT = "service";

	@Autowired
	protected ServerConfigService serverConfigService;

	@Autowired
	protected ObjectMapper objectMapper;

    public CloseableHttpClient getHttpClient(ServerConfig serverConfig) {
        return getHttpClient(serverConfig.getUserName(), serverConfigService.decrypt(serverConfig.getPassword()));
    }

	public CloseableHttpClient getHttpClient(String userName, String password) {

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));

        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            log.warn("Could not configure HTTP client to use SSL" , e);
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        if (sslsf != null) {
            httpClientBuilder.setSSLSocketFactory(sslsf);
        }

        return httpClientBuilder.build();
	}

	/**
	 * Execute the given request. Will return the parsed JSON present in the response-body, in case the status code is 200 - OK.
	 * In case the response returns a different status-code, an {@link ActivitiServiceException} is thrown with the error message received
	 * from the client, if possible.
	 */
	public JsonNode executeRequest(HttpUriRequest request, ServerConfig serverConfig) {
		return executeRequest(request, serverConfig, HttpStatus.SC_OK);
	}

    public JsonNode executeRequest(HttpUriRequest request, ServerConfig serverConfig, int expectedStatusCode) {
        return executeRequest(request, serverConfig.getUserName(), serverConfigService.decrypt(serverConfig.getPassword()), expectedStatusCode);
    }

    public JsonNode executeRequest(HttpUriRequest request, String userName, String password) {
        return executeRequest(request, userName, password, HttpStatus.SC_OK);
    }

	/**
	 * Execute the given request. Will return the parsed JSON present in the response-body, in case the status code is as expected.
	 * In case the response returns a different status-code, an {@link ActivitiServiceException} is thrown with the error message received
	 * from the client, if possible.
	 */
	public JsonNode executeRequest(HttpUriRequest request, String userName, String password, int expectedStatusCode) {

		ActivitiServiceException exception = null;
        CloseableHttpClient client = getHttpClient(userName, password);
		try {
            CloseableHttpResponse response = client.execute(request);

			try {
				InputStream responseContent = response.getEntity().getContent();
				String strResponse = IOUtils.toString(responseContent);

				boolean success = response.getStatusLine() != null && response.getStatusLine().getStatusCode() == expectedStatusCode;
				if (success) {
				    JsonNode bodyNode = objectMapper.readTree(strResponse);
					return bodyNode;

				} else {
				    JsonNode bodyNode = null;
				    try {
				        bodyNode = objectMapper.readTree(strResponse);
				    } catch (Exception e) {
				        log.debug("Error parsing error message", e);
				    }
					exception = new ActivitiServiceException(extractError(bodyNode, "An error occured while calling Activiti: " + response.getStatusLine()));
				}
			} catch (Exception e) {
				log.warn("Error consuming response from uri " + request.getURI(), e);
				exception = wrapException(e, request);
			} finally {
				response.close();
			}

		} catch (Exception e) {
			log.error("Error executing request to uri " + request.getURI(), e);
			exception = wrapException(e, request);
		} finally {
			try {
				client.close();
			} catch (Exception e) {
				log.warn("Error closing http client instance", e);
			}
		}

		if(exception != null) {
			throw exception;
		}

		return null;
	}
	
	public JsonNode executeDownloadRequest(HttpUriRequest request, HttpServletResponse httpResponse, ServerConfig serverConfig) {
        return executeDownloadRequest(request, httpResponse, serverConfig, HttpStatus.SC_OK);
    }

	public JsonNode executeDownloadRequest(HttpUriRequest request, HttpServletResponse httpResponse, ServerConfig serverConfig, int expectedStatusCode) {
        return executeDownloadRequest(request, httpResponse, serverConfig.getUserName(), serverConfigService.decrypt(serverConfig.getPassword()), expectedStatusCode);
    }
	
	public JsonNode executeDownloadRequest(HttpUriRequest request, HttpServletResponse httpResponse, String userName, String password, int expectedStatusCode) {

        ActivitiServiceException exception = null;
        CloseableHttpClient client = getHttpClient(userName, password);
        try {
            CloseableHttpResponse response = client.execute(request);
            try {
                boolean success = response.getStatusLine() != null && response.getStatusLine().getStatusCode() == expectedStatusCode;
                if (success) {
                    httpResponse.setHeader("Content-Disposition", response.getHeaders("Content-Disposition")[0].getValue());
                    response.getEntity().writeTo(httpResponse.getOutputStream());
                    return null;

                } else {
                    JsonNode bodyNode = null;
                    String strResponse = IOUtils.toString(response.getEntity().getContent());
                    try {
                        bodyNode = objectMapper.readTree(strResponse);
                    } catch (Exception e) {
                        log.debug("Error parsing error message", e);
                    }
                    exception = new ActivitiServiceException(extractError(bodyNode, "An error occured while calling Activiti: " + response.getStatusLine()));
                }
            } catch (Exception e) {
                log.warn("Error consuming response from uri " + request.getURI(), e);
                exception = wrapException(e, request);
            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("Error executing request to uri " + request.getURI(), e);
            exception = wrapException(e, request);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing http client instance", e);
            }
        }

        if(exception != null) {
            throw exception;
        }

        return null;
    }
	
	public AttachmentResponseInfo executeDownloadRequest(HttpUriRequest request, ServerConfig serverConfig) {
        return executeDownloadRequest(request, serverConfig, HttpStatus.SC_OK);
    }

    public AttachmentResponseInfo executeDownloadRequest(HttpUriRequest request, ServerConfig serverConfig, Integer... expectedStatusCodes) {
        return executeDownloadRequest(request, serverConfig.getUserName(), serverConfigService.decrypt(serverConfig.getPassword()), expectedStatusCodes);
    }

    public AttachmentResponseInfo executeDownloadRequest(HttpUriRequest request, String userName, String password) {
        return executeDownloadRequest(request, userName, password, HttpStatus.SC_OK);
    }
	
	public AttachmentResponseInfo executeDownloadRequest(HttpUriRequest request, String userName, String password, Integer... expectedStatusCodes) {
	    ActivitiServiceException exception = null;
        CloseableHttpClient client = getHttpClient(userName, password);
        try {
            CloseableHttpResponse response = client.execute(request);

            try {
                int statusCode = -1;
                if (response.getStatusLine() != null) {
                    statusCode = response.getStatusLine().getStatusCode();
                }
                boolean success = Arrays.asList(expectedStatusCodes).contains(statusCode);
                if (success) {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        String contentDispositionFileName[] = response.getHeaders("Content-Disposition")[0].getValue().split("=");
                        String fileName = contentDispositionFileName[contentDispositionFileName.length - 1];
                        return new AttachmentResponseInfo(fileName, IOUtils.toByteArray(response.getEntity().getContent()));
                    } else {
                        return new AttachmentResponseInfo(statusCode, readJsonContent(response.getEntity().getContent()));
                    }
                    
                } else {
                    exception = new ActivitiServiceException(extractError(readJsonContent(response.getEntity().getContent()), "An error occured while calling Activiti: " + response.getStatusLine()));
                }
            } catch (Exception e) {
                log.warn("Error consuming response from uri " + request.getURI(), e);
                exception = wrapException(e, request);
            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("Error executing request to uri " + request.getURI(), e);
            exception = wrapException(e, request);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing http client instance", e);
            }
        }

        if(exception != null) {
            throw exception;
        }

        return null;
	}
	
	public ResponseInfo execute(HttpUriRequest request, ServerConfig serverConfig) {
        return execute(request, serverConfig, HttpStatus.SC_OK);
    }

    public ResponseInfo execute(HttpUriRequest request, ServerConfig serverConfig, int... expectedStatusCodes) {
        return execute(request, serverConfig.getUserName(), serverConfigService.decrypt(serverConfig.getPassword()), expectedStatusCodes);
    }

    public ResponseInfo execute(HttpUriRequest request, String userName, String password, int... expectedStatusCodes) {

        ActivitiServiceException exception = null;
        CloseableHttpClient client = getHttpClient(userName, password);
        try {
            CloseableHttpResponse response = client.execute(request);

            try {
                JsonNode bodyNode = readJsonContent(response.getEntity().getContent());

                int statusCode = -1;
                if (response.getStatusLine() != null) {
                    statusCode = response.getStatusLine().getStatusCode();
                }
                boolean success = Arrays.asList(expectedStatusCodes).contains(statusCode);
                
                if (success) {
                    return new ResponseInfo(statusCode, bodyNode);

                } else {
                    exception = new ActivitiServiceException(extractError(readJsonContent(response.getEntity().getContent()), "An error occured while calling Activiti: " + response.getStatusLine()));
                }
            } catch (Exception e) {
                log.warn("Error consuming response from uri " + request.getURI(), e);
                exception = wrapException(e, request);
            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("Error executing request to uri " + request.getURI(), e);
            exception = wrapException(e, request);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing http client instance", e);
            }
        }

        if(exception != null) {
            throw exception;
        }

        return null;
    }
    
    public void execute(HttpUriRequest request, HttpServletResponse httpResponse, ServerConfig serverConfig) {
        execute(request, httpResponse, serverConfig.getUserName(), serverConfigService.decrypt(serverConfig.getPassword()));
    }

    public void execute(HttpUriRequest request, HttpServletResponse httpResponse, String userName, String password) {

        ActivitiServiceException exception = null;
        CloseableHttpClient client = getHttpClient(userName, password);
        try {
            CloseableHttpResponse response = client.execute(request);

            try {
                if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
                    httpResponse.setStatus(response.getStatusLine().getStatusCode());
                    if (response.getEntity() != null && response.getEntity().getContentType() != null) {
                        httpResponse.setContentType(response.getEntity().getContentType().getValue());
                        response.getEntity().writeTo(httpResponse.getOutputStream());
                    }
                } else {
                    exception = new ActivitiServiceException(extractError(readJsonContent(response.getEntity().getContent()), "An error occured while calling Activiti: " + response.getStatusLine()));
                }
            } catch (Exception e) {
                log.warn("Error consuming response from uri " + request.getURI(), e);
                exception = wrapException(e, request);
            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("Error executing request to uri " + request.getURI(), e);
            exception = wrapException(e, request);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing http client instance", e);
            }
        }

        if(exception != null) {
            throw exception;
        }

    }

	public String executeRequestAsString(HttpUriRequest request, ServerConfig serverConfig, int expectedStatusCode) {

    	ActivitiServiceException exception = null;
    	String result = null;
    	CloseableHttpClient client = getHttpClient(serverConfig);
    	try {
    		CloseableHttpResponse response = client.execute(request);
    		boolean success = response.getStatusLine() != null && response.getStatusLine().getStatusCode() == expectedStatusCode;
    		if (success) {
    			result = IOUtils.toString(response.getEntity().getContent());
    		} else {
    			String errorMessage = null;
    			try {
    				if (response.getEntity().getContentLength() != 0) {
    					InputStream responseContent = response.getEntity().getContent();
    					JsonNode errorBody = objectMapper.readTree(responseContent);
    					errorMessage = extractError(errorBody, "An error occured while calling Activiti: " + response.getStatusLine());
    				} else {
    					errorMessage = "An error was returned when calling the Activiti server";
    				}
    			} catch (Exception e) {
    				log.warn("Error consuming response from uri " + request.getURI(), e);
    				exception = wrapException(e, request);

    			} finally {
    				response.close();
    			}
    			exception = new ActivitiServiceException(errorMessage);
    		}
    	} catch (Exception e) {
    		log.error("Error executing request to uri " + request.getURI(), e);
    		exception = wrapException(e, request);

    	} finally {
    		try {
    			client.close();
    		} catch (Exception e) {
    			// No need to throw upwards, as this may hide exceptions/valid result
    			log.warn("Error closing http client instance", e);
    		}
    	}

    	if(exception != null) {
    		throw exception;
    	}

    	return result;
	}

	public ActivitiServiceException wrapException(Exception e, HttpUriRequest request) {
		if (e instanceof HttpHostConnectException) {
			return new ActivitiServiceException("Unable to connect to the Activiti server.");
		} else if (e instanceof ConnectTimeoutException) {
			return new ActivitiServiceException("Connection to the Activiti server timed out.");
		} else {
			// Use the raw exception message
			return new ActivitiServiceException(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/**
	 * Execute the given request, without using the response body.
	 * In case the response returns a different status-code than expected, an {@link ActivitiServiceException} is thrown with the error message received
	 * from the client, if possible.
	 */
	public void executeRequestNoResponseBody(HttpUriRequest request, ServerConfig serverConfig, int expectedStatusCode) {

		ActivitiServiceException exception = null;

		CloseableHttpClient client = getHttpClient(serverConfig);
		try {
            CloseableHttpResponse response = client.execute(request);
            boolean success = response.getStatusLine() != null &&
			        response.getStatusLine().getStatusCode() == expectedStatusCode;

			if (!success) {
				String errorMessage = null;
				try {
					if (response.getEntity() != null && response.getEntity().getContentLength() != 0) {
						InputStream responseContent = response.getEntity().getContent();
						JsonNode errorBody = objectMapper.readTree(responseContent);
						errorMessage = extractError(errorBody, "An error occured while calling Activiti: " + response.getStatusLine());

					} else {
						errorMessage = "An error was returned when calling the Activiti server";
					}
				} catch (Exception e) {
					log.warn("Error consuming response from uri " + request.getURI(), e);
					exception = wrapException(e, request);
				} finally {
					response.close();
				}
				exception = new ActivitiServiceException(errorMessage);
			}
		} catch (Exception e) {
			log.error("Error executing request to uri " + request.getURI(), e);
			exception = wrapException(e, request);

		} finally {
			try {
				client.close();
			} catch (Exception e) {
				// No need to throw upwards, as this may hide exceptions/valid result
				log.warn("Error closing http client instance", e);
			}
		}

		if(exception != null) {
			throw exception;
		}
	}

	public String extractError(JsonNode errorBody, String defaultValue) {
		if (errorBody != null && errorBody.isObject() && errorBody.has("errorMessage")) {
		    return errorBody.get("errorMessage").asText();
		}
		return defaultValue;
	}


	public HttpPost createPost(String uri, ServerConfig serverConfig) {
		HttpPost post = new HttpPost(getServerUrl(serverConfig, uri));
		post.setHeader("Content-Type", "application/json");
		post.setHeader("Accept", "application/json");
		return post;
	}

	public HttpPost createPost(URIBuilder builder, ServerConfig serverConfig) {
		HttpPost post = new HttpPost(getServerUrl(serverConfig, builder));
		post.setHeader("Content-Type", "application/json");
		post.setHeader("Accept", "application/json");
		return post;
	}

	public HttpPut createPut(URIBuilder builder, ServerConfig serverConfig) {
		HttpPut put = new HttpPut(getServerUrl(serverConfig, builder));
		put.setHeader("Content-Type", "application/json");
		put.setHeader("Accept", "application/json");
		return put;
	}

    public HttpDelete createDelete(URIBuilder builder, ServerConfig serverConfig) {
        HttpDelete delete = new HttpDelete(getServerUrl(serverConfig, builder));
        delete.setHeader("Content-Type", "application/json");
        delete.setHeader("Accept", "application/json");
        return delete;
    }

	public StringEntity createStringEntity(JsonNode json) {

		// add

		try {
			return new StringEntity(json.toString());
		} catch(Exception e) {
			log.warn("Error translation json to http client entity " + json, e);
		}
		return null;
	}

	public StringEntity createStringEntity(String json) {
		try {
			return new StringEntity(json);
		} catch(Exception e) {
			log.warn("Error translation json to http client entity " + json, e);
		}
		return null;
	}

    public String getServerUrl(ServerConfig serverConfig, String uri) {
        return getServerUrl(serverConfig.getContextRoot(), serverConfig.getRestRoot(),
            serverConfig.getServerAddress(), serverConfig.getPort(), uri);
    }

	public String getServerUrl(String contextRoot, String restRoot, String serverAddress, Integer port, String uri) {
		String actualContextRoot = null;
		if (contextRoot != null) {
		    actualContextRoot = stripSlashes(contextRoot);
		} else {
			actualContextRoot = DEFAULT_ACTIVITI_CONTEXT_ROOT;
		}
		
		String actualRestRoot = null;
		if (restRoot != null) {
			actualRestRoot = stripSlashes(restRoot);
		} else {
			actualRestRoot = DEFAULT_ACTIVITI_REST_ROOT;
		}

		String finalUrl = serverAddress + ":" + port;
		if (StringUtils.isNotEmpty(actualContextRoot)) {
		    finalUrl += "/" + actualContextRoot;
		}
		
		if (StringUtils.isNotEmpty(actualRestRoot)) {
            finalUrl += "/" + actualRestRoot;
        }
		
		URIBuilder builder = createUriBuilder(finalUrl + "/" + uri);

		return builder.toString();
	}

	public String getAppServerUrl(ServerConfig serverConfig, String uri) {
        String contextRoot = null;
        if (StringUtils.isNotEmpty(serverConfig.getContextRoot())) {
            contextRoot = stripSlashes(serverConfig.getContextRoot());
        } else {
            contextRoot = DEFAULT_ACTIVITI_CONTEXT_ROOT;
        }

        return "http://" + serverConfig.getServerAddress() + ":" + serverConfig.getPort()
                + "/" + contextRoot + "/" + uri;
    }

	public URIBuilder createUriBuilder(String url) {
		try {
			return new URIBuilder(url);
		} catch (URISyntaxException e) {
			throw new ActivitiServiceException("Error while creating Activiti endpoint URL: " + e.getMessage());
		}
	}

	public String getServerUrl(ServerConfig serverConfig, URIBuilder builder) {
		try {
			return getServerUrl(serverConfig, builder.build().toString());
		} catch (URISyntaxException e) {
			throw new ActivitiServiceException("Error while creating Activiti endpoint URL: " + e.getMessage());
		}
	}

	public String getUriWithPagingAndOrderParameters(URIBuilder builder, JsonNode bodyNode) throws URISyntaxException {
		addParameterToBuilder("size", bodyNode, builder);
		addParameterToBuilder("sort", bodyNode, builder);
		addParameterToBuilder("order", bodyNode, builder);
		return builder.build().toString();
	}

	public void addParameterToBuilder(String name, JsonNode bodyNode, URIBuilder builder) {
		JsonNode nameNode = bodyNode.get(name);
		if (nameNode != null && nameNode.isNull() == false) {
			builder.addParameter(name, nameNode.asText());
			((ObjectNode) bodyNode).remove(name);
		}
	}

	protected String stripSlashes(String url) {
		if (url.startsWith("/")) {
			url = url.substring(1);
		}
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}
	
	protected JsonNode readJsonContent(InputStream requestContent) {
        try {
            return objectMapper.readTree(IOUtils.toString(requestContent));
        } catch (Exception e) {
            log.debug("Error parsing error message", e);
        }
        return null;
	}

}
