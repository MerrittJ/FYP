package Model;

/**
 * @author JoshMerritt
 *
 * @param <X>
 * @param <Y>
 * 
 * Basic Pair class implementation. Used in selection operator when handling fitness and index of layouts.
 */
public class Pair<X, Y> {
	public X x;
	public Y y;

	public Pair(){
	}
	
	public Pair(X x, Y y){
		this.x = x;
		this.y = y;
	}
}
