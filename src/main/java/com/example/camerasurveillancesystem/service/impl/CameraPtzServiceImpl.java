package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.dto.response.PtzCommandResponse;
import com.example.camerasurveillancesystem.exception.ErrorCode;
import com.example.camerasurveillancesystem.exception.ResourceNotFoundException;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.CameraPtzService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class CameraPtzServiceImpl implements CameraPtzService {

    private static final String SOAP_ACTION_GET_PROFILES = "http://www.onvif.org/ver10/media/wsdl/GetProfiles";
    private static final String SOAP_ACTION_CONTINUOUS_MOVE = "http://www.onvif.org/ver20/ptz/wsdl/ContinuousMove";
    private static final String SOAP_ACTION_STOP = "http://www.onvif.org/ver20/ptz/wsdl/Stop";
    private static final int DEFAULT_SPEED_PERCENT = 40;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CameraRepository cameraRepository;

    @Value("${ptz.onvif.url-template:http://%s:2020/onvif/device_service}")
    private String onvifUrlTemplate;

    @Value("${ptz.onvif.request-timeout-ms:6000}")
    private int requestTimeoutMs;

    @Override
    public PtzCommandResponse panLeft(Long cameraId, Integer speedPercent, Long durationMs) {
        return continuousMove(cameraId, "PAN_LEFT", -toVelocity(speedPercent), 0.0, speedPercent, durationMs);
    }

    @Override
    public PtzCommandResponse panRight(Long cameraId, Integer speedPercent, Long durationMs) {
        return continuousMove(cameraId, "PAN_RIGHT", toVelocity(speedPercent), 0.0, speedPercent, durationMs);
    }

    @Override
    public PtzCommandResponse tiltUp(Long cameraId, Integer speedPercent, Long durationMs) {
        return continuousMove(cameraId, "TILT_UP", 0.0, toVelocity(speedPercent), speedPercent, durationMs);
    }

    @Override
    public PtzCommandResponse tiltDown(Long cameraId, Integer speedPercent, Long durationMs) {
        return continuousMove(cameraId, "TILT_DOWN", 0.0, -toVelocity(speedPercent), speedPercent, durationMs);
    }

    @Override
    public PtzCommandResponse stop(Long cameraId) {
        Camera camera = getCamera(cameraId);
        OnvifConnectionInfo connectionInfo = resolveOnvifConnectionInfo(camera);
        String profileToken = getProfileToken(connectionInfo);

        String body = buildStopSoapBody(profileToken);
        sendSoap(connectionInfo, SOAP_ACTION_STOP, body);

        log.info("PTZ STOP executed for cameraId={}, code={}", camera.getId(), camera.getCode());

        return PtzCommandResponse.builder()
                .cameraId(camera.getId())
                .cameraCode(camera.getCode())
                .command("STOP")
                .onvifUrl(connectionInfo.onvifUrl)
                .profileToken(profileToken)
                .executedAt(LocalDateTime.now())
                .build();
    }

    private PtzCommandResponse continuousMove(
            Long cameraId,
            String command,
            double xVelocity,
            double yVelocity,
            Integer speedPercent,
            Long durationMs
    ) {
        Camera camera = getCamera(cameraId);
        OnvifConnectionInfo connectionInfo = resolveOnvifConnectionInfo(camera);
        String profileToken = getProfileToken(connectionInfo);

        String timeout = toOnvifTimeout(durationMs);
        String body = buildContinuousMoveSoapBody(profileToken, xVelocity, yVelocity, timeout);
        sendSoap(connectionInfo, SOAP_ACTION_CONTINUOUS_MOVE, body);

        log.info("PTZ {} executed for cameraId={}, code={}, speedPercent={}, durationMs={}",
                command, camera.getId(), camera.getCode(), speedPercent, durationMs);

        return PtzCommandResponse.builder()
                .cameraId(camera.getId())
                .cameraCode(camera.getCode())
                .command(command)
                .onvifUrl(connectionInfo.onvifUrl)
                .profileToken(profileToken)
                .speedPercent(normalizeSpeedPercent(speedPercent))
                .durationMs(durationMs)
                .executedAt(LocalDateTime.now())
                .build();
    }

    private Camera getCamera(Long cameraId) {
        return cameraRepository.findById(cameraId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAMERA_NOT_FOUND));
    }

    private OnvifConnectionInfo resolveOnvifConnectionInfo(Camera camera) {
        String rtspUrl = trimToNull(camera.getRtspUrl());
        if (rtspUrl == null) {
            throw new IllegalArgumentException("Camera chưa có RTSP URL để suy ra ONVIF endpoint");
        }

        URI uri;
        try {
            uri = URI.create(rtspUrl);
        } catch (Exception e) {
            throw new IllegalArgumentException("RTSP URL không hợp lệ cho camera: " + camera.getCode());
        }

        String host = uri.getHost();
        if (trimToNull(host) == null) {
            throw new IllegalArgumentException("Không lấy được host từ RTSP URL của camera: " + camera.getCode());
        }

        String userInfo = trimToNull(uri.getUserInfo());
        if (userInfo == null || !userInfo.contains(":")) {
            throw new IllegalArgumentException("RTSP URL chưa có username/password (dạng rtsp://user:pass@host/...) để dùng ONVIF");
        }

        String[] parts = userInfo.split(":", 2);
        String username = decodeUserInfo(parts[0]);
        String password = decodeUserInfo(parts[1]);

        if (trimToNull(username) == null || trimToNull(password) == null) {
            throw new IllegalArgumentException("Không đọc được thông tin đăng nhập từ RTSP URL");
        }

        String onvifUrl = String.format(onvifUrlTemplate, host);
        return new OnvifConnectionInfo(onvifUrl, username, password);
    }

    private String getProfileToken(OnvifConnectionInfo connectionInfo) {
        String body = """
                <trt:GetProfiles xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\"/>
                """;
        String responseXml = sendSoap(connectionInfo, SOAP_ACTION_GET_PROFILES, body);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(responseXml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();
            String token = (String) xPath.evaluate("(//*[local-name()='Profiles'])[1]/@token", document, XPathConstants.STRING);
            token = trimToNull(token);
            if (token == null) {
                throw new IllegalArgumentException("Không tìm thấy profile token từ ONVIF response");
            }
            return token;
        } catch (Exception e) {
            throw new IllegalArgumentException("Không parse được ONVIF profile token: " + e.getMessage(), e);
        }
    }

    private String buildContinuousMoveSoapBody(String profileToken, double x, double y, String timeout) {
        String timeoutXml = timeout == null ? "" : "<tptz:Timeout>" + timeout + "</tptz:Timeout>";
        return """
                <tptz:ContinuousMove xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\"
                                     xmlns:tt=\"http://www.onvif.org/ver10/schema\">
                    <tptz:ProfileToken>%s</tptz:ProfileToken>
                    <tptz:Velocity>
                        <tt:PanTilt x=\"%s\" y=\"%s\"/>
                    </tptz:Velocity>
                    %s
                </tptz:ContinuousMove>
                """.formatted(profileToken, formatVelocity(x), formatVelocity(y), timeoutXml);
    }

    private String buildStopSoapBody(String profileToken) {
        return """
                <tptz:Stop xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\">
                    <tptz:ProfileToken>%s</tptz:ProfileToken>
                    <tptz:PanTilt>true</tptz:PanTilt>
                    <tptz:Zoom>true</tptz:Zoom>
                </tptz:Stop>
                """.formatted(profileToken);
    }

    private String sendSoap(OnvifConnectionInfo connectionInfo, String action, String bodyContent) {
        String envelope = buildSoapEnvelope(connectionInfo.username, connectionInfo.password, bodyContent);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(requestTimeoutMs))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connectionInfo.onvifUrl))
                .timeout(Duration.ofMillis(requestTimeoutMs))
                .header("Content-Type", "application/soap+xml; charset=utf-8")
                .header("SOAPAction", action)
                .header("Authorization", basicAuthHeader(connectionInfo.username, connectionInfo.password))
                .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IllegalArgumentException("ONVIF request failed with status " + response.statusCode()
                        + ", response=" + response.body());
            }
            return response.body();
        } catch (Exception e) {
            throw new IllegalArgumentException("Không gọi được ONVIF endpoint: " + e.getMessage(), e);
        }
    }

    private String buildSoapEnvelope(String username, String password, String bodyContent) {
        byte[] nonceBytes = new byte[16];
        SECURE_RANDOM.nextBytes(nonceBytes);

        String created = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String nonceBase64 = Base64.getEncoder().encodeToString(nonceBytes);
        String passwordDigest = buildPasswordDigest(nonceBytes, created, password);

        return """
                <?xml version=\"1.0\" encoding=\"UTF-8\"?>
                <soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"
                               xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"
                               xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">
                    <soap:Header>
                        <wsse:Security soap:mustUnderstand=\"true\">
                            <wsse:UsernameToken>
                                <wsse:Username>%s</wsse:Username>
                                <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password>
                                <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">%s</wsse:Nonce>
                                <wsu:Created>%s</wsu:Created>
                            </wsse:UsernameToken>
                        </wsse:Security>
                    </soap:Header>
                    <soap:Body>
                        %s
                    </soap:Body>
                </soap:Envelope>
                """.formatted(escapeXml(username), passwordDigest, nonceBase64, created, bodyContent);
    }

    private String basicAuthHeader(String username, String password) {
        String value = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String buildPasswordDigest(byte[] nonce, String created, String password) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(nonce);
            sha1.update(created.getBytes(StandardCharsets.UTF_8));
            sha1.update(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sha1.digest());
        } catch (Exception e) {
            throw new IllegalStateException("Không tạo được ONVIF password digest", e);
        }
    }

    private String toOnvifTimeout(Long durationMs) {
        if (durationMs == null || durationMs <= 0) {
            return null;
        }
        long seconds = Math.max(1L, Math.round(durationMs / 1000.0));
        return "PT" + seconds + "S";
    }

    private double toVelocity(Integer speedPercent) {
        int normalized = normalizeSpeedPercent(speedPercent);
        return normalized / 100.0;
    }

    private int normalizeSpeedPercent(Integer speedPercent) {
        if (speedPercent == null) {
            return DEFAULT_SPEED_PERCENT;
        }
        return Math.max(1, Math.min(100, speedPercent));
    }

    private String formatVelocity(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String decodeUserInfo(String value) {
        try {
            return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private record OnvifConnectionInfo(String onvifUrl, String username, String password) {
    }
}
