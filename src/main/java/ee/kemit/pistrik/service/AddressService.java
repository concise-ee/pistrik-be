package ee.kemit.pistrik.service;

import ee.kemit.pistrik.dto.AdsResponse;
import ee.kemit.pistrik.model.Address;
import ee.kemit.pistrik.model.DeliveryNote;
import ee.kemit.pistrik.repository.AddressRepository;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class AddressService {
  private final XRoadService xroadService;
  private final AddressRepository addressRepository;

  public Address updateAddress(Long addressId) {
    Optional<Address> addressToUpdateOptional = addressRepository.findById(addressId);
    if (addressToUpdateOptional.isEmpty()) {
      throw new RuntimeException("No such address");
    }

    Address addressToUpdate = addressToUpdateOptional.get();
    AdsResponse.AdsObjectWrapper activeAddress = getActiveAddress(addressToUpdate);
    if (activeAddress == null) return null;

    return addNewAddress(addressToUpdate, activeAddress.getAdsobjekt());
  }

  public List<Address> getAllActiveAddresses() {
    return addressRepository.findAllByIsActiveTrueOrderByFullAddressAsc();
  }

  public List<Address> getAllDataForAddress(Long addressId) {
    Optional<Address> addressOptional = addressRepository.findById(addressId);
    if (addressOptional.isEmpty()) {
      throw new RuntimeException("No such address");
    }
    Address address = addressOptional.get();
    return addressRepository.findAllByAddressTypeAndDeliveryNoteOrderByUpdatedAtDesc(
        address.getAddressType(), address.getDeliveryNote());
  }

  private AdsResponse.AdsObjectWrapper getActiveAddress(Address addressToUpdate) {
    AdsResponse adsResponse = xroadService.getAddressByAdsOid(addressToUpdate.getAdsOid());
    if (adsResponse == null
        || adsResponse.getAdsobjektid() == null
        || adsResponse.getAdsobjektid().isEmpty()) {
      throw new RuntimeException("No such address found in ads");
    }
    Optional<AdsResponse.AdsObjectWrapper> activeAddressOptional =
        adsResponse.getAdsobjektid().stream()
            .filter(
                adsObjectWrapper ->
                    adsObjectWrapper != null
                        && adsObjectWrapper.getAdsobjekt().getOlek().equals("K"))
            .findFirst();

    if (activeAddressOptional.isEmpty()) {
      throw new RuntimeException("No active address found in ads");
    }

    AdsResponse.AdsObjectWrapper activeAddress = activeAddressOptional.get();

    if (addressToUpdate.getAdobId().equals(activeAddress.getAdsobjekt().getAdobId())) {
      // address is same, no need to update
      return null;
    }
    return activeAddress;
  }

  private Address addNewAddress(Address addressToUpdate, AdsResponse.AdsObject activeAddress) {
    addressToUpdate.setActive(false);
    addressRepository.save(addressToUpdate);

    Address newAddress = new Address(activeAddress, addressToUpdate.getAddressType());
    newAddress.setAddressNumber(findNumberInAddressText(activeAddress.getLahiAadress()));
    addAddressData(newAddress, activeAddress);

    DeliveryNote deliveryNote = addressToUpdate.getDeliveryNote();
    deliveryNote.addNewRelatedAddress(newAddress);
    addressRepository.flush();
    return newAddress;
  }

  private void addAddressData(Address newAddress, AdsResponse.AdsObject activeAddress) {
    if (activeAddress.getAadressid() != null) {
      List<AdsResponse.Address> parallelAddresses = activeAddress.getAadressid().getAadress();
      if (parallelAddresses.isEmpty()) {
        throw new RuntimeException("No address data in ads response");
      }
      AdsResponse.Address firstAddress = parallelAddresses.getFirst();

      if (firstAddress.getAdsTase1() != null) {
        // maakond
        AdsResponse.AdsLevel county = firstAddress.getAdsTase1();
        newAddress.setCounty(county.getNimetus());
        newAddress.setEhakCounty(county.getKood());
      }

      if (firstAddress.getAdsTase2() != null) {
        // omavalitsus
        AdsResponse.AdsLevel municipality = firstAddress.getAdsTase2();
        newAddress.setMunicipality(municipality.getNimetus());
        newAddress.setEhakMunicipality(municipality.getKood());
      }

      if (firstAddress.getAdsTase3() != null) {
        // asustusüksus
        AdsResponse.AdsLevel settlement = firstAddress.getAdsTase3();
        newAddress.setSettlement(settlement.getNimetus());
        newAddress.setEhakSettlement(settlement.getKood());
      }

      newAddress.setCodeAddress(firstAddress.getKoodAadress());
      newAddress.setCoordinateY(firstAddress.getAadressiPunktX());
      newAddress.setCoordinateX(firstAddress.getAadressiPunktY());
      newAddress.setAddressId(firstAddress.getAdrId());
    }
  }

  private String findNumberInAddressText(String addressText) {
    StringJoiner stringJoiner = new StringJoiner("\\\\");
    String regex = "\\b\\d+(?:-\\d+)?\\b";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(addressText);

    while (matcher.find()) {
      stringJoiner.add(matcher.group());
    }
    return stringJoiner.toString();
  }
}
