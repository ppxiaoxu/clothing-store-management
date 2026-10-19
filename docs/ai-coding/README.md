# AI Coding 与工程化 Skills

童装店记货系统将门店规则与 AI 辅助开发结合：用工程 Skills 组织需求澄清、规格、任务拆分、行为测试、代码审查和缺陷诊断，以库存联动、业务归属和门店操作体验作为贯穿案例。

## 安装内容与来源

原作者：[Matt Pocock / skills](https://github.com/mattpocock/skills)。`docs/engineering` 为介绍目录，技能源文件位于 `skills/engineering` 和 `skills/productivity`。

固定提交：[`49dd158d1076134a641b33efb035946536778336`](https://github.com/mattpocock/skills/tree/49dd158d1076134a641b33efb035946536778336)，上游包版本 `1.3.1`。保留原文件及 UI 元数据，完整文件清单和 SHA-256 见 [upstream-lock.json](upstream-lock.json)。上游采用 MIT 许可，作者与许可全文见 [LICENSE](../../.agents/skills/LICENSE)；该许可适用于引入的技能文件，不替本项目其他源码授予许可。

| 简历中的技能 | 实际用途 | 本地原文 |
| --- | --- | --- |
| grill-with-docs | 澄清依赖决策，及时记录术语和必要的决策 | [SKILL.md](../../.agents/skills/grill-with-docs/SKILL.md) |
| to-spec | 从已有讨论整理规格、测试边界、验收与范围外内容 | [SKILL.md](../../.agents/skills/to-spec/SKILL.md) |
| to-tickets | 按端到端用户行为切分任务，声明阻塞关系 | [SKILL.md](../../.agents/skills/to-tickets/SKILL.md) |
| tdd | 在约定的公开接口上一条失败测试对应一条最小实现 | [SKILL.md](../../.agents/skills/tdd/SKILL.md) |
| code-review | 固定比较点，分别审查项目规范和规格符合度 | [SKILL.md](../../.agents/skills/code-review/SKILL.md) |
| diagnosing-bugs | 建立准确失败的复现循环，再定位、修复和回归 | [SKILL.md](../../.agents/skills/diagnosing-bugs/SKILL.md) |

另外安装四项依赖与配置入口：

- `grilling`、`domain-modeling`：`grill-with-docs` 委托它们完成澄清和领域文档。
- `codebase-design`：`tdd` 在测试接口设计需要时引用的共享设计词汇。
- `setup-matt-pocock-skills`：配置任务跟踪与领域文档的参考入口。

共 10 个技能目录、32 个原版文件；额外保留一份 MIT LICENSE。原版 `tdd/tests.md`、`tdd/mocking.md`、诊断脚本模板与 `agents/openai.yaml` 均随目录保留。

## 使用方式

在仓库中打开 Codex，技能目录为 `.agents/skills/`。按需显式选择技能，或在提示中给出技能名称及本地 `SKILL.md` 路径。Codex 的仓库技能发现规则见 [官方文档](https://learn.chatgpt.com/docs/build-skills#where-codex-loads-local-skills)。安装后可在后续对话使用；未显示时重新加载会话。

保留上游 `agents/openai.yaml` 的显式调用设置，按任务选择需要的技能。其他助手使用不同目录与选择器时，按对应助手的约定安装。

仓库 [AGENTS.md](../../AGENTS.md) 提供稳定约束；[本地任务跟踪](../agents/issue-tracker.md) 和 [领域文档](../agents/domain.md) 补齐上游工作流需要的上下文。默认产物写本地文件，不自动发 GitHub Issues。

一条建议的学习路径：

1. 先读 [业务词表](../../GLOSSARY.md) 和 [库存源码复盘](stock-review.md)，能讲清已有规则。
2. 用 `grill-with-docs` 讨论真实待决定项，不让 AI 把假设写成需求。
3. 用 `to-spec`、`to-tickets` 形成规格与纵向任务；[销售更正规格草案](examples/sales-correction-spec.md) 和 [任务示例](examples/tickets/01-correction.md) 可作起点。
4. 明确测试接口后按 `tdd` 一条行为推进；针对缺陷或新行为观察 Red，再做最小实现；对已有功能补充行为回归。
5. 用 `code-review` 在固定提交间分别检查 Standards 与 Spec；排障时用 `diagnosing-bugs` 先取得可靠失败信号。

例如：`请使用 tdd，先阅读库存复盘和销售更正规格，提出通过销售接口、SKU 查询、流水查询观察行为的测试边界；先讨论边界，不修改业务代码。`

## 面试与现场练习

- [高频面试追问与回答](interview-questions.md)：按 Skills 机制、简历四条主线和库存源码组织，附递进追问与优先练习题。
- [现场 AI Coding 实战指南](live-coding-playbook.md)：以 SKU 库存上限筛选为例，给出 30—45 分钟安排、微规格、测试例子、提示词和演示方法。

## Java、Vue 与 Windows 适配

这些是工作方法，不需要把项目改成 TypeScript。示例的测试思想可用于 JUnit、真实测试数据库、HTTP API 与页面验收。真实 MySQL 行锁与事务需要集成验证，Mock Mapper 不能证明它们。

当前固定版 `tdd` 的实现循环是 Red → Green，重构放在后续审查阶段；阅读时以 `SKILL.md` 为准，部分上游介绍页可能滞后。诊断过程需要对日志中的 Token、密码及云密钥脱敏。

日常使用 PowerShell、Maven 和 HTTP 组织复现。`diagnosing-bugs/scripts/hitl-loop.template.sh` 是人工复现兜底模板，使用时需要 Git Bash / WSL；PowerShell 环境可按同样反馈逻辑编写对应脚本。

## 完整性检查与业务验证

在仓库根目录执行：

```text
python scripts/verify_agent_skills.py
```

该离线检查核对原版文件哈希、技能元数据、依赖目录、引用文件与许可。业务验证围绕规格中的输入、预期与可观察结果单独组织。

源码复盘基线为项目提交 `48591640d0a1c3ef2ea2825c480d36feb0627b0d` 加本地工作区。公开快照与本地 Stock/Sale/DTO/VO 文件的差异记录在 [源码复盘](stock-review.md) 中，准备运行环境时先核对源码依赖。

现有后端自动化入口为 `contextLoads`，前端提供开发、构建与预览命令。库存、关联校验与权限的验收场景见源码复盘；执行时记录环境、输入、实际输出和覆盖范围。
