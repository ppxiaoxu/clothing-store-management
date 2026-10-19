# 任务跟踪：本地 Markdown

项目使用本地 Markdown 跟踪规格、任务和验收记录，代码托管在 GitHub。

- 规格：`.scratch/<feature-slug>/spec.md`。
- 任务：`.scratch/<feature-slug>/issues/<NN>-<slug>.md`，每张任务一个文件，依赖项排在前面。
- 每张任务包含用户可观察的交付行为、验收项、`Blocked by`、`Status`。
- `ready-for-agent` 表示规则与拆分已确认可执行；尚未确认的学习例子标为 `draft`。完成后记录实际验证，再设为 `done`。
- 用任务编号和相对路径记录阻塞边；只有阻塞任务完成，当前任务才进入可执行队列。
- 讨论可追加到文件的 `## Comments`；不因拆分任务而关闭或重写来源规格。

当上游技能说“publish to tracker”时，按上述路径写本地文件。`to-spec` 的测试接口确认、`to-tickets` 的拆分确认仍按当前任务上下文执行，已有明确决定无需重复询问。

`docs/ai-coding/examples/` 提供规格和任务草案，可按具体需求补齐决定与验收记录。

若用户以后明确要求使用 GitHub Issues，再更改本文件；创建、评论、关闭远程 Issue 的授权不会从技能安装或推送代码中自动获得。
