package com.quorum.tessera.q2t;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestPayloadPublisherTest {

    private RestPayloadPublisher publisher;

    private MockClient mockClient;

    private PayloadEncoder encoder;

    private Discovery partyInfoService;

    @Before
    public void onSetUp() {
        mockClient = new MockClient();
        encoder = mock(PayloadEncoder.class);
        partyInfoService = mock(Discovery.class);
        publisher = new RestPayloadPublisher(mockClient, encoder, partyInfoService);
    }

    @Test
    public void publish() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        List<javax.ws.rs.client.Entity> postedEntities = new ArrayList<>();

        doAnswer(
                        (invocation) -> {
                            postedEntities.add(invocation.getArgument(0));
                            return Response.ok().build();
                        })
                .when(invocationBuilder)
                .post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);
        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(partyInfoService.getCurrent()).thenReturn(nodeInfo);

        publisher.publishPayload(encodedPayload, recipientKey);

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(payloadData);

        verify(encoder).encode(encodedPayload);
        verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
    }

    @Test
    public void publishReturns201() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        List<javax.ws.rs.client.Entity> postedEntities = new ArrayList<>();

        doAnswer(
                        (invocation) -> {
                            postedEntities.add(invocation.getArgument(0));
                            return Response.created(URI.create("http://location")).build();
                        })
                .when(invocationBuilder)
                .post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);
        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(partyInfoService.getCurrent()).thenReturn(nodeInfo);

        publisher.publishPayload(encodedPayload, recipientKey);

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(payloadData);

        verify(encoder).encode(encodedPayload);
        verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
    }

    @Test
    public void publishReturnsError() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        doAnswer(
                        (invocation) -> {
                            return Response.serverError().build();
                        })
                .when(invocationBuilder)
                .post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);
        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(partyInfoService.getCurrent()).thenReturn(nodeInfo);

        try {
            publisher.publishPayload(encodedPayload, recipientKey);
            failBecauseExceptionWasNotThrown(PublishPayloadException.class);
        } catch (PublishPayloadException ex) {
            verify(encoder).encode(encodedPayload);
            verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
        }
    }

    @Test
    public void publicToUnknownRecipient() throws Exception {

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);

        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(partyInfoService.getCurrent()).thenReturn(nodeInfo);

        try {
            publisher.publishPayload(encodedPayload, mock(PublicKey.class));
            failBecauseExceptionWasNotThrown(KeyNotFoundException.class);
        } catch (KeyNotFoundException ex) {
            assertThat(ex).isNotNull();
        }
    }
}
