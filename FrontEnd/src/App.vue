<template>
	<div class="flex flex-col h-screen">
		<header class="bg-header-sidebar text-white p-4 flex items-center justify-between">
			<h1 class="text-2xl font-bold">参数敏感数据库模糊测试平台</h1>
			<div class="text-right">
				<div>SQLancer</div>
				<div class="text-sm">版本 1.0</div>
			</div>
		</header>

		<div class="flex flex-1 overflow-hidden">
			<aside class="bg-header-sidebar w-64 flex-shrink-0">
				<nav class="py-4">
					<ul>
						<li>
							<button id="test-settings-btn" class="w-full text-left p-4" :class="activePanel==='settings' ? 'sidebar-active' : 'sidebar-inactive'" @click="switchPanel('settings')">
								<i class="fa fa-sliders mr-2"></i> 测试设置
							</button>
						</li>
						<li>
							<button id="status-info-btn" class="w-full text-left p-4" :class="activePanel==='status' ? 'sidebar-active' : 'sidebar-inactive'" @click="switchPanel('status')">
								<i class="fa fa-dashboard mr-2"></i> 状态信息
							</button>
						</li>
						<li>
							<button id="test-report-btn" class="w-full text-left p-4" :class="activePanel==='report' ? 'sidebar-active' : 'sidebar-inactive'" @click="switchPanel('report')">
								<i class="fa fa-file-text mr-2"></i> 测试报告
							</button>
						</li>
					</ul>
				</nav>
			</aside>

			<main class="flex-1 overflow-auto p-6">
				<!-- 测试设置页面 -->
				<div v-show="activePanel==='settings'" class="space-y-6">
					<div class="bg-white rounded-lg shadow-md overflow-hidden">
						<div class="p-6">
							<h2 class="text-xl font-bold mb-6">测试设置</h2>
							<div class="flex items-center justify-between border-b-2 border-gray-200 mb-6">
								<div>
									<button
										id="fuzz-params-tab"
										class="px-4 py-2 font-medium"
										:class="activeSubTab==='fuzz' ? 'text-blue-700 border-b-2 border-blue-700' : 'text-gray-500'"
										@click="activeSubTab='fuzz'"
									>
										模糊测试
									</button>
									<button
										id="db-params-tab"
										class="px-4 py-2 font-medium"
										:class="activeSubTab==='db' ? 'text-blue-700 border-b-2 border-blue-700' : 'text-gray-500'"
										@click="activeSubTab='db'"
									>
										数据库参数
									</button>
								</div>
							</div>

							<!-- 数据库参数子页面 -->
							<div v-show="activeSubTab==='db'">
								<DatabaseParameterManager />
							</div>

							<!-- 模糊测试参数子页面 -->
							<div v-show="activeSubTab==='fuzz'" class="space-y-6">
								<div>
									<!-- 模糊测试参数操作按钮 -->
									<div class="flex gap-2 mb-4 flex-wrap">
										<button
											class="py-2 px-4 bg-green-600 text-white rounded hover:bg-green-700 text-sm"
											@click="saveConfigToDatabase"
										>
											保存配置
										</button>
										<button
											class="py-2 px-4 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm"
											@click="showSchemeDialog = true"
										>
											保存方案
										</button>
										<button
											class="px-4 py-1 text-sm border border-gray-400 text-gray-800 rounded hover:bg-gray-100 transition-colors"
											@click="openSchemeListDialog"
										>
											查看方案
										</button>
										<button
											class="px-4 py-1 text-sm border border-red-400 text-red-600 rounded hover:bg-red-50 transition-colors"
											@click="resetFuzzParams"
										>
											重置为默认
										</button>
									</div>
									<h3 class="text-lg font-semibold mb-4">基础参数</h3>
									<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
										<div>
							<label class="block text-sm font-medium mb-2">测试Oracle</label>
							<select v-model="form.testOracle" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded">
								<option value="TLP">TLP (Test Language Platform)</option>
								<option value="NoREC">NoREC</option>
								<option value="PQS">PQS</option>
							</select>
						</div>
										<div>
											<label class="block text-sm font-medium mb-2">随机种子</label>
											<input type="number" v-model.number="form.randomSeed" min="-1" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">最大表达式深度</label>
											<input type="number" v-model.number="form.maxExpressionDepth" min="1" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">查询数量</label>
											<input type="number" v-model.number="form.numQueries" min="1" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">最大插入数量</label>
											<input type="number" v-model.number="form.maxNumInserts" min="1" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">尝试次数</label>
											<input type="number" v-model.number="form.numTries" min="1" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">超时时间(秒)</label>
											<input type="number" v-model.number="form.timeoutSeconds" min="-1" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">最大生成数据库数</label>
											<input type="number" v-model.number="form.maxGeneratedDatabases" min="1" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>

									</div>
								</div>



								<div class="border-t-2 border-gray-200 pt-4">
									<h3 class="text-lg font-semibold mb-4">数据库登录凭据</h3>
									<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
										<div>
											<label class="block text-sm font-medium mb-2">用户名</label>
											<input type="text" v-model="form.username" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">密码</label>
											<input type="password" v-model="form.password" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">主机</label>
											<input type="text" v-model="form.host" placeholder="localhost" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">端口</label>
											<input type="number" v-model.number="form.port" min="-1" @keydown.enter="saveAsDefaultConfig" class="w-full p-2 border border-gray-300 rounded" />
										</div>
									</div>
								</div>


							</div>
							<div class="flex justify-center p-6">
								<button id="start-test-btn" class="bg-blue-600 text-white font-bold py-2 px-6 rounded hover:bg-blue-700 transition-all" @click="startTest">开始测试</button>
							</div>
						</div>
					</div>
				</div>

				<!-- 新的状态信息页面（左右分栏布局） -->
				<div v-show="activePanel==='status'" class="h-full w-full">
					<!-- 整体左右分栏容器 -->
					<div class="flex h-full gap-6">
						<!-- 左侧：参数权重调整区（占比50%，可滚动） -->
						<div class="w-half bg-white rounded-lg shadow-md p-6 border-2 border-gray-300 overflow-hidden flex flex-col">
							<div class="flex justify-between items-center mb-4">
								<h2 class="text-lg font-bold">参数权重调整</h2>
								<!-- 新增刷新按钮 -->
								<button 
									class="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700 text-sm"
									@click="getParameterList"
								>
									<i class="fa fa-refresh mr-1"></i> 刷新参数
								</button>
							</div>
							<!-- 参数表格容器（可滚动）- 加固定宽度，防止容器变形 -->
