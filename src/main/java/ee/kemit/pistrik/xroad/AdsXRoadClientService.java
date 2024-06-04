package ee.kemit.pistrik.xroad;

import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.niis.xrd4j.common.exception.XRd4JException;
import org.niis.xrd4j.common.member.ConsumerMember;
import org.niis.xrd4j.common.member.ProducerMember;
import org.niis.xrd4j.common.message.ServiceRequest;
import org.springframework.core.env.Environment;

@Log4j2
public class AdsXRoadClientService {

  // COMMON
  private final String instanceIdentifier;

  // Consumer
  private final String consumerMemberClass;
  private final String consumerCode;
  private final String consumerSubSystem;

  // Service provider
  private final String producerMemberClass;
  private final String producerCode;
  private final String producerSubSystem;
  private final String producerServiceCode;
  private final String producerServiceVersion;
  private final String nameSpacePrefix;
  private final String nameSpaceUrl;

  public AdsXRoadClientService(Environment environment) {
    this.instanceIdentifier = environment.getProperty("xroad.instance");

    this.consumerMemberClass = environment.getProperty("xroad.consumer.member-class");
    this.consumerCode = environment.getProperty("xroad.consumer.code");
    this.consumerSubSystem = environment.getProperty("xroad.consumer.subsystem");

    this.producerMemberClass = environment.getProperty("xroad.producer.member-class");
    this.producerCode = environment.getProperty("xroad.producer.code");
    this.producerSubSystem = environment.getProperty("xroad.producer.subsystem");
    this.producerServiceCode = environment.getProperty("xroad.producer.serviceCode");
    this.producerServiceVersion = environment.getProperty("xroad.producer.serviceVersion");
    this.nameSpacePrefix = environment.getProperty("xroad.ads.namespacePrefix");
    this.nameSpaceUrl = environment.getProperty("xroad.ads.namespaceUrl");
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T> ServiceRequest createServiceRequest() throws XRd4JException {
    log.info(
        "Using the following consumer values: "
            + instanceIdentifier
            + " / "
            + consumerMemberClass
            + " / "
            + consumerCode
            + " / "
            + consumerSubSystem);

    ConsumerMember consumerMember =
        new ConsumerMember(
            instanceIdentifier, consumerMemberClass, consumerCode, consumerSubSystem);

    ProducerMember producerMember =
        new ProducerMember(
            instanceIdentifier,
            producerMemberClass,
            producerCode,
            producerSubSystem,
            producerServiceCode,
            producerServiceVersion);

    producerMember.setNamespacePrefix(nameSpacePrefix);
    producerMember.setNamespaceUrl(nameSpaceUrl);

    String requestId = UUID.randomUUID().toString();

    ServiceRequest<T> serviceRequest =
        new ServiceRequest(consumerMember, producerMember, requestId);
    serviceRequest.setUserId("1");
    serviceRequest.setProcessingWrappers(false);
    serviceRequest.setAddNamespaceToRequest(false);
    return serviceRequest;
  }
}
