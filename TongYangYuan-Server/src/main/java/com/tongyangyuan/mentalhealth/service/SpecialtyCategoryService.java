package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.SpecialtyCategory;
import com.tongyangyuan.mentalhealth.entity.SpecialtyTag;
import com.tongyangyuan.mentalhealth.repository.SpecialtyCategoryRepository;
import com.tongyangyuan.mentalhealth.repository.SpecialtyTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialtyCategoryService {

    private final SpecialtyCategoryRepository categoryRepository;
    private final SpecialtyTagRepository tagRepository;

    public SpecialtyCategoryService(SpecialtyCategoryRepository categoryRepository,
                                    SpecialtyTagRepository tagRepository) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * 获取所有分类（含标签）
     */
    public List<SpecialtyCategory> getAllCategoriesWithTags() {
        return categoryRepository.findAllWithTags();
    }

    /**
     * 获取所有分类（不含标签）
     */
    public List<SpecialtyCategory> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * 根据编码获取分类
     */
    public SpecialtyCategory getCategoryByCode(String code) {
        return categoryRepository.findByCode(code).orElse(null);
    }

    /**
     * 获取某分类下的所有标签
     */
    public List<SpecialtyTag> getTagsByCategoryId(Long categoryId) {
        return tagRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
    }

    /**
     * 获取某分类下的所有标签（通过编码）
     */
    public List<SpecialtyTag> getTagsByCategoryCode(String code) {
        SpecialtyCategory category = getCategoryByCode(code);
        if (category == null) return List.of();
        return tagRepository.findByCategoryIdOrderBySortOrderAsc(category.getId());
    }
}
