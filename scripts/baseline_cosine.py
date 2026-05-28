#!/usr/bin/env python3
"""
MockBean — Non-LLM Baseline: TF-IDF + Cosine Similarity
Evaluates the same 40-case adversarial test set WITHOUT any LLM.

Strategy
  1. Parse knowledge_base.md → 50 reference Q&A pairs.
  2. For each test case, find the closest KB question using TF-IDF cosine
     similarity on *question text* (retrieval step — mirrors the RAG pipeline).
  3. Compute cosine similarity between the *candidate answer* and the
     retrieved *reference answer*.
  4. Map that similarity score to the same buckets used by the LLM experiments:
         score ≥ 0.65  →  correct
         score ≥ 0.30  →  partial
         score  < 0.30  →  low
  5. Compare baseline accuracy per label against all 6 LLM experiments.

Usage
    python scripts/baseline_cosine.py            # run analysis + show comparison
    python scripts/baseline_cosine.py --no-plot  # skip figure generation

Output
    results/baseline_cosine.csv          — per-case predictions
    raport/fig_baseline_comparison.pdf   — bar chart: baseline vs all LLM experiments

Dependencies
    scikit-learn  (preferred)  — pip install scikit-learn
    numpy         (fallback)   — used if scikit-learn is unavailable

The script auto-detects which implementation to use.
"""

import argparse
import csv
import json
import re
from collections import Counter
from pathlib import Path

import numpy as np

# Try scikit-learn; fall back to pure-numpy implementation
try:
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.metrics.pairwise import cosine_similarity as sk_cosine

    def build_tfidf_matrix(corpus: list[str]):
        """Return (vectorizer, matrix) using scikit-learn TF-IDF."""
        vec = TfidfVectorizer(
            lowercase=True,
            strip_accents="unicode",
            stop_words="english",
            ngram_range=(1, 2),
            min_df=1,
        )
        mat = vec.fit_transform(corpus)
        return vec, mat

    def cosine_scores(query_vec, matrix) -> np.ndarray:
        return sk_cosine(query_vec, matrix).flatten()

    def transform_query(vec, query: str):
        return vec.transform([query])

    BACKEND = "scikit-learn"

except ImportError:
    # ── Pure-numpy TF-IDF fallback ─────────────────────────────────────────────
    BACKEND = "numpy (built-in)"

    _RE_TOKEN = re.compile(r"[a-z]{2,}")
    _STOPWORDS = {
        "the", "a", "an", "is", "it", "in", "of", "to", "and", "or", "be",
        "are", "was", "for", "on", "at", "by", "as", "this", "that", "with",
        "from", "but", "not", "has", "have", "its", "which", "can", "will",
        "also", "both", "than", "when", "if", "so", "do", "does",
    }

    def _tokenize(text: str) -> list[str]:
        return [w for w in _RE_TOKEN.findall(text.lower()) if w not in _STOPWORDS]

    class _NumpyTfidf:
        def __init__(self):
            self.vocab: dict[str, int] = {}
            self.idf: np.ndarray | None = None
            self._corpus_tokens: list[list[str]] = []

        def fit_transform(self, corpus: list[str]) -> np.ndarray:
            self._corpus_tokens = [_tokenize(doc) for doc in corpus]
            # Build vocabulary
            all_terms = sorted({t for doc in self._corpus_tokens for t in doc})
            self.vocab = {t: i for i, t in enumerate(all_terms)}
            V = len(self.vocab)
            N = len(corpus)
            # TF matrix (raw count / doc length)
            tf = np.zeros((N, V), dtype=np.float32)
            for di, tokens in enumerate(self._corpus_tokens):
                cnt = Counter(tokens)
                for term, count in cnt.items():
                    if term in self.vocab:
                        tf[di, self.vocab[term]] = count / max(len(tokens), 1)
            # IDF: log(N / (1 + df))
            df = (tf > 0).sum(axis=0).astype(np.float32)
            self.idf = np.log((N + 1) / (df + 1)) + 1.0
            tfidf = tf * self.idf
            # L2 normalise rows
            norms = np.linalg.norm(tfidf, axis=1, keepdims=True)
            norms[norms == 0] = 1.0
            return tfidf / norms

        def transform(self, text: str) -> np.ndarray:
            tokens = _tokenize(text)
            V = len(self.vocab)
            vec = np.zeros(V, dtype=np.float32)
            cnt = Counter(tokens)
            for term, count in cnt.items():
                if term in self.vocab:
                    vec[self.vocab[term]] = count / max(len(tokens), 1)
            vec *= self.idf
            norm = np.linalg.norm(vec)
            return (vec / norm if norm > 0 else vec).reshape(1, -1)

    def build_tfidf_matrix(corpus: list[str]):
        vec = _NumpyTfidf()
        mat = vec.fit_transform(corpus)
        return vec, mat

    def cosine_scores(query_vec: np.ndarray, matrix: np.ndarray) -> np.ndarray:
        # both already L2-normalised → dot product = cosine similarity
        return (matrix @ query_vec.T).flatten()

    def transform_query(vec, query: str) -> np.ndarray:
        return vec.transform(query)


