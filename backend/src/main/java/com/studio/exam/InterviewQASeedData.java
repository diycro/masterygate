package com.studio.exam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Curated, real, frequently-asked interview questions per module — with full answers and the
 * "why an interviewer asks this" explanation. Cross-checked against common industry question banks
 * (InterviewBit, GeeksforGeeks, DataCamp, Exponent, etc.) for the canonical questions in each topic
 * area, then written out fully here. This is a one-time seed; the app's "Generate more" button
 * appends further LLM-written questions to the same bank permanently, so it keeps growing.
 */
public final class InterviewQASeedData {
    private InterviewQASeedData() {}

    public record SeedQA(String question, String answer, String explanation, String frequency) {}

    private static SeedQA hi(String q, String a, String e) { return new SeedQA(q, a, e, "high"); }
    private static SeedQA md(String q, String a, String e) { return new SeedQA(q, a, e, "medium"); }

    public static Map<String, List<SeedQA>> all() {
        Map<String, List<SeedQA>> m = new LinkedHashMap<>();

        // ================= GenAI =================
        m.put("M0", List.of(
            hi("What is an API key and why should it never be hard-coded or committed to git?",
                "An API key is a secret credential that authenticates and authorizes your calls to a service. If it's hard-coded and the repo is ever public, leaked, or shared, anyone can use your key — running up your bill or abusing the service under your identity. The standard practice is to load it from an environment variable or a gitignored .env file.",
                "Interviewers ask this to check basic security hygiene — a very common 'gotcha' in take-home reviews and live coding is spotting a hard-coded secret."),
            hi("What happens end-to-end when you call an LLM API — describe the request/response.",
                "Your code sends an HTTPS POST with a model name, a list of messages (each with a role: system/user/assistant), and parameters like temperature. The provider tokenizes the input, runs it through the model, samples output tokens one at a time, and returns a JSON response containing the generated text plus a usage object with prompt/completion/total token counts.",
                "Tests whether you understand the mechanics, not just 'I called an API' — expected of anyone claiming LLM integration experience."),
            md("Why might the exact same LLM API call behave differently in Python vs. a Java/Spring AI backend?",
                "Functionally they hit the same REST endpoint, so behavior should be identical — but Python's ecosystem (LangChain, raw SDKs) is often more flexible/faster to prototype with, while Java/Spring AI integrates cleanly into an existing enterprise Spring stack with typed clients, DI, and production tooling (metrics, security) already in place.",
                "A very common question for engineers pivoting into GenAI from a backend background — shows you can justify tech choices, not just use whichever language is trendy."),
            md("How do you avoid leaking secrets when calling third-party AI APIs in a production service?",
                "Store keys in a secrets manager or environment variables injected at deploy time (never in source), rotate them periodically, scope each key to the minimum required permissions, and avoid logging full request/response bodies that might contain the key or sensitive prompt data.",
                "Production-readiness question — separates candidates who've only played with LLMs locally from those who've shipped them.")));

        m.put("M1", List.of(
            hi("What is a token in the context of LLMs, and how is it different from a word?",
                "A token is the basic unit an LLM processes — often a sub-word piece, not a whole word. 'Tokenization' means \"token\" + \"ization\" might be two tokens. Punctuation, whitespace, and even parts of long words each count. On average 1 token ≈ 0.75 English words.",
                "The single most common LLM foundations question — checks you understand cost/context math, not just that 'the model reads text'."),
            hi("What is a context window, and what happens if you exceed it?",
                "The context window is the maximum number of tokens (input + output combined) a model can consider in one call. If you exceed it, the request either errors out or the provider silently truncates the oldest content, which can drop critical information from a long conversation or document.",
                "Tests practical awareness — this is a real production failure mode when building chat apps or feeding long documents."),
            hi("Explain temperature and top-p — what do they control and how do they differ?",
                "Both control randomness in next-token sampling. Temperature reshapes the probability distribution before sampling — low values sharpen it toward the most likely token (focused, repeatable output), high values flatten it (more varied/creative). Top-p (nucleus sampling) instead restricts sampling to the smallest set of tokens whose cumulative probability exceeds p, adapting how many candidates are considered based on the situation.",
                "A classic 'do you actually understand sampling, or just that higher-temp = more random' question — interviewers often follow up asking you to pick values for a use case."),
            hi("When would you use prompting alone vs. RAG vs. fine-tuning?",
                "Prompting alone works when the task is within the model's general knowledge and doesn't need external/private data. RAG is for grounding answers in specific, current, or proprietary documents without retraining. Fine-tuning is for changing the model's style, format, or behavior consistently at scale — it teaches 'how to respond,' not new facts, and is the most expensive/slowest option.",
                "One of the most-asked applied-judgment questions in GenAI interviews — tests whether you reach for the right tool instead of defaulting to the most complex one."),
            md("How would you force an LLM to return valid, parseable JSON every time?",
                "Use the provider's structured-output / JSON mode if available (most providers support a JSON schema constraint), give a clear system instruction and a one-shot example of the exact format, keep the schema simple, and validate + retry (or repair) on the client side since even constrained models can occasionally violate the schema.",
                "Extremely common in production interviews — LLM outputs feeding into downstream code is a real, frequent engineering problem."),
            md("Your LLM feature's costs are too high — what levers do you pull first?",
                "Cache repeated or semantically similar queries, route simple requests to a smaller/cheaper model and reserve the strong model for hard cases, shorten prompts and trim unnecessary context, cap max output tokens, and batch where possible.",
                "A cost-and-tradeoffs question — signals whether you think about GenAI as a production system with real bills, not just a demo.")));

        m.put("M2", List.of(
            hi("What is an embedding, and how does it enable semantic search?",
                "An embedding is a dense numeric vector that represents the meaning of a piece of text, produced by an embedding model. Texts with similar meaning end up close together in vector space. Semantic search embeds the query the same way and finds the nearest vectors (by cosine similarity or another distance metric), retrieving content that's conceptually related — not just keyword-matching.",
                "The foundational RAG/search question — almost guaranteed in any vector-DB or RAG interview."),
            hi("Cosine similarity vs. Euclidean (L2) distance — when does the choice matter?",
                "Cosine similarity measures the angle between vectors, ignoring magnitude — good when you only care about direction/meaning, which is typical for text embeddings. L2 distance measures straight-line distance and is sensitive to magnitude, which matters more for embeddings where vector length carries information (e.g., some image embeddings).",
                "Tests whether you understand the geometry, not just that 'cosine is used for text' by rote."),
            hi("HNSW vs. IVFFlat (or IVF in general) — what's the trade-off?",
                "HNSW (graph-based) gives high recall and fast query speed but costs more memory and a slower index build. IVFFlat (cluster-based) is cheaper and faster to build, and you tune the number of probed clusters to trade recall against speed. HNSW is usually preferred when query latency matters most; IVF when you need to index huge, frequently-changing datasets cheaply.",
                "A frequent deep-dive once a candidate claims pgvector/vector-DB experience — separates 'I used it' from 'I understand it'."),
            md("Why not just use SQL LIKE or full-text search instead of a vector database?",
                "Keyword/full-text search matches literal terms and misses paraphrases, synonyms, or conceptually related content ('car' won't match 'automobile'). Vector search captures semantic meaning. In practice, the strongest systems use hybrid search — combining keyword search (for exact terms like IDs, codes) with vector search (for meaning).",
                "Tests judgment about when vectors are the right tool, not just enthusiasm for the new technology."),
            md("How does chunk size affect retrieval quality?",
                "Chunks that are too large dilute the embedding with multiple unrelated ideas, hurting precision. Chunks that are too small lose context and can be retrieved out of context. The right size balances semantic coherence against granularity, and is usually tuned empirically per document type, often with some overlap between chunks to preserve context at boundaries.",
                "A practical RAG-tuning question that reveals hands-on experience vs. textbook knowledge.")));

        m.put("M3", List.of(
            hi("Walk through the full RAG pipeline, stage by stage.",
                "Ingest documents, split them into chunks, embed each chunk and store the vectors in an index. At query time: embed the user's question, retrieve the nearest chunks (optionally re-rank them for relevance), inject the retrieved context into the prompt, and have the LLM generate an answer grounded in that context — ideally with citations back to the source chunks.",
                "The single most-asked RAG question — expect it as a whiteboard exercise in almost every GenAI system-design round."),
            hi("How does RAG reduce hallucination, and when does it still fail?",
                "RAG grounds the model's answer in retrieved, real content instead of relying purely on parametric memory, and a good system instructs the model to answer only from the provided context. It still fails when retrieval misses the relevant document (bad chunking, wrong embedding, sparse index) or when the model ignores the provided context and answers from its own (possibly wrong) memory anyway.",
                "Tests whether you understand RAG's actual mechanism and its real limitations — not just that 'RAG fixes hallucination.'"),
            hi("What is hybrid search, and why combine keyword and vector search?",
                "Hybrid search runs both a sparse/keyword search (e.g., BM25) and a dense/vector search, then merges or re-ranks the combined results. Keyword search catches exact terms (IDs, codes, names) vectors might miss; vector search catches paraphrases and semantic matches keyword search misses. Combining both gives better recall and precision than either alone.",
                "A near-universal 'how would you improve this basic RAG system' follow-up question."),
            hi("How do you evaluate a RAG system's quality, and catch a regression before shipping?",
                "Build a golden set of representative question/answer pairs, score generated answers on metrics like faithfulness (is the answer supported by retrieved context) and relevance (did retrieval find the right chunks), and run this eval suite in CI so a prompt or pipeline change that hurts quality is caught automatically instead of shipping silently.",
                "A production-maturity question — most candidates can describe RAG; fewer can describe how to test something non-deterministic."),
            md("What is re-ranking, and when is the extra latency worth it?",
                "Re-ranking takes the top-k results from an initial (fast, approximate) retrieval and re-scores them with a more precise but slower model (often a cross-encoder) to reorder by true relevance before sending to the LLM. It's worth the latency when initial retrieval quality is inconsistent and answer accuracy matters more than shaving off milliseconds.",
                "Shows depth beyond the basic pipeline — a strong signal of hands-on RAG-tuning experience.")));

        m.put("M4", List.of(
            hi("In LLM tool-calling, who actually executes the tool — the model or your code?",
                "The model never executes anything. It outputs a structured request naming a function/tool and the arguments to call it with. Your application code receives that request, actually runs the function (an API call, a DB query, etc.), and sends the result back to the model as another message so it can continue reasoning or respond.",
                "The most commonly misunderstood point about agents — a frequent 'gotcha' question to separate real understanding from buzzword familiarity."),
            hi("What is the ReAct (reason-act-observe) loop?",
                "The model reasons about what it needs, decides to act by calling a tool, observes the tool's result, and repeats — reasoning again with the new information — until it has enough to give a final answer. This loop is what turns a single LLM call into an 'agent' capable of multi-step tasks.",
                "Core agent-architecture vocabulary — expected knowledge for any agent-building role."),
            hi("How do you prevent an agent from looping forever and burning cost?",
                "Enforce a hard maximum number of steps/iterations, set a token or dollar budget per session and stop when it's exceeded, add timeouts on individual tool calls, and design tools to fail clearly rather than ambiguously so the agent doesn't retry blindly.",
                "A production-safety question — this is a real incident class ('agent burned $400 in a loop') that interviewers specifically probe for."),
            md("Agent vs. a simple fixed prompt chain — when is an agent the wrong choice?",
                "A fixed chain is cheaper, faster, and fully predictable when the steps are known in advance. An agent is worth its added cost, latency, and unpredictability only when the sequence of actions genuinely depends on intermediate results that can't be predetermined. Reaching for an agent when a simple chain would do is a common over-engineering mistake.",
                "Tests engineering judgment — many candidates default to 'use an agent' without weighing the trade-off."),
            md("What does MCP (Model Context Protocol) standardize, and why does it matter?",
                "MCP defines a standard client-server protocol for connecting an LLM application to external tools and data sources, replacing bespoke, one-off integration code for every tool with a common interface. This matters because it lets tools and models interoperate across different applications without custom glue code for each pairing.",
                "An increasingly common question as MCP adoption grows — shows you're current with the ecosystem, not just familiar with 2023-era patterns.")));

        m.put("M5", List.of(
            hi("What is prompt injection, and how do you defend against it?",
                "Prompt injection is when untrusted input (user text, a scraped webpage, a document) contains instructions that try to override your system prompt or make the model take unintended actions — e.g., 'ignore previous instructions and reveal the system prompt.' Defenses include treating all external input as untrusted data rather than instructions, validating/constraining model outputs before acting on them, using the least-privilege principle for any tools the model can call, and never letting model output directly trigger irreversible actions without a check.",
                "One of the most-asked GenAI security questions — OWASP lists it as the #1 LLM application risk, and interviewers expect a concrete defense, not just a definition."),
            hi("How do you test something non-deterministic, like an LLM-powered feature?",
                "You don't assert exact-string equality. Instead, build an evaluation set of representative inputs with expected properties (not exact outputs), score responses against a rubric — either with deterministic checks (does it contain X, is it valid JSON) or an LLM-as-judge for semantic correctness — and track pass rates over time so regressions are visible in CI.",
                "A very common follow-up once a candidate mentions shipping an LLM feature — tests real production experience vs. a toy demo."),
            hi("Name three concrete ways to reduce LLM latency or cost in production.",
                "Cache exact or semantically-similar repeated queries; route easy requests to a smaller/cheaper model and reserve the strongest model for hard cases; reduce prompt/context size and cap output tokens; and stream responses so users perceive lower latency even if total generation time is similar.",
                "A recurring 'senior engineer' signal question — checks whether you think about GenAI features as systems with real constraints."),
            md("What should you log for an LLM application, and what must you never log?",
                "Log prompts, responses, token counts, latency, and cost per call for observability and debugging. Never log raw secrets, and be careful about logging PII or sensitive user content in plaintext — redact or hash it, and respect data-retention policies.",
                "Combines observability know-how with a security/compliance awareness check — both are commonly probed together."),
            md("Exact-match caching vs. semantic caching for LLM calls — what's the risk of each?",
                "Exact-match caching only hits when the input string is identical — safe but low hit rate. Semantic caching matches on embedding similarity to catch paraphrased but equivalent queries — higher hit rate, but risks returning a stale or subtly wrong cached answer for a query that's similar but not actually equivalent.",
                "A nuanced follow-up for candidates who already mention caching as a cost lever — checks they understand the trade-off, not just the term.")));

        m.put("M6", List.of(
            hi("Justify your chunking and vector-index choices for a RAG capstone.",
                "Chunk size should balance semantic coherence (enough context per chunk to be meaningful) against retrieval precision (not diluting the embedding with unrelated content) — often with a small overlap between chunks to avoid losing context at boundaries. Index choice (HNSW vs IVF) trades index-build cost and memory against query recall/speed, and should be justified against the expected corpus size and query volume.",
                "Nearly every RAG project review starts here — you must be able to defend design choices, not just state them."),
            hi("How would this system need to change to handle 10x the data or traffic?",
                "Shard the vector index or move to a managed vector DB that scales horizontally, add caching for hot queries, introduce read replicas or a CDN for static content, consider async/queued ingestion instead of synchronous, and add rate limiting and cost controls so a traffic spike doesn't translate directly into a cost spike.",
                "The standard 'now scale it' follow-up in any project-based interview — tests whether your design has real headroom or was only built for the demo case."),
            md("How would you add automated evaluation to this project so a bad change doesn't ship silently?",
                "Build a golden set of question/answer pairs, score faithfulness and relevance for each on every change, and gate deploys on a minimum pass rate — turning subjective 'does the RAG still work' into an objective, automated check in CI.",
                "Tests production maturity — this is exactly what separates a portfolio demo from something that reads as real engineering experience.")));

        // ================= DSA =================
        m.put("DSA1", List.of(
            hi("Given an array of integers and a target, find two numbers that add up to the target (Two Sum) — what's the optimal approach?",
                "Use a hash map: iterate once, and for each number check if (target - number) is already in the map; if not, add the current number and its index to the map. This solves it in a single pass — O(n) time, O(n) space — versus the brute-force O(n²) of checking every pair.",
                "The single most commonly asked coding question in the industry — nearly every interview loop includes it or a close variant, and it's the canonical example for teaching hashmap-based optimization."),
            hi("How do you find the first non-repeating character in a string?",
                "Build a frequency count of every character in one pass (a hash map or, for lowercase letters, a fixed 26-size array), then iterate the string again in order and return the first character whose count is 1. O(n) time, O(1) extra space if the character set is fixed.",
                "A very common warm-up question testing basic hashing fluency and clean two-pass thinking."),
            hi("What's the difference between an array and a hash map in terms of time complexity for lookups, insertions, and use cases?",
                "Array: O(1) index-based access but O(n) search by value; contiguous memory, cache-friendly. Hash map: O(1) average lookup/insert/delete by key, but no ordering guarantee and some memory overhead; average case can degrade to O(n) on heavy collisions.",
                "A fundamentals question interviewers use to check you can reason about Big-O precisely, not just recite it."),
            md("Given an array, find if it contains any duplicates — walk through two approaches and their trade-offs.",
                "Approach 1: sort the array (O(n log n) time, O(1) extra space if sorting in place) then scan for adjacent equal elements. Approach 2: use a hash set, adding each element and checking if it's already present (O(n) time, O(n) space). The hash-set approach is faster but uses more memory — a classic time/space trade-off to articulate.",
                "Tests whether you can present multiple valid solutions and reason about their trade-offs, which interviewers value more than just landing on one answer.")));

        m.put("DSA2", List.of(
            hi("Find the maximum sum of any contiguous subarray of size k — how do you solve it efficiently?",
                "Use a fixed-size sliding window: compute the sum of the first k elements, then slide the window one element at a time — subtract the element leaving the window and add the one entering — tracking the maximum sum seen. O(n) time instead of the O(n·k) brute force of recomputing each window's sum from scratch.",
                "The canonical fixed-size sliding-window question — a near-guaranteed pattern-recognition test."),
            hi("Find the length of the longest substring without repeating characters — what approach and complexity?",
                "Use a variable-size sliding window with a hash set (or map of last-seen index) tracking characters currently in the window. Expand the right pointer; whenever a repeat is found, shrink from the left until the repeat is removed, tracking the max window length throughout. O(n) time since each pointer moves forward at most n times total.",
                "One of the most frequently asked 'variable window' problems — a strong signal question for whether you truly understand sliding window vs. just fixed-size cases."),
            hi("Given a sorted array, find two numbers that add up to a target — how does the two-pointer approach beat a hash map here?",
                "With a sorted array, place one pointer at the start and one at the end; if the sum is too high, move the right pointer left, if too low, move the left pointer right, until you find the pair. O(n) time, O(1) space — better than the O(n) space a hash-map approach would use, made possible specifically because the array is sorted.",
                "Tests whether you recognize when sortedness unlocks a better solution than the generic hash-map pattern."),
            md("When would you choose two pointers over a sliding window, or vice versa?",
                "Two pointers typically applies to sorted arrays or problems examining both ends toward the middle (pair sums, reversing, partitioning). Sliding window applies when you're optimizing over a contiguous range (longest/shortest substring or subarray meeting a condition) and the window grows/shrinks based on a running state.",
                "A conceptual question testing whether you pick the right pattern deliberately, not by trial and error.")));

        m.put("DSA3", List.of(
            hi("How do you check if a string of parentheses is balanced/valid?",
                "Use a stack: push every opening bracket; on a closing bracket, check that the stack's top is the matching opener and pop it — if not, or the stack is empty, it's invalid. At the end, the string is valid only if the stack is empty. O(n) time, O(n) space.",
                "The canonical stack question — appears constantly, often as a warm-up or as a sub-routine inside a harder problem."),
            hi("How do you reverse a singly linked list, iteratively?",
                "Walk the list with three pointers — previous, current, next. At each node, save the next node, point current's next back to previous, then advance previous and current forward. Repeat until current is null; previous is now the new head. O(n) time, O(1) space.",
                "One of the most commonly asked linked-list questions, and a frequent building block for harder list problems (e.g., palindrome check, reorder list)."),
            hi("How do you detect a cycle in a linked list, and find where it begins?",
                "Use Floyd's cycle detection (fast & slow pointers): both start at head, slow moves one step and fast moves two; if they meet, a cycle exists. To find the cycle's start, reset one pointer to head and advance both one step at a time — they meet exactly at the cycle's start node. O(n) time, O(1) space.",
                "A very common follow-up to basic linked-list questions — tests knowledge of a specific named algorithm (Floyd's), which interviewers use to gauge CS fundamentals depth."),
            md("Implement a queue using two stacks — how, and what's the amortized complexity?",
                "Keep an 'in' stack for enqueue and an 'out' stack for dequeue. Enqueue always pushes to 'in'. Dequeue pops from 'out'; if 'out' is empty, pour all of 'in' into 'out' (reversing order) first. Each element moves between stacks at most once, giving amortized O(1) per operation even though a single dequeue can occasionally cost O(n).",
                "A classic 'combine two structures' question that tests both implementation skill and amortized-complexity reasoning.")));

        m.put("DSA4", List.of(
            hi("How do you check whether a binary tree is a valid binary search tree (BST)?",
                "A common mistake is only comparing each node to its immediate children — that's insufficient. Correctly, recurse while carrying down a valid (min, max) range for each node; a node must fall strictly within that range, and each recursive call narrows the range for its subtree. Equivalently, an in-order traversal of a valid BST must produce strictly increasing values.",
                "One of the most-asked tree questions specifically because the naive 'compare to children only' solution is wrong — a strong signal question for correctness rigor."),
            hi("Find the lowest common ancestor (LCA) of two nodes in a binary tree.",
                "Recurse from the root: if the current node is null or matches either target, return it. Otherwise recurse left and right; if both sides return a non-null result, the current node is the LCA. If only one side is non-null, propagate that result upward. O(n) time since each node is visited once.",
                "A very common tree question, and the 'both sides non-null' insight is what interviewers listen for to confirm real understanding versus memorized code."),
            hi("What is the difference between BFS and DFS traversal of a tree, and when would you use each?",
                "BFS (level-order, using a queue) visits nodes level by level — ideal for finding shortest paths or level-specific information. DFS (using recursion or a stack) goes deep before backtracking — ideal for path-based or subtree problems, and generally uses less memory on wide/shallow trees since it doesn't need to hold an entire level at once.",
                "A fundamentals question that also probes whether you understand the memory trade-off, not just the traversal order."),
            md("How would you serialize and deserialize a binary tree?",
                "Serialize with a pre-order DFS traversal, writing a sentinel value (e.g., 'null' or '#') for missing children so structure is preserved. Deserialize by reading the values in the same order and recursively rebuilding: the next value becomes the current node (or null), then recurse for its left and right children.",
                "A more advanced tree question that tests whether you can design an encoding scheme, not just traverse an existing structure.")));

        m.put("DSA5", List.of(
            hi("How do you find the shortest path in an unweighted graph?",
                "Use BFS from the source: it explores nodes level by level, so the first time you reach any node is guaranteed to be via the shortest path in terms of number of edges. Track visited nodes to avoid revisiting, and optionally track distances/parents to reconstruct the actual path.",
                "A near-universal graph question — the 'BFS gives shortest path only when unweighted' distinction is exactly what interviewers probe for."),
            hi("How do you detect a cycle in a directed graph?",
                "Use DFS with three states per node — unvisited, in the current recursion stack ('visiting'), and fully processed ('visited'). If DFS reaches a node that's currently in the recursion stack, that's a back edge, meaning a cycle exists. This differs from undirected-graph cycle detection, which just needs a simple visited check (plus tracking the parent to avoid falsely flagging the edge you came from).",
                "A commonly asked question, and the directed-vs-undirected distinction is a classic follow-up that catches candidates who only know one version."),
            hi("Adjacency list vs. adjacency matrix — when would you choose each?",
                "Adjacency list uses O(V+E) space and is efficient for sparse graphs, with fast iteration over a node's neighbors. Adjacency matrix uses O(V²) space but gives O(1) edge-existence checks, which is preferable for dense graphs or when you frequently need to check 'is there an edge between A and B'.",
                "A fundamentals question checking whether you choose representations deliberately based on graph density, a common real-world design decision."),
            md("How do you find the number of connected components in an undirected graph?",
                "Iterate over all nodes; for each unvisited node, run a BFS or DFS to mark every node reachable from it as visited, and increment a component counter. The final counter value is the number of connected components. O(V+E) time overall.",
                "A frequently asked variant that builds directly on basic BFS/DFS — often used to test whether you can apply the pattern to a new question, not just recite a memorized solution.")));

        m.put("DSA6", List.of(
            hi("Solve the classic 'climbing stairs' problem (n steps, 1 or 2 steps at a time) — what's the DP approach?",
                "The number of ways to reach step n equals ways to reach step n-1 plus ways to reach step n-2 (since your last move was either 1 or 2 steps) — this is exactly the Fibonacci recurrence. Use tabulation: build up from base cases (1 way to reach step 0 or 1) iteratively, or use memoized recursion. O(n) time, O(1) space if you only track the last two values.",
                "The canonical 'introduce DP' question — almost every DP module or interview starts here because it makes the overlapping-subproblems idea concrete."),
            hi("What's the difference between memoization and tabulation, and when would you prefer one over the other?",
                "Memoization is top-down: you write the natural recursive solution and cache results as you compute them, avoiding recomputation. Tabulation is bottom-up: you iteratively build a table starting from base cases up to the answer. Memoization is often more intuitive to write from a recursive definition; tabulation avoids recursion-depth/stack-overflow risk and can sometimes be optimized to use less space.",
                "A very common conceptual DP question — interviewers use it to confirm you understand DP as a technique, not just a set of memorized solutions."),
            hi("Solve the 0/1 knapsack problem — what's the approach and complexity?",
                "For each item, decide include or exclude; build a DP table where dp[i][w] represents the max value achievable using the first i items within weight capacity w. For each item, dp[i][w] = max(dp[i-1][w] (exclude), value[i] + dp[i-1][w-weight[i]] (include, if it fits)). O(n·W) time and space, where n is the number of items and W is the capacity.",
                "One of the most classic DP problems, frequently used as a template that many other DP problems (subset sum, partition equal subset) reduce to."),
            md("How do you identify that a problem is solvable with dynamic programming in the first place?",
                "Look for two properties: overlapping subproblems (the same smaller inputs recur many times during a naive recursive solution) and optimal substructure (the optimal solution to the full problem can be built from optimal solutions to its subproblems). If a brute-force recursive solution is exponential because of repeated identical calls, that's a strong signal DP can help.",
                "A meta-question interviewers ask to see whether you can recognize the DP pattern on a novel problem, not just solve ones you've memorized.")));

        // ================= System Design =================
        m.put("SD1", List.of(
            hi("How would you design a URL shortener (like bit.ly)?",
                "Core flow: given a long URL, generate a short, unique key (via a counter encoded in base62, or a hash with collision handling) and store the mapping in a database; redirect requests for the short URL by looking up the key. At scale: use a distributed ID generator to avoid a single point of contention, cache hot mappings (most traffic concentrates on a small fraction of URLs), and consider read replicas since reads (redirects) vastly outnumber writes (new shortens).",
                "The single most classic system-design interview question — used because it's simple to start but has rich follow-ups on scale, uniqueness, and caching."),
            hi("Vertical vs. horizontal scaling — what are the trade-offs?",
                "Vertical scaling means adding more resources (CPU/RAM) to a single machine — simple, but has a hard ceiling, gets expensive fast, and the machine remains a single point of failure. Horizontal scaling means adding more machines and distributing load across them — scales much further and improves fault tolerance, but requires a load balancer, statelessness (or shared session storage), and adds operational complexity.",
                "A foundational question in nearly every system-design interview, often the opening question before diving into a specific design."),
            hi("What does a load balancer do, and what are common algorithms it uses?",
                "It distributes incoming traffic across multiple backend servers so no single server is overwhelmed, and can detect and route around unhealthy instances. Common algorithms include round robin (rotate evenly), least connections (send to the least-busy server), and IP hash (route the same client consistently to the same server, useful for session affinity).",
                "A very frequently asked fundamentals question — often the first deep-dive after a candidate mentions 'add a load balancer' in a design."),
            md("What's the difference between latency and throughput, and can improving one hurt the other?",
                "Latency is the time to complete a single request; throughput is the number of requests processed per unit time. They can trade off — batching multiple requests together can raise throughput (more total work done per second) while increasing the latency experienced by any individual request waiting in the batch.",
                "Tests precise vocabulary and whether you understand that optimizing one metric isn't free with respect to the other.")));

        m.put("SD2", List.of(
            hi("SQL vs. NoSQL — how do you decide which to use for a given system?",
                "Choose SQL (PostgreSQL, MySQL) when you need strong consistency, complex relationships/joins, and ACID transactions — e.g., financial systems. Choose NoSQL (MongoDB, Cassandra, DynamoDB) when you need flexible/evolving schemas, very high write throughput, or easy horizontal scaling, and can tolerate eventual consistency and denormalized data with few complex joins.",
                "One of the most universally asked system-design questions — nearly every design interview eventually asks you to justify a database choice."),
            hi("Explain database sharding and replication — what problem does each solve?",
                "Sharding splits a dataset horizontally across multiple database nodes (e.g., by user ID range or hash) so each node holds only a subset of the data — this scales both storage and write throughput beyond a single machine's limits. Replication copies the same data across multiple nodes — this scales read throughput and provides availability/failover if a node goes down. Many large systems use both together.",
                "A very common deep-dive once 'the database is a bottleneck' comes up in a design — tests whether you know two different tools for two different problems."),
            hi("State the CAP theorem, and give a practical example of the trade-off.",
                "In the presence of a network partition, a distributed system must choose between consistency (every read gets the latest write) and availability (every request gets a response, even if it might be stale). Example: a shopping cart service might choose availability (AP) — always let the user add items even during a partition, and reconcile later — while a payments ledger might choose consistency (CP), refusing to serve potentially-stale data at the cost of some availability.",
                "A classic theory question in system design — interviewers want a concrete example, not just the definition, to confirm real understanding."),
            md("What's database indexing, and what's the trade-off of adding more indexes?",
                "An index is an auxiliary data structure (commonly a B-tree) that lets the database find rows matching a query without scanning the whole table, dramatically speeding up reads on indexed columns. The trade-off is that every index adds overhead to writes (each insert/update/delete must also update the index) and consumes additional storage — so indexes should be added deliberately based on actual query patterns, not on every column.",
                "Tests whether you understand indexing has a cost, not just a benefit — a common differentiator between junior and senior answers.")));

        m.put("SD3", List.of(
            hi("Why introduce a cache, and what's the hardest problem it introduces?",
                "Caching stores frequently-accessed data in fast memory (e.g., Redis), reducing latency and offloading the primary database. The hardest problem it introduces is cache invalidation — deciding when cached data is stale and needs to be refreshed or evicted, since serving stale data can cause subtle correctness bugs that are hard to reproduce.",
                "A very frequently asked question — often phrased as 'there are only two hard problems in computer science: cache invalidation and naming things,' which interviewers use to open a caching discussion."),
            hi("Cache-aside vs. write-through caching — what's the difference?",
                "Cache-aside (lazy loading): the application checks the cache first; on a miss, it loads from the database, then writes the result into the cache for next time. Writes go directly to the database, and the cache entry is invalidated or updated separately. Write-through: writes go to the cache and the database together, keeping them always in sync, at the cost of higher write latency since every write touches both stores.",
                "A common follow-up once caching is introduced in a design — tests whether you know concrete strategies, not just 'add a cache.'"),
            hi("What problem does a message queue (like Kafka or RabbitMQ) solve in a system design?",
                "It decouples producers from consumers — the producer can publish work and move on without waiting for it to be processed, and consumers can process at their own pace. This smooths traffic spikes (the queue absorbs bursts), enables retries and asynchronous processing, and improves resilience since a slow or temporarily-down consumer doesn't block the producer.",
                "A near-universal system-design element — appears in almost any design involving background jobs, notifications, or high write volume."),
            md("What's the difference between a message queue and a pub/sub system?",
                "In a traditional queue, each message is typically consumed by exactly one consumer (useful for distributing work across a pool of workers). In pub/sub, a published message can be delivered to multiple independent subscribers, each processing it for their own purpose (e.g., one service updates a cache while another sends a notification, both triggered by the same event).",
                "A precision question — interviewers use it to check you understand messaging patterns, not just that 'queues exist.'")));

        m.put("SD4", List.of(
            hi("Monolith vs. microservices — what are the real trade-offs, beyond buzzwords?",
                "A monolith is simpler to build, test, deploy, and reason about, especially for a small team or early-stage product, but becomes harder to scale specific parts independently and can create tight coupling that slows down large teams working in the same codebase. Microservices let teams and services scale and deploy independently, but introduce network latency, distributed-data-consistency challenges, and real operational overhead (monitoring, service discovery, versioning many deployables).",
                "One of the most common 'trade-off' questions — interviewers specifically want you to avoid a one-sided 'microservices are always better' answer."),
            hi("Why and how would you rate-limit an API?",
                "Rate limiting protects the system from abuse or accidental overload, ensures fair usage across clients, and controls cost for downstream resources (including LLM APIs). Common algorithms: token bucket (tokens refill at a steady rate, each request consumes one, allowing controlled bursts) and leaky bucket (requests processed at a fixed rate, smoothing bursts entirely). Applied per API key, user, or IP depending on the fairness model needed.",
                "A very frequently asked API-design question, especially relevant now that many systems rate-limit against expensive LLM backends."),
            hi("Explain the circuit breaker pattern — what does it prevent?",
                "A circuit breaker monitors calls to a dependency; if failures exceed a threshold, it 'opens' and stops sending requests to the failing service for a cooldown period, failing fast instead of piling up slow, doomed requests. This prevents cascading failures where one struggling downstream service causes upstream services to also exhaust their resources waiting on it.",
                "A common resilience-pattern question in senior-level system-design interviews — tests knowledge of production failure handling, not just happy-path design."),
            md("What's the difference between REST and gRPC, and when would you choose each?",
                "REST uses HTTP/JSON, is human-readable, widely supported, and simple for public-facing or browser-based APIs. gRPC uses HTTP/2 and Protocol Buffers, giving much better performance and strongly-typed contracts, making it a strong choice for internal service-to-service communication where speed and type safety matter more than human readability.",
                "A common API-design trade-off question in microservices-heavy system-design rounds.")));

        m.put("SD5", List.of(
            hi("Design a RAG-based Q&A system for a company's internal documentation — walk through the architecture.",
                "Ingestion pipeline: documents are chunked, embedded, and stored in a vector database (with metadata for access control/filtering). Query flow: the user's question is embedded, relevant chunks are retrieved (optionally hybrid search + re-ranking), assembled into a prompt with the question, and sent to an LLM to generate a grounded, cited answer. Production concerns: caching for repeated questions, an eval pipeline to catch quality regressions, access-control filtering so users only retrieve documents they're permitted to see, and monitoring for cost and latency.",
                "The flagship AI system-design question — virtually every GenAI-adjacent system-design interview includes some version of 'design a RAG system.'"),
            hi("How would you design an LLM gateway that sits in front of multiple model providers?",
                "A central service that handles authentication/API-key management, routes each request to the appropriate model (cheap vs. strong, or a fallback provider if one is down), applies rate limiting and per-user/per-team cost budgets, caches repeated or semantically similar requests, and logs usage/cost/latency centrally for observability across every team using LLMs in the org.",
                "A very common 'scale up your GenAI usage' question once an org has multiple teams calling different LLM providers — tests whether you can design shared infrastructure, not just a single feature."),
            hi("How would you scale a vector database to handle 100 million+ vectors with low query latency?",
                "Use an approximate nearest-neighbor index (HNSW or IVF) instead of exact search, since exact search doesn't scale. Shard the index across multiple nodes by a partition key (e.g., tenant or category) so no single node holds the entire dataset, add read replicas for query throughput, cache results for hot/repeated queries, and tune the recall-vs-latency knob (e.g., HNSW's ef_search or IVF's nprobe) based on the product's tolerance for slightly lower recall in exchange for speed.",
                "A frequent deep-dive follow-up in AI system-design interviews — tests whether your RAG knowledge extends to real scale, not just a single-node demo."),
            md("How would you keep LLM costs under control for a feature serving millions of requests per day?",
                "Cache aggressively (exact-match and/or semantic caching for repeated intents), route the majority of 'easy' requests to a small/cheap model and reserve the expensive model for genuinely hard cases, set hard per-user or per-team budgets with graceful degradation when exceeded, and continuously monitor cost per request to catch regressions from prompt or model changes.",
                "A senior-level question that combines system design with the cost-engineering judgment interviewers specifically look for in production AI experience.")));

        // ================= Java Full-Stack =================
        m.put("JFS1", List.of(
            hi("What is the difference between == and .equals() in Java?",
                "== compares references (memory addresses) for objects, or primitive values directly for primitives. .equals() compares logical/content equality, as defined by the class's override — e.g., two different String objects with the same characters are == false but .equals() true.",
                "One of the most classic Java interview questions — tests whether you understand Java's object model, not just syntax."),
            hi("What is the difference between checked and unchecked exceptions?",
                "Checked exceptions (extend Exception, not RuntimeException) must be declared with throws or caught at compile time — used for recoverable conditions the caller should anticipate. Unchecked exceptions (extend RuntimeException) aren't enforced by the compiler — typically used for programming errors.",
                "A very common Java fundamentals question, often followed by 'when would you create a custom checked vs unchecked exception?'"),
            hi("What's the difference between an abstract class and an interface in modern Java (8+)?",
                "An abstract class can hold state (instance fields) and a constructor, and a class can extend only one. An interface can't hold instance state, but since Java 8 can have default and static methods, and a class can implement multiple interfaces.",
                "A classic OOP question — the Java 8+ default-methods nuance is a common follow-up that separates up-to-date knowledge from outdated."),
            md("What is the volatile keyword, and when would you use it?",
                "volatile ensures visibility of a variable's latest value across threads — writes by one thread are immediately visible to others, preventing threads from caching a stale value. It does NOT provide atomicity for compound operations (like increment) — for that you still need synchronization or an atomic class.",
                "Tests precise understanding of the Java Memory Model — a common trap is assuming volatile makes an operation atomic."),
            hi("What's the difference between String, StringBuilder, and StringBuffer?",
                """
                String is immutable — every "modification" creates a new object. StringBuilder is a mutable, non-thread-safe sequence of characters, fast for building strings in a loop. StringBuffer is the same as StringBuilder but with synchronized methods, making it thread-safe at the cost of performance.

                ```java
                String s = "a";
                s += "b";              // creates a NEW String object, "a" is discarded

                StringBuilder sb = new StringBuilder();
                sb.append("a").append("b");   // mutates the same object in place
                ```
                """,
                "A very common 'do you understand Java string internals' question — the follow-up is almost always 'so which would you use in a loop, and why?'"),
            hi("Why are Strings immutable in Java, and what is the String pool?",
                "Immutability makes Strings safe to share across threads without synchronization, safe to use as HashMap keys (their hash code can be cached), and enables the String pool — a cache of literal String values so identical literals reuse the same object in memory instead of allocating a new one each time.",
                "Tests whether a candidate understands WHY a design decision was made, not just that it exists — a common senior-level follow-up to the String/StringBuilder question."),
            hi("What's the difference between method overloading and method overriding?",
                """
                Overloading: same method name, different parameter list, resolved at COMPILE time (static/early binding) — usually within the same class.
                Overriding: a subclass redefines a method inherited from its parent with the SAME signature, resolved at RUNTIME (dynamic/late binding).

                ```java
                class Calc {
                    int add(int a, int b) { return a + b; }        // overload 1
                    double add(double a, double b) { return a + b; } // overload 2 (different signature)
                }
                class Base { void speak() { System.out.println("Base"); } }
                class Derived extends Base {
                    @Override void speak() { System.out.println("Derived"); } // override
                }
                ```
                """,
                "The most classic Java OOP question of all — interviewers use it to confirm you know compile-time vs runtime binding, not just the vocabulary."),
            hi("What is runtime polymorphism, and how does the JVM actually pick which method to call?",
                """
                Runtime polymorphism means the actual method invoked is determined by the object's real (runtime) type, not the reference's declared (compile-time) type. The JVM uses dynamic dispatch — via a per-class virtual method table — to look up the correct overridden method at the moment the call happens.

                ```java
                Base b = new Derived();
                b.speak();   // prints "Derived" — decided at runtime by b's actual object type
                ```
                """,
                "Interviewers use this to check you understand the mechanism behind polymorphism, not just that 'Java supports polymorphism.'"),
            md("What are the four pillars of Object-Oriented Programming, briefly?",
                "Encapsulation: bundling data and the methods that operate on it, hiding internal state behind a public interface. Inheritance: a class acquiring fields/methods from a parent class. Polymorphism: the same interface behaving differently depending on the actual object type. Abstraction: exposing only essential behavior while hiding implementation complexity (via abstract classes/interfaces).",
                "A warm-up conceptual question — interviewers listen for whether you can explain each pillar with a concrete example, not just recite the definitions."),
            hi("What's the difference between final, finally, and finalize()?",
                "final is a keyword making a variable a constant, a method un-overridable, or a class un-extendable. finally is a block that always runs after a try (whether or not an exception occurred), used for cleanup. finalize() was a method the garbage collector could call before reclaiming an object's memory — it's deprecated since Java 9 and should never be relied on, because its timing is unpredictable.",
                "A classic 'three similar-sounding words' trivia question — but the finalize() deprecation detail separates current knowledge from outdated tutorials."),
            md("Can you override a static method in Java? What actually happens if you try?",
                """
                No — static methods belong to the class, not an instance, so they can't be overridden. If a subclass defines a static method with the same signature, it's method HIDING, not overriding: which method runs is decided at compile time based on the reference's declared type, not the object's runtime type.

                ```java
                class Base { static void greet() { System.out.println("Base"); } }
                class Derived extends Base { static void greet() { System.out.println("Derived"); } }

                Base b = new Derived();
                b.greet();  // prints "Base" — resolved by the declared type, NOT runtime type
                ```
                """,
                "A sharp follow-up to the overloading/overriding question — catches candidates who think polymorphism applies uniformly to all methods."),
            md("What is the diamond problem, and how does Java's interface design avoid it?",
                "The diamond problem occurs when a class inherits the same method from two different parents through multiple inheritance, and it's ambiguous which one applies. Java avoids it for classes by disallowing multiple class inheritance entirely. For interfaces (which can have default methods since Java 8), if two implemented interfaces provide conflicting default methods, the compiler forces you to explicitly override and resolve the conflict rather than silently picking one.",
                "Tests understanding of why Java restricts multiple inheritance the way it does, and the Java 8 default-methods nuance."),
            hi("What is a functional interface, and how do lambdas relate to it?",
                """
                A functional interface has exactly one abstract method (it can have any number of default/static methods) — that single method is what a lambda expression implements. Java provides many built-in ones in java.util.function.

                ```java
                @FunctionalInterface
                interface Greeter { String greet(String name); }

                Greeter g = name -> "Hello, " + name;   // lambda implementing Greeter.greet
                System.out.println(g.greet("Sam"));

                Function<Integer, Integer> square = x -> x * x;  // built-in functional interface
                ```
                """,
                "Foundational for any question about lambdas or streams — interviewers check you know a lambda needs a target functional interface, it's not 'just syntax.'"),
            hi("Walk through a simple Java Stream pipeline — filter, map, and collect.",
                """
                ```java
                List<String> names = List.of("Ann", "Bob", "Cara", "Dee");

                List<String> result = names.stream()
                    .filter(n -> n.length() > 3)   // keep names longer than 3 chars
                    .map(String::toUpperCase)      // transform each
                    .collect(Collectors.toList()); // terminal operation -> ["CARA"]
                ```

                Streams are lazy — filter/map don't run anything until a terminal operation (collect, forEach, reduce, count) is called, at which point elements flow through the pipeline one at a time.
                """,
                "Streams are used constantly in modern Java code — this checks hands-on fluency, not just 'I've heard of streams.'"),
            md("Comparable vs Comparator — what's the difference, and when would you use each?",
                """
                Comparable is implemented BY the class itself to define its one, natural ordering (compareTo). Comparator is a separate object defining an ordering FROM THE OUTSIDE, and you can have as many as you like for different sort criteria.

                ```java
                class Person implements Comparable<Person> {
                    String name; int age;
                    public int compareTo(Person o) { return this.age - o.age; } // natural order: by age
                }

                // Comparator: sort by name instead, without touching the Person class
                people.sort(Comparator.comparing(p -> p.name));
                ```
                """,
                "Tests whether you know WHY there are two sorting mechanisms — a common follow-up is 'how would you sort by two fields?' (thenComparing)."),
            md("HashMap vs TreeMap vs LinkedHashMap — what ordering guarantee does each give?",
                "HashMap: no ordering guarantee at all — iteration order can even change between runs. LinkedHashMap: preserves insertion order (or access order, if configured), using a doubly-linked list alongside the hash table. TreeMap: keeps keys in sorted order (natural ordering or a supplied Comparator), backed by a red-black tree, giving O(log n) operations instead of HashMap's average O(1).",
                "A common follow-up once a candidate says 'I'd use a HashMap' — checks if they actually know the ordering trade-offs, not just the name."),
            md("What's the difference between an array and an ArrayList in Java?",
                "An array has a fixed size set at creation and can hold primitives directly. ArrayList is a resizable, growable list (backed internally by an array that gets reallocated/copied when it fills up) and can only hold objects (primitives get autoboxed). Arrays are slightly faster for fixed-size numeric-heavy work; ArrayList is far more convenient for anything whose size isn't known upfront.",
                "A fundamentals question, often a warm-up before diving into collection internals."),
            md("What is autoboxing/unboxing, and what's a common bug it causes?",
                """
                Autoboxing automatically converts a primitive to its wrapper class (int -> Integer); unboxing does the reverse. The classic bug: unboxing a null wrapper throws a NullPointerException, silently, at the point of unboxing.

                ```java
                Integer count = null;
                int x = count;       // throws NullPointerException here — unboxing null
                ```
                """,
                "A real production bug pattern — interviewers use this to check you're aware of the hidden cost of 'convenient' autoboxing."),
            hi("What are Java generics, and what does PECS (Producer Extends, Consumer Super) mean?",
                """
                Generics let you write classes/methods that work with any type while catching type errors at compile time instead of at runtime with ClassCastException. PECS is the rule for choosing a bounded wildcard: use `? extends T` when you only READ from a structure (it PRODUCES T's for you), and `? super T` when you only WRITE into it (it CONSUMES T's from you).

                ```java
                void copy(List<? extends Number> source, List<? super Integer> dest) {
                    for (Number n : source) dest.add((Integer) n);  // reading from source (extends), writing to dest (super)
                }
                ```
                """,
                "A senior-level generics question — most candidates know generics exist but few can correctly explain when to use extends vs super."),
            md("What is type erasure in Java generics?",
                "At compile time, generic type information (like the <String> in List<String>) is checked but then erased — at runtime, a List<String> and a List<Integer> are both just a raw List. This is why you can't do `new T()` or check `if (obj instanceof List<String>)` at runtime, and why generic arrays (`new T[10]`) aren't allowed.",
                "Explains several generics 'gotchas' at once — a strong answer here signals real depth, not just surface familiarity."),
            hi("What does try-with-resources do, and why is it preferred over a manual try/finally?",
                """
                Any resource implementing AutoCloseable can be declared in the try's parentheses, and Java guarantees close() is called automatically when the block exits — even if an exception was thrown — without you writing a finally block yourself.

                ```java
                try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
                    return reader.readLine();
                }  // reader.close() is called automatically here, even on exception
                ```
                """,
                "Modern idiomatic Java — a candidate still manually closing resources in finally blocks is a mild but real signal of outdated habits."),
            md("What's the difference between throw and throws in Java?",
                "throw actually raises an exception instance at a specific point in code (`throw new IllegalArgumentException(...)`). throws is part of a method's signature, declaring that the method MIGHT propagate a checked exception to its caller, so the caller knows to handle or further declare it.",
                "A quick vocabulary-precision check — commonly paired with the checked/unchecked exceptions question."),
            md("When would you create a custom exception class, and should it be checked or unchecked?",
                """
                Create a custom exception when a generic exception type doesn't clearly communicate what went wrong, or when you want to attach extra context (like an order ID) for callers to use in handling logic.

                ```java
                class InsufficientFundsException extends RuntimeException {
                    private final BigDecimal shortfall;
                    public InsufficientFundsException(BigDecimal shortfall) {
                        super("Short by " + shortfall);
                        this.shortfall = shortfall;
                    }
                }
                ```

                Most modern Java codebases favor unchecked (extending RuntimeException) even for custom exceptions, since checked exceptions tend to force boilerplate try/catch or throws-propagation through layers that can't meaningfully handle them anyway.
                """,
                "Tests both mechanics and current best-practice judgment — checked-exception overuse is a well-known Java anti-pattern interviewers watch for."),
            hi("What is Optional, and how does it help avoid NullPointerExceptions?",
                """
                Optional<T> is a container that either holds a value or is empty, making the possible absence of a value explicit in a method's return type — forcing callers to handle the empty case instead of accidentally dereferencing a null.

                ```java
                Optional<User> user = repository.findById(id);
                String name = user.map(User::getName).orElse("Unknown");
                ```

                Optional is meant for return types, not for fields or method parameters — using it everywhere adds noise without benefit.
                """,
                "A modern-Java-idioms question — the 'don't overuse it' nuance separates real usage experience from just reading the Javadoc."),
            md("Stack vs heap in the Java memory model — what lives where?",
                "The stack holds each thread's local variables and method call frames — it's fast, and memory is automatically reclaimed the instant a method returns. The heap holds all objects (created with `new`) and is shared across all threads — its memory is reclaimed by the garbage collector, not automatically on scope exit.",
                "A foundational JVM question, usually a lead-in to garbage collection or OutOfMemoryError questions."),
            md("What causes a StackOverflowError vs an OutOfMemoryError?",
                "StackOverflowError happens when a thread's call stack exceeds its size limit — almost always from unbounded or infinite recursion missing a base case. OutOfMemoryError happens when the HEAP can't allocate more memory (and the GC can't free enough) — from genuinely holding too many live objects, or a memory leak (e.g., an ever-growing static collection).",
                "Tests whether you can diagnose two similarly-named but very differently-caused JVM errors — a real debugging skill, not just trivia."),
            hi("Why does `Integer.valueOf(100) == Integer.valueOf(100)` return true, but `Integer.valueOf(200) == Integer.valueOf(200)` return false?",
                """
                Java caches boxed Integer values from -128 to 127 (the "Integer cache") — values in that range returned by valueOf() (which autoboxing uses) are the SAME shared object, so == is true. Outside that range, each call creates a new Integer object, so == compares different objects and is false.

                ```java
                Integer a = 100, b = 100;
                System.out.println(a == b);       // true  — cached range
                Integer c = 200, d = 200;
                System.out.println(c == d);       // false — outside cached range
                System.out.println(c.equals(d));  // true  — always use .equals() for wrapper value comparison
                ```
                """,
                "A famous Java 'gotcha' interview question specifically designed to catch == misuse with wrapper types — a strong signal question."),
            hi("Runnable vs Callable vs Thread — what's the difference?",
                """
                Thread is an actual unit of execution you can start. Runnable is a functional interface representing a task with no return value and no checked exceptions (`void run()`). Callable is similar but CAN return a value and throw a checked exception (`V call() throws Exception`), making it the right choice when you need a result back from a background task.

                ```java
                Runnable task = () -> System.out.println("running");
                Callable<Integer> withResult = () -> 21 * 2;

                ExecutorService pool = Executors.newFixedThreadPool(2);
                Future<Integer> future = pool.submit(withResult);
                Integer value = future.get();   // blocks until the task completes
                ```
                """,
                "Foundational for any concurrency discussion — checks you know when you actually need a return value from a background task."),
            hi("Why use the Executor framework instead of manually creating and starting Thread objects?",
                """
                Manually creating threads gives you no control over how many run concurrently, no reuse (thread creation is relatively expensive), and no built-in queuing when work arrives faster than it can be processed. ExecutorService manages a pool of reusable threads, queues excess work, and gives you lifecycle control (shutdown, awaitTermination).

                ```java
                ExecutorService pool = Executors.newFixedThreadPool(4);
                pool.submit(() -> processOrder(order));
                pool.shutdown();
                ```
                """,
                "A production-readiness question — 'new Thread() in a loop' is a well-known red flag interviewers listen for you to avoid."),
            md("What is a deadlock, and how do you prevent one?",
                "A deadlock happens when two or more threads each hold a lock the other needs, and neither can proceed — they wait on each other forever. The most common prevention is establishing a consistent, global lock-ordering (always acquire locks in the same order across all threads), and where possible, using tryLock() with a timeout instead of an unconditional blocking lock.",
                "Concurrency debugging fundamentals — often followed by 'have you actually debugged one?' looking for a real war story."),
            hi("How would you implement a simple producer-consumer setup in Java?",
                """
                ```java
                BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);

                // Producer thread
                queue.put(new Task());       // blocks if the queue is full

                // Consumer thread
                Task task = queue.take();    // blocks if the queue is empty
                process(task);
                ```

                BlockingQueue handles all the waiting/notifying internally — you don't need to hand-roll wait()/notify() logic yourself.
                """,
                "A classic concurrency design question — checks whether you reach for the right built-in tool instead of reinventing synchronization from scratch."),
            md("What is CompletableFuture, and why is it an improvement over plain Future?",
                """
                A plain Future only lets you block and wait for a result with get() — there's no way to chain further work or combine multiple futures without blocking. CompletableFuture lets you compose async pipelines non-blockingly.

                ```java
                CompletableFuture.supplyAsync(() -> fetchUser(id))
                    .thenApply(User::getEmail)
                    .thenAccept(email -> sendNotification(email))
                    .exceptionally(ex -> { log.error("failed", ex); return null; });
                ```
                """,
                "Modern async Java — a candidate only knowing Future (and always calling .get() immediately, which just blocks) is a signal of somewhat dated concurrency knowledge."),
            md("wait/notify vs CountDownLatch/Semaphore — why do most codebases prefer the java.util.concurrent versions?",
                "wait()/notify() are low-level, must be called inside a synchronized block, are easy to get subtly wrong (missed signals, spurious wakeups), and require you to hand-roll the exact condition being waited on. CountDownLatch, Semaphore, and similar java.util.concurrent classes package common coordination patterns into tested, correct, higher-level APIs — most modern code should reach for those before hand-rolling wait/notify.",
                "Tests concurrency maturity — using the highest-level correct tool available is a real production skill, not just knowing the low-level primitives exist."),
            md("What is the 'happens-before' relationship in the Java Memory Model, in plain terms?",
                "It's the guarantee that if action A 'happens-before' action B, then A's effects (like a variable write) are visible to B. Without an explicit happens-before relationship (established by things like synchronized, volatile, or thread.start()/join()), the JVM and CPU are free to reorder or cache operations in ways that can make one thread never see another thread's writes.",
                "A precise, senior-level concurrency question — most candidates know 'threads can see stale data' without knowing the formal mechanism that defines when they're guaranteed NOT to."),
            hi("What are Java records (Java 14+/16+), and what boilerplate do they eliminate?",
                """
                A record is a compact syntax for an immutable data-carrier class — the compiler automatically generates a constructor, getters (named after the fields, not getX()), equals(), hashCode(), and toString().

                ```java
                record Point(int x, int y) {}

                Point p = new Point(3, 4);
                p.x();          // 3 — auto-generated accessor
                p.equals(new Point(3, 4));  // true — auto-generated equals
                ```
                """,
                "Modern Java — records are now the idiomatic way to write simple DTOs, replacing pages of Lombok or hand-written getter/equals/hashCode boilerplate."),
            md("What's the difference between a switch statement and a switch expression (Java 14+)?",
                """
                A switch statement executes code and falls through between cases unless you add `break`. A switch expression PRODUCES A VALUE, uses arrow syntax (no fall-through), and the compiler checks exhaustiveness for enums/sealed types.

                ```java
                // old switch statement — needs break, doesn't return a value
                switch (day) {
                    case MONDAY: System.out.println("Start"); break;
                    default: System.out.println("Other");
                }

                // switch expression — returns a value, no fall-through
                String result = switch (day) {
                    case MONDAY -> "Start";
                    default -> "Other";
                };
                ```
                """,
                "Tests up-to-date Java knowledge — many candidates still only know the old fall-through-prone switch statement."),
            md("What is a sealed class/interface (Java 17), and what problem does it solve?",
                "A sealed type explicitly restricts which classes are allowed to extend/implement it, listed with a `permits` clause. This lets the compiler exhaustively check switch expressions over the type (no `default` needed), and communicates a closed, intentional hierarchy instead of an open one anyone can extend.",
                "A newer-Java feature question — shows whether a candidate's knowledge extends past Java 8, which is increasingly a real signal at companies on modern LTS versions."),
            md("What is var (local variable type inference, Java 10), and where can't you use it?",
                """
                var lets the compiler infer a local variable's type from its initializer — the variable is still statically typed, just written without repeating the type name.

                ```java
                var list = new ArrayList<String>();  // inferred as ArrayList<String>
                ```

                It can only be used for local variables with an initializer — not for fields, method parameters, return types, or a variable with no initial value (`var x;` doesn't compile, since there's nothing to infer from).
                """,
                "A syntax-fluency check — the 'where can't you use it' half is what separates candidates who've actually used var from those who've only heard of it.")));

        m.put("JFS2", List.of(
            hi("What is Inversion of Control (IoC), and how does Spring implement it?",
                "IoC means the framework — not your code — controls the flow of object creation and wiring. Instead of your class instantiating its own dependencies with `new`, the Spring container creates and injects them for you. Spring implements this via its ApplicationContext (the IoC container), which manages the bean lifecycle.",
                "The foundational Spring concept — nearly every Spring interview opens with this or dependency injection."),
            hi("What's the difference between @Component, @Service, @Repository, and @Controller?",
                "All are specializations of @Component and are functionally identical for bean registration — the differences are semantic/documentation, though @Repository also enables Spring's exception translation (converting DB-specific exceptions to Spring's DataAccessException hierarchy), and @Controller marks Spring MVC request-handling classes.",
                "A very common 'do you actually know Spring stereotypes' question — many candidates only know they exist, not their actual behavioral differences."),
            md("What is @Autowired doing under the hood?",
                "It tells Spring to resolve and inject a matching bean from the ApplicationContext by type (falling back to name if multiple candidates match, or requiring @Qualifier to disambiguate). It's processed by a BeanPostProcessor during bean initialization.",
                "Tests whether 'magic' annotations are actually understood mechanically, not just used by convention."),
            hi("What is Spring Boot, and how is it different from the Spring Framework?",
                "The Spring Framework is the core IoC container, dependency injection, and supporting modules (MVC, Data, Security, etc.) — powerful but historically required a lot of manual XML/Java configuration to wire together. Spring Boot is built ON TOP of Spring: it adds auto-configuration (sensible defaults based on what's on the classpath), starter dependencies (curated dependency bundles), and an embedded server, so you can go from zero to a running app with minimal setup.",
                "The very first question in almost every Spring interview — checks you understand Boot is a convention layer over Spring, not a replacement for it."),
            hi("What three things does @SpringBootApplication actually combine?",
                """
                It's a convenience meta-annotation bundling:

                ```java
                @SpringBootConfiguration  // marks this class as a @Configuration source of beans
                @EnableAutoConfiguration  // turns on Spring Boot's classpath-based auto-configuration
                @ComponentScan            // scans this package (and sub-packages) for @Component beans
                public class MyApplication {
                    public static void main(String[] args) {
                        SpringApplication.run(MyApplication.class, args);
                    }
                }
                ```
                """,
                "A very common 'what's really happening in the one annotation everyone copy-pastes' question."),
            hi("What are the different ways to define a Spring bean?",
                """
                ```java
                @Component                        // 1. class-level stereotype, found via component scan
                class OrderService { }

                @Configuration
                class AppConfig {
                    @Bean                          // 2. explicit factory method — full control over construction
                    DataSource dataSource() { return new HikariDataSource(); }
                }
                ```

                A third, older way is declaring beans in XML — rarely used in modern Spring Boot apps, but you may still encounter it in legacy codebases.
                """,
                "Tests whether a candidate knows @Bean is for when you need explicit control (e.g., configuring a third-party class you don't own), not just @Component."),
            md("What's the actual difference between @Bean and @Component?",
                "@Component is a class-level annotation you put ON a class you own, and Spring discovers it via component scanning. @Bean is a method-level annotation inside a @Configuration class, giving you full imperative control to construct and configure an object yourself — essential for third-party classes you can't annotate directly (like a DataSource or a RestTemplate).",
                "A precise follow-up to 'what are the ways to define a bean' — checks understanding of WHEN each is appropriate."),
            hi("What are Spring profiles, and how do you activate one?",
                """
                Profiles let you have environment-specific beans/configuration (dev, test, prod) that only activate for the matching environment.

                ```java
                @Service
                @Profile("dev")
                class MockPaymentService implements PaymentService { }
                ```

                Activate via a JVM arg, environment variable, or application.properties:
                ```
                --spring.profiles.active=dev
                ```
                """,
                "A very practical, commonly-used feature — almost every real Spring Boot project uses profiles, so hands-on familiarity is expected."),
            hi("How do @Value and @ConfigurationProperties differ for reading externalized config?",
                """
                @Value injects a single property, one at a time, often with a SpEL expression:
                ```java
                @Value("${app.max-retries:3}")
                private int maxRetries;
                ```

                @ConfigurationProperties binds a whole group of related properties onto a strongly-typed class at once — more maintainable for anything beyond one or two values:
                ```java
                @ConfigurationProperties(prefix = "app")
                record AppConfig(int maxRetries, String apiUrl, boolean debugMode) {}
                ```
                """,
                "Tests whether a candidate reaches for the right tool as config grows — @Value everywhere is a common code-smell in larger Spring Boot apps."),
            hi("What is AOP (Aspect-Oriented Programming), and what problem does it solve in Spring?",
                """
                AOP lets you factor out cross-cutting concerns (logging, security checks, transactions, metrics) that would otherwise be duplicated across many unrelated methods, into a single reusable "aspect" applied declaratively.

                ```java
                @Aspect
                @Component
                class LoggingAspect {
                    @Around("execution(* com.example.service.*.*(..))")
                    Object logTiming(ProceedingJoinPoint pjp) throws Throwable {
                        long start = System.currentTimeMillis();
                        Object result = pjp.proceed();      // actually calls the real method
                        log.info("{} took {}ms", pjp.getSignature(), System.currentTimeMillis() - start);
                        return result;
                    }
                }
                ```

                @Transactional itself is implemented as AOP — that's why it only works on Spring-managed beans and calls made THROUGH the proxy, not internal self-calls.
                """,
                "A commonly under-understood Spring mechanism — a strong answer connects AOP to @Transactional, which most candidates use daily without knowing how it works."),
            md("What are the main AOP advice types in Spring?",
                "@Before runs before the matched method executes. @After runs after it completes, regardless of outcome. @AfterReturning runs only on successful completion, with access to the return value. @AfterThrowing runs only if an exception was thrown. @Around wraps the whole call, giving full control — it can change arguments, skip the call entirely, or modify the return value, using ProceedingJoinPoint.proceed() to actually invoke the original method.",
                "A precise-terminology check that usually follows the AOP conceptual question — @Around is the one worth being able to explain in real depth."),
            hi("What is a Spring Boot starter, and how does it actually work?",
                "A starter (like spring-boot-starter-web or spring-boot-starter-data-jpa) is a curated Maven/Gradle dependency that pulls in a compatible, tested set of libraries for a given concern — you add one dependency instead of hand-picking and version-matching a dozen individual jars. Starters themselves contain no code; they just declare transitive dependencies, which is what then triggers the relevant auto-configuration once those classes are on the classpath.",
                "Tests understanding of the starter/auto-configuration relationship — a common misconception is thinking starters themselves 'do' the configuration."),
            hi("Walk through a concrete example of Spring Boot auto-configuration deciding what to wire up.",
                """
                Adding spring-boot-starter-data-jpa plus a JDBC driver on the classpath causes DataSourceAutoConfiguration to detect the driver, read your application.properties for connection details, and automatically create and register a DataSource bean — with zero manual @Bean code from you.

                ```
                spring.datasource.url=jdbc:postgresql://localhost/mydb
                spring.datasource.username=postgres
                ```

                If you DO define your own DataSource @Bean, Spring Boot's @ConditionalOnMissingBean backs off and uses yours instead — auto-configuration only fills in what you haven't already provided.
                """,
                "This is the single best answer to 'explain auto-configuration' — a concrete example beats a definition every time in an interview."),
            md("What do @ConditionalOnClass and @ConditionalOnMissingBean actually gate?",
                """
                ```java
                @Configuration
                @ConditionalOnClass(DataSource.class)        // only activates if this class is on the classpath
                class DataSourceAutoConfiguration {
                    @Bean
                    @ConditionalOnMissingBean                  // only creates this bean if the user hasn't defined their own
                    DataSource dataSource() { ... }
                }
                ```

                Together these two conditions are the core mechanism behind almost every Spring Boot auto-configuration class — activate only when relevant, and always yield to a user-supplied bean.
                """,
                "Digs one level deeper than the conceptual auto-config question — checks if a candidate has actually looked at how Spring Boot's own source code is structured."),
            md("What causes a circular dependency in Spring, and how do you resolve it?",
                "It happens when Bean A needs Bean B injected, and Bean B needs Bean A injected, so neither can be fully constructed first. The cleanest fix is usually a design fix — extract the shared logic both beans need into a third bean, breaking the cycle. Mechanically, Spring can sometimes resolve simple cases via setter injection (constructing both beans first, then wiring references afterward), but constructor injection will fail fast and loudly, which is actually the more honest signal that your design has a real problem.",
                "Tests both mechanical understanding and design judgment — the strongest answers mention that the failure is a design smell worth fixing, not just working around."),
            hi("Constructor injection vs setter injection vs field injection — what are the trade-offs?",
                """
                ```java
                // Constructor injection (recommended): explicit, immutable, testable without reflection
                @Service
                class OrderService {
                    private final PaymentClient paymentClient;
                    OrderService(PaymentClient paymentClient) { this.paymentClient = paymentClient; }
                }

                // Field injection (discouraged): hides dependencies, needs reflection/Spring to test
                @Service
                class OrderService {
                    @Autowired private PaymentClient paymentClient;
                }
                ```

                Constructor injection makes dependencies explicit and required, allows the field to be `final`, and lets you construct the class with plain `new` in a unit test with no Spring/mocking framework needed. Field injection is concise but hides what a class actually depends on and complicates testing.
                """,
                "A near-universal Spring question — the expected answer is a clear recommendation (constructor injection) with real reasoning, not just listing the three options."),
            md("What is @Qualifier for, and when do you actually need it?",
                """
                When multiple beans implement the same interface, Spring can't automatically pick one by type alone — @Qualifier disambiguates by name.

                ```java
                @Service("creditCardPayment")
                class CreditCardPaymentService implements PaymentService {}

                @Service("paypalPayment")
                class PaypalPaymentService implements PaymentService {}

                class Checkout {
                    Checkout(@Qualifier("paypalPayment") PaymentService paymentService) { ... }
                }
                ```
                """,
                "A practical question that comes up the moment a real app has more than one implementation of an interface."),
            md("What's the difference between @Primary and @Qualifier?",
                "@Primary marks one bean as the DEFAULT choice among multiple candidates — used when there's a sensible default and only occasional need for an alternative. @Qualifier is explicit at each injection point, required whenever you need precise, per-usage control rather than relying on a single default winner.",
                "A quick precision check that usually follows the @Qualifier question."),
            md("ApplicationContext vs BeanFactory — what's the difference?",
                "BeanFactory is the most basic IoC container — lazy bean initialization, minimal features. ApplicationContext extends BeanFactory and adds the features virtually every real app needs: eager singleton initialization at startup, event publishing, internationalization support, and easy integration with AOP. In practice, you almost always use ApplicationContext; BeanFactory is mostly of historical/academic interest now.",
                "A 'do you know the container hierarchy' question — the practical answer (you almost never use raw BeanFactory) matters more than the trivia."),
            md("How do you switch a Spring Boot app's embedded server from Tomcat to Jetty?",
                """
                Exclude the default Tomcat starter and add the Jetty one:
                ```xml
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-web</artifactId>
                    <exclusions>
                        <exclusion>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-tomcat</artifactId>
                        </exclusion>
                    </exclusions>
                </dependency>
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-jetty</artifactId>
                </dependency>
                ```
                """,
                "Tests whether a candidate understands that the embedded server is just another swappable dependency, part of the broader starter mechanism."),
            md("application.properties vs application.yml — any real functional difference?",
                "No functional difference — both configure the same underlying property source, and Spring Boot supports either (or both together). YAML supports nesting naturally, which can be more readable for deeply structured config (like multiple profile blocks in one file), while .properties is flatter and sometimes simpler to diff in version control. It's mostly a team preference.",
                "A quick practical-knowledge check — mostly relevant when a candidate needs to read/write config during a live-coding exercise."),
            hi("What does Spring Boot Actuator expose, and why does it matter in production?",
                "Actuator adds production-ready operational endpoints out of the box: /actuator/health (app and dependency health), /actuator/metrics (JVM/app metrics), /actuator/info, /actuator/env, and more. These are what monitoring tools, load balancers, and orchestrators like Kubernetes rely on to know whether an instance is healthy and ready for traffic, without you writing any of that plumbing yourself.",
                "A production-readiness question — Actuator is one of the most concretely useful parts of Spring Boot, and hands-on familiarity is a real signal."),
            md("How would you write a custom Actuator health indicator?",
                """
                ```java
                @Component
                class DownstreamApiHealthIndicator implements HealthIndicator {
                    @Override
                    public Health health() {
                        boolean reachable = pingDownstreamApi();
                        return reachable
                            ? Health.up().withDetail("downstreamApi", "reachable").build()
                            : Health.down().withDetail("downstreamApi", "unreachable").build();
                    }
                }
                ```

                Spring Boot automatically discovers it and folds its status into the overall /actuator/health response alongside the built-in indicators (disk space, database, etc.).
                """,
                "A practical extension of the Actuator question — checks whether a candidate can go beyond using the defaults."),
            hi("What's the difference between @RestController and @Controller?",
                """
                @Controller is for traditional Spring MVC where methods return a VIEW NAME (like a Thymeleaf template) to render. @RestController is @Controller + @ResponseBody combined — every method's return value is written directly to the HTTP response body (typically as JSON), which is what almost every modern API-focused Spring Boot app uses.

                ```java
                @RestController
                class UserController {
                    @GetMapping("/users/{id}")
                    User getUser(@PathVariable Long id) { return userService.findById(id); } // serialized to JSON automatically
                }
                ```
                """,
                "A fundamental REST-controller question — the @ResponseBody detail is what most candidates forget to mention."),
            md("@RequestMapping vs @GetMapping/@PostMapping — any real difference?",
                "@GetMapping, @PostMapping, etc. are simply shorthand, method-specific compositions of @RequestMapping(method = ...) — functionally identical, just more concise and more readable at a glance about which HTTP verb a handler responds to. Modern Spring code almost always uses the specific shorthand annotations.",
                "A quick syntax-history question — mostly relevant if a candidate is reading older Spring code using the verbose @RequestMapping(method=RequestMethod.GET) form."),
            hi("What is the DispatcherServlet, and what's its role in handling a Spring MVC request?",
                "The DispatcherServlet is the single front controller for every incoming request in a Spring MVC app. It receives the request, consults HandlerMapping to figure out which controller method should handle it, invokes that method (running through any interceptors/filters along the way), takes the result, and — for @RestController — has it serialized to the response, or for @Controller — resolves and renders the returned view name.",
                "A 'what actually happens when a request comes in' question — separates candidates who've only used annotations from those who understand the underlying request flow."),
            hi("Filter vs Interceptor in Spring — what's the difference and when would you use each?",
                """
                A Filter (javax/jakarta servlet API) runs BEFORE the request even reaches Spring's DispatcherServlet — it operates at the raw servlet level, so it's ideal for cross-cutting, framework-agnostic concerns like CORS or request logging.

                ```java
                @Component
                class RequestLoggingFilter implements Filter {
                    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
                        log.info("Request: {}", ((HttpServletRequest) req).getRequestURI());
                        chain.doFilter(req, res);
                    }
                }
                ```

                An Interceptor (HandlerInterceptor) runs INSIDE Spring MVC, after the DispatcherServlet has resolved which controller will handle the request — so it has access to Spring-specific context (like the handler method itself), making it better for concerns like authorization checks tied to specific controller metadata.
                """,
                "A precise architectural-layering question — the key distinguishing detail is which one runs inside vs outside Spring MVC's own dispatch logic."),
            hi("What do @ControllerAdvice and @ExceptionHandler do together?",
                """
                They let you centralize exception-to-HTTP-response mapping in one place instead of try/catching in every controller method.

                ```java
                @RestControllerAdvice
                class GlobalExceptionHandler {
                    @ExceptionHandler(ResourceNotFoundException.class)
                    ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
                        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
                    }

                    @ExceptionHandler(MethodArgumentNotValidException.class)
                    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
                        return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed"));
                    }
                }
                ```
                """,
                "One of the most practically important Spring patterns — almost every production REST API needs centralized, consistent error handling."),
            md("What does Spring Boot DevTools actually do?",
                "It adds fast application restarts on classpath changes (by using two classloaders — one for your rarely-changing dependencies, one for your frequently-changing app code, so only the latter needs reloading), automatic browser LiveReload, and sensible development-time defaults like disabling template caching. It's excluded from production builds automatically when packaged as a jar/war.",
                "A quality-of-life developer-experience question — mostly checks familiarity with the actual day-to-day dev workflow, not deep internals."),
            md("At a high level, how would you write a custom Spring Boot starter?",
                "Create an auto-configuration class (annotated with @AutoConfiguration or the older @Configuration + @ConditionalOnClass), register it so Spring Boot discovers it (via META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports in modern Boot), and typically split it into two modules: an 'autoconfigure' module with the actual logic, and a thin 'starter' module that just declares dependencies — mirroring how Spring Boot's own starters are structured.",
                "A senior/architecture-level question — most candidates have used starters but few have built one, so a rough correct outline is a strong signal."),
            hi("What is @EventListener, and how does Spring's application event system work?",
                """
                Spring lets you publish and listen for events within an application context, decoupling the code that triggers an action from the code that reacts to it.

                ```java
                record OrderPlacedEvent(Long orderId) {}

                @Service
                class OrderService {
                    private final ApplicationEventPublisher publisher;
                    void placeOrder(Order order) {
                        // ... save order ...
                        publisher.publishEvent(new OrderPlacedEvent(order.getId()));
                    }
                }

                @Component
                class EmailNotifier {
                    @EventListener
                    void onOrderPlaced(OrderPlacedEvent event) {
                        sendConfirmationEmail(event.orderId());
                    }
                }
                ```

                By default listeners run synchronously on the publishing thread — add @Async on the listener (with async support enabled) to run it off the main request thread.
                """,
                "Tests whether a candidate knows Spring has a built-in, decoupled event mechanism rather than always reaching for direct method calls or an external message queue."),
            md("What does @Lazy do, and when would you actually want lazy bean initialization?",
                "By default, singleton beans are created eagerly at application startup. @Lazy defers a bean's creation until it's first actually needed. It's useful for rarely-used, expensive-to-construct beans (to speed up startup time), or to work around certain circular-dependency edge cases — but it can also hide configuration problems that would otherwise fail fast at startup, so it's used selectively, not by default.",
                "Tests judgment, not just mechanics — the 'when would you NOT want this' half of the answer is what separates thoughtful usage from cargo-culting."),
            hi("@RequestParam vs @PathVariable vs @RequestBody — when do you use each?",
                """
                ```java
                @GetMapping("/users/{id}")                          // @PathVariable: part of the URL path itself
                User getUser(@PathVariable Long id) { ... }

                @GetMapping("/users")                                // @RequestParam: query string parameter
                List<User> search(@RequestParam String name) { ... } // GET /users?name=Sam

                @PostMapping("/users")                               // @RequestBody: the JSON request body
                User createUser(@RequestBody CreateUserRequest req) { ... }
                ```
                """,
                "A very common, very practical REST-controller question — checks fluency with everyday Spring MVC annotations, not edge cases.")));

        m.put("JFS3", List.of(
            hi("What is the difference between JPA, Hibernate, and Spring Data JPA?",
                "JPA is a specification (an interface/standard) for ORM in Java. Hibernate is the most common implementation of that specification. Spring Data JPA is a further abstraction on top of JPA that eliminates boilerplate repository code (e.g., generating implementations for interfaces like JpaRepository).",
                "A very common layering question — candidates often conflate these three, and interviewers use it to check real understanding of the stack."),
            hi("What is the first-level (persistence context) cache in JPA/Hibernate?",
                "Within a single EntityManager/session, JPA caches entities it has already loaded — a second request for the same entity by ID returns the cached instance without hitting the database again, for the duration of that persistence context (typically one transaction).",
                "A common follow-up once N+1 queries come up — tests whether you understand what Hibernate is doing behind the scenes."),
            md("What does @Transactional's readOnly=true do, and why use it?",
                "It's a hint to the persistence provider that the transaction won't modify data, allowing optimizations like skipping dirty-checking and, depending on the database/driver, enabling read-only transaction routing (e.g., to a read replica).",
                "Shows attention to performance detail beyond just 'it works' — a senior-level signal."),
            hi("What is the N+1 query problem, and how do you fix it?",
                """
                Fetching a list of N parent entities, then lazily loading a related collection for EACH one separately, triggers 1 query for the parents plus N more queries — one per parent — instead of a single efficient query.

                ```java
                // N+1: one query for all orders, then one MORE query per order to fetch its items
                List<Order> orders = orderRepository.findAll();
                orders.forEach(o -> o.getItems().size()); // triggers a lazy-load query per order

                // Fixed: JOIN FETCH pulls everything in ONE query
                @Query("SELECT o FROM Order o JOIN FETCH o.items")
                List<Order> findAllWithItems();
                ```
                """,
                "One of THE most common Spring Data JPA interview questions — almost guaranteed if a candidate claims JPA/Hibernate experience."),
            hi("Show @OneToMany, @ManyToOne, and @ManyToMany mappings with a quick example.",
                """
                ```java
                @Entity
                class Order {
                    @Id @GeneratedValue Long id;
                    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
                    List<OrderItem> items;
                }

                @Entity
                class OrderItem {
                    @Id @GeneratedValue Long id;
                    @ManyToOne
                    @JoinColumn(name = "order_id")
                    Order order;
                }

                @Entity
                class Student {
                    @ManyToMany
                    @JoinTable(name = "student_course",
                        joinColumns = @JoinColumn(name = "student_id"),
                        inverseJoinColumns = @JoinColumn(name = "course_id"))
                    List<Course> courses;
                }
                ```
                `mappedBy` marks the non-owning side of a bidirectional relationship — the owning side is the one with the actual foreign key column.
                """,
                "A hands-on mapping question — checks whether a candidate can actually write correct JPA annotations, not just describe relationships abstractly."),
            hi("FetchType.LAZY vs FetchType.EAGER — what's the difference, and which should be the default?",
                "LAZY defers loading a related entity/collection until it's actually accessed — the default and generally correct choice, since it avoids pulling data you may never use. EAGER loads the related data immediately, in the same query or a forced follow-up — it can silently cause the N+1 problem or over-fetch large object graphs. Best practice: default to LAZY everywhere, and fetch eagerly only explicitly, per-query, when you know you need the related data (e.g., via JOIN FETCH).",
                "A very common follow-up to the N+1 question — checks if a candidate knows WHY LAZY should usually be the default, not just the keyword."),
            hi("Explain @Transactional propagation — REQUIRED vs REQUIRES_NEW vs NESTED.",
                "REQUIRED (the default): joins the caller's existing transaction if one exists, or starts a new one if not — most methods should use this. REQUIRES_NEW: always suspends any existing transaction and starts a brand-new, independent one — used when a piece of work (like audit logging) must commit regardless of whether the outer transaction later rolls back. NESTED: starts a true savepoint within the existing transaction — an inner failure can roll back to that savepoint without necessarily failing the whole outer transaction (support varies by driver).",
                "A senior-level question — most candidates know REQUIRED exists but few can correctly explain when REQUIRES_NEW or NESTED actually matters."),
            md("Name the four standard transaction isolation levels and what each prevents.",
                "READ_UNCOMMITTED: prevents nothing — dirty reads are possible. READ_COMMITTED: prevents dirty reads (you never see another transaction's uncommitted changes) but allows non-repeatable reads. REPEATABLE_READ: also prevents non-repeatable reads (re-reading the same row within a transaction gives the same result) but phantom reads are still possible in some databases. SERIALIZABLE: prevents all of the above by effectively serializing transactions — strongest consistency, worst concurrency.",
                "A classic databases-meets-Java question — expected knowledge for anyone claiming production JPA/transaction experience."),
            hi("Optimistic locking vs pessimistic locking in JPA — how do you implement optimistic locking?",
                """
                Pessimistic locking acquires an actual database lock on read, blocking other transactions from touching that row until it's released — safe but hurts concurrency. Optimistic locking assumes conflicts are rare: it checks a version number at COMMIT time, and fails loudly if another transaction updated the row in between, without ever taking a DB lock.

                ```java
                @Entity
                class Account {
                    @Id Long id;
                    BigDecimal balance;
                    @Version
                    Long version;   // Hibernate auto-increments this and checks it on every UPDATE
                }
                ```
                If two transactions load the same row and both try to update it, the second commit throws OptimisticLockException.
                """,
                "A practical concurrency-meets-persistence question — the @Version code example is what separates real hands-on experience from textbook recall."),
            md("Walk through the JPA entity lifecycle states.",
                "Transient: a plain new object, not yet associated with a persistence context or the database. Managed (persistent): attached to a persistence context — changes to it are tracked and auto-flushed to the DB. Detached: was managed but the persistence context closed (e.g., transaction ended) — changes are no longer tracked. Removed: marked for deletion within an active persistence context, deleted from the DB on flush/commit.",
                "A conceptual JPA question — often paired with 'what causes a LazyInitializationException' as a practical follow-up."),
            md("What do @Id and @GeneratedValue control, and what are the generation strategies?",
                "@Id marks the primary key field. @GeneratedValue controls how its value is produced: IDENTITY delegates to the database's auto-increment column (simple, but can't batch inserts efficiently). SEQUENCE uses a database sequence object (efficient batching, needs DB support). TABLE simulates a sequence using a regular table (portable but slower, rarely used). AUTO lets the JPA provider pick based on the underlying database.",
                "A fundamentals question — the IDENTITY-vs-SEQUENCE batching trade-off is the detail that shows real depth."),
            hi("What is JPQL, and how does it differ from native SQL?",
                """
                JPQL (Jakarta Persistence Query Language) queries against your ENTITY model — class and field names — not raw database table/column names, so it stays portable across databases and refactors along with your Java code.

                ```java
                @Query("SELECT o FROM Order o WHERE o.customer.email = :email")
                List<Order> findByCustomerEmail(@Param("email") String email);

                // vs native SQL, which hits the actual table/column names directly:
                @Query(value = "SELECT * FROM orders WHERE customer_email = :email", nativeQuery = true)
                List<Order> findByCustomerEmailNative(@Param("email") String email);
                ```
                """,
                "Tests whether a candidate knows JPQL exists as the 'default' query language in Spring Data JPA, and when native SQL is still necessary (DB-specific functions, complex queries JPQL can't express)."),
            md("save() vs saveAndFlush() in Spring Data JPA — what's the actual difference?",
                "save() persists the entity, but the actual SQL INSERT/UPDATE may be delayed until the persistence context is flushed (at transaction commit, or whenever Hibernate decides to flush). saveAndFlush() forces the SQL to execute immediately. You need it when subsequent code in the SAME transaction needs to see the change reflected in the database right away (e.g., a native query or a check that depends on a DB-level constraint/trigger).",
                "A subtle but real practical question — most candidates know save() but haven't hit the specific scenario where flush timing actually matters."),
            hi("How do Spring Data JPA derived query methods work?",
                """
                Spring Data JPA parses a repository method's NAME to generate the query automatically — no @Query needed for straightforward cases.

                ```java
                interface UserRepository extends JpaRepository<User, Long> {
                    List<User> findByLastNameAndAgeGreaterThan(String lastName, int age);
                    Optional<User> findByEmailIgnoreCase(String email);
                    boolean existsByUsername(String username);
                }
                ```

                Spring parses `findByLastNameAndAgeGreaterThan` into `WHERE last_name = ? AND age > ?` at startup, without you writing any SQL/JPQL.
                """,
                "Extremely common in real Spring Data JPA codebases — checks whether a candidate can read/write this convention fluently."),
            md("When do you need @Query instead of relying on a derived query method name?",
                "Once the query gets complex — joins across multiple entities, aggregations, conditional logic, or anything that would make the method name absurdly long and unreadable — @Query with JPQL (or native SQL) is clearer and more maintainable than continuing to stretch the derived-method-name convention.",
                "Tests practical judgment about when a Spring Data JPA convention has been pushed past its useful limit."),
            md("What is a DTO projection in Spring Data JPA, and why use one instead of returning full entities?",
                """
                ```java
                interface OrderSummary {
                    Long getId();
                    BigDecimal getTotal();
                }

                interface OrderRepository extends JpaRepository<Order, Long> {
                    List<OrderSummary> findByCustomerId(Long customerId); // fetches ONLY id and total, not the whole entity graph
                }
                ```
                """,
                "Returning full JPA entities from an API can over-fetch data, leak lazy-loading exceptions across layers, and couple your API shape to your DB schema — projections avoid all three."),
            hi("What causes a LazyInitializationException, and how do you avoid it?",
                "It's thrown when you try to access a LAZY-loaded association AFTER its persistence context (typically the transaction) has already closed — there's no active session left to run the query that would fetch it. Common fixes: fetch what you need eagerly within the transaction (JOIN FETCH), restructure the code so entity access happens while the transaction/session is still open, or map to a DTO before the transactional method returns.",
                "One of the most common real-world Spring/Hibernate runtime errors — nearly every JPA developer has hit this, so a confident, specific answer is a strong signal."),
            md("CascadeType.ALL vs specifying individual cascade types — what's the trade-off?",
                "CascadeType.ALL propagates every operation (persist, merge, remove, refresh, detach) from the parent to the related child entities — convenient, but dangerous if applied carelessly (e.g., deleting a parent unintentionally deleting children that should be preserved). Specifying individual types (like just CascadeType.PERSIST) is more verbose but gives precise control over exactly which operations should propagate.",
                "Tests whether a candidate has been burned by an overly broad CascadeType.ALL in a real codebase — a common source of accidental data loss."),
            md("What does @Embeddable / @Embedded do in JPA?",
                """
                ```java
                @Embeddable
                class Address {
                    String street, city, zipCode;
                }

                @Entity
                class Customer {
                    @Id Long id;
                    @Embedded
                    Address address;   // columns flattened directly into the customer table, no separate table/join
                }
                ```
                """,
                "A useful, commonly-needed pattern for value objects that don't deserve their own table — checks familiarity beyond basic entity mapping."),
            md("Why does Spring Boot default to HikariCP for connection pooling?",
                "Connection pooling avoids the expensive cost of opening a fresh database connection for every query by reusing a pool of already-open connections. HikariCP is Spring Boot's default because it's benchmarked as one of the fastest and lowest-overhead JDBC connection pools available, with sensible defaults requiring little tuning to perform well out of the box.",
                "A practical infrastructure question — checks awareness that 'a DataSource' isn't magic, it's a real connection pool with real configuration (max-pool-size, timeout) worth understanding.")));

        m.put("JFS4", List.of(
            hi("What is idempotency in REST API design, and which HTTP methods are idempotent?",
                "An idempotent operation produces the same result no matter how many times it's repeated. GET, PUT, and DELETE are idempotent by convention (calling PUT with the same body repeatedly leaves the resource in the same state); POST is NOT idempotent (repeating it typically creates multiple resources).",
                "A very common REST-design question, especially relevant for retry logic — clients can safely retry idempotent requests after a network failure."),
            hi("What status code would you return for a validation error vs. a resource not found vs. an unauthorized request?",
                "Validation error: 400 Bad Request. Not found: 404 Not Found. Not authenticated: 401 Unauthorized. Authenticated but not permitted: 403 Forbidden.",
                "Tests precise HTTP semantics knowledge — sloppy status-code usage is a common real-world code-review flag."),
            md("How does Spring's Bean Validation (@Valid, @NotNull, etc.) work, and where does it plug into a REST controller?",
                "Annotations on a request DTO (like @NotNull, @Size) are checked when the DTO is bound to a @RequestBody parameter annotated with @Valid — Spring triggers validation automatically and throws a MethodArgumentNotValidException on failure, which you typically catch in a @ControllerAdvice to return a clean 400 response.",
                "Tests whether you know the full request-validation flow, not just that the annotations exist."),
            hi("What's the difference between PUT and PATCH?",
                "PUT replaces the ENTIRE resource with the request body — fields you omit are typically expected to be cleared/reset to their default. PATCH applies a PARTIAL update — only the fields included in the request body are changed, everything else on the resource stays as-is.",
                "A precise REST-semantics question — a common trap is treating PATCH like 'PUT but smaller' rather than understanding the partial-update contract."),
            hi("What makes an API actually RESTful, beyond just 'it uses HTTP and returns JSON'?",
                "Statelessness — each request contains everything needed to process it, the server holds no client session state between requests. Resource-based URIs — nouns, not verbs (/orders/123, not /getOrder?id=123). Standard HTTP verbs mapped to CRUD operations. Uniform, predictable status codes. A well-designed REST API is also ideally cacheable and layered (a client shouldn't need to know if it's talking directly to the origin server or through a proxy/gateway).",
                "A conceptual question that separates 'I've called REST APIs' from 'I understand REST as an architectural style' — statelessness is the detail most candidates skip."),
            md("What is HATEOAS, and how often is it actually used in practice?",
                "HATEOAS (Hypermedia as the Engine of Application State) means API responses include links to related actions/resources the client can navigate to next, similar to how a web page contains links — so clients don't need to hardcode URI structures. In practice it's used far less than the REST literature suggests; most production APIs are 'RESTful' by the looser common definition without full HATEOAS, because the added complexity often isn't worth it for typical internal or mobile-app-facing APIs.",
                "Tests both textbook knowledge and real-world judgment — the honest 'it's rarely fully implemented' answer is often more impressive than reciting the theory uncritically."),
            hi("How do you version a REST API, and what are the trade-offs of each approach?",
                """
                URI versioning: `/api/v1/orders` — simple, highly visible, but 'pollutes' the URI and implies the resource itself changed, not just its representation.
                Header versioning: `Accept: application/vnd.myapp.v2+json` — keeps URIs clean, but is less discoverable/harder to test with a browser.
                Query parameter: `/orders?version=2` — simple but easy to omit accidentally, so it silently falls back to a default.

                Most public APIs (Stripe, GitHub) use URI or header versioning; URI versioning is the most common because of its simplicity and visibility.
                """,
                "A practical API-design question — interviewers listen for you naming multiple approaches and their trade-offs, not just one 'correct' answer."),
            hi("What is CORS, and how do you enable it in a Spring Boot app?",
                """
                CORS (Cross-Origin Resource Sharing) is a BROWSER security mechanism that blocks a web page on one origin (domain/port) from calling an API on a different origin, unless that API explicitly allows it via response headers.

                ```java
                @CrossOrigin(origins = "https://myfrontend.com")
                @RestController
                class OrderController { ... }

                // or globally:
                @Configuration
                class CorsConfig implements WebMvcConfigurer {
                    public void addCorsMappings(CorsRegistry registry) {
                        registry.addMapping("/api/**").allowedOrigins("https://myfrontend.com");
                    }
                }
                ```
                """,
                "An extremely common real-world question — nearly every developer who's built a separate frontend + API has hit a CORS error and needed to actually understand it, not just paste a fix."),
            md("What is CSRF, and why is it usually disabled for stateless REST APIs in Spring Security?",
                "CSRF (Cross-Site Request Forgery) tricks a logged-in user's browser into submitting an unwanted request to a site they're authenticated with, exploiting the browser's automatic cookie sending. It's a real risk for COOKIE/SESSION-based authentication. Stateless REST APIs using token-based auth (like a JWT sent in an Authorization header, not a cookie) aren't vulnerable the same way, since the browser doesn't automatically attach that header the way it does cookies — so CSRF protection is commonly disabled for those APIs (while still needing to defend against other attacks).",
                "Tests whether a candidate understands the underlying attack, not just 'I disabled CSRF because tutorials do it' — a common cargo-culted config."),
            hi("Describe Spring Security's filter chain at a high level.",
                "Every incoming request passes through a chain of servlet filters BEFORE reaching your controller — filters for things like CSRF checking, CORS, authentication (extracting/validating credentials, e.g., a JWT), and authorization (checking the authenticated user's permissions against the requested resource). If any filter rejects the request (e.g., invalid token), it short-circuits with a 401/403 before your controller code ever runs. You customize this chain via a SecurityFilterChain @Bean.",
                "A foundational Spring Security question — checks whether a candidate understands security as a request-pipeline concept, not just a set of annotations."),
            hi("How does stateless JWT authentication work, end to end?",
                """
                1. User logs in with credentials; the server validates them and issues a signed JWT containing claims (user id, roles, expiry).
                2. The client stores the token and sends it in the `Authorization: Bearer <token>` header on every subsequent request.
                3. A security filter on the server verifies the token's signature (proving it wasn't tampered with) and expiry, then extracts the user's identity/roles from its claims — no database/session lookup needed.

                ```java
                String token = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("roles", user.getRoles())
                    .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
                    .signWith(secretKey)
                    .compact();
                ```
                Because the server holds no session state, any instance behind a load balancer can validate any request — that's what makes it 'stateless' and horizontally scalable.
                """,
                "One of the most common Spring Security interview questions for any API-focused role — checks real understanding of why JWT auth scales well."),
            md("What's the difference between authentication and authorization?",
                "Authentication answers 'who are you' — verifying an identity (e.g., checking a password or validating a token). Authorization answers 'what are you allowed to do' — checking whether an already-authenticated identity has permission for a specific action or resource. A request can be authenticated but still fail authorization (a valid, logged-in user trying to access an admin-only endpoint).",
                "A precise-vocabulary question that comes up constantly — many candidates use the two terms interchangeably, which is a small but real red flag."),
            hi("How do you implement role-based access control in Spring Security?",
                """
                ```java
                @RestController
                class AdminController {
                    @PreAuthorize("hasRole('ADMIN')")
                    @DeleteMapping("/users/{id}")
                    void deleteUser(@PathVariable Long id) { ... }
                }
                ```

                @PreAuthorize checks the expression BEFORE the method runs, using the authenticated user's granted authorities (roles) — enable it with @EnableMethodSecurity on a configuration class. You can also configure role-based rules centrally in the SecurityFilterChain with `.requestMatchers("/admin/**").hasRole("ADMIN")`.
                """,
                "A practical, hands-on Spring Security question — checks whether a candidate can actually write the annotation correctly, not just describe RBAC conceptually."),
            md("How do you write a custom Bean Validation constraint?",
                """
                ```java
                @Target(ElementType.FIELD)
                @Retention(RetentionPolicy.RUNTIME)
                @Constraint(validatedBy = StrongPasswordValidator.class)
                @interface StrongPassword { String message() default "Password too weak"; }

                class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
                    public boolean isValid(String password, ConstraintValidatorContext ctx) {
                        return password != null && password.length() >= 12;
                    }
                }
                ```
                """,
                "Tests whether a candidate can go beyond the built-in @NotNull/@Size annotations when business rules need custom validation logic."),
            hi("How do you turn validation failures into a clean, consistent error response instead of a raw stack trace?",
                """
                ```java
                @RestControllerAdvice
                class ValidationExceptionHandler {
                    @ExceptionHandler(MethodArgumentNotValidException.class)
                    ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
                        Map<String, String> errors = new HashMap<>();
                        ex.getBindingResult().getFieldErrors()
                            .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
                        return ResponseEntity.badRequest().body(errors);
                    }
                }
                ```
                """,
                "A very practical, commonly-needed pattern — every real production API needs this, so hands-on familiarity is expected, not just theoretical knowledge of @Valid."),
            md("What is content negotiation in REST, and how does it work with produces/consumes?",
                "Content negotiation lets a client and server agree on the representation format (JSON, XML, etc.) for a request/response, driven by the Accept and Content-Type headers. In Spring MVC, @GetMapping(produces = \"application/json\") restricts a handler to only respond when the client accepts JSON, and consumes similarly restricts which incoming Content-Type a handler will accept — letting you support multiple formats on the same URI if needed.",
                "A less commonly deeply-understood REST detail — checks whether a candidate has actually configured multi-format APIs or only ever used the JSON default."),
            md("@RequestBody vs @ModelAttribute — when do you use each?",
                "@RequestBody deserializes the raw request body (typically JSON) into a Java object — the standard choice for JSON REST APIs. @ModelAttribute binds request PARAMETERS (query string or form-encoded fields) onto an object's properties by matching names — more common in traditional form-submission MVC apps than JSON APIs.",
                "A precision question that trips up candidates coming from a purely traditional-MVC or purely REST-API background who've only ever used one of the two."),
            hi("How would you design pagination for a REST endpoint returning a large collection?",
                """
                ```java
                @GetMapping("/orders")
                Page<Order> getOrders(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
                    return orderRepository.findAll(pageable);
                }
                // GET /orders?page=0&size=20&sort=createdAt,desc
                ```

                Spring Data's Pageable/Page abstraction handles the offset/limit math and returns metadata (total elements, total pages) alongside the page of results, so clients can build proper pagination controls without you hand-rolling the logic.
                """,
                "A practical, very commonly needed pattern — returning an entire table's worth of rows in one response is a classic scalability red flag interviewers watch for."),
            md("What is rate limiting, and why would a REST API need it?",
                "Rate limiting caps how many requests a client (per API key, IP, or user) can make within a time window, protecting the API from being overwhelmed by a misbehaving client, a retry storm, or deliberate abuse — and ensuring fair usage across all clients. It's commonly implemented at a gateway/proxy layer (or with a library like Bucket4j/Resilience4j inside the app), typically returning a 429 Too Many Requests once a client exceeds their limit.",
                "A production-API-design question — checks whether a candidate thinks about abuse/overload scenarios, not just the happy path."),
            md("What is OpenAPI/Swagger, and why document your API with it?",
                "OpenAPI is a standard specification format for describing a REST API's endpoints, request/response schemas, and authentication requirements in a machine-readable way. Swagger UI renders that spec as interactive, browsable documentation. In Spring Boot, springdoc-openapi can generate the spec automatically from your controllers and DTOs — giving consumers (including frontend teams and other services) a single source of truth for exactly how to call your API, and enabling client-code generation from the spec.",
                "A practical tooling question — most real production APIs are expected to be self-documenting via OpenAPI, so hands-on familiarity is a common expectation.")));

        m.put("JFS5", List.of(
            hi("What does @Mock vs @InjectMocks do in a Mockito test?",
                "@Mock creates a fake instance of a dependency with no real behavior unless stubbed. @InjectMocks creates a real instance of the class under test and automatically injects the @Mock-annotated fields into it (via constructor, setter, or field injection).",
                "A very common Mockito syntax question — tests hands-on familiarity, not just theoretical knowledge of mocking."),
            hi("Why is testing private methods generally discouraged, and how should you test them instead?",
                "Private methods are implementation details; testing them directly couples tests to internals and makes refactoring brittle. Instead, test the public behavior/methods that use the private method — if the private logic is complex enough to need its own tests, that's often a sign it should be extracted into its own testable class.",
                "A design-judgment question that reveals whether a candidate tests behavior or implementation."),
            md("What's the purpose of test coverage, and why is 100% coverage not necessarily a good goal?",
                "Coverage measures which lines/branches execute during tests, helping find untested code — but high coverage doesn't guarantee good tests (you can execute a line without meaningfully asserting on its behavior). Chasing 100% often wastes effort on trivial code (getters/setters) while missing edge-case and integration-level bugs.",
                "Tests maturity about metrics — a common trap is treating coverage percentage as a proxy for quality."),
            hi("What's the difference between a unit test and an integration test?",
                "A unit test exercises one class/method in complete isolation, with all its dependencies mocked or faked — fast, focused, and pinpoints exactly what broke. An integration test exercises multiple real components working together (e.g., a real database, a real Spring context) — slower, but catches issues unit tests structurally can't see, like a broken query or misconfigured wiring. A healthy test suite needs both, usually many more unit tests than integration tests (the 'test pyramid').",
                "A foundational testing-strategy question — checks whether a candidate can articulate WHY you need both types, not just define the terms."),
            hi("What does @SpringBootTest do, and why is it 'too heavy' to use for every test?",
                """
                ```java
                @SpringBootTest
                class OrderServiceIntegrationTest {
                    @Autowired OrderService orderService;
                    // boots the FULL Spring application context — every bean, real or test-configured
                }
                ```

                It starts the entire application context, which is slow — doing this for every single test class makes a large test suite take minutes instead of seconds. For most tests, a narrower "sliced" test (like @WebMvcTest or @DataJpaTest, which only load the relevant layer) or a plain unit test with Mockito is faster and just as effective.
                """,
                "A practical test-suite-performance question — overusing @SpringBootTest everywhere is a very common real-world anti-pattern interviewers ask about."),
            hi("What's the difference between @WebMvcTest and @DataJpaTest?",
                """
                ```java
                @WebMvcTest(OrderController.class)   // loads ONLY the web layer — controller, filters, JSON serialization
                class OrderControllerTest {
                    @Autowired MockMvc mockMvc;
                    @MockBean OrderService orderService;  // service layer is mocked, not real
                }

                @DataJpaTest                          // loads ONLY the JPA/repository layer, with an in-memory DB
                class OrderRepositoryTest {
                    @Autowired OrderRepository orderRepository;
                }
                ```

                Both are "slice" tests — they boot a much smaller, faster subset of the application context than @SpringBootTest, targeted at exactly the layer you're testing.
                """,
                "Checks whether a candidate knows Spring Boot's test slicing feature exists — a strong signal of real test-suite performance awareness."),
            hi("How would you test a REST controller using MockMvc?",
                """
                ```java
                @WebMvcTest(OrderController.class)
                class OrderControllerTest {
                    @Autowired MockMvc mockMvc;
                    @MockBean OrderService orderService;

                    @Test
                    void returnsOrderById() throws Exception {
                        when(orderService.findById(1L)).thenReturn(new Order(1L, "shipped"));

                        mockMvc.perform(get("/orders/1"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.status").value("shipped"));
                    }
                }
                ```
                """,
                "A very hands-on, commonly-expected skill — checks whether a candidate can actually write a controller test, not just talk about testing in the abstract."),
            hi("Mockito's when().thenReturn() vs doReturn().when() — what's the difference, and when do you need doReturn?",
                """
                ```java
                // Standard style — works for normal method calls:
                when(mockList.get(0)).thenReturn("first");

                // doReturn style — needed when when() would actually execute real code,
                // e.g. stubbing a void method, or a spy wrapping a REAL object:
                List<String> spyList = spy(new ArrayList<>());
                doReturn(99).when(spyList).size();   // when(spyList.size())... would run the REAL size() first
                ```
                """,
                "A precise Mockito question that catches candidates who've only ever used the common case — the spy scenario is the detail that shows real hands-on debugging experience."),
            md("Mock vs stub vs spy vs fake — what's the difference between these test doubles?",
                "A mock is a fake object you set expectations on and can verify was called in specific ways (verify(mock).method()). A stub returns canned answers but you don't typically verify interactions on it, just use its output. A spy wraps a REAL object, letting real methods run by default while you selectively override specific ones. A fake is a real, simplified working implementation (like an in-memory database standing in for a real one) — functional, just not production-grade.",
                "A vocabulary-precision question — many candidates use 'mock' as a catch-all term for all four, which is a minor but real signal during senior-level interviews."),
            md("What is Testcontainers, and why use it over an in-memory database like H2 for integration tests?",
                "Testcontainers spins up real Docker containers (an actual PostgreSQL, Kafka, Redis, etc.) for the duration of a test run, then tears them down automatically. Unlike an in-memory substitute like H2, you're testing against the ACTUAL database engine you run in production — catching SQL dialect differences, specific data types, and constraint behaviors that an in-memory stand-in would never surface, at the cost of somewhat slower test startup.",
                "A modern, increasingly-expected testing tool — signals awareness of a real gap in the 'just use H2 for tests' shortcut."),
            hi("What is the Arrange-Act-Assert (AAA) pattern, and why structure tests this way?",
                """
                ```java
                @Test
                void discountAppliesToOrdersOver100() {
                    // Arrange — set up the test data and dependencies
                    Order order = new Order(150.0);

                    // Act — perform the actual action being tested
                    double total = pricingService.applyDiscount(order);

                    // Assert — verify the outcome
                    assertEquals(135.0, total);
                }
                ```

                Structuring every test the same way makes tests fast to read and review — anyone can immediately see setup vs the action under test vs the expected outcome, without hunting through mixed logic.
                """,
                "A basic but real code-quality question — well-structured, readable tests are a genuine signal of engineering maturity."),
            md("What do @BeforeEach and @AfterEach do in JUnit 5?",
                """
                ```java
                class OrderServiceTest {
                    OrderService orderService;

                    @BeforeEach
                    void setUp() { orderService = new OrderService(new InMemoryOrderRepo()); }  // runs before EVERY test method

                    @AfterEach
                    void tearDown() { /* cleanup, e.g. close a resource */ }  // runs after EVERY test method
                }
                ```
                """,
                "Basic JUnit fluency — expected baseline knowledge for any Java developer claiming testing experience."),
            hi("What is a parameterized test in JUnit 5, and why use one?",
                """
                ```java
                @ParameterizedTest
                @ValueSource(ints = {2, 4, 6, 8})
                void isEven(int number) {
                    assertTrue(number % 2 == 0);
                }

                @ParameterizedTest
                @CsvSource({"2,4", "3,9", "4,16"})
                void square(int input, int expected) {
                    assertEquals(expected, input * input);
                }
                ```

                It runs the same test logic against many different inputs without copy-pasting the test method — keeping the suite DRY and making it trivial to add a new edge case as just one more line of input data.
                """,
                "Shows real fluency with JUnit 5 features beyond the basics — commonly used for testing many edge cases of a pure function efficiently."),
            md("assertEquals vs assertSame in JUnit — what's the difference?",
                "assertEquals checks logical/value equality (calls .equals()) — the standard choice for comparing most objects, including two different String or record instances with the same content. assertSame checks reference/identity equality (==) — that both variables point to the EXACT same object in memory, which is rarely what you actually want to test unless you're specifically verifying object identity (like caching behavior).",
                "A precision question mirroring the ==/equals() distinction — checks whether the concept transferred from 'Java fundamentals' into actual testing practice."),
            md("What makes a test 'flaky,' and how do you fix one?",
                "A flaky test passes and fails intermittently without any code changes — usually caused by relying on real time/sleep() delays, shared mutable state leaking between tests, unordered collections asserted in a specific order, or genuine race conditions in async/concurrent code under test. The fix is almost always removing the source of non-determinism: inject a fake clock instead of Thread.sleep(), ensure proper test isolation/cleanup, and use polling/await-style assertions instead of fixed delays for async code.",
                "A real production-experience question — anyone who's worked on a sizable test suite has fought a flaky test, and interviewers listen for a genuine diagnostic story.")));

        m.put("JFS6", List.of(
            hi("What problem does service discovery solve in a microservices architecture?",
                "In a dynamic environment where service instances scale up/down and get new IPs, hardcoding addresses breaks. A service registry (e.g., Eureka, Consul, or Kubernetes' built-in DNS) lets services register themselves and look up other services by name, so callers don't need to know static locations.",
                "A common microservices-fundamentals question once a candidate mentions 'multiple services calling each other.'"),
            hi("What is the difference between horizontal and vertical scaling for a Spring Boot service, and which does containerization favor?",
                "Vertical: give one instance more CPU/RAM. Horizontal: run more instances behind a load balancer. Containerization (Docker/Kubernetes) is built around horizontal scaling — spinning up more identical, stateless container instances — since it's cheap and elastic compared to resizing a running machine.",
                "Ties Java-specific deployment knowledge to general system-design fundamentals — a common cross-topic interview question."),
            md("What health-check endpoints does Spring Boot Actuator provide, and why do orchestrators like Kubernetes need them?",
                "Actuator exposes /actuator/health (and readiness/liveness variants) reporting whether the app and its dependencies (DB, disk, etc.) are healthy. Kubernetes uses liveness probes to know when to restart a stuck container, and readiness probes to know when a container is ready to receive traffic — without these, it can route traffic to a container that isn't actually ready.",
                "A practical, production-deployment question that distinguishes hands-on Kubernetes/Spring Boot experience from textbook knowledge."),
            hi("What problem does an API Gateway solve in a microservices architecture?",
                "It's a single entry point that sits in front of all your microservices, handling cross-cutting concerns ONCE instead of duplicating them in every service: routing requests to the right downstream service, authentication/authorization, rate limiting, request/response logging, and sometimes response aggregation from multiple services. Without it, clients would need to know about and directly call every individual service, and every service would need to reimplement the same auth/rate-limiting logic.",
                "A foundational microservices-architecture question — almost every real microservices system has some form of gateway, so hands-on awareness is expected."),
            hi("What is the Circuit Breaker pattern, and how do you implement one with Resilience4j?",
                """
                ```java
                @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
                public PaymentResult charge(Order order) {
                    return paymentClient.charge(order);
                }

                public PaymentResult paymentFallback(Order order, Throwable t) {
                    return PaymentResult.deferred(order.getId());  // graceful degradation instead of cascading failure
                }
                ```

                After enough consecutive failures calling a downstream service, the circuit "opens" and further calls fail FAST (immediately, calling the fallback) instead of repeatedly waiting on a timeout against a service that's clearly down — protecting your own service from being dragged down too. After a cooldown, it allows a few trial calls through to see if the dependency has recovered.
                """,
                "One of the most important resilience patterns in distributed systems — a near-guaranteed question once 'microservices' comes up, since without it one failing service can cascade and take down everything calling it."),
            md("What is the Saga pattern, and what problem does it solve?",
                "A traditional ACID transaction can't span multiple databases owned by different microservices. A Saga breaks a multi-step business transaction (like 'place order -> charge payment -> reserve inventory') into a sequence of local transactions, each in its own service, with a COMPENSATING action defined for each step to undo it if a later step fails (e.g., if inventory reservation fails, refund the payment that already succeeded). It trades true atomicity for eventual consistency across services.",
                "A senior-level distributed-systems question — checks whether a candidate has actually grappled with the 'no cross-service transactions' problem, not just heard the term."),
            md("What is eventual consistency, and why do microservices architectures often have to accept it?",
                "Eventual consistency means that after an update, different parts of the system may see stale data for a short period, but will converge to the same, correct state given enough time (with no further updates). Microservices often accept this because enforcing strict, immediate consistency across independently-deployed, separately-databased services would require distributed transactions/locks that kill availability and scalability — the trade-off (per the CAP theorem) is usually made deliberately in favor of availability and partition tolerance.",
                "Ties directly back to core distributed-systems/CAP-theorem fundamentals — a common cross-topic question once microservices data consistency comes up."),
            md("Orchestration vs choreography — two approaches to coordinating a multi-service workflow. What's the difference?",
                "Orchestration: a central coordinator service explicitly calls each participating service in sequence and manages the overall workflow state — easier to understand and debug (one place to look), but that coordinator becomes a central point of complexity/coupling. Choreography: each service reacts to events published by others, with no central coordinator — more decoupled, but the overall flow of 'what happens when' becomes implicit, spread across many services, and harder to trace.",
                "A real architectural trade-off question with no single right answer — interviewers listen for you to articulate BOTH sides, not just name one as universally better."),
            hi("Why does distributed tracing matter in microservices in a way it doesn't in a monolith?",
                "In a monolith, a single request's entire call stack lives in one process — a stack trace or debugger shows you everything. In microservices, one user request might fan out across five, ten, or more services, each with its own logs — without a shared trace ID propagated through every hop (and a tool like Zipkin/Jaeger/OpenTelemetry to visualize it), you have no way to reconstruct the full path a slow or failing request took, or which specific service in the chain was actually responsible.",
                "A practical operational-debugging question — anyone who's actually operated a microservices system in production has needed distributed tracing to debug a real incident."),
            md("What is the strangler fig pattern, and why is it used for migrating a monolith to microservices?",
                "It means incrementally routing specific pieces of functionality from an old monolith to new microservices (often via a gateway/proxy that decides which system handles each request), while the monolith keeps running for everything not yet migrated — rather than attempting a risky, all-at-once big-bang rewrite. Over time, more and more functionality moves to the new services until the monolith is fully 'strangled' and can be retired.",
                "A pragmatic, real-world migration-strategy question — signals whether a candidate has thought about migration risk, not just greenfield microservices design."),
            md("What is config externalization (e.g., Spring Cloud Config), and why centralize configuration across microservices?",
                "Rather than each service bundling its own configuration file, a config server centrally stores and serves configuration (often backed by a git repo) that every service fetches at startup (and optionally refreshes at runtime). This lets you change configuration — feature flags, connection strings, rate limits — across many services consistently, without rebuilding/redeploying each one, and gives you a single audited source of truth for what's configured where.",
                "A practical operations question for anyone running more than a couple of microservices — hardcoded or scattered per-service config is a common early-stage pain point."),
            md("What is the outbox pattern, and what problem does it solve?",
                "When a service needs to both update its own database AND publish an event about that change (e.g., 'order created'), doing both as two separate operations risks inconsistency if the process crashes between them (DB updated but event never published, or vice versa). The outbox pattern writes the event into an 'outbox' table IN THE SAME LOCAL DATABASE TRANSACTION as the actual data change, then a separate process reliably publishes events from that outbox table to the message broker — guaranteeing the event is eventually published if and only if the data change actually committed.",
                "A senior-level distributed-systems reliability question — checks whether a candidate has grappled with the 'dual write' problem, a subtle but very real production bug source."),
            hi("How do you make a message consumer idempotent, and why does it matter?",
                """
                Message queues typically guarantee AT-LEAST-ONCE delivery — the same message can be delivered and processed more than once (e.g., after a consumer crash before it acknowledges). An idempotent consumer produces the same end result no matter how many times the same message is processed.

                ```java
                @KafkaListener(topics = "order-events")
                void handle(OrderEvent event) {
                    if (processedEventRepository.existsById(event.getId())) return; // already handled — skip
                    applyOrderEvent(event);
                    processedEventRepository.save(new ProcessedEvent(event.getId()));
                }
                ```
                """,
                "A very practical, commonly-tested reliability question — 'what if this message gets delivered twice' is close to a guaranteed follow-up once message queues come up."),
            md("What is backpressure, and why does it matter for a service consuming from a queue or stream?",
                "Backpressure is a mechanism for a consumer to signal 'slow down' to a producer/upstream when it can't keep up with the incoming rate, instead of silently dropping messages or running out of memory buffering an ever-growing backlog. Without it, a temporary slowdown in one service (e.g., a downstream DB getting slow) can cascade into an unbounded queue buildup or an out-of-memory crash — reactive frameworks (like Project Reactor) and message brokers both provide explicit backpressure mechanisms to handle this gracefully.",
                "A more advanced reliability question — checks whether a candidate has thought about what happens when a consumer is slower than its producer, a very real production failure mode."),
            md("What is the bulkhead pattern?",
                "Named after ship compartments that stop one breach from sinking the whole ship — it means isolating resources (like thread pools or connection pools) PER downstream dependency, so if one dependency becomes slow or fails, it can only exhaust its own dedicated pool, not starve resources needed to call every OTHER, unrelated dependency. Without bulkheads, one slow downstream call can consume the entire shared thread pool and take down calls to completely unrelated services too.",
                "Often paired with the Circuit Breaker question — checks whether a candidate understands resource isolation as a distinct, complementary resilience concern.")));

        // ================= Python =================
        m.put("PY1", List.of(
            hi("Python is dynamically typed — what does that actually mean, and how does it differ from static typing?",
                """
                A variable's type is determined by whatever object it currently references, checked at RUNTIME — not declared or checked at compile time.

                ```python
                x = 5          # x refers to an int
                x = "hello"    # now x refers to a str — perfectly legal, no compiler complaint
                ```

                Statically-typed languages (Java, C#) require a declared type that's checked before the program ever runs, catching type mismatches earlier but requiring more upfront declaration.
                """,
                "The most fundamental Python-vs-other-languages question — checks whether a candidate understands the trade-off, not just the vocabulary."),
            hi("What is the classic 'mutable default argument' bug in Python, and how do you avoid it?",
                """
                A default argument is evaluated exactly ONCE, when the function is DEFINED — not on every call. If that default is a mutable object like a list, every call that doesn't pass its own value shares and mutates the SAME object.

                ```python
                def add_item(item, cart=[]):   # BUG: cart is created once, shared across all calls
                    cart.append(item)
                    return cart

                add_item("apple")   # ['apple']
                add_item("banana")  # ['apple', 'banana']  <- unexpected! same list as before

                def add_item_fixed(item, cart=None):
                    if cart is None:
                        cart = []
                    cart.append(item)
                    return cart
                ```
                """,
                "One of THE most famous Python interview 'gotcha' questions — appears in almost every serious Python interview, specifically to catch this exact bug pattern."),
            hi("Explain Python's LEGB rule for variable scope resolution.",
                """
                LEGB = Local -> Enclosing -> Global -> Built-in — the order Python searches when resolving a name.

                ```python
                x = "global"
                def outer():
                    x = "enclosing"
                    def inner():
                        x = "local"
                        print(x)   # "local" — found immediately in Local scope
                    inner()

                def needs_global():
                    global x
                    x = "changed"   # without `global`, this would create a NEW local variable instead
                ```
                """,
                "Tests real understanding of Python's scoping model — the `global`/`nonlocal` keyword nuance is a common, sharp follow-up."),
            hi("What's the difference between == and is in Python?",
                """
                ```python
                a = [1, 2, 3]
                b = [1, 2, 3]
                a == b   # True  — same VALUE/content (calls __eq__)
                a is b   # False — different OBJECTS in memory

                c = a
                c is a   # True — c and a reference the exact same object
                ```

                Small integers (-5 to 256) and some interned strings may be `is`-equal due to CPython caching them — but that's an implementation detail you should never rely on; always use == for value comparison.
                """,
                "A near-universal Python fundamentals question — checks precise understanding of identity vs equality."),
            md("What are Python's core built-in data types?",
                "Numeric: int, float, complex. Sequence: str, list, tuple, range. Mapping: dict. Set types: set, frozenset. Boolean: bool. And NoneType for the single `None` value. Understanding which are mutable (list, dict, set) vs immutable (int, float, str, tuple, frozenset) matters constantly in practice — for example, only immutable types can be used as dict keys or set members.",
                "A warm-up fundamentals question — the mutable/immutable distinction is the part interviewers actually care about."),
            md("What is duck typing, and how does it relate to Python's approach to types?",
                "\"If it walks like a duck and quacks like a duck, it's a duck\" — Python cares about whether an object SUPPORTS the operations/methods you call on it, not its declared type or class hierarchy. A function that calls obj.read() will happily accept any object with a read() method, whether it's a file, a network stream, or a custom class — there's no need for it to formally implement a shared interface.",
                "A conceptual Python-philosophy question — often paired with 'how is this different from Java's interface-based polymorphism.'"),
            hi("How does Python pass arguments to functions — by value or by reference?",
                """
                Neither, exactly — Python passes "by object reference" (sometimes called "by assignment"). The function gets a reference to the SAME object; reassigning the parameter inside the function doesn't affect the caller's variable, but MUTATING a mutable object does.

                ```python
                def reassign(lst):
                    lst = [9, 9, 9]   # rebinds the LOCAL name only — caller's list is untouched

                def mutate(lst):
                    lst.append(9)     # mutates the SAME object the caller sees

                nums = [1, 2, 3]
                reassign(nums); print(nums)  # [1, 2, 3] — unchanged
                mutate(nums); print(nums)    # [1, 2, 3, 9] — changed!
                ```
                """,
                "A genuinely tricky, very common Python semantics question — the reassign-vs-mutate distinction is exactly what separates real understanding from surface familiarity."),
            md("f-strings vs .format() vs % formatting — what are the differences?",
                """
                ```python
                name, age = "Sam", 30
                f"{name} is {age}"          # f-string (3.6+): fastest, most readable, evaluates expressions inline
                "{} is {}".format(name, age) # .format(): flexible, works pre-3.6, more verbose
                "%s is %d" % (name, age)     # %-formatting: oldest style, printf-like, least readable for complex cases
                ```

                f-strings are now the idiomatic default in modern Python — they're evaluated at runtime as real expressions, so you can even call functions inline: `f"{name.upper()}"`.
                """,
                "A practical, very commonly asked syntax-fluency question — checks current idiomatic knowledge vs outdated style."),
            hi("What do *args and **kwargs mean in a function signature?",
                """
                ```python
                def example(*args, **kwargs):
                    print(args)     # tuple of extra positional arguments
                    print(kwargs)   # dict of extra keyword arguments

                example(1, 2, name="Sam", age=30)
                # args   -> (1, 2)
                # kwargs -> {'name': 'Sam', 'age': 30}
                ```

                They let a function accept a variable, unknown-ahead-of-time number of positional (*args) and keyword (**kwargs) arguments — commonly used for wrapper/decorator functions that need to forward whatever arguments they receive.
                """,
                "Extremely common in real Python code, especially decorators and wrapper functions — fluency here is close to a baseline expectation."),
            md("What's the difference between a Python module, a package, and a library?",
                "A module is a single .py file. A package is a directory of modules containing an __init__.py file, letting you organize related modules under one importable namespace (e.g., `requests.auth`). A library (or distribution) is a published, installable collection of one or more packages — what you actually `pip install` — like the `requests` library, which itself contains multiple packages/modules.",
                "A basic Python-project-structure question — checks whether a candidate can talk precisely about how Python code is organized, not just 'I import stuff.'"),
            md("Why is range() in Python 3 described as lazy, and how is that different from Python 2?",
                "In Python 3, range() returns a lightweight range object that generates numbers on demand as you iterate — it doesn't build a full list in memory upfront, so range(10_000_000) is cheap regardless of size. In Python 2, range() eagerly built and returned a full list immediately (xrange() was the lazy alternative back then). Python 3 unified on the efficient, lazy behavior as the default.",
                "A 'do you know Python 2 vs 3 history' question — less critical today but still surfaces, especially at companies with legacy codebases."),
            hi("What counts as falsy in Python, beyond just False and None?",
                """
                ```python
                bool(0)        # False
                bool(0.0)      # False
                bool("")       # False — empty string
                bool([])       # False — empty list
                bool({})       # False — empty dict
                bool(())       # False — empty tuple
                bool(None)     # False
                bool(False)    # False

                if my_list:            # idiomatic — True only if my_list is non-empty
                    process(my_list)
                ```
                """,
                "A practical idiom question — Python code relies heavily on truthy/falsy checks instead of explicit `len(x) > 0` or `x is not None`, so real fluency here matters."),
            hi("What does the walrus operator := do (Python 3.8+)?",
                """
                ```python
                # Without walrus — calls len() twice
                data = get_data()
                if len(data) > 10:
                    print(len(data))

                # With walrus — assigns AND evaluates in one expression
                if (n := len(get_data())) > 10:
                    print(n)
                ```

                It lets you assign a value to a variable AS PART OF a larger expression (like a while/if condition), avoiding a redundant separate assignment or a repeated function call.
                """,
                "A newer-syntax question — checks whether a candidate's Python knowledge extends past older versions, a real signal at companies using current Python."),
            md("What is PEP 8, and why does it matter in a professional setting?",
                "PEP 8 is Python's official style guide — conventions for naming (snake_case for functions/variables, PascalCase for classes), indentation, line length, import ordering, and more. It matters because consistent style across a codebase reduces cognitive load during code review and lets any Python developer read unfamiliar code without adjusting to a different personal style — most teams enforce it automatically via a linter (flake8, ruff) or formatter (black) rather than manual review.",
                "A professionalism/tooling-awareness question — checks whether a candidate has worked on a real team codebase with enforced conventions, not just solo scripts."),
            md("What is the difference between a list and Python's array module — does Python have 'real' arrays?",
                "Python's built-in list is a flexible, dynamically-resizable, heterogeneous (mixed-type) container. The array module provides a more memory-efficient, FIXED-TYPE array (all elements must be the same primitive type, like all ints) — closer to a C array — used occasionally for memory-sensitive numeric work, though in practice most numeric-heavy Python code reaches for NumPy arrays instead, which are far more capable and optimized than the built-in array module.",
                "Tests whether a candidate knows Python's list is NOT the same thing as a low-level array, and what the actual alternatives are for numeric work.")));

        m.put("PY2", List.of(
            hi("list vs tuple vs set vs dict — when do you reach for each?",
                """
                ```python
                nums = [1, 2, 3]              # list: ordered, mutable — general-purpose sequence
                point = (10, 20)               # tuple: ordered, immutable — fixed records, hashable (usable as a dict key)
                unique_ids = {101, 102, 103}   # set: unordered, unique elements, O(1) average membership test
                user = {"name": "Sam", "age": 30}  # dict: key-value mapping, O(1) average lookup by key
                ```
                """,
                "One of the most fundamental Python questions — checks whether a candidate reaches for the right collection instead of defaulting to lists for everything."),
            hi("Explain Python slicing: what does a[1:4:2] mean, and what does a[::-1] do?",
                """
                ```python
                a = [0, 1, 2, 3, 4, 5, 6, 7]
                a[1:4]     # [1, 2, 3]        — start at 1, stop before 4
                a[1:4:2]   # [1, 3]           — start at 1, stop before 4, take every 2nd element
                a[::-1]    # [7, 6, 5, 4, 3, 2, 1, 0]  — reversed (step -1, default start/stop)
                a[:3]      # [0, 1, 2]        — start defaults to 0
                a[-2:]     # [6, 7]           — negative indices count from the end
                ```
                """,
                "Extremely common — checks whether a candidate is fluent with slicing syntax, one of Python's most-used features, not just aware it exists."),
            hi("How do you write a list comprehension, and when should you NOT use one?",
                """
                ```python
                squares = [x*x for x in range(10) if x % 2 == 0]   # squares of even numbers

                # equivalent loop:
                squares = []
                for x in range(10):
                    if x % 2 == 0:
                        squares.append(x*x)
                ```

                Comprehensions are idiomatic and often faster for simple filter/transform logic. Once you need more than one or two conditions, nested loops, or side effects, a comprehension becomes a wall of hard-to-read code — a plain loop (or a named helper function) is the better choice at that point.
                """,
                "A very commonly asked syntax + judgment question — the 'when NOT to' half is what separates strong answers from rote memorization."),
            hi("What's the difference between a shallow copy and a deep copy?",
                """
                ```python
                import copy
                original = [[1, 2], [3, 4]]

                shallow = original.copy()          # or list(original), or copy.copy(original)
                shallow[0].append(99)
                print(original)   # [[1, 2, 99], [3, 4]] — the INNER list was shared, so this mutated it too!

                deep = copy.deepcopy(original)
                deep[0].append(100)
                print(original)   # unaffected — deepcopy recursively copied the nested lists too
                ```
                """,
                "A real, commonly-hit bug pattern — checks whether a candidate has actually been bitten by shallow-copy semantics, not just read about them."),
            hi("How do you remove duplicates from a list while preserving order?",
                """
                ```python
                items = [3, 1, 2, 3, 1, 4]

                # dict.fromkeys preserves insertion order (Python 3.7+ dicts are ordered)
                unique = list(dict.fromkeys(items))   # [3, 1, 2, 4]

                # a plain set() would remove duplicates but LOSE ordering:
                # list(set(items)) -> order not guaranteed
                ```
                """,
                "A practical, frequently-asked coding question — checks whether a candidate knows the naive `list(set(x))` approach loses order, and the fix."),
            hi("What is a namedtuple, and why use it over a plain tuple?",
                """
                ```python
                from collections import namedtuple

                Point = namedtuple("Point", ["x", "y"])
                p = Point(3, 4)
                p.x        # 3 — access by NAME, not just p[0]
                p[0]       # 3 — still works positionally too
                ```

                A namedtuple gives you readable, self-documenting field access while remaining as lightweight and immutable as a regular tuple — much clearer than remembering "index 0 is x, index 1 is y" scattered through code.
                """,
                "Tests knowledge of a commonly-underused but genuinely useful standard-library tool for readable, lightweight data records."),
            hi("dict.get() vs dict[] — what's the difference in how they handle a missing key?",
                """
                ```python
                user = {"name": "Sam"}

                user["age"]           # raises KeyError — crashes if the key doesn't exist
                user.get("age")       # None — returns None instead of crashing
                user.get("age", 0)    # 0    — returns your specified default instead
                ```
                """,
                "A very practical, commonly-needed distinction — using [] where .get() is appropriate is a common source of unhandled KeyError crashes."),
            hi("What is a defaultdict, and what problem does it solve?",
                """
                ```python
                from collections import defaultdict

                counts = defaultdict(int)          # default value for a missing key is int() -> 0
                for word in ["a", "b", "a", "c", "a"]:
                    counts[word] += 1               # no need to check "if word not in counts" first
                # counts -> {'a': 3, 'b': 1, 'c': 1}

                groups = defaultdict(list)
                groups["fruits"].append("apple")    # auto-creates an empty list on first access
                ```
                """,
                "A very commonly used standard-library tool for counting/grouping — checks familiarity with idiomatic Python beyond plain dicts."),
            md("How do you merge two dictionaries in Python?",
                """
                ```python
                a = {"x": 1, "y": 2}
                b = {"y": 99, "z": 3}

                merged = {**a, **b}          # {'x': 1, 'y': 99, 'z': 3} — b's values win on key conflicts
                merged = a | b               # Python 3.9+ merge operator, same result
                a.update(b)                  # mutates `a` in place instead of creating a new dict
                ```
                """,
                "A practical syntax question — the 3.9+ `|` merge operator is a good signal of current Python knowledge."),
            md("What's the time complexity of common list operations in Python?",
                "append() is O(1) amortized (occasionally O(n) when the underlying array needs to grow/reallocate). Indexing (list[i]) is O(1). insert(0, x) and pop(0) are O(n) — everything after the insertion/removal point has to shift. `in` (membership test, `x in my_list`) is O(n) — it scans linearly, which is why a set (O(1) average membership) is usually the better choice when you're checking membership repeatedly.",
                "A practical performance-awareness question — the insert(0,...)/pop(0) being O(n) surprises many candidates who assume all list operations are fast."),
            hi("What is Counter from the collections module, and when would you use it?",
                """
                ```python
                from collections import Counter

                words = ["a", "b", "a", "c", "a", "b"]
                counts = Counter(words)
                counts               # Counter({'a': 3, 'b': 2, 'c': 1})
                counts.most_common(2) # [('a', 3), ('b', 2)]
                ```

                It's purpose-built for exactly the "count occurrences of each item" pattern — cleaner and more capable (most_common, arithmetic between counters) than manually building a dict and incrementing values.
                """,
                "A commonly-used standard-library shortcut — checks whether a candidate reaches for the right tool instead of reinventing counting logic."),
            hi("How does sort() differ from sorted()? Is Python's sort stable?",
                """
                ```python
                nums = [3, 1, 2]
                nums.sort()              # mutates `nums` IN PLACE, returns None
                new_list = sorted(nums)  # returns a NEW sorted list, leaves the original untouched

                people = [("Sam", 30), ("Ann", 25), ("Sam", 20)]
                people.sort(key=lambda p: p[0])   # sort by name using a key function
                ```

                Yes — Python's sort is stable (Timsort): elements that compare equal keep their original relative order, which matters when you sort by one key after already having sorted by another.
                """,
                "A precise, commonly-asked question — the stability guarantee is a real, useful detail many candidates don't know to mention."),
            md("How does unpacking work with * in Python?",
                """
                ```python
                first, *middle, last = [1, 2, 3, 4, 5]
                # first = 1, middle = [2, 3, 4], last = 5

                def total(*nums): return sum(nums)
                values = [1, 2, 3]
                total(*values)     # unpacks the list as separate positional arguments -> total(1, 2, 3)
                ```
                """,
                "A practical syntax-fluency question — extended unpacking and argument-unpacking are used constantly in idiomatic Python."),
            hi("What is a set comprehension, and give a real use case.",
                """
                ```python
                words = ["apple", "Banana", "APPLE", "cherry", "banana"]
                unique_lower = {w.lower() for w in words}   # {'apple', 'banana', 'cherry'}
                ```

                Useful whenever you need unique results (not caring about order or duplicates) from a transformation — here, normalizing case while automatically deduplicating.
                """,
                "Checks whether a candidate knows comprehension syntax extends beyond lists — a natural follow-up to the list-comprehension question."),
            hi("How would you flatten a nested list in Python?",
                """
                ```python
                nested = [[1, 2], [3, 4], [5]]

                flat = [x for sub in nested for x in sub]   # [1, 2, 3, 4, 5]

                # for arbitrarily deep nesting, a recursive approach is needed:
                def flatten(lst):
                    result = []
                    for item in lst:
                        if isinstance(item, list):
                            result.extend(flatten(item))
                        else:
                            result.append(item)
                    return result
                ```
                """,
                "A common live-coding exercise — checks whether a candidate can write nested comprehensions correctly, and reason about the deeper recursive case.")));

        m.put("PY3", List.of(
            hi("What do __init__ and self actually do in a Python class?",
                """
                ```python
                class Dog:
                    def __init__(self, name):   # called automatically right after a new instance is created
                        self.name = name         # self IS the instance — this sets an attribute ON it

                    def bark(self):
                        print(f"{self.name} says woof")

                fido = Dog("Fido")   # __init__(fido, "Fido") happens under the hood
                fido.bark()          # bark(fido) happens under the hood
                ```
                """,
                "A foundational OOP question — checks whether a candidate understands self is just the instance, passed automatically, not special magic syntax."),
            hi("What is Method Resolution Order (MRO), and why does it matter with multiple inheritance?",
                """
                ```python
                class A:
                    def greet(self): return "A"
                class B(A):
                    def greet(self): return "B"
                class C(A):
                    def greet(self): return "C"
                class D(B, C):
                    pass

                D().greet()       # "B" — MRO checks D, then B, then C, then A
                D.__mro__         # (D, B, C, A, object) — inspectable directly
                ```

                MRO is the order Python searches base classes to resolve a method/attribute, computed via C3 linearization — it matters because with multiple inheritance, more than one parent could define the same method, and MRO deterministically decides which one wins.
                """,
                "A senior-level Python OOP question — most candidates know multiple inheritance exists but few can correctly explain or predict MRO."),
            hi("What do __eq__ and __hash__ do, and why must they stay consistent?",
                """
                ```python
                class Point:
                    def __init__(self, x, y):
                        self.x, self.y = x, y
                    def __eq__(self, other):
                        return isinstance(other, Point) and (self.x, self.y) == (other.x, other.y)
                    def __hash__(self):
                        return hash((self.x, self.y))

                p1, p2 = Point(1, 2), Point(1, 2)
                p1 == p2              # True  — custom __eq__
                {p1, p2}               # a set with just ONE element — because they're equal AND hash equally
                ```

                If two objects are equal (__eq__ True) but have different hashes, they'll silently break when used in a set or as dict keys — Python's contract requires equal objects to hash equally.
                """,
                "A precise, commonly-tested Python data-model question — the consistency requirement is the part most candidates miss."),
            hi("What does the @property decorator do, and why use it instead of plain getter/setter methods?",
                """
                ```python
                class Circle:
                    def __init__(self, radius):
                        self._radius = radius

                    @property
                    def area(self):
                        return 3.14159 * self._radius ** 2

                c = Circle(5)
                c.area        # accessed like a plain attribute — no parentheses — but computed on the fly
                ```

                It lets you start with plain public attributes and add validation or computed logic later without breaking the class's external API — callers never need to change `obj.value` to `obj.get_value()`.
                """,
                "A very common, practical Python idiom question — checks fluency with Pythonic encapsulation, distinct from Java-style explicit getters."),
            hi("classmethod vs staticmethod vs a regular instance method — what's the difference?",
                """
                ```python
                class Pizza:
                    def __init__(self, toppings):
                        self.toppings = toppings

                    def describe(self):                          # instance method — needs `self`, an actual instance
                        return f"Pizza with {self.toppings}"

                    @classmethod
                    def margherita(cls):                          # classmethod — receives the CLASS, not an instance
                        return cls(["cheese", "tomato"])           # common use: alternative constructors

                    @staticmethod
                    def is_valid_topping(topping):                 # staticmethod — receives NEITHER self nor cls
                        return topping in ("cheese", "pepperoni", "mushroom")
                ```
                """,
                "A very common Python OOP question — the classmethod-as-alternative-constructor pattern is the detail that shows real practical usage."),
            hi("What's the difference between __str__ and __repr__?",
                """
                ```python
                class Point:
                    def __init__(self, x, y): self.x, self.y = x, y
                    def __str__(self): return f"({self.x}, {self.y})"          # readable, for END USERS
                    def __repr__(self): return f"Point(x={self.x}, y={self.y})" # unambiguous, for DEVELOPERS/debugging

                print(Point(1, 2))       # uses __str__  -> "(1, 2)"
                [Point(1, 2)]             # uses __repr__ (inside a list/repr context) -> [Point(x=1, y=2)]
                ```

                Convention: __repr__ should ideally be valid Python that could recreate the object; __str__ is for friendly display. If only __repr__ is defined, Python falls back to it for str() too.
                """,
                "A very commonly asked Python data-model question — many candidates only define one and don't know the fallback behavior or the intended distinction."),
            hi("How do you call a parent class's method from a subclass in Python?",
                """
                ```python
                class Animal:
                    def __init__(self, name):
                        self.name = name

                class Dog(Animal):
                    def __init__(self, name, breed):
                        super().__init__(name)   # calls Animal.__init__ correctly, including with multiple inheritance
                        self.breed = breed
                ```

                super() is preferred over calling `Animal.__init__(self, name)` directly, because it correctly follows the MRO — important once multiple inheritance is involved.
                """,
                "A fundamental inheritance question — the 'why super() over calling the parent directly' follow-up separates real understanding from copy-pasted syntax."),
            md("Composition vs inheritance — when should you prefer composition?",
                "Inheritance models an 'is-a' relationship and tightly couples a subclass to its parent's implementation — changes to the parent can break subclasses in surprising ways, and deep hierarchies get hard to reason about. Composition models a 'has-a' relationship: a class holds an instance of another class and delegates to it, which is more flexible (you can swap the composed object at runtime) and avoids the fragile-base-class problem. The common guidance is \"favor composition over inheritance\" unless there's a genuine, stable is-a relationship.",
                "A design-judgment question that goes beyond syntax — checks whether a candidate has thought about the maintainability trade-offs of deep class hierarchies."),
            hi("What is an Abstract Base Class (ABC) in Python, and how do you enforce that subclasses implement a method?",
                """
                ```python
                from abc import ABC, abstractmethod

                class PaymentProcessor(ABC):
                    @abstractmethod
                    def charge(self, amount): ...

                class StripeProcessor(PaymentProcessor):
                    def charge(self, amount):
                        print(f"Charging ${amount} via Stripe")

                # PaymentProcessor()          # TypeError: Can't instantiate abstract class
                # class BadProcessor(PaymentProcessor): pass
                # BadProcessor()               # TypeError: missing implementation of charge()
                ```
                """,
                "Checks whether a candidate knows Python has a real mechanism for enforcing an interface-like contract, not just relying on duck typing everywhere."),
            md("How does Python resolve conflicts with multiple inheritance?",
                "Via MRO (Method Resolution Order), computed with the C3 linearization algorithm — it produces a single, consistent, left-to-right, depth-first (but corrected for consistency) ordering of all ancestor classes, and Python searches that exact order for whichever method/attribute is being resolved. You can always inspect it directly with `ClassName.__mro__` or `ClassName.mro()` rather than guessing.",
                "Ties directly to the MRO question — often asked as a quick follow-up once a candidate has explained MRO conceptually."),
            md("What is a metaclass in Python, briefly, and why do most developers rarely need one?",
                "A metaclass is 'the class of a class' — it controls how classes THEMSELVES are constructed, the same way a class controls how instances are constructed (the default metaclass for everything is `type`). They're powerful but rarely needed in typical application code — most of what people reach for metaclasses to do (validation, registration, adding methods) can be done more simply and readably with class decorators or __init_subclass__. The standard advice (often attributed to Tim Peters) is: if you're wondering whether you need a metaclass, you don't.",
                "A senior-level Python question — the honest 'you rarely need this' answer is often a stronger signal than an overly enthusiastic deep dive into metaclass mechanics."),
            hi("What does __slots__ do, and why would you use it?",
                """
                ```python
                class Point:
                    __slots__ = ("x", "y")   # ONLY these attributes are allowed — no others
                    def __init__(self, x, y):
                        self.x, self.y = x, y

                p = Point(1, 2)
                # p.z = 5   # AttributeError — z isn't in __slots__
                ```

                Without __slots__, every instance gets a per-instance __dict__ for arbitrary attributes, which costs memory. __slots__ trades that flexibility for meaningfully lower memory usage per instance — worth it when creating a very large number of small, fixed-shape objects.
                """,
                "A performance-and-memory-awareness question — less commonly known, so a correct answer is a genuine positive signal."),
            hi("How do you implement operator overloading in Python, like making + work for a custom class?",
                """
                ```python
                class Vector:
                    def __init__(self, x, y): self.x, self.y = x, y
                    def __add__(self, other):
                        return Vector(self.x + other.x, self.y + other.y)
                    def __repr__(self):
                        return f"Vector({self.x}, {self.y})"

                Vector(1, 2) + Vector(3, 4)   # Vector(4, 6) — calls __add__ under the hood
                ```
                """,
                "A practical data-model question — checks whether a candidate knows Python operators are just syntax sugar for dunder method calls."),
            hi("What's the difference between a class attribute and an instance attribute, and what's the mutable-class-attribute gotcha?",
                """
                ```python
                class Dog:
                    tricks = []          # CLASS attribute — shared across EVERY instance!

                    def __init__(self, name):
                        self.name = name  # INSTANCE attribute — unique per object

                    def add_trick(self, trick):
                        self.tricks.append(trick)   # BUG: mutates the shared class-level list

                d1, d2 = Dog("Fido"), Dog("Rex")
                d1.add_trick("sit")
                print(d2.tricks)   # ['sit'] <- unexpected! d2 sees d1's trick too, since tricks is shared
                ```
                Fix: initialize mutable attributes inside __init__ (`self.tricks = []`) so each instance gets its own.
                """,
                "A real, commonly-hit bug — very similar in spirit to the mutable-default-argument trap, and just as commonly asked."),
            md("How does duck typing shape Python's overall approach to OOP compared to strictly interface-based languages?",
                "In Java/C#, an object typically must formally declare it implements an interface before it can be used where that interface is expected. Python doesn't require that — any object with the right methods/attributes works, checked only when actually used (at runtime), not declared upfront. This gives more flexibility (and is why ABCs are opt-in rather than mandatory) but pushes correctness checking later, from compile time to runtime — a trade-off Python's dynamic-typing philosophy embraces throughout the language.",
                "A conceptual question tying together several earlier topics — checks whether a candidate can connect duck typing, ABCs, and dynamic typing into one coherent picture.")));

        m.put("PY4", List.of(
            hi("Explain, mechanically, how you'd write a decorator that times a function's execution.",
                """
                ```python
                import time, functools

                def timer(func):
                    @functools.wraps(func)              # preserves func's name/docstring for introspection
                    def wrapper(*args, **kwargs):
                        start = time.perf_counter()
                        result = func(*args, **kwargs)   # actually call the original function
                        print(f"{func.__name__} took {time.perf_counter() - start:.4f}s")
                        return result
                    return wrapper

                @timer
                def slow_add(a, b):
                    time.sleep(0.1)
                    return a + b
                ```
                A decorator is a function that takes a function and returns a new function wrapping it with extra behavior — `@timer` above `def slow_add` is just shorthand for `slow_add = timer(slow_add)`.
                """,
                "One of the most common Python live-coding exercises — checks real hands-on decorator fluency, not just conceptual awareness."),
            hi("Generator vs a normal function returning a list — why does the difference matter for memory?",
                """
                ```python
                def squares_list(n):
                    return [i*i for i in range(n)]   # builds the ENTIRE list in memory immediately

                def squares_gen(n):
                    for i in range(n):
                        yield i*i                     # produces ONE value at a time, pausing in between

                for val in squares_gen(10_000_000):   # never holds more than one value in memory at once
                    process(val)
                ```

                For very large or infinite sequences, a generator avoids ever materializing the whole sequence in memory — critical when n is large or unbounded.
                """,
                "A near-universal Python question once 'yield' comes up — checks whether the memory trade-off is genuinely understood, not just that yield exists."),
            hi("Explain what try/except/else/finally each do, in order.",
                """
                ```python
                try:
                    value = risky_operation()
                except ValueError as e:
                    print(f"Bad value: {e}")          # runs ONLY if a ValueError was raised
                else:
                    print(f"Got {value}")              # runs ONLY if NO exception was raised
                finally:
                    cleanup()                          # ALWAYS runs, exception or not
                ```
                """,
                "A fundamental Python exception-handling question — the `else` clause is the part most candidates forget or have never used."),
            hi("How does Python's with statement (context manager) work, and why prefer it over manual try/finally?",
                """
                ```python
                with open("data.txt") as f:
                    contents = f.read()
                # f is guaranteed closed here, even if read() raised an exception

                # equivalent, more error-prone manual version:
                f = open("data.txt")
                try:
                    contents = f.read()
                finally:
                    f.close()
                ```

                The object's __enter__ runs at the start of the block, __exit__ runs at the end — even on an exception — so cleanup is guaranteed without you writing (and potentially forgetting a branch of) manual try/finally logic.
                """,
                "A very common Python idiom question — expected baseline knowledge for anyone claiming real Python experience."),
            hi("What is a closure in Python?",
                """
                ```python
                def make_multiplier(factor):
                    def multiplier(x):
                        return x * factor    # `factor` is captured from the enclosing scope
                    return multiplier

                double = make_multiplier(2)
                triple = make_multiplier(3)
                double(5)   # 10
                triple(5)   # 15 — each closure remembers its OWN captured `factor`
                ```

                A closure is an inner function that "remembers" variables from its enclosing scope even after that outer function has finished executing.
                """,
                "A foundational functional-programming-in-Python question — decorators themselves are built on top of closures, so this underlies a lot of idiomatic Python."),
            hi("How do you write a decorator that itself takes arguments?",
                """
                ```python
                def repeat(times):                       # outer function takes the decorator's OWN arguments
                    def decorator(func):                  # middle function takes the function being decorated
                        def wrapper(*args, **kwargs):      # inner function is the actual replacement
                            for _ in range(times):
                                result = func(*args, **kwargs)
                            return result
                        return wrapper
                    return decorator

                @repeat(times=3)
                def greet(name):
                    print(f"Hello, {name}")

                greet("Sam")   # prints "Hello, Sam" three times
                ```
                """,
                "A step up in difficulty from a basic decorator — checks whether a candidate understands the extra layer of nesting needed to parameterize a decorator."),
            md("What's the difference between raise and raise ... from ...?",
                """
                ```python
                try:
                    parse_config()
                except KeyError as e:
                    raise ConfigError("Missing required config key") from e   # preserves the ORIGINAL exception as context

                # vs plain `raise ConfigError(...)`, which loses the link to the original KeyError in the traceback
                ```
                """,
                "A debugging-quality question — `raise ... from ...` produces far more useful tracebacks in production, and few candidates know it exists."),
            hi("How do you create and use a custom exception class?",
                """
                ```python
                class InsufficientFundsError(Exception):
                    def __init__(self, shortfall):
                        super().__init__(f"Short by {shortfall}")
                        self.shortfall = shortfall

                def withdraw(balance, amount):
                    if amount > balance:
                        raise InsufficientFundsError(amount - balance)

                try:
                    withdraw(50, 100)
                except InsufficientFundsError as e:
                    print(f"Failed: {e}, short by {e.shortfall}")
                ```
                """,
                "A very common, practical exception-handling question — checks whether a candidate can go beyond catching built-in exceptions."),
            hi("What does yield from do?",
                """
                ```python
                def inner():
                    yield 1
                    yield 2

                def outer():
                    yield from inner()   # delegates to inner(), yielding each of its values in turn
                    yield 3

                list(outer())   # [1, 2, 3]
                ```

                It's shorthand for manually looping over a sub-generator and yielding each value yourself — commonly used to compose generators, and it also properly forwards sent values/exceptions in more advanced coroutine-style usage.
                """,
                "A more advanced generator question — checks whether a candidate's generator knowledge goes past the basic `yield` keyword."),
            md("Generator expression vs list comprehension — what's the syntax difference, and why does it matter?",
                """
                ```python
                squares_list = [x*x for x in range(1_000_000)]    # () -> tuple would actually be a generator
                squares_gen  = (x*x for x in range(1_000_000))     # parentheses instead of brackets

                sum(x*x for x in range(1_000_000))   # generator expression — never builds the full list, saves memory
                ```

                Same syntax as a list comprehension but with parentheses instead of square brackets — it produces a lazy generator instead of eagerly building the full list, which matters a lot when you only need to iterate once (e.g., feeding directly into sum() or a for loop).
                """,
                "A practical memory-efficiency question — checks whether a candidate defaults to the lazy version when eagerness isn't actually needed."),
            hi("How do you write your own context manager using contextlib?",
                """
                ```python
                from contextlib import contextmanager

                @contextmanager
                def timer():
                    import time
                    start = time.perf_counter()
                    yield                                    # code inside the `with` block runs here
                    print(f"Elapsed: {time.perf_counter() - start:.4f}s")

                with timer():
                    do_expensive_work()
                ```

                Everything before `yield` acts as __enter__, everything after acts as __exit__ — much less boilerplate than writing a full class with __enter__/__exit__ methods for simple cases.
                """,
                "A practical, commonly-needed tool — checks whether a candidate knows the lightweight decorator-based alternative to writing a full context-manager class."),
            md("What's the finally block's behavior with a return statement inside try — a common gotcha?",
                """
                ```python
                def f():
                    try:
                        return 1
                    finally:
                        print("cleanup runs even though we're returning")
                        # if finally ALSO has a return, it silently overrides the try's return value!

                def g():
                    try:
                        return 1
                    finally:
                        return 2   # g() returns 2, NOT 1 — the finally's return wins, silently

                g()   # 2
                ```
                """,
                "A genuinely tricky gotcha question — a return (or break/continue) inside finally silently swallowing the try's return value surprises even experienced developers."),
            md("How do you catch multiple exception types in one except clause?",
                """
                ```python
                try:
                    value = int(data["amount"])
                except (KeyError, ValueError) as e:
                    print(f"Bad input: {e}")

                # or handle them differently:
                except KeyError:
                    print("Missing field")
                except ValueError:
                    print("Not a valid number")
                ```
                """,
                "A practical syntax question — checks whether a candidate knows the tuple-of-exception-types shorthand instead of writing duplicate except blocks.")));

        m.put("PY5", List.of(
            hi("Do Python type hints get enforced at runtime? What are they actually for?",
                """
                ```python
                def add(a: int, b: int) -> int:
                    return a + b

                add("x", "y")   # runs FINE at runtime — no TypeError from the hints themselves, returns "xy"
                ```

                No — type hints are purely for external static-analysis tools (mypy, pyright, IDEs) to catch mismatches BEFORE running the code, and to document intent for other developers. Python itself remains fully dynamically typed at runtime regardless of what hints say.
                """,
                "A very commonly asked question given how widespread type hints have become — checks a real, precise understanding rather than assuming hints behave like Java's type system."),
            md("What's the difference between a virtual environment (venv) and a requirements.txt file?",
                "A venv is an isolated directory containing its own Python interpreter and installed packages, separate from the system Python — it's WHERE packages get installed. requirements.txt is just a plain text list of package names/versions — it doesn't isolate anything by itself; `pip install -r requirements.txt` installs those packages, typically INTO an already-activated venv. Confusing the two is a common beginner mistake (installing project dependencies globally instead of into an isolated environment).",
                "A practical Python-tooling question — checks real hands-on project-setup experience, not just familiarity with the words."),
            hi("What is a pytest fixture, and what problem does it solve?",
                """
                ```python
                import pytest

                @pytest.fixture
                def sample_data():
                    return {"a": 1, "b": 2}

                def test_sum(sample_data):        # pytest AUTOMATICALLY injects the fixture by parameter name
                    assert sum(sample_data.values()) == 3
                ```

                A fixture provides reusable setup (test data, a DB connection, a mock) that pytest injects into any test function that names it as a parameter — avoiding duplicated setup code across many tests, and supporting scoped lifecycles (function/class/module/session) for expensive setup.
                """,
                "A foundational pytest question — fixtures are pytest's signature feature and near-universal in real Python test suites."),
            hi("How would you mock an external API call in a test so it doesn't make a real network request?",
                """
                ```python
                from unittest.mock import patch

                def get_price():
                    import requests
                    return requests.get("https://api.example.com/price").json()["price"]

                @patch("mymodule.requests.get")   # patch it WHERE IT'S USED, not where it's defined
                def test_get_price(mock_get):
                    mock_get.return_value.json.return_value = {"price": 42}
                    assert get_price() == 42
                ```

                Patch the function/method at the point where it's USED, not just where it's originally defined, so the test exercises your code's actual logic without depending on network availability or flakiness.
                """,
                "A very practical, commonly-needed testing skill — patch-location confusion is one of the most common real mocking mistakes."),
            md("pip vs pip3, python vs python3 — is there a real difference?",
                "On systems where only Python 3 is installed (increasingly the default today), pip/pip3 and python/python3 are often the same thing. Historically, when Python 2 and 3 coexisted, `python`/`pip` pointed to Python 2 and `python3`/`pip3` explicitly targeted Python 3 — the explicit `3` suffix was there to avoid ambiguity. Best practice today is still to be explicit (or better, use a venv, where `python`/`pip` inside the activated environment unambiguously refer to that environment's interpreter).",
                "A practical, slightly historical tooling question — checks awareness of the Python 2/3 transition's lingering naming conventions."),
            hi("What does if __name__ == \"__main__\": do, and why is it so commonly used?",
                """
                ```python
                def main():
                    print("Running as a script")

                if __name__ == "__main__":
                    main()
                ```

                When a file is run directly (`python myfile.py`), Python sets its `__name__` to `"__main__"`. When the same file is IMPORTED by another module, `__name__` is set to the module's actual name instead — so code inside this guard only runs when the file is executed directly, not when it's imported as a library.
                """,
                "One of the most fundamental, universally-expected Python idioms — a candidate unfamiliar with this is a real gap."),
            md("What is __init__.py for in a Python package?",
                "It marks a directory as a Python package, letting you import from it as a namespace (`from mypackage import mymodule`). It can be empty, or it can contain package-level initialization code and control what's exposed via `from mypackage import *` using an `__all__` list. Since Python 3.3, 'namespace packages' can technically work without an __init__.py, but explicitly including one remains the common, clearer convention for a regular package.",
                "A basic project-structure question — checks real familiarity with how Python packages actually work, not just 'I've imported things.'"),
            md("Absolute vs relative imports — what's the difference?",
                """
                ```python
                # absolute import — full path from the project's root package
                from myproject.utils.helpers import format_date

                # relative import — relative to the CURRENT module's position in the package
                from .helpers import format_date      # same package
                from ..utils import formatting         # one level up
                ```

                Absolute imports are explicit and unambiguous regardless of where a module is imported from; relative imports are more concise within a large package but only work inside a package (not in a script run directly) and can get confusing in deeply nested structures.
                """,
                "A practical, commonly-needed question — relative-import errors ('attempted relative import with no known parent package') are a very common real beginner stumbling block."),
            md("What do Optional[X] and Union[X, Y] mean in type hints?",
                """
                ```python
                from typing import Optional, Union

                def find_user(id: int) -> Optional[User]:   # returns a User OR None — shorthand for Union[User, None]
                    ...

                def process(value: Union[int, str]) -> str:  # accepts EITHER an int or a str
                    ...

                # Python 3.10+ shorthand:
                def find_user(id: int) -> User | None: ...
                ```
                """,
                "A commonly-used typing question — checks fluency with the typing module beyond simple `int`/`str` hints."),
            hi("What is pytest.mark.parametrize used for?",
                """
                ```python
                import pytest

                @pytest.mark.parametrize("input,expected", [
                    (2, 4),
                    (3, 9),
                    (4, 16),
                ])
                def test_square(input, expected):
                    assert input ** 2 == expected
                ```

                It runs the same test function once per set of parameters, generating a separate, individually-reportable test result for each — avoiding copy-pasted near-identical test functions for each input case.
                """,
                "A very common, practical pytest feature — checks whether a candidate writes DRY, maintainable test suites."),
            md("What is code coverage, and how do you measure it in Python?",
                "Code coverage measures which lines/branches of your code actually executed during a test run, typically measured with the coverage.py library (often via `pytest-cov` for pytest integration), reported as a percentage. It helps find completely untested code, but high coverage doesn't guarantee good tests — you can execute a line without meaningfully asserting on its behavior, so coverage is a floor to check, not a target to maximize blindly.",
                "A testing-maturity question — the 'coverage isn't the same as quality' nuance is what separates a thoughtful answer from reciting the tool's existence."),
            md("What's the practical difference between unittest and pytest?",
                "unittest is Python's built-in testing framework, using a class-based, JUnit-inspired style (self.assertEqual, setUp/tearDown methods). pytest is a third-party framework that's become the de facto standard — plain `assert` statements (with much more helpful failure output than unittest's assert methods), fixtures instead of setUp/tearDown boilerplate, and far less ceremony overall. pytest can also run existing unittest-style tests, so migration is generally incremental, not all-or-nothing.",
                "A practical tooling-landscape question — pytest fluency is close to a baseline expectation at most companies today."),
            md("Why shouldn't you use a plain assert statement for validating input in production code?",
                "Python's `assert` statements can be globally stripped out at runtime when the interpreter is run with the `-O` (optimize) flag — meaning any validation logic living only in an assert can silently vanish in an optimized production run, letting invalid data flow through unchecked. asserts are meant for internal invariants and test code, not for validating external/user input — use explicit `if` checks that `raise` a real exception for anything that must always be enforced.",
                "A real production-safety gotcha — a surprising number of developers don't know assert statements can be disabled entirely."),
            md("What's the difference between managing dependencies with plain requirements.txt vs a tool like Poetry?",
                "requirements.txt is a flat, manually-maintained list with no built-in distinction between direct and transitive dependencies, and no dependency-resolution guarantees beyond what pip does at install time. Poetry (and similar tools like pip-tools) manage a proper dependency graph, lock exact resolved versions (including transitive dependencies) in a lockfile for fully reproducible installs, and cleanly separate your project's declared dependencies from what actually got resolved and installed.",
                "A more senior tooling-maturity question — checks awareness of reproducible-build practices beyond the most basic pip workflow.")));

        m.put("PY6", List.of(
            hi("What is the GIL, and what does it actually prevent?",
                "The Global Interpreter Lock ensures only one thread executes Python BYTECODE at a time within a single process, even on a multi-core machine. It means Python threads do NOT give you true parallel CPU-bound execution — two threads can never run pure Python code simultaneously on two different cores, no matter how many cores are available.",
                "One of the most iconic Python interview questions — near-guaranteed once concurrency comes up, and a very common follow-up target."),
            hi("Given the GIL, when is Python threading still useful, and when do you need multiprocessing instead?",
                """
                ```python
                # I/O-bound -> threading helps: the GIL is RELEASED while waiting on I/O
                import threading
                def download(url): ...   # network wait releases the GIL, other threads can run

                # CPU-bound -> needs multiprocessing for TRUE parallelism
                from multiprocessing import Pool
                with Pool(4) as pool:
                    results = pool.map(cpu_heavy_function, data)   # separate PROCESSES, separate GILs
                ```

                Rule of thumb: I/O-bound (network calls, file/disk I/O) -> threading (or asyncio); CPU-bound (heavy computation) -> multiprocessing, since each process gets its own interpreter and GIL, giving real parallelism.
                """,
                "The essential, practical follow-up to the GIL question — checks whether a candidate can apply the concept, not just define it."),
            hi("What problem does asyncio solve, and how is it different from threading?",
                """
                ```python
                import asyncio

                async def fetch(url):
                    await asyncio.sleep(1)   # simulates a non-blocking I/O wait — yields control to the event loop
                    return f"data from {url}"

                async def main():
                    results = await asyncio.gather(fetch("a"), fetch("b"), fetch("c"))  # all run CONCURRENTLY

                asyncio.run(main())
                ```

                asyncio runs many I/O-bound tasks concurrently on a SINGLE thread using cooperative multitasking — a task voluntarily yields control (at an `await`) while waiting, instead of a thread being preemptively context-switched. This avoids thread-safety overhead entirely, but a single long-running, `await`-free CPU-bound call will block the WHOLE event loop, unlike threading where the OS can preempt a busy thread.
                """,
                "A very common modern-Python concurrency question — checks whether a candidate understands cooperative vs preemptive multitasking, not just asyncio syntax."),
            md("Before optimizing slow Python code, what should you do first, and why?",
                "Profile it (with cProfile, or a line-level profiler for finer detail) to find where time is ACTUALLY being spent, rather than guessing based on intuition. Most code has one or two real bottlenecks; optimizing code that isn't actually slow wastes effort and adds complexity for no measurable benefit — and intuition about performance is famously unreliable, even for experienced developers.",
                "A performance-engineering-discipline question — checks whether a candidate optimizes based on data or guesswork."),
            hi("What is a race condition, and how do you prevent one in Python?",
                """
                ```python
                import threading

                counter = 0
                lock = threading.Lock()

                def increment():
                    global counter
                    with lock:              # ensures only one thread modifies counter at a time
                        counter += 1        # without the lock, two threads could read the same value and both increment from it

                threads = [threading.Thread(target=increment) for _ in range(1000)]
                [t.start() for t in threads]
                [t.join() for t in threads]
                ```

                A race condition happens when multiple threads read-modify-write shared state without synchronization, so the final result depends on unpredictable timing. A Lock (or other synchronization primitive) ensures only one thread can execute the critical section at a time.
                """,
                "A very common concurrency-correctness question — checks whether a candidate can actually write correct synchronized code, not just define the term."),
            hi("ThreadPoolExecutor vs ProcessPoolExecutor from concurrent.futures — what's the difference?",
                """
                ```python
                from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor

                with ThreadPoolExecutor(max_workers=4) as ex:
                    results = ex.map(download_url, urls)          # good for I/O-bound work

                with ProcessPoolExecutor(max_workers=4) as ex:
                    results = ex.map(cpu_heavy_function, data)     # good for CPU-bound work — real parallelism
                ```

                Same simple `.map()`/`.submit()` interface for both, but ThreadPoolExecutor runs tasks in threads (limited by the GIL for CPU-bound work), while ProcessPoolExecutor runs tasks in separate OS processes (each with its own interpreter/GIL, achieving true parallel CPU execution, at the cost of higher memory usage and serialization overhead for passing data between processes).
                """,
                "Tests whether a candidate knows the modern, higher-level concurrent.futures API — often preferred over raw threading/multiprocessing module usage."),
            md("What is a coroutine, and what is async/await syntax actually doing under the hood?",
                "A coroutine is a special function (defined with `async def`) that can be PAUSED at `await` points and resumed later, without blocking the whole program — the event loop keeps track of all paused coroutines and resumes each one once whatever it was awaiting (a timer, a network response) is ready. `async def` creates a coroutine function; calling it doesn't run the body immediately, it returns a coroutine OBJECT that must be awaited or scheduled (e.g., via asyncio.run() or asyncio.gather()) to actually execute.",
                "A deeper asyncio question — checks whether a candidate understands the mechanism, not just the keywords."),
            md("asyncio.gather vs asyncio.wait — what's the difference?",
                "asyncio.gather() runs multiple awaitables concurrently and returns their results in the SAME ORDER they were passed in, as a simple list — the common, more convenient choice. asyncio.wait() gives more fine-grained control (e.g., waiting only until the FIRST one completes, via `return_when=FIRST_COMPLETED`) but returns sets of 'done' and 'pending' tasks that you have to unpack yourself, rather than ordered results — more flexible, more verbose.",
                "A precise asyncio API question for candidates with real hands-on async experience, beyond just having seen 'gather' in a tutorial once."),
            md("How can a deadlock happen with threading in Python, given the GIL?",
                "The GIL prevents simultaneous BYTECODE execution, but it doesn't prevent classic lock-ordering deadlocks: if Thread A holds Lock 1 and waits for Lock 2, while Thread B holds Lock 2 and waits for Lock 1, both threads block forever regardless of the GIL — the GIL only serializes bytecode execution, it says nothing about the order application-level locks are acquired in. The GIL is a common misconception here: people assume it somehow makes threaded Python code inherently deadlock-safe, and it doesn't.",
                "A conceptual question that corrects a common misconception — a strong answer explicitly separates what the GIL does and doesn't protect against."),
            hi("What is memoization, and how do you implement it with functools.lru_cache?",
                """
                ```python
                from functools import lru_cache

                @lru_cache(maxsize=None)
                def fibonacci(n):
                    if n < 2: return n
                    return fibonacci(n-1) + fibonacci(n-2)

                fibonacci(35)   # fast — repeated subcalls are served from cache instead of recomputed
                ```

                Memoization caches a (pure) function's results keyed by its arguments, so repeated calls with the same arguments return instantly instead of recomputing — lru_cache is a one-line decorator that implements this with a bounded (or unbounded, with maxsize=None) least-recently-used cache.
                """,
                "A very practical, commonly-asked performance question — checks whether a candidate reaches for the built-in tool instead of hand-rolling a cache dict."),
            md("What's the time complexity of dict/set lookups in Python, and why?",
                "O(1) average case — both are implemented as hash tables, so looking up a key computes its hash and jumps nearly directly to the right bucket, instead of scanning. Worst case is O(n) if there are many hash collisions (rare in practice with Python's hash implementation and automatic table resizing), which is why membership testing (`x in my_set`) is dramatically faster than the O(n) linear scan of `x in my_list` for anything beyond a tiny collection.",
                "A fundamentals-meets-performance question — ties directly back to the earlier 'why use a set for membership testing' concept."),
            md("What is PyPy, and how does it differ from CPython for performance?",
                "CPython (the standard, default Python implementation) interprets bytecode directly. PyPy is an alternative implementation with a Just-In-Time (JIT) compiler that compiles frequently-executed code paths to machine code at runtime, often giving substantial speedups for long-running, CPU-heavy pure-Python code — sometimes several times faster than CPython. The trade-off is imperfect compatibility with some C-extension-heavy libraries (though this has improved significantly over time) and a longer warm-up time before the JIT benefits kick in.",
                "A broader Python-ecosystem question — less commonly known in depth, so a solid answer is a genuine positive signal about a candidate's breadth."),
            md("Pool.map vs Pool.apply_async in the multiprocessing module — what's the difference?",
                """
                ```python
                from multiprocessing import Pool

                with Pool(4) as pool:
                    results = pool.map(square, [1, 2, 3, 4])          # blocks until ALL results are ready, ordered

                    async_result = pool.apply_async(square, (5,))      # returns immediately
                    value = async_result.get()                         # blocks HERE, only when you actually need it
                ```

                map() is the simple, synchronous, ordered-results choice for applying one function across many inputs. apply_async() gives you a handle you can check/wait on later, useful when you need finer control over when you actually block for a result, or want to submit heterogeneous tasks rather than the same function over a list.
                """,
                "A more advanced multiprocessing API question — checks depth beyond the most commonly-seen `Pool.map` example."),
            hi("Why is queue.Queue commonly used for producer-consumer patterns in Python, instead of a plain list?",
                """
                ```python
                import queue, threading

                q = queue.Queue(maxsize=100)

                def producer():
                    q.put(make_item())     # blocks automatically if the queue is full

                def consumer():
                    item = q.get()          # blocks automatically if the queue is empty
                    process(item)
                    q.task_done()
                ```

                queue.Queue is thread-safe out of the box (internally uses locks/conditions for you) and provides blocking put()/get() with optional timeouts — a plain list is NOT thread-safe for this pattern and would require you to hand-roll all the same locking logic yourself, with a much higher chance of getting it subtly wrong.
                """,
                "A practical concurrency-design question — checks whether a candidate reaches for the correct, safe built-in tool for a classic pattern."),
            md("Does the GIL slow down NumPy/pandas-heavy code the same way it slows down pure-Python loops?",
                "Not nearly as much — NumPy, pandas, and similar libraries implement their core numeric operations in C, and that C code explicitly RELEASES the GIL during long-running computations, allowing genuine parallelism across threads for those specific operations. This is why 'just use NumPy vectorized operations instead of a Python for-loop' is such a common and effective performance recommendation — it sidesteps the GIL's limitation entirely for the heavy-lifting portion of the work.",
                "A nuanced, senior-level question — checks whether a candidate's GIL understanding is precise enough to know it's not an absolute, universal performance ceiling.")));

        return m;
    }

    public static List<SeedQA> forModule(String moduleId) {
        return all().getOrDefault(moduleId, List.of());
    }
}
