package de.ait.homerent.issue.model;

import de.ait.homerent.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 03.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Entity
@Table(name = "issue_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class IssueReport {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Booking related to the issue
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    */

    // Reported by (tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @Column(nullable = false, length = 1000)
    @NotBlank(message = "Description must not be empty")
    private String description;

    @Column(name = "photo_path", nullable = false)
    private String photoPath;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

}
