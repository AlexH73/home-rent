package de.ait.homerent.issue.repository;

import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 03.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {

    List<IssueReport> findByStatus (IssueStatus status);

    //List<IssueReport> findByBookingId(Long bookingId);

    List<IssueReport> findByReportedById(Long userId);

}
