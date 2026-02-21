package de.ait.homerent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = HomeRentApplication.class)
@ActiveProfiles("test")
class HomeRentApplicationTests {
    @Test void contextLoads() {}
}


