package com.nckh.yte.service;

import com.nckh.yte.entity.*;
import com.nckh.yte.repository.*;
import com.nckh.yte.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor; // Đảm bảo import này
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.Random;

@Service
@RequiredArgsConstructor // Lombok sẽ tự động tiêm các 'final' repository VÀ service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final UserRepository userRepository;

    // ✅ BƯỚC 1: TIÊM GEMINI SERVICE
    private final GeminiService geminiService;

    // === CÁC HÀM GET (KHÔNG THAY ĐỔI) ===
    public List<Appointment> getAppointmentsForDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public List<Appointment> getAppointmentsForNurse(UUID nurseId) {
        return appointmentRepository.findByNurseId(nurseId);
    }

    public List<Appointment> getAppointmentsForPatient(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public Appointment create(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }


    // === HÀM AUTOBOOK ĐÃ SỬA ĐỔI ===
    @Transactional
    public Appointment autoBook(String patientName, String email, String phone, String gender,
                                String symptom, LocalDate preferredDate, String preferredWindow) {

        Patient patient = null;
        User user = null;

        // ✅ BƯỚC 2: SỬ DỤNG GEMINI ĐỂ XÁC ĐỊNH CHUYÊN KHOA
        String requiredSpecialty = null;
        try {
            // Thử gọi Gemini trước
            requiredSpecialty = geminiService.determineSpecialtyFromSymptom(symptom);
        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi gọi GeminiService, chuyển sang logic cũ. Lỗi: " + e.getMessage());
        }

        // Nếu Gemini lỗi, hoặc trả về null/rỗng, DÙNG LOGIC CŨ (FALLBACK)
        if (requiredSpecialty == null || requiredSpecialty.trim().isEmpty()) {
            System.out.println("GEMINI FAILED hoặc trả về rỗng. Đang dùng logic dự phòng (legacy).");
            requiredSpecialty = determineSpecialtyLegacy(symptom); // Đổi tên hàm cũ
        } else {
            System.out.println("GEMINI SUCCESS: Chuyên khoa được xác định: " + requiredSpecialty);
        }

        // ✅ BƯỚC 3: Tìm User và Patient (Logic giữ nguyên)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl principal) {
            if (principal.hasRole("PATIENT")) {
                user = userRepository.findByUsername(principal.getUsername()).orElse(null);
                if (user != null) {
                    patient = patientRepository.findByUser(user).orElse(null);
                }
            }
        }

        // ✅ BƯỚC 4: Logic tạo Patient (giữ nguyên, nhưng giờ 'requiredSpecialty' đã thông minh hơn)
        if (patient == null) {
            String fn, ln;
            String fullName = (user != null && user.getFullName() != null) ? user.getFullName() : patientName;

            if (fullName != null && !fullName.trim().isEmpty()) {
                String[] parts = fullName.trim().split("\\s+", 2);
                fn = parts.length > 0 ? parts[0] : "";
                ln = parts.length > 1 ? parts[1] : "";
            } else {
                fn = "";
                ln = "";
            }

            patient = patientRepository.save(
                    Patient.builder()
                            .firstName(fn)
                            .lastName(ln)
                            .user(user)
                            .department(requiredSpecialty) // Gán chuyên khoa ở đây
                            .email(email)
                            .phone(phone)
                            .gender(gender)
                            .build()
            );
        }

        // ✅ BƯỚC 5: CHỌN BÁC SĨ (Logic giữ nguyên)
        List<Doctor> specialists = doctorRepository.findBySpecialtyIgnoreCase(requiredSpecialty);
        Doctor doctor;
        
        if (!specialists.isEmpty()) {
            doctor = specialists.get(new Random().nextInt(specialists.size()));
        } else {
            // Fallback: Nếu không có BS chuyên khoa -> chuyển về "Đa khoa"
            List<Doctor> generalists = doctorRepository.findBySpecialtyIgnoreCase("Đa khoa");
            if (!generalists.isEmpty()) {
                System.out.println("Không tìm thấy BS chuyên khoa: " + requiredSpecialty + ". Chuyển về Đa khoa.");
                doctor = generalists.get(new Random().nextInt(generalists.size()));
            } else {
                // Trường hợp tệ nhất: Không có BS Đa khoa
                List<Doctor> allDoctors = doctorRepository.findAll();
                doctor = allDoctors.isEmpty() ? null : allDoctors.get(new Random().nextInt(allDoctors.size()));
            }
        }

        // ✅ BƯỚC 6: Logic tính thời gian (Logic giữ nguyên)
        LocalDateTime startTime;
        try {
            String startTimeStr = preferredWindow.split("\\s*-\\s*")[0]; // Lấy "08:00"
            LocalTime time = LocalTime.parse(startTimeStr); // Parse "08:00"
            startTime = preferredDate.atTime(time); // Ghép ngày và giờ
        } catch (Exception e) {
            startTime = preferredDate.atTime(8, 0); // Fallback nếu parse lỗi
        }
        LocalDateTime endTime = startTime.plusMinutes(30);

        // ✅ BƯỚC 7: Tạo Appointment (Logic giữ nguyên)
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .startTime(startTime)
                .endTime(endTime)
                .symptom(symptom)
                .status(AppointmentStatus.PENDING)
                .preferredDate(preferredDate)
                .preferredWindow(preferredWindow)
                .build();

        return appointmentRepository.save(appointment);
    }

    // ✅ BƯỚC 8: ĐỔI TÊN HÀM CŨ (dùng làm fallback)
    private String determineSpecialtyLegacy(String reason) {
        if (reason == null || reason.trim().isEmpty()) return "Đa khoa";
        String r = reason.toLowerCase();
        if (r.contains("tim") || r.contains("huyết áp")) return "Tim mạch";
        if (r.contains("xương") || r.contains("khớp") || r.contains("đau lưng")) return "Cơ xương khớp";
        if (r.contains("tai") || r.contains("mũi") || r.contains("họng")) return "Tai mũi họng";
        if (r.contains("da") || r.contains("ngứa")) return "Da liễu";
        if (r.contains("răng") || r.contains("nướu")) return "Răng hàm mặt";
        return "Đa khoa";
    }
}