<div class="flex-1 overflow-y-auto" style="width: 800px;">
  <!-- 表格强制固定布局 + 总宽度100% -->
  <table class="min-w-full border-2 border-gray-300" style="width: 100%; table-layout: fixed;">
    <thead>
      <tr class="bg-gray-100 font-medium">
        <!-- 每个表头强制固定像素宽度，所有页都用这个尺寸 -->
        <th class="py-2 px-4 border border-gray-300 text-left" style="width: 100px;">参数ID</th>
        <th class="py-2 px-4 border border-gray-300 text-left" style="width: 400px;">参数名称</th>
        <th class="py-2 px-4 border border-gray-300 text-left" style="width: 150px;">当前权重</th>
        <th class="py-2 px-4 border border-gray-300 text-left" style="width: 150px;">操作</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="param in currentPageParams" :key="param.id" :class="param.id%2===0 ? 'bg-gray-50': ''">
        <!-- 单元格也强制继承表头宽度，加text-ellipsis防止内容撑大 -->
        <td class="py-2 px-4 border border-gray-300" style="width: 100px; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">{{ param.id }}</td>
        <td class="py-2 px-4 border border-gray-300" style="width: 400px; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">{{ param.paramName }}</td>
        <td class="py-2 px-4 border border-gray-300" style="width: 150px;">
          <input 
            type="number" 
            v-model.number="param.inputWeight" 
            min="0" 
            max="10" 
            step="0.1"
            class="w-20 p-1 border border-gray-300 rounded"
            :disabled="param.isSaving"
          >
        </td>
        <td class="py-2 px-4 border border-gray-300" style="width: 150px;">
          <button 
            @click="updateWeight(param)"
            class="bg-green-500 text-white px-3 py-1 rounded hover:bg-green-600 transition-all"
            :disabled="param.isSaving"
          >
            {{ param.isSaving ? '保存中...' : '保存' }}
          </button>
        </td>
      </tr>
      <tr v-if="parameterList.length === 0">
        <td colspan="4" class="py-2 px-4 border border-gray-300 text-center text-gray-500">
          暂无参数数据，请先在数据库添加参数
        </td>
      </tr>
    </tbody>
  </table>

  <!-- 分页栏 -->
  <div class="mt-4 text-sm text-gray-600 text-center">
    <div class="flex justify-center items-center gap-2">
      <button 
        class="px-2 py-1 border border-gray-300 rounded hover:bg-gray-100"
        @click="currentPage = Math.max(1, currentPage - 1)"
        :disabled="currentPage === 1"
      >
        上一页
      </button>
      <span>第 {{ currentPage }} / {{ totalPages }} 页 共 {{ totalParams }} 个参数</span>
      <button 
        class="px-2 py-1 border border-gray-300 rounded hover:bg-gray-100"
        @click="currentPage = Math.min(totalPages, currentPage + 1)"
        :disabled="currentPage === totalPages || totalPages === 0"
      >
        下一页
      </button>
      <span>跳转到：</span>
      <input 
        type="number" 
        v-model.number="currentPage" 
        min="1" 
        :max="totalPages"
        class="w-16 p-1 border border-gray-300 rounded text-center"
        @change="handlePageChange"
      >
    </div>
  </div>
