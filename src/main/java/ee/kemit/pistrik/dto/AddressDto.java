package ee.kemit.pistrik.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressDto {
  private String taisaadress;
  private String adr_id;
  private String ehakmk;
  private String maakond;
  private String ehakov;
  private String omavalitsus;
  private String ehak;
  private String asustusyksus;
  private String aadresstekst;
  private String aadress_nr;
  private String koodaadress;
  private String viitepunkt_x;
  private String viitepunkt_y;
  private String ads_oid;
  private String adob_id;
  private String addressType;
  private String deliveryNoteId;
  private boolean active;
  private Long id;
}
