package ee.kemit.pistrik.xroad;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.niis.xrd4j.client.SOAPClient;
import org.niis.xrd4j.client.deserializer.GetSecurityServerMetricsResponseDeserializer;
import org.niis.xrd4j.client.deserializer.ListCentralServicesResponseDeserializer;
import org.niis.xrd4j.client.deserializer.ListClientsResponseDeserializer;
import org.niis.xrd4j.client.deserializer.ListServicesResponseDeserializer;
import org.niis.xrd4j.client.deserializer.ServiceResponseDeserializer;
import org.niis.xrd4j.client.serializer.DefaultServiceRequestSerializer;
import org.niis.xrd4j.client.serializer.ServiceRequestSerializer;
import org.niis.xrd4j.common.exception.XRd4JRuntimeException;
import org.niis.xrd4j.common.member.ConsumerMember;
import org.niis.xrd4j.common.member.ProducerMember;
import org.niis.xrd4j.common.message.ServiceRequest;
import org.niis.xrd4j.common.message.ServiceResponse;
import org.niis.xrd4j.common.util.Constants;
import org.niis.xrd4j.common.util.SOAPHelper;
import org.niis.xrd4j.rest.ClientResponse;
import org.niis.xrd4j.rest.client.RESTClient;
import org.niis.xrd4j.rest.client.RESTClientFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * This class represents a SOAP client that can be used for sending SOAPMessage and ServiceRequest
 * objects to SOAP endpoints.
 *
 * <p>Copied from
 * https://bitbucket.niis.org/projects/X-ROAD/repos/xrd4j/browse/src/client/src/main/java/org/niis/xrd4j/client/SOAPClientImpl.java?until=da48f44ba52f1b3e575305464265a4c11e66f752&untilPath=src%2Fclient%2Fsrc%2Fmain%2Fjava%2Fcom%2Fpkrete%2Fxrd4j%2Fclient%2FSOAPClientImpl.java&at=refs%2Fheads%2Fdevelop
 *
 * <p>Original repo https://github.com/petkivim/xrd4j Latest repo
 * https://github.com/nordic-institute/xrd4j
 *
 * <p>
 *
 * <p>Modified to support also mTLS
 */
@Log4j2
public class SoapClientImpl implements SOAPClient {

  private static final String SEND_SOAP_TO = "Send SOAP message to \"{}\".";
  private static final String CALL_METASERVICE = "Call \"{}\" meta service.";

  @Setter private String keyStoreFileName;

  @Setter private String keyStorePassword;

  private HttpsURLConnection httpsConnection = null;

  /** Constructs and initializes a new SOAPClientImpl. */
  public SoapClientImpl() {}