</div>
						</div>

						<!-- 右侧：数据看板区（占比50%） -->
						<div class="w-half flex flex-col gap-6">
							<!-- 数据库信息卡片 -->
							<div class="bg-white rounded-lg shadow-md p-6 border-2 border-gray-300">
								<h2 class="text-lg font-bold mb-4">数据库信息</h2>
								<div class="space-y-2">
									<div class="flex"><span class="font-semibold w-24">数据库：</span><span id="db-name">MySQL</span></div>
									<div class="flex"><span class="font-semibold w-24">版本：</span><span id="db-version">8.0.26</span></div>
								</div>
							</div>

							<!-- 测试状态信息卡片 -->
							<div class="bg-white rounded-lg shadow-md p-6 border-2 border-gray-300">
								<h2 class="text-lg font-bold mb-4">测试状态信息</h2>
								<div class="grid grid-two-cols gap-4">
									<div class="flex justify-between"><span class="font-semibold">测试Oracle：</span><span>{{ form.testOracle }}</span></div>
									<div class="flex justify-between"><span class="font-semibold">Bug数量：</span><span>15</span></div>

									<div class="flex justify-between"><span class="font-semibold">吞吐量：</span><span>120 qps</span></div>
									<div class="flex justify-between"><span class="font-semibold">测试时间：</span><span>2h 30m</span></div>
									<div class="flex justify-between"><span class="font-semibold">执行次数：</span><span>1,258,390</span></div>
								</div>
							</div>

							<!-- 覆盖率图表卡片（占满剩余空间） -->
							<div class="bg-white rounded-lg shadow-md p-6 border-2 border-gray-300 flex-1">
								<h2 class="text-lg font-bold mb-4">覆盖率变化曲线</h2>
								<!-- 增加固定高度 + 溢出隐藏 -->
								<div class="h-[300px] overflow-hidden">
									<canvas id="coverage-chart"></canvas>
								</div>
							</div>
						</div>
					</div>
				</div>

				<!-- 测试报告页面 -->
				<div v-show="activePanel==='report'">
					<div class="bg-white rounded-lg shadow-md p-6 border-2 border-gray-300">
						<h2 class="text-lg font-bold mb-4">Bug列表</h2>

						<!-- 加载状态 -->
						<div v-if="bugReportsLoading" class="text-center py-8">
							<div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
							<div class="mt-2 text-gray-600">加载中...</div>
						</div>

						<!-- Bug列表 -->
						<div v-else-if="bugReports.length > 0" class="overflow-x-auto">
							<table class="min-w-full border-2 border-gray-300">
								<thead>
									<tr class="bg-gray-100">
										<th class="py-2 px-4 border border-gray-300 text-left">Bug Id</th>
										<th class="py-2 px-4 border border-gray-300 text-left">类型</th>
										<th class="py-2 px-4 border border-gray-300 text-left">目标数据库</th>
										<th class="py-2 px-4 border border-gray-300 text-left">Oracle</th>
										<th class="py-2 px-4 border border-gray-300 text-left">测试时间</th>
										<th class="py-2 px-4 border border-gray-300 text-left">参数设置</th>
										<th class="py-2 px-4 border border-gray-300 text-left">测试样例</th>
									</tr>
								</thead>
								<tbody>
									<tr v-for="(bug, index) in bugReports" :key="bug.id" :class="index%2===0 ? 'bg-white' : 'bg-gray-50'">
										<td class="py-2 px-4 border border-gray-300">{{ bug.id }}</td>
										<td class="py-2 px-4 border border-gray-300">
											<span :class="bug.bugType === '崩溃' ? 'text-red-600 font-medium' : 'text-orange-600 font-medium'">
												{{ bug.bugType }}
											</span>
										</td>
										<td class="py-2 px-4 border border-gray-300">{{ bug.targetDatabase }}</td>
										<td class="py-2 px-4 border border-gray-300">{{ bug.oracleType }}</td>
										<td class="py-2 px-4 border border-gray-300 text-sm">
											{{ new Date(bug.testTime).toLocaleString('zh-CN') }}
										</td>
										<td class="py-2 px-4 border border-gray-300">
											<button class="show-params-btn bg-blue-500 text-white px-2 py-1 rounded hover:bg-blue-600 transition-all text-sm" @click="showBugParameterSettings(bug)">显示参数 <i class="fa fa-eye ml-1"></i></button>
										</td>
										<td class="py-2 px-4 border border-gray-300">
											<button class="show-example-btn bg-green-500 text-white px-2 py-1 rounded hover:bg-green-600 transition-all text-sm" @click="showBugTestCase(bug)">显示样例 <i class="fa fa-eye ml-1"></i></button>
										</td>
									</tr>
								</tbody>
							</table>

							<!-- 分页信息 -->
							<div class="mt-4 text-sm text-gray-600 text-center">
								共 {{ totalBugReports }} 个Bug，第 {{ bugReportsPage + 1 }} / {{ totalBugPages }} 页
							</div>
						</div>

						<!-- 空状态 -->
						<div v-else class="text-center py-12">
							<div class="text-gray-500 text-lg">暂无Bug报告数据</div>
							<div class="text-gray-400 text-sm mt-2">
								运行模糊测试后将显示发现的Bug信息
							</div>
						</div>
					</div>
				</div>
			</main>
		</div>

		<!-- 代码显示折叠面板 -->
		<div id="code-collapse-panel" class="fixed bottom-0 left-0 right-0 bg-white border-t-2 border-gray-300 rounded-t-lg shadow-lg" v-show="collapseVisible">
			<div class="flex justify-between items-center p-4 cursor-pointer" @click="collapseVisible = !collapseVisible">
				<h3 class="text-lg font-bold">{{ collapseTitle }}</h3>
				<button class="text-gray-500 hover:text-gray-700" @click.stop="collapseVisible=false">
					<i class="fa fa-times text-xl"></i>
				</button>
			</div>
			<div class="p-4 max-h-96 overflow-auto">
				<pre class="bg-gray-100 p-4 rounded whitespace-pre-wrap">{{ collapseContent }}</pre>
			</div>
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

	<!-- 保存方案对话框 -->
	<div v-if="showSchemeDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
		<div class="bg-white rounded-lg p-6 w-full max-w-md">
			<h3 class="text-lg font-semibold mb-4">保存参数方案</h3>
			<div class="space-y-4">
				<div>
					<label class="block text-sm font-medium mb-2">方案名称</label>
					<input 
						v-model="newSchemeName" 
						type="text" 
						class="w-full p-2 border border-gray-300 rounded"
						placeholder="请输入方案名称"
					>
				</div>
				<div>
					<label class="block text-sm font-medium mb-2">方案描述</label>
					<textarea 
						v-model="newSchemeDescription" 
						class="w-full p-2 border border-gray-300 rounded"
						placeholder="请输入方案描述（可选）"
						rows="3"
					></textarea>
				</div>
				<div class="flex justify-end gap-2">
					<button 
						class="px-4 py-2 border border-gray-300 text-gray-700 rounded hover:bg-gray-100"
						@click="showSchemeDialog = false"
					>
						取消
					</button>
					<button 
						class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
						@click="saveCurrentParametersAsScheme"
					>
						保存
					</button>
				</div>
			</div>
		</div>
	</div>
	
	<!-- 查看方案列表弹窗 -->
	<div v-if="showSchemeListDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
		<div class="bg-white rounded-lg shadow-xl w-full max-w-3xl p-6">
			<div class="flex items-center justify-between mb-4">
				<h3 class="text-lg font-semibold">已保存的参数方案</h3>
				<button
					@click="closeSchemeListDialog"
					class="text-gray-500 hover:text-gray-700 text-xl"
				>
					✕
				</button>
			</div>

			<div v-if="fuzzSchemes.length === 0" class="text-center text-gray-500 py-6">
				暂无已保存的参数方案，请先点击“保存方案”创建。
			</div>

			<div v-else class="max-h-96 overflow-y-auto border border-gray-200 rounded">
				<table class="min-w-full text-sm">
					<thead class="bg-gray-100">
						<tr>
							<th class="px-4 py-2 text-left border-b">方案名称</th>
							<th class="px-4 py-2 text-left border-b">描述</th>
							<th class="px-4 py-2 text-left border-b">创建时间</th>
							<th class="px-4 py-2 text-center border-b">操作</th>
						</tr>
					</thead>
					<tbody>
						<tr
							v-for="scheme in fuzzSchemes"
							:key="scheme.id"
							class="hover:bg-gray-50"
						>
							<td class="px-4 py-2 border-b font-medium">
								{{ scheme.name }}
							</td>
							<td class="px-4 py-2 border-b max-w-xs truncate" :title="scheme.description">
								{{ scheme.description || '（无描述）' }}
							</td>
							<td class="px-4 py-2 border-b">
								{{ formatDate(scheme.createdAt) }}
							</td>
							<td class="px-4 py-2 border-b text-center space-x-2">
								<button
									@click="viewSchemeDescription(scheme)"
									class="px-2 py-1 text-xs border border-gray-300 rounded hover:bg-gray-100"
								>
									查看描述
								</button>
								<button
									@click="applyFuzzScheme(scheme)"
									class="px-2 py-1 text-xs border border-green-500 text-green-600 rounded hover:bg-green-50"
								>
									应用方案
								</button>
								<button
									@click="deleteFuzzScheme(scheme.id)"
									class="px-2 py-1 text-xs border border-red-400 text-red-600 rounded hover:bg-red-50"
								>
									删除方案
								</button>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
	
	<!-- 参数方案详情弹窗 -->
	<div
		v-if="schemeDetail"
		class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40"
	>
		<div class="bg-white rounded-lg shadow-xl w-full max-w-lg p-6">
			<div class="flex items-center justify-between mb-4">
				<h3 class="text-lg font-semibold">方案详情</h3>
				<button
					@click="closeSchemeDetailDialog"
					class="text-gray-500 hover:text-gray-700"
				>
					✕
				</button>
			</div>

			<div class="space-y-3 text-sm text-gray-700">
				<div class="flex justify-between">
					<span class="font-medium text-gray-900">方案名称</span>
					<span>{{ schemeDetail?.name }}</span>
				</div>
				<div class="flex justify-between">
					<span class="font-medium text-gray-900">参数数量</span>
					<span>{{ Object.keys(schemeDetail?.parameters || {}).length }}</span>
				</div>
				<div class="flex justify-between">
					<span class="font-medium text-gray-900">创建时间</span>
					<span>{{ formatDate(schemeDetail?.createdAt || 0) }}</span>
				</div>
				<div>
					<span class="block font-medium text-gray-900 mb-1">方案描述</span>
					<div class="p-3 rounded border border-gray-200 bg-gray-50 text-gray-600 leading-relaxed whitespace-pre-wrap">
						{{ schemeDetail?.description || '（无描述）' }}
					</div>
				</div>
			</div>

			<div class="mt-6 flex justify-end">
				<button
					@click="closeSchemeDetailDialog"
					class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
				>
					我知道了
				</button>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import DatabaseParameterManager from './components/DatabaseParameterManager.vue'
