<template>
  <div class="database-parameter-manager">
    <!-- 工具栏 -->
    <div class="mb-6 space-y-4">
      <!-- 数据库选择和导入 -->
      <div class="flex items-center space-x-4">
        <div class="flex-1">
          <label class="block text-sm font-medium mb-2">选择数据库配置</label>
          <select 
            v-model="selectedDbConfig" 
            class="w-full p-2 border border-gray-300 rounded focus:border-blue-500 focus:outline-none"
            @change="onDbConfigChange"
          >
            <option value="">请选择数据库配置</option>
            <option 
              v-for="config in dbConfigs" 
              :key="config.id" 
              :value="config.id"
            >
              {{ config.name }} ({{ config.dbType }})
            </option>
          </select>
        </div>
        
        <div class="flex space-x-2">
          <button
            @click="testConnection"
            :disabled="!selectedDbConfig || testing"
            class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ testing ? '测试中...' : '测试连接' }}
          </button>
          
          <button
            @click="importParameters"
            :disabled="!selectedDbConfig || importing"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ importing ? '导入中...' : '导入参数' }}
          </button>


        </div>
      </div>
      
      <!-- 搜索和过滤 -->
      <div class="space-y-4">
        <!-- 第一行：搜索框 -->
        <div class="flex items-center space-x-4">
          <div class="flex-1 relative">
            <div class="relative">
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索参数名、描述、候选值..."
                class="w-full p-2 pl-10 border border-gray-300 rounded focus:border-blue-500 focus:outline-none"
                @input="onSearchInput"
                @focus="showSuggestions = searchSuggestions.length > 0"
                @blur="hideSuggestions"
              />
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
              </div>
            </div>
            
            <!-- 搜索建议下拉框 -->
            <div 
              v-if="showSuggestions && searchSuggestions.length > 0"
              class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-y-auto"
            >
              <div
                v-for="(suggestion, index) in searchSuggestions"
                :key="index"
                @mousedown="selectSuggestion(suggestion)"
                class="px-3 py-2 cursor-pointer hover:bg-gray-100 text-sm"
              >
                <span class="font-medium">{{ suggestion }}</span>
              </div>
            </div>
          </div>
          
          <button
            @click="clearFilters"
            class="px-4 py-2 border border-gray-300 rounded hover:bg-gray-100"
          >
            清空筛选
          </button>
        </div>

        <!-- 快速筛选标签 -->
        <div class="flex items-center space-x-2 mb-3">
          <span class="text-sm font-medium text-gray-700">快速筛选:</span>
          <button
            v-for="filter in quickFilters"
            :key="filter.key"
            @click="applyQuickFilter(filter)"
            class="px-3 py-1 text-xs rounded-full border transition-colors"
            :class="isQuickFilterActive(filter) ? 
              'bg-blue-100 text-blue-800 border-blue-300' : 
              'bg-gray-100 text-gray-700 border-gray-300 hover:bg-gray-200'"
          >
            {{ filter.label }}
            <span v-if="filter.count !== undefined" class="ml-1 text-xs opacity-75">
              ({{ filter.count }})
            </span>
          </button>
        </div>

        <!-- 第二行：详细筛选选项 -->
        <div class="flex items-center space-x-4">
          <div class="w-48">
            <label class="block text-sm font-medium mb-1">参数类别</label>
            <select 
              v-model="selectedCategory" 
              class="w-full p-2 border border-gray-300 rounded focus:border-blue-500 focus:outline-none"
              @change="loadParameters"
            >
              <option value="">所有类别</option>
              <option 
                v-for="category in categories" 
                :key="category" 
                :value="category"
              >
                {{ category }} ({{ getCategoryCount(category) }})
              </option>
            </select>
          </div>

          <div class="w-48">
            <label class="block text-sm font-medium mb-1">设置范围</label>
            <select 
              v-model="selectedValueRange" 
              class="w-full p-2 border border-gray-300 rounded focus:border-blue-500 focus:outline-none"
              @change="loadParameters"
            >
              <option value="">所有范围</option>
              <option 
                v-for="range in valueRanges" 
                :key="range" 
                :value="range"
              >
                {{ range }} ({{ getValueRangeCount(range) }})
              </option>
            </select>
          </div>

          <div class="w-48">
            <label class="block text-sm font-medium mb-1">约束类型</label>
            <select 
              v-model="selectedConstraintType" 
              class="w-full p-2 border border-gray-300 rounded focus:border-blue-500 focus:outline-none"
              @change="loadParameters"
            >
              <option value="">所有类型</option>
              <option value="candidates">有候选值 ({{ getConstraintTypeCount('candidates') }})</option>
              <option value="range">有范围限制 ({{ getConstraintTypeCount('range') }})</option>
              <option value="both">有约束条件 ({{ getConstraintTypeCount('both') }})</option>
              <option value="none">无约束 ({{ getConstraintTypeCount('none') }})</option>
            </select>
          </div>

          <div class="w-32">
            <label class="block text-sm font-medium mb-1">测试状态</label>
            <select 
              v-model="selectedTestStatus" 
              class="w-full p-2 border border-gray-300 rounded focus:border-blue-500 focus:outline-none"
              @change="loadParameters"
            >
              <option value="">全部</option>
              <option value="true">启用测试 ({{ getTestStatusCount(true) }})</option>
              <option value="false">禁用测试 ({{ getTestStatusCount(false) }})</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据迁移管理面板 -->
    <div v-if="showMigrationPanel" class="mb-6 bg-white rounded-lg shadow-md border border-purple-200">
      <div class="p-4 bg-purple-50 border-b border-purple-200">
        <h3 class="text-lg font-semibold text-purple-800 flex items-center">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"></path>
          </svg>
          数据迁移管理
        </h3>
        <p class="text-sm text-purple-600 mt-1">
          将测试数据替换为真实的MySQL参数数据
        </p>
      </div>

      <div class="p-4 space-y-4">
        <!-- 迁移状态显示 -->
        <div class="bg-gray-50 rounded-lg p-4">
          <div class="flex items-center justify-between mb-3">
            <h4 class="font-medium text-gray-900">迁移状态</h4>
            <button
              @click="refreshMigrationStatus"
              class="text-sm text-blue-600 hover:text-blue-800"
              :disabled="refreshingStatus"
            >
              {{ refreshingStatus ? '刷新中...' : '刷新状态' }}
            </button>
          </div>

          <div v-if="migrationStatus" class="space-y-2">
            <div class="flex items-center space-x-2">
              <span class="text-sm font-medium">当前状态:</span>
              <span 
                class="px-2 py-1 text-xs rounded-full"
                :class="getMigrationStatusClass(migrationStatus.status)"
              >
                {{ getMigrationStatusText(migrationStatus.status) }}
              </span>
            </div>

            <div v-if="migrationStatus.lastMigrationTime" class="flex items-center space-x-2">
              <span class="text-sm font-medium">上次迁移:</span>
              <span class="text-sm text-gray-600">
                {{ formatDateTime(migrationStatus.lastMigrationTime) }}
              </span>
            </div>

            <div class="flex items-center space-x-2">
              <span class="text-sm font-medium">参数总数:</span>
              <span class="text-sm text-gray-600">{{ migrationStatus.totalParameters }}</span>
            </div>

            <div v-if="migrationStatus.categoryStats" class="mt-3">
              <span class="text-sm font-medium">类别分布:</span>
              <div class="flex flex-wrap gap-2 mt-1">
                <span 
                  v-for="(count, category) in migrationStatus.categoryStats" 
                  :key="category"
                  class="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded"
                >
                  {{ category }}: {{ count }}
                </span>
              </div>
            </div>
          </div>

          <div v-else class="text-sm text-gray-500">
            正在加载迁移状态...
          </div>
        </div>

        <!-- 迁移操作按钮 -->
        <div class="flex items-center space-x-3">
          <button
            @click="executeMigration"
            :disabled="migrating || migrationStatus?.status === 'IN_PROGRESS'"
            class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ migrating ? '迁移中...' : '执行迁移' }}
          </button>

          <button
            @click="validateMigration"
            :disabled="validating"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ validating ? '验证中...' : '验证迁移' }}
          </button>

          <button
            @click="rollbackMigration"
            :disabled="rollingBack || migrationStatus?.status === 'READY'"
            class="px-4 py-2 bg-yellow-600 text-white rounded hover:bg-yellow-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ rollingBack ? '回滚中...' : '回滚迁移' }}
          </button>

          <button
            @click="cleanupBackup"
            :disabled="cleaningUp"
            class="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ cleaningUp ? '清理中...' : '清理备份' }}
          </button>
        </div>

        <!-- 迁移结果显示 -->
        <div v-if="migrationResult" class="mt-4">
          <div 
            class="p-3 rounded-lg"
            :class="migrationResult.success ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'"
          >
            <div class="flex items-center">
              <svg 
                class="w-5 h-5 mr-2"
                :class="migrationResult.success ? 'text-green-600' : 'text-red-600'"
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path 
                  v-if="migrationResult.success"
                  stroke-linecap="round" 
                  stroke-linejoin="round" 
                  stroke-width="2" 
                  d="M5 13l4 4L19 7"
                ></path>
                <path 
                  v-else
                  stroke-linecap="round" 
                  stroke-linejoin="round" 
                  stroke-width="2" 
                  d="M6 18L18 6M6 6l12 12"
                ></path>
              </svg>
              <span 
                class="font-medium"
                :class="migrationResult.success ? 'text-green-800' : 'text-red-800'"
              >
                {{ migrationResult.message }}
              </span>
            </div>
          </div>
        </div>

        <!-- 验证结果显示 -->
        <div v-if="validationResult" class="mt-4">
          <div 
            class="p-3 rounded-lg"
            :class="validationResult.isValid ? 'bg-green-50 border border-green-200' : 'bg-yellow-50 border border-yellow-200'"
          >
            <div class="flex items-start">
              <svg 
                class="w-5 h-5 mr-2 mt-0.5"
                :class="validationResult.isValid ? 'text-green-600' : 'text-yellow-600'"
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path 
                  stroke-linecap="round" 
                  stroke-linejoin="round" 
                  stroke-width="2" 
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                ></path>
              </svg>
              <div class="flex-1">
                <span 
                  class="font-medium"
                  :class="validationResult.isValid ? 'text-green-800' : 'text-yellow-800'"
                >
                  {{ validationResult.isValid ? '验证通过' : '发现问题' }}
                </span>
                
                <div v-if="validationResult.issues && validationResult.issues.length > 0" class="mt-2">
                  <ul class="text-sm text-yellow-700 space-y-1">
                    <li v-for="issue in validationResult.issues" :key="issue" class="flex items-start">
                      <span class="mr-1">•</span>
                      <span>{{ issue }}</span>
                    </li>
                  </ul>
                </div>

                <div class="mt-2 text-sm text-gray-600">
                  <div>总参数数: {{ validationResult.totalParameters }}</div>
                  <div>有候选值: {{ validationResult.parametersWithCandidates }}</div>
                  <div>有范围约束: {{ validationResult.parametersWithRanges }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      <div class="mt-2 text-gray-600">加载中...</div>
    </div>

    <!-- 参数表格 -->
    <div v-else-if="parameters.length > 0" class="bg-white rounded-lg shadow-md overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full border-2 border-table-border">
          <thead>
            <tr class="bg-gray-100">
              <th class="py-3 px-4 border border-table-border text-left">
                <input 
                  type="checkbox" 
                  :checked="allSelected"
                  @change="toggleSelectAll"
                  class="mr-2"
                />
                参数名
              </th>
              <th class="py-3 px-4 border border-table-border text-left">描述</th>
              <th class="py-3 px-4 border border-table-border text-left">类别</th>
              <th class="py-3 px-4 border border-table-border text-left">设置范围</th>
              <th class="py-3 px-4 border border-table-border text-left">约束信息</th>
              <th class="py-3 px-4 border border-table-border text-left">默认值</th>
              <th class="py-3 px-4 border border-table-border text-center">是否测试</th>
              <th class="py-3 px-4 border border-table-border text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(param, index) in parameters" 
              :key="param.id"
              :class="index % 2 === 0 ? 'bg-white' : 'bg-gray-50'"
              @mouseenter="hoveredParam = param.id"
              @mouseleave="hoveredParam = null"
            >
              <!-- 参数名 -->
              <td class="py-3 px-4 border border-table-border">
                <div class="flex items-center">
                  <input 
                    type="checkbox" 
                    v-model="param.isSelected"
                    @change="onParameterSelectionChange(param)"
                    class="mr-2"
                  />
                  <div class="flex flex-col">
                    <span class="font-medium">{{ param.paramName }}</span>
                    <span class="text-xs text-gray-500">{{ param.paramType }}</span>
                  </div>
                </div>
              </td>
              
              <!-- 描述 -->
              <td class="py-3 px-4 border border-table-border">
                <div class="max-w-xs">
                  <span 
                    class="text-sm text-gray-600 cursor-help" 
                    :title="param.description"
                  >
                    {{ truncateText(param.description, 50) || '-' }}
                  </span>
                </div>
              </td>
              
              <!-- 类别 -->
              <td class="py-3 px-4 border border-table-border">
                <span class="inline-block px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded">
                  {{ param.category }}
                </span>
              </td>

              <!-- 设置范围 -->
              <td class="py-3 px-4 border border-table-border">
                <span 
                  v-if="param.valueRange"
                  class="inline-block px-2 py-1 text-xs rounded"
                  :class="getValueRangeClass(param.valueRange)"
                  :title="getValueRangeTooltip(param.valueRange)"
                >
                  {{ param.valueRange }}
                </span>
                <span v-else class="text-gray-400 text-xs">-</span>
              </td>

              <!-- 约束信息 -->
              <td class="py-3 px-4 border border-table-border">
                <div class="flex flex-wrap gap-1">
                  <!-- 候选值标识 -->
                  <span 
                    v-if="hasCandidateValues(param)"
                    class="inline-flex items-center px-2 py-1 text-xs bg-green-100 text-green-800 rounded cursor-help"
                    :title="getCandidateValuesPreview(param)"
                  >
                    <span class="mr-1">📋</span>
                    {{ param.candidateValues?.length || param.allowedValues?.length }} 个选项
                  </span>

                  <!-- 范围约束标识 -->
                  <span 
                    v-if="hasRangeConstraint(param)"
                    class="inline-flex items-center px-2 py-1 text-xs bg-orange-100 text-orange-800 rounded cursor-help"
                    :title="getRangeConstraintPreview(param)"
                  >
                    <span class="mr-1">📏</span>
                    范围限制
                  </span>

                  <!-- 无约束 -->
                  <span 
                    v-if="!hasCandidateValues(param) && !hasRangeConstraint(param)"
                    class="text-gray-400 text-xs"
                  >
                    无约束
                  </span>
                </div>
              </td>
              
              <!-- 默认值编辑器 -->
              <td class="py-3 px-4 border border-table-border">
                <ParameterEditor
                  :parameter="param"
                  :always-editing="false"
                  :show-constraints="false"
                  @save="(value) => updateParameterValue(param, value)"
                />
              </td>
              
              <!-- 是否测试 -->
              <td class="py-3 px-4 border border-table-border text-center">
                <input 
                  type="checkbox" 
                  v-model="param.isTestDefault"
                  @change="updateParameterTestStatus(param)"
                  class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
              </td>
              
              <!-- 操作 -->
              <td class="py-3 px-4 border border-table-border text-center">
                <div class="flex items-center justify-center space-x-2">
                  <button
                    @click="showParameterDetails(param)"
                    class="text-blue-600 hover:text-blue-800 text-sm"
                    title="查看详情"
                  >
                    详情
                  </button>
                  <button
                    @click="deleteParameter(param)"
                    class="text-red-600 hover:text-red-800 text-sm"
                    title="删除参数"
                  >
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页组件 -->
      <div class="p-4 border-t">
        <Pagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :total-elements="totalElements"
          @page-change="onPageChange"
        />
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="text-center py-12">
      <div class="text-gray-500 text-lg">暂无参数数据</div>
      <div class="text-gray-400 text-sm mt-2">
        请选择数据库配置并导入参数
      </div>
    </div>

    <!-- 消息提示 -->
    <div 
      v-if="message.text" 
      :class="messageClass"
      class="fixed top-4 right-4 px-4 py-2 rounded shadow-lg z-50"
    >
      {{ message.text }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { parameterApi } from '../api/parameterApi'
import { databaseConfigApi } from '../api/databaseConfigApi'
import ParameterEditor from './ParameterEditor.vue'
import Pagination from './Pagination.vue'
import type { 
  ParameterItem, 
  DatabaseConfig, 
  ConnectionTestResult,
  ImportResult 
} from '../types'

// 响应式数据
const parameters = ref<ParameterItem[]>([])
const dbConfigs = ref<DatabaseConfig[]>([])
const categories = ref<string[]>([])
const valueRanges = ref<string[]>([])

const selectedDbConfig = ref<number | ''>('')
const searchKeyword = ref('')
const selectedCategory = ref('')
const selectedValueRange = ref('')
const selectedConstraintType = ref('')
const selectedTestStatus = ref('')
const hoveredParam = ref<number | null>(null)

// 快速筛选选项
const quickFilters = ref([
  { key: 'innodb', label: 'InnoDB参数', category: 'INNODB' },
  { key: 'memory', label: '内存相关', category: 'MEMORY' },
  { key: 'boolean', label: '布尔类型', paramType: 'BOOLEAN' },
  { key: 'with-candidates', label: '有候选值', constraintType: 'candidates' },
  { key: 'global', label: '全局设置', valueRange: 'Global' },
  { key: 'test-enabled', label: '启用测试', testStatus: true }
])

const activeQuickFilter = ref<string | null>(null)

// 数据迁移相关状态
const showMigrationPanel = ref(false)
const migrationStatus = ref<any>(null)
const migrationResult = ref<any>(null)
const validationResult = ref<any>(null)
const migrating = ref(false)
const validating = ref(false)
const rollingBack = ref(false)
const cleaningUp = ref(false)
const refreshingStatus = ref(false)

const currentPage = ref(0)
const pageSize = ref(20)
const totalPages = ref(0)
const totalElements = ref(0)

const loading = ref(false)
const testing = ref(false)
const importing = ref(false)

const message = ref({ text: '', type: 'info' as 'success' | 'error' | 'info' })

// 计算属性
const allSelected = computed(() => {
  return parameters.value.length > 0 && parameters.value.every(p => p.isSelected)
})

const messageClass = computed(() => {
  const baseClass = 'transition-all duration-300'
  switch (message.value.type) {
    case 'success':
      return `${baseClass} bg-green-100 text-green-800 border border-green-200`
    case 'error':
      return `${baseClass} bg-red-100 text-red-800 border border-red-200`
    default:
      return `${baseClass} bg-blue-100 text-blue-800 border border-blue-200`
  }
})

// 防抖搜索
let searchTimeout: number | null = null
const debouncedSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadParameters()
  }, 300)
}

