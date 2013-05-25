package com.nerdwin15.stash.webhook.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * A credential checker used to avoid exceptions with self signed SSL 
 * certificates.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class UnsafeX509TrustManager implements X509TrustManager {

  /**
   * {@inheritDoc}
   */
  public final void checkClientTrusted(X509Certificate[] arg0, String arg1)
      throws CertificateException {
    // don't throw any exception
  }

  /**
   * {@inheritDoc}
   */
  public void checkServerTrusted(X509Certificate[] arg0, String arg1)
      throws CertificateException {
    // don't throw any exception
  }

  /**
   * {@inheritDoc}
   */
  public X509Certificate[] getAcceptedIssuers() {
    return null;
  }
}
