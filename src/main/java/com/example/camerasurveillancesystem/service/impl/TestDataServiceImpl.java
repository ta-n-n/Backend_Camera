package com.example.camerasurveillancesystem.service.impl;

import com.example.camerasurveillancesystem.domain.Camera;
import com.example.camerasurveillancesystem.domain.CameraGroup;
import com.example.camerasurveillancesystem.domain.CameraLocation;
import com.example.camerasurveillancesystem.repository.CameraGroupRepository;
import com.example.camerasurveillancesystem.repository.CameraLocationRepository;
import com.example.camerasurveillancesystem.repository.CameraRepository;
import com.example.camerasurveillancesystem.service.TestDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TestDataServiceImpl implements TestDataService {

    private final CameraRepository cameraRepository;
    private final CameraLocationRepository locationRepository;
    private final CameraGroupRepository groupRepository;

    @Override
    public Map<String, Long> seedCameras(int count) {
        log.info("Starting to seed {} fake cameras", count);

        long initialCameraCount = cameraRepository.count();
        long initialLocationCount = locationRepository.count();
        long initialGroupCount = groupRepository.count();

        // 1. Create locations
        List<CameraLocation> locations = createLocations();
        log.info("Created {} locations", locations.size());

        // 2. Create groups
        List<CameraGroup> groups = createGroups();
        log.info("Created {} groups", groups.size());

        // 3. Create cameras
        List<Camera> cameras = createCameras(count, locations, groups);
        log.info("Created {} cameras", cameras.size());

        Map<String, Long> result = new HashMap<>();
        result.put("cameras", (long) cameras.size());
        result.put("locations", locationRepository.count() - initialLocationCount);
        result.put("groups", groupRepository.count() - initialGroupCount);
        result.put("total", cameraRepository.count());

        return result;
    }

    @Override
    public long clearTestCameras() {
        log.info("Clearing all test cameras with code starting with 'CAM-'");
        
        List<Camera> testCameras = cameraRepository.findAll().stream()
                .filter(camera -> camera.getCode().startsWith("CAM-"))
                .toList();
        
        long count = testCameras.size();
        cameraRepository.deleteAll(testCameras);
        
        log.info("Deleted {} test cameras", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCameraStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCameras = cameraRepository.count();
        long activeCameras = cameraRepository.countByStatus("ACTIVE");
        long inactiveCameras = cameraRepository.countByStatus("INACTIVE");
        long maintenanceCameras = cameraRepository.countByStatus("MAINTENANCE");
        long errorCameras = cameraRepository.countByStatus("ERROR");
        
        stats.put("total_cameras", totalCameras);
        stats.put("active_cameras", activeCameras);
        stats.put("inactive_cameras", inactiveCameras);
        stats.put("maintenance_cameras", maintenanceCameras);
        stats.put("error_cameras", errorCameras);
        stats.put("total_locations", locationRepository.count());
        stats.put("total_groups", groupRepository.count());
        
        // Test cameras count
        long testCameras = cameraRepository.findAll().stream()
                .filter(camera -> camera.getCode().startsWith("CAM-"))
                .count();
        stats.put("test_cameras", testCameras);
        
        return stats;
    }

    @Override
    public void resetAllCameras() {
        log.info("Resetting all cameras to ACTIVE status");
        
        List<Camera> allCameras = cameraRepository.findAll();
        allCameras.forEach(camera -> camera.setStatus("ACTIVE"));
        cameraRepository.saveAll(allCameras);
        
        log.info("Reset {} cameras", allCameras.size());
    }

    private List<CameraLocation> createLocations() {
        // Check if locations already exist
        if (locationRepository.count() >= 10) {
            log.info("Locations already exist, skipping creation");
            return locationRepository.findAll();
        }

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
        // Check if groups already exist
        List<CameraGroup> existingGroups = groupRepository.findAll();
        if (!existingGroups.isEmpty()) {
            log.info("Groups already exist, skipping creation");
            return existingGroups;
        }

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

    private List<Camera> createCameras(int count, List<CameraLocation> locations, List<CameraGroup> groups) {
        List<Camera> cameras = new ArrayList<>();
        Random random = new Random();

        String[] statuses = {"ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "INACTIVE", "MAINTENANCE"};
        String[] manufacturers = {"Hikvision", "Dahua", "Axis", "Bosch", "Samsung", "Sony"};
        String[] models = {"DS-2CD2143G0-I", "IPC-HDW5831R-ZE", "M3046-V", "FLEXIDOME IP 5000i", "QNO-8080R", "SNC-EM632R"};
        String[] resolutions = {"1920x1080", "2560x1440", "3840x2160", "1280x720"};
        Integer[] frameRates = {15, 20, 25, 30};

        // Get current max camera number
        long maxCameraNumber = cameraRepository.findAll().stream()
                .filter(c -> c.getCode().startsWith("CAM-"))
                .mapToLong(c -> {
                    try {
                        return Long.parseLong(c.getCode().substring(4));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        for (int i = 1; i <= count; i++) {
            long cameraNumber = maxCameraNumber + i;
            
            Camera camera = new Camera();
            camera.setName("Camera " + String.format("%02d", cameraNumber));
            camera.setCode("CAM-" + String.format("%03d", cameraNumber));
            camera.setManufacturer(manufacturers[random.nextInt(manufacturers.length)]);
            camera.setModel(models[random.nextInt(models.length)]);
            camera.setStatus(statuses[random.nextInt(statuses.length)]);
            camera.setResolution(resolutions[random.nextInt(resolutions.length)]);
            camera.setFrameRate(frameRates[random.nextInt(frameRates.length)]);
            
            // Realistic RTSP URLs
            camera.setRtspUrl(String.format("rtsp://192.168.1.%d:554/stream1", 100 + (cameraNumber % 150)));
            camera.setSnapshotUrl(String.format("http://192.168.1.%d/snapshot.jpg", 100 + (cameraNumber % 150)));
            
            camera.setDescription("Camera giám sát số " + cameraNumber + " - Tự động fake data");

            // Assign random location
            camera.setLocation(locations.get(random.nextInt(locations.size())));

            // Assign 1-3 random groups
            Set<CameraGroup> cameraGroups = new HashSet<>();
            int numGroups = 1 + random.nextInt(Math.min(3, groups.size()));
            for (int j = 0; j < numGroups; j++) {
                cameraGroups.add(groups.get(random.nextInt(groups.size())));
            }
            camera.setGroups(cameraGroups);

            cameras.add(cameraRepository.save(camera));
        }

        return cameras;
    }
}
