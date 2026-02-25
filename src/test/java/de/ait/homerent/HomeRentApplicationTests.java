package de.ait.homerent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@SpringBootTest(classes = HomeRentApplication.class)
class HomeRentApplicationTests {
    @Test void contextLoads() {}
}


