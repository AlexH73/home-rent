package de.ait.homerent.property.model;

import de.ait.homerent.property.enums.PropertyStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 02.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Владелец (User с ролью ROLE_OWNER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    @NotBlank(message = "Title must not be empty")
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Address must not be empty")
    private String address;

    @Column(nullable = false, length = 1000)
    @NotBlank(message = "Description must not be empty")
    private String description;

    @Column(name = "price_per_day")
    @Min(value = 1, message = "Price per day must be greater than 0")
    private int pricePerDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status;

    // Связь с фото PropertyPhoto (1 Property- много фото)
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyPhoto> photos;

    // Дата создания
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Дополнительно: период доступности
    @Column(nullable = false)
    private LocalDate availableFrom;

    @Column(nullable = false)
    private LocalDate availableTo;

}
