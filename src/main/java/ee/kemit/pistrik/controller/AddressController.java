package ee.kemit.pistrik.controller;

import ee.kemit.pistrik.dto.AddressDto;
import ee.kemit.pistrik.model.Address;
import ee.kemit.pistrik.service.AddressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("address")
@RequiredArgsConstructor
public class AddressController {
  private final AddressService addressService;

  @PutMapping("update/{addressId}")
  public AddressDto updateAddress(@PathVariable Long addressId) {
    Address updatedAddress = addressService.updateAddress(addressId);
    if (updatedAddress != null) {
      return updatedAddress.toDto();
    }
    return null;
  }

  @GetMapping("active")
  public List<AddressDto> getAddresses() {
    return addressService.getAllActiveAddresses().stream().map(Address::toListDto).toList();
  }

  @GetMapping("{addressId}")
  public List<AddressDto> getAddressDataWithHistoric(@PathVariable Long addressId) {
    return addressService.getAllDataForAddress(addressId).stream().map(Address::toDto).toList();
  }
}
