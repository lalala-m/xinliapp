package com.tongyangyuan.mentalhealth.config;

import com.tongyangyuan.mentalhealth.entity.SpecialtyCategory;
import com.tongyangyuan.mentalhealth.entity.SpecialtyTag;
import com.tongyangyuan.mentalhealth.repository.SpecialtyCategoryRepository;
import com.tongyangyuan.mentalhealth.repository.SpecialtyTagRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SpecialtyCategoryRepository categoryRepository;
    private final SpecialtyTagRepository tagRepository;

    public DataInitializer(SpecialtyCategoryRepository categoryRepository,
                           SpecialtyTagRepository tagRepository) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public void run(String... args) {
        // 如果已经有数据，跳过
        if (categoryRepository.count() > 0) {
            return;
        }

        // 1. 创建9大分类
        List<SpecialtyCategory> categories = Arrays.asList(
            createCategory("mental_health", "心理健康", 1),
            createCategory("love", "恋爱心理", 2),
            createCategory("interpersonal", "人际关系", 3),
            createCategory("emotion", "情绪管理", 4),
            createCategory("marriage", "婚姻家庭", 5),
            createCategory("parenting", "亲子教育", 6),
            createCategory("career", "职场心理", 7),
            createCategory("sexual", "性心理", 8),
            createCategory("growth", "个人成长", 9)
        );
        categoryRepository.saveAll(categories);

        // 重新加载以获取ID
        categories = categoryRepository.findAllByOrderBySortOrderAsc();

        // 2. 为每个分类创建标签
        for (SpecialtyCategory cat : categories) {
            List<String> tagNames = getTagsForCategory(cat.getCode());
            int sortOrder = 1;
            for (String name : tagNames) {
                SpecialtyTag tag = new SpecialtyTag();
                tag.setCategoryId(cat.getId());
                tag.setName(name);
                tag.setSortOrder(sortOrder++);
                tagRepository.save(tag);
            }
        }
    }

    private SpecialtyCategory createCategory(String code, String name, int sortOrder) {
        SpecialtyCategory cat = new SpecialtyCategory();
        cat.setCode(code);
        cat.setName(name);
        cat.setSortOrder(sortOrder);
        return cat;
    }

    private List<String> getTagsForCategory(String code) {
        switch (code) {
            case "mental_health":
                return Arrays.asList(
                    "抑郁症", "焦虑症", "心理创伤", "双相情感障碍", "强迫症",
                    "注意力缺陷多动障碍ADHD", "PTSD创伤应激综合症", "躯体化反应", "睡眠问题",
                    "回避型人格", "社交恐惧", "自杀倾向", "自残行为问题", "边缘型人格障碍BPD",
                    "微笑抑郁症", "暴饮暴食", "成瘾问题", "人格障碍", "丧失与哀伤辅导",
                    "偏执型人格障碍", "自恋性人格特质", "表演型人格", "妄想性障碍", "恋物癖",
                    "产后抑郁", "性别认同", "躁狂症", "自闭症", "冲动控制/破坏性行为",
                    "斯德哥尔摩综合症", "恐高", "进食障碍(厌食症)", "怀孕/围产期/产后心理健康",
                    "异装症", "幽闭恐惧症", "更年期情绪问题", "巨物恐惧症", "器质性精神障碍", "异食症"
                );
            case "love":
                return Arrays.asList(
                    "失恋分手", "情感创伤", "回避型依恋", "分离焦虑", "择偶焦虑",
                    "前任情结", "第三者困扰", "多重恋爱困扰", "异地恋", "处女情结",
                    "结婚恐惧", "单恋困扰", "冷暴力", "相亲恐惧", "爱情嫉妒",
                    "性单恋", "恋爱成瘾", "处男情结"
                );
            case "interpersonal":
                return Arrays.asList(
                    "人际关系敏感", "社交焦虑", "社交困难", "社交退缩", "社交技巧",
                    "社交孤立", "异性恐惧", "被害妄想", "友谊嫉妒", "对视恐惧",
                    "余光恐惧", "网络人际关系成瘾", "电话恐惧", "网络暴力"
                );
            case "emotion":
                return Arrays.asList(
                    "内耗情绪", "焦虑情绪", "抑郁情绪", "恐惧", "无价值感",
                    "情绪调节困难", "暴躁易怒", "情绪低落", "孤独", "自责",
                    "情绪失控", "死亡焦虑", "空虚感", "压抑", "情绪性进食",
                    "情感淡漠", "怨恨心理", "失败恐惧", "空心病", "负债",
                    "犯罪恐惧", "外表焦虑", "躁狂"
                );
            case "marriage":
                return Arrays.asList(
                    "原生家庭创伤", "伴侣沟通", "婚外性", "关系修复", "亲密关系",
                    "老公出轨", "亲子沟通", "婚姻危机", "家庭冲突", "离婚咨询",
                    "婆媳矛盾", "精神出轨", "感情变故", "三角关系", "依恋问题",
                    "婚姻倦怠", "重男轻女", "冷暴力", "催婚压力", "家暴创伤",
                    "恐婚", "育儿分歧", "分娩恐惧", "丧偶"
                );
            case "parenting":
                return Arrays.asList(
                    "休学厌学", "青少年情绪问题", "青少年抑郁", "青少年学业压力", "未成年网瘾",
                    "青少年人际关系", "自卑/不自信", "学习拖延", "性教育", "青春期性心理",
                    "考试焦虑", "恋母情结", "青春期叛逆", "早恋", "校园暴力创伤",
                    "儿童分离焦虑", "注意力问题", "暴力倾向", "多动症", "校园欺凌",
                    "逃学", "未成年人性别认知困难", "攻击行为问题", "青春期风险行为",
                    "儿童自闭倾向", "幼儿入园恐惧", "自理困难"
                );
            case "career":
                return Arrays.asList(
                    "工作迷茫", "职业发展", "职场人际", "职业倦怠", "失业焦虑",
                    "就业压力", "入职焦虑", "内卷", "沟通技能", "职场PUA",
                    "职场霸凌", "职场晋升", "潜规则", "跳槽", "同级关系",
                    "职场信任", "职场恋情", "职场歧视"
                );
            case "sexual":
                return Arrays.asList(
                    "性瘾", "性变态", "性取向", "性生活不协调", "同性恋",
                    "性健康和功能性障碍", "性创伤", "双性恋", "性冷淡",
                    "性侵创伤症候群", "跨性别"
                );
            case "growth":
                return Arrays.asList(
                    "童年创伤", "自我探索", "自我价值", "学业压力", "女性成长",
                    "自卑", "讨好型人格", "完美主义", "拖延症", "性格缺陷",
                    "男性成长", "时间管理", "选择困难", "抗挫力", "自尊",
                    "宗教信仰", "中年危机", "同一性混乱", "留学适应", "睡前拖延",
                    "彼得潘综合征", "非理性消费", "皮肤饥渴", "反社会行为",
                    "跨文化适应压力", "虚荣心"
                );
            default:
                return Arrays.asList();
        }
    }
}
