package com.soa.auth.config;

import com.soa.auth.entity.User;
import com.soa.auth.entity.UserIdentity;
import com.soa.auth.repository.UserIdentityRepository;
import com.soa.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu người dùng mẫu cho môi trường Seminar/Demo...");

            // 1. Sinh viên TDTU chính thức (Tác giả)
            User quanStudent = userRepository.save(User.builder()
                    .email("524h0122@student.tdtu.edu.vn")
                    .fullName("Nguyễn Anh Quân")
                    .accountType("STUDENT")
                    .role("ROLE_USER")
                    .avatarUrl("https://ui-avatars.com/api/?name=Nguyen+Anh+Quan")
                    .build());

            // 2. Tài khoản Google cá nhân của tác giả
            User quanPersonal = userRepository.save(User.builder()
                    .email("quan03313@gmail.com")
                    .fullName("Quân học lập trình")
                    .accountType("PERSONAL")
                    .role("ROLE_USER")
                    .avatarUrl("https://ui-avatars.com/api/?name=Quan+Lap+Trinh")
                    .build());

            // 3. Tài khoản sinh viên mẫu
            User studentDemo = userRepository.save(User.builder()
                    .email("52100888@student.tdtu.edu.vn")
                    .fullName("Sinh Vien TDTU")
                    .accountType("STUDENT")
                    .role("ROLE_USER")
                    .avatarUrl("https://ui-avatars.com/api/?name=Sinh+Vien+TDTU")
                    .build());

            // 4. Tài khoản test tự động hóa (Dùng trong test-external-actors.sh)
            User testUser = userRepository.save(User.builder()
                    .email("hieuhieu5933@gmail.com")
                    .fullName("Hieu Hieu")
                    .accountType("PERSONAL")
                    .role("ROLE_USER")
                    .avatarUrl("https://ui-avatars.com/api/?name=Hieu+Hieu")
                    .build());

            // Gắn Google Identity mẫu cho hieuhieu5933 để test kịch bản unlinking & linked-accounts
            userIdentityRepository.save(UserIdentity.builder()
                    .user(testUser)
                    .provider("GOOGLE")
                    .providerUserId("106790612631009028951")
                    .providerEmail("hieuhieu5933@gmail.com")
                    .accessToken("ya29.a0AdM_demo_token_for_hieuhieu")
                    .build());

            log.info("Khởi tạo dữ liệu mẫu thành công: 4 tài khoản người dùng và liên kết Google identity.");
        }
    }
}
