#!/usr/bin/env python3
"""Verify the vendored agent skills offline; this does not run business tests."""

import hashlib
import json
import re
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit


ROOT = Path(__file__).resolve().parents[1]
SKILLS_ROOT = Path(".agents/skills")
LOCK_PATH = Path("docs/ai-coding/upstream-lock.json")
UPSTREAM_COMMIT = "49dd158d1076134a641b33efb035946536778336"
EXPECTED_SKILLS = {
    "grill-with-docs": "skills/engineering/grill-with-docs",
    "to-spec": "skills/engineering/to-spec",
    "to-tickets": "skills/engineering/to-tickets",
    "tdd": "skills/engineering/tdd",
    "code-review": "skills/engineering/code-review",
    "diagnosing-bugs": "skills/engineering/diagnosing-bugs",
    "grilling": "skills/productivity/grilling",
    "domain-modeling": "skills/engineering/domain-modeling",
    "codebase-design": "skills/engineering/codebase-design",
    "setup-matt-pocock-skills": "skills/engineering/setup-matt-pocock-skills",
}
REQUIRED_DEPENDENCIES = {
    "grill-with-docs": ("grilling", "domain-modeling"),
    "tdd": ("codebase-design",),
    "to-spec": ("setup-matt-pocock-skills",),
    "to-tickets": ("setup-matt-pocock-skills",),
    "code-review": ("setup-matt-pocock-skills",),
}
MARKDOWN_LINK = re.compile(r"!?\[[^\]\n]*\]\(\s*(<[^>\n]+>|[^\s)]+)(?:\s+[^)\n]*)?\)")


def markdown_prose(text):
    """Ignore fenced examples and inline code, including shell sample paths."""
    lines = []
    fence_char = None
    fence_length = 0
    for line in text.splitlines():
        match = re.match(r"^\s{0,3}(`{3,}|~{3,})", line)
        if match:
            marker = match.group(1)
            if fence_char is None:
                fence_char, fence_length = marker[0], len(marker)
            elif marker[0] == fence_char and len(marker) >= fence_length:
                fence_char = None
            continue
        if fence_char is None:
            lines.append(line)
    return re.sub(r"(`+).*?\1", "", "\n".join(lines))


def frontmatter_fields(text):
    """Read only the simple scalar fields used upstream, without a YAML dependency.

    Other upstream fields, including disable-model-invocation, are preserved
    and accepted. This is intentionally not a general YAML validator.
    """
    lines = text.splitlines()
    if not lines or lines[0] != "---":
        raise ValueError("missing YAML frontmatter")
    try:
        end = lines.index("---", 1)
    except ValueError:
        raise ValueError("unclosed YAML frontmatter") from None
    fields = {}
    for line in lines[1:end]:
        match = re.match(r"^(name|description):\s*(.*)$", line)
        if match:
            value = match.group(2).strip()
            if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
                value = value[1:-1]
            fields[match.group(1)] = value
    return fields


