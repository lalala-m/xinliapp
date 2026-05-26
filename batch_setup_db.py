#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Batch create consultants and assign tags via direct DB connection
"""

import pymysql
import random

# Connect to MySQL
conn = pymysql.connect(
    host='localhost',
    port=3306,
    user='root',
    password='123456',
    database='mental_health_db',
    charset='utf8mb4'
)
cursor = conn.cursor()

# 1. Clear existing tag assignments
print("Step 1: Clear existing tag assignments")
cursor.execute("DELETE FROM consultant_specialties")
conn.commit()

# 2. Get all tags
print("Step 2: Get all tags")
cursor.execute("SELECT id, name, category_id FROM specialty_tags ORDER BY id")
all_tags = cursor.fetchall()
print(f"Total tags: {len(all_tags)}")

# 3. Get existing consultants
print("Step 3: Get existing consultants")
cursor.execute("SELECT id, name, user_id FROM consultants ORDER BY id")
existing_consultants = cursor.fetchall()
print(f"Existing consultants: {len(existing_consultants)}")
for c in existing_consultants:
    print(f"  ID:{c[0]} Name:{c[1]} UserID:{c[2]}")

# 4. Assign tags to existing 4 consultants (6 tags each)
print("Step 4: Assign tags to existing consultants")
existing_assignments = {
    1: [1, 2, 3, 5, 9, 12],        # 张医生: 心理健康
    3: [40, 42, 48, 72, 75, 82],   # 咨询师一: 恋爱+情绪
    4: [95, 96, 102, 119, 125, 131], # 咨询师二: 婚姻+亲子
    5: [146, 149, 153, 175, 180, 183] # 咨询师三: 职场+成长
}

for consultant_id, tag_ids in existing_assignments.items():
    for tag_id in tag_ids:
        cursor.execute(
            "INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES (%s, %s)",
            (consultant_id, tag_id)
        )
    print(f"  Consultant ID {consultant_id}: assigned {len(tag_ids)} tags")
conn.commit()

# 5. Create 30 new users (consultants)
print("Step 5: Create 30 new users")

# Generate bcrypt password for "123456"
# Using a pre-computed bcrypt hash
password_hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO"

new_consultants_info = [
    ("13800010001", "李心怡", "高级心理咨询师", "情绪管理、焦虑抑郁", "擅长情绪调节与压力管理，帮助来访者重建内心平衡"),
    ("13800010002", "王晓明", "婚姻家庭咨询师", "婚姻修复、家庭关系", "专注于婚姻家庭领域，帮助夫妻重建信任与沟通"),
    ("13800010003", "陈静", "儿童心理专家", "儿童心理、亲子教育", "多年儿童心理咨询经验，擅长处理青少年情绪问题"),
    ("13800010004", "刘芳", "职场心理顾问", "职场压力、职业规划", "帮助企业员工缓解职场压力，提升工作幸福感"),
    ("13800010005", "赵强", "恋爱心理导师", "恋爱关系、情感修复", "专注于恋爱心理学，帮助来访者建立健康的亲密关系"),
    ("13800010006", "孙丽", "性心理咨询师", "性心理、性别认同", "提供专业的性心理咨询服务，尊重每一位来访者"),
    ("13800010007", "周杰", "人际关系专家", "社交恐惧、人际沟通", "帮助来访者克服社交障碍，建立良好的人际关系"),
    ("13800010008", "吴敏", "创伤治疗师", "心理创伤、PTSD", "擅长创伤后应激障碍的治疗，帮助来访者走出阴影"),
    ("13800010009", "郑华", "个人成长导师", "自我探索、自信培养", "引导来访者进行自我探索，发现内在潜能"),
    ("13800010010", "黄蓉", "青少年心理专家", "青春期问题、学业压力", "专注于青少年心理健康，帮助青少年健康成长"),
    ("13800010011", "林峰", "认知行为治疗师", "认知行为疗法、焦虑障碍", "运用认知行为疗法，帮助来访者改变负面思维模式"),
    ("13800010012", "徐倩", "精神分析师", "精神分析、人格障碍", "深耕精神分析领域，帮助来访者理解潜意识"),
    ("13800010013", "马超", "正念冥想导师", "正念冥想、情绪调节", "结合正念冥想技术，帮助来访者获得内心平静"),
    ("13800010014", "朱琳", "家庭治疗师", "家庭系统、亲子沟通", "运用家庭系统理论，帮助家庭重建和谐关系"),
    ("13800010015", "胡军", "危机干预专家", "自杀干预、危机处理", "24小时危机干预服务，守护每一位需要帮助的人"),
    ("13800010016", "郭雪", "艺术治疗师", "艺术治疗、表达性治疗", "通过艺术形式帮助来访者表达和疗愈内心"),
    ("13800010017", "何伟", "睡眠障碍专家", "失眠治疗、睡眠障碍", "专注于睡眠障碍的治疗，帮助来访者重获好睡眠"),
    ("13800010018", "高娜", "饮食心理专家", "进食障碍、情绪性进食", "帮助来访者建立健康的饮食关系和身心态"),
    ("13800010019", "罗刚", "成瘾治疗专家", "网络成瘾、物质成瘾", "帮助来访者摆脱各种成瘾行为，重获自由"),
    ("13800010020", "梁雨", "跨文化心理顾问", "跨文化适应、留学心理", "帮助跨文化背景下的来访者适应新环境"),
    ("13800010021", "宋阳", "男性心理专家", "男性成长、中年危机", "关注男性心理健康，帮助男性应对人生挑战"),
    ("13800010022", "唐薇", "女性心理导师", "女性成长、产后抑郁", "陪伴女性成长，帮助女性找到自我价值和力量"),
    ("13800010023", "韩冰", "老年心理专家", "老年心理、丧偶哀伤", "关注老年人心理健康，帮助老年人安享晚年"),
    ("13800010024", "冯磊", "团体治疗师", "团体治疗、社交技能", "通过团体治疗，帮助来访者在互动中成长"),
    ("13800010025", "董欣", "游戏治疗师", "儿童游戏治疗、沙盘治疗", "运用游戏和沙盘技术，帮助儿童表达内心世界"),
    ("13800010026", "曾辉", "催眠治疗师", "催眠治疗、潜意识探索", "运用催眠技术，帮助来访者深入潜意识进行疗愈"),
    ("13800010027", "彭静", "叙事治疗师", "叙事疗法、生命故事", "通过重写生命故事，帮助来访者找到新的可能"),
    ("13800010028", "潘峰", "存在主义治疗师", "存在主义、人生意义", "探讨人生意义和价值，帮助来访者找到生命方向"),
    ("13800010029", "袁梅", "积极心理学导师", "积极心理、幸福力", "运用积极心理学，帮助来访者培养幸福感和韧性"),
    ("13800010030", "蒋文", "神经心理专家", "ADHD、自闭症", "专注于神经发育障碍的评估和干预"),
]

new_user_ids = []
for phone, nickname, title, specialty, intro in new_consultants_info:
    # Check if user exists
    cursor.execute("SELECT id FROM users WHERE phone = %s", (phone,))
    existing = cursor.fetchone()
    if existing:
        user_id = existing[0]
        print(f"  User {nickname} already exists, ID: {user_id}")
    else:
        cursor.execute(
            """INSERT INTO users (phone, password, nickname, user_type, status, gmt_create, gmt_modified) 
               VALUES (%s, %s, %s, 'CONSULTANT', 'ACTIVE', NOW(), NOW())""",
            (phone, password_hash, nickname)
        )
        user_id = cursor.lastrowid
        print(f"  Created user {nickname}, ID: {user_id}")
    new_user_ids.append((user_id, nickname, title, specialty, intro))

conn.commit()

# 6. Create consultant profiles for new users
print("Step 6: Create consultant profiles")
new_consultant_ids = []
for user_id, nickname, title, specialty, intro in new_user_ids:
    cursor.execute(
        """INSERT INTO consultants (user_id, name, title, specialty, intro, is_available, identity_tier)
           VALUES (%s, %s, %s, %s, %s, TRUE, 'BRONZE')""",
        (user_id, nickname, title, specialty, intro)
    )
    consultant_id = cursor.lastrowid
    new_consultant_ids.append(consultant_id)
    print(f"  Created consultant {nickname}, ID: {consultant_id}")

conn.commit()

# 7. Assign tags to all 34 consultants (ensure all 200 tags are covered)
print("Step 7: Assign tags to all consultants")

# Get all tag IDs
tag_ids = [t[0] for t in all_tags]
print(f"Total tags to assign: {len(tag_ids)}")

# Get all consultant IDs (4 existing + 30 new)
cursor.execute("SELECT id FROM consultants ORDER BY id")
all_consultant_ids = [row[0] for row in cursor.fetchall()]
print(f"Total consultants: {len(all_consultant_ids)}")

# Strategy: Shuffle tags and distribute evenly
random.seed(42)
tags_shuffled = tag_ids.copy()
random.shuffle(tags_shuffled)

# Assign 6 tags per consultant, ensuring all 200 tags are covered
assignments = {cid: [] for cid in all_consultant_ids}

# First pass: ensure each tag is assigned at least once
consultant_idx = 0
for tag_id in tags_shuffled:
    cid = all_consultant_ids[consultant_idx % len(all_consultant_ids)]
    if tag_id not in assignments[cid]:
        assignments[cid].append(tag_id)
    consultant_idx += 1

# Second pass: fill up to 6 tags per consultant
extra_tags = tags_shuffled.copy()
random.shuffle(extra_tags)
extra_idx = 0
for cid in all_consultant_ids:
    while len(assignments[cid]) < 6:
        tag_id = extra_tags[extra_idx % len(extra_tags)]
        if tag_id not in assignments[cid]:
            assignments[cid].append(tag_id)
            extra_idx += 1
        else:
            extra_idx += 1
            # Prevent infinite loop
            if extra_idx > len(extra_tags) * 3:
                break

# Insert assignments
for cid, tags in assignments.items():
    for tag_id in tags:
        cursor.execute(
            "INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES (%s, %s)",
            (cid, tag_id)
        )
    print(f"  Consultant ID {cid}: assigned {len(tags)} tags")

conn.commit()

# 8. Verify coverage
print("Step 8: Verify tag coverage")
cursor.execute("SELECT DISTINCT tag_id FROM consultant_specialties")
assigned_tags = set(row[0] for row in cursor.fetchall())
print(f"Assigned tags: {len(assigned_tags)} / {len(tag_ids)}")

missing = set(tag_ids) - assigned_tags
if missing:
    print(f"Missing tags: {sorted(missing)}")
else:
    print("[OK] All 200 tags are assigned!")

# Show consultant details
print("\nConsultant details:")
cursor.execute("""
    SELECT c.id, c.name, COUNT(cs.tag_id) as tag_count
    FROM consultants c
    LEFT JOIN consultant_specialties cs ON c.id = cs.consultant_id
    GROUP BY c.id, c.name
    ORDER BY c.id
""")
for row in cursor.fetchall():
    print(f"  {row[1]}(ID:{row[0]}): {row[2]} tags")

# Show tag distribution
print("\nTag distribution by category:")
cursor.execute("""
    SELECT sc.name, COUNT(DISTINCT cs.tag_id) as tag_count
    FROM consultant_specialties cs
    JOIN specialty_tags st ON cs.tag_id = st.id
    JOIN specialty_categories sc ON st.category_id = sc.id
    GROUP BY sc.id, sc.name
    ORDER BY sc.id
""")
for row in cursor.fetchall():
    print(f"  {row[0]}: {row[1]} tags assigned")

print("\nDone!")

cursor.close()
conn.close()
