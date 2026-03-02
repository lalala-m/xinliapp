package com.jslh.controller;


import com.jslh.commons.tools.page.PageData;
import com.jslh.commons.tools.utils.Result;
import com.jslh.dto.AiModelDTO;
import com.jslh.service.AiModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI模型
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/ai/model")
@Tag(name = "AI模型")
@AllArgsConstructor
public class AiModelController {
    private final AiModelService aiModelService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('ai:model')")
    public Result<PageData<AiModelDTO>> page(@RequestParam Map<String, Object> params) {
        PageData<AiModelDTO> page = aiModelService.page(params);

        return new Result<PageData<AiModelDTO>>().ok(page);
    }

    @GetMapping("list")
    public Result<List<AiModelDTO>> list() {
        List<AiModelDTO> list = aiModelService.getList();

        return new Result<List<AiModelDTO>>().ok(list);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('ai:model')")
    public Result<AiModelDTO> get(@PathVariable("id") Long id) {
        AiModelDTO data = aiModelService.get(id);

        return new Result<AiModelDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @PreAuthorize("hasAuthority('ai:model')")
    public Result<String> save(@RequestBody AiModelDTO dto) {
        aiModelService.save(dto);

        return new Result<>();
    }

    @PutMapping
    @Operation(summary = "修改")
    @PreAuthorize("hasAuthority('ai:model')")
    public Result<String> update(@RequestBody @Valid AiModelDTO dto) {
        aiModelService.update(dto);

        return new Result<>();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('ai:model')")
    public Result<String> delete(@RequestBody List<Long> idList) {
        aiModelService.delete(idList);

        return new Result<>();
    }

}
