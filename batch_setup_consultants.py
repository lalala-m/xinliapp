#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Batch create consultants and assign tags
- Assign 6 tags to each of 4 existing consultants
- Create 30 new consultants, assign 6 tags each
- Ensure all 200 tags are covered
"""

import requests
import json
import random
import sys
import io

# Fix Windows console encoding
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

BASE_URL = "http://localhost:8080"
API_URL = f"{BASE_URL}/api"
ADMIN_URL = f"{BASE_URL}/admin"

# 1. Get all tags
print("=" * 60)
print("Step 1: Get all tags")
resp = requests.get(f"{API_URL}/specialty-categories", timeout=10)
resp.raise_for_status()
categories = resp.json()["data"]

all_tags = []
for cat in categories:
    for tag in cat["tags"]:
        all_tags.append({
            "id": tag["id"],
            "name": tag["name"],
            "category_code": cat["code"],
            "category_name": cat["name"]
        })

print(f"Total {len(all_tags)} tags")

# 2. Get existing consultants
print("=" * 60)
print("Step 2: Get existing consultants")
resp = requests.get(f"{API_URL}/consultants?page=1&limit=100", timeout=10)
resp.raise_for_status()
existing_consultants = resp.json()["data"]
print(f"Existing consultants: {len(existing_consultants)}")
for c in existing_consultants:
    print(f"  ID:{c['id']} Name:{c['name']} UserID:{c['userId']}")

# 3. Generate tag assignment plan
print("=" * 60)
print("Step 3: Generate tag assignment plan")

random.seed(42)
tags_shuffled = all_tags.copy()
random.shuffle(tags_shuffled)

total_consultants = len(existing_consultants) + 30
assignments = [[] for _ in range(total_consultants)]

# Ensure each tag is assigned at least once
for i, tag in enumerate(tags_shuffled):
    consultant_idx = i % total_consultants
    assignments[consultant_idx].append(tag["id"])

# Fill up to 6 tags per consultant
extra_tags = tags_shuffled.copy()
random.shuffle(extra_tags)
extra_idx = 0
for i in range(total_consultants):
    while len(assignments[i]) < 6:
        tag_id = extra_tags[extra_idx % len(extra_tags)]["id"]
        if tag_id not in assignments[i]:
            assignments[i].append(tag_id)
            extra_idx += 1
        else:
            extra_idx += 1
            if extra_idx > len(extra_tags) * 2:
                break

all_assigned = set()
for a in assignments:
    all_assigned.update(a)
print(f"Assigned tags: {len(all_assigned)} / {len(all_tags)}")
missing = [t['id'] for t in all_tags if t['id'] not in all_assigned]
print(f"Missing tags: {missing}")

# 4. Assign tags to existing consultants
print("=" * 60)
print("Step 4: Assign tags to existing consultants")
for i, c in enumerate(existing_consultants):
    tag_ids = assignments[i]
    print(f"  Consultant {c['name']}(ID:{c['id']}): tags {tag_ids}")
    
    resp = requests.put(
        f"{ADMIN_URL}/consultants/{c['id']}/tags",
        json={"tagIds": tag_ids},
        timeout=10
    )
    if resp.status_code == 200:
        print("    [OK] Success")
    else:
        print(f"    [FAIL] HTTP {resp.status_code}: {resp.text[:200]}")

# 5. Create 30 new consultants
print("=" * 60)
print("Step 5: Create 30 new consultants")

new_consultant_data = [
    {"name": "Li Xinyi", "title": "Senior Counselor", "phone": "13800010001", "specialty": "Emotion, Anxiety", "intro": "Expert in emotion regulation"},
    {"name": "Wang Xiaoming", "title": "Marriage Counselor", "phone": "13800010002", "specialty": "Marriage, Family", "intro": "Focus on marriage repair"},
    {"name": "Chen Jing", "title": "Child Psychologist", "phone": "13800010003", "specialty": "Children, Parenting", "intro": "Years of child counseling"},
    {"name": "Liu Fang", "title": "Career Advisor", "phone": "13800010004", "specialty": "Career, Stress", "intro": "Help with workplace issues"},
    {"name": "Zhao Qiang", "title": "Love Mentor", "phone": "13800010005", "specialty": "Love, Relationships", "intro": "Focus on love psychology"},
    {"name": "Sun Li", "title": "Sex Counselor", "phone": "13800010006", "specialty": "Sexual, Gender", "intro": "Professional sex counseling"},
    {"name": "Zhou Jie", "title": "Social Expert", "phone": "13800010007", "specialty": "Social, Communication", "intro": "Help with social anxiety"},
    {"name": "Wu Min", "title": "Trauma Therapist", "phone": "13800010008", "specialty": "Trauma, PTSD", "intro": "Expert in PTSD treatment"},
    {"name": "Zheng Hua", "title": "Growth Mentor", "phone": "13800010009", "specialty": "Growth, Confidence", "intro": "Guide self-exploration"},
    {"name": "Huang Rong", "title": "Teen Expert", "phone": "13800010010", "specialty": "Teen, Academic", "intro": "Focus on teen mental health"},
    {"name": "Lin Feng", "title": "CBT Therapist", "phone": "13800010011", "specialty": "CBT, Anxiety", "intro": "Cognitive behavioral therapy"},
    {"name": "Xu Qian", "title": "Psychoanalyst", "phone": "13800010012", "specialty": "Analysis, Personality", "intro": "Deep psychoanalysis"},
    {"name": "Ma Chao", "title": "Mindfulness Guide", "phone": "13800010013", "specialty": "Mindfulness, Emotion", "intro": "Mindfulness meditation"},
    {"name": "Zhu Lin", "title": "Family Therapist", "phone": "13800010014", "specialty": "Family, Parenting", "intro": "Family systems therapy"},
    {"name": "Hu Jun", "title": "Crisis Expert", "phone": "13800010015", "specialty": "Crisis, Suicide", "intro": "24/7 crisis intervention"},
    {"name": "Guo Xue", "title": "Art Therapist", "phone": "13800010016", "specialty": "Art, Expression", "intro": "Art-based healing"},
    {"name": "He Wei", "title": "Sleep Expert", "phone": "13800010017", "specialty": "Sleep, Insomnia", "intro": "Sleep disorder treatment"},
    {"name": "Gao Na", "title": "Eating Expert", "phone": "13800010018", "specialty": "Eating, Disorders", "intro": "Healthy eating psychology"},
    {"name": "Luo Gang", "title": "Addiction Expert", "phone": "13800010019", "specialty": "Addiction, Internet", "intro": "Overcome addictions"},
    {"name": "Liang Yu", "title": "Cross-Cultural", "phone": "13800010020", "specialty": "Culture, Study Abroad", "intro": "Cross-cultural adaptation"},
    {"name": "Song Yang", "title": "Male Psychologist", "phone": "13800010021", "specialty": "Men, Midlife", "intro": "Male mental health"},
    {"name": "Tang Wei", "title": "Female Mentor", "phone": "13800010022", "specialty": "Women, Postpartum", "intro": "Women growth support"},
    {"name": "Han Bing", "title": "Elder Expert", "phone": "13800010023", "specialty": "Elder, Grief", "intro": "Elder mental health"},
    {"name": "Feng Lei", "title": "Group Therapist", "phone": "13800010024", "specialty": "Group, Social", "intro": "Group therapy sessions"},
    {"name": "Dong Xin", "title": "Play Therapist", "phone": "13800010025", "specialty": "Play, Sandplay", "intro": "Play therapy for children"},
    {"name": "Zeng Hui", "title": "Hypnotherapist", "phone": "13800010026", "specialty": "Hypnosis, Subconscious", "intro": "Hypnotherapy healing"},
    {"name": "Peng Jing", "title": "Narrative Therapist", "phone": "13800010027", "specialty": "Narrative, Story", "intro": "Rewrite life stories"},
    {"name": "Pan Feng", "title": "Existentialist", "phone": "13800010028", "specialty": "Existential, Meaning", "intro": "Find life meaning"},
    {"name": "Yuan Mei", "title": "Positive Psych", "phone": "13800010029", "specialty": "Positive, Happiness", "intro": "Build resilience"},
    {"name": "Jiang Wen", "title": "Neuropsychologist", "phone": "13800010030", "specialty": "ADHD, Autism", "intro": "Neurodevelopment disorders"},
]

new_consultant_ids = []
for i, data in enumerate(new_consultant_data):
    assignment_idx = len(existing_consultants) + i
    tag_ids = assignments[assignment_idx]
    
    payload = {
        "name": data["name"],
        "title": data["title"],
        "phone": data["phone"],
        "specialty": data["specialty"],
        "intro": data["intro"],
        "identityTier": "BRONZE",
        "tagIds": tag_ids
    }
    
    print(f"  Creating: {data['name']} (tags: {tag_ids})")
    resp = requests.post(
        f"{ADMIN_URL}/consultants",
        json=payload,
        timeout=10
    )
    
    if resp.status_code == 200:
        result = resp.json()
        if result.get("code") == 200:
            new_id = result["data"]["id"]
            new_consultant_ids.append(new_id)
            print(f"    [OK] Created, ID:{new_id}")
        else:
            print(f"    [FAIL] {result.get('message', 'Unknown')}")
    else:
        print(f"    [FAIL] HTTP {resp.status_code}: {resp.text[:200]}")

# 6. Verify all tags are assigned
print("=" * 60)
print("Step 6: Verify tag coverage")

all_assigned_tags = set()
resp = requests.get(f"{API_URL}/consultants?page=1&limit=100", timeout=10)
all_consultants = resp.json()["data"]

for c in all_consultants:
    c_id = c["id"]
    resp = requests.get(f"{ADMIN_URL}/consultants/{c_id}/tags", timeout=10)
    if resp.status_code == 200:
        tags = resp.json().get("data", [])
        for t in tags:
            all_assigned_tags.add(t["id"])

print(f"Total consultants: {len(all_consultants)}")
print(f"Assigned tags: {len(all_assigned_tags)} / {len(all_tags)}")

missing = [t["id"] for t in all_tags if t["id"] not in all_assigned_tags]
if missing:
    print(f"Missing tags: {missing}")
else:
    print("[OK] All 200 tags are assigned!")

print("\nConsultant tag details:")
for c in all_consultants:
    c_id = c["id"]
    resp = requests.get(f"{ADMIN_URL}/consultants/{c_id}/tags", timeout=10)
    if resp.status_code == 200:
        tags = resp.json().get("data", [])
        tag_names = [t["name"] for t in tags]
        print(f"  {c['name']}(ID:{c_id}): {len(tags)} tags")

print("=" * 60)
print("Done!")
