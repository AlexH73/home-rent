package de.ait.homerent.booking.service;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceTest.class);

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void getActiveBookings_returnsList() {
        log.info("Running test: getActiveBookings_returnsList");

        // ARRANGE
        Property property = new Property();
        property.setTitle("Test House");

        User tenant = new User();
        tenant.setUsername("John");

        Booking booking = new Booking();
        booking.setProperty(property);
        booking.setTenant(tenant);
        booking.setStatus(BookingStatus.ACTIVE);

        when(bookingRepository.findByStatus(BookingStatus.ACTIVE))
                .thenReturn(List.of(booking));

        // ACT
        List<BookingResponse> result = bookingService.getActiveBookings();

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test House", result.get(0).getPropertyTitle());
        assertEquals("John", result.get(0).getTenantName());

        log.info("Test completed: getActiveBookings_returnsList");
    }
}



