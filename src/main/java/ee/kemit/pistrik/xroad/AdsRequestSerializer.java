package ee.kemit.pistrik.xroad;

import java.io.IOException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import lombok.extern.log4j.Log4j2;
import org.niis.xrd4j.client.serializer.AbstractServiceRequestSerializer;
import org.niis.xrd4j.common.message.ServiceRequest;

@Log4j2
public class AdsRequestSerializer extends AbstractServiceRequestSerializer {

  @Override
  protected void serializeRequest(
      ServiceRequest serviceRequest, SOAPElement soapElement, SOAPEnvelope soapEnvelope) {
    try {
      AdsRequestDto request = (AdsRequestDto) serviceRequest.getRequestData();

      soapElement.addChildElement("aadressKomp").addTextNode("true");
      soapElement.addChildElement("andmeVektor").addTextNode("001");
      soapElement.addChildElement("adsOid").addTextNode(request.getAdsOid());

      this.debug(serviceRequest);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @SuppressWarnings("rawtypes")
  private void debug(ServiceRequest serviceRequest) throws SOAPException, IOException {
    SOAPMessage soap = serviceRequest.getSoapMessage();

    java.io.ByteArrayOutputStream baosHolder = new java.io.ByteArrayOutputStream();
    soap.writeTo(baosHolder);

    String strSOAPMsg =
        baosHolder.toString().replaceAll("\n+", "").replaceAll("\r+", "").replaceAll("\t+", "");
    log.info(" ------ XML --------");
    log.info(strSOAPMsg);
  }
}
