package ee.kemit.pistrik.xroad;

import java.util.Map;
import lombok.Data;

@Data
public class AdsResponseDto {
  private Map<String, Object> soapAsMap;
}
