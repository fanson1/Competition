# 一站到底 - 知识竞答挑战

基于 **Kotlin Multiplatform + Compose Multiplatform** 开发的知识竞答游戏，支持 **Android / iOS / Desktop(JVM)** 多端，并内置基于 **Ktor** 的后端服务用于账号、排行榜、对战与数据同步。

## 功能特性

- **关卡闯关**：共 10 关，难度逐级递增（初出茅庐 → 一代宗师），每关由基础题、中级题、高级题混合组成
- **题库分类**：科学、历史、地理、文化、综合 五大类
- **计时答题**：按难度设置限时（初级 10s / 中级 15s / 高级 20s），支持连击加分与玩家称号（势如破竹、表现出色等）
- **用户体系**：注册 / 登录（bcrypt 密码加密）、昵称与 Emoji 头像、修改密码
- **个人中心**：总分、最高关卡、答对数、最高连击、已通关卡等统计
- **排行榜**：总排行 + 单关排行（按关卡记录个人最佳成绩）
- **单关挑战**：向排行榜上的其他玩家发起同关卡比分挑战
- **挑战英雄榜**：统计挑战胜负、胜率及挑战记录（我挑战的 / 被挑战的）
- **在线 / 离线双模式**：离线模式本地存储，在线模式连接服务器同步数据
- **本地持久化**：SQLDelight（SQLite）存储用户、进度、排行与挑战记录
- **云同步**：进度、个人资料、排行榜数据可一键与服务器同步

## 技术栈

- Kotlin Multiplatform / Compose Multiplatform（共享 UI）
- SQLDelight（本地数据库）
- Ktor Client / Server（网络通信与后端服务）
- kotlinx.serialization（JSON 序列化）
- Exposed + H2/PostgreSQL + bcrypt（服务端存储与加密）

## 模块结构

```
.
├── core/                         # 跨端共享的领域模型与基础设施（纯 KMP，无 Compose 依赖）
│   └── src/commonMain/kotlin/com/example/competition/
│       ├── model/                # GameState、Question、用户/排行模型
│       ├── data/                 # LevelConfig、QuestionRepository
│       ├── mvi/                  # 可复用 MVI 架构基座（MviContract / MviViewModel）
│       └── util/                 # LevelMapCodec 等纯工具类
├── app/
│   ├── shared/                   # Compose Multiplatform 共享 UI 与业务逻辑
│   │   └── src/commonMain/kotlin/com/example/competition/
│   │       ├── App.kt            # 应用入口：AppViewModel 协调路由与启动流程
│   │       ├── presentation/     # 分层后的 MVI 层，每个功能一个子包
│   │       │   ├── app/          #   启动 / 会话 / 导航（AppUiState/Intent/Effect/ViewModel）
│   │       │   ├── game/         #   答题主流程（GameUiState/Intent/Effect/ViewModel）
│   │       │   ├── login/        #   登录注册
│   │       │   ├── profile/      #   个人中心
│   │       │   ├── leaderboard/  #   排行榜
│   │       │   ├── challenge/    #   单关挑战
│   │       │   ├── challengehero/#   挑战英雄榜
│   │       │   └── mode/         #   在线/离线模式选择
│   │       ├── ui/
│   │       │   ├── screens/      # 各功能页面（仅负责渲染与分发 Intent）
│   │       │   ├── components/   # 可复用组件（ScreenBackground/ScreenHeader/StatItem…）
│   │       │   ├── theme/        # 颜色、主题、字符串
│   │       │   └── MviUi.kt      # rememberViewModel / MviEffectCollector 等 UI 接线工具
│   │       ├── repository/       # 数据层：接口 + 本地/远程实现 + 模式路由
│   │       │   ├── bridge/       #   RepositoryBridge 统一出口
│   │       │   ├── local/        #   SQLDelight 本地实现
│   │       │   └── remote/       #   Ktor 远程实现
│   │       ├── data/             # UserManager、关卡配置、题库加载、偏好设置
│   │       ├── api/              # Ktor API 客户端与 DTO
│   │       ├── sync/             # 数据同步
│   │       └── db/               # SQLDelight 数据库管理
│   ├── androidApp/               # Android 入口
│   ├── iosApp/                   # iOS 入口（Xcode 工程）
│   └── desktopApp/               # Desktop (JVM) 入口
└── server/                       # Ktor 后端服务
    └── src/main/kotlin/com/example/competition/
        ├── Application.kt        # 服务入口（默认端口 8080）
        ├── route/                # Auth / User / Leaderboard / Challenge / Sync 路由
        ├── service/              # 业务逻辑
        ├── model/                # Exposed 表定义
        ├── config/               # 数据库配置
        └── middleware/           # 认证中间件
```

## 架构设计

项目采用 **MVI（Model-View-Intent）单向数据流**，并在 `core` 中沉淀了可复用的架构基座，所有功能页共享同一套模式。

### MVI 数据流

