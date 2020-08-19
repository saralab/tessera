package com.quorum.tessera.discovery;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeUriTest {

    @Test
    public void createNormalisesStringValue() {

        String stringValue = "http://ilovesparrows.com";

        NodeUri nodeUri = NodeUri.create(stringValue);

        assertThat(nodeUri).isNotNull();
        assertThat(nodeUri.asString()).startsWith(stringValue).endsWith("/");

    }

    @Test
    public void createNormalisesUriValue() {
        String stringValue = "http://ilovesparrows.com";
        URI uriValue = URI.create(stringValue);

        NodeUri nodeUri = NodeUri.create(uriValue);

        assertThat(nodeUri).isNotNull();
        assertThat(nodeUri.asString()).startsWith(stringValue).endsWith("/");
        assertThat(nodeUri.toString()).isNotNull();
    }

    @Test
    public void hashCodeAndEquals() {
        EqualsVerifier.forClass(NodeUri.class)
            .usingGetClass()
            .withNonnullFields("value")
            .verify();

    }
}
