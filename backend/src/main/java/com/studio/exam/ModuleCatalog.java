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
        buildJava();
        buildSpring();
        buildSpringBoot();
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

    // ------------------------------------------------------------------ Core Java track (standalone)
    private void buildJava() {
        String t = "java";
        topics.put(t, new Topic(t, "Core Java Engineer",
                "Java the language, deep — collections, concurrency, the JVM, and modern Java 8-21, independent of any framework."));

        add(new Module("JAVA1", t, 0, "OOP & Language Fundamentals",
            List.of("The four pillars of OOP in Java", "String immutability & the string pool", "equals/hashCode/toString contracts", "Access modifiers & packages"),
            List.of(
                free("Java OOP concepts", "GeeksforGeeks", "https://www.geeksforgeeks.org/java/object-oriented-programming-oops-concept-in-java/", "guide"),
                free("Why String is immutable in Java", "Baeldung", "https://www.baeldung.com/java-string-immutable", "guide"),
                free("Java Language Specification", "Oracle docs", "https://docs.oracle.com/javase/specs/", "docs"),
                paid("Java Programming Masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=java+programming+masterclass", "search")),
            List.of("Explain each OOP pillar with a Java example", "Why overriding equals() requires overriding hashCode()", "== vs .equals() on objects and Strings"),
            List.of(
                q("java1q1", "What are the four pillars of OOP, and how does Java express each one?",
                    "encapsulation: private fields + public getters/setters hide internal state",
                    "inheritance: extends/implements lets a class reuse and specialize another type's behavior",
                    "polymorphism: a subclass reference used through its parent type, with the actual overridden method resolved at runtime (dynamic dispatch)",
                    "abstraction: abstract classes and interfaces expose a contract without committing to implementation details"),
                q("java1q2", "Why is String immutable in Java, and what is the string constant pool?",
                    "immutability makes String safe to share across threads without synchronization, safe as a HashMap key (hashCode can be cached), and safe to intern",
                    "the string pool is a special memory area where literal strings are cached and reused — two identical literals point to the same object, saving memory",
                    "string concatenation with + creates new objects, which is why StringBuilder is preferred in loops"),
                q("java1q3", "What is the contract between equals() and hashCode(), and what breaks if you violate it?",
                    "if two objects are equal per equals(), they MUST return the same hashCode()",
                    "the reverse isn't required — unequal objects may share a hash code (a collision), just less efficiently",
                    "violating this contract breaks hash-based collections like HashMap/HashSet: an object can be 'lost' (inserted under one bucket, looked up in another) even though equals() says it should be found"),
                q("java1q4", "== vs .equals() — what's the difference for objects, and why does it matter for String specifically?",
                    "== compares references (are these the same object in memory) for any object type",
                    ".equals() compares logical/value equality, as defined by the class's own override",
                    "for Strings, two different literals may or may not be == depending on interning, so relying on == for string comparison is a common, subtle bug — always use .equals()"))));

        add(new Module("JAVA2", t, 1, "Collections Framework Deep Dive",
            List.of("List/Set/Map implementations & internals", "Comparable vs Comparator", "Fail-fast vs fail-safe iterators", "Choosing the right collection"),
            List.of(
                free("Java Collections Framework overview", "Oracle docs", "https://docs.oracle.com/javase/tutorial/collections/", "docs"),
                free("HashMap vs TreeMap vs LinkedHashMap", "Baeldung", "https://www.baeldung.com/java-hashmap-vs-treemap-vs-linkedhashmap", "guide"),
                free("ConcurrentModificationException explained", "Baeldung", "https://www.baeldung.com/java-concurrentmodificationexception", "guide"),
                paid("Java Collections & Generics deep dive", "Udemy", "https://www.udemy.com/courses/search/?q=java+collections+framework", "search")),
            List.of("Pick the right List/Set/Map for a scenario and justify it", "Explain ConcurrentModificationException", "Comparable vs Comparator, with when to use each"),
            List.of(
                q("java2q1", "HashSet vs TreeSet vs LinkedHashSet — how do they differ and when would you pick each?",
                    "HashSet: no ordering guarantee, O(1) average add/contains, backed by a HashMap",
                    "LinkedHashSet: preserves insertion order, small extra overhead over HashSet",
                    "TreeSet: keeps elements sorted (natural order or a Comparator), O(log n) operations, backed by a red-black tree",
                    "pick HashSet by default for pure uniqueness, LinkedHashSet when insertion order matters for iteration/output, TreeSet when you need sorted iteration"),
                q("java2q2", "What causes a ConcurrentModificationException, and how do you safely remove elements while iterating?",
                    "structurally modifying a collection (add/remove) directly while iterating over it with a for-each loop invalidates the iterator's internal modCount check",
                    "fix by using the Iterator's own remove() method, or a CopyOnWriteArrayList/ConcurrentHashMap for concurrent scenarios, or collecting items to remove and removing them after the loop"),
                q("java2q3", "Comparable vs Comparator — what's the difference and when do you use each?",
                    "Comparable: implemented BY the class itself (compareTo), defines that type's single 'natural' ordering",
                    "Comparator: a separate strategy object (compare), lets you define multiple different orderings without touching the class, and can be composed (thenComparing)",
                    "use Comparable when there's one obvious default ordering; use Comparator for ad-hoc or multiple orderings, especially for classes you don't own"),
                q("java2q4", "Why is it usually a bad idea to use a mutable object as a HashMap key?",
                    "the key's hashCode is used to place it in a bucket at insertion time",
                    "if the key object is later mutated in a way that changes its hashCode/equals result, the map can no longer find it in the correct bucket — the entry becomes effectively lost",
                    "prefer immutable keys (String, wrapper types, records, or objects deliberately designed to be immutable)"))));

        add(new Module("JAVA3", t, 2, "Exceptions, I/O & NIO",
            List.of("Checked vs unchecked exceptions", "try-with-resources & the AutoCloseable contract", "Custom exception design", "java.nio basics"),
            List.of(
                free("Java exceptions tutorial", "Oracle docs", "https://docs.oracle.com/javase/tutorial/essential/exceptions/", "docs"),
                free("try-with-resources", "Baeldung", "https://www.baeldung.com/java-try-with-resources", "guide"),
                free("java.nio.file basics", "Baeldung", "https://www.baeldung.com/java-nio-2-file-api", "guide"),
                paid("Java exception handling best practices", "Udemy", "https://www.udemy.com/courses/search/?q=java+exception+handling", "search")),
            List.of("Checked vs unchecked, with a design rationale", "Explain try-with-resources under the hood", "When to write a custom exception"),
            List.of(
                q("java3q1", "Checked vs unchecked exceptions — what's the difference, and how should you decide which to use for a new exception type?",
                    "checked exceptions (extend Exception, not RuntimeException) must be declared or caught — the compiler enforces handling",
                    "unchecked exceptions (extend RuntimeException) don't require declaration — used for programming errors or conditions the caller usually can't recover from",
                    "modern guidance leans toward unchecked for most application exceptions, reserving checked exceptions for conditions a caller is genuinely expected to recover from"),
                q("java3q2", "What does try-with-resources do, and what interface must a resource implement to use it?",
                    "the resource must implement AutoCloseable (or Closeable)",
                    "the compiler automatically generates a finally block that calls close() on the resource(s), even if an exception is thrown, in reverse order of declaration",
                    "this eliminates the boilerplate and bug-risk of manually writing finally { resource.close(); }"),
                q("java3q3", "When does it make sense to create a custom exception class instead of reusing a built-in one?",
                    "when the exception represents a distinct, meaningful business/domain condition callers need to catch and handle specifically (e.g., InsufficientFundsException)",
                    "custom exceptions can carry extra context (fields) relevant to that failure",
                    "avoid overusing custom exceptions for cases a standard exception (IllegalArgumentException, IllegalStateException) already covers clearly"),
                q("java3q4", "What's the difference between the older java.io streams and java.nio, at a conceptual level?",
                    "java.io is stream-based and blocking — you read/write sequentially and the thread blocks while waiting",
                    "java.nio adds buffer/channel-based I/O and (via NIO.2 in java.nio.file) a much better file API (Path, Files), plus non-blocking/selector-based I/O for scalable network servers",
                    "for typical file operations today, java.nio.file's Files/Path API is preferred over the legacy File class"))));

        add(new Module("JAVA4", t, 3, "Multithreading & Concurrency",
            List.of("Thread lifecycle & creation", "ExecutorService & thread pools", "Locks, synchronized & the Java Memory Model", "CompletableFuture & async composition"),
            List.of(
                free("Java Concurrency in Practice (summary/guide)", "Baeldung", "https://www.baeldung.com/java-concurrency", "guide"),
                free("ExecutorService guide", "Baeldung", "https://www.baeldung.com/java-executor-service-tutorial", "guide"),
                free("Java Memory Model explained", "Baeldung", "https://www.baeldung.com/java-volatile", "guide"),
                paid("Java Multithreading & Concurrency", "Udemy", "https://www.udemy.com/courses/search/?q=java+concurrency", "search")),
            List.of("Explain the Java Memory Model & volatile", "Design a thread pool for a workload", "CompletableFuture composition (thenApply/thenCompose)"),
            List.of(
                q("java4q1", "What does the volatile keyword guarantee, and what does it NOT guarantee?",
                    "volatile guarantees visibility — a write to a volatile field is immediately visible to all threads (no stale cached value), and it prevents certain reorderings",
                    "it does NOT guarantee atomicity for compound operations like i++ — a read-modify-write on a volatile field can still race",
                    "use volatile for simple flags/single-value visibility; use synchronized/atomic classes/locks when you need atomicity too"),
                q("java4q2", "Why prefer an ExecutorService/thread pool over creating raw new Thread() instances?",
                    "creating a new OS thread per task is expensive (memory + context-switch overhead) and gives you no control over concurrency limits",
                    "a thread pool reuses a bounded set of worker threads, queues excess work, and gives you lifecycle control (shutdown, awaitTermination) and back-pressure",
                    "choosing pool size/type (fixed, cached, work-stealing/ForkJoinPool) should match whether the workload is CPU-bound or I/O-bound"),
                q("java4q3", "What is a race condition, and name two different ways to prevent one on a shared counter.",
                    "a race condition happens when multiple threads read-modify-write shared state without coordination, so the final result depends on unlucky interleaving",
                    "option 1: synchronize the critical section (synchronized block/method or a Lock) so only one thread mutates it at a time",
                    "option 2: use an atomic class (AtomicInteger/AtomicLong) which performs the update via a lock-free CAS (compare-and-swap) loop"),
                q("java4q4", "What does CompletableFuture.thenApply vs thenCompose do differently, and why does that distinction matter?",
                    "thenApply transforms the result with a plain function (T -> U) — use it when the next step is synchronous",
                    "thenCompose chains to another CompletableFuture-returning function (T -> CompletableFuture<U>) and flattens the result — use it when the next step is itself async, to avoid nested futures",
                    "using thenApply where thenCompose is needed produces a CompletableFuture<CompletableFuture<U>>, which is almost never what you want"))));

        add(new Module("JAVA5", t, 4, "JVM Internals & Memory Management",
            List.of("JVM memory areas (heap, stack, metaspace)", "Garbage collection algorithms", "Class loading", "Reading a stack trace & basic JVM tuning"),
            List.of(
                free("JVM architecture overview", "Baeldung", "https://www.baeldung.com/jvm-architecture", "guide"),
                free("Garbage Collection tuning guide", "Oracle docs", "https://docs.oracle.com/en/java/javase/21/gctuning/", "docs"),
                free("Class loading in Java", "Baeldung", "https://www.baeldung.com/java-classloaders", "guide"),
                paid("JVM Internals for Java developers", "Udemy", "https://www.udemy.com/courses/search/?q=jvm+internals", "search")),
            List.of("Draw the JVM memory layout from memory", "Compare G1, Parallel and ZGC at a high level", "Explain a ClassNotFoundException vs NoClassDefFoundError"),
            List.of(
                q("java5q1", "What are the main JVM runtime memory areas, and what lives in each?",
                    "heap: all objects and arrays live here, shared across threads, divided into young (eden + survivor) and old generation",
                    "stack: one per thread, holds method call frames — local variables and partial results; a runaway recursion causes a StackOverflowError here",
                    "metaspace (replaced PermGen since Java 8): class metadata, native memory rather than heap",
                    "an OutOfMemoryError can come from any of these being exhausted, and the specific message tells you which"),
                q("java5q2", "At a high level, how does a generational garbage collector (like G1) decide what to collect, and why generational?",
                    "the 'weak generational hypothesis' observes most objects die young — so the young generation is collected frequently and cheaply (minor GC), promoting survivors to the old generation",
                    "the old generation is collected less often since most long-lived objects genuinely stay alive, making full/mixed collections rarer and more expensive",
                    "G1 further divides the heap into regions and prioritizes collecting the regions with the most garbage first ('garbage first')"),
                q("java5q3", "What's the difference between ClassNotFoundException and NoClassDefFoundError?",
                    "ClassNotFoundException: a checked exception thrown when code explicitly tries to load a class by name (e.g., Class.forName) and it isn't found on the classpath",
                    "NoClassDefFoundError: an Error thrown when a class WAS available at compile time but is missing at runtime when the JVM tries to link/use it — often a packaging/classpath mismatch between build and deploy"),
                q("java5q4", "What's the difference between a memory leak in Java and one in a language like C, given Java has garbage collection?",
                    "Java can still leak memory — not by forgetting to free it, but by keeping unintended references alive (e.g., a growing static collection, unclosed listeners, ThreadLocal not cleared) so the GC can never reclaim those objects",
                    "the fix is finding and breaking the unwanted reference chain (often via a heap dump analysis tool), not manual memory management"))));

        add(new Module("JAVA6", t, 5, "Modern Java (8-21) Features",
            List.of("Streams & lambdas", "Optional", "Records & sealed classes", "Pattern matching & virtual threads"),
            List.of(
                free("Java Stream API tutorial", "Baeldung", "https://www.baeldung.com/java-8-streams", "guide"),
                free("Java Records", "Baeldung", "https://www.baeldung.com/java-record-keyword", "guide"),
                free("Virtual Threads (JEP 444)", "Oracle docs", "https://openjdk.org/jeps/444", "docs"),
                paid("Java 17-21 new features masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=java+17+21+new+features", "search")),
            List.of("Write a non-trivial Stream pipeline", "Explain why Optional isn't meant for fields/parameters", "Records vs classes, and what virtual threads change"),
            List.of(
                q("java6q1", "What's the difference between an intermediate and a terminal Stream operation, and why does that matter for performance?",
                    "intermediate operations (map, filter, sorted) are LAZY — they just build up a pipeline description and don't run anything yet",
                    "a terminal operation (collect, forEach, reduce) triggers actual evaluation of the whole pipeline, element by element",
                    "laziness lets the JVM avoid unnecessary work, e.g., short-circuiting with findFirst() without processing the whole source"),
                q("java6q2", "Why is Optional generally discouraged as a field type or method parameter?",
                    "Optional was designed specifically as a RETURN type to signal 'this method may not have a result', to be checked by the caller instead of returning null",
                    "using it as a field adds serialization/boilerplate overhead with no real benefit over just allowing the field to be null (and documenting that)",
                    "using it as a parameter forces every caller to wrap a value in Optional.of just to call the method — overloading or a sensible default is usually cleaner"),
                q("java6q3", "What problem do Java records solve, and what do you get 'for free' by declaring one?",
                    "records eliminate the boilerplate of a plain immutable data-carrier class",
                    "declaring record Point(int x, int y) {} automatically generates a canonical constructor, private final fields, accessor methods (x(), y()), and correct equals/hashCode/toString based on the components",
                    "records are implicitly final and can't extend another class (though they can implement interfaces), reinforcing that they're meant to model simple immutable data"),
                q("java6q4", "What are virtual threads (Java 21), and what kind of workload benefits most from them?",
                    "virtual threads are lightweight threads managed by the JVM rather than mapped 1:1 to an OS thread — you can create millions of them cheaply",
                    "they benefit I/O-bound workloads most: a virtual thread that blocks on I/O (e.g., a DB call, an HTTP call) is 'unmounted' from its carrier OS thread, freeing that OS thread to run other virtual threads, instead of the OS thread sitting idle",
                    "they don't speed up CPU-bound work — for that, the number of CPU cores is still the real limit"))));
    }

    // ------------------------------------------------------------------ Spring Framework track (standalone)
    private void buildSpring() {
        String t = "spring";
        topics.put(t, new Topic(t, "Spring Framework Engineer",
                "The Spring Framework itself — IoC, AOP, MVC, and data access — independent of Spring Boot's auto-configuration layer."));

        add(new Module("SPR1", t, 0, "IoC Container & Dependency Injection",
            List.of("Inversion of Control & the ApplicationContext", "Constructor vs setter vs field injection", "Bean scopes", "Java config vs XML vs component scanning"),
            List.of(
                free("Spring IoC container", "Spring", "https://docs.spring.io/spring-framework/reference/core/beans/introduction.html", "docs"),
                free("Spring dependency injection", "Baeldung", "https://www.baeldung.com/spring-dependency-injection", "guide"),
                free("Spring bean scopes", "Baeldung", "https://www.baeldung.com/spring-bean-scopes", "guide"),
                paid("Spring Framework 6 masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+framework+6", "search")),
            List.of("Explain IoC vs DI precisely", "Justify constructor injection as the default", "Name all standard bean scopes and when each applies"),
            List.of(
                q("spr1q1", "What's the difference between Inversion of Control and Dependency Injection?",
                    "IoC is the broader principle: control over object creation/wiring is inverted from the code itself to a container/framework",
                    "DI is ONE specific technique for achieving IoC — the container supplies (injects) a component's dependencies rather than the component looking them up or creating them itself",
                    "other IoC techniques exist (e.g., service locator) but DI is what Spring is built around"),
                q("spr1q2", "Why does Spring documentation recommend constructor injection as the default choice?",
                    "it makes required dependencies explicit and allows fields to be declared final (immutable after construction)",
                    "it fails fast at application startup if a required dependency is missing, rather than surfacing a NullPointerException later at runtime",
                    "it makes unit testing trivial — you just call the constructor with mocks, no reflection or Spring context needed",
                    "field injection, by contrast, hides dependencies, allows constructing an incomplete object, and requires a DI framework (or reflection) even in tests"),
                q("spr1q3", "List Spring's standard bean scopes and when you'd use each.",
                    "singleton (default): one instance per Spring container — the right choice for stateless, shared services",
                    "prototype: a new instance every time the bean is requested — for stateful or non-thread-safe objects",
                    "request/session/application: web-aware scopes tied to an HTTP request, session, or ServletContext respectively — used for web-tier beans that must not be shared across users"),
                q("spr1q4", "What's the difference between @Component, @Service, @Repository and @Controller, given they all register a bean the same way?",
                    "functionally, all four are stereotypes of @Component and register a bean identically via component scanning",
                    "the more specific ones add semantic meaning for readability AND extra framework behavior — @Repository enables Spring's exception translation (wrapping JDBC/JPA exceptions into Spring's DataAccessException hierarchy)",
                    "using the right stereotype documents the layer a class belongs to, which also helps AOP pointcuts that target a layer by annotation"))));

        add(new Module("SPR2", t, 1, "AOP & Cross-Cutting Concerns",
            List.of("Aspect-oriented programming concepts", "Advice types (before/after/around)", "Proxy-based AOP (JDK dynamic vs CGLIB)", "@Transactional under the hood"),
            List.of(
                free("Spring AOP", "Spring", "https://docs.spring.io/spring-framework/reference/core/aop.html", "docs"),
                free("Introduction to Spring AOP", "Baeldung", "https://www.baeldung.com/spring-aop", "guide"),
                free("Spring @Transactional internals", "Baeldung", "https://www.baeldung.com/transaction-configuration-with-jpa-and-spring", "guide"),
                paid("Spring AOP deep dive", "Udemy", "https://www.udemy.com/courses/search/?q=spring+aop", "search")),
            List.of("Explain a cross-cutting concern with an example", "JDK dynamic proxy vs CGLIB proxy", "Why calling a @Transactional method from within the same class doesn't work"),
            List.of(
                q("spr2q1", "What problem does AOP solve, and what's a concrete example of a cross-cutting concern in a typical Spring app?",
                    "cross-cutting concerns are behaviors needed across many unrelated classes (logging, security checks, transaction management, caching) that would otherwise be duplicated in every method",
                    "AOP lets you define that behavior ONCE as an aspect and apply it declaratively wherever a pointcut expression matches, keeping business logic classes focused on their actual responsibility",
                    "example: @Transactional itself is implemented as AOP advice wrapping a method call in a transaction"),
                q("spr2q2", "How does Spring implement AOP under the hood — JDK dynamic proxies vs CGLIB?",
                    "Spring AOP is proxy-based: it wraps your bean in a proxy object that intercepts method calls",
                    "JDK dynamic proxies are used when the target class implements at least one interface — the proxy implements the same interface(s)",
                    "CGLIB proxies (subclassing) are used when there's no interface — the proxy subclasses the target class at runtime, which is why AOP doesn't work on final classes/methods"),
                q("spr2q3", "Why does calling a @Transactional-annotated method from another method in the SAME class not actually start a transaction?",
                    "Spring AOP proxies work by intercepting calls made TO the proxy from outside — an internal self-invocation (this.method()) bypasses the proxy entirely and calls the real method directly",
                    "fix by injecting a self-reference/using AopContext.currentProxy(), or better, moving the @Transactional method into a separate collaborating bean that gets called through its own proxy"),
                q("spr2q4", "What's the difference between @Before, @AfterReturning, @AfterThrowing, @After and @Around advice?",
                    "@Before runs before the method; @AfterReturning runs after a successful return; @AfterThrowing runs only if an exception propagates out; @After runs regardless (like finally)",
                    "@Around is the most powerful: it wraps the whole invocation, receives a ProceedingJoinPoint, and can choose whether/when to call proceed(), inspect/modify the return value, or short-circuit the call entirely"))));

        add(new Module("SPR3", t, 2, "Spring MVC & Web Layer",
            List.of("DispatcherServlet request lifecycle", "@RequestMapping family & content negotiation", "Bean Validation in controllers", "Global exception handling"),
            List.of(
                free("Spring Web MVC", "Spring", "https://docs.spring.io/spring-framework/reference/web/webmvc.html", "docs"),
                free("Spring MVC request lifecycle", "Baeldung", "https://www.baeldung.com/spring-mvc-handlermapping-handleradapter", "guide"),
                free("Spring exception handling for REST", "Baeldung", "https://www.baeldung.com/exception-handling-for-rest-with-spring", "guide"),
                paid("Spring MVC masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+mvc", "search")),
            List.of("Trace a request from DispatcherServlet to the response", "Explain @Valid + BindingResult", "@ControllerAdvice vs per-controller @ExceptionHandler"),
            List.of(
                q("spr3q1", "Walk through what happens to an HTTP request from the moment it hits DispatcherServlet.",
                    "DispatcherServlet (the front controller) receives the request and asks a HandlerMapping which controller method should handle it",
                    "a HandlerAdapter invokes that controller method, resolving method arguments (path variables, request body, etc.) along the way",
                    "the controller returns a value; if it's a ResponseEntity or an @ResponseBody-annotated result, an HttpMessageConverter serializes it (e.g., to JSON) directly to the response",
                    "if it's a view name instead, a ViewResolver locates and renders the corresponding view"),
                q("spr3q2", "How do you validate an incoming request body, and what happens if validation fails?",
                    "annotate the parameter with @Valid (or @Validated) and the DTO fields with Bean Validation annotations (@NotNull, @Size, @Email, etc.)",
                    "by default, a failed validation throws MethodArgumentNotValidException, which Spring Boot turns into a 400 response automatically",
                    "alternatively, add a BindingResult parameter right after the @Valid argument to inspect errors yourself instead of letting the exception propagate"),
                q("spr3q3", "@ControllerAdvice with @ExceptionHandler vs a try/catch in each controller method — why prefer the former?",
                    "@ControllerAdvice centralizes exception-to-response mapping in ONE place, applied globally across all (or a targeted subset of) controllers",
                    "it keeps controller methods focused on the happy path instead of repeating the same error-formatting logic everywhere",
                    "it produces consistent error response shapes across the whole API, which API consumers can rely on"),
                q("spr3q4", "What's the difference between @RequestParam, @PathVariable and @RequestBody?",
                    "@RequestParam binds a query string parameter (?name=value) or form field",
                    "@PathVariable binds a segment of the URI path itself (e.g., /users/{id})",
                    "@RequestBody deserializes the entire HTTP request body (typically JSON) into a Java object using an HttpMessageConverter"))));

        add(new Module("SPR4", t, 3, "Data Access & Transactions",
            List.of("JdbcTemplate vs ORM options", "Spring's transaction abstraction", "Transaction propagation & isolation", "Exception translation"),
            List.of(
                free("Spring Data Access", "Spring", "https://docs.spring.io/spring-framework/reference/data-access.html", "docs"),
                free("Spring transaction management", "Spring", "https://docs.spring.io/spring-framework/reference/data-access/transaction.html", "docs"),
                free("Spring's DataAccessException hierarchy", "Baeldung", "https://www.baeldung.com/spring-dataexception", "guide"),
                paid("Spring Data Access masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+data+access", "search")),
            List.of("Explain declarative vs programmatic transactions", "Propagation levels beyond REQUIRED", "Why Spring wraps JDBC exceptions"),
            List.of(
                q("spr4q1", "What's the difference between declarative and programmatic transaction management in Spring, and which does Spring generally recommend?",
                    "declarative: annotate a method with @Transactional and let an AOP proxy start/commit/rollback the transaction around it — no transaction code in your business logic",
                    "programmatic: manually use TransactionTemplate or PlatformTransactionManager to control transaction boundaries in code",
                    "Spring recommends declarative for the vast majority of cases — it's less error-prone and keeps transaction concerns out of business logic; programmatic is reserved for cases needing fine-grained control within a single method"),
                q("spr4q2", "Explain REQUIRED, REQUIRES_NEW, and NESTED propagation, and how they differ in rollback behavior.",
                    "REQUIRED (default): joins the caller's existing transaction if one exists, else starts a new one — a rollback anywhere in that shared transaction rolls back everything",
                    "REQUIRES_NEW: always suspends any existing transaction and starts a fully independent new one — the outer transaction's rollback does NOT undo what the inner one already committed",
                    "NESTED: uses a savepoint within the SAME physical transaction — a rollback in the nested part can be contained (rolled back to the savepoint) without necessarily rolling back the whole outer transaction, but it's still tied to the outer transaction's ultimate commit/rollback"),
                q("spr4q3", "What is Spring's DataAccessException hierarchy, and why does it matter that it's unchecked?",
                    "Spring translates low-level, technology-specific exceptions (SQLException, Hibernate exceptions, etc.) into a consistent, technology-agnostic DataAccessException hierarchy",
                    "being unchecked (a RuntimeException subtype) means callers aren't forced to catch/declare it everywhere, and the abstraction lets you swap the underlying persistence technology without changing every catch clause up the call stack",
                    "this translation is what @Repository's stereotype behavior enables via a BeanPostProcessor"),
                q("spr4q4", "When would you reach for JdbcTemplate instead of an ORM like Hibernate/Spring Data JPA?",
                    "when you need tight control over the exact SQL executed (complex reporting queries, bulk operations, performance-critical paths where ORM-generated SQL is suboptimal)",
                    "JdbcTemplate removes JDBC boilerplate (connection/statement/resultset handling, resource closing) while still letting you write raw SQL",
                    "for typical CRUD-heavy domain-object persistence, an ORM/Spring Data JPA is usually more productive; JdbcTemplate is the pragmatic escape hatch"))));

        add(new Module("SPR5", t, 4, "Spring Testing",
            List.of("Spring TestContext framework", "@ContextConfiguration & context caching", "Test slices (@WebMvcTest, @DataJpaTest)", "Mocking beans in a Spring test"),
            List.of(
                free("Testing the Spring MVC", "Spring", "https://docs.spring.io/spring-framework/reference/testing.html", "docs"),
                free("Spring Boot Test annotations guide", "Baeldung", "https://www.baeldung.com/spring-tests", "guide"),
                free("Mockito with Spring", "Baeldung", "https://www.baeldung.com/mockito-spring", "guide"),
                paid("Spring Testing masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+testing", "search")),
            List.of("Explain Spring's test context caching", "MockMvc vs a real running server for a controller test", "@MockBean vs a manually constructed mock"),
            List.of(
                q("spr5q1", "Why does Spring's TestContextFramework cache the ApplicationContext between test classes, and what can accidentally break that caching?",
                    "starting a full Spring context is expensive; caching it (keyed by its configuration) lets many test classes share the SAME context instead of rebuilding it per class, dramatically speeding up a test suite",
                    "anything that changes the effective configuration key breaks the cache and forces a rebuild — e.g., different @ActiveProfiles, different @MockBean sets, or different context configuration classes between test classes",
                    "minimizing unique context configurations across a test suite is a real, practical performance technique"),
                q("spr5q2", "What does MockMvc give you that a full @SpringBootTest with a real embedded server doesn't, for testing a controller?",
                    "MockMvc simulates HTTP requests/responses against the DispatcherServlet WITHOUT starting an actual network server/port — faster, and still exercises the real MVC request-handling pipeline (argument resolution, validation, exception handling, serialization)",
                    "a full embedded-server test (@SpringBootTest(webEnvironment=RANDOM_PORT) + a real HTTP client) is slower but verifies actual network behavior end-to-end, useful for a smaller number of true integration tests"),
                q("spr5q3", "What's the difference between a test slice like @WebMvcTest and the full @SpringBootTest?",
                    "@WebMvcTest loads only the web layer (controllers, filters, MVC infra) and auto-configures MockMvc, leaving service/repository beans unloaded — you supply mocks (e.g., @MockBean) for them",
                    "@SpringBootTest loads the entire application context, which is thorough but much slower — appropriate for true end-to-end integration tests, not for testing one controller in isolation")
            )));
    }

    // ------------------------------------------------------------------ Spring Boot track (standalone)
    private void buildSpringBoot() {
        String t = "springboot";
        topics.put(t, new Topic(t, "Spring Boot Engineer",
                "Spring Boot specifically — auto-configuration, starters, Spring Data JPA, security, actuator, and cloud-native microservices."));

        add(new Module("SB1", t, 0, "Auto-Configuration & Starters",
            List.of("@SpringBootApplication under the hood", "How auto-configuration is triggered & ordered", "Starter dependencies", "Externalized configuration & profiles"),
            List.of(
                free("Spring Boot auto-configuration", "Spring", "https://docs.spring.io/spring-boot/reference/using/auto-configuration.html", "docs"),
                free("Custom auto-configuration guide", "Baeldung", "https://www.baeldung.com/spring-boot-custom-auto-configuration", "guide"),
                free("Externalized Configuration", "Spring", "https://docs.spring.io/spring-boot/reference/features/external-config.html", "docs"),
                paid("Spring Boot masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+boot", "search")),
            List.of("Explain what @SpringBootApplication actually composes", "How @ConditionalOnClass/@ConditionalOnMissingBean drive auto-config", "Property override precedence across profiles/env vars/args"),
            List.of(
                q("sb1q1", "What three annotations does @SpringBootApplication compose, and what does each contribute?",
                    "@SpringBootConfiguration: marks the class as a source of bean definitions (a specialization of @Configuration)",
                    "@EnableAutoConfiguration: triggers Spring Boot's auto-configuration mechanism, which conditionally registers beans based on the classpath and existing configuration",
                    "@ComponentScan: scans the current package (and sub-packages) for @Component-annotated classes to register as beans"),
                q("sb1q2", "How does a single Spring Boot 'starter' dependency end up auto-configuring an entire subsystem, like spring-boot-starter-data-jpa?",
                    "the starter is really just a curated set of transitive dependencies (Hibernate, Spring Data JPA, a JDBC driver, etc.) with compatible versions — it doesn't itself contain framework code",
                    "adding those jars to the classpath is what auto-configuration classes react to via @ConditionalOnClass — e.g., seeing Hibernate + a DataSource on the classpath triggers JPA-related beans to auto-configure",
                    "you can always override any auto-configured bean by defining your own bean of that type — @ConditionalOnMissingBean means your explicit bean wins"),
                q("sb1q3", "What's the property override precedence in Spring Boot when the same property is set in application.yml, an environment variable, and a command-line argument?",
                    "command-line arguments win over almost everything else",
                    "environment variables and JVM system properties come next, ahead of the packaged application.properties/yml",
                    "profile-specific files (application-{profile}.yml) override the base application.yml for properties they redefine",
                    "understanding this order matters for debugging 'why isn't my config taking effect' issues in different environments"),
                q("sb1q4", "How would you disable a specific auto-configuration class you don't want (e.g., the default DataSource auto-config)?",
                    "use @SpringBootApplication(exclude = DataSourceAutoConfiguration.class) or the spring.autoconfigure.exclude property",
                    "this is common when you want to configure that concern manually, or when a starter on the classpath brings auto-configuration you don't actually want active"))));

        add(new Module("SB2", t, 1, "Spring Data JPA in Boot",
            List.of("Repository interfaces & derived queries", "N+1 problem & fetch strategies", "@Transactional boundaries in a service layer", "Schema migration with Flyway/Liquibase"),
            List.of(
                free("Spring Data JPA reference", "Spring", "https://docs.spring.io/spring-data/jpa/reference/", "docs"),
                free("Hibernate N+1 problem", "Baeldung", "https://www.baeldung.com/hibernate-N-plus-1-problem-different-fetching-strategies", "guide"),
                free("Flyway with Spring Boot", "Baeldung", "https://www.baeldung.com/database-migrations-with-flyway", "guide"),
                paid("Spring Data JPA masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+data+jpa", "search")),
            List.of("Derive a query method from its name", "Diagnose and fix an N+1 query", "Explain why migrations belong in version control"),
            List.of(
                q("sb2q1", "How does Spring Data JPA generate a working query just from a method name like findByLastNameAndActiveTrue?",
                    "Spring Data parses the method name against a defined grammar (By, And, Or, property names, keywords like GreaterThan/OrderBy) and builds the equivalent JPQL query at startup, without you writing any SQL/JPQL",
                    "for anything the naming convention can't express cleanly, you fall back to @Query with JPQL or native SQL"),
                q("sb2q2", "What is the N+1 query problem in a Spring Data JPA repository, concretely, and how do you fix it?",
                    "calling findAll() on an entity that has a lazy @OneToMany, then accessing that collection for each of the N results triggers 1 query for the list plus N additional queries — one per entity",
                    "fix with a JOIN FETCH in a custom @Query, an @EntityGraph on the repository method, or batch fetching (hibernate.default_batch_fetch_size) to collapse the N extra queries into far fewer"),
                q("sb2q3", "Where should @Transactional boundaries typically live in a Spring Boot app — the controller, service, or repository layer — and why?",
                    "the service layer — it represents a meaningful unit of business work that may span multiple repository calls that must succeed or fail together",
                    "controllers shouldn't hold transactions open across HTTP-layer concerns (like serialization); repositories are too fine-grained and would create a separate transaction per data-access call, defeating the purpose"),
                q("sb2q4", "Why use a migration tool like Flyway or Liquibase instead of letting Hibernate auto-generate/update the schema (ddl-auto=update)?",
                    "ddl-auto=update is convenient for local dev but unsafe in production — it can silently make destructive or unexpected changes, and there's no audit trail or rollback",
                    "Flyway/Liquibase store versioned, ordered migration scripts that are applied deterministically and consistently across every environment, forming a reviewable history of every schema change",
                    "the standard practice is ddl-auto=validate (or none) in production, with real migrations owning schema evolution"))));

        add(new Module("SB3", t, 2, "REST APIs, Validation & Error Handling",
            List.of("Designing REST resources & status codes", "Bean Validation on request DTOs", "Centralized exception handling", "API documentation with OpenAPI/Swagger"),
            List.of(
                free("Building a RESTful Web Service", "Spring guides", "https://spring.io/guides/gs/rest-service/", "guide"),
                free("Spring Boot exception handling", "Baeldung", "https://www.baeldung.com/exception-handling-for-rest-with-spring", "guide"),
                free("springdoc-openapi", "springdoc.org", "https://springdoc.org/", "docs"),
                paid("REST APIs with Spring Boot masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=rest+api+spring+boot", "search")),
            List.of("Design clean, RESTful resource URIs and status codes", "Wire up @ControllerAdvice for consistent errors", "Explain RFC 7807 Problem Details"),
            List.of(
                q("sb3q1", "What makes a REST API 'RESTful' rather than just an HTTP API — name a few concrete practices.",
                    "resource-oriented URIs (nouns, not verbs: /orders/{id}, not /getOrder)",
                    "correct use of HTTP methods (GET for reads, POST to create, PUT/PATCH to update, DELETE to remove) and status codes (201 Created with a Location header, 404, 409, etc.)",
                    "statelessness — each request contains everything needed to process it, no server-side session state between requests",
                    "HATEOAS (hypermedia links) is the more purist/advanced criterion, though many production APIs skip it pragmatically"),
                q("sb3q2", "How do you return a consistent, structured error body across every endpoint in a Spring Boot API?",
                    "a single @RestControllerAdvice class with @ExceptionHandler methods per exception type (or exception hierarchy), each returning a consistent error DTO (status, message, timestamp, maybe a field-level validation error list)",
                    "Spring Boot 3+ also supports RFC 7807 'Problem Details' (ProblemDetail) out of the box as a standardized error response format"),
                q("sb3q3", "How do you validate a request DTO's fields and return a useful 400 response listing exactly what's wrong?",
                    "annotate the DTO fields with Bean Validation (@NotBlank, @Size, @Min, @Email, etc.) and the controller parameter with @Valid",
                    "catch MethodArgumentNotValidException in an @ExceptionHandler and map its BindingResult/FieldErrors into a response listing each invalid field and its specific message, rather than a single generic 400"),
                q("sb3q4", "What does OpenAPI/Swagger give you in a Spring Boot project, and how is it typically generated?",
                    "a machine-readable specification of your API's endpoints, request/response shapes, and status codes — enabling interactive docs (Swagger UI) and client-code generation",
                    "springdoc-openapi generates it automatically from your controllers/DTOs and annotations at runtime, rather than you hand-writing a YAML/JSON spec"))));

        add(new Module("SB4", t, 3, "Spring Security, JWT & OAuth2",
            List.of("SecurityFilterChain configuration", "Stateless JWT authentication", "OAuth2 login & resource server", "Method-level security"),
            List.of(
                free("Spring Security reference", "Spring", "https://docs.spring.io/spring-security/reference/", "docs"),
                free("Spring Security JWT tutorial", "Baeldung", "https://www.baeldung.com/spring-security-oauth-jwt", "guide"),
                free("OAuth2 Resource Server", "Spring", "https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html", "docs"),
                paid("Spring Security masterclass", "Udemy", "https://www.udemy.com/courses/search/?q=spring+security+oauth2+jwt", "search")),
            List.of("Configure a SecurityFilterChain from scratch", "Design a stateless JWT auth flow end to end", "Explain the OAuth2 authorization code flow"),
            List.of(
                q("sb4q1", "How do you configure Spring Security in a modern (Spring Boot 3+) app, and how did that change from older versions?",
                    "modern Spring Security uses a SecurityFilterChain @Bean built with a lambda DSL (http.authorizeHttpRequests(...).csrf(...).oauth2ResourceServer(...) etc.), instead of extending WebSecurityConfigurerAdapter (deprecated/removed)",
                    "this component-based approach also makes it straightforward to define multiple, independently-ordered filter chains for different URL patterns (e.g., stateless API vs a stateful admin UI)"),
                q("sb4q2", "Design a stateless JWT authentication flow for a Spring Boot API end to end.",
                    "client sends credentials to a login endpoint; the server verifies them and issues a signed JWT (with claims like subject, roles, expiry) instead of creating a server-side session",
                    "the client sends that JWT in the Authorization: Bearer header on every subsequent request",
                    "a custom filter (added to the SecurityFilterChain before the standard auth filter) validates the token's signature and expiry on each request and populates the SecurityContext — no session lookup needed, which is what makes it horizontally scalable",
                    "token revocation before expiry is the classic weak point — mitigated with short expiries + refresh tokens, or a server-side denylist for the rare case"),
                q("sb4q3", "In the OAuth2 authorization code flow, what problem does the extra 'code exchange' step solve versus just returning the access token directly to the browser?",
                    "the authorization code is short-lived and passed through the browser (less sensitive if intercepted), while the actual access token exchange happens server-to-server (client backend <-> authorization server) using a client secret",
                    "this keeps the long-lived, powerful access token out of the browser's history/referrer headers/JS-accessible storage, reducing the attack surface versus the older, now-discouraged implicit flow"),
                q("sb4q4", "How do you secure individual service methods (not just URLs) in Spring, and why would you want to?",
                    "@EnableMethodSecurity plus annotations like @PreAuthorize(\"hasRole('ADMIN')\") or @PostAuthorize on service methods",
                    "URL-level security alone can't express fine-grained rules like 'a user can only edit their OWN order' — method security can evaluate the actual arguments/return value (e.g., @PreAuthorize(\"#order.ownerId == authentication.name\"))"))));

        add(new Module("SB5", t, 4, "Actuator, Observability & Testing",
            List.of("Actuator endpoints (health, metrics, info)", "Custom health indicators & metrics", "Structured logging & tracing", "@SpringBootTest & Testcontainers"),
            List.of(
                free("Spring Boot Actuator", "Spring", "https://docs.spring.io/spring-boot/reference/actuator/index.html", "docs"),
                free("Micrometer metrics", "Micrometer", "https://micrometer.io/docs", "docs"),
                free("Testcontainers for Java", "Testcontainers", "https://testcontainers.com/guides/getting-started-with-testcontainers-for-java/", "guide"),
                paid("Spring Boot Actuator & observability", "Udemy", "https://www.udemy.com/courses/search/?q=spring+boot+actuator", "search")),
            List.of("Explain what /actuator/health aggregates and how", "Design a custom HealthIndicator", "Why Testcontainers over H2 for a real integration test"),
            List.of(
                q("sb5q1", "What does the /actuator/health endpoint actually report, and how is its overall status computed?",
                    "it aggregates the status of every registered HealthIndicator (DB connectivity, disk space, message broker connectivity, custom ones you add) into one overall status",
                    "the overall status is the WORST of all individual indicator statuses (e.g., if the DB indicator is DOWN, the whole endpoint reports DOWN) — this makes it suitable for load balancer/orchestrator health checks",
                    "showDetails/authorization controls how much of that breakdown is exposed publicly vs only to authenticated/internal callers"),
                q("sb5q2", "How would you add a custom health check — say, verifying a downstream payment gateway is reachable?",
                    "implement the HealthIndicator interface (or HealthContributor) and register it as a bean; Spring Boot Actuator auto-discovers it and folds its result into /actuator/health",
                    "the health() method returns Health.up()/.down().withDetail(...) — keep it fast and non-blocking-forever (with a timeout), since a hung health check can itself cause cascading readiness-probe failures"),
                q("sb5q3", "What's the difference between a metric exposed via Micrometer and a log line, and why do you generally want both?",
                    "metrics are numeric, aggregatable time-series data (request count, latency percentiles, error rate) suited for dashboards/alerting on trends",
                    "logs are discrete, detailed event records suited for diagnosing a SPECIFIC failure after an alert fires",
                    "Micrometer gives Spring Boot a vendor-neutral metrics facade (like SLF4J does for logging) that can export to Prometheus, Datadog, etc. without changing application code"),
                q("sb5q4", "Why reach for Testcontainers rather than an embedded H2 database when writing a Spring Boot integration test against a Postgres-backed service?",
                    "H2 doesn't perfectly replicate Postgres's SQL dialect, functions, and constraint-enforcement behavior — a test passing against H2 can still fail against real Postgres in production",
                    "Testcontainers spins up the actual Postgres engine in a throwaway Docker container for the test run, so the integration test verifies behavior against the real database technology",
                    "the trade-off is a slower test (container startup) versus much higher confidence — typically reserved for a smaller set of true integration tests, not every test"))));

        add(new Module("SB6", t, 5, "Microservices, Messaging & Cloud-Native",
            List.of("Service discovery & API gateway", "Resilience patterns (circuit breaker, retry, bulkhead)", "Event-driven communication with Kafka", "Containerizing & deploying a Spring Boot service"),
            List.of(
                free("Spring Cloud reference", "Spring", "https://spring.io/projects/spring-cloud", "docs"),
                free("Resilience4j guide", "Resilience4j", "https://resilience4j.readme.io/docs/getting-started", "docs"),
                free("Spring for Apache Kafka", "Spring", "https://docs.spring.io/spring-kafka/reference/", "docs"),
                paid("Microservices with Spring Boot & Spring Cloud", "Udemy", "https://www.udemy.com/courses/search/?q=microservices+spring+cloud", "search")),
            List.of("Design a circuit breaker + fallback for a flaky dependency", "Explain the Saga pattern for a distributed 'transaction'", "Trace a request through an API gateway to a downstream service"),
            List.of(
                q("sb6q1", "What are the three states of a Resilience4j circuit breaker, and what triggers each transition?",
                    "CLOSED: normal operation, requests flow through and failures are counted against a rolling window",
                    "OPEN: once the failure rate crosses a configured threshold, the breaker trips — requests fail FAST (immediately, no call to the downstream service) for a wait duration, protecting the failing dependency from more load and freeing up the caller's resources",
                    "HALF_OPEN: after the wait duration, a limited number of trial requests are allowed through — if they succeed, the breaker closes again; if they fail, it reopens"),
                q("sb6q2", "What problem does the Saga pattern solve for microservices, and how does it work?",
                    "a single business operation that spans multiple services (e.g., place an order: reserve inventory + charge payment + schedule shipping) can't use a traditional single-database ACID transaction across service boundaries",
                    "a saga breaks it into a sequence of local transactions, each committing in its own service and publishing an event/triggering the next step",
                    "if a later step fails, previously completed steps are undone via explicit COMPENSATING actions (e.g., release the inventory reservation) rather than a real rollback — this is eventual consistency, not atomicity"),
                q("sb6q3", "Synchronous REST calls vs Kafka-based async messaging between two Spring Boot microservices — what's the real trade-off?",
                    "sync REST: simpler mental model, immediate response, but tightly couples the caller's availability/latency to the callee's — if the callee is down or slow, the caller is directly affected",
                    "Kafka: decouples producer and consumer completely (the producer doesn't even need the consumer to be running), naturally buffers bursts of load, and enables multiple independent consumers of the same event",
                    "the cost is added complexity: eventual consistency, harder request tracing, and needing to reason about message ordering/idempotency/at-least-once delivery"),
                q("sb6q4", "What's the role of an API gateway in a microservices architecture, and name two concerns it commonly centralizes.",
                    "an API gateway is the single entry point clients call, which routes each request to the correct downstream microservice — clients don't need to know the internal service topology",
                    "it commonly centralizes cross-cutting concerns like authentication/authorization, rate limiting, request logging, and response aggregation from multiple services — so individual microservices don't each reimplement them"))));
    }
}
