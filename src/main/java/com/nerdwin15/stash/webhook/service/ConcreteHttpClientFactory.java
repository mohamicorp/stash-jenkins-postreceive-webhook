package com.nerdwin15.stash.webhook.service;

import com.nerdwin15.stash.webhook.ClientKeyStore;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * An implementation of the {@link HttpClientFactory} that returns a
 * DefaultHttpClient that is either not configured at all (non-ssl and default
 * trusts) or configured to accept all certificates.  If told to accept all
 * certificates, an unsafe X509 trust manager is used.
 * <p>
 * If setup of the "trust-all" HttpClient fails, a non-configured HttpClient
 * is returned.
 *
 * @author Michael Irwin (mikesir87)
 * @author Philip Dodds (pdodds)
 */
public class ConcreteHttpClientFactory implements HttpClientFactory {

    private static final Integer HTTP_PORT = 80;
    private static final Integer HTTPS_PORT = 443;
    private KeyStore keyStore;

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpClient getHttpClient(Boolean usingSsl, Boolean trustAllCerts, ClientKeyStore clientKeyStore)
            throws Exception {
        return createHttpClient(usingSsl, trustAllCerts, clientKeyStore);
    }

    /**
     * Create a new HttpClient.
     *
     *
     * @param usingSsl using SSL
     * @param useConfigured True if the client should be configured to accept any
     *                      certificate.
     * @param clientKeyStore
     * @return The requested HttpClient
     * @throws Exception
     */
    protected HttpClient createHttpClient(Boolean usingSsl, boolean useConfigured, ClientKeyStore clientKeyStore) throws Exception {

        HttpClientBuilder builder = HttpClientBuilder.create();

        if (usingSsl) {
            try {
                SSLConnectionSocketFactory sslConnSocketFactory
                        = new SSLConnectionSocketFactory(buildSslContext(useConfigured, clientKeyStore));
                builder.setSSLSocketFactory(sslConnSocketFactory);

                Registry<ConnectionSocketFactory> registry
                        = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslConnSocketFactory)
                        .build();

                HttpClientConnectionManager ccm
                        = new BasicHttpClientConnectionManager(registry);

                builder.setConnectionManager(ccm);
            } catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Unable to connect to Jenkins due to algorithm exception", nsae);
            } catch (KeyManagementException kme) {
                throw new RuntimeException("Unable to connect to Jenkins due to key management exception", kme);
            } catch (KeyStoreException kse) {
                throw new RuntimeException("Unable to connect to Jenkins due to key store exception", kse);
            }
        }

        return builder.build();

    }

    /**
     * Prepare the SSL Context,  this will determine whether to ignore the unverified
     * and also can look to see if an environment setting is going to cause us to load
     * a keystore
     *
     * @param ignoreUnverifiedSSL
     * @param clientKeyStore
     * @return the updated SSL context
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws KeyStoreException
     */
    private SSLContext buildSslContext(boolean ignoreUnverifiedSSL, ClientKeyStore clientKeyStore) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, KeyStoreException, IOException, CertificateException {

        SSLContextBuilder customContext = SSLContexts.custom();
        if (ignoreUnverifiedSSL) {
            customContext = ignoreUnverifiedSSL(customContext);
            if (clientKeyStore.isAvailable()) {
                customContext.loadKeyMaterial(clientKeyStore.getKeyStore(),clientKeyStore.getPassword());
            }
        }

        return customContext.build();
    }

    /**
     * Setup SSL context to ignore unverified SSL connections
     *
     * @param customContext
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    public SSLContextBuilder ignoreUnverifiedSSL(SSLContextBuilder customContext) throws KeyStoreException, NoSuchAlgorithmException {
        TrustStrategy easyStrategy = new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] chain, String authType) {
                return true;
            }
        };
        customContext = customContext
                .loadTrustMaterial(null, easyStrategy);

        return customContext;
    }

}