package ee.kemit.pistrik.service;

import ee.kemit.pistrik.dto.DeliveryNoteDto;
import ee.kemit.pistrik.model.Address;
import ee.kemit.pistrik.model.DeliveryNote;
import ee.kemit.pistrik.repository.DeliveryNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeliveryService {
  private final DeliveryNoteRepository deliveryNoteRepository;

  @Transactional
  public DeliveryNote saveNewDeliveryNote(DeliveryNoteDto deliveryNoteDto) {
    DeliveryNote deliveryNote = new DeliveryNote(deliveryNoteDto);
    deliveryNote = deliveryNoteRepository.save(deliveryNote);
    deliveryNote.addNewRelatedAddress(new Address(deliveryNoteDto.getIssuingAddress(), "ISSUING"));
    deliveryNote.addNewRelatedAddress(
        new Address(deliveryNoteDto.getReceivingAddress(), "RECEIVING"));
    return deliveryNote;
  }
}
