package com.example.camerasurveillancesystem.config;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraGroup;
import com.example.camerasurveillancesystem.domain.CameraLocation;
import com.example.camerasurveillancesystem.repository.CameraGroupRepository;
import com.example.camerasurveillancesystem.repository.CameraLocationRepository;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CameraDataSeeder {

    private final CameraRepository cameraRepository;
    private final CameraLocationRepository locationRepository;
    private final CameraGroupRepository groupRepository;

    /**
     * Seed fake camera data for testing
     * Only runs when profile "seed" is active: --spring.profiles.active=seed
     */
    @Bean
    @Profile("seed")
    public CommandLineRunner seedCameraData() {
        return args -> {
            log.info("🌱 Starting camera data seeding...");

            // Check if data already exists
            if (cameraRepository.count() >= 50) {
                log.info("✅ Camera data already seeded ({} cameras exist)", cameraRepository.count());
                return;
            }

            // 1. Create locations (10 locations)
            List<CameraLocation> locations = createLocations();
            log.info("✅ Created {} locations", locations.size());

            // 2. Create camera groups (5 groups)
            List<CameraGroup> groups = createGroups();
            log.info("✅ Created {} camera groups", groups.size());

            // 3. Create 50 cameras
            List<Camera> cameras = createCameras(locations, groups);
            log.info("✅ Created {} cameras", cameras.size());

            log.info("🎉 Camera data seeding completed!");
            log.info("📊 Summary: {} locations, {} groups, {} cameras", 
                locations.size(), groups.size(), cameras.size());
        };
    }

    private List<CameraLocation> createLocations() {
        List<CameraLocation> locations = new ArrayList<>();

        String[][] locationData = {
            {"Tầng 1 - Lễ tân", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729170", "106.719023", "TP.HCM", "Quận 7"},
            {"Tầng 2 - Văn phòng A", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729180", "106.719033", "TP.HCM", "Quận 7"},
            {"Tầng 3 - Văn phòng B", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729190", "106.719043", "TP.HCM", "Quận 7"},
            {"Tầng 4 - Khu họp", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729200", "106.719053", "TP.HCM", "Quận 7"},
            {"Tầng 5 - Khu giải trí", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729210", "106.719063", "TP.HCM", "Quận 7"},
            {"Bãi đỗ xe tầng hầm 1", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729220", "106.719073", "TP.HCM", "Quận 7"},
            {"Bãi đỗ xe tầng hầm 2", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729230", "106.719083", "TP.HCM", "Quận 7"},
            {"Sân vườn phía Đông", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729240", "106.719093", "TP.HCM", "Quận 7"},
            {"Sân vườn phía Tây", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729250", "106.719103", "TP.HCM", "Quận 7"},
            {"Khu bảo vệ cổng chính", "123 Nguyễn Văn Linh, Q7, TP.HCM", "10.729260", "106.719113", "TP.HCM", "Quận 7"}
        };

        for (String[] data : locationData) {
            CameraLocation location = new CameraLocation();
            location.setName(data[0]);
            location.setAddress(data[1]);
            location.setLatitude(Double.parseDouble(data[2]));
            location.setLongitude(Double.parseDouble(data[3]));
            location.setCity(data[4]);
            location.setDistrict(data[5]);
            location.setDescription("Vị trí camera giám sát " + data[0]);
            locations.add(locationRepository.save(location));
        }

        return locations;
    }

    private List<CameraGroup> createGroups() {
        List<CameraGroup> groups = new ArrayList<>();

        String[][] groupData = {
            {"Camera cổng ra vào", "Giám sát các điểm ra vào chính"},
            {"Camera bãi đỗ xe", "Giám sát toàn bộ khu vực đỗ xe"},
            {"Camera văn phòng", "Giám sát các tầng văn phòng làm việc"},
            {"Camera ngoài trời", "Giám sát khu vực sân vườn bên ngoài"},
            {"Camera an ninh", "Hệ thống camera quan trọng cho bảo vệ"}
        };

        for (String[] data : groupData) {
            CameraGroup group = new CameraGroup();
            group.setName(data[0]);
            group.setDescription(data[1]);
            groups.add(groupRepository.save(group));
        }

        return groups;
    }

    private List<Camera> createCameras(List<CameraLocation> locations, List<CameraGroup> groups) {
        List<Camera> cameras = new ArrayList<>();
        Random random = new Random();

        String[] statuses = {"ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "INACTIVE", "MAINTENANCE"};
        String[] manufacturers = {"Hikvision", "Dahua", "Axis", "Bosch", "Samsung", "Sony"};
        String[] models = {"DS-2CD2143G0-I", "IPC-HDW5831R-ZE", "M3046-V", "FLEXIDOME IP 5000i", "QNO-8080R", "SNC-EM632R"};
        String[] resolutions = {"1920x1080", "2560x1440", "3840x2160", "1280x720"};
        Integer[] frameRates = {15, 20, 25, 30};

        for (int i = 1; i <= 50; i++) {
            Camera camera = new Camera();
            camera.setName("Camera " + String.format("%02d", i));
            camera.setCode("CAM-" + String.format("%03d", i));
            camera.setManufacturer(manufacturers[random.nextInt(manufacturers.length)]);
            camera.setModel(models[random.nextInt(models.length)]);
            camera.setStatus(statuses[random.nextInt(statuses.length)]);
            camera.setResolution(resolutions[random.nextInt(resolutions.length)]);
            camera.setFrameRate(frameRates[random.nextInt(frameRates.length)]);
            
            // Realistic RTSP URLs
            camera.setRtspUrl(String.format("rtsp://192.168.1.%d:554/stream1", 100 + i));
            camera.setSnapshotUrl(String.format("http://192.168.1.%d/snapshot.jpg", 100 + i));
            
            camera.setDescription("Camera giám sát số " + i + " - Tự động fake data");

            // Assign random location
            camera.setLocation(locations.get(random.nextInt(locations.size())));

            // Assign 1-3 random groups
            Set<CameraGroup> cameraGroups = new HashSet<>();
            int numGroups = 1 + random.nextInt(3); // 1 to 3 groups
            for (int j = 0; j < numGroups; j++) {
                cameraGroups.add(groups.get(random.nextInt(groups.size())));
            }
            camera.setGroups(cameraGroups);

            cameras.add(cameraRepository.save(camera));
        }

        return cameras;
    }
}
