# AI Coding 与工程化 Skills

这是童装店记货系统的一次补充迭代：早期通过自然语言需求、Codex 辅助实现和人工反馈完成开发，本次引入可复用的工程工作流并做源码复盘。安装技能不能证明早期开发已经使用它们，也不能替代业务测试。

## 安装内容与来源

原作者：[Matt Pocock / skills](https://github.com/mattpocock/skills)。用户提供的 `docs/engineering` 是介绍目录，实际安装来源为 `skills/engineering` 和 `skills/productivity`。

固定提交：[`49dd158d1076134a641b33efb035946536778336`](https://github.com/mattpocock/skills/tree/49dd158d1076134a641b33efb035946536778336)，上游包版本 `1.3.1`。本次安装保留原文件及 UI 元数据，完整文件清单和 SHA-256 见 [upstream-lock.json](upstream-lock.json)。上游采用 MIT 许可，作者与许可全文见 [LICENSE](../../.agents/skills/LICENSE)；该许可适用于引入的技能文件，不替本项目其他源码授予许可。

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

保留上游 `agents/openai.yaml` 的显式调用设置，不把技能当成每次都要执行的固定流水线。其他助手有不同目录与选择器时，应按该助手的说明安装；本仓库没有创建 Claude 专用目录或宣称已在所有客户端验证。

仓库 [AGENTS.md](../../AGENTS.md) 提供稳定约束；[本地任务跟踪](../agents/issue-tracker.md) 和 [领域文档](../agents/domain.md) 补齐上游工作流需要的上下文。默认产物写本地文件，不自动发 GitHub Issues。

一条建议的学习路径：

1. 先读 [业务词表](../../GLOSSARY.md) 和 [库存源码复盘](stock-review.md)，能讲清已有规则。
2. 用 `grill-with-docs` 讨论真实待决定项，不让 AI 把假设写成需求。
3. 用 `to-spec`、`to-tickets` 形成规格与纵向任务；[销售更正规格草案](examples/sales-correction-spec.md) 和 [任务示例](examples/tickets/01-correction.md) 可作起点。
4. 明确测试接口后按 `tdd` 一条行为推进；已有代码本来就通过的补测记为回归覆盖，不能虚构 Red。
5. 用 `code-review` 在固定提交间分别检查 Standards 与 Spec；排障时用 `diagnosing-bugs` 先取得可靠失败信号。

例如：`请使用 tdd，先阅读库存复盘和销售更正规格，提出通过销售接口、SKU 查询、流水查询观察行为的测试边界；先讨论边界，不修改业务代码。`

## Java、Vue 与 Windows 适配

这些是工作方法，不需要把项目改成 TypeScript。示例的测试思想可用于 JUnit、真实测试数据库、HTTP API 与页面验收。真实 MySQL 行锁与事务需要集成验证，Mock Mapper 不能证明它们。

当前固定版 `tdd` 的实现循环是 Red → Green，重构放在后续审查阶段；阅读时以 `SKILL.md` 为准，部分上游介绍页可能滞后。诊断过程需要对日志中的 Token、密码及云密钥脱敏。

日常使用 PowerShell、Maven 和 HTTP 复现不受诊断技能中的 Bash 示例限制。`diagnosing-bugs/scripts/hitl-loop.template.sh` 仅是最后人工复现兜底的模板，使用它需要 Git Bash / WSL，不能原样当成 PowerShell 执行；本次没有运行该模板。

## 本次验证与现状

在仓库根目录执行：

```text
python scripts/verify_agent_skills.py
```

该离线检查核对原版文件哈希、技能元数据、依赖目录、引用文件与许可，不验证业务功能。本次没有改业务代码，没有新增声称已执行的 TDD、浏览器回归、部署或压测结果。

源码复盘基线为项目提交 `48591640d0a1c3ef2ea2825c480d36feb0627b0d` 加本地工作区。当前公开快照缺少部分本地 Stock/Sale/DTO/VO 文件，已有跟踪文件会引用它们，不能承诺克隆后完整构建。本次提交仅包含技能、配置和说明，不夹带这些本地业务文件。

现有后端测试只有 `contextLoads`；前端没有单元或端到端测试命令。README 中历史功能与部署描述需要与实际文件、运行记录分别核对。后续优先补齐可交付源码快照，再在隔离环境运行库存、关联校验与权限场景。
