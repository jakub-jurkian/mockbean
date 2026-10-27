#!/usr/bin/env python3
"""
MockBean Evaluation Harness
============================
Replays the adversarial test set against the MockBean API and records results.

Usage:
    python scripts/run_evaluation.py
    python scripts/run_evaluation.py --model gemini-flash --mode rag
    python scripts/run_evaluation.py --model gemini-flash --mode pure
    python scripts/run_evaluation.py --model llama3.1 --experiment rag_top1
    python scripts/run_evaluation.py --help

Modes:
    rag   (default) — retrieves a reference answer from pgvector before judging
    pure            — skips retrieval; LLM judges from its own knowledge only
"""

import json
import time
import csv
import argparse
from datetime import datetime
from pathlib import Path

import requests

# ── Configuration ─────────────────────────────────────────────────────────────

DEFAULT_BASE_URL = "http://localhost:8080"
TEST_SET_PATH   = Path("data/test_set.json")
RESULTS_DIR     = Path("results")

# Score → label bucket mapping.
# The LLM returns 0.0–1.0; we map it to the same 3 buckets as the human labels.
# Tweak these thresholds if the model systematically scores too high/low.
THRESHOLDS = {
    "correct_min":  0.65,   # score >= 0.65  → predicted "correct"
    "partial_min":  0.30,   # score >= 0.30  → predicted "partial"
                            # score <  0.30  → predicted "low" (wrong/waffle/evasive)
}

HUMAN_LABEL_TO_BUCKET = {
    "correct":  "correct",
    "partial":  "partial",
    "wrong":    "low",
    "waffle":   "low",
    "evasive":  "low",
}

# ── Helpers ───────────────────────────────────────────────────────────────────

def score_to_bucket(score: float) -> str:
    if score >= THRESHOLDS["correct_min"]:
        return "correct"
    elif score >= THRESHOLDS["partial_min"]:
        return "partial"
    else:
        return "low"


def check_api_health(base_url: str) -> bool:
    try:
        r = requests.get(f"{base_url}/actuator/health", timeout=5)
        status = r.json().get("status", "UNKNOWN")
        print(f"API health: {status}")
        return status == "UP"
    except Exception as e:
        print(f"WARNING: Cannot reach API ({e}). Is the Spring Boot app running?")
        return False


def load_test_set(path: Path) -> list:
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    print(f"Loaded {len(data)} test cases from {path}")
    return data

# ── Core loop ─────────────────────────────────────────────────────────────────

def run_evaluation(test_set: list, experiment_id: str, model: str, mode: str = "rag",
                   base_url: str = DEFAULT_BASE_URL, delay: float = 0.3) -> list:
    results = []

    print(f"\n{'=' * 62}")
    print(f"  Experiment : {experiment_id}")
    print(f"  Model      : {model}")
    print(f"  Mode       : {mode.upper()}")
    print(f"  Test cases : {len(test_set)}")
    print(f"{'=' * 62}")
    print(f"  {'#':<4} {'id':<4} {'label':<10} {'score':<7} {'latency':>8}  match")
    print(f"  {'-' * 52}")

    for i, case in enumerate(test_set, 1):
        payload = {
            "question":   case["question"],
            "userAnswer": case["candidateAnswer"],
            "mode":       mode,
        }

        start = time.monotonic()
        error_msg = ""
        llm_score = -1.0
        feedback = strengths = missed = ""
        effective_mode = judge_model = retrieved_reference = ""
        retrieval_score = ""

        try:
            response = requests.post(f"{base_url}/api/v1/evaluations", json=payload, timeout=120)
            latency_ms = int((time.monotonic() - start) * 1000)
            response.raise_for_status()
            body = response.json()

            llm_score  = body.get("score", -1.0)
            feedback   = body.get("feedback", "")
            strengths  = "; ".join(body.get("strengths", []))
            missed     = "; ".join(body.get("missedConcepts", []))
            # What the judge actually saw - present since the Q&A-chunking change
            effective_mode      = body.get("mode") or ""
            judge_model         = body.get("judgeModel") or ""
            retrieval_score     = body.get("retrievalScore") if body.get("retrievalScore") is not None else ""
            retrieved_reference = body.get("retrievedReference") or ""

        except Exception as e:
            latency_ms = int((time.monotonic() - start) * 1000)
            error_msg  = str(e)

        predicted = score_to_bucket(llm_score) if llm_score >= 0 else "error"
        expected  = HUMAN_LABEL_TO_BUCKET[case["label"]]
        match     = predicted == expected and not error_msg

        marker = "✓" if match else ("!" if error_msg else "✗")
        score_str = f"{llm_score:.2f}" if llm_score >= 0 else "err "
        print(f"  [{i:02d}] {case['id']:<4} {case['label']:<10} {score_str:<7} {latency_ms:>7}ms  {marker}")

        results.append({
            "experiment_id":   experiment_id,
            "model":           model,
            "mode":            mode,
            "effective_mode":  effective_mode,
            "judge_model":     judge_model,
            "test_id":         case["id"],
            "topic":           case["topic"],
            "question":        case["question"],
            "candidate_answer": case["candidateAnswer"],
            "human_label":     case["label"],
            "expected_bucket": expected,
            "llm_score":       llm_score,
            "predicted_bucket": predicted,
            "match":           match,
            "feedback":        feedback,
            "strengths":       strengths,
            "missed_concepts": missed,
            "retrieval_score": retrieval_score,
            "retrieved_reference": retrieved_reference,
            "latency_ms":      latency_ms,
            "timestamp":       datetime.utcnow().isoformat(),
            "error":           error_msg,
        })

        time.sleep(delay)   # pause between calls - raise it for rate-limited free API tiers

    return results

