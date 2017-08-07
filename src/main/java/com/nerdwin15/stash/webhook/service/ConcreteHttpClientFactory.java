package com.nerdwin15.stash.webhook.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

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
    HttpClientBuilder builder = HttpClientBuilder.create();
    if (useConfigured) {
      SSLContext sslContext = createContext();

      HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(createRegistry(sslContext));
      builder.setConnectionManager(ccm);

      builder.setSSLContext(sslContext);
    }

    builder.useSystemProperties();

    return builder.build();
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
  protected Registry<ConnectionSocketFactory> createRegistry(SSLContext sslContext)
      throws Exception {
    SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext,
      NoopHostnameVerifier.INSTANCE);
    return RegistryBuilder.<ConnectionSocketFactory>create()
      .register("https", sslConnectionFactory)
      .register("http", PlainConnectionSocketFactory.getSocketFactory())
      .build();
  }

}
