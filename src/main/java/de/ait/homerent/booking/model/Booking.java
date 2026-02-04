package de.ait.homerent.booking.model;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 02.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    @Column(nullable = false)
    private Double totalPrice;
}