# ── Summary ───────────────────────────────────────────────────────────────────

def print_summary(results: list):
    errors  = [r for r in results if r["error"]]
    valid   = [r for r in results if not r["error"]]
    matches = [r for r in valid if r["match"]]

    accuracy    = len(matches) / len(valid) if valid else 0
    avg_latency = sum(r["latency_ms"] for r in valid) / len(valid) if valid else 0
    p95_latency = sorted(r["latency_ms"] for r in valid)[int(len(valid) * 0.95) - 1] if valid else 0

    print(f"\n{'=' * 62}")
    print("  RESULTS SUMMARY")
    print(f"{'=' * 62}")
    print(f"  Total cases  : {len(results)}")
    print(f"  Errors       : {len(errors)}")
    print(f"  Accuracy     : {accuracy:.1%}  ({len(matches)}/{len(valid)})")
    print(f"  Latency avg  : {avg_latency:.0f} ms")
    print(f"  Latency p95  : {p95_latency:.0f} ms")

    print(f"\n  Per-label accuracy:")
    print(f"  {'Label':<12} {'Cases':>6} {'Match':>6} {'Acc':>8}")
    print(f"  {'-' * 36}")

    for label in ["correct", "partial", "wrong", "waffle", "evasive"]:
        group   = [r for r in valid if r["human_label"] == label]
        correct = [r for r in group if r["match"]]
        acc     = len(correct) / len(group) if group else 0
        print(f"  {label:<12} {len(group):>6} {len(correct):>6} {acc:>7.1%}")

    # Gullibility check: waffle/evasive cases the model scored >= 0.5
    fooled = [r for r in valid
              if r["human_label"] in ("waffle", "evasive")
              and r["llm_score"] >= 0.5]
    if fooled:
        print(f"\n  ⚠ Gullibility: model scored ≥0.5 on {len(fooled)} waffle/evasive answers")
        for r in fooled:
            print(f"    id={r['test_id']}  score={r['llm_score']:.2f}  label={r['human_label']}")

    print(f"{'=' * 62}\n")

# ── Save ─────────────────────────────────────────────────────────────────────

def save_results(results: list, experiment_id: str, out_dir: Path = RESULTS_DIR) -> Path:
    out_dir.mkdir(parents=True, exist_ok=True)
    out = out_dir / f"{experiment_id}.csv"

    fields = [
        "experiment_id", "model", "mode", "effective_mode", "judge_model",
        "test_id", "topic", "question",
        "candidate_answer", "human_label", "expected_bucket",
        "llm_score", "predicted_bucket", "match",
        "feedback", "strengths", "missed_concepts",
        "retrieval_score", "retrieved_reference",
        "latency_ms", "timestamp", "error",
    ]
    with open(out, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(results)

    print(f"  Results saved → {out}")
    return out

# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="MockBean Evaluation Harness")
    parser.add_argument(
        "--model", default="llama3.1",
        help="Model name tag written into results (default: llama3.1)"
    )
    parser.add_argument(
        "--experiment", default=None,
        help="Experiment ID prefix (default: <model>__<timestamp>)"
    )
    parser.add_argument(
        "--mode", default="rag", choices=["rag", "pure"],
        help="Evaluation mode: 'rag' (default) uses vector retrieval; 'pure' skips it"
    )
    parser.add_argument(
        "--test-set", default=str(TEST_SET_PATH),
        help=f"Path to test set JSON (default: {TEST_SET_PATH})"
    )
    parser.add_argument(
        "--base-url", default=DEFAULT_BASE_URL,
        help=f"MockBean API base URL (default: {DEFAULT_BASE_URL})"
    )
    parser.add_argument(
        "--out-dir", default=str(RESULTS_DIR),
        help=f"Directory for the results CSV (default: {RESULTS_DIR})"
    )
    parser.add_argument(
        "--delay", type=float, default=0.3,
        help="Seconds to wait between requests (default: 0.3; e.g. 7 for Gemini's free tier)"
    )
    args = parser.parse_args()

    experiment_id = args.experiment or f"{args.model}_{args.mode}_{datetime.now().strftime('%Y%m%d_%H%M%S')}"

    test_set_path = Path(args.test_set)
    if not test_set_path.exists():
        print(f"ERROR: test set not found at '{test_set_path}'")
        raise SystemExit(1)

    check_api_health(args.base_url)
    test_set = load_test_set(test_set_path)

    results = run_evaluation(test_set, experiment_id, args.model, args.mode, args.base_url, args.delay)
    print_summary(results)
    save_results(results, experiment_id, Path(args.out_dir))


if __name__ == "__main__":
    main()