  /**
   * Sends the given message to the specified endpoint and blocks until it has returned the
   * response. Null is returned if the given URL is malformed or if sending the message fails
   *
   * @param request the SOAPMessage object to be sent
   * @param url URL that identifies where the message should be sent
   * @return the SOAPMessage object that is the response to the request message that was sent.
   * @throws SOAPException if there's a SOAP error
   */
  @Override
  public SOAPMessage send(final SOAPMessage request, final String url) throws SOAPException {
    URL targetURL;
    try {
      targetURL = new URL(url);
    } catch (MalformedURLException ex) {
      log.error(ex.getMessage(), ex);
      throw new XRd4JRuntimeException(ex.getMessage());
    }

    // if HTTPS then open underlying HTTPS connection first
    if (this.isHTTPS()) {
      try {
        log.debug("Trying to use SSL connection to send the request");
        this.loadSecureRequestContext(url);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      log.debug("Using regular connection to send the request!");
    }

    // create a new instance every time to overcome handshake issues (just in case)
    SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
    log.debug(SEND_SOAP_TO, url);
    log.debug("Outgoing SOAP request : \"{}\".", SOAPHelper.toString(request));

    SOAPMessage response = null;
    try {
      response = connection.call(request, targetURL);
      log.debug("SOAP response received");
      log.debug("Incoming SOAP response : \"{}\"", SOAPHelper.toString(response));

      connection.close();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    // close underlying connection
    if (this.isHTTPS() && this.httpsConnection != null) {
      this.httpsConnection.disconnect();
    }

    return response;
  }

  /**
   * Sends the given message to the specified endpoint and blocks until it has returned the
   * response. Null is returned if the given url is malformed or if sending the message fails.
   * Serialization and deserialization from/to SOAPMessage is done inside the method.
   *
   * @param request the ServiceRequest object to be sent
   * @param targetURL URL that identifies where the message should be sent
   * @param serializer the ServiceRequestSerializer object that serializes the request to
   *     SOAPMessage
   * @param deserializer the ServiceResponseDeserializer object that deserializes SOAPMessage
   *     response to ServiceResponse
   * @return the ServiceResponse object that is the response to the message that was sent.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public ServiceResponse send(
      final ServiceRequest request,
      final String targetURL,
      final ServiceRequestSerializer serializer,
      final ServiceResponseDeserializer deserializer)
      throws SOAPException {

    SOAPMessage soapRequest = serializer.serialize(request);
    log.debug("Send ServiceRequest to \"{}\", requestId: \"{}\"", targetURL, request.getId());
    log.debug("Consumer : {}", request.getConsumer().toString());
    log.debug("Producer : {}", request.getProducer().toString());

    SOAPMessage soapResponse = this.send(soapRequest, targetURL);
    String producerNamespaceURI =
        request.getProducer().getNamespaceUrl() == null
                || request.getProducer().getNamespaceUrl().isEmpty()
            ? "*"
            : request.getProducer().getNamespaceUrl();

    ServiceResponse response =
        deserializer.deserialize(
            soapResponse, producerNamespaceURI, request.isProcessingWrappers());

    log.debug("ServiceResponse received. Request id : \"{}\"", request.getId());
    return response;
  }

  /**
   * Calls listClients meta service and retrieves list of all the potential service providers (i.e.,
   * members and subsystems) of an X-Road instance. Returns a list of list of ConsumerMembers that
   * represent X-Road clients.
   *
   * @param url URL of X-Road security server
   * @return list of ConsumerMembers
   */
  @Override
  public List<ConsumerMember> listClients(String url) {

    log.debug(CALL_METASERVICE, Constants.META_SERVICE_LIST_CLIENTS);
    if (!url.endsWith("/")) {
      url += "/";
    }

    log.debug(SEND_SOAP_TO, url);
    RESTClient client = RESTClientFactory.createRESTClient("get");
    ClientResponse response =
        client.send(url + Constants.META_SERVICE_LIST_CLIENTS, null, null, null);
    List<ConsumerMember> list =
        new ListClientsResponseDeserializer().deserializeConsumerList(response.getData());

    log.debug("Received \"{}\" clients from the security server", list.size());
    return list;
  }

  /**
   * Calls listCentralServices meta service and retrieves list of all central services defined in an
   * X-Road instance. Returns a list of ProducerMembers that represent X-Road central services.
   *
   * @param url URL of X-Road security server
   * @return list of ProducerMembers
   */
  @Override
  public List<ProducerMember> listCentralServices(String url) {

    log.debug(CALL_METASERVICE, Constants.META_SERVICE_LIST_CENTRAL_SERVICES);
    if (!url.endsWith("/")) {
      url += "/";
    }

    log.debug(SEND_SOAP_TO, url);

    RESTClient client = RESTClientFactory.createRESTClient("get");
    ClientResponse response =
        client.send(url + Constants.META_SERVICE_LIST_CENTRAL_SERVICES, null, null, null);
    List<ProducerMember> list =
        new ListCentralServicesResponseDeserializer().deserializeProducerList(response.getData());

    log.debug("Received \"{}\" clients from the security server", list.size());
    return list;
  }

  /**
   * Calls listMethods meta service that lists all the services offered by a service provider.
   * Returns a list of ProducerMember objects wrapped in ServiceResponse object's responseData
   * variable.
   *
   * @param request the ServiceRequest object to be sent
   * @param url URL that identifies where the message should be sent
   * @return ServiceResponse that holds a list of ProducerMember objects
   * @throws SOAPException if there's a SOAP error
   */
  @SuppressWarnings("rawtypes")
  @Override
  public ServiceResponse listMethods(final ServiceRequest request, final String url)
      throws SOAPException {
    log.debug(CALL_METASERVICE, Constants.META_SERVICE_LIST_METHODS);
    return this.listServices(request, url, Constants.META_SERVICE_LIST_METHODS);
  }

  /**
   * Calls allowedMethods meta service that lists all the services by a service provider that the
   * caller has permission to invoke. Returns a list of ProducerMember objects wrapped in
   * ServiceResponse object's responseData variable.
   *
   * @param request the ServiceRequest object to be sent
   * @param url URL that identifies where the message should be sent
   * @return ServiceResponse that holds a list of ProducerMember objects
   * @throws SOAPException if there's a SOAP error
   */
  @SuppressWarnings("rawtypes")
  @Override
  public ServiceResponse allowedMethods(final ServiceRequest request, final String url)
      throws SOAPException {
    return this.listServices(request, url, Constants.META_SERVICE_ALLOWED_METHODS);
  }

  /**
   * Calls getSecurityServerMetrics monitoring service that returns a data set collected by
   * environmental monitoring sensors.
   *
   * @param request the ServiceRequest object to be sent
   * @param url URL that identifies where the message should be sent
   * @return ServiceResponse that holds a NodeList containing the response data
   * @throws SOAPException if there's a SOAP error
   */
  @SuppressWarnings("rawtypes")
  @Override
  public ServiceResponse getSecurityServerMetrics(final ServiceRequest request, final String url)
      throws SOAPException {

    // Set correct values for meta service call
    request.getProducer().setSubsystemCode(null);
    request.getProducer().setServiceCode(Constants.ENV_MONITORING_GET_SECURITY_SERVER_METRICS);
    request.getProducer().setServiceVersion(null);
    request.getProducer().setNamespacePrefix(Constants.NS_ENV_MONITORING_PREFIX);
    request.getProducer().setNamespaceUrl(Constants.NS_ENV_MONITORING_URL);

    // Request serializer
    ServiceRequestSerializer serializer = new DefaultServiceRequestSerializer();

    // Response deserializer
    ServiceResponseDeserializer deserializer = new GetSecurityServerMetricsResponseDeserializer();

    // Return response
    return this.send(request, url, serializer, deserializer);
  }

  /**
   * This is a helper method for meta service calls that don't have a request body. The method sets
   * the service code, name space and name space prefix, and removes the service code.
   *
   * @param request the ServiceRequest object to be sent
   * @param url URL that identifies where the message should be sent
   * @param serviceCode service code of the meta service to be called
   * @return ServiceResponse that holds the response of the meta service
   * @throws SOAPException if there's a SOAP error
   */
  @SuppressWarnings("rawtypes")
  private ServiceResponse listServices(
      final ServiceRequest request, final String url, final String serviceCode)
      throws SOAPException {

    // Set correct values for meta service call
    request.getProducer().setServiceCode(serviceCode);
    request.getProducer().setServiceVersion(null);
    request.getProducer().setNamespacePrefix(Constants.NS_XRD_PREFIX);
    request.getProducer().setNamespaceUrl(Constants.NS_XRD_URL);

    // Request serializer
    ServiceRequestSerializer serializer = new DefaultServiceRequestSerializer();

    // Response deserializer
    ServiceResponseDeserializer deserializer = new ListServicesResponseDeserializer();

    // Return response
    return this.send(request, url, serializer, deserializer);
  }

  private boolean isHTTPS() {
    return (this.keyStoreFileName != null && !this.keyStoreFileName.isEmpty());
  }

  private void loadSecureRequestContext(String targetURL)
      throws NoSuchAlgorithmException,
          CertificateException,
          IOException,
          KeyStoreException,
          UnrecoverableKeyException,
          KeyManagementException {

    ClassPathResource cpr = new ClassPathResource(this.keyStoreFileName);

    // keystore files need to have been created from a public-private pair with the following
    // command
    // $ openssl pkcs12 -export -in mtp-xtee-dev.crt -inkey mtp-xtee-dev.key -out mtp-xtee-dev.p12
    // needs "winpty" in front of in git bash console under windows

    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(cpr.getInputStream(), this.keyStorePassword.toCharArray());

    log.debug("Loaded keystore: " + cpr.getURI());
    try {
      cpr.getInputStream().close();
    } catch (IOException ignored) {
    }

    KeyManagerFactory keyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(ks, this.keyStorePassword.toCharArray());

    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

    TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
          }
        };
    sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new SecureRandom());

    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

    HostnameVerifier allHostsValid =
        (hostname, sslSession) -> hostname.equals(sslSession.getPeerHost());

    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    log.debug("Using secure TLS to wrap the connection: {}", this.keyStoreFileName);

    URL url = new URL(targetURL);
    this.httpsConnection = (HttpsURLConnection) url.openConnection();
    this.httpsConnection.connect();
  }
}
