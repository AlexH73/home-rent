package de.ait.homerent.contract.model;
import de.ait.homerent.booking.model.Booking;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 02.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Entity
@Table(name = "rental_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking; // One-to-one relationship with booking

    @Column(nullable = false)
    private String filePath;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
