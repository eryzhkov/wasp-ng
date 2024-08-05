package ru.vsu.uic.wasp.ng.core.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.aaa4j.radius.client.RadiusClientException;
import org.aaa4j.radius.client.clients.UdpRadiusClient;
import org.aaa4j.radius.core.attribute.Ipv4AddrData;
import org.aaa4j.radius.core.attribute.StringData;
import org.aaa4j.radius.core.attribute.TextData;
import org.aaa4j.radius.core.attribute.attributes.NasIdentifier;
import org.aaa4j.radius.core.attribute.attributes.NasIpAddress;
import org.aaa4j.radius.core.attribute.attributes.UserName;
import org.aaa4j.radius.core.attribute.attributes.UserPassword;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.packets.AccessAccept;
import org.aaa4j.radius.core.packet.packets.AccessRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.vsu.uic.wasp.ng.core.exception.ExternalSystemException;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A simple RADIUS-client to be used in the RadiusAuthenticationProvider class.
 */
@Component
@Slf4j
public class RadiusClient {

    @Value("${wasp.radius.secret}")
    private String radiusSecret;

    @Value("${wasp.radius.nas.identifier}")
    private String radiusNasIdentifier;

    @Value("${wasp.radius.ip}")
    private String radiusIp;

    @Value("${wasp.radius.port:1812}")
    private int radiusPort;

    @Value("${wasp.radius.nas.ip}")
    private String radiusNasIp;

    private UdpRadiusClient radiusClient;

    public boolean isAccepted(String userName, String password) throws ExternalSystemException {
        log.debug("username = {}", userName);
        boolean accepted;
        try {
            log.debug("Build the request...");
            AccessRequest accessRequest = new AccessRequest(List.of(
                    new UserName(new TextData(userName)),
                    new UserPassword(new StringData(password.getBytes(UTF_8))),
                    new NasIpAddress(new Ipv4AddrData((Inet4Address) Inet4Address.getByName(radiusNasIp))),
                    new NasIdentifier(new TextData(radiusNasIdentifier))
            ));
            log.debug("Send the request...");
            Packet responsePacket = radiusClient.send(accessRequest);
            log.debug("Verify the response...");
            accepted = responsePacket instanceof AccessAccept;
        } catch (UnknownHostException | RadiusClientException e) {
            log.error("{}", e.getMessage(), e);
            throw new ExternalSystemException("Unexpected external system error!", e);
        }
        log.debug("RADIUS accepted: {}", accepted);
        return accepted;
    }

    @PostConstruct
    protected void postConstruct() {
        radiusClient = UdpRadiusClient.newBuilder()
                .secret(radiusSecret.getBytes(UTF_8))
                .address(new InetSocketAddress(radiusIp, radiusPort))
                .build();
    }

}
