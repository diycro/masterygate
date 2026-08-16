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
        buildJavaFullStack();
        buildPython();
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

    // ------------------------------------------------------------------ Java Full-Stack track
    private void buildJavaFullStack() {
        String t = "javafs";
        topics.put(t, new Topic(t, "Java Full-Stack Engineer",
                "Senior-level Java/Spring depth — your actual current stack, interview-ready."));

        add(new Module("JFS1", t, 0, "Core Java & Concurrency",
            List.of("Collections internals", "Concurrency (threads, executors, locks)", "JVM memory & garbage collection", "Generics & functional interfaces"),
            List.of(
                free("Java Concurrency in Practice (summary/guide)", "Baeldung", "https://www.baeldung.com/java-concurrency", "guide"),
                free("HashMap internals", "Baeldung", "https://www.baeldung.com/java-hashmap", "guide"),
                free("JVM Garbage Collection basics", "Oracle docs", "https://docs.oracle.com/en/java/javase/21/gctuning/", "docs"),
                paid("Java Multithreading & Concurrency", "Udemy", "https://www.udemy.com/courses/search/?q=java+concurrency", "search")),
            List.of("Explain HashMap internals", "Thread-safety trade-offs", "GC tuning basics"),
            List.of(
                q("jfs1q1", "How does a HashMap work internally, and what happens on a hash collision?",
                    "keys are hashed to a bucket index; Java 8+ uses a linked list per bucket that treeifies into a red-black tree once a bucket gets large enough",
                    "collisions are resolved by chaining within the bucket", "resizing (rehashing) happens once the load factor threshold is exceeded"),
                q("jfs1q2", "ArrayList vs LinkedList — when would you choose each?",
                    "ArrayList: O(1) index access, O(n) insert/remove in the middle, contiguous/cache-friendly",
                    "LinkedList: O(1) insert/remove given a node reference, O(n) access by index, more memory overhead per element",
                    "ArrayList is the default choice for most cases; LinkedList rarely wins in practice on modern hardware"),
                q("jfs1q3", "What's the difference between synchronized, a ReentrantLock, and using java.util.concurrent classes like ConcurrentHashMap?",
                    "synchronized: simplest, JVM-managed, coarse-grained, no timeout/tryLock ability",
                    "ReentrantLock: explicit lock/unlock, supports tryLock/timeouts/fairness, more flexible but must be unlocked in a finally block",
                    "ConcurrentHashMap: lock-striped/lock-free concurrent data structure — usually the right choice over manually synchronizing a HashMap"),
                q("jfs1q4", "What triggers a garbage collection, and what's the difference between minor and major/full GC?",
                    "objects are collected when no longer reachable from GC roots",
                    "minor GC: cleans the young generation, frequent and fast",
                    "major/full GC: cleans the old generation (or the whole heap), less frequent but much more expensive/pausing"))));

        add(new Module("JFS2", t, 1, "Spring & Spring Boot Fundamentals",
            List.of("Dependency injection & bean lifecycle", "Auto-configuration", "Profiles & externalized config", "AOP basics"),
            List.of(
                free("Spring Core / IoC container", "Spring", "https://docs.spring.io/spring-framework/reference/core/beans/introduction.html", "docs"),
                free("Spring Boot Auto-configuration", "Baeldung", "https://www.baeldung.com/spring-boot-custom-auto-configuration", "guide"),
                free("Advanced Spring Boot Interview Questions", "Medium", "https://medium.com/@sharmapraveen91/30-advanced-spring-boot-interview-questions-for-experienced-professionals-3574173472c1", "article"),
                paid("Spring Framework masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+framework+5", "search")),
            List.of("Bean scopes & lifecycle", "How auto-configuration decides what to wire", "Constructor vs field injection"),
            List.of(
                q("jfs2q1", "What is dependency injection, and why does Spring favor constructor injection over field injection?",
                    "DI means an object's dependencies are provided externally rather than created internally, decoupling components",
                    "constructor injection makes dependencies explicit/immutable/required, enables easier testing (no reflection needed), and fails fast at startup if a dependency is missing",
                    "field injection hides dependencies, allows partially-constructed objects, and complicates unit testing"),
                q("jfs2q2", "Explain the Spring bean lifecycle at a high level.",
                    "instantiate -> populate properties/inject dependencies -> call Aware interfaces -> BeanPostProcessors (before) -> @PostConstruct/InitializingBean -> bean ready for use -> @PreDestroy/DisposableBean on shutdown",
                    "understanding this matters for correctly hooking initialization/cleanup logic"),
                q("jfs2q3", "How does Spring Boot's auto-configuration decide what to configure?",
                    "@ConditionalOnClass/@ConditionalOnMissingBean/@ConditionalOnProperty etc. gate each auto-configuration class",
                    "it inspects the classpath and existing bean definitions, only activating configuration when its conditions are met",
                    "this is why adding a dependency (e.g., a JDBC driver) can automatically wire up related beans"),
                q("jfs2q4", "Singleton vs prototype bean scope — what's the difference and a case for each?",
                    "singleton (default): one shared instance per Spring container — fine for stateless services",
                    "prototype: a new instance every time the bean is requested — used for stateful, non-thread-safe objects"))));

        add(new Module("JFS3", t, 2, "Spring Data JPA & Transactions",
            List.of("Transaction propagation & isolation", "Lazy vs eager loading", "N+1 query problem", "Entity lifecycle"),
            List.of(
                free("Spring Transaction Management", "Spring", "https://docs.spring.io/spring-framework/reference/data-access/transaction.html", "docs"),
                free("Hibernate N+1 problem", "Baeldung", "https://www.baeldung.com/hibernate-N-plus-1-problem-different-fetching-strategies", "guide"),
                free("JPA entity lifecycle", "Baeldung", "https://www.baeldung.com/jpa-entity-lifecycle", "guide"),
                paid("Spring Data JPA masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+data+jpa", "search")),
            List.of("Explain @Transactional propagation", "Diagnose an N+1 query", "Lazy-loading pitfalls (LazyInitializationException)"),
            List.of(
                q("jfs3q1", "What is the N+1 query problem, and how do you fix it?",
                    "fetching a list of N entities, then lazily fetching a related entity for EACH one triggers 1 + N queries instead of one efficient query",
                    "fix with a JOIN FETCH in JPQL, an @EntityGraph, or batch fetching, to retrieve related data in one (or few) queries"),
                q("jfs3q2", "Explain @Transactional propagation levels — REQUIRED vs REQUIRES_NEW.",
                    "REQUIRED (default): joins an existing transaction if one exists, or creates a new one",
                    "REQUIRES_NEW: always suspends any existing transaction and starts a new, independent one — a rollback in the outer transaction won't undo what REQUIRES_NEW already committed"),
                q("jfs3q3", "What causes a LazyInitializationException, and how do you avoid it?",
                    "accessing a lazily-loaded association after the persistence session/transaction that fetched the entity has closed",
                    "fix by fetching eagerly when needed (JOIN FETCH), keeping the access within the transactional boundary, or using a DTO projection instead of the raw entity"),
                q("jfs3q4", "Lazy vs eager fetching in JPA — what's the trade-off, and what's the default for @OneToMany vs @ManyToOne?",
                    "lazy loads the association only when accessed (saves memory/query cost, but risks LazyInitializationException outside a session)",
                    "eager always loads it immediately (simpler, but can over-fetch and hurt performance)",
                    "@OneToMany/@ManyToMany default to lazy; @ManyToOne/@OneToOne default to eager"))));

        add(new Module("JFS4", t, 3, "REST APIs, Validation & Security",
            List.of("REST API design", "Bean validation & exception handling", "OAuth2/JWT authentication", "CORS & CSRF"),
            List.of(
                free("Spring Security reference", "Spring", "https://docs.spring.io/spring-security/reference/", "docs"),
                free("Spring Boot exception handling", "Baeldung", "https://www.baeldung.com/exception-handling-for-rest-with-spring", "guide"),
                free("REST API design best practices", "Microsoft docs", "https://learn.microsoft.com/en-us/azure/architecture/best-practices/api-design", "guide"),
                paid("Spring Security masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+security+oauth2+jwt", "search")),
            List.of("Design a clean REST error response", "Explain JWT vs session auth", "CORS vs CSRF — what each protects against"),
            List.of(
                q("jfs4q1", "How do you handle exceptions cleanly across a Spring Boot REST API?",
                    "a centralized @ControllerAdvice with @ExceptionHandler methods mapping exceptions to consistent error responses (status code, message, timestamp)",
                    "avoids scattering try/catch blocks across controllers and keeps error responses consistent for API consumers"),
                q("jfs4q2", "JWT-based auth vs traditional session-based auth — what's the trade-off?",
                    "JWT: stateless, the token itself carries the claims, scales horizontally without shared session storage, but harder to revoke before expiry",
                    "session-based: server holds state (in-memory or shared store), easy to revoke immediately, but needs sticky sessions or a shared session store to scale"),
                q("jfs4q3", "What's the difference between CORS and CSRF, and what does each protect against?",
                    "CORS (Cross-Origin Resource Sharing): a browser mechanism controlling which origins are ALLOWED to call your API from client-side JS",
                    "CSRF (Cross-Site Request Forgery): an attack where a malicious site tricks a logged-in user's browser into making an unwanted request; defended with CSRF tokens or SameSite cookies",
                    "CORS is about permission to call; CSRF protection is about proving the request was intentional"),
                q("jfs4q4", "How do you version a REST API, and what are the trade-offs of common approaches?",
                    "URI versioning (/api/v1/...): simple, visible, but can lead to code duplication",
                    "header/content-negotiation versioning: cleaner URIs but less discoverable and harder to test manually",
                    "the choice depends on how many consumers you have and how often breaking changes occur"))));

        add(new Module("JFS5", t, 4, "Testing (JUnit, Mockito, Integration)",
            List.of("Unit vs integration testing", "Mockito mocking", "@SpringBootTest & test slices", "Testcontainers basics"),
            List.of(
                free("Testing in Spring Boot", "Spring", "https://docs.spring.io/spring-boot/reference/testing/index.html", "docs"),
                free("Mockito tutorial", "Baeldung", "https://www.baeldung.com/mockito-series", "guide"),
                free("Testcontainers for Java", "Testcontainers", "https://testcontainers.com/guides/getting-started-with-testcontainers-for-java/", "guide"),
                paid("Spring Boot Testing masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+boot+testing+junit+mockito", "search")),
            List.of("Unit vs integration test boundary", "Mock vs spy", "Why testcontainers over an in-memory DB for integration tests"),
            List.of(
                q("jfs5q1", "What's the difference between a unit test and an integration test, and where's the boundary?",
                    "unit test: tests one class/method in isolation, dependencies mocked, fast, no Spring context",
                    "integration test: verifies multiple components working together (e.g., a real DB, the Spring context), slower but catches wiring/config issues unit tests miss"),
                q("jfs5q2", "Mock vs spy in Mockito — what's the difference?",
                    "mock: a fully fake object with no real behavior unless stubbed",
                    "spy: wraps a REAL object, calling real methods by default unless specific methods are stubbed — useful for partial mocking"),
                q("jfs5q3", "Why use Testcontainers instead of an in-memory database (like H2) for integration tests?",
                    "an in-memory DB can behave differently from your real production database (SQL dialect quirks, feature gaps)",
                    "Testcontainers spins up the REAL database engine (e.g., actual Postgres) in Docker for the test, giving much higher confidence the tests reflect production behavior"),
                q("jfs5q4", "What does @SpringBootTest do, and why would you use a narrower test slice like @WebMvcTest instead?",
                    "@SpringBootTest loads the full application context — thorough but slow",
                    "@WebMvcTest (or @DataJpaTest, etc.) loads only the relevant slice of the context — much faster, appropriate when you're only testing one layer"))));

        add(new Module("JFS6", t, 5, "Microservices, Resilience & Deployment",
            List.of("Service-to-service communication", "Resilience patterns (circuit breaker, retry)", "Docker & container basics", "CI/CD for a Spring app"),
            List.of(
                free("Spring Cloud reference", "Spring", "https://spring.io/projects/spring-cloud", "docs"),
                free("Resilience4j guide", "Resilience4j", "https://resilience4j.readme.io/docs/getting-started", "docs"),
                free("Docker for Java developers", "Docker docs", "https://docs.docker.com/language/java/", "guide"),
                paid("Microservices with Spring Boot & Spring Cloud", "Udemy", "https://www.udemy.com/courses/search/?q=microservices+spring+cloud", "search")),
            List.of("Design a resilient service call", "Explain a multi-stage Dockerfile for a Spring app", "CI/CD pipeline stages"),
            List.of(
                q("jfs6q1", "How do you make a call to another microservice resilient to that service being slow or down?",
                    "set an explicit timeout so a slow dependency can't hang your thread indefinitely",
                    "add retries with backoff for transient failures",
                    "add a circuit breaker (e.g., Resilience4j) to stop calling a failing service and fail fast, with a fallback response"),
                q("jfs6q2", "What's the benefit of a multi-stage Docker build for a Spring Boot app?",
                    "one stage builds/compiles the app (with the full JDK + build tool), a second stage copies only the built artifact into a minimal runtime image",
                    "this keeps the final image small and avoids shipping build tools/source code in production"),
                q("jfs6q3", "What are the typical stages of a CI/CD pipeline for a Spring Boot service?",
                    "build/compile -> run unit + integration tests -> static analysis/security scan -> build the container image -> deploy to staging -> (manual or automated) promote to production",
                    "the goal is to catch issues as early and cheaply as possible in the pipeline"),
                q("jfs6q4", "Synchronous REST calls vs an async message queue between microservices — when do you choose each?",
                    "sync REST: simple, immediate response needed, tighter coupling, caller blocks on the callee's availability",
                    "async messaging: decouples services, smooths load, survives temporary downstream outages, but adds eventual-consistency complexity"))));
    }

    // ------------------------------------------------------------------ Python track
    private void buildPython() {
        String t = "python";
        topics.put(t, new Topic(t, "Python Programming",
                "Core Python for backend and GenAI engineering — syntax to concurrency, interview-ready."));

        List<Resource> base = List.of(
                free("The Python Tutorial (official docs)", "python.org", "https://docs.python.org/3/tutorial/", "docs"),
                free("Real Python", "realpython.com", "https://realpython.com/", "tutorials"),
                free("Automate the Boring Stuff with Python", "free book", "https://automatetheboringstuff.com/", "book"),
                paid("100 Days of Code: Python", "Udemy", "https://www.udemy.com/courses/search/?q=python+bootcamp", "search"));

        add(new Module("PY1", t, 0, "Python Fundamentals & Syntax",
            List.of("Variables, dynamic typing & mutability", "Control flow", "Functions, arguments & scope", "String formatting"),
            base,
            List.of("Mutable vs immutable default-argument trap", "Explain LEGB scoping", "f-strings vs .format()"),
            List.of(
                q("py1q1", "Python is dynamically typed — what does that mean, and how does it differ from static typing?",
                    "a variable's type is determined at runtime by the object it references, not declared ahead of time",
                    "the same variable name can be rebound to a different type later", "static typing (Java/C#) checks types at compile time instead"),
                q("py1q2", "What is the classic 'mutable default argument' bug in Python, and how do you avoid it?",
                    "a default argument like def f(items=[]) is evaluated ONCE at function definition time, not on every call",
                    "so mutations to that default list persist and leak across calls", "fix: use None as the default and create the mutable object inside the function body"),
                q("py1q3", "Explain Python's LEGB rule for variable scope resolution.",
                    "Local -> Enclosing -> Global -> Built-in — the order Python searches when resolving a name",
                    "a local variable shadows an enclosing/global one of the same name; `global`/`nonlocal` keywords are needed to assign to an outer scope from inside a function"),
                q("py1q4", "What's the difference between == and is in Python?",
                    "== compares value/content equality (calls __eq__)", "is compares object identity (same object in memory)",
                    "small integers and interned strings may be `is`-equal due to caching, which is an implementation detail you shouldn't rely on"))));

        add(new Module("PY2", t, 1, "Data Structures & Collections",
            List.of("list/tuple/dict/set trade-offs", "Slicing", "Comprehensions", "Shallow vs deep copy"),
            base,
            List.of("Pick the right collection and justify it", "Comprehension readability limits", "Copy semantics bugs"),
            List.of(
                q("py2q1", "list vs tuple vs set vs dict — when do you reach for each?",
                    "list: ordered, mutable sequence — general-purpose collection",
                    "tuple: ordered, immutable — fixed records, safe to use as a dict key/hashable",
                    "set: unordered, unique elements, O(1) average membership test", "dict: key-value mapping, O(1) average lookup by key"),
                q("py2q2", "What does list slicing like a[1:4:2] mean, and what does a[::-1] do?",
                    "a[start:stop:step] returns elements from index start up to (not including) stop, taking every `step`-th element",
                    "a[::-1] reverses the sequence — start and stop default to the full range, step -1 walks backward"),
                q("py2q3", "How would you rewrite a filter-and-transform for loop as a list comprehension, and when should you NOT use one?",
                    "a comprehension like [x*x for x in nums if x % 2 == 0] replaces a for loop that filters then appends",
                    "comprehensions are idiomatic and often faster for simple transforms/filters",
                    "nesting more than one or two conditions/loops hurts readability — a plain loop or a named function is clearer at that point"),
                q("py2q4", "What's the difference between a shallow copy and a deep copy, and where does a shallow copy bite you?",
                    "a shallow copy (list(x), x.copy(), copy.copy()) creates a new outer container but reuses references to the same inner/nested objects",
                    "mutating a nested list/dict inside a shallow copy also mutates the original", "copy.deepcopy() recursively copies nested objects to avoid this"))));

        add(new Module("PY3", t, 2, "Object-Oriented Python",
            List.of("Classes, __init__ & self", "Inheritance & MRO", "Dunder/magic methods", "@property & encapsulation"),
            base,
            List.of("Explain a dunder method's purpose", "Method Resolution Order", "When composition beats inheritance"),
            List.of(
                q("py3q1", "What do __init__ and self actually do in a Python class?",
                    "__init__ is the initializer, called automatically right after a new instance is created, to set up its initial state",
                    "self is the instance itself, passed automatically as the first parameter to instance methods — it's how a method accesses/modifies that specific object's attributes"),
                q("py3q2", "What is Method Resolution Order (MRO), and why does it matter with multiple inheritance?",
                    "MRO is the order Python searches base classes to resolve an attribute/method lookup, computed via the C3 linearization algorithm",
                    "it matters because with multiple inheritance, two parent classes could define the same method — MRO deterministically decides which one wins",
                    "you can inspect it with ClassName.__mro__ or ClassName.mro()"),
                q("py3q3", "What does implementing __eq__ and __hash__ let you do, and why must they be consistent?",
                    "__eq__ defines what == means for your objects; __hash__ defines their hash bucket for use as dict keys / set members",
                    "if two objects are equal (__eq__ True) but have different hashes, they'll silently break in sets/dicts — Python's contract requires equal objects to hash equally"),
                q("py3q4", "What does the @property decorator do, and why use it instead of plain getter/setter methods?",
                    "it lets a method be accessed like a plain attribute (obj.value instead of obj.get_value()) while still running code on access",
                    "it lets you start with plain public attributes and add validation/computed logic later without breaking the class's external API",
                    "common use: exposing a computed value (like a circle's area from its radius) as if it were a stored attribute"))));

        add(new Module("PY4", t, 3, "Decorators, Generators & Error Handling",
            List.of("Decorators & closures", "Generators & yield", "Exception handling & custom exceptions", "Context managers"),
            base,
            List.of("Write a decorator from scratch", "Generator vs list memory trade-off", "try/except/else/finally semantics"),
            List.of(
                q("py4q1", "Explain how you'd write a decorator that times how long a function takes to run, mechanically.",
                    "a decorator is a function that takes a function and returns a new (wrapper) function that adds behavior around the original call",
                    "the wrapper records a start time, calls the original function, records the end time, and returns the original result",
                    "functools.wraps is used on the wrapper to preserve the original function's name/docstring for introspection — a common thing junior candidates forget"),
                q("py4q2", "What's the difference between a generator and a normal function that returns a list, and why does it matter for memory?",
                    "a normal function computes and returns the entire list in memory at once",
                    "a generator (using yield) produces values lazily, one at a time, pausing its execution state between each — only one value needs to be in memory at a time",
                    "for very large or infinite sequences, a generator avoids loading everything into memory upfront"),
                q("py4q3", "Explain what try/except/else/finally each do, in order.",
                    "try: the code that might raise an exception", "except: runs only if a matching exception was raised in the try block",
                    "else: runs only if the try block completed with NO exception raised", "finally: always runs, whether or not an exception occurred — used for cleanup (closing files/connections)"),
                q("py4q4", "How does Python's `with` statement (a context manager) work, and why is it preferred over manual try/finally for resource cleanup?",
                    "the object's __enter__ method runs at the start of the block and __exit__ runs at the end — even if an exception occurred inside the block",
                    "a common example is opening a file: it's guaranteed to be closed when the block exits, even if an error was raised while reading it",
                    "it guarantees cleanup happens exactly once, in the right place, without the boilerplate and easy-to-forget-a-branch risk of manual try/finally"))));

        add(new Module("PY5", t, 4, "Modules, Typing & Testing",
            List.of("venv & pip / packaging", "Type hints", "pytest fundamentals", "Fixtures & mocking"),
            List.of(
                free("Python venv (official docs)", "python.org", "https://docs.python.org/3/library/venv.html", "docs"),
                free("Type hints cheat sheet", "mypy docs", "https://mypy.readthedocs.io/en/stable/cheat_sheet_py3.html", "docs"),
                free("pytest documentation", "pytest.org", "https://docs.pytest.org/", "docs"),
                paid("Python Testing with pytest", "Udemy", "https://www.udemy.com/courses/search/?q=pytest", "search")),
            List.of("Why type hints don't enforce anything at runtime", "Structuring a pytest suite", "Mocking an external call"),
            List.of(
                q("py5q1", "Do Python type hints get enforced at runtime? What are they actually for?",
                    "no — type hints are not checked or enforced when the code runs; Python remains fully dynamically typed at runtime",
                    "calling a hinted function with the 'wrong' type still runs without error — nothing raises a TypeError from the hints themselves",
                    "they're read by external static-analysis tools (mypy, IDEs, linters) to catch type mistakes before running the code, and they document intent for other developers"),
                q("py5q2", "What's the difference between a virtual environment (venv) and a requirements.txt file?",
                    "a venv is an isolated Python installation/directory holding a project's own interpreter and installed packages, separate from the system Python",
                    "requirements.txt is just a text list of package names/versions to install — it doesn't isolate anything by itself; `pip install -r requirements.txt` installs them, typically INTO an active venv"),
                q("py5q3", "What is a pytest fixture, and what problem does it solve?",
                    "a fixture is a reusable setup function (marked with @pytest.fixture) that provides test data, a DB connection, or a mock",
                    "pytest injects it automatically into any test function that names it as a parameter",
                    "it avoids duplicating the same setup/teardown code across many test functions"),
                q("py5q4", "How would you mock an external API call in a test so it doesn't make a real network request?",
                    "replace the function/method that performs the network call with a fake (a mock or monkeypatched function) that returns a canned response",
                    "patch it at the point where it's USED, not just where it's defined, so the test exercises your code's logic without depending on network availability or flakiness"))));

        add(new Module("PY6", t, 5, "Concurrency & Performance",
            List.of("The GIL", "threading vs multiprocessing", "asyncio basics", "Profiling & optimization"),
            List.of(
                free("What is the Python GIL?", "realpython.com", "https://realpython.com/python-gil/", "article"),
                free("asyncio — official docs", "python.org", "https://docs.python.org/3/library/asyncio.html", "docs"),
                free("Speed Up Your Python Program With Concurrency", "realpython.com", "https://realpython.com/python-concurrency/", "article"),
                paid("Python concurrency & parallelism", "Udemy", "https://www.udemy.com/courses/search/?q=python+concurrency", "search")),
            List.of("Explain the GIL's real impact", "threading vs multiprocessing vs asyncio, correctly chosen", "Where to actually profile before optimizing"),
            List.of(
                q("py6q1", "What is the GIL, and what does it actually prevent?",
                    "the Global Interpreter Lock ensures only one thread executes Python bytecode at a time within a single process, even on a multi-core machine",
                    "it means Python threads do NOT give you true parallel CPU-bound execution — two threads can't run Python code simultaneously on two cores"),
                q("py6q2", "Given the GIL, when is Python threading still useful, and when do you need multiprocessing instead?",
                    "threading still helps for I/O-bound work (network calls, file/disk I/O) — the GIL is released while waiting on I/O, so threads overlap that waiting time",
                    "multiprocessing runs separate Python processes, each with its own GIL/interpreter, giving true parallelism for CPU-bound work (heavy computation)",
                    "rule of thumb: I/O-bound -> threading (or asyncio); CPU-bound -> multiprocessing"),
                q("py6q3", "What problem does asyncio solve, and how is it different from threading?",
                    "asyncio runs many I/O-bound tasks concurrently on a SINGLE thread using cooperative multitasking — a task voluntarily yields control (at an `await`) while waiting on I/O instead of blocking",
                    "```python\nimport asyncio\n\nasync def fetch(url):\n    await asyncio.sleep(1)  # simulates a non-blocking I/O wait\n    return f\"data from {url}\"\n\nasync def main():\n    results = await asyncio.gather(fetch(\"a\"), fetch(\"b\"))\n```",
                    "unlike threading, there's no preemptive context-switching or thread-safety overhead — but a single long-running CPU-bound `await`-free call will block the whole event loop"),
                q("py6q4", "Before optimizing slow Python code, what should you do first, and why?",
                    "profile it (e.g., with cProfile or a line profiler) to find where time is ACTUALLY being spent, rather than guessing",
                    "most code has one or two real bottlenecks — optimizing code that isn't actually slow wastes effort and adds complexity for no measurable benefit"))));
    }
}
