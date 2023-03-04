package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ShareItApp.class)
public class ShareItAppIntegrationTest {

    @Test
    public void contextLoads() {
        ShareItApp.main(new String[] {});
    }
}