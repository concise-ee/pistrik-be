package ee.kemit.pistrik.xroad;

import java.net.MalformedURLException;
import java.util.concurrent.Future;
import javax.xml.soap.SOAPException;
import lombok.extern.log4j.Log4j2;
import org.niis.xrd4j.client.deserializer.ServiceResponseDeserializer;
import org.niis.xrd4j.client.serializer.ServiceRequestSerializer;
import org.niis.xrd4j.common.message.ServiceRequest;
import org.niis.xrd4j.common.message.ServiceResponse;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncResult;

@Log4j2
public class ClientXRoad6 {

  public static final Long SERVICE_RESPONSE_TIMEOUT = 10L;

  private final String securityServerURL;
  private final String keyStoreFileName;
  private final String keyStorePassword;

  public ClientXRoad6(Environment environment) {
    this.securityServerURL = environment.getProperty("xroad.security-server.url");
    this.keyStoreFileName = environment.getProperty("xroad.keystore.file");
    this.keyStorePassword = environment.getProperty("xroad.keystore.password");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Future<ServiceResponse> send(
      ServiceRequest request,
      ServiceRequestSerializer serializer,
      ServiceResponseDeserializer deserializer)
      throws MalformedURLException, SOAPException {

    if (this.securityServerURL != null) {

      SoapClientImpl client = new SoapClientImpl();
      this.setKeyStoreContext(client);

      ServiceResponse<String, String> serviceResponse =
          client.send(request, this.securityServerURL, serializer, deserializer);

      return new AsyncResult<>(serviceResponse);
    }
    throw new MalformedURLException("X-Road security server URL has not been provided");
  }

  private void setKeyStoreContext(SoapClientImpl client) {

    if (this.keyStoreFileName != null && !this.keyStoreFileName.isEmpty()) {

      // validate security server address
      if (this.securityServerURL != null
          && !this.securityServerURL.toLowerCase().startsWith("https://")) {
        throw new RuntimeException(
            "Cannot load keystore with HTTP based security server address, check application configuration!");
      }
      log.debug("Adding secure context to the request: {}", this.keyStoreFileName);
      client.setKeyStoreFileName(this.keyStoreFileName);
      client.setKeyStorePassword(this.keyStorePassword);
    }
  }
}
