package de.ait.homerent.booking.model;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.user.model.User;
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

    @CreationTimestamp
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @CreationTimestamp
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NonNull
    private BookingStatus status;


    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;
}