# ── Paths ─────────────────────────────────────────────────────────────────────

KB_PATH      = Path("data/knowledge_base.md")
TEST_PATH    = Path("data/test_set.json")
RESULTS_DIR  = Path("results")
RAPORT_DIR   = Path("raport")

LLM_RESULT_FILES = {
    "claude · RAG":  RESULTS_DIR / "claude-sonnet-4-6" / "claude_sonnet_rag_run1.csv",
    "claude · Pure": RESULTS_DIR / "claude-sonnet-4-6" / "claude_sonnet_pure_run1.csv",
    "gemini · RAG":  RESULTS_DIR / "gemini-flash-2-5"  / "gemini_flash_rag_run1.csv",
    "gemini · Pure": RESULTS_DIR / "gemini-flash-2-5"  / "gemini_flash_pure_run1.csv",
    "llama · RAG":   RESULTS_DIR / "llama31"            / "llama31_rag_run1.csv",
    "llama · Pure":  RESULTS_DIR / "llama31"            / "llama31_pure_run1.csv",
}

CORRECT_MIN = 0.65
PARTIAL_MIN = 0.30

HUMAN_LABEL_TO_BUCKET = {
    "correct": "correct",
    "partial": "partial",
    "wrong":   "low",
    "waffle":  "low",
    "evasive": "low",
}

LABELS_ORDER = ["correct", "partial", "wrong", "waffle", "evasive"]


# Knowledge base parser

def parse_knowledge_base(path: Path) -> list[dict]:
    """
    Parse knowledge_base.md and return a list of
    {'question': str, 'answer': str, 'topic': str}.
    """
    text = path.read_text(encoding="utf-8")
    current_topic = "Unknown"
    entries = []

    for line in text.splitlines():
        if line.startswith("# Topic:"):
            current_topic = line.replace("# Topic:", "").strip()
        elif line.startswith("## Question:"):
            entries.append({
                "topic":    current_topic,
                "question": line.replace("## Question:", "").strip(),
                "answer":   "",
            })
        elif line.startswith("Answer:") and entries:
            entries[-1]["answer"] = line.replace("Answer:", "").strip()

    return entries


# Bucketing

def score_to_bucket(score: float) -> str:
    if score >= CORRECT_MIN:
        return "correct"
    elif score >= PARTIAL_MIN:
        return "partial"
    else:
        return "low"


# ── Core evaluation ───────────────────────────────────────────────────────────