// 高级搜索功能
const performAdvancedSearch = (params: ParameterItem[], searchTerm: string): ParameterItem[] => {
  if (!searchTerm.trim()) return params
  
  const term = searchTerm.toLowerCase().trim()
  
  return params.filter(param => {
    // 基本字段搜索
    const basicMatch = 
      param.paramName.toLowerCase().includes(term) ||
      (param.description && param.description.toLowerCase().includes(term)) ||
      param.category.toLowerCase().includes(term) ||
      (param.defaultValue && param.defaultValue.toLowerCase().includes(term))
    
    // 候选值搜索
    const candidateMatch = (param.candidateValues || param.allowedValues || [])
      .some(value => value.toLowerCase().includes(term))
    
    // 约束信息搜索
    const constraintMatch = 
      (param.minValue && param.minValue.includes(term)) ||
      (param.maxValue && param.maxValue.includes(term)) ||
      (param.valueRange && param.valueRange.toLowerCase().includes(term))
    
    return basicMatch || candidateMatch || constraintMatch
  })
}

// 搜索建议功能
const searchSuggestions = ref<string[]>([])
const showSuggestions = ref(false)

const generateSearchSuggestions = (term: string) => {
  if (!term.trim() || term.length < 2) {
    searchSuggestions.value = []
    showSuggestions.value = false
    return
  }
  
  const suggestions = new Set<string>()
  const lowerTerm = term.toLowerCase()
  
  parameters.value.forEach(param => {
    // 参数名建议
    if (param.paramName.toLowerCase().includes(lowerTerm)) {
      suggestions.add(param.paramName)
    }
    
    // 类别建议
    if (param.category.toLowerCase().includes(lowerTerm)) {
      suggestions.add(param.category)
    }
    
    // 候选值建议
    const candidates = param.candidateValues || param.allowedValues || []
    candidates.forEach(value => {
      if (value.toLowerCase().includes(lowerTerm)) {
        suggestions.add(value)
      }
    })
  })
  
  searchSuggestions.value = Array.from(suggestions).slice(0, 8)
  showSuggestions.value = suggestions.size > 0
}

