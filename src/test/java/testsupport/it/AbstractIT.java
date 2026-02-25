package testsupport.it;

import de.ait.homerent.mail.EmailService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 * Base class for Spring Boot integration tests.
 * Contains common test overrides/mocks (e.g., external integrations).
 */
public abstract class AbstractIT {

    @MockitoBean
    protected EmailService emailService;
}
