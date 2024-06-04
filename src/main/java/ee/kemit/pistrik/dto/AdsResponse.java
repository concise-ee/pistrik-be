package ee.kemit.pistrik.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdsResponse {
  private List<AdsObjectWrapper> adsobjektid;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AdsObjectWrapper {
    private AdsObject adsobjekt;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AdsObject {
    private String adsOid;
    private String olek;
    private String lahiAadress;
    private boolean unikaalne;
    private String adobId;
    private String origTunnus;
    private String objektiUrl;
    private String aadress;
    private Addresses aadressid;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Addresses {
    private List<Address> aadress;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Address {
    private AdsLevel adsTase1;
    private AdsLevel adsTase2;
    private AdsLevel adsTase3;
    private AdsLevel adsTase4;
    private AdsLevel adsTase5;
    private AdsLevel adsTase6;
    private AdsLevel adsTase7;
    private String lahiAadress;
    private String adrId;
    private String maPiirkond;
    private boolean tehniline;
    private String aadressiPunktX;
    private String aadressiPunktY;
    private String koodAadress;
    private String primaarseimObjekt;
    private int sihtnumber;
    private String taisAadress;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AdsLevel {
    private String nimetus;
    private String kood;
    private String nimetus_liigiga;
  }
}
