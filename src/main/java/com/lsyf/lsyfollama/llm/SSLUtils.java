package com.lsyf.lsyfollama.llm;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSLUtils {

  public static SSLSocketFactory createSSLSocketFactory(String caCertPath) {
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate caCert = (X509Certificate) cf.generateCertificate(
          new FileInputStream(caCertPath)
      );

      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null);
      keyStore.setCertificateEntry("ca", caCert);

      TrustManagerFactory tmf = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm()
      );
      tmf.init(keyStore);

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, tmf.getTrustManagers(), null);

      return sslContext.getSocketFactory();
    } catch (Exception e) {
      throw new RuntimeException("CA 证书加载失败", e);
    }
  }
}