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
├── core/                         # 跨端共享的领域模型与数据层
│   └── src/commonMain/kotlin/com/example/competition/
│       ├── model/                # GameState、Question、难度与分类定义
│       └── data/                 # QuestionRepository
├── app/
│   ├── shared/                   # Compose Multiplatform 共享 UI 与业务逻辑
│   │   └── src/commonMain/kotlin/com/example/competition/
│   │       ├── App.kt            # 应用入口与页面路由
│   │       ├── ui/screens/       # 各功能页面（登录、首页、答题、结果等）
│   │       ├── viewmodel/        # GameViewModel
│   │       ├── repository/       # 本地/远程数据仓库、在线/离线路由
│   │       ├── data/             # 关卡配置、题库加载、偏好设置
│   │       ├── api/              # Ktor API 客户端
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
