package de.ait.homerent.contract.repository;
import de.ait.homerent.contract.model.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 02.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {

    Optional<RentalContract> findByBookingId(Long bookingId);
}
