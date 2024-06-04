package ee.kemit.pistrik.service;

import ee.kemit.pistrik.dto.AdsResponse;
import ee.kemit.pistrik.util.MappingUtil;
import ee.kemit.pistrik.util.XroadUtil;
import ee.kemit.pistrik.xroad.AdsRequestDto;
import ee.kemit.pistrik.xroad.AdsRequestSerializer;
import ee.kemit.pistrik.xroad.AdsResponseDeserializer;
import ee.kemit.pistrik.xroad.AdsResponseDto;
import ee.kemit.pistrik.xroad.AdsXRoadClientService;
import ee.kemit.pistrik.xroad.ClientXRoad6;
import java.util.Map;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.niis.xrd4j.common.message.ServiceRequest;
import org.niis.xrd4j.common.message.ServiceResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class XRoadService {
  private final Environment environment;

  public AdsResponse getAddressByAdsOid(String adsOid) {
    try {
      AdsRequestDto adsRequestDto = new AdsRequestDto();
      adsRequestDto.setAdsOid(adsOid);

      ServiceRequest<AdsRequestDto> request =
          new AdsXRoadClientService(environment).createServiceRequest();
      request.setRequestData(adsRequestDto);

      ClientXRoad6 clientXRoad6 = new ClientXRoad6(environment);

      Future<ServiceResponse> responseFuture =
          clientXRoad6.send(request, new AdsRequestSerializer(), new AdsResponseDeserializer());

      while (!(responseFuture.isDone())) {
        Thread.sleep(ClientXRoad6.SERVICE_RESPONSE_TIMEOUT);
      }

      AdsResponseDto adsResponseDto = (AdsResponseDto) responseFuture.get().getResponseData();
      Map<String, Object> soapResponse = adsResponseDto.getSoapAsMap();
      String correctBody = XroadUtil.removeSOAPStuff(soapResponse);
      return MappingUtil.mapXroadResponse(correctBody, AdsResponse.class);
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong", e);
    }
  }
}
