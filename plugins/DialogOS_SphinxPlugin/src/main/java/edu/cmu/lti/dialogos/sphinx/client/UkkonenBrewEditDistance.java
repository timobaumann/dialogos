package edu.cmu.lti.dialogos.sphinx.client;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class UkkonenBrewEditDistance{

  static enum Move{
    INITIAL, DELETE, INSERT, SUBSTITUTE, MATCH, INVALID;
	public int editCost() { return (this == MATCH)? 0 : 1; }
  }

  static class EditResults{
    private int totalCost;
    private List<Move> editPath = new LinkedList<Move>();
    public int getTotalCost() {
      return totalCost;
    }
    public List<Move> getEditPath() {
      return editPath;
    }
  
    public void setTotalCost(int totalCost) {
      this.totalCost = totalCost;
    }  
  }
 
  static class TraceBack {
		static final TraceBack MAX_COST = new TraceBack(Integer.MAX_VALUE, Move.INVALID, (TraceBack) null);
    int cost;
    Move edit;
    TraceBack prevTraceBack;
    public TraceBack(int cost, Move edit, TraceBack nextTraceBack) {
      this.cost = cost;
      this.edit = edit;
      this.prevTraceBack = nextTraceBack;
    }
  }
 
  static EditResults constructResultFromTraceBack(TraceBack traceBack){
    EditResults ret =new EditResults();
    ret.setTotalCost(traceBack.cost);
    TraceBack t = traceBack;
    while(t != null && t.edit != null && t.edit != Move.INITIAL){
      ret.editPath.add(0, t.edit);
      t = t.prevTraceBack;
    }
   	ret.editPath.add(0, Move.INITIAL);
    return ret;
  }

    public static TraceBack wordLevelBrewEditDistance(String fromString, String toString) {
      String[] from = fromString.split(" ");
      String[] to   =   toString.split(" ");
      return brewEditDistance(from, to);
    }

    public static TraceBack characterLevelBrewEditDistance(String fromString, String toString) {
      Object[] from = fromString.chars().mapToObj(i->(char) i).toArray();
      Object[] to   =   toString.chars().mapToObj(i->(char) i).toArray();
      return brewEditDistance(from, to);
  }
  
  public static TraceBack brewEditDistance(Object[] from, Object[] to) {
		TraceBack result;
		int maxDistance = 8;
		do {
            maxDistance *= 2;
			result = brewEditDistance(from, to, maxDistance);
//            System.err.println("now trying with " + maxDistance);
		} while (result.cost >= maxDistance);
		return result;
  }
 
  public static TraceBack brewEditDistance(Object[] from, Object[] to, int maxDistance) {
    if (from == null || to == null) {
        throw new IllegalArgumentException("Strings must not be null");
    }
    //Apache common-langs's implementation always transform from t to s , which is very counter intuitive.
    //In their case it doesnt matter because it doesnt track edits and all edit costs are the same
    //but why the heck would anyone in the right mind want to call editDistance(s, t) and have it compute as
    //transform t to s? here I just substitute them back to what they are supposed to be meant
    int n = to.length;
    int m = from.length;

    if (n == 0) { //to is empty, so we are doing all deletes
      return constructTraceBack(from, Move.DELETE);
    } else if (m == 0) {//from is empty, so we are doing all inserts
      return constructTraceBack(to, Move.INSERT);
    }

    //(see original apache common-lang getLevensteinDistance())
    //we cannot do swap strings memory optimization any more because insert/delete cost can be different
    //swapping the strings will tamper the final edit cost. Swapping the strings will also screw up the edit path
    //however, in many applications your should have 2 similar length strings, otherwise
    //you can skip edit distance and just consider 2 strings that varies greatly in length
    //to be |s1.length - s2.length| which should be a good enough approximation
  
    TraceBack p[] = new TraceBack[n+1]; //'previous' cost array, horizontally
    TraceBack c[] = new TraceBack[n+1]; // cost array, horizontally

    // indexes into strings toString and fromString
    int j; // iterates through toString
    int i; // iterates through fromString

    Object from_i; // jth character of fromString
    Object to_j; // ith character of toString

    p[0] = new TraceBack(0, Move.INITIAL, null);
    for (j = 0; j<Math.min(n,maxDistance); j++) {
      TraceBack prev = p[j];
			p[j+1] = new TraceBack(prev.cost+Move.INSERT.editCost(), Move.INSERT, prev);
    }
    int prevBestRow = 0;
    //Arrays.asList(p).stream().forEach(item -> System.err.printf("%3s", item == null ? "- " : item.cost + " "));
    //System.err.println("best row: " + prevBestRow);
    for (i = 1; i<=m; i++) {
        from_i = from[i-1];
        int low = Math.max(1, prevBestRow - maxDistance);
        if (low == 1) { // during startup
				c[0] = new TraceBack(p[0].cost+Move.DELETE.editCost(), Move.DELETE, TraceBack.MAX_COST);
        } else { // later in the game
				c[low-1] = new TraceBack(maxDistance, Move.INVALID, TraceBack.MAX_COST);
        }
        int high= Math.min(n, prevBestRow + maxDistance);
        int bestCost = Integer.MAX_VALUE;
        int bestRow = 0;
        for (j=low; j<=high; j++) {
          to_j = to[j-1];
          Move move = to_j.equals(from_i) ? Move.MATCH:Move.SUBSTITUTE;
          TraceBack trace = p[j-1];
          int cost = trace != null ? trace.cost + move.editCost() : maxDistance;
				if (p[j] != null && p[j].cost + Move.DELETE.editCost() < cost) {
        	  move = Move.DELETE;
        	  trace = p[j];
					cost = p[j].cost + Move.DELETE.editCost();
          }
				if (c[j-1].cost + Move.INSERT.editCost() < cost) {
        	  move = Move.INSERT;
        	  trace = c[j-1];
					cost = c[j-1].cost + Move.INSERT.editCost();
          }
          if (cost < bestCost) {
        	  bestCost = cost;
        	  bestRow = j;
          }
          c[j] = new TraceBack(cost, move, trace);
        }
			if (bestCost >= maxDistance) 
				return TraceBack.MAX_COST;
        prevBestRow = bestRow;
        //Arrays.asList(c).stream().forEach(item -> System.err.printf("%3s", item == null ? "- " : item.cost + " "));
        //System.err.println("best row: " + bestRow);
        // TODO: need to correctly set prevBestRow
        TraceBack[] _c = p; //placeholder to assist in swapping p and d
        p = c;
        c = _c;
    }

    // our last action in the above loop was to switch d and p, so p now
    // actually has the most recent cost counts
		return p[n] != null ? p[n] : TraceBack.MAX_COST;
  }
 
  private static TraceBack constructTraceBack(Object[] s, Move move){
    TraceBack trace = new TraceBack(0, Move.INITIAL, null);
    for(int i =0;i<s.length;i++){
      trace = new TraceBack(move.editCost(), move, trace);
    }
    return trace;
  }


  @SuppressWarnings("unused")
public static void main(String[] args) {
	  //Thread.sleep(10000);
		int N = 10;
		int length = 160000;
		int offset = 100000;
	/*	String a = FileUtil.ReadAllText(new File("/tmp/a.nonew"));
		//a = a.substring(offset, offset+length);
		String b = FileUtil.ReadAllText(new File("/tmp/b.nonew"));
		//b = b.substring(offset, offset+length);  
/**/
		String a = "VerySamefromString";
		String b = "VerySametoString";
		long start = System.currentTimeMillis();
		int distance = 0;
		for (int i = 0; i<N; i++) {
			System.err.print(".");
			//distance = StringUtils.getLevenshteinDistance(a, b);
		}
		System.err.println();
		System.out.println(((System.currentTimeMillis() - start)/1000.0) + "s; N=" + N);
		System.out.println("distance is " + distance);
		System.out.flush();
		start = System.currentTimeMillis();
		TraceBack tb = null;
		for (int i = 0; i<N; i++){
			System.err.print(".");
			tb = UkkonenBrewEditDistance.characterLevelBrewEditDistance(a, b);
		}
		System.err.println();
		System.out.println(((System.currentTimeMillis() - start)/1000.0) + "s; N=" + N);
		System.err.println(tb);
        EditResults res = constructResultFromTraceBack(tb);
        System.out.println(res.getEditPath());
        System.out.println("distance is " + res.totalCost);
		/* some numbers: for StringUtils.getLevenshteinDistance:
		 * 160k   -> 83s, 69s
		 * 80000 -> 27s, 20s
		 * 40000 -> 5.7s
		 * 20000 -> 1.4s // double size, 3-4 times the execution time
		 * 10000 -> 0.5s
		 */
		/* some numbers: for brewEditDistance:
		 * 160k-> 710s
		 * 80k ->  20s 
		 * 40k -> 148s --> down to 21 when I completely inline the cost calculation, only keep edit type (get rid of edit object) and inline best() so that it does not create too many objects
		 * 20k ->  30s // double size, 5 times the execution time
		 * 10k ->   6s
		 */
  }

}

