package com.fuzz.service.impl;

import com.fuzz.dto.*;
import com.fuzz.entity.DatabaseConfig;
import com.fuzz.entity.ParameterTemplate;
import com.fuzz.repository.DatabaseConfigRepository;
import com.fuzz.repository.ParameterTemplateRepository;
import com.fuzz.service.DatabaseConnectionService;
import com.fuzz.service.ParameterConstraintValidator;
import com.fuzz.service.ParameterConstraintValidator.ValidationResult;
import com.fuzz.service.ParameterReaderService;
import com.fuzz.service.ParameterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 参数管理服务实现类
 */
@Service
@Transactional
public class ParameterServiceImpl implements ParameterService {
    
    private static final Logger logger = LoggerFactory.getLogger(ParameterServiceImpl.class);
    
    @Autowired
    private ParameterTemplateRepository parameterTemplateRepository;
    
    @Autowired
    private DatabaseConfigRepository databaseConfigRepository;
    
    @Autowired
    private DatabaseConnectionService databaseConnectionService;
    
    @Autowired
    private ParameterReaderService parameterReaderService;
    
    @Autowired
    private ParameterConstraintValidator parameterConstraintValidator;
    
    @Autowired
    private ParameterTemplateMapper parameterTemplateMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ParameterTemplateDto> getParameters(int page, int size, String search, String category) {
        Specification<ParameterTemplate> spec = buildSpecification(search, category);
        Pageable pageable = PageRequest.of(page, size, Sort.by("paramName"));
        
        Page<ParameterTemplate> parameterPage = parameterTemplateRepository.findAll(spec, pageable);
        
        List<ParameterTemplateDto> dtoList = parameterPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
            dtoList,
            parameterPage.getTotalElements(),
            page,
            size
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public ParameterTemplateDto getParameterById(Long id) {
        ParameterTemplate parameter = parameterTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("参数不存在: " + id));
        return convertToDto(parameter);
    }
    
    @Override
    public ParameterTemplateDto updateParameter(Long id, UpdateParameterRequest request) {
        ParameterTemplate parameter = parameterTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("参数不存在: " + id));
        
        // 验证参数值
        if (request.getDefaultValue() != null) {
            boolean isValid = parameterReaderService.validateParameterValue(
                parameter.getParamName(), 
                request.getDefaultValue(), 
                parameter.getParamType()
            );
            
            if (!isValid) {
                throw new RuntimeException("参数值无效: " + request.getDefaultValue());
            }
            
            parameter.setDefaultValue(request.getDefaultValue());
        }
        
        if (request.getIsTestDefault() != null) {
            parameter.setIsTestDefault(request.getIsTestDefault());
        }
        
        if (request.getDescription() != null) {
            parameter.setDescription(request.getDescription());
        }
        
        ParameterTemplate saved = parameterTemplateRepository.save(parameter);
        logger.info("参数更新成功: {}", saved.getParamName());
        
