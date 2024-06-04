package ee.kemit.pistrik.repository;

import ee.kemit.pistrik.model.Address;
import ee.kemit.pistrik.model.DeliveryNote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findAllByIsActiveTrueOrderByFullAddressAsc();

  List<Address> findAllByAddressTypeAndDeliveryNoteOrderByUpdatedAtDesc(
      String addressType, DeliveryNote deliveryNote);
}
