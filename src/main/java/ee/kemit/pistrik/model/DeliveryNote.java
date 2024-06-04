package ee.kemit.pistrik.model;

import ee.kemit.pistrik.dto.DeliveryNoteDto;
import ee.kemit.pistrik.util.MappingUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "delivery_note")
@NoArgsConstructor
public class DeliveryNote extends DomainBase {
  private String issuerCode;
  private String issuerName;
  private String delivererCode;
  private String delivererName;
  private String receiverCode;
  private String receiverName;
  private String uuid = UUID.randomUUID().toString();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_note_id")
  private List<Address> relatedAddresses = new ArrayList<>();

  public DeliveryNote(DeliveryNoteDto dto) {
    this.issuerCode = dto.getIssuerCode();
    this.issuerName = dto.getIssuerName();
    this.delivererCode = dto.getDelivererCode();
    this.delivererName = dto.getDelivererName();
    this.receiverCode = dto.getReceiverCode();
    this.receiverName = dto.getReceiverName();
  }

  public void addNewRelatedAddress(Address address) {
    relatedAddresses.add(address);
    address.setDeliveryNote(this);
  }

  public DeliveryNoteDto toDto() {
    DeliveryNoteDto deliveryNoteDto = MappingUtil.map(this, DeliveryNoteDto.class);
    deliveryNoteDto.setIssuingAddressFull(getActiveAddressByType("ISSUING"));
    deliveryNoteDto.setReceivingAddressFull(getActiveAddressByType("RECEIVING"));
    return deliveryNoteDto;
  }

  private String getActiveAddressByType(String type) {
    Optional<Address> addressOptional =
        relatedAddresses.stream()
            .filter(address -> address.isActive() && type.equals(address.getAddressType()))
            .findFirst();
    return addressOptional.map(Address::getFullAddress).orElse(null);
  }
}
