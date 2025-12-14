// 前端类型定义

export interface ParameterItem {
    id: number
    paramName: string
    description: string
    category: string
    defaultValue: string
    currentValue?: string
    paramType: 'INTEGER' | 'BOOLEAN' | 'STRING' | 'DECIMAL'
    isSelected: boolean
    isTestDefault: boolean
    minValue?: string
    maxValue?: string
    allowedValues?: string[] // 保持向后兼容
    candidateValues?: string[] // 候选取值列表
    valueRange?: string // 设置范围：Global, Both, Session
    constraints?: ParameterConstraint // 约束信息
    createTime?: string
    updateTime?: string
}

export interface ParameterConstraint {
    minValue?: string
    maxValue?: string
    candidateValues?: string[]
    valueRange?: string
    hasRangeConstraint: boolean
    hasCandidateConstraint: boolean
    rangeDescription?: string
    candidateValuesDisplay?: string
    hasAnyConstraint: boolean
}

export interface PagedResponse<T> {
    content: T[]
    totalElements: number
    totalPages: number
    currentPage: number
    pageSize: number
    hasNext: boolean
    hasPrevious: boolean
    first: boolean
    last: boolean
}

export interface DatabaseConfig {
    id: number
    name: string
    dbType: string
    connectionUrl: string
    username: string
    status: string
    createTime?: string
    updateTime?: string
}

export interface ConnectionTestResult {
    success: boolean
    message: string
    dbVersion?: string
    responseTime?: number
}

export interface ImportResult {
    success: boolean
    message: string
    totalCount: number
    importedCount: number
    updatedCount: number
    skippedCount: number
    errorMessages?: string[]
}

export interface UpdateParameterRequest {
    id: number
    defaultValue?: string
    isTestDefault?: boolean
    description?: string
    minValue?: string
    maxValue?: string
    candidateValues?: string[]
    valueRange?: string
}

export interface BatchUpdateResult {
    totalCount: number
    successCount: number
    failureCount: number
    errorMessages?: string[]
}

export interface ErrorResponse {
    code: string
    message: string
    timestamp: string
    path?: string
}

// 新增接口定义

export interface ValidationResult {
    valid: boolean
    message: string
    errors?: string[]
}



// 参数筛选和搜索相关接口
export interface ParameterSearchParams {
    page?: number
    size?: number
    search?: string
    category?: string
    valueRange?: string
}

export interface ParameterStats {
    totalCount: number
    categoryStats: Record<string, number>
    typeStats: Record<string, number>
    valueRangeStats: Record<string, number>
    constraintStats: {
        withCandidates: number
        withRanges: number
        withBoth: number
        withNone: number
    }
}