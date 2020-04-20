package compilador;

public class TabladeSimbolos {
	String nombre;
	String valor;
	String tipo;
	String alcance;
	int renglon;
	public String toString() {
		return "Identificador [Nombre = " + nombre + ", Valor = " + valor + ", Tipo = " + tipo + ", Alcance = "+alcance+" , Posición = "+renglon+" ]";
	}
	public TabladeSimbolos(String nombre, String valor, String tipo, String alcance, int renglon) {
		super();
		this.nombre = nombre;
		this.valor = valor;
		this.tipo = tipo;
		this.alcance=alcance;
		this.renglon=renglon;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public String getAlcance() {
		return alcance;
	}
	public void setAlcance(String alcance) {
		this.alcance = alcance;
	}
	public int getRenglon() {
		return renglon;
	}
	public void setRenglon(int renglon) {
		this.renglon = renglon;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}	
}
