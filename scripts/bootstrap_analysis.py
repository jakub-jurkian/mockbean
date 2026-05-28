#!/usr/bin/env python3
"""
MockBean — Bootstrap CI & Threshold Sensitivity Analysis
=========================================================
Reads the 12 existing experiment CSVs and produces:

  1. Bootstrap 95% confidence intervals for accuracy (per experiment)
  2. Threshold sensitivity analysis (correct_min × partial_min grid)
  3. Two PDF figures ready for inclusion in the LaTeX report:
       raport/fig_bootstrap_ci.pdf   — accuracy + 95% CI error bars
       raport/fig_threshold_sens.pdf — sensitivity heatmap (Claude Pure baseline)
  4. Two CSV tables with numeric results:
       results/bootstrap_ci.csv
       results/threshold_sensitivity.csv

No API calls are made — this operates solely on the result CSVs already on disk.

Usage:
    python scripts/bootstrap_analysis.py
"""

import csv
import random
import math
from pathlib import Path
from itertools import product

# ── optional matplotlib import ────────────────────────────────────────────────
try:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    import matplotlib.patches as mpatches
    import numpy as np
    HAS_MPL = True
except ImportError:
    HAS_MPL = False
    print("WARNING: matplotlib not found — skipping figure generation.")
    print("         Install with: pip install matplotlib numpy")

# ── Paths ─────────────────────────────────────────────────────────────────────

RESULTS_DIR = Path("results")
RAPORT_DIR  = Path("raport")

EXPERIMENT_FILES = {
    "claude · RAG":   RESULTS_DIR / "claude-sonnet-4-6" / "claude_sonnet_rag_run1.csv",
    "claude · Pure":  RESULTS_DIR / "claude-sonnet-4-6" / "claude_sonnet_pure_run1.csv",
    "gemini · RAG":   RESULTS_DIR / "gemini-flash-2-5"  / "gemini_flash_rag_run1.csv",
    "gemini · Pure":  RESULTS_DIR / "gemini-flash-2-5"  / "gemini_flash_pure_run1.csv",
    "llama · RAG":    RESULTS_DIR / "llama31"            / "llama31_rag_run1.csv",
    "llama · Pure":   RESULTS_DIR / "llama31"            / "llama31_pure_run1.csv",
}

# Baseline thresholds used during the experiments
DEFAULT_CORRECT_MIN = 0.65
DEFAULT_PARTIAL_MIN = 0.30

# Bootstrap parameters
N_BOOTSTRAP   = 10_000
CONFIDENCE    = 0.95
RANDOM_SEED   = 42

# ── Helpers ───────────────────────────────────────────────────────────────────

