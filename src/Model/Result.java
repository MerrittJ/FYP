package Model;

/**
 * @author JoshMerritt
 *
 * @param <X>
 * @param <Y>
 * @param <Z>
 * 
 * Triplet class implementation. Used to store experiment results to be written to a results Excel file. Results come in the following form:
 * Individual index, individual fitness, individual size
 */
public class Result<X, Y, Z> {
	public X x;
	public Y y;
	public Z z;

	public Result(){
	}
	
	public Result(X x, Y y, Z z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
}

