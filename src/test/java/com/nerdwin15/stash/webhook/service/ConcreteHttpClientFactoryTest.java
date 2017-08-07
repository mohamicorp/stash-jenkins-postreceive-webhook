package com.nerdwin15.stash.webhook.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the ConcreteHttpClientFactory test
 * 
 * @author Michael Irwin (mikesir87)
 */
public class ConcreteHttpClientFactoryTest {

  private InstrumentedConcreteHttpClientFactory factory;
  
  /**
   * Setup tasks
   */
  @Before
  public void setup() {
    factory = new InstrumentedConcreteHttpClientFactory();
  }
  
  /**
   * Validate the non-SSL path for configuration
   */
  @Test
  public void validateNonSslGeneration() throws Exception {
    factory.getHttpClient(false, false);
    assertFalse(factory.wasSslContextCreated());
    assertFalse(factory.wasSchemeRegistryCreated());

    factory.getHttpClient(false, true);
    assertFalse(factory.wasSslContextCreated());
    assertFalse(factory.wasSchemeRegistryCreated());
  }

  /**
   * Validate that not trusting all certs works as expected
   */
  @Test
  public void validateUsingDefaultCertificates() throws Exception {
    factory.getHttpClient(true, false);
    assertFalse(factory.wasSslContextCreated());
    assertFalse(factory.wasSchemeRegistryCreated());
  }

  /**
   * Validate that if all certs are trusted, the custom configuration was used
   */
  @Test
  public void validateIgnoringSslCertValidation() throws Exception {
    factory.getHttpClient(true, true);
    assertTrue(factory.wasSslContextCreated());
    assertTrue(factory.wasSchemeRegistryCreated());
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
    private boolean sslContextCreated = false;
    private boolean schemeRegistryCreated = false;

    public boolean wasSchemeRegistryCreated() {
      return schemeRegistryCreated;
    }

    public boolean wasSslContextCreated() {
      return sslContextCreated;
    }

    @Override
    protected SSLContext createContext() throws NoSuchAlgorithmException,
        KeyManagementException {
      sslContextCreated = true;
      return super.createContext();
    }

    @Override
    protected Registry<ConnectionSocketFactory> createRegistry(SSLContext sslContext) throws Exception  {
      schemeRegistryCreated = true;
      return super.createRegistry(sslContext);
    }
  }
}
