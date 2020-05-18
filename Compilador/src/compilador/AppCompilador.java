package compilador;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
public class AppCompilador extends JFrame implements ActionListener{
	// Componentes o Atributos
	private JMenuBar barraMenu;
	private JMenu menuArchivo;
	// Menu Archivo
	private JMenuItem itemNuevo,itemAbrir,itemGuardar,itemSalir,itemAnalisLexico;
	private JFileChooser ventanaArchivos;
	private File archivo;
	private JTextArea areaTexto;
	public NumeroLinea numLinea;
	private JScrollPane barrita; 
	private JList<String> tokens;
	private JTabbedPane documentos,consola,tabla,tabla2;
	private String [] titulos2 ={"Operador","Operando 1°","Operando 2°","Resultado"};
	DefaultTableModel modelo2 = new DefaultTableModel(new Object[0][0],titulos2);
	private String [] titulos ={"Tipo","Nombre","Valor","Alcance","Pocisión"};
	DefaultTableModel modelo = new DefaultTableModel(new Object[0][0],titulos);
	private JTable mitabla2 = new JTable(modelo2);
	private JTable mitabla = new JTable(modelo);
	private JButton btnAnalizar;
	
	public static void main(String[] args) {

		/*try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}*/
		new AppCompilador();
	}
	public AppCompilador() {
		super("Analizador Lexico y Sintáctico");
		System.out.println("Iniciando...");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setLayout(new GridLayout(2,2));
		setSize(1200,700);
		setLocationRelativeTo(null);
		creaInterFaz();
		setVisible(true);
	}
	private void creaInterFaz() {
		barraMenu = new JMenuBar();
		setJMenuBar(barraMenu);
		menuArchivo = new JMenu("Archivo");
		menuArchivo.setIcon(new ImageIcon("archivo.png"));
		//MenuAnalisis =  new JMenu("Analisis");
		//MenuAnalisis.setIcon(new ImageIcon("analisis.png"));
		ventanaArchivos = new JFileChooser();
		itemNuevo = new JMenuItem("Nuevo");
		itemAbrir = new JMenuItem("Abrir...");
		itemGuardar = new JMenuItem("Guardar...");
		itemSalir = new JMenuItem("Salir");
		itemSalir.addActionListener(this);
		itemGuardar.addActionListener(this);
		itemAbrir.addActionListener(this);
		itemNuevo.addActionListener(this);
		itemAnalisLexico  = new JMenuItem("Analizar codigo");
		itemAnalisLexico.addActionListener(this);
		btnAnalizar = new JButton("ANALIZAR");
		btnAnalizar.setFont(new Font("Dialog",Font.PLAIN,30));
		btnAnalizar.addActionListener(this);
		
		
		ventanaArchivos = new JFileChooser();
		menuArchivo.add(itemNuevo);
		menuArchivo.add(itemAbrir);
		menuArchivo.add(itemGuardar);
		menuArchivo.addSeparator();
		menuArchivo.add(itemSalir);
		//MenuAnalisis.add(itemAnalisLexico);
		barraMenu.add(menuArchivo);
		//barraMenu.add(MenuAnalisis);
		areaTexto = new JTextArea();
		ventanaArchivos= new JFileChooser("Guardar");
		areaTexto.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		NumeroLinea lineNumber = new NumeroLinea(areaTexto);
		barrita = new JScrollPane(areaTexto);
		barrita.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		barrita.setPreferredSize(new Dimension(870, 65));
		barrita.setRowHeaderView(lineNumber);
		
		documentos = new JTabbedPane();
		consola = new JTabbedPane();
		tabla = new JTabbedPane();
		tabla2 = new JTabbedPane();

		
		documentos.addTab("Nuevo", barrita);
		documentos.setToolTipText("Aqui se muestra el codigo");
		add(documentos);
		tokens=new JList<String>();
		consola.addTab("Consola",new JScrollPane(tokens));
		//consola.addTab("Tabla",new JScrollPane(mitabla));
		tabla2.addTab("Cuadruplos",new JScrollPane(mitabla2) );
		tabla.addTab("Tabla de simbolos",new JScrollPane(mitabla) );
		add(consola);
		consola.setToolTipText("Aqui se muestra el resultado del analisis");
		
		add(btnAnalizar);
		add (tabla2);
		add(tabla);
		//documentos.add("Analizar", btnAnalizar);

	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==btnAnalizar) {
			if(guardar()){
				Analisis analisador = new Analisis(archivo.getAbsolutePath());
				tokens.setListData(analisador.getmistokens().toArray( new String [0]));
				modelo = new DefaultTableModel(new Object[0][0],titulos);
				modelo2 = new DefaultTableModel(new Object[0][0],titulos2);
				
				
				mitabla.setModel(modelo);
				for (int i=0; i < analisador.getTabla().size(); i++) {
					TabladeSimbolos id = analisador.getTabla().get(i);
					if(!id.tipo.equals("")) {
						Object datostabla[]= {id.tipo,id.nombre,id.valor,id.alcance,id.renglon};
						modelo.addRow(datostabla);
					}
				}
				mitabla2.setModel(modelo2);
				for (int i=0; i < analisador.getTabla2().size(); i++) {
					Operaciones id2 =analisador.getTabla2().get(i);							
							Object datostabla2[]= {id2.operador,id2.argumento1,id2.argumento2,id2.resultado};
						modelo2.addRow(datostabla2);
						
						if(id2.operador.equals("=")){
							Object datostabla3[]= {" "," "," "," "," "};
							modelo2.addRow(datostabla3);
						}
						
				}
				
						
				
				
			}
		
			return;
		}
		if (e.getSource()==itemSalir) {
			System.exit(0);
			return;
		}
		if(e.getSource()==itemNuevo) {
			documentos.setTitleAt(0, "Nuevo");
			areaTexto.setText("");
			archivo=null;
			tokens.setListData(new String[0]);
			return;
		}
		if(e.getSource()==itemAbrir) {
			ventanaArchivos.setDialogTitle("Abrir..");
			ventanaArchivos.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(ventanaArchivos.showOpenDialog(this)==JFileChooser.CANCEL_OPTION) 
				return;
			archivo=ventanaArchivos.getSelectedFile();
			documentos.setTitleAt(0, archivo.getName());
			abrir();
		}
		if(e.getSource()==itemGuardar) {
			guardar();
		}
	}
	public boolean guardar() {
		try {
			if(archivo==null) {
				ventanaArchivos.setDialogTitle("Guardando..");
				ventanaArchivos.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(ventanaArchivos.showSaveDialog(this)==JFileChooser.CANCEL_OPTION) 
					return false;
				archivo=ventanaArchivos.getSelectedFile();
				documentos.setTitleAt(0, archivo.getName());
			}
			FileWriter fw = new FileWriter(archivo);
			BufferedWriter bf = new BufferedWriter(fw);
			bf.write(areaTexto.getText());
			bf.close();
			fw.close();
		}catch (Exception e) {
			System.out.println("Houston tenemos un problema?");
			return false;
		}
		return true;
	}
	public boolean abrir() {
		String texto="",linea;
		try {
			FileReader fr = new FileReader(archivo) ; 
			BufferedReader br= new BufferedReader(fr);
			while((linea=br.readLine())!=null) 
				texto+=linea+"\n";
			areaTexto.setText(texto);
			return true;
		}catch (Exception e) {
			archivo=null;
			JOptionPane.showMessageDialog(null, "Tipo de archivo incompatible", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}
}