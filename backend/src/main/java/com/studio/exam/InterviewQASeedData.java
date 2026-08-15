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

        return m;
    }

    public static List<SeedQA> forModule(String moduleId) {
        return all().getOrDefault(moduleId, List.of());
    }
}
