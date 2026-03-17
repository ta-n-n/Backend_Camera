package com.example.camerasurveillancesystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    
    // Camera Errors (1xxx)
    CAMERA_NOT_FOUND("CAMERA_1001", "Không tìm thấy camera", HttpStatus.NOT_FOUND),
    CAMERA_CODE_ALREADY_EXISTS("CAMERA_1002", "Mã camera đã tồn tại", HttpStatus.BAD_REQUEST),
    CAMERA_CODE_INVALID("CAMERA_1003", "Mã camera không hợp lệ", HttpStatus.BAD_REQUEST),
    CAMERA_STATUS_INVALID("CAMERA_1004", "Trạng thái camera không hợp lệ", HttpStatus.BAD_REQUEST),
    CAMERA_DELETION_FAILED("CAMERA_1005", "Không thể xóa camera", HttpStatus.BAD_REQUEST),
    CAMERA_UPDATE_FAILED("CAMERA_1006", "Không thể cập nhật camera", HttpStatus.BAD_REQUEST),
    
    // Location Errors (2xxx)
    LOCATION_NOT_FOUND("LOCATION_2001", "Không tìm thấy vị trí", HttpStatus.NOT_FOUND),
    LOCATION_INVALID("LOCATION_2002", "Vị trí không hợp lệ", HttpStatus.BAD_REQUEST),
    
    // Group Errors (3xxx)
    GROUP_NOT_FOUND("GROUP_3001", "Không tìm thấy nhóm camera", HttpStatus.NOT_FOUND),
    GROUP_NAME_ALREADY_EXISTS("GROUP_3002", "Tên nhóm đã tồn tại", HttpStatus.BAD_REQUEST),
    
    // AI Model Errors (4xxx)
    AI_MODEL_NOT_FOUND("AI_MODEL_4001", "Không tìm thấy mô hình AI", HttpStatus.NOT_FOUND),
    AI_MODEL_INACTIVE("AI_MODEL_4002", "Mô hình AI chưa được kích hoạt", HttpStatus.BAD_REQUEST),
    AI_MODEL_LOAD_FAILED("AI_MODEL_4003", "Không thể tải mô hình AI", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // AI Event Errors (5xxx)
    AI_EVENT_NOT_FOUND("AI_EVENT_5001", "Không tìm thấy sự kiện AI", HttpStatus.NOT_FOUND),
    AI_EVENT_PROCESSING_FAILED("AI_EVENT_5002", "Xử lý sự kiện AI thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // Alert Errors (6xxx)
    ALERT_NOT_FOUND("ALERT_6001", "Không tìm thấy cảnh báo", HttpStatus.NOT_FOUND),
    ALERT_ALREADY_RESOLVED("ALERT_6002", "Cảnh báo đã được giải quyết", HttpStatus.BAD_REQUEST),
    ALERT_STATUS_INVALID("ALERT_6003", "Trạng thái cảnh báo không hợp lệ", HttpStatus.BAD_REQUEST),
    
    // User Errors (7xxx)
    USER_NOT_FOUND("USER_7001", "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_7002", "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_INACTIVE("USER_7003", "Tài khoản người dùng đã bị khóa", HttpStatus.FORBIDDEN),
    USER_EMAIL_NOT_VERIFIED("USER_7004", "Email chưa được xác thực", HttpStatus.FORBIDDEN),
    
    // Role Errors (7xxx)
    ROLE_NOT_FOUND("ROLE_7005", "Không tìm thấy vai trò", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS("ROLE_7006", "Vai trò đã tồn tại", HttpStatus.BAD_REQUEST),
    
    // Permission Errors (7xxx)
    PERMISSION_NOT_FOUND("PERMISSION_7007", "Không tìm thấy quyền", HttpStatus.NOT_FOUND),
    PERMISSION_ALREADY_EXISTS("PERMISSION_7008", "Quyền đã tồn tại", HttpStatus.BAD_REQUEST),
    
    // Authentication Errors (8xxx)
    UNAUTHORIZED("AUTH_8001", "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("AUTH_8002", "Thông tin đăng nhập không chính xác", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_8003", "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH_8004", "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    TOKEN_BLACKLISTED("AUTH_8005", "Token đã bị vô hiệu hóa", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH_8006", "Không có quyền truy cập", HttpStatus.FORBIDDEN),
    
    // Validation Errors (9xxx)
    VALIDATION_ERROR("VALIDATION_9001", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING("VALIDATION_9002", "Thiếu trường bắt buộc", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("VALIDATION_9003", "Định dạng không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER("VALIDATION_9004", "Tham số không hợp lệ", HttpStatus.BAD_REQUEST),
    
    // Processing Job Errors (10xxx)
    JOB_NOT_FOUND("JOB_10001", "Không tìm thấy công việc", HttpStatus.NOT_FOUND),
    JOB_ALREADY_RUNNING("JOB_10002", "Công việc đang chạy", HttpStatus.BAD_REQUEST),
    JOB_EXECUTION_FAILED("JOB_10003", "Thực thi công việc thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // Stream Errors (11xxx)
    STREAM_NOT_FOUND("STREAM_11001", "Không tìm thấy luồng video", HttpStatus.NOT_FOUND),
    STREAM_CONNECTION_FAILED("STREAM_11002", "Kết nối luồng video thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    STREAM_INACTIVE("STREAM_11003", "Luồng video không hoạt động", HttpStatus.BAD_REQUEST),
    
    // Notification Errors (12xxx)
    NOTIFICATION_SEND_FAILED("NOTIFICATION_12001", "Gửi thông báo thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_CHANNEL_NOT_FOUND("NOTIFICATION_12002", "Không tìm thấy kênh thông báo", HttpStatus.NOT_FOUND),
    NOTIFICATION_CHANNEL_INACTIVE("NOTIFICATION_12003", "Kênh thông báo không hoạt động", HttpStatus.BAD_REQUEST),
    
    // Storage Errors (13xxx)
    FILE_NOT_FOUND("STORAGE_13001", "Không tìm thấy file", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED("STORAGE_13002", "Tải file lên thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("STORAGE_13003", "Xóa file thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    STORAGE_FULL("STORAGE_13004", "Dung lượng lưu trữ đã đầy", HttpStatus.INSUFFICIENT_STORAGE),
    
    // System Errors (14xxx)
    INTERNAL_SERVER_ERROR("SYSTEM_14001", "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("SYSTEM_14002", "Dịch vụ không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
    DATABASE_ERROR("SYSTEM_14003", "Lỗi cơ sở dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR),
    NETWORK_ERROR("SYSTEM_14004", "Lỗi kết nối mạng", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // Business Logic Errors (15xxx)
    BUSINESS_LOGIC_ERROR("BUSINESS_15001", "Lỗi logic nghiệp vụ", HttpStatus.BAD_REQUEST),
    DUPLICATE_ENTRY("BUSINESS_15002", "Dữ liệu trùng lặp", HttpStatus.CONFLICT),
    RESOURCE_IN_USE("BUSINESS_15003", "Tài nguyên đang được sử dụng", HttpStatus.CONFLICT),
    OPERATION_NOT_ALLOWED("BUSINESS_15004", "Thao tác không được phép", HttpStatus.FORBIDDEN);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
