package com.nerdwin15.stash.webhook.service;

import org.apache.http.client.HttpClient;

/**
 * Defines a generator that will create a HttpClient used to communicate with
 * the Jenkins instance.
 * 
 * @author Michael Irwin (mikesir87)
 */
public interface HttpClientFactory {

  /**
   * Generate a HttpClient to communicate with Jenkins.
   * @param usingSsl True if using ssl.
   * @param trustAllCerts True if all certs should be trusted.
   * @return An HttpClient configured to communicate with Jenkins.
   * @throws Exception Any exception, but shouldn't happen.
   */
  HttpClient getHttpClient(Boolean usingSsl, Boolean trustAllCerts)
      throws Exception;
}