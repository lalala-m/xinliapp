package com.example.tongyangyuan;

import com.example.tongyangyuan.database.DataSyncService;
import com.example.tongyangyuan.database.entity.ConsultantEntity;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class ApiParsingTest {
    @Test
    public void parseConsultantsJson_success() {
        String json = "{\"code\":200,\"message\":\"Success\",\"data\":[{\"id\":1,\"userId\":10,\"name\":\"张三\",\"title\":\"高级咨询师\",\"specialty\":\"亲子沟通\",\"rating\":4.8,\"servedCount\":123,\"intro\":\"简介\",\"avatarColor\":\"#6FA6F8\",\"isAvailable\":true},{\"id\":2,\"userId\":11,\"name\":\"李四\",\"title\":\"咨询师\",\"specialty\":\"情绪管理\",\"rating\":5.0,\"servedCount\":45,\"intro\":\"简介2\",\"avatarColor\":\"#FF9900\",\"isAvailable\":true}]}";
        List<ConsultantEntity> list = DataSyncService.parseConsultantsJson(json);
        assertNotNull(list);
        assertEquals(2, list.size());
        ConsultantEntity c1 = list.get(0);
        assertEquals("张三", c1.getName());
        assertEquals("高级咨询师", c1.getTitle());
        assertEquals("亲子沟通", c1.getSpecialty());
        assertEquals(4.8, c1.getRating(), 0.001);
        assertEquals("123", c1.getServedCount());
        assertEquals("#6FA6F8", c1.getAvatarColor());
        ConsultantEntity c2 = list.get(1);
        assertEquals("李四", c2.getName());
        assertEquals("咨询师", c2.getTitle());
        assertEquals("情绪管理", c2.getSpecialty());
        assertEquals(5.0, c2.getRating(), 0.001);
        assertEquals("45", c2.getServedCount());
        assertEquals("#FF9900", c2.getAvatarColor());
    }
}