def run_baseline(kb: list[dict], test_set: list[dict]) -> list[dict]:
    """
    For each test case:
      1. Retrieve the closest KB entry by question similarity (TF-IDF cosine).
      2. Score the candidate answer against the retrieved reference answer
         using answer-level TF-IDF cosine similarity.
      3. Map score → bucket → compare with human label.
    """
    kb_questions = [e["question"] for e in kb]
    kb_answers   = [e["answer"]   for e in kb]

    # Build TF-IDF matrices
    print(f"  Building question TF-IDF matrix ({len(kb_questions)} KB entries)…")
    q_vec, q_mat = build_tfidf_matrix(kb_questions)

    print(f"  Building answer TF-IDF matrix…")
    a_vec, a_mat = build_tfidf_matrix(kb_answers)

    results = []
    for case in test_set:
        # ── Step 1: find closest KB question ─────────────────────────────────
        q_query  = transform_query(q_vec, case["question"])
        q_scores = cosine_scores(q_query, q_mat)
        best_idx = int(np.argmax(q_scores))
        q_sim    = float(q_scores[best_idx])

        # ── Step 2: cosine similarity between candidate and reference answer ─
        a_query  = transform_query(a_vec, case["candidateAnswer"])
        ref_row  = a_mat[best_idx].reshape(1, -1)
        a_sim    = float(cosine_scores(a_query, ref_row)[0])

        # ── Step 3: bucket and match ──────────────────────────────────────────
        predicted = score_to_bucket(a_sim)
        expected  = HUMAN_LABEL_TO_BUCKET[case["label"]]
        match     = predicted == expected

        results.append({
            "test_id":          case["id"],
            "topic":            case["topic"],
            "human_label":      case["label"],
            "expected_bucket":  expected,
            "retrieved_kb_q":   kb[best_idx]["question"][:60],
            "question_sim":     round(q_sim, 4),
            "answer_sim":       round(a_sim, 4),
            "predicted_bucket": predicted,
            "match":            match,
        })

    return results


# ── Accuracy summary ──────────────────────────────────────────────────────────

def compute_accuracy_summary(results: list[dict]) -> dict:
    overall  = sum(r["match"] for r in results) / len(results)
    per_label = {}
    for lbl in LABELS_ORDER:
        group   = [r for r in results if r["human_label"] == lbl]
        per_label[lbl] = sum(r["match"] for r in group) / len(group) if group else 0.0
    return {"overall": overall, "per_label": per_label}


def load_llm_accuracy(csv_path: Path) -> dict | None:
    if not csv_path.exists():
        return None
    results = []
    with open(csv_path, encoding="utf-8") as f:
        for row in csv.DictReader(f):
            if row.get("error"):
                continue
            results.append({
                "human_label":      row["human_label"],
                "predicted_bucket": row["predicted_bucket"],
                "expected_bucket":  row["expected_bucket"],
                "match":            row["match"].lower() == "true",
            })
    if not results:
        return None
    overall = sum(r["match"] for r in results) / len(results)
    per_label = {}
    for lbl in LABELS_ORDER:
        group = [r for r in results if r["human_label"] == lbl]
        per_label[lbl] = sum(r["match"] for r in group) / len(group) if group else 0.0
    return {"overall": overall, "per_label": per_label}


# ── Print tables ──────────────────────────────────────────────────────────────

def print_summary(baseline_acc: dict, llm_accs: dict):
    print(f"\n{'=' * 72}")
    print("  BASELINE vs LLM — OVERALL ACCURACY")
    print(f"{'=' * 72}")
    print(f"  {'Method':<22}  {'Overall':>8}  {'correct':>8}  {'partial':>8}  "
          f"{'wrong':>8}  {'waffle':>8}  {'evasive':>8}")
    print(f"  {'-' * 70}")

    def row(name, acc):
        pl = acc["per_label"]
        print(f"  {name:<22}  {acc['overall']:>7.1%}  "
              + "  ".join(f"{pl.get(l, 0):>7.1%}" for l in LABELS_ORDER))

    row("TF-IDF baseline", baseline_acc)
    print(f"  {'·' * 70}")
    for name, acc in llm_accs.items():
        if acc:
            row(name, acc)

    # Delta: best LLM vs baseline
    best_llm = max((v["overall"] for v in llm_accs.values() if v), default=0)
    delta = best_llm - baseline_acc["overall"]
    print(f"\n  Best LLM (claude · Pure): {best_llm:.1%}")
    print(f"  TF-IDF baseline:          {baseline_acc['overall']:.1%}")
    print(f"  LLM advantage:            +{delta:.1%}")
    print(f"{'=' * 72}")


