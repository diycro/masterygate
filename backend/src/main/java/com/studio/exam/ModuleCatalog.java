package com.studio.exam;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seed curriculum: topics -> ordered modules -> objectives, curated resources (free-preferred + paid),
 * interview focus, and free-text gate questions (with grader key points, kept server-side).
 * MVP is hardcoded; a later sprint moves this to the DB and adds LLM path generation.
 */
@Component
public class ModuleCatalog {

    public record Resource(String title, String provider, String url, String meta, boolean free) {}

    public record Module(String id, String topicId, int order, String title,
                         List<String> objectives, List<Resource> resources,
                         List<String> interviewFocus, List<Question> questions) {}

    public record Topic(String id, String title, String subtitle) {}

    private final Map<String, Topic> topics = new LinkedHashMap<>();
    private final Map<String, Module> modules = new LinkedHashMap<>();
    private final Map<String, List<Module>> byTopic = new LinkedHashMap<>();

    public ModuleCatalog() {
        buildGenAi();
        buildDsa();
        buildSystemDesign();
    }

    private void add(Module m) {
        modules.put(m.id(), m);
        byTopic.computeIfAbsent(m.topicId(), k -> new ArrayList<>()).add(m);
    }

    public Collection<Topic> topics() { return topics.values(); }
    public Module getModule(String id) { return modules.get(id); }
    public List<Module> modulesFor(String topicId) { return byTopic.getOrDefault(topicId, List.of()); }

    private static Resource free(String title, String provider, String url, String meta) {
        return new Resource(title, provider, url, meta, true);
    }
    private static Resource paid(String title, String provider, String url, String meta) {
        return new Resource(title, provider, url, meta, false);
    }
    private static Question q(String id, String text, String... keyPoints) {
        return new Question(id, text, List.of(keyPoints));
    }

