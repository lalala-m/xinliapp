#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Batch create consultants and assign tags - Part 2
(Users already created, need consultant profiles and tag assignments)
"""

import pymysql
import random

conn = pymysql.connect(
    host='localhost', port=3306, user='root', password='123456',
    database='mental_health_db', charset='utf8mb4'
)
cursor = conn.cursor()

# 1. Clear existing tag assignments
print("Step 1: Clear existing tag assignments")
cursor.execute("DELETE FROM consultant_specialties")
conn.commit()

# 2. Get all tags
print("Step 2: Get all tags")
cursor.execute("SELECT id FROM specialty_tags ORDER BY id")
all_tag_ids = [row[0] for row in cursor.fetchall()]
print(f"Total tags: {len(all_tag_ids)}")

# 3. Check existing users with CONSULTANT type but no consultant profile
print("Step 3: Check users and consultants")
cursor.execute("SELECT id, nickname FROM users WHERE user_type = 'CONSULTANT' ORDER BY id")
all_consultant_users = cursor.fetchall()
print(f"Consultant users: {len(all_consultant_users)}")

# Get existing consultant profiles
cursor.execute("SELECT id, user_id, name FROM consultants ORDER BY id")
existing_profiles = {row[1]: row for row in cursor.fetchall()}
print(f"Existing consultant profiles: {len(existing_profiles)}")

# 4. Create consultant profiles for users without one
print("Step 4: Create missing consultant profiles")

consultant_info = {
    "李心怡": ("高级心理咨询师", "情绪管理、焦虑抑郁", "擅长情绪调节与压力管理"),
    "王晓明": ("婚姻家庭咨询师", "婚姻修复、家庭关系", "专注于婚姻家庭领域"),
    "陈静": ("儿童心理专家", "儿童心理、亲子教育", "多年儿童心理咨询经验"),
    "刘芳": ("职场心理顾问", "职场压力、职业规划", "帮助企业员工缓解职场压力"),
    "赵强": ("恋爱心理导师", "恋爱关系、情感修复", "专注于恋爱心理学"),
    "孙丽": ("性心理咨询师", "性心理、性别认同", "提供专业的性心理咨询服务"),
    "周杰": ("人际关系专家", "社交恐惧、人际沟通", "帮助来访者克服社交障碍"),
    "吴敏": ("创伤治疗师", "心理创伤、PTSD", "擅长创伤后应激障碍的治疗"),
    "郑华": ("个人成长导师", "自我探索、自信培养", "引导来访者进行自我探索"),
    "黄蓉": ("青少年心理专家", "青春期问题、学业压力", "专注于青少年心理健康"),
    "林峰": ("认知行为治疗师", "认知行为疗法、焦虑障碍", "运用认知行为疗法"),
    "徐倩": ("精神分析师", "精神分析、人格障碍", "深耕精神分析领域"),
    "马超": ("正念冥想导师", "正念冥想、情绪调节", "结合正念冥想技术"),
    "朱琳": ("家庭治疗师", "家庭系统、亲子沟通", "运用家庭系统理论"),
    "胡军": ("危机干预专家", "自杀干预、危机处理", "24小时危机干预服务"),
    "郭雪": ("艺术治疗师", "艺术治疗、表达性治疗", "通过艺术形式帮助来访者"),
    "何伟": ("睡眠障碍专家", "失眠治疗、睡眠障碍", "专注于睡眠障碍的治疗"),
    "高娜": ("饮食心理专家", "进食障碍、情绪性进食", "帮助来访者建立健康饮食关系"),
    "罗刚": ("成瘾治疗专家", "网络成瘾、物质成瘾", "帮助来访者摆脱成瘾行为"),
    "梁雨": ("跨文化心理顾问", "跨文化适应、留学心理", "帮助跨文化背景来访者"),
    "宋阳": ("男性心理专家", "男性成长、中年危机", "关注男性心理健康"),
    "唐薇": ("女性心理导师", "女性成长、产后抑郁", "陪伴女性成长"),
    "韩冰": ("老年心理专家", "老年心理、丧偶哀伤", "关注老年人心理健康"),
    "冯磊": ("团体治疗师", "团体治疗、社交技能", "通过团体治疗帮助来访者"),
    "董欣": ("游戏治疗师", "儿童游戏治疗、沙盘治疗", "运用游戏和沙盘技术"),
    "曾辉": ("催眠治疗师", "催眠治疗、潜意识探索", "运用催眠技术"),
    "彭静": ("叙事治疗师", "叙事疗法、生命故事", "通过重写生命故事"),
    "潘峰": ("存在主义治疗师", "存在主义、人生意义", "探讨人生意义和价值"),
    "袁梅": ("积极心理学导师", "积极心理、幸福力", "运用积极心理学"),
    "蒋文": ("神经心理专家", "ADHD、自闭症", "专注于神经发育障碍"),
}

new_consultant_ids = []
for user_id, nickname in all_consultant_users:
    if user_id in existing_profiles:
        consultant_id = existing_profiles[user_id][0]
        print(f"  Profile exists: {nickname} (ID:{consultant_id})")
    else:
        info = consultant_info.get(nickname, ("心理咨询师", "综合心理", "专业心理咨询师"))
        cursor.execute(
            """INSERT INTO consultants (user_id, name, title, specialty, intro, is_available, identity_tier)
               VALUES (%s, %s, %s, %s, %s, TRUE, 'BRONZE')""",
            (user_id, nickname, info[0], info[1], info[2])
        )
        consultant_id = cursor.lastrowid
        print(f"  Created profile: {nickname} (ID:{consultant_id})")
    new_consultant_ids.append(consultant_id)

conn.commit()

# 5. Assign tags to all consultants
print("Step 5: Assign tags to all consultants")

# Get all consultant IDs
cursor.execute("SELECT id FROM consultants ORDER BY id")
all_consultant_ids = [row[0] for row in cursor.fetchall()]
print(f"Total consultants: {len(all_consultant_ids)}")

# Strategy: Distribute 200 tags across 34 consultants (6 tags each)
random.seed(42)
tags_shuffled = all_tag_ids.copy()
random.shuffle(tags_shuffled)

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

# 6. Verify coverage
print("Step 6: Verify tag coverage")
cursor.execute("SELECT DISTINCT tag_id FROM consultant_specialties")
assigned_tags = set(row[0] for row in cursor.fetchall())
print(f"Assigned tags: {len(assigned_tags)} / {len(all_tag_ids)}")

missing = set(all_tag_ids) - assigned_tags
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

# Show tag distribution by category
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
