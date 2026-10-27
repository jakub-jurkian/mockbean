#!/usr/bin/env python3
"""
Compare RAG accuracy before and after the Q&A-pair chunking fix.

Old results (300-character chunks) live in results/<model>/,
new results (one chunk per question-and-answer pair) in results/v2-qa-chunking/<model>/.
For each model and mode it reports accuracy per run, the run-1 bootstrap 95% CI,
per-label accuracy, and - for new RAG runs - how often retrieval fell back to Pure mode.

Usage:
    python scripts/compare_chunking.py            # prints a Markdown report
    python scripts/compare_chunking.py --save     # also writes results/v2-qa-chunking/comparison.md
"""

import argparse
import csv
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from bootstrap_analysis import (  # noqa: E402  (reuse the exact method of the original study)
    DEFAULT_CORRECT_MIN, DEFAULT_PARTIAL_MIN, N_BOOTSTRAP, CONFIDENCE, RANDOM_SEED,
    bootstrap_ci, compute_accuracy, load_scores,
)

OLD_DIR = Path("results")
NEW_DIR = Path("results/v2-qa-chunking")
MODELS = ["claude-sonnet-4-6", "gemini-flash-2-5", "llama31"]
LABELS = ["correct", "partial", "wrong", "waffle", "evasive"]


def runs(model_dir: Path, mode: str) -> list[Path]:
    return sorted(model_dir.glob(f"*_{mode}_run*.csv")) + sorted(model_dir.glob(f"*_{mode}_control*.csv"))


def summarise(files: list[Path]) -> dict | None:
    if not files:
        return None
    per_run = [compute_accuracy(load_scores(f), DEFAULT_CORRECT_MIN, DEFAULT_PARTIAL_MIN) for f in files]
    rows = load_scores(files[0])
    _, lo, hi = bootstrap_ci(rows, N_BOOTSTRAP, CONFIDENCE, RANDOM_SEED, DEFAULT_CORRECT_MIN, DEFAULT_PARTIAL_MIN)
    per_label = {}
    for label in LABELS:
        group = [r for r in rows if r["human_label"] == label]
        per_label[label] = compute_accuracy(group, DEFAULT_CORRECT_MIN, DEFAULT_PARTIAL_MIN) if group else None
    return {"runs": per_run, "ci": (lo, hi), "per_label": per_label, "files": files}


def retrieval_stats(files: list[Path]) -> str:
    modes, scores = {}, []
    for f in files:
        with open(f, encoding="utf-8") as fh:
            for row in csv.DictReader(fh):
                mode = row.get("effective_mode") or "?"
                modes[mode] = modes.get(mode, 0) + 1
                if row.get("retrieval_score"):
                    scores.append(float(row["retrieval_score"]))
    mode_text = ", ".join(f"{k}: {v}" for k, v in sorted(modes.items()))
    score_text = f"relevance min {min(scores):.3f} / mean {sum(scores) / len(scores):.3f}" if scores else "no scores"
    return f"{mode_text}; {score_text}"


def pct(x: float | None) -> str:
    return "-" if x is None else f"{x:.1%}"


def report() -> str:
    out = ["# RAG before vs after Q&A-pair chunking", "",
           f"Thresholds: correct >= {DEFAULT_CORRECT_MIN}, partial >= {DEFAULT_PARTIAL_MIN}. "
           f"CI: {CONFIDENCE:.0%} percentile bootstrap, {N_BOOTSTRAP:,} resamples, on run 1.", "",
           "| Model | Mode | Chunking | Accuracy per run | 95% CI (run 1) |",
           "|---|---|---|---|---|"]
    details = []

    for model in MODELS:
        for mode in ("rag", "pure"):
            for name, base in (("old (300 chars)", OLD_DIR), ("new (Q&A pairs)", NEW_DIR)):
                s = summarise(runs(base / model, mode))
                if not s:
                    continue
                if mode == "pure" and base == NEW_DIR:
                    name = "same-day control"
                elif mode == "pure":
                    name = "n/a (no retrieval)"
                acc = " / ".join(pct(a) for a in s["runs"])
                lo, hi = s["ci"]
                out.append(f"| {model} | {mode.upper()} | {name} | {acc} | {pct(lo)} - {pct(hi)} |")
                labels = ", ".join(f"{k} {pct(v)}" for k, v in s["per_label"].items())
                details.append(f"- **{model} · {mode.upper()} · {name}** - per label (run 1): {labels}")
                if mode == "rag" and base == NEW_DIR:
                    details.append(f"  - retrieval: {retrieval_stats(s['files'])}")

    return "\n".join(out + ["", "## Details", ""] + details) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--save", action="store_true", help="write results/v2-qa-chunking/comparison.md")
    args = parser.parse_args()

    text = report()
    print(text)
    if args.save:
        NEW_DIR.mkdir(parents=True, exist_ok=True)
        (NEW_DIR / "comparison.md").write_text(text, encoding="utf-8")
        print(f"Saved -> {NEW_DIR / 'comparison.md'}")


if __name__ == "__main__":
    main()
