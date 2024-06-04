package ee.kemit.pistrik.xroad;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.json.XML;
import org.niis.xrd4j.client.deserializer.AbstractResponseDeserializer;

@Log4j2
public class AdsResponseDeserializer
    extends AbstractResponseDeserializer<AdsRequestDto, AdsResponseDto> {

  @Override
  protected AdsRequestDto deserializeRequestData(Node node) {
    return null;
  }

  @Override
  protected AdsResponseDto deserializeResponseData(Node responseNode, SOAPMessage adsResponseSoap) {
    AdsResponseDto response = new AdsResponseDto();
    try {

      DOMSource source = new DOMSource(adsResponseSoap.getSOAPBody());
      StringWriter stringResult = new StringWriter();
      TransformerFactory.newInstance()
          .newTransformer()
          .transform(source, new StreamResult(stringResult));

      String message = stringResult.toString();
      JSONObject xmlJSONObj = XML.toJSONObject(message);

      Map<String, Object> rawObject = new HashMap<>();
      if (!xmlJSONObj.isEmpty()) {
        JSONObject payload =
            xmlJSONObj.getJSONObject("SOAP-ENV:Body").getJSONObject("tns:ADSobjotsingV8Response");
        rawObject = payload.toMap();
      }

      response.setSoapAsMap(rawObject);

      return response;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }
}
