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
							<div class="border-b-2 border-gray-200 mb-6">
								<button id="db-params-tab" class="px-4 py-2 font-medium" :class="activeSubTab==='db' ? 'text-blue-700 border-b-2 border-blue-700' : 'text-gray-500'" @click="activeSubTab='db'">数据库参数</button>
								<button id="fuzz-params-tab" class="px-4 py-2 font-medium" :class="activeSubTab==='fuzz' ? 'text-blue-700 border-b-2 border-blue-700' : 'text-gray-500'" @click="activeSubTab='fuzz'">模糊测试参数</button>
							</div>

							<!-- 数据库参数子页面 -->
							<div v-show="activeSubTab==='db'">
								<DatabaseParameterManager />
							</div>

							<!-- 模糊测试参数子页面 -->
							<div v-show="activeSubTab==='fuzz'" class="space-y-6">
								<div>
									<h3 class="text-lg font-semibold mb-4">基础参数</h3>
									<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
										<div>
											<label class="block text-sm font-medium mb-2">测试Oracle</label>
											<select v-model="form.testOracle" class="w-full p-2 border border-gray-300 rounded">
												<option value="TLP">TLP (Test Language Platform)</option>
												<option value="RBR">RBR (Result Set Comparison)</option>
												<option value="CBR">CBR (Coverage-Based Routing)</option>
											</select>
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">并行线程数</label>
											<input type="number" v-model.number="form.numThreads" min="1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">随机种子</label>
											<input type="number" v-model.number="form.randomSeed" min="-1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">最大表达式深度</label>
											<input type="number" v-model.number="form.maxExpressionDepth" min="1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">查询数量</label>
											<input type="number" v-model.number="form.numQueries" min="1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">最大插入数量</label>
											<input type="number" v-model.number="form.maxNumInserts" min="1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">尝试次数</label>
											<input type="number" v-model.number="form.numTries" min="1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">超时时间(秒)</label>
											<input type="number" v-model.number="form.timeoutSeconds" min="-1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">最大生成数据库数</label>
											<input type="number" v-model.number="form.maxGeneratedDatabases" min="-1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">最大字符串长度</label>
											<input type="number" v-model.number="form.maxStringLength" min="1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
										<div>
											<label class="block text-sm font-medium mb-2">常量缓存大小</label>
											<input type="number" v-model.number="form.constantCacheSize" min="1" class="w-full p-2 border border-gray-300 rounded" />
										</div>
									</div>
								</div>

							<div class="border-t-2 border-gray-200 pt-4">
								<div class="flex items-center mb-4">
									<input type="checkbox" id="qpg-enable" class="mr-2" v-model="form.qpgEnabled">
									<h3 class="text-lg font-semibold">启用QPG (Query Plan Guidance)</h3>
								</div>
								<div class="grid grid-cols-1 md:grid-cols-2 gap-4 pl-6">
									<div class="flex items-center">
										<input type="checkbox" id="qpg-log-query-plan" class="mr-2" :disabled="!form.qpgEnabled" v-model="form.qpgLogQueryPlan">
										<label class="text-sm font-medium">记录查询计划</label>
									</div>
									<div>
										<label class="block text-sm font-medium mb-2">最大间隔</label>
										<input type="number" id="qpg-max-interval" :disabled="!form.qpgEnabled" v-model.number="form.qpgMaxInterval" class="w-full p-2 border border-gray-300 rounded" />
									</div>
									<div>
										<label class="block text-sm font-medium mb-2">奖励权重</label>
										<input type="number" step="0.01" min="0" max="1" id="qpg-reward-weight" :disabled="!form.qpgEnabled" v-model.number="form.qpgRewardWeight" class="w-full p-2 border border-gray-300 rounded" />
									</div>
									<div>
										<label class="block text-sm font-medium mb-2">选择概率</label>
										<input type="number" step="0.01" min="0" max="1" id="qpg-selection-probability" :disabled="!form.qpgEnabled" v-model.number="form.qpgSelectionProbability" class="w-full p-2 border border-gray-300 rounded" />
									</div>
								</div>
							</div>

							<div class="border-t-2 border-gray-200 pt-4">
								<h3 class="text-lg font-semibold mb-4">数据库登录凭据</h3>
								<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
									<div>
										<label class="block text-sm font-medium mb-2">用户名</label>
										<input type="text" v-model="form.username" class="w-full p-2 border border-gray-300 rounded" />
									</div>
									<div>
										<label class="block text-sm font-medium mb-2">密码</label>
										<input type="password" v-model="form.password" class="w-full p-2 border border-gray-300 rounded" />
									</div>
									<div>
										<label class="block text-sm font-medium mb-2">主机</label>
										<input type="text" v-model="form.host" placeholder="localhost" class="w-full p-2 border border-gray-300 rounded" />
									</div>
									<div>
										<label class="block text-sm font-medium mb-2">端口</label>
										<input type="number" v-model.number="form.port" min="-1" class="w-full p-2 border border-gray-300 rounded" />
									</div>
								</div>
							</div>

							<div class="border-t-2 border-gray-200 pt-4">
								<h3 class="text-lg font-semibold mb-4">额外选项</h3>
								<div class="space-y-2">
									<div class="flex items-center">
										<input type="checkbox" v-model="form.logEachSelect" class="mr-2" />
										<label class="text-sm font-medium">记录每个SELECT语句</label>
									</div>
									<div class="flex items-center pl-6">
										<input type="checkbox" v-model="form.logExecutionTime" :disabled="!form.logEachSelect" class="mr-2" />
										<label class="text-sm font-medium">记录执行时间</label>
									</div>
									<div class="flex items-center">
										<input type="checkbox" v-model="form.printFailed" class="mr-2" />
										<label class="text-sm font-medium">打印失败的语句</label>
									</div>
									<div class="flex items-center">
										<input type="checkbox" v-model="form.printProgressInfo" class="mr-2" />
										<label class="text-sm font-medium">打印进度信息</label>
									</div>
									<div class="flex items-center">
										<input type="checkbox" v-model="form.printStatements" class="mr-2" />
										<label class="text-sm font-medium">打印所有语句</label>
									</div>
									<div class="flex items-center">
										<input type="checkbox" v-model="form.testOnlyNonempty" class="mr-2" />
										<label class="text-sm font-medium">仅测试非空表</label>
									</div>
								</div>
							</div>
						</div>

						<!-- 结束模糊测试参数子页面容器 -->
					</div>
						<div class="flex justify-center p-6">
							<button id="start-test-btn" class="bg-blue-600 text-white font-bold py-2 px-6 rounded hover:bg-blue-700 transition-all" @click="startTest">开始测试</button>
						</div>
					</div>
				</div>

				<!-- 状态信息页面 -->
				<div v-show="activePanel==='status'" class="space-y-6">
					<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
						<div class="bg-white rounded-lg shadow-md p-6 border-2 border-table-border">
							<h2 class="text-lg font-bold mb-4">数据库信息</h2>
							<div class="space-y-2">
								<div class="flex"><span class="font-semibold w-24">数据库：</span><span id="db-name">MySQL</span></div>
								<div class="flex"><span class="font-semibold w-24">版本：</span><span id="db-version">8.0.26</span></div>
							</div>
						</div>
						<div class="bg-white rounded-lg shadow-md p-6 border-2 border-table-border">
							<h2 class="text-lg font-bold mb-4">测试状态信息</h2>
							<div class="space-y-2">
								<div class="flex justify-between"><span class="font-semibold">测试Oracle：</span><span>{{ form.testOracle }}</span></div>
								<div class="flex justify-between"><span class="font-semibold">Bug数量：</span><span>15</span></div>
								<div class="flex justify-between"><span class="font-semibold">测试线程数：</span><span>{{ form.numThreads }}</span></div>
								<div class="flex justify-between"><span class="font-semibold">吞吐量：</span><span>120 qps</span></div>
								<div class="flex justify-between"><span class="font-semibold">测试时间：</span><span>2h 30m</span></div>
								<div class="flex justify-between"><span class="font-semibold">执行次数：</span><span>1,258,390</span></div>
							</div>
						</div>
					</div>
					<div class="bg-white rounded-lg shadow-md p-6 border-2 border-table-border">
						<h2 class="text-lg font-bold mb-4 text-center">覆盖率变化曲线</h2>
						<div class="h-80">
							<canvas id="coverage-chart"></canvas>
						</div>
					</div>
				</div>

				<!-- 测试报告页面 -->
				<div v-show="activePanel==='report'">
					<div class="bg-white rounded-lg shadow-md p-6 border-2 border-table-border">
						<h2 class="text-lg font-bold mb-4">Bug列表</h2>
						<div class="overflow-x-auto">
							<table class="min-w-full border-2 border-table-border">
								<thead>
									<tr class="bg-gray-100">
										<th class="py-2 px-4 border border-table-border text-left">Bug Id</th>
										<th class="py-2 px-4 border border-table-border text-left">类型（崩溃、逻辑）</th>
										<th class="py-2 px-4 border border-table-border text-left">目标数据库</th>
										<th class="py-2 px-4 border border-table-border text-left">Oracle</th>
										<th class="py-2 px-4 border border-table-border text-left">参数设置</th>
										<th class="py-2 px-4 border border-table-border text-left">测试样例</th>
									</tr>
								</thead>
								<tbody>
									<tr v-for="row in 5" :key="row" :class="row%2===0 ? 'bg-gray-50': ''">
										<td class="py-2 px-4 border border-table-border">{{ row }}</td>
										<td class="py-2 px-4 border border-table-border">{{ row%2===0 ? '逻辑' : '崩溃' }}</td>
										<td class="py-2 px-4 border border-table-border">{{ ['MySQL','PostgreSQL','Oracle','MySQL','SQL Server'][row-1] }}</td>
										<td class="py-2 px-4 border border-table-border">{{ ['TLP','RBR','CBR','TLP','RBR'][row-1] }}</td>
										<td class="py-2 px-4 border border-table-border">
											<button class="show-params-btn bg-blue-500 text-white px-2 py-1 rounded hover:bg-blue-600 transition-all" @click="openCollapse('参数设置', paramsSample)">显示代码 <i class="fa fa-eye ml-1"></i></button>
										</td>
										<td class="py-2 px-4 border border-table-border">
											<button class="show-example-btn bg-green-500 text-white px-2 py-1 rounded hover:bg-green-600 transition-all" @click="openCollapse('测试样例', exampleSample)">显示代码 <i class="fa fa-eye ml-1"></i></button>
										</td>
									</tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</main>
		</div>

		<!-- 代码显示折叠面板 -->
		<div id="code-collapse-panel" class="fixed bottom-0 left-0 right-0 bg-white border-t-2 border-table-border rounded-t-lg shadow-lg" v-show="collapseVisible">
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
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import DatabaseParameterManager from './components/DatabaseParameterManager.vue'