```
UI  --dispatch(Intent)-->  ViewModel --reduce/setState-->  StateFlow<S> --collectAsState-->  UI
                              |
                              +---emit(Effect)-->  MviEffectCollector --> 一次性副作用（导航/提示）
```

- `MviState`：不可变的 UI 快照，UI 只根据 State 渲染
- `MviIntent`：用户意图（sealed interface），UI 通过 `dispatch` 提交
- `MviEffect`：一次性副作用（页面跳转、弹窗等），由 `MviEffectCollector` 消费
- `MviViewModel`：基类封装 StateFlow / SharedFlow / `setState` / `emit` / `launch`，子类在 `onIntent` 中编排异步逻辑

### 分层与解耦

```
┌──────────────────────────────────────────────────────────┐
│ presentation/  （MVI：UiState / Intent / Effect / VM）    │
├──────────────────────────────────────────────────────────┤
│ ui/screens/    （纯渲染，只调用 ViewModel，不碰数据层）     │
├──────────────────────────────────────────────────────────┤
│ repository/    （数据访问接口 + 本地/远程实现，ModeRouter  │
│                 按在线/离线模式切换，RepositoryBridge 统一 │
│                 出口，业务层不感知具体实现）               │
├──────────────────────────────────────────────────────────┤
│ data/  api/  sync/  db/   （底层存储、网络、同步）         │
└──────────────────────────────────────────────────────────┘
```

- 所有 `GameState`、`Question`、用户模型等纯数据类下沉到 `core`，业务层只依赖接口，便于多端复用与测试
- `ModeRouter` 根据在线/离线模式自动路由到本地或远程实现；`RepositoryBridge` 提供统一入口，上层无感知
- `LevelMapCodec` 等纯逻辑工具放入 `core/util`，替换原先分散在 `UserManager` 等处的重复序列化代码

### 页面接线

```kotlin
val viewModel = rememberViewModel { ProfileViewModel() }   // 与组合生命周期绑定
val state by viewModel.state.collectAsState()              // 渲染状态

MviEffectCollector(viewModel) { effect ->                  // 一次性副作用
    when (effect) {
        ProfileEffect.Back -> onBack()
        ...
    }
}

// 用户操作
viewModel.dispatch(ProfileIntent.ToggleOnline(true))
```

## 可复用组件与工具

| 名称 | 位置 | 说明 |
| --- | --- | --- |
| `MviViewModel` | `core/.../mvi` | MVI 基类：State/Effect 管道、`setState`、`emit`、`launch` |
| `MviContract` | `core/.../mvi` | `MviState` / `MviIntent` / `MviEffect` 标记接口 |
| `LevelMapCodec` | `core/.../util` | 关卡进度 `Map<Int,Int>` ↔ `"1:200,2:450"` 编解码 |
| `rememberViewModel` | `app/.../ui/MviUi.kt` | 在组合中持有 ViewModel |
| `MviEffectCollector` | `app/.../ui/MviUi.kt` | 收集一次性副作用 |
| `ScreenBackground` | `app/.../ui/components` | 全屏渐变背景（支持自定义渐变色与对齐） |
| `ScreenHeader` | `app/.../ui/components` | 统一的"返回 / 标题"头部 |
| `StatItem` | `app/.../ui/components` | 数值 / 标签统计项 |
| `rememberPulseScale` | `app/.../ui/components` | 呼吸缩放动画 |

## 运行项目

### Android

```bash
./gradlew :app:androidApp:assembleDebug
```

安装 APK 到设备或模拟器即可。也可以直接用 IDE（Android Studio）的 Run 配置运行。

### Desktop (JVM)

```bash
./gradlew :app:desktopApp:run
```

### Server

```bash
./gradlew :server:run
```

服务默认监听 `0.0.0.0:8080`。数据库默认使用文件型 H2（`./data/competition`），可通过环境变量切换：

| 环境变量 | 说明 | 默认值 |
| --- | --- | --- |
| `DATABASE_URL` | JDBC 连接串 | `jdbc:h2:file:./data/competition;DB_CLOSE_DELAY=-1` |
| `DATABASE_USER` | 数据库用户名 | `sa` |
| `DATABASE_PASSWORD` | 数据库密码 | 空 |

### iOS

打开 [app/iosApp](./app/iosApp) 目录下的 Xcode 工程，选择模拟器或真机运行。

## 服务器地址配置

各端默认服务器地址定义在 `PlatformUtils` 各平台实现中：

- Android：`http://192.168.50.39:8080`
- iOS / Desktop：`http://localhost:8080`

应用内可在“在线模式”设置中修改服务器地址（会持久化保存）。真机调试时请将地址改为局域网内服务端的 IP。

## 题库

题库文件位于 [app/shared/src/commonMain/composeResources/files/questions.json](./app/shared/src/commonMain/composeResources/files/questions.json)，由 `QuestionLoader` 加载，可通过修改该文件扩充题目。
