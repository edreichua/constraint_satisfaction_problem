package csp_solver;

import java.util.*;

import javafx.util.Pair;

/**
 * Simple CSP solver
 *
 */
public class ConstraintSatisfactionProblem {
    // boolean flag to set heuristic
    private static final boolean MRV = true;
    private static final boolean LCV = false;

    // if both MAC3 and FC is chosen, MAC3 has priority over FC
    private static final boolean MAC3 = false;
    private static final boolean FC = true;

    private int nodesExplored;
    private int constraintsChecked;
    private Map<Integer,Set<Integer>> D;
    private Map<Integer,Map<Integer, Set<Pair<Integer, Integer>>>> C;
    private Set<Pair<Integer,Integer>> arc;
    private Map<Integer,Set<Integer>> graph;

    public ConstraintSatisfactionProblem(){
        resetStats();
        D = new HashMap<>();
        C = new HashMap<>();
        arc = new HashSet<>();
        graph = new HashMap<>();
    }
    /**
     * Solve for the CSP problem
     * @return the mapping from variables to values
     */
    public Map<Integer, Integer> solve() {
        resetStats();
        long before = System.currentTimeMillis();
        if (!enforceConsistency()) {
            return null;
        }
        Map<Integer, Integer> solution;

        solution = backtracking(new HashMap<>());

        double duration = (System.currentTimeMillis() - before) / 1000.0;
        printStats();
        System.out.println(String.format("Search time is %.2f second", duration));
        return solution;
    }
    
    private void resetStats() {
        nodesExplored = 0;
        constraintsChecked = 0;
    }
    
    private void incrementNodeCount() {
        ++nodesExplored;
    }
    
    private void incrementConstraintCheck() {
        ++constraintsChecked;
    }
    
    public int getNodeCount() {
        return nodesExplored;
    }
    
    public int getConstraintCheck() {
        return constraintsChecked;
    }
    
    protected void printStats() {
        System.out.println("Nodes explored during last search:  " + nodesExplored);
        System.out.println("Constraints checked during last search " + constraintsChecked);
    }

    /**
     * Add a variable with its domain
     * @param id      the identifier of the variable
     * @param domain  the domain of the variable
     */
    public void addVariable(Integer id, Set<Integer> domain) {
        D.put(id, new HashSet<>(domain));
    }
    
    /**
     * Add a binary constraint
     * @param id1         the identifier of the first variable
     * @param id2         the identifier of the second variable
     * @param constraint  the constraint
     */
    public void addConstraint(Integer id1, Integer id2, Set<Pair<Integer, Integer>> constraint) {

        // add constraints
        if(!C.containsKey(id1)) {
            C.put(id1, new HashMap<>());
        }

        C.get(id1).put(id2,new HashSet<>(constraint));

        // add arcs
        arc.add(new Pair<>(id1, id2));
        arc.add(new Pair<>(id2, id1));

        // add graph
        if(!graph.containsKey(id1)){
            graph.put(id1,new HashSet<>());
        }
        graph.get(id1).add(id2);

    }
    
    /**
     * Enforce consistency by AC-3, PC-3.
     */
    private boolean enforceConsistency() {

        Queue<Pair<Integer,Integer>> q = new LinkedList<>();

        // add all arcs to the queue
        q.addAll(arc);

        return AC3(q,false,null);
    }

