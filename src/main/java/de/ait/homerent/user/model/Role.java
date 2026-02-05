package de.ait.homerent.user.model;

import jakarta.persistence.*;
<<<<<<< feature/task-18-contract
/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 01.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
=======
import lombok.*;
>>>>>>> dev

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleName name;
}