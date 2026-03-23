package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    long countByAppointmentDate(LocalDate date);
    List<Appointment> findByConsultantIdOrderByCreatedAtDesc(Long consultantId);
    List<Appointment> findByParentUserIdOrderByCreatedAtDesc(Long parentUserId);
    List<Appointment> findByConsultantIdAndStatusOrderByCreatedAtDesc(Long consultantId, Appointment.AppointmentStatus status);
    boolean existsByConsultantIdAndAppointmentDateAndTimeSlotAndStatusNot(Long consultantId, LocalDate appointmentDate, String timeSlot, Appointment.AppointmentStatus status);
}