import axios from 'axios'
import { bugReportApi } from './api/bugReportApi'
import { fuzzConfigApi } from './api/fuzzConfigApi'
import type { BugReportItem, PagedBugReports, FuzzTestConfig } from './types'

// 新增：定义参数类型（适配后端返回的字段，id/paramName/weight 必须和后端一致）
interface Parameter {
  id: number;         // 参数ID（后端是Long类型，前端用number兼容）
  paramName: string;  // 参数名称（后端字段名是paramName，前端对应一致）
  weight: number;     // 参数权重（后端是Double类型，前端用number兼容）
  inputWeight: number; // 输入框中的权重（用于临时存储用户输入）
  isSaving?: boolean; // 是否正在保存（用于显示加载状态）
}

// 模糊测试参数方案相关接口定义
interface FuzzParameterSchemeItem {
  id: string;
  name: string;
  description?: string;
  parameters: typeof defaultForm;
  createdAt: number;
}

interface FuzzParameterScheme {
  schemes: FuzzParameterSchemeItem[];
}

// 新增：存储参数列表（响应式数据，用ref包裹数组，类型指定为Parameter[]）
const parameterList = ref<Parameter[]>([])
// 👇 新增：分页核心变量（复制粘贴）
const currentPage = ref(1) // 当前页码（默认第1页）
const pageSize = ref(10)   // 每页显示10条（固定）
const totalParams = computed(() => parameterList.value.length) // 总参数数
const totalPages = computed(() => Math.ceil(totalParams.value / pageSize.value)) // 总页数

