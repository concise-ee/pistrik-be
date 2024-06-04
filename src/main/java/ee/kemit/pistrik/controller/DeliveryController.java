package ee.kemit.pistrik.controller;

import ee.kemit.pistrik.dto.DeliveryNoteDto;
import ee.kemit.pistrik.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("delivery")
@RequiredArgsConstructor
public class DeliveryController {
  private final DeliveryService deliveryService;

  @PostMapping
  public DeliveryNoteDto createNewDeliveryNote(@RequestBody DeliveryNoteDto deliveryNoteDto) {
    return deliveryService.saveNewDeliveryNote(deliveryNoteDto).toDto();
  }
}
