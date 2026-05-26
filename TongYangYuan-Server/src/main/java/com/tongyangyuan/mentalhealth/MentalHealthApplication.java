package com.tongyangyuan.mentalhealth;

import com.tongyangyuan.mentalhealth.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@SpringBootApplication
@RestController
public class MentalHealthApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(MentalHealthApplication.class);
    
    public static void main(String[] args) {
        SpringApplication.run(MentalHealthApplication.class, args);
    }
    
    /**
     * 检查数据库表是否存在
     */
    @GetMapping("/check-tables")
    public String checkTables(DataSource dataSource) {
        StringBuilder result = new StringBuilder();
        result.append("=== 数据库表检查 ===\n\n");
        
        String[] requiredTables = {
            "membership_packages",
            "payment_orders", 
            "membership_records",
            "payment_config"
        };
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            
            for (String tableName : requiredTables) {
                boolean exists = false;
                try (ResultSet rs = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
                    exists = rs.next();
                }
                
                String status = exists ? "✓ 存在" : "✗ 不存在";
                result.append(tableName).append(": ").append(status).append("\n");
                
                if (exists) {
                    // 获取列信息
                    try (ResultSet columns = metaData.getColumns(catalog, null, tableName, null)) {
                        result.append("  列: ");
                        while (columns.next()) {
                            result.append(columns.getString("COLUMN_NAME")).append(" ");
                        }
                        result.append("\n");
                    }
                }
            }
            
            result.append("\n=== 建议操作 ===\n");
            result.append("如果表不存在，请执行: manual_init.sql\n");
            
        } catch (Exception e) {
            result.append("检查失败: ").append(e.getMessage());
            logger.error("检查数据库表失败", e);
        }
        
        return result.toString();
    }
    
    /**
     * 初始化支付系统数据（如果表已存在但数据为空）
     */
    @Bean
    CommandLineRunner initPaymentData(
            com.tongyangyuan.mentalhealth.repository.MembershipPackageRepository packageRepository) {
        return args -> {
            logger.info("检查支付系统初始化状态...");
            try {
                // 如果套餐表为空，插入默认套餐
                if (packageRepository.count() == 0) {
                    logger.info("插入默认套餐...");
                    
                    com.tongyangyuan.mentalhealth.entity.MembershipPackage month = new com.tongyangyuan.mentalhealth.entity.MembershipPackage();
                    month.setName("月度会员");
                    month.setCode("month");
                    month.setPrice(new java.math.BigDecimal("99.00"));
                    month.setOriginalPrice(new java.math.BigDecimal("99.00"));
                    month.setDays(30);
                    month.setDescription("30天无限次咨询服务");
                    month.setFeatures("{\"consultations\": -1, \"video_calls\": -1, \"pdf_export\": true}");
                    month.setSortOrder(1);
                    month.setIsActive(true);
                    packageRepository.save(month);
                    
                    com.tongyangyuan.mentalhealth.entity.MembershipPackage quarter = new com.tongyangyuan.mentalhealth.entity.MembershipPackage();
                    quarter.setName("季度会员");
                    quarter.setCode("quarter");
                    quarter.setPrice(new java.math.BigDecimal("269.00"));
                    quarter.setOriginalPrice(new java.math.BigDecimal("299.00"));
                    quarter.setDays(90);
                    quarter.setDescription("90天无限次咨询服务 · 省30元");
                    quarter.setFeatures("{\"consultations\": -1, \"video_calls\": -1, \"pdf_export\": true, \"priority_support\": true}");
                    quarter.setSortOrder(2);
                    quarter.setIsActive(true);
                    packageRepository.save(quarter);
                    
                    logger.info("默认套餐插入完成");
                } else {
                    logger.info("套餐已存在，跳过初始化");
                }
            } catch (Exception e) {
                logger.error("支付系统初始化失败", e);
            }
        };
    }
}