const activePanel = ref<'settings'|'status'|'report'>('settings')
const activeSubTab = ref<'db'|'fuzz'>('db')

const form = reactive({
	testOracle: 'TLP',
	numThreads: 16,
	randomSeed: -1,
	maxExpressionDepth: 3,
	numQueries: 100000,
	maxNumInserts: 30,
	numTries: 100,
	timeoutSeconds: -1,
	maxGeneratedDatabases: -1,
	maxStringLength: 10,
	constantCacheSize: 100,
	qpgEnabled: false,
	qpgLogQueryPlan: false,
	qpgMaxInterval: 1000,
	qpgRewardWeight: 0.25,
	qpgSelectionProbability: 0.7,
	username: 'sqlancer',
	password: 'sqlancer',
	host: '',
	port: -1,
	logEachSelect: true,
	logExecutionTime: true,
	printFailed: true,
	printProgressInfo: true,
	printStatements: false,
	testOnlyNonempty: false,
})

const collapseVisible = ref(false)
const collapseTitle = ref('参数设置')
const collapseContent = ref('')

const paramsSample = `// 这里是参数设置的代码示例
max_connections = 151
wait_timeout = 28800
innodb_buffer_pool_size = 134217728
max_allowed_packet = 4194304
query_cache_size = 0
sort_buffer_size = 262144`

