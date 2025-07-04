package inne;

/*
 * ta klasa zazwyczj używana kiedy z metody chcemy też zwrócić informację czy coś się udało zrobić
 */
public class Bool<T>{
	public T f;
	public boolean bool;
	public Bool(T f, boolean p){
		this.f = f;
		this.bool = p;
	}
}
