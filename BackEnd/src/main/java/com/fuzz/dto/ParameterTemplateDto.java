package com.fuzz.dto;

import com.fuzz.entity.ParameterType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 参数模板数据传输对象
 * 包含完整的参数信息和约束条件
 */
public class ParameterTemplateDto {
    
    private Long id;
    private String paramName;
    private String description;
    private String category;
    private String defaultValue;
    private String currentValue; // 当前数据库中的实际值
    private ParameterType paramType;
    private Boolean isSelected; // 是否被选中进行测试
    private Boolean isTestDefault;
    private String minValue;
    private String maxValue;
    private List<String> allowedValues; // 保持向后兼容
    private List<String> candidateValues; // 候选取值列表
    private String valueRange; // 设置范围：Global, Both, Session
    private ParameterConstraintDto constraints; // 约束信息
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public ParameterTemplateDto() {
    }
    
    // Getter和Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getParamName() {
        return paramName;
    }
    
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }
    
    public ParameterType getParamType() {
        return paramType;
    }
    
    public void setParamType(ParameterType paramType) {
        this.paramType = paramType;
    }
    
    public Boolean getIsSelected() {
        return isSelected;
    }
    
    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    public Boolean getIsTestDefault() {
        return isTestDefault;
    }
    
    public void setIsTestDefault(Boolean isTestDefault) {
        this.isTestDefault = isTestDefault;
    }
    
    public String getMinValue() {
        return minValue;
    }
    
    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }
    
    public String getMaxValue() {
        return maxValue;
    }
    
    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }
    
    public List<String> getAllowedValues() {
        return allowedValues;
    }
    
    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
        // 同时更新candidateValues以保持一致性
        this.candidateValues = allowedValues;
    }
    
    public List<String> getCandidateValues() {
        return candidateValues;
    }
    
    public void setCandidateValues(List<String> candidateValues) {
        this.candidateValues = candidateValues;
        // 同时更新allowedValues以保持向后兼容
        this.allowedValues = candidateValues;
    }
    
    public String getValueRange() {
        return valueRange;
    }
    
    public void setValueRange(String valueRange) {
        this.valueRange = valueRange;
    }
    
    public ParameterConstraintDto getConstraints() {
        return constraints;
    }
    
    public void setConstraints(ParameterConstraintDto constraints) {
        this.constraints = constraints;
    }
    
    /**
     * 根据当前字段自动构建约束信息
     */
    public void buildConstraints() {
        this.constraints = new ParameterConstraintDto(minValue, maxValue, candidateValues, valueRange);
    }
    
    /**
     * 检查参数值是否有效
     * @param value 要检查的值
     * @return 如果值有效则返回true
     */
    public boolean isValidValue(String value) {
        if (constraints == null) {
            buildConstraints();
        }
        
        // 检查候选取值约束
        if (constraints.isHasCandidateConstraint()) {
            return candidateValues.contains(value);
        }
        
        // 检查范围约束
        if (constraints.isHasRangeConstraint()) {
            try {
                if (paramType == ParameterType.INTEGER) {
                    long longValue = Long.parseLong(value);
                    if (minValue != null && longValue < Long.parseLong(minValue)) {
                        return false;
                    }
                    if (maxValue != null && longValue > Long.parseLong(maxValue)) {
                        return false;
                    }
                } else if (paramType == ParameterType.DECIMAL) {
                    double doubleValue = Double.parseDouble(value);
                    if (minValue != null && doubleValue < Double.parseDouble(minValue)) {
                        return false;
                    }
                    if (maxValue != null && doubleValue > Double.parseDouble(maxValue)) {
                        return false;
                    }
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取约束描述信息
     * @return 约束描述字符串
     */
    public String getConstraintDescription() {
        if (constraints == null) {
            buildConstraints();
        }
        
        StringBuilder desc = new StringBuilder();
        
        if (constraints.isHasCandidateConstraint()) {
            desc.append("候选值: ").append(constraints.getCandidateValuesDisplay());
        }
        
        if (constraints.isHasRangeConstraint()) {
            if (desc.length() > 0) {
                desc.append("; ");
            }
            desc.append("范围: ").append(constraints.getRangeDescription());
        }
        
        if (valueRange != null && !valueRange.isEmpty()) {
            if (desc.length() > 0) {
                desc.append("; ");
            }
            desc.append("作用域: ").append(valueRange);
        }
        
        return desc.toString();
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}