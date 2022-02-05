package scanner;

import java.util.HashMap;
import java.util.Stack;

/**
 * This is the file you will modify.
 */
public class Scanner {

  //------------------------------------------------------------
  // TODO: declare the HashMaps that you will use to store
  // your tables. Also declare the start state.
  //------------------------------------------------------------

  private HashMap<Character, String> categoryMap;
  private HashMap<String, HashMap<String, String>> transitionMap;
  private HashMap<String, String> tokenTypeMap;
  // Either the start state, or the current state. Not sure.
  private String state;
  private Stack<String> stack;

  //------------------------------------------------------------
  // TODO: build your tables in the constructor and implement
  // the get methods.
  //------------------------------------------------------------

  /**
   * Builds the tables needed for the scanner.
   */
  public Scanner(TableReader tableReader) {
    // TODO: starting with the skeleton code below, build the
    // classifer, transition and token type tables. You will need
    // to also implement the test functions below once you have your
    // tables built.

    // Build catMap, mapping a character to a category.
    categoryMap = new HashMap<>();
    for (TableReader.CharCat cat : tableReader.getClassifier()) {
      System.out.println("Character " + cat.getC() + " is of category "
              + cat.getCategory());
      categoryMap.put(cat.getC(), cat.getCategory()); 
    }

    // Build the transition table. Given a state and a character category,
    // give a new state.
    transitionMap = new HashMap<>();
    // HashMap<String, String> innerMap = new HashMap<>(); // put new HashMap on the heap
    for (TableReader.Transition t : tableReader.getTransitions()) {
      System.out.println(t.getFromStateName() + " -- " + t.getCategory()
              + " --> " + t.getToStateName());
      // innerMap.put(t.getCategory(), t.getToStateName()); // put the to state in dict with key cat
      // if the key is not already in there
      if(!transitionMap.containsKey(t.getFromStateName())){
        transitionMap.put(t.getFromStateName(), new HashMap<String, String>());
      }
      transitionMap.get(t.getFromStateName()).put(t.getCategory(), t.getToStateName()); // Put the dict in transition table with key from state
    }

    // Build the token types table
    tokenTypeMap = new HashMap<>();
    for (TableReader.TokenType tt : tableReader.getTokens()) {
      System.out.println("State " + tt.getState()
              + " accepts with the lexeme being of type " + tt.getType());
      tokenTypeMap.put(tt.getState(), tt.getType());
    }

    this.stack = new Stack<>();
  }

  /**
   * Returns the category for c or "not in alphabet" if c has no category. Do not hardcode
   * this. That is, this function should have nothing more than a table lookup
   * or two. You should not have any character literals in here such as 'r' or '3'.
   */
  public String getCategory(Character c) {
    if(this.categoryMap.containsKey(c)){
      return this.categoryMap.get(c);
    }
    return "not in alphabet";
  }

  /**
   * Returns the new state given a current state and category. This models
   * the transition table. Returns "error" if there is no transition.
   * Do not hardcode any state names or categories. You should have only
   * table lookups here.
   */
  public String getNewState(String state, String category) {
    if(this.transitionMap.containsKey(state)){
      if(this.transitionMap.get(state).containsKey(category)){
        return this.transitionMap.get(state).get(category);
      }
    }
    return "error";
  }

  /**
   * Returns the type of token corresponding to a given state. If the state
   * is not accepting then return "error".
   * Do not hardcode any state names or token types.
   */
  public String getTokenType(String state) {
    if(this.tokenTypeMap.containsKey(state)){
      return this.tokenTypeMap.get(state);
    }
    return "error";
  }

  //------------------------------------------------------------
  // TODO: implement nextToken
  //------------------------------------------------------------

  /**
   * Return the next token or null if there's a lexical error.
   */
  public Token nextToken(ScanStream ss) {
    // TODO: get a single token. This is an implementation of the nextToken
    // algorithm given in class. You may *not* use TableReader in this
    // function. Return null if there is a lexical error.
    this.state = "s0";
    String lexeme = "";
    if(!this.stack.empty()){
      this.stack.clear();
    }
    this.stack.push("bad");
    while(state != "error"){
      if(ss.eof()){
        return null;
      }
      char c = ss.next();
      lexeme = lexeme + c;
      // If the state is accepting 
      if(this.getTokenType(state) != "error"){
        if(!this.stack.empty()){
          this.stack.clear();
        }
      }
      this.stack.push(this.state);
      String category = this.getCategory(c);
      this.state = this.getNewState(state, category);
    }
    while(this.getTokenType(state) == "error" && this.state != "bad"){
      if(!this.stack.empty()){
        state = this.stack.pop();
      }
      if(lexeme.length() > 0){
        lexeme = lexeme.substring(0, lexeme.length() - 1); // truncate
      }      
      ss.rollback();
    } 
    if(this.getTokenType(state) != "error"){
      return new Token(this.tokenTypeMap.get(state), lexeme);
    }
    return null;
  }

}