    /**
     * AC3 inference heuristic
     * @param q         the queue for AC3
     * @param infer     boolean flag to determine if inference is used
     * @param removed   store the removed values
     * @return          true if it is arc consistent, and false if otherwise
     */
    private boolean AC3(Queue<Pair<Integer,Integer>> q, boolean infer, Map<Integer, Set<Integer>> removed){

        while(!q.isEmpty()){

            Pair<Integer,Integer> p = q.remove();
            incrementConstraintCheck();

            if(revise(p.getKey(),p.getValue(),infer,removed)){
                if(D.get(p.getKey()).isEmpty()) {
                    return false;
                }
                if(graph.containsKey(p.getKey())) {
                    for (int i : graph.get(p.getKey())){
                        if (i != p.getValue()) {
                            q.add(new Pair<>(i, p.getKey()));
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Revise - Helper function for AC3
     * @param id1       identifier of first variable
     * @param id2       identifier of second variable
     * @param infer     boolean flag to determine if inference is used
     * @param removed   true if revision is made, false otherwise
     * @return
     */
    private boolean revise(Integer id1, Integer id2, boolean infer, Map<Integer, Set<Integer>> removed) {

        boolean revised = false;
        Set<Integer> toremove = new HashSet<>();

        for(int x: D.get(id1)){

            boolean flag = true;

            for(int y : D.get(id2)){
                Pair<Integer,Integer> p = new Pair<>(x,y);

                // there exist a (x,y) that satisfied the constraint
                if((C.containsKey(id1) && C.get(id1).containsKey(id2) && C.get(id1).get(id2).contains(p)) ||
                        (C.containsKey(id2) && C.get(id2).containsKey(id1) && C.get(id2).get(id1).contains(p))){
                    flag = false;
                    break;
                }
            }

            if(flag){ // no value y allow (x,y) to satisfy the constraint
                toremove.add(x);
                revised = true;
            }
        }

        // remove all entries
        for (int i : toremove) {
            D.get(id1).remove(i);
        }

        if(infer && revised){
            if(!removed.containsKey(id1)) {
                removed.put(id1, toremove);
            }else {
                removed.get(id1).addAll(toremove);
            }
        }

        return revised;
    }

    /**
     * Forward Checking
     * @param var
     * @param value
     * @param partialSolution
     * @param removed
     * @return
     */
    private boolean FC(Integer var, Integer value, Map<Integer, Integer> partialSolution, Map<Integer, Set<Integer>> removed){
        for(int v: graph.get(var)){
            if(!partialSolution.containsKey(v)){
                for(int u: new HashSet<>(D.get(v))){
                    Pair<Integer,Integer> p = new Pair<>(value,u);
                    if(!C.get(var).get(v).contains(p)){
                        if(!removed.containsKey(v)) {
                            removed.put(v,new HashSet<>());
                        }
                        removed.get(v).add(u);
                        D.get(v).remove(u);
                    }
                }
                if(D.get(v).isEmpty())
                    return false;
            }
        }
        return true;
    }

    /**
     * Backtracking algorithm
     * @param partialSolution  a partial solution
     * @return a solution if found, null otherwise.
     */
    private Map<Integer, Integer> backtracking(Map<Integer, Integer> partialSolution) {

        incrementNodeCount();
        if(partialSolution.size() == D.size()){
            return partialSolution;
        }

        int var = selectUnassignedVariable(partialSolution);

        for(int value: orderDomainValues(var, partialSolution)){
            Map<Integer,Set<Integer>> removed = new HashMap<>();

            if(isConsistent(var,value,partialSolution) && !partialSolution.containsKey(var)){
                partialSolution.put(var, value);

                if(inference(var,value,partialSolution,removed)){
                    Map<Integer,Integer> result = backtracking(partialSolution);

                    if(result != null){
                        return result;
                    }
                }
            }

            for(Map.Entry<Integer,Set<Integer>> e: removed.entrySet()){
                D.get(e.getKey()).addAll(e.getValue());
            }

            partialSolution.remove(var);
        }
        return null;
    }

    /**
     * isConsistent - helper function to check if a value is consistent
     * @param var               the identifier of the variable
     * @param value             the value to be checked
     * @param partialSolution   the solution so far
     * @return                  true if solution is consistent, false otherwise
     */
    private boolean isConsistent(Integer var, Integer value, Map<Integer, Integer> partialSolution){
        if(graph.get(var) != null) {
            for (int i : graph.get(var)) {
                incrementConstraintCheck();
                if (partialSolution.containsKey(i)) {

                    Pair<Integer, Integer> p = new Pair<>(value, partialSolution.get(i));

                    if (C.containsKey(var) && C.get(var).containsKey(i) && !C.get(var).get(i).contains(p))
                        return false;

                }
            }
        }

        return true;
    }

    
    /**
     * Inference for backtracking
     * Implement FC and MAC3
     * @param var              the new assigned variable
     * @param value            the new assigned value
     * @param partialSolution  the partialSolution
     * @param removed          the values removed from other variables' domains
     * @return true if the partial solution may lead to a solution, false otherwise.
     */
    private boolean inference(Integer var, Integer value, Map<Integer, Integer> partialSolution, Map<Integer, Set<Integer>> removed) {

        // store all the removed values in a hash map
        if(!removed.containsKey(var)) {
            removed.put(var, new HashSet<>(D.get(var)));
        }else{
            removed.get(var).addAll(new HashSet<>(D.get(var)));
        }

        // remove all values for var in domain, except for the guess
        D.get(var).clear();
        D.get(var).add(value);

        if(!MAC3 && !FC){ // short circuit if inference is not used
            return true;
        }else if(MAC3) {  // MAC 3
            // Add arcs originating from var to queue
            Queue<Pair<Integer, Integer>> q = new LinkedList<>();
            if (graph.get(var) != null) {
                for (int i : graph.get(var)) {
                    q.add(new Pair<>(i, var));
                }
            }
            return AC3(q, true, removed);
        }else{ // FC
            return FC(var,value,partialSolution,removed);
        }
    }
 
    /**
     * Look-ahead value ordering
     * Pick the least constraining value (min-conflicts)
     * @param var              the variable to be assigned
     * @param partialSolution  the partial solution
     * @return an order of values in var's domain
     */
    private Iterable<Integer> orderDomainValues(Integer var, Map<Integer, Integer> partialSolution) {

        if(!LCV) { // return the unordered domain if LCV heuristic is not used
            return new HashSet<>(D.get(var));
        }

        Integer[][] result = new Integer[D.get(var).size()][2];
        int r = 0;

        for(int num: D.get(var)){
            int count = 0;

            // check with immediate neighbours
            if(graph.get(var) != null) {
                for (int i : graph.get(var)) {
                    if (D.get(i).contains(num))
                        count++;
                }
            }
            result[r][0] = num;
            result[r][1] = count;
            r++;
        }

        // the greater the count, the more conflicts there are. Sort in ascending order
        Arrays.sort(result, Comparator.comparing((Integer[] arr) -> arr[1]));
        List<Integer> res = new ArrayList<>();

        for(int i=0; i<result.length; i++){
            res.add(result[i][0]);
        }

        return res;
    }

    /**
     * Dynamic variable ordering
     * Pick the variable with the minimum remaining values or the variable with the max degree.
     * Or pick the variable with the minimum ratio of remaining values to degree.
     * @param partialSolution  the partial solution
     * @return one unassigned variable
     */
    private Integer selectUnassignedVariable(Map<Integer, Integer> partialSolution) {
        int minnum, minsize;
        minnum = minsize = Integer.MAX_VALUE;

        for(int i: D.keySet()){
            if(!partialSolution.containsKey(i)){
                if(!MRV) // return the first non-conflict value if MRV heuristic is not used
                    return i;
                if(D.get(i).size() < minsize){
                    minnum = i;
                    minsize = D.get(i).size();
                }
            }
        }
        return minnum;
    }
    
    /**
     * Backjumping
     * Conflict-directed-backjumping
     * @param partialSolution
     */
    private void jumpBack(Map<Integer, Integer> partialSolution) {

    }

}
