package com.studio.code;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One live-coding problem per DSA module. Each harness below was hand-verified outside the app —
 * compiled and run with a correct reference implementation (all tests PASS) and with a deliberately
 * wrong one (tests correctly FAIL) — before being embedded here.
 */
@Component
public class CodeProblemCatalog {

    private final Map<String, CodeProblem> byModule = new LinkedHashMap<>();

    public CodeProblemCatalog() {
        add(new CodeProblem("two-sum", "DSA1", "Two Sum",
            "Given an array of integers nums and an integer target, return the indices of the two numbers that add up to target. Assume exactly one solution exists.",
            """
            public int[] twoSum(int[] nums, int target) {
                // Write your solution here

            }""",
            """
            import java.util.*;

            public class Solution {
                {{USER_METHOD}}

                public static void main(String[] args) {
                    Solution sol = new Solution();
                    int pass = 0, total = 0;
                    total++; { int[] r = sol.twoSum(new int[]{2,7,11,15}, 9); boolean ok = Arrays.equals(r, new int[]{0,1}); if(ok) pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=[0, 1] actual="+Arrays.toString(r)); }
                    total++; { int[] r = sol.twoSum(new int[]{3,2,4}, 6); boolean ok = Arrays.equals(r, new int[]{1,2}); if(ok) pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=[1, 2] actual="+Arrays.toString(r)); }
                    total++; { int[] r = sol.twoSum(new int[]{3,3}, 6); boolean ok = Arrays.equals(r, new int[]{0,1}); if(ok) pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=[0, 1] actual="+Arrays.toString(r)); }
                    System.out.println("RESULT: "+pass+"/"+total);
                }
            }
            """));

        add(new CodeProblem("longest-substr", "DSA2", "Longest Substring Without Repeating Characters",
            "Given a string s, return the length of the longest substring without repeating characters.",
            """
            public int lengthOfLongestSubstring(String s) {
                // Write your solution here

            }""",
            """
            import java.util.*;

            public class Solution {
                {{USER_METHOD}}

                public static void main(String[] args) {
                    Solution sol = new Solution();
                    int pass=0,total=0;
                    total++; { int r=sol.lengthOfLongestSubstring("abcabcbb"); boolean ok=r==3; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=3 actual="+r); }
                    total++; { int r=sol.lengthOfLongestSubstring("bbbbb"); boolean ok=r==1; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=1 actual="+r); }
                    total++; { int r=sol.lengthOfLongestSubstring("pwwkew"); boolean ok=r==3; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=3 actual="+r); }
                    total++; { int r=sol.lengthOfLongestSubstring(""); boolean ok=r==0; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=0 actual="+r); }
                    System.out.println("RESULT: "+pass+"/"+total);
                }
            }
            """));

        add(new CodeProblem("valid-parens", "DSA3", "Valid Parentheses",
            "Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid (every bracket is closed by the same type, in the correct order).",
            """
            public boolean isValid(String s) {
                // Write your solution here

            }""",
            """
            import java.util.*;

            public class Solution {
                {{USER_METHOD}}

                public static void main(String[] args) {
                    Solution sol = new Solution();
                    int pass=0,total=0;
                    total++; { boolean r=sol.isValid("()"); boolean ok=r==true; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=true actual="+r); }
                    total++; { boolean r=sol.isValid("()[]{}"); boolean ok=r==true; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=true actual="+r); }
                    total++; { boolean r=sol.isValid("(]"); boolean ok=r==false; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=false actual="+r); }
                    total++; { boolean r=sol.isValid("([)]"); boolean ok=r==false; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=false actual="+r); }
                    total++; { boolean r=sol.isValid("{[]}"); boolean ok=r==true; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=true actual="+r); }
                    System.out.println("RESULT: "+pass+"/"+total);
                }
            }
            """));

        add(new CodeProblem("valid-bst", "DSA4", "Validate Binary Search Tree",
            "Given the root of a binary tree, determine if it is a valid binary search tree (BST): every node's left subtree contains only values less than the node, and the right subtree only values greater.",
            """
            public boolean isValidBST(TreeNode root) {
                // Write your solution here

            }""",
            """
            import java.util.*;

            class TreeNode {
                int val; TreeNode left; TreeNode right;
                TreeNode(int val) { this.val = val; }
            }

            public class Solution {
                {{USER_METHOD}}

                // Builds a tree from a level-order array with nulls (LeetCode style) — used by the test harness only.
                static TreeNode build(Integer[] vals) {
                    if (vals.length == 0 || vals[0] == null) return null;
                    TreeNode root = new TreeNode(vals[0]);
                    Deque<TreeNode> q = new ArrayDeque<>();
                    q.add(root);
                    int i = 1;
                    while (!q.isEmpty() && i < vals.length) {
                        TreeNode cur = q.poll();
                        if (i < vals.length) { Integer lv = vals[i++]; if (lv != null) { cur.left = new TreeNode(lv); q.add(cur.left); } }
                        if (i < vals.length) { Integer rv = vals[i++]; if (rv != null) { cur.right = new TreeNode(rv); q.add(cur.right); } }
                    }
                    return root;
                }

                public static void main(String[] args) {
                    Solution sol = new Solution();
                    int pass=0,total=0;
                    total++; { boolean r=sol.isValidBST(build(new Integer[]{2,1,3})); boolean ok=r==true; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=true actual="+r); }
                    total++; { boolean r=sol.isValidBST(build(new Integer[]{5,1,4,null,null,3,6})); boolean ok=r==false; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=false actual="+r); }
                    total++; { boolean r=sol.isValidBST(build(new Integer[]{1})); boolean ok=r==true; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=true actual="+r); }
                    total++; { boolean r=sol.isValidBST(build(new Integer[]{5,4,6,null,null,3,7})); boolean ok=r==false; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=false actual="+r); }
                    System.out.println("RESULT: "+pass+"/"+total);
                }
            }
            """));

        add(new CodeProblem("connected-components", "DSA5", "Number of Connected Components",
            "Given n nodes labeled 0..n-1 and a list of undirected edges, return the number of connected components in the graph.",
            """
            public int countComponents(int n, int[][] edges) {
                // Write your solution here

            }""",
            """
            import java.util.*;

            public class Solution {
                {{USER_METHOD}}

                public static void main(String[] args) {
                    Solution sol = new Solution();
                    int pass=0,total=0;
                    total++; { int r=sol.countComponents(5, new int[][]{{0,1},{1,2},{3,4}}); boolean ok=r==2; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=2 actual="+r); }
                    total++; { int r=sol.countComponents(5, new int[][]{{0,1},{1,2},{2,3},{3,4}}); boolean ok=r==1; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=1 actual="+r); }
                    total++; { int r=sol.countComponents(4, new int[][]{}); boolean ok=r==4; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=4 actual="+r); }
                    total++; { int r=sol.countComponents(1, new int[][]{}); boolean ok=r==1; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=1 actual="+r); }
                    System.out.println("RESULT: "+pass+"/"+total);
                }
            }
            """));

        add(new CodeProblem("climbing-stairs", "DSA6", "Climbing Stairs",
            "You are climbing a staircase with n steps. Each time you can climb 1 or 2 steps. In how many distinct ways can you reach the top?",
            """
            public int climbStairs(int n) {
                // Write your solution here

            }""",
            """
            public class Solution {
                {{USER_METHOD}}

                public static void main(String[] args) {
                    Solution sol = new Solution();
                    int pass=0,total=0;
                    total++; { int r=sol.climbStairs(2); boolean ok=r==2; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=2 actual="+r); }
                    total++; { int r=sol.climbStairs(3); boolean ok=r==3; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=3 actual="+r); }
                    total++; { int r=sol.climbStairs(5); boolean ok=r==8; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=8 actual="+r); }
                    total++; { int r=sol.climbStairs(1); boolean ok=r==1; if(ok)pass++; System.out.println("TEST "+total+": "+(ok?"PASS":"FAIL")+" expected=1 actual="+r); }
                    System.out.println("RESULT: "+pass+"/"+total);
                }
            }
            """));
    }

    private void add(CodeProblem p) { byModule.put(p.moduleId(), p); }

    public CodeProblem forModule(String moduleId) { return byModule.get(moduleId); }
}
