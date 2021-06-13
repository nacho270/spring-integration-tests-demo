package cucumber.config;

import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import com.nacho.blog.spring.integration.tests.demo.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.spy;

@ContextConfiguration
public class ShipmentRepositoryConfiguration {

  @Bean
  @Primary
  public ShipmentRepository shipmentTestRepository(ShipmentRepository shipmentRepository) {
    return spy(new ShipmentRepositoryWrapper(shipmentRepository));
  }

  /**
   * A wrapper class if {@link ShipmentRepository} to be used in the failure scenarios.
   * Cannot spy {@link ShipmentRepository} directly because it is a proxy created by spring and it's final.
   * The strategy here is to create a partial mock of a concrete non-final class that can be overridden is needed.
   * As with every partial mock, if not overridden, the real method is executed which in this case
   * is delegated to {@link ShipmentRepository}.
   */
  @RequiredArgsConstructor
  static class ShipmentRepositoryWrapper implements ShipmentRepository {

    private final ShipmentRepository shipmentRepository;

    @Override
    public <S extends Shipment> S save(S s) {
      return shipmentRepository.save(s);
    }

    @Override
    public <S extends Shipment> Iterable<S> saveAll(Iterable<S> iterable) {
      return shipmentRepository.saveAll(iterable);
    }

    @Override
    public Optional<Shipment> findById(UUID uuid) {
      return shipmentRepository.findById(uuid);
    }

    @Override
    public boolean existsById(UUID uuid) {
      return shipmentRepository.existsById(uuid);
    }

    @Override
    public Iterable<Shipment> findAll() {
      return shipmentRepository.findAll();
    }

    @Override
    public Iterable<Shipment> findAllById(Iterable<UUID> iterable) {
      return shipmentRepository.findAllById(iterable);
    }

    @Override
    public long count() {
      return shipmentRepository.count();
    }

    @Override
    public void deleteById(UUID uuid) {
      shipmentRepository.deleteById(uuid);
    }

    @Override
    public void delete(Shipment shipment) {
      shipmentRepository.delete(shipment);
    }

    @Override
    public void deleteAll(Iterable<? extends Shipment> iterable) {
      shipmentRepository.deleteAll(iterable);
    }

    @Override
    public void deleteAll() {
      shipmentRepository.deleteAll();
    }

    @Override
    public void markAs(UUID shipmentId, Shipment.ShipmentPaymentStatus paymentStatus) {
      shipmentRepository.markAs(shipmentId, paymentStatus);
    }
  }
}