// 👇 新增：计算当前页要显示的参数（核心分页逻辑）
const currentPageParams = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return parameterList.value.slice(start, end)
})

// Bug报告相关状态
const bugReports = ref<BugReportItem[]>([])
const bugReportsLoading = ref(false)
const bugReportsPage = ref(0)
const bugReportsSize = ref(20)
const totalBugReports = ref(0)
const totalBugPages = ref(0)

// 模糊配置相关状态
const savingConfig = ref(false)
const loadingConfig = ref(false)

// 消息提示系统
const message = ref({ text: '', type: 'info' as 'success' | 'error' | 'info' })
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

// 修复后的代码：刷新保留页面状态
const activePanel = ref<'settings'|'status'|'report'>(
  // 优先读取本地存储的状态，没有就默认显示测试设置页
  (localStorage.getItem('activePanel') as 'settings'|'status'|'report') || 'settings'
)

// 监听 activePanel 变化，同步保存到本地存储
watch(activePanel, (newVal) => {
  localStorage.setItem('activePanel', newVal)
}, { immediate: true }) // immediate: true 确保初始状态也会保存

const activeSubTab = ref<'db'|'fuzz'>('fuzz')

// 👇 新增：页码输入后校验（防止输入超出范围）
const handlePageChange = () => {
  // 处理空值/非数字
  if (isNaN(currentPage.value) || currentPage.value < 1) {
    currentPage.value = 1
  }
  // 处理超出最大页数
  if (currentPage.value > totalPages.value && totalPages.value > 0) {
    currentPage.value = totalPages.value
  }
  // 无数据时重置为1
  if (totalPages.value === 0) {
    currentPage.value = 1
  }
}

// 模糊测试参数默认值
const defaultForm = {
	testOracle: 'TLP',
	randomSeed: -1,
	maxExpressionDepth: 3,
	numQueries: 10000,
	maxNumInserts: 30,
	numTries: 100,
	timeoutSeconds: 30,
	maxGeneratedDatabases: 1,
	username: 'sqlancer',
	password: 'sqlancer',
	host: '',
	port: -1,
}

// 当前模糊测试参数表单
const form = reactive({ ...defaultForm })

// 模糊测试参数方案相关
const FUZZ_SCHEME_STORAGE_KEY = 'fuzz-parameter-schemes'
const fuzzSchemes = ref<FuzzParameterSchemeItem[]>([])
const showSchemeDialog = ref(false) // 保存方案弹窗
const showSchemeListDialog = ref(false) // 查看方案列表弹窗
const schemeDetail = ref<FuzzParameterSchemeItem | null>(null) // 当前查看详情的方案
const newSchemeName = ref('')
const newSchemeDescription = ref('')
const selectedScheme = ref<FuzzParameterSchemeItem | null>(null)

// 加载方案列表
const loadFuzzSchemesFromStorage = () => {
  try {
    const stored = localStorage.getItem(FUZZ_SCHEME_STORAGE_KEY)
    if (stored) {
      const schemeData = JSON.parse(stored) as FuzzParameterScheme
      fuzzSchemes.value = schemeData.schemes || []
    }
  } catch (error) {
    console.error('加载模糊测试参数方案失败:', error)
    fuzzSchemes.value = []
  }
}

// 打开方案列表弹窗
const openSchemeListDialog = () => {
  loadFuzzSchemesFromStorage() // 打开前刷新方案列表
  showSchemeListDialog.value = true
}

// 关闭方案列表弹窗
const closeSchemeListDialog = () => {
  showSchemeListDialog.value = false
}

// 查看方案描述
const viewSchemeDescription = (scheme: FuzzParameterSchemeItem) => {
  schemeDetail.value = scheme
}

// 关闭方案详情弹窗
const closeSchemeDetailDialog = () => {
  schemeDetail.value = null
}

// 保存方案列表
const saveFuzzSchemesToStorage = () => {
  try {
    const schemeData: FuzzParameterScheme = { schemes: fuzzSchemes.value }
    localStorage.setItem(FUZZ_SCHEME_STORAGE_KEY, JSON.stringify(schemeData))
  } catch (error) {
    console.error('保存模糊测试参数方案失败:', error)
  }
}

