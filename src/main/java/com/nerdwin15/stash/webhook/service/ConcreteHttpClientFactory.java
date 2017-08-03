package com.nerdwin15.stash.webhook.service;

import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;

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

  private static final Integer HTTP_PORT = 80;
  private static final Integer HTTPS_PORT = 443;
  
  /**
   * {@inheritDoc}
   */
  @Override
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
  protected HttpClient createHttpClient(boolean useConfigured) throws Exception {
    SchemeRegistry schemeRegistry;
    DefaultHttpClient client;
    if (useConfigured) {
        SSLContext sslContext = createContext();
        schemeRegistry = createScheme(sslContext);
        client = new DefaultHttpClient(
            new BasicClientConnectionManager(schemeRegistry));
    } else {
        client = new DefaultHttpClient();
        schemeRegistry = client.getConnectionManager().getSchemeRegistry();
    }

    client.setRoutePlanner(new ProxySelectorRoutePlanner(schemeRegistry,
        ProxySelector.getDefault()));
    return client;
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
    SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext);
    socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    schemeRegistry.register(new Scheme("https", HTTPS_PORT, socketFactory));
    schemeRegistry.register(
        new Scheme("http", HTTP_PORT, PlainSocketFactory.getSocketFactory()));
    return schemeRegistry;
  }

}
