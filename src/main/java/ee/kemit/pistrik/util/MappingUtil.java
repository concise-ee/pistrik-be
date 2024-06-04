package ee.kemit.pistrik.util;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;

public class MappingUtil {

  private static final ModelMapper modelMapper = new ModelMapper();

  public static <D> D map(Object source, Class<D> destinationType) {
    return modelMapper.map(source, destinationType);
  }

  public static <D> D mapXroadResponse(String source, Class<D> destinationType) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(ACCEPT_SINGLE_VALUE_AS_ARRAY);
    mapper.enable(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    mapper.findAndRegisterModules();
    try {
      return mapper.readValue(source, destinationType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