// 保存当前参数为新方案
const saveCurrentParametersAsScheme = () => {
  if (!newSchemeName.value.trim()) {
    alert('请输入方案名称')
    return
  }

  const newScheme: FuzzParameterSchemeItem = {
    id: Date.now().toString(),
    name: newSchemeName.value.trim(),
    description: newSchemeDescription.value.trim(),
    parameters: JSON.parse(JSON.stringify(form)),
    createdAt: Date.now()
  }

  fuzzSchemes.value.push(newScheme)
  saveFuzzSchemesToStorage()

  // 重置对话框
  newSchemeName.value = ''
  newSchemeDescription.value = ''
  showSchemeDialog.value = false
  alert('参数方案已保存')
}

// 应用方案
const applyFuzzScheme = (scheme: FuzzParameterSchemeItem) => {
  if (confirm(`确定要应用方案 "${scheme.name}" 吗？当前参数将被覆盖。`)) {
    Object.assign(form, JSON.parse(JSON.stringify(scheme.parameters)))
    alert(`已应用方案 "${scheme.name}"`)
    // 保持弹窗打开，用户可继续操作其他方案
  }
}

// 删除方案
const deleteFuzzScheme = (schemeId: string) => {
  if (confirm('确定要删除该方案吗？此操作不可恢复。')) {
    const index = fuzzSchemes.value.findIndex(s => s.id === schemeId)
    if (index !== -1) {
      fuzzSchemes.value.splice(index, 1)
      saveFuzzSchemesToStorage()
      
      // 如果当前查看的是被删除的方案详情，则关闭详情弹窗
      if (schemeDetail.value?.id === schemeId) {
        schemeDetail.value = null
      }
      
      alert('方案已删除')
      // 保持弹窗打开，用户可继续操作其他方案
    }
  }
}

// 格式化时间
const formatDate = (timestamp: number) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const collapseVisible = ref(false)
const collapseTitle = ref('参数设置')
const collapseContent = ref('')

function openCollapse(title: string, content: string) {
	collapseTitle.value = title
	collapseContent.value = content
	collapseVisible.value = true
}

function switchPanel(panel: 'settings'|'status'|'report') {
  // 仅在从非状态页切换到状态页时，才初始化图表
  const isSwitchToStatus = activePanel.value !== 'status' && panel === 'status';
  
  activePanel.value = panel;
  if (panel === 'status') {
    getParameterList();
    if (isSwitchToStatus) { // 避免重复调用
      initCoverageChart();
    }
  } else if (panel === 'report') {
    getBugReports();
  }
}

const getParameterList = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/parameters/all');
    parameterList.value = response.data.map((item: any) => ({
      ...item,
      inputWeight: item.weight || 0,
      isSaving: false
    }));
    // 👇 新增：刷新后回到第1页
    currentPage.value = 1
  } catch (error) {
    console.error('获取参数列表失败:', error);
    showMessage('获取参数列表失败', 'error');
  }
};

// 获取Bug报告列表
const getBugReports = async () => {
  bugReportsLoading.value = true
  try {
    const response: PagedBugReports = await bugReportApi.getBugReports({
      page: bugReportsPage.value,
      size: bugReportsSize.value
    })
    bugReports.value = response.content
    totalBugReports.value = response.totalElements
    totalBugPages.value = response.totalPages
    console.log('Bug报告列表：', bugReports.value)
  } catch (err) {
    console.error('获取Bug报告失败：', err)
    bugReports.value = []
  } finally {
    bugReportsLoading.value = false
  }
}

// 显示Bug的参数设置
const showBugParameterSettings = (bug: BugReportItem) => {
  const title = `Bug #${bug.id} - 参数设置 (${bug.targetDatabase})`
  const content = bug.formattedParameterSettings || '// 无参数设置信息'
  openCollapse(title, content)
}

// 显示Bug的测试样例
const showBugTestCase = (bug: BugReportItem) => {
  const title = `Bug #${bug.id} - 测试样例 (${bug.oracleType})`
  let content = `// 测试时间: ${new Date(bug.testTime).toLocaleString('zh-CN')}\n`
  content += `// 错误信息: ${bug.errorMessage || '无'}\n\n`
  content += bug.testCase
  openCollapse(title, content)
}

// 显示消息提示
const showMessage = (text: string, type: 'success' | 'error' | 'info' = 'info') => {
  message.value = { text, type }
  setTimeout(() => {
    message.value.text = ''
  }, 3000)
}

// 加载默认模糊配置
const loadDefaultConfig = async () => {
  loadingConfig.value = true
  try {
    const config: FuzzTestConfig = await fuzzConfigApi.getDefaultConfig()
    // 将配置应用到表单
    Object.assign(form, {
      testOracle: config.testOracle,
      randomSeed: config.randomSeed,
      maxExpressionDepth: config.maxExpressionDepth,
      numQueries: config.numQueries,
      maxNumInserts: config.maxNumInserts,
      numTries: config.numTries,
      timeoutSeconds: config.timeoutSeconds,
      maxGeneratedDatabases: config.maxGeneratedDatabases,
      username: config.username,
      password: config.password,
      host: config.host,
      port: config.port
    })
    console.log('已加载默认配置:', config)
  } catch (err) {
    console.error('加载默认配置失败:', err)
    // 如果加载失败，使用前端默认值
    console.log('使用前端默认配置')
  } finally {
    loadingConfig.value = false
  }
}

