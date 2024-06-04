package ee.kemit.pistrik.dto;

import lombok.Data;

@Data
public class DeliveryNoteDto {
  private String id;
  private String issuerCode;
  private String issuerName;
  private AddressDto issuingAddress;
  private String issuingAddressFull;
  private String delivererCode;
  private String delivererName;
  private String receiverCode;
  private String receiverName;
  private AddressDto receivingAddress;
  private String receivingAddressFull;
  private String uuid;
}