def load_scores(csv_path: Path) -> list[dict]:
    """Return a list of rows (dicts) from an experiment CSV."""
    rows = []
    with open(csv_path, encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            if row.get("error"):
                continue
            try:
                rows.append({
                    "llm_score":   float(row["llm_score"]),
                    "human_label": row["human_label"],
                })
            except (ValueError, KeyError):
                continue
    return rows


HUMAN_LABEL_TO_BUCKET = {
    "correct": "correct",
    "partial": "partial",
    "wrong":   "low",
    "waffle":  "low",
    "evasive": "low",
}


def score_to_bucket(score: float, correct_min: float, partial_min: float) -> str:
    if score >= correct_min:
        return "correct"
    elif score >= partial_min:
        return "partial"
    else:
        return "low"


def compute_accuracy(rows: list[dict], correct_min: float, partial_min: float) -> float:
    """Fraction of rows where predicted bucket == expected bucket."""
    if not rows:
        return 0.0
    hits = sum(
        score_to_bucket(r["llm_score"], correct_min, partial_min)
        == HUMAN_LABEL_TO_BUCKET[r["human_label"]]
        for r in rows
    )
    return hits / len(rows)


# ── 1. Bootstrap CI ───────────────────────────────────────────────────────────

def bootstrap_ci(rows: list[dict], n: int, conf: float, seed: int,
                 correct_min: float, partial_min: float) -> tuple[float, float, float]:
    """
    Return (point_estimate, lower_bound, upper_bound) using percentile bootstrap.
    """
    rng = random.Random(seed)
    point = compute_accuracy(rows, correct_min, partial_min)

    boot_accs = []
    for _ in range(n):
        sample = rng.choices(rows, k=len(rows))
        boot_accs.append(compute_accuracy(sample, correct_min, partial_min))

    boot_accs.sort()
    alpha = 1 - conf
    lo = boot_accs[int(math.floor(alpha / 2 * n))]
    hi = boot_accs[int(math.ceil((1 - alpha / 2) * n)) - 1]
    return point, lo, hi


def run_bootstrap_analysis() -> list[dict]:
    print("\n" + "=" * 64)
    print("  BOOTSTRAP 95% CONFIDENCE INTERVALS")
    print("=" * 64)
    print(f"  Resamples : {N_BOOTSTRAP:,}  |  Seed : {RANDOM_SEED}")
    print(f"  Thresholds: correct≥{DEFAULT_CORRECT_MIN}  partial≥{DEFAULT_PARTIAL_MIN}")
    print("-" * 64)
    print(f"  {'Experiment':<20} {'Acc':>6}  {'95% CI':^16}  {'±half':>6}")
    print(f"  {'-' * 56}")

    ci_results = []
    for label, csv_path in EXPERIMENT_FILES.items():
        if not csv_path.exists():
            print(f"  {label:<20}  FILE NOT FOUND: {csv_path}")
            continue

        rows = load_scores(csv_path)
        point, lo, hi = bootstrap_ci(
            rows, N_BOOTSTRAP, CONFIDENCE, RANDOM_SEED,
            DEFAULT_CORRECT_MIN, DEFAULT_PARTIAL_MIN,
        )
        half = (hi - lo) / 2
        print(f"  {label:<20} {point:>5.1%}  [{lo:.1%} – {hi:.1%}]  ±{half:.1%}")

        ci_results.append({
            "experiment":   label,
            "accuracy":     round(point, 4),
            "ci_lower":     round(lo, 4),
            "ci_upper":     round(hi, 4),
            "ci_half":      round(half, 4),
            "n_cases":      len(rows),
            "n_bootstrap":  N_BOOTSTRAP,
        })

    return ci_results


# ── 2. Threshold Sensitivity ──────────────────────────────────────────────────

def run_threshold_sensitivity(focus_experiment: str = "claude · Pure") -> list[dict]:
    """
    Sweep correct_min ∈ [0.50, 0.80] and partial_min ∈ [0.20, 0.45] for one
    representative experiment to show how sensitive results are to threshold choice.
    """
    csv_path = EXPERIMENT_FILES.get(focus_experiment)
    if not csv_path or not csv_path.exists():
        print(f"\nCannot run threshold sensitivity: '{focus_experiment}' not found.")
        return []

    rows = load_scores(csv_path)

    correct_vals = [round(v, 2) for v in [0.50, 0.55, 0.60, 0.65, 0.70, 0.75, 0.80]]
    partial_vals = [round(v, 2) for v in [0.20, 0.25, 0.30, 0.35, 0.40, 0.45]]

    print(f"\n{'=' * 64}")
    print(f"  THRESHOLD SENSITIVITY — {focus_experiment}")
    print(f"{'=' * 64}")
    header = "  partial↓\\correct→ " + "  ".join(f"{v:.2f}" for v in correct_vals)
    print(header)
    print(f"  {'-' * (len(header) - 2)}")

    sens_results = []
    grid = {}
    for partial_min in partial_vals:
        row_str = f"  {partial_min:.2f}              "
        for correct_min in correct_vals:
            if partial_min >= correct_min:
                acc = float("nan")
                row_str += "  n/a  "
            else:
                acc = compute_accuracy(rows, correct_min, partial_min)
                row_str += f" {acc:5.1%} "
            grid[(correct_min, partial_min)] = acc
            sens_results.append({
                "experiment":  focus_experiment,
                "correct_min": correct_min,
                "partial_min": partial_min,
                "accuracy":    round(acc, 4) if not math.isnan(acc) else "",
            })
        print(row_str)

    # Mark the default threshold
    default_acc = grid.get((DEFAULT_CORRECT_MIN, DEFAULT_PARTIAL_MIN), float("nan"))
    print(f"\n  Default thresholds ({DEFAULT_CORRECT_MIN} / {DEFAULT_PARTIAL_MIN}): "
          f"accuracy = {default_acc:.1%}")
    print(f"  Min across grid : {min(v for v in grid.values() if not math.isnan(v)):.1%}")
    print(f"  Max across grid : {max(v for v in grid.values() if not math.isnan(v)):.1%}")

    return sens_results, correct_vals, partial_vals, grid


# ── 3. Save CSVs ─────────────────────────────────────────────────────────────

def save_csv(rows: list[dict], path: Path, fields: list[str]):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)
    print(f"\n  Saved → {path}")


