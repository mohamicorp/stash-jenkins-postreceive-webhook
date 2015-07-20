package com.nerdwin15.stash.webhook.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import com.atlassian.stash.server.ApplicationPropertiesService;
import com.nerdwin15.stash.webhook.ClientKeyStore;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the ConcreteHttpClientFactory test
 * 
 * @author Michael Irwin (mikesir87)
 */
public class ConcreteHttpClientFactoryTest {

  private InstrumentedConcreteHttpClientFactory factory;
  private ApplicationPropertiesService applicationPropertiesService;
  private ClientKeyStore clientStore;

  /**
   * Setup tasks
   */
  @Before
  public void setup() {
    factory = new InstrumentedConcreteHttpClientFactory();
    applicationPropertiesService = mock(ApplicationPropertiesService.class);
    when(applicationPropertiesService.getPluginProperty(eq("keyStore"))).thenReturn(null);
    clientStore = new ClientKeyStore(applicationPropertiesService);
  }

  /**
   * Validate the non-SSL path for configuration
   */
  @Test
  public void validateNonSslGeneration() throws Exception {
    factory.getHttpClient(false, false, clientStore);
    assertFalse(factory.ignoreUnverifiedSSL);

    factory.getHttpClient(false, true, clientStore);
    assertFalse(factory.ignoreUnverifiedSSL);
  }

  /**
   * Validate that not trusting all certs works as expected
   */
  @Test
  public void validateUsingDefaultCertificates() throws Exception {
    factory.getHttpClient(true, false, clientStore);
    assertFalse(factory.ignoreUnverifiedSSL);
  }

  /**
   * Validate that if all certs are trusted, the custom configuration was used
   */
  @Test
  public void validateIgnoringSslCertValidation() throws Exception {
    factory.getHttpClient(true, true, clientStore);
    assertTrue(factory.ignoreUnverifiedSSL);
  }

  /**
   * An instrumented extension of the ConcreteHttpClientFactory that delegates
   * all functionality to the parent, but checks that various methods are
   * actually being called as expected.
   * 
   * @author Michael Irwin
   */
  private class InstrumentedConcreteHttpClientFactory 
      extends ConcreteHttpClientFactory {
    public boolean ignoreUnverifiedSSL = false;

    @Override
    public SSLContextBuilder ignoreUnverifiedSSL(SSLContextBuilder customContext) throws KeyStoreException, NoSuchAlgorithmException {
      ignoreUnverifiedSSL = true;
      return super.ignoreUnverifiedSSL(customContext);
    }
  }

}
