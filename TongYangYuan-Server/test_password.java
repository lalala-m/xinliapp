import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class test_password {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK";
        System.out.println("password123 matches: " + encoder.matches("password123", hash));
        System.out.println("admin123 matches: " + encoder.matches("admin123", hash));
    }
}