const exampleSample = `// 这里是测试样例的代码示例
SELECT * FROM users WHERE id = ' OR '1' = '1' --
INSERT INTO products VALUES (NULL, 'test', 'desc', -1)
UPDATE accounts SET balance = balance + 1000 WHERE user_id = 1`

function openCollapse(title: string, content: string) {
	collapseTitle.value = title
	collapseContent.value = content
	collapseVisible.value = true
}

function switchPanel(panel: 'settings'|'status'|'report') {
	activePanel.value = panel
	if (panel === 'status') {
		initCoverageChart()
	}
}



function startTest() {
	const payload = { ...form }
	console.log('测试参数:', payload)
	alert('测试已启动！参数已记录到控制台。')
	switchPanel('status')
}

function initCoverageChart() {
	const canvas = document.getElementById('coverage-chart') as HTMLCanvasElement | null
	if (!canvas) return
	// @ts-ignore - Chart is provided by global CDN
	const { Chart } = window as any
	if (!Chart) return
	// @ts-ignore
	if (window.coverageChart) {
		// @ts-ignore
		window.coverageChart.destroy()
	}
	// @ts-ignore
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
			maintainAspectRatio: false,
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
	})
}

onMounted(() => {
	// 初始化
	activePanel.value = 'settings'
	activeSubTab.value = 'db'
})
</script>

<style scoped>
</style>

