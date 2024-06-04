package ee.kemit.pistrik.dto;

import lombok.Data;

@Data
public class ResponseMessageDto {
  private Status status;
  private Object message;

  public enum Status {
    OK,
    NOK
  }
}