// 保存当前配置为默认值
const saveAsDefaultConfig = async () => {
  savingConfig.value = true
  try {
    await fuzzConfigApi.saveDefaultConfig(form)
    showMessage('默认值修改成功！', 'success')
    console.log('配置保存成功')
  } catch (err) {
    console.error('保存配置失败:', err)
    showMessage('保存配置失败，请检查网络连接', 'error')
  } finally {
    savingConfig.value = false
  }
}

// 保存配置到数据库
const saveConfigToDatabase = async () => {
  // 验证输入合法性
  if (!validateFuzzConfig(form)) {
    alert('输入不合法，请检查参数设置！')
    return
  }

  savingConfig.value = true
  try {
    await fuzzConfigApi.saveDefaultConfig(form)
    showMessage('配置保存成功！', 'success')
    console.log('配置保存到数据库成功')
  } catch (err) {
    console.error('保存配置到数据库失败:', err)
    showMessage('保存配置失败，请检查网络连接', 'error')
  } finally {
    savingConfig.value = false
  }
}

// 验证模糊测试配置合法性
const validateFuzzConfig = (config: any): boolean => {
  // 检查必需的参数是否存在且有效
  if (!config.testOracle || config.testOracle.trim() === '') {
    return false
  }
  if (config.randomSeed < -1) {
    return false
  }
  if (config.maxExpressionDepth < 1) {
    return false
  }
  if (config.numQueries < 1) {
    return false
  }
  if (config.maxNumInserts < 1) {
    return false
  }
  if (config.numTries < 1) {
    return false
  }
  if (config.timeoutSeconds < -1) {
    return false
  }
  if (config.maxGeneratedDatabases < 1) {
    return false
  }
  return true
}

// 重置为系统默认值
const resetToSystemDefaults = async () => {
  if (!confirm('确定要重置为系统默认值吗？这将清除所有自定义设置。')) {
    return
  }

  try {
    const defaultConfig: FuzzTestConfig = await fuzzConfigApi.resetDefaultConfig()
    // 应用重置后的配置
    Object.assign(form, {
      testOracle: defaultConfig.testOracle,
      randomSeed: defaultConfig.randomSeed,
      maxExpressionDepth: defaultConfig.maxExpressionDepth,
      numQueries: defaultConfig.numQueries,
      maxNumInserts: defaultConfig.maxNumInserts,
      numTries: defaultConfig.numTries,
      timeoutSeconds: defaultConfig.timeoutSeconds,
      maxGeneratedDatabases: defaultConfig.maxGeneratedDatabases,
      username: defaultConfig.username,
      password: defaultConfig.password,
      host: defaultConfig.host,
      port: defaultConfig.port
    })
    showMessage('已重置为系统默认值！', 'success')
    console.log('配置重置成功')
  } catch (err) {
    console.error('重置配置失败:', err)
    showMessage('重置配置失败，请检查网络连接', 'error')
  }
}

// 新增：更新参数权重（点击保存按钮时调用）
// 保存参数权重
const updateWeight = async (param: Parameter) => {
  // 验证输入是否有效
  if (isNaN(param.inputWeight) || param.inputWeight < 0 || param.inputWeight > 10) {
    showMessage('权重必须是0-10之间的数字', 'error');
    return;
  }

  // 如果输入的权重和原来的一样，不需要保存
  if (param.inputWeight === param.weight) {
    showMessage('权重未变化，无需保存', 'info');
    return;
  }

  try {
    // 显示保存中状态
    param.isSaving = true;
    
    // 调用后端正确的权重更新接口（PUT方式 + URL参数）
    await axios.put(`http://localhost:8080/api/parameters/${param.id}/weight`, {}, {
      params: { weight: param.inputWeight } // 权重放在URL参数里
    });

    // 保存成功后更新显示的权重
    param.weight = param.inputWeight;
    showMessage('权重保存成功', 'success');
  } catch (error) {
    console.error('保存权重失败:', error);
    showMessage('保存权重失败，请重试', 'error');
    // 失败时恢复输入框的值为原来的权重
    param.inputWeight = param.weight;
  } finally {
    // 无论成功失败，都结束保存状态
    param.isSaving = false;
  }
};

function startTest() {
	const payload = { ...form }
	console.log('测试参数:', payload)
	alert('测试已启动！参数已记录到控制台。')
	switchPanel('status')
}

// 重置模糊测试参数为默认值
function resetFuzzParams() {
	Object.assign(form, defaultForm)
}