        return convertToDto(saved);
    }
    
    @Override
    public BatchUpdateResult batchUpdateParameters(List<UpdateParameterRequest> requests) {
        int totalCount = requests.size();
        int successCount = 0;
        int failureCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        for (UpdateParameterRequest request : requests) {
            try {
                updateParameter(request.getId(), request);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                errorMessages.add("参数ID " + request.getId() + ": " + e.getMessage());
                logger.error("批量更新参数失败: {}", request.getId(), e);
            }
        }
        
        BatchUpdateResult result = new BatchUpdateResult(totalCount, successCount, failureCount);
        result.setErrorMessages(errorMessages);
        
        logger.info("批量更新完成: 总数={}, 成功={}, 失败={}", totalCount, successCount, failureCount);
        return result;
    }
    
    @Override
    public ImportResult importParametersFromDatabase(Long dbConfigId) {
        DatabaseConfig dbConfig = databaseConfigRepository.findById(dbConfigId)
                .orElseThrow(() -> new RuntimeException("数据库配置不存在: " + dbConfigId));
        
        try {
            // 测试数据库连接
            ConnectionTestResult testResult = databaseConnectionService.testConnection(dbConfig);
            if (!testResult.isSuccess()) {
                return ImportResult.failure("数据库连接失败: " + testResult.getMessage());
            }
            
            // 读取数据库参数
            List<DatabaseParameter> dbParameters = parameterReaderService.readAllParameters(dbConfig);
            
            int totalCount = dbParameters.size();
            int importedCount = 0;
            int updatedCount = 0;
            int skippedCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            for (DatabaseParameter dbParam : dbParameters) {
                try {
                    Optional<ParameterTemplate> existingOpt = 
                        parameterTemplateRepository.findByParamName(dbParam.getName());
                    
                    if (existingOpt.isPresent()) {
                        // 更新现有参数
                        ParameterTemplate existing = existingOpt.get();
                        existing.setDefaultValue(dbParam.getValue());
                        existing.setDescription(dbParam.getDescription());
                        existing.setParamType(dbParam.getType());
                        existing.setCategory(dbParam.getCategory());
                        
                        // 更新推荐配置
                        updateParameterRecommendations(existing, dbParam);
                        
                        parameterTemplateRepository.save(existing);
                        updatedCount++;
                    } else {
                        // 创建新参数
                        ParameterTemplate newParam = new ParameterTemplate();
                        newParam.setParamName(dbParam.getName());
                        newParam.setDefaultValue(dbParam.getValue());
                        newParam.setDescription(dbParam.getDescription());
                        newParam.setParamType(dbParam.getType());
                        newParam.setCategory(dbParam.getCategory());
                        newParam.setIsTestDefault(true);
                        
                        // 设置推荐配置
                        updateParameterRecommendations(newParam, dbParam);
                        
                        parameterTemplateRepository.save(newParam);
                        importedCount++;
                    }
                } catch (Exception e) {
                    skippedCount++;
                    errorMessages.add("参数 " + dbParam.getName() + ": " + e.getMessage());
                    logger.error("导入参数失败: {}", dbParam.getName(), e);
                }
            }
            
            // 更新数据库配置状态
            dbConfig.setStatus("导入成功");
            databaseConfigRepository.save(dbConfig);
            
            ImportResult result = ImportResult.success(totalCount, importedCount, updatedCount);
            result.setSkippedCount(skippedCount);
            result.setErrorMessages(errorMessages);
            
            logger.info("参数导入完成: 总数={}, 新增={}, 更新={}, 跳过={}", 
                       totalCount, importedCount, updatedCount, skippedCount);
            
            return result;
            
        } catch (Exception e) {
            logger.error("导入参数失败", e);
            dbConfig.setStatus("导入失败");
            databaseConfigRepository.save(dbConfig);
            return ImportResult.failure("导入失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getDatabaseParameterValues(DatabaseConfig dbConfig) {
        Map<String, DatabaseParameter> parameters = 
            databaseConnectionService.readDatabaseParameters(dbConfig);
        
        return parameters.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getValue()
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable("categories")
    public List<String> getAllCategories() {
        return parameterTemplateRepository.findAllCategories();
    }
    
    @Override
    public void deleteParameter(Long id) {
        if (!parameterTemplateRepository.existsById(id)) {
            throw new RuntimeException("参数不存在: " + id);
        }
        
        parameterTemplateRepository.deleteById(id);
        logger.info("参数删除成功: {}", id);
    }
    
    @Override
    public void batchDeleteParameters(List<Long> ids) {
        parameterTemplateRepository.deleteByIdIn(ids);
        logger.info("批量删除参数成功: {} 个", ids.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ValidationResult validateParameterConstraints(String paramName, String value) {
        Optional<ParameterTemplate> parameterOpt = parameterTemplateRepository.findByParamName(paramName);
        if (!parameterOpt.isPresent()) {
            return ValidationResult.error("参数不存在: " + paramName);
        }
        
        return parameterConstraintValidator.validateParameterValue(parameterOpt.get(), value);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getParameterCandidateValues(Long parameterId) {
        ParameterTemplate parameter = parameterTemplateRepository.findById(parameterId)
                .orElseThrow(() -> new RuntimeException("参数不存在: " + parameterId));
        
        return parameter.getCandidateValues();
    }
    
    @Override
    @Transactional(readOnly = true)
    public ParameterConstraintDto getParameterConstraints(Long parameterId) {
        ParameterTemplate parameter = parameterTemplateRepository.findById(parameterId)
                .orElseThrow(() -> new RuntimeException("参数不存在: " + parameterId));
        
        return parameterConstraintValidator.getParameterConstraints(parameter);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ParameterTemplateDto getParameterByName(String paramName) {
        ParameterTemplate parameter = parameterTemplateRepository.findByParamName(paramName)
                .orElseThrow(() -> new RuntimeException("参数不存在: " + paramName));
        
        return parameterTemplateMapper.toDto(parameter);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAllValueRanges() {
        return parameterTemplateRepository.findAllValueRanges();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ParameterTemplateDto> getParametersByValueRange(int page, int size, String valueRange) {
        Specification<ParameterTemplate> spec = (root, query, cb) -> 
            cb.equal(root.get("valueRange"), valueRange);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("paramName"));
        Page<ParameterTemplate> parameterPage = parameterTemplateRepository.findAll(spec, pageable);
        
        List<ParameterTemplateDto> dtoList = parameterTemplateMapper.toDtoList(parameterPage.getContent());
        
        return new PagedResponse<>(
            dtoList,
            parameterPage.getTotalElements(),
            page,
            size
        );
    }
    
    /**
     * 构建查询条件
     */
    private Specification<ParameterTemplate> buildSpecification(String search, String category) {
        Specification<ParameterTemplate> spec = Specification.where(null);
        
        // 搜索条件
        if (StringUtils.hasText(search)) {
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("paramName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
                )
            );
        }
        
        // 分类过滤
        if (StringUtils.hasText(category)) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("category"), category)
            );
        }
        
        return spec;
    }
    
    /**
     * 转换为DTO（使用新的Mapper）
     */
    private ParameterTemplateDto convertToDto(ParameterTemplate entity) {
        ParameterTemplateDto dto = parameterTemplateMapper.toDto(entity);
        dto.setIsSelected(entity.getIsTestDefault()); // 默认选中状态等于测试默认状态
        return dto;
    }
    
    /**
     * 更新参数推荐配置
     */
    private void updateParameterRecommendations(ParameterTemplate parameter, DatabaseParameter dbParam) {
        Map<String, Object> recommendations = 
            parameterReaderService.getParameterRecommendations(dbParam.getName(), dbParam.getType());
        
        if (recommendations.containsKey("minValue")) {
            parameter.setMinValue(String.valueOf(recommendations.get("minValue")));
        }
        
        if (recommendations.containsKey("maxValue")) {
            parameter.setMaxValue(String.valueOf(recommendations.get("maxValue")));
        }
        
        if (recommendations.containsKey("allowedValues")) {
            try {
                @SuppressWarnings("unchecked")
                List<String> allowedValues = (List<String>) recommendations.get("allowedValues");
                parameter.setAllowedValues(objectMapper.writeValueAsString(allowedValues));
            } catch (Exception e) {
                logger.warn("序列化允许值失败: {}", parameter.getParamName(), e);
            }
        }
    }
}