    // ------------------------------------------------------------------ GenAI track
    private void buildGenAi() {
        String t = "genai";
        topics.put(t, new Topic(t, "GenAI Application Engineer",
                "Build production LLM apps on your Java + cloud stack. Free-first, gated, interview-ready."));

        add(new Module("M0", t, 0, "Setup + Your First LLM Call",
            List.of("Python venv + keys the safe way", "Call an LLM from Python and Java (Spring AI)",
                    "Read token usage, cost, and determinism"),
            List.of(
                free("Python venv (official docs)", "python.org", "https://docs.python.org/3/library/venv.html", "docs"),
                free("Automate the Boring Stuff (Python)", "free book", "https://automatetheboringstuff.com/", "book"),
                free("Groq console (free API key)", "Groq", "https://console.groq.com/", "signup"),
                free("Spring AI reference", "Spring", "https://docs.spring.io/spring-ai/reference/", "docs"),
                paid("The Complete Python Bootcamp", "Udemy", "https://www.udemy.com/courses/search/?q=python+bootcamp", "search")),
            List.of("Explain a first LLM call end-to-end", "Secrets handling: env vars / .env, never in code"),
            List.of(
                q("m0q1", "Why do we keep API keys in an environment variable or a gitignored .env file instead of hard-coding them?",
                    "secrets must never be committed to git", "separates config from code",
                    "lets you rotate keys without code changes", ".env is gitignored so it stays out of the repo"),
                q("m0q2", "What does a Python virtual environment (venv) give you?",
                    "an isolated set of dependencies per project", "avoids polluting the system Python",
                    "reproducible installs without version clashes"),
                q("m0q3", "Give one situation where you'd use Java/Spring AI for an LLM feature, and one where you'd use Python.",
                    "Java/Spring AI: embedding LLM into an existing JVM/Spring production backend",
                    "Python: the AI-first library ecosystem / fast prototyping / newest tools land there first"))));

        add(new Module("M1", t, 1, "LLM Fundamentals & Prompting",
            List.of("Tokens, context window, temperature", "Cost & determinism", "Prompting vs RAG vs fine-tuning",
                    "System vs user messages, structured output"),
            List.of(
                free("ChatGPT Prompt Engineering for Developers", "DeepLearning.AI", "https://www.deeplearning.ai/short-courses/", "1.5h course"),
                free("Prompt Engineering Interactive Tutorial", "Anthropic", "https://github.com/anthropics/prompt-eng-interactive-tutorial", "hands-on"),
                free("Intro to Large Language Models (Karpathy)", "YouTube", "https://www.youtube.com/watch?v=zjkBMFhNj_g", "1h video"),
                paid("Prompt Engineering courses", "Udemy", "https://www.udemy.com/courses/search/?q=prompt+engineering", "search")),
            List.of("Cost/latency levers", "Force valid JSON output", "When RAG vs fine-tune"),
            List.of(
                q("q1", "Why is an LLM call's token count higher than the number of words in your message?",
                    "tokens include the role/formatting scaffolding (system/user markers), not just your words",
                    "tokenization splits text into sub-word pieces", "punctuation and spaces also count as tokens"),
                q("q2", "What does the 'temperature' setting control, and what do higher vs lower values do?",
                    "controls the randomness of token sampling, NOT accuracy or correctness",
                    "low = focused, repeatable, near-deterministic", "high = more varied and creative",
                    "it reshapes the probability distribution before sampling"),
                q("q3", "You pay per token in two buckets — name both, and say which is usually priced higher.",
                    "input / prompt tokens", "output / completion tokens", "output tokens are priced higher"),
                q("q4", "Mechanically, why can the exact same prompt return different text on different runs?",
                    "the model produces a probability distribution over the next token",
                    "it samples (a weighted random draw) instead of always the top token", "so runs diverge"),
                q("q5", "What is a system message vs a user message, and when would you use a system message?",
                    "user = the end-user's actual request", "system = standing instructions/persona/rules/format for the whole conversation",
                    "use system to set behavior or output format"),
                q("q6", "When would you choose RAG over plain prompting, and over fine-tuning?",
                    "RAG when answers need external/private/up-to-date facts grounded in your documents",
                    "prompting for tasks within the model's general knowledge",
                    "fine-tuning to change style/format/behavior at scale, not to inject facts"))));

        add(new Module("M2", t, 2, "Embeddings & Vector Search (pgvector)",
            List.of("Embeddings & semantic similarity", "cosine vs L2", "HNSW vs IVFFlat", "Chunking"),
            List.of(
                free("pgvector (README)", "GitHub", "https://github.com/pgvector/pgvector", "docs"),
                free("Building Applications with Vector Databases", "DeepLearning.AI", "https://www.deeplearning.ai/short-courses/", "1h course"),
                free("Vector search learning center", "Pinecone", "https://www.pinecone.io/learn/", "articles"),
                paid("Vector database courses", "Udemy", "https://www.udemy.com/courses/search/?q=vector+database+embeddings", "search")),
            List.of("Why vectors beat LIKE/full-text", "Debug bad retrieval", "Scale a vector index"),
            List.of(
                q("m2q1", "What is an embedding, and how does semantic search use it to find relevant text?",
                    "text is converted to a vector that captures meaning", "similar meaning -> nearby vectors",
                    "the query is embedded and nearest-neighbor search returns the closest chunks"),
                q("m2q2", "cosine similarity vs L2 distance — what's the difference and which is common for text embeddings?",
                    "cosine compares direction/angle (magnitude-insensitive)", "L2 is straight-line distance",
                    "cosine is common for text embeddings"),
                q("m2q3", "HNSW vs IVFFlat index — what's the trade-off?",
                    "both are approximate nearest-neighbor indexes", "HNSW: graph-based, high recall/speed, more memory & build cost",
                    "IVFFlat: cluster-based, cheaper/faster to build, tune probes for recall"),
                q("m2q4", "Why not just use SQL LIKE or full-text search instead of vector search?",
                    "keyword search misses paraphrases and meaning", "vectors match semantic similarity",
                    "hybrid search combines keyword + vector for the best of both"))));

        add(new Module("M3", t, 3, "RAG End-to-End",
            List.of("Full pipeline + citations", "Hybrid search & re-ranking", "Grounding to cut hallucination", "Evaluating RAG"),
            List.of(
                free("Building and Evaluating Advanced RAG", "DeepLearning.AI", "https://www.deeplearning.ai/short-courses/", "1.5h course"),
                free("Spring AI — RAG", "Spring", "https://docs.spring.io/spring-ai/reference/", "docs"),
                free("Ragas — RAG evaluation", "Ragas", "https://docs.ragas.io/", "docs"),
                paid("LangChain / RAG masterclasses", "Udemy", "https://www.udemy.com/courses/search/?q=retrieval+augmented+generation", "search")),
            List.of("Design a RAG system for private docs", "Debug hallucination", "Evals in CI"),
            List.of(
                q("m3q1", "Walk through the stages of a RAG pipeline in order.",
                    "ingest -> chunk -> embed -> store (index)", "retrieve (optionally re-rank)",
                    "augment the prompt with retrieved context", "generate the answer, ideally with citations"),
                q("m3q2", "How does RAG reduce hallucination, and when does it still fail?",
                    "it grounds the answer in retrieved, cited context", "fails if retrieval misses the relevant docs",
                    "or if the model ignores the provided context"),
                q("m3q3", "What is hybrid search and why use it?",
                    "combine vector (semantic) with keyword/BM25 search", "catches paraphrase AND exact terms like IDs/code",
                    "scores from both are merged"),
                q("m3q4", "How do you evaluate RAG quality and catch a regression before shipping?",
                    "an eval set scoring faithfulness/relevance/answer-correctness", "run it in CI as a gate",
                    "golden question/answer pairs"))));

        add(new Module("M4", t, 4, "Agents, Tool-Calling & MCP",
            List.of("Tool/function calling", "The reason-act-observe loop", "Cost/loop guards", "MCP"),
            List.of(
                free("AI Agents Course", "Hugging Face", "https://huggingface.co/learn/agents-course", "course"),
                free("Functions, Tools and Agents with LangChain", "DeepLearning.AI", "https://www.deeplearning.ai/short-courses/", "course"),
                free("Model Context Protocol (MCP)", "modelcontextprotocol.io", "https://modelcontextprotocol.io/", "docs"),
                paid("AI agents courses", "Udemy", "https://www.udemy.com/courses/search/?q=ai+agents+llm", "search")),
            List.of("Agent vs prompt chain", "Prevent runaway loops/cost", "What MCP solves"),
            List.of(
                q("m4q1", "In LLM tool-calling, who actually runs the tool — the model or your code?",
                    "the model outputs a request to call a named function with arguments", "YOUR code executes the tool",
                    "the result is fed back to the model; the model does not run code itself"),
                q("m4q2", "What is the agent reason-act-observe loop, and when is an agent the wrong choice?",
                    "reason -> call a tool (act) -> observe result -> repeat", "wrong when a simple deterministic chain suffices",
                    "agents add cost, latency, and unpredictability"),
                q("m4q3", "How do you stop an agent from looping forever and burning tokens/cost?",
                    "a max step/iteration limit", "a token/cost budget or timeout", "guardrails on tool use"),
                q("m4q4", "What does MCP (Model Context Protocol) standardize?",
                    "a standard protocol/interface for connecting LLMs to tools and data sources",
                    "replaces bespoke per-integration glue with a common client-server contract"))));

        add(new Module("M5", t, 5, "Production GenAI",
            List.of("Evals & testing non-determinism", "Prompt-injection & guardrails", "Cost/latency & caching", "Observability"),
            List.of(
                free("LLMOps", "DeepLearning.AI", "https://www.deeplearning.ai/short-courses/", "course"),
                free("OWASP Top 10 for LLM Applications", "OWASP", "https://owasp.org/www-project-top-10-for-large-language-model-applications/", "guide"),
                free("Quality & Safety for LLM Applications", "DeepLearning.AI", "https://www.deeplearning.ai/short-courses/", "course"),
                paid("LLMOps / production LLM courses", "Udemy", "https://www.udemy.com/courses/search/?q=llmops", "search")),
            List.of("Defend against prompt injection", "Test non-determinism", "Cost controls at scale"),
            List.of(
                q("m5q1", "What is prompt injection, and give two concrete mitigations.",
                    "malicious input that overrides your instructions or exfiltrates data",
                    "separate/treat user input as untrusted; validate model output",
                    "least-privilege tools; never blindly execute model output; guardrails"),
                q("m5q2", "How do you test something non-deterministic like an LLM feature?",
                    "eval sets scored by metrics or LLM-as-judge", "assert on properties/rubrics not exact strings",
                    "golden datasets with thresholds, run in CI"),
                q("m5q3", "Name three levers to cut LLM latency or cost in production.",
                    "caching (exact and/or semantic)", "route easy calls to a cheaper/smaller model",
                    "shorter prompts/context; batching; streaming for perceived latency"),
                q("m5q4", "What should you log for an LLM app, and what must you NOT log?",
                    "log prompts/responses, tokens, cost, latency for observability",
                    "do NOT log secrets or PII (redact them)"))));

        add(new Module("M6", t, 6, "Capstone — RAG Assistant",
            List.of("Integrate M1–M5 into one app", "Deploy on AWS", "Defend every design choice"),
            List.of(
                free("Your capstone spec (this repo)", "Learning Studio", "https://github.com/", "project"),
                free("Spring AI reference", "Spring", "https://docs.spring.io/spring-ai/reference/", "docs")),
            List.of("5-min architecture walkthrough", "Why X not Y for chunking/index/model", "Real latency & cost numbers"),
            List.of(
                q("m6q1", "For your RAG app, justify your chunk size and index choice, and what you'd change at 10x scale.",
                    "chunk size trades retrieval granularity vs context dilution",
                    "index choice (HNSW/IVFFlat) trades recall/speed vs memory/build",
                    "at scale: sharding, caching, cost controls, monitoring"),
                q("m6q2", "How would you add cost controls and evaluation to your capstone?",
                    "caching, cheaper-model routing, per-session budgets",
                    "an eval set (faithfulness/relevance) run in CI"))));
    }