function initCoverageChart() {
  const canvas = document.getElementById('coverage-chart') as HTMLCanvasElement | null;
  if (!canvas) return;
  // @ts-ignore - Chart 是全局变量
  const { Chart } = window as any;
  if (!Chart) return;

  // 关键：先销毁已存在的图表，避免重复渲染
  if (window.coverageChart) {
    window.coverageChart.destroy();
    window.coverageChart = null; // 清空引用
  }

  // 强制设置画布高度（防止无限变长）
  canvas.style.height = '300px'; // 固定高度，可根据需求调整

  // 初始化新图表
  window.coverageChart = new Chart(canvas.getContext('2d'), {
    type: 'line',
    data: {
      labels: ['0h','0.5h','1h','1.5h','2h','2.5h','3h'],
      datasets: [{
        label: '代码覆盖率',
        data: [10,25,42,58,65,72,80],
        borderColor: '#3E92CC',
        backgroundColor: 'rgba(62,146,204,0.1)',
        tension: 0.4,
        fill: true,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false, // 配合固定高度使用
      scales: {
        y: {
          beginAtZero: true,
          max: 100,
          ticks: { callback: (value: number) => `${value}%` },
          title: { display: true, text: '覆盖率 (%)' },
        },
        x: { title: { display: true, text: '测试时间' } },
      },
      plugins: {
        tooltip: { callbacks: { label: (ctx: any) => `覆盖率: ${ctx.parsed.y}%` } },
      },
    },
  });
}

onMounted(async () => {
	// 初始化
	activeSubTab.value = 'fuzz'
	loadFuzzSchemesFromStorage() // 加载模糊测试参数方案
	await loadDefaultConfig() // 加载默认配置

	if (activePanel.value === 'status') {
    getParameterList()
	initCoverageChart()
  }
})
</script>

<style scoped>
/* 状态页面左右分栏布局 - 完全兼容Vue解析器 */
.flex {
  display: flex;
}
.flex-col {
  flex-direction: column;
}
.justify-between {
  justify-content: space-between;
}
.items-center {
  align-items: center;
}
.gap-6 {
  gap: 1.5rem;
}
.gap-4 {
  gap: 1rem;
}
/* 替换 w-1/2 为 w-half，避免 / 解析错误 */
.w-half {
  width: 50%;
}
.h-full {
  height: 100%;
}
.overflow-hidden {
  overflow: hidden;
}
.overflow-y-auto {
  overflow-y: auto;
}
.flex-1 {
  flex: 1;
}
/* 替换 min-h-\[200px\] 为 min-h-200，删除转义符 */
.min-h-200 {
  min-height: 200px;
}
/* 替换 grid-cols-2 为 grid-two-cols（可选，防止后续报错） */
.grid-two-cols {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}
/* 按钮 hover 效果 */
button:hover {
  cursor: pointer;
}
/* 适配小屏幕 */
@media (max-width: 768px) {
  .flex {
    flex-direction: column;
  }
  .w-half {
    width: 100%;
  }
}

/* ========== 关键新增：穿透样式，强制DatabaseParameterManager组件内的表格列宽固定 ========== */
/* 穿透到DatabaseParameterManager组件内的表格容器 */
:deep(.table-container) {
  width: 100%;
  overflow-x: auto; /* 确保内容超长时横向滚动，不挤压列宽 */
}

/* 强制表格布局固定，所有分页列宽统一（优先级最高） */
:deep(.table-container table) {
  table-layout: fixed !important; 
  width: 100% !important;
  border-collapse: collapse !important;
}

/* 统一表头/单元格基础样式，防止样式冲突 */
:deep(.table-container th),
:deep(.table-container td) {
  white-space: nowrap !important; /* 禁止换行，避免列宽被内容撑开 */
  overflow: hidden !important; /* 超出部分隐藏 */
  text-overflow: ellipsis !important; /* 超长内容显示省略号 */
  border: 1px solid #d1d5db !important;
  padding: 12px 8px !important;
}

/* 逐个固定8列宽度（测试设置页面数据库参数表格），所有分页永久统一 */
:deep(.table-container th:nth-child(1)),
:deep(.table-container td:nth-child(1)) { width: 180px !important; } /* 参数名 */
:deep(.table-container th:nth-child(2)),
:deep(.table-container td:nth-child(2)) { width: 200px !important; } /* 描述 */
:deep(.table-container th:nth-child(3)),
:deep(.table-container td:nth-child(3)) { width: 100px !important; } /* 类别 */
:deep(.table-container th:nth-child(4)),
:deep(.table-container td:nth-child(4)) { width: 100px !important; } /* 设置范围 */
:deep(.table-container th:nth-child(5)),
:deep(.table-container td:nth-child(5)) { width: 120px !important; } /* 约束信息 */
:deep(.table-container th:nth-child(6)),
:deep(.table-container td:nth-child(6)) { width: 120px !important; } /* 默认值 */
:deep(.table-container th:nth-child(7)),
:deep(.table-container td:nth-child(7)) { width: 80px !important; text-align: center !important; } /* 是否测试 */
:deep(.table-container th:nth-child(8)),
:deep(.table-container td:nth-child(8)) { width: 100px !important; text-align: center !important; } /* 操作 */

/* 允许单元格内容换行，显示完整（列宽不变） */
:deep(.table-container td) {
  white-space: normal !important;    /* 取消禁止换行，允许自动换行 */
  line-height: 1.5 !important;       /* 行高调大，换行后更易读 */
  padding: 12px 8px !important;      /* 上下内边距，避免内容贴边 */
  min-height: 40px !important;       /* 最小高度，保证单元格不挤 */
  word-break: break-word !important; /* 超长单词/字符串也能换行（比如长参数名） */
}

/* 表头保持单行，不换行，保证对齐 */
:deep(.table-container th) {
  white-space: nowrap !important;
}

/* 新增：固定表格列宽的核心样式 */
.table-fixed {
  table-layout: fixed;
}
.w-20 { width: 5rem; }
.w-24 { width: 6rem; }
.w-48 { width: 12rem; }
/* 表头加粗 */
.font-medium {
  font-weight: 500;
}
</style>