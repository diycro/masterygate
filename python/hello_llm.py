"""
============================================================================
 MODULE 0 EXERCISE — "Hello LLM" in Python
============================================================================
 Goal: make one LLM call and understand it, then COMPARE with the Java
 (Spring AI) version in the backend. Keep it tiny.

 Steps:
   1. python -m venv .venv && source .venv/bin/activate
   2. pip install -r requirements.txt
   3. export OPENAI_API_KEY=sk-...      (or your provider's key)
   4. python hello_llm.py

 Then be ready for your tutor's Module 0 gate:
   - What is a token? Estimate this call's token usage and rough cost.
   - Why can the SAME prompt return different text run-to-run?
   - When would you reach for Python vs Java (Spring AI) for LLM work?
============================================================================
"""
import os


def main():
    api_key = os.environ.get("OPENAI_API_KEY")
    if not api_key:
        print("Set OPENAI_API_KEY first (see the steps in this file's docstring).")
        return

    # TODO(Module 0): create the client, send a one-line prompt, and print:
    #   - the model's reply text
    #   - prompt_tokens / completion_tokens / total_tokens from the response
    #
    # Example shape (OpenAI SDK):
    #   from openai import OpenAI
    #   client = OpenAI()
    #   resp = client.chat.completions.create(
    #       model="gpt-4o-mini",
    #       messages=[{"role": "user", "content": "Say hello in one short sentence."}],
    #   )
    #   print(resp.choices[0].message.content)
    #   print(resp.usage)
    print("TODO: implement the LLM call (Module 0 exercise).")


if __name__ == "__main__":
    main()
