package com.github.yamikazoo.pokepad.services;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class S3StorageServiceTest {

    private final S3StorageService service = new S3StorageService(
            mock(S3Client.class),
            "us-west-2",
            "pokepad-images");

    @Test
    void publicUrlUsesVirtualHostedStyle() {
        assertEquals(
                "https://pokepad-images.s3.us-west-2.amazonaws.com/catalog/sv8pt5/1.jpg",
                service.publicUrl("catalog/sv8pt5/1.jpg"));
    }

    @Test
    void rejectsPathTraversalInKeys() {
        assertThrows(IllegalArgumentException.class, () -> service.sanitizeKey("catalog/../secret.jpg"));
    }

    @Test
    void rejectsBlankKeys() {
        assertThrows(IllegalArgumentException.class, () -> service.sanitizeKey(" "));
    }
}
