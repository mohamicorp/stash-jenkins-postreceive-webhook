package com.nerdwin15.stash.webhook.service;

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
import java.io.FileInputStream;
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
    public HttpClient getHttpClient(Boolean usingSsl, Boolean trustAllCerts)
            throws Exception {
        return createHttpClient(usingSsl, trustAllCerts);
    }

    /**
     * Create a new HttpClient.
     *
     *
     * @param usingSsl using SSL
     * @param useConfigured True if the client should be configured to accept any
     *                      certificate.
     * @return The requested HttpClient
     * @throws Exception
     */
    protected HttpClient createHttpClient(Boolean usingSsl, boolean useConfigured) throws Exception {

        HttpClientBuilder builder = HttpClientBuilder.create();

        if (usingSsl) {
            try {
                SSLConnectionSocketFactory sslConnSocketFactory
                        = new SSLConnectionSocketFactory(buildSslContext(useConfigured));
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
     * @return the updated SSL context
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws KeyStoreException
     */
    private SSLContext buildSslContext(boolean ignoreUnverifiedSSL) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, KeyStoreException, IOException, CertificateException {

        SSLContextBuilder customContext = SSLContexts.custom();
        if (ignoreUnverifiedSSL) {
            customContext = ignoreUnverifiedSSL(customContext);
        }

        System.out.println("Checking for keystore = "+System.getenv("STASH_KEYSTORE_PATH"));


        // We need to allow client certs to be presented to the server, we
        // will allow the system administrator to register a key store
        // and password that we can load
        if (System.getenv("STASH_KEYSTORE_PATH") != null) {
            KeyStore ks = getKeyStore();

            System.out.println("Loading keystore "+System.getenv("STASH_KEYSTORE_PATH"));

            char[] password = null;
            if (System.getenv("STASH_KEYSTORE_PASSWORD") != null)
                password = System.getenv("STASH_KEYSTORE_PASSWORD").toCharArray();

            ks.load(new FileInputStream(System.getenv("STASH_KEYSTORE_PATH")), password);

            customContext.loadKeyMaterial(ks,password);
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

    /**
     * Get a local keystore,  based on the default type or the type specified
     *
     * @return
     * @throws KeyStoreException
     */
    public KeyStore getKeyStore() throws KeyStoreException {
        if (System.getenv("STASH_KEYSTORE_TYPE") != null)
            return KeyStore.getInstance(System.getenv("STASH_KEYSTORE_TYPE"));
        else
            return KeyStore.getInstance(KeyStore.getDefaultType());
    }
}