package ee.kemit.pistrik.repository;

import ee.kemit.pistrik.model.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long> {}
