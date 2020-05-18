package compilador;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class Analisis
{
	int renglon=1;
	ArrayList<String> impresion; //para la salida
	ListaDoble<Token> tokens;
	final Token vacio=new Token("", 9,0);
	boolean bandera = true, banderaclase = false, banderaErroresSintacticos = false;
	ArrayList<TabladeSimbolos> tablasimbolos = new ArrayList<TabladeSimbolos>();
	ArrayList<Operaciones> opera = new ArrayList<Operaciones>();
	ArrayList<String> expresion = new ArrayList<String>();
	

	public ArrayList<TabladeSimbolos> getTabla() {
		return tablasimbolos ;
	}
	public ArrayList<Operaciones> getTabla2() {
		return opera ;
	}
	
	
	public Analisis(String ruta) {//Recibe el nombre del archivo de texto
		analisaCodigo(ruta);
		if(bandera) {
			impresion.add("¡No hay errores lexicos!");
			analisisSintactico(tokens.getInicio());
			AnalizadorSemantico(tokens.getInicio());
			Semantico2(tokens.getInicio());
		}
		if(impresion.get(impresion.size()-1).equals("¡No hay errores lexicos!"))
			impresion.add("¡No hay errores sintacticos!");
		if(impresion.get(impresion.size()-1).equals("¡No hay errores sintacticos!"))
			impresion.add("¡No hay errores semanticos!");
			//impresion.add();
		
		for (int i = 0; i < tablasimbolos.size(); i++) {
			System.out.println(tablasimbolos.get(i).toString());
		}
		System.out.println();
			
	}
	public void analisaCodigo(String ruta) {
		String linea="", token="";
		StringTokenizer tokenizer;
		try{
	          FileReader file = new FileReader(ruta);
	          BufferedReader archivoEntrada = new BufferedReader(file);
	          linea = archivoEntrada.readLine();
	          impresion=new ArrayList<String>();
	          tokens = new ListaDoble<Token>();
	          while (linea != null){
	        	    linea = separaDelimitadores(linea);
	                tokenizer = new StringTokenizer(linea);
	                while(tokenizer.hasMoreTokens()) {
	                	token = tokenizer.nextToken();
	                	analisisLexico(token);
	                }
	                linea=archivoEntrada.readLine();
	                renglon++;
	          }
	          archivoEntrada.close();
		}catch(IOException e) {
			JOptionPane.showMessageDialog(null,"No se encontro el archivo favor de checar la ruta","Alerta",JOptionPane.ERROR_MESSAGE);
		}
	}
		
	public void analisisLexico(String token) {
		int tipo=0;
		//Se usan listas con los tipos de token
		// Esto se asemeja a un in en base de datos 
		//Ejemplo select * from Clientes where Edad in (18,17,21,44)
		if(Arrays.asList("public","static","private","protected").contains(token)) 
			tipo = Token.MODIFICADOR;
		else if(Arrays.asList("if","else").contains(token)) 
			tipo = Token.PALABRA_RESERVADA;
		else if(Arrays.asList("int","char","float","boolean").contains(token))
			tipo = Token.TIPO_DATO;
		else if(Arrays.asList("(",")","{","}","=",";").contains(token))
			tipo = Token.SIMBOLO;
		else if(Arrays.asList("<","<=",">",">=","==","!=").contains(token))
			tipo = Token.OPERADOR_LOGICO; 
		else if(Arrays.asList("+","-","*","/").contains(token))
			tipo = Token.OPERADOR_ARITMETICO;
		else if(Arrays.asList("True","False").contains(token)||Pattern.matches("^[0-9]+$",token)||Pattern.matches("[0-9]+.[0-9]+",token)||Pattern.matches("'[a-zA-Z]'",token)) 
			tipo = Token.CONSTANTE;
		else if(token.equals("class")) 
			tipo =Token.CLASE;
		else {
			//Cadenas validas
			Pattern pat = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*$")  ;//Expresiones Regulares
			Matcher mat = pat.matcher(token);
			//tipo = Token.CONSTANTE;
				
			if(mat.find()) 
				tipo = Token.IDENTIFICADOR;
			
	
			else {
				impresion.add("Error lexico en la pocisión "+renglon+" token "+token);
				bandera = false;
				return;
			}
		}
		tokens.insertar(new Token(token,tipo,renglon));
		impresion.add(new Token(token,tipo,renglon).toString());
	}
		
	public Token analisisSintactico(NodoDoble<Token> nodo) {
		Token  to;
		if(nodo!=null) // si no llego al ultimo de la lista
		{
			to =  nodo.dato;
		
			try{
			switch (to.getTipo()) // un switch para validar la estructura
			{
			case Token.MODIFICADOR:
				int sig=nodo.siguiente.dato.getTipo();
				// aqui se valida que sea 'public int' o 'public class' 
				if(sig!=Token.TIPO_DATO && sig!=Token.CLASE)// si lo que sigue 
					impresion.add("Error sintactico en la linea "+to.getLinea()+" se esperaba un tipo de dato correcto");
				break;
			case Token.IDENTIFICADOR:
				// lo que puede seguir despues de un identificador
				try{
					if((Arrays.asList("{","=",";","==",")").contains(nodo.siguiente.dato.getValor()))) 
						if(nodo.anterior.dato.getValor().equals("class")) // se encontro la declaracion de la clase
						{
							tablasimbolos.add( new TabladeSimbolos(to.getValor(), " ", "class"," ",to.getLinea()));
						}
				}catch (Exception e){
					impresion.add("Error sintactico en la posición: "+to.getLinea()+" Se esperaba un simbolo");
					System.out.println(e.getMessage());
				}
				
				break;
			// Estos dos entran en el mismo caso
			case Token.TIPO_DATO:
			case Token.CLASE:
			// si lo anterior fue modificador
				if (nodo.anterior!=null) 
					if(nodo.anterior.dato.getTipo()==Token.MODIFICADOR) {
						if(nodo.siguiente.dato.getTipo()!=Token.IDENTIFICADOR) 
							impresion.add("Error sintactico en la posición "+to.getLinea()+" se esperaba un identificador");
					}else
						impresion.add("Error sintactico en la posición "+to.getLinea()+" se esperaba un modificador");
				break;
				
			case Token.SIMBOLO:
				
			
				
				
				// Verificar que el mismo numero de parentesis y llaves que abren sean lo mismo que los que cierran
				if(to.getValor().equals("}")) 
				{
					if(cuenta("{")!=cuenta("}"))
						impresion.add("Error sintactico en la posición "+to.getLinea()+ " falta un {");
				}else if(to.getValor().equals("{")) {
					if(cuenta("{")!=cuenta("}"))
						impresion.add("Error sintactico en la posición "+to.getLinea()+ " falta un }");
				}
			
				else if(to.getValor().equals("(")) {
					if(cuenta("(")!=cuenta(")"))
						impresion.add("Error sintactico en la posición "+to.getLinea()+ " falta un )");
				}else if(to.getValor().equals(")")) {
					if(cuenta("(")!=cuenta(")"))
						impresion.add("Error sintactico en la posición "+to.getLinea()+ " falta un (");
				}
				// verificar la asignacion
				else if(to.getValor().equals("=")){
					
					
						if(nodo.anterior.dato.getTipo()==Token.IDENTIFICADOR) {	
							//if(nodo.siguiente.dato.getTipo()!=Token.CONSTANTE)
							if(nodo.siguiente.dato.getTipo()!=Token.CONSTANTE && 
									!nodo.siguiente.dato.getValor().contains("(") 
									&& nodo.siguiente.dato.getTipo()!=Token.IDENTIFICADOR){
								impresion.add("Error sintactico en la posición "+to.getLinea()+ " se esperaba una constante");
							}	
						}
					} 
					
				
				else if (to.getValor().equals(";"))
				{
					
					//int aux=0;

					
					boolean banderita=false;
					try
					{

						 if (nodo.anterior.anterior.dato.getTipo()==Token.TIPO_DATO && nodo.anterior.dato.getTipo()==Token.IDENTIFICADOR){
							tablasimbolos.add(new TabladeSimbolos(nodo.anterior.dato.getValor(),"",nodo.anterior.anterior.dato.getValor(),"Local",to.getLinea()));

						}
						 
						 ////////04: VALIDAR LAS VARIABLES YA DECLARADAS///////////////////
						 
						 else if (nodo.anterior.anterior.anterior.anterior.dato.getTipo()==Token.TIPO_DATO 
								 && nodo.anterior.anterior.anterior.dato.getTipo()==Token.IDENTIFICADOR 
								 && nodo.anterior.anterior.dato.getTipo()==Token.SIMBOLO
								 &&nodo.anterior.dato.getTipo()==Token.CONSTANTE){

								int x =0,auxRenglon=0;
								boolean bandera=false;
								for (int i = 0; i < tablasimbolos.size(); i++) {
									if (tablasimbolos.get(i).getNombre().contains(nodo.anterior.anterior.anterior.dato.getValor()) ){
										x++;
										auxRenglon=i;
									}
								}
								if(nodo.anterior.anterior.anterior.anterior.dato.getTipo()==Token.TIPO_DATO && x>0 && nodo.anterior.anterior.anterior.dato.getTipo()==Token.IDENTIFICADOR){
									impresion.add("Error semantico en la posición: "+to.getLinea()+ ". La variable:  "+nodo.anterior.anterior.anterior.dato.getValor()+"  ya habia sido declarada con tipo: "+tablasimbolos.get(auxRenglon).tipo+". Que se encuentra en la posición: "+tablasimbolos.get(auxRenglon).renglon);
									bandera=true;
								}
								
								if(!bandera)
								tablasimbolos.add(new TabladeSimbolos(nodo.anterior.anterior.anterior.dato.getValor(),nodo.anterior.dato.getValor(),nodo.anterior.anterior.anterior.anterior.dato.getValor(),"Local",to.getLinea()));
							}
						 /*
						 else if (nodo.anterior.anterior.anterior.anterior.anterior.dato.getTipo()==Token.IDENTIFICADOR
								 &&nodo.anterior.anterior.anterior.anterior.dato.getValor().contains("=") 
								 && nodo.anterior.anterior.anterior.dato.getTipo()==Token.CONSTANTE 
								 && nodo.anterior.anterior.dato.getTipo()==Token.OPERADOR_ARITMETICO 
								 && nodo.anterior.dato.getTipo()==Token.CONSTANTE){
							 
							 for (int i = 0; i < tablasimbolos.size(); i++) {
								if (tablasimbolos.get(i).getNombre().contains(nodo.anterior.anterior.anterior.anterior.anterior.dato.getValor())){
									aux=i;
								}
							}
							 tablasimbolos.get(aux).setValor(Sumar(nodo.anterior.anterior.anterior.dato.getValor(),nodo.anterior.dato.getValor())+"");
						 }
						 */
						 else if ((nodo.anterior.anterior.anterior.anterior.dato.getTipo()==Token.CONSTANTE
								 && nodo.anterior.anterior.anterior.dato.getTipo()==Token.OPERADOR_ARITMETICO
								 && nodo.anterior.anterior.dato.getTipo()==Token.CONSTANTE
									&& nodo.anterior.dato.getValor().contains(")"))
								 ||
								 (nodo.anterior.anterior.anterior.anterior.dato.getTipo()==Token.CONSTANTE
								 && nodo.anterior.anterior.anterior.dato.getTipo()==Token.SIMBOLO
								 && nodo.anterior.anterior.dato.getTipo()==Token.OPERADOR_ARITMETICO
									&& nodo.anterior.dato.getTipo()==Token.CONSTANTE)
								 ||
								 (nodo.anterior.anterior.anterior.dato.getTipo()==Token.CONSTANTE
								 && nodo.anterior.anterior.dato.getTipo()==Token.OPERADOR_ARITMETICO
									&& nodo.anterior.dato.getTipo()==Token.CONSTANTE)
								 ||
								 (nodo.anterior.anterior.anterior.dato.getTipo()==Token.IDENTIFICADOR 
								 && nodo.anterior.anterior.dato.getTipo()==Token.OPERADOR_ARITMETICO
									&& nodo.anterior.dato.getTipo()==Token.IDENTIFICADOR)	
								 ||
								 (nodo.anterior.anterior.anterior.dato.getTipo()==Token.IDENTIFICADOR 
								 && nodo.anterior.anterior.dato.getTipo()==Token.OPERADOR_ARITMETICO
									&& nodo.anterior.dato.getTipo()==Token.CONSTANTE)
								 ||
								 (nodo.anterior.anterior.anterior.dato.getTipo()==Token.CONSTANTE 
								 && nodo.anterior.anterior.dato.getTipo()==Token.OPERADOR_ARITMETICO
									&& nodo.anterior.dato.getTipo()==Token.IDENTIFICADOR)
								 
								 ){
						 
							 NodoDoble<Token> nodoaux = nodo;
								NodoDoble<Token> nodoaux2 = nodo;
								//NodoDoble<Token> nodoaux3 = nodo;
								while(nodoaux!=null){
									String aux2 = nodoaux.anterior.dato.getValor();
									System.out.println(aux2);
									if(aux2.contains("="))
										break;
									nodoaux = nodoaux.anterior;
								}
								while(nodoaux!=null){
									String aux2 = nodoaux.dato.getValor();
									System.out.println(aux2);
									if(aux2.contains(";"))
										break;
									expresion.add(aux2);
									nodoaux = nodoaux.siguiente;
								}
								for (int i = 0; i < expresion.size(); i++) {
									for (int j = 0; j < tablasimbolos.size(); j++) {
										if(tablasimbolos.get(j).getNombre().equals(expresion.get(i))){
											System.out.println(tablasimbolos.get(j).getNombre());
											System.out.println(expresion.get(i));
											expresion.set(i, tablasimbolos.get(j).getValor());
										}	
									}
								}
								ArrayList<String> expresion2 = new ArrayList<String>(expresion);

								for (int i = 0; i < expresion.size(); i++) {
									
									if(expresion.get(i).contains("("))
									expresion.set(i, "ParAbierto");
									
									else if(expresion.get(i).contains(")"))
										expresion.set(i, "ParCerrado");
									
									else if(expresion.get(i).contains("/"))
										expresion.set(i, "Divide");
									
									else if(expresion.get(i).contains("*"))
										expresion.set(i, "Multiplica");
									
									else if(expresion.get(i).contains("+"))
										expresion.set(i, "Suma");
									
									else if(expresion.get(i).equals("-"))
										expresion.set(i, "Resta");
									
								}
								int Resultadofinal=0;
								int contador =1;
								for (int i = 0; i < expresion.size(); i++) {
									
									try{
										if(Integer.parseInt(expresion.get(i)) <0){
											expresion2.set(i,"temp"+contador);
					
											opera.add(new Operaciones("-",expresion.get(i).substring(1)," " ,expresion2.get(i)));
											contador++;

										}	
									}catch (Exception e){
										e.getMessage();
									}
								if(expresion.get(i).contains("ParAbierto") ){
									if (expresion.get(i).contains("ParAbierto")){
										int aux5 = i;
										int aux6 = 0 ;
										boolean banderaParentesis = false;
										for (int j = 0; j < expresion.size(); j++) {
											if(expresion.get(j).contains("ParCerrado")){
											aux6 = j;
											break;
											}
										}
										while(!banderaParentesis){
											//(7 * 1 - 5)	
												for (int j = aux5; j < aux6; j++) {
													if(expresion.get(j).contains("Divide")){
														Resultadofinal =  dividir(expresion.get(j-1), expresion.get(j+1));
														expresion2.set(j,"temp"+contador);
														opera.add(new Operaciones("/",expresion2.get(j-1),expresion2.get(j+1),expresion2.get(j)));														
														expresion2.remove(j+1);
														expresion2.remove(j-1);
														expresion.set(j,Resultadofinal+"" );
														expresion.remove(j+1);
														expresion.remove(j-1);
														aux6 = aux6 - 2;
														contador++;
													}
													 if (expresion.get(j).contains("Multiplica")){
														Resultadofinal =  multiplicar(expresion.get(j-1), expresion.get(j+1));
														expresion2.set(j,"temp"+contador);
														opera.add(new Operaciones("*",expresion2.get(j-1),expresion2.get(j+1),expresion2.get(j)));
														expresion2.remove(j+1);
														expresion2.remove(j-1);
														expresion.set(j,Resultadofinal+"" );
														expresion.remove(j+1);
														expresion.remove(j-1);
														aux6 = aux6 - 2;
														contador++;
													}
												}
												 if (expresion.get(i+2).contains("Suma")){
													Resultadofinal =  Sumar(expresion.get(i+1), expresion.get(i+3));
													expresion2.set(i+2,"temp"+contador);
													opera.add(new Operaciones("+",expresion2.get(i+1),expresion2.get(i+3),expresion2.get(i+2)));
													expresion2.remove(i+3);
													expresion2.remove(i+1);
													expresion.set(i+1,Resultadofinal+"" );
													expresion.remove(i+2);
													expresion.remove(i+2);
													contador++;
												}
												 if (expresion.get(i+2).contains("Resta")){
													Resultadofinal =  Restar(expresion.get(i+1), expresion.get(i+3));
													expresion2.set(i+2,"temp"+contador);
													opera.add(new Operaciones("-",expresion2.get(i+1),expresion2.get(i+3),expresion2.get(i+2)));
													expresion2.remove(i+3);
													expresion2.remove(i+1);
													expresion.set(i+1,Resultadofinal+"" );
													expresion.remove(i+2);
													expresion.remove(i+2);
													contador++;
												}
												if(expresion.get(i+2).contains("ParCerrado"))	{
													expresion.remove(i+2);
													expresion.remove(i);
													expresion2.remove(i+2);
													expresion2.remove(i);
													banderaParentesis = true;
												}
											}
										}
									}
							}
								for (int i = 0; i < expresion.size(); i++) {
									if(expresion.get(i).contains("Multiplica") || expresion.get(i).contains("Divide")){
										if (expresion.get(i).contains("Multiplica")){
											Resultadofinal =  multiplicar(expresion.get(i-1), expresion.get(i+1));
											expresion2.set(i,"temp"+contador);
											opera.add(new Operaciones("*",expresion2.get(i-1),expresion2.get(i+1),expresion2.get(i)));
											expresion2.remove(i+1);
											expresion2.remove(i-1);
											expresion.set(i-1,Resultadofinal+"" );
											expresion.remove(i);
											expresion.remove(i);
											i--;
											contador++;
										}
										else if (expresion.get(i).contains("Divide")){
											Resultadofinal =  dividir(expresion.get(i-1), expresion.get(i+1));
											expresion2.set(i,"temp"+contador);
											opera.add(new Operaciones("/",expresion2.get(i-1),expresion2.get(i+1),expresion2.get(i)));
											expresion2.remove(i+1);
											expresion2.remove(i-1);
											expresion.set(i-1,Resultadofinal+"" );
											expresion.remove(i);
											expresion.remove(i);
											i--;
											contador++;
										}
									}
								}
								for (int i = 0; i < expresion.size(); i++) {
									if(expresion.get(i).contains("Suma") || expresion.get(i).contains("Resta")){
										if (expresion.get(i).contains("Suma")){
											Resultadofinal =  Sumar(expresion.get(i-1), expresion.get(i+1));
											expresion2.set(i,"temp"+contador);
											opera.add(new Operaciones("+",expresion2.get(i-1),expresion2.get(i+1),expresion2.get(i)));
											expresion2.remove(i+1);
											expresion2.remove(i-1);
											expresion.set(i-1,Resultadofinal+"" );
											expresion.remove(i);
											expresion.remove(i);
											i--;
											contador++;
										}
										else if (expresion.get(i).contains("Resta")){
											if(expresion.get(i).contains("Resta")){
												Resultadofinal =  Restar(expresion.get(i-1), expresion.get(i+1));
												expresion2.set(i,"temp"+contador);
												opera.add(new Operaciones("-",expresion2.get(i-1),expresion2.get(i+1),expresion2.get(i)));
												expresion2.remove(i+1);
												expresion2.remove(i-1);
												expresion.set(i-1,Resultadofinal+"" );
												expresion.remove(i);
												expresion.remove(i);
												i--;
												contador++;
											}
										}
									}
								}
								int Tipo;
								//int nombre;
								String auxTipo ="", auxNombre = "";
								
								while(nodoaux2!=null){
									Tipo = nodoaux2.anterior.dato.getTipo();
									System.out.println(Tipo);
									if(Tipo==2 ){
										auxTipo = nodoaux2.anterior.dato.getValor();
										auxNombre = nodoaux2.dato.getValor();
										break;
									}
									nodoaux2 = nodoaux2.anterior;
								}
								
							opera.add(new Operaciones("=",expresion2.get(0)," ",auxNombre));
							tablasimbolos.add(new TabladeSimbolos(auxNombre,Resultadofinal+"",auxTipo,"Local",to.getLinea()));							
							expresion.remove(0);
							expresion2.remove(0);
				

							 
							 
								
								
								
								
								
								
						 }
						
						 else if (nodo.anterior.anterior.anterior.dato.getTipo()==Token.IDENTIFICADOR
								 &&nodo.anterior.anterior.dato.getTipo()==Token.SIMBOLO
								 &&nodo.anterior.dato.getTipo()==Token.CONSTANTE)
							{
								for (int i = 0; i < tablasimbolos.size(); i++) {
									if(tablasimbolos.get(i).getNombre().contains(nodo.anterior.anterior.anterior.dato.getValor())){
										tablasimbolos.get(i).setValor(nodo.anterior.dato.getValor());
										banderita=true;
									}
								}
								if(!banderita){
									impresion.add("Error sintactico en posición "+to.getLinea()+ " se esperaba un Tipo de Dato correcto");
								}
							}
						 else if (nodo.anterior.anterior.anterior.dato.getTipo()==Token.IDENTIFICADOR
								 &&nodo.anterior.anterior.dato.getTipo()==Token.SIMBOLO
								 &&nodo.anterior.dato.getTipo()==Token.CONSTANTE)
							{
								for (int i = 0; i < tablasimbolos.size(); i++) {
									if(tablasimbolos.get(i).getNombre().contains(nodo.anterior.anterior.anterior.dato.getValor())){
										tablasimbolos.get(i).setValor(nodo.anterior.dato.getValor());
										banderita=true;
									}
								}
								if(!banderita){
									impresion.add("Error sintactico en posición "+to.getLinea()+ " se esperaba un Tipo de Dato correcto");
								}
							}
					} catch (Exception e){
						System.out.println(e.getMessage());
					}
				}
				break;
				case Token.CONSTANTE:
				if(nodo.anterior.dato.getValor().equals("="))
					if(nodo.siguiente.dato.getTipo()!=Token.OPERADOR_ARITMETICO&&!nodo.siguiente.dato.getValor().equals(";"))
						impresion.add("Error sintactico en posición "+to.getLinea()+ " asignacion no valida");
				break;
			case Token.PALABRA_RESERVADA:
				// verificar esructura de if
				if(to.getValor().equals("if"))
				{
					if(!nodo.siguiente.dato.getValor().equals("(")) {
						impresion.add("Error sintactico en posición "+to.getLinea()+ " se esperaba un (");
					}		
				}
				else 
				{
					// si es un else, buscar en los anteriores y si no hay un if ocurrira un error
					NodoDoble<Token> aux = nodo.anterior;
					boolean bandera=false;
					while(aux!=null&&!bandera) {
						if(aux.dato.getValor().equals("if"))
							bandera=true;
						aux =aux.anterior;
					}
					if(!bandera)
						impresion.add("Error sintactico en posición "+to.getLinea()+ " else no valido");
				}
				break;
			case Token.OPERADOR_LOGICO:
				// verificar que sea  'numero' + 'operador' + 'numero' 
				if (to.getValor().equals("==")){
					if (nodo.anterior.anterior.anterior.dato.getTipo()!=Token.PALABRA_RESERVADA){
						impresion.add("Error sintactico en la posición "+to.getLinea()+ " se esperaba una palabra reservada (if)");
					}					
					if (nodo.anterior.anterior.dato.getTipo()!=Token.SIMBOLO){
						impresion.add("Error sintactico en la posición "+to.getLinea()+ " se esperaba un simbolo");
					}
					if (!nodo.siguiente.siguiente.dato.getValor().contains(")")){
						impresion.add("Error sintactico en la posición "+to.getLinea()+ " se esperaba un simbolo");
					}
				}
				if(nodo.anterior.dato.getTipo()!=Token.CONSTANTE && nodo.anterior.dato.getTipo()!=Token.IDENTIFICADOR  ) 
					impresion.add("Error sintactico en posición "+to.getLinea()+ " se esperaba una Constante/Identificador");
				if(nodo.siguiente.dato.getTipo()!=Token.CONSTANTE && nodo.siguiente.dato.getTipo()!=Token.IDENTIFICADOR )
					impresion.add("Error sintactico en posición "+to.getLinea()+ " se esperaba una Constante/Identificador");				 
		
				 ////////05: VALIDAR OPERANDOS DE TIPOS COMPATIBLES///////////////////				
				/*
				String operando1,operando2;				
				operando1= TipoCadena(nodo.anterior.dato.getValor());
				operando2= TipoCadena(nodo.siguiente.dato.getValor());				
				if(!operando1.contains(operando2))
					impresion.add("Error semantico en posición "+to.getLinea()+ ", no coindicen los tipos de los operandos ("+operando1+"/"+operando2+")");
				break;
			case Token.OPERADOR_ARITMETICO:
				*/
			
				String operando1,operando2;
				if (nodo.anterior.dato.getTipo()==Token.IDENTIFICADOR)
				{
					String valor="";
					for (int i = 0; i < tablasimbolos.size(); i++) {
						if (tablasimbolos.get(i).getNombre().equals(nodo.anterior.dato.getValor()))
							valor = tablasimbolos.get(i).getValor();
					}
					operando1= TipoCadena(valor);
				}else
				operando1= TipoCadena(nodo.anterior.dato.getValor());
				
				if (nodo.siguiente.dato.getTipo()==Token.IDENTIFICADOR)
				{
					String valor="";
					for (int i = 0; i < tablasimbolos.size(); i++) {
						if (tablasimbolos.get(i).getNombre().equals(nodo.siguiente.dato.getValor()))
							valor = tablasimbolos.get(i).getValor();
					}
					operando2 = TipoCadena(valor);
				}else
				operando2 = TipoCadena(nodo.siguiente.dato.getValor());
				if(!operando1.contains(operando2)){
					impresion.add("Error semantico en la posición "+to.getLinea()+ ", no coinciden los tipos de los operandos ("+operando1+"/"+operando2+")");	
				}
				break;
			case Token.OPERADOR_ARITMETICO:
				String operando3,operando4;
				if (nodo.anterior.dato.getTipo()==Token.IDENTIFICADOR)
				{
					String valor="";
					for (int i = 0; i < tablasimbolos.size(); i++) {
						if (tablasimbolos.get(i).getNombre().equals(nodo.anterior.dato.getValor()))
							valor = tablasimbolos.get(i).getValor();
					}
					operando3= TipoCadena(valor);
				}else
				operando3= TipoCadena(nodo.anterior.dato.getValor());
				if(operando3.equals(""))
					operando3= "char";
				
				if (nodo.siguiente.dato.getTipo()==Token.IDENTIFICADOR)
				{
					String valor="";
					for (int i = 0; i < tablasimbolos.size(); i++) {
						if (tablasimbolos.get(i).getNombre().equals(nodo.siguiente.dato.getValor()))
							valor = tablasimbolos.get(i).getValor();
					}
					operando4 = TipoCadena(valor);

				}else
				operando4 = TipoCadena(nodo.siguiente.dato.getValor());
				if(!operando3.contains(operando4)){
					impresion.add("Error semantico en posición "+to.getLinea()+ ", no coinciden los tipos de los operandos ("+operando3+"/"+operando4+")");					
				}
				break;
			
			}
			}catch (Exception e){
				System.out.println(e.getMessage());
			}
			
			analisisSintactico(nodo.siguiente);
			return to;
		}
		return  vacio;// para no regresar null y evitar null pointer
	}
	
	
	 ////////02: VALIDAR LA ASIGNACIÓN A UNA VARIABLE///////////////////
	public  Token AnalizadorSemantico (NodoDoble<Token> nodo){
		//Token  to;
		if(nodo!=null) // si no llego al ultimo de la lista
		{
			//to =  nodo.dato;
			String aux;
			String aux2,auxiliarTipo = "";
			//int aux3;
			int renglon;
			for (int i = 0; i < tablasimbolos.size(); i++) {
				aux = tablasimbolos.get(i).tipo;
				renglon = tablasimbolos.get(i).getRenglon();
				
				if(aux.contains("int")){
					aux2=tablasimbolos.get(i).getValor();
					if(!aux2.isEmpty())
					auxiliarTipo =TipoCadena(aux2);
					
					if (EsNumeroEntero(aux2) == false && !aux2.isEmpty()) {
						impresion.add("Error Semantico en la posición "+renglon+ ", se recibió un "+auxiliarTipo+ " y se esperaba un INT");	
			        } 
				}
				else if(aux.contains("float")){					
					aux2=tablasimbolos.get(i).getValor();
					if(!aux2.isEmpty())
					auxiliarTipo =TipoCadena(aux2);				
					if (Esfloat(aux2) == false && !aux2.isEmpty()) {
						impresion.add("Error Semantico en la posición "+renglon+ ", se recibió un "+auxiliarTipo+ " y se esperaba un FLOAT");
			        } 
				}
				else if(aux.contains("char")){
					aux2=tablasimbolos.get(i).getValor();
					if(!aux2.isEmpty())
					auxiliarTipo =TipoCadena(aux2);					
					if (EsChar(aux2) == false && !aux2.isEmpty()) {
						impresion.add("Error Semantico en la posición "+renglon+ ", se recibió un "+auxiliarTipo+ " y se esperaba un CHAR");	
			        } 
				}else if(aux.contains("boolean")){
					aux2=tablasimbolos.get(i).getValor();
					if(!aux2.isEmpty())
					auxiliarTipo =TipoCadena(aux2);				
					if (EsBoolean(aux2) == false && !aux2.isEmpty() ) {
						impresion.add("Error Semantico en la posición "+renglon+ ", se recibió un "+auxiliarTipo+ " y se esperaba un BOOLEAN");	
			        } 
				}
			}
		}
		return vacio;
	}
	
	 ////////03: VALIDAR LAS VARIABLES USADAS Y NO DEFINIDAS///////////////////
	
	public Token Semantico2(NodoDoble<Token> nodo) {
		Token  to;
		if(nodo!=null) // si no llego al ultimo de la lista
		{
			to =  nodo.dato;
			if(to.getTipo()==Token.IDENTIFICADOR){
			String auxiliar = to.getValor();
			boolean bandera2 = false;
			for (int i = 0; i < tablasimbolos.size(); i++) {
			if(tablasimbolos.get(i).getNombre().contains(auxiliar)){
				bandera2=true;
				}
			}			
			if(!bandera2)
				impresion.add("Error semantico en posición "+to.getLinea()+ " se uso la variable "+auxiliar+" no está declarada");
		}
		Semantico2(nodo.siguiente);
		return to;
	}
		return vacio;
	}
	public Token VerificarClase(NodoDoble<Token> nodo) {
		Token  to;
		if(banderaclase){
			return vacio;
		}
		if(nodo!=null) // si no llego al ultimo de la lista
		{
			to =  nodo.dato;
			if(to.getTipo()==Token.CLASE){
				banderaclase= true;
			}
			VerificarClase(nodo.siguiente);
			return to;
		}
		return vacio;
	}

	public static boolean EsNumeroEntero(String cadena) {
        boolean resultado;
        try {
            Integer.parseInt(cadena);
            resultado = true;
        } catch (NumberFormatException excepcion) {
            resultado = false;
        }
        return resultado;
    }
	public static boolean Esfloat(String cadena) {
        boolean resultado;
        try {
            Float.parseFloat(cadena);
            resultado = true;
        } catch (NumberFormatException excepcion) {
            resultado = false;
        }
        return resultado;
    }
	public static boolean EsChar(String cadena) {
		if(Pattern.matches("'[a-zA-Z]'",cadena))
    	   return true;
    			   return false;
    }		
	public static boolean EsBoolean(String cadena) {
      if(cadena.contains("True")||cadena.contains("False"))
    	  return true;
      		return false;
    }
	public static String TipoCadena(String cadena) {
        String resultado = "";   	
        if(Pattern.matches("[0-9]+",cadena)){
        	resultado = "int";
        	return resultado;
        }        
        if(Pattern.matches("[0-9]+.[0-9]+",cadena)){
        	resultado = "float";
        }                
        if(cadena.contains("'[a-zA-Z]'")){
        	resultado = "char";
        }        
        if(cadena.contains("True")||cadena.contains("False")){
        	resultado = "boolean";
        }        
        return resultado;
    }
	// por si alguien escribe todo junto
	public String separaDelimitadores(String linea){
		for (String string : Arrays.asList("(",")","{","}","=",";")) {
			if(string.equals("=")) {
				if(linea.indexOf(">=")>=0) {
					linea = linea.replace(">=", " >= ");
					break;
				}
				if(linea.indexOf("<=")>=0) {
					linea = linea.replace("<=", " <= ");
					break;
				}
				if(linea.indexOf("==")>=0)
				{
					linea = linea.replace("==", " == ");
					break;
				}
			}
			if(linea.contains(string)) 
				linea = linea.replace(string, " "+string+" ");
		}
		return linea;
	}
	public int cuenta (String token) {		
		int conta=0;
		NodoDoble<Token> Aux=tokens.getInicio();
		while(Aux !=null){
			if(Aux.dato.getValor().equals(token))
				conta++;
			Aux=Aux.siguiente;
		}	
		return conta;
	}
	public ArrayList<String> getmistokens() {
		return impresion;
	}
	public int Sumar (String uno, String dos){		
		int suma =0;
		suma = suma+Integer.parseInt(uno) + Integer.parseInt(dos);
		return suma;
	}
	public int Restar (String uno, String dos){
		int Resta =0;
		Resta = Resta + Integer.parseInt(uno) - Integer.parseInt(dos);
		return Resta;
	}
	public int multiplicar (String uno, String dos){
		int multiplica =0;
		multiplica = multiplica + Integer.parseInt(uno) * Integer.parseInt(dos);
		return multiplica;
	}
	public int dividir (String uno, String dos){
		int divide =0;
		divide = divide + (int)( Integer.parseInt(uno) / Integer.parseInt(dos));
		return divide;
	}
}
