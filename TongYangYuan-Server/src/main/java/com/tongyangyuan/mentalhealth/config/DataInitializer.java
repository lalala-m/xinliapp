package com.tongyangyuan.mentalhealth.config;

import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ConsultantRepository consultantRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, ConsultantRepository consultantRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.consultantRepository = consultantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // initializeUsers();
        // 数据初始化逻辑已移至手动执行或仅首次执行
    }

    private void initializeUsers() {
        // 1. 确保存在 ID 为 1 的家长用户 (用于测试预约)
        // 注意：ID 是自增的，我们不能强制设为 1，但可以确保至少有一个家长用户
        // 前端 mock-data.js 中写死 parentUserId = 1，这里我们需要尽量匹配，或者至少保证有数据
        
        // 检查是否存在手机号为 13900000001 的家长
        if (!userRepository.existsByPhone("13900000001")) {
            User parent = new User();
            parent.setPhone("13900000001");
            parent.setPassword(passwordEncoder.encode("123456"));
            parent.setUserType(User.UserType.PARENT);
            parent.setNickname("测试家长");
            parent.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(parent);
            System.out.println("DataInitializer: Created test parent user (13900000001)");
        }

        // 2. 确保存在测试咨询师 (张医生)
        // 前端登录账号: 13800000001
        User consultantUser = userRepository.findByPhone("13800000001").orElse(null);
        if (consultantUser == null) {
            consultantUser = new User();
            consultantUser.setPhone("13800000001");
            consultantUser.setPassword(passwordEncoder.encode("123456"));
            consultantUser.setUserType(User.UserType.CONSULTANT);
            consultantUser.setNickname("张医生");
            consultantUser.setStatus(User.UserStatus.ACTIVE);
            consultantUser = userRepository.save(consultantUser);
            System.out.println("DataInitializer: Created test consultant user (13800000001)");
        }

        // 3. 确保该咨询师用户有关联的 Consultant 记录
        if (!consultantRepository.existsByUserId(consultantUser.getId())) {
            Consultant consultant = new Consultant();
            consultant.setUserId(consultantUser.getId());
            consultant.setName("张医生");
            consultant.setTitle("资深心理咨询师");
            consultant.setSpecialty("青少年心理, 亲子关系");
            consultant.setIntro("从事心理咨询工作10年，擅长解决青少年成长问题。");
            consultant.setAvailable(true);
            // 其他字段使用默认值
            consultantRepository.save(consultant);
            System.out.println("DataInitializer: Created consultant profile for user " + consultantUser.getId());
        }
        
        // 4. 确保 ID 为 1 的用户存在 (为了兼容前端硬编码)
        // 如果 ID 1 被占用了但不是我们想要的，也没关系，只要数据库里有数据，外键约束通常是引用的 ID
        // 如果是空库，上面的插入操作应该会产生 ID 1, 2
    }
}