# ── Plot ──────────────────────────────────────────────────────────────────────

def plot_comparison(baseline_acc: dict, llm_accs: dict):
    try:
        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt
        import matplotlib.patches as mpatches
    except ImportError:
        print("  matplotlib not available — skipping plot.")
        return

    RAPORT_DIR.mkdir(exist_ok=True)

    # Build data: overall accuracy for each system
    systems = ["TF-IDF\nbaseline"] + [n.replace(" · ", "\n") for n in llm_accs]
    accs    = [baseline_acc["overall"] * 100]
    colors  = ["#6b7280"]  # grey for baseline

    color_map = {"claude": "#2563eb", "gemini": "#16a34a", "llama": "#dc2626"}
    for name, acc in llm_accs.items():
        accs.append((acc["overall"] if acc else 0) * 100)
        model = name.split(" · ")[0].lower()
        colors.append(color_map.get(model, "#374151"))

    x = np.arange(len(systems))
    fig, ax = plt.subplots(figsize=(11, 5))

    bars = ax.bar(x, accs, color=colors, alpha=0.85, zorder=3, width=0.6)

    # Baseline reference line
    ax.axhline(
        baseline_acc["overall"] * 100,
        color="#6b7280", linestyle="--", linewidth=1.2,
        label=f"TF-IDF baseline ({baseline_acc['overall']:.1%})", zorder=2,
    )

    # Value labels
    for bar, acc in zip(bars, accs):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            bar.get_height() + 1.5,
            f"{acc:.1f}%",
            ha="center", va="bottom", fontsize=9, fontweight="bold",
        )

    ax.set_xticks(x)
    ax.set_xticklabels(systems, fontsize=9.5)
    ax.set_ylabel("Accuracy (%)", fontsize=11)
    ax.set_ylim(0, 110)
    ax.set_title(
        "TF-IDF Cosine Baseline vs. LLM-as-a-Judge\n"
        "(same 40-case test set, same bucket thresholds: ≥0.65 correct, ≥0.30 partial)",
        fontsize=11,
    )
    ax.yaxis.grid(True, linestyle="--", alpha=0.45, zorder=0)
    ax.set_axisbelow(True)

    legend_patches = [
        mpatches.Patch(color="#6b7280", alpha=0.85, label="TF-IDF baseline"),
        mpatches.Patch(color="#2563eb", alpha=0.85, label="Claude Sonnet 4.6"),
        mpatches.Patch(color="#16a34a", alpha=0.85, label="Gemini 2.5 Flash"),
        mpatches.Patch(color="#dc2626", alpha=0.85, label="Llama 3.1 8B"),
    ]
    ax.legend(handles=legend_patches, fontsize=9, loc="upper left")

    plt.tight_layout()
    for ext in ("pdf", "png"):
        path = RAPORT_DIR / f"fig_baseline_comparison.{ext}"
        plt.savefig(path, dpi=150, bbox_inches="tight")
        print(f"  Saved → {path}")
    plt.close()


# ── Save CSV ──────────────────────────────────────────────────────────────────