# ── 4. Figures ────────────────────────────────────────────────────────────────

def plot_bootstrap_ci(ci_results: list[dict]):
    if not HAS_MPL:
        return

    RAPORT_DIR.mkdir(exist_ok=True)

    labels    = [r["experiment"]              for r in ci_results]
    accs      = [r["accuracy"] * 100          for r in ci_results]
    lowers    = [r["accuracy"] * 100 - r["ci_lower"] * 100  for r in ci_results]
    uppers    = [r["ci_upper"] * 100 - r["accuracy"] * 100  for r in ci_results]

    # colour by model family
    colours = []
    for lbl in labels:
        if "claude" in lbl:
            colours.append("#2563eb")   # blue
        elif "gemini" in lbl:
            colours.append("#16a34a")   # green
        else:
            colours.append("#dc2626")   # red

    x = np.arange(len(labels))
    fig, ax = plt.subplots(figsize=(10, 5))

    bars = ax.bar(x, accs, color=colours, alpha=0.82, zorder=3)
    ax.errorbar(
        x, accs,
        yerr=[lowers, uppers],
        fmt="none",
        ecolor="black",
        elinewidth=1.5,
        capsize=5,
        capthick=1.5,
        zorder=4,
    )

    # value labels inside/above bars
    for bar, acc, lo_err, hi_err in zip(bars, accs, lowers, uppers):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            bar.get_height() + hi_err + 1.2,
            f"{acc:.1f}%",
            ha="center", va="bottom", fontsize=9, fontweight="bold",
        )

    ax.set_xticks(x)
    ax.set_xticklabels(labels, rotation=25, ha="right", fontsize=10)
    ax.set_ylabel("Accuracy (%)", fontsize=11)
    ax.set_ylim(0, 110)
    ax.set_title("Accuracy with Bootstrap 95% Confidence Intervals (n=10 000)", fontsize=12)
    ax.yaxis.grid(True, linestyle="--", alpha=0.5, zorder=0)
    ax.set_axisbelow(True)

    legend_patches = [
        mpatches.Patch(color="#2563eb", alpha=0.82, label="Claude Sonnet 4.6"),
        mpatches.Patch(color="#16a34a", alpha=0.82, label="Gemini 2.5 Flash"),
        mpatches.Patch(color="#dc2626", alpha=0.82, label="Llama 3.1 8B"),
    ]
    ax.legend(handles=legend_patches, loc="upper right", fontsize=9)

    plt.tight_layout()
    for ext in ("pdf", "png"):
        path = RAPORT_DIR / f"fig_bootstrap_ci.{ext}"
        plt.savefig(path, dpi=150, bbox_inches="tight")
        print(f"  Saved → {path}")
    plt.close()


