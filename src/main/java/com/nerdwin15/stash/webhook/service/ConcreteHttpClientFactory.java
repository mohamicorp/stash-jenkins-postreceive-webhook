package com.nerdwin15.stash.webhook.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;

/**
 * An implementation of the {@link HttpClientFactory} that returns a
 * DefaultHttpClient that is either not configured at all (non-ssl and default
 * trusts) or configured to accept all certificates.  If told to accept all
 * certificates, an unsafe X509 trust manager is used.
 * 
 * If setup of the "trust-all" HttpClient fails, a non-configured HttpClient
 * is returned.
 * 
 * @author Michael Irwin (mikesir87)
 *
 */
public class ConcreteHttpClientFactory implements HttpClientFactory {

	private static final Integer SSL_PORT = 443;
	
  /**
   * {@inheritDoc}
   */
  public HttpClient getHttpClient(Boolean usingSsl, Boolean trustAllCerts) 
      throws Exception {
    return createHttpClient(usingSsl && trustAllCerts);
  }

  /**
   * Create a new HttpClient.
   * @param useConfigured True if the client should be configured to accept any
   * certificate.
   * @return The requested HttpClient
   * @throws Exception
   */
  protected HttpClient createHttpClient(Boolean useConfigured) 
  		throws Exception {
    if (useConfigured)
      return configuredClient();
    return new DefaultHttpClient();
  }

  /**
   * Creates a HttpClient that is configured to accept any certificate.
   * @return The configured certificate
   * @throws Exception
   */
  protected HttpClient configuredClient() throws Exception {
    SSLContext sslContext = createContext();
    SchemeRegistry schemeRegistry = createScheme(sslContext);
    return new DefaultHttpClient(
        new BasicClientConnectionManager(schemeRegistry));
  }

  /**
   * Creates an SSL context
   * @return The SSL context
   * @throws NoSuchAlgorithmException
   * @throws KeyManagementException
   */
  protected SSLContext createContext() throws NoSuchAlgorithmException, 
      KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(
        null, 
        new TrustManager[] { new UnsafeX509TrustManager() }, 
        new SecureRandom());
    return sslContext;
  }

  /**
   * Creates the SSL SchemeRegistry
   * @param sslContext The SSL Context the scheme registry should use.
   * @return The SSL SchemeRegistry
   * @throws Exception
   */
  protected SchemeRegistry createScheme(SSLContext sslContext) 
      throws Exception {
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(
        new Scheme("https", SSL_PORT, new SSLSocketFactory(sslContext)));
    return schemeRegistry;
  }

}