def verify(root):
    errors = []
    lock_file = root / LOCK_PATH
    try:
        lock = json.loads(lock_file.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        return [f"{LOCK_PATH.as_posix()}: cannot read lock: {exc}"], 0

    if lock.get("schema_version") != 1:
        errors.append(f"{LOCK_PATH.as_posix()}: unsupported schema_version")
    upstream = lock.get("upstream", {})
    if upstream.get("repository") != "https://github.com/mattpocock/skills":
        errors.append(f"{LOCK_PATH.as_posix()}: unexpected upstream repository")
    if upstream.get("commit") != UPSTREAM_COMMIT:
        errors.append(f"{LOCK_PATH.as_posix()}: unexpected upstream commit")

    mappings = lock["skills"]
    for name in sorted(set(mappings) ^ set(EXPECTED_SKILLS)):
        errors.append(f"{LOCK_PATH.as_posix()}: missing or unexpected skill mapping: {name}")
    for name, source in EXPECTED_SKILLS.items():
        mapping = mappings.get(name, {})
        local = (SKILLS_ROOT / name).as_posix()
        if mapping.get("source_path") != source or mapping.get("local_path") != local:
            errors.append(f"{LOCK_PATH.as_posix()}: incorrect path mapping for {name}")
        skill_file = root / SKILLS_ROOT / name / "SKILL.md"
        try:
            fields = frontmatter_fields(skill_file.read_text(encoding="utf-8"))
            if fields.get("name") != name:
                errors.append(f"{local}/SKILL.md: name must match directory '{name}'")
            if not fields.get("description") or fields["description"] in ("|", ">"):
                errors.append(f"{local}/SKILL.md: missing nonempty scalar description")
        except (OSError, UnicodeError, ValueError) as exc:
            errors.append(f"{local}/SKILL.md: {exc}")

    for caller, dependencies in REQUIRED_DEPENDENCIES.items():
        for dependency in dependencies:
            if not (root / SKILLS_ROOT / dependency / "SKILL.md").is_file():
                errors.append(f"{SKILLS_ROOT.as_posix()}/{caller}: missing dependency {dependency}")

    files = lock["files"]
    if len(files) != 32:
        errors.append(f"{LOCK_PATH.as_posix()}: expected 32 upstream skill files, found {len(files)}")
    records = dict(files)
    license_record = lock["license"]
    license_path = (SKILLS_ROOT / "LICENSE").as_posix()
    if license_record.get("local_path") != license_path or license_record.get("source_path") != "LICENSE":
        errors.append(f"{LOCK_PATH.as_posix()}: incorrect license mapping")
    records[license_path] = license_record

    installed_root = root / SKILLS_ROOT
    actual = {
        path.relative_to(root).as_posix()
        for path in installed_root.rglob("*")
        if path.is_file()
    }
    for path in sorted(actual - set(records)):
        errors.append(f"{path}: installed file is not locked")
    for path in sorted(set(records) - actual):
        errors.append(f"{path}: locked file is missing")

    for local, record in sorted(records.items()):
        relative = Path(local)
        target = root / relative
        if relative.is_absolute() or ".." in relative.parts or not target.resolve().is_relative_to(installed_root.resolve()):
            errors.append(f"{local}: locked path is outside the skills directory")
            continue
        if local != license_path:
            parts = relative.parts
            name = parts[2] if len(parts) > 3 and parts[:2] == (".agents", "skills") else None
            source = EXPECTED_SKILLS.get(name)
            expected_source = source + "/" + "/".join(parts[3:]) if source else None
            if record.get("source_path") != expected_source or expected_source is None:
                errors.append(f"{local}: incorrect upstream source path")
        digest = record.get("sha256", "")
        if not re.fullmatch(r"[0-9a-f]{64}", digest):
            errors.append(f"{local}: invalid locked SHA-256")
        try:
            content = target.read_bytes()
        except OSError:
            continue  # Missing paths are already listed above.
        if hashlib.sha256(content).hexdigest() != digest:
            errors.append(f"{local}: SHA-256 mismatch (modified or wrong upstream bytes)")
        if target.suffix.lower() != ".md":
            continue
        try:
            prose = markdown_prose(content.decode("utf-8"))
        except UnicodeError as exc:
            errors.append(f"{local}: invalid UTF-8: {exc}")
            continue
        for match in MARKDOWN_LINK.finditer(prose):
            link = match.group(1).strip("<>")
            parsed = urlsplit(link)
            if parsed.scheme or parsed.netloc or not parsed.path or parsed.path.startswith("/"):
                continue
            destination = target.parent / unquote(parsed.path)
            if not destination.exists():
                errors.append(f"{local}: broken relative link: {link}")

    return errors, len(files)


def main():
    try:
        errors, count = verify(ROOT)
    except (KeyError, TypeError, AttributeError, ValueError) as exc:
        errors, count = [f"{LOCK_PATH.as_posix()}: malformed lock: {exc}"], 0
    if errors:
        print("Agent skills verification FAILED:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1
    print(f"OK: {len(EXPECTED_SKILLS)} skills, {count} locked files + MIT LICENSE; metadata, dependencies and relative links verified offline.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
