package ee.kemit.pistrik.model;

import ee.kemit.pistrik.dto.AddressDto;
import ee.kemit.pistrik.dto.AdsResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "address")
@NoArgsConstructor
public class Address extends DomainBase {
  private String fullAddress;
  private String addressId;
  private String ehakCounty;
  private String county;
  private String ehakMunicipality;
  private String municipality;
  private String ehakSettlement;
  private String settlement;
  private String addressText;
  private String addressNumber;
  private String codeAddress;
  private String adsOid;
  private String adobId;

  @Column(name = "coordinate_x")
  private String coordinateX;

  @Column(name = "coordinate_y")
  private String coordinateY;

  private boolean isActive = true;
  private String addressType;

  @ManyToOne
  @JoinColumn(name = "delivery_note_id")
  private DeliveryNote deliveryNote;

  public Address(AddressDto addressDto, String addressType) {
    this.fullAddress = addressDto.getTaisaadress();
    this.addressId = addressDto.getAdr_id();
    this.ehakCounty = addressDto.getEhakmk();
    this.county = addressDto.getMaakond();
    this.ehakMunicipality = addressDto.getEhakov();
    this.municipality = addressDto.getOmavalitsus();
    this.ehakSettlement = addressDto.getEhak();
    this.settlement = addressDto.getAsustusyksus();
    this.addressText = addressDto.getAadresstekst();
    this.addressNumber = addressDto.getAadress_nr();
    this.codeAddress = addressDto.getKoodaadress();
    this.coordinateX = addressDto.getViitepunkt_x();
    this.coordinateY = addressDto.getViitepunkt_y();
    this.adsOid = addressDto.getAds_oid();
    this.adobId = addressDto.getAdob_id();
    this.addressType = addressType;
  }

  public Address(AdsResponse.AdsObject activeAddress, String addressType) {
    this.fullAddress = activeAddress.getAadress();
    this.adsOid = activeAddress.getAdsOid();
    this.adobId = activeAddress.getAdobId();
    this.addressText = activeAddress.getLahiAadress();
    this.addressType = addressType;
  }

  public AddressDto toDto() {
    AddressDto addressDto = new AddressDto();
    addressDto.setTaisaadress(fullAddress);
    addressDto.setAdr_id(addressId);
    addressDto.setEhakmk(ehakCounty);
    addressDto.setMaakond(county);
    addressDto.setEhakov(ehakMunicipality);
    addressDto.setOmavalitsus(municipality);
    addressDto.setEhak(ehakSettlement);
    addressDto.setAsustusyksus(settlement);
    addressDto.setAadresstekst(addressText);
    addressDto.setAadress_nr(addressNumber);
    addressDto.setKoodaadress(codeAddress);
    addressDto.setViitepunkt_x(coordinateX);
    addressDto.setViitepunkt_y(coordinateY);
    addressDto.setAds_oid(adsOid);
    addressDto.setAdob_id(adobId);
    addressDto.setAddressType(addressType);
    addressDto.setDeliveryNoteId(deliveryNote.getUuid());
    addressDto.setActive(isActive);
    addressDto.setId(this.getId());
    return addressDto;
  }

  public AddressDto toListDto() {
    AddressDto addressDto = new AddressDto();
    addressDto.setTaisaadress(fullAddress);
    addressDto.setId(this.getId());
    return addressDto;
  }
}