    // ------------------------------------------------------------------ DSA track
    private void buildDsa() {
        String t = "dsa";
        topics.put(t, new Topic(t, "Data Structures & Algorithms",
                "Crack the #1 coding-interview gate — pattern-based, from arrays to graphs to DP."));

        List<Resource> base = List.of(
                free("NeetCode roadmap + solutions", "NeetCode", "https://neetcode.io/", "practice + videos"),
                free("Striver's A2Z DSA Sheet", "takeUforward", "https://takeuforward.org/strivers-a2z-dsa-course/strivers-a2z-dsa-course-sheet-2/", "sheet + videos"),
                free("Grind 75 problem list", "Tech Interview Handbook", "https://www.techinterviewhandbook.org/grind75/", "curated list"),
                paid("Master the Coding Interview: DSA", "Udemy", "https://www.udemy.com/courses/search/?q=data+structures+algorithms", "search"));

        add(new Module("DSA1", t, 0, "Arrays, Strings & Hashing",
            List.of("Array/string traversal patterns", "Hashing for O(1) lookup", "Big-O time/space analysis"), base,
            List.of("Always state complexity", "Hashmap vs sorting trade-offs"),
            List.of(
                q("dsa1q1", "When would you use a hash map instead of sorting to solve a problem, and what's the time/space trade-off?",
                    "hash map gives O(1) average lookup -> often O(n) time but O(n) extra space",
                    "sorting is O(n log n) time with little/no extra space",
                    "use hashing when you need fast membership/counts and can afford the space"),
                q("dsa1q2", "What does Big-O notation describe, and why do interviews focus on the worst case?",
                    "asymptotic growth of time/space as input grows; constants are ignored",
                    "worst case gives a guaranteed upper bound regardless of input"),
                q("dsa1q3", "Give an example where a hash set turns an O(n^2) solution into O(n).",
                    "e.g. two-sum or detecting duplicates", "store seen elements and check the complement/existence in O(1)"))));

        add(new Module("DSA2", t, 1, "Two Pointers & Sliding Window",
            List.of("Two-pointer technique", "Fixed & variable sliding windows", "Removing nested loops"), base,
            List.of("Recognize window vs two-pointer", "Justify the O(n) complexity"),
            List.of(
                q("dsa2q1", "What signals in a problem suggest a sliding-window approach?",
                    "a contiguous subarray/substring", "finding the longest/shortest/optimal window meeting a condition",
                    "avoids recomputation by expanding/shrinking the window"),
                q("dsa2q2", "How does a variable-size sliding window achieve O(n) instead of O(n^2)?",
                    "each element enters and leaves the window at most once", "both pointers only move forward"),
                q("dsa2q3", "Two pointers vs sliding window — when would you use each?",
                    "two pointers: often sorted arrays / both ends (pair sums, partitioning)",
                    "sliding window: optimize over a contiguous range"))));

        add(new Module("DSA3", t, 2, "Stacks, Queues & Linked Lists",
            List.of("Stack/queue use-cases", "Linked-list manipulation", "Choosing the right structure"), base,
            List.of("Match structure to access pattern", "State complexity + space"),
            List.of(
                q("dsa3q1", "Give a problem where a stack is the natural fit, and why.",
                    "matching parentheses / monotonic stack / undo", "LIFO — the most recent item is what you need next"),
                q("dsa3q2", "How do you detect a cycle in a linked list, and what's the complexity?",
                    "Floyd's fast & slow pointers", "O(n) time, O(1) space; the pointers meet inside the cycle"),
                q("dsa3q3", "Array vs linked list — the key trade-offs?",
                    "array: O(1) index access, contiguous, costly mid insert/delete",
                    "linked list: O(1) insert/delete at a node, O(n) access, extra pointer memory"))));

        add(new Module("DSA4", t, 3, "Trees & Binary Search Trees",
            List.of("Tree traversals (DFS/BFS)", "BST properties", "Recursion on trees"), base,
            List.of("When BST degrades", "In-order = sorted for a BST"),
            List.of(
                q("dsa4q1", "What property makes BST search O(log n), and when does it degrade to O(n)?",
                    "left < root < right ordering enables binary search",
                    "degrades to O(n) when the tree is unbalanced/skewed; balanced trees (AVL/Red-Black) keep O(log n)"),
                q("dsa4q2", "BFS vs DFS traversal of a tree — difference and when to use each?",
                    "BFS = level-order (queue), good for shortest path / level info",
                    "DFS = deep (stack/recursion), good for path/subtree problems, less memory on wide trees"),
                q("dsa4q3", "How would you check whether a binary tree is a valid BST?",
                    "in-order traversal must be strictly increasing, OR recurse carrying min/max bounds",
                    "a simple parent-child check is not sufficient"))));

        add(new Module("DSA5", t, 4, "Graphs (BFS/DFS)",
            List.of("Graph representations", "BFS/DFS on graphs", "Shortest-path basics"), base,
            List.of("Sparse vs dense representation", "Avoiding infinite loops"),
            List.of(
                q("dsa5q1", "Adjacency list vs adjacency matrix — trade-offs?",
                    "list: O(V+E) space, efficient for sparse graphs",
                    "matrix: O(V^2) space, O(1) edge lookup, better for dense graphs"),
                q("dsa5q2", "When do you use BFS vs DFS on a graph?",
                    "BFS: shortest path in an unweighted graph, level exploration",
                    "DFS: cycle detection, topological sort, connectivity, path existence"),
                q("dsa5q3", "How do you avoid infinite loops when traversing a graph?",
                    "keep a visited set/array and mark nodes as you go", "graphs can have cycles, unlike trees"))));

        add(new Module("DSA6", t, 5, "Recursion & Dynamic Programming",
            List.of("Recursion & base cases", "Memoization vs tabulation", "Spotting DP problems"), base,
            List.of("Identify overlapping subproblems", "Explain the complexity win"),
            List.of(
                q("dsa6q1", "What signals that a problem can be solved with dynamic programming?",
                    "overlapping subproblems AND optimal substructure",
                    "the same subproblems recur and can be cached/reused"),
                q("dsa6q2", "Memoization vs tabulation — what's the difference?",
                    "memoization: top-down recursion with a cache",
                    "tabulation: bottom-up iterative table; both remove recomputation"),
                q("dsa6q3", "How does memoization change naive recursive Fibonacci's complexity?",
                    "naive is O(2^n) exponential", "memoized is O(n) time and O(n) space by caching subresults"))));
    }

