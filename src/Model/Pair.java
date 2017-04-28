package Model;

/**Basic Pair class implementation. Used in selection operator when handling fitness and index of layouts.
 * @author JoshMerritt
 *
 * @param <X>
 * @param <Y>
 * 
 * 
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