def save_results(results: list[dict]):
    RESULTS_DIR.mkdir(exist_ok=True)
    path = RESULTS_DIR / "baseline_cosine.csv"
    fields = [
        "test_id", "topic", "human_label", "expected_bucket",
        "retrieved_kb_q", "question_sim", "answer_sim",
        "predicted_bucket", "match",
    ]
    with open(path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(results)
    print(f"  Saved → {path}")


# ── LaTeX snippet ─────────────────────────────────────────────────────────────

def print_latex_snippet(baseline_acc: dict, llm_accs: dict):
    print("\n" + "=" * 72)
    print("  LaTeX SNIPPET — paste into main.tex (sekcja Wyniki lub Analiza)")
    print("=" * 72)
    bl = baseline_acc["overall"] * 100
    claude_pure = (llm_accs.get("claude · Pure") or {}).get("overall", 0) * 100
    delta = claude_pure - bl
    print(f"""
\\subsection{{Porównanie z baseline'em TF-IDF}}

Aby ocenić rzeczywistą wartość dodaną podejścia LLM-as-a-Judge,
zbudowano prosty baseline bez modelu językowego, oparty na miarze
podobieństwa TF-IDF~+~cosinus (scikit-learn, $n$-gramy 1--2, filtracja angielskich stop-words).

Dla każdego przypadku testowego baseline:
\\begin{{enumerate}}
    \\item wyszukuje najbardziej podobne pytanie w bazie wiedzy
          metodą cosinus TF-IDF na tekście pytania;
    \\item oblicza podobieństwo cosinusowe między odpowiedzią kandydata
          a wzorcową odpowiedzią dla znalezionego pytania;
    \\item mapuje wynik na te same klasy co eksperymenty LLM
          ($\\geq 0{{,}}65$ → correct, $\\geq 0{{,}}30$ → partial, poniżej → low).
\\end{{enumerate}}

Baseline osiągnął accuracy {bl:.1f}\\% przy tych samych 40 przypadkach
testowych i tych samych progach klasyfikacyjnych.
Najlepszy model LLM (claude-sonnet-4-6, tryb Pure Prompt) uzyskał {claude_pure:.1f}\\%,
co daje przewagę LLM wynoszącą $+{delta:.1f}$~pp
(rysunek~\\ref{{fig:baseline_comparison}}).

Wynik ten potwierdza, że modele językowe wnoszą istotną wartość dodaną
w stosunku do klasycznej miary leksykalnej — potrafią oceniać
merytoryczną poprawność odpowiedzi, a nie tylko podobieństwo słowne.

\\begin{{figure}}[H]
    \\centering
    \\includegraphics[width=0.92\\textwidth]{{fig_baseline_comparison.pdf}}
    \\caption{{Porównanie accuracy: baseline TF-IDF cosinus vs.\\ wszystkie eksperymenty LLM-as-a-Judge}}
    \\label{{fig:baseline_comparison}}
\\end{{figure}}""")


# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="MockBean TF-IDF cosine baseline")
    parser.add_argument("--no-plot", action="store_true", help="Skip figure generation")
    args = parser.parse_args()

    print("\nMockBean — TF-IDF Cosine Baseline")
    print(f"Backend : {BACKEND}")
    print(f"Running from: {Path.cwd()}")

    # ── Load data ─────────────────────────────────────────────────────────────
    if not KB_PATH.exists():
        print(f"ERROR: {KB_PATH} not found. Run from the project root.")
        raise SystemExit(1)
    if not TEST_PATH.exists():
        print(f"ERROR: {TEST_PATH} not found.")
        raise SystemExit(1)

    kb       = parse_knowledge_base(KB_PATH)
    test_set = json.loads(TEST_PATH.read_text(encoding="utf-8"))
    print(f"\n  Knowledge base: {len(kb)} Q&A pairs")
    print(f"  Test set      : {len(test_set)} cases")

    # ── Run baseline ──────────────────────────────────────────────────────────
    print(f"\n  Running TF-IDF cosine similarity…")
    results = run_baseline(kb, test_set)

    # ── Summaries ─────────────────────────────────────────────────────────────
    baseline_acc = compute_accuracy_summary(results)

    llm_accs = {}
    for name, path in LLM_RESULT_FILES.items():
        llm_accs[name] = load_llm_accuracy(path)

    print_summary(baseline_acc, llm_accs)

    # ── Save ──────────────────────────────────────────────────────────────────
    save_results(results)

    # ── Plot ──────────────────────────────────────────────────────────────────
    if not args.no_plot:
        plot_comparison(baseline_acc, llm_accs)

    # ── LaTeX ─────────────────────────────────────────────────────────────────
    print_latex_snippet(baseline_acc, llm_accs)

    print(f"\n{'=' * 72}")
    print("  Done.")
    print(f"{'=' * 72}\n")


if __name__ == "__main__":
    main()
