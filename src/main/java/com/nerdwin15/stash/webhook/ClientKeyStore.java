package com.nerdwin15.stash.webhook;

import com.atlassian.stash.server.ApplicationPropertiesService;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * A keystore based on the definition from the
 * application properties
 */
public class ClientKeyStore {

    public final static String KEYSTORE_KEY = "plugin.stash-webhook-jenkins.keyStore";
    public final static String KEYSTORE_TYPE_KEY = "plugin.stash-webhook-jenkins.keyStoreType";
    public final static String KEYSTORE_PASSWORD_KEY = "plugin.stash-webhook-jenkins.keyStorePassword";



    private KeyStore keyStore = null;
    private char[] password = null;

    public ClientKeyStore(ApplicationPropertiesService applicationPropertiesService) {
        try {
            // We need to allow client certs to be presented to the server, we
            // will allow the system administrator to register a key store
            // and password that we can load
            if (applicationPropertiesService.getPluginProperty(KEYSTORE_KEY) != null) {
                keyStore = getKeyStore(applicationPropertiesService.getPluginProperty(KEYSTORE_TYPE_KEY));

                if (applicationPropertiesService.getPluginProperty(KEYSTORE_PASSWORD_KEY) != null)
                    password = applicationPropertiesService.getPluginProperty(KEYSTORE_PASSWORD_KEY).toCharArray();

                keyStore.load(new FileInputStream(applicationPropertiesService.getPluginProperty(KEYSTORE_KEY)), password);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to build keystore", e);
        }
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public char[] getPassword() {
        return password;
    }

    public boolean isAvailable() {
        return keyStore != null;
    }

    private KeyStore getKeyStore(String keyStoreType) throws KeyStoreException {
        if (keyStoreType != null)
            return KeyStore.getInstance(keyStoreType);
        else
            return KeyStore.getInstance(KeyStore.getDefaultType());
    }
}
