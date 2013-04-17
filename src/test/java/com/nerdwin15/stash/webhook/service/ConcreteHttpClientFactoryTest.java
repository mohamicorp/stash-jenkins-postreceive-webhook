package com.nerdwin15.stash.webhook.service;

import static org.junit.Assert.*;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.junit.Before;
import org.junit.Test;

public class ConcreteHttpClientFactoryTest {

	private InstrumentedConcreteHttpClientFactory factory;
	
	@Before
	public void setup() {
		factory = new InstrumentedConcreteHttpClientFactory();
	}
	
	@Test
	public void validateNonSslGeneration() throws Exception {
		factory.getHttpClient(false, false);
		assertFalse(factory.wasClientCustomConfigured());
		assertFalse(factory.wasSslContextCreated());
		assertFalse(factory.wasSchemeRegistryCreated());

		factory.getHttpClient(false, true);
		assertFalse(factory.wasClientCustomConfigured());
		assertFalse(factory.wasSslContextCreated());
		assertFalse(factory.wasSchemeRegistryCreated());
	}

	@Test
	public void validateUsingDefaultCertificates() throws Exception {
		factory.getHttpClient(true, false);
		assertFalse(factory.wasClientCustomConfigured());
		assertFalse(factory.wasSslContextCreated());
		assertFalse(factory.wasSchemeRegistryCreated());
	}

	@Test
	public void validateIgnoringSslCertValidation() throws Exception {
		factory.getHttpClient(true, true);
		assertTrue(factory.wasClientCustomConfigured());
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
	private class InstrumentedConcreteHttpClientFactory extends ConcreteHttpClientFactory {
		private boolean clientConfigured = false;
		private boolean sslContextCreated = false;
		private boolean schemeRegistryCreated = false;

		public boolean wasClientCustomConfigured() {
			return clientConfigured;
		}

		public boolean wasSchemeRegistryCreated() {
			return schemeRegistryCreated;
		}

		public boolean wasSslContextCreated() {
			return sslContextCreated;
		}

		@Override
		protected HttpClient createHttpClient(Boolean useConfigured) 
				throws Exception {
			clientConfigured = useConfigured;
			return super.createHttpClient(useConfigured);
		}

		@Override
		protected SSLContext createContext() throws NoSuchAlgorithmException,
				KeyManagementException {
			sslContextCreated = true;
			return super.createContext();
		}

		@Override
		protected SchemeRegistry createScheme(SSLContext sslContext) throws Exception  {
			schemeRegistryCreated = true;
			return super.createScheme(sslContext);
		}
	}
}