def plot_threshold_sensitivity(correct_vals, partial_vals, grid, focus_exp: str):
    if not HAS_MPL:
        return

    RAPORT_DIR.mkdir(exist_ok=True)

    # Build matrix (rows = partial_min, cols = correct_min)
    matrix = np.full((len(partial_vals), len(correct_vals)), np.nan)
    for ci, cv in enumerate(correct_vals):
        for pi, pv in enumerate(partial_vals):
            val = grid.get((cv, pv), float("nan"))
            if not math.isnan(val):
                matrix[pi, ci] = val * 100

    fig, ax = plt.subplots(figsize=(9, 5))
    cmap = plt.cm.RdYlGn
    cmap.set_bad(color="#e5e7eb")

    im = ax.imshow(matrix, aspect="auto", cmap=cmap, vmin=50, vmax=100)
    plt.colorbar(im, ax=ax, label="Accuracy (%)")

    ax.set_xticks(range(len(correct_vals)))
    ax.set_xticklabels([f"{v:.2f}" for v in correct_vals], fontsize=10)
    ax.set_yticks(range(len(partial_vals)))
    ax.set_yticklabels([f"{v:.2f}" for v in partial_vals], fontsize=10)
    ax.set_xlabel("correct_min threshold", fontsize=11)
    ax.set_ylabel("partial_min threshold", fontsize=11)
    ax.set_title(
        f"Threshold Sensitivity — {focus_exp}\n"
        f"(grey cells: partial_min ≥ correct_min — invalid combination)",
        fontsize=11,
    )

    # Annotate cells with accuracy values
    for pi in range(len(partial_vals)):
        for ci in range(len(correct_vals)):
            val = matrix[pi, ci]
            if not np.isnan(val):
                ax.text(ci, pi, f"{val:.0f}%", ha="center", va="center",
                        fontsize=8, color="black", fontweight="bold")

    # Highlight the default threshold with a rectangle
    default_ci = correct_vals.index(DEFAULT_CORRECT_MIN)
    default_pi = partial_vals.index(DEFAULT_PARTIAL_MIN)
    rect = plt.Rectangle(
        (default_ci - 0.5, default_pi - 0.5), 1, 1,
        linewidth=2.5, edgecolor="#1d4ed8", facecolor="none",
    )
    ax.add_patch(rect)
    ax.text(
        default_ci, default_pi - 0.62,
        "default", ha="center", va="top", fontsize=7.5,
        color="#1d4ed8", fontweight="bold",
    )

    plt.tight_layout()
    for ext in ("pdf", "png"):
        path = RAPORT_DIR / f"fig_threshold_sens.{ext}"
        plt.savefig(path, dpi=150, bbox_inches="tight")
        print(f"  Saved → {path}")
    plt.close()


# ── LaTeX snippet ─────────────────────────────────────────────────────────────

def print_latex_table(ci_results: list[dict]):
    print("\n" + "=" * 64)
    print("  LaTeX TABLE SNIPPET (paste into main.tex)")
    print("=" * 64)
    print(r"""
\begin{table}[H]
    \centering
    \caption{Accuracy z bootstrapowym 95\% przedziałem ufności ($n=10\,000$ prób)}
    \label{tab:bootstrap_ci}
    \begin{tabular}{@{}lcccc@{}}
        \toprule
        \textbf{Eksperyment} & \textbf{Accuracy} & \textbf{CI dolny} & \textbf{CI górny} & \textbf{$\pm$half} \\
        \midrule""")
    for r in ci_results:
        print(f"        {r['experiment']:<20} & {r['accuracy']*100:.1f}\\% "
              f"& {r['ci_lower']*100:.1f}\\% & {r['ci_upper']*100:.1f}\\% "
              f"& $\\pm${r['ci_half']*100:.1f}\\% \\\\")
    print(r"""        \bottomrule
    \end{tabular}
\end{table}""")


# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    print("\nMockBean Bootstrap & Threshold Analysis")
    print("Running from:", Path.cwd())

    # ── Bootstrap CI ──────────────────────────────────────────────────────────
    ci_results = run_bootstrap_analysis()

    save_csv(
        ci_results,
        RESULTS_DIR / "bootstrap_ci.csv",
        fields=["experiment", "accuracy", "ci_lower", "ci_upper", "ci_half",
                "n_cases", "n_bootstrap"],
    )

    print_latex_table(ci_results)

    if HAS_MPL and ci_results:
        plot_bootstrap_ci(ci_results)

    # ── Threshold sensitivity ─────────────────────────────────────────────────
    focus = "claude · Pure"
    result = run_threshold_sensitivity(focus)
    if result:
        sens_results, correct_vals, partial_vals, grid = result

        save_csv(
            sens_results,
            RESULTS_DIR / "threshold_sensitivity.csv",
            fields=["experiment", "correct_min", "partial_min", "accuracy"],
        )

        if HAS_MPL:
            plot_threshold_sensitivity(correct_vals, partial_vals, grid, focus)

    print(f"\n{'=' * 64}")
    print("  Done.")
    print(f"{'=' * 64}\n")


if __name__ == "__main__":
    main()