const selectSuggestion = (suggestion: string) => {
  searchKeyword.value = suggestion
  showSuggestions.value = false
  debouncedSearch()
}

const onSearchInput = () => {
  generateSearchSuggestions(searchKeyword.value)
  debouncedSearch()
}

const hideSuggestions = () => {
  // 延迟隐藏，允许点击建议项
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

// 显示消息
const showMessage = (text: string, type: 'success' | 'error' | 'info' = 'info') => {
  message.value = { text, type }
  setTimeout(() => {
    message.value.text = ''
  }, 3000)
}

// 加载数据库配置
const loadDatabaseConfigs = async () => {
  try {
    dbConfigs.value = await databaseConfigApi.getDatabaseConfigs()
  } catch (error) {
    console.error('加载数据库配置失败:', error)
    showMessage('加载数据库配置失败', 'error')
  }
}

// 加载参数类别
const loadCategories = async () => {
  try {
    categories.value = await parameterApi.getCategories()
  } catch (error) {
    console.error('加载参数类别失败:', error)
  }
}

// 加载设置范围
const loadValueRanges = async () => {
  try {
    valueRanges.value = await parameterApi.getValueRanges()
  } catch (error) {
    console.error('加载设置范围失败:', error)
  }
}

// 加载参数列表
const loadParameters = async () => {
  loading.value = true
  try {
    const response = await parameterApi.getEnhancedParameters({
      page: currentPage.value,
      size: pageSize.value,
      search: searchKeyword.value || undefined,
      category: selectedCategory.value || undefined,
      valueRange: selectedValueRange.value || undefined
    })
    
    // 根据约束类型筛选
    let filteredContent = response.content
    if (selectedConstraintType.value) {
      filteredContent = filterByConstraintType(response.content, selectedConstraintType.value)
    }

    // 根据测试状态筛选
    if (selectedTestStatus.value) {
      const testStatus = selectedTestStatus.value === 'true'
      filteredContent = filteredContent.filter(param => param.isTestDefault === testStatus)
    }
    
    parameters.value = filteredContent
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
    
  } catch (error) {
    console.error('加载参数失败:', error)
    showMessage('加载参数失败', 'error')
  } finally {
    loading.value = false
  }
}

// 数据库配置变化
const onDbConfigChange = () => {
  // 可以在这里添加额外的逻辑
}

// 测试数据库连接
const testConnection = async () => {
  if (!selectedDbConfig.value) return
  
  testing.value = true
  try {
    const result: ConnectionTestResult = await databaseConfigApi.testConnection(selectedDbConfig.value as number)
    
    if (result.success) {
      showMessage(`连接成功！数据库版本: ${result.dbVersion}`, 'success')
    } else {
      showMessage(`连接失败: ${result.message}`, 'error')
    }
  } catch (error) {
    console.error('测试连接失败:', error)
    showMessage('测试连接失败', 'error')
  } finally {
    testing.value = false
  }
}

// 导入参数
const importParameters = async () => {
  if (!selectedDbConfig.value) return
  
  importing.value = true
  try {
    const result: ImportResult = await parameterApi.importFromDatabase(selectedDbConfig.value as number)
    
    if (result.success) {
      showMessage(
        `导入成功！总计: ${result.totalCount}, 新增: ${result.importedCount}, 更新: ${result.updatedCount}`,
        'success'
      )
      await loadParameters()
      await loadCategories()
    } else {
      showMessage(`导入失败: ${result.message}`, 'error')
    }
  } catch (error) {
    console.error('导入参数失败:', error)
    showMessage('导入参数失败', 'error')
  } finally {
    importing.value = false
  }
}

// 更新参数值
const updateParameterValue = async (param: ParameterItem, value: string) => {
  try {
    await parameterApi.updateParameter(param.id, {
      defaultValue: value
    })
    
    // 更新本地数据
    param.defaultValue = value
    showMessage('参数更新成功', 'success')
    
  } catch (error) {
    console.error('更新参数失败:', error)
    showMessage('更新参数失败', 'error')
  }
}

// 更新参数测试状态
const updateParameterTestStatus = async (param: ParameterItem) => {
  try {
    await parameterApi.updateParameter(param.id, {
      isTestDefault: param.isTestDefault
    })
    
    showMessage('测试状态更新成功', 'success')
    
  } catch (error) {
    console.error('更新测试状态失败:', error)
    showMessage('更新测试状态失败', 'error')
    // 回滚状态
    param.isTestDefault = !param.isTestDefault
  }
}

// 参数选择变化
const onParameterSelectionChange = (param: ParameterItem) => {
  // 可以在这里添加选择变化的逻辑
}

// 全选/取消全选
const toggleSelectAll = () => {
  const newState = !allSelected.value
  parameters.value.forEach(param => {
    param.isSelected = newState
  })
}

// 删除参数
const deleteParameter = async (param: ParameterItem) => {
  if (!confirm(`确定要删除参数 "${param.paramName}" 吗？`)) {
    return
  }
  
  try {
    await parameterApi.deleteParameter(param.id)
    showMessage('参数删除成功', 'success')
    await loadParameters()
  } catch (error) {
    console.error('删除参数失败:', error)
    showMessage('删除参数失败', 'error')
  }
}

// 清空筛选
const clearFilters = () => {
  searchKeyword.value = ''
  selectedCategory.value = ''
  selectedValueRange.value = ''
  selectedConstraintType.value = ''
  selectedTestStatus.value = ''
  activeQuickFilter.value = null
  showSuggestions.value = false
  searchSuggestions.value = []
  currentPage.value = 0
  loadParameters()
}

// 工具方法
const truncateText = (text: string | undefined, maxLength: number): string => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const hasCandidateValues = (param: ParameterItem): boolean => {
  const candidates = param.candidateValues || param.allowedValues || []
  return candidates.length > 0
}

const hasRangeConstraint = (param: ParameterItem): boolean => {
  return !!(param.minValue || param.maxValue)
}

const getCandidateValuesPreview = (param: ParameterItem): string => {
  const candidates = param.candidateValues || param.allowedValues || []
  if (candidates.length <= 3) {
    return `候选值: ${candidates.join(', ')}`
  }
  return `候选值 (${candidates.length} 个): ${candidates.slice(0, 3).join(', ')}...`
}

const getRangeConstraintPreview = (param: ParameterItem): string => {
  const { minValue, maxValue } = param
  if (minValue && maxValue) {
    return `范围: ${minValue} - ${maxValue}`
  } else if (minValue) {
    return `范围: ≥ ${minValue}`
  } else if (maxValue) {
    return `范围: ≤ ${maxValue}`
  }
  return '有范围限制'
}

const getValueRangeClass = (valueRange: string): string => {
  const classMap: Record<string, string> = {
    'Global': 'bg-purple-100 text-purple-800',
    'Session': 'bg-green-100 text-green-800',
    'Both': 'bg-yellow-100 text-yellow-800'
  }
  return classMap[valueRange] || 'bg-gray-100 text-gray-800'
}

const getValueRangeTooltip = (valueRange: string): string => {
  const tooltipMap: Record<string, string> = {
    'Global': '全局设置，影响整个MySQL服务器',
    'Session': '会话设置，仅影响当前连接',
    'Both': '可以设置为全局或会话级别'
  }
  return tooltipMap[valueRange] || valueRange
}

const filterByConstraintType = (params: ParameterItem[], constraintType: string): ParameterItem[] => {
  switch (constraintType) {
    case 'candidates':
      return params.filter(param => hasCandidateValues(param))
    case 'range':
      return params.filter(param => hasRangeConstraint(param))
    case 'both':
      return params.filter(param => hasCandidateValues(param) || hasRangeConstraint(param))
    case 'none':
      return params.filter(param => !hasCandidateValues(param) && !hasRangeConstraint(param))
    default:
      return params
  }
}

const showParameterDetails = (param: ParameterItem) => {
  // 这里可以打开一个详情弹窗或导航到详情页面
  console.log('显示参数详情:', param)
  // 临时实现：显示alert
  const details = [
    `参数名: ${param.paramName}`,
    `类型: ${param.paramType}`,
    `类别: ${param.category}`,
    `设置范围: ${param.valueRange || '未知'}`,
    `默认值: ${param.defaultValue || '未设置'}`,
    `描述: ${param.description || '无描述'}`
  ]
  
  if (hasCandidateValues(param)) {
    const candidates = param.candidateValues || param.allowedValues || []
    details.push(`候选值: ${candidates.join(', ')}`)
  }
  
  if (hasRangeConstraint(param)) {
    details.push(`范围约束: ${getRangeConstraintPreview(param)}`)
  }
  
  alert(details.join('\n'))
}

// 快速筛选相关方法
const applyQuickFilter = (filter: any) => {
  // 如果点击的是当前激活的筛选，则取消筛选
  if (activeQuickFilter.value === filter.key) {
    clearFilters()
    activeQuickFilter.value = null
    return
  }
  
  // 清空其他筛选条件
  searchKeyword.value = ''
  selectedCategory.value = ''
  selectedValueRange.value = ''
  selectedConstraintType.value = ''
  selectedTestStatus.value = ''
  
  // 应用快速筛选
  activeQuickFilter.value = filter.key
  
  if (filter.category) {
    selectedCategory.value = filter.category
  }
  if (filter.valueRange) {
    selectedValueRange.value = filter.valueRange
  }
  if (filter.constraintType) {
    selectedConstraintType.value = filter.constraintType
  }
  if (filter.testStatus !== undefined) {
    selectedTestStatus.value = filter.testStatus.toString()
  }
  if (filter.paramType) {
    // 对于参数类型筛选，我们需要在客户端进行筛选
    // 这里可以扩展后端API支持参数类型筛选
  }
  
  currentPage.value = 0
  loadParameters()
}

const isQuickFilterActive = (filter: any): boolean => {
  return activeQuickFilter.value === filter.key
}

// 统计方法
const getCategoryCount = (category: string): number => {
  return parameters.value.filter(p => p.category === category).length
}

const getValueRangeCount = (range: string): number => {
  return parameters.value.filter(p => p.valueRange === range).length
}

const getConstraintTypeCount = (type: string): number => {
  return filterByConstraintType(parameters.value, type).length
}

const getTestStatusCount = (status: boolean): number => {
  return parameters.value.filter(p => p.isTestDefault === status).length
}

// 数据迁移相关方法
const refreshMigrationStatus = async () => {
  refreshingStatus.value = true
  try {
    migrationStatus.value = await migrationApi.getMigrationStatus()
  } catch (error) {
    console.error('获取迁移状态失败:', error)
    showMessage('获取迁移状态失败', 'error')
  } finally {
    refreshingStatus.value = false
  }
}

const executeMigration = async () => {
  if (!confirm('确定要执行数据迁移吗？这将替换当前的所有参数数据。')) {
    return
  }

  migrating.value = true
  migrationResult.value = null
  
  try {
    const result = await migrationApi.executeMigration()
    migrationResult.value = result
    
    if (result.success) {
      showMessage('数据迁移成功完成', 'success')
      await Promise.all([
        loadParameters(),
        loadCategories(),
        loadValueRanges(),
        refreshMigrationStatus()
      ])
    } else {
      showMessage(`数据迁移失败: ${result.message}`, 'error')
    }
  } catch (error) {
    console.error('执行迁移失败:', error)
    showMessage('执行迁移失败', 'error')
    migrationResult.value = { success: false, message: '迁移过程中发生错误' }
  } finally {
    migrating.value = false
  }
}

const validateMigration = async () => {
  validating.value = true
  validationResult.value = null
  
  try {
    validationResult.value = await migrationApi.validateMigration()
    
    if (validationResult.value.isValid) {
      showMessage('迁移验证通过', 'success')
    } else {
      showMessage(`发现 ${validationResult.value.issues.length} 个问题`, 'info')
    }
  } catch (error) {
    console.error('验证迁移失败:', error)
    showMessage('验证迁移失败', 'error')
  } finally {
    validating.value = false
  }
}

const rollbackMigration = async () => {
  if (!confirm('确定要回滚数据迁移吗？这将恢复到迁移前的状态。')) {
    return
  }

  rollingBack.value = true
  migrationResult.value = null
  
  try {
    const result = await migrationApi.rollbackMigration()
    migrationResult.value = result
    
    if (result.success) {
      showMessage('数据迁移回滚成功', 'success')
      await Promise.all([
        loadParameters(),
        loadCategories(),
        loadValueRanges(),
        refreshMigrationStatus()
      ])
    } else {
      showMessage(`回滚失败: ${result.message}`, 'error')
    }
  } catch (error) {
    console.error('回滚迁移失败:', error)
    showMessage('回滚迁移失败', 'error')
    migrationResult.value = { success: false, message: '回滚过程中发生错误' }
  } finally {
    rollingBack.value = false
  }
}

const cleanupBackup = async () => {
  if (!confirm('确定要清理备份数据吗？清理后将无法回滚。')) {
    return
  }

  cleaningUp.value = true
  
  try {
    const result = await migrationApi.cleanupMigrationBackup()
    
    if (result.success) {
      showMessage('备份数据清理成功', 'success')
      await refreshMigrationStatus()
    } else {
      showMessage(`清理失败: ${result.message}`, 'error')
    }
  } catch (error) {
    console.error('清理备份失败:', error)
    showMessage('清理备份失败', 'error')
  } finally {
    cleaningUp.value = false
  }
}

// 工具方法
const getMigrationStatusClass = (status: string): string => {
  const classMap: Record<string, string> = {
    'READY': 'bg-gray-100 text-gray-800',
    'IN_PROGRESS': 'bg-blue-100 text-blue-800',
    'COMPLETED': 'bg-green-100 text-green-800',
    'FAILED': 'bg-red-100 text-red-800',
    'ROLLING_BACK': 'bg-yellow-100 text-yellow-800',
    'ROLLED_BACK': 'bg-orange-100 text-orange-800',
    'ROLLBACK_FAILED': 'bg-red-100 text-red-800'
  }
  return classMap[status] || 'bg-gray-100 text-gray-800'
}

const getMigrationStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    'READY': '准备就绪',
    'IN_PROGRESS': '迁移中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'ROLLING_BACK': '回滚中',
    'ROLLED_BACK': '已回滚',
    'ROLLBACK_FAILED': '回滚失败'
  }
  return textMap[status] || status
}

const formatDateTime = (dateTimeStr: string): string => {
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleString('zh-CN')
  } catch (error) {
    return dateTimeStr
  }
}

// 页面变化
const onPageChange = (page: number) => {
  currentPage.value = page
  loadParameters()
}

// 组件挂载
onMounted(async () => {
  await Promise.all([
    loadDatabaseConfigs(),
    loadCategories(),
    loadValueRanges(),
    loadParameters(),
    refreshMigrationStatus()
  ])
})
</script>