package Model;

public class Result<X, Y, Z, A> {
	public X x;
	public Y y;
	public Z z;
	public A a;

	public Result(){
	}
	
	public Result(X x, Y y, Z z, A a){
		this.x = x;
		this.y = y;
		this.z = z;
		this.a = a;
	}
}

