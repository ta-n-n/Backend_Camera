package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Long>, JpaSpecificationExecutor<Camera> {
    
    /**
     * Tìm camera theo mã code (unique)
     */
    Optional<Camera> findByCode(String code);
    
    /**
     * Kiểm tra camera có tồn tại theo code không
     */
    boolean existsByCode(String code);
    
    /**
     * Tìm camera theo trạng thái
     */
    List<Camera> findByStatus(String status);
    
    /**
     * Tìm camera theo location
     */
    List<Camera> findByLocationId(Long locationId);
    
    /**
     * Tìm camera theo tên (không phân biệt hoa thường)
     */
    List<Camera> findByNameContainingIgnoreCase(String name);
    
    /**
     * Tìm camera theo model và manufacturer
     */
    List<Camera> findByModelAndManufacturer(String model, String manufacturer);
    
    /**
     * Đếm số camera theo trạng thái
     */
    @Query("SELECT COUNT(c) FROM Camera c WHERE c.status = :status")
    long countByStatus(@Param("status") String status);
    
    /**
     * Lấy tất cả camera theo danh sách ID
     */
    List<Camera> findByIdIn(List<Long> ids);
    
    /**
     * Tìm camera active theo group
     */
    @Query("SELECT c FROM Camera c JOIN c.groups g WHERE g.id = :groupId AND c.status = 'ACTIVE'")
    List<Camera> findActiveCamerasByGroupId(@Param("groupId") Long groupId);
}
