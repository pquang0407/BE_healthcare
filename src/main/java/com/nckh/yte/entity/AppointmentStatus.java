package com.nckh.yte.entity;

/**
 * Enumeration representing the lifecycle of an appointment.
 */
public enum AppointmentStatus {
    /** Yêu cầu mới, đang chờ hệ thống xử lý và xếp lịch. */
    PENDING,
    /** Đã được xếp lịch (có bác sĩ, thời gian cụ thể). */
    SCHEDULED,
    /** Đã hoàn thành. */
    COMPLETED,
    /** Đã hủy. */
    CANCELLED
}