    // ------------------------------------------------------------------ System Design track
    private void buildSystemDesign() {
        String t = "sysdesign";
        topics.put(t, new Topic(t, "System Design",
                "Design scalable systems — the senior-level gate, ending with AI system design."));

        List<Resource> base = List.of(
                free("System Design Primer", "GitHub", "https://github.com/donnemartin/system-design-primer", "reference"),
                free("Hello Interview — System Design", "Hello Interview", "https://www.hellointerview.com/learn/system-design", "guides"),
                free("Gaurav Sen — System Design", "YouTube", "https://www.youtube.com/@gkcs", "videos"),
                paid("ByteByteGo (Alex Xu)", "ByteByteGo", "https://bytebytego.com/", "course/book"));

        add(new Module("SD1", t, 0, "Fundamentals: Scaling & Load Balancing",
            List.of("Vertical vs horizontal scaling", "Load balancing & statelessness", "Latency vs throughput"), base,
            List.of("Drive requirements first", "Back-of-envelope scale estimates"),
            List.of(
                q("sd1q1", "Vertical vs horizontal scaling — the trade-offs?",
                    "vertical = bigger machine: simple but limited/expensive, single point of failure",
                    "horizontal = more machines: scales far but needs load balancing and stateless servers"),
                q("sd1q2", "What does a load balancer do, and why is statelessness important behind it?",
                    "distributes traffic across servers", "stateless servers let any server handle any request (session in shared store), enabling scale and failover"),
                q("sd1q3", "Define latency and throughput, and give a trade-off between them.",
                    "latency = time per request; throughput = requests per second",
                    "batching can raise throughput but increase per-request latency"))));

        add(new Module("SD2", t, 1, "Databases & Data Modeling",
            List.of("SQL vs NoSQL", "Sharding & replication", "Indexing & consistency (CAP)"), base,
            List.of("Justify the DB choice", "Explain a consistency trade-off"),
            List.of(
                q("sd2q1", "When would you choose NoSQL over a relational database?",
                    "flexible/large-scale/denormalized data, high write throughput, easy horizontal scaling, few complex joins",
                    "choose SQL for strong consistency, relations, and ACID transactions"),
                q("sd2q2", "Sharding vs replication — what does each solve?",
                    "sharding splits data across nodes for scale / write throughput",
                    "replication copies data for read scaling and availability/failover"),
                q("sd2q3", "State the CAP theorem in one sentence, with a practical implication.",
                    "under a network partition you must choose consistency or availability",
                    "e.g. pick AP (eventual consistency) or CP depending on the use case"))));

        add(new Module("SD3", t, 2, "Caching, Queues & Async",
            List.of("Caching strategies & invalidation", "Message queues", "Async processing"), base,
            List.of("Cache invalidation trade-offs", "Why decouple with a queue"),
            List.of(
                q("sd3q1", "Why cache, and name a hard problem caching introduces.",
                    "reduces latency and DB load", "cache invalidation is hard — stale data, TTL vs write-through/write-back trade-offs"),
                q("sd3q2", "What problem does a message queue solve?",
                    "decouples producers from consumers", "smooths load spikes and enables async processing, retries, and resilience"),
                q("sd3q3", "Cache-aside vs write-through — what's the difference?",
                    "cache-aside: app loads on miss, writes DB then invalidates cache",
                    "write-through: write cache and DB together — fresher reads, higher write latency"))));

        add(new Module("SD4", t, 3, "Scalable APIs & Microservices",
            List.of("API design & rate limiting", "Monolith vs microservices", "Resilience patterns"), base,
            List.of("Trade-offs, not buzzwords", "Name concrete resilience patterns"),
            List.of(
                q("sd4q1", "Monolith vs microservices — the key trade-offs?",
                    "monolith: simpler to build/deploy but harder to scale teams and parts independently",
                    "microservices: independent scaling/deploy but network complexity, data consistency, and ops overhead"),
                q("sd4q2", "Why and how do you rate-limit an API?",
                    "protect from abuse/overload, ensure fairness, control cost",
                    "algorithms like token bucket or leaky bucket, applied per API key/user"),
                q("sd4q3", "Name two resilience patterns for service-to-service calls and what they prevent.",
                    "timeouts and retries with backoff", "circuit breaker (stop calling a failing service), bulkhead, fallback"))));

        add(new Module("SD5", t, 4, "AI System Design",
            List.of("Design a RAG pipeline", "LLM gateway (cache/route/limit)", "Scale a vector DB"), base,
            List.of("Reuse your GenAI edge", "Cost & latency at scale"),
            List.of(
                q("sd5q1", "Sketch the high-level components of a RAG system.",
                    "ingestion: chunk -> embed -> store in a vector DB", "retrieval (optionally re-rank) -> augment the prompt -> LLM generates",
                    "plus caching, evals, and guardrails"),
                q("sd5q2", "What does an LLM gateway do in production?",
                    "a central layer for caching, model routing (cheap vs strong), rate limiting, retries/fallback",
                    "and cost/usage tracking + key management"),
                q("sd5q3", "How would you scale a vector DB to 100M vectors with low latency?",
                    "an ANN index (HNSW/IVF)", "sharding/partitioning + read replicas, cache hot queries, tune the recall/latency trade-off"))));
    }
}
