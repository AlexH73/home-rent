package de.ait.homerent.property.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 02.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Entity
@Table(name = "property_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "property")

public class PropertyPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(nullable = false)
    private LocalDate uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonBackReference
    private Property property;

}
